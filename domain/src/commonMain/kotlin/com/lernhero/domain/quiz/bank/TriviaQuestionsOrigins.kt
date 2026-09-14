package com.lernhero.domain.quiz.bank

import com.lernhero.domain.quiz.QuizCategory
import com.lernhero.domain.quiz.QuizDifficulty
import com.lernhero.domain.quiz.QuizEntry

/**
 * Skad sie to wzielo: nazwy, slowa, marki i wynalazki.
 *
 * Cala partia trzyma sie jednej zasady z [TriviaQuestionsNature] - pytanie ma zostawiac po sobie
 * fakt, ktory chce sie komus powtorzyc ("Nokia zaczynala jako papiernia", "bubble wrap miala byc
 * tapeta"). Pochodzenie rzeczy nadaje sie do tego wyjatkowo dobrze, bo prawie zawsze konczy sie
 * zwrotem akcji: przedmiot powstal do czegos zupelnie innego, niz do czego sluzy dzisiaj.
 *
 * Dystraktory sa z tej samej polki co odpowiedz - przy pytaniu o firme stoja inne firmy z tej samej
 * epoki, przy pytaniu o slowo inne slowa tego samego rodzaju. Inaczej pytanie sprawdzaloby tylko,
 * czy gracz czyta.
 */
object TriviaQuestionsOrigins {

    private fun ask(
        id: String,
        prompt: String,
        correct: String,
        wrong: List<String>,
        category: QuizCategory,
        difficulty: QuizDifficulty
    ) = QuizEntry(
        id = "org_$id",
        prompt = prompt,
        correct = correct,
        wrong = wrong,
        category = category,
        difficulty = difficulty
    )

    private val T = QuizCategory.Technology
    private val L = QuizCategory.Literature
    private val H = QuizCategory.History
    private val M = QuizCategory.Mythology
    private val S = QuizCategory.Science

    private val E = QuizDifficulty.Easy
    private val D = QuizDifficulty.Medium
    private val R = QuizDifficulty.Hard
    private val X = QuizDifficulty.Expert

    val all: List<QuizEntry> = listOf(

        /* --------------------- Firmy, ktore zaczynaly inaczej --------------------- */
        ask(
            id = "nokia_papier",
            prompt = "Nokia began in 1865 as a mill producing what?",
            correct = "Paper",
            wrong = listOf("Rubber boots", "Copper wire", "Gunpowder"),
            category = T,
            difficulty = R
        ),
        ask(
            id = "google_backrub",
            prompt = "Before it was renamed, Google ran under what project name?",
            correct = "BackRub",
            wrong = listOf("PageRank", "WebCrawl", "Stanford Search"),
            category = T,
            difficulty = R
        ),
        ask(
            id = "amazon_ksiazki",
            prompt = "Amazon spent its first years selling almost nothing but what?",
            correct = "Books",
            wrong = listOf("Music CDs", "Computer parts", "Toys"),
            category = T,
            difficulty = E
        ),
        ask(
            id = "twitter_twttr",
            prompt = "Twitter launched under which vowel-free spelling of its name?",
            correct = "twttr",
            wrong = listOf("Chirp", "Jttr", "Status"),
            category = T,
            difficulty = R
        ),
        ask(
            id = "samsung_gwiazdy",
            prompt = "The name Samsung means what in Korean?",
            correct = "Three stars",
            wrong = listOf("Bright future", "Great river", "New dawn"),
            category = T,
            difficulty = R
        ),
        ask(
            id = "facebook_harvard",
            prompt = "Facebook was open only to students of which university at launch?",
            correct = "Harvard",
            wrong = listOf("Stanford", "MIT", "Yale"),
            category = T,
            difficulty = E
        ),
        ask(
            id = "lego_stolarz",
            prompt = "Before plastic bricks, the Lego workshop made toys out of what?",
            correct = "Wood",
            wrong = listOf("Tin", "Rubber", "Cardboard"),
            category = T,
            difficulty = D
        ),

        /* ------------------ Wynalazki, ktore mialy sluzyc do czegos innego ------------------ */
        ask(
            id = "bubble_tapeta",
            prompt = "Bubble wrap was first put on sale as which product?",
            correct = "Wallpaper",
            wrong = listOf("Greenhouse insulation", "A children's toy", "Mattress padding"),
            category = T,
            difficulty = R
        ),
        ask(
            id = "playdoh_czyscik",
            prompt = "Play-Doh was originally sold as a cleaner for what?",
            correct = "Wallpaper",
            wrong = listOf("Car upholstery", "Silverware", "Chalkboards"),
            category = T,
            difficulty = R
        ),
        ask(
            id = "cola_lek",
            prompt = "Coca-Cola was first sold at pharmacy counters as a remedy for what?",
            correct = "Headaches",
            wrong = listOf("Sore throats", "Insomnia", "Toothache"),
            category = H,
            difficulty = D
        ),
        ask(
            id = "mikrofalowka_batonik",
            prompt = "The microwave oven was discovered when an engineer noticed what melting in his pocket?",
            correct = "A chocolate bar",
            wrong = listOf("A plastic comb", "A wax crayon", "A rubber band"),
            category = T,
            difficulty = D
        ),
        ask(
            id = "rzep_ost",
            prompt = "Velcro was invented after its creator studied what stuck to his dog's fur?",
            correct = "Burrs",
            wrong = listOf("Pine resin", "Thistledown", "Wet clay"),
            category = T,
            difficulty = D
        ),
        ask(
            id = "slinky_sprezyna",
            prompt = "The Slinky toy came from an engineer's work on springs meant to steady what?",
            correct = "Ship instruments",
            wrong = listOf("Aircraft landing gear", "Railway carriages", "Factory lathes"),
            category = T,
            difficulty = R
        ),
        ask(
            id = "ebay_wskaznik",
            prompt = "The first item ever sold on eBay was a broken what?",
            correct = "Laser pointer",
            wrong = listOf("Wristwatch", "Camera lens", "Games console"),
            category = T,
            difficulty = R
        ),

        /* ------------------------- Kto byl pierwszy ------------------------- */
        ask(
            id = "faks_przed_telefonem",
            prompt = "Which of these was patented first?",
            correct = "The fax machine",
            wrong = listOf("The telephone", "The radio", "The light bulb"),
            category = T,
            difficulty = R
        ),
        ask(
            id = "motorola_komorka",
            prompt = "Which company placed the first handheld mobile phone call in 1973?",
            correct = "Motorola",
            wrong = listOf("Nokia", "Ericsson", "Bell Labs"),
            category = T,
            difficulty = D
        ),
        ask(
            id = "malpa_email",
            prompt = "Who picked the @ sign as the separator in email addresses?",
            correct = "Ray Tomlinson",
            wrong = listOf("Tim Berners-Lee", "Vint Cerf", "Linus Torvalds"),
            category = T,
            difficulty = R
        ),
        ask(
            id = "biro_dlugopis",
            prompt = "Hungarian journalist Laszlo Biro gave the world which writing tool?",
            correct = "The ballpoint pen",
            wrong = listOf("The fountain pen", "The mechanical pencil", "The felt-tip marker"),
            category = T,
            difficulty = D
        ),
        ask(
            id = "uran_teleskop",
            prompt = "Which planet was the first ever found with a telescope rather than the naked eye?",
            correct = "Uranus",
            wrong = listOf("Neptune", "Saturn", "Mercury"),
            category = S,
            difficulty = D
        ),

        /* --------------------------- Skad sie wzielo slowo --------------------------- */
        ask(
            id = "robot_capek",
            prompt = "The word 'robot' entered English from a 1920s play written in which language?",
            correct = "Czech",
            wrong = listOf("German", "Russian", "Hungarian"),
            category = L,
            difficulty = D
        ),
        ask(
            id = "nerd_seuss",
            prompt = "The word 'nerd' first appears in print in a book by which author?",
            correct = "Dr. Seuss",
            wrong = listOf("Roald Dahl", "A. A. Milne", "Lewis Carroll"),
            category = L,
            difficulty = R
        ),
        ask(
            id = "chortle_carroll",
            prompt = "Which author blended 'chuckle' and 'snort' to coin 'chortle'?",
            correct = "Lewis Carroll",
            wrong = listOf("Edward Lear", "Charles Dickens", "Mark Twain"),
            category = L,
            difficulty = R
        ),
        ask(
            id = "utopia_more",
            prompt = "Who invented the word 'utopia' as the title of his 1516 book?",
            correct = "Thomas More",
            wrong = listOf("Francis Bacon", "Jonathan Swift", "Erasmus"),
            category = L,
            difficulty = D
        ),
        ask(
            id = "quixotic_bohater",
            prompt = "The adjective 'quixotic' comes from a character created by whom?",
            correct = "Cervantes",
            wrong = listOf("Moliere", "Dante", "Boccaccio"),
            category = L,
            difficulty = D
        ),
        ask(
            id = "catch22_heller",
            prompt = "Which novelist put the phrase 'Catch-22' into everyday English?",
            correct = "Joseph Heller",
            wrong = listOf("Kurt Vonnegut", "Norman Mailer", "John Steinbeck"),
            category = L,
            difficulty = D
        ),
        ask(
            id = "serendipity_ksiazeta",
            prompt = "'Serendipity' was coined from a tale about three princes of which land?",
            correct = "Serendip",
            wrong = listOf("Samarkand", "Cathay", "Trebizond"),
            category = L,
            difficulty = X
        ),
        ask(
            id = "pensja_sol",
            prompt = "The word 'salary' traces back to Roman payments made in what?",
            correct = "Salt",
            wrong = listOf("Grain", "Wine", "Bronze"),
            category = H,
            difficulty = D
        ),
        ask(
            id = "denim_nimes",
            prompt = "Which fabric takes its name from the French city of Nimes?",
            correct = "Denim",
            wrong = listOf("Chiffon", "Tweed", "Satin"),
            category = H,
            difficulty = R
        ),
        ask(
            id = "bojkot_nazwisko",
            prompt = "The word 'boycott' comes from what?",
            correct = "A land agent's surname",
            wrong = listOf("An Irish village", "A trade union slogan", "A Dutch verb"),
            category = H,
            difficulty = R
        ),
        ask(
            id = "bokobrody_burnside",
            prompt = "'Sideburns' is a scrambled version of which general's surname?",
            correct = "Burnside",
            wrong = listOf("Sherman", "Custer", "Hooker"),
            category = H,
            difficulty = R
        ),
        ask(
            id = "kardigan_wojna",
            prompt = "The cardigan is named after a commander who fought in which war?",
            correct = "The Crimean War",
            wrong = listOf("The Boer War", "The Napoleonic Wars", "The American Civil War"),
            category = H,
            difficulty = R
        ),
        ask(
            id = "kanapka_tytul",
            prompt = "The sandwich is named after a nobleman holding which title?",
            correct = "Earl",
            wrong = listOf("Duke", "Baron", "Viscount"),
            category = H,
            difficulty = D
        ),
        ask(
            id = "spam_skecz",
            prompt = "Junk email got the name 'spam' from what?",
            correct = "A comedy sketch",
            wrong = listOf("An acronym", "A brand of software", "A programmer's surname"),
            category = T,
            difficulty = D
        ),
        ask(
            id = "wifi_hifi",
            prompt = "The name Wi-Fi was invented purely to echo which older term?",
            correct = "Hi-Fi",
            wrong = listOf("Sci-Fi", "Lo-Fi", "Hi-Tech"),
            category = T,
            difficulty = D
        ),

        /* ------------------------- Bogowie w kalendarzu i slowniku ------------------------- */
        ask(
            id = "czwartek_thor",
            prompt = "Thursday is named after which Norse god?",
            correct = "Thor",
            wrong = listOf("Odin", "Freyr", "Tyr"),
            category = M,
            difficulty = E
        ),
        ask(
            id = "sroda_odyn",
            prompt = "Wednesday takes its name from which Norse god?",
            correct = "Odin",
            wrong = listOf("Thor", "Loki", "Balder"),
            category = M,
            difficulty = D
        ),
        ask(
            id = "marzec_mars",
            prompt = "Which month is named after the Roman god of war?",
            correct = "March",
            wrong = listOf("May", "June", "August"),
            category = M,
            difficulty = D
        ),
        ask(
            id = "panika_pan",
            prompt = "The word 'panic' comes from which Greek god?",
            correct = "Pan",
            wrong = listOf("Ares", "Phobos", "Hades"),
            category = M,
            difficulty = D
        ),
        ask(
            id = "tantal_kara",
            prompt = "'Tantalize' comes from the endless punishment of which mythological figure?",
            correct = "Tantalus",
            wrong = listOf("Sisyphus", "Ixion", "Prometheus"),
            category = M,
            difficulty = R
        ),
        ask(
            id = "platki_ceres",
            prompt = "The word 'cereal' comes from which Roman goddess?",
            correct = "Ceres",
            wrong = listOf("Flora", "Vesta", "Pomona"),
            category = M,
            difficulty = D
        ),
        ask(
            id = "nemezis_bogini",
            prompt = "Nemesis was the Greek goddess of what?",
            correct = "Retribution",
            wrong = listOf("Wisdom", "Victory", "Discord"),
            category = M,
            difficulty = R
        ),
        ask(
            id = "prometeusz_imie",
            prompt = "The name Prometheus means what in Greek?",
            correct = "Forethought",
            wrong = listOf("Fire-bringer", "Chained one", "Sky-holder"),
            category = M,
            difficulty = R
        ),
        ask(
            id = "muzeum_muzy",
            prompt = "The word 'museum' originally meant a place dedicated to whom?",
            correct = "The Muses",
            wrong = listOf("Athena", "Apollo", "The Fates"),
            category = M,
            difficulty = D
        ),
        ask(
            id = "echo_nimfa",
            prompt = "The word 'echo' comes from a nymph who could only do what?",
            correct = "Repeat others' words",
            wrong = listOf("Sing without stopping", "Speak in riddles", "Whisper the future"),
            category = M,
            difficulty = D
        ),
        ask(
            id = "olimpiada_gory",
            prompt = "The Olympic Games take their name from a sanctuary in which region of Greece?",
            correct = "Elis",
            wrong = listOf("Attica", "Laconia", "Thessaly"),
            category = M,
            difficulty = X
        ),

        /* --------------------------- Nauka i jednostki --------------------------- */
        ask(
            id = "wolt_volta",
            prompt = "Which unit honours the Italian who built the first battery?",
            correct = "The volt",
            wrong = listOf("The ampere", "The ohm", "The joule"),
            category = S,
            difficulty = D
        ),
        ask(
            id = "celsjusz_odwrotnie",
            prompt = "On the original Celsius scale, what did 0 degrees mark?",
            correct = "The boiling point of water",
            wrong = listOf("Absolute zero", "Human body temperature", "The freezing point of water"),
            category = S,
            difficulty = R
        ),
        ask(
            id = "argon_leniwy",
            prompt = "Which element takes its name from the Greek word for 'idle'?",
            correct = "Argon",
            wrong = listOf("Neon", "Krypton", "Xenon"),
            category = S,
            difficulty = R
        ),
        ask(
            id = "szczepionka_krowa",
            prompt = "The word 'vaccine' comes from the Latin name for which animal?",
            correct = "Cow",
            wrong = listOf("Horse", "Sheep", "Goat"),
            category = S,
            difficulty = D
        ),
        ask(
            id = "ytterby_wioska",
            prompt = "Four chemical elements are named after a quarry in a village in which country?",
            correct = "Sweden",
            wrong = listOf("Norway", "Germany", "Scotland"),
            category = S,
            difficulty = R
        ),
        ask(
            id = "gaz_slowo",
            prompt = "The word 'gas' was made up by a 17th-century chemist based on which Greek word?",
            correct = "Chaos",
            wrong = listOf("Kosmos", "Pneuma", "Aither"),
            category = S,
            difficulty = X
        ),
        ask(
            id = "bug_owad",
            prompt = "In computing, 'bug' is said to date from an actual insect found where?",
            correct = "Inside a relay",
            wrong = listOf("On a punch card", "In a vacuum tube crate", "On a printer ribbon"),
            category = T,
            difficulty = D
        ),
        ask(
            id = "algorytm_uczony",
            prompt = "The word 'algorithm' comes from the name of a scholar from where?",
            correct = "Persia",
            wrong = listOf("Greece", "Egypt", "India"),
            category = S,
            difficulty = R
        ),
        ask(
            id = "gilotyna_lekarz",
            prompt = "The man behind the guillotine's name actually did what?",
            correct = "Proposed it as a humane method",
            wrong = listOf("Built the first one", "Was executed by it", "Sharpened its blade"),
            category = H,
            difficulty = R
        ),
        ask(
            id = "znaczek_kraj",
            prompt = "Which country issued the world's first adhesive postage stamp?",
            correct = "The United Kingdom",
            wrong = listOf("France", "The United States", "Austria"),
            category = H,
            difficulty = D
        ),
        ask(
            id = "kartagina_nazwa",
            prompt = "The name Carthage meant what in Phoenician?",
            correct = "New city",
            wrong = listOf("Great harbour", "Sea gate", "Red hill"),
            category = H,
            difficulty = X
        ),
        ask(
            id = "ekwador_rownik",
            prompt = "Ecuador is named after which geographical feature running through it?",
            correct = "The equator",
            wrong = listOf("The Andes", "The Amazon", "The Pacific coast"),
            category = H,
            difficulty = E
        ),
        ask(
            id = "katastrofa_gwiazda",
            prompt = "The word 'disaster' comes from an old belief and literally means what?",
            correct = "Bad star",
            wrong = listOf("Broken road", "Lost harvest", "Dark day"),
            category = M,
            difficulty = R
        )
    )
}
