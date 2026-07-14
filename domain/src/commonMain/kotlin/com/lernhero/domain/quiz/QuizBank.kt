package com.lernhero.domain.quiz

import com.lernhero.domain.quiz.bank.AnimalQuestions
import com.lernhero.domain.quiz.bank.BiologyQuestions
import com.lernhero.domain.quiz.bank.CapitalQuestions
import com.lernhero.domain.quiz.bank.ElementQuestions
import com.lernhero.domain.quiz.bank.FlagQuestions
import com.lernhero.domain.quiz.bank.HistoryQuestions
import com.lernhero.domain.quiz.bank.ScienceQuestions
import kotlin.random.Random

/**
 * Bank pytan i losowanie quizu.
 *
 * Quiz dziala jak drabinka w "Milionerach": pierwsze pytanie jest rozgrzewka, ostatnie potrafi
 * zabolec. Im dalsza fala, tym wyzej zaczyna sie drabinka - w fali 5 nie ma juz latwych pytan.
 */
object QuizBank {

    /** Pytania tekstowe. Flagi dochodza osobno, bo losuja sobie bledne odpowiedzi. */
    private val textEntries: List<QuizEntry> =
        BiologyQuestions.all +
            AnimalQuestions.all +
            HistoryQuestions.all +
            ElementQuestions.all +
            CapitalQuestions.all +
            ScienceQuestions.all

    fun all(random: Random = Random.Default): List<QuizEntry> =
        textEntries + FlagQuestions.entries(random)

    /**
     * Drabinka trudnosci dla danej fali. Cztery pytania, coraz ostrzejsze - i coraz ostrzejsze
     * z kazda kolejna fala.
     */
    fun ladderFor(wave: Int): List<QuizDifficulty> = when (wave) {
        1 -> listOf(QuizDifficulty.Easy, QuizDifficulty.Easy, QuizDifficulty.Medium, QuizDifficulty.Medium)
        2 -> listOf(QuizDifficulty.Easy, QuizDifficulty.Medium, QuizDifficulty.Medium, QuizDifficulty.Hard)
        3 -> listOf(QuizDifficulty.Medium, QuizDifficulty.Medium, QuizDifficulty.Hard, QuizDifficulty.Hard)
        4 -> listOf(QuizDifficulty.Medium, QuizDifficulty.Hard, QuizDifficulty.Hard, QuizDifficulty.Expert)
        else -> listOf(QuizDifficulty.Hard, QuizDifficulty.Hard, QuizDifficulty.Expert, QuizDifficulty.Expert)
    }

    /**
     * Losuje quiz na dana fale: jedno pytanie na kazdy szczebel drabinki, bez powtorek.
     *
     * Gdy pula danego poziomu sie wyczerpie (bo gracz dostal juz wszystkie eksperckie),
     * schodzimy o szczebel nizej zamiast zwracac puste pytanie.
     */
    fun drawForWave(wave: Int, random: Random = Random.Default): List<QuizEntry> {
        val pool = all(random).groupBy { it.difficulty }
        val used = mutableSetOf<String>()

        return ladderFor(wave).mapNotNull { difficulty ->
            val entry = pickFrom(difficulty, pool, used, random)
            entry?.also { used += it.id }
        }
    }

    private fun pickFrom(
        difficulty: QuizDifficulty,
        pool: Map<QuizDifficulty, List<QuizEntry>>,
        used: Set<String>,
        random: Random
    ): QuizEntry? {
        // Kolejnosc szukania: zadany poziom, potem coraz latwiejsze, na koncu trudniejsze.
        val fallback = QuizDifficulty.entries.sortedBy { candidate ->
            kotlin.math.abs(candidate.ordinal - difficulty.ordinal)
        }

        fallback.forEach { level ->
            val candidates = pool[level].orEmpty().filter { it.id !in used }
            if (candidates.isNotEmpty()) return candidates[random.nextInt(candidates.size)]
        }
        return null
    }

    fun byDifficulty(difficulty: QuizDifficulty, random: Random = Random.Default): List<QuizEntry> =
        all(random).filter { it.difficulty == difficulty }

    fun byCategory(category: QuizCategory, random: Random = Random.Default): List<QuizEntry> =
        all(random).filter { it.category == category }
}
