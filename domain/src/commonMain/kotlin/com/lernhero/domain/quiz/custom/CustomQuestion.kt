package com.lernhero.domain.quiz.custom

import com.lernhero.domain.quiz.EstimationEntry
import com.lernhero.domain.quiz.MultiSelectEntry
import com.lernhero.domain.quiz.OrderingEntry
import com.lernhero.domain.quiz.QuizCategory
import com.lernhero.domain.quiz.QuizDifficulty
import com.lernhero.domain.quiz.QuizEntry
import com.lernhero.domain.quiz.defaultYearTolerance
import kotlinx.serialization.Serializable

/** Ktory z czterech rodzajow pytania gracz wlasnie ulozyl - patrz [CustomQuestion]. */
@Serializable
enum class CustomQuestionType(val label: String, val hint: String) {
    Choice("One answer", "One correct, the rest wrong"),
    MultiSelect("Multiple answers", "Select all that are true"),
    Ordering("Ordering", "Arrange the items in the right order"),
    Estimation("Estimate", "Slider - only appears against an elite/boss")
}

/**
 * Jedno pytanie z wlasnej talii gracza.
 *
 * Celowo JEDEN plaski typ z polem [type], a nie cztery osobne klasy w hierarchii: to leci prosto
 * do Firestore (polimorfizm wymagalby wlasnej konfiguracji serializatora po obu stronach), przez
 * jeden edytor w UI i przez jedna liste do walidacji. Cena jest taka, ze czesc pol jest pusta
 * przy czesci typow - i to jest dokladnie to, czego pilnuje [issues].
 *
 * Zamiana na pytanie, ktore rozumie walka, dzieje sie w [toQuizEntry] i sasiadach - dzieki temu
 * silnik quizu nie wie, ze pytanie napisal gracz (albo model), a nie autor wbudowanego banku.
 */
@Serializable
data class CustomQuestion(
    val id: String,
    val type: CustomQuestionType = CustomQuestionType.Choice,
    val prompt: String = "",
    val difficulty: QuizDifficulty = QuizDifficulty.Medium,
    /** [CustomQuestionType.Choice]: dokladnie jedna. [CustomQuestionType.MultiSelect]: co najmniej jedna. */
    val correct: List<String> = emptyList(),
    /** Dystraktory - dla obu typow z odpowiedziami do wyboru. */
    val wrong: List<String> = emptyList(),
    /** [CustomQuestionType.Ordering]: elementy zapisane JUZ w poprawnej kolejnosci (UI je tasuje). */
    val items: List<String> = emptyList(),
    /** [CustomQuestionType.Estimation]: szukana wartosc i widelki skali. */
    val correctValue: Int? = null,
    val minValue: Int? = null,
    val maxValue: Int? = null,
    val unit: String = "",
    /**
     * Ile jednostek obok wciaz liczy sie za trafienie. Null = wyliczana z epoki, jak w banku
     * wbudowanym (patrz [defaultYearTolerance]) - autor pytania nie musi jej rozumiec, zeby
     * pytanie dzialalo sensownie.
     */
    val tolerance: Int? = null
) {

    /**
     * Co jest nie tak z tym pytaniem - pusta lista znaczy "gotowe do gry".
     *
     * Komunikaty sa po polsku i wprost dla gracza, bo ta sama lista wisi pod formularzem w
     * edytorze i pod pytaniem wygenerowanym przez AI (tam odsiewa smieci, zanim wejda do talii).
     */
    fun issues(): List<String> {
        val problems = mutableListOf<String>()
        if (prompt.isBlank()) problems += "Brak tresci pytania."

        when (type) {
            CustomQuestionType.Choice -> {
                val good = correct.filter { it.isNotBlank() }
                val bad = wrong.filter { it.isNotBlank() }
                if (good.size != 1) problems += "Wybor jednej odpowiedzi wymaga DOKLADNIE jednej poprawnej."
                if (bad.size < MIN_WRONG_ANSWERS) {
                    problems += "Za malo blednych odpowiedzi (min. $MIN_WRONG_ANSWERS)."
                }
                if (bad.size > MAX_WRONG_ANSWERS) {
                    problems += "Za duzo blednych odpowiedzi (max. $MAX_WRONG_ANSWERS)."
                }
                if (hasDuplicateAnswers(good + bad)) problems += "Odpowiedzi sie powtarzaja."
            }

            CustomQuestionType.MultiSelect -> {
                val good = correct.filter { it.isNotBlank() }
                val bad = wrong.filter { it.isNotBlank() }
                if (good.isEmpty()) problems += "Zaznacz przynajmniej jedna poprawna odpowiedz."
                if (bad.isEmpty()) problems += "Potrzebna jest przynajmniej jedna bledna odpowiedz."
                if (good.size + bad.size > MAX_OPTIONS) problems += "Za duzo opcji (max. $MAX_OPTIONS)."
                if (hasDuplicateAnswers(good + bad)) problems += "Opcje sie powtarzaja."
            }

            CustomQuestionType.Ordering -> {
                val steps = items.filter { it.isNotBlank() }
                if (steps.size < MIN_ORDERING_ITEMS) {
                    problems += "Ukladanka potrzebuje min. $MIN_ORDERING_ITEMS elementow."
                }
                if (steps.size > MAX_ORDERING_ITEMS) {
                    problems += "Ukladanka miesci max. $MAX_ORDERING_ITEMS elementow."
                }
                if (hasDuplicateAnswers(steps)) problems += "Elementy sie powtarzaja."
            }

            CustomQuestionType.Estimation -> {
                val value = correctValue
                val min = minValue
                val max = maxValue
                if (value == null || min == null || max == null) {
                    problems += "Podaj szukana wartosc oraz zakres od-do."
                } else {
                    if (min >= max) problems += "Zakres musi rosnac: \"od\" mniejsze niz \"do\"."
                    if (value < min || value > max) problems += "Szukana wartosc lezy poza zakresem."
                }
                tolerance?.let { if (it <= 0) problems += "Tolerancja musi byc dodatnia." }
            }
        }
        return problems
    }

    val isValid: Boolean get() = issues().isEmpty()

    /** Wszystkie odpowiedzi widoczne dla gracza - do podgladu na liscie w edytorze. */
    val allAnswers: List<String>
        get() = when (type) {
            CustomQuestionType.Choice, CustomQuestionType.MultiSelect -> correct + wrong
            CustomQuestionType.Ordering -> items
            CustomQuestionType.Estimation -> listOfNotNull(correctValue?.toString())
        }

    fun toQuizEntry(deckId: String): QuizEntry? {
        if (type != CustomQuestionType.Choice || !isValid) return null
        return QuizEntry(
            id = CustomDeckBank.entryId(deckId, id),
            prompt = prompt,
            correct = correct.first { it.isNotBlank() },
            wrong = wrong.filter { it.isNotBlank() },
            category = QuizCategory.Custom,
            difficulty = difficulty
        )
    }

    fun toMultiSelectEntry(deckId: String): MultiSelectEntry? {
        if (type != CustomQuestionType.MultiSelect || !isValid) return null
        return MultiSelectEntry(
            id = CustomDeckBank.entryId(deckId, id),
            prompt = prompt,
            correct = correct.filter { it.isNotBlank() },
            wrong = wrong.filter { it.isNotBlank() },
            category = QuizCategory.Custom,
            difficulty = difficulty
        )
    }

    fun toOrderingEntry(deckId: String): OrderingEntry? {
        if (type != CustomQuestionType.Ordering || !isValid) return null
        return OrderingEntry(
            id = CustomDeckBank.entryId(deckId, id),
            prompt = prompt,
            items = items.filter { it.isNotBlank() },
            category = QuizCategory.Custom,
            difficulty = difficulty
        )
    }

    fun toEstimationEntry(deckId: String): EstimationEntry? {
        if (type != CustomQuestionType.Estimation || !isValid) return null
        val value = correctValue ?: return null
        return EstimationEntry(
            id = CustomDeckBank.entryId(deckId, id),
            prompt = prompt,
            minValue = minValue ?: return null,
            maxValue = maxValue ?: return null,
            correctValue = value,
            unit = unit,
            tolerance = tolerance ?: defaultYearTolerance(value)
        )
    }

    companion object {
        const val MIN_WRONG_ANSWERS = 2
        const val MAX_WRONG_ANSWERS = 5
        const val MAX_OPTIONS = 8
        const val MIN_ORDERING_ITEMS = 3
        const val MAX_ORDERING_ITEMS = 6

        /** Puste pytanie danego typu - punkt startowy formularza w edytorze. */
        fun blank(id: String, type: CustomQuestionType, difficulty: QuizDifficulty): CustomQuestion =
            when (type) {
                CustomQuestionType.Choice -> CustomQuestion(
                    id = id,
                    type = type,
                    difficulty = difficulty,
                    correct = listOf(""),
                    wrong = List(3) { "" }
                )

                CustomQuestionType.MultiSelect -> CustomQuestion(
                    id = id,
                    type = type,
                    difficulty = difficulty,
                    correct = List(2) { "" },
                    wrong = List(2) { "" }
                )

                CustomQuestionType.Ordering -> CustomQuestion(
                    id = id,
                    type = type,
                    difficulty = difficulty,
                    items = List(MIN_ORDERING_ITEMS) { "" }
                )

                CustomQuestionType.Estimation -> CustomQuestion(
                    id = id,
                    type = type,
                    difficulty = difficulty,
                    unit = "rok"
                )
            }

        private fun hasDuplicateAnswers(answers: List<String>): Boolean =
            answers.map { it.trim().lowercase() }.let { it.size != it.toSet().size }
    }
}
