package com.lernhero.domain.quiz.bank

import com.lernhero.domain.quiz.QuizCategory
import com.lernhero.domain.quiz.QuizDifficulty
import com.lernhero.domain.quiz.QuizEntry

/**
 * Druga polowa partii "pytan, ktore chce sie zapamietac" - literatura, technologia, sport,
 * matematyka, mitologia, pierwiastki. Te same zasady co w [TriviaQuestionsNature]: latwe znaczy
 * "trzeba sobie przypomniec", nie "nie da sie nie wiedziec", a kazdy dystraktor jest z tej samej
 * polki co odpowiedz.
 *
 * Matematyka jest tu celowo inna niz w starych plikach: zamiast "ile to 7 razy 8" (co sprawdza
 * tabliczke mnozenia, nie myslenie) sa pytania, ktore da sie ROZWIAZAC w dziesiec sekund -
 * nastepny wyraz ciagu, prawdopodobienstwo trzech orlow, liczba krawedzi szescianu.
 */
object TriviaQuestionsCulture {

    private fun ask(
        id: String,
        prompt: String,
        correct: String,
        wrong: List<String>,
        category: QuizCategory,
        difficulty: QuizDifficulty
    ) = QuizEntry(
        id = "trv2_$id",
        prompt = prompt,
        correct = correct,
        wrong = wrong,
        category = category,
        difficulty = difficulty
    )

    private val L = QuizCategory.Literature
    private val T = QuizCategory.Technology
    private val P = QuizCategory.Sport
    private val M = QuizCategory.Math
    private val Y = QuizCategory.Mythology
    private val E = QuizCategory.Elements

    val all: List<QuizEntry> = listOf(

        /* =============================== Literature =============================== */
        ask("ishmael", "Which novel opens with the line 'Call me Ishmael'?", "Moby-Dick", listOf("Treasure Island", "Robinson Crusoe", "The Old Man and the Sea"), L, QuizDifficulty.Medium),
        ask("werona", "In which Italian city is 'Romeo and Juliet' set?", "Verona", listOf("Venice", "Florence", "Rome"), L, QuizDifficulty.Easy),
        ask("poirot", "Which author created the detective Hercule Poirot?", "Agatha Christie", listOf("Arthur Conan Doyle", "Dorothy L. Sayers", "Georges Simenon"), L, QuizDifficulty.Easy),
        ask("twain_prawdziwe", "What was Mark Twain's real name?", "Samuel Clemens", listOf("Nathaniel Hawthorne", "Herman Melville", "Washington Irving"), L, QuizDifficulty.Medium),
        ask("iliada_wojna", "Which epic poem tells the story of the Trojan War?", "The Iliad", listOf("The Odyssey", "The Aeneid", "The Epic of Gilgamesh"), L, QuizDifficulty.Easy),
        ask("atticus", "In which novel is Atticus Finch a character?", "To Kill a Mockingbird", listOf("The Grapes of Wrath", "Of Mice and Men", "The Catcher in the Rye"), L, QuizDifficulty.Medium),
        ask("macbeth_czarownice", "In which Shakespeare play do three witches chant 'Double, double toil and trouble'?", "Macbeth", listOf("Hamlet", "King Lear", "The Tempest"), L, QuizDifficulty.Medium),
        ask("kafka_owad", "In which story does a man wake up transformed into a giant insect?", "Kafka's 'The Metamorphosis'", listOf("Gogol's 'The Nose'", "Poe's 'The Raven'", "Camus's 'The Stranger'"), L, QuizDifficulty.Medium),
        ask("murakami", "Which Japanese author wrote 'Norwegian Wood' and 'Kafka on the Shore'?", "Haruki Murakami", listOf("Yukio Mishima", "Kazuo Ishiguro", "Yasunari Kawabata"), L, QuizDifficulty.Hard),
        ask("baker_street", "Which fictional detective lives at 221B Baker Street?", "Sherlock Holmes", listOf("Hercule Poirot", "Philip Marlowe", "Inspector Morse"), L, QuizDifficulty.Easy),
        ask("nemo", "In which Jules Verne novel does Captain Nemo appear?", "Twenty Thousand Leagues Under the Sea", listOf("Around the World in Eighty Days", "Journey to the Centre of the Earth", "From the Earth to the Moon"), L, QuizDifficulty.Medium),
        ask("silver", "Who is the one-legged pirate in 'Treasure Island'?", "Long John Silver", listOf("Captain Hook", "Blackbeard", "Captain Flint"), L, QuizDifficulty.Medium),
        ask("dracula_kraina", "In which region does Count Dracula's castle stand?", "Transylvania", listOf("Bavaria", "Bohemia", "Wallonia"), L, QuizDifficulty.Easy),
        ask("orwell_folwark", "In 'Animal Farm', what kind of animal is Napoleon?", "A pig", listOf("A horse", "A dog", "A donkey"), L, QuizDifficulty.Medium),
        ask("odyseusz_dom", "What is the name of Odysseus's home island?", "Ithaca", listOf("Crete", "Rhodes", "Corfu"), L, QuizDifficulty.Hard),
        ask("dorian_gray", "Who wrote 'The Picture of Dorian Gray'?", "Oscar Wilde", listOf("Bram Stoker", "Robert Louis Stevenson", "Charles Dickens"), L, QuizDifficulty.Medium),
        ask("hamlet_kraj", "Hamlet is a prince of which country?", "Denmark", listOf("Norway", "Scotland", "Sweden"), L, QuizDifficulty.Medium),
        ask("tolkien_jezyki", "What was J.R.R. Tolkien's profession besides writing?", "A professor of language and literature", listOf("A physician working in a city hospital", "A lawyer with his own practice", "An officer in the regular army"), L, QuizDifficulty.Hard),
        ask("mala_syrenka_autor", "Who wrote the original tale of 'The Little Mermaid'?", "Hans Christian Andersen", listOf("The Brothers Grimm", "Charles Perrault", "Oscar Wilde"), L, QuizDifficulty.Easy),
        ask("zbrodnia_bohater", "What is the name of the student who commits murder in 'Crime and Punishment'?", "Raskolnikov", listOf("Karamazov", "Oblomov", "Onegin"), L, QuizDifficulty.Hard),
        ask("haiku_sylaby", "How many syllables does a traditional haiku have?", "17", listOf("14", "21", "10"), L, QuizDifficulty.Medium),
        ask("wielki_brat", "In which novel does 'Big Brother' watch everyone?", "1984", listOf("Brave New World", "Fahrenheit 451", "We"), L, QuizDifficulty.Easy),

        /* =============================== Technology =============================== */
        ask("youtube_zoo", "Where was the very first YouTube video, uploaded in 2005, filmed?", "At a zoo", listOf("In a garage", "At a concert", "In a lecture hall"), T, QuizDifficulty.Hard),
        ask("python_nazwa", "The programming language Python is named after what?", "A comedy troupe", listOf("A snake", "Its creator's pet", "A Greek myth"), T, QuizDifficulty.Medium),
        ask("wifi_fale", "What does Wi-Fi use to carry data through the air?", "Radio waves", listOf("Infrared light", "Sound waves", "Microscopic wires"), T, QuizDifficulty.Easy),
        ask("iphone_rok", "In which year was the first iPhone released?", "2007", listOf("2004", "2010", "2001"), T, QuizDifficulty.Medium),
        ask("binarne_1010", "What is the binary number 1010 in decimal?", "10", listOf("5", "12", "20"), T, QuizDifficulty.Medium),
        ask("playstation", "Which company makes the PlayStation?", "Sony", listOf("Nintendo", "Microsoft", "Sega"), T, QuizDifficulty.Easy),
        ask("mosaic", "Which early web browser, released in 1993, made the web popular by showing images inline?", "Mosaic", listOf("Netscape", "Internet Explorer", "Opera"), T, QuizDifficulty.Hard),
        ask("googol", "Which company's name comes from a misspelling of 'googol', the number 1 followed by 100 zeros?", "Google", listOf("Yahoo", "Microsoft", "Oracle"), T, QuizDifficulty.Medium),
        ask("internet_uzytkownicy", "Which country has the most internet users?", "China", listOf("The United States", "India", "Russia"), T, QuizDifficulty.Medium),
        ask("qr", "What does 'QR' stand for in 'QR code'?", "Quick Response", listOf("Quality Reader", "Quantum Register", "Quick Reference"), T, QuizDifficulty.Medium),
        ask("baza_danych", "Which of these is a database system?", "MySQL", listOf("Firefox", "Photoshop", "PowerPoint"), T, QuizDifficulty.Easy),
        ask("arpanet", "What was ARPANET?", "The forerunner of the internet", listOf("The first home computer", "An early video game", "A satellite TV network"), T, QuizDifficulty.Medium),
        ask("hdmi", "What does an HDMI cable carry?", "Video and audio", listOf("Only video", "Only audio", "Electrical power only"), T, QuizDifficulty.Easy),
        ask("captcha", "What does a CAPTCHA test check?", "Whether the user is a human", listOf("Whether the password is strong", "Whether the connection is secure", "Whether the browser is up to date"), T, QuizDifficulty.Easy),
        ask("phishing", "What is 'phishing'?", "Tricking people into revealing passwords or data", listOf("Speeding up a computer that has got slow", "Copying the contents of a hard drive", "Blocking the adverts shown on a page"), T, QuizDifficulty.Easy),
        ask("bitcoin", "What is Bitcoin?", "A digital currency", listOf("A social network", "A web browser", "A computer chip"), T, QuizDifficulty.Easy),
        ask("curiosity", "On which planet did the Curiosity rover land in 2012?", "Mars", listOf("Venus", "The Moon", "Mercury"), T, QuizDifficulty.Easy),
        ask("engelbart", "Who invented the computer mouse?", "Douglas Engelbart", listOf("Steve Wozniak", "Alan Kay", "Xerox's Steve Jobs"), T, QuizDifficulty.Hard),
        ask("turing_test", "What does the Turing test try to judge?", "Whether a machine can pass as human in conversation", listOf("How fast a computer can carry out its sums", "Whether a cipher can ever be broken", "How much memory a program needs"), T, QuizDifficulty.Medium),
        ask("bug_cma", "What was the first computer 'bug', found in 1947, literally?", "A moth stuck in a relay", listOf("A typo in a program", "A loose wire", "A power surge"), T, QuizDifficulty.Hard),
        ask("vpn", "What does a VPN do?", "Sends your traffic through an encrypted tunnel", listOf("Speeds up a weak home Wi-Fi signal", "Blocks every advertisement on a page", "Stores all of your passwords safely"), T, QuizDifficulty.Medium),
        ask("ssd_hdd", "What is the main advantage of an SSD over a traditional hard drive?", "It has no moving parts and is much faster", listOf("It holds far more data on the same disk", "It never fails, however long it runs", "It costs less for every gigabyte"), T, QuizDifficulty.Medium),
        ask("pixel", "What is a pixel?", "The smallest dot in a digital image", listOf("A unit of internet speed", "A type of processor", "A file format"), T, QuizDifficulty.Easy),
        ask("bluetooth_nazwa", "Bluetooth is named after what?", "A 10th-century Scandinavian king", listOf("Its inventor", "A type of radio wave", "A blue LED on the first device"), T, QuizDifficulty.Hard),
        ask("mac_pierwszy", "In which year did Apple release the first Macintosh?", "1984", listOf("1977", "1991", "1980"), T, QuizDifficulty.Hard),
        ask("nintendo_karty", "What did Nintendo originally make when it was founded in 1889?", "Playing cards", listOf("Toys", "Radios", "Bicycles"), T, QuizDifficulty.Hard),
        ask("tetris_kraj", "In which country was Tetris created?", "The Soviet Union", listOf("Japan", "The United States", "Finland"), T, QuizDifficulty.Medium),
        ask("hashtag", "What is the '#' symbol called when used to tag posts?", "A hashtag", listOf("A pound tag", "A sharp tag", "A grid tag"), T, QuizDifficulty.Easy),

        /* ================================= Sport ================================== */
        ask("stanley_cup", "In which sport is the Stanley Cup awarded?", "Ice hockey", listOf("Baseball", "Basketball", "Lacrosse"), P, QuizDifficulty.Easy),
        ask("maraton_dystans", "How long is a marathon?", "42.195 km", listOf("40 km", "50 km", "45.5 km"), P, QuizDifficulty.Easy),
        ask("krykiet_over", "In cricket, how many balls make up an over?", "Six", listOf("Four", "Eight", "Ten"), P, QuizDifficulty.Medium),
        ask("snooker_147", "What is the maximum break in snooker?", "147", listOf("100", "180", "155"), P, QuizDifficulty.Hard),
        ask("ping_pong_kraj", "In which country did table tennis originate?", "England", listOf("China", "Japan", "Germany"), P, QuizDifficulty.Hard),
        ask("judo_znaczenie", "Which martial art's name means 'the gentle way'?", "Judo", listOf("Karate", "Taekwondo", "Kung fu"), P, QuizDifficulty.Medium),
        ask("londyn_igrzyska", "Which city has hosted the Summer Olympics three times?", "London", listOf("Paris", "Athens", "Los Angeles"), P, QuizDifficulty.Medium),
        ask("flaga_biala", "In NASCAR, what does a white flag signal?", "The final lap", listOf("The race is stopped", "Slippery track", "A driver must pit"), P, QuizDifficulty.Hard),
        ask("golf_eagle", "In golf, what is a score of two under par on a hole called?", "An eagle", listOf("A birdie", "An albatross", "A bogey"), P, QuizDifficulty.Medium),
        ask("skoczek_ruchy", "From the centre of an empty chessboard, how many squares can a knight move to?", "8", listOf("4", "6", "12"), P, QuizDifficulty.Hard),
        ask("ashes", "In which sport do England and Australia compete for 'the Ashes'?", "Cricket", listOf("Rugby", "Rowing", "Sailing"), P, QuizDifficulty.Medium),
        ask("sumo_ring", "What is a sumo wrestling ring called?", "A dohyo", listOf("A dojo", "A tatami", "A kumite"), P, QuizDifficulty.Hard),
        ask("touchdown", "How many points is a touchdown worth in American football?", "6", listOf("3", "7", "5"), P, QuizDifficulty.Medium),
        ask("bolt", "Which Jamaican sprinter holds the 100 m world record?", "Usain Bolt", listOf("Asafa Powell", "Yohan Blake", "Carl Lewis"), P, QuizDifficulty.Easy),
        ask("golf_szkocja", "Which country is the birthplace of golf?", "Scotland", listOf("England", "The United States", "The Netherlands"), P, QuizDifficulty.Medium),
        ask("kregle_turkey", "In bowling, what are three strikes in a row called?", "A turkey", listOf("A hat-trick", "A triple", "An eagle"), P, QuizDifficulty.Hard),
        ask("tenis_as", "In tennis, what is a serve called that the receiver cannot touch?", "An ace", listOf("A let", "A winner", "A lob"), P, QuizDifficulty.Easy),
        ask("maraton_nazwa", "The marathon is named after a battle in which country?", "Greece", listOf("Italy", "Turkey", "Egypt"), P, QuizDifficulty.Medium),
        ask("tour_de_france_start", "In which year was the first Tour de France held?", "1903", listOf("1923", "1889", "1948"), P, QuizDifficulty.Hard),
        ask("hokej_krazek", "What is the disc used in ice hockey called?", "A puck", listOf("A pellet", "A shuttle", "A stone"), P, QuizDifficulty.Easy),
        ask("curling_kamien", "In curling, what is the stone traditionally made of?", "Granite", listOf("Marble", "Steel", "Basalt"), P, QuizDifficulty.Hard),
        ask("siatkowka_wynalazek", "In which country was volleyball invented?", "The United States", listOf("Brazil", "Japan", "Italy"), P, QuizDifficulty.Medium),
        ask("koszykowka_wynalazek", "Who invented basketball in 1891?", "James Naismith", listOf("Walter Camp", "Abner Doubleday", "Pierre de Coubertin"), P, QuizDifficulty.Hard),
        ask("igrzyska_zimowe_pierwsze", "In which country were the first Winter Olympics held, in 1924?", "France", listOf("Switzerland", "Norway", "Austria"), P, QuizDifficulty.Hard),
        ask("pilka_nozna_bramka", "Who is the only player allowed to handle the ball in open play in football?", "The goalkeeper, inside the penalty area", listOf("The captain", "Any defender inside the box", "The referee's chosen player"), P, QuizDifficulty.Easy),
        ask("f1_rekordy", "Which driver has won the most Formula 1 world championships (tied at seven)?", "Michael Schumacher and Lewis Hamilton", listOf("Ayrton Senna and Alain Prost", "Sebastian Vettel and Max Verstappen", "Juan Manuel Fangio and Niki Lauda"), P, QuizDifficulty.Medium),
        ask("baseball_inningi", "How many innings are in a standard baseball game?", "9", listOf("7", "10", "12"), P, QuizDifficulty.Medium),
        ask("szermierka_bronie", "Which of these is one of the three weapons used in Olympic fencing?", "The sabre", listOf("The rapier", "The broadsword", "The katana"), P, QuizDifficulty.Hard),

        /* ================================== Math ================================== */
        ask("parzysta_pierwsza", "What is the only even prime number?", "2", listOf("0", "4", "6"), M, QuizDifficulty.Easy),
        ask("dziesieciokat", "What is a polygon with ten sides called?", "A decagon", listOf("A hexagon", "A dodecagon", "A nonagon"), M, QuizDifficulty.Medium),
        ask("sekundy_doba", "How many seconds are in a day?", "86,400", listOf("3,600", "1,440", "864,000"), M, QuizDifficulty.Medium),
        ask("rzymskie_2024", "What is 2024 in Roman numerals?", "MMXXIV", listOf("MMXXVI", "MXXIV", "MMXIV"), M, QuizDifficulty.Medium),
        ask("moneta_3_orly", "If you flip a fair coin three times, what is the chance of getting heads every time?", "1 in 8", listOf("1 in 3", "1 in 6", "1 in 4"), M, QuizDifficulty.Medium),
        ask("szesciokat_katy", "What do the interior angles of a hexagon add up to?", "720 degrees", listOf("540 degrees", "360 degrees", "900 degrees"), M, QuizDifficulty.Hard),
        ask("ulamek_075", "What is 0.75 as a fraction?", "3/4", listOf("2/3", "7/5", "3/5"), M, QuizDifficulty.Easy),
        ask("jeden_ani", "Which number is neither prime nor composite?", "1", listOf("2", "0", "9"), M, QuizDifficulty.Hard),
        ask("ulamki_suma", "What is 1/3 + 1/6?", "1/2", listOf("2/9", "1/9", "2/3"), M, QuizDifficulty.Medium),
        ask("milion_zera", "How many zeros are in one million?", "6", listOf("5", "7", "9"), M, QuizDifficulty.Easy),
        ask("45x11", "What is 45 x 11?", "495", listOf("485", "505", "455"), M, QuizDifficulty.Medium),
        ask("srednia_4", "What is the average of 10, 20, 30 and 40?", "25", listOf("20", "30", "35"), M, QuizDifficulty.Easy),
        ask("obwod_srednica", "A circle's circumference divided by its diameter always equals what?", "Pi", listOf("2", "Its radius", "The square root of 2"), M, QuizDifficulty.Easy),
        ask("pierwsza_po_31", "What is the next prime number after 31?", "37", listOf("33", "35", "39"), M, QuizDifficulty.Medium),
        ask("szescian_krawedzie", "How many edges does a cube have?", "12", listOf("8", "6", "10"), M, QuizDifficulty.Medium),
        ask("procent_20_45", "What is 20% of 45?", "9", listOf("8", "10", "11"), M, QuizDifficulty.Medium),
        ask("ciag_potegi", "What comes next in the sequence 2, 6, 18, 54, ...?", "162", listOf("108", "216", "158"), M, QuizDifficulty.Medium),
        ask("kwadrat_25", "What is 25 squared?", "625", listOf("525", "650", "575"), M, QuizDifficulty.Medium),
        ask("kostka_suma", "What is the most likely total when you roll two dice?", "7", listOf("6", "8", "12"), M, QuizDifficulty.Medium),
        ask("liczba_pi_niewymierna", "Which kind of number is pi?", "Irrational", listOf("A fraction", "An integer", "A prime"), M, QuizDifficulty.Medium),
        ask("trojkat_rownoboczny_katy", "How big is each angle of an equilateral triangle?", "60 degrees", listOf("45 degrees", "90 degrees", "30 degrees"), M, QuizDifficulty.Medium),
        ask("11_kwadrat", "What is 11 squared?", "121", listOf("111", "132", "141"), M, QuizDifficulty.Easy),
        ask("podzielne_9", "A number is divisible by 9 if...", "The sum of its digits is divisible by 9", listOf("Its last digit happens to be a nine", "It is an odd number, not an even", "Its very first digit is a nine"), M, QuizDifficulty.Hard),
        ask("polowa_polowy", "What is half of a half of 100?", "25", listOf("50", "12.5", "20"), M, QuizDifficulty.Easy),
        ask("ile_minut_dwa_dni", "How many hours are in a week?", "168", listOf("144", "196", "172"), M, QuizDifficulty.Medium),
        ask("liczby_1_100_suma", "What is the sum of all whole numbers from 1 to 100?", "5050", listOf("5000", "10100", "4950"), M, QuizDifficulty.Hard),

        /* ================================ Mythology =============================== */
        ask("mjolnir", "What is the name of Thor's hammer?", "Mjolnir", listOf("Gungnir", "Gram", "Skidbladnir"), Y, QuizDifficulty.Medium),
        ask("ikar", "Who flew too close to the Sun on wings of feathers and wax?", "Icarus", listOf("Daedalus", "Perseus", "Hermes"), Y, QuizDifficulty.Easy),
        ask("atena_glowa", "Which goddess was born fully grown from the head of Zeus?", "Athena", listOf("Aphrodite", "Artemis", "Hera"), Y, QuizDifficulty.Medium),
        ask("walhalla", "What is Valhalla?", "Odin's hall for warriors slain in battle", listOf("The Norse underworld", "The home of the giants", "The bridge to the gods' realm"), Y, QuizDifficulty.Medium),
        ask("marzec_mars", "The month of March is named after which Roman god?", "Mars", listOf("Mercury", "Jupiter", "Janus"), Y, QuizDifficulty.Medium),
        ask("arachne", "Who was turned into a spider by Athena after a weaving contest?", "Arachne", listOf("Medusa", "Niobe", "Daphne"), Y, QuizDifficulty.Hard),
        ask("ganesha", "Which Hindu god has the head of an elephant?", "Ganesha", listOf("Hanuman", "Vishnu", "Shiva"), Y, QuizDifficulty.Medium),
        ask("sleipnir", "What is the name of Odin's eight-legged horse?", "Sleipnir", listOf("Fenrir", "Jormungandr", "Huginn"), Y, QuizDifficulty.Hard),
        ask("midas", "What did everything King Midas touched turn into?", "Gold", listOf("Stone", "Silver", "Dust"), Y, QuizDifficulty.Easy),
        ask("cerber", "What is the three-headed dog that guards the Greek underworld called?", "Cerberus", listOf("Orthrus", "Fenrir", "Argos"), Y, QuizDifficulty.Easy),
        ask("achilles_pieta", "Which hero could only be wounded in his heel?", "Achilles", listOf("Hector", "Ajax", "Odysseus"), Y, QuizDifficulty.Easy),
        ask("pandora", "Who opened a jar (or box) that released all the evils into the world?", "Pandora", listOf("Persephone", "Psyche", "Helen"), Y, QuizDifficulty.Easy),
        ask("feniks", "Which mythical bird is reborn from its own ashes?", "The phoenix", listOf("The griffin", "The roc", "The harpy"), Y, QuizDifficulty.Easy),
        ask("fenrir", "Which Norse god is fated to be swallowed by the wolf Fenrir at Ragnarok?", "Odin", listOf("Thor", "Loki", "Heimdall"), Y, QuizDifficulty.Hard),
        ask("quetzalcoatl", "Which Aztec god is the 'feathered serpent'?", "Quetzalcoatl", listOf("Huitzilopochtli", "Tlaloc", "Tezcatlipoca"), Y, QuizDifficulty.Hard),
        ask("atlas", "Which Titan was condemned to hold up the sky?", "Atlas", listOf("Cronus", "Prometheus", "Hyperion"), Y, QuizDifficulty.Medium),
        ask("chimera", "Which monster had a lion's head, a goat's body and a serpent's tail?", "The Chimera", listOf("The Hydra", "The Sphinx", "The Manticore"), Y, QuizDifficulty.Medium),
        ask("persefona", "Who was abducted by Hades and made queen of the underworld?", "Persephone", listOf("Aphrodite", "Ariadne", "Eurydice"), Y, QuizDifficulty.Medium),
        ask("pegaz_narodziny", "From whose blood was Pegasus born?", "Medusa's", listOf("The Hydra's", "Achilles's", "Zeus's"), Y, QuizDifficulty.Hard),
        ask("hermes_buty", "What did Hermes wear that let him fly?", "Winged sandals", listOf("A winged helmet only", "A magic cloak", "A feathered belt"), Y, QuizDifficulty.Easy),
        ask("ragnarok", "What is Ragnarok in Norse mythology?", "The battle at the end of the world", listOf("The creation of the world", "Thor's wedding", "The first winter"), Y, QuizDifficulty.Medium),
        ask("ozyrys", "Which Egyptian god ruled the afterlife and was killed by his brother Set?", "Osiris", listOf("Ra", "Horus", "Anubis"), Y, QuizDifficulty.Medium),
        ask("dionizos", "Which Greek god presided over wine and festivity?", "Dionysus", listOf("Apollo", "Hephaestus", "Hermes"), Y, QuizDifficulty.Easy),
        ask("hefajstos", "Which Greek god was the smith who forged the gods' weapons?", "Hephaestus", listOf("Ares", "Hermes", "Poseidon"), Y, QuizDifficulty.Medium),
        ask("thor_dzien", "Which day of the week is named after Thor?", "Thursday", listOf("Tuesday", "Friday", "Wednesday"), Y, QuizDifficulty.Medium),
        ask("sroda_odyn", "Which day of the week is named after Odin (Woden)?", "Wednesday", listOf("Monday", "Thursday", "Saturday"), Y, QuizDifficulty.Hard),
        ask("minotaur_labirynt", "Who built the Labyrinth that held the Minotaur?", "Daedalus", listOf("Icarus", "Minos", "Theseus"), Y, QuizDifficulty.Hard),
        ask("sfinks_zagadka", "What did the Sphinx do to travellers who could not answer her riddle?", "Devoured them", listOf("Turned them to stone", "Sent them home", "Made them her servants"), Y, QuizDifficulty.Medium),

        /* ================================ Elements ================================ */
        ask("potas_k", "Which element has the symbol K?", "Potassium", listOf("Krypton", "Calcium", "Carbon"), E, QuizDifficulty.Medium),
        ask("sod_na", "Which element has the symbol Na?", "Sodium", listOf("Nitrogen", "Neon", "Nickel"), E, QuizDifficulty.Medium),
        ask("europ", "Which element is named after a continent?", "Europium", listOf("Americium", "Germanium", "Polonium"), E, QuizDifficulty.Hard),
        ask("stront_fajerwerki", "Which element gives fireworks a deep red colour?", "Strontium", listOf("Copper", "Sodium", "Barium"), E, QuizDifficulty.Hard),
        ask("hel_2", "Which element has atomic number 2?", "Helium", listOf("Hydrogen", "Lithium", "Neon"), E, QuizDifficulty.Easy),
        ask("stal_zelazo", "Which element is the main component of steel?", "Iron", listOf("Carbon", "Chromium", "Nickel"), E, QuizDifficulty.Easy),
        ask("lit_najlzejszy", "Which is the lightest metal?", "Lithium", listOf("Aluminium", "Magnesium", "Sodium"), E, QuizDifficulty.Hard),
        ask("argon_leniwy", "Which element's name comes from the Greek word for 'lazy' or 'idle'?", "Argon", listOf("Neon", "Krypton", "Xenon"), E, QuizDifficulty.Hard),
        ask("magnez_chlorofil", "Which metal sits at the centre of the chlorophyll molecule?", "Magnesium", listOf("Iron", "Zinc", "Copper"), E, QuizDifficulty.Expert),
        ask("banan_potas", "Which element are bananas famously rich in?", "Potassium", listOf("Calcium", "Iron", "Zinc"), E, QuizDifficulty.Easy),
        ask("wolfram_w", "What is the chemical symbol for tungsten?", "W", listOf("T", "Tu", "Tg"), E, QuizDifficulty.Hard),
        ask("jod_sol", "Which element is added to table salt to prevent thyroid problems?", "Iodine", listOf("Fluorine", "Zinc", "Magnesium"), E, QuizDifficulty.Medium),
        ask("cyna_sn", "Which element has the symbol Sn?", "Tin", listOf("Silicon", "Selenium", "Silver"), E, QuizDifficulty.Hard),
        ask("brom_ciecz", "Which halogen is a liquid at room temperature?", "Bromine", listOf("Chlorine", "Iodine", "Fluorine"), E, QuizDifficulty.Hard),
        ask("boksyt_glin", "Which metal is extracted from the ore bauxite?", "Aluminium", listOf("Copper", "Zinc", "Titanium"), E, QuizDifficulty.Hard),
        ask("srebro_ag", "Which element has the symbol Ag?", "Silver", listOf("Gold", "Argon", "Aluminium"), E, QuizDifficulty.Easy),
        ask("rad_curie", "Which glowing radioactive element did the Curies discover in 1898, alongside polonium?", "Radium", listOf("Uranium", "Plutonium", "Thorium"), E, QuizDifficulty.Medium),
        ask("rtec_hg", "Why is mercury's symbol Hg?", "From its Latin name 'hydrargyrum'", listOf("From its discoverer's initials", "From the Greek for 'heavy gas'", "It was assigned at random"), E, QuizDifficulty.Hard),
        ask("zloto_au", "Gold's symbol Au comes from which Latin word?", "Aurum", listOf("Aureus", "Auris", "Aurora"), E, QuizDifficulty.Medium),
        ask("chlor_basen", "Which element is used to disinfect swimming pools?", "Chlorine", listOf("Fluorine", "Bromine", "Iodine"), E, QuizDifficulty.Easy),
        ask("hel_slonce", "Which element was discovered in the Sun before it was found on Earth?", "Helium", listOf("Hydrogen", "Neon", "Argon"), E, QuizDifficulty.Hard),
        ask("krzem_chipy", "Which element is the basis of most computer chips?", "Silicon", listOf("Carbon", "Germanium", "Copper"), E, QuizDifficulty.Easy),
        ask("tytan_lekki", "Which strong, lightweight metal is used in aircraft and hip implants?", "Titanium", listOf("Tungsten", "Nickel", "Lead"), E, QuizDifficulty.Medium),
        ask("neon_gaz_szlachetny", "Why are the noble gases called 'noble'?", "They almost never react with other elements", listOf("They were discovered by aristocrats", "They are very expensive", "They glow with a golden light"), E, QuizDifficulty.Medium),
        ask("uran_reaktor", "Which element is the usual fuel in nuclear power plants?", "Uranium", listOf("Plutonium only", "Radium", "Thorium"), E, QuizDifficulty.Easy),
        ask("wodor_h2o", "How many hydrogen atoms are in a molecule of water?", "Two", listOf("One", "Three", "Four"), E, QuizDifficulty.Easy),
        ask("wegiel_diament_grafit", "Diamond and graphite are both made of which element?", "Carbon", listOf("Silicon", "Boron", "Sulfur"), E, QuizDifficulty.Easy),
        ask("fluor_pasta", "Which element is added to toothpaste to strengthen tooth enamel?", "Fluorine (as fluoride)", listOf("Calcium", "Chlorine", "Sodium"), E, QuizDifficulty.Easy),
        ask("neon_liczba", "Roughly how many elements are on the periodic table?", "About 118", listOf("About 92", "About 150", "About 200"), E, QuizDifficulty.Medium),
        ask("miedz_zielona", "Why do copper roofs and statues turn green over time?", "Copper reacts with air and water to form a green patina", listOf("Green algae slowly spread across the surface of the metal", "The green paint underneath begins to show", "Copper naturally holds green minerals"), E, QuizDifficulty.Medium),
        ask("kobalt_niebieski", "Which metal gives 'cobalt blue' glass and ceramics their colour?", "Cobalt", listOf("Copper", "Chromium", "Nickel"), E, QuizDifficulty.Medium),
    )
}
