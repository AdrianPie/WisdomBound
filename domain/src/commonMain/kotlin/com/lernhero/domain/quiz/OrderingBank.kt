package com.lernhero.domain.quiz

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
    ): OrderingEntry? = all.drawFresh(difficulty, category, random, seen, { it.id }, { it.category }, { it.difficulty })
}
