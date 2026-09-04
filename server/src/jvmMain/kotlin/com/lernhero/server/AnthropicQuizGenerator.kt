package com.lernhero.server

import com.lernhero.domain.quiz.ai.QuizGenerationParser
import com.lernhero.domain.quiz.ai.QuizGenerationPrompt
import com.lernhero.domain.quiz.ai.QuizGenerationRequest
import com.lernhero.domain.quiz.ai.QuizGenerationResult
import com.lernhero.domain.quiz.ai.QuizGenerator
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/**
 * Prawdziwa implementacja [QuizGenerator] - jedyne miejsce w calym systemie, ktore zna klucz do
 * Anthropic API ([apiKey], z ENV, nigdy z repo). Zyje wylacznie tutaj, w `:server` - apka na
 * telefonie nigdy nie woła tego dostawcy bezposrednio (patrz notatka przy [QuizGenerator]).
 *
 * Cala trudna czesc (budowa promptu, parsowanie i walidacja odpowiedzi) siedzi w `:domain` i jest
 * tam przetestowana bez sieci - ta klasa tylko dokleja transport HTTP.
 */
class AnthropicQuizGenerator(
    private val apiKey: String,
    private val model: String = DEFAULT_MODEL,
    private val client: HttpClient = HttpClient(CIO) {
        install(ContentNegotiation) { json(Json { ignoreUnknownKeys = true }) }
    }
) : QuizGenerator {

    override val isAvailable: Boolean get() = apiKey.isNotBlank()

    override suspend fun generate(request: QuizGenerationRequest): QuizGenerationResult {
        if (!isAvailable) return QuizGenerationResult.Unavailable

        val prompt = QuizGenerationPrompt.build(request)

        val response: HttpResponse = try {
            client.post("https://api.anthropic.com/v1/messages") {
                header("x-api-key", apiKey)
                header("anthropic-version", "2023-06-01")
                contentType(ContentType.Application.Json)
                setBody(
                    AnthropicRequest(
                        model = model,
                        maxTokens = maxTokensFor(request.count),
                        messages = listOf(AnthropicMessage(role = "user", content = prompt))
                    )
                )
            }
        } catch (e: Exception) {
            return QuizGenerationResult.Failed("Couldn't reach the model provider (${e.message}).")
        }

        if (!response.status.isSuccess()) {
            return QuizGenerationResult.Failed("Model provider returned ${response.status.value}.")
        }

        val parsed = try {
            response.body<AnthropicResponse>()
        } catch (e: Exception) {
            return QuizGenerationResult.Failed("Couldn't read the model's response (${e.message}).")
        }

        val text = parsed.content.firstOrNull { it.type == "text" }?.text
            ?: return QuizGenerationResult.Failed("The model didn't return any text.")

        // Cala walidacja/odsianie zlych pytan dzieje sie tutaj, w :domain, bez wiedzy o sieci.
        return QuizGenerationParser.parse(
            raw = text,
            idPrefix = "ai_${System.currentTimeMillis()}",
            allowedTypes = request.types,
            defaultDifficulty = request.difficulty ?: com.lernhero.domain.quiz.QuizDifficulty.Medium
        )
    }

    /** Male, zaokraglone w gore - wiecej pytan potrzebuje wiecej miejsca na odpowiedz. */
    private fun maxTokensFor(count: Int): Int = (count.coerceIn(1, 30) * 150 + 500)

    companion object {
        /** Szybki, tani model - w sam raz do generowania ustrukturyzowanych pytan quizowych. */
        const val DEFAULT_MODEL = "claude-haiku-4-5-20251001"
    }
}

@Serializable
private data class AnthropicRequest(
    val model: String,
    @SerialName("max_tokens") val maxTokens: Int,
    val messages: List<AnthropicMessage>
)

@Serializable
private data class AnthropicMessage(val role: String, val content: String)

@Serializable
private data class AnthropicResponse(val content: List<AnthropicContentBlock> = emptyList())

@Serializable
private data class AnthropicContentBlock(val type: String = "", val text: String? = null)
