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
import de.fraunhofer.aisec.cpg.graph.declarations.FunctionDeclaration
import de.fraunhofer.aisec.cpg.graph.firstParentOrNull
import de.fraunhofer.aisec.cpg.graph.statements.expressions.MemberCallExpression
import de.fraunhofer.aisec.cpg.passes.concepts.TaggingContext
import de.fraunhofer.aisec.cpg.passes.concepts.each
import de.fraunhofer.aisec.cpg.passes.concepts.with

/** Tagging for Python logging */
fun TaggingContext.tagCriticalFunctionality() {
    each<MemberCallExpression> {
            it.name.localName == "get" &&
                it.base?.name?.localName == "data" &&
                it.firstParentOrNull<FunctionDeclaration> {
                    it.overlays.any { overlay ->
                        overlay is HttpEndpoint &&
                            listOf("login", "register").any { overlay.url?.endsWith(it) == true }
                    }
                } != null
        }
        .with { Identity() }
}
