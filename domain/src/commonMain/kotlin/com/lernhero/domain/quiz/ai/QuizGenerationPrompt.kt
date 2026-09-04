package com.lernhero.domain.quiz.ai

import com.lernhero.domain.quiz.QuizDifficulty
import com.lernhero.domain.quiz.custom.CustomQuestion
import com.lernhero.domain.quiz.custom.CustomQuestionType

/**
 * Instrukcja wysylana do modelu. Siedzi w domenie, a nie w kliencie HTTP, z dwoch powodow:
 * zna limity pytania ([CustomQuestion.MIN_WRONG_ANSWERS] i reszta rodzenstwa), wiec model dostaje
 * te same reguly, ktorych i tak pilnuje walidacja - a po drugie da sie ja przeczytac i poprawic
 * bez wchodzenia w warstwe sieci.
 *
 * Format odpowiedzi jest CELOWO plaski i jednoznaczny: jeden obiekt `{"questions":[...]}`,
 * a kazdy typ pytania ma swoj komplet pol. Parser ([QuizGenerationParser]) i tak jest tolerancyjny,
 * ale im mniej modelowi wolno improwizowac, tym mniej pytan przepada.
 */
object QuizGenerationPrompt {

    fun build(request: QuizGenerationRequest): String = buildString {
        appendLine("Jestes autorem pytan do gry edukacyjnej. Ulóz ${request.count} pytan.")
        appendLine("Temat: ${request.topic}")
        appendLine("Jezyk pytan i odpowiedzi: ${request.language}.")
        appendLine(
            "Poziom trudnosci: " + when (val level = request.difficulty) {
                null -> "wymieszany - od latwych do eksperckich."
                else -> "${level.label} (w polu difficulty wpisz \"${level.name.lowercase()}\")."
            }
        )
        if (request.notes.isNotBlank()) appendLine("Dodatkowe wytyczne autora: ${request.notes}")

        appendLine()
        appendLine("Dozwolone typy pytan:")
        request.types.forEach { type -> appendLine("- ${type.jsonName}: ${type.rule}") }

        appendLine()
        appendLine("Zasady twarde:")
        appendLine("- Odpowiedz WYLACZNIE poprawnym JSON-em, bez komentarza i bez znacznikow markdown.")
        appendLine("- Ksztalt: {\"questions\": [ ... ]}")
        appendLine("- difficulty: jedno z easy/medium/hard/expert.")
        appendLine("- Bledne odpowiedzi maja byc prawdopodobne, ale jednoznacznie bledne.")
        appendLine("- Zadna odpowiedz w jednym pytaniu nie moze sie powtarzac.")
        appendLine("- Nie pisz pytan, ktorych poprawnosci nie jestes pewien.")

        appendLine()
        appendLine("Pola dla kazdego typu:")
        appendLine(SCHEMA)
    }

    /**
     * Nazwa typu w JSON-ie - jedno miejsce dla promptu i parsera, zeby nie dalo sie ich rozjechac.
     */
    val CustomQuestionType.jsonName: String
        get() = when (this) {
            CustomQuestionType.Choice -> "choice"
            CustomQuestionType.MultiSelect -> "multi"
            CustomQuestionType.Ordering -> "ordering"
            CustomQuestionType.Estimation -> "estimation"
        }

    fun typeOf(jsonName: String): CustomQuestionType? =
        CustomQuestionType.entries.firstOrNull { it.jsonName.equals(jsonName.trim(), ignoreCase = true) }

    fun difficultyOf(jsonName: String): QuizDifficulty? =
        QuizDifficulty.entries.firstOrNull { it.name.equals(jsonName.trim(), ignoreCase = true) }

    private val CustomQuestionType.rule: String
        get() = when (this) {
            CustomQuestionType.Choice ->
                "jedna poprawna odpowiedz i ${CustomQuestion.MIN_WRONG_ANSWERS}-${CustomQuestion.MAX_WRONG_ANSWERS} blednych (najlepiej 3)"

            CustomQuestionType.MultiSelect ->
                "od 2 do 4 poprawnych odpowiedzi i co najmniej jedna bledna, lacznie max ${CustomQuestion.MAX_OPTIONS}"

            CustomQuestionType.Ordering ->
                "${CustomQuestion.MIN_ORDERING_ITEMS}-${CustomQuestion.MAX_ORDERING_ITEMS} elementow zapisanych JUZ w poprawnej kolejnosci"

            CustomQuestionType.Estimation ->
                "pytanie o liczbe (rok, dlugosc, masa) z zakresem min-max i tolerancja bledu"
        }

    private val SCHEMA = """
        choice:     {"type":"choice","prompt":"...","difficulty":"medium","correct":"poprawna","wrong":["zla 1","zla 2","zla 3"]}
        multi:      {"type":"multi","prompt":"...","difficulty":"hard","correct":["a","b"],"wrong":["c","d"]}
        ordering:   {"type":"ordering","prompt":"...","difficulty":"medium","items":["pierwszy","drugi","trzeci"]}
        estimation: {"type":"estimation","prompt":"...","difficulty":"hard","value":1969,"min":1900,"max":2000,"unit":"rok","tolerance":3}
    """.trimIndent()
}
