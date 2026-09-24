package com.lernhero.domain.quiz

import com.lernhero.domain.quiz.bank.OrderingQuestions
import com.lernhero.domain.quiz.bank.OrderingQuestionsExtra
import com.lernhero.domain.quiz.bank.OrderingQuestionsExtra2
import com.lernhero.domain.quiz.bank.OrderingQuestionsTrivia
import com.lernhero.domain.quiz.bank.OrderingQuestionsWorld
import kotlin.random.Random

/** Bank pytan typu "ulóz w kolejnosci" - losowanie dziala jak w [QuizBank]: trzyma sie zadanego poziomu, a gdy go brakuje, schodzi na najblizszy. */
object OrderingBank {

    val all: List<OrderingEntry> = OrderingQuestions.all + OrderingQuestionsExtra.all + OrderingQuestionsExtra2.all +
        // Rankingi swiatowe - patrz [OrderingQuestionsWorld].
        OrderingQuestionsWorld.all +
        // Partia po audycie - kolejnosc, do ktorej trzeba DOJSC, nie "poranek, poludnie, noc".
        OrderingQuestionsTrivia.all

    /** Tylko pytania jeszcze nie zadane - patrz [drawFresh]. Null = walka bierze zwykle pytanie. */
    fun draw(
        difficulty: QuizDifficulty,
        category: QuizCategory? = null,
        random: Random = Random.Default,
        seen: (String) -> Boolean = { false }
    ): OrderingEntry? = all.drawFresh(difficulty, category, random, seen, { it.id }, { it.category }, { it.difficulty })
}
