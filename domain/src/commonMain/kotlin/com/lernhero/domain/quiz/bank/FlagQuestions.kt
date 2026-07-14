package com.lernhero.domain.quiz.bank

import com.lernhero.domain.quiz.QuizCategory
import com.lernhero.domain.quiz.QuizDifficulty
import com.lernhero.domain.quiz.QuizEntry
import kotlin.random.Random

/**
 * Flagi. Cala kategoria siedzi w trudnych i eksperckich - rozpoznanie flagi jest trudniejsze
 * niz nazwanie stolicy Francji, wiec nie ma czego szukac wsrod latwych pytan.
 *
 * Bledne odpowiedzi losujemy dopiero przy budowaniu pytania, z tej samej puli krajow. Dzieki temu
 * jedna flaga daje za kazdym razem inny zestaw pulapek, a bank nie puchnie od recznie
 * wpisywanych dystraktorow.
 */
object FlagQuestions {

    /** Kod kraju + nazwa. Obrazek dokłada warstwa UI, bo domena nie zna zasobow graficznych. */
    data class FlagFact(
        val code: String,
        val country: String,
        val difficulty: QuizDifficulty
    )

    val facts: List<FlagFact> = listOf(
        // Flagi, ktore wiekszosc ludzi kojarzy - ale wciaz trzeba je odroznic od podobnych.
        FlagFact("pl", "Polska", QuizDifficulty.Hard),
        FlagFact("de", "Niemcy", QuizDifficulty.Hard),
        FlagFact("fr", "Francja", QuizDifficulty.Hard),
        FlagFact("es", "Hiszpania", QuizDifficulty.Hard),
        FlagFact("it", "Wlochy", QuizDifficulty.Hard),
        FlagFact("pt", "Portugalia", QuizDifficulty.Hard),
        FlagFact("gb", "Wielka Brytania", QuizDifficulty.Hard),
        FlagFact("us", "Stany Zjednoczone", QuizDifficulty.Hard),
        FlagFact("ca", "Kanada", QuizDifficulty.Hard),
        FlagFact("br", "Brazylia", QuizDifficulty.Hard),
        FlagFact("cn", "Chiny", QuizDifficulty.Hard),
        FlagFact("jp", "Japonia", QuizDifficulty.Hard),
        FlagFact("au", "Australia", QuizDifficulty.Hard),
        FlagFact("no", "Norwegia", QuizDifficulty.Hard),
        FlagFact("se", "Szwecja", QuizDifficulty.Hard),
        FlagFact("dk", "Dania", QuizDifficulty.Hard),
        FlagFact("nl", "Holandia", QuizDifficulty.Hard),
        FlagFact("ch", "Szwajcaria", QuizDifficulty.Hard),
        FlagFact("cz", "Czechy", QuizDifficulty.Hard),
        FlagFact("gr", "Grecja", QuizDifficulty.Hard),
        FlagFact("tr", "Turcja", QuizDifficulty.Hard),
        FlagFact("ua", "Ukraina", QuizDifficulty.Hard),
        FlagFact("mx", "Meksyk", QuizDifficulty.Hard),
        FlagFact("ar", "Argentyna", QuizDifficulty.Hard),
        FlagFact("kr", "Korea Poludniowa", QuizDifficulty.Hard),
        FlagFact("ie", "Irlandia", QuizDifficulty.Hard),
        FlagFact("at", "Austria", QuizDifficulty.Hard),
        FlagFact("fi", "Finlandia", QuizDifficulty.Hard),
        FlagFact("be", "Belgia", QuizDifficulty.Hard),

        // Tu zaczynaja sie flagi, przy ktorych mylimy sie najczesciej.
        FlagFact("sk", "Slowacja", QuizDifficulty.Expert),
        FlagFact("hu", "Wegry", QuizDifficulty.Expert),
        FlagFact("ro", "Rumunia", QuizDifficulty.Expert),
        FlagFact("bg", "Bulgaria", QuizDifficulty.Expert),
        FlagFact("nz", "Nowa Zelandia", QuizDifficulty.Expert),
        FlagFact("eg", "Egipt", QuizDifficulty.Expert),
        FlagFact("ma", "Maroko", QuizDifficulty.Expert),
        FlagFact("ng", "Nigeria", QuizDifficulty.Expert),
        FlagFact("ke", "Kenia", QuizDifficulty.Expert),
        FlagFact("za", "Republika Poludniowej Afryki", QuizDifficulty.Expert),
        FlagFact("th", "Tajlandia", QuizDifficulty.Expert),
        FlagFact("vn", "Wietnam", QuizDifficulty.Expert),
        FlagFact("ph", "Filipiny", QuizDifficulty.Expert),
        FlagFact("sg", "Singapur", QuizDifficulty.Expert)
    )

    /**
     * Buduje pytania o flagi, dobierajac kazdej trzy bledne odpowiedzi z pozostalych krajow.
     * Losowanie idzie z [random], wiec z tym samym seedem quiz jest odtwarzalny.
     */
    fun entries(random: Random): List<QuizEntry> = facts.map { fact ->
        val distractors = facts
            .filter { it.code != fact.code }
            .shuffled(random)
            .take(3)
            .map { it.country }

        QuizEntry(
            id = "flag_${fact.code}",
            prompt = "Jakiego panstwa jest to flaga?",
            correct = fact.country,
            wrong = distractors,
            category = QuizCategory.Flags,
            difficulty = fact.difficulty,
            flagCode = fact.code
        )
    }
}
