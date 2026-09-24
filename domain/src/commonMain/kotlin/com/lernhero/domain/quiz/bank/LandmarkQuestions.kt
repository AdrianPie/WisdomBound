package com.lernhero.domain.quiz.bank

import com.lernhero.domain.quiz.QuizCategory
import com.lernhero.domain.quiz.QuizDifficulty
import com.lernhero.domain.quiz.QuizEntry

/**
 * Zabytki, rzeki, ciesniny i mniej oczywiste stolice.
 *
 * Geografia w banku konczyla sie na tym, co widac na mapie politycznej. Tymczasem w quizach
 * najlepiej zapadaja w pamiec pytania o KONKRETNE miejsce - Petra, Uluru, Machu Picchu - bo gracz
 * zwykle widzial zdjecie i tylko nie wiedzial, gdzie to jest. To pytania, po ktorych sie czegos
 * uczy, a nie tylko sprawdza.
 *
 * Stolice sa tu wylacznie te NIEOCZYWISTE (Valletta, Lublana, siedziba rzadu Boliwii) - reszta
 * siedzi juz w [CapitalQuestions] i powtarzanie ich byloby dokladaniem banalow, na ktore bank
 * i tak cierpial.
 */
object LandmarkQuestions {

    private fun ask(
        id: String,
        prompt: String,
        correct: String,
        wrong: List<String>,
        category: QuizCategory,
        difficulty: QuizDifficulty
    ) = QuizEntry(
        id = "lmk_$id",
        prompt = prompt,
        correct = correct,
        wrong = wrong,
        category = category,
        difficulty = difficulty
    )

    val all: List<QuizEntry> = listOf(

        /* ------------------ Miejsca na mapie i nieoczywiste stolice ---------------- */
        ask("serengeti_kraj", "In which country is the Serengeti National Park?", "Tanzania", listOf("Kenya", "Uganda", "Zambia"), QuizCategory.Geography, QuizDifficulty.Medium),
        ask("atacama_kraj", "In which country is the Atacama Desert?", "Chile", listOf("Peru", "Bolivia", "Argentina"), QuizCategory.Geography, QuizDifficulty.Medium),
        ask("morze_martwe_kraje", "The Dead Sea lies between Israel and which other country?", "Jordan", listOf("Egypt", "Syria", "Lebanon"), QuizCategory.Geography, QuizDifficulty.Medium),
        ask("blekitny_nil_zrodlo", "In which country does the Blue Nile rise?", "Ethiopia", listOf("Sudan", "Uganda", "Kenya"), QuizCategory.Geography, QuizDifficulty.Hard),
        ask("stolica_malta", "What is the capital of Malta?", "Valletta", listOf("Mdina", "Sliema", "Birgu"), QuizCategory.Capitals, QuizDifficulty.Hard),
        ask("stolica_sri_lanka_handlowa", "Which city is Sri Lanka's largest city and commercial centre?", "Colombo", listOf("Kandy", "Galle", "Jaffna"), QuizCategory.Capitals, QuizDifficulty.Hard),
        ask("delta_dunaju", "In which country is the Danube delta?", "Romania", listOf("Bulgaria", "Ukraine", "Serbia"), QuizCategory.Geography, QuizDifficulty.Hard),
        ask("pompeje_wulkan", "Which ancient city did Vesuvius bury in AD 79?", "Pompeii", listOf("Ostia", "Capua", "Syracuse"), QuizCategory.History, QuizDifficulty.Easy),
        ask("dolina_loary", "In which country is the Loire Valley, famous for its chateaux?", "France", listOf("Belgium", "Switzerland", "Spain"), QuizCategory.Geography, QuizDifficulty.Medium),
        ask("bosfor_laczy", "Which strait connects the Black Sea with the Sea of Marmara?", "The Bosphorus", listOf("The Dardanelles", "The Strait of Kerch", "The Strait of Otranto"), QuizCategory.Geography, QuizDifficulty.Hard),
        ask("masai_mara", "In which country is the Maasai Mara reserve?", "Kenya", listOf("Tanzania", "Botswana", "Namibia"), QuizCategory.Geography, QuizDifficulty.Hard),
        ask("jezioro_afryka_najwieksze", "Which is the largest lake in Africa by area?", "Lake Victoria", listOf("Lake Tanganyika", "Lake Malawi", "Lake Chad"), QuizCategory.Geography, QuizDifficulty.Medium),
        ask("okawango_kraj", "In which country is the Okavango Delta?", "Botswana", listOf("Namibia", "Zambia", "Angola"), QuizCategory.Geography, QuizDifficulty.Expert),

        /* ------------------------- Zabytki i miejsca ze zdjec ---------------------- */
        ask("galapagos_kraj", "The Galapagos Islands belong to which country?", "Ecuador", listOf("Peru", "Colombia", "Chile"), QuizCategory.Geography, QuizDifficulty.Medium),
        ask("tadz_mahal_kraj", "In which country is the Taj Mahal?", "India", listOf("Pakistan", "Iran", "Bangladesh"), QuizCategory.Geography, QuizDifficulty.Easy),
        ask("brama_brandenburska", "In which city is the Brandenburg Gate?", "Berlin", listOf("Munich", "Vienna", "Hamburg"), QuizCategory.Geography, QuizDifficulty.Easy),
        ask("chrystus_odkupiciel", "In which country does the statue of Christ the Redeemer stand?", "Brazil", listOf("Portugal", "Argentina", "Mexico"), QuizCategory.Geography, QuizDifficulty.Easy),
        ask("stonehenge_kraj", "In which country is Stonehenge?", "The United Kingdom", listOf("Ireland", "France", "Denmark"), QuizCategory.Geography, QuizDifficulty.Easy),
        ask("sagrada_familia", "In which city is the Sagrada Familia?", "Barcelona", listOf("Madrid", "Valencia", "Seville"), QuizCategory.Geography, QuizDifficulty.Medium),
        ask("petra_kraj", "In which country is the rock city of Petra?", "Jordan", listOf("Egypt", "Syria", "Israel"), QuizCategory.Geography, QuizDifficulty.Medium),
        ask("blekitny_meczet", "In which city is the Blue Mosque?", "Istanbul", listOf("Cairo", "Isfahan", "Marrakesh"), QuizCategory.Geography, QuizDifficulty.Medium),
        ask("akropol_miasto", "In which city is the Acropolis?", "Athens", listOf("Rome", "Istanbul", "Alexandria"), QuizCategory.Geography, QuizDifficulty.Easy),
        ask("moai_kraj", "The Moai statues of Easter Island belong to which country?", "Chile", listOf("Peru", "New Zealand", "Argentina"), QuizCategory.Geography, QuizDifficulty.Hard),
        ask("mount_rushmore", "In which country is Mount Rushmore?", "The United States", listOf("Canada", "Mexico", "Australia"), QuizCategory.Geography, QuizDifficulty.Easy),
        ask("mala_syrenka", "In which city is the Little Mermaid statue?", "Copenhagen", listOf("Stockholm", "Oslo", "Helsinki"), QuizCategory.Geography, QuizDifficulty.Medium),
        ask("alhambra_kraj", "In which country is the Alhambra?", "Spain", listOf("Morocco", "Portugal", "Tunisia"), QuizCategory.Geography, QuizDifficulty.Medium),
        ask("neuschwanstein", "In which country is Neuschwanstein Castle?", "Germany", listOf("Austria", "Switzerland", "Czechia"), QuizCategory.Geography, QuizDifficulty.Medium),
        ask("ermitaz_miasto", "In which city is the Hermitage Museum?", "Saint Petersburg", listOf("Moscow", "Kyiv", "Warsaw"), QuizCategory.Geography, QuizDifficulty.Hard),
        ask("prado_miasto", "In which city is the Prado Museum?", "Madrid", listOf("Barcelona", "Lisbon", "Rome"), QuizCategory.Geography, QuizDifficulty.Medium),
        ask("rijksmuseum", "In which city is the Rijksmuseum?", "Amsterdam", listOf("Brussels", "Rotterdam", "Antwerp"), QuizCategory.Geography, QuizDifficulty.Hard),
        ask("uluru_kraj", "In which country is Uluru?", "Australia", listOf("New Zealand", "South Africa", "Namibia"), QuizCategory.Geography, QuizDifficulty.Medium),
        ask("chichen_itza", "In which country is Chichen Itza?", "Mexico", listOf("Guatemala", "Peru", "Honduras"), QuizCategory.Geography, QuizDifficulty.Medium),

        /* --------------------------- Rzeki, morza, ciesniny ----------------------- */
        ask("rzeka_paryz", "Which river flows through Paris?", "The Seine", listOf("The Loire", "The Rhone", "The Garonne"), QuizCategory.Geography, QuizDifficulty.Easy),
        ask("rzeka_londyn", "Which river flows through London?", "The Thames", listOf("The Severn", "The Mersey", "The Trent"), QuizCategory.Geography, QuizDifficulty.Easy),
        ask("rzeka_wieden_budapeszt", "Which river flows through both Vienna and Budapest?", "The Danube", listOf("The Rhine", "The Elbe", "The Vltava"), QuizCategory.Geography, QuizDifficulty.Medium),
        ask("ciesnina_gibraltar", "Which strait separates Europe from Africa at its narrowest point?", "The Strait of Gibraltar", listOf("The Bosphorus", "The Strait of Messina", "The Dardanelles"), QuizCategory.Geography, QuizDifficulty.Medium),
        ask("morze_wlochy_balkany", "Which sea lies between Italy and the Balkan peninsula?", "The Adriatic Sea", listOf("The Ionian Sea", "The Aegean Sea", "The Tyrrhenian Sea"), QuizCategory.Geography, QuizDifficulty.Medium),
        ask("gory_europa_azja", "Which mountain range is the conventional border between Europe and Asia?", "The Urals", listOf("The Caucasus", "The Carpathians", "The Altai"), QuizCategory.Geography, QuizDifficulty.Medium),
        ask("polwysep_hiszpania", "Which peninsula do Spain and Portugal share?", "The Iberian Peninsula", listOf("The Apennine Peninsula", "The Balkan Peninsula", "The Jutland Peninsula"), QuizCategory.Geography, QuizDifficulty.Easy),
        ask("otoczony_rpa", "Which country is completely surrounded by South Africa?", "Lesotho", listOf("Eswatini", "Botswana", "Namibia"), QuizCategory.Geography, QuizDifficulty.Hard),
        ask("otoczony_wlochy", "Which country is completely surrounded by Italy?", "San Marino", listOf("Monaco", "Andorra", "Liechtenstein"), QuizCategory.Geography, QuizDifficulty.Hard),
        ask("unesco_najwiecej", "Which country has the most UNESCO World Heritage sites?", "Italy", listOf("France", "Spain", "China"), QuizCategory.Geography, QuizDifficulty.Expert),
        ask("jezioro_wiktorii_kraje", "Lake Victoria is shared by Uganda, Kenya and which third country?", "Tanzania", listOf("Rwanda", "Burundi", "Sudan"), QuizCategory.Geography, QuizDifficulty.Hard),
        ask("gory_maroko", "Which mountain range runs through Morocco and Algeria?", "The Atlas Mountains", listOf("The Rif alone", "The Ahaggar", "The Drakensberg"), QuizCategory.Geography, QuizDifficulty.Medium),
        ask("rzeka_egipt_delta", "On which river does Cairo lie?", "The Nile", listOf("The Euphrates", "The Jordan", "The Tigris"), QuizCategory.Geography, QuizDifficulty.Easy),
        ask("gory_ameryka_pn", "Which mountain range runs down the west of North America?", "The Rocky Mountains", listOf("The Appalachians", "The Sierra Madre", "The Alaska Range"), QuizCategory.Geography, QuizDifficulty.Easy),
        ask("wielka_rafa", "Off the coast of which country is the Great Barrier Reef?", "Australia", listOf("Indonesia", "The Philippines", "Fiji"), QuizCategory.Geography, QuizDifficulty.Easy),
        ask("fiordy_kraj", "Which country is best known for its fjords?", "Norway", listOf("Sweden", "Finland", "Denmark"), QuizCategory.Geography, QuizDifficulty.Easy)
    )
}
