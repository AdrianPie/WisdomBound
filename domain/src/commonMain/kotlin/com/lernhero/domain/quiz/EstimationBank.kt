package com.lernhero.domain.quiz

import com.lernhero.domain.quiz.bank.EstimationQuestions
import com.lernhero.domain.quiz.bank.EstimationQuestionsMeasures
import com.lernhero.domain.quiz.bank.EstimationQuestionsModern
import com.lernhero.domain.quiz.bank.EstimationQuestionsPopulation
import com.lernhero.domain.quiz.bank.EstimationQuestionsScale
import com.lernhero.domain.quiz.bank.EstimationQuestionsWorld
import kotlin.random.Random

/** Pula pytan "oszacuj wartosc" dla specjalnych zdarzen elit/bossow. */
object EstimationBank {
    val all: List<EstimationEntry> = EstimationQuestions.all +
        // Historia powszechna, dowazona ku dawnym epokom - patrz [EstimationQuestionsWorld].
        EstimationQuestionsWorld.all +
        // Szacowania BEZ dat - zdarzenie bossa przestaje byc kartkowka z historii.
        EstimationQuestionsPopulation.all +
        EstimationQuestionsMeasures.all +
        EstimationQuestionsModern.all +
        // Czwarta pula bez dat - bieg pyta o szacowanie cztery razy na plansze, nie raz.
        EstimationQuestionsScale.all

    /**
     * Pula zawezona do jezyka gracza - patrz [EstimationEntry.localeCode]. Bank trzyma wszystko,
     * filtruje dopiero to wywolanie, tak samo jak przy [com.lernhero.domain.quiz.QuizCategory].
     */
    fun forLanguage(languageCode: String): List<EstimationEntry> =
        all.filter { it.availableIn(languageCode) }

    fun random(random: Random = Random.Default): EstimationEntry = all.random(random)

    fun random(random: Random, languageCode: String): EstimationEntry =
        forLanguage(languageCode).ifEmpty { all }.random(random)
}
