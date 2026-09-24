package com.lernhero.domain.quiz

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
    ): TrueFalseEntry? = all.drawFresh(difficulty, category, random, seen, { it.id }, { it.category }, { it.difficulty })
}
