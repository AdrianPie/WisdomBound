package com.lernhero.domain.quiz

import com.lernhero.domain.quiz.bank.CountryQuestions
import com.lernhero.domain.quiz.bank.OrderingQuestions
import kotlin.random.Random

/** Bank pytan typu "ulóz w kolejnosci" - losowanie dziala jak w [QuizBank]: trzyma sie zadanego poziomu, a gdy go brakuje, schodzi na najblizszy. */
object OrderingBank {

    val all: List<OrderingEntry> = OrderingQuestions.all

    /** Tylko pytania jeszcze nie zadane - patrz [drawFresh]. Null = walka bierze zwykle pytanie. */
    fun draw(
        difficulty: QuizDifficulty,
        category: QuizCategory? = null,
        random: Random = Random.Default,
        seen: (String) -> Boolean = { false }
    ): OrderingEntry? = pool(category, random).drawFresh(difficulty, category, random, seen, { it.id }, { it.category }, { it.difficulty })

    /**
     * Pula losowania: [all] plus ukladanki z danych o panstwach - te powstaja przy
     * kazdym losowaniu od nowa, wiec w geografii ten typ tury sie nie konczy. Dla innych tematow
     * nie ma ich po co budowac.
     */
    private fun pool(category: QuizCategory?, random: Random): List<OrderingEntry> =
        if (category == null || category == QuizCategory.Geography) all + CountryQuestions.ordering(random) else all
}
