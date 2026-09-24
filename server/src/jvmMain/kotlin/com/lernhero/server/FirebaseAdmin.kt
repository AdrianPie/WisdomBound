package com.lernhero.server

import com.google.auth.oauth2.GoogleCredentials
import com.google.cloud.firestore.Firestore
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.cloud.FirestoreClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.concurrent.ExecutionException

/**
 * Jedno miejsce inicjalizacji Admin SDK. Dziala bez zadnego pliku klucza zarowno na Cloud Run
 * (metadata server konta serwisowego kontenera), jak i lokalnie po `gcloud auth application-default
 * login` - `GoogleCredentials.getApplicationDefault()` sam znajduje wlasciwe poswiadczenia w obu
 * przypadkach. Nigdy nie trzymamy klucza JSON w repo.
 */
object FirebaseAdmin {

    /**
     * Jedyne miejsce, ktore woła [FirebaseApp.initializeApp] - i [firestore], i [verifyIdToken]
     * przechodza przez ten sam `by lazy`, wiec ktokolwiek z nich zapyta pierwszy, inicjalizuje
     * app dla obu. Trzymanie initu tylko w [firestore] (jak bylo wczesniej) psulo weryfikacje
     * tokenow, bo /quiz/generate woła [verifyIdToken] ZANIM cokolwiek dotknie Firestore.
     */
    private val app: FirebaseApp by lazy {
        FirebaseApp.getApps().firstOrNull() ?: FirebaseApp.initializeApp(
            FirebaseOptions.builder()
                .setCredentials(GoogleCredentials.getApplicationDefault())
                // GoogleCredentials.getApplicationDefault() na Cloud Run NIE niesie w sobie ID
                // projektu (to konto serwisowe metadata servera, nie plik JSON z project_id) -
                // bez jawnego .setProjectId() verifyIdToken() rzuca IllegalStateException.
                // FIREBASE_PROJECT_ID w ENV pozwala latwo zmienic projekt bez rebuildu; domyslny
                // to ten sam projekt co composeApp/google-services.json.
                .setProjectId(System.getenv("FIREBASE_PROJECT_ID") ?: "quizhero-467415")
                .build()
        )
    }

    val firestore: Firestore by lazy {
        app
        FirestoreClient.getFirestore()
    }

    /** Sprawdza token Firebase Auth z naglowka `Authorization: Bearer <token>`. Null = nieprawidlowy/brak. */
    suspend fun verifyIdToken(idToken: String): String? = withContext(Dispatchers.IO) {
        try {
            app
            FirebaseAuth.getInstance().verifyIdToken(idToken).uid
        } catch (e: Exception) {
            null
        }
    }
}

/** Wynik sprawdzenia/zuzycia limitu miesiecznego - patrz [AiQuota.consume]. */
sealed interface QuotaCheck {
    data class Allowed(val remainingAfterThisCall: Int) : QuotaCheck
    data class Denied(val resetsAt: Long) : QuotaCheck
}

/**
 * Limit generowan AI subskrybenta - [PREMIUM_MONTHLY_LIMIT] na 30 dni liczone od pierwszego
 * generowania w okresie. Zyje w `player/{uid}/privateData/aiUsage`, dokumencie, do ktorego klient
 * NIE MA dostepu wedlug Security Rules (patrz firestore.rules) - Admin SDK je omija, wiec to
 * jedyne miejsce, ktore w ogole moze ten dokument tknac.
 *
 * Sprawdzenie i inkrementacja licza sie ATOMOWO w jednej transakcji Firestore, zeby dwa
 * rownoczesne requesty tego samego gracza nie zdazyly obie przejsc "jeszcze jest limit"
 * przed zapisem. Czy gracz W OGOLE moze generowac (subskrypcja), rozstrzyga wczesniej
 * [Entitlements] - ten obiekt liczy juz tylko zuzycie.
 */
object AiQuota {

    private const val PERIOD_MS = 30L * 24 * 60 * 60 * 1000

    suspend fun consume(uid: String): QuotaCheck = withContext(Dispatchers.IO) {
        val doc = FirebaseAdmin.firestore
            .collection("player").document(uid)
            .collection("privateData").document("aiUsage")

        try {
            FirebaseAdmin.firestore.runTransaction { tx ->
                val now = System.currentTimeMillis()
                val snapshot = tx.get(doc).get()

                val periodStartedAt = snapshot.getLong("periodStartedAt") ?: 0L
                val storedCount = snapshot.getLong("count") ?: 0L

                val periodExpired = now - periodStartedAt >= PERIOD_MS
                val effectiveStart = if (periodExpired) now else periodStartedAt
                val effectiveCount = if (periodExpired) 0L else storedCount

                if (effectiveCount >= PREMIUM_MONTHLY_LIMIT) {
                    return@runTransaction QuotaCheck.Denied(resetsAt = effectiveStart + PERIOD_MS)
                }

                val newCount = effectiveCount + 1
                tx.set(
                    doc,
                    mapOf(
                        "count" to newCount,
                        "periodStartedAt" to effectiveStart
                    )
                )
                QuotaCheck.Allowed(remainingAfterThisCall = (PREMIUM_MONTHLY_LIMIT - newCount).toInt())
            }.get()
        } catch (e: ExecutionException) {
            // Awaria Firestore nie powinna wywalic calego requestu - lepiej odmowic generowania
            // (koszt: gracz traci jedna probe) niz zawiesic caly endpoint na wyjatku transakcji.
            QuotaCheck.Denied(resetsAt = System.currentTimeMillis() + PERIOD_MS)
        }
    }
}
