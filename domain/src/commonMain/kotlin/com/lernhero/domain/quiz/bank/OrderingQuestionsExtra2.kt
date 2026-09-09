package com.lernhero.domain.quiz.bank

import com.lernhero.domain.quiz.OrderingEntry
import com.lernhero.domain.quiz.QuizCategory
import com.lernhero.domain.quiz.QuizDifficulty

/**
 * Third batch of "put in order" questions. [OrderingEntry.items] is always already stored
 * in the correct order - the UI layer handles shuffling.
 */
object OrderingQuestionsExtra2 {

    private fun order(
        id: String,
        prompt: String,
        items: List<String>,
        category: QuizCategory,
        difficulty: QuizDifficulty
    ) = OrderingEntry("ord3_$id", prompt, items, category, difficulty)

    val all: List<OrderingEntry> = listOf(

        /* ------------------------------- Easy ------------------------------- */

        /* ------------------------------ Medium ------------------------------ */
        order("wielkosc_panstw_europa", "Order these European countries from smallest to largest.", listOf("Monaco", "Belgium", "Poland", "France"), QuizCategory.Geography, QuizDifficulty.Medium),
        order("wynalazki_transport", "Order these modes of transport from oldest to newest.", listOf("The wheel", "The horse-drawn carriage", "The steam locomotive", "The airplane"), QuizCategory.Technology, QuizDifficulty.Medium),
        order("polska_krolowie_wczesni", "Order these Polish rulers from earliest reign.", listOf("Mieszko I", "Boleslaw the Brave", "Casimir the Restorer", "Boleslaw the Wrymouth"), QuizCategory.History, QuizDifficulty.Medium),
        order("literatura_dziecieca", "Order these children's books from oldest to newest.", listOf("Aesop's Fables", "Winnie the Pooh", "The Moomins", "Harry Potter"), QuizCategory.Literature, QuizDifficulty.Medium),

        /* ------------------------------ Hard ------------------------------- */
        order("panstwa_ludnosc", "Order these countries from least to most populous.", listOf("Poland", "Germany", "Brazil", "India"), QuizCategory.Geography, QuizDifficulty.Hard),
        order("historia_wynalazkow_komunikacja2", "Order these communication inventions from oldest.", listOf("Carrier pigeons", "The telegraph", "Radio", "The internet"), QuizCategory.Technology, QuizDifficulty.Hard),
        order("gwiazdy_jasnosc", "Order these objects from dimmest to brightest as seen from Earth.", listOf("A dwarf star", "A full moon", "Venus", "The Sun"), QuizCategory.Science, QuizDifficulty.Hard),
        order("epoki_geologiczne_proste", "Order these geological eras from oldest.", listOf("Precambrian", "Paleozoic", "Mesozoic", "Cenozoic"), QuizCategory.Biology, QuizDifficulty.Hard),
        order("wojny_europa", "Order these European conflicts from oldest.", listOf("The Hundred Years' War", "The Thirty Years' War", "The Napoleonic Wars", "World War I"), QuizCategory.History, QuizDifficulty.Hard),
        order("sportowe_rekordy_dystans", "Order these Olympic races from shortest to longest.", listOf("100 m", "1500 m", "10000 m", "Marathon"), QuizCategory.Sport, QuizDifficulty.Hard),

        /* ----------------------------- Expert ---------------------------- */
        order("cywilizacje_ameryka", "Order these pre-Columbian civilizations by their peak, from oldest.", listOf("The Olmecs", "The Maya", "The Aztecs", "The Incas"), QuizCategory.History, QuizDifficulty.Expert),
        order("czastki_wielkosc", "Order these objects from smallest to largest.", listOf("A quark", "An atom", "A virus", "A cell"), QuizCategory.Science, QuizDifficulty.Expert),
        order("dynastie_chiny", "Order these Chinese dynasties from oldest.", listOf("Han", "Tang", "Song", "Ming"), QuizCategory.History, QuizDifficulty.Expert),
        order("nobel_fizyka_odkrycia", "Order these physics discoveries from oldest.", listOf("The electron", "X-rays", "The Higgs boson", "Gravitational waves"), QuizCategory.Science, QuizDifficulty.Expert)
    )
}
