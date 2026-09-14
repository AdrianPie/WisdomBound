package com.lernhero.domain.progress

import com.lernhero.domain.quiz.QuizCategory
import com.lernhero.domain.quiz.QuizDifficulty
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Poziom tematu ma byc powodem, dla ktorego gracz wraca w ten sam temat - a nie sama odznaka.
 * Te testy pilnuja dwoch rzeczy, na ktorych ta obietnica stoi: ze awans faktycznie podnosi punkt
 * startowy pytan, i ze nie podnosi go w nieskonczonosc.
 */
class CategoryMasteryTest {

    @Test
    fun `poziom podnosi startowa trudnosc co dwa stopnie`() {
        assertEquals(QuizDifficulty.Easy, CategoryMastery.startingDifficulty(1))
        assertEquals(QuizDifficulty.Easy, CategoryMastery.startingDifficulty(2))
        assertEquals(QuizDifficulty.Medium, CategoryMastery.startingDifficulty(3))
        assertEquals(QuizDifficulty.Hard, CategoryMastery.startingDifficulty(5))
        assertEquals(QuizDifficulty.Expert, CategoryMastery.startingDifficulty(7))
    }

    /**
     * Poziom nie ma sufitu, ale trudnosc ma - inaczej po kilkunastu przejsciach temat stalby sie
     * niegrywalny zamiast bardziej oplacalny.
     */
    @Test
    fun `trudnosc zatrzymuje sie na Expert, nagrody rosna dalej`() {
        assertEquals(QuizDifficulty.Expert, CategoryMastery.startingDifficulty(50))
        assertTrue(
            CategoryMastery.rewardMultiplier(50) > CategoryMastery.rewardMultiplier(7),
            "Poziom powyzej progu trudnosci ma dalej podnosic nagrody"
        )
    }

    /** Wewnatrz jednego wejscia drabinka ma rosnac z fala, tak jak w kampanii. */
    @Test
    fun `fala podnosi trudnosc ponad poziom startowy`() {
        val start = CategoryMastery.difficultyFor(level = 1, wave = 1)
        val late = CategoryMastery.difficultyFor(level = 1, wave = 20)
        assertEquals(QuizDifficulty.Easy, start)
        assertTrue(late.ordinal > start.ordinal, "Fala 20 ma byc trudniejsza niz fala 1")
    }

    @Test
    fun `nietkniety temat startuje na pierwszym poziomie, awans dopisuje jeden`() {
        val levels = CategoryLevels()
        assertEquals(CategoryMastery.FIRST_LEVEL, levels.levelOf(QuizCategory.Science))

        val after = levels.advanced(QuizCategory.Science)
        assertEquals(2, after.levelOf(QuizCategory.Science))
        // Awans jednego tematu nie rusza pozostalych.
        assertEquals(CategoryMastery.FIRST_LEVEL, after.levelOf(QuizCategory.History))
    }

    /**
     * Powyzej progu trudnosci nagroda ma rosnac NIE SZYBCIEJ niz wrogowie. Inaczej kazdy kolejny
     * poziom jest czysta inflacja: pytania juz stoja na Expert, a mnoznik zlota rosnie dalej
     * dwa razy szybciej od tego, co stawia mu opor.
     */
    @Test
    fun `powyzej progu trudnosci nagroda nie odrywa sie od wrogow`() {
        val cap = CategoryMastery.MAX_DIFFICULTY_LEVEL
        val rewardStepBelow = CategoryMastery.rewardMultiplier(cap) - CategoryMastery.rewardMultiplier(cap - 1)
        val rewardStepAbove = CategoryMastery.rewardMultiplier(cap + 1) - CategoryMastery.rewardMultiplier(cap)
        val enemyStep = CategoryMastery.enemyMultiplier(cap + 1) - CategoryMastery.enemyMultiplier(cap)

        assertTrue(rewardStepBelow > enemyStep, "Dopoki pytania rosna, nagroda ma wyprzedzac wrogow")
        assertTrue(rewardStepAbove <= enemyStep + 0.0001f, "Po progu nagroda ma isc noga w noge z wrogami")
    }

    /**
     * Endless pozwala przybic staly poziom pytan. Bez tej reguly najoplacalniejszym wejsciem
     * w tryb nauki bylo wybranie "Easy" na temacie z wysokim poziomem: rozgrzewkowe pytania
     * platne mnoznikiem poziomu 9.
     */
    @Test
    fun `zanizony poziom pytan placi wedlug tego, co sie naprawde gralo`() {
        val high = 9

        assertEquals(high, CategoryMastery.effectiveLevel(high, fixed = null))
        assertEquals(1, CategoryMastery.effectiveLevel(high, QuizDifficulty.Easy))
        assertEquals(5, CategoryMastery.effectiveLevel(high, QuizDifficulty.Hard))
        // Wybor trudniejszy niz wlasny poziom tematu nie kupuje mnoznika ponad ten poziom.
        assertEquals(2, CategoryMastery.effectiveLevel(level = 2, fixed = QuizDifficulty.Expert))

        assertTrue(
            CategoryMastery.rewardMultiplier(CategoryMastery.effectiveLevel(high, QuizDifficulty.Easy)) == 1f,
            "Rozgrzewka nie ma placic premii wysokiego poziomu"
        )
    }

    /** Trenowac latwiejszy szczebel wolno - ale to nie jest droga do awansu tematu. */
    @Test
    fun `awans nalezy sie tylko za wlasny poziom tematu`() {
        assertTrue(CategoryMastery.countsForMastery(level = 5, fixed = null))
        assertTrue(CategoryMastery.countsForMastery(level = 5, fixed = QuizDifficulty.Hard))
        assertTrue(CategoryMastery.countsForMastery(level = 5, fixed = QuizDifficulty.Expert))
        assertTrue(!CategoryMastery.countsForMastery(level = 5, fixed = QuizDifficulty.Easy))
        assertTrue(!CategoryMastery.countsForMastery(level = 5, fixed = QuizDifficulty.Medium))
        // Temat nietkniety: kazdy wybor jest jego wlasnym poziomem albo wyzej.
        assertTrue(CategoryMastery.countsForMastery(level = 1, fixed = QuizDifficulty.Easy))
    }

    @Test
    fun `komplet fal domyka poziom`() {
        assertTrue(!CategoryMastery.clearsLevel(CategoryMastery.WAVES_PER_LEVEL - 1))
        assertTrue(CategoryMastery.clearsLevel(CategoryMastery.WAVES_PER_LEVEL))
    }
}
