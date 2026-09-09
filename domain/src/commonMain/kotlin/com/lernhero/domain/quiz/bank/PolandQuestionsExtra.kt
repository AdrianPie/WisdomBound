package com.lernhero.domain.quiz.bank

import com.lernhero.domain.quiz.QuizCategory
import com.lernhero.domain.quiz.QuizDifficulty
import com.lernhero.domain.quiz.QuizEntry

/**
 * Druga partia tematu lokalnego - patrz [PolandQuestions] po powod istnienia calej kategorii.
 *
 * Przenosiny z History/Literature/Geography daly prawie same daty i lektury szkolne, wiec ta
 * partia celowo idzie szerzej: kultura, muzyka, kuchnia, przyroda, wspolczesnosc. Kategoria ma byc
 * dla gracza z Polski przyjemnoscia rozpoznawania swojego podworka, a nie druga klasowka
 * z historii.
 */
object PolandQuestionsExtra {

    private fun ask(
        id: String,
        prompt: String,
        correct: String,
        wrong: List<String>,
        difficulty: QuizDifficulty
    ) = QuizEntry(
        id = "pol2_$id",
        prompt = prompt,
        correct = correct,
        wrong = wrong,
        category = QuizCategory.Poland,
        difficulty = difficulty
    )

    val all: List<QuizEntry> = listOf(

        /* ------------------------------- Easy ------------------------------- */
        ask("waluta", "What is the currency of Poland?", "The zloty", listOf("The koruna", "The forint", "The euro"), QuizDifficulty.Easy),
        ask("morze_polnoc", "Which sea washes Poland's northern coast?", "The Baltic Sea", listOf("The North Sea", "The Black Sea", "The Adriatic Sea"), QuizDifficulty.Easy),
        ask("godlo_zwierze", "Which bird appears on Poland's coat of arms?", "A white eagle", listOf("A raven", "A falcon", "A swan"), QuizDifficulty.Easy),
        ask("kolory_flagi", "What are the colours of the Polish flag?", "White and red", listOf("Red and green", "White and blue", "Red and yellow"), QuizDifficulty.Easy),
        ask("najdluzsza_rzeka", "What is the longest river in Poland?", "The Vistula", listOf("The Oder", "The Warta", "The Bug"), QuizDifficulty.Easy),
        ask("najwieksze_miasto", "What is the largest city in Poland by population?", "Warsaw", listOf("Krakow", "Lodz", "Wroclaw"), QuizDifficulty.Easy),
        ask("chopin_instrument", "Which instrument did Frederic Chopin compose for above all?", "The piano", listOf("The violin", "The organ", "The cello"), QuizDifficulty.Easy),
        ask("pierogi_co", "What are pierogi?", "Filled dumplings", listOf("A cabbage soup", "A rye bread", "A smoked cheese"), QuizDifficulty.Easy),
        ask("zubr_gdzie", "In which forest does Poland's best-known bison population live?", "The Bialowieza Forest", listOf("The Kampinos Forest", "The Tuchola Forest", "The Augustow Forest"), QuizDifficulty.Easy),
        ask("wejscie_ue", "In which year did Poland join the European Union?", "2004", listOf("1999", "2007", "2011"), QuizDifficulty.Easy),

        /* ------------------------------ Medium ------------------------------ */
        ask("najwyzszy_szczyt", "What is the highest peak in Poland?", "Rysy", listOf("Sniezka", "Babia Gora", "Giewont"), QuizDifficulty.Medium),
        ask("kopalnia_soli", "Which Polish salt mine is a UNESCO World Heritage site?", "Wieliczka", listOf("Klodawa", "Bochnia alone", "Inowroclaw"), QuizDifficulty.Medium),
        ask("nato_rok", "In which year did Poland join NATO?", "1999", listOf("1991", "2004", "1997"), QuizDifficulty.Medium),
        ask("hymn_nazwa", "What is the Polish national anthem called?", "Dabrowski's Mazurka", listOf("Rota", "Boze cos Polske", "Warszawianka"), QuizDifficulty.Medium),
        ask("noblistki_ile", "How many Nobel Prizes did Maria Sklodowska-Curie win?", "Two", listOf("One", "Three", "Four"), QuizDifficulty.Medium),
        ask("papiez_polak", "Which Pole became pope in 1978?", "John Paul II", listOf("John XXIII", "Paul VI", "Benedict XVI"), QuizDifficulty.Medium),
        ask("walesa_funkcja", "What office did Lech Walesa hold in the 1990s?", "President of Poland", listOf("Prime Minister", "Speaker of the Sejm", "Mayor of Gdansk"), QuizDifficulty.Medium),
        ask("wisla_ujscie", "Into which sea does the Vistula flow?", "The Baltic Sea", listOf("The North Sea", "The Black Sea", "The Sea of Azov"), QuizDifficulty.Medium),
        ask("bigos_co", "What is bigos?", "A stew of sauerkraut, cabbage and meat", listOf("A cold beetroot soup", "A poppy-seed cake", "A potato pancake"), QuizDifficulty.Medium),
        ask("skoki_narciarskie_obiekt", "In which Polish town is the Wielka Krokiew ski jumping hill?", "Zakopane", listOf("Szczyrk", "Karpacz", "Wisla"), QuizDifficulty.Medium),
        ask("uniwersytet_najstarszy", "Which is the oldest university in Poland?", "The Jagiellonian University", listOf("The University of Warsaw", "Adam Mickiewicz University", "The University of Wroclaw"), QuizDifficulty.Medium),

        /* ------------------------------ Hard ------------------------------- */
        ask("malbork_co", "What is Malbork best known for?", "The largest brick castle in the world", listOf("A salt mine", "A Gothic cathedral", "A Renaissance town hall"), QuizDifficulty.Hard),
        ask("rejewski_co", "What did Marian Rejewski achieve in the 1930s?", "The first break of the Enigma cipher", listOf("The first Polish radio broadcast", "The design of a jet engine", "A theory of radioactivity"), QuizDifficulty.Hard),
        ask("penderecki_dziedzina", "In which field was Krzysztof Penderecki famous?", "Classical composition", listOf("Film directing", "Architecture", "Painting"), QuizDifficulty.Hard),
        ask("wajda_dziedzina", "What was Andrzej Wajda known for?", "Film directing", listOf("Poetry", "Sculpture", "Conducting"), QuizDifficulty.Hard),
        ask("szymborska_nobel_rok", "In which year did Wislawa Szymborska win the Nobel Prize in Literature?", "1996", listOf("1980", "1991", "2004"), QuizDifficulty.Hard),
        ask("konstytucja_1997", "In which year was Poland's current constitution adopted?", "1997", listOf("1989", "1992", "2004"), QuizDifficulty.Hard),
        ask("okragly_stol_rok", "In which year did the Round Table Talks take place?", "1989", listOf("1980", "1981", "1990"), QuizDifficulty.Hard),
        ask("stan_wojenny_rok", "In which year was martial law introduced in Poland?", "1981", listOf("1976", "1980", "1983"), QuizDifficulty.Hard),
        ask("bory_tucholskie_co", "What are the Tuchola Forests?", "One of Poland's largest forest complexes", listOf("A mountain range", "A lake district in Masuria", "A coastal dune belt"), QuizDifficulty.Hard),

        /* ----------------------------- Expert ---------------------------- */
        ask("banach_dziedzina", "In which field did Stefan Banach work?", "Mathematics", listOf("Chemistry", "Medicine", "Astronomy"), QuizDifficulty.Expert),
        ask("szkola_lwowsko_warszawska", "The Lwow-Warsaw School was a school of what?", "Logic and philosophy", listOf("Painting", "Architecture", "Military engineering"), QuizDifficulty.Expert),
        ask("statut_wislicki", "Which king issued the Statutes of Wislica and Piotrkow?", "Casimir the Great", listOf("Wladyslaw the Elbow-high", "Louis of Hungary", "Wladyslaw Jagiello"), QuizDifficulty.Expert),
        ask("elekcja_pierwsza", "Who was the first king elected in a free election in Poland?", "Henry of Valois", listOf("Stephen Bathory", "Sigismund III Vasa", "Wladyslaw IV"), QuizDifficulty.Expert),
        ask("komisja_edukacji", "What was the Commission of National Education (1773) notable for?", "One of the first state education ministries in Europe", listOf("Founding the first university", "Reforming the army", "Printing the first Polish newspaper"), QuizDifficulty.Expert),
        ask("bitwa_pod_cecora", "Near which town did Hetman Zolkiewski fall in 1620?", "Cecora", listOf("Chocim", "Beresteczko", "Zborow"), QuizDifficulty.Expert),
        ask("polska_ludowa_nazwa", "What was the official name of the Polish state between 1952 and 1989?", "The Polish People's Republic", listOf("The Second Polish Republic", "The Congress Kingdom", "The Polish Commonwealth"), QuizDifficulty.Expert),
        ask("traktat_ryski", "The 1921 Treaty of Riga ended a war between Poland and which state?", "Soviet Russia", listOf("Germany", "Lithuania", "Czechoslovakia"), QuizDifficulty.Expert),
        ask("gdynia_port_rok", "In which decade was the port of Gdynia built from scratch?", "The 1920s", listOf("The 1900s", "The 1940s", "The 1950s"), QuizDifficulty.Expert),
        ask("konstytucja_kwietniowa", "What is the Polish constitution of 1935 usually called?", "The April Constitution", listOf("The March Constitution", "The Small Constitution", "The Silent Constitution"), QuizDifficulty.Expert)
    )
}
