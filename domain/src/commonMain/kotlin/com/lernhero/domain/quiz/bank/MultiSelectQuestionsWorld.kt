package com.lernhero.domain.quiz.bank

import com.lernhero.domain.quiz.MultiSelectEntry
import com.lernhero.domain.quiz.QuizCategory
import com.lernhero.domain.quiz.QuizDifficulty

/**
 * "Zaznacz wszystkie prawdziwe" - druga partia, bez watkow lokalnych.
 *
 * Zasada budowania dystraktorow jest ta sama co w [MultiSelectQuestions] i wazniejsza niz sama
 * tresc: bledna odpowiedz musi byc WIARYGODNA. "Ktore z tych to dinozaury?" z dystraktorem
 * "rower" nie sprawdza niczego; z dystraktorem "pterodaktyl" sprawdza, czy gracz wie, ze pterozaury
 * nie byly dinozaurami. Kilka pytan tutaj jest zbudowanych dokladnie wokol takich pulapek.
 */
object MultiSelectQuestionsWorld {

    private fun e(
        id: String,
        prompt: String,
        correct: List<String>,
        wrong: List<String>,
        category: QuizCategory,
        difficulty: QuizDifficulty
    ) = MultiSelectEntry("msw_$id", prompt, correct, wrong, category, difficulty)

    val all: List<MultiSelectEntry> = listOf(

        /* -------------------------------- Geografia -------------------------------- */
        e("stolice", "Which of these cities are national capitals?",
            listOf("Ottawa", "Canberra", "Wellington", "Bern"),
            listOf("Sydney", "Toronto", "Zurich"),
            QuizCategory.Geography, QuizDifficulty.Medium),
        e("srodladowe", "Which of these countries are landlocked?",
            listOf("Bolivia", "Mongolia", "Austria", "Nepal"),
            listOf("Vietnam", "Portugal", "Denmark"),
            QuizCategory.Geography, QuizDifficulty.Medium),
        e("pustynie", "Which of these are deserts?",
            listOf("The Sahara", "The Gobi", "The Atacama", "The Kalahari"),
            listOf("The Serengeti", "The Pampas", "The Amazon"),
            QuizCategory.Geography, QuizDifficulty.Easy),
        e("wielkie_jeziora", "Which of these are Great Lakes of North America?",
            listOf("Superior", "Michigan", "Huron", "Erie"),
            listOf("Winnipeg", "Great Bear", "Great Slave"),
            QuizCategory.Geography, QuizDifficulty.Hard),
        e("morza_srodziemne", "Which of these are parts of the Mediterranean Sea?",
            listOf("The Adriatic Sea", "The Aegean Sea", "The Ionian Sea", "The Tyrrhenian Sea"),
            listOf("The Baltic Sea", "The Caspian Sea", "The Barents Sea"),
            QuizCategory.Geography, QuizDifficulty.Hard),
        e("ameryka_srodkowa", "Which of these countries are in Central America?",
            listOf("Guatemala", "Honduras", "Costa Rica", "Panama"),
            listOf("Colombia", "Venezuela", "Ecuador"),
            QuizCategory.Geography, QuizDifficulty.Medium),
        e("stany_usa", "Which of these are US states?",
            listOf("Oregon", "Vermont", "Nebraska", "Idaho"),
            listOf("Ontario", "Alberta", "Manitoba"),
            QuizCategory.Geography, QuizDifficulty.Medium),
        e("strefa_euro", "Which of these countries use the euro?",
            listOf("Germany", "Finland", "Ireland", "Slovakia"),
            listOf("Sweden", "Poland", "Denmark"),
            QuizCategory.Geography, QuizDifficulty.Medium),
        e("nato_czlonkowie", "Which of these countries are NATO members?",
            listOf("Norway", "Turkiye", "Canada", "Poland"),
            listOf("Austria", "Ireland", "Switzerland"),
            QuizCategory.Geography, QuizDifficulty.Hard),

        /* ------------------------- Nauka, pierwiastki, kosmos ---------------------- */
        e("gazy_szlachetne", "Which of these are noble gases?",
            listOf("Helium", "Neon", "Argon", "Krypton"),
            listOf("Nitrogen", "Oxygen", "Hydrogen"),
            QuizCategory.Elements, QuizDifficulty.Medium),
        e("pierwiastki_czy_stopy", "Which of these are chemical elements?",
            listOf("Tungsten", "Cobalt", "Iodine", "Bismuth"),
            listOf("Bronze", "Brass", "Steel"),
            QuizCategory.Elements, QuizDifficulty.Medium),
        e("gazy_pokojowa", "Which of these are gases at room temperature?",
            listOf("Nitrogen", "Helium", "Methane", "Chlorine"),
            listOf("Bromine", "Mercury", "Iodine"),
            QuizCategory.Elements, QuizDifficulty.Hard),
        e("planety", "Which of these are planets of the Solar System?",
            listOf("Neptune", "Venus", "Mars", "Jupiter"),
            listOf("Pluto", "Titan", "Ceres"),
            QuizCategory.Science, QuizDifficulty.Easy),
        e("ksiezyce", "Which of these are moons?",
            listOf("Titan", "Europa", "Ganymede", "Io"),
            listOf("Ceres", "Vesta", "Eris"),
            QuizCategory.Science, QuizDifficulty.Hard),
        e("nobel_kategorie", "Which of these are Nobel Prize categories?",
            listOf("Physics", "Chemistry", "Peace", "Literature"),
            listOf("Mathematics", "Philosophy", "Music"),
            QuizCategory.Science, QuizDifficulty.Medium),
        e("chmury", "Which of these are types of cloud?",
            listOf("Cumulus", "Cirrus", "Stratus", "Nimbostratus"),
            listOf("Corona", "Aurora", "Zephyr"),
            QuizCategory.Science, QuizDifficulty.Medium),

        /* ---------------------------- Biologia i zwierzeta ------------------------- */
        e("ssaki", "Which of these animals are mammals?",
            listOf("A blue whale", "A bat", "A dolphin", "A platypus"),
            listOf("A shark", "A penguin", "A crocodile"),
            QuizCategory.Animals, QuizDifficulty.Easy),
        e("nieloty", "Which of these birds cannot fly?",
            listOf("The ostrich", "The penguin", "The kiwi", "The emu"),
            listOf("The albatross", "The falcon", "The swan"),
            QuizCategory.Animals, QuizDifficulty.Easy),
        e("dinozaury", "Which of these were dinosaurs?",
            listOf("Triceratops", "Stegosaurus", "Velociraptor", "Brachiosaurus"),
            listOf("Pterodactyl", "Mosasaurus", "Dimetrodon"),
            QuizCategory.Animals, QuizDifficulty.Hard),
        e("uklad_pokarmowy", "Which of these are organs of the digestive system?",
            listOf("The stomach", "The liver", "The pancreas", "The small intestine"),
            listOf("The kidney", "The spleen", "The lung"),
            QuizCategory.Biology, QuizDifficulty.Medium),
        e("kosci_ciala", "Which of these are bones of the human body?",
            listOf("The femur", "The tibia", "The humerus", "The scapula"),
            listOf("The aorta", "The trachea", "The larynx"),
            QuizCategory.Biology, QuizDifficulty.Medium),

        /* ------------------------ Kultura, jezyk, technologia --------------------- */
        e("szekspir", "Which of these plays did Shakespeare write?",
            listOf("Macbeth", "Othello", "The Tempest", "King Lear"),
            listOf("Doctor Faustus", "Volpone", "The Duchess of Malfi"),
            QuizCategory.Literature, QuizDifficulty.Medium),
        e("nobel_literatura", "Which of these writers won the Nobel Prize in Literature?",
            listOf("Ernest Hemingway", "Gabriel Garcia Marquez", "Toni Morrison", "Kazuo Ishiguro"),
            listOf("Leo Tolstoy", "James Joyce", "Jorge Luis Borges"),
            QuizCategory.Literature, QuizDifficulty.Expert),
        e("jezyki_romanskie", "Which of these are Romance languages?",
            listOf("Spanish", "Romanian", "Portuguese", "Italian"),
            listOf("Greek", "Albanian", "Hungarian"),
            QuizCategory.Literature, QuizDifficulty.Hard),
        e("bogowie_greccy", "Which of these are Greek gods?",
            listOf("Zeus", "Hera", "Apollo", "Artemis"),
            listOf("Jupiter", "Odin", "Ra"),
            QuizCategory.Mythology, QuizDifficulty.Easy),
        e("cuda_swiata", "Which of these were among the Seven Wonders of the Ancient World?",
            listOf("The Great Pyramid of Giza", "The Hanging Gardens of Babylon", "The Colossus of Rhodes", "The Lighthouse of Alexandria"),
            listOf("The Colosseum", "The Great Wall of China", "Petra"),
            QuizCategory.Mythology, QuizDifficulty.Hard),
        e("jezyki_programowania", "Which of these are programming languages?",
            listOf("Python", "Rust", "Kotlin", "Haskell"),
            listOf("Docker", "Kubernetes", "Linux"),
            QuizCategory.Technology, QuizDifficulty.Medium),
        e("instrumenty_dete_drewniane", "Which of these are woodwind instruments?",
            listOf("The clarinet", "The oboe", "The bassoon", "The flute"),
            listOf("The trumpet", "The trombone", "The French horn"),
            QuizCategory.Technology, QuizDifficulty.Hard),

        /* --------------------------------- Sport ---------------------------------- */
        e("igrzyska_letnie", "Which of these are Summer Olympic sports?",
            listOf("Archery", "Judo", "Fencing", "Handball"),
            listOf("Curling", "Biathlon", "Luge"),
            QuizCategory.Sport, QuizDifficulty.Medium),
        e("mistrzowie_swiata", "Which of these countries have won the FIFA World Cup?",
            listOf("Uruguay", "Argentina", "Spain", "England"),
            listOf("The Netherlands", "Hungary", "Portugal"),
            QuizCategory.Sport, QuizDifficulty.Hard),
        e("wielki_szlem_turnieje", "Which of these are tennis Grand Slam tournaments?",
            listOf("Wimbledon", "The US Open", "Roland Garros", "The Australian Open"),
            listOf("The Madrid Open", "Indian Wells", "The Monte-Carlo Masters"),
            QuizCategory.Sport, QuizDifficulty.Medium),

        /* ---------------------------------- Math ---------------------------------- */
        e("liczby_pierwsze", "Which of these are prime numbers?",
            listOf("2", "13", "29", "41"),
            listOf("21", "51", "91"),
            QuizCategory.Math, QuizDifficulty.Hard),
        e("liczby_kwadratowe", "Which of these are perfect squares?",
            listOf("49", "81", "144", "225"),
            listOf("50", "128", "200"),
            QuizCategory.Math, QuizDifficulty.Medium)
    )
}
