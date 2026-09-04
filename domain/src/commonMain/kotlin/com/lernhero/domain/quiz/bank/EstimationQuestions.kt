package com.lernhero.domain.quiz.bank

import com.lernhero.domain.quiz.EstimationEntry

/**
 * Test pool of "guess the year" questions for the boss special event - all on a shared slider
 * scale (see [ESTIMATION_YEAR_MIN]/[ESTIMATION_YEAR_MAX]), so the UI can use one fixed slider
 * regardless of which question got drawn.
 */
const val ESTIMATION_YEAR_MIN = -3000
const val ESTIMATION_YEAR_MAX = 2025

object EstimationQuestions {
    val all: List<EstimationEntry> = listOf(
        EstimationEntry(
            id = "est_genghis_khan_birth",
            prompt = "In which year was Genghis Khan born?",
            minValue = ESTIMATION_YEAR_MIN,
            maxValue = ESTIMATION_YEAR_MAX,
            correctValue = 1162,
            unit = "year"
        ),
        EstimationEntry(
            id = "est_islam_founding",
            prompt = "In which year did Muhammad receive the first revelation, giving rise to Islam?",
            minValue = ESTIMATION_YEAR_MIN,
            maxValue = ESTIMATION_YEAR_MAX,
            correctValue = 610,
            unit = "year"
        ),
        EstimationEntry(
            id = "est_rome_fall",
            prompt = "In which year did the Western Roman Empire fall?",
            minValue = ESTIMATION_YEAR_MIN,
            maxValue = ESTIMATION_YEAR_MAX,
            correctValue = 476,
            unit = "year"
        ),
        EstimationEntry(
            id = "est_french_revolution",
            prompt = "In which year did the French Revolution break out?",
            minValue = ESTIMATION_YEAR_MIN,
            maxValue = ESTIMATION_YEAR_MAX,
            correctValue = 1789,
            unit = "year"
        ),
        EstimationEntry(
            id = "est_moon_landing",
            prompt = "In which year did humans first land on the Moon?",
            minValue = ESTIMATION_YEAR_MIN,
            maxValue = ESTIMATION_YEAR_MAX,
            correctValue = 1969,
            unit = "year"
        ),
        EstimationEntry(
            id = "est_ww1_start",
            prompt = "In which year did World War I break out?",
            minValue = ESTIMATION_YEAR_MIN,
            maxValue = ESTIMATION_YEAR_MAX,
            correctValue = 1914,
            unit = "year"
        ),
        EstimationEntry(
            id = "est_magna_carta",
            prompt = "In which year was the Magna Carta signed?",
            minValue = ESTIMATION_YEAR_MIN,
            maxValue = ESTIMATION_YEAR_MAX,
            correctValue = 1215,
            unit = "year"
        ),
        EstimationEntry(
            id = "est_columbus_america",
            prompt = "In which year did Columbus reach America?",
            minValue = ESTIMATION_YEAR_MIN,
            maxValue = ESTIMATION_YEAR_MAX,
            correctValue = 1492,
            unit = "year"
        )
    )
}
