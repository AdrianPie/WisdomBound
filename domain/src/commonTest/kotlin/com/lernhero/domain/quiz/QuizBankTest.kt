package com.lernhero.domain.quiz

import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Przy kilkuset pytaniach to testy, a nie czytanie, wylapia zdublowane id albo odpowiedz
 * powtorzona wsrod dystraktorow. Kazde takie pytanie byloby w grze albo niemozliwe, albo darmowe.
 */
class QuizBankTest {

    private val random = Random(2024)
    private val all = QuizBank.all(random)

    @Test
    fun `bank ma duzo pytan w kazdej kategorii i na kazdym poziomie`() {
        assertTrue(all.size >= 1300, "Bank powinien miec ponad 1300 pytan, ma ${all.size}")

        // Slowka i wlasne talie nie sa w tym banku - powstaja z materialu gracza (patrz QuizCategory.builtin).
        // Flagi sa ograniczone liczba dostepnych grafik krajow, wiec maja nizszy prog.
        QuizCategory.builtin.filter { it != QuizCategory.Flags }.forEach { category ->
            val count = all.count { it.category == category }
            assertTrue(count >= 100, "Kategoria ${category.label} ma tylko $count pytan")
        }
        assertTrue(all.count { it.category == QuizCategory.Flags } >= 40, "Kategoria flag ma za malo pytan")

        QuizDifficulty.entries.forEach { difficulty ->
            val count = all.count { it.difficulty == difficulty }
            assertTrue(count >= 300, "Poziom ${difficulty.label} ma tylko $count pytan")
        }
    }

    @Test
    fun `tresci pytan sie nie powtarzaja`() {
        // Gracz szybciej zauwazy powtorzone pytanie niz powtorzone id - wiec pilnujemy tresci.
        val duplicates = all
            .filter { it.category != QuizCategory.Flags }
            .groupBy { it.prompt }
            .filter { it.value.size > 1 }
            .keys

        assertTrue(duplicates.isEmpty(), "Powtorzone tresci pytan: $duplicates")
    }

    @Test
    fun `kazde pytanie ma jedna poprawna i trzy bledne odpowiedzi`() {
        all.forEach { entry ->
            assertEquals(4, entry.answers.size, "Pytanie '${entry.id}' nie ma czterech odpowiedzi")
            assertEquals(
                4,
                entry.answers.distinct().size,
                "Pytanie '${entry.id}' powtarza odpowiedz - poprawna byla wsrod blednych"
            )
        }
    }

    @Test
    fun `identyfikatory pytan sa unikalne`() {
        val duplicates = all.groupBy { it.id }.filter { it.value.size > 1 }.keys
        assertTrue(duplicates.isEmpty(), "Zdublowane id pytan: $duplicates")
    }

    @Test
    fun `flagi sa tylko wsrod trudnych i eksperckich`() {
        val flags = all.filter { it.category == QuizCategory.Flags }

        assertTrue(flags.isNotEmpty(), "Kategoria flag nie moze byc pusta")
        assertTrue(
            flags.all { it.difficulty == QuizDifficulty.Hard || it.difficulty == QuizDifficulty.Expert },
            "Flagi mialy trafic do trudnych pytan"
        )
        assertTrue(flags.all { it.flagCode != null }, "Pytanie o flage bez kodu kraju")
    }

    @Test
    fun `drabinka trudnosci rosnie z fala`() {
        val firstWave = QuizBank.ladderFor(wave = 1)
        val lateWave = QuizBank.ladderFor(wave = 7)

        assertTrue(
            firstWave.any { it == QuizDifficulty.Easy },
            "Pierwsza fala ma sie zaczynac lagodnie"
        )
        assertTrue(
            lateWave.none { it == QuizDifficulty.Easy || it == QuizDifficulty.Medium },
            "W poznych falach nie ma juz taryfy ulgowej"
        )
    }

    @Test
    fun `losowanie quizu trzyma sie drabinki i nie powtarza pytan`() {
        (1..8).forEach { wave ->
            val quiz = QuizBank.drawForWave(wave, Random(wave.toLong()))
            val ladder = QuizBank.ladderFor(wave)

            assertEquals(ladder.size, quiz.size, "Fala $wave dostala zla liczbe pytan")
            assertEquals(
                ladder,
                quiz.map { it.difficulty },
                "Fala $wave zlamala drabinke trudnosci"
            )
            assertEquals(
                quiz.size,
                quiz.map { it.id }.distinct().size,
                "Fala $wave powtorzyla pytanie"
            )
        }
    }

    @Test
    fun `ten sam seed daje ten sam quiz`() {
        assertEquals(
            QuizBank.drawForWave(wave = 3, random = Random(7)).map { it.id },
            QuizBank.drawForWave(wave = 3, random = Random(7)).map { it.id }
        )
    }
}
