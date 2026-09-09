package com.lernhero.domain.quiz.bank

import com.lernhero.domain.quiz.MultiSelectEntry
import com.lernhero.domain.quiz.QuizCategory
import com.lernhero.domain.quiz.QuizDifficulty

/**
 * "Zaznacz wszystkie prawdziwe" - partia po audycie, ktory wyrzucil pytania z dystraktorami
 * w rodzaju "uklady ciala: krwionosny, nerwowy, CHLODZACY, NAPEDOWY".
 *
 * Zasada jest ta sama, co w [MultiSelectQuestions], tylko egzekwowana bez wyjatkow: **kazda
 * bledna opcja musi byc czyms, co gracz moglby naprawde zaznaczyc**. "Ktore z tych to
 * ssaki?" z opcja "rekin" sprawdza wiedze, bo rekin wyglada jak kandydat; z opcja "rower"
 * sprawdza tylko czytanie. Najlepsze pytania tego typu maja dystraktory, ktore sa
 * POPULARNYMI BLEDAMI: Pluton wsrod planet, pomidor wsrod warzyw, pajak wsrod owadow.
 */
object MultiSelectQuestionsTrivia {

    private fun e(
        id: String,
        prompt: String,
        correct: List<String>,
        wrong: List<String>,
        category: QuizCategory,
        difficulty: QuizDifficulty
    ) = MultiSelectEntry("mst_$id", prompt, correct, wrong, category, difficulty)

    val all: List<MultiSelectEntry> = listOf(

        /* ---------------------------------- Easy ---------------------------------- */
        e("owoce_botanicznie", "Which of these are botanically fruits?", listOf("Tomato", "Cucumber", "Avocado", "Pumpkin"), listOf("Carrot", "Potato", "Onion"), QuizCategory.Biology, QuizDifficulty.Easy),
        e("ssaki_morskie", "Which of these sea animals are mammals?", listOf("The dolphin", "The seal", "The manatee", "The orca"), listOf("The great white shark", "The tuna", "The octopus"), QuizCategory.Animals, QuizDifficulty.Easy),
        e("europa_stolice_rzeki", "Which of these capitals lie on the Danube?", listOf("Vienna", "Budapest", "Belgrade", "Bratislava"), listOf("Prague", "Warsaw", "Berlin"), QuizCategory.Capitals, QuizDifficulty.Easy),
        e("planety_skaliste", "Which of these are rocky planets?", listOf("Mercury", "Venus", "Earth", "Mars"), listOf("Jupiter", "Saturn", "Neptune"), QuizCategory.Science, QuizDifficulty.Easy),
        e("ww2_osi", "Which countries were Axis powers in World War II?", listOf("Germany", "Italy", "Japan"), listOf("The Soviet Union", "The United States", "China", "Poland"), QuizCategory.History, QuizDifficulty.Easy),
        e("sporty_rakieta", "Which of these sports are played with a racket?", listOf("Tennis", "Badminton", "Squash", "Table tennis"), listOf("Baseball", "Cricket", "Golf"), QuizCategory.Sport, QuizDifficulty.Easy),
        e("liczby_pierwsze_male", "Which of these numbers are prime?", listOf("7", "11", "17", "23"), listOf("9", "15", "21"), QuizCategory.Math, QuizDifficulty.Easy),
        e("tolkien_postacie", "Which of these characters appear in 'The Lord of the Rings'?", listOf("Gandalf", "Aragorn", "Gollum", "Legolas"), listOf("Dumbledore", "Aslan", "Drizzt"), QuizCategory.Literature, QuizDifficulty.Easy),
        e("bogowie_rzymscy", "Which of these are Roman gods?", listOf("Jupiter", "Mars", "Venus", "Neptune"), listOf("Zeus", "Ares", "Thor"), QuizCategory.Mythology, QuizDifficulty.Easy),
        e("metale_szlachetne_2", "Which of these metals do not rust or tarnish easily?", listOf("Gold", "Platinum"), listOf("Iron", "Copper", "Silver", "Zinc"), QuizCategory.Elements, QuizDifficulty.Easy),
        e("firmy_amerykanskie", "Which of these tech companies were founded in the United States?", listOf("Apple", "Microsoft", "Google", "Amazon"), listOf("Samsung", "Nokia", "Sony"), QuizCategory.Technology, QuizDifficulty.Easy),
        e("afryka_kraje_2", "Which of these countries are in Africa?", listOf("Egypt", "Nigeria", "Kenya", "Morocco"), listOf("Turkey", "Yemen", "Sri Lanka"), QuizCategory.Geography, QuizDifficulty.Easy),

        /* --------------------------------- Medium --------------------------------- */
        e("owady_nie", "Which of these creatures are insects?", listOf("The ant", "The butterfly", "The mosquito", "The ladybird"), listOf("The spider", "The tick", "The scorpion"), QuizCategory.Animals, QuizDifficulty.Medium),
        e("ptaki_migrujace", "Which of these animals hibernate through winter?", listOf("The hedgehog", "The bat", "The dormouse", "The ground squirrel"), listOf("The fox", "The deer", "The wolf"), QuizCategory.Animals, QuizDifficulty.Medium),
        e("witaminy_tluszcz", "Which of these vitamins are fat-soluble?", listOf("Vitamin A", "Vitamin D", "Vitamin E", "Vitamin K"), listOf("Vitamin C", "Vitamin B12", "Vitamin B6"), QuizCategory.Biology, QuizDifficulty.Medium),
        e("cialo_narzady_parzyste", "Which of these organs do humans have two of?", listOf("Lungs", "Kidneys", "Ovaries or testes"), listOf("Liver", "Heart", "Pancreas", "Spleen"), QuizCategory.Biology, QuizDifficulty.Medium),
        e("kraje_ue_nie", "Which of these European countries are NOT in the European Union?", listOf("Norway", "Switzerland", "Iceland", "Serbia"), listOf("Croatia", "Ireland", "Portugal"), QuizCategory.Geography, QuizDifficulty.Medium),
        e("rownik_kraje", "Which of these countries does the equator pass through?", listOf("Brazil", "Kenya", "Indonesia", "Ecuador"), listOf("Mexico", "India", "Egypt"), QuizCategory.Geography, QuizDifficulty.Medium),
        e("stolice_nie_najwieksze", "In which of these countries is the capital NOT the largest city?", listOf("Australia", "Canada", "Turkey", "The United States"), listOf("France", "Japan", "Egypt"), QuizCategory.Capitals, QuizDifficulty.Medium),
        e("wladcy_francja", "Which of these people ruled France?", listOf("Louis XIV", "Napoleon Bonaparte", "Charles de Gaulle"), listOf("Otto von Bismarck", "Oliver Cromwell", "Philip II of Spain", "Maria Theresa"), QuizCategory.History, QuizDifficulty.Medium),
        e("xx_wiek_wydarzenia", "Which of these happened in the 20th century?", listOf("The sinking of the Titanic", "The first Moon landing", "The fall of the Berlin Wall", "The Chernobyl disaster"), listOf("The French Revolution", "The American Civil War", "The invention of the telephone"), QuizCategory.History, QuizDifficulty.Medium),
        e("planety_pierscienie", "Which of these planets have rings?", listOf("Jupiter", "Saturn", "Uranus", "Neptune"), listOf("Mars", "Venus", "Earth"), QuizCategory.Science, QuizDifficulty.Medium),
        e("energia_odnawialna_nie", "Which of these are fossil fuels?", listOf("Coal", "Crude oil", "Natural gas", "Lignite"), listOf("Uranium", "Hydrogen", "Biomass"), QuizCategory.Science, QuizDifficulty.Medium),
        e("magnetyczne", "Which of these metals are attracted to a magnet?", listOf("Iron", "Nickel", "Cobalt"), listOf("Aluminium", "Copper", "Gold", "Silver"), QuizCategory.Elements, QuizDifficulty.Medium),
        e("pierwiastki_ciecze_gazy", "Which of these elements are gases at room temperature?", listOf("Oxygen", "Chlorine", "Fluorine", "Argon"), listOf("Bromine", "Mercury", "Iodine"), QuizCategory.Elements, QuizDifficulty.Medium),
        e("olimpiady_letnie_gosp", "Which of these cities have hosted the Summer Olympics?", listOf("Tokyo", "Sydney", "Beijing", "Rio de Janeiro"), listOf("New Delhi", "Cape Town", "Toronto"), QuizCategory.Sport, QuizDifficulty.Medium),
        e("sporty_druzynowe_11", "In which of these sports does a team have exactly 11 players on the field?", listOf("Football (soccer)", "Cricket", "Field hockey", "American football"), listOf("Rugby union", "Basketball", "Volleyball"), QuizCategory.Sport, QuizDifficulty.Medium),
        e("dickens_dziela", "Which of these novels did Charles Dickens write?", listOf("Oliver Twist", "Great Expectations", "A Christmas Carol", "David Copperfield"), listOf("Jane Eyre", "Wuthering Heights", "Vanity Fair"), QuizCategory.Literature, QuizDifficulty.Medium),
        e("pseudonimy", "Which of these names are pen names rather than birth names?", listOf("Mark Twain", "George Orwell", "Lewis Carroll", "George Eliot"), listOf("Charles Dickens", "Jane Austen", "Ernest Hemingway"), QuizCategory.Literature, QuizDifficulty.Medium),
        e("liczby_podzielne_6", "Which of these numbers are divisible by 6?", listOf("18", "42", "96", "114"), listOf("26", "64", "93"), QuizCategory.Math, QuizDifficulty.Medium),
        e("figury_katy_proste", "Which of these shapes always have at least one right angle?", listOf("A square", "A rectangle", "A right triangle"), listOf("A rhombus", "A regular hexagon", "A parallelogram", "An equilateral triangle"), QuizCategory.Math, QuizDifficulty.Medium),
        e("olimp_dwunastu", "Which of these are among the twelve Olympian gods?", listOf("Hera", "Apollo", "Artemis", "Hephaestus"), listOf("Hades", "Persephone", "Prometheus"), QuizCategory.Mythology, QuizDifficulty.Medium),
        e("potwory_wiele_glow", "Which of these mythical creatures have more than one head?", listOf("Cerberus", "The Hydra", "The Chimera"), listOf("The Minotaur", "The Cyclops", "Pegasus", "The Sphinx"), QuizCategory.Mythology, QuizDifficulty.Medium),
        e("jezyki_prog_google", "Which of these were created or are maintained mainly by Google?", listOf("Android", "Chrome", "Go (the language)", "YouTube"), listOf("Linux", "Firefox", "WhatsApp"), QuizCategory.Technology, QuizDifficulty.Medium),
        e("formaty_obraz", "Which of these are image file formats?", listOf("JPEG", "PNG", "GIF", "SVG"), listOf("MP3", "DOCX", "ZIP"), QuizCategory.Technology, QuizDifficulty.Medium),

        /* ---------------------------------- Hard ---------------------------------- */
        e("zwierzeta_jadowite_2", "Which of these mammals are venomous?", listOf("The platypus", "The slow loris", "The solenodon"), listOf("The hedgehog", "The vampire bat", "The mongoose", "The honey badger"), QuizCategory.Animals, QuizDifficulty.Hard),
        e("ptaki_nie_ptaki", "Which of these animals are birds?", listOf("The penguin", "The ostrich", "The kiwi"), listOf("The bat", "The flying fox", "The pterodactyl", "The flying squirrel"), QuizCategory.Animals, QuizDifficulty.Hard),
        e("krew_grupy", "Which of these blood groups can safely receive O-negative blood?", listOf("A positive", "B negative", "AB positive", "O positive"), listOf("None - only O negative can", "Only AB", "Only O groups"), QuizCategory.Biology, QuizDifficulty.Hard),
        e("kraje_dwa_kontynenty", "Which of these countries have territory on two continents?", listOf("Russia", "Turkey", "Egypt", "Kazakhstan"), listOf("Iran", "Morocco", "Greece"), QuizCategory.Geography, QuizDifficulty.Hard),
        e("kraje_bez_armii", "Which of these countries have no standing army?", listOf("Costa Rica", "Iceland", "Liechtenstein"), listOf("Switzerland", "Ireland", "Austria", "Norway"), QuizCategory.Geography, QuizDifficulty.Hard),
        e("cesarze_rzymscy_2", "Which of these figures were Roman emperors?", listOf("Augustus", "Marcus Aurelius", "Diocletian", "Vespasian"), listOf("Cicero", "Brutus", "Scipio"), QuizCategory.History, QuizDifficulty.Hard),
        e("wynalazki_chinskie", "Which of these were invented in ancient or medieval China?", listOf("Paper", "Gunpowder", "The compass", "Printing"), listOf("The wheel", "Glass", "The steam engine"), QuizCategory.History, QuizDifficulty.Hard),
        e("czastki_elementarne", "Which of these are subatomic particles?", listOf("The electron", "The neutrino", "The quark", "The photon"), listOf("The atom", "The molecule", "The ion"), QuizCategory.Science, QuizDifficulty.Hard),
        e("skale_jednostki", "Which of these are units of energy?", listOf("The joule", "The calorie", "The kilowatt-hour", "The electronvolt"), listOf("The watt", "The newton", "The pascal"), QuizCategory.Science, QuizDifficulty.Hard),
        e("pierwiastki_curie", "Which of these elements are named after people?", listOf("Einsteinium", "Curium", "Nobelium", "Mendelevium"), listOf("Polonium", "Francium", "Germanium"), QuizCategory.Elements, QuizDifficulty.Hard),
        e("pierwiastki_ziemia", "Which of these elements are named after places?", listOf("Polonium", "Francium", "Germanium", "Californium"), listOf("Curium", "Einsteinium", "Fermium"), QuizCategory.Elements, QuizDifficulty.Hard),
        e("wielki_szlem_zwyciezcy", "Which of these players have won all four tennis Grand Slam tournaments?", listOf("Roger Federer", "Rafael Nadal", "Novak Djokovic", "Serena Williams"), listOf("Pete Sampras", "Bjorn Borg", "Boris Becker"), QuizCategory.Sport, QuizDifficulty.Hard),
        e("olimpijskie_sporty_dawne", "Which of these were once Olympic events?", listOf("Tug of war", "Art competitions", "Live pigeon shooting", "Solo synchronised swimming"), listOf("Chess", "Darts", "Bowling"), QuizCategory.Sport, QuizDifficulty.Hard),
        e("nobel_lit_pisarze", "Which of these writers won the Nobel Prize in Literature?", listOf("Ernest Hemingway", "William Faulkner", "Bob Dylan", "Toni Morrison"), listOf("James Joyce", "Virginia Woolf", "Franz Kafka"), QuizCategory.Literature, QuizDifficulty.Hard),
        e("liczby_doskonale", "Which of these are perfect numbers (equal to the sum of their proper divisors)?", listOf("6", "28"), listOf("12", "16", "24", "30", "36"), QuizCategory.Math, QuizDifficulty.Hard),
        e("bogowie_nordyccy_2", "Which of these belong to Norse mythology?", listOf("Yggdrasil", "Fenrir", "Valkyries", "Mjolnir"), listOf("Excalibur", "The Golden Fleece", "Cerberus"), QuizCategory.Mythology, QuizDifficulty.Hard),
        e("jezyki_prog_typowane", "Which of these languages are primarily used for web browsers' front-end?", listOf("JavaScript", "TypeScript"), listOf("Python", "C++", "Rust", "Kotlin", "Swift"), QuizCategory.Technology, QuizDifficulty.Hard),
    )
}
