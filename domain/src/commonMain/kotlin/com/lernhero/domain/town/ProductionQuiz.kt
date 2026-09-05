package com.lernhero.domain.town

import com.lernhero.domain.quiz.QuizDifficulty

/**
 * Quiz podbijajacy produkcje budynku: piec szybkich pytan raz na dobe, a od liczby trafien zalezy,
 * jak mocny bedzie bonus na kolejne 24h.
 *
 * "Raz na 24h" nie potrzebuje wlasnego licznika - bonus i tak trwa [ProductionMath.BOOST_DURATION_MS],
 * wiec sam [ProductionMath.isBoostActive] jest ta blokada. Wazny skutek uboczny: quiz zaliczony na
 * ZERO tez startuje okno (mnoznik 1.0), czyli spalasz dzienna probe. To jest cala stawka tego quizu -
 * bez tego nie byloby czym ryzykowac i pytania bylyby dekoracja.
 */
object ProductionQuiz {

    /** Ile pytan w jednym podejsciu. Piec - tyle, ile da sie zrobic "przy okazji", bez sesji nauki. */
    const val QUESTION_COUNT = 5

    /**
     * Drabinka trudnosci podejscia. Dwa latwe na rozgrzewke, potem srednie, na koncu jedno trudne -
     * dzieki temu 3/5 jest w zasiegu kazdego, a komplet wymaga juz wiedzy.
     */
    val LADDER: List<QuizDifficulty> = listOf(
        QuizDifficulty.Easy,
        QuizDifficulty.Easy,
        QuizDifficulty.Medium,
        QuizDifficulty.Medium,
        QuizDifficulty.Hard
    )

    /**
     * Bonus do produkcji za [correct] trafien z [QUESTION_COUNT].
     *
     * Skala jest celowo wypukla (rosnie coraz szybciej): dawny, darmowy bonus wynosil +25%, wiec
     * dopiero 4/5 przebija to, co gracz dostawal wczesniej za samo klikniecie. Komplet daje +40%,
     * czyli realnie warto sie postarac, a nie tylko przeklikac.
     */
    fun boostPercentFor(correct: Int): Int = when (correct.coerceIn(0, QUESTION_COUNT)) {
        0 -> 0
        1 -> 5
        2 -> 12
        3 -> 20
        4 -> 30
        else -> 40
    }

    /** Najwyzszy mozliwy bonus - pokazywany na przycisku, zeby bylo widac, o co sie gra. */
    val MAX_BOOST_PERCENT: Int = boostPercentFor(QUESTION_COUNT)
}
