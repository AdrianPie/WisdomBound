package com.lernhero.domain.quiz

import com.lernhero.domain.quiz.bank.EstimationQuestions
import kotlin.random.Random

/** Pula pytan "oszacuj wartosc" dla specjalnych zdarzen elit/bossow. */
object EstimationBank {
    val all: List<EstimationEntry> = EstimationQuestions.all

    fun random(random: Random = Random.Default): EstimationEntry = all.random(random)
}
