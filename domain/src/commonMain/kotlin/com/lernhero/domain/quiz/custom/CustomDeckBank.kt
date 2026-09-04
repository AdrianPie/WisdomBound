package com.lernhero.domain.quiz.custom

import com.lernhero.domain.quiz.EstimationEntry
import com.lernhero.domain.quiz.QuizDifficulty
import kotlin.random.Random

/**
 * Losowanie pytan z wlasnej talii - to samo, czym dla wbudowanego banku jest
 * [com.lernhero.domain.quiz.QuizBank], a dla fiszek [com.lernhero.domain.quiz.WordQuizBank].
 *
 * Zasada jest ta sama co przy slowkach: skoro gracz wybral swoja talie, to endless pyta TYLKO
 * z niej. Zejscie na wbudowany bank byloby klamstwem wzgledem "cwicze na prawo jazdy".
 */
object CustomDeckBank {

    /**
     * Losuje pytanie na ture. [preferred] to poziom wybrany przed walka (albo z drabinki fali) -
     * gdy talia nie ma pytania na tym progu, schodzimy na najblizszy zamiast nie oddac nic:
     * wlasna talia bywa cala na jednym poziomie i nie moze przez to blokowac tury.
     */
    fun draw(
        deck: CustomDeck,
        preferred: QuizDifficulty? = null,
        random: Random = Random.Default
    ): CustomQuestion? {
        val pool = deck.turnReady
        if (pool.isEmpty()) return null
        if (preferred == null) return pool.random(random)

        val byDifficulty = pool.groupBy { it.difficulty }
        QuizDifficulty.entries
            .sortedBy { candidate -> kotlin.math.abs(candidate.ordinal - preferred.ordinal) }
            .forEach { level ->
                val candidates = byDifficulty[level].orEmpty()
                if (candidates.isNotEmpty()) return candidates.random(random)
            }
        return null
    }

    /**
     * Pytanie "oszacuj" na zdarzenie elity/bossa. Null, gdy gracz nie napisal ani jednego takiego -
     * wolajacy siega wtedy po wbudowany [com.lernhero.domain.quiz.EstimationBank], bo boss musi
     * miec czym zaatakowac niezaleznie od ksztaltu talii.
     */
    fun drawEstimation(deck: CustomDeck, random: Random = Random.Default): EstimationEntry? =
        deck.ready
            .filter { it.type == CustomQuestionType.Estimation }
            .randomOrNull(random)
            ?.toEstimationEntry(deck.id)

    /**
     * Id pytania widziane przez walke. Niesie w sobie id talii i id pytania, dzieki czemu ekran
     * moze pozniej odnotowac wynik na konkretnym pytaniu, nie wiedzac nic o [CustomDeck] - tak
     * samo, jak dziala prefiks fiszek w [com.lernhero.domain.quiz.WordQuizBank].
     */
    fun entryId(deckId: String, questionId: String): String = "$ENTRY_PREFIX$deckId$SEPARATOR$questionId"

    /** Rozklada id z [entryId] z powrotem na (idTalii, idPytania) - null, gdy pytanie nie jest z talii. */
    fun sourceOf(quizEntryId: String): Pair<String, String>? {
        if (!quizEntryId.startsWith(ENTRY_PREFIX)) return null
        val body = quizEntryId.removePrefix(ENTRY_PREFIX)
        val separator = body.indexOf(SEPARATOR)
        if (separator <= 0 || separator == body.lastIndex) return null
        return body.substring(0, separator) to body.substring(separator + SEPARATOR.length)
    }

    private const val ENTRY_PREFIX = "deck_"

    // Podkreslnik nie wchodzi w gre - id talii i pytan sa generowane z czasu i licznika, a te
    // moglyby go zawierac. Dwukropek w zadnym z nich nie wystepuje.
    private const val SEPARATOR = ":"
}
