package com.lernhero.domain.quiz.bank

import com.lernhero.domain.quiz.QuizCategory
import com.lernhero.domain.quiz.QuizDifficulty
import com.lernhero.domain.quiz.QuizEntry

/**
 * GLEBOKOSC banku: po czternascie pytan Hard/Expert na KAZDY temat z [QuizCategory.builtin].
 *
 * Bank byl dotad szeroki, ale plytki. Dopoki endless losowal po calym banku, nie mialo to
 * znaczenia - dopiero tryb opanowania tematu ([com.lernhero.domain.progress.CategoryMastery])
 * sprawil, ze gracz siada na JEDNYM temacie i przechodzi w nim dwadziescia fal, a od poziomu 5
 * startuje juz na Hard, od 7 na Expert. Kategoria, ktora miala 23 pytania eksperckie (Sport),
 * wyczerpywala je w jednym wejsciu i zaczynala schodzic nizej - i gracz, ktory wlasnie awansowal
 * temat na "Expert", dostawal za to pytania coraz LATWIEJSZE. Awans dzialal na odwrot.
 *
 * Stad rozklad tej partii: zero pytan latwych i srednich, za to rowno w kazdym temacie. Nie jest
 * to "jeszcze jedna paczka ciekawostek" - to dolozenie sufitu tam, gdzie drabinka o niego zaczela
 * uderzac. Pierwsza taka partia to [TriviaQuestionsMastery]; ta jest jej dokonczeniem, tyle ze
 * rozlozonym po tematach po rowno, a nie tam, gdzie akurat bylo o czym pisac.
 *
 * Dwie zasady tresci, ktore rozni to od zwyklego dosypania pytan:
 *
 * 1. **Zaden dystraktor nie jest wypelniaczem.** Kazda bledna odpowiedz jest rzecza, ktora
 *    naprawde istnieje i naprawde daloby sie z nia pomylic - inaczej pytanie eksperckie jest
 *    trudne tylko z nazwy.
 * 2. **Dlugosc odpowiedzi nie zdradza poprawnej** - patrz test "poprawna odpowiedz nie wyroznia
 *    sie sama dlugoscia" w QuizBankAuditTest. Dluga, rozwlekla odpowiedz wsrod trzech urwanych
 *    to najtansza sciaga w kazdym quizie i bank byl jej pelen.
 */
object MasteryDepthQuestions {

    private fun ask(
        id: String,
        prompt: String,
        correct: String,
        wrong: List<String>,
        category: QuizCategory,
        difficulty: QuizDifficulty
    ) = QuizEntry(
        id = "dpt_$id",
        prompt = prompt,
        correct = correct,
        wrong = wrong,
        category = category,
        difficulty = difficulty
    )

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
    private val Pol = QuizCategory.Poland

    private val H = QuizDifficulty.Hard
    private val X = QuizDifficulty.Expert

    val all: List<QuizEntry> = listOf(

        /* ================================= Biologia ================================= */
        ask("bio_mejoza_roznica", "What happens in meiosis that never happens in mitosis?", "Chromosomes swap sections", listOf("The nucleus dissolves fully", "The cell copies its own DNA", "Two daughter cells appear"), Bio, H),
        ask("bio_szorstka_siateczka", "What does the rough endoplasmic reticulum do?", "Builds and folds proteins", listOf("Breaks down worn organelles", "Stores the cell's calcium", "Copies the cell's own DNA"), Bio, H),
        ask("bio_karotenoidy", "Which pigments turn leaves yellow once chlorophyll fades?", "Carotenoids", listOf("Anthocyanins", "Phycocyanins", "Melanins"), Bio, H),
        ask("bio_hayflick", "What does the Hayflick limit describe?", "How often a normal cell can divide", listOf("How long an enzyme stays active", "How fast a nerve signal travels", "How much oxygen blood carries"), Bio, X),
        ask("bio_trna", "Which molecule brings amino acids to the ribosome?", "Transfer RNA", listOf("Messenger RNA", "Ribosomal RNA", "DNA polymerase"), Bio, X),
        ask("bio_crispr_guide", "What does CRISPR-Cas9 use to find its target in DNA?", "A guide RNA sequence", listOf("A matching protein tag", "A methyl group marker", "An antibody-like probe"), Bio, X),
        ask("bio_endosymbioza", "What does endosymbiotic theory explain?", "Where mitochondria came from", listOf("How viruses infect bacteria", "How one species splits in two", "How enzymes settled on shape"), Bio, X),
        ask("bio_hox", "What do Hox genes set up in an embryo?", "The body plan along its main axis", listOf("The number of chromosomes in it", "The rate of cell respiration in it", "The colour of its first tissues"), Bio, X),
        ask("bio_chemiosmoza", "What is chemiosmosis in a cell?", "ATP made from a proton gradient", listOf("Sugar broken down into acids", "Water pulled across a membrane", "Gases swapped inside the lungs"), Bio, X),
        ask("bio_helikaza", "Which enzyme unwinds the DNA double helix before copying?", "Helicase", listOf("Ligase", "Primase", "Telomerase"), Bio, X),
        ask("bio_beztlenowiec", "What does 'obligate anaerobe' mean?", "It dies where there is oxygen", listOf("It needs oxygen to survive", "It lives with or without air", "It grows only in salt water"), Bio, X),

        /* ================================ Zwierzeta ================================= */
        ask("zwi_osmiornica_kolor", "How does an octopus change colour?", "Pigment sacs in its skin flex open", listOf("It grows fresh coloured scales", "It reflects light with its blood", "It borrows colour from the sand"), Ani, H),
        ask("zwi_krokodyl_zgryz", "Which living animal has the strongest measured bite?", "The saltwater crocodile", listOf("The great white shark", "The spotted hyena", "The Bengal tiger"), Ani, H),
        ask("zwi_rekin_pole", "Which organs let sharks sense electric fields?", "The ampullae of Lorenzini", listOf("The lateral line canals", "The nictitating membranes", "The spiracles behind the eyes"), Ani, H),
        ask("zwi_pingwin_nurek", "How long can an emperor penguin stay down on one dive?", "Over twenty minutes", listOf("About two minutes", "About five minutes", "Almost two hours"), Ani, H),
        ask("zwi_kontrcien", "What is countershading in animals?", "Dark on top, pale below, to hide", listOf("Bright colours warning of poison", "Stripes that confuse a chasing eye", "A coat that changes with the season"), Ani, H),
        ask("zwi_slon_uszy", "How does an elephant shed excess heat?", "Through its huge, blood-rich ears", listOf("By sweating across its whole back", "By rolling in a bath of cool dust", "By slowing its heart in the heat"), Ani, X),
        ask("zwi_luskowiec", "What is a pangolin's armour made of?", "Keratin, like fingernails", listOf("Fused plates of bone", "Hardened plates of chitin", "Layers of calcified skin"), Ani, X),
        ask("zwi_gepard_limit", "Why must a cheetah break off a long chase?", "It overheats within a minute", listOf("Its claws wear down too fast", "It cannot breathe at full speed", "Its eyes lose focus at speed"), Ani, X),
        ask("zwi_wal_dziobowy", "Which mammal holds the record for the deepest dive?", "Cuvier's beaked whale", listOf("The sperm whale", "The southern right whale", "The leopard seal"), Ani, X),
        ask("zwi_dziobak_ostroga", "Which egg-laying mammal carries a venomous spur?", "The male platypus", listOf("The short-beaked echidna", "The marsupial mole", "The water opossum"), Ani, X),
        ask("zwi_koliber_serce", "What is unusual about a hummingbird at night?", "It drops into a torpor to save energy", listOf("It keeps flying while it is asleep", "It buries itself under leaf litter", "It shares a nest with other birds"), Ani, X),

        /* ================================= Historia ================================= */
        ask("his_vasco_da_gama", "Which sea route did Vasco da Gama open in 1498?", "Europe to India round Africa", listOf("Europe to China across the north", "Spain to the Caribbean islands", "Portugal to Brazil and back"), His, H),
        ask("his_mur_ming", "Which dynasty built most of the Great Wall as it stands today?", "The Ming dynasty", listOf("The Han dynasty", "The Qin dynasty", "The Tang dynasty"), His, H),
        ask("his_leningrad", "Which city endured a siege of 872 days in the Second World War?", "Leningrad", listOf("Stalingrad", "Sevastopol", "Budapest"), His, H),
        ask("his_defenestracja", "The Defenestration of Prague set off which war?", "The Thirty Years' War", listOf("The Hussite Wars", "The Seven Years' War", "The War of the Roses"), His, X),
        ask("his_kipu", "Which civilisation kept records on knotted cords called quipu?", "The Inca", listOf("The Maya", "The Aztecs", "The Toltecs"), His, X),

        /* =============================== Pierwiastki ================================ */
        ask("pie_wodor_slonce", "Which element makes up most of the Sun's mass?", "Hydrogen", listOf("Helium", "Carbon", "Neon"), Ele, H),
        ask("pie_liczba_atomowa", "What does an element's atomic number count?", "Protons in the nucleus", listOf("Neutrons in the nucleus", "Shells around the nucleus", "Isotopes of the element"), Ele, H),
        ask("pie_stop_definicja", "What is an alloy?", "A metal mixed with other elements", listOf("A metal heated until it glows", "A metal grown as one crystal", "A metal dissolved in strong acid"), Ele, H),
        ask("pie_krypton_nazwa", "Which noble gas is named from the Greek for 'hidden'?", "Krypton", listOf("Xenon", "Argon", "Radon"), Ele, X),
        ask("pie_promieniotworczosc", "What makes an element radioactive?", "An unstable atomic nucleus", listOf("A full outer electron shell", "Too many bonded neighbours", "A very high melting point"), Ele, X),
        ask("pie_itr_ytterby", "Four elements are named after one Swedish village - which is one of them?", "Yttrium", listOf("Scandium", "Thulium", "Lutetium"), Ele, X),
        ask("pie_lantanowce", "The lanthanides are better known under which name?", "The rare earth elements", listOf("The noble gases", "The alkali metals", "The transition metals"), Ele, X),
        ask("pie_wegiel_14", "Which isotope is used to date organic remains?", "Carbon-14", listOf("Uranium-238", "Potassium-40", "Argon-40"), Ele, X),
        ask("pie_gazy_szlachetne", "Why are the noble gases so unreactive?", "Their outer shell is already full", listOf("Their nuclei are unusually heavy", "They exist only as single atoms", "They have no electrons to share"), Ele, X),

        /* ================================= Stolice ================================== */
        ask("sto_nauru", "What is the capital of Nauru?", "Yaren", listOf("Denigomodu", "Aiwo", "Anibare"), Cap, H),
        ask("sto_tuvalu", "What is the capital of Tuvalu?", "Funafuti", listOf("Nanumea", "Vaitupu", "Nukufetau"), Cap, H),
        ask("sto_surinam", "What is the capital of Suriname?", "Paramaribo", listOf("Nieuw Nickerie", "Albina", "Lelydorp"), Cap, H),
        ask("sto_wyspy_marshalla", "What is the capital of the Marshall Islands?", "Majuro", listOf("Ebeye", "Jaluit", "Wotje"), Cap, H),
        ask("sto_salomona", "What is the capital of the Solomon Islands?", "Honiara", listOf("Gizo", "Auki", "Munda"), Cap, H),
        ask("sto_gwinea_rownikowa", "What is the capital of Equatorial Guinea?", "Malabo", listOf("Bata", "Ebebiyin", "Mongomo"), Cap, H),
        ask("sto_palau", "What is the capital of Palau?", "Ngerulmud", listOf("Koror", "Melekeok", "Airai"), Cap, X),
        ask("sto_kiribati", "What is the capital of Kiribati?", "South Tarawa", listOf("Betio", "Kiritimati", "Banaba"), Cap, X),
        ask("sto_togo", "What is the capital of Togo?", "Lome", listOf("Sokode", "Kara", "Kpalime"), Cap, X),
        ask("sto_benin", "What is the official capital of Benin?", "Porto-Novo", listOf("Cotonou", "Parakou", "Abomey"), Cap, X),
        ask("sto_czad", "What is the capital of Chad?", "N'Djamena", listOf("Moundou", "Abeche", "Sarh"), Cap, X),
        ask("sto_komory", "What is the capital of the Comoros?", "Moroni", listOf("Mutsamudu", "Fomboni", "Domoni"), Cap, X),
        ask("sto_vanuatu", "What is the capital of Vanuatu?", "Port Vila", listOf("Luganville", "Isangel", "Lakatoro"), Cap, X),

        /* ================================== Nauka =================================== */
        ask("nau_klatka_faradaya", "What does a Faraday cage do?", "Blocks outside electric fields", listOf("Stores charge for later use", "Turns current into light", "Cools a magnet towards zero"), Sci, H),
        ask("nau_coriolis", "What is the Coriolis effect?", "Motion deflected by Earth's spin", listOf("The Moon's pull on the oceans", "Light bent by layers of air", "Wind dragging waves along"), Sci, H),
        ask("nau_sily_jadro", "What holds an atomic nucleus together?", "The strong nuclear force", listOf("The electromagnetic force", "The weak nuclear force", "Gravity between the parts"), Sci, H),
        ask("nau_masa_ciezar", "How does weight differ from mass?", "Weight depends on local gravity", listOf("Mass depends on local gravity", "They are the very same thing", "Mass is measured in newtons"), Sci, H),
        ask("nau_slonce_fuzja", "What powers the Sun?", "Hydrogen fusing into helium", listOf("Hydrogen burning in oxygen", "Decay of heavy elements", "Friction deep in its core"), Sci, X),
        ask("nau_ciemna_materia", "How does dark matter reveal itself?", "By its pull on whole galaxies", listOf("By the faint light it gives off", "By the radio waves it absorbs", "By the heat it leaves in dust"), Sci, X),

        /* ================================ Geografia ================================= */
        ask("geo_morze_tasmana", "Which sea lies between Australia and New Zealand?", "The Tasman Sea", listOf("The Coral Sea", "The Timor Sea", "The Arafura Sea"), Geo, H),
        ask("geo_przesmyk", "What is an isthmus?", "A narrow neck of land between seas", listOf("A bay closed off by a sandbar", "A river mouth split into arms", "A ridge of rock below the water"), Geo, H),
        ask("geo_kolorado_kanion", "Which river carved the Grand Canyon?", "The Colorado", listOf("The Rio Grande", "The Snake River", "The Columbia"), Geo, H),
        ask("geo_fiord", "How does a fjord form?", "Ice carves a valley, sea floods it", listOf("A coral reef closes off a bay", "A landslide dams a river mouth", "A desert river cuts a canyon"), Geo, X),
        ask("geo_pierscien_ognia", "What is the Ring of Fire?", "A belt of volcanoes round the Pacific", listOf("A chain of deserts along the tropics", "A band of storms over the equator", "A line of reefs in the Indian Ocean"), Geo, X),
        ask("geo_cien_opadowy", "What causes a rain shadow?", "Mountains wring the moisture out of air", listOf("Cold water chills the coastal strip", "Forests soak up most of the rainfall", "Deserts heat the wind passing over"), Geo, X),
        ask("geo_zwrotnik_raka", "Which line marks the northern tropic?", "The Tropic of Cancer", listOf("The Tropic of Capricorn", "The Arctic Circle", "The Prime Meridian"), Geo, X),
        ask("geo_sahel", "What is the Sahel?", "The dry belt south of the Sahara", listOf("The delta of the river Niger", "The highlands of East Africa", "The coastal plain of Namibia"), Geo, X),
        ask("geo_chimborazo", "Whose summit lies farthest from the Earth's centre?", "Chimborazo", listOf("Everest", "Denali", "Kilimanjaro"), Geo, X),
        ask("geo_kaldera", "What is a caldera?", "A basin left by a collapsed volcano", listOf("A crater blasted by a meteorite", "A cone built up of cooled lava", "A vent that lets out only steam"), Geo, X),

        /* ================================== Sport =================================== */
        ask("spo_waterpolo", "How many players from one team are in the water in water polo?", "Seven", listOf("Five", "Six", "Nine"), Spo, H),
        ask("spo_davis", "In which sport is the Davis Cup contested?", "Tennis", listOf("Golf", "Rugby", "Sailing"), Spo, H),
        ask("spo_kosz_obrecz", "How wide is a basketball hoop?", "About 45 centimetres", listOf("About 30 centimetres", "About 60 centimetres", "About 75 centimetres"), Spo, H),
        ask("spo_nutmeg", "What is a 'nutmeg' in football?", "Putting the ball through an opponent's legs", listOf("A shot struck from the halfway line", "A tackle made from behind the player", "A pass hit with the outside of the boot"), Spo, X),
        ask("spo_fosbury", "In which event would you see a Fosbury flop?", "The high jump", listOf("The pole vault", "The long jump", "The triple jump"), Spo, X),
        ask("spo_indy_500", "How many laps make up the Indianapolis 500?", "200", listOf("100", "250", "500"), Spo, X),
        ask("spo_rugby_league", "How many players make up a rugby league team?", "Thirteen", listOf("Fifteen", "Eleven", "Twelve"), Spo, X),
        ask("spo_f1_flaga", "What does a black and white diagonal flag mean in Formula 1?", "A warning for unsporting driving", listOf("The driver must pit immediately", "The session has been suspended", "A faster car is about to lap you"), Spo, X),
        ask("spo_zmienny_sztafeta", "Which stroke opens a medley relay?", "Backstroke", listOf("Butterfly", "Breaststroke", "Freestyle"), Spo, X),

        /* =============================== Matematyka ================================= */
        ask("mat_pieciokat_katy", "What do the interior angles of a pentagon add up to?", "540 degrees", listOf("360 degrees", "450 degrees", "720 degrees"), Mat, H),
        ask("mat_pierwsze_20", "How many prime numbers are there below 20?", "Eight", listOf("Seven", "Nine", "Ten"), Mat, H),
        ask("mat_silnia_zero", "What is the value of zero factorial?", "One", listOf("Zero", "Undefined", "Infinity"), Mat, H),
        ask("mat_mediana", "What is the median of a data set?", "The middle value once sorted", listOf("The most frequent value in it", "The average of all the values", "The gap between the extremes"), Mat, H),
        ask("mat_suma_siedem", "What is the chance of two dice totalling seven?", "One in six", listOf("One in eight", "One in twelve", "One in thirty-six"), Mat, H),
        ask("mat_nachylenie", "What is the slope of a horizontal line?", "Zero", listOf("One", "Undefined", "Infinite"), Mat, H),
        ask("mat_pochodna", "What does a derivative measure?", "The rate at which a function changes", listOf("The area lying under a function", "The largest value a function takes", "The number of roots a function has"), Mat, X),
        ask("mat_przestepna", "What is a transcendental number?", "Not a root of any integer polynomial", listOf("A number with an exact square root", "A number that repeats after a period", "A number written as a simple fraction"), Mat, X),
        ask("mat_dwunastoscian", "How many faces does a regular dodecahedron have?", "Twelve", listOf("Ten", "Twenty", "Eight"), Mat, X),

        /* =============================== Literatura ================================= */
        ask("lit_carraway", "Which novel is narrated by Nick Carraway?", "The Great Gatsby", listOf("Tender Is the Night", "The Sun Also Rises", "Brideshead Revisited"), Lit, H),
        ask("lit_nowela", "What is a novella?", "A story between tale and novel", listOf("A play written to be read only", "A poem that tells a long story", "A set of essays on one theme"), Lit, H),
        ask("lit_achebe", "Who wrote 'Things Fall Apart'?", "Chinua Achebe", listOf("Wole Soyinka", "Ngugi wa Thiong'o", "Ben Okri"), Lit, H),
        ask("lit_narrator", "What is an unreliable narrator?", "One whose account cannot be trusted", listOf("One who never appears in the story", "One who speaks only about the past", "One who talks to the reader directly"), Lit, H),
        ask("lit_rama_narracyjna", "In literary criticism, what is a frame story?", "A tale that holds other tales", listOf("A tale told out of order", "A tale with two endings", "A tale without a narrator"), Lit, H),
        ask("lit_genji", "Who wrote 'The Tale of Genji', often called the first novel?", "Murasaki Shikibu", listOf("Sei Shonagon", "Matsuo Basho", "Yoshida Kenko"), Lit, X),
        ask("lit_strumien", "What is stream of consciousness writing?", "Thought set down as it runs", listOf("Dialogue written without quotes", "A story told out of sequence", "Prose broken into short verses"), Lit, X),
        ask("lit_epistolarna", "What is an epistolary novel?", "One told entirely in letters", listOf("One written without dialogue", "One told from the end back", "One published in weekly parts"), Lit, X),
        ask("lit_in_medias_res", "What does 'in medias res' mean?", "Starting in the middle of things", listOf("Ending without any resolution", "Told entirely through letters", "Written in the present tense"), Lit, X),
        ask("lit_bildungsroman", "What is a Bildungsroman?", "A novel about growing up", listOf("A novel written all in verse", "A novel with no main character", "A novel set on a single day"), Lit, X),
        ask("lit_kawabata", "Which Japanese writer won the Nobel Prize in 1968?", "Yasunari Kawabata", listOf("Yukio Mishima", "Kenzaburo Oe", "Junichiro Tanizaki"), Lit, X),
        ask("lit_pentametr", "What is iambic pentameter?", "Five beats of light-then-heavy stress", listOf("Ten rhyming lines placed in a row", "Five rhymes repeated in fixed order", "A line of exactly ten short words"), Lit, X),

        /* =============================== Technologia ================================ */
        ask("tec_wyscig", "What is a race condition in software?", "Two tasks clashing over timing", listOf("A loop that never terminates", "A memory block never freed", "A test that runs far too long"), Tec, H),
        ask("tec_suma_kontrolna", "What is a checksum for?", "Spotting that data has changed", listOf("Encrypting a file while at rest", "Shrinking a file before sending", "Sorting records by their key"), Tec, H),
        ask("tec_gpu_vs_cpu", "What does a GPU do better than a CPU?", "Many simple jobs at once", listOf("One complex job very fast", "Storing data permanently", "Running the whole system"), Tec, H),
        ask("tec_dwuskladnikowe", "What is two-factor authentication?", "Proving who you are in two ways", listOf("Two passwords for one account", "Logging in from two devices", "Encrypting the data twice over"), Tec, H),
        ask("tec_odsmiecanie", "What does garbage collection do in a runtime?", "Frees memory nothing points to", listOf("Deletes files never opened", "Clears the stored cookies", "Strips unused code at build"), Tec, X),
        ask("tec_notacja_o", "What does big O notation describe?", "How cost grows with input size", listOf("How much memory a chip holds", "How fast a network moves data", "How many bugs a program has"), Tec, X),
        ask("tec_przepelnienie_bufora", "What is a buffer overflow?", "Writing past the end of a memory block", listOf("Reading a file that does not exist", "Sending more packets than allowed", "Opening more windows than allowed"), Tec, X),
        ask("tec_arpanet", "What did ARPANET pioneer?", "Packet switching between computers", listOf("Wireless links between telephones", "Fibre optic cable across oceans", "Compression of digital video"), Tec, X),
        ask("tec_zero_day", "What is a zero-day vulnerability?", "A flaw with no fix available yet", listOf("A bug that takes a day to fix", "An attack lasting under a day", "A password that expires at once"), Tec, X),

        /* ================================ Mitologia ================================= */
        ask("mit_tyr_fenrir", "Which Norse god loses a hand to the wolf Fenrir?", "Tyr", listOf("Odin", "Baldr", "Heimdall"), Myt, H),
        ask("mit_sfinks", "Which Greek creature had a lion's body and a woman's head?", "The Sphinx", listOf("The Chimera", "The Harpy", "The Gorgon"), Myt, H),
        ask("mit_siwa", "Which Hindu god is known as the destroyer?", "Shiva", listOf("Vishnu", "Brahma", "Indra"), Myt, H),
        ask("mit_leta", "Which underworld river made the Greek dead forget?", "The Lethe", listOf("The Styx", "The Acheron", "The Cocytus"), Myt, X),
        ask("mit_kitsune", "What is a kitsune in Japanese folklore?", "A fox spirit that gains tails with age", listOf("A mountain demon with bright red skin", "A drowned soul that haunts the rivers", "A dragon set to guard a hidden shrine"), Myt, X),
        ask("mit_gilgamesz", "Which Mesopotamian epic follows a king seeking immortality?", "The Epic of Gilgamesh", listOf("The Enuma Elish", "The Book of the Dead", "The Song of Roland"), Myt, X),

        /* ================================== Polska ================================== */
        ask("pol_nihil_novi", "What did the Nihil Novi act of 1505 establish?", "No new law without the parliament", listOf("A ban on all foreign merchants", "A permanent tax for the king", "A truce with the Teutonic Order"), Pol, H),
        ask("pol_oscypek", "What is oscypek?", "A smoked sheep's cheese from the mountains", listOf("A sour rye soup served at Easter time", "A folk dance from the southern highlands", "A carved wooden figure of a shepherd"), Pol, H),
        ask("pol_wyspy_wroclaw", "Which Polish city is built across the most islands?", "Wroclaw", listOf("Szczecin", "Torun", "Bydgoszcz"), Pol, H),
        ask("pol_mazury", "What are the Masurian lands known for?", "A landscape full of lakes", listOf("A range of active volcanoes", "A vast inland salt desert", "The country's highest peaks"), Pol, H),
        ask("pol_konstytucja_maja", "What was the Constitution of 3 May 1791 meant to do?", "Reform the state before it was partitioned", listOf("End the uprising against the Russian army", "Found the republic after the First World War", "Create the country's first standing army"), Pol, X),
        ask("pol_rozbiory_panstwa", "Which three powers partitioned the country in the 18th century?", "Russia, Prussia and Austria", listOf("Russia, Sweden and Turkey", "Prussia, France and Austria", "Austria, Hungary and Russia"), Pol, X),
        ask("pol_nowa_huta", "What was Nowa Huta built as?", "A model workers' town by a steelworks", listOf("A fortress guarding the river crossing", "A spa resort in the southern mountains", "A port district added after the war"), Pol, X),
        ask("pol_bialowieza", "What makes the Bialowieza Forest exceptional?", "It is Europe's last primeval lowland forest", listOf("It is the largest planted pine forest in Europe", "It grows inside a canyon cut through limestone", "It holds the oldest vineyards north of the Alps"), Pol, X),
        ask("pol_sejm_izba", "What does the word 'sejm' name?", "The lower house of parliament", listOf("The council of city mayors", "The supreme court of appeal", "The office of the president"), Pol, X),
        ask("pol_uklad_warszawski", "What was the Warsaw Pact?", "A military alliance of the Eastern bloc", listOf("A treaty ending the Second World War", "An agreement fixing the western border", "A trade union of the Baltic harbours"), Pol, X)
    )
}
