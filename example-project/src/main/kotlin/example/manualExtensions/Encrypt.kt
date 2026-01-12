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
import de.fraunhofer.aisec.cpg.graph.concepts.ontology.Encryption
import de.fraunhofer.aisec.cpg.graph.concepts.ontology.EncryptionOperation
import de.fraunhofer.aisec.cpg.graph.concepts.ontology.Secret

/** Represents an encryption operation. */
class Encrypt(
    algorithm: String?,
    secret: Secret?,
    linkedConcept: Encryption,
    underlyingNode: Node?,
) :
    EncryptionOperation(
        algorithm = algorithm,
        secret = secret,
        linkedConcept = linkedConcept,
        underlyingNode = underlyingNode,
    )
