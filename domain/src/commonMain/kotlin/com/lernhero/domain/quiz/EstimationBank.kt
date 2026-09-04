package com.lernhero.domain.quiz

import com.lernhero.domain.quiz.bank.EstimationQuestions
import com.lernhero.domain.quiz.bank.EstimationQuestionsExtra
import com.lernhero.domain.quiz.bank.EstimationQuestionsExtra2
import com.lernhero.domain.quiz.bank.EstimationQuestionsWorld
import kotlin.random.Random

/** Pula pytan "oszacuj wartosc" dla specjalnych zdarzen elit/bossow. */
object EstimationBank {
    val all: List<EstimationEntry> = EstimationQuestions.all +
        EstimationQuestionsExtra.all +
        EstimationQuestionsExtra2.all +
        // Historia powszechna, dowazona ku dawnym epokom - patrz [EstimationQuestionsWorld].
        EstimationQuestionsWorld.all

    fun random(random: Random = Random.Default): EstimationEntry = all.random(random)
}
