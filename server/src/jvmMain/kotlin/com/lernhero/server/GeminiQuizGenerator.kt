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
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.request.url
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/**
 * Alternatywna implementacja [QuizGenerator] oparta o Gemini API. Klucz ([apiKey], z ENV,
 * nigdy z repo) idzie jako query param `key` - tak dziala Gemini REST API, nie ma tu naglowka
 * auth jak w Anthropic. Reszta zasady ta sama co w [AnthropicQuizGenerator]: cala trudna czesc
 * (prompt, parsowanie, walidacja) siedzi w `:domain`, ta klasa tylko dokleja transport HTTP.
 */
class GeminiQuizGenerator(
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
            client.post {
                url("https://generativelanguage.googleapis.com/v1beta/models/$model:generateContent")
                parameter("key", apiKey)
                contentType(ContentType.Application.Json)
                setBody(
                    GeminiRequest(
                        contents = listOf(GeminiContent(parts = listOf(GeminiPart(text = prompt))))
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
            response.body<GeminiResponse>()
        } catch (e: Exception) {
            return QuizGenerationResult.Failed("Couldn't read the model's response (${e.message}).")
        }

        val text = parsed.candidates.firstOrNull()
            ?.content
            ?.parts
            ?.firstOrNull { !it.text.isNullOrBlank() }
            ?.text
            ?: return QuizGenerationResult.Failed("The model didn't return any text.")

        // Cala walidacja/odsianie zlych pytan dzieje sie tutaj, w :domain, bez wiedzy o sieci.
        return QuizGenerationParser.parse(
            raw = text,
            idPrefix = "ai_${System.currentTimeMillis()}",
            allowedTypes = request.types,
            defaultDifficulty = request.difficulty ?: com.lernhero.domain.quiz.QuizDifficulty.Medium
        )
    }

    companion object {
        /** Szybki, tani model - w sam raz do generowania ustrukturyzowanych pytan quizowych. */
        const val DEFAULT_MODEL = "gemini-2.5-flash"
    }
}

@Serializable
private data class GeminiRequest(val contents: List<GeminiContent>)

@Serializable
private data class GeminiContent(val parts: List<GeminiPart>)

@Serializable
private data class GeminiPart(val text: String? = null)

@Serializable
private data class GeminiResponse(val candidates: List<GeminiCandidate> = emptyList())

@Serializable
private data class GeminiCandidate(val content: GeminiContent? = null)
