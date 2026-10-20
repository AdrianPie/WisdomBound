package com.lernhero.domain.quiz.bank

import com.lernhero.domain.quiz.QuizCategory
import com.lernhero.domain.quiz.QuizDifficulty
import com.lernhero.domain.quiz.QuizEntry
import kotlin.random.Random

/**
 * Flags. The whole category sits at hard and expert - recognizing a flag is harder than naming
 * the capital of France, so there's nothing to look for among the easy questions.
 *
 * Wrong answers are drawn only when the question is built, from the same pool of countries. That
 * way one flag gives a different set of traps every time, and the bank doesn't bloat with
 * hand-typed distractors.
 */
object FlagQuestions {

    /** Country code + name. The UI layer adds the image, since the domain doesn't know about art assets. */
    data class FlagFact(
        val code: String,
        val country: String,
        val difficulty: QuizDifficulty
    )

    val facts: List<FlagFact> = listOf(
        // Flags most people recognize - but you still have to tell them apart from similar ones.
        FlagFact("pl", "Poland", QuizDifficulty.Hard),
        FlagFact("de", "Germany", QuizDifficulty.Hard),
        FlagFact("fr", "France", QuizDifficulty.Hard),
        FlagFact("es", "Spain", QuizDifficulty.Hard),
        FlagFact("it", "Italy", QuizDifficulty.Hard),
        FlagFact("pt", "Portugal", QuizDifficulty.Hard),
        FlagFact("gb", "United Kingdom", QuizDifficulty.Hard),
        FlagFact("us", "United States", QuizDifficulty.Hard),
        FlagFact("ca", "Canada", QuizDifficulty.Hard),
        FlagFact("br", "Brazil", QuizDifficulty.Hard),
        FlagFact("cn", "China", QuizDifficulty.Hard),
        FlagFact("jp", "Japan", QuizDifficulty.Hard),
        FlagFact("au", "Australia", QuizDifficulty.Hard),
        FlagFact("no", "Norway", QuizDifficulty.Hard),
        FlagFact("se", "Sweden", QuizDifficulty.Hard),
        FlagFact("dk", "Denmark", QuizDifficulty.Hard),
        FlagFact("nl", "Netherlands", QuizDifficulty.Hard),
        FlagFact("ch", "Switzerland", QuizDifficulty.Hard),
        FlagFact("cz", "Czechia", QuizDifficulty.Hard),
        FlagFact("gr", "Greece", QuizDifficulty.Hard),
        FlagFact("tr", "Turkey", QuizDifficulty.Hard),
        FlagFact("ua", "Ukraine", QuizDifficulty.Hard),
        FlagFact("mx", "Mexico", QuizDifficulty.Hard),
        FlagFact("ar", "Argentina", QuizDifficulty.Hard),
        FlagFact("kr", "South Korea", QuizDifficulty.Hard),
        FlagFact("ie", "Ireland", QuizDifficulty.Hard),
        FlagFact("at", "Austria", QuizDifficulty.Hard),
        FlagFact("fi", "Finland", QuizDifficulty.Hard),
        FlagFact("be", "Belgium", QuizDifficulty.Hard),

        // This is where the flags people mix up most often start.
        FlagFact("sk", "Slovakia", QuizDifficulty.Expert),
        FlagFact("hu", "Hungary", QuizDifficulty.Expert),
        FlagFact("ro", "Romania", QuizDifficulty.Expert),
        FlagFact("bg", "Bulgaria", QuizDifficulty.Expert),
        FlagFact("nz", "New Zealand", QuizDifficulty.Expert),
        FlagFact("eg", "Egypt", QuizDifficulty.Expert),
        FlagFact("ma", "Morocco", QuizDifficulty.Expert),
        FlagFact("ng", "Nigeria", QuizDifficulty.Expert),
        FlagFact("ke", "Kenya", QuizDifficulty.Expert),
        FlagFact("za", "South Africa", QuizDifficulty.Expert),
        FlagFact("th", "Thailand", QuizDifficulty.Expert),
        FlagFact("vn", "Vietnam", QuizDifficulty.Expert),
        FlagFact("ph", "Philippines", QuizDifficulty.Expert),
        FlagFact("sg", "Singapore", QuizDifficulty.Expert)
    )

    /**
     * Builds flag questions, drawing three wrong answers for each from the remaining countries.
     * Randomization goes through [random], so the quiz is reproducible with the same seed.
     */
    fun entries(random: Random): List<QuizEntry> = facts.map { fact ->
        val distractors = facts
            .filter { it.code != fact.code }
            .shuffled(random)
            .take(3)
            .map { it.country }

        QuizEntry(
            id = "flag_${fact.code}",
            prompt = "Which country does this flag belong to?",
            correct = fact.country,
            wrong = distractors,
            category = QuizCategory.Flags,
            difficulty = fact.difficulty,
            flagCode = fact.code,
            generated = true
        )
    }
}
