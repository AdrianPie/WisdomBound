package com.lernhero.domain.quiz.bank

import com.lernhero.domain.quiz.QuizCategory
import com.lernhero.domain.quiz.QuizDifficulty
import com.lernhero.domain.quiz.QuizEntry

/**
 * "Ciekawostki" - pytania, po ktorych gracz ma ochote komus opowiedziec, czego sie dowiedzial.
 * Kazde opiera sie na zaskakujacym, ale sprawdzonym fakcie; bledne odpowiedzi sa wiarygodne,
 * zeby nie dalo sie trafic przez eliminacje absurdow.
 */
object QuestionsCurious {

    private val E = QuizDifficulty.Easy
    private val M = QuizDifficulty.Medium
    private val H = QuizDifficulty.Hard
    private val X = QuizDifficulty.Expert

    private fun q(id: String, prompt: String, correct: String, wrong: List<String>, category: QuizCategory, difficulty: QuizDifficulty) =
        QuizEntry(id = "cur_$id", prompt = prompt, correct = correct, wrong = wrong, category = category, difficulty = difficulty)

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
        /* ------------------------------ Animals ------------------------------ */
        q("krewetka_modliszkowa", "Which sea creature punches so fast it boils the water around its claw?", "The mantis shrimp", listOf("The pistol crab", "The lobster", "The cuttlefish"), Ani, H),
        q("niesmiertelna_meduza", "Which jellyfish can turn back into a juvenile polyp and start its life over?", "Turritopsis dohrnii", listOf("The box jellyfish", "The lion's mane jellyfish", "The moon jellyfish"), Ani, X),
        q("zyrafa_jezyk", "What colour is a giraffe's tongue?", "Dark blue-purple", listOf("Pink", "Grey", "Bright red"), Ani, M),
        q("slon_skok", "Which large land mammal is unable to jump?", "The elephant", listOf("The hippopotamus", "The rhinoceros", "The giraffe"), Ani, M),
        q("krowy_najlepsi", "Studies show cows get stressed when separated from whom?", "Their best friend in the herd", listOf("Their calf only", "The farmer", "Any other cow"), Ani, H),
        q("kolibry_wstecz", "Which is the only group of birds that can fly backwards?", "Hummingbirds", listOf("Swallows", "Kingfishers", "Swifts"), Ani, E),
        q("niesporczak", "Which tiny animal has survived exposure to the vacuum of space?", "The tardigrade", listOf("The flea", "The springtail", "The rotifer"), Ani, H),

        /* ------------------------------ Biology ------------------------------ */
        q("kosci_niemowle", "Do babies have more or fewer bones than adults?", "More - around 300 that fuse over time", listOf("Fewer - bones split as we grow", "Exactly the same, 206", "Fewer - around 150"), Bio, M),
        q("zoladek_kwas", "Why doesn't your stomach digest itself?", "It is lined with a protective layer of mucus", listOf("Stomach acid is too weak", "Its walls are made of bone-like tissue", "Acid only forms after eating"), Bio, M),
        q("banan_geny", "Humans share roughly what share of their genes with a banana?", "About 60%", listOf("About 5%", "About 25%", "About 95%"), Bio, H),
        q("rogowka_tlen", "Which part of the body gets its oxygen straight from the air rather than from blood?", "The cornea", listOf("The eardrum", "The fingernails", "The tongue"), Bio, X),

        /* ------------------------------ History ------------------------------ */
        q("kleopatra_czas", "Cleopatra lived closer in time to which event?", "The Moon landing", listOf("The building of the Great Pyramid", "The birth of Homer", "The founding of Egypt"), His, H),
        q("oxford_aztekowie", "Which is older: the University of Oxford or the Aztec Empire?", "The University of Oxford", listOf("The Aztec Empire", "They were founded the same year", "Neither - both date from the 1700s"), His, X),
        q("wojna_angielsko_zanzibarska", "The shortest war in recorded history, in 1896, lasted about how long?", "38 minutes", listOf("3 days", "6 hours", "2 weeks"), His, H),
        q("napoleon_wzrost", "Napoleon's 'short' reputation largely came from what?", "Confusion between French and English inches", listOf("His height of 1.40 m", "A painting of him sitting down", "A false death certificate"), His, X),
        q("mamuty_piramidy", "Were woolly mammoths still alive when the Great Pyramid of Giza was built?", "Yes, on a remote Arctic island", listOf("No, they died out 50,000 years earlier", "No, they died out with the dinosaurs", "Only in zoos of the pharaohs"), His, X),
        q("wiking_helmy", "What did real Viking helmets NOT have, despite popular images?", "Horns", listOf("Nose guards", "Iron caps", "Leather lining"), His, M),

        /* ------------------------------ Elements ----------------------------- */
        q("gal_dlon", "Which metal melts in your hand, at about 30 °C?", "Gallium", listOf("Aluminium", "Tin", "Magnesium"), Ele, H),
        q("hel_glos", "Why does helium make your voice sound higher?", "Sound travels faster through it", listOf("It tightens your vocal cords", "It is lighter than your tongue", "It cools your throat"), Ele, M),
        q("zloto_jadalne", "Which metal can be eaten as leaf decoration on cakes without harm?", "Gold", listOf("Lead", "Mercury", "Copper"), Ele, E),
        q("wolfram_litera", "Why is tungsten's symbol W?", "From its other name, wolfram", listOf("W stands for 'white metal'", "It was the 23rd element found", "Its discoverer was named Weber"), Ele, H),

        /* ------------------------------ Capitals ----------------------------- */
        q("rpa_trzy_stolice", "Which country has three capital cities?", "South Africa", listOf("Switzerland", "Malaysia", "Nigeria"), Cap, H),
        q("kazachstan_stolica", "What is the capital of Kazakhstan?", "Astana", listOf("Almaty", "Bishkek", "Tashkent"), Cap, M),
        q("birma_stolica", "Myanmar moved its capital in 2006 to which purpose-built city?", "Naypyidaw", listOf("Yangon", "Mandalay", "Bagan"), Cap, X),

        /* ------------------------------ Science ------------------------------ */
        q("mars_zachod", "What colour are sunsets on Mars?", "Blue", listOf("Red", "Green", "Purple"), Sci, H),
        q("gwiazda_neutronowa", "A teaspoon of neutron star matter would weigh roughly how much on Earth?", "Billions of tonnes", listOf("About a kilogram", "About a tonne", "About as much as a car"), Sci, H),
        q("woda_rozszerzanie", "Why do water pipes burst in hard frost?", "Water expands when it freezes", listOf("Ice is heavier than water", "Cold makes metal soft", "Frozen water releases gas"), Sci, E),
        q("kosmos_zapach", "Astronauts say space smells like what, on their suits after a spacewalk?", "Seared steak and hot metal", listOf("Fresh rain", "Nothing at all", "Rotten eggs"), Sci, X),

        /* ------------------------------ Geography ---------------------------- */
        q("rosja_strefy", "How many time zones does Russia span?", "11", listOf("5", "8", "15"), Geo, H),
        q("kanada_jeziora", "Which country has the most lakes in the world?", "Canada", listOf("Finland", "Russia", "Sweden"), Geo, M),
        q("afryka_kraje", "Roughly how many countries are there in Africa?", "54", listOf("32", "41", "67"), Geo, M),
        q("atakama", "Which desert has places where rain has never been recorded?", "The Atacama", listOf("The Sahara", "The Gobi", "The Kalahari"), Geo, H),
        q("rzeka_bez_mostu", "Which huge river is crossed by no bridge along its main channel?", "The Amazon", listOf("The Nile", "The Mississippi", "The Congo"), Geo, X),

        /* ------------------------------- Sport ------------------------------- */
        q("golf_ksiezyc", "Which sport has been played on the Moon?", "Golf", listOf("Tennis", "Baseball", "Frisbee"), Spo, M),
        q("olimpiada_sztuka", "Until 1948 the Olympics awarded medals in which unusual field?", "Art competitions", listOf("Chess", "Cooking", "Ballroom dancing"), Spo, X),
        q("tenis_love", "In tennis, what does the score 'love' mean?", "Zero", listOf("Deuce", "Advantage", "Match point"), Spo, E),
        q("pilka_nozna_zasady", "Which is the only player in football allowed to handle the ball in their own penalty area?", "The goalkeeper", listOf("The captain", "The sweeper", "Any defender"), Spo, E),

        /* -------------------------------- Math ------------------------------- */
        q("urodziny_paradoks", "How many people must be in a room for a better-than-even chance that two share a birthday?", "23", listOf("183", "57", "366"), Mat, H),
        q("zero_silnia", "What is 0! (zero factorial)?", "1", listOf("0", "Undefined", "Infinity"), Mat, H),
        q("kartka_zlozenia", "Folding a 0.1 mm sheet of paper 42 times would make it roughly how thick?", "Enough to reach the Moon", listOf("About 4 mm", "About as tall as a house", "About 42 metres"), Mat, X),
        q("najmniejsza_2520", "What is the smallest number divisible by every whole number from 1 to 10?", "2520", listOf("3628800", "1260", "5040"), Mat, X),
        q("kwadrat_111", "What is 111,111,111 × 111,111,111?", "12,345,678,987,654,321", listOf("1,234,567,890", "11,111,111,111", "123,456,789,123,456,789"), Mat, X),

        /* ----------------------------- Literature ---------------------------- */
        q("frankenstein_wiek", "How old was Mary Shelley when she began writing 'Frankenstein'?", "18", listOf("25", "33", "41"), Lit, H),
        q("hobbit_poczatek", "Which novel begins 'In a hole in the ground there lived a...'?", "The Hobbit", listOf("Watership Down", "Alice's Adventures in Wonderland", "The Wind in the Willows"), Lit, E),
        q("sherlock_cytat", "Which famous line does Sherlock Holmes never actually say in Conan Doyle's stories?", "'Elementary, my dear Watson'", listOf("'The game is afoot'", "'You have been in Afghanistan, I perceive'", "'Data! Data! Data!'"), Lit, X),
        q("dickens_tygodnik", "How were most of Charles Dickens's novels first published?", "In monthly or weekly instalments", listOf("As single hardback volumes", "As stage plays", "Read aloud on the radio"), Lit, H),

        /* ----------------------------- Technology ---------------------------- */
        q("captcha", "What is a CAPTCHA designed to tell apart?", "Humans from bots", listOf("Adults from children", "Phones from computers", "Real emails from spam"), Tec, E),
        q("qwerty", "Why was the QWERTY layout designed?", "To stop typewriter keys from jamming", listOf("To make typing as fast as possible", "To spell 'typewriter' on one row for sales demos only", "To match the alphabet"), Tec, H),
        q("apollo_pamiec", "The Apollo 11 guidance computer had roughly how much memory?", "About 4 KB of RAM", listOf("About 4 MB of RAM", "About 4 GB of RAM", "No memory at all"), Tec, H),
        q("wifi_skrot", "What does the 'Wi' in Wi-Fi actually stand for?", "Nothing - it's a brand name", listOf("Wireless fidelity, like hi-fi", "Wide-area frequency", "Wave-integrated fibre"), Tec, X),

        /* ----------------------------- Mythology ----------------------------- */
        q("thor_kozy", "What pulls the Norse god Thor's chariot?", "Two goats", listOf("Two wolves", "Eight-legged horse", "Two ravens"), Myt, M),
        q("syzyf", "What was Sisyphus's punishment in the underworld?", "Rolling a boulder uphill forever", listOf("Being chained to a rock", "Standing in water he could never drink", "Holding up the sky"), Myt, E),
        q("pandora_naczynie", "In the original Greek myth, what did Pandora open?", "A jar", listOf("A box", "A chest", "A door"), Myt, H),
        q("anubis_glowa", "Anubis, Egyptian god of the dead, has the head of which animal?", "A jackal", listOf("A falcon", "A crocodile", "An ibis"), Myt, E),
        q("minotaur_miasto", "In which city's labyrinth was the Minotaur kept?", "Knossos, on Crete", listOf("Athens", "Sparta", "Troy"), Myt, M)
    )
}
