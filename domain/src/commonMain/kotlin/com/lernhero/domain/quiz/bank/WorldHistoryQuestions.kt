package com.lernhero.domain.quiz.bank

import com.lernhero.domain.quiz.QuizCategory
import com.lernhero.domain.quiz.QuizDifficulty
import com.lernhero.domain.quiz.QuizEntry

/**
 * WORLD history - deliberately without Polish threads.
 *
 * The other history banks lean heavily on Poland (its baptism, Grunwald, the partitions), which
 * is fine for a Polish player but closes the game off to one country. This file adds the other
 * leg: ancient civilizations, Asia, Africa, both Americas and 20th-century world history - so a
 * player from outside Poland has something to play, and a Polish player isn't stuck with the
 * same three dates on repeat.
 *
 * The spread of eras is DELIBERATELY stretched (antiquity -> the present), because the "guess the
 * year" question pool was skewed toward the 20th century and the game boiled down to dates from
 * the last hundred years.
 */
object WorldHistoryQuestions {

    private fun ask(
        id: String,
        prompt: String,
        correct: String,
        wrong: List<String>,
        difficulty: QuizDifficulty,
        category: QuizCategory = QuizCategory.History
    ) = QuizEntry(
        id = "wh_$id",
        prompt = prompt,
        correct = correct,
        wrong = wrong,
        category = category,
        difficulty = difficulty
    )

    val all: List<QuizEntry> = listOf(

        /* =========================== ANTIQUITY - easy =========================== */
        ask(
            id = "kolosseum_miasto",
            prompt = "In which city does the Colosseum stand?",
            correct = "Rome",
            wrong = listOf("Athens", "Istanbul", "Naples"),
            difficulty = QuizDifficulty.Easy
        ),
        ask(
            id = "mur_chinski",
            prompt = "Which country built the Great Wall?",
            correct = "China",
            wrong = listOf("Japan", "Mongolia", "Korea"),
            difficulty = QuizDifficulty.Easy
        ),
        ask(
            id = "faraon_maska",
            prompt = "Which pharaoh's golden death mask is the most famous in the world?",
            correct = "Tutankhamun",
            wrong = listOf("Ramesses II", "Khufu", "Akhenaten"),
            difficulty = QuizDifficulty.Easy
        ),
        ask(
            id = "igrzyska_staroz",
            prompt = "In which country were the ancient Olympic Games held?",
            correct = "Greece",
            wrong = listOf("Italy", "Egypt", "Turkey"),
            difficulty = QuizDifficulty.Easy
        ),
        ask(
            id = "cezar_zabojstwo",
            prompt = "Who was murdered in the Roman senate on the Ides of March, 44 BC?",
            correct = "Julius Caesar",
            wrong = listOf("Augustus", "Nero", "Mark Antony"),
            difficulty = QuizDifficulty.Easy
        ),

        /* =========================== ANTIQUITY - medium =========================== */
        ask(
            id = "hammurabi",
            prompt = "Which Babylonian ruler is known for compiling one of the oldest law codes?",
            correct = "Hammurabi",
            wrong = listOf("Nebuchadnezzar II", "Sargon the Great", "Ashurbanipal"),
            difficulty = QuizDifficulty.Medium
        ),
        ask(
            id = "aleksander_nauczyciel",
            prompt = "Which philosopher was the teacher of Alexander the Great?",
            correct = "Aristotle",
            wrong = listOf("Plato", "Socrates", "Epicurus"),
            difficulty = QuizDifficulty.Medium
        ),
        ask(
            id = "kartagina_wodz",
            prompt = "Which Carthaginian general led elephants across the Alps?",
            correct = "Hannibal",
            wrong = listOf("Hasdrubal", "Scipio", "Vercingetorix"),
            difficulty = QuizDifficulty.Medium
        ),
        ask(
            id = "wojny_punickie",
            prompt = "Between which powers were the Punic Wars fought?",
            correct = "Rome and Carthage",
            wrong = listOf("Rome and Greece", "Greece and Persia", "Rome and Egypt"),
            difficulty = QuizDifficulty.Medium
        ),
        ask(
            id = "termopile",
            prompt = "Which city-state defended the pass of Thermopylae against the Persians?",
            correct = "Sparta",
            wrong = listOf("Athens", "Thebes", "Corinth"),
            difficulty = QuizDifficulty.Medium
        ),
        ask(
            id = "pismo_klinowe",
            prompt = "Which civilization created cuneiform writing?",
            correct = "The Sumerians",
            wrong = listOf("The Egyptians", "The Phoenicians", "The Hittites"),
            difficulty = QuizDifficulty.Medium
        ),
        ask(
            id = "kleopatra_dynastia",
            prompt = "Which dynasty did Cleopatra VII belong to?",
            correct = "The Ptolemies",
            wrong = listOf("The Seleucids", "The Achaemenids", "The Nubian Ptolemies"),
            difficulty = QuizDifficulty.Hard
        ),
        ask(
            id = "rosetta",
            prompt = "Who deciphered Egyptian hieroglyphs thanks to the Rosetta Stone?",
            correct = "Jean-Francois Champollion",
            wrong = listOf("Howard Carter", "Heinrich Schliemann", "Thomas Young"),
            difficulty = QuizDifficulty.Hard
        ),
        ask(
            id = "persepolis",
            prompt = "Persepolis was the capital of which empire?",
            correct = "The Persian Empire",
            wrong = listOf("The Assyrian Empire", "The Babylonian Empire", "The Macedonian Empire"),
            difficulty = QuizDifficulty.Hard
        ),

        /* ============================ MIDDLE AGES ============================ */
        ask(
            id = "wikingowie_kraj",
            prompt = "Which region of Europe did the Vikings come from?",
            correct = "Scandinavia",
            wrong = listOf("The Balkans", "Iberia", "Britain"),
            difficulty = QuizDifficulty.Easy
        ),
        ask(
            id = "hastings_zwyciezca",
            prompt = "Who won the Battle of Hastings in 1066?",
            correct = "William the Conqueror",
            wrong = listOf("Harold II", "Richard the Lionheart", "Canute the Great"),
            difficulty = QuizDifficulty.Medium
        ),
        ask(
            id = "konstantynopol_zdobywca",
            prompt = "Which state captured Constantinople in 1453?",
            correct = "The Ottoman Empire",
            wrong = listOf("Venice", "Bulgaria", "Persia"),
            difficulty = QuizDifficulty.Medium
        ),
        ask(
            id = "czyngis_imperium",
            prompt = "Which empire did Genghis Khan found?",
            correct = "The Mongol Empire",
            wrong = listOf("The Ottoman Empire", "The Chinese Empire", "The Persian Empire"),
            difficulty = QuizDifficulty.Easy
        ),
        ask(
            id = "magna_carta",
            prompt = "In which country was the Magna Carta signed?",
            correct = "England",
            wrong = listOf("France", "Scotland", "Germany"),
            difficulty = QuizDifficulty.Medium
        ),
        ask(
            id = "czarna_smierc",
            prompt = "What is the plague epidemic called that killed about a third of Europeans in the 14th century?",
            correct = "The Black Death",
            wrong = listOf("The Spanish Flu", "Asiatic Cholera", "The English Sweat"),
            difficulty = QuizDifficulty.Easy
        ),
        ask(
            id = "joanna_darc",
            prompt = "During which conflict did Joan of Arc fight?",
            correct = "The Hundred Years' War",
            wrong = listOf("The Thirty Years' War", "The Wars of the Roses", "The Reconquista"),
            difficulty = QuizDifficulty.Medium
        ),
        ask(
            id = "mansa_musa",
            prompt = "Mansa Musa, regarded as the richest person in history, ruled which African empire?",
            correct = "Mali",
            wrong = listOf("Songhai", "Ethiopia", "Zimbabwe"),
            difficulty = QuizDifficulty.Hard
        ),
        ask(
            id = "wojna_dwoch_roz",
            prompt = "The houses of York and Lancaster fought each other during...",
            correct = "The Wars of the Roses",
            wrong = listOf("The Hundred Years' War", "The English Revolution", "The Seven Years' War"),
            difficulty = QuizDifficulty.Hard
        ),
        ask(
            id = "bizancjum_nazwa",
            prompt = "What was the Byzantine Empire otherwise known as?",
            correct = "The Eastern Roman Empire",
            wrong = listOf("The Latin Empire", "The Empire of Trebizond", "The Carolingian Empire"),
            difficulty = QuizDifficulty.Medium
        ),
        ask(
            id = "angkor_wat",
            prompt = "In which country is the Angkor Wat temple complex located?",
            correct = "Cambodia",
            wrong = listOf("Thailand", "Vietnam", "India"),
            difficulty = QuizDifficulty.Medium,
            category = QuizCategory.Geography
        ),

        /* ======================= DISCOVERIES AND THE EARLY MODERN AGE ======================= */
        ask(
            id = "kolumb_rok",
            prompt = "Who reached America under the Spanish flag in 1492?",
            correct = "Christopher Columbus",
            wrong = listOf("Vasco da Gama", "Ferdinand Magellan", "Amerigo Vespucci"),
            difficulty = QuizDifficulty.Easy
        ),
        ask(
            id = "aztekowie_zdobywca",
            prompt = "Which conquistador toppled the Aztec empire?",
            correct = "Hernan Cortes",
            wrong = listOf("Francisco Pizarro", "Vasco Nunez de Balboa", "Diego de Almagro"),
            difficulty = QuizDifficulty.Medium
        ),
        ask(
            id = "inkowie_zdobywca",
            prompt = "Which conquistador conquered the Inca empire?",
            correct = "Francisco Pizarro",
            wrong = listOf("Hernan Cortes", "Juan Ponce de Leon", "Hernando de Soto"),
            difficulty = QuizDifficulty.Hard
        ),
        ask(
            id = "machu_picchu",
            prompt = "Which civilization built Machu Picchu?",
            correct = "The Incas",
            wrong = listOf("The Maya", "The Aztecs", "The Olmecs"),
            difficulty = QuizDifficulty.Medium
        ),
        ask(
            id = "reformacja_luter",
            prompt = "Who started the Reformation by posting 95 theses?",
            correct = "Martin Luther",
            wrong = listOf("John Calvin", "Ulrich Zwingli", "Jan Hus"),
            difficulty = QuizDifficulty.Easy
        ),
        ask(
            id = "wojna_trzydziestoletnia",
            prompt = "Which peace ended the Thirty Years' War?",
            correct = "The Peace of Westphalia",
            wrong = listOf("The Peace of Augsburg", "The Treaty of Utrecht", "The Peace of Nicaea"),
            difficulty = QuizDifficulty.Hard
        ),
        ask(
            id = "bastylia",
            prompt = "The storming of which fortress is considered the start of the French Revolution?",
            correct = "The Bastille",
            wrong = listOf("The Louvre", "Versailles", "The Conciergerie"),
            difficulty = QuizDifficulty.Easy
        ),
        ask(
            id = "napoleon_waterloo",
            prompt = "Near which town did Napoleon suffer his final defeat?",
            correct = "Waterloo",
            wrong = listOf("Leipzig", "Austerlitz", "Borodino"),
            difficulty = QuizDifficulty.Easy
        ),
        ask(
            id = "napoleon_zeslanie",
            prompt = "On which island did Napoleon Bonaparte die?",
            correct = "Saint Helena",
            wrong = listOf("Elba", "Corsica", "Malta"),
            difficulty = QuizDifficulty.Medium
        ),
        ask(
            id = "wojna_secesyjna",
            prompt = "Which US president led the country during the Civil War?",
            correct = "Abraham Lincoln",
            wrong = listOf("George Washington", "Thomas Jefferson", "Ulysses Grant"),
            difficulty = QuizDifficulty.Easy
        ),
        ask(
            id = "meiji",
            prompt = "What is Japan's period of rapid modernization starting in 1868 called?",
            correct = "The Meiji Restoration",
            wrong = listOf("The Edo period", "The Tokugawa shogunate", "The Showa era"),
            difficulty = QuizDifficulty.Hard
        ),
        ask(
            id = "wojna_opiumowa",
            prompt = "Between which countries was the First Opium War fought?",
            correct = "China and Great Britain",
            wrong = listOf("China and Japan", "India and Great Britain", "China and Russia"),
            difficulty = QuizDifficulty.Hard
        ),

        /* ============================== THE 20TH CENTURY ============================== */
        ask(
            id = "ww1_iskra",
            prompt = "The assassination of whom in Sarajevo sparked World War I?",
            correct = "Archduke Franz Ferdinand",
            wrong = listOf("Kaiser Wilhelm II", "Tsar Nicholas II", "King Peter I"),
            difficulty = QuizDifficulty.Medium
        ),
        ask(
            id = "rewolucja_pazdziernikowa",
            prompt = "Who led the Bolsheviks during the October Revolution?",
            correct = "Vladimir Lenin",
            wrong = listOf("Joseph Stalin", "Leon Trotsky", "Alexander Kerensky"),
            difficulty = QuizDifficulty.Easy
        ),
        ask(
            id = "wielki_kryzys",
            prompt = "The crash of which stock exchange started the Great Depression in 1929?",
            correct = "New York's",
            wrong = listOf("London's", "Frankfurt's", "Tokyo's"),
            difficulty = QuizDifficulty.Medium
        ),
        ask(
            id = "gandhi_metoda",
            prompt = "What method of fighting for India's independence did Mahatma Gandhi advocate?",
            correct = "Nonviolent civil resistance",
            wrong = listOf("Guerrilla warfare", "An armed uprising", "A general military strike"),
            difficulty = QuizDifficulty.Easy
        ),
        ask(
            id = "pearl_harbor",
            prompt = "Which country attacked Pearl Harbor in 1941?",
            correct = "Japan",
            wrong = listOf("Germany", "Italy", "The Soviet Union"),
            difficulty = QuizDifficulty.Easy
        ),
        ask(
            id = "d_day",
            prompt = "In which region of France did the Allied D-Day landings begin?",
            correct = "Normandy",
            wrong = listOf("Brittany", "Provence", "Alsace"),
            difficulty = QuizDifficulty.Medium
        ),
        ask(
            id = "onz_rok",
            prompt = "In which year was the United Nations founded?",
            correct = "1945",
            wrong = listOf("1919", "1948", "1950"),
            difficulty = QuizDifficulty.Medium
        ),
        ask(
            id = "apartheid_kraj",
            prompt = "In which country was the apartheid system in force?",
            correct = "South Africa",
            wrong = listOf("Rhodesia", "Namibia", "Kenya"),
            difficulty = QuizDifficulty.Easy
        ),
        ask(
            id = "mandela",
            prompt = "Who became President of South Africa after 27 years in prison?",
            correct = "Nelson Mandela",
            wrong = listOf("Desmond Tutu", "Thabo Mbeki", "Steve Biko"),
            difficulty = QuizDifficulty.Easy
        ),
        ask(
            id = "mao_chiny",
            prompt = "Who proclaimed the People's Republic of China in 1949?",
            correct = "Mao Zedong",
            wrong = listOf("Deng Xiaoping", "Chiang Kai-shek", "Zhou Enlai"),
            difficulty = QuizDifficulty.Medium
        ),
        ask(
            id = "kryzys_kubanski",
            prompt = "Missiles placed on which island triggered a crisis in 1962?",
            correct = "Cuba",
            wrong = listOf("Haiti", "Jamaica", "Puerto Rico"),
            difficulty = QuizDifficulty.Medium
        ),
        ask(
            id = "mur_berlinski_kraj",
            prompt = "Which city was divided by the wall torn down in 1989?",
            correct = "Berlin",
            wrong = listOf("Vienna", "Prague", "Budapest"),
            difficulty = QuizDifficulty.Easy
        ),
        ask(
            id = "zsrr_rozpad",
            prompt = "In which year was the Soviet Union formally dissolved?",
            correct = "1991",
            wrong = listOf("1989", "1990", "1993"),
            difficulty = QuizDifficulty.Medium
        ),
        ask(
            id = "titanic_rejs",
            prompt = "During which voyage did the Titanic sink?",
            correct = "Its maiden voyage",
            wrong = listOf("Its second voyage", "Its tenth voyage", "Its return trip from New York"),
            difficulty = QuizDifficulty.Medium
        ),
        ask(
            id = "hiroszima_kraj",
            prompt = "Which country had atomic bombs dropped on it in 1945?",
            correct = "Japan",
            wrong = listOf("Germany", "China", "Korea"),
            difficulty = QuizDifficulty.Easy
        ),
        ask(
            id = "wojna_wietnamska",
            prompt = "Which power withdrew from Vietnam in 1973?",
            correct = "The United States",
            wrong = listOf("France", "The Soviet Union", "Great Britain"),
            difficulty = QuizDifficulty.Medium
        ),

        /* ====================== WORLD FIGURES AND INVENTIONS ====================== */
        ask(
            id = "gutenberg_wynalazek",
            prompt = "What did Johannes Gutenberg popularize in Europe?",
            correct = "Movable-type printing",
            wrong = listOf("Paper", "The hydraulic press", "Printer's ink"),
            difficulty = QuizDifficulty.Easy,
            category = QuizCategory.Technology
        ),
        ask(
            id = "darwin_dzielo",
            prompt = "Who wrote 'On the Origin of Species'?",
            correct = "Charles Darwin",
            wrong = listOf("Gregor Mendel", "Jean-Baptiste Lamarck", "Alfred Wallace"),
            difficulty = QuizDifficulty.Easy,
            category = QuizCategory.Science
        ),
        ask(
            id = "einstein_teoria",
            prompt = "Which theory is Albert Einstein best known for?",
            correct = "The theory of relativity",
            wrong = listOf("String theory", "Quantum mechanics", "Chaos theory"),
            difficulty = QuizDifficulty.Easy,
            category = QuizCategory.Science
        ),
        ask(
            id = "dna_struktura",
            prompt = "Which scientists described the DNA double helix in 1953?",
            correct = "Watson and Crick",
            wrong = listOf("Mendel and Morgan", "Pasteur and Koch", "Fleming and Florey"),
            difficulty = QuizDifficulty.Hard,
            category = QuizCategory.Biology
        ),
        ask(
            id = "telefon_wynalazca",
            prompt = "Who is credited with patenting the telephone in 1876?",
            correct = "Alexander Graham Bell",
            wrong = listOf("Thomas Edison", "Nikola Tesla", "Guglielmo Marconi"),
            difficulty = QuizDifficulty.Medium,
            category = QuizCategory.Technology
        ),
        ask(
            id = "radio_marconi",
            prompt = "Who carried out the first transatlantic radio transmission?",
            correct = "Guglielmo Marconi",
            wrong = listOf("Nikola Tesla", "Heinrich Hertz", "Lee de Forest"),
            difficulty = QuizDifficulty.Hard,
            category = QuizCategory.Technology
        ),
        ask(
            id = "bracia_wright",
            prompt = "Who made the first successful powered airplane flight in 1903?",
            correct = "The Wright brothers",
            wrong = listOf("Louis Bleriot", "Otto Lilienthal", "Alberto Santos-Dumont"),
            difficulty = QuizDifficulty.Easy,
            category = QuizCategory.Technology
        ),
        ask(
            id = "kopernik_teoria",
            prompt = "Which model of the Solar System holds that planets orbit the Sun?",
            correct = "The heliocentric model",
            wrong = listOf("The geocentric model", "The geoheliocentric model", "The cosmocentric model"),
            difficulty = QuizDifficulty.Easy,
            category = QuizCategory.Science
        ),

        /* ======================= WORLD LITERATURE AND CULTURE ======================= */
        ask(
            id = "szekspir_hamlet",
            prompt = "Who wrote 'Hamlet'?",
            correct = "William Shakespeare",
            wrong = listOf("Christopher Marlowe", "Ben Jonson", "John Milton"),
            difficulty = QuizDifficulty.Easy,
            category = QuizCategory.Literature
        ),
        ask(
            id = "boska_komedia",
            prompt = "Who wrote 'The Divine Comedy'?",
            correct = "Dante Alighieri",
            wrong = listOf("Petrarch", "Boccaccio", "Virgil"),
            difficulty = QuizDifficulty.Medium,
            category = QuizCategory.Literature
        ),
        ask(
            id = "mona_lisa",
            prompt = "Who painted the 'Mona Lisa'?",
            correct = "Leonardo da Vinci",
            wrong = listOf("Michelangelo", "Raphael", "Sandro Botticelli"),
            difficulty = QuizDifficulty.Easy
        ),
        ask(
            id = "kaplica_sykstynska",
            prompt = "Who painted the ceiling of the Sistine Chapel?",
            correct = "Michelangelo",
            wrong = listOf("Leonardo da Vinci", "Raphael", "Caravaggio"),
            difficulty = QuizDifficulty.Medium
        ),
        ask(
            id = "iliada_autor",
            prompt = "Who is 'The Iliad' and 'The Odyssey' attributed to?",
            correct = "Homer",
            wrong = listOf("Hesiod", "Sophocles", "Virgil"),
            difficulty = QuizDifficulty.Easy,
            category = QuizCategory.Literature
        ),

        /* ============================== WORLD MYTHOLOGY ============================== */
        ask(
            id = "mit_wisznu",
            prompt = "In which religion do the gods Brahma, Vishnu and Shiva appear?",
            correct = "Hinduism",
            wrong = listOf("Buddhism", "Sikhism", "Jainism"),
            difficulty = QuizDifficulty.Medium,
            category = QuizCategory.Mythology
        ),
        ask(
            id = "mit_amaterasu",
            prompt = "Amaterasu, the sun goddess, comes from which mythology?",
            correct = "Japanese",
            wrong = listOf("Chinese", "Korean", "Mongolian"),
            difficulty = QuizDifficulty.Hard,
            category = QuizCategory.Mythology
        ),
        ask(
            id = "mit_quetzalcoatl",
            prompt = "Quetzalcoatl, the feathered serpent, was a god of which civilization?",
            correct = "The Aztecs",
            wrong = listOf("The Incas", "The Maya", "The Olmecs"),
            difficulty = QuizDifficulty.Hard,
            category = QuizCategory.Mythology
        ),
        ask(
            id = "mit_anansi",
            prompt = "Anansi, the cunning spider, is a figure from which mythology?",
            correct = "West African",
            wrong = listOf("Australian", "Caribbean", "Polynesian"),
            difficulty = QuizDifficulty.Expert,
            category = QuizCategory.Mythology
        ),

        /* ============================ HISTORICAL GEOGRAPHY ============================ */
        ask(
            id = "mezopotamia_rzeki",
            prompt = "Between which rivers did Mesopotamia lie?",
            correct = "The Euphrates and the Tigris",
            wrong = listOf("The Nile and the Jordan", "The Indus and the Ganges", "The Danube and the Rhine"),
            difficulty = QuizDifficulty.Medium,
            category = QuizCategory.Geography
        ),
        ask(
            id = "jedwabny_szlak",
            prompt = "Which two regions did the Silk Road connect?",
            correct = "China and Europe",
            wrong = listOf("India and Africa", "Persia and Scandinavia", "Japan and America"),
            difficulty = QuizDifficulty.Medium,
            category = QuizCategory.Geography
        ),
        ask(
            id = "timbuktu",
            prompt = "In which country is the historic city of Timbuktu?",
            correct = "Mali",
            wrong = listOf("Niger", "Chad", "Mauritania"),
            difficulty = QuizDifficulty.Hard,
            category = QuizCategory.Geography
        ),
        ask(
            id = "cesarstwo_bizantyjskie_stolica",
            prompt = "What is the city that was the capital of Byzantium called today?",
            correct = "Istanbul",
            wrong = listOf("Ankara", "Athens", "Izmir"),
            difficulty = QuizDifficulty.Medium,
            category = QuizCategory.Geography
        ),

        /* =============================== EXPERT =============================== */
        ask(
            id = "traktat_tordesillas",
            prompt = "Which two countries divided the New World between themselves in the Treaty of Tordesillas?",
            correct = "Spain and Portugal",
            wrong = listOf("England and France", "The Netherlands and Spain", "Portugal and France"),
            difficulty = QuizDifficulty.Expert
        ),
        ask(
            id = "kongres_wiedenski",
            prompt = "After whose defeat was the Congress of Vienna convened?",
            correct = "Napoleon's",
            wrong = listOf("Frederick the Great's", "Charles XII's", "Louis XIV's"),
            difficulty = QuizDifficulty.Expert
        ),
        ask(
            id = "sykes_picot",
            prompt = "Which secret agreement divided the Middle East between Great Britain and France?",
            correct = "Sykes-Picot",
            wrong = listOf("Molotov-Ribbentrop", "The Balfour Declaration", "Locarno"),
            difficulty = QuizDifficulty.Expert
        ),
        ask(
            id = "bretton_woods",
            prompt = "The 1944 Bretton Woods conference established, among other things...",
            correct = "The International Monetary Fund",
            wrong = listOf("NATO", "The European Union", "The Bank of England"),
            difficulty = QuizDifficulty.Expert
        ),
        ask(
            id = "kartagina_zniszczenie",
            prompt = "Which state razed Carthage in 146 BC?",
            correct = "Rome",
            wrong = listOf("Greece", "Egypt", "Numidia"),
            difficulty = QuizDifficulty.Expert
        ),
        ask(
            id = "hetyci_zelazo",
            prompt = "Which ancient people were among the first to master ironworking?",
            correct = "The Hittites",
            wrong = listOf("The Phoenicians", "The Scythians", "The Thracians"),
            difficulty = QuizDifficulty.Expert
        ),
        ask(
            id = "wielki_glod_irlandia",
            prompt = "A failed harvest of which crop caused the Great Famine in Ireland in the 1840s?",
            correct = "The potato",
            wrong = listOf("Wheat", "Oats", "Barley"),
            difficulty = QuizDifficulty.Hard
        ),
        ask(
            id = "kongres_berlinski_afryka",
            prompt = "Which 1884-1885 conference sanctioned the partition of Africa among the powers?",
            correct = "The Berlin Conference",
            wrong = listOf("The Congress of Vienna", "The Yalta Conference", "The Congress of Paris"),
            difficulty = QuizDifficulty.Expert
        )
    )
}
