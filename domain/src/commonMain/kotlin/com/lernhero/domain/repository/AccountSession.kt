package com.lernhero.domain.repository

/**
 * Sesja konta - kto jest zalogowany i jak z tego wyjsc.
 *
 * Osobno od [PlayerRepository], bo to dwie rozne rzeczy: tam mieszka POSTEP gracza (dokument
 * w Firestore), tutaj sama TOZSAMOSC. Ekran ustawien potrzebuje wylacznie tej drugiej i nie ma
 * powodu, zeby w tym celu ciagnac cale repozytorium gracza.
 *
 * Implementacja siedzi w `:data` (Firebase Auth) - domena nie wie, kto wystawia tozsamosc.
 */
interface AccountSession {

    /** E-mail zalogowanego konta, albo null (logowanie przez dostawce bez e-maila / brak sesji). */
    val email: String?

    /**
     * Wylogowanie. Po powrocie z tej funkcji [PlayerRepository.getUserId] zwraca juz null, wiec
     * ekran, ktory to wola, MUSI zaraz potem odeslac gracza na logowanie - inaczej zostanie
     * w grze bez konta i kazdy zapis przepadnie po cichu.
     */
    suspend fun signOut()

    /**
     * Trwale usuwa konto i wszystkie dane gracza (wymog Google Play dla aplikacji z kontami).
     * Subskrypcji w sklepie to NIE anuluje - to robi sam gracz w Google Play / App Store.
     */
    suspend fun deleteAccount(): AccountDeletion
}

enum class AccountDeletion {
    Deleted,

    /** Firebase usuwa konto tylko tuz po zalogowaniu - trzeba sie wylogowac i zalogowac ponownie. */
    NeedsRecentLogin,
    Failed
}
