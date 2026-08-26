package com.lernhero.domain.progress

import com.lernhero.domain.quiz.QuizCategory
import kotlinx.serialization.Serializable
import kotlin.math.roundToInt

/** Celnosc w jednej kategorii pytan - z tego liczy sie caly ekran "czego sie uczysz". */
@Serializable
data class CategoryStat(
    val answered: Int = 0,
    val correct: Int = 0
) {
    val accuracy: Float get() = if (answered <= 0) 0f else correct.toFloat() / answered
    val accuracyPercent: Int get() = (accuracy * 100).roundToInt()

    operator fun plus(other: CategoryStat): CategoryStat =
        CategoryStat(answered = answered + other.answered, correct = correct + other.correct)
}

/**
 * Wszystko, co gracz zrobil w grze od poczatku konta. Jedno miejsce zamiast licznikow rozrzuconych
 * po ekranach - i jedno zrodlo dla osiagniec ([AchievementCatalog]), ktore nie potrzebuja wlasnego
 * zapisu, bo licza sie wprost z tych liczb.
 *
 * [perCategory] jest kluczowane nazwa [QuizCategory] (a nie samym enumem), zeby dopisanie kategorii
 * nie wysadzalo deserializacji starych kont.
 */
@Serializable
data class GameStats(
    val questionsAnswered: Int = 0,
    val questionsCorrect: Int = 0,
    val perCategory: Map<String, CategoryStat> = emptyMap(),
    val bestAnswerStreak: Int = 0,
    val battlesWon: Int = 0,
    val battlesLost: Int = 0,
    val bossesDefeated: Int = 0,
    val dungeonsCleared: Int = 0,
    val bestEndlessWave: Int = 0,
    val totalWavesCleared: Int = 0,
    val goldEarned: Int = 0,
    val itemsCrafted: Int = 0,
    val monstersSummoned: Int = 0
) {
    val accuracy: Float get() = if (questionsAnswered <= 0) 0f else questionsCorrect.toFloat() / questionsAnswered
    val accuracyPercent: Int get() = (accuracy * 100).roundToInt()
    val questionsWrong: Int get() = (questionsAnswered - questionsCorrect).coerceAtLeast(0)

    fun statFor(category: QuizCategory): CategoryStat = perCategory[category.name] ?: CategoryStat()

    /** Kategorie, w ktorych gracz cokolwiek odpowiadal - posortowane od najslabszej celnosci. */
    fun weakestCategories(minAnswers: Int = 5): List<Pair<QuizCategory, CategoryStat>> =
        QuizCategory.entries
            .map { it to statFor(it) }
            .filter { (_, stat) -> stat.answered >= minAnswers }
            .sortedBy { (_, stat) -> stat.accuracy }

    /**
     * Dokleja wynik jednego zejscia. Liczniki sie sumuja, rekordy biora maksimum - to jedyne
     * miejsce, w ktorym statystyki konta rosna, wiec nie da sie ich przypadkiem nadpisac.
     */
    operator fun plus(run: RunStats): GameStats {
        val mergedCategories = perCategory.toMutableMap()
        run.perCategory.forEach { (key, stat) ->
            mergedCategories[key] = (mergedCategories[key] ?: CategoryStat()) + stat
        }

        return copy(
            questionsAnswered = questionsAnswered + run.questionsAnswered,
            questionsCorrect = questionsCorrect + run.questionsCorrect,
            perCategory = mergedCategories,
            bestAnswerStreak = maxOf(bestAnswerStreak, run.bestAnswerStreak),
            battlesWon = battlesWon + if (run.countsAsBattle && run.won) 1 else 0,
            battlesLost = battlesLost + if (run.countsAsBattle && !run.won) 1 else 0,
            bossesDefeated = bossesDefeated + if (run.bossDefeated) 1 else 0,
            dungeonsCleared = dungeonsCleared + if (run.dungeonCleared) 1 else 0,
            bestEndlessWave = maxOf(bestEndlessWave, run.bestEndlessWave),
            totalWavesCleared = totalWavesCleared + run.wavesCleared,
            goldEarned = goldEarned + run.gold
        )
    }
}

/**
 * Wynik JEDNEGO zejscia - to, co ekran koncowy pokazuje graczowi i co dopisuje sie do [GameStats].
 * Zbiera go walka ([com.lernhero.domain.run.RunSession]), a zapisuje repozytorium jednym zapisem:
 * XP, statystyki, zloto i surowce zmieniaja sie razem, wiec nie ma sensu wysylac tego osobno.
 */
@Serializable
data class RunStats(
    val won: Boolean = false,
    val endless: Boolean = false,
    val wavesCleared: Int = 0,
    val bestEndlessWave: Int = 0,
    val bossDefeated: Boolean = false,
    val dungeonCleared: Boolean = false,
    val questionsAnswered: Int = 0,
    val questionsCorrect: Int = 0,
    val bestAnswerStreak: Int = 0,
    val perCategory: Map<String, CategoryStat> = emptyMap(),
    val gold: Int = 0,
    val xp: Int = 0,
    val wood: Int = 0,
    val stone: Int = 0,
    val iron: Int = 0,
    val essence: Int = 0,
    val summonTickets: Int = 0,
    /**
     * Czy to rozliczenie liczy sie jako STOCZONA walka. Ukonczenie lochu tez przechodzi ta sama
     * droga (jeden zapis, te same liczniki), ale nie jest walka - bez tego kazde wyjscie z lochu
     * dopisywaloby fikcyjna wygrana albo przegrana do statystyk.
     */
    val countsAsBattle: Boolean = true
) {
    val accuracyPercent: Int
        get() = if (questionsAnswered <= 0) 0 else (questionsCorrect * 100f / questionsAnswered).roundToInt()

    val anyResources: Boolean get() = wood > 0 || stone > 0 || iron > 0 || essence > 0 || summonTickets > 0
}
