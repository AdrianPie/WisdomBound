package com.lernhero.domain.reading

/**
 * Dwa archiwa fiszek. Slowko zaczyna w [Learning] i awansuje do [Mastered] dopiero po
 * [VocabularyDeck.MASTERY_STREAK] poprawnych odpowiedziach Z RZEDU - jedno szczesliwe trafienie
 * nie wystarcza, a pomylka po drodze zeruje serie.
 */
enum class VocabularyBox(val label: String) {
    Learning("Learning"),
    Mastered("Mastered")
}

/**
 * Cala logika "co sie dzieje z fiszka po odpowiedzi" - jedno miejsce, z ktorego korzystaja i quiz
 * w walce, i ekran Slowek. Bez tego regula awansu rozlazlaby sie po UI i po walce, a to sa dwa
 * miejsca, ktore musza sie zgadzac co do jednego licznika.
 */
object VocabularyDeck {

    /** Ile poprawnych odpowiedzi Z RZEDU przenosi slowo do archiwum "opanowane". */
    const val MASTERY_STREAK = 5

    fun learning(words: List<SavedWord>): List<SavedWord> = words.filter { it.masteredAt == null }

    fun mastered(words: List<SavedWord>): List<SavedWord> = words.filter { it.masteredAt != null }

    fun inBox(words: List<SavedWord>, box: VocabularyBox): List<SavedWord> = when (box) {
        VocabularyBox.Learning -> learning(words)
        VocabularyBox.Mastered -> mastered(words)
    }

    /**
     * Zapisuje wynik jednej odpowiedzi na fiszke i zwraca ja w nowym stanie.
     *
     * Poprawna odpowiedz wydluza serie i - po [MASTERY_STREAK] z rzedu - przenosi slowo do
     * archiwum. Bledna zeruje serie i **cofa** slowo z archiwum z powrotem do nauki: skoro gracz
     * przestal je pamietac, to znaczy, ze nie bylo opanowane, i udawanie inaczej tylko zabralo by
     * mu to slowo z puli powtorek.
     */
    fun recordAnswer(word: SavedWord, correct: Boolean, now: Long): SavedWord =
        if (correct) {
            val streak = word.correctStreak + 1
            word.copy(
                correctStreak = streak,
                correctTotal = word.correctTotal + 1,
                masteredAt = word.masteredAt ?: if (streak >= MASTERY_STREAK) now else null
            )
        } else {
            word.copy(
                correctStreak = 0,
                wrongTotal = word.wrongTotal + 1,
                masteredAt = null
            )
        }

    /** Ile jeszcze trafien z rzedu dzieli slowo od archiwum. 0 = juz opanowane. */
    fun stepsToMastery(word: SavedWord): Int =
        if (word.masteredAt != null) 0 else (MASTERY_STREAK - word.correctStreak).coerceAtLeast(0)
}
