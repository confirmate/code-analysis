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
package example.tagging.custom

import de.fraunhofer.aisec.cpg.graph.concepts.ontology.HttpEndpoint
import de.fraunhofer.aisec.cpg.graph.concepts.ontology.Identity
import de.fraunhofer.aisec.cpg.graph.declarations.Function
import de.fraunhofer.aisec.cpg.graph.concepts.ontology.PasswordBasedAuthentication
import de.fraunhofer.aisec.cpg.graph.firstParentOrNull
import de.fraunhofer.aisec.cpg.graph.statements.expressions.MemberCall
import de.fraunhofer.aisec.cpg.passes.concepts.TaggingContext
import de.fraunhofer.aisec.cpg.passes.concepts.each
import de.fraunhofer.aisec.cpg.passes.concepts.getOverlaysByPrevDFG
import de.fraunhofer.aisec.cpg.passes.concepts.propagate
import de.fraunhofer.aisec.cpg.passes.concepts.with
import de.fraunhofer.aisec.cpg.passes.concepts.withMultiple

/** Tagging for Python logging */
fun TaggingContext.tagCriticalFunctionality() {
    each<MemberCall> {
            it.name.localName == "get" &&
                it.base?.name?.localName == "data" &&
                it.firstParentOrNull<Function> {
                    it.overlays.any { overlay ->
                        overlay is HttpEndpoint &&
                            listOf("login", "register").any { overlay.url?.endsWith(it) == true }
                    }
                } != null
        }
        .with { Identity() }

    each<Function> { functionDeclaration ->
            // functionDeclaration.overlays.any { overlay -> overlay is HttpEndpoint } &&
            functionDeclaration.annotations.any { annotation ->
                annotation.name.localName == "token_required"
            } && functionDeclaration.parameters.isNotEmpty()
        }
        .withMultiple {
            val endpoints = node.getOverlaysByPrevDFG<HttpEndpoint>(this.state)
            if (endpoints.isNotEmpty()) {
                propagate { node }
                    .with {
                        val auth =
                            PasswordBasedAuthentication(
                                activated = true,
                                contextIsChecked = null,
                                rotationInterval = null,
                            )
                        for (endpoint in endpoints) {
                            endpoint.authenticity = auth
                        }
                        auth
                    }
                propagate { node.parameters[0] }.with { Identity() }
            }
            listOf()
        }
}
