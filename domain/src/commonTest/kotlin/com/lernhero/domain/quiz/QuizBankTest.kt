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
        // Drugi przeglad banku wycial ~820 pytan (ten sam fakt w kilku plikach, definicje
        // z podrecznika, tabliczka mnozenia, pytania-zerowki) i dopisal ~1040 nowych, a kolejna
        // partia nastepny tysiac (glownie historia i nowa popkultura), a po niej jeszcze tysiac
        // wiedzy ogolnej, przewaznie Easy/Medium (potem jeszcze 500 i kolejny tysiac, a z podzialem
        // historii na dzialy - ponad tysiac pytan historycznych). Progi ida w gore razem z bankiem -
        // ma nie wrocic do rozmiaru, przy ktorym pytania zaczynaly sie powtarzac.
        assertTrue(all.size >= 7500, "Bank powinien miec ponad 7500 pytan, ma ${all.size}")

        // Slowka i wlasne talie nie sa w tym banku - powstaja z materialu gracza (patrz QuizCategory.builtin).
        // Flagi sa ograniczone liczba dostepnych grafik krajow, wiec maja nizszy prog.
        QuizCategory.builtin.filter { it != QuizCategory.Flags }.forEach { category ->
            val count = all.count { it.category == category }
            assertTrue(count >= 180, "Kategoria ${category.label} ma tylko $count pytan")
        }
        assertTrue(all.count { it.category == QuizCategory.Flags } >= 40, "Kategoria flag ma za malo pytan")

        QuizDifficulty.entries.forEach { difficulty ->
            val count = all.count { it.difficulty == difficulty }
            assertTrue(count >= 600, "Poziom ${difficulty.label} ma tylko $count pytan")
        }
    }

    /**
     * **Glebokosc, nie tylko szerokosc.**
     *
     * Odkad endless gra sie na JEDNYM temacie przez dwadziescia fal
     * ([com.lernhero.domain.progress.CategoryMastery]), suma pytan w kategorii przestala cokolwiek
     * mowic. Liczy sie to, ile ma ona pytan na GORNYCH szczeblach - bo od poziomu 5 tematu gracz
     * startuje na Hard, a od 7 na Expert i nizej juz nie schodzi. Kategoria z dwudziestoma
     * pytaniami eksperckimi wyczerpuje je w jednym wejsciu, a losowanie zaczyna po cichu schodzic
     * o szczebel nizej (patrz [QuizBank.pickFrom]) - czyli awans tematu robi pytania LATWIEJSZYMI.
     *
     * Flagi sa poza tym progiem: ich liczbe ogranicza liczba dostepnych grafik krajow, a nie bank.
     */
    @Test
    fun `kazdy temat ma z czego pytac na gornych szczeblach`() {
        QuizCategory.builtin.filter { it != QuizCategory.Flags }.forEach { category ->
            val inCategory = all.filter { it.category == category }
            val hard = inCategory.count { it.difficulty == QuizDifficulty.Hard }
            val expert = inCategory.count { it.difficulty == QuizDifficulty.Expert }

            assertTrue(hard >= 40, "Temat ${category.label} ma tylko $hard pytan trudnych")
            assertTrue(expert >= 30, "Temat ${category.label} ma tylko $expert pytan eksperckich")
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
