package com.lernhero.domain.reading

import kotlinx.serialization.Serializable

/**
 * Slowo albo zdanie zapisane z czytnika przyciskiem "zakladka". [sentence] jedzie razem ze
 * slowem, bo bez zdania-kontekstu powtorka nie ma jak przypomniec, w jakim znaczeniu gracz to
 * slowo widzial.
 *
 * [isSentence] rozroznia, co dokladnie zapisano - pojedyncze slowo, czy calosc zaznaczenia
 * rozszerzonego na zdanie (przycisk "rozszerz" w popupie tlumaczenia). Ma to znaczenie dla
 * [com.lernhero.domain.quiz.WordQuizBank]: quiz pyta WYLACZNIE o pojedyncze slowa - zdanie jako
 * pytanie albo jako jedna z czterech odpowiedzi nie ma sensu (ani do zgadniecia, ani jako
 * wiarygodny dystraktor).
 */
@Serializable
data class SavedWord(
    val id: String,
    val text: String,
    val translation: String,
    val sentence: String,
    val bookId: String,
    val bookTitle: String,
    /** epoch millis - kiedy zapisano, do sortowania i do rozkladania powtorek w czasie. */
    val savedAt: Long,
    val isSentence: Boolean = false
)
