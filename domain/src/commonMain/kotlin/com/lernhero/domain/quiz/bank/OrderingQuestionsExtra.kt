package com.lernhero.domain.quiz.bank

import com.lernhero.domain.quiz.OrderingEntry
import com.lernhero.domain.quiz.QuizCategory
import com.lernhero.domain.quiz.QuizDifficulty

/**
 * Second batch of "put in order" questions. [OrderingEntry.items] is always already stored
 * in the correct order - the UI layer handles shuffling.
 */
object OrderingQuestionsExtra {

    private fun order(
        id: String,
        prompt: String,
        items: List<String>,
        category: QuizCategory,
        difficulty: QuizDifficulty
    ) = OrderingEntry("ord2_$id", prompt, items, category, difficulty)

    val all: List<OrderingEntry> = listOf(

        /* ------------------------------- Easy ------------------------------- */
        order("etapy_motyla", "Order the stages of a butterfly's development.", listOf("Egg", "Caterpillar", "Chrysalis", "Butterfly"), QuizCategory.Biology, QuizDifficulty.Easy),
        order("kontynenty_wielkosc", "Order these continents from smallest to largest.", listOf("Australia", "Europe", "Africa", "Asia"), QuizCategory.Geography, QuizDifficulty.Easy),

        /* ------------------------------ Medium ------------------------------ */
        order("planety_wielkosc", "Order these planets from smallest to largest.", listOf("Mercury", "Mars", "Neptune", "Jupiter"), QuizCategory.Science, QuizDifficulty.Medium),
        order("miasta_polska", "Order these Polish cities from least to most populous.", listOf("Bialystok", "Wroclaw", "Krakow", "Warsaw"), QuizCategory.Geography, QuizDifficulty.Medium),
        order("epoki_literackie", "Order these literary eras from oldest to newest.", listOf("Antiquity", "The Middle Ages", "The Renaissance", "Romanticism"), QuizCategory.Literature, QuizDifficulty.Medium),
        order("wynalazki_komunikacja", "Order these inventions from oldest to newest.", listOf("The telegraph", "The telephone", "Television", "The mobile phone"), QuizCategory.Technology, QuizDifficulty.Medium),
        order("polska_wojny", "Order these events from Polish history from oldest.", listOf("The Baptism of Poland", "The Battle of Grunwald", "The Constitution of May 3", "Regaining independence"), QuizCategory.History, QuizDifficulty.Medium),
        order("liczby_ulamki2", "Order these values from smallest to largest.", listOf("0.05", "0.5", "5/6", "1.2"), QuizCategory.Math, QuizDifficulty.Medium),
        order("morza_glebokosc", "Order these bodies of water from shallowest to deepest.", listOf("The Baltic Sea", "The North Sea", "The Mediterranean Sea", "The Pacific Ocean"), QuizCategory.Geography, QuizDifficulty.Medium),
        order("sport_dystanse", "Order these track events from shortest to longest.", listOf("100 m", "800 m", "5000 m", "Marathon"), QuizCategory.Sport, QuizDifficulty.Medium),
        order("uklad_pokarmowy", "Order these parts of the digestive system in the order food passes through them.", listOf("Esophagus", "Stomach", "Small intestine", "Large intestine"), QuizCategory.Biology, QuizDifficulty.Medium),
        order("wojny_swiatowe", "Order these 20th-century events from oldest.", listOf("World War I", "World War II", "Gagarin's spaceflight", "The fall of the Berlin Wall"), QuizCategory.History, QuizDifficulty.Medium),

        /* ------------------------------ Hard ------------------------------- */
        order("gwiazdy_zycie", "Order the life stages of a Sun-like star.", listOf("Gas cloud", "Main-sequence star", "Red giant", "White dwarf"), QuizCategory.Science, QuizDifficulty.Hard),
        order("panstwa_powierzchnia", "Order these countries from smallest to largest area.", listOf("Poland", "France", "India", "Canada"), QuizCategory.Geography, QuizDifficulty.Hard),
        order("pierwiastki_masa", "Order these elements from lightest to heaviest.", listOf("Hydrogen", "Carbon", "Iron", "Lead"), QuizCategory.Elements, QuizDifficulty.Hard),
        order("skala_ph", "Order these substances from most acidic to most alkaline.", listOf("Lemon juice", "Coffee", "Water", "Baking soda"), QuizCategory.Science, QuizDifficulty.Hard),
        order("literatura_polska", "Order these Polish works from oldest to newest.", listOf("Bogurodzica", "Kochanowski's Laments", "Pan Tadeusz", "Ferdydurke"), QuizCategory.Literature, QuizDifficulty.Hard),
        order("technologia_komputery", "Order these technologies from oldest to newest.", listOf("ENIAC", "The Intel 4004 microprocessor", "The World Wide Web", "The touchscreen smartphone"), QuizCategory.Technology, QuizDifficulty.Hard),
        order("mitologia_prace", "Order these Greek heroes by the traditional chronology of the myths.", listOf("Perseus", "Heracles", "Theseus", "Odysseus"), QuizCategory.Mythology, QuizDifficulty.Hard),
        order("sport_igrzyska", "Order these Olympic Games from oldest.", listOf("Athens 1896", "Berlin 1936", "Moscow 1980", "London 2012"), QuizCategory.Sport, QuizDifficulty.Hard),
        order("stolice_szerokosc", "Order these capitals from southernmost to northernmost.", listOf("Cairo", "Rome", "Warsaw", "Oslo"), QuizCategory.Capitals, QuizDifficulty.Hard),
        order("bio_hierarchia", "Order these levels of biological organization from smallest.", listOf("Cell", "Tissue", "Organ", "Organism"), QuizCategory.Biology, QuizDifficulty.Hard),

        /* ----------------------------- Expert ---------------------------- */
        order("historia_starozytnosc", "Order these civilizations by when they began to flourish, from oldest.", listOf("Sumer", "Ancient Egypt", "Classical Greece", "The Roman Empire"), QuizCategory.History, QuizDifficulty.Expert),
        order("mohs_pelna", "Order these minerals by the Mohs scale, from softest.", listOf("Gypsum", "Calcite", "Orthoclase", "Corundum"), QuizCategory.Science, QuizDifficulty.Expert),
        order("pierwiastki_liczba_at", "Order these elements by increasing atomic number.", listOf("Helium", "Nitrogen", "Sulfur", "Iron"), QuizCategory.Elements, QuizDifficulty.Expert),
        order("nobel_polacy", "Order these Polish Nobel laureates in Literature by year of the award.", listOf("Henryk Sienkiewicz", "Wladyslaw Reymont", "Czeslaw Milosz", "Olga Tokarczuk"), QuizCategory.Literature, QuizDifficulty.Expert),
        order("krolowie_anglii", "Order these events from English history from oldest.", listOf("The Battle of Hastings", "The Magna Carta", "The Wars of the Roses", "The Industrial Revolution"), QuizCategory.History, QuizDifficulty.Expert),
        order("kosmos_odleglosci", "Order these objects by increasing distance from Earth.", listOf("The Moon", "The Sun", "Proxima Centauri", "The Andromeda Galaxy"), QuizCategory.Science, QuizDifficulty.Expert),
        order("rzeki_dlugosc2", "Order these rivers from shortest to longest.", listOf("Oder", "Rhine", "Danube", "Yangtze"), QuizCategory.Geography, QuizDifficulty.Expert),
        order("ery_geologiczne", "Order these geological eras/periods from oldest.", listOf("Paleozoic", "Mesozoic", "Paleogene", "Neogene"), QuizCategory.Biology, QuizDifficulty.Expert),
        order("mitologia_nordycka", "Order these Norse mythology events in story order.", listOf("The world forming from Ymir", "The building of Asgard", "Baldur's death", "Ragnarok"), QuizCategory.Mythology, QuizDifficulty.Expert)
    )
}
