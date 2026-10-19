package com.lernhero.domain.quiz

import java.io.File
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertTrue

/**
 * Pytania wyboru maja zrodlo w `domain/quizbank/<temat>.tsv`, a pliki `<Temat>Questions.kt` sa
 * z nich GENEROWANE przez `tools/quizbank.py generate`. Ten test pilnuje, zeby jedno z drugim sie
 * nie rozjechalo: reczna poprawka w Kotlinie przepadlaby przy nastepnym generowaniu, a pytanie
 * dopisane do TSV bez generowania nie trafiloby do gry.
 *
 * Siedzi w desktopTest, a nie w commonTest, bo czyta pliki z dysku. Katalog roboczy testow to
 * katalog modulu :domain.
 */
class QuizBankTsvSyncTest {

    private val dataDir = File("quizbank")

    /** Nazwa pliku TSV -> temat. Nowy plik tematu wymaga wpisu tutaj i w TOPICS w tools/quizbank.py. */
    private val categoryOf = mapOf(
        "biology" to QuizCategory.Biology,
        "animals" to QuizCategory.Animals,
        "history" to QuizCategory.History,
        "elements" to QuizCategory.Elements,
        "capitals" to QuizCategory.Capitals,
        "science" to QuizCategory.Science,
        "geography" to QuizCategory.Geography,
        "sport" to QuizCategory.Sport,
        "math" to QuizCategory.Math,
        "literature" to QuizCategory.Literature,
        "technology" to QuizCategory.Technology,
        "mythology" to QuizCategory.Mythology,
        "pop_culture" to QuizCategory.PopCulture,
        "poland" to QuizCategory.Poland
    )

    @Test
    fun `pliki Kotlin banku sa wygenerowane z aktualnych TSV`() {
        assertTrue(dataDir.isDirectory, "Brak katalogu z TSV: ${dataDir.absolutePath}")
        val files = dataDir.listFiles { file -> file.extension == "tsv" }.orEmpty().sortedBy { it.name }
        val unknown = files.map { it.nameWithoutExtension }.filter { it !in categoryOf }
        assertTrue(unknown.isEmpty(), "TSV bez przypisanego tematu: $unknown")

        val fromTsv = files.flatMap { readTsv(it, categoryOf.getValue(it.nameWithoutExtension)) }.associateBy { it.id }
        val fromKotlin = QuizBank.all(Random(1)).filter { it.category != QuizCategory.Flags }.associateBy { it.id }

        val onlyTsv = fromTsv.keys - fromKotlin.keys
        val onlyKotlin = fromKotlin.keys - fromTsv.keys
        val different = fromTsv.keys.intersect(fromKotlin.keys).filter { fromTsv[it] != fromKotlin[it] }
        val hint = "Uruchom `python3 tools/quizbank.py generate` i nie poprawiaj plikow *Questions.kt recznie."

        assertTrue(onlyTsv.isEmpty(), "Pytania z TSV, ktorych nie ma w Kotlinie: ${onlyTsv.take(10)}. $hint")
        assertTrue(onlyKotlin.isEmpty(), "Pytania z Kotlina, ktorych nie ma w TSV: ${onlyKotlin.take(10)}. $hint")
        assertTrue(
            different.isEmpty(),
            "Rozne tresci w TSV i w Kotlinie: ${different.take(5).map { "${fromTsv[it]} != ${fromKotlin[it]}" }}. $hint"
        )
    }

    private fun readTsv(file: File, category: QuizCategory): List<QuizEntry> {
        val lines = file.readLines().filter { it.isNotBlank() && !it.startsWith("#") }
        val header = lines.first().split('\t')
        assertTrue(
            header == listOf("id", "difficulty", "prompt", "correct", "wrong1", "wrong2", "wrong3"),
            "${file.name}: zly naglowek $header"
        )
        return lines.drop(1).map { line ->
            val cells = line.split('\t')
            assertTrue(cells.size == 7, "${file.name}: zla liczba kolumn w linii '$line'")
            QuizEntry(
                id = cells[0],
                prompt = cells[2],
                correct = cells[3],
                wrong = cells.subList(4, 7),
                category = category,
                difficulty = QuizDifficulty.valueOf(cells[1])
            )
        }
    }
}
