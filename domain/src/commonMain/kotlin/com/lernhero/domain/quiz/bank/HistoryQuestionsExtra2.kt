package com.lernhero.domain.quiz.bank

import com.lernhero.domain.quiz.QuizCategory
import com.lernhero.domain.quiz.QuizDifficulty
import com.lernhero.domain.quiz.QuizEntry

/** Third batch of history - geographic discoveries, rulers and wars not yet covered. */
object HistoryQuestionsExtra2 {

    private fun ask(
        id: String,
        prompt: String,
        correct: String,
        wrong: List<String>,
        difficulty: QuizDifficulty
    ) = QuizEntry(
        id = "his3_$id",
        prompt = prompt,
        correct = correct,
        wrong = wrong,
        category = QuizCategory.History,
        difficulty = difficulty
    )

    val all: List<QuizEntry> = listOf(

        /* ------------------------------- Easy ------------------------------- */
        ask("faraon_grob", "Where were pharaohs buried in ancient Egypt?", "In pyramids or tombs", listOf("At sea", "In trees", "Underwater"), QuizDifficulty.Easy),
        ask("zamek_sredniowiecze", "What were castles used for in the Middle Ages?", "Defense and housing rulers", listOf("Only trade", "Producing grain", "Transporting water"), QuizDifficulty.Easy),
        ask("statek_kolumb", "What was one of Columbus's ships called?", "Santa Maria", listOf("Titanic", "Beagle", "Endeavour"), QuizDifficulty.Easy),
        ask("piramidy_material", "What were the great Egyptian pyramids built from?", "Stone blocks", listOf("Wood", "Brick", "Steel"), QuizDifficulty.Easy),
        ask("jedwabny_szlak", "What was the Silk Road?", "A network of trade routes between Asia and Europe", listOf("A river in China", "A knightly order", "A peace treaty"), QuizDifficulty.Easy),
        ask("krzyzowcy_cel", "Where did the Crusades set out for?", "The Holy Land", listOf("America", "China", "Australia"), QuizDifficulty.Easy),
        ask("kolumb_kraj", "On whose behalf did Columbus set out for the Indies?", "The Monarchs of Spain", listOf("The King of Portugal", "The King of England", "The King of France"), QuizDifficulty.Easy),
        ask("piastowie", "What was the first ruling dynasty of Poland called?", "The Piasts", listOf("The Jagiellons", "The Vasas", "The Habsburgs"), QuizDifficulty.Easy),
        ask("jagiellonowie", "Which dynasty ruled Poland after the Piasts (with an Angevin interlude)?", "The Jagiellons", listOf("The Vasas", "The Habsburgs", "The Romanovs"), QuizDifficulty.Easy),
        ask("polska_stolica_pierwsza", "Which city was Poland's first capital?", "Gniezno", listOf("Warsaw", "Krakow", "Poznan"), QuizDifficulty.Easy),

        /* ------------------------------ Medium ------------------------------ */
        ask("krakow_stolica_do", "Until which century was Krakow the capital of Poland?", "The early 17th century", listOf("The 15th century", "The 19th century", "The 20th century"), QuizDifficulty.Medium),
        ask("zygmunt_iii_stolica", "Which king moved Poland's capital to Warsaw?", "Sigismund III Vasa", listOf("Wladyslaw Jagiello", "Casimir the Great", "John III Sobieski"), QuizDifficulty.Medium),
        ask("kazimierz_wielki_przydomek", "What did Casimir the Great earn his epithet for?", "Expanding and building cities in stone", listOf("Conquering Rus alone", "Winning wars against the Teutonic Knights", "Founding Warsaw"), QuizDifficulty.Medium),
        ask("akademia_krakowska", "Who founded the Krakow Academy (today's Jagiellonian University)?", "Casimir the Great", listOf("Wladyslaw Jagiello", "Boleslaw the Brave", "Sigismund the Old"), QuizDifficulty.Medium),
        ask("magellan", "Who led the first expedition to circumnavigate the globe?", "Ferdinand Magellan", listOf("Christopher Columbus", "Vasco da Gama", "James Cook"), QuizDifficulty.Medium),
        ask("cook_odkrycia", "Which continent/islands did Captain James Cook explore?", "Australia and the Pacific Islands", listOf("South America", "Africa", "Antarctica, as the first to land"), QuizDifficulty.Medium),
        ask("wielka_wojna_polnocna", "Which powers fought the Great Northern War?", "Sweden and Russia (and their allies)", listOf("England and France", "Austria and Prussia", "Spain and Portugal"), QuizDifficulty.Medium),
        ask("wojna_siedmioletnia", "Which powers were the main sides in the Seven Years' War?", "Prussia and Great Britain against Austria, France and Russia", listOf("The USA against Great Britain", "Spain against Portugal", "Russia against Turkey"), QuizDifficulty.Medium),
        ask("deklaracja_niepodleglosci_usa", "In which year was the US Declaration of Independence signed?", "1776", listOf("1789", "1800", "1765"), QuizDifficulty.Medium),
        ask("konstytucja_usa", "In which year was the US Constitution adopted?", "1787", listOf("1776", "1791", "1800"), QuizDifficulty.Medium),
        ask("napoleon_rosja", "How did Napoleon's 1812 invasion of Russia end?", "Defeat and the retreat of the Grande Armee", listOf("Permanent capture of Moscow", "A peace treaty favorable to France", "Unification of both countries"), QuizDifficulty.Medium),
        ask("waterloo_przeciwnicy", "Who defeated Napoleon at Waterloo?", "A coalition led by Wellington and Blücher", listOf("Wellington alone, without allies", "Russian forces", "Austrian forces"), QuizDifficulty.Medium),

        /* ------------------------------ Hard ------------------------------- */
        ask("unia_krewska", "What did the Union of Krewo of 1385 establish?", "A union of Poland with Lithuania and Lithuania's baptism", listOf("A partition of Poland", "An alliance with the Teutonic Knights", "The end of the Thirteen Years' War"), QuizDifficulty.Hard),
        ask("wojna_trzynastoletnia", "How did the Thirteen Years' War with the Teutonic Knights end?", "The Peace of Thorn and Pomerania joining Poland", listOf("Losing Pomerania to the Teutonic Knights", "The partition of Poland", "A union with the Teutonic Knights"), QuizDifficulty.Hard),
        ask("bitwa_pod_beresteczkiem", "Who did the Commonwealth fight at Berestechko in 1651?", "Khmelnytsky's Cossacks and the Tatars", listOf("Sweden", "Turkey", "Moscow"), QuizDifficulty.Hard),
        ask("chmielnicki_powstanie", "Where did the Khmelnytsky Uprising break out?", "Ukraine", listOf("Lithuania", "Prussia", "Silesia"), QuizDifficulty.Hard),
        ask("wojna_o_niepodleglosc_usa_pomoc", "Which Pole fought in the American War of Independence?", "Tadeusz Kosciuszko", listOf("Jozef Pilsudski", "John III Sobieski", "Wladyslaw Sikorski"), QuizDifficulty.Hard),
        ask("pulaski", "Which Pole died fighting for the American side in the War of Independence?", "Casimir Pulaski", listOf("Tadeusz Kosciuszko", "Jozef Bem", "Ignacy Moscicki"), QuizDifficulty.Hard),
        ask("wiosna_ludow_polska", "Where in Polish lands did the Spring of Nations uprisings of 1848 occur?", "Greater Poland (the 1848 Greater Poland Uprising)", listOf("Silesia", "Pomerania", "Masuria"), QuizDifficulty.Hard),
        ask("plan_marshalla", "What was the Marshall Plan for after World War II?", "Rebuilding Western Europe's economy", listOf("Dividing Germany", "Founding the UN", "Disarming Japan"), QuizDifficulty.Hard),
        ask("konferencja_jalta", "What was decided at the 1945 Yalta Conference?", "The postwar division of spheres of influence in Europe", listOf("Japan's surrender terms", "The founding of NATO", "Poland's prewar borders"), QuizDifficulty.Hard),
        ask("nato_powstanie", "In which year was NATO founded?", "1949", listOf("1945", "1955", "1939"), QuizDifficulty.Hard),

        /* ----------------------------- Expert ---------------------------- */
        ask("unia_horodelska", "What did the Union of Horodlo of 1413 regulate?", "Equalizing the rights of Lithuanian and Polish nobility", listOf("Adding Pomerania to Poland", "Ending the Thirteen Years' War", "A peace with Sweden"), QuizDifficulty.Expert),
        ask("konfederacja_barska", "Who was the Bar Confederation (1768) directed against?", "King Poniatowski and Russian influence", listOf("The Teutonic Knights", "Sweden", "Turkey"), QuizDifficulty.Expert),
        ask("insurekcja_kosciuszkowska_rok", "In which year did the Kosciuszko Uprising break out?", "1794", listOf("1791", "1830", "1863"), QuizDifficulty.Expert),
        ask("krolestwo_kongresowe", "When was the Congress Kingdom of Poland formed?", "At the Congress of Vienna in 1815", listOf("After the November Uprising", "After the first partition", "After regaining independence in 1918"), QuizDifficulty.Expert),
        ask("wielka_wojna_pln_skutek", "What was one of the main consequences of the Great Northern War for the Commonwealth?", "Weakening and growing dependence on Russia", listOf("Gaining new territory in the north", "Growing military power", "A personal union with Sweden"), QuizDifficulty.Expert),
        ask("sejm_czteroletni_reformy", "Which reform did the Four-Year Sejm introduce besides the constitution?", "Expanding the army and townspeople's rights", listOf("Abolishing liberum veto permanently before 1791", "Establishing a republic", "Abolishing the monarchy"), QuizDifficulty.Expert),
        ask("targowica", "What was the Targowica Confederation?", "A magnate conspiracy against the reforms of the May 3 Constitution", listOf("An alliance with Napoleon", "An independence movement", "A union with Lithuania"), QuizDifficulty.Expert),
        ask("polski_rzad_na_uchodzstwie", "Where did the Polish government-in-exile operate during World War II?", "London (earlier in France)", listOf("Moscow", "Washington", "Rome"), QuizDifficulty.Expert),
        ask("bitwa_o_anglie", "What was the Battle of Britain in 1940?", "The Luftwaffe's air campaign against Great Britain", listOf("The Allied landing at Normandy", "A naval battle in the Atlantic only", "A land offensive in Africa"), QuizDifficulty.Expert),
        ask("dywizjon_303", "What was 303 Squadron known for in the Battle of Britain?", "It was one of the RAF's most effective fighter squadrons", listOf("It defended Warsaw in September 1939", "It fought at Monte Cassino", "It was a naval unit"), QuizDifficulty.Expert)
    )
}
