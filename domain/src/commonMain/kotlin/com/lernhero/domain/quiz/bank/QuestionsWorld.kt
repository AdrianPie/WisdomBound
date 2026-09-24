package com.lernhero.domain.quiz.bank

import com.lernhero.domain.quiz.QuizCategory
import com.lernhero.domain.quiz.QuizDifficulty
import com.lernhero.domain.quiz.QuizEntry

/**
 * Duza paczka "sensownych" pytan: wiedza, ktora przydaje sie poza gra (jak dziala cialo, swiat,
 * technika), podana tak, zeby pytanie samo w sobie bylo ciekawe. Bledne odpowiedzi sa z tej samej
 * polki co poprawna - nie da sie trafic, odrzucajac absurdy.
 */
object QuestionsWorld {

    private val E = QuizDifficulty.Easy
    private val M = QuizDifficulty.Medium
    private val H = QuizDifficulty.Hard
    private val X = QuizDifficulty.Expert

    private fun q(id: String, prompt: String, correct: String, wrong: List<String>, category: QuizCategory, difficulty: QuizDifficulty) =
        QuizEntry(id = "wld_$id", prompt = prompt, correct = correct, wrong = wrong, category = category, difficulty = difficulty)

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

    val all: List<QuizEntry> = listOf(
        /* ------------------------------ Biology ------------------------------ */
        q("krew_grupa_dawca", "Which blood type is known as the universal donor for red cells?", "O negative", listOf("AB positive", "A negative", "B positive"), Bio, M),
        q("najmniejsza_kosc", "Where in the body is the smallest bone?", "In the middle ear", listOf("In the little toe", "In the wrist", "In the nose"), Bio, M),
        q("czerwone_krwinki_jadro", "What do mature human red blood cells lack?", "A nucleus", listOf("A membrane", "Haemoglobin", "Water"), Bio, H),
        q("fotosynteza_gaz", "Which gas do plants take in to carry out photosynthesis?", "Carbon dioxide", listOf("Oxygen", "Nitrogen", "Hydrogen"), Bio, E),
        q("mitochondria", "Which part of a cell is often called its 'powerhouse'?", "The mitochondrion", listOf("The nucleus", "The ribosome", "The cell wall"), Bio, E),
        q("szkliwo", "What is the hardest substance in the human body?", "Tooth enamel", listOf("Thigh bone", "Fingernail keratin", "Skull bone"), Bio, M),
        q("watroba_regeneracja", "Which human organ can regrow to full size after part of it is removed?", "The liver", listOf("The heart", "The pancreas", "The kidney"), Bio, M),
        q("dna_zasady", "Which base is found in RNA but not in DNA?", "Uracil", listOf("Thymine", "Adenine", "Guanine"), Bio, X),
        q("antybiotyki_wirusy", "Why don't antibiotics cure the common cold?", "Colds are caused by viruses", listOf("The cold is too mild", "Antibiotics only work on fungi", "The body blocks them in winter"), Bio, M),
        q("mozg_energia", "The brain is about 2% of body weight. Roughly what share of the body's energy does it use?", "About 20%", listOf("About 2%", "About 50%", "About 5%"), Bio, H),

        /* ------------------------------ Animals ------------------------------ */
        q("sokol_nurkowanie", "Which bird is the fastest animal on Earth when diving?", "The peregrine falcon", listOf("The golden eagle", "The swift", "The albatross"), Ani, M),
        q("delfin_sen", "How do dolphins sleep without drowning?", "One half of the brain sleeps at a time", listOf("They float on their backs", "They sleep on the seabed", "They don't sleep at all"), Ani, H),
        q("pingwin_biegun", "At which pole do wild penguins live?", "The South Pole region", listOf("The North Pole region", "Both poles", "Neither - only in the tropics"), Ani, E),
        q("pajak_owad", "Why is a spider not an insect?", "It has eight legs and two body parts", listOf("It cannot fly and has no wings", "It spins silk instead of wax", "It has no antennae on its head"), Ani, E),
        q("slonie_pamiec", "Which land mammal has the longest pregnancy, about 22 months?", "The elephant", listOf("The rhinoceros", "The giraffe", "The blue whale"), Ani, M),
        q("pszczola_taniec", "How does a honeybee tell others where to find flowers?", "With a 'waggle dance'", listOf("By buzzing a tune", "By leaving a scent map", "By touching antennae only"), Ani, M),
        q("kameleon_kolor", "Chameleons change colour mainly to do what?", "Signal mood and control temperature", listOf("Hide from every predator", "Match any background exactly", "Attract prey"), Ani, H),
        q("sowa_glowa", "About how far can an owl turn its head?", "About 270 degrees", listOf("About 90 degrees", "A full 360 degrees", "About 180 degrees"), Ani, M),
        q("koala_dieta", "What makes up almost the entire diet of a koala?", "Eucalyptus leaves", listOf("Bamboo shoots", "Insects", "Acacia seeds"), Ani, E),
        q("aksolotl_regeneracja", "Which amphibian can regrow lost limbs and even parts of its heart?", "The axolotl", listOf("The common frog", "The cane toad", "The fire salamander"), Ani, H),

        /* ------------------------------ History ------------------------------ */
        q("magna_carta", "Which English king sealed the Magna Carta in 1215?", "King John", listOf("Richard the Lionheart", "Henry VIII", "William the Conqueror"), His, H),
        q("kolumb_statki", "How many ships did Columbus take on his first voyage in 1492?", "Three", listOf("Five", "One", "Twelve"), His, M),
        q("druk_gutenberg", "Around which year did Gutenberg start printing with movable type in Europe?", "1450", listOf("1250", "1650", "1350"), His, M),
        q("pierwsza_wojna_start", "The assassination of which man sparked the First World War?", "Archduke Franz Ferdinand", listOf("Tsar Nicholas II", "Kaiser Wilhelm II", "Otto von Bismarck"), His, M),
        q("rzym_koniec", "In which year is the Western Roman Empire usually said to have fallen?", "476", listOf("1453", "44 BC", "800"), His, H),
        q("dzuma", "The Black Death of the 1340s was most likely which disease?", "Bubonic plague", listOf("Smallpox", "Cholera", "Leprosy"), His, M),
        q("hammurabi", "Hammurabi is famous for one of the oldest written what?", "Law codes", listOf("Poems", "Maps", "Calendars"), His, H),
        q("rewolucja_francuska", "The French Revolution began with the storming of which prison?", "The Bastille", listOf("The Tower", "Alcatraz", "The Conciergerie"), His, M),

        /* ------------------------------ Elements ----------------------------- */
        q("hel_balon", "Which gas makes party balloons float?", "Helium", listOf("Hydrogen", "Neon", "Oxygen"), Ele, E),
        q("diament_wegiel", "Diamonds are made of which element?", "Carbon", listOf("Silicon", "Quartz", "Boron"), Ele, E),
        q("sol_kuchenna", "Table salt is a compound of sodium and which element?", "Chlorine", listOf("Calcium", "Sulphur", "Iodine"), Ele, E),
        q("rdza", "Rust forms when iron reacts with water and what?", "Oxygen", listOf("Nitrogen", "Carbon dioxide", "Hydrogen"), Ele, E),
        q("neon_reklamy", "Classic red glowing signs got their name from which gas?", "Neon", listOf("Argon", "Xenon", "Krypton"), Ele, E),
        q("uran_elektrownia", "Which element fuels most nuclear power stations?", "Uranium", listOf("Plutonium", "Thorium", "Radium"), Ele, M),
        q("krzem_chip", "Computer chips are mostly built from which element?", "Silicon", listOf("Carbon", "Germanium", "Copper"), Ele, M),
        q("fluor_zeby", "Which element is added to toothpaste to strengthen enamel?", "Fluorine", listOf("Chlorine", "Calcium", "Zinc"), Ele, M),
        q("ozon", "Ozone is a molecule made of three atoms of what?", "Oxygen", listOf("Nitrogen", "Carbon", "Hydrogen"), Ele, M),
        q("lit_baterie", "Which light metal powers most phone batteries?", "Lithium", listOf("Sodium", "Magnesium", "Nickel"), Ele, M),

        /* ------------------------------ Capitals ----------------------------- */
        q("szwajcaria_stolica", "Which city is the seat of Switzerland's government?", "Bern", listOf("Zurich", "Geneva", "Basel"), Cap, M),

        /* ------------------------------ Science ------------------------------ */
        q("dzwiek_proznia", "Why can't sound travel through space?", "There is no air to carry the vibrations", listOf("Space is far too cold for waves", "Sunlight drowns out all vibrations", "Gravity is too weak to hold it"), Sci, E),
        q("niebo_niebieskie", "Why is the daytime sky blue?", "Air scatters blue light more than red", listOf("It reflects the colour of oceans", "Space behind the air is blue", "The ozone layer is coloured blue"), Sci, M),
        q("woda_wrzenie_gory", "Why does water boil at a lower temperature on a high mountain?", "The air pressure is lower", listOf("The air is colder", "There is less oxygen", "The water is purer"), Sci, M),
        q("ksiezyc_strona", "Why do we always see the same side of the Moon?", "It spins once for every orbit of Earth", listOf("It doesn't spin at all", "The other side is always dark", "Earth's shadow hides it"), Sci, H),
        q("przyplywy", "What mainly causes ocean tides?", "The Moon's gravity", listOf("Wind", "Earth's rotation alone", "Underwater volcanoes"), Sci, E),
        q("tecza_kolory", "How many colours are traditionally named in a rainbow?", "Seven", listOf("Five", "Six", "Nine"), Sci, E),
        q("atom_jadro", "What sits in the centre of an atom?", "The nucleus", listOf("The electron", "The photon", "A vacuum"), Sci, E),
        q("czarna_dziura", "Why is a black hole black?", "Not even light can escape its gravity", listOf("It is made of dark matter", "It is very cold", "It absorbs only visible colours"), Sci, M),
        q("zero_bezwzgledne", "Absolute zero is roughly which temperature?", "-273 °C", listOf("-100 °C", "-459 °C", "0 °C"), Sci, H),

        /* ------------------------------ Geography ---------------------------- */
        q("everest_kraje", "Mount Everest sits on the border of Nepal and which other country?", "China", listOf("India", "Bhutan", "Pakistan"), Geo, M),
        q("najwieksza_wyspa", "What is the largest island in the world (not counting Australia)?", "Greenland", listOf("New Guinea", "Borneo", "Madagascar"), Geo, M),
        q("morze_martwe", "Why is it easy to float in the Dead Sea?", "Its water is extremely salty", listOf("It is very warm", "It has strong currents", "It is very shallow"), Geo, E),
        q("rownik_kraje", "Which of these countries does the Equator pass through?", "Ecuador", listOf("Mexico", "Egypt", "India"), Geo, E),
        q("sahara_rozmiar", "The Sahara is roughly the size of which country?", "The United States", listOf("France", "Germany", "Japan"), Geo, H),
        q("najglebsze_jezioro", "Which is the deepest lake in the world?", "Lake Baikal", listOf("Lake Superior", "Lake Tanganyika", "Lake Victoria"), Geo, M),
        q("kanal_panamski", "The Panama Canal connects which two oceans?", "The Atlantic and the Pacific", listOf("The Atlantic and the Indian", "The Pacific and the Arctic", "The Indian and the Pacific"), Geo, E),
        q("islandia_ogien", "Iceland is known as the land of fire and what?", "Ice", listOf("Wind", "Stone", "Rain"), Geo, E),
        q("kontynenty", "How many continents are usually counted in English-speaking schools?", "Seven", listOf("Five", "Six", "Eight"), Geo, E),
        q("japonia_wyspy", "Roughly how many islands make up Japan?", "Over 14,000", listOf("About 4", "About 70", "About 400"), Geo, X),

        /* ------------------------------- Sport ------------------------------- */
        q("olimpiada_kola", "How many rings are on the Olympic flag?", "Five", listOf("Four", "Six", "Seven"), Spo, E),
        q("szachy_krol", "In chess, what is it called when the king is attacked and cannot escape?", "Checkmate", listOf("Stalemate", "Castling", "En passant"), Spo, E),
        q("golf_par", "In golf, what is one stroke under par on a hole called?", "A birdie", listOf("A bogey", "An eagle", "An albatross"), Spo, M),
        q("tenis_wielki_szlem", "Which of these is NOT a tennis Grand Slam tournament?", "The Davis Cup", listOf("Wimbledon", "The US Open", "Roland Garros"), Spo, M),
        q("sumo_kraj", "Sumo wrestling is the national sport of which country?", "Japan", listOf("China", "Mongolia", "Korea"), Spo, E),
        q("rugby_ksztalt", "What shape is a rugby ball?", "An oval", listOf("A sphere", "A disc", "A cylinder"), Spo, E),
        q("bolt_rekord", "Usain Bolt's 100 m world record is closest to which time?", "9.58 seconds", listOf("9.85 seconds", "10.02 seconds", "9.28 seconds"), Spo, H),
        q("hokej_krazek", "What is the ball in ice hockey called?", "A puck", listOf("A disc", "A stone", "A chip"), Spo, E),

        /* -------------------------------- Math ------------------------------- */
        q("pi_przyblizenie", "Pi is closest to which value?", "3.14", listOf("3.41", "2.71", "3.00"), Mat, E),
        q("liczba_pierwsza_97", "Which of these is a prime number?", "97", listOf("91", "87", "93"), Mat, H),
        q("procent_zniżka", "A jacket costs 80 and is 25% off. What is the new price?", "60", listOf("55", "65", "75"), Mat, E),
        q("pitagoras_3_4", "A right triangle has legs of 3 and 4. How long is its longest side?", "5", listOf("6", "7", "12"), Mat, M),
        q("potega_zero", "What is any non-zero number raised to the power of 0?", "1", listOf("0", "The number itself", "Undefined"), Mat, M),
        q("srednia", "What is the average of 4, 8 and 12?", "8", listOf("6", "10", "24"), Mat, E),
        q("rzymskie_mcm", "What year is written as MCMXC in Roman numerals?", "1990", listOf("1910", "2090", "1490"), Mat, H),
        q("fibonacci", "What is the next number: 1, 1, 2, 3, 5, 8, ...?", "13", listOf("11", "12", "15"), Mat, M),
        q("kwadrat_przekatne", "How many diagonals does a square have?", "2", listOf("4", "1", "0"), Mat, E),

        /* ----------------------------- Literature ---------------------------- */
        q("romeo_miasto", "In which Italian city is 'Romeo and Juliet' set?", "Verona", listOf("Venice", "Florence", "Rome"), Lit, M),
        q("orwell_1984", "Who wrote '1984'?", "George Orwell", listOf("Aldous Huxley", "Ray Bradbury", "H. G. Wells"), Lit, E),
        q("maly_ksiaze_planeta", "In 'The Little Prince', what does the prince love on his tiny planet?", "A rose", listOf("A fox", "A baobab", "A lamplighter"), Lit, M),
        q("moby_dick_kapitan", "Which captain hunts the white whale in 'Moby-Dick'?", "Captain Ahab", listOf("Captain Nemo", "Captain Hook", "Captain Flint"), Lit, M),
        q("sherlock_adres", "At which London address does Sherlock Holmes live?", "221B Baker Street", listOf("10 Downing Street", "221B Oxford Street", "42 Wallaby Way"), Lit, M),
        q("odyseja_powrot", "In Homer's 'Odyssey', which island is Odysseus trying to reach?", "Ithaca", listOf("Crete", "Troy", "Sparta"), Lit, M),
        q("tolkien_pierscien", "In 'The Lord of the Rings', where must the One Ring be destroyed?", "Mount Doom", listOf("Rivendell", "Helm's Deep", "Minas Tirith"), Lit, E),
        q("alicja_krolik", "What does Alice follow down the hole in 'Alice's Adventures in Wonderland'?", "A white rabbit", listOf("A black cat", "A talking caterpillar", "A playing card"), Lit, E),
        q("wojna_i_pokoj", "Who wrote 'War and Peace'?", "Leo Tolstoy", listOf("Fyodor Dostoevsky", "Anton Chekhov", "Ivan Turgenev"), Lit, M),
        q("kafka_przemiana", "In Kafka's 'The Metamorphosis', what does Gregor Samsa wake up as?", "A giant insect", listOf("A wolf", "A statue", "An old man"), Lit, H),

        /* ----------------------------- Technology ---------------------------- */
        q("cpu_skrot", "What does CPU stand for?", "Central Processing Unit", listOf("Computer Power Unit", "Central Program Utility", "Core Processing Unit"), Tec, E),
        q("url_skrot", "What is the address you type into a browser called?", "A URL", listOf("A CPU", "A USB", "An API"), Tec, E),
        q("gps_satelity", "How does a phone's GPS find your position?", "By timing signals from satellites", listOf("By asking nearby Wi-Fi only", "By measuring the Earth's magnetism", "By reading cell towers' names"), Tec, M),
        q("ram_skrot", "What happens to data in RAM when a computer loses power?", "It is lost", listOf("It is saved automatically", "It moves to the CPU", "It is sent to the cloud"), Tec, M),
        q("haslo_bezpieczne", "Which password is hardest to crack?", "A long random phrase of several words", listOf("Your name plus 123", "A single dictionary word", "Your birthday"), Tec, E),
        q("open_source", "What does 'open source' software mean?", "Its code is public and can be changed", listOf("It is always free of bugs", "It only runs online", "It was made by a government"), Tec, M),
        q("bluetooth_nazwa", "Bluetooth is named after a king of which country?", "Denmark", listOf("Norway", "Sweden", "England"), Tec, H),
        q("pixel", "What is a 'pixel'?", "The smallest dot of an image on a screen", listOf("The smallest unit of sound in a file", "A tiny battery cell inside a screen", "A short program that draws images"), Tec, E),

        /* ----------------------------- Mythology ----------------------------- */
        q("zeus_bron", "What weapon does Zeus throw?", "Thunderbolts", listOf("A trident", "A golden spear", "Arrows of fire"), Myt, E),
        q("posejdon_trojzab", "Which Greek god carries a trident?", "Poseidon", listOf("Hades", "Ares", "Apollo"), Myt, E),
        q("achilles_pieta", "Where was Achilles vulnerable?", "His heel", listOf("His neck", "His heart", "His back"), Myt, E),
        q("meduza_wzrok", "What happened to anyone who looked at Medusa?", "They turned to stone", listOf("They went blind", "They fell asleep forever", "They forgot who they were"), Myt, E),
        q("odyn_oko", "What did Odin give up to drink from the well of wisdom?", "One of his eyes", listOf("His hammer", "His horse", "His voice"), Myt, M),
        q("ikar", "Why did Icarus fall into the sea?", "He flew too close to the Sun and his wax wings melted", listOf("Zeus struck him down with a thunderbolt", "His father cut the ropes holding his wings", "A sudden storm tore his feathered wings apart"), Myt, E),
        q("trojanski_kon", "How did the Greeks finally get inside Troy?", "Hidden inside a wooden horse", listOf("Through a secret tunnel", "By climbing the walls at night", "Disguised as merchants"), Myt, E),
        q("ra_bog", "Ra was the Egyptian god of what?", "The Sun", listOf("The Nile", "The dead", "War"), Myt, E),
        q("valhalla", "In Norse myth, where do warriors who die in battle go?", "Valhalla", listOf("Asgard's gate", "Niflheim", "Olympus"), Myt, M),
        q("herkules_prace", "How many labours did Heracles have to complete?", "Twelve", listOf("Seven", "Ten", "Nine"), Myt, M),
        q("atena_symbol", "Which bird is the symbol of Athena?", "The owl", listOf("The eagle", "The peacock", "The dove"), Myt, M),
        q("feniks", "What does a phoenix do at the end of its life?", "Bursts into flames and is reborn from the ashes", listOf("Rises into the sky and becomes a star", "Falls asleep in a cave for a thousand years", "Sheds its feathers and becomes a dragon"), Myt, E),
        /* ------------------------- Druga porcja: mniej oczywiste ------------------------- */
        q("bio_ziewanie", "Which of these is thought to be one purpose of yawning?", "Cooling the brain", listOf("Refilling the stomach with air", "Cleaning the ears", "Resetting the heartbeat"), Bio, H),
        q("bio_gesia_skorka", "Goosebumps are a leftover reflex that once did what for our ancestors?", "Fluffed up body hair", listOf("Released a warning smell", "Toughened the skin", "Stopped bleeding"), Bio, H),
        q("bio_smak_nos", "Why does food taste bland when you have a blocked nose?", "Most flavour comes from smell", listOf("The tongue swells up", "Mucus coats the taste buds completely", "Fever switches off taste"), Bio, M),
        q("bio_wlosy_rosna", "Roughly how fast does hair on your head grow?", "About 1 cm a month", listOf("About 1 mm a month", "About 5 cm a month", "About 1 cm a year"), Bio, H),
        q("bio_zoladek_nowy", "How often does the lining of your stomach renew itself?", "Every few days", listOf("Every few years", "Once in a lifetime", "Every hour"), Bio, X),
        q("bio_krew_kolor", "Why is blood red?", "Iron-containing haemoglobin", listOf("Copper in the plasma", "Red white blood cells", "Oxygen gas bubbles"), Bio, M),

        q("ani_ptaki_dinozaury", "Which living animals are the direct descendants of dinosaurs?", "Birds", listOf("Crocodiles", "Lizards", "Sharks"), Ani, M),
        q("ani_mrowki_waga", "An ant can carry roughly how many times its own body weight?", "Around 10-50 times", listOf("About 2 times", "About 1,000 times", "Less than its own weight"), Ani, H),
        q("ani_ryba_pila", "Which fish uses a long saw-toothed snout to slash at prey?", "The sawfish", listOf("The swordfish", "The marlin", "The needlefish"), Ani, M),
        q("ani_zebra_paski", "Scientists think zebra stripes most likely help to do what?", "Keep biting flies away", listOf("Attract mates from far away", "Store heat at night", "Help them swim"), Ani, X),
        q("ani_slimak_zeby", "Which of these has thousands of tiny teeth on a ribbon-like tongue?", "A garden snail", listOf("A jellyfish", "A starfish", "An earthworm"), Ani, H),
        q("ani_leniwiec", "How often does a sloth usually climb down from its tree to go to the toilet?", "About once a week", listOf("Several times a day", "Once a year", "Never - it goes from the branches"), Ani, H),
        q("ani_orka", "The orca, or 'killer whale', actually belongs to which family?", "Dolphins", listOf("Baleen whales", "Sharks", "Seals"), Ani, M),

        q("his_piramidy_budowniczowie", "Who is now believed to have built the Egyptian pyramids?", "Paid skilled workers and labourers", listOf("Slaves captured in war", "Greek architects", "Roman engineers"), His, H),
        q("his_wikingowie_ameryka", "Which Viking reached North America around the year 1000?", "Leif Erikson", listOf("Erik the Red", "Ragnar Lothbrok", "Harald Hardrada"), His, H),
        q("his_konstantynopol", "Constantinople fell to the Ottomans in which year?", "1453", listOf("1204", "1683", "1071"), His, H),
        q("his_zimna_wojna", "Which two superpowers faced each other in the Cold War?", "The USA and the Soviet Union", listOf("Britain and Germany", "China and Japan", "France and the USA"), His, E),
        q("his_marco_polo", "Which Venetian traveller told Europe about China's Kublai Khan?", "Marco Polo", listOf("Vasco da Gama", "Ferdinand Magellan", "Christopher Columbus"), His, M),
        q("his_magellan", "Whose expedition was the first to sail all the way around the world?", "Ferdinand Magellan's", listOf("Christopher Columbus's", "James Cook's", "Francis Drake's"), His, M),
        q("his_mur_chinski_cel", "The Great Wall of China was mainly built to do what?", "Keep out raiders from the north", listOf("Hold back floods", "Mark a trade route", "Protect the emperor's palace"), His, E),
        q("his_kleopatra_jezyk", "Cleopatra VII was famous as the first of her dynasty to speak which language?", "Egyptian", listOf("Latin", "Greek", "Persian"), His, X),

        q("ele_tlen_atmosfera", "Roughly what share of the air we breathe is oxygen?", "About 21%", listOf("About 78%", "About 50%", "About 1%"), Ele, M),
        q("ele_wapn_kosci", "Which element is especially important for strong bones and teeth?", "Calcium", listOf("Sodium", "Potassium", "Iron"), Ele, E),
        q("ele_zelazo_krew", "Lack of which element in the diet can cause anaemia?", "Iron", listOf("Zinc", "Iodine", "Calcium"), Ele, M),
        q("ele_platyna", "Which precious metal is used in car catalytic converters?", "Platinum", listOf("Silver", "Gold", "Titanium"), Ele, H),
        q("ele_tytan_implanty", "Which strong, light metal is often used for hip implants?", "Titanium", listOf("Lead", "Copper", "Tin"), Ele, M),

        q("cap_new_york", "Is New York the capital of the United States?", "No - it's Washington, D.C.", listOf("Yes, since 1776", "No - it's Philadelphia", "No - it's Boston"), Cap, E),

        q("sci_magnes_biegun", "What happens when two north poles of magnets meet?", "They push each other away", listOf("They stick together", "They cancel out", "They heat up"), Sci, E),
        q("sci_mars_kolor", "Why is Mars red?", "Its soil is rich in iron oxide - rust", listOf("It is extremely hot", "It reflects the Sun's red light", "Its atmosphere is red gas"), Sci, M),
        q("sci_iss_predkosc", "About how long does the International Space Station take to circle Earth?", "About 90 minutes", listOf("About 24 hours", "About 1 week", "About 10 minutes"), Sci, H),
        q("sci_dzwiek_woda", "Does sound travel faster in water or in air?", "In water", listOf("In air", "At the same speed", "Sound can't travel in water"), Sci, M),

        q("geo_australia_kontynent", "Which is the only country that is also a whole continent?", "Australia", listOf("Greenland", "Antarctica", "Russia"), Geo, E),
        q("geo_nil_kierunek", "In which direction does the Nile flow?", "North", listOf("South", "East", "West"), Geo, M),
        q("geo_alpy_szczyt", "What is the highest mountain in the Alps?", "Mont Blanc", listOf("The Matterhorn", "The Eiger", "Zugspitze"), Geo, M),
        q("geo_wulkan_etna", "Mount Etna, one of the most active volcanoes, is on which island?", "Sicily", listOf("Sardinia", "Crete", "Iceland"), Geo, M),
        q("geo_fiordy", "Which country is most famous for its fjords?", "Norway", listOf("Netherlands", "Portugal", "Hungary"), Geo, E),
        q("geo_kraje_bez_morza", "Which of these countries has no coastline?", "Switzerland", listOf("Portugal", "Croatia", "Belgium"), Geo, E),
        q("geo_najwyzszy_wodospad", "Where is Angel Falls, the world's highest waterfall?", "Venezuela", listOf("Brazil", "Canada", "Zambia"), Geo, H),
        q("geo_himalaje_rosna", "The Himalayas are still growing because of what?", "Two tectonic plates pushing together", listOf("Volcanoes erupting underneath them", "Snow and ice piling up on top", "Rivers dropping heavy sediment"), Geo, M),

        q("spo_pilka_druzyna", "How many players does a football team have on the pitch?", "11", listOf("10", "9", "12"), Spo, E),
        q("spo_boks_rundy", "How long is a standard round in professional boxing?", "3 minutes", listOf("1 minute", "5 minutes", "10 minutes"), Spo, M),
        q("spo_kregle", "How many pins stand at the end of a ten-pin bowling lane?", "10", listOf("9", "12", "8"), Spo, E),
        q("spo_olimpiada_zima", "In which winter sport do players sweep the ice in front of a sliding stone?", "Curling", listOf("Biathlon", "Skeleton", "Bobsleigh"), Spo, E),
        q("spo_triathlon", "Which three sports make up a triathlon?", "Swimming, cycling, running", listOf("Running, rowing, cycling", "Swimming, running, shooting", "Cycling, skiing, running"), Spo, E),
        q("spo_mundial_czesto", "Which country has won the FIFA World Cup the most times?", "Brazil", listOf("Germany", "Italy", "Argentina"), Spo, M),

        q("mat_polowa_polowy", "What is half of a half?", "A quarter", listOf("A third", "One", "An eighth"), Mat, E),
        q("mat_zegar_kat", "What angle do a clock's hands make at 3 o'clock?", "90 degrees", listOf("45 degrees", "180 degrees", "30 degrees"), Mat, E),
        q("mat_milion_sekund", "Roughly how long is one million seconds?", "About 11.5 days", listOf("About 1 year", "About 11 hours", "About 3 months"), Mat, H),
        q("mat_miliard_sekund", "Roughly how long is one billion seconds?", "About 31.7 years", listOf("About 3 years", "About 317 years", "About 1 year"), Mat, X),
        q("mat_kwadrat_12", "What is 12 squared?", "144", listOf("124", "121", "132"), Mat, E),
        q("mat_procent_procent", "A price rises by 10% and then falls by 10%. Compared with the start, it is...", "1% lower", listOf("Exactly the same", "1% higher", "10% lower"), Mat, H),
        q("mat_szachy_ziarna", "On a chessboard you put 1 grain on the first square and double it on each next one. How many grains on the 10th square?", "512", listOf("1024", "100", "20"), Mat, H),

        q("lit_szekspir_hamlet", "Which Shakespeare character says 'To be, or not to be'?", "Hamlet", listOf("Macbeth", "King Lear", "Othello"), Lit, E),
        q("lit_frankenstein_potwor", "In Mary Shelley's novel, who is 'Frankenstein'?", "The scientist who creates the creature", listOf("The monster he builds from parts", "The mayor of a Swiss village", "The creature's intended bride"), Lit, M),
        q("lit_piotrus_pan", "In which land do Peter Pan and the Lost Boys live?", "Neverland", listOf("Narnia", "Wonderland", "Oz"), Lit, E),
        q("lit_czarnoksieznik_oz", "In 'The Wizard of Oz', what road does Dorothy follow?", "The yellow brick road", listOf("The silver road", "The emerald path", "The rainbow road"), Lit, E),
        q("lit_dracula_autor", "Who wrote the novel 'Dracula'?", "Bram Stoker", listOf("Mary Shelley", "Edgar Allan Poe", "Oscar Wilde"), Lit, M),
        q("lit_narnia_wejscie", "How do the children first enter Narnia?", "Through a wardrobe", listOf("Through a mirror", "Down a rabbit hole", "Through a painting"), Lit, E),
        q("lit_guliwer", "In 'Gulliver's Travels', what are the Lilliputians like?", "Tiny people", listOf("Giants", "Talking horses", "Flying scientists"), Lit, M),

        q("tec_telefon_bell", "Who is usually credited with patenting the telephone in 1876?", "Alexander Graham Bell", listOf("Thomas Edison", "Nikola Tesla", "Guglielmo Marconi"), Tec, M),
        q("tec_zarowka", "Which inventor made the first practical long-lasting light bulb famous in 1879?", "Thomas Edison", listOf("Nikola Tesla", "Benjamin Franklin", "James Watt"), Tec, M),
        q("tec_wifi_fale", "What kind of waves does Wi-Fi use?", "Radio waves", listOf("Sound waves", "X-rays", "Infrared only"), Tec, M),
        q("tec_qr", "What does the 'QR' in QR code stand for?", "Quick Response", listOf("Quality Record", "Query Reader", "Quad Resolution"), Tec, H),
        q("tec_emoji_kraj", "In which country were emoji invented?", "Japan", listOf("The USA", "South Korea", "Finland"), Tec, M),
        q("tec_ai_skrot", "What does 'AI' stand for?", "Artificial Intelligence", listOf("Automatic Input", "Advanced Internet", "Analog Interface"), Tec, E),
        q("tec_backup", "What is a 'backup'?", "A spare copy of your files", listOf("A faster internet plan", "A type of antivirus", "A reverse camera"), Tec, E),

        q("myt_hades", "Who rules the underworld in Greek myth?", "Hades", listOf("Hermes", "Ares", "Kronos"), Myt, E),
        q("myt_cerber", "How many heads does Cerberus, the guard dog of the underworld, have?", "Three", listOf("Two", "Seven", "Nine"), Myt, E),
        q("myt_cyklop", "What is special about a Cyclops?", "It has one eye", listOf("It has three heads", "It can fly", "It is made of stone"), Myt, E),
        q("myt_loki", "Which Norse god is famous as a shape-shifting trickster?", "Loki", listOf("Tyr", "Baldr", "Heimdall"), Myt, E),
        q("myt_narcyz", "Narcissus fell in love with what?", "His own reflection", listOf("A river nymph", "The goddess of the Moon", "A golden statue"), Myt, M),
        q("myt_ozyrys", "Which Egyptian god was killed by his brother and became lord of the dead?", "Osiris", listOf("Horus", "Anubis", "Thoth"), Myt, H)
    )
}
