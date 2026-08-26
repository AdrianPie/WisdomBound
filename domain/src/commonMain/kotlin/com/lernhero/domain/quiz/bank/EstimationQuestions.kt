package com.lernhero.domain.quiz.bank

import com.lernhero.domain.quiz.EstimationEntry

/**
 * Pula testowa "zgadnij rok" dla specjalnego zdarzenia bossa - wszystkie na wspolnej skali suwaka
 * (patrz [ESTIMATION_YEAR_MIN]/[ESTIMATION_YEAR_MAX]), zeby UI mogl uzyc jednego, stalego suwaka
 * niezaleznie od wylosowanego pytania.
 */
const val ESTIMATION_YEAR_MIN = -1000
const val ESTIMATION_YEAR_MAX = 2025

object EstimationQuestions {
    val all: List<EstimationEntry> = listOf(
        EstimationEntry(
            id = "est_genghis_khan_birth",
            prompt = "W ktorym roku urodzil sie Czyngis-chan?",
            minValue = ESTIMATION_YEAR_MIN,
            maxValue = ESTIMATION_YEAR_MAX,
            correctValue = 1162,
            unit = "rok"
        ),
        EstimationEntry(
            id = "est_islam_founding",
            prompt = "W ktorym roku Mahomet otrzymal pierwsze objawienie, dajace poczatek islamowi?",
            minValue = ESTIMATION_YEAR_MIN,
            maxValue = ESTIMATION_YEAR_MAX,
            correctValue = 610,
            unit = "rok"
        ),
        EstimationEntry(
            id = "est_rome_fall",
            prompt = "W ktorym roku upadlo Cesarstwo Zachodniorzymskie?",
            minValue = ESTIMATION_YEAR_MIN,
            maxValue = ESTIMATION_YEAR_MAX,
            correctValue = 476,
            unit = "rok"
        ),
        EstimationEntry(
            id = "est_french_revolution",
            prompt = "W ktorym roku wybuchla Rewolucja Francuska?",
            minValue = ESTIMATION_YEAR_MIN,
            maxValue = ESTIMATION_YEAR_MAX,
            correctValue = 1789,
            unit = "rok"
        ),
        EstimationEntry(
            id = "est_moon_landing",
            prompt = "W ktorym roku czlowiek pierwszy raz wyladowal na Ksiezycu?",
            minValue = ESTIMATION_YEAR_MIN,
            maxValue = ESTIMATION_YEAR_MAX,
            correctValue = 1969,
            unit = "rok"
        ),
        EstimationEntry(
            id = "est_ww1_start",
            prompt = "W ktorym roku wybuchla Pierwsza Wojna Swiatowa?",
            minValue = ESTIMATION_YEAR_MIN,
            maxValue = ESTIMATION_YEAR_MAX,
            correctValue = 1914,
            unit = "rok"
        ),
        EstimationEntry(
            id = "est_magna_carta",
            prompt = "W ktorym roku podpisano Magna Carte?",
            minValue = ESTIMATION_YEAR_MIN,
            maxValue = ESTIMATION_YEAR_MAX,
            correctValue = 1215,
            unit = "rok"
        ),
        EstimationEntry(
            id = "est_columbus_america",
            prompt = "W ktorym roku Kolumb dotarl do Ameryki?",
            minValue = ESTIMATION_YEAR_MIN,
            maxValue = ESTIMATION_YEAR_MAX,
            correctValue = 1492,
            unit = "rok"
        )
    )
}
