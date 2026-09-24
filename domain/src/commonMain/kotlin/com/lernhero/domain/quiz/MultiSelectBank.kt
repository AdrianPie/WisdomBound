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

    /** Tylko pytania jeszcze nie zadane - patrz [drawFresh]. Null = walka bierze zwykle pytanie. */
    fun draw(
        difficulty: QuizDifficulty,
        category: QuizCategory? = null,
        random: Random = Random.Default,
        seen: (String) -> Boolean = { false }
    ): MultiSelectEntry? = all.drawFresh(difficulty, category, random, seen, { it.id }, { it.category }, { it.difficulty })
}
