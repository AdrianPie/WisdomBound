package com.lernhero.domain.reading

import kotlinx.serialization.Serializable

/**
 * Slowo albo zdanie zapisane z czytnika przyciskiem "zakladka". To surowiec pod quiz ze slowek -
 * [sentence] jedzie razem ze slowem, bo bez zdania-kontekstu powtorka nie ma jak przypomniec,
 * w jakim znaczeniu gracz to slowo widzial.
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
    val savedAt: Long
)
