package com.lernhero.data

import com.lernhero.domain.repository.AccountDeletion
import com.lernhero.domain.repository.AccountSession
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.auth.FirebaseAuthRecentLoginRequiredException
import dev.gitlive.firebase.auth.auth
import dev.gitlive.firebase.firestore.firestore
import kotlin.time.Clock

/**
 * Tozsamosc konta z Firebase Auth - patrz [AccountSession].
 *
 * Wylogowanie kasuje sesje LOKALNIE (token na urzadzeniu), a nie konto: postep zostaje w Firestore
 * i wraca w calosci po ponownym zalogowaniu. Dlatego ekran ustawien moze o to pytac zwyklym
 * potwierdzeniem, bez ostrzezen o utracie danych.
 */
class FirebaseAccountSession : AccountSession {

    override val email: String?
        get() = Firebase.auth.currentUser?.email

    override suspend fun signOut() {
        Firebase.auth.signOut()
    }

    override suspend fun deleteAccount(): AccountDeletion {
        val user = Firebase.auth.currentUser ?: return AccountDeletion.Failed
        // Firebase odmawia usuniecia konta, jesli logowanie bylo dawno. Sprawdzamy to PRZED
        // skasowaniem danych - inaczej gracz zostalby z pustym kontem, ktorego nie da sie usunac.
        if (!signedInRecently(user.metaData?.lastSignInTime)) return AccountDeletion.NeedsRecentLogin

        return try {
            val player = Firebase.firestore.collection("player").document(user.uid)
            player.collection("decks").get().documents.forEach { it.reference.delete() }
            player.collection("privateData").document("role").delete()
            player.delete()
            user.delete()
            Firebase.auth.signOut()
            AccountDeletion.Deleted
        } catch (e: FirebaseAuthRecentLoginRequiredException) {
            AccountDeletion.NeedsRecentLogin
        } catch (e: Exception) {
            AccountDeletion.Failed
        }
    }

    private fun signedInRecently(lastSignIn: Double?): Boolean {
        if (lastSignIn == null) return false
        // Android podaje milisekundy, iOS sekundy.
        val millis = if (lastSignIn > 1e11) lastSignIn else lastSignIn * 1000
        return Clock.System.now().toEpochMilliseconds() - millis < RECENT_LOGIN_MS
    }

    private companion object {
        const val RECENT_LOGIN_MS = 5 * 60 * 1000L
    }
}
