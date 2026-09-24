package com.lernhero.domain.quiz.bank

import com.lernhero.domain.quiz.QuizCategory
import com.lernhero.domain.quiz.QuizDifficulty
import com.lernhero.domain.quiz.QuizEntry

/** Second batch of history - Poland, the world, and dates worth knowing. */
object HistoryQuestionsExtra {

    private fun ask(
        id: String,
        prompt: String,
        correct: String,
        wrong: List<String>,
        difficulty: QuizDifficulty
    ) = QuizEntry(
        id = "his2_$id",
        prompt = prompt,
        correct = correct,
        wrong = wrong,
        category = QuizCategory.History,
        difficulty = difficulty
    )

    val all: List<QuizEntry> = listOf(

        /* ------------------------------- Easy ------------------------------- */
        ask("ww2_koniec", "In which year did World War II end?", "1945", listOf("1944", "1946", "1943"), QuizDifficulty.Easy),
        ask("wikingowie_skad", "Where did the Vikings come from?", "Scandinavia", listOf("Spain", "Greece", "Egypt"), QuizDifficulty.Easy),
        ask("pierwsza_wojna_koniec", "In which year did World War I end?", "1918", listOf("1919", "1917", "1920"), QuizDifficulty.Easy),
        ask("egipt_pismo", "What is the writing of the ancient Egyptians called?", "Hieroglyphs", listOf("Cuneiform", "Runic", "The Greek alphabet"), QuizDifficulty.Easy),
        ask("nowozytne_odkrycia", "Who sailed around the Cape of Good Hope on the way to India?", "Vasco da Gama", listOf("Columbus", "Cortes", "Cook"), QuizDifficulty.Easy),

        /* ------------------------------ Medium ------------------------------ */
        ask("rewolucja_pazdziernikowa", "In which year did the October Revolution occur in Russia?", "1917", listOf("1905", "1918", "1921"), QuizDifficulty.Medium),
        ask("hiroszima", "In which year was the atomic bomb dropped on Hiroshima?", "1945", listOf("1944", "1946", "1949"), QuizDifficulty.Medium),
        ask("czlowiek_kosmos_rok", "In which year did Yuri Gagarin fly into space?", "1961", listOf("1957", "1969", "1965"), QuizDifficulty.Medium),
        ask("sputnik", "What was Earth's first artificial satellite called?", "Sputnik 1", listOf("Vostok 1", "Explorer 1", "Luna 1"), QuizDifficulty.Medium),
        ask("napoleon_kraj", "Where did Napoleon Bonaparte come from?", "Corsica", listOf("Sicily", "Provence", "Sardinia"), QuizDifficulty.Medium),
        ask("wojna_secesyjna", "What was the American Civil War fought over, above all?", "Slavery and the union of the country", listOf("The border with Mexico", "California gold", "Independence from England"), QuizDifficulty.Medium),
        ask("wojna_stuletnia", "Between which countries was the Hundred Years' War fought?", "England and France", listOf("Spain and Portugal", "Sweden and Denmark", "Austria and Prussia"), QuizDifficulty.Medium),
        ask("renesans_kraj", "In which country did the Renaissance begin?", "Italy", listOf("France", "Germany", "England"), QuizDifficulty.Medium),

        /* ------------------------------ Hard ------------------------------- */
        ask("traktat_wersalski", "In which year was the Treaty of Versailles signed?", "1919", listOf("1918", "1920", "1923"), QuizDifficulty.Hard),
        ask("kryzys_kubanski", "In which year did the Cuban Missile Crisis erupt?", "1962", listOf("1959", "1968", "1956"), QuizDifficulty.Hard),
        ask("mur_upadek_kto", "Which Soviet leader announced perestroika?", "Mikhail Gorbachev", listOf("Leonid Brezhnev", "Nikita Khrushchev", "Yuri Andropov"), QuizDifficulty.Hard),
        ask("sobor_trydencki", "What was the Council of Trent for?", "The Counter-Reformation in the Catholic Church", listOf("Dividing the Holy Roman Empire in two", "Settling the end of the Hundred Years' War", "Fixing the borders of Byzantium"), QuizDifficulty.Hard),
        ask("hanza", "What was the Hanseatic League?", "A league of merchant cities on the Baltic and North Sea", listOf("A knightly order founded in the Holy Land", "A German dynasty ruling the Rhine valley", "An Italian military alliance against France"), QuizDifficulty.Hard),
        ask("cesarz_chin_pierwszy", "Who unified China as its first emperor?", "Qin Shi Huang", listOf("Kublai Khan", "Confucius", "Sun Tzu"), QuizDifficulty.Hard),
        ask("inkowie_kraj", "The heartland of the Inca empire lay in which present-day country?", "Peru", listOf("Mexico", "Brazil", "Colombia"), QuizDifficulty.Hard),
        ask("rewolucja_przemyslowa", "In which country did the Industrial Revolution begin?", "The United Kingdom", listOf("Germany", "France", "The USA"), QuizDifficulty.Hard),

        /* ----------------------------- Expert ---------------------------- */
        ask("edykt_nantejski", "What did the Edict of Nantes guarantee?", "Rights for the Huguenots in France", listOf("Free trade in the Hanseatic League", "Independence for the Netherlands", "Dividing the Empire"), QuizDifficulty.Expert),
        ask("kongres_wiedenski", "In which years did the Congress of Vienna meet?", "1814-1815", listOf("1806-1807", "1830-1831", "1848-1849"), QuizDifficulty.Expert),
        ask("wiosna_ludow", "In which year did the Spring of Nations break out?", "1848", listOf("1830", "1863", "1871"), QuizDifficulty.Expert),
        ask("bitwa_lepanto", "Who lost the Battle of Lepanto in 1571?", "The Ottoman Empire", listOf("The Republic of Venice", "Spain", "The Papal States"), QuizDifficulty.Expert),
        ask("dynastia_ming", "Which dynasty ruled China before the Qing?", "Ming", listOf("Tang", "Han", "Song"), QuizDifficulty.Expert),
        ask("bizancjum_justynian", "Which Byzantine emperor had the famous Roman law code compiled?", "Justinian I", listOf("Constantine the Great", "Heraclius", "Basil II"), QuizDifficulty.Expert),
        ask("rewolta_taiping", "In which country did the Taiping Rebellion break out?", "China", listOf("Japan", "India", "Korea"), QuizDifficulty.Expert),
    )
}
