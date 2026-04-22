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
package de.fraunhofer.aisec.confirmate.technicaldocument

import de.fraunhofer.aisec.confirmate.integration.ClouditorClient
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.http.content.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Application.configureRouting(client: ClouditorClient) {
    routing {
        apiRoutes(client)
        frontendRoutes()
    }
}

fun Routing.apiRoutes(client: ClouditorClient) {
    route("/api") {
        get("/targets-of-evaluation") {
            try {
                val toes = client.listTargetOfEvaluations()
                call.respond(mapOf("targetsOfEvaluation" to toes))
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, mapOf("error" to e.message))
            }
        }
        get("/evidences") {
            try {
                val toeId = call.request.queryParameters["targetOfEvaluationId"]
                val evidences = client.listEvidences(targetOfEvaluationId = toeId)
                call.respond(mapOf("evidences" to evidences))
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, mapOf("error" to e.message))
            }
        }
    }
}

fun Routing.frontendRoutes() {
    staticResources("/", "static", "index.html") { default("index.html") }
}
