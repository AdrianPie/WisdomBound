package com.lernhero.reader.translate

import kotlin.coroutines.resume
import kotlinx.coroutines.suspendCancellableCoroutine

actual fun platformTranslationSource(): TranslationSource = IosTranslationSource

/**
 * Apple `Translation` framework jest czystym Swiftem (pusty naglowek Objective-C), wiec
 * Kotlin/Native nie moze go wywolac bezposrednio - most idzie w odwrotna strone: `iosApp`
 * (patrz `TranslationBridge.swift`) ustawia [handler] raz, przy starcie aplikacji, PRZED
 * otwarciem czytnika. Handler jedzie na `TranslationSession` uzyskiwanej przez modyfikator
 * SwiftUI `.translationTask` (nie na imperatywnym inicjalizatorze, ktory wymaga iOS 26 i nie
 * umie sam pobrac pakietu jezykowego) - dziala wiec od iOS 18, czyli na calym zakresie, ktory
 * wspiera apka, i system SAM pyta uzytkownika o zgode na pobranie jezyka, gdy jest potrzebny.
 */
object IosTranslationBridge {
    var handler: ((text: String, targetLanguage: String, onResult: (String?) -> Unit) -> Unit)? = null
}

private object IosTranslationSource : TranslationSource {
    override suspend fun translate(text: String, targetLanguage: String): TranslationResult {
        val handler = IosTranslationBridge.handler ?: return TranslationResult.Unavailable
        val translated = suspendCancellableCoroutine<String?> { cont ->
            handler(text, targetLanguage) { result -> cont.resume(result) }
        }
        return translated?.let { TranslationResult.Translated(it) } ?: TranslationResult.Failed
    }
}
