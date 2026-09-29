package com.lernhero.domain.quiz.bank

import com.lernhero.domain.quiz.MultiSelectEntry
import com.lernhero.domain.quiz.QuizCategory
import com.lernhero.domain.quiz.QuizDifficulty

/**
 * "Zaznacz wszystkie prawdziwe" - wszystkie tematy w jednym pliku.
 *
 * Zasada budowania dystraktorow jest wazniejsza niz sama tresc: bledna opcja musi byc czyms, co
 * gracz moglby naprawde zaznaczyc. "Ktore z tych to ssaki?" z opcja "rower" sprawdza tylko
 * czytanie; z opcja "rekin" sprawdza wiedze. Najlepsze pytania tego typu maja dystraktory, ktore
 * sa POPULARNYMI BLEDAMI: Pluton wsrod planet, pajak wsrod owadow, pterodaktyl wsrod dinozaurow.
 *
 * Tresci znane tylko w Polsce (lektury, krolowie) siedza w [QuizCategory.Poland].
 */
object MultiSelectQuestions {

    private val E = QuizDifficulty.Easy
    private val M = QuizDifficulty.Medium
    private val H = QuizDifficulty.Hard
    private val X = QuizDifficulty.Expert

    private fun ms(id: String, prompt: String, correct: List<String>, wrong: List<String>, category: QuizCategory, difficulty: QuizDifficulty) =
        MultiSelectEntry(id = id, prompt = prompt, correct = correct, wrong = wrong, category = category, difficulty = difficulty)

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

    val all: List<MultiSelectEntry> = listOf(
        /* ---------------------------- Biology ----------------------------- */
        ms("ms_citrus", "Which of these fruits are citrus?", listOf("Lemon", "Orange", "Grapefruit", "Lime"), listOf("Plum", "Apricot", "Cherry"), Bio, E),
        ms("mst_owoce_botanicznie", "Which of these are botanically fruits?", listOf("Tomato", "Cucumber", "Avocado", "Pumpkin"), listOf("Carrot", "Potato", "Onion"), Bio, E),
        ms("ms_organs", "Which of these are human organs?", listOf("Liver", "Pancreas", "Spleen", "Kidney"), listOf("Cartilage", "Tendon", "Epidermis"), Bio, M),
        ms("ms_bones", "Which of these are human bones?", listOf("Femur", "Kneecap", "Collarbone", "Fibula"), listOf("Biceps", "Diaphragm", "Aorta"), Bio, M),
        ms("ms_photosynthesis", "What does a plant take in for photosynthesis?", listOf("Carbon dioxide", "Water", "Light"), listOf("Oxygen", "Nitrogen", "Glucose"), Bio, M),
        ms("ms_spices", "Which of these are spices?", listOf("Cinnamon", "Turmeric", "Cardamom", "Cloves"), listOf("Basil", "Spinach", "Arugula"), Bio, M),
        ms("msw_uklad_pokarmowy", "Which of these are organs of the digestive system?", listOf("The stomach", "The liver", "The pancreas", "The small intestine"), listOf("The kidney", "The spleen", "The lung"), Bio, M),
        ms("mst_witaminy_tluszcz", "Which of these vitamins are fat-soluble?", listOf("Vitamin A", "Vitamin D", "Vitamin E", "Vitamin K"), listOf("Vitamin C", "Vitamin B12", "Vitamin B6"), Bio, M),
        ms("mst_cialo_narzady_parzyste", "Which of these organs do humans have two of?", listOf("Lungs", "Kidneys", "Ovaries or testes"), listOf("Liver", "Heart", "Pancreas", "Spleen"), Bio, M),
        ms("ms_dna_bases", "Which of these are nitrogenous bases in DNA?", listOf("Adenine", "Guanine", "Cytosine", "Thymine"), listOf("Uracil", "Lysine", "Serine"), Bio, H),
        ms("mst_krew_grupy", "Which of these blood groups can safely receive O-negative blood?", listOf("A positive", "B negative", "AB positive", "O positive"), listOf("None - only O negative can", "Only AB", "Only O groups"), Bio, H),

        /* ---------------------------- Animals ----------------------------- */
        ms("ms_mammals", "Which of these animals are mammals?", listOf("Dolphin", "Bat", "Whale", "Platypus"), listOf("Shark", "Penguin", "Crocodile"), Ani, E),
        ms("mst_ssaki_morskie", "Which of these sea animals are mammals?", listOf("The dolphin", "The seal", "The manatee", "The orca"), listOf("The great white shark", "The tuna", "The octopus"), Ani, E),
        ms("ms_birds_flightless", "Which birds can't fly?", listOf("Ostrich", "Penguin", "Kiwi", "Emu"), listOf("Albatross", "Swift", "Hummingbird"), Ani, M),
        ms("ms_reptiles", "Which of these animals are reptiles?", listOf("Chameleon", "Turtle", "Iguana", "Cobra"), listOf("Newt", "Salamander", "Frog"), Ani, M),
        ms("ms_insects", "Which of these are insects?", listOf("Ant", "Dragonfly", "Beetle", "Bee"), listOf("Spider", "Scorpion", "Centipede"), Ani, M),
        ms("ms_amphibians", "Which of these are amphibians?", listOf("Frog", "Toad", "Newt", "Salamander"), listOf("Lizard", "Snake", "Turtle"), Ani, M),
        ms("mst_ptaki_migrujace", "Which of these animals hibernate through winter?", listOf("The hedgehog", "The bat", "The dormouse", "The ground squirrel"), listOf("The fox", "The deer", "The wolf"), Ani, M),
        ms("ms_venomous", "Which of these animals are venomous?", listOf("Cobra", "Scorpion", "Wasp", "Platypus"), listOf("Mole", "Beaver", "Hedgehog"), Ani, H),
        ms("ms_marsupials", "Which of these are marsupials?", listOf("Kangaroo", "Koala", "Wombat", "Tasmanian devil"), listOf("Lemur", "Panda", "Meerkat"), Ani, H),
        ms("msw_dinozaury", "Which of these were dinosaurs?", listOf("Triceratops", "Stegosaurus", "Velociraptor", "Brachiosaurus"), listOf("Pterodactyl", "Mosasaurus", "Dimetrodon"), Ani, H),
        ms("mst_zwierzeta_jadowite_2", "Which of these mammals are venomous?", listOf("The platypus", "The slow loris", "The solenodon"), listOf("The hedgehog", "The vampire bat", "The mongoose", "The honey badger"), Ani, H),
        ms("mst_ptaki_nie_ptaki", "Which of these animals are birds?", listOf("The penguin", "The ostrich", "The kiwi"), listOf("The bat", "The flying fox", "The pterodactyl", "The flying squirrel"), Ani, H),

        /* ---------------------------- History ----------------------------- */
        ms("ms_explorers", "Which of these figures are explorers and travelers?", listOf("Christopher Columbus", "Ferdinand Magellan", "Vasco da Gama", "Marco Polo"), listOf("Galileo", "Descartes", "Machiavelli"), His, E),
        ms("mst_ww2_osi", "Which countries were Axis powers in World War II?", listOf("Germany", "Italy", "Japan"), listOf("The Soviet Union", "The United States", "China", "Poland"), His, E),
        ms("ms_ww2_allies", "Which countries fought on the Allied side in World War II?", listOf("Great Britain", "USSR", "USA", "France"), listOf("Italy", "Japan", "Hungary"), His, M),
        ms("ms_revolutions", "Which of these events are revolutions?", listOf("The French Revolution of 1789", "The October Revolution of 1917", "The American Revolution of 1776", "The Industrial Revolution"), listOf("The Congress of Vienna", "The Peace of Westphalia", "The Treaty of Versailles"), His, M),
        ms("ms_cold_war", "Which of these events belong to the Cold War?", listOf("The Cuban Missile Crisis", "The Berlin Wall", "The Space Race", "The Korean War"), listOf("The Battle of Verdun", "The November Uprising", "The American Civil War"), His, M),
        ms("ms_dictators", "Which of these figures were 20th-century authoritarian leaders?", listOf("Stalin", "Mussolini", "Franco", "Mao Zedong"), listOf("Churchill", "Roosevelt", "de Gaulle"), His, M),
        ms("mst_wladcy_francja", "Which of these people ruled France?", listOf("Louis XIV", "Napoleon Bonaparte", "Charles de Gaulle"), listOf("Otto von Bismarck", "Oliver Cromwell", "Philip II of Spain", "Maria Theresa"), His, M),
        ms("mst_xx_wiek_wydarzenia", "Which of these happened in the 20th century?", listOf("The sinking of the Titanic", "The first Moon landing", "The fall of the Berlin Wall", "The Chernobyl disaster"), listOf("The French Revolution", "The American Civil War", "The invention of the telephone"), His, M),
        ms("ms_hx_wladczynie", "Which of these women ruled as monarchs in their own right?", listOf("Elizabeth I", "Catherine the Great", "Queen Victoria", "Maria Theresa"), listOf("Marie Antoinette", "Anne Boleyn", "Josephine Bonaparte"), His, M),
        ms("ms_roman_emperors", "Which of these figures were Roman emperors?", listOf("Nero", "Trajan", "Hadrian", "Caligula"), listOf("Pericles", "Hannibal", "Spartacus"), His, H),
        ms("ms_ancient_wonders", "Which of these belong to the Seven Wonders of the Ancient World?", listOf("The Great Pyramid of Giza", "The Colossus of Rhodes", "The Hanging Gardens of Babylon", "The Lighthouse of Alexandria"), listOf("The Colosseum", "Machu Picchu", "The Great Wall"), His, H),
        ms("ms_un_security", "Which countries hold a permanent seat on the UN Security Council?", listOf("USA", "China", "Russia", "France"), listOf("Germany", "India", "Japan"), His, H),
        ms("mst_wynalazki_chinskie", "Which of these were invented in ancient or medieval China?", listOf("Paper", "Gunpowder", "The compass", "Printing"), listOf("The wheel", "Glass", "The steam engine"), His, H),
        ms("ms_hx_afryka", "Which of these were empires or kingdoms in Africa?", listOf("Songhai", "Benin", "Kush", "Kongo"), listOf("Khmer", "Toltec", "Parthia"), His, H),

        /* ---------------------------- Elements ---------------------------- */
        ms("ms_metals", "Which of these elements are metals?", listOf("Iron", "Copper", "Zinc", "Lead"), listOf("Sulfur", "Phosphorus", "Iodine"), Ele, E),
        ms("mst_metale_szlachetne_2", "Which of these metals do not rust or tarnish easily?", listOf("Gold", "Platinum"), listOf("Iron", "Copper", "Silver", "Zinc"), Ele, E),
        ms("ms_precious_metals", "Which of these are precious metals?", listOf("Gold", "Silver", "Platinum", "Palladium"), listOf("Aluminum", "Nickel", "Tin"), Ele, M),
        ms("msw_pierwiastki_czy_stopy", "Which of these are chemical elements?", listOf("Tungsten", "Cobalt", "Iodine", "Bismuth"), listOf("Bronze", "Brass", "Steel"), Ele, M),
        ms("mst_magnetyczne", "Which of these metals are attracted to a magnet?", listOf("Iron", "Nickel", "Cobalt"), listOf("Aluminium", "Copper", "Gold", "Silver"), Ele, M),
        ms("mst_pierwiastki_ciecze_gazy", "Which of these elements are gases at room temperature?", listOf("Oxygen", "Chlorine", "Fluorine", "Argon"), listOf("Bromine", "Mercury", "Iodine"), Ele, M),
        ms("ms_noble_gases", "Which of these are noble gases?", listOf("Helium", "Neon", "Argon", "Krypton"), listOf("Nitrogen", "Hydrogen", "Chlorine"), Ele, H),
        ms("ms_halogens", "Which of these are halogens?", listOf("Fluorine", "Chlorine", "Bromine", "Iodine"), listOf("Selenium", "Tellurium", "Boron"), Ele, H),
        ms("ms_radioactive", "Which of these elements are radioactive?", listOf("Uranium", "Radium", "Plutonium", "Polonium"), listOf("Calcium", "Magnesium", "Potassium"), Ele, H),
        ms("msw_gazy_pokojowa", "Which of these are gases at room temperature?", listOf("Nitrogen", "Helium", "Methane", "Chlorine"), listOf("Bromine", "Mercury", "Iodine"), Ele, H),
        ms("mst_pierwiastki_curie", "Which of these elements are named after people?", listOf("Einsteinium", "Curium", "Nobelium", "Mendelevium"), listOf("Polonium", "Francium", "Germanium"), Ele, H),
        ms("mst_pierwiastki_ziemia", "Which of these elements are named after places?", listOf("Polonium", "Francium", "Germanium", "Californium"), listOf("Curium", "Einsteinium", "Fermium"), Ele, H),

        /* ---------------------------- Capitals ---------------------------- */
        ms("mst_europa_stolice_rzeki", "Which of these capitals lie on the Danube?", listOf("Vienna", "Budapest", "Belgrade", "Bratislava"), listOf("Prague", "Warsaw", "Berlin"), Cap, E),
        ms("ms_capitals_real", "Which of these cities are the capitals of their countries?", listOf("Canberra", "Ottawa", "Brasilia", "Ankara"), listOf("Sydney", "Toronto", "Rio de Janeiro"), Cap, M),
        ms("ms_asia_capitals", "Which of these are capitals of Asian countries?", listOf("Hanoi", "Seoul", "Manila", "Bangkok"), listOf("Osaka", "Shanghai", "Mumbai"), Cap, M),
        ms("mst_stolice_nie_najwieksze", "In which of these countries is the capital NOT the largest city?", listOf("Australia", "Canada", "Turkey", "The United States"), listOf("France", "Japan", "Egypt"), Cap, M),

        /* ---------------------------- Science ----------------------------- */
        ms("ms_renewable", "Which of these are renewable energy sources?", listOf("Wind", "Solar", "Hydro", "Geothermal"), listOf("Coal", "Oil", "Natural gas"), Sci, E),
        ms("ms_instruments_string", "Which of these instruments are string instruments?", listOf("Violin", "Guitar", "Harp", "Cello"), listOf("Trumpet", "Flute", "Drums"), Sci, E),
        ms("mst_planety_skaliste", "Which of these are rocky planets?", listOf("Mercury", "Venus", "Earth", "Mars"), listOf("Jupiter", "Saturn", "Neptune"), Sci, E),
        ms("ms_planets", "Which of these are planets of the Solar System?", listOf("Mars", "Venus", "Neptune", "Saturn"), listOf("Pluto", "Ceres", "The Moon"), Sci, M),
        ms("ms_states_matter", "Which of these are states of matter?", listOf("Solid", "Liquid", "Gas", "Plasma"), listOf("Crystalline", "Colloidal", "Amorphous"), Sci, M),
        ms("ms_greenhouse", "Which of these are greenhouse gases?", listOf("Carbon dioxide", "Methane", "Nitrous oxide", "Water vapor"), listOf("Oxygen", "Argon", "Helium"), Sci, M),
        ms("ms_primary_colors", "Which of these are primary colors in the RGB model?", listOf("Red", "Green", "Blue"), listOf("Yellow", "Magenta", "Cyan"), Sci, M),
        ms("msw_nobel_kategorie", "Which of these are Nobel Prize categories?", listOf("Physics", "Chemistry", "Peace", "Literature"), listOf("Mathematics", "Philosophy", "Music"), Sci, M),
        ms("msw_chmury", "Which of these are types of cloud?", listOf("Cumulus", "Cirrus", "Stratus", "Nimbostratus"), listOf("Corona", "Aurora", "Zephyr"), Sci, M),
        ms("mst_planety_pierscienie", "Which of these planets have rings?", listOf("Jupiter", "Saturn", "Uranus", "Neptune"), listOf("Mars", "Venus", "Earth"), Sci, M),
        ms("mst_energia_odnawialna_nie", "Which of these are fossil fuels?", listOf("Coal", "Crude oil", "Natural gas", "Lignite"), listOf("Uranium", "Hydrogen", "Biomass"), Sci, M),
        ms("ms_si_units", "Which of these are base SI units?", listOf("Meter", "Kilogram", "Second", "Ampere"), listOf("Newton", "Watt", "Joule"), Sci, H),
        ms("ms_forces", "Which of these are fundamental forces in physics?", listOf("Gravity", "Electromagnetism", "The strong nuclear force", "The weak nuclear force"), listOf("Friction", "Elasticity", "Centrifugal force"), Sci, H),
        ms("ms_red_wine", "Which of these are red wine grape varieties?", listOf("Merlot", "Cabernet Sauvignon", "Pinot Noir", "Syrah"), listOf("Chardonnay", "Riesling", "Sauvignon Blanc"), Sci, H),
        ms("msw_ksiezyce", "Which of these are moons?", listOf("Titan", "Europa", "Ganymede", "Io"), listOf("Ceres", "Vesta", "Eris"), Sci, H),
        ms("mst_czastki_elementarne", "Which of these are subatomic particles?", listOf("The electron", "The neutrino", "The quark", "The photon"), listOf("The atom", "The molecule", "The ion"), Sci, H),
        ms("mst_skale_jednostki", "Which of these are units of energy?", listOf("The joule", "The calorie", "The kilowatt-hour", "The electronvolt"), listOf("The watt", "The newton", "The pascal"), Sci, H),

        /* --------------------------- Geography ---------------------------- */
        ms("ms_africa", "Which of these countries are in Africa?", listOf("Kenya", "Morocco", "Namibia", "Chad"), listOf("Nepal", "Bolivia", "Laos"), Geo, E),
        ms("ms_islands", "Which of these countries are islands?", listOf("Iceland", "Madagascar", "Sri Lanka", "Malta"), listOf("Austria", "Paraguay", "Mongolia"), Geo, E),
        ms("ms_rivers_europe", "Which of these rivers flow through Europe?", listOf("Danube", "Rhine", "Loire", "Thames"), listOf("Mekong", "Zambezi", "Parana"), Geo, E),
        ms("ms_mountains", "Which of these are mountain ranges?", listOf("Andes", "Alps", "Urals", "Carpathians"), listOf("Sahel", "Riviera", "Fjord"), Geo, E),
        ms("ms_oceans", "Which of these are oceans?", listOf("Pacific", "Atlantic", "Indian", "Arctic"), listOf("Mediterranean", "Caribbean", "Caspian"), Geo, E),
        ms("ms_continents", "Which of these are continents?", listOf("Asia", "Africa", "Europe", "Antarctica"), listOf("Greenland", "Scandinavia", "The Middle East"), Geo, E),
        ms("ms_baltic", "Which countries border the Baltic Sea?", listOf("Poland", "Lithuania", "Finland", "Estonia"), listOf("Czechia", "Slovakia", "Hungary"), Geo, M),
        ms("ms_south_america", "Which of these countries are in South America?", listOf("Peru", "Uruguay", "Guyana", "Ecuador"), listOf("Honduras", "Belize", "Costa Rica"), Geo, M),
        ms("ms_landlocked", "Which of these countries are landlocked?", listOf("Switzerland", "Mongolia", "Nepal", "Paraguay"), listOf("Portugal", "Vietnam", "Croatia"), Geo, M),
        ms("ms_deserts", "Which of these are deserts?", listOf("Gobi", "Atacama", "Kalahari", "Mojave"), listOf("Pampas", "Taiga", "Tundra"), Geo, M),
        ms("ms_scandinavia", "Which countries are Scandinavian or Nordic?", listOf("Norway", "Sweden", "Denmark", "Iceland"), listOf("Estonia", "Netherlands", "Scotland"), Geo, M),
        ms("ms_euro", "Which of these countries use the euro?", listOf("Germany", "Slovakia", "Greece", "Portugal"), listOf("Poland", "Czechia", "Hungary"), Geo, M),
        ms("msw_ameryka_srodkowa", "Which of these countries are in Central America?", listOf("Guatemala", "Honduras", "Costa Rica", "Panama"), listOf("Colombia", "Venezuela", "Ecuador"), Geo, M),
        ms("msw_stany_usa", "Which of these are US states?", listOf("Oregon", "Vermont", "Nebraska", "Idaho"), listOf("Ontario", "Alberta", "Manitoba"), Geo, M),
        ms("mst_kraje_ue_nie", "Which of these European countries are NOT in the European Union?", listOf("Norway", "Switzerland", "Iceland", "Serbia"), listOf("Croatia", "Ireland", "Portugal"), Geo, M),
        ms("mst_rownik_kraje", "Which of these countries does the equator pass through?", listOf("Brazil", "Kenya", "Indonesia", "Ecuador"), listOf("Mexico", "India", "Egypt"), Geo, M),
        ms("msw_wielkie_jeziora", "Which of these are Great Lakes of North America?", listOf("Superior", "Michigan", "Huron", "Erie"), listOf("Winnipeg", "Great Bear", "Great Slave"), Geo, H),
        ms("msw_morza_srodziemne", "Which of these are parts of the Mediterranean Sea?", listOf("The Adriatic Sea", "The Aegean Sea", "The Ionian Sea", "The Tyrrhenian Sea"), listOf("The Baltic Sea", "The Caspian Sea", "The Barents Sea"), Geo, H),
        ms("msw_nato_czlonkowie", "Which of these countries are NATO members?", listOf("Norway", "Turkiye", "Canada", "Poland"), listOf("Austria", "Ireland", "Switzerland"), Geo, H),
        ms("mst_kraje_dwa_kontynenty", "Which of these countries have territory on two continents?", listOf("Russia", "Turkey", "Egypt", "Kazakhstan"), listOf("Iran", "Morocco", "Greece"), Geo, H),
        ms("mst_kraje_bez_armii", "Which of these countries have no standing army?", listOf("Costa Rica", "Iceland", "Liechtenstein"), listOf("Switzerland", "Ireland", "Austria", "Norway"), Geo, H),

        /* ----------------------------- Sport ------------------------------ */
        ms("ms_winter_sports", "Which of these are winter sports?", listOf("Ice hockey", "Biathlon", "Bobsled", "Figure skating"), listOf("Cycling", "Baseball", "Rugby"), Spo, E),
        ms("mst_sporty_rakieta", "Which of these sports are played with a racket?", listOf("Tennis", "Badminton", "Squash", "Table tennis"), listOf("Baseball", "Cricket", "Golf"), Spo, E),
        ms("ms_olympic_summer", "Which of these disciplines are Summer Olympic sports?", listOf("Track and field", "Swimming", "Fencing", "Rowing"), listOf("Biathlon", "Curling", "Ski jumping"), Spo, M),
        ms("ms_football_clubs", "Which of these clubs play in Spain?", listOf("Real Madrid", "FC Barcelona", "Atletico Madrid", "Sevilla"), listOf("Juventus", "Ajax", "Porto"), Spo, M),
        ms("ms_tennis_slam", "Which tournaments belong to tennis's Grand Slam?", listOf("Wimbledon", "Roland Garros", "US Open", "Australian Open"), listOf("The Davis Cup", "The ATP Finals", "Indian Wells"), Spo, M),
        ms("ms_f1", "Which of these are Formula 1 teams?", listOf("Ferrari", "McLaren", "Mercedes", "Red Bull"), listOf("Yamaha", "Ducati", "Peugeot"), Spo, M),
        ms("mst_olimpiady_letnie_gosp", "Which of these cities have hosted the Summer Olympics?", listOf("Tokyo", "Sydney", "Beijing", "Rio de Janeiro"), listOf("New Delhi", "Cape Town", "Toronto"), Spo, M),
        ms("mst_sporty_druzynowe_11", "In which of these sports does a team have exactly 11 players on the field?", listOf("Football (soccer)", "Cricket", "Field hockey", "American football"), listOf("Rugby union", "Basketball", "Volleyball"), Spo, M),
        ms("msw_mistrzowie_swiata", "Which of these countries have won the FIFA World Cup?", listOf("Uruguay", "Argentina", "Spain", "England"), listOf("The Netherlands", "Hungary", "Portugal"), Spo, H),
        ms("mst_wielki_szlem_zwyciezcy", "Which of these players have won all four tennis Grand Slam tournaments?", listOf("Roger Federer", "Rafael Nadal", "Novak Djokovic", "Serena Williams"), listOf("Pete Sampras", "Bjorn Borg", "Boris Becker"), Spo, H),
        ms("mst_olimpijskie_sporty_dawne", "Which of these were once Olympic events?", listOf("Tug of war", "Art competitions", "Live pigeon shooting", "Solo synchronised swimming"), listOf("Chess", "Darts", "Bowling"), Spo, H),

        /* ------------------------------ Math ------------------------------ */
        ms("ms_triangles", "Which of these are types of triangles?", listOf("Equilateral", "Isosceles", "Right", "Obtuse"), listOf("Parallel", "Convex", "Symmetric"), Mat, E),
        ms("ms_even", "Which of these numbers are divisible by 3?", listOf("9", "27", "51", "123"), listOf("20", "44", "70"), Mat, E),
        ms("ms_metric", "Which of these are units of length?", listOf("Millimeter", "Kilometer", "Mile", "Inch"), listOf("Liter", "Gram", "Hectare"), Mat, E),
        ms("mst_liczby_pierwsze_male", "Which of these numbers are prime?", listOf("7", "11", "17", "23"), listOf("9", "15", "21"), Mat, E),
        ms("ms_primes", "Which of these numbers are prime?", listOf("2", "13", "29", "47"), listOf("21", "51", "91"), Mat, M),
        ms("ms_squares", "Which of these numbers are squares of integers?", listOf("16", "49", "81", "144"), listOf("50", "99", "120"), Mat, M),
        ms("mst_liczby_podzielne_6", "Which of these numbers are divisible by 6?", listOf("18", "42", "96", "114"), listOf("26", "64", "93"), Mat, M),
        ms("mst_figury_katy_proste", "Which of these shapes always have at least one right angle?", listOf("A square", "A rectangle", "A right triangle"), listOf("A rhombus", "A regular hexagon", "A parallelogram", "An equilateral triangle"), Mat, M),
        ms("ms_irrational", "Which of these numbers are irrational?", listOf("Pi", "e", "The square root of 2", "The golden ratio"), listOf("0.5", "3", "7/2"), Mat, H),
        ms("mst_liczby_doskonale", "Which of these are perfect numbers (equal to the sum of their proper divisors)?", listOf("6", "28"), listOf("12", "16", "24", "30", "36"), Mat, H),

        /* --------------------------- Literature --------------------------- */
        ms("ms_painters", "Which of these figures are painters?", listOf("Van Gogh", "Picasso", "Rembrandt", "Monet"), listOf("Mozart", "Chopin", "Beethoven"), Lit, E),
        ms("mst_tolkien_postacie", "Which of these characters appear in 'The Lord of the Rings'?", listOf("Gandalf", "Aragorn", "Gollum", "Legolas"), listOf("Dumbledore", "Aslan", "Drizzt"), Lit, E),
        ms("ms_shakespeare", "Which of these are works by Shakespeare?", listOf("Hamlet", "Macbeth", "Othello", "The Tempest"), listOf("Faust", "The Odyssey", "The Divine Comedy"), Lit, M),
        ms("ms_dystopia", "Which of these novels are dystopias?", listOf("1984", "Brave New World", "Fahrenheit 451", "Animal Farm"), listOf("Pride and Prejudice", "The Great Gatsby", "Anna Karenina"), Lit, M),
        ms("ms_composers", "Which of these figures are composers?", listOf("Bach", "Mozart", "Chopin", "Vivaldi"), listOf("Rodin", "Michelangelo", "Donatello"), Lit, M),
        ms("mst_dickens_dziela", "Which of these novels did Charles Dickens write?", listOf("Oliver Twist", "Great Expectations", "A Christmas Carol", "David Copperfield"), listOf("Jane Eyre", "Wuthering Heights", "Vanity Fair"), Lit, M),
        ms("mst_pseudonimy", "Which of these names are pen names rather than birth names?", listOf("Mark Twain", "George Orwell", "Lewis Carroll", "George Eliot"), listOf("Charles Dickens", "Jane Austen", "Ernest Hemingway"), Lit, M),
        ms("ms_greek_epics", "Which of these are ancient epics?", listOf("The Iliad", "The Odyssey", "The Aeneid", "The Epic of Gilgamesh"), listOf("Hamlet", "The Decameron", "Candide"), Lit, H),
        ms("ms_russian_lit", "Which of these works were written by Russian authors?", listOf("War and Peace", "Crime and Punishment", "Anna Karenina", "The Master and Margarita"), listOf("Les Miserables", "The Trial", "The Stranger"), Lit, H),
        ms("ms_languages_romance", "Which of these languages are Romance languages?", listOf("Spanish", "French", "Italian", "Romanian"), listOf("German", "Polish", "Hungarian"), Lit, H),
        ms("ms_slavic", "Which of these languages are Slavic?", listOf("Polish", "Czech", "Serbian", "Bulgarian"), listOf("Lithuanian", "Estonian", "Finnish"), Lit, H),
        ms("mst_nobel_lit_pisarze", "Which of these writers won the Nobel Prize in Literature?", listOf("Ernest Hemingway", "William Faulkner", "Bob Dylan", "Toni Morrison"), listOf("James Joyce", "Virginia Woolf", "Franz Kafka"), Lit, H),
        ms("msw_nobel_literatura", "Which of these writers won the Nobel Prize in Literature?", listOf("Ernest Hemingway", "Gabriel Garcia Marquez", "Toni Morrison", "Kazuo Ishiguro"), listOf("Leo Tolstoy", "James Joyce", "Jorge Luis Borges"), Lit, X),

        /* --------------------------- Technology --------------------------- */
        ms("ms_os", "Which of these are operating systems?", listOf("Linux", "Windows", "macOS", "Android"), listOf("Firefox", "Docker", "Photoshop"), Tec, E),
        ms("ms_browsers", "Which of these are web browsers?", listOf("Chrome", "Firefox", "Safari", "Opera"), listOf("Outlook", "Slack", "Spotify"), Tec, E),
        ms("ms_apple", "Which of these products did Apple make?", listOf("iPhone", "iPad", "Macintosh", "iPod"), listOf("Galaxy", "Surface", "Pixel"), Tec, E),
        ms("mst_firmy_amerykanskie", "Which of these tech companies were founded in the United States?", listOf("Apple", "Microsoft", "Google", "Amazon"), listOf("Samsung", "Nokia", "Sony"), Tec, E),
        ms("ms_prog_langs", "Which of these are programming languages?", listOf("Python", "Kotlin", "Rust", "Swift"), listOf("HTML", "JSON", "YAML"), Tec, M),
        ms("ms_storage", "Which of these are storage media?", listOf("SSD", "Hard drive", "Flash drive", "DVD"), listOf("Processor", "Graphics card", "Power supply"), Tec, M),
        ms("ms_social", "Which of these services belong to Meta?", listOf("Facebook", "Instagram", "WhatsApp", "Threads"), listOf("TikTok", "Snapchat", "Telegram"), Tec, M),
        ms("mst_jezyki_prog_google", "Which of these were created or are maintained mainly by Google?", listOf("Android", "Chrome", "Go (the language)", "YouTube"), listOf("Linux", "Firefox", "WhatsApp"), Tec, M),
        ms("mst_formaty_obraz", "Which of these are image file formats?", listOf("JPEG", "PNG", "GIF", "SVG"), listOf("MP3", "DOCX", "ZIP"), Tec, M),
        ms("ms_network", "Which of these are network protocols?", listOf("HTTP", "FTP", "TCP", "DNS"), listOf("USB", "HDMI", "PDF"), Tec, H),
        ms("msw_instrumenty_dete_drewniane", "Which of these are woodwind instruments?", listOf("The clarinet", "The oboe", "The bassoon", "The flute"), listOf("The trumpet", "The trombone", "The French horn"), Tec, H),
        ms("mst_jezyki_prog_typowane", "Which of these languages are primarily used for web browsers' front-end?", listOf("JavaScript", "TypeScript"), listOf("Python", "C++", "Rust", "Kotlin", "Swift"), Tec, H),

        /* --------------------------- Mythology ---------------------------- */
        ms("ms_greek_gods", "Which of these figures are Greek gods?", listOf("Zeus", "Athena", "Poseidon", "Apollo"), listOf("Jupiter", "Odin", "Ra"), Myt, E),
        ms("mst_bogowie_rzymscy", "Which of these are Roman gods?", listOf("Jupiter", "Mars", "Venus", "Neptune"), listOf("Zeus", "Ares", "Thor"), Myt, E),
        ms("ms_norse", "Which of these figures belong to Norse mythology?", listOf("Odin", "Thor", "Loki", "Freya"), listOf("Anubis", "Hermes", "Vishnu"), Myt, M),
        ms("ms_egyptian", "Which of these deities belong to Egyptian mythology?", listOf("Ra", "Osiris", "Anubis", "Isis"), listOf("Ares", "Hera", "Baldur"), Myt, M),
        ms("ms_mythical_creatures", "Which of these creatures come from Greek mythology?", listOf("Medusa", "Centaur", "Cyclops", "Chimera"), listOf("Werewolf", "Golem", "Kraken"), Myt, M),
        ms("mst_olimp_dwunastu", "Which of these are among the twelve Olympian gods?", listOf("Hera", "Apollo", "Artemis", "Hephaestus"), listOf("Hades", "Persephone", "Prometheus"), Myt, M),
        ms("mst_potwory_wiele_glow", "Which of these mythical creatures have more than one head?", listOf("Cerberus", "The Hydra", "The Chimera"), listOf("The Minotaur", "The Cyclops", "Pegasus", "The Sphinx"), Myt, M),
        ms("ms_heracles", "Which of these are labors of Heracles?", listOf("The Nemean lion", "The Lernaean Hydra", "The Augean stables", "The apples of the Hesperides"), listOf("The Minotaur in the labyrinth", "The Golden Fleece", "The Trojan Horse"), Myt, H),
        ms("mst_bogowie_nordyccy_2", "Which of these belong to Norse mythology?", listOf("Yggdrasil", "Fenrir", "Valkyries", "Mjolnir"), listOf("Excalibur", "The Golden Fleece", "Cerberus"), Myt, H),

        /* --------------------------- PopCulture --------------------------- */
        ms("ms_pop_pixar", "Which of these films were made by Pixar?", listOf("Ratatouille", "Finding Nemo", "Up", "Cars"), listOf("Shrek", "Frozen", "Madagascar"), Pop, E),
        ms("ms_pop_ksiezniczki", "Which of these are Disney princesses?", listOf("Ariel", "Belle", "Mulan", "Jasmine"), listOf("Fiona", "Anastasia", "Dorothy"), Pop, E),
        ms("ms_pop_beatles", "Which of these were members of The Beatles?", listOf("John Lennon", "Paul McCartney", "George Harrison", "Ringo Starr"), listOf("Mick Jagger", "Keith Moon", "Eric Clapton"), Pop, M),
        ms("ms_pop_nintendo", "Which of these game characters were created by Nintendo?", listOf("Link", "Kirby", "Donkey Kong", "Yoshi"), listOf("Sonic", "Crash Bandicoot", "Lara Croft"), Pop, M),
        ms("ms_pop_marvel", "Which of these superheroes come from Marvel, not DC?", listOf("Spider-Man", "Iron Man", "Wolverine", "Black Panther"), listOf("Batman", "Superman", "Wonder Woman"), Pop, M),
        ms("ms_pop_bond", "Which of these actors have played James Bond?", listOf("Timothy Dalton", "Roger Moore", "Pierce Brosnan", "Daniel Craig"), listOf("Hugh Grant", "Tom Cruise", "Christian Bale"), Pop, H),
        ms("ms_pop_szwecja", "Which of these music acts come from Sweden?", listOf("Roxette", "Avicii", "Europe", "The Cardigans"), listOf("a-ha", "Bjork", "Aqua"), Pop, H),

        /* ----------------------------- Poland ----------------------------- */
        ms("ms_polish_kings", "Which of these figures were Kings of Poland?", listOf("Casimir the Great", "Wladyslaw Jagiello", "John III Sobieski", "Boleslaw the Brave"), listOf("Tadeusz Kosciuszko", "Jozef Pilsudski", "Nicolaus Copernicus"), Pol, M),
        ms("ms_mickiewicz", "Which of these are works by Adam Mickiewicz?", listOf("Pan Tadeusz", "Forefathers' Eve", "Konrad Wallenrod", "Grazyna"), listOf("The Doll", "Quo Vadis", "The Wedding"), Pol, M),
        ms("ms_sienkiewicz", "Which of these novels did Henryk Sienkiewicz write?", listOf("Quo Vadis", "The Deluge", "With Fire and Sword", "The Knights of the Cross"), listOf("The Doll", "The Peasants", "Ferdydurke"), Pol, M),
        ms("ms_nobel_pl", "Which of these Poles won the Nobel Prize in Literature?", listOf("Wislawa Szymborska", "Czeslaw Milosz", "Olga Tokarczuk", "Wladyslaw Reymont"), listOf("Zbigniew Herbert", "Stanislaw Lem", "Witold Gombrowicz"), Pol, H)
    )
}
