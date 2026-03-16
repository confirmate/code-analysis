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
import de.fraunhofer.aisec.cpg.graph.OverlayNode
import de.fraunhofer.aisec.cpg.graph.codeAndLocationFrom
import de.fraunhofer.aisec.cpg.graph.concepts.ontology.GetCurrentTimeOperation
import de.fraunhofer.aisec.cpg.graph.concepts.ontology.Time
import de.fraunhofer.aisec.cpg.graph.declarations.Namespace
import de.fraunhofer.aisec.cpg.graph.expressions.MemberCall
import de.fraunhofer.aisec.cpg.graph.firstParentOrNull
import de.fraunhofer.aisec.cpg.graph.imports
import de.fraunhofer.aisec.cpg.passes.concepts.TaggingContext
import de.fraunhofer.aisec.cpg.passes.concepts.each
import de.fraunhofer.aisec.cpg.passes.concepts.propagate
import de.fraunhofer.aisec.cpg.passes.concepts.with
import de.fraunhofer.aisec.cpg.passes.concepts.withMultiple
import reactor.core.Exceptions.propagate

/** Tagging for Python datetime operations */
fun TaggingContext.tagTime() {
    // Tag datetime.now() calls
    each<MemberCall>(
            predicate = { it.name.localName == "now" && it.base?.name?.localName == "datetime" }
        )
        .withMultiple {
            val callNode = node
            val dateTimeImport =
                callNode.firstParentOrNull<Namespace>()?.imports?.firstOrNull {
                    it.name.localName == "datetime"
                }

            val results = mutableListOf<OverlayNode>()

            dateTimeImport?.let { importDecl ->
                propagate { importDecl }
                    .with {
                        val timeConcept = Time(underlyingNode = node)
                        results.add(
                            GetCurrentTimeOperation(
                                    linkedConcept = timeConcept,
                                    underlyingNode = callNode,
                                )
                                .apply {
                                    this.codeAndLocationFrom(callNode)
                                    this.name = Name(callNode.name.localName)
                                    this.nextDFG += callNode
                                }
                        )
                        timeConcept
                    }
            }

            results
        }
}
