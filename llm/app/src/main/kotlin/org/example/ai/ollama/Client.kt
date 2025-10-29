package org.example.ai.ollama

import io.ktor.client.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.utils.io.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.json.Json
import java.io.File

class Client(val url: String, val model: String) {

    val client = HttpClient {
        install(ContentNegotiation) {
            json()
        }
    }

    suspend fun chat(messages: List<Message>): Flow<String> {
        val res = client.post("$url/api/chat") {
            contentType(ContentType.Application.Json)
            setBody(ChatRequest(model, messages))
        }

        val json = Json { ignoreUnknownKeys = true }

        return flow {
            val channel: ByteReadChannel = res.bodyAsChannel()
            while(true) {
                if(channel.availableForRead > 0) {
                    val chatResponse = channel.readUTF8Line()?.let { json.decodeFromString<ChatResponse>(it) }
                    if(chatResponse != null) {
                        emit(chatResponse.message.content)
                    }
                }
                if(channel.isClosedForRead) {
                    break
                }
                delay(50)
            }
        }
    }

}

fun File.embed(): String {
    val content = this.readText()
    return "```${this.path}\n$content\n```"
}