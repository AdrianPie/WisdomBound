package com.lernhero.domain.reading

import kotlinx.serialization.Serializable

/** Sumaryczne statystyki czytania - licza sie od pierwszego otwarcia ksiazki, nigdy nie znikaja. */
@Serializable
data class ReadingStats(
    val totalMinutes: Int = 0,
    val totalPages: Int = 0,
    val totalWords: Int = 0,
    val totalCharacters: Int = 0
) {
    /** Srednie tempo w slowach na minute - licza sie tylko minuty AKTYWNEGO czytania (patrz AFK-guard). */
    val averageWpm: Int
        get() = if (totalMinutes <= 0) 0 else totalWords / totalMinutes
}

/**
 * Aktywnosc czytelnicza z dzisiejszego dnia - reset nastepuje, gdy [dateKey] przestaje byc
 * dzisiejszy (podobnie jak [com.lernhero.domain.fitness.FitnessSnapshot.dateKey]).
 */
@Serializable
data class ReadingDayProgress(
    val dateKey: String = "",
    val minutes: Int = 0,
    val pages: Int = 0
) {
    /** Progres na dzisiaj, albo puste zero, gdy zapisana data jest juz nieaktualna. */
    fun forToday(todayKey: String): ReadingDayProgress =
        if (dateKey == todayKey) this else ReadingDayProgress(dateKey = todayKey)
}
