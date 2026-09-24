package com.lernhero.domain.quiz.bank

import com.lernhero.domain.quiz.QuizCategory
import com.lernhero.domain.quiz.QuizDifficulty
import com.lernhero.domain.quiz.QuizEntry

/** Second batch of geography - the world map, Poland and planetary records. */
object GeographyQuestionsExtra {

    private fun ask(
        id: String,
        prompt: String,
        correct: String,
        wrong: List<String>,
        difficulty: QuizDifficulty
    ) = QuizEntry(
        id = "geo2_$id",
        prompt = prompt,
        correct = correct,
        wrong = wrong,
        category = QuizCategory.Geography,
        difficulty = difficulty
    )

    val all: List<QuizEntry> = listOf(

        /* ------------------------------- Easy ------------------------------- */
        ask("morze_wlochy", "On which sea does Venice sit?", "The Adriatic", listOf("The Tyrrhenian Sea", "The Aegean Sea", "The Black Sea"), QuizDifficulty.Easy),
        ask("kontynent_najwiekszy", "Which continent is the largest?", "Asia", listOf("Africa", "North America", "Europe"), QuizDifficulty.Easy),
        ask("kontynent_najmniejszy", "Which continent is the smallest?", "Australia", listOf("Europe", "Antarctica", "South America"), QuizDifficulty.Easy),
        ask("amazonia_kontynent", "On which continent is the Amazon rainforest?", "South America", listOf("Africa", "Asia", "Australia"), QuizDifficulty.Easy),
        ask("wieza_eiffla", "In which city does the Eiffel Tower stand?", "Paris", listOf("London", "Rome", "Vienna"), QuizDifficulty.Easy),
        ask("morze_martwe", "What is the Dead Sea known for?", "Extremely high salinity", listOf("The coldest water", "The greatest depth", "Being fresh water"), QuizDifficulty.Easy),

        /* ------------------------------ Medium ------------------------------ */
        ask("kanal_sueski", "Which seas does the Suez Canal connect?", "The Mediterranean with the Red Sea", listOf("The Baltic with the North Sea", "The Black Sea with the Aegean", "The Pacific with the Atlantic"), QuizDifficulty.Medium),
        ask("kanal_panamski", "Which oceans does the Panama Canal connect?", "The Atlantic with the Pacific", listOf("The Pacific with the Indian Ocean", "The Atlantic with the Arctic", "The Indian with the Southern Ocean"), QuizDifficulty.Medium),
        ask("gory_ameryka_pd", "What is the longest mountain range in South America called?", "The Andes", listOf("The Cordilleras", "The Appalachians", "The Sierra Nevada"), QuizDifficulty.Medium),
        ask("wulkan_wlochy", "Which volcano on Sicily is the most active in Europe?", "Etna", listOf("Vesuvius", "Stromboli", "Hekla"), QuizDifficulty.Medium),
        ask("panstwo_dwa_konty", "Which country spans two continents: Europe and Asia?", "Turkey", listOf("Greece", "Egypt", "Cyprus"), QuizDifficulty.Medium),
        ask("najmniejsze_panstwo", "Which country is the smallest in the world?", "Vatican City", listOf("Monaco", "San Marino", "Liechtenstein"), QuizDifficulty.Medium),
        ask("ciesnina_bering", "What does the Bering Strait separate?", "Asia from North America", listOf("Europe from Africa", "Australia from Asia", "Greenland from Canada"), QuizDifficulty.Medium),
        ask("pustynia_azja", "Which desert lies in Mongolia and China?", "The Gobi", listOf("The Kalahari", "The Atacama", "The Karakum"), QuizDifficulty.Medium),
        ask("wodospad_wiktorii", "Victoria Falls lies on the border of which two countries?", "Zambia and Zimbabwe", listOf("Kenya and Tanzania", "South Africa and Botswana", "Uganda and Rwanda"), QuizDifficulty.Medium),
        ask("morze_srodziemne_wyspa", "Which Mediterranean island is the largest?", "Sicily", listOf("Sardinia", "Cyprus", "Corsica"), QuizDifficulty.Medium),

        /* ------------------------------ Hard ------------------------------- */
        ask("najsuchsze_miejsce", "Which desert is regarded as the driest place on Earth?", "The Atacama", listOf("The Sahara", "The Gobi", "The Namib"), QuizDifficulty.Hard),
        ask("jezioro_powierzchnia", "Which lake has the largest surface area in the world?", "The Caspian Sea", listOf("Lake Superior", "Lake Baikal", "Lake Victoria"), QuizDifficulty.Hard),
        ask("najdluzsza_granica", "Which two countries share the longest land border in the world?", "Canada and the USA", listOf("Russia and China", "Argentina and Chile", "India and China"), QuizDifficulty.Hard),
        ask("najwyzszy_wodospad", "Which waterfall is the highest in the world?", "Angel Falls", listOf("Niagara Falls", "Iguazu Falls", "Victoria Falls"), QuizDifficulty.Hard),
        ask("panstwa_afryka", "How many countries does Africa have according to the UN?", "54", listOf("47", "60", "51"), QuizDifficulty.Hard),
        ask("rownolezniki_zwrotnik", "What is the tropic in the southern hemisphere called?", "Tropic of Capricorn", listOf("Tropic of Cancer", "Arctic Circle", "Prime Meridian"), QuizDifficulty.Hard),
        ask("greenwich", "Which city does the prime meridian pass through?", "London", listOf("Paris", "Rome", "Madrid"), QuizDifficulty.Hard),
        ask("najdluzsza_rzeka_europy", "Which river is the longest in Europe?", "The Volga", listOf("The Danube", "The Rhine", "The Dnieper"), QuizDifficulty.Hard),
        ask("morze_aralskie", "What happened to the Aral Sea in the 20th century?", "It shrank to a fraction of its former size", listOf("It doubled in size", "It froze permanently", "It merged with the Black Sea"), QuizDifficulty.Hard),
        ask("wyspa_borneo", "Between how many countries is the island of Borneo divided?", "Three", listOf("Two", "Four", "One"), QuizDifficulty.Hard),
        ask("najwiekszy_archipelag", "Which country is the largest island nation in the world?", "Indonesia", listOf("The Philippines", "Japan", "Madagascar"), QuizDifficulty.Hard),

        /* ----------------------------- Expert ---------------------------- */
        ask("panstwo_bez_dostepu", "Which country is Europe's only doubly landlocked state?", "Liechtenstein", listOf("Switzerland", "Austria", "Andorra"), QuizDifficulty.Expert),
        ask("najwiekszy_ladolod", "Where is the world's largest ice sheet?", "Antarctica", listOf("Greenland", "Alaska", "The Himalayas"), QuizDifficulty.Expert),
        ask("nunatak", "What is a nunatak?", "A rocky peak protruding above a glacier", listOf("A narrow bay cut into a fjord coast", "A crescent dune moved by desert wind", "A lake left behind by a glacier"), QuizDifficulty.Expert),
        ask("kaldera_toba", "Where is Lake Toba, formed by a giant caldera?", "On Sumatra", listOf("On Java", "In Japan", "In New Zealand"), QuizDifficulty.Expert),
        ask("rzeka_kongo", "Which river is the deepest in the world?", "The Congo", listOf("The Amazon", "The Yangtze", "The Mississippi"), QuizDifficulty.Expert),
        ask("caspian_status", "Why is the status of the Caspian Sea disputed?", "It's hard to decide whether it's a sea or a lake", listOf("It spans three continents", "Its level changes by 100 m every year", "It has no permanent shoreline"), QuizDifficulty.Expert),
        ask("najwiecej_wulkanow", "Which country has the most active volcanoes?", "Indonesia", listOf("Japan", "Chile", "USA"), QuizDifficulty.Expert),
        ask("linia_wallace", "What does the Wallace Line separate?", "Asian fauna from Australian fauna", listOf("Pacific time zones", "African tectonic plates", "Humid climate from dry climate"), QuizDifficulty.Expert),
        ask("panstwo_najdluzsze", "Which country is the longest from north to south?", "Chile", listOf("Norway", "Brazil", "Russia"), QuizDifficulty.Expert),
        ask("polwysep_najwiekszy", "Which peninsula is the largest in the world?", "The Arabian Peninsula", listOf("The Indian Peninsula", "Scandinavia", "The Balkan Peninsula"), QuizDifficulty.Expert)
    )
}
