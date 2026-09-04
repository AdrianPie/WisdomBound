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
        ask("biegun_polnocny", "What is Earth's northernmost point called?", "The North Pole", listOf("The South Pole", "The Arctic Circle", "The Equator"), QuizDifficulty.Easy),
        ask("rownik_klimat", "What climate is typical at the equator?", "Hot and humid", listOf("Very cold", "Desert everywhere", "Temperate"), QuizDifficulty.Easy),
        ask("pustynia_klimat", "What defines a desert climate?", "Very little rainfall", listOf("Heavy snowfall", "Constant high humidity", "Daily storms"), QuizDifficulty.Easy),
        ask("mapa_kompas", "What is a compass used for?", "Finding directions", listOf("Measuring distance", "Predicting weather", "Measuring altitude"), QuizDifficulty.Easy),
        ask("globus_ksztalt", "What shape is a globe?", "A sphere", listOf("A cube", "A cone", "A cylinder"), QuizDifficulty.Easy),
        ask("morze_ocean_roznica", "What's the difference between a sea and an ocean?", "An ocean is larger and deeper", listOf("A sea is always fresh water", "An ocean has no waves", "They're identical, no difference"), QuizDifficulty.Easy),
        ask("wyspa_definicja", "What is an island?", "Land surrounded by water on all sides", listOf("A mountain above the sea", "Part of a continent", "A type of desert"), QuizDifficulty.Easy),
        ask("polwysep_definicja", "What is a peninsula?", "Land surrounded by water on three sides", listOf("An island in a lake", "An underwater mountain", "A strait"), QuizDifficulty.Easy),
        ask("delta_rzeki", "What forms where some rivers meet the sea?", "A delta", listOf("A canyon", "A waterfall", "A cave"), QuizDifficulty.Easy),
        ask("gora_lodowa", "What is an iceberg made of?", "Broken-off glacier ice", listOf("Volcanic rock", "Frozen sand", "Snow and dust"), QuizDifficulty.Easy),

        /* ------------------------------ Medium ------------------------------ */
        ask("strefa_klimatyczna_polska", "Which climate zone is Poland in?", "Temperate", listOf("Subtropical", "Equatorial", "Polar"), QuizDifficulty.Medium),
        ask("wiatr_halny", "What is the warm, gusty wind blowing from the Tatras called?", "The Halny", listOf("The Mistral", "The Sirocco", "Only the alpine Foehn"), QuizDifficulty.Medium),
        ask("monsun", "What characterizes a monsoon climate?", "A distinct dry season and rainy season", listOf("Constant temperature year-round", "No rainfall all year", "Perpetual snow"), QuizDifficulty.Medium),
        ask("step_definicja", "What is a steppe?", "A vast, grassy plain with a dry climate", listOf("A dense tropical forest", "A sandy desert", "A swamp"), QuizDifficulty.Medium),
        ask("tundra_definicja", "Where is tundra found?", "In cold, polar regions", listOf("In the equatorial zone", "In hot deserts", "In tropical mountains"), QuizDifficulty.Medium),
        ask("wyzyna_gora_roznica", "How does a mountain differ from an upland (plateau)?", "A mountain has a distinct, steep peak", listOf("An upland is always higher", "A mountain is always flat", "They're the same thing"), QuizDifficulty.Medium),
        ask("archipelag_definicja", "What is an archipelago?", "A group of islands", listOf("A single large island", "A type of bay", "A mountain range"), QuizDifficulty.Medium),
        ask("wybrzeze_klifowe", "What characterizes a cliff coast?", "Steep, rocky bluffs above the sea", listOf("Flat, sandy beaches", "Coastal marshes", "Only coral reefs"), QuizDifficulty.Medium),
        ask("rafa_koralowa", "What is a coral reef?", "A structure built by coral organisms", listOf("An underwater volcanic rock", "An iceberg", "A sandbank"), QuizDifficulty.Medium),
        ask("czas_uniwersalny", "What is the world's universal reference time called?", "Coordinated Universal Time (UTC)", listOf("Uncorrected local Greenwich time", "Sidereal time", "Paris atomic time"), QuizDifficulty.Medium),

        /* ------------------------------ Hard ------------------------------- */
        ask("el_nino", "What is the El Nino phenomenon?", "A periodic warming of Pacific waters affecting world weather", listOf("A permanent cooling of the Arctic", "A cyclical earthquake", "A type of Atlantic hurricane"), QuizDifficulty.Hard),
        ask("prad_golfsztrom", "What is the warm ocean current warming Western Europe called?", "The Gulf Stream", listOf("The Humboldt Current", "The Labrador Current", "The California Current"), QuizDifficulty.Hard),
        ask("strefa_subdukcji", "What happens at a tectonic subduction zone?", "One plate slides beneath another", listOf("Plates move apart from each other", "Plates stay motionless", "Only flat plains form"), QuizDifficulty.Hard),
        ask("ognisty_pierscien", "What is the Pacific Ring of Fire?", "A zone of frequent earthquakes and volcanoes around the Pacific", listOf("A mountain range in South America", "An ocean current", "An equatorial climate zone"), QuizDifficulty.Hard),
        ask("wiecznamarzlocz", "What is permafrost?", "Ground that stays permanently frozen year-round", listOf("A mountain glacier", "Seasonal snow", "Sea ice"), QuizDifficulty.Hard),
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
