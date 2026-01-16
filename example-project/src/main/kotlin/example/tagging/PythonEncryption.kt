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
import de.fraunhofer.aisec.cpg.graph.codeAndLocationFrom
import de.fraunhofer.aisec.cpg.graph.concepts.ontology.*
import de.fraunhofer.aisec.cpg.graph.statements.expressions.CallExpression
import de.fraunhofer.aisec.cpg.graph.statements.expressions.MemberCallExpression
import de.fraunhofer.aisec.cpg.passes.concepts.TaggingContext
import de.fraunhofer.aisec.cpg.passes.concepts.each
import de.fraunhofer.aisec.cpg.passes.concepts.with
import de.fraunhofer.aisec.cpg.passes.concepts.withMultiple

fun TaggingContext.tagEncryption() {
    each<CallExpression>("Fernet").with {
        val cipher =
            SymmetricCipher(
                    authTagSize = null,
                    modus = "CBC",
                    initializationVector = null,
                    blockSize = 128,
                    cipherName = "AES-128-CBC",
                    keySize = 256,
                    padding = null,
                    underlyingNode = node,
                )
                .apply {
                    this.codeAndLocationFrom(node)
                    this.name = Name(node.name.localName)
                }

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
                ?: // Fallback: create a cipher if it doesn't exist
                SymmetricCipher(
                        authTagSize = null,
                        modus = "CBC",
                        initializationVector = null,
                        blockSize = 128,
                        cipherName = "AES-128-CBC",
                        keySize = 256,
                        padding = null,
                        underlyingNode = node,
                    )
                    .apply { this.codeAndLocationFrom(node) }
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
                    this.nextDFG += node
                }
        )
    }
}

fun TaggingContext.tagPythonEncryption() {
    tagEncryption()
    tagEncryptionOperation()
}
