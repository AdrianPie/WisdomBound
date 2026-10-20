package com.lernhero.domain.quiz

import com.lernhero.domain.quiz.bank.CountryQuestions
import com.lernhero.domain.quiz.bank.TrueFalseQuestions
import kotlin.random.Random

/** Pula pytan "prawda czy falsz" - patrz [TrueFalseEntry]. */
object TrueFalseBank {

    val all: List<TrueFalseEntry> = TrueFalseQuestions.all

    /** Tylko pytania jeszcze nie zadane - patrz [drawFresh]. Null = walka bierze zwykle pytanie. */
    fun draw(
        difficulty: QuizDifficulty,
        category: QuizCategory? = null,
        random: Random = Random.Default,
        seen: (String) -> Boolean = { false }
    ): TrueFalseEntry? = pool(category, random).drawFresh(difficulty, category, random, seen, { it.id }, { it.category }, { it.difficulty })

    /**
     * Pula losowania: [all] plus zdania "wyzej czy nizej" z danych o panstwach - te powstaja przy
     * kazdym losowaniu od nowa, wiec w geografii ten typ tury sie nie konczy. Dla innych tematow
     * nie ma ich po co budowac.
     */
    private fun pool(category: QuizCategory?, random: Random): List<TrueFalseEntry> =
        if (category == null || category == QuizCategory.Geography) all + CountryQuestions.trueFalse(random) else all
}
