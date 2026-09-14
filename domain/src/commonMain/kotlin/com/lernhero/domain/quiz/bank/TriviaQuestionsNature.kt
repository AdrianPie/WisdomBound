package com.lernhero.domain.quiz.bank

import com.lernhero.domain.quiz.QuizCategory
import com.lernhero.domain.quiz.QuizDifficulty
import com.lernhero.domain.quiz.QuizEntry

/**
 * Pytania, ktore chce sie zapamietac: zwierzeta, cialo, kosmos, Ziemia, historia.
 *
 * Powstaly po audycie starego banku, w ktorym poziom "latwy" oznaczal czesto "przedszkole"
 * ("ile oczu ma czlowiek?", "jaki ksztalt ma globus?"). Takie pytanie nie jest latwe - jest
 * ZEROWE: nie da sie go nie wiedziec, wiec odpowiedz nic nie daje. Ponizsze trzymaja sie innej
 * zasady: **latwe pytanie to takie, ktore wiekszosc doroslych zna, ale musi sobie przypomniec**
 * ("ktora planeta jest najgoretsza?"), a trudne - takie, po ktorym gracz mowi "serio?"
 * ("Kleopatra zyla blizej ladowania na Ksiezycu niz budowy piramid").
 *
 * Kazdy dystraktor jest z tej samej polki co odpowiedz. "Ktore zwierze ma niebieska krew?"
 * z opcjami osmiornica/kot/koza/kura sprawdza wiedze; z opcjami osmiornica/rower/Paryz/wtorek
 * sprawdza tylko, czy gracz czyta.
 */
object TriviaQuestionsNature {

    private fun ask(
        id: String,
        prompt: String,
        correct: String,
        wrong: List<String>,
        category: QuizCategory,
        difficulty: QuizDifficulty
    ) = QuizEntry(
        id = "trv_$id",
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

    val all: List<QuizEntry> = listOf(

        /* ================================ Animals ================================ */
        ask("koala_odciski", "Which animal has fingerprints so similar to a human's that they could confuse a crime lab?", "The koala", listOf("The chimpanzee", "The raccoon", "The otter"), A, QuizDifficulty.Medium),
        ask("niedzwiedz_skora", "What colour is a polar bear's skin under its white fur?", "Black", listOf("White", "Pink", "Grey"), A, QuizDifficulty.Medium),
        ask("kiwi_jajo", "Which bird lays the largest egg relative to its own body size?", "The kiwi", listOf("The ostrich", "The hummingbird", "The emu"), A, QuizDifficulty.Hard),
        ask("delfin_sen", "Which animal sleeps with one half of its brain at a time?", "The dolphin", listOf("The horse", "The sloth", "The hedgehog"), A, QuizDifficulty.Medium),
        ask("rybitwa_migracja", "Which bird makes the longest yearly migration, roughly from the Arctic to the Antarctic and back?", "The Arctic tern", listOf("The swallow", "The white stork", "The Canada goose"), A, QuizDifficulty.Medium),
        ask("wrony_stado", "What is a group of crows called?", "A murder", listOf("A parliament", "A gaggle", "A pride"), A, QuizDifficulty.Medium),
        ask("sowy_stado", "What is a group of owls called?", "A parliament", listOf("A murder", "A congress", "A choir"), A, QuizDifficulty.Hard),
        ask("strus_oczy", "Which land animal has the largest eyes?", "The ostrich", listOf("The horse", "The giraffe", "The elephant"), A, QuizDifficulty.Hard),
        ask("flaming_kolor", "Where do flamingos get their pink colour from?", "Pigments in the food they eat", listOf("Sunlight on their feathers", "Their blood showing through", "Minerals in the water"), A, QuizDifficulty.Easy),
        ask("sowa_glowa", "Which bird can turn its head about 270 degrees?", "The owl", listOf("The eagle", "The parrot", "The crow"), A, QuizDifficulty.Easy),
        ask("zuk_sila", "Relative to its weight, which animal is the strongest, pulling over 1,000 times its own mass?", "The dung beetle", listOf("The ant", "The gorilla", "The elephant"), A, QuizDifficulty.Hard),
        ask("osmiornica_krew", "What colour is an octopus's blood?", "Blue", listOf("Red", "Green", "Clear"), A, QuizDifficulty.Medium),
        ask("homar_wzrost", "Which animal keeps growing for its whole life?", "The lobster", listOf("The cat", "The eagle", "The gorilla"), A, QuizDifficulty.Medium),
        ask("nietoperz_lot", "Which is the only mammal capable of true, powered flight?", "The bat", listOf("The flying squirrel", "The sugar glider", "The colugo"), A, QuizDifficulty.Easy),
        ask("kaczor_nazwa", "What is a male duck called?", "A drake", listOf("A gander", "A cob", "A buck"), A, QuizDifficulty.Medium),
        ask("zyrafa_noworodek", "Which of these newborns can usually stand and walk within an hour of birth?", "A giraffe calf", listOf("A kitten", "A kangaroo joey", "A human baby"), A, QuizDifficulty.Medium),
        ask("wielblad_powieka", "What is a camel's transparent third eyelid for?", "Wiping sand off the eye while still seeing", listOf("Sleeping with its eyes open", "Seeing in the dark", "Keeping the eye warm at night"), A, QuizDifficulty.Medium),
        ask("pies_pot", "Where does a dog mostly sweat from?", "Its paw pads", listOf("Its tongue", "Its back", "Its ears"), A, QuizDifficulty.Medium),
        ask("koza_zrenice", "Which animal has rectangular, horizontal pupils?", "The goat", listOf("The cat", "The owl", "The frog"), A, QuizDifficulty.Medium),
        ask("zaba_mroz", "Which animal can survive being frozen solid through the winter?", "The wood frog", listOf("The garden snail", "The brown bear", "The garter snake"), A, QuizDifficulty.Hard),
        ask("homar_nogi", "How many legs does a lobster have, counting its claws?", "Ten", listOf("Six", "Eight", "Twelve"), A, QuizDifficulty.Hard),
        ask("opos_torbacz", "Which is the only marsupial native to North America?", "The Virginia opossum", listOf("The raccoon", "The armadillo", "The porcupine"), A, QuizDifficulty.Hard),
        ask("konik_morski_ryba", "Which of these sea creatures is actually a fish?", "The seahorse", listOf("The jellyfish", "The starfish", "The cuttlefish"), A, QuizDifficulty.Medium),
        ask("konik_morski_ciaza", "In which animal does the male carry the developing young?", "The seahorse", listOf("The penguin", "The crocodile", "The frog"), A, QuizDifficulty.Medium),
        ask("mrowka_krolowa", "How long can a queen ant of some species live?", "Over 20 years", listOf("About a month", "About a year", "About 3 years"), A, QuizDifficulty.Hard),
        ask("slon_skok", "Which is the only mammal that cannot jump?", "The elephant", listOf("The hippopotamus", "The rhinoceros", "The cow"), A, QuizDifficulty.Medium),
        ask("krowa_przyjaciolki", "Which farm animal has been shown to form close friendships and get stressed when separated?", "The cow", listOf("The goose", "The goat", "The chicken"), A, QuizDifficulty.Hard),
        ask("pingwin_polnoc", "Where do penguins live in the wild?", "Only in the Southern Hemisphere", listOf("Only in the Northern Hemisphere", "At both poles", "Only in Antarctica"), A, QuizDifficulty.Medium),
        ask("kolibr_serce", "Which bird's heart can beat more than 1,000 times per minute?", "The hummingbird", listOf("The sparrow", "The pigeon", "The swift"), A, QuizDifficulty.Medium),
        ask("wombat_kupa", "Which animal produces cube-shaped droppings?", "The wombat", listOf("The koala", "The rabbit", "The beaver"), A, QuizDifficulty.Hard),
        ask("zebra_paski", "What is the leading explanation for a zebra's stripes?", "They confuse biting flies", listOf("They keep the zebra warm", "They attract mates", "They help it hide in grass"), A, QuizDifficulty.Hard),
        ask("pszczola_taniec", "How do honeybees tell each other where flowers are?", "With a 'waggle dance'", listOf("By buzzing at different pitches", "By leaving a trail of wax", "By flashing their wings"), A, QuizDifficulty.Medium),

        /* ================================ Biology ================================ */
        ask("kosc_najmniejsza", "What is the smallest bone in the human body?", "The stapes, in the ear", listOf("The fibula", "The hyoid", "A finger bone"), B, QuizDifficulty.Medium),
        ask("miesien_najsilniejszy", "Which muscle exerts the most force for its size?", "The masseter (jaw muscle)", listOf("The biceps", "The calf muscle", "The heart"), B, QuizDifficulty.Hard),
        ask("witamina_krzepniecie", "Which vitamin is essential for blood clotting?", "Vitamin K", listOf("Vitamin C", "Vitamin A", "Vitamin B12"), B, QuizDifficulty.Hard),
        ask("serce_dziennie", "Roughly how many times does the human heart beat in a day?", "About 100,000", listOf("About 10,000", "About 1,000,000", "About 25,000"), B, QuizDifficulty.Medium),
        ask("melanina", "Which pigment gives skin, hair and eyes their colour?", "Melanin", listOf("Keratin", "Hemoglobin", "Carotene"), B, QuizDifficulty.Easy),
        ask("krtan", "What is the medical name for the voice box?", "The larynx", listOf("The pharynx", "The trachea", "The epiglottis"), B, QuizDifficulty.Medium),
        ask("zebra_pary", "How many pairs of ribs does a typical adult have?", "12", listOf("10", "14", "8"), B, QuizDifficulty.Medium),
        ask("watroba_regeneracja", "Which organ can regrow most of itself after part of it is removed?", "The liver", listOf("The heart", "The lung", "The kidney"), B, QuizDifficulty.Medium),
        ask("erytrocyt_zycie", "How long does a red blood cell live, on average?", "About 120 days", listOf("About 12 days", "About a year", "About 5 years"), B, QuizDifficulty.Hard),
        ask("paznokcie_wzrost", "Which grow faster?", "Fingernails", listOf("Toenails", "They grow at the same rate", "It depends on the season only"), B, QuizDifficulty.Medium),
        ask("pecherzyki", "What are the tiny air sacs in the lungs called?", "Alveoli", listOf("Bronchi", "Villi", "Capillaries"), B, QuizDifficulty.Medium),
        ask("aorta", "What is the largest artery in the human body?", "The aorta", listOf("The carotid", "The femoral artery", "The pulmonary artery"), B, QuizDifficulty.Easy),
        ask("zoladek_kwas", "Why doesn't stomach acid digest the stomach itself?", "A layer of mucus protects the stomach wall", listOf("The acid is too weak", "The stomach wall is made of bone", "The acid only works on meat"), B, QuizDifficulty.Medium),
        ask("mrugniecie", "Roughly how many times does a person blink per minute while awake?", "About 15-20", listOf("About 2-3", "About 50", "About 100"), B, QuizDifficulty.Hard),
        ask("dna_dlugosc", "If you stretched out the DNA from a single human cell, roughly how long would it be?", "About 2 metres", listOf("About 2 millimetres", "About 2 centimetres", "About 200 metres"), B, QuizDifficulty.Hard),
        ask("nerki_funkcja", "What is the main job of the kidneys?", "Filtering waste from the blood", listOf("Producing bile for digestion", "Storing sugar for later use", "Pumping lymph round the body"), B, QuizDifficulty.Easy),
        ask("kichniecie", "What really happens if you try to sneeze with your eyes open?", "Nothing bad - the eyes just close by reflex", listOf("The eyes could be forced out of their sockets", "The sneeze comes out much weaker", "You cannot sneeze at all that way"), B, QuizDifficulty.Hard),
        ask("goraczka_cel", "What is the purpose of a fever?", "Making the body a worse place for germs", listOf("Cooling the brain during an illness", "Burning off the body's spare fat", "Speeding up digestion while ill"), B, QuizDifficulty.Medium),
        ask("mleko_laktoza", "What is lactose?", "The sugar in milk", listOf("The fat in milk", "The protein in milk", "A milk vitamin"), B, QuizDifficulty.Easy),
        ask("kofeina_dziala", "How does caffeine keep you awake?", "It blocks the brain's 'sleepy' signal, adenosine", listOf("It raises the level of sugar in the blood", "It cools the brain by a fraction", "It thickens the blood a little"), B, QuizDifficulty.Hard),
        ask("sluch_kosci", "Where are the three smallest bones in your body?", "In the middle ear", listOf("In the wrist", "In the toes", "Behind the eye"), B, QuizDifficulty.Medium),
        ask("skora_odnowa", "Roughly how often does the outer layer of your skin fully replace itself?", "About once a month", listOf("Once a day", "Once a year", "Once every 7 years"), B, QuizDifficulty.Hard),
        ask("odruch_kolanowy", "Where does the signal for the knee-jerk reflex get processed?", "In the spinal cord", listOf("In the brain's frontal lobe", "In the knee itself", "In the cerebellum"), B, QuizDifficulty.Hard),
        ask("gluten_co", "What is gluten?", "A protein found in wheat", listOf("A sugar found in wheat", "A fat found in bread", "A preservative added to flour"), B, QuizDifficulty.Easy),
        ask("bakterie_jelita", "Which organ hosts most of the body's helpful bacteria?", "The large intestine", listOf("The stomach", "The liver", "The lungs"), B, QuizDifficulty.Medium),

        /* ================================ Science ================================ */
        ask("wenus_dzien", "Which planet has a day that lasts longer than its year?", "Venus", listOf("Mercury", "Mars", "Jupiter"), S, QuizDifficulty.Hard),
        ask("ciezar_planety", "On which of these planets would you weigh the most?", "Jupiter", listOf("Mars", "Venus", "Earth"), S, QuizDifficulty.Medium),
        ask("uran_bok", "Which planet spins on its side, with its axis nearly in the plane of its orbit?", "Uranus", listOf("Neptune", "Saturn", "Mars"), S, QuizDifficulty.Medium),
        ask("proxima", "What is the nearest star to Earth after the Sun?", "Proxima Centauri", listOf("Sirius", "Betelgeuse", "Polaris"), S, QuizDifficulty.Medium),
        ask("dzwiek_najszybciej", "Through which of these does sound travel fastest?", "Steel", listOf("Water", "Air", "A vacuum"), S, QuizDifficulty.Medium),
        ask("mars_atmosfera", "Which gas makes up most of the atmosphere of Mars?", "Carbon dioxide", listOf("Nitrogen", "Oxygen", "Methane"), S, QuizDifficulty.Hard),
        ask("swiatlo_predkosc", "Roughly how fast does light travel?", "300,000 km per second", listOf("300,000 km per hour", "3,000 km per second", "30 million km per second"), S, QuizDifficulty.Easy),
        ask("minus_40", "At what temperature do the Celsius and Fahrenheit scales show the same number?", "-40 degrees", listOf("0 degrees", "-20 degrees", "100 degrees"), S, QuizDifficulty.Medium),
        ask("everest_wrzenie", "Roughly at what temperature does water boil on the summit of Mount Everest?", "About 70 C", listOf("About 100 C", "About 90 C", "About 50 C"), S, QuizDifficulty.Hard),
        ask("lakmus_kwas", "What colour does blue litmus paper turn in an acid?", "Red", listOf("Green", "Yellow", "It stays blue"), S, QuizDifficulty.Easy),
        ask("elektron_ladunek", "Which subatomic particle carries a negative charge?", "The electron", listOf("The proton", "The neutron", "The photon"), S, QuizDifficulty.Easy),
        ask("rok_swietlny", "What does a light-year measure?", "Distance", listOf("Time", "Brightness", "Speed"), S, QuizDifficulty.Easy),
        ask("przewodnik_najlepszy", "Which metal conducts electricity best?", "Silver", listOf("Copper", "Gold", "Aluminium"), S, QuizDifficulty.Hard),
        ask("dna_rozwiniecie", "What does DNA stand for?", "Deoxyribonucleic acid", listOf("Dinucleic acid", "Deoxyribose nitrogen acid", "Double nucleic arrangement"), S, QuizDifficulty.Medium),
        ask("zero_absolutne", "What is absolute zero, roughly, in Celsius?", "-273 C", listOf("-100 C", "-459 C", "-1000 C"), S, QuizDifficulty.Medium),
        ask("tecza_ksztalt", "What shape is a full rainbow when seen from an aircraft?", "A complete circle", listOf("A half-circle", "A straight band", "A spiral"), S, QuizDifficulty.Medium),
        ask("nie_promieniowanie", "Which of these is NOT electromagnetic radiation?", "Sound waves", listOf("X-rays", "Radio waves", "Visible light"), S, QuizDifficulty.Easy),
        ask("slonce_swiatlo_minuty", "How long does sunlight take to reach Earth?", "About 8 minutes", listOf("About 8 seconds", "About 8 hours", "About 1 day"), S, QuizDifficulty.Medium),
        ask("olympus_mons", "On which planet is Olympus Mons, the tallest known volcano in the Solar System?", "Mars", listOf("Venus", "Earth", "Jupiter's moon Io"), S, QuizDifficulty.Medium),
        ask("ksiezyc_oddala", "What is happening to the distance between the Earth and the Moon?", "It grows by a few centimetres a year", listOf("It shrinks by a few centimetres a year", "It never changes", "It changes only during eclipses"), S, QuizDifficulty.Hard),
        ask("helium_glos", "Why does inhaling helium make your voice sound higher?", "Sound travels faster in helium than in air", listOf("Helium tightens the vocal cords", "Helium is lighter than the voice", "Helium heats the throat"), S, QuizDifficulty.Hard),
        ask("mikrofala_dziala", "How does a microwave oven heat food?", "It makes water molecules vibrate", listOf("It heats the air inside", "It uses infrared light", "It passes electricity through the food"), S, QuizDifficulty.Medium),
        ask("piorun_grzmot", "Why do you see lightning before you hear thunder?", "Light travels much faster than sound", listOf("Thunder happens later than lightning", "The ears react slower than the eyes", "Clouds block the sound at first"), S, QuizDifficulty.Easy),
        ask("saturn_plywa", "Which planet is less dense than water and would float, in theory, in a big enough bathtub?", "Saturn", listOf("Jupiter", "Neptune", "Mars"), S, QuizDifficulty.Hard),
        ask("neutron_gwiazda", "A teaspoon of material from which object would weigh billions of tonnes?", "A neutron star", listOf("The Sun", "A white dwarf", "Jupiter's core"), S, QuizDifficulty.Hard),
        ask("stal_co", "What is steel?", "Iron with a little carbon", listOf("Pure iron", "Iron mixed with copper", "A natural mineral"), S, QuizDifficulty.Easy),
        ask("rdza_co", "What is rust?", "Iron combined with oxygen", listOf("Iron combined with carbon", "Dirt stuck to metal", "Iron that has melted and cooled"), S, QuizDifficulty.Easy),
        ask("balon_wysokosc", "Why does a helium balloon rise?", "Helium is lighter than the surrounding air", listOf("Helium is hot", "The rubber pushes it up", "Air pressure pulls it upward"), S, QuizDifficulty.Easy),
        ask("ziemia_wiek", "How old is the Earth, approximately?", "About 4.5 billion years", listOf("About 4.5 million years", "About 450 million years", "About 45 billion years"), S, QuizDifficulty.Medium),
        ask("dzien_dluzszy", "What is slowly happening to the length of a day on Earth?", "It is getting longer", listOf("It is getting shorter", "It stays exactly the same", "It changes with the seasons only"), S, QuizDifficulty.Hard),

        /* =============================== Geography ================================ */
        ask("strefy_czasowe", "Which country spans the most time zones, counting its overseas territories?", "France", listOf("Russia", "The United States", "China"), G, QuizDifficulty.Hard),
        ask("kontynent_wulkany", "Which continent's mainland has no active volcanoes?", "Australia", listOf("Europe", "Antarctica", "North America"), G, QuizDifficulty.Hard),
        ask("stambul_kontynenty", "Which city lies on two continents?", "Istanbul", listOf("Cairo", "Moscow", "Athens"), G, QuizDifficulty.Easy),
        ask("amazonka_ocean", "Into which ocean does the Amazon River flow?", "The Atlantic", listOf("The Pacific", "The Indian Ocean", "The Caribbean Sea"), G, QuizDifficulty.Easy),
        ask("pustynia_zimna", "Counting cold deserts, which is the largest desert on Earth?", "Antarctica", listOf("The Sahara", "The Gobi", "The Arabian Desert"), G, QuizDifficulty.Medium),
        ask("etiopia_kolonia", "Which African country was never colonised by a European power?", "Ethiopia", listOf("Kenya", "Ghana", "Algeria"), G, QuizDifficulty.Medium),
        ask("nepal_flaga", "Which country's flag is not a rectangle?", "Nepal", listOf("Bhutan", "Switzerland", "Vatican City"), G, QuizDifficulty.Medium),
        ask("dunaj_kraje", "Which river flows through the most countries?", "The Danube", listOf("The Rhine", "The Nile", "The Amazon"), G, QuizDifficulty.Hard),
        ask("malediwy_plaskie", "Which is the flattest country in the world?", "The Maldives", listOf("The Netherlands", "Denmark", "Bangladesh"), G, QuizDifficulty.Hard),
        ask("sargassowe", "Which sea has no coastline at all, being bounded only by ocean currents?", "The Sargasso Sea", listOf("The Caspian Sea", "The Coral Sea", "The Tasman Sea"), G, QuizDifficulty.Hard),
        ask("alaska", "Which US state is the largest by area?", "Alaska", listOf("Texas", "California", "Montana"), G, QuizDifficulty.Easy),
        ask("rpa_stolice", "Which country has three capital cities?", "South Africa", listOf("Brazil", "Australia", "Switzerland"), G, QuizDifficulty.Hard),
        ask("dunaj_nie", "Through which of these countries does the Danube NOT flow?", "Poland", listOf("Austria", "Hungary", "Germany"), G, QuizDifficulty.Easy),
        ask("ukraina_europa", "Which is the largest country lying entirely within Europe?", "Ukraine", listOf("France", "Spain", "Germany"), G, QuizDifficulty.Hard),
        ask("mongolia_morze", "Which of these countries has no access to the sea?", "Mongolia", listOf("Vietnam", "Chile", "Morocco"), G, QuizDifficulty.Easy),
        ask("islandia_lodowce", "Which country gets most of its electricity from geothermal and hydro power?", "Iceland", listOf("Ireland", "Belgium", "Greece"), G, QuizDifficulty.Medium),
        ask("mumbai_dawniej", "Which city was formerly known as Bombay?", "Mumbai", listOf("Kolkata", "Chennai", "Delhi"), G, QuizDifficulty.Easy),
        ask("stambul_dawniej", "Which city was once called Constantinople?", "Istanbul", listOf("Athens", "Ankara", "Sofia"), G, QuizDifficulty.Easy),
        ask("himalaje_kraje", "Mount Everest lies on the border of which two countries?", "Nepal and China", listOf("India and Nepal", "Nepal and Bhutan", "India and China"), G, QuizDifficulty.Medium),
        ask("hawaje_stan", "Which US state is made up entirely of islands?", "Hawaii", listOf("Florida", "Alaska", "Rhode Island"), G, QuizDifficulty.Easy),
        ask("wielki_kanion", "In which US state is the Grand Canyon?", "Arizona", listOf("Nevada", "Utah", "Colorado"), G, QuizDifficulty.Medium),
        ask("morze_martwe_lezy", "Which is the lowest point on land?", "The shore of the Dead Sea", listOf("Death Valley", "The Caspian shore", "Lake Eyre"), G, QuizDifficulty.Medium),
        ask("panama_ocean", "The Panama Canal connects the Pacific with which body of water?", "The Atlantic (via the Caribbean)", listOf("The Indian Ocean", "The Gulf of California", "The Amazon"), G, QuizDifficulty.Easy),
        ask("australia_stolica_nie", "Which of these is NOT the capital of its country?", "Sydney", listOf("Canberra", "Wellington", "Ottawa"), G, QuizDifficulty.Easy),
        ask("madagaskar_kontynent", "Off the coast of which continent is Madagascar?", "Africa", listOf("Asia", "Australia", "South America"), G, QuizDifficulty.Easy),

        /* ================================ History ================================ */
        ask("cud_stoi", "Which of the Seven Wonders of the Ancient World still stands?", "The Great Pyramid of Giza", listOf("The Colossus of Rhodes", "The Hanging Gardens of Babylon", "The Lighthouse of Alexandria"), H, QuizDifficulty.Easy),
        ask("curie_nobel", "Who was the first woman to win a Nobel Prize?", "Marie Curie", listOf("Rosalind Franklin", "Lise Meitner", "Florence Nightingale"), H, QuizDifficulty.Easy),
        ask("machu_picchu", "Which civilisation built Machu Picchu?", "The Inca", listOf("The Aztecs", "The Maya", "The Olmecs"), H, QuizDifficulty.Easy),
        ask("rosetta", "What did the Rosetta Stone help scholars decipher?", "Egyptian hieroglyphs", listOf("Cuneiform", "Linear B", "The Maya script"), H, QuizDifficulty.Medium),
        ask("statua_wolnosci", "Which country gave the Statue of Liberty to the United States?", "France", listOf("The United Kingdom", "Italy", "Spain"), H, QuizDifficulty.Easy),
        ask("bizancjum_stolica", "Which city was the capital of the Byzantine Empire?", "Constantinople", listOf("Athens", "Rome", "Alexandria"), H, QuizDifficulty.Medium),
        ask("hadrian_mur", "In which present-day country is Hadrian's Wall?", "The United Kingdom", listOf("Germany", "Italy", "France"), H, QuizDifficulty.Medium),
        ask("magna_carta", "What was the Magna Carta of 1215?", "A charter limiting the English king's power", listOf("A peace treaty signed with the king of France", "A map of the whole of the known world", "A list of the laws of the Church"), H, QuizDifficulty.Medium),
        ask("papier_chiny", "Which civilisation invented paper?", "Ancient China", listOf("Ancient Egypt", "Ancient Greece", "The Maya"), H, QuizDifficulty.Easy),
        ask("kleopatra_czas", "Which event is closer in time to Cleopatra's reign?", "The Moon landing", listOf("The building of the Great Pyramid", "They are equally far", "The founding of Rome"), H, QuizDifficulty.Hard),
        ask("nowa_zelandia_glos", "Which country was the first to give all women the right to vote, in 1893?", "New Zealand", listOf("The United States", "The United Kingdom", "Finland"), H, QuizDifficulty.Medium),
        ask("manifest", "Who wrote 'The Communist Manifesto'?", "Karl Marx and Friedrich Engels", listOf("Vladimir Lenin, while in exile", "Leon Trotsky and his circle", "Rosa Luxemburg and her party"), H, QuizDifficulty.Medium),
        ask("manhattan", "What was the Manhattan Project?", "The US programme to build the atomic bomb", listOf("The building of New York's subway", "A plan to land on the Moon", "A secret code-breaking effort"), H, QuizDifficulty.Medium),
        ask("hatszepsut", "Which Egyptian queen ruled as pharaoh and built the temple at Deir el-Bahari?", "Hatshepsut", listOf("Nefertiti", "Cleopatra", "Nefertari"), H, QuizDifficulty.Hard),
        ask("sulejman", "Which empire did Suleiman the Magnificent rule?", "The Ottoman Empire", listOf("The Mughal Empire", "The Persian Empire", "The Byzantine Empire"), H, QuizDifficulty.Medium),
        ask("londyn_pozar", "In which year was the Great Fire of London?", "1666", listOf("1566", "1766", "1616"), H, QuizDifficulty.Medium),
        ask("franklin_banknot", "Whose face is on the US $100 bill, even though he was never president?", "Benjamin Franklin's", listOf("Thomas Jefferson's", "Ulysses Grant's", "Abraham Lincoln's"), H, QuizDifficulty.Hard),
        ask("oxford_aztekowie", "Which is older?", "The University of Oxford", listOf("The Aztec Empire", "The Inca Empire", "The Ottoman Empire"), H, QuizDifficulty.Hard),
        ask("wojna_najkrotsza", "The shortest war in history, between Britain and Zanzibar in 1896, lasted about how long?", "About 40 minutes", listOf("About 4 hours", "About 4 days", "About 4 weeks"), H, QuizDifficulty.Hard),
        ask("tutanchamon_odkrycie", "Who discovered Tutankhamun's tomb in 1922?", "Howard Carter", listOf("Heinrich Schliemann", "Flinders Petrie", "Jean-Francois Champollion"), H, QuizDifficulty.Hard),
        ask("pompeje_odkopanie", "Roughly how long was Pompeii buried before it was rediscovered?", "About 1,700 years", listOf("About 200 years", "About 700 years", "About 3,000 years"), H, QuizDifficulty.Medium),
        ask("wielki_mur_ksiezyc", "Is the Great Wall of China visible to the naked eye from the Moon?", "No, that is a myth", listOf("Yes, clearly", "Only at night", "Only from the far side"), H, QuizDifficulty.Medium),
        ask("wikingowie_helmy", "Did Viking warriors really wear horned helmets?", "No, that is a 19th-century invention", listOf("Yes, in every battle they fought", "Only the chieftains wore them", "Only during the winter raids"), H, QuizDifficulty.Medium),
        ask("czyngis_potomkowie", "Which ruler is thought to have millions of living descendants today?", "Genghis Khan", listOf("Julius Caesar", "Charlemagne", "Alexander the Great"), H, QuizDifficulty.Medium),
        ask("pierwsza_uni", "Which is the oldest continuously operating university in the world?", "The University of Bologna", listOf("The University of Oxford", "The Sorbonne", "Harvard University"), H, QuizDifficulty.Hard),
        ask("kolumb_gdzie", "Where did Columbus first land in 1492?", "In the Bahamas", listOf("In Florida", "In Mexico", "In Brazil"), H, QuizDifficulty.Medium),
        ask("wojna_secesji_lata", "In which decade was the American Civil War fought?", "The 1860s", listOf("The 1840s", "The 1880s", "The 1770s"), H, QuizDifficulty.Medium),
        ask("dinozaury_ludzie", "Did humans and dinosaurs ever live at the same time?", "No, dinosaurs died out some 65 million years earlier", listOf("Yes, but only for a very brief period at the end", "Yes, for several thousand years", "Only on the continent of Australia"), H, QuizDifficulty.Easy),
        ask("faraon_ostatni", "Who was the last active pharaoh of ancient Egypt?", "Cleopatra VII", listOf("Tutankhamun", "Ramesses II", "Nefertiti"), H, QuizDifficulty.Medium),
        ask("dzuma_szczury", "The Black Death was spread mainly by fleas living on which animal?", "Rats", listOf("Dogs", "Cats", "Pigeons"), H, QuizDifficulty.Easy),
        ask("konstantyn_chrzescijanstwo", "Which Roman emperor was the first to convert to Christianity?", "Constantine the Great", listOf("Nero", "Augustus", "Marcus Aurelius"), H, QuizDifficulty.Hard),
    )
}
