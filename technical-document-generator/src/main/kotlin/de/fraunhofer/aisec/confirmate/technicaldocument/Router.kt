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
        get("/evidences/{toeId}") {
            val toeId =
                call.parameters["toeId"]
                    ?: return@get call.respond(
                        HttpStatusCode.BadRequest,
                        mapOf("error" to "Missing targetOfEvaluationId"),
                    )

            try {
                val evidences = client.listEvidences(toeId)
                call.respond(mapOf("evidences" to evidences))
            } catch (e: Exception) {
                call.respond(
                    HttpStatusCode.InternalServerError,
                    mapOf("error" to e.message),
                )
            }
        }
    }
}

fun Routing.frontendRoutes() {
    staticResources("/", "static", "index.html") { default("index.html") }
}