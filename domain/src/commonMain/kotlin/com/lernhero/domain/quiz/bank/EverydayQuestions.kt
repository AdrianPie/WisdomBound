package com.lernhero.domain.quiz.bank

import com.lernhero.domain.quiz.QuizCategory
import com.lernhero.domain.quiz.QuizDifficulty
import com.lernhero.domain.quiz.QuizEntry

/**
 * Wiedza CODZIENNA: kuchnia, dom, muzyka, gry, sport od strony kibica, technologia od strony
 * uzytkownika.
 *
 * Bank byl dotad zbudowany jak podrecznik - historia, biologia, pierwiastki. Tymczasem w quizach
 * najlepiej dzialaja pytania o rzeczy, ktore gracz ma pod reka i o ktorych nigdy nie pomyslal
 * ("dlaczego rosnie ciasto?", "ile strun ma gitara?"). Nie sa latwiejsze - sa BLIZSZE, a przez to
 * pamieta sie je po rozgrywce.
 *
 * Swiadomie pominiete sa pytania, ktore odpowiadaja same sobie ("ile pierscieni ma flaga
 * olimpijska") - to byla najczestsza wada starego banku i nie ma sensu jej tu powielac.
 */
object EverydayQuestions {

    private fun ask(
        id: String,
        prompt: String,
        correct: String,
        wrong: List<String>,
        category: QuizCategory,
        difficulty: QuizDifficulty
    ) = QuizEntry(
        id = "day_$id",
        prompt = prompt,
        correct = correct,
        wrong = wrong,
        category = category,
        difficulty = difficulty
    )

    val all: List<QuizEntry> = listOf(

        /* -------------------------- Nauka w kuchni i w domu ------------------------ */
        ask("ciasto_rosnie", "Why does bread dough rise?", "Yeast releases carbon dioxide", listOf("Water evaporates from it", "Gluten expands when heated", "Salt draws in moisture"), QuizCategory.Science, QuizDifficulty.Medium),
        ask("papryka_ostra", "Which compound makes chilli peppers hot?", "Capsaicin", listOf("Menthol", "Tannin", "Caffeine"), QuizCategory.Science, QuizDifficulty.Medium),
        ask("jablko_brazowieje", "Why does a cut apple turn brown?", "Its compounds oxidize in air", listOf("It starts to ferment", "Bacteria colonise it", "It loses water"), QuizCategory.Science, QuizDifficulty.Medium),
        ask("cebula_lzy", "Why does cutting an onion make your eyes water?", "It releases a sulfur compound that irritates the eyes", listOf("It releases pure ammonia", "Its acid evaporates", "Its dust scratches the eye"), QuizCategory.Science, QuizDifficulty.Medium),
        ask("suchy_lod", "What is dry ice?", "Frozen carbon dioxide", listOf("Very cold water ice", "Frozen nitrogen", "Compressed helium"), QuizCategory.Science, QuizDifficulty.Medium),
        ask("lod_plywa", "Why does ice float on water?", "Ice is less dense than liquid water", listOf("Ice is warmer than water", "Air bubbles hold it up", "Water pushes it up by surface tension"), QuizCategory.Science, QuizDifficulty.Medium),
        ask("szklo_surowiec", "What is the main raw material of glass?", "Sand", listOf("Limestone", "Clay", "Quartz crystal"), QuizCategory.Science, QuizDifficulty.Medium),
        ask("soda_wzor", "What is the chemical formula of baking soda?", "NaHCO3", listOf("NaCl", "CaCO3", "KNO3"), QuizCategory.Elements, QuizDifficulty.Hard),
        ask("miedz_przewody", "Which metal is used in most household electrical wiring?", "Copper", listOf("Aluminium", "Iron", "Zinc"), QuizCategory.Elements, QuizDifficulty.Easy),
        ask("katalizator_rola", "What does a catalyst do in a chemical reaction?", "Speeds it up without being consumed", listOf("Provides all the energy for it", "Turns into the product", "Stops it once it starts"), QuizCategory.Science, QuizDifficulty.Hard),
        ask("ph_siedem", "What does a pH of 7 mean?", "The solution is neutral", listOf("The solution is strongly acidic", "The solution is strongly basic", "The solution conducts no current"), QuizCategory.Science, QuizDifficulty.Easy),
        ask("witamina_gotowanie", "Which vitamin is most easily destroyed by cooking?", "Vitamin C", listOf("Vitamin D", "Vitamin K", "Vitamin E"), QuizCategory.Biology, QuizDifficulty.Hard),
        ask("kofeina_mozg", "What does caffeine block in the brain?", "Adenosine receptors", listOf("Dopamine production", "Serotonin uptake", "Oxygen transport"), QuizCategory.Biology, QuizDifficulty.Expert),
        ask("szczepionka_uczy", "What does a vaccine train?", "The immune system", listOf("The nervous system", "The digestive system", "The circulatory system"), QuizCategory.Biology, QuizDifficulty.Easy),
        ask("grupa_krwi_czesta", "Which is the most common blood type worldwide?", "O positive", listOf("A negative", "AB positive", "B negative"), QuizCategory.Biology, QuizDifficulty.Hard),

        /* ------------------------------ Zjawiska wokol nas ------------------------- */
        ask("niebo_niebieskie", "Why is the sky blue?", "Sunlight is scattered by the atmosphere", listOf("The oceans reflect onto it", "Ozone is blue", "Water vapour absorbs red light"), QuizCategory.Science, QuizDifficulty.Medium),
        ask("tecza_powstaje", "What causes a rainbow?", "Light refracting and dispersing in water droplets", listOf("Clouds reflecting sunlight", "Dust scattering light", "The magnetic field bending light"), QuizCategory.Science, QuizDifficulty.Medium),
        ask("zorza_polarna", "What causes the aurora?", "Charged particles from the Sun hitting the atmosphere", listOf("Ice crystals refracting moonlight", "Lightning high in the atmosphere", "Reflections off the polar ice"), QuizCategory.Science, QuizDifficulty.Hard),
        ask("higrometr_mierzy", "What does a hygrometer measure?", "Air humidity", listOf("Air pressure", "Wind speed", "Rainfall"), QuizCategory.Science, QuizDifficulty.Medium),
        ask("sejsmograf_mierzy", "What does a seismograph record?", "Ground motion during earthquakes", listOf("Ocean currents", "Solar flares", "Volcanic gas output"), QuizCategory.Science, QuizDifficulty.Easy),
        ask("om_jednostka", "What is the unit of electrical resistance?", "The ohm", listOf("The volt", "The ampere", "The watt"), QuizCategory.Science, QuizDifficulty.Medium),

        /* ---------------------------------- Kuchnia -------------------------------- */
        ask("sushi_kraj", "Which country is sushi originally from?", "Japan", listOf("China", "Korea", "Thailand"), QuizCategory.Geography, QuizDifficulty.Easy),
        ask("paella_kraj", "Which country is paella from?", "Spain", listOf("Italy", "Portugal", "Greece"), QuizCategory.Geography, QuizDifficulty.Easy),
        ask("hummus_skladnik", "What is the main ingredient of hummus?", "Chickpeas", listOf("Lentils", "White beans", "Peas"), QuizCategory.Biology, QuizDifficulty.Medium),
        ask("tofu_z_czego", "What is tofu made from?", "Soybeans", listOf("Rice", "Wheat", "Almonds"), QuizCategory.Biology, QuizDifficulty.Easy),
        ask("najdrozsza_przyprawa", "Which spice is the most expensive by weight?", "Saffron", listOf("Vanilla", "Cardamom", "Black pepper"), QuizCategory.Geography, QuizDifficulty.Medium),
        ask("guacamole_skladnik", "What is the main ingredient of guacamole?", "Avocado", listOf("Courgette", "Green pepper", "Broad beans"), QuizCategory.Biology, QuizDifficulty.Easy),
        ask("cydr_z_czego", "Cider is made by fermenting which fruit?", "Apples", listOf("Grapes", "Pears", "Plums"), QuizCategory.Biology, QuizDifficulty.Easy),
        ask("risotto_ryz", "Which rice variety is traditionally used for risotto?", "Arborio", listOf("Basmati", "Jasmine", "Long-grain"), QuizCategory.Geography, QuizDifficulty.Hard),
        ask("wanilia_roslina", "Vanilla flavouring comes from the pods of which plant family?", "An orchid", listOf("A palm", "A cactus", "A fern"), QuizCategory.Biology, QuizDifficulty.Hard),
        ask("marcepan_orzech", "Which nut is marzipan made from?", "Almond", listOf("Hazelnut", "Walnut", "Cashew"), QuizCategory.Biology, QuizDifficulty.Medium),
        ask("pesto_ziolo", "Which herb is the base of pesto alla genovese?", "Basil", listOf("Parsley", "Oregano", "Mint"), QuizCategory.Geography, QuizDifficulty.Medium),

        /* ------------------------------ Muzyka i sztuka ---------------------------- */
        ask("gitara_struny", "How many strings does a standard guitar have?", "Six", listOf("Four", "Five", "Seven"), QuizCategory.Technology, QuizDifficulty.Easy),
        ask("harfa_pedaly", "How many pedals does a modern concert harp have?", "Seven", listOf("Three", "Five", "Nine"), QuizCategory.Technology, QuizDifficulty.Expert),
        ask("sopran_glos", "Which is the highest female singing voice?", "Soprano", listOf("Alto", "Mezzo-soprano", "Contralto"), QuizCategory.Literature, QuizDifficulty.Medium),
        ask("forte_znaczenie", "In music, what does 'forte' mean?", "Loud", listOf("Fast", "Soft", "Slowly"), QuizCategory.Literature, QuizDifficulty.Easy),
        ask("luthier_buduje", "What does a luthier build?", "Stringed instruments", listOf("Organs", "Drums", "Brass instruments"), QuizCategory.Technology, QuizDifficulty.Hard),
        ask("sonet_wersy", "How many lines does a sonnet have?", "Fourteen", listOf("Twelve", "Sixteen", "Ten"), QuizCategory.Literature, QuizDifficulty.Medium),
        ask("zlota_palma", "Which is the top prize at the Cannes Film Festival?", "The Palme d'Or", listOf("The Golden Bear", "The Golden Lion", "The Silver Shell"), QuizCategory.Literature, QuizDifficulty.Medium),
        ask("zloty_niedzwiedz", "At which film festival is the Golden Bear awarded?", "Berlin", listOf("Venice", "Cannes", "Toronto"), QuizCategory.Literature, QuizDifficulty.Hard),
        ask("zloty_lew", "At which film festival is the Golden Lion awarded?", "Venice", listOf("Cannes", "Berlin", "San Sebastian"), QuizCategory.Literature, QuizDifficulty.Hard),

        /* ------------------------------- Gry i rozrywka ---------------------------- */
        ask("szachownica_pola", "How many squares does a chessboard have?", "64", listOf("81", "100", "49"), QuizCategory.Math, QuizDifficulty.Easy),
        ask("kostka_oczka", "How many pips are there in total on a standard six-sided die?", "21", listOf("18", "24", "27"), QuizCategory.Math, QuizDifficulty.Medium),
        ask("blackjack_liczba", "In which card game do players aim for a total of 21?", "Blackjack", listOf("Poker", "Bridge", "Rummy"), QuizCategory.Math, QuizDifficulty.Easy),
        ask("minecraft_sprzedaz", "Which is the best-selling video game of all time?", "Minecraft", listOf("Tetris", "Grand Theft Auto V", "Wii Sports"), QuizCategory.Technology, QuizDifficulty.Medium),
        ask("mario_firma", "Which company created the Mario series?", "Nintendo", listOf("Sega", "Sony", "Atari"), QuizCategory.Technology, QuizDifficulty.Easy),
        ask("pong_gra", "Which early arcade game was the first commercial hit?", "Pong", listOf("Pac-Man", "Space Invaders", "Breakout"), QuizCategory.Technology, QuizDifficulty.Hard),
        ask("netflix_poczatki", "Which streaming service started out renting DVDs by mail?", "Netflix", listOf("Hulu", "Disney+", "Amazon Prime Video"), QuizCategory.Technology, QuizDifficulty.Medium),
        ask("hashtag_serwis", "On which platform did the hashtag first catch on?", "Twitter", listOf("Facebook", "Instagram", "MySpace"), QuizCategory.Technology, QuizDifficulty.Hard),

        /* ---------------------------------- Sport ---------------------------------- */
        ask("golf_dolki", "How many holes are played in a standard round of golf?", "18", listOf("9", "12", "24"), QuizCategory.Sport, QuizDifficulty.Easy),
        ask("golf_birdie", "In which sport is a 'birdie' scored?", "Golf", listOf("Cricket", "Archery", "Badminton"), QuizCategory.Sport, QuizDifficulty.Easy),
        ask("hokej_tercje", "How many periods are played in an ice hockey game?", "Three", listOf("Two", "Four", "Five"), QuizCategory.Sport, QuizDifficulty.Medium),
        ask("boks_runda", "How long is one round in professional men's boxing?", "Three minutes", listOf("Two minutes", "Four minutes", "Five minutes"), QuizCategory.Sport, QuizDifficulty.Medium),
        ask("kola_olimpijskie_znaczenie", "What do the five Olympic rings represent?", "The five inhabited continents", listOf("The five founding countries", "The five original sports", "The five Olympic virtues"), QuizCategory.Sport, QuizDifficulty.Medium),
        ask("giro_koszulka", "What colour is the leader's jersey in the Giro d'Italia?", "Pink", listOf("Yellow", "Red", "Blue"), QuizCategory.Sport, QuizDifficulty.Hard),

        /* -------------------------------- Technologia ------------------------------ */
        ask("http_skrot", "What does HTTP stand for?", "Hypertext transfer protocol", listOf("High-throughput transfer protocol", "Hyperlink text transport process", "Host transfer to protocol"), QuizCategory.Technology, QuizDifficulty.Medium),
        ask("gps_skrot", "What does GPS stand for?", "Global positioning system", listOf("General position service", "Geographic pointing system", "Global path sensor"), QuizCategory.Technology, QuizDifficulty.Easy),
        ask("piatka_g", "In '5G', what does the G stand for?", "Generation", listOf("Gigahertz", "Global", "Gateway"), QuizCategory.Technology, QuizDifficulty.Medium),
        ask("bity_w_bajcie", "How many bits are in one byte?", "Eight", listOf("Four", "Ten", "Sixteen"), QuizCategory.Technology, QuizDifficulty.Easy),
        ask("system_binarny", "How many digits does the binary system use?", "Two", listOf("Eight", "Ten", "Sixteen"), QuizCategory.Math, QuizDifficulty.Easy),
        ask("led_skrot", "What does LED stand for?", "Light-emitting diode", listOf("Low-energy device", "Linear electric display", "Layered emission disc"), QuizCategory.Technology, QuizDifficulty.Medium)
    )
}
