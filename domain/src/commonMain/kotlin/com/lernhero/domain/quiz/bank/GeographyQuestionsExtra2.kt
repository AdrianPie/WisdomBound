package com.lernhero.domain.quiz.bank

import com.lernhero.domain.quiz.QuizCategory
import com.lernhero.domain.quiz.QuizDifficulty
import com.lernhero.domain.quiz.QuizEntry

/** Third batch of geography - climate, maps and trivia not yet covered. */
object GeographyQuestionsExtra2 {

    private fun ask(
        id: String,
        prompt: String,
        correct: String,
        wrong: List<String>,
        difficulty: QuizDifficulty
    ) = QuizEntry(
        id = "geo3_$id",
        prompt = prompt,
        correct = correct,
        wrong = wrong,
        category = QuizCategory.Geography,
        difficulty = difficulty
    )

    val all: List<QuizEntry> = listOf(

        /* ------------------------------- Easy ------------------------------- */
        ask("rownik_klimat", "What climate is typical at the equator?", "Hot and humid", listOf("Very cold", "Desert everywhere", "Temperate"), QuizDifficulty.Easy),
        ask("polwysep_definicja", "What is a peninsula?", "Land surrounded by water on three sides", listOf("An island in a lake", "An underwater mountain", "A strait"), QuizDifficulty.Easy),
        ask("delta_rzeki", "What forms where some rivers meet the sea?", "A delta", listOf("A canyon", "A waterfall", "A cave"), QuizDifficulty.Easy),
        ask("gora_lodowa", "What is an iceberg made of?", "Broken-off glacier ice", listOf("Volcanic rock", "Frozen sand", "Snow and dust"), QuizDifficulty.Easy),

        /* ------------------------------ Medium ------------------------------ */
        ask("monsun", "What characterizes a monsoon climate?", "A distinct dry season and rainy season", listOf("Constant temperature year-round", "No rainfall all year", "Perpetual snow"), QuizDifficulty.Medium),
        ask("step_definicja", "What is a steppe?", "A vast, grassy plain with a dry climate", listOf("A dense tropical forest", "A sandy desert", "A swamp"), QuizDifficulty.Medium),
        ask("tundra_definicja", "Where is tundra found?", "In cold, polar regions", listOf("In the equatorial zone", "In hot deserts", "In tropical mountains"), QuizDifficulty.Medium),
        ask("wyzyna_gora_roznica", "How does a mountain differ from an upland (plateau)?", "A mountain has a distinct, steep peak", listOf("An upland is always higher", "A mountain is always flat", "They're the same thing"), QuizDifficulty.Medium),
        ask("archipelag_definicja", "What is an archipelago?", "A group of islands", listOf("A single large island", "A type of bay", "A mountain range"), QuizDifficulty.Medium),
        ask("wybrzeze_klifowe", "What characterizes a cliff coast?", "Steep, rocky bluffs above the sea", listOf("Flat, sandy beaches", "Coastal marshes", "Only coral reefs"), QuizDifficulty.Medium),
        ask("blakniecie_rafy", "What causes coral bleaching?", "Overheated water makes corals expel the algae living inside them", listOf("Sunlight bleaches the coral skeleton", "Salt crystallises on the coral", "Fish eat the coral's pigment"), QuizDifficulty.Medium),
        ask("pierwszy_dzien", "Which country is the first in the world to start each new day?", "Kiribati", listOf("New Zealand", "Japan", "Australia"), QuizDifficulty.Medium),

        /* ------------------------------ Hard ------------------------------- */
        ask("el_nino", "What is the El Nino phenomenon?", "A periodic warming of Pacific waters affecting world weather", listOf("A permanent cooling of the Arctic", "A cyclical earthquake", "A type of Atlantic hurricane"), QuizDifficulty.Hard),
        ask("prad_golfsztrom", "What is the warm ocean current warming Western Europe called?", "The Gulf Stream", listOf("The Humboldt Current", "The Labrador Current", "The California Current"), QuizDifficulty.Hard),
        ask("strefa_subdukcji", "What happens at a tectonic subduction zone?", "One plate slides beneath another", listOf("Plates move apart from each other", "Plates stay motionless", "Only flat plains form"), QuizDifficulty.Hard),
        ask("san_andreas", "Which two tectonic plates grind past each other along California's San Andreas Fault?", "The Pacific and North American plates", listOf("The Nazca and South American plates", "The Eurasian and African plates", "The Indian and Australian plates"), QuizDifficulty.Hard),
        ask("marzlocz_klimat", "Why is thawing permafrost a problem for the climate?", "It releases methane and carbon dioxide stored in the ground", listOf("It raises the ocean's salinity", "It thins the ozone layer", "It cools the atmosphere too fast"), QuizDifficulty.Hard),
        ask("krasowy_teren", "What characterizes karst terrain?", "Caves and formations from dissolved limestone", listOf("Only sand dunes", "Only glaciers", "An alluvial plain"), QuizDifficulty.Hard),
        ask("linia_zmiany_daty", "Where does the International Date Line run?", "Through the middle of the Pacific, near the 180th meridian", listOf("Through Greenwich", "Through the equator", "Through the Urals"), QuizDifficulty.Hard),
        ask("kaniony_powstawanie", "How do canyons like the Grand Canyon usually form?", "Through many years of river erosion", listOf("Through a meteorite impact", "Through earthquakes alone", "Through volcanism alone"), QuizDifficulty.Hard),

        /* ----------------------------- Expert ---------------------------- */
        ask("izostazja", "What is isostasy in geology?", "The balance of lithospheric plates on Earth's plastic mantle", listOf("The El Nino cycle", "The phenomenon of ocean tides", "A type of ocean current"), QuizDifficulty.Expert),
        ask("milankovic_cykle", "What do Milankovitch cycles concern?", "Periodic changes in Earth's orbital parameters affecting climate", listOf("Solar sunspot cycles", "Ocean tide cycles", "Volcanic cycles"), QuizDifficulty.Expert),
        ask("gory_uskokowe", "How do fault-block mountains form?", "Through vertical displacement of a crustal block along faults", listOf("Through folding of rock layers", "Through wind erosion", "Through glacial deposition"), QuizDifficulty.Expert),
        ask("delta_ganges", "Which river delta is considered the largest in the world?", "The Ganges-Brahmaputra Delta", listOf("The Nile Delta", "The Mississippi Delta", "The Amazon Delta"), QuizDifficulty.Expert),
        ask("strefa_ryftowa", "What is the Great East African Rift?", "A zone where continental plates are tearing apart", listOf("A chain of extinct volcanoes", "A waterless desert", "A permafrost region"), QuizDifficulty.Expert),
        ask("thermohalinowa_cyrkulacja", "What drives the global thermohaline ocean circulation?", "Density differences in water from temperature and salinity", listOf("Surface wind alone", "The Moon's gravity", "Volcanic activity on the seafloor"), QuizDifficulty.Expert),
        ask("wyspa_wulkaniczna_powstawanie", "How do volcanic island chains like Hawaii most often form?", "A plate moves over a stationary hot spot in the mantle", listOf("Through a collision of two continents", "Through subduction of one plate under another only", "Through coastal erosion"), QuizDifficulty.Expert)
    )
}
