package com.lernhero.reader.translate

import com.google.mlkit.common.model.DownloadConditions
import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.Translator
import com.google.mlkit.nl.translate.TranslatorOptions
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

actual fun platformTranslationSource(): TranslationSource = MlKitTranslationSource

/**
 * Google ML Kit Translation - model jezykowy (~30 MB) sciaga sie raz przy pierwszym tlumaczeniu
 * na dana pare jezykow, potem dziala calkowicie offline, na urzadzeniu, bez klucza API.
 *
 * Zrodlowy jezyk jest na sztywno angielski, bo tekst ksiazek w czytniku dzis zawsze jest po
 * angielsku - gdy dojdzie import wielu jezykow, to jest miejsce do rozbudowy.
 */
private object MlKitTranslationSource : TranslationSource {
    // Jeden Translator per para jezykow - stworzenie go jest tanie, ale ponowne uzycie
    // oszczedza powtarzanie sprawdzenia "czy model juz sciagniety" przy kazdym dotknieciu.
    private val translators = mutableMapOf<String, Translator>()

    override suspend fun translate(text: String, targetLanguage: String): TranslationResult {
        val target = TranslateLanguage.fromLanguageTag(targetLanguage) ?: return TranslationResult.Failed
        val translator = translatorFor(target)

        return runCatching {
            downloadModelIfNeeded(translator)
            TranslationResult.Translated(translateAwait(translator, text))
        }.getOrElse { TranslationResult.Failed }
    }

    private fun translatorFor(target: String): Translator = translators.getOrPut(target) {
        val options = TranslatorOptions.Builder()
            .setSourceLanguage(TranslateLanguage.ENGLISH)
            .setTargetLanguage(target)
            .build()
        Translation.getClient(options)
    }

    private suspend fun downloadModelIfNeeded(translator: Translator) = suspendCancellableCoroutine { cont ->
        // Domyslne warunki pozwalaja sciagnac model przez dowolne polaczenie - jednorazowe
        // ~30 MB przy pierwszym tlumaczeniu tej pary jezykow, potem nic wiecej sie nie sciaga.
        translator.downloadModelIfNeeded(DownloadConditions.Builder().build())
            .addOnSuccessListener { cont.resume(Unit) }
            .addOnFailureListener { cont.resumeWithException(it) }
    }

    private suspend fun translateAwait(translator: Translator, text: String): String =
        suspendCancellableCoroutine { cont ->
            translator.translate(text)
                .addOnSuccessListener { cont.resume(it) }
                .addOnFailureListener { cont.resumeWithException(it) }
        }
}
