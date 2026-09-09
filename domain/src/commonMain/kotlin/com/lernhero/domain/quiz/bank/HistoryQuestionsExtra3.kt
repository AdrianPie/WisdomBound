package com.lernhero.domain.quiz.bank

import com.lernhero.domain.quiz.QuizCategory
import com.lernhero.domain.quiz.QuizDifficulty
import com.lernhero.domain.quiz.QuizEntry

/**
 * Historia bez Europy w centrum.
 *
 * Powstala, gdy watki polskie wyjechaly z History do [PolandQuestions] - ale nie jako zwykla
 * latanina dziury po nich. Bank mial silne przechylenie ku Europie i XX wiekowi, wiec zamiast
 * dopisac kolejne daty z frontu zachodniego, ta partia celowo idzie w Azje, Afryke, obie Ameryki
 * i imperia, o ktorych europejski podrecznik wspomina zdaniem. Gracz z dowolnego kraju ma tu
 * szanse rozpoznac cos swojego.
 */
object HistoryQuestionsExtra3 {

    private fun ask(
        id: String,
        prompt: String,
        correct: String,
        wrong: List<String>,
        difficulty: QuizDifficulty
    ) = QuizEntry(
        id = "his4_$id",
        prompt = prompt,
        correct = correct,
        wrong = wrong,
        category = QuizCategory.History,
        difficulty = difficulty
    )

    val all: List<QuizEntry> = listOf(

        /* ------------------------------- Easy ------------------------------- */
        ask("terakotowa_armia", "In which country does the Terracotta Army guard an emperor's tomb?", "China", listOf("Japan", "India", "Korea"), QuizDifficulty.Easy),
        ask("samuraj_kraj", "In which country did the samurai serve?", "Japan", listOf("China", "Korea", "Vietnam"), QuizDifficulty.Easy),
        ask("tenochtitlan_kto", "Which empire was ruled from the city of Tenochtitlan?", "The Aztec empire", listOf("The Inca empire", "The Maya city-states", "The Olmec"), QuizDifficulty.Easy),
        ask("gandhi_metoda", "What method did Mahatma Gandhi use against British rule?", "Non-violent resistance", listOf("Guerrilla warfare", "A naval blockade", "A palace coup"), QuizDifficulty.Easy),
        ask("mandela_kraj", "In which country was Nelson Mandela president?", "South Africa", listOf("Kenya", "Nigeria", "Ghana"), QuizDifficulty.Easy),
        ask("apartheid_co", "What was apartheid?", "A system of enforced racial segregation", listOf("A trade agreement", "A style of architecture", "A form of taxation"), QuizDifficulty.Easy),
        ask("sfinks_kraj", "In which country does the Great Sphinx stand?", "Egypt", listOf("Iraq", "Greece", "Mexico"), QuizDifficulty.Easy),
        ask("titanic_rok_wiek", "In which century did the Titanic sink?", "The 20th century", listOf("The 19th century", "The 18th century", "The 21st century"), QuizDifficulty.Easy),
        ask("karawany_sahara", "Which animal made long-distance trade routes across the Sahara possible?", "The camel", listOf("The horse", "The ox", "The donkey"), QuizDifficulty.Easy),
        ask("aztekowie_kraj", "The Aztec empire lay in the territory of which present-day country?", "Mexico", listOf("Peru", "Chile", "Guatemala"), QuizDifficulty.Easy),

        /* ------------------------------ Medium ------------------------------ */
        ask("meiji_co", "What did Japan's Meiji Restoration bring?", "Rapid modernization and industrialization", listOf("A return to the shogunate", "Conquest by China", "The abolition of the emperor"), QuizDifficulty.Medium),
        ask("simon_bolivar", "Simon Bolivar is known for liberating which continent's colonies?", "South America", listOf("Africa", "Southeast Asia", "Central Europe"), QuizDifficulty.Medium),
        ask("kanal_sueski_rok", "In which decade was the Suez Canal opened?", "The 1860s", listOf("The 1830s", "The 1890s", "The 1910s"), QuizDifficulty.Medium),
        ask("wojna_opiumowa_kto", "The Opium Wars were fought between China and which power?", "Britain", listOf("Russia", "Portugal", "The Netherlands"), QuizDifficulty.Medium),
        ask("indie_niepodleglosc", "In which year did India gain independence?", "1947", listOf("1935", "1950", "1962"), QuizDifficulty.Medium),
        ask("bizancjum_upadek", "In which year did Constantinople fall to the Ottomans?", "1453", listOf("1204", "1492", "1389"), QuizDifficulty.Medium),
        ask("hammurabi_co", "What is Hammurabi best known for?", "One of the earliest written law codes", listOf("Building the pyramids", "Inventing the alphabet", "Founding Athens"), QuizDifficulty.Medium),
        ask("czarna_smierc_wiek", "In which century did the Black Death sweep Europe?", "The 14th century", listOf("The 12th century", "The 16th century", "The 17th century"), QuizDifficulty.Medium),
        ask("zlota_horda_kto", "The Golden Horde was a state founded by which people?", "The Mongols", listOf("The Ottomans", "The Vikings", "The Magyars"), QuizDifficulty.Medium),
        ask("wojna_krymska_strony", "In the Crimean War, Russia fought a coalition led by which two powers?", "Britain and France", listOf("Prussia and Austria", "Spain and Portugal", "Sweden and Denmark"), QuizDifficulty.Medium),
        ask("apollo_program_cel", "What was the goal of the Apollo program?", "Landing humans on the Moon", listOf("Building a space station", "Reaching Mars", "Mapping Venus"), QuizDifficulty.Medium),

        /* ------------------------------ Hard ------------------------------- */
        ask("traktat_tordesillas", "What did the 1494 Treaty of Tordesillas divide?", "Newly discovered lands between Spain and Portugal", listOf("The Holy Roman Empire", "The Baltic trade", "Italy between France and Austria"), QuizDifficulty.Hard),
        ask("wielki_zimbabwe", "Great Zimbabwe was the centre of a state on which continent?", "Africa", listOf("South America", "Asia", "Australia"), QuizDifficulty.Hard),
        ask("dynastia_mogolow", "The Mughal Empire ruled mainly over which region?", "The Indian subcontinent", listOf("Persia", "Anatolia", "Central China"), QuizDifficulty.Hard),
        ask("sakoku_japonia", "What was Japan's sakoku policy?", "Near-total isolation from the outside world", listOf("Forced conversion to Buddhism", "Land reform for peasants", "An alliance with Portugal"), QuizDifficulty.Hard),
        ask("rewolucja_haiti", "Haiti's revolution is notable as the first successful large-scale revolt of which group?", "Enslaved people", listOf("Colonial merchants", "Indigenous nobility", "Naval officers"), QuizDifficulty.Hard),
        ask("kalifat_abbasydzki_stolica", "Which city was the Abbasid Caliphate's capital?", "Baghdad", listOf("Damascus", "Cairo", "Cordoba"), QuizDifficulty.Hard),
        ask("utrecht_traktat", "Which treaty ended the War of the Spanish Succession?", "The Treaty of Utrecht", listOf("The Peace of Westphalia", "The Congress of Vienna", "The Peace of Augsburg"), QuizDifficulty.Hard),
        ask("angkor_kraj", "Angkor Wat stands in which present-day country?", "Cambodia", listOf("Thailand", "Vietnam", "Myanmar"), QuizDifficulty.Hard),
        ask("wielki_glod_irlandia", "The Great Famine of the 1840s struck which country hardest?", "Ireland", listOf("Scotland", "Portugal", "Norway"), QuizDifficulty.Hard),
        ask("kongres_berlinski_afryka", "What did the Berlin Conference of 1884-1885 regulate?", "The European partition of Africa", listOf("Reparations after a world war", "Borders in the Balkans", "Trade on the Baltic"), QuizDifficulty.Hard),

        /* ----------------------------- Expert ---------------------------- */
        ask("bitwa_talas", "The 751 Battle of Talas was fought between the Abbasids and which empire?", "Tang China", listOf("The Byzantine Empire", "The Sassanid Empire", "The Kingdom of Tibet"), QuizDifficulty.Expert),
        ask("edykt_mediolanski", "What did the Edict of Milan of 313 grant?", "Freedom of worship for Christians in Rome", listOf("Citizenship to all provincials", "The division of the empire", "Tax relief for Egypt"), QuizDifficulty.Expert),
        ask("imperium_songhaj", "The Songhai Empire controlled trade around which river?", "The Niger", listOf("The Congo", "The Zambezi", "The Orange"), QuizDifficulty.Expert),
        ask("wojna_boerska", "The Boer Wars were fought between Britain and settlers of what descent?", "Dutch", listOf("Portuguese", "German", "French"), QuizDifficulty.Expert),
        ask("rewolta_bokserow", "Against whom was the Boxer Rebellion directed?", "Foreign influence in China", listOf("The Japanese emperor", "The Mughal court", "Russian serfdom"), QuizDifficulty.Expert),
        ask("kodeks_justyniana_nazwa", "By what name is the great compilation of Roman law known?", "Corpus Juris Civilis", listOf("Lex Salica", "Codex Theodosianus", "Digesta Gothica"), QuizDifficulty.Expert),
        ask("dynastia_tokugawa_miasto", "Which city was the seat of the Tokugawa shogunate?", "Edo", listOf("Kyoto", "Osaka", "Nara"), QuizDifficulty.Expert),
        ask("wielki_skok_naprzod", "What was China's Great Leap Forward meant to achieve?", "Rapid industrialization and collectivization", listOf("Opening trade with the West", "A return to imperial rule", "Demilitarization"), QuizDifficulty.Expert),
        ask("imperium_ottomanskie_szczyt", "Under which sultan did the Ottoman Empire reach its greatest expansion in Europe?", "Suleiman the Magnificent", listOf("Osman I", "Mehmed II", "Selim III"), QuizDifficulty.Expert),
        ask("traktat_nerczynski", "The 1689 Treaty of Nerchinsk settled a border between Russia and which state?", "Qing China", listOf("Persia", "The Ottoman Empire", "Sweden"), QuizDifficulty.Expert)
    )
}
