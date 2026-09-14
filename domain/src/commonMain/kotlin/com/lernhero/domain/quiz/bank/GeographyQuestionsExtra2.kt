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
        ask("polwysep_definicja", "What is a peninsula?", "Land surrounded by water on three sides", listOf("An island lying in the middle of a lake", "A mountain that rises from the seabed", "A narrow channel joining two seas"), QuizDifficulty.Easy),
        ask("delta_rzeki", "What forms where some rivers meet the sea?", "A delta", listOf("A canyon", "A waterfall", "A cave"), QuizDifficulty.Easy),
        ask("gora_lodowa", "What is an iceberg made of?", "Broken-off glacier ice", listOf("Volcanic rock", "Frozen sand", "Snow and dust"), QuizDifficulty.Easy),

        /* ------------------------------ Medium ------------------------------ */
        ask("monsun", "What characterizes a monsoon climate?", "A distinct dry season and rainy season", listOf("Constant temperature year-round", "No rainfall all year", "Perpetual snow"), QuizDifficulty.Medium),
        ask("step_definicja", "What is a steppe?", "A vast, grassy plain with a dry climate", listOf("A dense forest in the wet tropics", "A desert of shifting sand dunes", "A wetland of reeds and standing water"), QuizDifficulty.Medium),
        ask("tundra_definicja", "Where is tundra found?", "In cold, polar regions", listOf("In the equatorial zone", "In hot deserts", "In tropical mountains"), QuizDifficulty.Medium),
        ask("wyzyna_gora_roznica", "How does a mountain differ from an upland (plateau)?", "A mountain has a distinct, steep peak", listOf("An upland is always higher", "A mountain is always flat", "They're the same thing"), QuizDifficulty.Medium),
        ask("archipelag_definicja", "What is an archipelago?", "A group of islands", listOf("A single large island", "A type of bay", "A mountain range"), QuizDifficulty.Medium),
        ask("wybrzeze_klifowe", "What characterizes a cliff coast?", "Steep, rocky bluffs above the sea", listOf("Flat, sandy beaches", "Coastal marshes", "Only coral reefs"), QuizDifficulty.Medium),
        ask("blakniecie_rafy", "What causes coral bleaching?", "Overheated water makes corals expel the algae living inside them", listOf("Strong sunlight slowly bleaches the stony skeleton of the coral", "Salt crystallises on the surface of the coral", "Fish graze away the pigment in the coral"), QuizDifficulty.Medium),
        ask("pierwszy_dzien", "Which country is the first in the world to start each new day?", "Kiribati", listOf("New Zealand", "Japan", "Australia"), QuizDifficulty.Medium),

        /* ------------------------------ Hard ------------------------------- */
        ask("el_nino", "What is the El Nino phenomenon?", "A periodic warming of Pacific waters affecting world weather", listOf("A permanent cooling of the waters of the Arctic", "An earthquake that returns on a fixed cycle", "A particular kind of Atlantic hurricane"), QuizDifficulty.Hard),
        ask("prad_golfsztrom", "What is the warm ocean current warming Western Europe called?", "The Gulf Stream", listOf("The Humboldt Current", "The Labrador Current", "The California Current"), QuizDifficulty.Hard),
        ask("strefa_subdukcji", "What happens at a tectonic subduction zone?", "One plate slides beneath another", listOf("Plates move apart from each other", "Plates stay motionless", "Only flat plains form"), QuizDifficulty.Hard),
        ask("san_andreas", "Which two tectonic plates grind past each other along California's San Andreas Fault?", "The Pacific and North American plates", listOf("The Nazca and South American plates", "The Eurasian and African plates", "The Indian and Australian plates"), QuizDifficulty.Hard),
        ask("marzlocz_klimat", "Why is thawing permafrost a problem for the climate?", "It releases methane and carbon dioxide stored in the ground", listOf("It raises the salinity of the oceans worldwide", "It thins the ozone layer above the poles", "It cools the atmosphere far too quickly"), QuizDifficulty.Hard),
        ask("krasowy_teren", "What characterizes karst terrain?", "Caves and formations from dissolved limestone", listOf("Dunes of sand and nothing else besides", "Glaciers and the valleys they have cut", "A flat plain built up of river silt"), QuizDifficulty.Hard),
        ask("linia_zmiany_daty", "Where does the International Date Line run?", "Through the middle of the Pacific, near the 180th meridian", listOf("Straight through Greenwich, on the edge of London", "Along the equator, right round the globe", "Along the Ural mountains and the Caspian"), QuizDifficulty.Hard),
        ask("kaniony_powstawanie", "How do canyons like the Grand Canyon usually form?", "Through many years of river erosion", listOf("Through a meteorite impact", "Through earthquakes alone", "Through volcanism alone"), QuizDifficulty.Hard),

        /* ----------------------------- Expert ---------------------------- */
        ask("izostazja", "What is isostasy in geology?", "The balance of lithospheric plates on Earth's plastic mantle", listOf("The cycle of warming and cooling in the Pacific", "The daily rise and fall of the ocean tides", "A cold current running along a coastline"), QuizDifficulty.Expert),
        ask("milankovic_cykle", "What do Milankovitch cycles concern?", "Periodic changes in Earth's orbital parameters affecting climate", listOf("The cycle of sunspots appearing on the Sun's surface", "The cycle of tides raised by the Moon and Sun", "The cycle of great volcanic eruptions"), QuizDifficulty.Expert),
        ask("gory_uskokowe", "How do fault-block mountains form?", "Through vertical displacement of a crustal block along faults", listOf("Through the folding of layers of rock under pressure", "Through wind wearing down an older plateau", "Through material dropped by melting glaciers"), QuizDifficulty.Expert),
        ask("delta_ganges", "Which river delta is considered the largest in the world?", "The Ganges-Brahmaputra Delta", listOf("The Nile Delta", "The Mississippi Delta", "The Amazon Delta"), QuizDifficulty.Expert),
        ask("strefa_ryftowa", "What is the Great East African Rift?", "A zone where continental plates are tearing apart", listOf("A chain of volcanoes that went extinct long ago", "A desert where no river reaches the sea", "A region where the ground never thaws"), QuizDifficulty.Expert),
        ask("thermohalinowa_cyrkulacja", "What drives the global thermohaline ocean circulation?", "Density differences in water from temperature and salinity", listOf("The wind blowing across the surface, and nothing else", "The gravitational pull of the Moon on the sea", "Heat escaping from volcanoes on the seafloor"), QuizDifficulty.Expert),
        ask("wyspa_wulkaniczna_powstawanie", "How do volcanic island chains like Hawaii most often form?", "A plate moves over a stationary hot spot in the mantle", listOf("Through a collision of two continents", "Through subduction of one plate under another only", "Through coastal erosion"), QuizDifficulty.Expert)
    )
}
