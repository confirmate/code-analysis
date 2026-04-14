package de.fraunhofer.aisec.confirmate.technicaldocument

import com.fasterxml.jackson.databind.SerializationFeature
import de.fraunhofer.aisec.confirmate.integration.ClouditorClient
import io.ktor.http.*
import io.ktor.serialization.jackson.*
import io.ktor.server.application.*
import io.ktor.server.cio.*
import io.ktor.server.engine.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.cors.routing.*

fun main() {
    val client = ClouditorClient()

    embeddedServer(CIO, host = "localhost", port = 9090) {
            configureWebapp(client)
        }
        .start(wait = true)
}

fun Application.configureWebapp(client: ClouditorClient) {
    install(CORS) {
        anyHost()
        allowHeader(HttpHeaders.ContentType)
        allowHeader(HttpHeaders.Accept)
    }

    install(ContentNegotiation) {
        jackson {
            enable(SerializationFeature.INDENT_OUTPUT)
            disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
        }
    }

    configureRouting(client)
}