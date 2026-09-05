package com.lernhero.domain.quiz.ai

import com.lernhero.domain.quiz.QuizDifficulty
import com.lernhero.domain.quiz.custom.CustomQuestionType
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class QuizGenerationParserTest {

    private fun parse(raw: String, types: Set<CustomQuestionType> = CustomQuestionType.entries.toSet()) =
        QuizGenerationParser.parse(raw, idPrefix = "ai1", allowedTypes = types)

    @Test
    fun `czyta czysta odpowiedz modelu`() {
        val raw = """
            {"questions":[
              {"type":"choice","prompt":"Co oznacza znak B-2?","difficulty":"easy",
               "correct":"Zakaz wjazdu","wrong":["Zakaz postoju","Nakaz jazdy prosto","Koniec zakazu"]}
            ]}
        """.trimIndent()

        val result = assertIs<QuizGenerationResult.Generated>(parse(raw))
        assertEquals(1, result.questions.size)
        val question = result.questions.single()
        assertEquals(CustomQuestionType.Choice, question.type)
        assertEquals(QuizDifficulty.Easy, question.difficulty)
        assertEquals(listOf("Zakaz wjazdu"), question.correct)
        assertEquals(3, question.wrong.size)
    }

    @Test
    fun `przezywa oplotke z markdown i gadanie dookola`() {
        val raw = """
            Jasne, oto pytania:
            ```json
            {"questions":[{"type":"choice","prompt":"Ile wynosi limit w terenie zabudowanym?",
             "correct":["50 km/h"],"wrong":["40 km/h","60 km/h","70 km/h"]}]}
            ```
            Daj znac, jesli chcesz wiecej.
        """.trimIndent()

        val result = assertIs<QuizGenerationResult.Generated>(parse(raw))
        assertEquals("Ile wynosi limit w terenie zabudowanym?", result.questions.single().prompt)
    }

    @Test
    fun `przyjmuje sama tablice zamiast obiektu`() {
        val raw = """
            [{"type":"choice","prompt":"P?","correct":"A","wrong":["B","C","D"]}]
        """.trimIndent()
        assertIs<QuizGenerationResult.Generated>(parse(raw))
    }

    @Test
    fun `klamra w tresci pytania nie urywa JSON-a`() {
        val raw = """
            {"questions":[{"type":"choice","prompt":"Co robi {klamra} w kodzie?",
             "correct":"Otwiera blok","wrong":["Konczy plik","Nic","Komentuje"]}]}
        """.trimIndent()
        val result = assertIs<QuizGenerationResult.Generated>(parse(raw))
        assertTrue(result.questions.single().prompt.contains("{klamra}"))
    }

    @Test
    fun `odsiewa pytania z bledem, ale oddaje reszte i mowi dlaczego`() {
        val raw = """
            {"questions":[
              {"type":"choice","prompt":"Dobre?","correct":"A","wrong":["B","C","D"]},
              {"type":"choice","prompt":"Zle - duplikat","correct":"A","wrong":["A","C","D"]},
              {"type":"choice","prompt":"","correct":"A","wrong":["B","C","D"]}
            ]}
        """.trimIndent()

        val result = assertIs<QuizGenerationResult.Generated>(parse(raw))
        assertEquals(1, result.questions.size)
        assertEquals(2, result.warnings.size)
    }

    @Test
    fun `typ spoza zamowienia nie wchodzi do talii`() {
        val raw = """
            {"questions":[
              {"type":"ordering","prompt":"Ulóz","items":["A","B","C"]},
              {"type":"choice","prompt":"P?","correct":"A","wrong":["B","C","D"]}
            ]}
        """.trimIndent()

        val result = assertIs<QuizGenerationResult.Generated>(parse(raw, types = setOf(CustomQuestionType.Choice)))
        assertEquals(CustomQuestionType.Choice, result.questions.single().type)
        assertTrue(result.warnings.single().contains("was not requested"))
    }

    @Test
    fun `liczby przyslane jako napisy wciaz dzialaja`() {
        val raw = """
            {"questions":[{"type":"estimation","prompt":"Ktory rok?","value":"1969",
             "min":"1900","max":"2000","unit":"rok","tolerance":"3"}]}
        """.trimIndent()

        val result = assertIs<QuizGenerationResult.Generated>(parse(raw))
        val question = result.questions.single()
        assertEquals(1969, question.correctValue)
        assertEquals(3, question.tolerance)
    }

    @Test
    fun `brak JSON-a to blad, a nie pusta talia`() {
        assertIs<QuizGenerationResult.Failed>(parse("Przepraszam, nie moge tego zrobic."))
    }

    @Test
    fun `same smieci daja blad z powodem`() {
        val raw = """{"questions":[{"type":"choice","prompt":"P?","correct":"A","wrong":["A"]}]}"""
        val failed = assertIs<QuizGenerationResult.Failed>(parse(raw))
        assertTrue(failed.reason.isNotBlank())
    }

    @Test
    fun `prompt niesie zamowiona liczbe pytan i temat`() {
        val prompt = QuizGenerationPrompt.build(
            QuizGenerationRequest(topic = "prawo jazdy kat. B", count = 12, difficulty = QuizDifficulty.Hard)
        )
        assertTrue(prompt.contains("12"))
        assertTrue(prompt.contains("prawo jazdy kat. B"))
        assertTrue(prompt.contains("hard"))
    }
}
