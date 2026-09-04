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
            // TODO: usun po zdiagnozowaniu przyczyny 401.
            System.err.println("verifyIdToken failed: ${e::class.simpleName}: ${e.message}")
            null
        }
    }
}

/** Wynik sprawdzenia/zuzycia limitu tygodniowego - patrz [AiQuota.consume]. */
sealed interface QuotaCheck {
    data class Allowed(val remainingAfterThisCall: Int) : QuotaCheck
    data class Denied(val resetsAt: Long) : QuotaCheck
}

/**
 * Limit generowan AI - "2x/tydzien" per gracz. Zyje w `player/{uid}/privateData/aiUsage`,
 * dokumencie, do ktorego klient NIE MA dostepu wedlug Security Rules (patrz firestore.rules) -
 * Admin SDK je omija, wiec to jedyne miejsce, ktore w ogole moze ten dokument tknac.
 *
 * Sprawdzenie i inkrementacja licza sie ATOMOWO w jednej transakcji Firestore, zeby dwa
 * rownoczesne requesty tego samego gracza nie zdazyly obie przejsc "jeszcze jest limit"
 * przed zapisem.
 */
object AiQuota {

    private const val LIMIT_PER_WEEK = 2
    private const val WEEK_MS = 7L * 24 * 60 * 60 * 1000

    suspend fun consume(uid: String): QuotaCheck = withContext(Dispatchers.IO) {
        val doc = FirebaseAdmin.firestore
            .collection("player").document(uid)
            .collection("privateData").document("aiUsage")

        try {
            FirebaseAdmin.firestore.runTransaction { tx ->
                val now = System.currentTimeMillis()
                val snapshot = tx.get(doc).get()

                val weekStartedAt = snapshot.getLong("weekStartedAt") ?: 0L
                val storedCount = snapshot.getLong("count") ?: 0L

                val weekExpired = now - weekStartedAt >= WEEK_MS
                val effectiveWeekStart = if (weekExpired) now else weekStartedAt
                val effectiveCount = if (weekExpired) 0L else storedCount

                if (effectiveCount >= LIMIT_PER_WEEK) {
                    return@runTransaction QuotaCheck.Denied(resetsAt = effectiveWeekStart + WEEK_MS)
                }

                val newCount = effectiveCount + 1
                tx.set(
                    doc,
                    mapOf(
                        "count" to newCount,
                        "weekStartedAt" to effectiveWeekStart
                    )
                )
                QuotaCheck.Allowed(remainingAfterThisCall = (LIMIT_PER_WEEK - newCount).toInt())
            }.get()
        } catch (e: ExecutionException) {
            // Awaria Firestore nie powinna wywalic calego requestu - lepiej odmowic generowania
            // (koszt: gracz traci jedna probe) niz zawiesic caly endpoint na wyjatku transakcji.
            QuotaCheck.Denied(resetsAt = System.currentTimeMillis() + WEEK_MS)
        }
    }
}
