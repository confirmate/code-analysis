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
package de.fraunhofer.aisec.confirmate.integration

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.databind.module.SimpleModule
import de.fraunhofer.aisec.codyze.AnalysisResult
import io.clouditor.api.EvidenceStoreApi
import io.clouditor.api.OrchestratorApi
import io.clouditor.model.Evidence
import io.ktor.client.*
import io.ktor.client.HttpClientConfig
import io.ktor.client.call.*
import io.ktor.client.plugins.auth.*
import io.ktor.client.plugins.auth.providers.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.runBlocking
import org.openapitools.client.infrastructure.ApiClient.Companion.JSON_DEFAULT
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class ClouditorClient {
    val log: Logger = LoggerFactory.getLogger(ClouditorClient::class.java)

    val orchApi: OrchestratorApi
    val evidenceApi: EvidenceStoreApi

    suspend fun oauthToken(clientID: String, clientSecret: String): String? {
        val client = HttpClient { install(ContentNegotiation) { json() } }

        val response =
            client.submitForm(
                System.getenv("AUTH_TOKEN_ENDPOINT") ?: "http://localhost:8080/v1/auth/token",
                formParameters = parameters { append("grant_type", "client_credentials") },
            ) {
                basicAuth(clientID, clientSecret)
            }

        if (!response.status.isSuccess()) {
            log.error(
                "OAuth token request failed with status ${response.status.value}: ${response.status.description}"
            )
            return null
        }

        val map = response.body<HashMap<String, String>>()

        return map["access_token"]
    }

    init {
        val token =
            runBlocking {
                oauthToken(
                    System.getenv("AUTH_CLIENT_ID") ?: "confirmate",
                    System.getenv("AUTH_CLIENT_SECRET") ?: "confirmate",
                )
            }
                ?: run {
                    log.error("No OAuth token retrieved, requests will be unauthenticated")
                    throw RuntimeException("No OAuth token retrieved")
                }

        val config: (HttpClientConfig<*>) -> Unit = {
            it.install(Auth) { bearer { loadTokens { BearerTokens(token, "") } } }
            it.install(ContentNegotiation) {
                /*json(json = Json{
                    serializersModule = module
                })*/
            }
        }

        val jsonBlock: ObjectMapper.() -> Unit = {
            JSON_DEFAULT(this)

            val module = SimpleModule()
            registerModule(module)
            disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
        }

        val baseUrl = System.getenv("CONFIRMATE_API_BASE") ?: "http://localhost:8080"

        orchApi = OrchestratorApi(baseUrl, httpClientConfig = config, jsonBlock = jsonBlock)
        evidenceApi = EvidenceStoreApi(baseUrl, httpClientConfig = config, jsonBlock = jsonBlock)
    }

    suspend fun listEvidences(targetOfEvaluationId: String): List<Evidence> {
        val evidences = mutableListOf<Evidence>()
        var pageToken: String? = null
        
        do {
            val response =
                evidenceApi.evidenceStoreListEvidences(
                    filterTargetOfEvaluationId = targetOfEvaluationId,
                    filterToolId = null,
                    pageSize = 100,
                    pageToken = pageToken,
                    orderBy = null,
                    asc = null,
                )

            if (response.success) {
                val body = response.body()
                body.evidences?.let { evidences.addAll(it) }
                pageToken = body.nextPageToken
            } else {
                log.error("Failed to list evidences: ${response.response}")
                break
            }
        } while (!pageToken.isNullOrBlank())

        return evidences
    }

    suspend fun sendConfirmateResults(result: AnalysisResult) {
        val confirmateResults = with(log) { result.toConfirmateResult() }
        for (assessment in confirmateResults.assessmentResult) {
            val response = orchApi.orchestratorStoreAssessmentResult(assessment)
            if (response.success) {
                log.info(
                    "Successfully sent assessment result with id ${assessment.id} to Clouditor."
                )
            } else {
                log.error(
                    "Failed to send assessment result with id ${assessment.id} to Clouditor: ${response.response}"
                )
            }
            println("Assessment: $assessment")
        }

        for (evidence in confirmateResults.evidences) {
            val response = evidenceApi.evidenceStoreStoreEvidence(evidence)
            if (response.success) {
                log.info("Successfully sent evidence with id ${evidence.id} to Clouditor.")
            } else {
                log.error(
                    "Failed to send evidence with id ${evidence.id} to Clouditor: ${response.response}."
                )
            }
        }
    }
}
