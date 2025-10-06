/*
 * This file is part of the Confirmate project.
￼
￼
 */
package de.fraunhofer.aisec.confirmate.queries.cra

import de.fraunhofer.aisec.confirmate.queries.LogWriteWithArguments
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
import de.fraunhofer.aisec.cpg.graph.concepts.logging.LogWrite
import de.fraunhofer.aisec.cpg.query.GenericQueryOperators
import de.fraunhofer.aisec.cpg.query.Must
import de.fraunhofer.aisec.cpg.query.QueryTree
import de.fraunhofer.aisec.cpg.query.allExtended
import de.fraunhofer.aisec.cpg.query.executionPath
import de.fraunhofer.aisec.cpg.query.mergeWithAll

val endpoints = listOf(HttpEndpoint::class)

context(translationResult: TranslationResult)
fun authenticationAtEndpoint(
    isAuthentication: (Node) -> Boolean,
) = getEndpoints().map { endpoint ->
    executionPath(
        startNode = endpoint,
        direction = Forward(GraphToFollow.EOG),
        type = Must,
        scope = Interprocedural(),
    ) {
        isAuthentication(it)
    }
}.mergeWithAll()


context(translationResult: TranslationResult)
fun authorizationAtEndpoint(
    isAuthorization: (Node) -> Boolean,
) = getEndpoints().map { endpoint ->
    executionPath(
        startNode = endpoint,
        direction = Forward(GraphToFollow.EOG),
        type = Must,
        scope = Interprocedural(),
    ) {
        isAuthorization(it)
    }
}.mergeWithAll()

context(translationResult: TranslationResult)
fun authenticationBeforeCriticalFunctionality(
    isAuthentication: (Node) -> Boolean,
isCritical: (Node) -> Boolean) = getCriticalFunctionality(isCritical).map { endpoint ->
    executionPath(
        startNode = endpoint,
        direction = Forward(GraphToFollow.EOG),
        type = Must,
        scope = Interprocedural(),
    ) {
        isAuthentication(it)
    }
}.mergeWithAll()


context(translationResult: TranslationResult)
fun authorizationBeforeCriticalFunctionality(
    isAuthorization: (Node) -> Boolean,
    isCritical: (Node) -> Boolean
): QueryTree<Boolean>  = getCriticalFunctionality(isCritical).map { endpoint ->
        executionPath(
            startNode = endpoint,
            direction = Forward(GraphToFollow.EOG),
            type = Must,
            scope = Interprocedural(),
        ) {
            isAuthorization(it)
        }
    }.mergeWithAll()

/**
 * Currently this check evaluates to true if there is any logging reachable from the negative branch of a node,
 * considered to be evaluation. This could be refined, although we expect to not have merging baths of a successful
 * and unsuccessful authentication/authorization.
 */
context(translationResult: TranslationResult)
fun loggingOnSecurityErrors(
    isAuthentication: (Node) -> Boolean,
    isAuthorization: (Node) -> Boolean,
    ): QueryTree<Boolean> {
    val errorNodes = getSecurityErrors(getAuthentication(isAuthentication)).union(getSecurityErrors(getAuthorization(isAuthorization)))


    return errorNodes.map {errorNode -> executionPath(
        startNode = errorNode,
        direction = Forward(GraphToFollow.EOG),
        type = Must,
        scope = Interprocedural(),
    ) { node -> node is LogWriteWithArguments && node.isLevelEnabled }

}.mergeWithAll()
}

context(translationResult: TranslationResult)
fun getAuthentication(    isAuthentication: (Node) -> Boolean,) = translationResult.allChildrenWithOverlays<Node>(isAuthentication)


context(translationResult: TranslationResult)
fun getAuthorization(    isAuthorization: (Node) -> Boolean,) = translationResult.allChildrenWithOverlays<Node>(isAuthorization)

/**
 * This function return the first node after a branch that leads to a path that will be executed after an evaluation to `false`.
 * This can be used to build a further paths for inspection.
 */
context(translationResult: TranslationResult)
fun getSecurityErrors( securityChecks: List<Node>) = securityChecks.flatMap { it.nextEOGEdges.filter { !(it.branch?: true) }.map { it.end } }


context(translationResult: TranslationResult)
fun getEndpoints(): List<Node> {
    return translationResult.allChildrenWithOverlays<Node>({ node -> endpoints.any {it -> it.isInstance(node) }  })
}

context(translationResult: TranslationResult)
fun getCriticalFunctionality(isCritical: (Node) -> Boolean): List<Node> {
    return translationResult.allChildrenWithOverlays<Node>(isCritical)
}