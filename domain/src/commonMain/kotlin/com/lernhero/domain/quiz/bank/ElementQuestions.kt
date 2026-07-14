package com.lernhero.domain.quiz.bank

import com.lernhero.domain.quiz.QuizCategory
import com.lernhero.domain.quiz.QuizDifficulty
import com.lernhero.domain.quiz.QuizEntry

/** "Jaki to pierwiastek?" - symbole z ukladu okresowego i podstawy chemii. */
object ElementQuestions {

    private fun ask(
        id: String,
        prompt: String,
        correct: String,
        wrong: List<String>,
        difficulty: QuizDifficulty
    ) = QuizEntry(
        id = "ele_$id",
        prompt = prompt,
        correct = correct,
        wrong = wrong,
        category = QuizCategory.Elements,
        difficulty = difficulty
    )

    /** Skrot na najczestszy typ pytania: "Jaki pierwiastek ma symbol X?". */
    private fun symbol(
        symbol: String,
        correct: String,
        wrong: List<String>,
        difficulty: QuizDifficulty
    ) = ask(
        id = "symbol_${symbol.lowercase()}",
        prompt = "Jaki pierwiastek kryje sie pod symbolem $symbol?",
        correct = correct,
        wrong = wrong,
        difficulty = difficulty
    )

    val all: List<QuizEntry> = listOf(

        /* ------------------------------- Latwe ------------------------------- */
        symbol("O", "Tlen", listOf("Zloto", "Osm", "Olow"), QuizDifficulty.Easy),
        symbol("H", "Wodor", listOf("Hel", "Rtec", "Wapn"), QuizDifficulty.Easy),
        symbol("Fe", "Zelazo", listOf("Fluor", "Fosfor", "Frans"), QuizDifficulty.Easy),
        symbol("Au", "Zloto", listOf("Srebro", "Miedz", "Glin"), QuizDifficulty.Easy),
        symbol("C", "Wegiel", listOf("Wapn", "Chlor", "Cez"), QuizDifficulty.Easy),
        ask(
            id = "woda",
            prompt = "Jaki jest wzor chemiczny wody?",
            correct = "H2O",
            wrong = listOf("CO2", "O2", "H2O2"),
            difficulty = QuizDifficulty.Easy
        ),
        ask(
            id = "powietrze",
            prompt = "Ktorego gazu jest najwiecej w powietrzu?",
            correct = "Azotu",
            wrong = listOf("Tlenu", "Dwutlenku wegla", "Argonu"),
            difficulty = QuizDifficulty.Easy
        ),
        ask(
            id = "sol",
            prompt = "Z jakich pierwiastkow sklada sie sol kuchenna?",
            correct = "Sod i chlor",
            wrong = listOf("Potas i chlor", "Sod i siarka", "Wapn i chlor"),
            difficulty = QuizDifficulty.Easy
        ),

        /* ------------------------------ Srednie ------------------------------ */
        symbol("Ag", "Srebro", listOf("Zloto", "Argon", "Glin"), QuizDifficulty.Medium),
        symbol("Na", "Sod", listOf("Azot", "Nikiel", "Neon"), QuizDifficulty.Medium),
        symbol("K", "Potas", listOf("Krypton", "Wapn", "Kobalt"), QuizDifficulty.Medium),
        symbol("Cu", "Miedz", listOf("Kobalt", "Chrom", "Cez"), QuizDifficulty.Medium),
        symbol("Pb", "Olow", listOf("Platyna", "Pallad", "Polon"), QuizDifficulty.Medium),
        symbol("N", "Azot", listOf("Neon", "Nikiel", "Sod"), QuizDifficulty.Medium),
        symbol("S", "Siarka", listOf("Sod", "Selen", "Krzem"), QuizDifficulty.Medium),
        ask(
            id = "liczba_1",
            prompt = "Ktory pierwiastek ma liczbe atomowa 1?",
            correct = "Wodor",
            wrong = listOf("Hel", "Wegiel", "Tlen"),
            difficulty = QuizDifficulty.Medium
        ),
        ask(
            id = "co2",
            prompt = "Jaki gaz wydychamy z pluc po oddychaniu komorkowym?",
            correct = "Dwutlenek wegla",
            wrong = listOf("Tlen", "Azot", "Metan"),
            difficulty = QuizDifficulty.Medium
        ),

        /* ------------------------------ Trudne ------------------------------- */
        symbol("Hg", "Rtec", listOf("Hel", "Hafn", "Holm"), QuizDifficulty.Hard),
        symbol("Sn", "Cyna", listOf("Skand", "Selen", "Stront"), QuizDifficulty.Hard),
        symbol("Si", "Krzem", listOf("Siarka", "Srebro", "Sod"), QuizDifficulty.Hard),
        symbol("Zn", "Cynk", listOf("Cyrkon", "Cyna", "Cez"), QuizDifficulty.Hard),
        symbol("W", "Wolfram", listOf("Wanad", "Wodor", "Wapn"), QuizDifficulty.Hard),
        ask(
            id = "najczestszy",
            prompt = "Ktory pierwiastek jest najczestszy we Wszechswiecie?",
            correct = "Wodor",
            wrong = listOf("Hel", "Tlen", "Wegiel"),
            difficulty = QuizDifficulty.Hard
        ),
        ask(
            id = "ilosc",
            prompt = "Ile pierwiastkow liczy dzisiejszy uklad okresowy?",
            correct = "118",
            wrong = listOf("108", "92", "120"),
            difficulty = QuizDifficulty.Hard
        ),
        ask(
            id = "cieklym",
            prompt = "Ktory metal jest w temperaturze pokojowej ciecza?",
            correct = "Rtec",
            wrong = listOf("Olow", "Cyna", "Gal"),
            difficulty = QuizDifficulty.Hard
        ),

        /* ----------------------------- Eksperckie ---------------------------- */
        symbol("Sb", "Antymon", listOf("Stront", "Srebro", "Selen"), QuizDifficulty.Expert),
        symbol("Kr", "Krypton", listOf("Chrom", "Kobalt", "Potas"), QuizDifficulty.Expert),
        symbol("Mn", "Mangan", listOf("Magnez", "Molibden", "Miedz"), QuizDifficulty.Expert),
        ask(
            id = "curie",
            prompt = "Ktory pierwiastek Maria Sklodowska-Curie nazwala na czesc swojej ojczyzny?",
            correct = "Polon",
            wrong = listOf("Rad", "Uran", "Radon"),
            difficulty = QuizDifficulty.Expert
        ),
        ask(
            id = "mendelejew",
            prompt = "Kto opracowal uklad okresowy pierwiastkow?",
            correct = "Dmitrij Mendelejew",
            wrong = listOf("Niels Bohr", "Antoine Lavoisier", "John Dalton"),
            difficulty = QuizDifficulty.Expert
        ),
        ask(
            id = "szlachetne",
            prompt = "Ktory z tych gazow jest gazem szlachetnym?",
            correct = "Argon",
            wrong = listOf("Azot", "Metan", "Wodor"),
            difficulty = QuizDifficulty.Expert
        ),
        ask(
            id = "twardy",
            prompt = "Jaka odmiana wegla jest najtwardszym naturalnym mineralem?",
            correct = "Diament",
            wrong = listOf("Grafit", "Fuleren", "Wegiel drzewny"),
            difficulty = QuizDifficulty.Expert
        )
    )
}
