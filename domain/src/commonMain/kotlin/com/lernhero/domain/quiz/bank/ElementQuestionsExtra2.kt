package com.lernhero.domain.quiz.bank

import com.lernhero.domain.quiz.QuizCategory
import com.lernhero.domain.quiz.QuizDifficulty
import com.lernhero.domain.quiz.QuizEntry

/** Third batch of elements - more symbols and uses not yet covered. */
object ElementQuestionsExtra2 {

    private fun ask(
        id: String,
        prompt: String,
        correct: String,
        wrong: List<String>,
        difficulty: QuizDifficulty
    ) = QuizEntry(
        id = "pie3_$id",
        prompt = prompt,
        correct = correct,
        wrong = wrong,
        category = QuizCategory.Elements,
        difficulty = difficulty
    )

    private fun symbol(
        symbol: String,
        correct: String,
        wrong: List<String>,
        difficulty: QuizDifficulty
    ) = ask("sym_${symbol.lowercase()}", "Which element hides behind the symbol $symbol?", correct, wrong, difficulty)

    private fun forSymbol(
        name: String,
        correct: String,
        wrong: List<String>,
        difficulty: QuizDifficulty
    ) = ask("nazwa_${name.lowercase()}", "What is the chemical symbol for $name?", correct, wrong, difficulty)

    val all: List<QuizEntry> = listOf(

        /* ------------------------------- Easy ------------------------------- */
        symbol("F", "Fluorine", listOf("Phosphorus", "Francium", "Fermium"), QuizDifficulty.Easy),
        symbol("I", "Iodine", listOf("Iridium", "Indium", "Yttrium"), QuizDifficulty.Easy),
        symbol("Ar", "Argon", listOf("Arsenic", "Astatine", "Actinium"), QuizDifficulty.Easy),
        forSymbol("nitrogen", "N", listOf("Ni", "Na", "Nt"), QuizDifficulty.Easy),
        forSymbol("silicon", "Si", listOf("Sc", "Sl", "Sx"), QuizDifficulty.Easy),
        ask("dwutlenek_wegla_wzor", "What is the chemical formula of carbon dioxide?", "CO2", listOf("CO", "C2O", "H2O"), QuizDifficulty.Easy),
        ask("sol_kuchenna_wzor", "What is the chemical formula of table salt?", "NaCl", listOf("KCl", "CaCl2", "NaOH"), QuizDifficulty.Easy),
        ask("metal_bizuteria", "Which precious metal is often used in jewelry alongside gold?", "Silver", listOf("Lead", "Zinc", "Tin"), QuizDifficulty.Easy),

        /* ------------------------------ Medium ------------------------------ */
        symbol("Cr", "Chromium", listOf("Cobalt", "Krypton", "Cesium"), QuizDifficulty.Medium),
        symbol("Ni", "Nickel", listOf("Neon", "Niobium", "Neodymium"), QuizDifficulty.Medium),
        symbol("As", "Arsenic", listOf("Astatine", "Argon", "Actinium"), QuizDifficulty.Medium),
        symbol("Br", "Bromine", listOf("Barium", "Boron", "Beryllium"), QuizDifficulty.Medium),
        ask("gaz_cieplarniany", "Which gas is the main greenhouse gas from human activity?", "Carbon dioxide", listOf("Oxygen", "Nitrogen", "Argon"), QuizDifficulty.Medium),
        ask("rdza_reakcja", "What does iron react with to form rust?", "Oxygen and water", listOf("Nitrogen", "Hydrogen", "Argon"), QuizDifficulty.Medium),
        ask("ocet_kwas", "Which acid is found in vinegar?", "Acetic acid", listOf("Hydrochloric acid", "Sulfuric acid", "Citric acid"), QuizDifficulty.Medium),
        ask("cytryna_kwas", "Which acid gives lemon its sour taste?", "Citric acid", listOf("Acetic acid", "Lactic acid", "Hydrochloric acid"), QuizDifficulty.Medium),

        /* ------------------------------ Hard ------------------------------- */
        symbol("Cd", "Cadmium", listOf("Cesium", "Chromium", "Cobalt"), QuizDifficulty.Hard),
        symbol("Ge", "Germanium", listOf("Gadolinium", "Gallium", "Aluminum"), QuizDifficulty.Hard),
        symbol("Sr", "Strontium", listOf("Selenium", "Scandium", "Silver"), QuizDifficulty.Hard),
        symbol("Mo", "Molybdenum", listOf("Manganese", "Magnesium", "Copper"), QuizDifficulty.Hard),
        forSymbol("boron", "B", listOf("Bo", "Br", "Ba"), QuizDifficulty.Hard),
        ask("stop_lutownia", "Which metals make up a typical tin-lead solder?", "Tin and lead", listOf("Gold and silver", "Copper and zinc", "Iron and nickel"), QuizDifficulty.Hard),
        ask("krzem_elektronika", "Why is silicon key to electronics?", "It's a semiconductor used in integrated circuits", listOf("It's the lightest metal", "It conducts electricity like copper", "It's the hardest element"), QuizDifficulty.Hard),
        ask("azot_atmosfera", "What percentage of Earth's atmosphere is nitrogen?", "About 78%", listOf("About 21%", "About 50%", "About 95%"), QuizDifficulty.Hard),

        /* ----------------------------- Expert ---------------------------- */
        symbol("Os", "Osmium", listOf("Lead", "Iridium", "Platinum"), QuizDifficulty.Expert),
        symbol("Pd", "Palladium", listOf("Platinum", "Polonium", "Potassium"), QuizDifficulty.Expert),
        ask("pierwiastek_najciezszy_stabilny", "Which is the heaviest element with stable isotopes?", "Lead", listOf("Bismuth", "Uranium", "Radium"), QuizDifficulty.Expert),
        ask("astat_rzadki", "Which is the rarest naturally occurring element in Earth's crust?", "Astatine", listOf("Francium", "Promethium", "Polonium"), QuizDifficulty.Expert),
    )
}
