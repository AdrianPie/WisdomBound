package com.lernhero.domain.quiz.ai

import com.lernhero.domain.quiz.QuizDifficulty
import com.lernhero.domain.quiz.custom.CustomQuestion
import com.lernhero.domain.quiz.custom.CustomQuestionType
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

/**
 * Zamienia odpowiedz modelu na pytania talii.
 *
 * Jest CELOWO tolerancyjny na ksztalt i bezlitosny na tresc:
 *
 * - ksztalt: przyjmuje `{"questions":[...]}` i samą tablice, przezywa oplotke ` ```json `,
 *   tekst dookola JSON-a, `correct` podane jako pojedynczy string zamiast tablicy oraz liczby
 *   przyslane jako napisy. Model prawie nigdy nie odda idealnie tego, o co prosil prompt.
 * - tresc: kazde pytanie przechodzi przez [CustomQuestion.issues] i pytanie z bledem NIE wchodzi
 *   do talii, tylko laduje w ostrzezeniach. Lepiej oddac 8 pytan z 10 niz wpuscic do quizu
 *   pytanie z dwiema identycznymi odpowiedziami.
 *
 * Cala ta klasa dziala bez sieci, wiec jest testowalna - i dlatego mozna ja bylo napisac,
 * zanim jakikolwiek model zostal podpiety.
 */
object QuizGenerationParser {

    private val json = Json { ignoreUnknownKeys = true; isLenient = true }

    /**
     * [idPrefix] wchodzi w id pytan - wolajacy podaje cos unikalnego dla tego wywolania
     * (np. znacznik czasu), zeby dogenerowanie do istniejacej talii nie nadpisalo starych pytan.
     */
    fun parse(
        raw: String,
        idPrefix: String,
        allowedTypes: Set<CustomQuestionType> = CustomQuestionType.entries.toSet(),
        defaultDifficulty: QuizDifficulty = QuizDifficulty.Medium
    ): QuizGenerationResult {
        val body = extractJson(raw) ?: return QuizGenerationResult.Failed(
            "Model nie odpowiedzial JSON-em - sprobuj jeszcze raz."
        )

        val root = try {
            json.parseToJsonElement(body)
        } catch (e: Exception) {
            return QuizGenerationResult.Failed("Nie udalo sie odczytac odpowiedzi modelu (${e.message}).")
        }

        val array = when {
            root is JsonArray -> root
            root is JsonObject -> (root["questions"] as? JsonArray) ?: (root["pytania"] as? JsonArray)
            else -> null
        } ?: return QuizGenerationResult.Failed("Odpowiedz modelu nie zawiera listy pytan.")

        val questions = mutableListOf<CustomQuestion>()
        val warnings = mutableListOf<String>()

        array.forEachIndexed { index, element ->
            val obj = element as? JsonObject
            if (obj == null) {
                warnings += "Pytanie ${index + 1}: nie jest obiektem JSON."
                return@forEachIndexed
            }

            val typeName = obj.string("type") ?: obj.string("typ")
            val type = typeName?.let(QuizGenerationPrompt::typeOf)
            if (type == null) {
                warnings += "Pytanie ${index + 1}: nieznany typ \"${typeName.orEmpty()}\"."
                return@forEachIndexed
            }
            if (type !in allowedTypes) {
                warnings += "Pytanie ${index + 1}: typ \"${typeName}\" nie byl zamowiony - pominiete."
                return@forEachIndexed
            }

            val difficulty = obj.string("difficulty")
                ?.let(QuizGenerationPrompt::difficultyOf)
                ?: defaultDifficulty

            val question = CustomQuestion(
                id = "${idPrefix}_$index",
                type = type,
                prompt = (obj.string("prompt") ?: obj.string("pytanie")).orEmpty().trim(),
                difficulty = difficulty,
                correct = obj.strings("correct").ifEmpty { obj.strings("poprawna") },
                wrong = obj.strings("wrong").ifEmpty { obj.strings("bledne") },
                items = obj.strings("items").ifEmpty { obj.strings("elementy") },
                correctValue = obj.int("value") ?: obj.int("correctValue"),
                minValue = obj.int("min") ?: obj.int("minValue"),
                maxValue = obj.int("max") ?: obj.int("maxValue"),
                unit = obj.string("unit").orEmpty().trim(),
                tolerance = obj.int("tolerance")
            ).cleaned()

            val issues = question.issues()
            if (issues.isEmpty()) {
                questions += question
            } else {
                val label = question.prompt.take(40).ifBlank { "bez tresci" }
                warnings += "Pominieto \"$label\": ${issues.joinToString(" ")}"
            }
        }

        if (questions.isEmpty()) {
            return QuizGenerationResult.Failed(
                "Zadne z pytan modelu nie nadawalo sie do talii." +
                    warnings.firstOrNull()?.let { " ($it)" }.orEmpty()
            )
        }
        return QuizGenerationResult.Generated(questions = questions, warnings = warnings)
    }

    /** Obcina puste odpowiedzi i biale znaki - model lubi dokladac "" na koncu tablicy. */
    private fun CustomQuestion.cleaned(): CustomQuestion = copy(
        correct = correct.map { it.trim() }.filter { it.isNotEmpty() },
        wrong = wrong.map { it.trim() }.filter { it.isNotEmpty() },
        items = items.map { it.trim() }.filter { it.isNotEmpty() }
    )

    /**
     * Wycina JSON z tego, co model naprawde przyslal: czasem oplecione w ```json, czasem
     * poprzedzone zdaniem "Oto pytania:". Bierzemy pierwszy nawias otwierajacy i pasujacy do
     * niego domykajacy - liczac tylko te poza cudzyslowami, bo tresc pytania moze zawierac klamre.
     */
    private fun extractJson(raw: String): String? {
        val text = raw.trim().removeSurrounding("```json", "```").trim()
        val start = text.indexOfFirst { it == '{' || it == '[' }
        if (start < 0) return null

        val open = text[start]
        val close = if (open == '{') '}' else ']'
        var depth = 0
        var inString = false
        var escaped = false

        for (index in start until text.length) {
            val char = text[index]
            when {
                escaped -> escaped = false
                char == '\\' && inString -> escaped = true
                char == '"' -> inString = !inString
                inString -> Unit
                char == open -> depth += 1
                char == close -> {
                    depth -= 1
                    if (depth == 0) return text.substring(start, index + 1)
                }
            }
        }
        return null
    }

    private fun JsonObject.string(key: String): String? =
        (this[key] as? JsonPrimitive)?.contentOrNull?.takeIf { it.isNotBlank() }

    private fun JsonObject.int(key: String): Int? = when (val value = this[key]) {
        is JsonPrimitive -> value.intOrNull ?: value.contentOrNull?.trim()?.toIntOrNull()
        else -> null
    }

    /** Tablica napisow ALBO pojedynczy napis - model myli te dwie rzeczy nagminnie. */
    private fun JsonObject.strings(key: String): List<String> = when (val value: JsonElement? = this[key]) {
        is JsonArray -> value.jsonArray.mapNotNull { element ->
            (element as? JsonPrimitive)?.contentOrNull
                ?: (element as? JsonObject)?.string("text")
        }
        is JsonPrimitive -> listOfNotNull(value.jsonPrimitive.contentOrNull?.takeIf { it.isNotBlank() })
        else -> emptyList()
    }
}
