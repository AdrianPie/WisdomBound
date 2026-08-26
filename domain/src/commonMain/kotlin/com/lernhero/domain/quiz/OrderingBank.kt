package com.lernhero.domain.quiz

import com.lernhero.domain.quiz.bank.OrderingQuestions
import kotlin.random.Random

/** Bank pytan typu "ulóz w kolejnosci" - losowanie dziala jak w [QuizBank]: trzyma sie zadanego poziomu, a gdy go brakuje, schodzi na najblizszy. */
object OrderingBank {

    val all: List<OrderingEntry> = OrderingQuestions.all

    fun draw(difficulty: QuizDifficulty, category: QuizCategory? = null, random: Random = Random.Default): OrderingEntry? {
        val preferred = if (category == null) all else all.filter { it.category == category }
        val pool = preferred.ifEmpty { all }.groupBy { it.difficulty }

        val fallback = QuizDifficulty.entries.sortedBy { candidate ->
            kotlin.math.abs(candidate.ordinal - difficulty.ordinal)
        }
        fallback.forEach { level ->
            val candidates = pool[level].orEmpty()
            if (candidates.isNotEmpty()) return candidates[random.nextInt(candidates.size)]
        }
        return null
    }
}
