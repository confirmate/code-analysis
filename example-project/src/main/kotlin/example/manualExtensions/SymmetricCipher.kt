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
package de.fraunhofer.aisec.cpg.graph.concepts.manualExtensions

import de.fraunhofer.aisec.cpg.graph.Node
import de.fraunhofer.aisec.cpg.graph.concepts.ontology.Cipher
import de.fraunhofer.aisec.cpg.graph.concepts.ontology.Padding

/** Represents a symmetric cipher (e.g., AES, DES). */
class SymmetricCipher(
    blockSize: Int? = null,
    cipherName: String? = null,
    keySize: Int? = null,
    padding: Padding? = null,
    underlyingNode: Node? = null,
) :
    Cipher(
        blockSize = blockSize,
        cipherName = cipherName,
        keySize = keySize,
        padding = padding,
        underlyingNode = underlyingNode,
    )
