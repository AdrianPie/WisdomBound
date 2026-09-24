package com.lernhero.data.quiz

import com.lernhero.domain.quiz.ai.QuizGenerationRequest
import com.lernhero.domain.quiz.ai.QuizGenerationResult
import com.lernhero.domain.quiz.ai.QuizGenerator
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.auth.auth
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

/**
 * To, co dzis w apce zastepuje [QuizGenerator.Stub]: NIE generuje nic sama, tylko woła
 * wlasny serwer (`:server`, `POST /quiz/generate`) z tokenem zalogowanego gracza w naglowku -
 * dokladnie tak, jak opisuje notatka przy [QuizGenerator]. Klucz do dostawcy modelu apka
 * nigdy nie widzi.
 *
 * [baseUrl] pusty = [isAvailable] `false`, czyli identyczne zachowanie UI jak przy Stub -
 * bezpieczny domyslny stan, dopoki [ServerConfig.AI_QUIZ_SERVER_BASE_URL] nie wskazuje na
 * prawdziwe wdrozenie.
 */
class RemoteQuizGenerator(
    private val baseUrl: String = ServerConfig.AI_QUIZ_SERVER_BASE_URL,
    private val client: HttpClient = HttpClient {
        install(ContentNegotiation) { json(Json { ignoreUnknownKeys = true }) }
        // Serwer musi jeszcze poczekac na model (patrz GeminiQuizGenerator - do 180s), wiec
        // telefon nie moze sie rozlaczyc po domyslnych kilkunastu sekundach silnika.
        install(HttpTimeout) {
            requestTimeoutMillis = 200_000
            connectTimeoutMillis = 30_000
            socketTimeoutMillis = 200_000
        }
    }
) : QuizGenerator {

    override val isAvailable: Boolean get() = baseUrl.isNotBlank()

    override suspend fun generate(request: QuizGenerationRequest): QuizGenerationResult {
        if (!isAvailable) return QuizGenerationResult.Unavailable

        val idToken = try {
            Firebase.auth.currentUser?.getIdToken(false)
        } catch (e: Exception) {
            null
        } ?: return QuizGenerationResult.Failed("You need to be signed in to use AI generation.")

        val response: HttpResponse = try {
            client.post("$baseUrl/quiz/generate") {
                header("Authorization", "Bearer $idToken")
                contentType(ContentType.Application.Json)
                setBody(request)
            }
        } catch (e: Exception) {
            return QuizGenerationResult.Failed("Couldn't reach the server (${e.message}).")
        }

        return when {
            response.status == HttpStatusCode.PaymentRequired -> QuizGenerationResult.SubscriptionRequired

            response.status == HttpStatusCode.TooManyRequests ->
                try {
                    response.body<QuizGenerationResult.QuotaExceeded>()
                } catch (e: Exception) {
                    QuizGenerationResult.QuotaExceeded(resetsAt = 0L)
                }

            response.status.isSuccessOk() -> try {
                response.body<QuizGenerationResult>()
            } catch (e: Exception) {
                QuizGenerationResult.Failed("Couldn't read the server's response (${e.message}).")
            }

            else -> QuizGenerationResult.Failed("Server returned ${response.status.value}.")
        }
    }

    private fun HttpStatusCode.isSuccessOk(): Boolean = value in 200..299
}
