package com.lernhero.domain.quiz.bank

import com.lernhero.domain.quiz.QuizCategory
import com.lernhero.domain.quiz.QuizDifficulty
import com.lernhero.domain.quiz.QuizEntry

/**
 * Second batch of elements. Besides symbols, this adds questions about properties and uses,
 * so the category isn't just "symbol -> name" flashcards.
 */
object ElementQuestionsExtra {

    private fun ask(
        id: String,
        prompt: String,
        correct: String,
        wrong: List<String>,
        difficulty: QuizDifficulty
    ) = QuizEntry(
        id = "pie2_$id",
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
        symbol("He", "Helium", listOf("Hydrogen", "Hafnium", "Holmium"), QuizDifficulty.Easy),
        symbol("Ca", "Calcium", listOf("Carbon", "Cadmium", "Cesium"), QuizDifficulty.Easy),
        symbol("Cl", "Chlorine", listOf("Chromium", "Cobalt", "Cesium"), QuizDifficulty.Easy),
        forSymbol("oxygen", "O", listOf("Ox", "Og", "Oy"), QuizDifficulty.Easy),
        forSymbol("hydrogen", "H", listOf("Hy", "Hg", "Hd"), QuizDifficulty.Easy),
        forSymbol("gold", "Au", listOf("Go", "Ag", "Gd"), QuizDifficulty.Easy),
        ask("gaz_balony", "Which gas fills balloons so they float and don't ignite?", "Helium", listOf("Hydrogen", "Nitrogen", "Oxygen"), QuizDifficulty.Easy),
        ask("metal_puszki", "Which metal are lightweight drink cans made of?", "Aluminum", listOf("Lead", "Zinc", "Chromium"), QuizDifficulty.Easy),
        ask("grafit_olowek", "Which element is the main component of graphite in a pencil?", "Carbon", listOf("Lead", "Silicon", "Graphene"), QuizDifficulty.Easy),
        ask("wapn_kosci", "Which element builds bones and teeth?", "Calcium", listOf("Iron", "Potassium", "Sodium"), QuizDifficulty.Easy),

        /* ------------------------------ Medium ------------------------------ */
        symbol("Mg", "Magnesium", listOf("Manganese", "Molybdenum", "Copper"), QuizDifficulty.Medium),
        symbol("Al", "Aluminum", listOf("Argon", "Antimony", "Actinium"), QuizDifficulty.Medium),
        symbol("P", "Phosphorus", listOf("Potassium", "Platinum", "Polonium"), QuizDifficulty.Medium),
        symbol("Li", "Lithium", listOf("Lanthanum", "Lutetium", "Livermorium"), QuizDifficulty.Medium),
        symbol("Ne", "Neon", listOf("Nickel", "Niobium", "Neptunium"), QuizDifficulty.Medium),
        forSymbol("iron", "Fe", listOf("Ze", "Zr", "Fr"), QuizDifficulty.Medium),
        forSymbol("potassium", "K", listOf("P", "Po", "Pt"), QuizDifficulty.Medium),
        forSymbol("sodium", "Na", listOf("So", "S", "Sd"), QuizDifficulty.Medium),
        ask("gaz_szlachetny", "Which group of elements almost never forms chemical compounds?", "The noble gases", listOf("Alkali metals", "Halogens", "Lanthanides"), QuizDifficulty.Medium),
        ask("baterie_lit", "Which element is key in phone batteries?", "Lithium", listOf("Zinc", "Nickel", "Cadmium"), QuizDifficulty.Medium),
        ask("neon_reklamy", "Which gas glows red in classic sign tubes?", "Neon", listOf("Argon", "Krypton", "Xenon"), QuizDifficulty.Medium),
        ask("hel_glos", "Why does your voice sound high after inhaling helium?", "Sound travels faster through helium than through air", listOf("Helium tightens the vocal cords for a while", "Helium muffles the lower tones of speech", "Helium changes the shape of the mouth cavity"), QuizDifficulty.Medium),
        ask("stal_sklad", "What is steel mainly made of?", "Iron and carbon", listOf("Iron and zinc", "Copper and tin", "Aluminum and silicon"), QuizDifficulty.Medium),
        ask("braz_stop", "What is bronze, an alloy known since antiquity, made of?", "Copper and tin", listOf("Copper and zinc", "Iron and nickel", "Lead and silver"), QuizDifficulty.Medium),

        /* ------------------------------ Hard ------------------------------- */
        symbol("Ti", "Titanium", listOf("Thallium", "Thorium", "Thulium"), QuizDifficulty.Hard),
        symbol("Co", "Cobalt", listOf("Chromium", "Copper", "Cesium"), QuizDifficulty.Hard),
        symbol("Ba", "Barium", listOf("Beryllium", "Boron", "Bismuth"), QuizDifficulty.Hard),
        symbol("Xe", "Xenon", listOf("Krypton", "Radon", "Argon"), QuizDifficulty.Hard),
        symbol("Bi", "Bismuth", listOf("Beryllium", "Barium", "Berkelium"), QuizDifficulty.Hard),
        symbol("Se", "Selenium", listOf("Sulfur", "Scandium", "Silver"), QuizDifficulty.Hard),
        forSymbol("tin", "Sn", listOf("Ti", "Tn", "Ta"), QuizDifficulty.Hard),
        forSymbol("lead", "Pb", listOf("Ld", "Le", "Po"), QuizDifficulty.Hard),
        ask("mosiadz_sklad", "What is brass made of?", "Copper and zinc", listOf("Copper and tin", "Iron and chromium", "Aluminum and magnesium"), QuizDifficulty.Hard),
        ask("stal_nierdzewna", "Which addition makes steel stainless?", "Chromium", listOf("Zinc", "Lead", "Calcium"), QuizDifficulty.Hard),
        ask("wolfram_temperatura", "Which element has the highest melting point among metals?", "Tungsten", listOf("Platinum", "Titanium", "Osmium"), QuizDifficulty.Hard),
        ask("najgestszy_metal", "Which element is the densest?", "Osmium", listOf("Lead", "Gold", "Platinum"), QuizDifficulty.Hard),
        ask("kwas_solny", "What is the formula of hydrochloric acid?", "HCl", listOf("H2SO4", "HNO3", "H2CO3"), QuizDifficulty.Hard),
        ask("kwas_siarkowy", "What is the formula of sulfuric acid?", "H2SO4", listOf("HCl", "H2SO3", "HClO4"), QuizDifficulty.Hard),
        ask("amoniak", "What is the formula of ammonia?", "NH3", listOf("CH4", "NO2", "N2O"), QuizDifficulty.Hard),

        /* ----------------------------- Expert ---------------------------- */
        symbol("Zr", "Zirconium", listOf("Zinc", "Cesium", "Tin"), QuizDifficulty.Expert),
        symbol("Nb", "Niobium", listOf("Nickel", "Neodymium", "Neptunium"), QuizDifficulty.Expert),
        symbol("Gd", "Gadolinium", listOf("Gallium", "Germanium", "Aluminum"), QuizDifficulty.Expert),
        symbol("Ir", "Iridium", listOf("Indium", "Ytterbium", "Yttrium"), QuizDifficulty.Expert),
        symbol("Rb", "Rubidium", listOf("Radium", "Radon", "Rhenium"), QuizDifficulty.Expert),
        symbol("Ta", "Tantalum", listOf("Thallium", "Thorium", "Technetium"), QuizDifficulty.Expert),
        ask("pierwiastek_sztuczny", "Which element was the first to be artificially produced?", "Technetium", listOf("Plutonium", "Francium", "Astatine"), QuizDifficulty.Expert),
        ask("najrzadszy_natura", "Which natural element occurs on Earth in the smallest quantities?", "Astatine", listOf("Francium", "Polonium", "Radium"), QuizDifficulty.Expert),
        ask("uran_izotop", "Which uranium isotope is fissile and used in reactors?", "Uranium-235", listOf("Uranium-238", "Uranium-234", "Uranium-240"), QuizDifficulty.Expert),
        ask("lantanowce_ile", "How many elements make up the lanthanide group?", "15", listOf("14", "12", "17"), QuizDifficulty.Expert),
        ask("pierwiastek_kopernik", "Who is the element with atomic number 112 named after?", "Nicolaus Copernicus", listOf("Marie Sklodowska-Curie", "Enrico Fermi", "Niels Bohr"), QuizDifficulty.Expert),
        ask("gal_topnienie", "Which metal melts in your hand, with a melting point around 86 F (30 C)?", "Gallium", listOf("Cesium", "Indium", "Tin"), QuizDifficulty.Expert),
        ask("aqua_regia", "Which acid mixture dissolves gold?", "Aqua regia", listOf("Sulfuric acid", "Acetic acid", "Hydrogen peroxide"), QuizDifficulty.Expert)
    )
}
