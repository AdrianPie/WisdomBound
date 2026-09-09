package com.lernhero.domain.quiz.bank

import com.lernhero.domain.quiz.OrderingEntry
import com.lernhero.domain.quiz.QuizCategory
import com.lernhero.domain.quiz.QuizDifficulty

/**
 * "Uloz w kolejnosci" - partia po audycie, ktory wyrzucil z banku "poranek, poludnie, wieczor,
 * noc" i "poniedzialek, sroda, piatek, niedziela".
 *
 * Dobre pytanie tego typu ma jedna ceche: **kolejnosc nie jest oczywista, ale da sie do niej
 * DOJSC**. Gracz wie, ze Rzym jest starszy od Bizancjum i ze Bizancjum jest starsze od
 * Osmanow - i z tych dwoch faktow sklada odpowiedz. Uporzadkowanie liczb 15, 50, 150, 500 nie
 * wymaga zadnej wiedzy, wiec nie jest pytaniem, tylko testem, czy gracz umie czytac.
 *
 * Kazda lista jest zapisana w POPRAWNEJ kolejnosci - tasuje UI.
 */
object OrderingQuestionsTrivia {

    private fun order(
        id: String,
        prompt: String,
        items: List<String>,
        category: QuizCategory,
        difficulty: QuizDifficulty
    ) = OrderingEntry("ordt_$id", prompt, items, category, difficulty)

    val all: List<OrderingEntry> = listOf(

        /* ----------------------------------- Easy ----------------------------------- */
        order("planety_slonce_4", "Order these planets by distance from the Sun, nearest first.", listOf("Venus", "Earth", "Jupiter", "Neptune"), QuizCategory.Science, QuizDifficulty.Easy),
        order("wynalazki_xx", "Order these inventions from oldest to newest.", listOf("The telephone", "The airplane", "Television", "The smartphone"), QuizCategory.Technology, QuizDifficulty.Easy),
        order("imperia_stare", "Order these from oldest to most recent.", listOf("Ancient Egypt", "The Roman Empire", "The Viking Age", "The British Empire"), QuizCategory.History, QuizDifficulty.Easy),
        order("zwierzeta_szybkosc_prawdziwe", "Order these animals by top running speed, slowest first.", listOf("An elephant", "A grizzly bear", "A racehorse", "A cheetah"), QuizCategory.Animals, QuizDifficulty.Easy),
        order("kontynenty_ludnosc", "Order these continents by population, smallest first.", listOf("Australia and Oceania", "Europe", "Africa", "Asia"), QuizCategory.Geography, QuizDifficulty.Easy),
        order("ksiazki_bohater", "Order these Harry Potter books by release, first to last.", listOf("The Philosopher's Stone", "The Prisoner of Azkaban", "The Order of the Phoenix", "The Deathly Hallows"), QuizCategory.Literature, QuizDifficulty.Easy),
        order("igrzyska_medale", "Order these countries by total Summer Olympic medals, fewest first.", listOf("Brazil", "Japan", "Germany", "The United States"), QuizCategory.Sport, QuizDifficulty.Easy),
        order("liczby_kwadrat", "Order these from smallest to largest.", listOf("2 to the power of 4", "5 squared", "3 cubed", "The square root of 1000"), QuizCategory.Math, QuizDifficulty.Easy),
        order("dinozaury_ssaki", "Order these from earliest to appear on Earth.", listOf("Bacteria", "Fish", "Dinosaurs", "Humans"), QuizCategory.Biology, QuizDifficulty.Easy),
        order("epoki_muzyka", "Order these composers from earliest to latest born.", listOf("Bach", "Mozart", "Chopin", "Tchaikovsky"), QuizCategory.History, QuizDifficulty.Easy),
        order("planety_ksiezyce", "Order these planets by number of known moons, fewest first.", listOf("Earth", "Mars", "Uranus", "Saturn"), QuizCategory.Science, QuizDifficulty.Easy),
        order("bogowie_pokolenia", "Order these Greek figures from oldest generation to youngest.", listOf("Uranus", "Cronus", "Zeus", "Athena"), QuizCategory.Mythology, QuizDifficulty.Easy),

        /* ---------------------------------- Medium ---------------------------------- */
        order("miasta_ludnosc_swiat", "Order these cities by population, smallest first.", listOf("Rome", "Madrid", "London", "Shanghai"), QuizCategory.Geography, QuizDifficulty.Medium),
        order("kraje_powierzchnia_2", "Order these countries by area, smallest first.", listOf("Japan", "Mexico", "India", "Australia"), QuizCategory.Geography, QuizDifficulty.Medium),
        order("odkrycia_geo", "Order these voyages chronologically, earliest first.", listOf("Columbus reaches the Americas", "Magellan's expedition sails around the world", "Cook reaches Australia's east coast", "Amundsen reaches the South Pole"), QuizCategory.History, QuizDifficulty.Medium),
        order("wojny_usa", "Order these American conflicts chronologically, earliest first.", listOf("The War of Independence", "The Civil War", "The Vietnam War", "The Gulf War"), QuizCategory.History, QuizDifficulty.Medium),
        order("pierwiastki_topnienie", "Order these by melting point, lowest first.", listOf("Ice", "Butter", "Lead", "Iron"), QuizCategory.Science, QuizDifficulty.Medium),
        order("zwierzeta_ciaza", "Order these animals by length of pregnancy, shortest first.", listOf("A mouse", "A dog", "A human", "An elephant"), QuizCategory.Animals, QuizDifficulty.Medium),
        order("zwierzeta_serce", "Order these animals by resting heart rate, slowest first.", listOf("A blue whale", "A human", "A cat", "A hummingbird"), QuizCategory.Animals, QuizDifficulty.Medium),
        order("organizmy_komorki", "Order these by size, smallest first.", listOf("A virus", "A bacterium", "A human red blood cell", "A human egg cell"), QuizCategory.Biology, QuizDifficulty.Medium),
        order("ksiazki_klasyka", "Order these novels by publication, oldest first.", listOf("Robinson Crusoe", "Pride and Prejudice", "Moby-Dick", "The Great Gatsby"), QuizCategory.Literature, QuizDifficulty.Medium),
        order("szekspir_dzieje", "Order these Shakespeare plays by when they were written, earliest first.", listOf("Romeo and Juliet", "Hamlet", "Macbeth", "The Tempest"), QuizCategory.Literature, QuizDifficulty.Medium),
        order("konsole", "Order these game consoles by release, oldest first.", listOf("The Atari 2600", "The Nintendo Entertainment System", "The PlayStation", "The Xbox"), QuizCategory.Technology, QuizDifficulty.Medium),
        order("firmy_tech", "Order these companies by founding year, earliest first.", listOf("Microsoft", "Amazon", "Google", "Facebook"), QuizCategory.Technology, QuizDifficulty.Medium),
        order("sport_boiska", "Order these playing areas by size, smallest first.", listOf("A tennis court", "A basketball court", "A football pitch", "A polo field"), QuizCategory.Sport, QuizDifficulty.Medium),
        order("sport_pilki_masa", "Order these balls by weight, lightest first.", listOf("A table tennis ball", "A golf ball", "A football", "A bowling ball"), QuizCategory.Sport, QuizDifficulty.Medium),
        order("ulamki_mieszane", "Order these from smallest to largest.", listOf("0.3", "1/3", "35%", "3/8"), QuizCategory.Math, QuizDifficulty.Medium),
        order("potegi_dwojki", "Order these from smallest to largest.", listOf("10 squared", "2 to the power of 7", "3 to the power of 5", "5 cubed times 2"), QuizCategory.Math, QuizDifficulty.Medium),
        order("heraklesa_prace", "Order these labours of Heracles as traditionally listed, first to last.", listOf("The Nemean lion", "The Lernaean Hydra", "The Augean stables", "Cerberus"), QuizCategory.Mythology, QuizDifficulty.Medium),
        order("pierwiastki_odkrycie", "Order these elements by discovery, earliest first.", listOf("Gold", "Phosphorus", "Oxygen", "Helium"), QuizCategory.Elements, QuizDifficulty.Medium),
        order("metale_gestosc", "Order these metals by density, lightest first.", listOf("Aluminium", "Iron", "Lead", "Gold"), QuizCategory.Elements, QuizDifficulty.Medium),
        order("stolice_wschod", "Order these capitals from west to east.", listOf("Lisbon", "Paris", "Warsaw", "Moscow"), QuizCategory.Capitals, QuizDifficulty.Medium),
        order("stolice_wysokosc", "Order these capitals by elevation, lowest first.", listOf("Amsterdam", "Madrid", "Mexico City", "La Paz"), QuizCategory.Capitals, QuizDifficulty.Medium),

        /* ----------------------------------- Hard ----------------------------------- */
        order("rzeki_swiat_2", "Order these rivers by length, shortest first.", listOf("The Thames", "The Rhine", "The Mississippi", "The Amazon"), QuizCategory.Geography, QuizDifficulty.Hard),
        order("kraje_ludnosc_3", "Order these countries by population, smallest first.", listOf("Canada", "The United Kingdom", "Japan", "Indonesia"), QuizCategory.Geography, QuizDifficulty.Hard),
        order("wyspy_wielkosc", "Order these islands by area, smallest first.", listOf("Iceland", "Great Britain", "Madagascar", "Greenland"), QuizCategory.Geography, QuizDifficulty.Hard),
        order("imperia_upadek", "Order these empires by when they ended, earliest first.", listOf("The Western Roman Empire", "The Byzantine Empire", "The Mughal Empire", "The Ottoman Empire"), QuizCategory.History, QuizDifficulty.Hard),
        order("rewolucje", "Order these revolutions chronologically, earliest first.", listOf("The American Revolution", "The French Revolution", "The Russian Revolution", "The Chinese Communist Revolution"), QuizCategory.History, QuizDifficulty.Hard),
        order("naukowcy_ur", "Order these scientists by birth, earliest first.", listOf("Galileo", "Newton", "Darwin", "Einstein"), QuizCategory.Science, QuizDifficulty.Hard),
        order("temperatura_kosmos", "Order these by temperature, coldest first.", listOf("Liquid nitrogen", "Antarctica's record low", "Boiling water", "The Sun's surface"), QuizCategory.Science, QuizDifficulty.Hard),
        order("zwierzeta_mozg", "Order these animals by brain size, smallest first.", listOf("A cat", "A chimpanzee", "A human", "A sperm whale"), QuizCategory.Animals, QuizDifficulty.Hard),
        order("zwierzeta_zycie_2", "Order these animals by maximum recorded lifespan, shortest first.", listOf("A rabbit", "A horse", "An African elephant", "A bowhead whale"), QuizCategory.Animals, QuizDifficulty.Hard),
        order("kosci_dlugosc", "Order these human bones by length, shortest first.", listOf("The stapes", "The collarbone", "The humerus", "The femur"), QuizCategory.Biology, QuizDifficulty.Hard),
        order("organy_masa", "Order these human organs by weight, lightest first.", listOf("The heart", "The brain", "The liver", "The skin"), QuizCategory.Biology, QuizDifficulty.Hard),
        order("ksiazki_dlugosc", "Order these novels by length, shortest first.", listOf("The Old Man and the Sea", "The Great Gatsby", "Moby-Dick", "War and Peace"), QuizCategory.Literature, QuizDifficulty.Hard),
        order("pisarze_ur", "Order these writers by birth, earliest first.", listOf("Shakespeare", "Jane Austen", "Mark Twain", "Ernest Hemingway"), QuizCategory.Literature, QuizDifficulty.Hard),
        order("jezyki_prog", "Order these programming languages by when they appeared, earliest first.", listOf("Fortran", "C", "Java", "Swift"), QuizCategory.Technology, QuizDifficulty.Hard),
        order("nosniki_pojemnosc", "Order these by storage capacity, smallest first.", listOf("A floppy disk", "A CD", "A DVD", "A Blu-ray disc"), QuizCategory.Technology, QuizDifficulty.Hard),
        order("mundial_gospodarze", "Order these World Cup host countries chronologically, earliest first.", listOf("Uruguay", "England", "Mexico", "Qatar"), QuizCategory.Sport, QuizDifficulty.Hard),
        order("sport_rekordy", "Order these Olympic distances by world-record time, shortest first.", listOf("100 m sprint", "400 m", "1,500 m", "10,000 m"), QuizCategory.Sport, QuizDifficulty.Hard),
        order("liczby_pierwsze_kolejne", "Order these numbers by how many divisors they have, fewest first.", listOf("13", "9", "12", "24"), QuizCategory.Math, QuizDifficulty.Hard),
        order("liczby_specjalne", "Order these from smallest to largest.", listOf("The square root of 2", "The golden ratio", "e", "Pi"), QuizCategory.Math, QuizDifficulty.Hard),
        order("mitologia_wojna_trojanska", "Order these events of the Trojan War story, first to last.", listOf("The judgement of Paris", "The abduction of Helen", "The death of Hector", "The wooden horse"), QuizCategory.Mythology, QuizDifficulty.Hard),
        order("odyseusz_droga", "Order these stops on Odysseus's journey home, first to last.", listOf("The Cyclops Polyphemus", "Circe's island", "The Sirens", "Calypso's island"), QuizCategory.Mythology, QuizDifficulty.Hard),
        order("pierwiastki_liczba_2", "Order these elements by atomic number, lowest first.", listOf("Sodium", "Calcium", "Silver", "Uranium"), QuizCategory.Elements, QuizDifficulty.Hard),
        order("gazy_szlachetne_masa", "Order these noble gases from lightest to heaviest.", listOf("Helium", "Neon", "Argon", "Xenon"), QuizCategory.Elements, QuizDifficulty.Hard),
        order("stolice_ludnosc_2", "Order these capitals by population, smallest first.", listOf("Canberra", "Vienna", "Cairo", "Tokyo"), QuizCategory.Capitals, QuizDifficulty.Hard),

        /* ---------------------------------- Expert ---------------------------------- */
        order("cesarstwa_powierzchnia", "Order these empires by their greatest land area, smallest first.", listOf("The Roman Empire", "The Spanish Empire", "The Mongol Empire", "The British Empire"), QuizCategory.History, QuizDifficulty.Expert),
        order("pierwiastki_liczba_3", "Order these elements by atomic number, lowest first.", listOf("Titanium", "Zinc", "Tin", "Platinum"), QuizCategory.Elements, QuizDifficulty.Expert),
        order("planety_gestosc", "Order these planets by density, least dense first.", listOf("Saturn", "Jupiter", "Mars", "Earth"), QuizCategory.Science, QuizDifficulty.Expert),
        order("jeziora_glebokosc", "Order these lakes by maximum depth, shallowest first.", listOf("Lake Victoria", "Lake Superior", "Lake Tanganyika", "Lake Baikal"), QuizCategory.Geography, QuizDifficulty.Expert),
        order("nobel_lit_lata", "Order these Nobel laureates in Literature by the year of their award, earliest first.", listOf("Rudyard Kipling", "Ernest Hemingway", "Gabriel Garcia Marquez", "Kazuo Ishiguro"), QuizCategory.Literature, QuizDifficulty.Expert),
        order("zwierzeta_nurkowanie", "Order these animals by how deep they can dive, shallowest first.", listOf("An emperor penguin", "A leatherback turtle", "An elephant seal", "A Cuvier's beaked whale"), QuizCategory.Animals, QuizDifficulty.Expert),
    )
}
