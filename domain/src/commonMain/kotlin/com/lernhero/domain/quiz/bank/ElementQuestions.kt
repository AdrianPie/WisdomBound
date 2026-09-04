package com.lernhero.domain.quiz.bank

import com.lernhero.domain.quiz.QuizCategory
import com.lernhero.domain.quiz.QuizDifficulty
import com.lernhero.domain.quiz.QuizEntry

/** "What element is this?" - periodic table symbols and chemistry basics. */
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

    /** Shorthand for the most common question type: "Which element has the symbol X?". */
    private fun symbol(
        symbol: String,
        correct: String,
        wrong: List<String>,
        difficulty: QuizDifficulty
    ) = ask(
        id = "symbol_${symbol.lowercase()}",
        prompt = "Which element hides behind the symbol $symbol?",
        correct = correct,
        wrong = wrong,
        difficulty = difficulty
    )

    val all: List<QuizEntry> = listOf(

        /* ------------------------------- Easy ------------------------------- */
        symbol("O", "Oxygen", listOf("Gold", "Osmium", "Lead"), QuizDifficulty.Easy),
        symbol("H", "Hydrogen", listOf("Helium", "Mercury", "Calcium"), QuizDifficulty.Easy),
        symbol("Fe", "Iron", listOf("Fluorine", "Phosphorus", "Francium"), QuizDifficulty.Easy),
        symbol("Au", "Gold", listOf("Silver", "Copper", "Aluminum"), QuizDifficulty.Easy),
        symbol("C", "Carbon", listOf("Calcium", "Chlorine", "Cesium"), QuizDifficulty.Easy),
        ask(
            id = "woda",
            prompt = "What is the chemical formula of water?",
            correct = "H2O",
            wrong = listOf("CO2", "O2", "H2O2"),
            difficulty = QuizDifficulty.Easy
        ),
        ask(
            id = "powietrze",
            prompt = "Which gas is most abundant in air?",
            correct = "Nitrogen",
            wrong = listOf("Oxygen", "Carbon dioxide", "Argon"),
            difficulty = QuizDifficulty.Easy
        ),
        ask(
            id = "sol",
            prompt = "Which elements make up table salt?",
            correct = "Sodium and chlorine",
            wrong = listOf("Potassium and chlorine", "Sodium and sulfur", "Calcium and chlorine"),
            difficulty = QuizDifficulty.Easy
        ),

        /* ------------------------------ Medium ------------------------------ */
        symbol("Ag", "Silver", listOf("Gold", "Argon", "Aluminum"), QuizDifficulty.Medium),
        symbol("Na", "Sodium", listOf("Nitrogen", "Nickel", "Neon"), QuizDifficulty.Medium),
        symbol("K", "Potassium", listOf("Krypton", "Calcium", "Cobalt"), QuizDifficulty.Medium),
        symbol("Cu", "Copper", listOf("Cobalt", "Chromium", "Cesium"), QuizDifficulty.Medium),
        symbol("Pb", "Lead", listOf("Platinum", "Palladium", "Polonium"), QuizDifficulty.Medium),
        symbol("N", "Nitrogen", listOf("Neon", "Nickel", "Sodium"), QuizDifficulty.Medium),
        symbol("S", "Sulfur", listOf("Sodium", "Selenium", "Silicon"), QuizDifficulty.Medium),
        ask(
            id = "liczba_1",
            prompt = "Which element has atomic number 1?",
            correct = "Hydrogen",
            wrong = listOf("Helium", "Carbon", "Oxygen"),
            difficulty = QuizDifficulty.Medium
        ),
        ask(
            id = "co2",
            prompt = "Which gas do we exhale from the lungs after cellular respiration?",
            correct = "Carbon dioxide",
            wrong = listOf("Oxygen", "Nitrogen", "Methane"),
            difficulty = QuizDifficulty.Medium
        ),

        /* ------------------------------ Hard ------------------------------- */
        symbol("Hg", "Mercury", listOf("Helium", "Hafnium", "Holmium"), QuizDifficulty.Hard),
        symbol("Sn", "Tin", listOf("Scandium", "Selenium", "Strontium"), QuizDifficulty.Hard),
        symbol("Si", "Silicon", listOf("Sulfur", "Silver", "Sodium"), QuizDifficulty.Hard),
        symbol("Zn", "Zinc", listOf("Zirconium", "Tin", "Cesium"), QuizDifficulty.Hard),
        symbol("W", "Tungsten", listOf("Vanadium", "Hydrogen", "Calcium"), QuizDifficulty.Hard),
        ask(
            id = "najczestszy",
            prompt = "Which element is the most abundant in the Universe?",
            correct = "Hydrogen",
            wrong = listOf("Helium", "Oxygen", "Carbon"),
            difficulty = QuizDifficulty.Hard
        ),
        ask(
            id = "ilosc",
            prompt = "How many elements does today's periodic table have?",
            correct = "118",
            wrong = listOf("108", "92", "120"),
            difficulty = QuizDifficulty.Hard
        ),
        ask(
            id = "cieklym",
            prompt = "Which metal is liquid at room temperature?",
            correct = "Mercury",
            wrong = listOf("Lead", "Tin", "Gallium"),
            difficulty = QuizDifficulty.Hard
        ),

        /* ----------------------------- Expert ---------------------------- */
        symbol("Sb", "Antimony", listOf("Strontium", "Silver", "Selenium"), QuizDifficulty.Expert),
        symbol("Kr", "Krypton", listOf("Chromium", "Cobalt", "Potassium"), QuizDifficulty.Expert),
        symbol("Mn", "Manganese", listOf("Magnesium", "Molybdenum", "Copper"), QuizDifficulty.Expert),
        ask(
            id = "curie",
            prompt = "Which element did Marie Sklodowska-Curie name after her homeland?",
            correct = "Polonium",
            wrong = listOf("Radium", "Uranium", "Radon"),
            difficulty = QuizDifficulty.Expert
        ),
        ask(
            id = "mendelejew",
            prompt = "Who devised the periodic table of elements?",
            correct = "Dmitri Mendeleev",
            wrong = listOf("Niels Bohr", "Antoine Lavoisier", "John Dalton"),
            difficulty = QuizDifficulty.Expert
        ),
        ask(
            id = "szlachetne",
            prompt = "Which of these gases is a noble gas?",
            correct = "Argon",
            wrong = listOf("Nitrogen", "Methane", "Hydrogen"),
            difficulty = QuizDifficulty.Expert
        ),
        ask(
            id = "twardy",
            prompt = "Which form of carbon is the hardest natural mineral?",
            correct = "Diamond",
            wrong = listOf("Graphite", "Fullerene", "Charcoal"),
            difficulty = QuizDifficulty.Expert
        )
    )
}
