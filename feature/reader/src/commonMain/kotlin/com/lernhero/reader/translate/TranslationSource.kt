package com.lernhero.reader.translate

/**
 * Zrodlo tlumaczen dla czytnika. Jeden interfejs, dwie mozliwe implementacje docelowo:
 *
 * - `MlKitTranslationSource` (Android) - Google ML Kit Translation, model jezykowy pobierany raz
 *   (~30 MB) i dziala offline, bez klucza API.
 * - `AppleTranslationSource` (iOS 17+) - `Translation` framework, tez offline, tez bez klucza.
 *
 * Obie sa prawdziwym tlumaczeniem NA URZADZENIU, nie zapytaniem do chmury - to podejscie
 * (nie wlasny slownik, nie LLM przez siec) daje dokladnie efekt znany z Dicto: dziala bez
 * internetu i nie kosztuje nic za zapytanie.
 *
 * [Stub] jest tym, czego dzis uzywa apka - miejsce na tlumaczenie jest widoczne i gotowe w UI,
 * ale realny silnik jeszcze nie jest podpiety.
 */
interface TranslationSource {
    suspend fun translate(text: String, targetLanguage: String = "pl"): TranslationResult

    object Stub : TranslationSource {
        override suspend fun translate(text: String, targetLanguage: String): TranslationResult =
            TranslationResult.Unavailable
    }
}

/**
 * Prawdziwy silnik tlumaczenia danej platformy - ML Kit na Androidzie, `Translation` framework
 * na iOS (iOS 17+; ponizej tej wersji [TranslationSource.Stub], bo starszy iOS nie ma tego API).
 * Desktop nie ma odpowiednika, wiec dostaje [TranslationSource.Stub].
 */
expect fun platformTranslationSource(): TranslationSource

sealed interface TranslationResult {
    data class Translated(val text: String) : TranslationResult
    /** Silnik tlumaczenia nie jest jeszcze podpiety (patrz [TranslationSource.Stub]). */
    data object Unavailable : TranslationResult
    data object Failed : TranslationResult
    /** Dzienny limit darmowych tlumaczen wyczerpany - patrz [com.lernhero.shared.monetization.TranslationQuota]. */
    data object LimitReached : TranslationResult
}
