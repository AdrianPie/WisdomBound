package com.lernhero.data

import com.lernhero.domain.repository.AccountSession
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.auth.auth

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
}
