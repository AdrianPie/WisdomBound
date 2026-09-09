package com.lernhero.domain.quiz.bank

import com.lernhero.domain.quiz.OrderingEntry
import com.lernhero.domain.quiz.QuizCategory
import com.lernhero.domain.quiz.QuizDifficulty

/**
 * Pytania "ustaw w kolejnosci" - rankingi swiatowe, bez watkow lokalnych.
 *
 * Format ma dzis drugie zycie w quizach online wlasnie dlatego, ze nie da sie go strzelic: przy
 * czterech pozycjach jest 24 ukladow, wiec przypadkowe trafienie to 4%. Jednoczesnie jest
 * najlagodniejszy dla gracza, ktory "mniej wiecej wie" - kolejnosc da sie wyprowadzic z jednego
 * pewnego punktu odniesienia.
 *
 * [OrderingEntry.items] jest ZAWSZE zapisane w poprawnej kolejnosci; tasowaniem zajmuje sie UI.
 * Kazdy ranking dobrany tak, zeby roznice miedzy sasiednimi pozycjami byly wyrazne - "ustaw wedlug
 * powierzchni: Kanada, Chiny, USA" byloby sporem o metode pomiaru, a nie pytaniem.
 */
object OrderingQuestionsWorld {

    private fun ord(
        id: String,
        prompt: String,
        items: List<String>,
        category: QuizCategory,
        difficulty: QuizDifficulty
    ) = OrderingEntry(
        id = "ordw_$id",
        prompt = prompt,
        items = items,
        category = category,
        difficulty = difficulty
    )

    val all: List<OrderingEntry> = listOf(

        /* ------------------------------- Geografia -------------------------------- */
        ord("ludnosc_krajow", "Order these countries by population, from smallest to largest.",
            listOf("Iceland", "Norway", "Poland", "Germany"),
            QuizCategory.Geography, QuizDifficulty.Easy),
        ord("powierzchnia_krajow", "Order these countries by area, from smallest to largest.",
            listOf("Malta", "Belgium", "France", "Russia"),
            QuizCategory.Geography, QuizDifficulty.Easy),
        ord("miasta_polnoc", "Order these cities from north to south.",
            listOf("Oslo", "Berlin", "Rome", "Cairo"),
            QuizCategory.Geography, QuizDifficulty.Easy),
        ord("gory_wysokosc", "Order these mountains by height, from lowest to highest.",
            listOf("Mont Blanc", "Kilimanjaro", "Aconcagua", "Mount Everest"),
            QuizCategory.Geography, QuizDifficulty.Medium),
        ord("rzeki_dlugosc", "Order these rivers by length, from shortest to longest.",
            listOf("The Rhine", "The Danube", "The Volga", "The Nile"),
            QuizCategory.Geography, QuizDifficulty.Medium),
        ord("oceany_powierzchnia", "Order these oceans by area, from smallest to largest.",
            listOf("The Arctic Ocean", "The Indian Ocean", "The Atlantic", "The Pacific"),
            QuizCategory.Geography, QuizDifficulty.Medium),
        ord("morza_glebokosc", "Order these waters by their greatest depth, from shallowest to deepest.",
            listOf("The Baltic Sea", "The Mediterranean Sea", "The Caribbean Sea", "The Mariana Trench"),
            QuizCategory.Geography, QuizDifficulty.Hard),
        ord("miasta_ludnosc", "Order these cities by population, from smallest to largest.",
            listOf("Reykjavik", "Amsterdam", "Berlin", "Tokyo"),
            QuizCategory.Geography, QuizDifficulty.Easy),
        ord("male_panstwa", "Order these countries by area, from smallest to largest.",
            listOf("Vatican City", "Monaco", "Malta", "Luxembourg"),
            QuizCategory.Geography, QuizDifficulty.Hard),
        ord("ue_akcesja", "Order these countries by when they joined the EU or its predecessor, earliest first.",
            listOf("France", "Denmark", "Sweden", "Croatia"),
            QuizCategory.Geography, QuizDifficulty.Hard),

        /* -------------------------------- Historia -------------------------------- */
        ord("wydarzenia_swiat", "Order these events from oldest to most recent.",
            listOf("The fall of Constantinople", "Columbus reaching America", "The French Revolution", "The Moon landing"),
            QuizCategory.History, QuizDifficulty.Easy),
        ord("xx_wiek", "Order these 20th-century events from earliest to latest.",
            listOf("The outbreak of World War I", "The outbreak of World War II", "The building of the Berlin Wall", "The fall of the Berlin Wall"),
            QuizCategory.History, QuizDifficulty.Easy),
        ord("cesarze_rzymu", "Order these Roman emperors chronologically, earliest first.",
            listOf("Augustus", "Nero", "Trajan", "Constantine the Great"),
            QuizCategory.History, QuizDifficulty.Hard),
        ord("cywilizacje", "Order these from oldest to most recent.",
            listOf("The Sumerian city-states", "Classical Athens", "The Roman Empire", "The Byzantine Empire"),
            QuizCategory.History, QuizDifficulty.Medium),
        ord("imperia_azji", "Order these empires chronologically, earliest first.",
            listOf("The Han dynasty", "The Mongol Empire", "The Mughal Empire", "The Meiji Empire of Japan"),
            QuizCategory.History, QuizDifficulty.Hard),
        ord("dekolonizacja", "Order these by when they gained independence, earliest first.",
            listOf("The United States", "Brazil", "India", "Namibia"),
            QuizCategory.History, QuizDifficulty.Medium),

        /* ------------------------------- Technologia ------------------------------ */
        ord("wynalazki_komunikacja", "Order these means of communication by when they appeared, oldest first.",
            listOf("The electric telegraph", "The telephone", "Radio broadcasting", "The World Wide Web"),
            QuizCategory.Technology, QuizDifficulty.Easy),
        ord("firmy_zalozenie", "Order these companies by their founding year, earliest first.",
            listOf("Coca-Cola", "Nintendo", "IBM", "Apple"),
            QuizCategory.Technology, QuizDifficulty.Hard),
        ord("nosniki_danych", "Order these storage media by when they became common, oldest first.",
            listOf("The punched card", "The floppy disk", "The CD-ROM", "The USB flash drive"),
            QuizCategory.Technology, QuizDifficulty.Medium),
        ord("kosmos_kamienie", "Order these spaceflight milestones chronologically, earliest first.",
            listOf("Sputnik 1", "Gagarin's flight", "The first Moon landing", "The first module of the ISS"),
            QuizCategory.Technology, QuizDifficulty.Medium),

        /* ---------------------------- Nauka i pierwiastki ------------------------- */
        ord("planety_odleglosc", "Order these planets by distance from the Sun, nearest first.",
            listOf("Mercury", "Venus", "Earth", "Mars"),
            QuizCategory.Science, QuizDifficulty.Easy),
        ord("planety_wielkosc", "Order these planets by size, smallest first.",
            listOf("Mercury", "Mars", "Venus", "Earth"),
            QuizCategory.Science, QuizDifficulty.Medium),
        ord("odleglosci_kosmos", "Order these by distance from Earth, nearest first.",
            listOf("The Moon", "The Sun", "Proxima Centauri", "The Andromeda Galaxy"),
            QuizCategory.Science, QuizDifficulty.Medium),
        ord("fale_dlugosc", "Order these kinds of radiation by wavelength, shortest first.",
            listOf("Gamma rays", "Ultraviolet", "Visible light", "Radio waves"),
            QuizCategory.Science, QuizDifficulty.Hard),
        ord("ph_skala", "Order these by pH, most acidic first.",
            listOf("Lemon juice", "Black coffee", "Pure water", "Baking soda solution"),
            QuizCategory.Science, QuizDifficulty.Medium),
        ord("ery_geologiczne", "Order these geological periods from oldest to youngest.",
            listOf("The Cambrian", "The Jurassic", "The Cretaceous", "The Paleogene"),
            QuizCategory.Science, QuizDifficulty.Hard),
        ord("ewolucja_czlowieka", "Order these from oldest to most recent.",
            listOf("Australopithecus", "Homo habilis", "Homo erectus", "Homo sapiens"),
            QuizCategory.Biology, QuizDifficulty.Medium),
        ord("metale_topnienie", "Order these metals by melting point, lowest first.",
            listOf("Mercury", "Tin", "Iron", "Tungsten"),
            QuizCategory.Elements, QuizDifficulty.Hard),
        ord("liczba_atomowa", "Order these elements by atomic number, lowest first.",
            listOf("Hydrogen", "Carbon", "Iron", "Gold"),
            QuizCategory.Elements, QuizDifficulty.Easy),

        /* --------------------------- Zwierzeta i biologia ------------------------- */
        ord("zwierzeta_predkosc", "Order these by top speed, slowest first.",
            listOf("A human sprinter", "A greyhound", "A cheetah", "A peregrine falcon in a dive"),
            QuizCategory.Animals, QuizDifficulty.Medium),

        /* --------------------------- Literatura i sport --------------------------- */
        ord("ksiazki_publikacja", "Order these books by publication date, oldest first.",
            listOf("Don Quixote", "Frankenstein", "1984", "Harry Potter and the Philosopher's Stone"),
            QuizCategory.Literature, QuizDifficulty.Medium),
        ord("dziela_klasyczne", "Order these works from oldest to most recent.",
            listOf("The Odyssey", "The Divine Comedy", "Hamlet", "War and Peace"),
            QuizCategory.Literature, QuizDifficulty.Medium),
        ord("zawodnicy_na_boisku", "Order these sports by how many players one team fields, fewest first.",
            listOf("Tennis singles", "Basketball", "Football", "Rugby union"),
            QuizCategory.Sport, QuizDifficulty.Medium)
    )
}
