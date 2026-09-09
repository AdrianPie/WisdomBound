package com.lernhero.domain.quiz

import com.lernhero.domain.quiz.bank.MultiSelectQuestions
import com.lernhero.domain.quiz.bank.MultiSelectQuestionsTrivia
import com.lernhero.domain.quiz.bank.MultiSelectQuestionsWorld
import kotlin.random.Random

/** Pula pytan "zaznacz wszystkie prawdziwe" - patrz [MultiSelectEntry]. */
object MultiSelectBank {

    val all: List<MultiSelectEntry> = MultiSelectQuestions.all +
        // Druga partia, bez watkow lokalnych - patrz [MultiSelectQuestionsWorld].
        MultiSelectQuestionsWorld.all +
        // Partia po audycie - dystraktory, ktore sa POPULARNYMI bledami. Patrz [MultiSelectQuestionsTrivia].
        MultiSelectQuestionsTrivia.all

    /**
     * Losuje pytanie na dany poziom, opcjonalnie z jednej kategorii.
     *
     * Gdy dokladne dopasowanie (kategoria + poziom) nie ma pokrycia w banku, schodzimy najpierw do
     * samej kategorii, a dopiero potem do calej puli - zwrocenie null zamiast pytania zablokowaloby
     * ture, a to gorsze niz pytanie o oczko latwiejsze.
     */
    fun draw(
        difficulty: QuizDifficulty,
        category: QuizCategory? = null,
        random: Random = Random.Default
    ): MultiSelectEntry? {
        if (all.isEmpty()) return null
        val byCategory = if (category == null) all else all.filter { it.category == category }
        return byCategory.filter { it.difficulty == difficulty }.randomOrNull(random)
            ?: byCategory.randomOrNull(random)
            ?: all.randomOrNull(random)
    }
}
