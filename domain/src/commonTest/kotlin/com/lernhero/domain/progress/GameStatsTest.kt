package com.lernhero.domain.progress

import com.lernhero.domain.quiz.QuizCategory
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class GameStatsTest {

    private fun run(
        won: Boolean = true,
        answered: Int = 10,
        correct: Int = 7,
        streak: Int = 4,
        waves: Int = 3,
        gold: Int = 100,
        countsAsBattle: Boolean = true,
        dungeonCleared: Boolean = false,
        category: QuizCategory = QuizCategory.Biology
    ) = RunStats(
        won = won,
        wavesCleared = waves,
        questionsAnswered = answered,
        questionsCorrect = correct,
        bestAnswerStreak = streak,
        perCategory = mapOf(category.name to CategoryStat(answered = answered, correct = correct)),
        gold = gold,
        countsAsBattle = countsAsBattle,
        dungeonCleared = dungeonCleared
    )

    @Test
    fun `liczniki sie sumuja a rekordy biora maksimum`() {
        val stats = GameStats() + run(streak = 4) + run(streak = 9) + run(streak = 2)

        assertEquals(30, stats.questionsAnswered)
        assertEquals(21, stats.questionsCorrect)
        assertEquals(9, stats.bestAnswerStreak)
        assertEquals(9, stats.totalWavesCleared)
        assertEquals(300, stats.goldEarned)
        assertEquals(3, stats.battlesWon)
        assertEquals(0, stats.battlesLost)
    }

    @Test
    fun `kategorie sumuja sie osobno`() {
        val stats = GameStats() +
            run(category = QuizCategory.Biology, answered = 10, correct = 8) +
            run(category = QuizCategory.History, answered = 10, correct = 3) +
            run(category = QuizCategory.Biology, answered = 10, correct = 6)

        assertEquals(20, stats.statFor(QuizCategory.Biology).answered)
        assertEquals(14, stats.statFor(QuizCategory.Biology).correct)
        assertEquals(30, stats.statFor(QuizCategory.History).accuracyPercent)
        assertEquals(0, stats.statFor(QuizCategory.Math).answered)
    }

    @Test
    fun `ukonczony loch nie liczy sie jako walka`() {
        val stats = GameStats() + run(won = true, countsAsBattle = false, dungeonCleared = true, gold = 0)

        assertEquals(0, stats.battlesWon)
        assertEquals(0, stats.battlesLost)
        assertEquals(1, stats.dungeonsCleared)
    }

    @Test
    fun `najslabsze kategorie ida od najgorszej i pomijaja male proby`() {
        val stats = GameStats() +
            run(category = QuizCategory.Biology, answered = 10, correct = 9) +
            run(category = QuizCategory.History, answered = 10, correct = 2) +
            run(category = QuizCategory.Math, answered = 2, correct = 0)

        val weakest = stats.weakestCategories(minAnswers = 5)
        assertEquals(QuizCategory.History, weakest.first().first)
        assertTrue(weakest.none { it.first == QuizCategory.Math }, "za mala proba nie powinna sie liczyc")
    }

    @Test
    fun `osiagniecia licza sie wprost ze statystyk`() {
        val fresh = GameStats()
        val firstWin = AchievementCatalog.all.first { it.id == "wins_1" }
        assertFalse(firstWin.isUnlocked(fresh))

        val afterWin = fresh + run()
        assertTrue(firstWin.isUnlocked(afterWin))
        assertTrue(AchievementCatalog.unlockedCount(afterWin) >= 1)
    }

    @Test
    fun `postep osiagniecia nie przekracza celu`() {
        val stats = GameStats() + run(answered = 10_000, correct = 10_000, gold = 0)
        AchievementCatalog.all.forEach { achievement ->
            assertTrue(
                achievement.progress(stats) <= achievement.target,
                "${achievement.id}: ${achievement.progress(stats)} > ${achievement.target}"
            )
        }
    }

    @Test
    fun `id osiagniec sa unikalne`() {
        val ids = AchievementCatalog.all.map { it.id }
        assertEquals(ids.size, ids.toSet().size)
    }
}
