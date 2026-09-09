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
        ask("statek_kolumb", "What was one of Columbus's ships called?", "Santa Maria", listOf("Titanic", "Beagle", "Endeavour"), QuizDifficulty.Easy),
        ask("jedwabny_szlak", "What was the Silk Road?", "A network of trade routes between Asia and Europe", listOf("A river in China", "A knightly order", "A peace treaty"), QuizDifficulty.Easy),
        ask("krzyzowcy_cel", "Where did the Crusades set out for?", "The Holy Land", listOf("America", "China", "Australia"), QuizDifficulty.Easy),
        ask("kolumb_kraj", "On whose behalf did Columbus set out for the Indies?", "The Monarchs of Spain", listOf("The King of Portugal", "The King of England", "The King of France"), QuizDifficulty.Easy),

        /* ------------------------------ Medium ------------------------------ */
        ask("magellan", "Who led the first expedition to circumnavigate the globe?", "Ferdinand Magellan", listOf("Christopher Columbus", "Vasco da Gama", "James Cook"), QuizDifficulty.Medium),
        ask("cook_odkrycia", "Which continent/islands did Captain James Cook explore?", "Australia and the Pacific Islands", listOf("South America", "Africa", "Antarctica, as the first to land"), QuizDifficulty.Medium),
        ask("wielka_wojna_polnocna", "Which powers fought the Great Northern War?", "Sweden and Russia (and their allies)", listOf("England and France", "Austria and Prussia", "Spain and Portugal"), QuizDifficulty.Medium),
        ask("wojna_siedmioletnia", "Which powers were the main sides in the Seven Years' War?", "Prussia and Great Britain against Austria, France and Russia", listOf("The USA against Great Britain", "Spain against Portugal", "Russia against Turkey"), QuizDifficulty.Medium),
        ask("deklaracja_niepodleglosci_usa", "In which year was the US Declaration of Independence signed?", "1776", listOf("1789", "1800", "1765"), QuizDifficulty.Medium),
        ask("konstytucja_usa", "In which year was the US Constitution adopted?", "1787", listOf("1776", "1791", "1800"), QuizDifficulty.Medium),
        ask("napoleon_rosja", "How did Napoleon's 1812 invasion of Russia end?", "Defeat and the retreat of the Grande Armee", listOf("Permanent capture of Moscow", "A peace treaty favorable to France", "Unification of both countries"), QuizDifficulty.Medium),
        ask("waterloo_przeciwnicy", "Who defeated Napoleon at Waterloo?", "A coalition led by Wellington and Blücher", listOf("Wellington alone, without allies", "Russian forces", "Austrian forces"), QuizDifficulty.Medium),

        /* ------------------------------ Hard ------------------------------- */
        ask("plan_marshalla", "What was the Marshall Plan for after World War II?", "Rebuilding Western Europe's economy", listOf("Dividing Germany", "Founding the UN", "Disarming Japan"), QuizDifficulty.Hard),
        ask("konferencja_jalta", "What was decided at the 1945 Yalta Conference?", "The postwar division of spheres of influence in Europe", listOf("Japan's surrender terms", "The founding of NATO", "Poland's prewar borders"), QuizDifficulty.Hard),
        ask("nato_powstanie", "In which year was NATO founded?", "1949", listOf("1945", "1955", "1939"), QuizDifficulty.Hard),

        /* ----------------------------- Expert ---------------------------- */
        ask("targowica", "What was the Targowica Confederation?", "A magnate conspiracy against the reforms of the May 3 Constitution", listOf("An alliance with Napoleon", "An independence movement", "A union with Lithuania"), QuizDifficulty.Expert),
        ask("bitwa_o_anglie", "What was the Battle of Britain in 1940?", "The Luftwaffe's air campaign against Great Britain", listOf("The Allied landing at Normandy", "A naval battle in the Atlantic only", "A land offensive in Africa"), QuizDifficulty.Expert),
    )
}
