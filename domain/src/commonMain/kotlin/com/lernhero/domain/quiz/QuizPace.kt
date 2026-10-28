package com.lernhero.domain.quiz

/**
 * Ile czasu ponad bazowe okno ([com.lernhero.domain.combat.live.AnswerTimer.WINDOW_MS]) potrzebuje
 * KONKRETNE pytanie. Bazowe okno jest skrojone pod typowe pytanie, ktore sie po prostu wie albo
 * nie wie ("Who wrote 'Hamlet'?"). Dwa rodzaje pytan potrzebuja wiecej i oba dostaja dodatek tutaj:
 *
 *  - **Dlugie do przeczytania.** Tresc i odpowiedzi dluzsze niz typowe ([BASE_CHARS]) dokladaja
 *    czas na czytanie ([MS_PER_CHAR], najwyzej [MAX_READING_MS]). Inaczej dlugie pytanie byloby
 *    trudniejsze nie trescia, tylko czytaniem - ta sama zasada, co dodatek za opcje w
 *    [com.lernhero.domain.combat.live.AnswerTimer.windowFor].
 *  - **Do przemyslenia.** Porownania i proporcje ("Poland vs Chile: who has more people?", "ile
 *    razy dluzszy jest Nil od Tamizy?") i rachunki nie da sie odpowiedziec z pamieci w sekunde,
 *    nawet znajac fakty - trzeba zestawic dwie liczby. Takie pytania dostaja staly [THINKING_MS].
 *    Ktore to sa, mowi dzial albo temat ([isThinking]), a nie zgadywanie z tresci: reguly na tekscie
 *    myla rok ("1666") z rachunkiem.
 *
 * Wynik dodaje sie do okna przed skroceniem za glebokosc fali, wiec dalekie fale tna tez dodatek.
 */
object QuizPace {
    /** Tyle znakow (tresc + wszystkie odpowiedzi) czyta sie w bazowym oknie - mediana banku to ~110. */
    const val BASE_CHARS = 120

    /** 50 ms na znak = 20 znakow na sekunde, spokojne czytanie pod presja czasu. */
    const val MS_PER_CHAR = 50L

    /** Najdluzsze pytania banku maja ~250 znakow: dodatek za czytanie nie rosnie bez konca. */
    const val MAX_READING_MS = 6000L

    /** Dodatek za pytanie do przemyslenia (porownanie, proporcja, rachunek). */
    const val THINKING_MS = 4000L

    /** Dzialy, w ktorych sedno pytania to porownanie albo proporcja - patrz README banku. */
    val THINKING_SECTIONS: Set<String> = setOf("geography/comparisons")

    /** Tematy, w ktorych pytanie zwykle trzeba policzyc. */
    val THINKING_CATEGORIES: Set<QuizCategory> = setOf(QuizCategory.Math)

    fun isThinking(entry: QuizEntry): Boolean =
        entry.category in THINKING_CATEGORIES ||
            THINKING_SECTIONS.any { prefix -> entry.section == prefix || entry.section?.startsWith("$prefix/") == true }

    /** Dodatek do okna dla pytania o tresci [prompt] i odpowiedziach [options]. */
    fun extraMs(prompt: String, options: List<String>, thinking: Boolean): Long {
        val chars = prompt.length + options.sumOf { it.length }
        val reading = ((chars - BASE_CHARS).coerceAtLeast(0) * MS_PER_CHAR).coerceAtMost(MAX_READING_MS)
        return reading + if (thinking) THINKING_MS else 0L
    }
}
