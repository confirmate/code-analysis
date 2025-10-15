/*
 * This file is part of the Confirmate project.
 */
package example.tagging

import de.fraunhofer.aisec.confirmate.queries.HttpClientWithProtocol
import de.fraunhofer.aisec.confirmate.queries.HttpRequestWithArguments
import de.fraunhofer.aisec.cpg.graph.Name
import de.fraunhofer.aisec.cpg.graph.Node
import de.fraunhofer.aisec.cpg.graph.codeAndLocationFrom
import de.fraunhofer.aisec.cpg.graph.concepts.http.HttpMethod
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
        val httpClient =
            node.overlays.filterIsInstance<HttpClientWithProtocol>().singleOrNull()
                ?: HttpClientWithProtocol(
                    protocol = null,
                    underlyingNode = node,
                    authenticity = null,
                )

        HttpRequestWithArguments(
                arguments =
                    if (node.arguments.size == 1) listOf()
                    else node.arguments.subList(1, node.arguments.size - 1),
                httpMethod = HttpMethod.GET,
                url = node.arguments.firstOrNull().getUrl(),
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
        val httpClient =
            node.overlays.filterIsInstance<HttpClientWithProtocol>().singleOrNull()
                ?: HttpClientWithProtocol(
                    protocol = null,
                    underlyingNode = node,
                    authenticity = null,
                )

        HttpRequestWithArguments(
                arguments =
                    if (node.arguments.size == 1) listOf()
                    else node.arguments.subList(1, node.arguments.size - 1),
                httpMethod = HttpMethod.POST,
                url = node.arguments.firstOrNull().getUrl(),
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
        val httpClient =
            node.overlays.filterIsInstance<HttpClientWithProtocol>().singleOrNull()
                ?: HttpClientWithProtocol(
                    protocol = null,
                    underlyingNode = node,
                    authenticity = null,
                )

        HttpRequestWithArguments(
                arguments =
                    if (node.arguments.size == 1) listOf()
                    else node.arguments.subList(1, node.arguments.size - 1),
                httpMethod = HttpMethod.DELETE,
                url = node.arguments.firstOrNull().getUrl(),
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
        val httpClient =
            node.overlays.filterIsInstance<HttpClientWithProtocol>().singleOrNull()
                ?: HttpClientWithProtocol(
                    protocol = null,
                    underlyingNode = node,
                    authenticity = null,
                )

        HttpRequestWithArguments(
                arguments =
                    if (node.arguments.size == 1) listOf()
                    else node.arguments.subList(1, node.arguments.size - 1),
                httpMethod = HttpMethod.PUT,
                url = node.arguments.firstOrNull().getUrl(),
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
