package com.lernhero.server

import com.lernhero.domain.quiz.ai.QuizGenerationRequest
import com.lernhero.domain.quiz.ai.QuizGenerationResult
import com.lernhero.domain.quiz.ai.QuizGenerator
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.Application
import io.ktor.server.application.call
import io.ktor.server.application.install
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.plugins.statuspages.StatusPages
import io.ktor.server.request.receive
import io.ktor.server.request.header
import io.ktor.server.response.respond
import io.ktor.server.routing.post
import io.ktor.server.routing.routing
import kotlinx.serialization.json.Json

/**
 * Punkt wejscia serwisu. Cloud Run wystawia numer portu w zmiennej PORT - domyslny 8080 dziala
 * tez lokalnie bez zadnej konfiguracji.
 */
fun main() {
    val port = System.getenv("PORT")?.toIntOrNull() ?: 8080
    embeddedServer(Netty, port = port, host = "0.0.0.0", module = Application::module).start(wait = true)
}

fun Application.module() {
    install(ContentNegotiation) {
        json(Json { ignoreUnknownKeys = true })
    }
    install(StatusPages) {
        exception<Throwable> { call, cause ->
            cause.printStackTrace()
            call.respond(HttpStatusCode.InternalServerError, mapOf("error" to (cause.message ?: "unknown error")))
        }
    }

    // Pusty klucz = QuizGenerator.isAvailable == false, endpoint wciaz odpowiada (Unavailable),
    // zamiast wywalic caly proces na starcie - wygodne, zeby serwis dalo sie postawic i
    // przetestowac (auth, limit, routing) zanim ktokolwiek doda prawdziwy klucz.
    //
    // Dwa mozliwe dostawcy - wybor po tym, ktory klucz jest w ENV. Gemini ma pierwszenstwo, bo
    // to on jest teraz domyslny (patrz GEMINI_API_KEY); Anthropic zostaje jako fallback dla
    // srodowisk, ktore go juz maja skonfigurowanego.
    val generator: QuizGenerator = when {
        System.getenv("GEMINI_API_KEY")?.isNotBlank() == true -> GeminiQuizGenerator(
            apiKey = System.getenv("GEMINI_API_KEY").orEmpty(),
            model = System.getenv("GEMINI_MODEL") ?: GeminiQuizGenerator.DEFAULT_MODEL
        )
        else -> AnthropicQuizGenerator(
            apiKey = System.getenv("ANTHROPIC_API_KEY").orEmpty(),
            model = System.getenv("ANTHROPIC_MODEL") ?: AnthropicQuizGenerator.DEFAULT_MODEL
        )
    }

    routing {
        quizGenerationRoute(generator)
    }
}

private fun io.ktor.server.routing.Routing.quizGenerationRoute(generator: QuizGenerator) {
    post("/quiz/generate") {
        val uid = call.authenticatedUid() ?: run {
            call.respond(HttpStatusCode.Unauthorized, mapOf("error" to "Missing or invalid Authorization header"))
            return@post
        }

        val request = try {
            call.receive<QuizGenerationRequest>()
        } catch (e: Exception) {
            call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Malformed request body"))
            return@post
        }

        if (request.topic.isBlank()) {
            call.respond(HttpStatusCode.BadRequest, mapOf("error" to "topic must not be blank"))
            return@post
        }

        // Generowanie AI jest tylko dla subskrybentow - sprawdzane w RevenueCat, nie w apce.
        if (!Entitlements.isPremium(uid)) {
            call.respond(HttpStatusCode.PaymentRequired, QuizGenerationResult.SubscriptionRequired)
            return@post
        }

        // Limit sprawdzany i ZUZYTY PRZED wywolaniem modelu - inaczej gracz moglby wywolac
        // (kosztowny) request do LLM, a limit i tak by go nie policzyl przy bledzie zapisu.
        when (val quota = AiQuota.consume(uid)) {
            is QuotaCheck.Denied -> {
                call.respond(HttpStatusCode.TooManyRequests, QuizGenerationResult.QuotaExceeded(quota.resetsAt))
                return@post
            }
            is QuotaCheck.Allowed -> {
                val result = generator.generate(
                    request.copy(count = request.count.coerceIn(QuizGenerationRequest.MIN_COUNT, QuizGenerationRequest.MAX_COUNT))
                )
                if (result is QuizGenerationResult.Failed) {
                    // Endpoint zawsze zwraca 200 (bledy providera to poprawna, oczekiwana
                    // odpowiedz domenowa, nie blad HTTP) - bez tego logu access-log Cloud Run
                    // pokazuje "200" nawet dla timeoutu/bledu modelu, co jest myslace przy
                    // diagnozowaniu (patrz historia commitow przy tym pliku).
                    System.err.println("quiz/generate: model provider failed: ${result.reason}")
                }
                val withQuota = if (result is QuizGenerationResult.Generated) {
                    result.copy(remainingThisMonth = quota.remainingAfterThisCall)
                } else {
                    result
                }
                call.respond(HttpStatusCode.OK, withQuota)
            }
        }
    }
}

/** Weryfikuje `Authorization: Bearer <idToken>` przez Firebase Admin SDK. Null = odmowa. */
private suspend fun io.ktor.server.application.ApplicationCall.authenticatedUid(): String? {
    val header = request.header("Authorization") ?: return null
    val token = header.removePrefix("Bearer ").trim()
    if (token.isBlank() || token == header) return null
    return FirebaseAdmin.verifyIdToken(token)
}
