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
@file:OptIn(ExperimentalUuidApi::class)

import de.fraunhofer.aisec.codyze.catalogs.*
import de.fraunhofer.aisec.codyze.catalogs.german.*
import de.fraunhofer.aisec.confirmate.queries.catalogs.configs.DefaultConfig
import de.fraunhofer.aisec.confirmate.queries.catalogs.vulnerabilities.*
import de.fraunhofer.aisec.confirmate.queries.cra.*
import de.fraunhofer.aisec.cpg.graph.Node
import de.fraunhofer.aisec.cpg.graph.concepts.file.WriteFile
import de.fraunhofer.aisec.cpg.graph.concepts.ontology.*
import de.fraunhofer.aisec.cpg.passes.concepts.file.python.PythonFileConceptPass
import kotlin.collections.listOf
import kotlin.time.Duration.Companion.hours
import kotlin.uuid.ExperimentalUuidApi

include {
    Tagging from "tagging.codyze.kts"
    AssumptionDecisions from "assumptions.codyze.kts"
    Suppressions from "suppressions.codyze.kts"
}

project {
    name = "Password Manager"

    tool {
        configuration {
            registerPass<PythonFileConceptPass>()
            // Using pythonLogging tagging logic for now, since PythoNloggingConceptPass does not
            // set all required properties
            // registerPass<PythonLoggingConceptPass>()
        }
    }

    toe {
        name = "Password Manager"
        version = "1.0.0"

        architecture {
            modules {
                module("test-module") { directory = "src/main/resources/toe/vibe-pythonpass" }
            }
        }
    }

    val personalDataSources: (Node) -> Boolean = { node ->
        /* Let's say we have no personal data processed in this example project */
        false
    }

    val postponeUpdate: (Node) -> Boolean = { node ->
        /* Let's say we have no way to postpone automatic updates. */
        false
    }

    val notificationChannel: (Node) -> Boolean = { node ->
        /* Let's say we have no way to notify users about automatic updates. */
        false
    }

    val relevantActivities: (Node) -> Boolean = {
        it is FileOperation ||
            it is DatabaseOperation ||
            it is HttpClientOperation ||
            it is AuthenticationOperation
        /* TODO: No modifications to settings possible yet */
    }

    requirements {
        category("CRA") {
            requirement {
                name = "X.1.1.2"
                description =
                    "Products with digital elements shall be made available on the market without known exploitable vulnerabilities;"

                fulfilledBy {
                    with(BSI_TR02102()) {
                        OwaspTop10Scanner().scanAll().withMetricId("VulnerabilityScanReportUpdated")
                    }
                }
            }

            requirement {
                name = "X.1.1.3"
                description =
                    "Products with digital elements shall be made available on the market with a secure by default configuration, " +
                        "unless otherwise agreed between manufacturer and business user in relation to a tailor-made product " +
                        "with digital elements, including the possibility to reset the product to its original state;"

                fulfilledBy {
                    with(DefaultConfig()) {
                        listOf(
                                secureConfigAlwaysUsed()
                                    .withMetricId("SecureConfigurationEnforced"),
                                noNonConfigConstantsToSecureOperation()
                                    .withMetricId("SecureConfigurationEnforced"),
                                secureValuesConfigured().withMetricId("SecureConfigurationEnforced"),
                            )
                            .mergeWithAll()
                    }
                }
            }

            requirement {
                name = "X.1.1.4"
                description =
                    "Products with digital elements shall ensure that vulnerabilities can be addressed through security updates, including, where applicable, through automatic security updates that are installed within an appropriate timeframe enabled as a default setting, with a clear and easy-to-use opt-out mechanism, through the notification of available updates to users, and the option to temporarily postpone them;"

                fulfilledBy {
                    listOf(
                            // We want to check at least once per 720 hours. Says the security
                            // metrics repo:
                            // https://github.com/Cybersecurity-Certification-Hub/security-metrics/blob/main/metrics/EndpointSecurity/AutomaticUpdatesInterval/AutomaticUpdatesInterval.yml
                            updateIntervalSmallEnough(720.hours)
                                .withMetricId("AutomaticUpdatesInterval"),
                            updatesEnabled().withMetricId("AutomaticUpdatesEnabled"),
                            updateCanBePostponed(postponeUpdate)
                                .withMetricId("PostponeAlertOptionEnabled"),
                            notificationOfUpdates(notificationChannel)
                                .withMetricId("PostponeAlertOptionEnabled"),
                        )
                        .mergeWithAll()
                }
            }

            requirement {
                name = "X.1.1.5"
                description =
                    "Products with digital elements shall ensure protection from unauthorised access by appropriate control mechanisms, including but not limited to authentication, identity or access management systems, and report on possible unauthorised access;"

                fulfilledBy {
                    listOf(
                            authorizationAtEndpoint(::authorizationSelector)
                                .withMetricId("AnomalyDetectionEnabled"),
                            identityPasswordPolicyEnabled()
                                .withMetricId("IdentityPasswordPolicyEnabled"),
                            authorizationBeforeCriticalFunctionality(
                                    ::authorizationSelector,
                                    ::criticalSelector,
                                )
                                .withMetricId("AnomalyDetectionEnabled"),
                            authenticationAtEndpoint(::authenticationSelector)
                                .withMetricId("AnomalyDetectionEnabled"),
                            authenticationBeforeCriticalFunctionality(
                                    ::authenticationSelector,
                                    ::criticalSelector,
                                )
                                .withMetricId("AnomalyDetectionEnabled"),
                            loggingOnSecurityErrors(
                                    ::authenticationSelector,
                                    ::authorizationSelector,
                                )
                                .withMetricId("AnomalyDetectionOutput"),
                            adminAuthenticationWithMFA(::authenticationSelector)
                                .withMetricId("AdminMFAEnabled"),
                            identityPasswordPolicyEnabled()
                                .withMetricId("IdentityPasswordPolicyEnabled"),
                            anomalyDetectionEnabled().withMetricId("AnomalyDetectionEnabled"),
                            with(BSI_TR02102()) {
                                dataEncryptedBeforePersisting()
                                    .withMetricId("AtRestEncryptionEnabled")
                            },
                        )
                        .mergeWithAll()
                }
            }

            requirement {
                name = "X.1.1.6"
                description =
                    "Products with digital elements shall protect the confidentiality of stored, transmitted or otherwise processed data, personal or other, such as by encrypting relevant data at rest or in transit by state of the art mechanisms, and by using other technical means;"

                fulfilledBy {
                    with(BSI_TR02102()) {
                        listOf(
                                dataEncryptedBeforePersisting(
                                        isSensitiveData = {
                                            it.code !in
                                                listOf(
                                                    "data['username']",
                                                    "data['website']",
                                                    "user.id",
                                                    "Password()",
                                                    "User(username=username)",
                                                )
                                        },
                                        isPersistentSink = {
                                            it is WriteFile ||
                                                (it is DatabaseOperation &&
                                                    it.underlyingNode?.name?.localName != "commit")
                                        },
                                    )
                                    .withMetricId("AtRestEncryptionEnabled"),
                                dataInTransitEncrypted().withMetricId("TransportEncryptionEnabled"),
                                identityPasswordPolicyEnabled()
                                    .withMetricId("IdentityPasswordPolicyEnabled"),
                            )
                            .mergeWithAll()
                    }
                }
            }

            requirement {
                name = "X.1.1.7"
                description =
                    "Products with digital elements shall protect the integrity of stored, transmitted or otherwise processed data, personal or other, commands, programs and configuration against any manipulation or modification not authorised by the user, and report on corruptions;"

                fulfilledBy {
                    with(BSI_TR02102()) {
                        listOf(
                                secureProtocolsEnabled(
                                        {
                                            true /* Let's just say that all connections have to be secure. This should be fair nowadays. */
                                        },
                                        {
                                            false /* We do not know this, so let's fail to enforce a manual check. */
                                        },
                                    )
                                    .withMetricId("TransportEncryptionEnabled"),
                                identityPasswordPolicyEnabled()
                                    .withMetricId("IdentityPasswordPolicyEnabled"),
                            )
                            .mergeWithAll()
                    }
                }
            }

            requirement {
                name = "X.1.1.8"
                description =
                    "Products with digital elements shall process only data, personal or other, that are adequate, relevant and limited to what is necessary in relation to the intended purpose of the product with digital elements (data minimisation);"

                fulfilledBy {
                    listOf(
                            sensitiveDataHasSafeguards(
                                    isSensitiveData = personalDataSources,
                                    isStorageOperation = { n ->
                                        n is DatabaseOperation || n is WriteFile
                                    },
                                )
                                .withMetricId("DataMinimisationTechniquesEnabled"),
                            collectedDataIsProcessed(personalDataSources)
                                .withMetricId("DataMinimisationTechniquesEnabled"),
                            dbHasTTLConfigured().withMetricId("AutomatedDeletionOfDataEnabled"),
                            dbStoredDataIsRead(
                                    isWriteQuery = { query: DatabaseQuery ->
                                        "insert" in (query.calls ?: listOf<String>())
                                    },
                                    isReadQuery = { query: DatabaseQuery ->
                                        "select" in (query.calls ?: listOf<String>())
                                    },
                                )
                                .withMetricId("DataMinimisationTechniquesEnabled"),
                        )
                        .mergeWithAll()
                }
            }

            requirement {
                name = "X.1.1.9"
                description =
                    "Products with digital elements shall protect the availability of essential and basic functions, also after an incident, including through resilience and mitigation measures against denial-of-service attacks;"

                fulfilledBy {
                    (endpointsHaveRateLimiting() and endpointsHaveSizeLimiting())
                        .apply {
                            stringRepresentation =
                                if (value) "Basic DoS protection mechanisms are in place."
                                else "Basic DoS protection mechanisms are not in place."
                        }
                        .withMetricId("DDOSMechanismActivated")
                }
            }

            requirement {
                name = "X.1.1.10"
                description =
                    "Products with digital elements shall minimise the negative impact by the products themselves or connected devices on the availability of services provided by other devices or networks;"

                fulfilledBy { interfacesAreRequired().withMetricId("StandardProtocolsListed") }
            }

            requirement {
                name = "X.1.1.11"
                description =
                    "Products with digital elements shall be designed, developed and produced to limit attack surfaces, including external interfaces;"

                fulfilledBy {
                    interfaceHasRiskAssessment()
                        .withMetricId("RiskAssessmentMappingtoServicesandControl") and
                        interfacesAreRequired().withMetricId("StandardProtocolsListed")
                }
            }

            requirement {
                name = "X.1.1.12"
                description =
                    "Products with digital elements shall be designed, developed and produced to reduce the impact of an incident using appropriate exploitation mitigation mechanisms and techniques;"

                fulfilledBy { relevantDataFlowToBackup().withMetricId("BackupEnabled") }
            }

            requirement {
                name = "X.1.1.13"
                description =
                    " Products with digital elements shall provide security related information by recording and monitoring relevant internal activity, including the access to or modification of data, services or functions, with an opt-out mechanism for the user;"

                fulfilledBy {
                    listOf(
                            relevantActivityHasLogging({ relevantActivities(it) })
                                .withMetricId("ActivityLoggingEnabled"),
                            logEntriesHaveTimestamp(),
                            logEntriesContainInitiator().withMetricId("IdentityRecentActivity"),
                            loggingOptOut {
                                    false /* There's no opt-out mechanism we can identify. */
                                }
                                .withMetricId("LoggingOptOutMechanismAvailable"),
                            loggingEnabledByDefault(),
                            loggedDataAvailableToUser({ false }, { false })
                                .withMetricId("SecurityDataAvailableToUser"),
                        )
                        .mergeWithAll()
                }
            }

            requirement {
                name = "X.1.1.14"
                description =
                    "Products with digital elements shall provide the possibility for users to securely and easily remove on a permanent basis all data and settings and, where such data can be transferred to other products or systems, ensure that this is done in a secure manner."

                fulfilledBy {
                    with(BSI_TR02102()) { secureHttpRequests() and secureHttpResponses() } and
                        allowDeletionOfData(
                                trigger = { false },
                                getIdentity = { null },
                                dataSinks = sinksHoldingUserData({ personalDataSources(it) }),
                            )
                            .withMetricId("SecureDataDeletionMechanismActivated")
                }
            }
        }
    }
}
