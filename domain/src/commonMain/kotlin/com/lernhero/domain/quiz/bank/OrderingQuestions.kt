package com.lernhero.domain.quiz.bank

import com.lernhero.domain.quiz.OrderingEntry
import com.lernhero.domain.quiz.QuizCategory
import com.lernhero.domain.quiz.QuizDifficulty

/**
 * "Uloz w kolejnosci" - wszystkie tematy w jednym pliku. [OrderingEntry.items] jest ZAWSZE
 * zapisane w poprawnej kolejnosci; tasuje UI.
 *
 * Dobre pytanie tego typu ma jedna ceche: kolejnosc nie jest oczywista, ale da sie do niej DOJSC
 * z jednego pewnego punktu odniesienia. Uporzadkowanie "100 m, 800 m, 5000 m" nie wymaga zadnej
 * wiedzy, wiec nie jest pytaniem - takie wylecialy. Rankingi dobrane tak, zeby roznice miedzy
 * sasiednimi pozycjami byly wyrazne.
 *
 * Tresci znane tylko w Polsce siedza w [QuizCategory.Poland].
 */
object OrderingQuestions {

    private val E = QuizDifficulty.Easy
    private val M = QuizDifficulty.Medium
    private val H = QuizDifficulty.Hard
    private val X = QuizDifficulty.Expert

    private fun ord(id: String, prompt: String, items: List<String>, category: QuizCategory, difficulty: QuizDifficulty) =
        OrderingEntry(id = id, prompt = prompt, items = items, category = category, difficulty = difficulty)

    private val Bio = QuizCategory.Biology
    private val Ani = QuizCategory.Animals
    private val His = QuizCategory.History
    private val Ele = QuizCategory.Elements
    private val Cap = QuizCategory.Capitals
    private val Sci = QuizCategory.Science
    private val Geo = QuizCategory.Geography
    private val Spo = QuizCategory.Sport
    private val Mat = QuizCategory.Math
    private val Lit = QuizCategory.Literature
    private val Tec = QuizCategory.Technology
    private val Myt = QuizCategory.Mythology
    private val Pop = QuizCategory.PopCulture
    private val Pol = QuizCategory.Poland

    val all: List<OrderingEntry> = listOf(
        /* ---------------------------- Biology ----------------------------- */
        ord("ord2_etapy_motyla", "Order the stages of a butterfly's development.", listOf("Egg", "Caterpillar", "Chrysalis", "Butterfly"), Bio, E),
        ord("ordt_dinozaury_ssaki", "Order these from earliest to appear on Earth.", listOf("Bacteria", "Fish", "Dinosaurs", "Humans"), Bio, E),
        ord("ord2_uklad_pokarmowy", "Order these parts of the digestive system in the order food passes through them.", listOf("Esophagus", "Stomach", "Small intestine", "Large intestine"), Bio, M),
        ord("ordw_ewolucja_czlowieka", "Order these from oldest to most recent.", listOf("Australopithecus", "Homo habilis", "Homo erectus", "Homo sapiens"), Bio, M),
        ord("ordt_organizmy_komorki", "Order these by size, smallest first.", listOf("A virus", "A bacterium", "A human red blood cell", "A human egg cell"), Bio, M),
        ord("ord2_bio_hierarchia", "Order these levels of biological organization from smallest.", listOf("Cell", "Tissue", "Organ", "Organism"), Bio, H),
        ord("ordt_kosci_dlugosc", "Order these human bones by length, shortest first.", listOf("The stapes", "The collarbone", "The humerus", "The femur"), Bio, H),
        ord("ordt_organy_masa", "Order these human organs by weight, lightest first.", listOf("The heart", "The brain", "The liver", "The skin"), Bio, H),
        ord("ord2_ery_geologiczne", "Order these geological eras/periods from oldest.", listOf("Paleozoic", "Mesozoic", "Paleogene", "Neogene"), Bio, X),

        /* ---------------------------- Animals ----------------------------- */
        ord("ordt_zwierzeta_szybkosc_prawdziwe", "Order these animals by top running speed, slowest first.", listOf("An elephant", "A grizzly bear", "A racehorse", "A cheetah"), Ani, E),
        ord("ordw_zwierzeta_predkosc", "Order these by top speed, slowest first.", listOf("A human sprinter", "A greyhound", "A cheetah", "A peregrine falcon in a dive"), Ani, M),
        ord("ordt_zwierzeta_ciaza", "Order these animals by length of pregnancy, shortest first.", listOf("A mouse", "A dog", "A human", "An elephant"), Ani, M),
        ord("ordt_zwierzeta_serce", "Order these animals by resting heart rate, slowest first.", listOf("A blue whale", "A human", "A cat", "A hummingbird"), Ani, M),
        ord("ord_bio_animal_lifespan", "Order these animals from shortest to longest average lifespan.", listOf("Fruit fly", "Mouse", "Dog", "Giant tortoise"), Ani, H),
        ord("ordt_zwierzeta_mozg", "Order these animals by brain size, smallest first.", listOf("A cat", "A chimpanzee", "A human", "A sperm whale"), Ani, H),
        ord("ordt_zwierzeta_zycie_2", "Order these animals by maximum recorded lifespan, shortest first.", listOf("A rabbit", "A horse", "An African elephant", "A bowhead whale"), Ani, H),
        ord("ordt_zwierzeta_nurkowanie", "Order these animals by how deep they can dive, shallowest first.", listOf("An emperor penguin", "A leatherback turtle", "An elephant seal", "A Cuvier's beaked whale"), Ani, X),

        /* ---------------------------- History ----------------------------- */
        ord("ordw_wydarzenia_swiat", "Order these events from oldest to most recent.", listOf("The fall of Constantinople", "Columbus reaching America", "The French Revolution", "The Moon landing"), His, E),
        ord("ordw_xx_wiek", "Order these 20th-century events from earliest to latest.", listOf("The outbreak of World War I", "The outbreak of World War II", "The building of the Berlin Wall", "The fall of the Berlin Wall"), His, E),
        ord("ordt_imperia_stare", "Order these from oldest to most recent.", listOf("Ancient Egypt", "The Roman Empire", "The Viking Age", "The British Empire"), His, E),
        ord("ordt_epoki_muzyka", "Order these composers from earliest to latest born.", listOf("Bach", "Mozart", "Chopin", "Tchaikovsky"), His, E),
        ord("ord_history_wars", "Order these historical events from oldest to most recent.", listOf("The fall of the Roman Empire", "Columbus's discovery of America", "The French Revolution", "World War I"), His, M),
        ord("ord2_wojny_swiatowe", "Order these 20th-century events from oldest.", listOf("World War I", "World War II", "Gagarin's spaceflight", "The fall of the Berlin Wall"), His, M),
        ord("ordw_cywilizacje", "Order these from oldest to most recent.", listOf("The Sumerian city-states", "Classical Athens", "The Roman Empire", "The Byzantine Empire"), His, M),
        ord("ordw_dekolonizacja", "Order these by when they gained independence, earliest first.", listOf("The United States", "Brazil", "India", "Namibia"), His, M),
        ord("ordt_odkrycia_geo", "Order these voyages chronologically, earliest first.", listOf("Columbus reaches the Americas", "Magellan's expedition sails around the world", "Cook reaches Australia's east coast", "Amundsen reaches the South Pole"), His, M),
        ord("ordt_wojny_usa", "Order these American conflicts chronologically, earliest first.", listOf("The War of Independence", "The Civil War", "The Vietnam War", "The Gulf War"), His, M),
        ord("ord_hx_budowle", "Order these structures from oldest to newest.", listOf("Stonehenge", "The Colosseum", "The Taj Mahal", "The Eiffel Tower"), His, M),
        ord("ord3_wojny_europa", "Order these European conflicts from oldest.", listOf("The Hundred Years' War", "The Thirty Years' War", "The Napoleonic Wars", "World War I"), His, H),
        ord("ordw_cesarze_rzymu", "Order these Roman emperors chronologically, earliest first.", listOf("Augustus", "Nero", "Trajan", "Constantine the Great"), His, H),
        ord("ordw_imperia_azji", "Order these empires chronologically, earliest first.", listOf("The Han dynasty", "The Mongol Empire", "The Mughal Empire", "The Meiji Empire of Japan"), His, H),
        ord("ordt_imperia_upadek", "Order these empires by when they ended, earliest first.", listOf("The Western Roman Empire", "The Byzantine Empire", "The Mughal Empire", "The Ottoman Empire"), His, H),
        ord("ordt_rewolucje", "Order these revolutions chronologically, earliest first.", listOf("The American Revolution", "The French Revolution", "The Russian Revolution", "The Chinese Communist Revolution"), His, H),
        ord("ord_hx_monarchowie", "Order these British monarchs by reign, earliest first.", listOf("Henry VIII", "Elizabeth I", "Queen Victoria", "Elizabeth II"), His, H),
        ord("ord_hx_lotnictwo", "Order these milestones of flight from earliest.", listOf("The first hot-air balloon flight", "The Wright brothers' first flight", "Lindbergh's solo Atlantic crossing", "The Moon landing"), His, H),
        ord("ord2_historia_starozytnosc", "Order these civilizations by when they began to flourish, from oldest.", listOf("Sumer", "Ancient Egypt", "Classical Greece", "The Roman Empire"), His, X),
        ord("ord2_krolowie_anglii", "Order these events from English history from oldest.", listOf("The Battle of Hastings", "The Magna Carta", "The Wars of the Roses", "The Industrial Revolution"), His, X),
        ord("ord3_cywilizacje_ameryka", "Order these pre-Columbian civilizations by their peak, from oldest.", listOf("The Olmecs", "The Maya", "The Aztecs", "The Incas"), His, X),
        ord("ord3_dynastie_chiny", "Order these Chinese dynasties from oldest.", listOf("Han", "Tang", "Song", "Ming"), His, X),
        ord("ordt_cesarstwa_powierzchnia", "Order these empires by their greatest land area, smallest first.", listOf("The Roman Empire", "The Spanish Empire", "The Mongol Empire", "The British Empire"), His, X),
        ord("ord_hx_starozytni", "Order these ancient figures by when they lived, earliest first.", listOf("Hammurabi", "Tutankhamun", "Alexander the Great", "Julius Caesar"), His, X),

        /* ---------------------------- Elements ---------------------------- */
        ord("ordw_liczba_atomowa", "Order these elements by atomic number, lowest first.", listOf("Hydrogen", "Carbon", "Iron", "Gold"), Ele, E),
        ord("ordt_pierwiastki_odkrycie", "Order these elements by discovery, earliest first.", listOf("Gold", "Phosphorus", "Oxygen", "Helium"), Ele, M),
        ord("ordt_metale_gestosc", "Order these metals by density, lightest first.", listOf("Aluminium", "Iron", "Lead", "Gold"), Ele, M),
        ord("ord2_pierwiastki_masa", "Order these elements from lightest to heaviest.", listOf("Hydrogen", "Carbon", "Iron", "Lead"), Ele, H),
        ord("ordw_metale_topnienie", "Order these metals by melting point, lowest first.", listOf("Mercury", "Tin", "Iron", "Tungsten"), Ele, H),
        ord("ordt_pierwiastki_liczba_2", "Order these elements by atomic number, lowest first.", listOf("Sodium", "Calcium", "Silver", "Uranium"), Ele, H),
        ord("ordt_gazy_szlachetne_masa", "Order these noble gases from lightest to heaviest.", listOf("Helium", "Neon", "Argon", "Xenon"), Ele, H),
        ord("ord2_pierwiastki_liczba_at", "Order these elements by increasing atomic number.", listOf("Helium", "Nitrogen", "Sulfur", "Iron"), Ele, X),
        ord("ordt_pierwiastki_liczba_3", "Order these elements by atomic number, lowest first.", listOf("Titanium", "Zinc", "Tin", "Platinum"), Ele, X),

        /* ---------------------------- Capitals ---------------------------- */
        ord("ordt_stolice_wschod", "Order these capitals from west to east.", listOf("Lisbon", "Paris", "Warsaw", "Moscow"), Cap, M),
        ord("ordt_stolice_wysokosc", "Order these capitals by elevation, lowest first.", listOf("Amsterdam", "Madrid", "Mexico City", "La Paz"), Cap, M),
        ord("ord2_stolice_szerokosc", "Order these capitals from southernmost to northernmost.", listOf("Cairo", "Rome", "Warsaw", "Oslo"), Cap, H),
        ord("ordt_stolice_ludnosc_2", "Order these capitals by population, smallest first.", listOf("Canberra", "Vienna", "Cairo", "Tokyo"), Cap, H),

        /* ---------------------------- Science ----------------------------- */
        ord("ordt_planety_slonce_4", "Order these planets by distance from the Sun, nearest first.", listOf("Venus", "Earth", "Jupiter", "Neptune"), Sci, E),
        ord("ordt_planety_ksiezyce", "Order these planets by number of known moons, fewest first.", listOf("Earth", "Mars", "Uranus", "Saturn"), Sci, E),
        ord("ord2_planety_wielkosc", "Order these planets from smallest to largest.", listOf("Mercury", "Mars", "Neptune", "Jupiter"), Sci, M),
        ord("ordt_pierwiastki_topnienie", "Order these by melting point, lowest first.", listOf("Ice", "Butter", "Lead", "Iron"), Sci, M),
        ord("ord2_gwiazdy_zycie", "Order the life stages of a Sun-like star.", listOf("Gas cloud", "Main-sequence star", "Red giant", "White dwarf"), Sci, H),
        ord("ord2_skala_ph", "Order these substances from most acidic to most alkaline.", listOf("Lemon juice", "Coffee", "Water", "Baking soda"), Sci, H),
        ord("ord3_gwiazdy_jasnosc", "Order these objects from dimmest to brightest as seen from Earth.", listOf("A dwarf star", "A full moon", "Venus", "The Sun"), Sci, H),
        ord("ordw_fale_dlugosc", "Order these kinds of radiation by wavelength, shortest first.", listOf("Gamma rays", "Ultraviolet", "Visible light", "Radio waves"), Sci, H),
        ord("ordw_ery_geologiczne", "Order these geological periods from oldest to youngest.", listOf("The Cambrian", "The Jurassic", "The Cretaceous", "The Paleogene"), Sci, H),
        ord("ordt_naukowcy_ur", "Order these scientists by birth, earliest first.", listOf("Galileo", "Newton", "Darwin", "Einstein"), Sci, H),
        ord("ordt_temperatura_kosmos", "Order these by temperature, coldest first.", listOf("Liquid nitrogen", "Antarctica's record low", "Boiling water", "The Sun's surface"), Sci, H),
        ord("ord_science_hardness", "Order these materials from softest to hardest.", listOf("Talc", "Gypsum", "Quartz", "Diamond"), Sci, X),
        ord("ord2_mohs_pelna", "Order these minerals by the Mohs scale, from softest.", listOf("Gypsum", "Calcite", "Orthoclase", "Corundum"), Sci, X),
        ord("ord2_kosmos_odleglosci", "Order these objects by increasing distance from Earth.", listOf("The Moon", "The Sun", "Proxima Centauri", "The Andromeda Galaxy"), Sci, X),
        ord("ord3_czastki_wielkosc", "Order these objects from smallest to largest.", listOf("A quark", "An atom", "A virus", "A cell"), Sci, X),
        ord("ord3_nobel_fizyka_odkrycia", "Order these physics discoveries from oldest.", listOf("The electron", "X-rays", "The Higgs boson", "Gravitational waves"), Sci, X),
        ord("ordt_planety_gestosc", "Order these planets by density, least dense first.", listOf("Saturn", "Jupiter", "Mars", "Earth"), Sci, X),

        /* --------------------------- Geography ---------------------------- */
        ord("ord2_kontynenty_wielkosc", "Order these continents from smallest to largest.", listOf("Australia", "Europe", "Africa", "Asia"), Geo, E),
        ord("ordw_ludnosc_krajow", "Order these countries by population, from smallest to largest.", listOf("Iceland", "Norway", "Poland", "Germany"), Geo, E),
        ord("ordw_powierzchnia_krajow", "Order these countries by area, from smallest to largest.", listOf("Malta", "Belgium", "France", "Russia"), Geo, E),
        ord("ordw_miasta_polnoc", "Order these cities from north to south.", listOf("Oslo", "Berlin", "Rome", "Cairo"), Geo, E),
        ord("ordw_miasta_ludnosc", "Order these cities by population, from smallest to largest.", listOf("Reykjavik", "Amsterdam", "Berlin", "Tokyo"), Geo, E),
        ord("ordt_kontynenty_ludnosc", "Order these continents by population, smallest first.", listOf("Australia and Oceania", "Europe", "Africa", "Asia"), Geo, E),
        ord("ord_geo_ny_distance", "Starting with the closest, order these cities by distance from New York.", listOf("Boston", "Detroit", "Chicago", "Los Angeles"), Geo, M),
        ord("ord2_morza_glebokosc", "Order these bodies of water from shallowest to deepest.", listOf("The Baltic Sea", "The North Sea", "The Mediterranean Sea", "The Pacific Ocean"), Geo, M),
        ord("ord3_wielkosc_panstw_europa", "Order these European countries from smallest to largest.", listOf("Monaco", "Belgium", "Poland", "France"), Geo, M),
        ord("ordw_gory_wysokosc", "Order these mountains by height, from lowest to highest.", listOf("Mont Blanc", "Kilimanjaro", "Aconcagua", "Mount Everest"), Geo, M),
        ord("ordw_rzeki_dlugosc", "Order these rivers by length, from shortest to longest.", listOf("The Rhine", "The Danube", "The Volga", "The Nile"), Geo, M),
        ord("ordw_oceany_powierzchnia", "Order these oceans by area, from smallest to largest.", listOf("The Arctic Ocean", "The Indian Ocean", "The Atlantic", "The Pacific"), Geo, M),
        ord("ordt_miasta_ludnosc_swiat", "Order these cities by population, smallest first.", listOf("Rome", "Madrid", "London", "Shanghai"), Geo, M),
        ord("ordt_kraje_powierzchnia_2", "Order these countries by area, smallest first.", listOf("Japan", "Mexico", "India", "Australia"), Geo, M),
        ord("ord_geo_mountains", "Order these mountains from shortest to tallest.", listOf("Sniezka", "Mont Blanc", "Kilimanjaro", "Mount Everest"), Geo, H),
        ord("ord_geo_rivers", "Order these rivers from shortest to longest.", listOf("Vistula", "Danube", "Nile", "Amazon"), Geo, H),
        ord("ord2_panstwa_powierzchnia", "Order these countries from smallest to largest area.", listOf("Poland", "France", "India", "Canada"), Geo, H),
        ord("ord3_panstwa_ludnosc", "Order these countries from least to most populous.", listOf("Poland", "Germany", "Brazil", "India"), Geo, H),
        ord("ordw_morza_glebokosc", "Order these waters by their greatest depth, from shallowest to deepest.", listOf("The Baltic Sea", "The Mediterranean Sea", "The Caribbean Sea", "The Mariana Trench"), Geo, H),
        ord("ordw_male_panstwa", "Order these countries by area, from smallest to largest.", listOf("Vatican City", "Monaco", "Malta", "Luxembourg"), Geo, H),
        ord("ordw_ue_akcesja", "Order these countries by when they joined the EU or its predecessor, earliest first.", listOf("France", "Denmark", "Sweden", "Croatia"), Geo, H),
        ord("ordt_rzeki_swiat_2", "Order these rivers by length, shortest first.", listOf("The Thames", "The Rhine", "The Mississippi", "The Amazon"), Geo, H),
        ord("ordt_kraje_ludnosc_3", "Order these countries by population, smallest first.", listOf("Canada", "The United Kingdom", "Japan", "Indonesia"), Geo, H),
        ord("ordt_wyspy_wielkosc", "Order these islands by area, smallest first.", listOf("Iceland", "Great Britain", "Madagascar", "Greenland"), Geo, H),
        ord("ord2_rzeki_dlugosc2", "Order these rivers from shortest to longest.", listOf("Oder", "Rhine", "Danube", "Yangtze"), Geo, X),
        ord("ordt_jeziora_glebokosc", "Order these lakes by maximum depth, shallowest first.", listOf("Lake Victoria", "Lake Superior", "Lake Tanganyika", "Lake Baikal"), Geo, X),

        /* ----------------------------- Sport ------------------------------ */
        ord("ordt_igrzyska_medale", "Order these countries by total Summer Olympic medals, fewest first.", listOf("Brazil", "Japan", "Germany", "The United States"), Spo, E),
        ord("ordw_zawodnicy_na_boisku", "Order these sports by how many players one team fields, fewest first.", listOf("Tennis singles", "Basketball", "Football", "Rugby union"), Spo, M),
        ord("ordt_sport_boiska", "Order these playing areas by size, smallest first.", listOf("A tennis court", "A basketball court", "A football pitch", "A polo field"), Spo, M),
        ord("ordt_sport_pilki_masa", "Order these balls by weight, lightest first.", listOf("A table tennis ball", "A golf ball", "A football", "A bowling ball"), Spo, M),
        ord("ord2_sport_igrzyska", "Order these Olympic Games from oldest.", listOf("Athens 1896", "Berlin 1936", "Moscow 1980", "London 2012"), Spo, H),
        ord("ordt_mundial_gospodarze", "Order these World Cup host countries chronologically, earliest first.", listOf("Uruguay", "England", "Mexico", "Qatar"), Spo, H),

        /* ------------------------------ Math ------------------------------ */
        ord("ordt_liczby_kwadrat", "Order these from smallest to largest.", listOf("2 to the power of 4", "5 squared", "3 cubed", "The square root of 1000"), Mat, E),
        ord("ord_math_fractions", "Order these fractions from smallest to largest.", listOf("1/8", "1/4", "1/2", "3/4"), Mat, M),
        ord("ord2_liczby_ulamki2", "Order these values from smallest to largest.", listOf("0.05", "0.5", "5/6", "1.2"), Mat, M),
        ord("ordt_ulamki_mieszane", "Order these from smallest to largest.", listOf("0.3", "1/3", "35%", "3/8"), Mat, M),
        ord("ordt_potegi_dwojki", "Order these from smallest to largest.", listOf("10 squared", "2 to the power of 7", "3 to the power of 5", "5 cubed times 2"), Mat, M),
        ord("ordt_liczby_pierwsze_kolejne", "Order these numbers by how many divisors they have, fewest first.", listOf("13", "9", "12", "24"), Mat, H),
        ord("ordt_liczby_specjalne", "Order these from smallest to largest.", listOf("The square root of 2", "The golden ratio", "e", "Pi"), Mat, H),

        /* --------------------------- Literature --------------------------- */
        ord("ordt_ksiazki_bohater", "Order these Harry Potter books by release, first to last.", listOf("The Philosopher's Stone", "The Prisoner of Azkaban", "The Order of the Phoenix", "The Deathly Hallows"), Lit, E),
        ord("ord2_epoki_literackie", "Order these literary eras from oldest to newest.", listOf("Antiquity", "The Middle Ages", "The Renaissance", "Romanticism"), Lit, M),
        ord("ord3_literatura_dziecieca", "Order these children's books from oldest to newest.", listOf("Aesop's Fables", "Winnie the Pooh", "The Moomins", "Harry Potter"), Lit, M),
        ord("ordw_ksiazki_publikacja", "Order these books by publication date, oldest first.", listOf("Don Quixote", "Frankenstein", "1984", "Harry Potter and the Philosopher's Stone"), Lit, M),
        ord("ordw_dziela_klasyczne", "Order these works from oldest to most recent.", listOf("The Odyssey", "The Divine Comedy", "Hamlet", "War and Peace"), Lit, M),
        ord("ordt_ksiazki_klasyka", "Order these novels by publication, oldest first.", listOf("Robinson Crusoe", "Pride and Prejudice", "Moby-Dick", "The Great Gatsby"), Lit, M),
        ord("ordt_szekspir_dzieje", "Order these Shakespeare plays by when they were written, earliest first.", listOf("Romeo and Juliet", "Hamlet", "Macbeth", "The Tempest"), Lit, M),
        ord("ordt_ksiazki_dlugosc", "Order these novels by length, shortest first.", listOf("The Old Man and the Sea", "The Great Gatsby", "Moby-Dick", "War and Peace"), Lit, H),
        ord("ordt_pisarze_ur", "Order these writers by birth, earliest first.", listOf("Shakespeare", "Jane Austen", "Mark Twain", "Ernest Hemingway"), Lit, H),
        ord("ordt_nobel_lit_lata", "Order these Nobel laureates in Literature by the year of their award, earliest first.", listOf("Rudyard Kipling", "Ernest Hemingway", "Gabriel Garcia Marquez", "Kazuo Ishiguro"), Lit, X),

        /* --------------------------- Technology --------------------------- */
        ord("ord_history_inventions", "Order these inventions from oldest to newest.", listOf("The wheel", "The printing press", "The light bulb", "The internet"), Tec, E),
        ord("ordt_wynalazki_xx", "Order these inventions from oldest to newest.", listOf("The telephone", "The airplane", "Television", "The smartphone"), Tec, E),
        ord("ord2_wynalazki_komunikacja", "Order these inventions from oldest to newest.", listOf("The telegraph", "The telephone", "Television", "The mobile phone"), Tec, M),
        ord("ord3_wynalazki_transport", "Order these modes of transport from oldest to newest.", listOf("The wheel", "The horse-drawn carriage", "The steam locomotive", "The airplane"), Tec, M),
        ord("ordw_nosniki_danych", "Order these storage media by when they became common, oldest first.", listOf("The punched card", "The floppy disk", "The CD-ROM", "The USB flash drive"), Tec, M),
        ord("ordw_kosmos_kamienie", "Order these spaceflight milestones chronologically, earliest first.", listOf("Sputnik 1", "Gagarin's flight", "The first Moon landing", "The first module of the ISS"), Tec, M),
        ord("ordt_konsole", "Order these game consoles by release, oldest first.", listOf("The Atari 2600", "The Nintendo Entertainment System", "The PlayStation", "The Xbox"), Tec, M),
        ord("ordt_firmy_tech", "Order these companies by founding year, earliest first.", listOf("Microsoft", "Amazon", "Google", "Facebook"), Tec, M),
        ord("ord2_technologia_komputery", "Order these technologies from oldest to newest.", listOf("ENIAC", "The Intel 4004 microprocessor", "The World Wide Web", "The touchscreen smartphone"), Tec, H),
        ord("ord3_historia_wynalazkow_komunikacja2", "Order these communication inventions from oldest.", listOf("Carrier pigeons", "The telegraph", "Radio", "The internet"), Tec, H),
        ord("ordw_firmy_zalozenie", "Order these companies by their founding year, earliest first.", listOf("Coca-Cola", "Nintendo", "IBM", "Apple"), Tec, H),
        ord("ordt_jezyki_prog", "Order these programming languages by when they appeared, earliest first.", listOf("Fortran", "C", "Java", "Swift"), Tec, H),
        ord("ordt_nosniki_pojemnosc", "Order these by storage capacity, smallest first.", listOf("A floppy disk", "A CD", "A DVD", "A Blu-ray disc"), Tec, H),

        /* --------------------------- Mythology ---------------------------- */
        ord("ordt_bogowie_pokolenia", "Order these Greek figures from oldest generation to youngest.", listOf("Uranus", "Cronus", "Zeus", "Athena"), Myt, E),
        ord("ordt_heraklesa_prace", "Order these labours of Heracles as traditionally listed, first to last.", listOf("The Nemean lion", "The Lernaean Hydra", "The Augean stables", "Cerberus"), Myt, M),
        ord("ord2_mitologia_prace", "Order these Greek heroes by the traditional chronology of the myths.", listOf("Perseus", "Heracles", "Theseus", "Odysseus"), Myt, H),
        ord("ordt_mitologia_wojna_trojanska", "Order these events of the Trojan War story, first to last.", listOf("The judgement of Paris", "The abduction of Helen", "The death of Hector", "The wooden horse"), Myt, H),
        ord("ordt_odyseusz_droga", "Order these stops on Odysseus's journey home, first to last.", listOf("The Cyclops Polyphemus", "Circe's island", "The Sirens", "Calypso's island"), Myt, H),
        ord("ord2_mitologia_nordycka", "Order these Norse mythology events in story order.", listOf("The world forming from Ymir", "The building of Asgard", "Baldur's death", "Ragnarok"), Myt, X),

        /* --------------------------- PopCulture --------------------------- */
        ord("ord_pop_star_wars", "Order these 'Star Wars' films by release, oldest first.", listOf("A New Hope", "The Empire Strikes Back", "Return of the Jedi", "The Phantom Menace"), Pop, M),
        ord("ord_pop_style_muzyczne", "Order these music styles by when they first appeared, oldest first.", listOf("Jazz", "Rock and roll", "Hip hop", "Grunge"), Pop, M),
        ord("ord_pop_disney", "Order these Disney animated films from oldest to newest.", listOf("Bambi", "Cinderella", "The Little Mermaid", "Frozen"), Pop, H),
        ord("ord_pop_pixar", "Order these Pixar films from oldest to newest.", listOf("A Bug's Life", "Finding Nemo", "Up", "Coco"), Pop, H),
        ord("ord_pop_gry", "Order these video games by release, oldest first.", listOf("Pong", "Pac-Man", "Tetris", "Minecraft"), Pop, H),

        /* ----------------------------- Poland ----------------------------- */
        ord("ord2_miasta_polska", "Order these Polish cities from least to most populous.", listOf("Bialystok", "Wroclaw", "Krakow", "Warsaw"), Pol, M),
        ord("ord2_polska_wojny", "Order these events from Polish history from oldest.", listOf("The Baptism of Poland", "The Battle of Grunwald", "The Constitution of May 3", "Regaining independence"), Pol, M),
        ord("ord3_polska_krolowie_wczesni", "Order these Polish rulers from earliest reign.", listOf("Mieszko I", "Boleslaw the Brave", "Casimir the Restorer", "Boleslaw the Wrymouth"), Pol, M),
        ord("ord2_literatura_polska", "Order these Polish works from oldest to newest.", listOf("Bogurodzica", "Kochanowski's Laments", "Pan Tadeusz", "Ferdydurke"), Pol, H),
        ord("ord_history_dynasties", "Order these Polish rulers from earliest to latest reign.", listOf("Mieszko I", "Casimir the Great", "Stephen Bathory", "Stanislaw August Poniatowski"), Pol, X),
        ord("ord2_nobel_polacy", "Order these Polish Nobel laureates in Literature by year of the award.", listOf("Henryk Sienkiewicz", "Wladyslaw Reymont", "Czeslaw Milosz", "Olga Tokarczuk"), Pol, X)
    )
}
