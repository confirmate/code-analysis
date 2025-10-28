package org.example.ai.ollama

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ChatRequest(
    val model: String,
    val messages: List<Message>
)

@Serializable
data class ChatResponse(
    val model: String,
    // created_at
    val message: Message,
    val done: Boolean
)

@Serializable
data class Message(
    val role: Role,
    val content: String
)

@Serializable
enum class Role {
    @SerialName("system") SYSTEM,
    @SerialName("user") USER,
    @SerialName("assistant") ASSISTANT,
    @SerialName("tool") TOOL,
}
