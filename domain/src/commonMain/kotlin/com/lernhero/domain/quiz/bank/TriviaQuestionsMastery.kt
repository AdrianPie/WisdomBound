package com.lernhero.domain.quiz.bank

import com.lernhero.domain.quiz.QuizCategory
import com.lernhero.domain.quiz.QuizDifficulty
import com.lernhero.domain.quiz.QuizEntry

/**
 * Partia dopisana pod tryb opanowania tematu ([com.lernhero.domain.progress.CategoryMastery]).
 *
 * Endless nie losuje juz po calym banku - gracz siada na JEDNYM temacie i przechodzi w nim
 * dwadziescia fal, potem wraca i robi to jeszcze raz, wyzej. Kategoria, ktora miala sto pytan,
 * przy takim uzyciu wyczerpuje sie w dwa wejscia i zaczyna sie powtarzac. Ta partia dokłada
 * material tam, gdzie ten tryb boli najbardziej: w gornych szczeblach trudnosci, ktore po awansie
 * staja sie startowymi.
 *
 * Stad rozklad inny niz w [TriviaQuestionsOrigins]: prawie nie ma tu pytan latwych, bo na Easy
 * bank byl juz gesty - brakowalo Hard i Expert, na ktorych ląduje gracz z poziomem 5+.
 */
object TriviaQuestionsMastery {

    private fun ask(
        id: String,
        prompt: String,
        correct: String,
        wrong: List<String>,
        category: QuizCategory,
        difficulty: QuizDifficulty
    ) = QuizEntry(
        id = "mst_$id",
        prompt = prompt,
        correct = correct,
        wrong = wrong,
        category = category,
        difficulty = difficulty
    )

    private val A = QuizCategory.Animals
    private val B = QuizCategory.Biology
    private val S = QuizCategory.Science
    private val G = QuizCategory.Geography
    private val H = QuizCategory.History
    private val L = QuizCategory.Literature
    private val T = QuizCategory.Technology
    private val M = QuizCategory.Mythology
    private val C = QuizCategory.Elements
    private val N = QuizCategory.Math
    private val P = QuizCategory.Sport
    private val K = QuizCategory.Capitals

    private val D = QuizDifficulty.Medium
    private val R = QuizDifficulty.Hard
    private val X = QuizDifficulty.Expert

    val all: List<QuizEntry> = listOf(

        /* ------------------------------- Zwierzeta ------------------------------- */
        ask(
            id = "wombat_odchody",
            prompt = "The wombat is the only animal known to produce droppings of what shape?",
            correct = "Cubes",
            wrong = listOf("Spirals", "Discs", "Hexagons"),
            category = A,
            difficulty = R
        ),
        ask(
            id = "flaming_kolor",
            prompt = "Flamingos get their pink colour from what?",
            correct = "Their diet",
            wrong = listOf("Sunlight exposure", "Their age", "Mineral-rich mud"),
            category = A,
            difficulty = D
        ),
        ask(
            id = "sowa_oczy",
            prompt = "Why must an owl turn its whole head to look sideways?",
            correct = "Its eyes cannot rotate",
            wrong = listOf("Its eyes have no lids", "Its neck is rigid", "It sees only in front"),
            category = A,
            difficulty = R
        ),
        ask(
            id = "delfin_sen",
            prompt = "How does a dolphin sleep without drowning?",
            correct = "Half its brain stays awake",
            wrong = listOf("It floats on its back", "It sleeps on the sea floor", "It stops breathing"),
            category = A,
            difficulty = R
        ),
        ask(
            id = "koala_palce",
            prompt = "Koalas share which surprising trait with humans?",
            correct = "Fingerprints",
            wrong = listOf("Colour vision", "Sweat glands on the palms", "A collarbone"),
            category = A,
            difficulty = X
        ),
        ask(
            id = "pszczola_taniec",
            prompt = "A honeybee's waggle dance communicates what to the hive?",
            correct = "Direction and distance to food",
            wrong = listOf("The queen's health", "A warning of predators", "The time of day"),
            category = A,
            difficulty = D
        ),
        ask(
            id = "rekin_wiek_luski",
            prompt = "Shark skin feels like sandpaper because it is covered in what?",
            correct = "Tiny tooth-like scales",
            wrong = listOf("Dried salt crystals", "Hardened mucus", "Bony plates"),
            category = A,
            difficulty = R
        ),
        ask(
            id = "kot_mruczenie",
            prompt = "Which of these big cats cannot roar?",
            correct = "The cheetah",
            wrong = listOf("The lion", "The tiger", "The leopard"),
            category = A,
            difficulty = R
        ),

        /* --------------------------------- Cialo --------------------------------- */
        ask(
            id = "watroba_regeneracja",
            prompt = "Which human organ can regrow after losing a large part of itself?",
            correct = "The liver",
            wrong = listOf("The kidney", "The pancreas", "The spleen"),
            category = B,
            difficulty = D
        ),
        ask(
            id = "kosc_strzemiaczko",
            prompt = "The smallest bone in the human body sits where?",
            correct = "In the ear",
            wrong = listOf("In the nose", "In the little toe", "In the wrist"),
            category = B,
            difficulty = D
        ),
        ask(
            id = "rogowka_naczynia",
            prompt = "Which part of the human body has no blood vessels at all?",
            correct = "The cornea",
            wrong = listOf("The earlobe", "The fingernail bed", "The eyelid"),
            category = B,
            difficulty = X
        ),
        ask(
            id = "czkawka_przepona",
            prompt = "A hiccup is a sudden spasm of which muscle?",
            correct = "The diaphragm",
            wrong = listOf("The stomach wall", "The throat", "The heart"),
            category = B,
            difficulty = D
        ),
        ask(
            id = "dna_dlugosc",
            prompt = "Uncoiled, the DNA in a single human cell would stretch roughly how far?",
            correct = "About two metres",
            wrong = listOf("About two centimetres", "About twenty metres", "About two kilometres"),
            category = B,
            difficulty = R
        ),
        ask(
            id = "bakterie_liczba",
            prompt = "Roughly how do bacterial cells in your body compare with your own cells?",
            correct = "Comparable in number",
            wrong = listOf("A thousand times fewer", "A million times more", "Only a few thousand total"),
            category = B,
            difficulty = R
        ),

        /* ------------------------------- Geografia ------------------------------- */
        ask(
            id = "wyspa_w_jeziorze",
            prompt = "Which country contains the largest island within a lake on an island?",
            correct = "Canada",
            wrong = listOf("Finland", "Indonesia", "New Zealand"),
            category = G,
            difficulty = X
        ),
        ask(
            id = "kraj_dwa_oceany",
            prompt = "Panama is unusual in that its Caribbean coast lies where relative to its Pacific coast?",
            correct = "To the north",
            wrong = listOf("To the east", "To the south", "To the west"),
            category = G,
            difficulty = D
        ),
        ask(
            id = "kraj_bez_rzeki",
            prompt = "Saudi Arabia is notable for having none of what?",
            correct = "Permanent rivers",
            wrong = listOf("Mountains", "Coastline", "Natural lakes and mountains both"),
            category = G,
            difficulty = R
        ),
        ask(
            id = "istambul_kontynenty",
            prompt = "Which major city sits on two continents at once?",
            correct = "Istanbul",
            wrong = listOf("Cairo", "Athens", "Baku"),
            category = G,
            difficulty = D
        ),
        ask(
            id = "hispaniola_podzial",
            prompt = "Haiti and the Dominican Republic share which island?",
            correct = "Hispaniola",
            wrong = listOf("Cuba", "Jamaica", "Puerto Rico"),
            category = G,
            difficulty = R
        ),
        ask(
            id = "punkt_najnizszy",
            prompt = "The lowest exposed land on Earth lies on the shore of what?",
            correct = "The Dead Sea",
            wrong = listOf("The Caspian Sea", "Death Valley", "Lake Assal"),
            category = G,
            difficulty = D
        ),
        ask(
            id = "afryka_rownik",
            prompt = "The equator passes through all of these continents except which?",
            correct = "Australia",
            wrong = listOf("Africa", "South America", "Asia"),
            category = G,
            difficulty = R
        ),
        ask(
            id = "kraj_enklawa",
            prompt = "Which country is entirely surrounded by just one other country?",
            correct = "Lesotho",
            wrong = listOf("Nepal", "Andorra", "Luxembourg"),
            category = G,
            difficulty = R
        ),

        /* -------------------------------- Stolice -------------------------------- */
        ask(
            id = "stolica_najdalej_polnoc",
            prompt = "Which of these capital cities lies furthest north?",
            correct = "Reykjavik",
            wrong = listOf("Helsinki", "Oslo", "Tallinn"),
            category = K,
            difficulty = R
        ),
        ask(
            id = "stolica_australia",
            prompt = "Canberra became Australia's capital mainly to settle a dispute between which cities?",
            correct = "Sydney and Melbourne",
            wrong = listOf("Perth and Adelaide", "Brisbane and Sydney", "Hobart and Melbourne"),
            category = K,
            difficulty = R
        ),
        ask(
            id = "stolica_mjanma",
            prompt = "Myanmar moved its seat of government to which purpose-built city?",
            correct = "Naypyidaw",
            wrong = listOf("Yangon", "Mandalay", "Bago"),
            category = K,
            difficulty = D
        ),
        ask(
            id = "stolica_rpa",
            prompt = "South Africa is unusual among countries for having how many capitals?",
            correct = "Three",
            wrong = listOf("One", "Two", "Four"),
            category = K,
            difficulty = R
        ),
        ask(
            id = "stolica_dwie_nazwy",
            prompt = "Which country's seat of government sits in Amsterdam's rival city, The Hague?",
            correct = "The Netherlands",
            wrong = listOf("Belgium", "Denmark", "Luxembourg"),
            category = K,
            difficulty = D
        ),
        ask(
            id = "stolica_brazylia",
            prompt = "Brasilia was built from nothing in the 1950s to replace which capital?",
            correct = "Rio de Janeiro",
            wrong = listOf("Sao Paulo", "Salvador", "Belo Horizonte"),
            category = K,
            difficulty = R
        ),
        ask(
            id = "stolica_kazachstan",
            prompt = "Kazakhstan moved its capital from Almaty to which city?",
            correct = "Astana",
            wrong = listOf("Shymkent", "Karaganda", "Aktobe"),
            category = K,
            difficulty = X
        ),

        /* -------------------------------- Historia -------------------------------- */
        ask(
            id = "wojna_najkrotsza",
            prompt = "The shortest war in recorded history lasted roughly how long?",
            correct = "Under an hour",
            wrong = listOf("Three days", "Two weeks", "A single month"),
            category = H,
            difficulty = R
        ),
        ask(
            id = "oxford_azteków",
            prompt = "Which was founded first: the University of Oxford or the Aztec Empire?",
            correct = "Oxford",
            wrong = listOf("The Aztec Empire", "They began the same decade", "Neither has a known date"),
            category = H,
            difficulty = R
        ),
        ask(
            id = "faks_lincoln",
            prompt = "Which empire was still standing when the first fax machine was patented?",
            correct = "The Ottoman Empire",
            wrong = listOf("The Roman Empire", "The Aztec Empire", "The Mongol Empire"),
            category = H,
            difficulty = X
        ),
        ask(
            id = "mur_widocznosc",
            prompt = "What is true about seeing the Great Wall of China from space?",
            correct = "It cannot be seen with the naked eye",
            wrong = listOf("It is the only visible structure", "It is visible from the Moon", "It is visible only at night"),
            category = H,
            difficulty = D
        ),
        ask(
            id = "napoleon_wzrost",
            prompt = "Napoleon's reputation for being short came mainly from what?",
            correct = "A difference between French and English units",
            wrong = listOf("An illness he suffered as a small child", "The unusually tall guards around him", "Claims he made himself in his letters"),
            category = H,
            difficulty = X
        ),
        ask(
            id = "wikingowie_helmy",
            prompt = "Horned Viking helmets were popularised by what?",
            correct = "19th-century opera costumes",
            wrong = listOf("Norse burial finds", "Medieval chronicles", "Roman accounts"),
            category = H,
            difficulty = R
        ),
        ask(
            id = "pyramida_robotnicy",
            prompt = "Evidence suggests the Great Pyramid was built mainly by whom?",
            correct = "Paid labourers",
            wrong = listOf("Foreign slaves", "Prisoners of war", "Conscripted soldiers"),
            category = H,
            difficulty = R
        ),

        /* ------------------------------- Literatura ------------------------------- */
        ask(
            id = "don_kichot_pierwszy",
            prompt = "Don Quixote is often called the first what?",
            correct = "Modern novel",
            wrong = listOf("Printed book", "Autobiography", "Detective story"),
            category = L,
            difficulty = D
        ),
        ask(
            id = "frankenstein_autor",
            prompt = "Frankenstein was written by an author who was how old at the time?",
            correct = "A teenager",
            wrong = listOf("In her forties", "In her sixties", "In her thirties"),
            category = L,
            difficulty = R
        ),
        ask(
            id = "sherlock_zwrot",
            prompt = "Which famous line does Sherlock Holmes never actually say in the books?",
            correct = "Elementary, my dear Watson",
            wrong = listOf("The game is afoot", "You see, but you do not observe", "Data, data, data"),
            category = L,
            difficulty = R
        ),
        ask(
            id = "tolkien_zawod",
            prompt = "Besides writing fiction, J.R.R. Tolkien worked professionally as what?",
            correct = "A philologist",
            wrong = listOf("An architect", "A cartographer", "A composer"),
            category = L,
            difficulty = R
        ),
        ask(
            id = "orwell_rok",
            prompt = "Orwell's title 1984 is widely thought to be a rearrangement of which year?",
            correct = "1948",
            wrong = listOf("1894", "1849", "1940"),
            category = L,
            difficulty = R
        ),
        ask(
            id = "currer_bell",
            prompt = "Which novelist first published under the pen name Currer Bell?",
            correct = "Charlotte Bronte",
            wrong = listOf("Jane Austen", "George Eliot", "Elizabeth Gaskell"),
            category = L,
            difficulty = R
        ),

        /* ------------------------------- Technologia ------------------------------- */
        ask(
            id = "apollo_pamiec",
            prompt = "The Apollo Guidance Computer had roughly how much memory?",
            correct = "A few kilobytes",
            wrong = listOf("A few megabytes", "A few gigabytes", "About one terabyte"),
            category = T,
            difficulty = R
        ),
        ask(
            id = "qwerty_uklad",
            prompt = "The QWERTY layout was arranged the way it is mainly to do what?",
            correct = "Suit early typewriter mechanics",
            wrong = listOf("Maximise typing speed", "Match alphabetical order", "Fit the English alphabet exactly"),
            category = T,
            difficulty = D
        ),
        ask(
            id = "gps_relatywistyka",
            prompt = "GPS satellites must correct their clocks for what?",
            correct = "Relativistic time differences",
            wrong = listOf("Magnetic interference", "Solar wind drag", "Atmospheric pressure"),
            category = T,
            difficulty = X
        ),
        ask(
            id = "pierwsza_strona",
            prompt = "The world's first website was created to explain what?",
            correct = "The World Wide Web itself",
            wrong = listOf("A physics experiment", "A university timetable", "An email system"),
            category = T,
            difficulty = R
        ),
        ask(
            id = "kod_ada",
            prompt = "Ada Lovelace is credited with writing the first what?",
            correct = "Computer algorithm",
            wrong = listOf("Operating system", "Programming manual", "Electronic circuit"),
            category = T,
            difficulty = D
        ),
        ask(
            id = "usb_wtyczka",
            prompt = "The original USB plug was designed to be reversible but was not, because of what?",
            correct = "Cost",
            wrong = listOf("Patent limits", "Signal loss", "Safety rules"),
            category = T,
            difficulty = X
        ),

        /* --------------------------------- Nauka --------------------------------- */
        ask(
            id = "szklo_ciecz",
            prompt = "What is the truth about old window glass being thicker at the bottom?",
            correct = "It is a result of how it was made",
            wrong = listOf("Glass slowly flows downward", "Gravity compresses it", "Moisture settles in it"),
            category = S,
            difficulty = X
        ),
        ask(
            id = "woda_wrzenie_gora",
            prompt = "Why does water boil at a lower temperature on a mountain?",
            correct = "Lower air pressure",
            wrong = listOf("Thinner oxygen", "Colder surroundings", "Lower humidity"),
            category = S,
            difficulty = D
        ),
        ask(
            id = "swiatlo_diament",
            prompt = "Light slows down the most while passing through which of these?",
            correct = "Diamond",
            wrong = listOf("Air", "Water", "Window glass"),
            category = S,
            difficulty = D
        ),
        ask(
            id = "blyskawica_temperatura",
            prompt = "A lightning bolt can be roughly how hot compared with the Sun's surface?",
            correct = "Several times hotter",
            wrong = listOf("About the same", "Half as hot", "A tenth as hot"),
            category = S,
            difficulty = R
        ),
        ask(
            id = "efekt_dopplera",
            prompt = "The rising then falling pitch of a passing siren is explained by what?",
            correct = "The Doppler effect",
            wrong = listOf("Refraction", "Resonance", "Diffraction"),
            category = S,
            difficulty = D
        ),
        ask(
            id = "antymateria_zderzenie",
            prompt = "When matter meets antimatter, what happens?",
            correct = "Both convert to energy",
            wrong = listOf("They repel each other", "They form a new element", "Nothing measurable"),
            category = S,
            difficulty = R
        ),

        /* ------------------------------- Pierwiastki ------------------------------- */
        ask(
            id = "gal_dlon",
            prompt = "Which metal will melt in your hand from body heat alone?",
            correct = "Gallium",
            wrong = listOf("Aluminium", "Zinc", "Tin"),
            category = C,
            difficulty = D
        ),
        ask(
            id = "wodor_wszechswiat",
            prompt = "Which element makes up most of the ordinary matter in the universe?",
            correct = "Hydrogen",
            wrong = listOf("Helium", "Oxygen", "Carbon"),
            category = C,
            difficulty = D
        ),
        ask(
            id = "krzem_skorupa",
            prompt = "After oxygen, which element is most abundant in Earth's crust?",
            correct = "Silicon",
            wrong = listOf("Iron", "Aluminium", "Calcium"),
            category = C,
            difficulty = R
        ),
        ask(
            id = "fosfor_nazwa",
            prompt = "Phosphorus takes its name from a Greek phrase meaning what?",
            correct = "Light-bearer",
            wrong = listOf("Bone-maker", "Fire-eater", "Sea-salt"),
            category = C,
            difficulty = R
        ),
        ask(
            id = "cez_zegar",
            prompt = "The modern definition of the second is based on which element?",
            correct = "Caesium",
            wrong = listOf("Rubidium", "Strontium", "Hydrogen"),
            category = C,
            difficulty = X
        ),

        /* ------------------------------- Matematyka ------------------------------- */
        ask(
            id = "zero_indie",
            prompt = "Zero as a number in its own right was first fully developed where?",
            correct = "India",
            wrong = listOf("Greece", "Egypt", "Rome"),
            category = N,
            difficulty = R
        ),
        ask(
            id = "liczba_pierwsza_parzysta",
            prompt = "How many even prime numbers are there?",
            correct = "Exactly one",
            wrong = listOf("None", "Infinitely many", "Exactly two"),
            category = N,
            difficulty = D
        ),
        ask(
            id = "wstega_mobiusa",
            prompt = "How many sides does a Mobius strip have?",
            correct = "One",
            wrong = listOf("Two", "Three", "None"),
            category = N,
            difficulty = R
        ),
        ask(
            id = "fibonacci_natura",
            prompt = "The Fibonacci sequence shows up in nature most famously in what?",
            correct = "Spiral arrangements in plants",
            wrong = listOf("Animal migration routes", "Snowflake symmetry", "Tidal cycles"),
            category = N,
            difficulty = D
        ),
        ask(
            id = "nieskonczonosc_rozne",
            prompt = "Mathematically, how do the infinity of whole numbers and of real numbers compare?",
            correct = "The reals are a larger infinity",
            wrong = listOf("They are equal", "The whole numbers are larger", "Neither can be compared"),
            category = N,
            difficulty = X
        ),

        /* ---------------------------------- Sport ---------------------------------- */
        ask(
            id = "olimpiada_przerwa",
            prompt = "The modern Olympic Games have been cancelled only because of what?",
            correct = "World wars",
            wrong = listOf("Financial crises", "Disease outbreaks", "Boycotts"),
            category = P,
            difficulty = R
        ),
        ask(
            id = "pilka_lata",
            prompt = "A standard football match clock runs for 90 minutes, but the ball is typically in play for how long?",
            correct = "Under an hour",
            wrong = listOf("About 80 minutes", "The full 90", "About 75 minutes"),
            category = P,
            difficulty = R
        ),
        ask(
            id = "boks_rundy",
            prompt = "Professional championship boxing bouts were shortened from 15 rounds to 12 mainly for what reason?",
            correct = "Fighter safety",
            wrong = listOf("Television schedules", "Judging disputes", "Crowd attention"),
            category = P,
            difficulty = R
        ),
        ask(
            id = "koszykowka_kosz",
            prompt = "The first basketball games used what as the goal?",
            correct = "A peach basket",
            wrong = listOf("A barrel", "A metal hoop", "A painted square"),
            category = P,
            difficulty = D
        ),
        ask(
            id = "wimbledon_kolor",
            prompt = "Wimbledon is unique among tennis majors for requiring what?",
            correct = "Almost entirely white clothing",
            wrong = listOf("Wooden rackets", "No line judges", "Matches only in daylight"),
            category = P,
            difficulty = D
        ),

        /* -------------------------------- Mitologia -------------------------------- */
        ask(
            id = "syzyf_kara",
            prompt = "Sisyphus was condemned to eternally do what?",
            correct = "Roll a boulder uphill",
            wrong = listOf("Reach for fruit he cannot grasp", "Be chained to a rock", "Carry the sky"),
            category = M,
            difficulty = D
        ),
        ask(
            id = "atlas_kara",
            prompt = "In Greek myth, Atlas was made to hold up what?",
            correct = "The sky",
            wrong = listOf("The Earth", "A mountain", "The sea"),
            category = M,
            difficulty = D
        ),
        ask(
            id = "ragnarok_koniec",
            prompt = "In Norse myth, Ragnarok describes what?",
            correct = "The end and rebirth of the world",
            wrong = listOf("The creation of humans", "A feast of the gods", "Odin's journey for wisdom"),
            category = M,
            difficulty = R
        ),
        ask(
            id = "anubis_rola",
            prompt = "The Egyptian god Anubis presided over what?",
            correct = "Mummification and the dead",
            wrong = listOf("The Nile flood", "War and hunting", "Writing and wisdom"),
            category = M,
            difficulty = R
        ),
        ask(
            id = "puszka_pandory",
            prompt = "What was left inside Pandora's container after everything escaped?",
            correct = "Hope",
            wrong = listOf("Fear", "Wisdom", "Time"),
            category = M,
            difficulty = D
        ),
        ask(
            id = "kasandra_klatwa",
            prompt = "Which figure could foresee the future but was cursed never to be believed?",
            correct = "Cassandra",
            wrong = listOf("Medea", "Circe", "Antigone"),
            category = M,
            difficulty = R
        )
    )
}
