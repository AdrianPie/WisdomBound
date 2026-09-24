package com.lernhero.server

import com.lernhero.domain.monetization.PremiumLimits
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.statement.bodyAsText
import io.ktor.http.encodeURLPathPart
import io.ktor.http.isSuccess
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.time.Instant

/**
 * Czy gracz ma aktywna subskrypcje premium - pytane WPROST w RevenueCat (REST API v1), a nie
 * brane od apki. Apka tez to wie, ale limit AI kosztuje prawdziwe pieniadze za kazde wywolanie
 * modelu, wiec o nim moze decydowac tylko ktos, kogo gracz nie da rady oszukac.
 *
 * Gracz jest w RevenueCat pod swoim uid Firebase (apka robi `logIn(uid)`), wiec ten sam uid,
 * ktory przyszedl w tokenie, jest kluczem subskrybenta.
 *
 * Brak REVENUECAT_SECRET_KEY = nikt nie jest premium, czyli generowanie AI jest dla wszystkich
 * zablokowane. To swiadomie bezpieczna strona: lepiej zablokowane niz darmowe dla kazdego.
 */
object Entitlements {

    private val secretKey = System.getenv("REVENUECAT_SECRET_KEY").orEmpty()
    private val entitlement = System.getenv("REVENUECAT_ENTITLEMENT") ?: "premium"
    private val json = Json { ignoreUnknownKeys = true }
    private val client = HttpClient(CIO) {
        install(HttpTimeout) { requestTimeoutMillis = 10_000 }
    }

    suspend fun isPremium(uid: String): Boolean {
        if (secretKey.isBlank()) return false
        return try {
            val response = client.get("https://api.revenuecat.com/v1/subscribers/${uid.encodeURLPathPart()}") {
                header("Authorization", "Bearer $secretKey")
            }
            if (!response.status.isSuccess()) return false
            isActive(json.parseToJsonElement(response.bodyAsText()).jsonObject, Instant.now())
        } catch (e: Exception) {
            System.err.println("entitlements: RevenueCat lookup failed: ${e.message}")
            false
        }
    }

    /** Aktywne = jest wpis uprawnienia i nie wygaslo (null = bezterminowe). */
    internal fun isActive(body: JsonObject, now: Instant): Boolean {
        val grant = body["subscriber"]?.jsonObject
            ?.get("entitlements")?.jsonObject
            ?.get(entitlement)?.jsonObject
            ?: return false
        val expires = grant["expires_date"]?.jsonPrimitive?.contentOrNull ?: return true
        return Instant.parse(expires).isAfter(now)
    }
}

/** Limit generowan AI dla subskrybenta - domyslnie [PremiumLimits.PREMIUM_AI_QUIZZES_PER_MONTH]. */
internal val PREMIUM_MONTHLY_LIMIT: Int =
    System.getenv("AI_QUOTA_LIMIT_PER_MONTH")?.toIntOrNull() ?: PremiumLimits.PREMIUM_AI_QUIZZES_PER_MONTH
