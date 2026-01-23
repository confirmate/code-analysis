/*
 * This file is part of the Confirmate project.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */
package example.tagging

import de.fraunhofer.aisec.cpg.graph.Name
import de.fraunhofer.aisec.cpg.graph.Node
import de.fraunhofer.aisec.cpg.graph.codeAndLocationFrom
import de.fraunhofer.aisec.cpg.graph.concepts.manualExtensions.HashFunction
import de.fraunhofer.aisec.cpg.graph.concepts.manualExtensions.RNG
import de.fraunhofer.aisec.cpg.graph.concepts.manualExtensions.RngGet
import de.fraunhofer.aisec.cpg.graph.concepts.ontology.*
import de.fraunhofer.aisec.cpg.graph.edges.flows.insertNodeAfterwardInDFGPath
import de.fraunhofer.aisec.cpg.graph.statements.expressions.CallExpression
import de.fraunhofer.aisec.cpg.graph.statements.expressions.MemberCallExpression
import de.fraunhofer.aisec.cpg.passes.concepts.TaggingContext
import de.fraunhofer.aisec.cpg.passes.concepts.each
import de.fraunhofer.aisec.cpg.passes.concepts.getOverlaysByPrevDFG
import de.fraunhofer.aisec.cpg.passes.concepts.with
import de.fraunhofer.aisec.cpg.passes.concepts.withMultiple

fun FernetCipher(node: Node) =
    SymmetricCipher(
            authTagSize = null,
            modus = "CBC",
            initializationVector =
                InitializationVector(size = 128, underlyingNode = null).apply {
                    this.isImplicit = true
                    this.prevDFG.add(
                        RngGet(rng = RNG(underlyingNode = null), underlyingNode = null).apply {
                            this.isImplicit = true
                        }
                    )
                },
            blockSize = 128,
            cipherName = "AES-128-CBC",
            keySize = 128,
            padding = Padding("PKCS7", underlyingNode = null).apply { this.isImplicit = true },
            underlyingNode = node,
        )
        .apply {
            this.codeAndLocationFrom(node)
            this.name = Name(node.name.localName)
        }

fun TaggingContext.tagEncryption() {
    each<CallExpression>("Fernet").with {
        val cipher = FernetCipher(node)

        Encryption(basedOn = cipher, secret = null, underlyingNode = node).apply {
            this.codeAndLocationFrom(node)
        }
    }
}

/** Tagging for Fernet encrypt calls */
fun TaggingContext.tagEncryptionOperation() {
    each<MemberCallExpression>(predicate = { it.name.localName == "encrypt" }).withMultiple {
        val cipher =
            node.overlays.filterIsInstance<Encryption>().singleOrNull()?.basedOn
                ?: FernetCipher(node) // Fallback: create a cipher if it doesn't exist
        listOf(
            Encrypt(
                    algorithm = "Fernet",
                    secret = null,
                    linkedConcept = cipher,
                    underlyingNode = node,
                )
                .apply {
                    this.codeAndLocationFrom(node)
                    this.name = Name(node.name.localName)
                    node.insertNodeAfterwardInDFGPath(this)
                    // this.nextDFG += node
                }
        )
    }
}

/** Tagging for Fernet hashlib calls */
fun TaggingContext.tagHashlib() {
    each<CallExpression>(predicate = { it.name.toString() == "hashlib.md5" }).withMultiple {
        val hashFunction =
            node.getOverlaysByPrevDFG<HashFunction>(this.state).singleOrNull()
                ?: HashFunction(hashFunctionName = "MD5", outputSize = 128, node).apply {
                    this.codeAndLocationFrom(node)
                    this.name = Name(node.name.localName)
                }
        val confidentiality =
            node.getOverlaysByPrevDFG<Confidentiality>(this.state).singleOrNull()
                ?: Confidentiality(underlyingNode = node)

        listOf(
            hashFunction,
            confidentiality,
            CryptographicHash(
                    algorithm = hashFunction.hashFunctionName,
                    hashFunction = hashFunction,
                    usesSalt = null,
                    linkedConcept = confidentiality,
                    underlyingNode = node,
                )
                .apply {
                    this.codeAndLocationFrom(node)
                    this.name = Name(node.name.localName)
                },
        )
    }
}

fun TaggingContext.tagPythonEncryption() {
    tagEncryption()
    tagEncryptionOperation()
    tagHashlib()
}
