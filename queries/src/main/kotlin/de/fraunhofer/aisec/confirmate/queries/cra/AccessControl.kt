/*
 * This file is part of the Confirmate project.
￼
￼
 */
package de.fraunhofer.aisec.confirmate.queries.cra

import de.fraunhofer.aisec.cpg.TranslationResult
import de.fraunhofer.aisec.cpg.assumptions.AssumptionType
import de.fraunhofer.aisec.cpg.assumptions.assume
import de.fraunhofer.aisec.cpg.graph.Forward
import de.fraunhofer.aisec.cpg.graph.GraphToFollow
import de.fraunhofer.aisec.cpg.graph.Interprocedural
import de.fraunhofer.aisec.cpg.graph.Node
import de.fraunhofer.aisec.cpg.graph.allChildrenWithOverlays
import de.fraunhofer.aisec.cpg.graph.collectAllNextEOGPaths
import de.fraunhofer.aisec.cpg.graph.concepts.file.WriteFile
import de.fraunhofer.aisec.cpg.graph.concepts.http.HttpEndpoint
import de.fraunhofer.aisec.cpg.query.GenericQueryOperators
import de.fraunhofer.aisec.cpg.query.Must
import de.fraunhofer.aisec.cpg.query.QueryTree
import de.fraunhofer.aisec.cpg.query.allExtended
import de.fraunhofer.aisec.cpg.query.executionPath

val endpoints = listOf(HttpEndpoint::class)

context(translationResult: TranslationResult)
fun authenticationAtEndpoint(
    isAuthentication: (Node) -> Boolean,
) = getEndpoints().all { endpoint ->
    executionPath(
        startNode = endpoint,
        direction = Forward(GraphToFollow.EOG),
        type = Must,
        scope = Interprocedural(),
    ) {
        isAuthentication(it)
    }
}


context(translationResult: TranslationResult)
fun authorizationAtEndpoint(
    isAuthorization: (Node) -> Boolean,
) = getEndpoints().all { endpoint ->
    executionPath(
        startNode = endpoint,
        direction = Forward(GraphToFollow.EOG),
        type = Must,
        scope = Interprocedural(),
    ) {
        isAuthorization(it)
    }
}

fun authenticationBeforeCriticalFunctionality(): QueryTree<Boolean> {
    // Todo getCriticalFunctionality
}

fun authorizationBeforeCriticalFunctionality(): QueryTree<Boolean> {
    // Todo getCriticalFunctionality
}

fun loggingOnSecurityErrors(): QueryTree<Boolean> {
    // Todo make a Union of  authenticationErrors and authorizationErrors
}


fun authenticationErrors(): QueryTree<Boolean> {

}


fun authorizationErrors(): QueryTree<Boolean> {

}

context(translationResult: TranslationResult)
fun getEndpoints(): List<Node> {
    return translationResult.allChildrenWithOverlays<Node>({ node -> endpoints.any {it -> it.isInstance(node) }  })
}

context(translationResult: TranslationResult)
fun getCriticalFunctionality(isCritical: (Node) -> Boolean): List<Node> {
    return translationResult.allChildrenWithOverlays<Node>(isCritical)
}