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
import de.fraunhofer.aisec.cpg.graph.concepts.manualExtensions.TLS
import de.fraunhofer.aisec.cpg.graph.concepts.ontology.HttpClient
import de.fraunhofer.aisec.cpg.graph.concepts.ontology.HttpMethod
import de.fraunhofer.aisec.cpg.graph.concepts.ontology.HttpRequest
import de.fraunhofer.aisec.cpg.graph.evaluate
import de.fraunhofer.aisec.cpg.graph.statements.expressions.BinaryOperator
import de.fraunhofer.aisec.cpg.graph.statements.expressions.CallExpression
import de.fraunhofer.aisec.cpg.passes.concepts.TaggingContext
import de.fraunhofer.aisec.cpg.passes.concepts.each
import de.fraunhofer.aisec.cpg.passes.concepts.with

fun Node?.getUrl(): String? {
    var url = this?.evaluate() as? String
    if (url == null && this is BinaryOperator) {
        url = this.lhs.evaluate() as? String
    }
    return url
}

fun TaggingContext.tagHttpRequestsGet() {
    each<CallExpression>("requests.get").with {
        val urlArg = node.arguments.firstOrNull()
        val url = urlArg.getUrl()
        val httpClient =
            node.overlays.filterIsInstance<HttpClient>().singleOrNull()
                ?: HttpClient(
                    protocol =
                        if (url?.startsWith("https://") == true) {
                            TLS(underlyingNode = urlArg)
                        } else null,
                    isTLS = false,
                    underlyingNode = node,
                    authenticity = null,
                )

        HttpRequest(
                arguments =
                    if (node.arguments.size == 1) listOf()
                    else node.arguments.subList(1, node.arguments.size - 1),
                method = HttpMethod.GET,
                call = null,
                reqBody = null,
                httpEndpoint = null,
                linkedConcept = httpClient,
                underlyingNode = null, // Will be set by the pass
            )
            .apply {
                this.codeAndLocationFrom(node)
                this.name = Name(node.name.localName)
            }
    }
}

fun TaggingContext.tagHttpRequestsPost() {
    each<CallExpression>("requests.post").with {
        val urlArg = node.arguments.firstOrNull()
        val url = urlArg.getUrl()
        val httpClient =
            node.overlays.filterIsInstance<HttpClient>().singleOrNull()
                ?: HttpClient(
                    protocol =
                        if (url?.startsWith("https://") == true) {
                            TLS(underlyingNode = urlArg)
                        } else null,
                    isTLS = false,
                    underlyingNode = node,
                    authenticity = null,
                )

        HttpRequest(
                arguments =
                    if (node.arguments.size == 1) listOf()
                    else node.arguments.subList(1, node.arguments.size - 1),
                method = HttpMethod.POST,
                call = null,
                reqBody = null,
                httpEndpoint = null,
                linkedConcept = httpClient,
                underlyingNode = null, // Will be set by the pass
            )
            .apply {
                this.codeAndLocationFrom(node)
                this.name = Name(node.name.localName)
            }
    }
}

fun TaggingContext.tagHttpRequestsDelete() {
    each<CallExpression>("requests.delete").with {
        val urlArg = node.arguments.firstOrNull()
        val url = urlArg.getUrl()
        val httpClient =
            node.overlays.filterIsInstance<HttpClient>().singleOrNull()
                ?: HttpClient(
                    protocol =
                        if (url?.startsWith("https://") == true) {
                            TLS(underlyingNode = urlArg)
                        } else null,
                    isTLS = false,
                    underlyingNode = node,
                    authenticity = null,
                )

        HttpRequest(
                arguments =
                    if (node.arguments.size == 1) listOf()
                    else node.arguments.subList(1, node.arguments.size - 1),
                method = HttpMethod.DELETE,
                call = null,
                reqBody = null,
                httpEndpoint = null,
                linkedConcept = httpClient,
                underlyingNode = null, // Will be set by the pass
            )
            .apply {
                this.codeAndLocationFrom(node)
                this.name = Name(node.name.localName)
            }
    }
}

fun TaggingContext.tagHttpRequestsPut() {
    each<CallExpression>("requests.put").with {
        val urlArg = node.arguments.firstOrNull()
        val url = urlArg.getUrl()
        val httpClient =
            node.overlays.filterIsInstance<HttpClient>().singleOrNull()
                ?: HttpClient(
                    protocol =
                        if (url?.startsWith("https://") == true) {
                            TLS(underlyingNode = urlArg)
                        } else null,
                    isTLS = false,
                    underlyingNode = node,
                    authenticity = null,
                )

        HttpRequest(
                arguments =
                    if (node.arguments.size == 1) listOf()
                    else node.arguments.subList(1, node.arguments.size - 1),
                method = HttpMethod.PUT,
                call = null,
                reqBody = null,
                httpEndpoint = null,
                linkedConcept = httpClient,
                underlyingNode = null, // Will be set by the pass
            )
            .apply {
                this.codeAndLocationFrom(node)
                this.name = Name(node.name.localName)
            }
    }
}

fun TaggingContext.tagHttpRequests() {
    tagHttpRequestsGet()
    tagHttpRequestsPost()
    tagHttpRequestsDelete()
    tagHttpRequestsPut()
}
