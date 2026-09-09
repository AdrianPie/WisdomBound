package com.lernhero.domain.quiz.bank

import com.lernhero.domain.quiz.QuizCategory
import com.lernhero.domain.quiz.QuizDifficulty
import com.lernhero.domain.quiz.QuizEntry

/**
 * Pytania POROWNAWCZE: "gdzie wiecej", "co wieksze", "co odstaje od reszty".
 *
 * Reszta banku pyta o fakt, ktory albo sie zna, albo nie ("stolica Bhutanu"). Tutaj cztery
 * odpowiedzi sa POWIAZANE i trzeba je porownac - a to inna umiejetnosc: gracz, ktory nie zna
 * dokladnej liczby ludnosci Etiopii, wciaz moze wygrac, bo wie, ze Tanzania jest mniejsza.
 * To najpopularniejszy dzis format w quizach ("higher or lower", "odd one out") i jedyny,
 * w ktorym da sie wygrac rozumowaniem zamiast pamiecia.
 *
 * Kazdy dystraktor jest tu SPRAWDZANY pod tym katem, ze roznica miedzy nim a poprawna odpowiedzia
 * jest wyrazna. Porownania na styk (Kanada vs Chiny vs USA po powierzchni, gdzie zrodla same sie
 * nie zgadzaja) sa celowo pominiete - pytanie, o ktore da sie sensownie sprzeczac, jest zepsute.
 */
object GeographyQuestionsCompare {

    private fun ask(
        id: String,
        prompt: String,
        correct: String,
        wrong: List<String>,
        difficulty: QuizDifficulty
    ) = QuizEntry(
        id = "geocmp_$id",
        prompt = prompt,
        correct = correct,
        wrong = wrong,
        category = QuizCategory.Geography,
        difficulty = difficulty
    )

    val all: List<QuizEntry> = listOf(

        /* ------------------------- Gdzie wiecej ludzi - Easy ----------------------- */
        ask("ludnosc_top", "Which of these countries has the largest population?", "India", listOf("The United States", "Indonesia", "Brazil"), QuizDifficulty.Easy),
        ask("ludnosc_afryka", "Which is the most populous country in Africa?", "Nigeria", listOf("Egypt", "Ethiopia", "South Africa"), QuizDifficulty.Easy),
        ask("ludnosc_ameryka_pd", "Which is the most populous country in South America?", "Brazil", listOf("Argentina", "Colombia", "Peru"), QuizDifficulty.Easy),
        ask("ludnosc_ue", "Which is the most populous country in the European Union?", "Germany", listOf("France", "Italy", "Spain"), QuizDifficulty.Easy),
        ask("miasto_najwieksze", "Which of these metropolitan areas has the most inhabitants?", "Tokyo", listOf("New York", "London", "Paris"), QuizDifficulty.Easy),
        ask("kontynent_panstwa", "Which continent has the most countries?", "Africa", listOf("Asia", "Europe", "South America"), QuizDifficulty.Easy),

        /* ------------------------ Gdzie wiecej ludzi - Medium ---------------------- */
        ask("ludnosc_azja_srednia", "Which of these four countries is home to the most people?", "Indonesia", listOf("Brazil", "Nigeria", "Bangladesh"), QuizDifficulty.Medium),
        ask("ludnosc_egipt_niemcy", "Which of these countries counts the most inhabitants?", "Egypt", listOf("Germany", "Turkiye", "Thailand"), QuizDifficulty.Medium),
        ask("ludnosc_etiopia", "Which of these African countries has the largest population?", "Ethiopia", listOf("The Democratic Republic of the Congo", "Tanzania", "Kenya"), QuizDifficulty.Medium),
        ask("ludnosc_najmniej", "Which of these countries has the smallest population?", "Iceland", listOf("Malta", "Luxembourg", "Cyprus"), QuizDifficulty.Medium),
        ask("miasto_azja", "Which of these urban areas is the most populous?", "Delhi", listOf("Bangkok", "Seoul", "Istanbul"), QuizDifficulty.Medium),
        ask("gestosc_zaludnienia", "Which of these has the highest population density?", "Monaco", listOf("Singapore", "Malta", "Bangladesh"), QuizDifficulty.Medium),
        ask("jezyk_ojczysty", "Which language has the most native speakers?", "Mandarin Chinese", listOf("English", "Spanish", "Hindi"), QuizDifficulty.Hard),

        /* --------------------------- Co wieksze - Easy/Medium ---------------------- */
        ask("panstwo_powierzchnia", "Which of these countries is the largest by area?", "Russia", listOf("Canada", "Australia", "India"), QuizDifficulty.Easy),
        ask("pustynia_najwieksza", "Which is the largest hot desert?", "The Sahara", listOf("The Arabian Desert", "The Gobi", "The Kalahari"), QuizDifficulty.Easy),
        ask("panstwo_afryka_pow", "Which is the largest country in Africa by area?", "Algeria", listOf("Sudan", "Libya", "The Democratic Republic of the Congo"), QuizDifficulty.Medium),
        ask("wyspa_druga", "Which of these islands is the largest?", "Borneo", listOf("Madagascar", "Sumatra", "Honshu"), QuizDifficulty.Medium),
        ask("panstwo_ue_pow", "Which is the largest European Union country by area?", "France", listOf("Spain", "Sweden", "Germany"), QuizDifficulty.Hard),
        ask("las_powierzchnia", "Which country has the largest forest area?", "Russia", listOf("Brazil", "Canada", "The United States"), QuizDifficulty.Hard),
        ask("linia_brzegowa", "Which country has the longest coastline?", "Canada", listOf("Russia", "Indonesia", "Australia"), QuizDifficulty.Hard),

        /* ------------------------------ Co dluzsze/wyzsze -------------------------- */
        ask("rzeka_najdluzsza", "Which of these rivers is the longest?", "The Nile", listOf("The Danube", "The Volga", "The Rhine"), QuizDifficulty.Easy),
        ask("szczyt_afryka", "Which is the highest mountain in Africa?", "Kilimanjaro", listOf("Mount Kenya", "The Atlas peaks", "Mount Meru"), QuizDifficulty.Easy),
        ask("szczyt_europa", "Counting the Caucasus, which is the highest peak in Europe?", "Mount Elbrus", listOf("Mont Blanc", "The Matterhorn", "The Zugspitze"), QuizDifficulty.Medium),

        /* --------------------------- Gdzie dalej na polnoc ------------------------- */
        ask("najdalej_polnoc_nordyckie", "Which of these cities lies furthest north?", "Helsinki", listOf("Oslo", "Stockholm", "Copenhagen"), QuizDifficulty.Medium),
        ask("najdalej_polnoc_stolice", "Which of these capitals lies furthest north?", "Reykjavik", listOf("Helsinki", "Oslo", "Tallinn"), QuizDifficulty.Medium),
        ask("najdalej_poludnie", "Which of these cities lies furthest south?", "Auckland", listOf("Sydney", "Cape Town", "Buenos Aires"), QuizDifficulty.Hard),
        ask("stolica_najdalej_poludnie", "Which of these capitals lies furthest south?", "Wellington", listOf("Canberra", "Santiago", "Pretoria"), QuizDifficulty.Hard),
        ask("najdalej_wschod_europa", "Which of these cities lies furthest east?", "Istanbul", listOf("Athens", "Rome", "Vienna"), QuizDifficulty.Medium),
        ask("rownik_kraj", "Which of these countries does the equator cross?", "Ecuador", listOf("Peru", "Bolivia", "Chile"), QuizDifficulty.Medium),
        ask("dwa_kontynenty", "Which of these countries lies on two continents?", "Turkiye", listOf("Greece", "Bulgaria", "Italy"), QuizDifficulty.Medium),

        /* -------------------------------- Odd one out ------------------------------ */
        ask("nie_afryka", "Which of these countries is NOT in Africa?", "Guyana", listOf("Ghana", "Guinea", "Gabon"), QuizDifficulty.Easy),
        ask("nie_wyspa", "Which of these countries is NOT an island nation?", "Nepal", listOf("Malta", "Iceland", "Sri Lanka"), QuizDifficulty.Easy),
        ask("nie_srodladowy", "Which of these countries is NOT landlocked?", "Croatia", listOf("Austria", "Hungary", "Slovakia"), QuizDifficulty.Medium),
        ask("nie_srodziemne", "Which of these countries does NOT border the Mediterranean Sea?", "Portugal", listOf("Spain", "France", "Italy"), QuizDifficulty.Medium),
        ask("nie_ue", "Which of these countries is NOT a member of the European Union?", "Norway", listOf("Sweden", "Finland", "Denmark"), QuizDifficulty.Easy),
        ask("nie_ameryka_pd", "Which of these countries is NOT in South America?", "Belize", listOf("Guyana", "Suriname", "Paraguay"), QuizDifficulty.Hard),
        ask("nie_nordyckie", "Which of these countries is NOT a Nordic country?", "Estonia", listOf("Norway", "Sweden", "Denmark"), QuizDifficulty.Medium),
        ask("nie_stolica", "Which of these cities is NOT a national capital?", "Zurich", listOf("Vienna", "Prague", "Warsaw"), QuizDifficulty.Easy),
        ask("nie_baltyk", "Which of these countries does NOT have a Baltic Sea coast?", "Belarus", listOf("Latvia", "Lithuania", "Estonia"), QuizDifficulty.Medium),
        ask("nie_alpy", "Which of these countries does NOT have Alpine territory?", "Czechia", listOf("Austria", "Switzerland", "Slovenia"), QuizDifficulty.Hard),

        /* ------------------------------ Rekordy i skrajnosci ----------------------- */
        ask("najbardziej_slone", "Which of these bodies of water is the saltiest?", "The Dead Sea", listOf("The Red Sea", "The Mediterranean Sea", "The Baltic Sea"), QuizDifficulty.Medium),
        ask("turystyka_kraj", "Which country receives the most international tourist arrivals?", "France", listOf("Spain", "The United States", "Italy"), QuizDifficulty.Hard),
        ask("kawa_producent", "Which country produces the most coffee?", "Brazil", listOf("Colombia", "Vietnam", "Ethiopia"), QuizDifficulty.Medium),
        ask("eksport_towary", "Which country exports the largest value of goods?", "China", listOf("The United States", "Germany", "Japan"), QuizDifficulty.Hard),
        ask("najzimniejszy_kontynent", "Which is the coldest continent?", "Antarctica", listOf("Asia", "Europe", "North America"), QuizDifficulty.Easy),
        ask("najwyzej_polozona_stolica", "Which of these is the highest-lying capital city?", "La Paz", listOf("Quito", "Bogota", "Mexico City"), QuizDifficulty.Expert),
        ask("najwiecej_wysp", "Which country is made up of the most islands?", "Sweden", listOf("Indonesia", "The Philippines", "Japan"), QuizDifficulty.Expert),
        ask("najglebszy_punkt", "In which ocean is the deepest known point on Earth?", "The Pacific", listOf("The Atlantic", "The Indian Ocean", "The Arctic Ocean"), QuizDifficulty.Medium),
        ask("najwiecej_sasiadow", "Which of these countries borders the most other countries?", "Brazil", listOf("Mexico", "Argentina", "Colombia"), QuizDifficulty.Expert)
    )
}
