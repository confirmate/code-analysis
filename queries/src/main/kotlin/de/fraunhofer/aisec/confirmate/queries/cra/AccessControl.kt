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
package de.fraunhofer.aisec.confirmate.queries.cra

import de.fraunhofer.aisec.cpg.TranslationResult
import de.fraunhofer.aisec.cpg.assumptions.AssumptionType
import de.fraunhofer.aisec.cpg.assumptions.assume
import de.fraunhofer.aisec.cpg.graph.Forward
import de.fraunhofer.aisec.cpg.graph.GraphToFollow
import de.fraunhofer.aisec.cpg.graph.Interprocedural
import de.fraunhofer.aisec.cpg.graph.Node
import de.fraunhofer.aisec.cpg.graph.allChildrenWithOverlays
import de.fraunhofer.aisec.cpg.graph.concepts.ontology.*
import de.fraunhofer.aisec.cpg.query.GenericQueryOperators
import de.fraunhofer.aisec.cpg.query.Must
import de.fraunhofer.aisec.cpg.query.QueryTree
import de.fraunhofer.aisec.cpg.query.allExtended
import de.fraunhofer.aisec.cpg.query.executionPath
import de.fraunhofer.aisec.cpg.query.mergeWithAll
import de.fraunhofer.aisec.cpg.query.or

val endpoints = listOf(HttpEndpoint::class)

fun criticalSelector(node: Node): Boolean {

    return node is DatabaseOperation ||
        node is FileOperation ||
        node is HttpClientOperation ||
        node is ConfigurationOperation ||
        node is BlockStorageOperation ||
        node is CreateEncryptedDisk ||
        node is DDoSProtection ||
        node is CryptographicOperation ||
        node is DiskEncryptionOperation ||
        node is DynamicLoadingOperation ||
        node is SecretOperation ||
        node is HttpEndpointOperation ||
        node is MemoryOperation
}

fun authenticationSelector(node: Node): Boolean {
    val authKeywords = listOf("auth", "login", "signin", "sign-in", "sign_in")
    return authKeywords.any { node.name.toString().lowercase().contains(it) } ||
        node is AuthenticationOperation ||
        node is TokenBasedAuthentication ||
        node is PasswordBasedAuthentication ||
        node is OTPBasedAuthentication
}

fun authorizationSelector(node: Node): Boolean {
    val authorizationKeywords =
        listOf("access", "permit", "privilege", "role", "authz", "authoriz", "token")
    return authorizationKeywords.any { node.name.toString().lowercase().contains(it) } ||
        node is AccessRestriction ||
        node is Authorization ||
        node is AuthorizeJwt ||
        node is CheckAccess
}

context(translationResult: TranslationResult)
fun authenticationAtEndpoint(isAuthentication: (Node) -> Boolean) =
    getEndpoints()
        .map { endpoint ->
            val hasAuth =
                QueryTree(
                    value = endpoint.authenticity != null,
                    stringRepresentation =
                        if (endpoint.authenticity != null)
                            "Endpoint ${endpoint.name} has an authenticity concept assigned"
                        else "Endpoint ${endpoint.name} has no authenticity concept assigned",
                    node = endpoint,
                    operator = GenericQueryOperators.EVALUATE,
                )

            val hasEOG =
                executionPath(
                        startNode = endpoint,
                        direction = Forward(GraphToFollow.EOG),
                        type = Must,
                        scope = Interprocedural(),
                    ) {
                        isAuthentication(it)
                    }
                    .apply {
                        stringRepresentation =
                            if (value)
                                "There is an authentication check on the path from endpoint ${endpoint.name}"
                            else
                                "No authentication check on the path from endpoint ${endpoint.name}"
                    }
            (hasAuth or hasEOG).apply {
                stringRepresentation =
                    if (value) "There endpoint ${endpoint.name} is authenticated."
                    else "There endpoint ${endpoint.name} is not authenticated."
                checkForSuppression()
            }
        }
        .mergeWithAll()
        .apply {
            stringRepresentation =
                if (value) "All endpoints have an authentication check on their path"
                else "Some endpoints are missing an authentication check on their path"
        }

context(translationResult: TranslationResult)
fun authorizationAtEndpoint(isAuthorization: (Node) -> Boolean) =
    getEndpoints()
        .map { endpoint ->
            executionPath(
                    startNode = endpoint,
                    direction = Forward(GraphToFollow.EOG),
                    type = Must,
                    scope = Interprocedural(),
                ) {
                    isAuthorization(it)
                }
                .apply {
                    stringRepresentation =
                        if (value)
                            "There is an authorization check on the path from endpoint ${endpoint.name}"
                        else "No authorization check on the path from endpoint ${endpoint.name}"
                }
        }
        .mergeWithAll()
        .apply {
            stringRepresentation =
                if (value) "All endpoints have an authorization check on their path"
                else "Some endpoints are missing an authorization check on their path"
        }

context(translationResult: TranslationResult)
fun authenticationBeforeCriticalFunctionality(
    isAuthentication: (Node) -> Boolean,
    isCritical: (Node) -> Boolean,
) =
    getCriticalFunctionality(isCritical)
        .map { criticalFunctionality ->
            executionPath(
                    startNode = criticalFunctionality,
                    direction = Forward(GraphToFollow.EOG),
                    type = Must,
                    scope = Interprocedural(),
                ) {
                    isAuthentication(it)
                }
                .apply {
                    stringRepresentation =
                        if (value)
                            "There is an authentication check before critical functionality ${criticalFunctionality.name}"
                        else
                            "No authentication check critical functionality ${criticalFunctionality.name}"
                }
        }
        .mergeWithAll()
        .apply {
            stringRepresentation =
                if (value) "All critical functionality has a prior authentication check."
                else "Some critical functionalities do not have a prior authentication check."
        }

context(translationResult: TranslationResult)
fun authorizationBeforeCriticalFunctionality(
    isAuthorization: (Node) -> Boolean,
    isCritical: (Node) -> Boolean,
): QueryTree<Boolean> =
    getCriticalFunctionality(isCritical)
        .map { criticalFunctionality ->
            executionPath(
                    startNode = criticalFunctionality,
                    direction = Forward(GraphToFollow.EOG),
                    type = Must,
                    scope = Interprocedural(),
                ) {
                    isAuthorization(it)
                }
                .apply {
                    stringRepresentation =
                        if (value)
                            "There is an authorization check before critical functionality ${criticalFunctionality.name}"
                        else
                            "No authorization check critical functionality ${criticalFunctionality.name}"
                }
        }
        .mergeWithAll()
        .apply {
            stringRepresentation =
                if (value) "All critical functionality has a prior authorization."
                else "Some critical functionalities do not have a prior authorization."
        }

/**
 * Currently this check evaluates to true if there is any logging reachable from the negative branch
 * of a node, considered to be evaluation. This could be refined, although we expect to not have
 * merging baths of a successful and unsuccessful authentication/authorization.
 */
context(translationResult: TranslationResult)
fun loggingOnSecurityErrors(
    isAuthentication: (Node) -> Boolean,
    isAuthorization: (Node) -> Boolean,
): QueryTree<Boolean> {
    val errorNodes =
        getSecurityErrors(getAuthentication(isAuthentication))
            .union(getSecurityErrors(getAuthorization(isAuthorization)))

    return errorNodes
        .map { errorNode ->
            executionPath(
                    startNode = errorNode,
                    direction = Forward(GraphToFollow.EOG),
                    type = Must,
                    scope = Interprocedural(),
                ) { node ->
                    node is LogWrite && node.isLevelEnabled
                }
                .apply {
                    stringRepresentation =
                        if (value)
                            "There is a logging operation on the error path from ${errorNode.name}"
                        else "No logging operation on the error path from ${errorNode.name}"
                }
        }
        .mergeWithAll()
        .apply {
            stringRepresentation =
                if (value) "All errors of authentications or authorizations are logged."
                else "Some errors of authentications or authorizations are not logged."
        }
}

context(translationResult: TranslationResult)
fun adminAuthenticationWithMFA(
    isAdminEndpoint: (Node) -> Boolean = { n -> n.name.toString().lowercase().contains("admin") }
): QueryTree<Boolean> {
    return translationResult
        .allExtended<HttpEndpoint>(
            { n -> isAdminEndpoint(n) },
            {
                executionPath(
                    startNode = it,
                    direction = Forward(GraphToFollow.DFG),
                    type = Must,
                    earlyTermination = { n ->
                        n is AuthenticationOperation && n.concept !is MultiFactorAuthentiation
                    },
                    scope = Interprocedural(),
                    predicate = { n ->
                        n is AuthenticationOperation && n.concept is MultiFactorAuthentiation
                    },
                )
            },
        )
        .apply {
            if (children.isEmpty()) {
                value = true
                stringRepresentation = "No admin endpoints found"
                return this
            }
            stringRepresentation =
                if (value) {
                    "All admin endpoints have authentication with MFA"
                } else {
                    "Some admin endpoints are missing authentication with MFA"
                }
        }
}

context(translationResult: TranslationResult)
fun identityPasswordPolicyEnabled(): QueryTree<Boolean> {
    return translationResult
        .allExtended<Identity> { node ->
            val hasPolicy = node.disablePasswordPolicy == false
            QueryTree(
                value = hasPolicy,
                stringRepresentation =
                    if (hasPolicy) {
                        "Identity ${node.name} has password policy enabled"
                    } else {
                        "Identity ${node.name} has NO password policy enabled (disabled or missing)"
                    },
                node = node,
                operator = GenericQueryOperators.EVALUATE,
            )
        }
        .apply {
            stringRepresentation =
                if (value) {
                    "All identities have password policy enabled"
                } else {
                    "Some identities have NO password policy enabled (disabled or missing)"
                }
        }
}

context(translationResult: TranslationResult)
fun anomalyDetectionEnabled(): QueryTree<Boolean> {
    return translationResult
        .allExtended<AnomalyDetection> { node ->
            QueryTree<Boolean>(
                value = node.enabled == true,
                stringRepresentation = "Anomaly detection ${node.name} is enabled",
                node = node,
                operator = GenericQueryOperators.EVALUATE,
            )
        }
        .apply {
            if (children.isEmpty()) {
                value = true
                stringRepresentation = "No anomaly detection concepts found"
                this.assume(
                    AssumptionType.ExternalDataAssumption,
                    "No anomaly detection concepts found, assuming they are implemented by the ecosystem.",
                )
                return this
            }
            stringRepresentation =
                if (value) {
                    "All anomaly detection concepts are enabled"
                } else {
                    "Some anomaly detection concepts are disabled"
                }
        }
}

context(translationResult: TranslationResult)
fun getAuthentication(isAuthentication: (Node) -> Boolean) =
    translationResult.allChildrenWithOverlays<Node>(isAuthentication)

context(translationResult: TranslationResult)
fun getAuthorization(isAuthorization: (Node) -> Boolean) =
    translationResult.allChildrenWithOverlays<Node>(isAuthorization)

/**
 * This function return the first node after a branch that leads to a path that will be executed
 * after an evaluation to `false`. This can be used to build a further paths for inspection.
 */
context(translationResult: TranslationResult)
fun getSecurityErrors(securityChecks: List<Node>) =
    securityChecks.flatMap { it.nextEOGEdges.filter { !(it.branch ?: true) }.map { it.end } }

context(translationResult: TranslationResult)
fun getEndpoints(): List<HttpEndpoint> {
    return translationResult.allChildrenWithOverlays<HttpEndpoint>({ node ->
        endpoints.any { it.isInstance(node) }
    })
}

context(translationResult: TranslationResult)
fun getCriticalFunctionality(isCritical: (Node) -> Boolean): List<Node> {
    return translationResult.allChildrenWithOverlays<Node>(isCritical)
}
