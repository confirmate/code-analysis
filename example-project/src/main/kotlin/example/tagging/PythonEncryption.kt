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
import de.fraunhofer.aisec.cpg.graph.concepts.crypto.encryption.Cipher
import de.fraunhofer.aisec.cpg.graph.concepts.crypto.encryption.Encrypt
import de.fraunhofer.aisec.cpg.graph.concepts.crypto.encryption.Secret
import de.fraunhofer.aisec.cpg.graph.statements.expressions.MemberCallExpression
import de.fraunhofer.aisec.cpg.graph.statements.expressions.MemberExpression
import de.fraunhofer.aisec.cpg.passes.concepts.TaggingContext
import de.fraunhofer.aisec.cpg.passes.concepts.each
import de.fraunhofer.aisec.cpg.passes.concepts.propagate
import de.fraunhofer.aisec.cpg.passes.concepts.with
import de.fraunhofer.aisec.cpg.passes.concepts.withMultiple

/**
 * Tagging encryption operations in Python code that use the Fernet encryption library (from
 * cryptography.fernet). It creates:
 * - A [Cipher] concept representing the Fernet cipher
 * - A [Secret] concept for the encryption key
 * - An [Encrypt] operation
 */
fun TaggingContext.tagFernetEncrypt() {
    each<MemberCallExpression>(predicate = { it.name.localName == "encrypt" }).withMultiple {
        // Get the callee (MemberExpression: fernet.encrypt)
        val callee = node.callee as? MemberExpression
        val base = callee?.base

        if (callee != null && base != null) {
            val cipher =
                Cipher(underlyingNode = base).apply {
                    this.cipherName = "Fernet"
                    this.keySize = 256
                    this.blockSize = 128
                    this.codeAndLocationFrom(base)
                    this.name = Name("Fernet")
                }

            val secret =
                Secret(underlyingNode = base).apply {
                    this.codeAndLocationFrom(base)
                    this.name = Name("fernet_key")
                }

            propagate { base }.with { cipher }
            propagate { base }.with { secret }

            listOf(
                Encrypt(
                        underlyingNode = node,
                        concept = cipher,
                        key = secret,
                        plaintext = node.arguments.firstOrNull(),
                        ciphertext = node,
                    )
                    .apply {
                        this.codeAndLocationFrom(node)
                        this.name = Name(node.name.localName)
                        this.prevDFG += node
                        node.nextDFG.forEach { it.prevDFG += this }
                    }
            )
        } else {
            emptyList()
        }
    }
}

/** Main tagging function for Python encryption operations. */
fun TaggingContext.tagPythonEncryption() {
    tagFernetEncrypt()
}
