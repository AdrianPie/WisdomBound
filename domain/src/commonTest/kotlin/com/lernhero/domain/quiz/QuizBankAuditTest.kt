package com.lernhero.domain.quiz

import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertTrue

/**
 * Testy z AUDYTU banku - kazdy z nich powstal po tym, jak dana wada realnie siedziala w grze
 * i przeszla przez [QuizBankTest].
 *
 * [QuizBankTest] pilnuje rzeczy mechanicznych (zdublowane id, cztery odpowiedzi, identyczna
 * tresc). Tutaj sa dwie wady, ktorych ono NIE widzialo, a gracz widzialby od razu.
 */
class QuizBankAuditTest {

    private val all = QuizBank.all(Random(7))

    /**
     * **Ten sam fakt zadany dwa razy innymi slowami.**
     *
     * "Which is the largest island in the world?" i "Which island is the largest in the world?"
     * to dla testu tresci dwa rozne pytania, a dla gracza jedno i to samo - z wrazeniem, ze gra
     * sie zapetlila. Audyt znalazl 52 takie pary w banku, ktory przechodzil wszystkie testy.
     *
     * Warunek: te same slowa kluczowe w pytaniu ORAZ ta sama odpowiedz. Rodziny pytan-wprawek
     * ("ile to 2 do potegi 6" / "2 do potegi 10") maja rozne odpowiedzi, wiec nie wpadaja tu
     * przez przypadek.
     */
    @Test
    fun `ten sam fakt nie wraca w dwoch pytaniach`() {
        val collisions = all
            .filter { it.category != QuizCategory.Flags }
            .groupBy { it.correct.normalizedAnswer() }
            .flatMap { (_, group) ->
                group.indices.flatMap { i ->
                    (i + 1 until group.size).mapNotNull { j ->
                        val a = group[i]
                        val b = group[j]
                        val overlap = a.prompt.keywords().jaccard(b.prompt.keywords())
                        if (overlap >= DUPLICATE_THRESHOLD) "'${a.prompt}' ~ '${b.prompt}'" else null
                    }
                }
            }

        assertTrue(collisions.isEmpty(), "Ten sam fakt w kilku pytaniach: $collisions")
    }

    /**
     * **Tresci wylacznie polskie poza kategoria [QuizCategory.Poland].**
     *
     * Gra jest wydana globalnie, a "ile razy Adam Malysz wygral Puchar Swiata?" dla gracza
     * z Teksasu nie jest pytaniem trudnym, tylko niemozliwym - i po to powstala kategoria lokalna
     * (patrz [QuizCategory.Poland]). Pierwsza przeprowadzka objela historie i literature, ale
     * przeoczyla sport, wiec caly zestaw polskich nazwisk zostal w globalnej puli.
     *
     * Lista nazwisk jest CELOWO waska: sa na niej tylko postacie i miejsca nierozpoznawalne poza
     * Polska. Kopernik, Maria Sklodowska-Curie, Solidarnosc czy Lewandowski zostaja w puli
     * globalnej, bo o kazdym z nich moze wiedziec ktos, kto nigdy nie byl w Polsce.
     */
    @Test
    fun `tresci tylko polskie siedza w kategorii Poland`() {
        val misplaced = all
            .filter { it.category != QuizCategory.Poland }
            .filter { entry ->
                // Tylko pytanie i POPRAWNA odpowiedz. Polskie nazwisko wsrod dystraktorow
                // ("Kto stworzyl Muminki?" z Tuwimem do odrzucenia) nie czyni pytania lokalnym.
                val text = entry.prompt + " " + entry.correct
                POLAND_ONLY.any { marker -> Regex("\\b$marker", RegexOption.IGNORE_CASE).containsMatchIn(text) }
            }
            .map { "${it.id} (${it.category.label}): ${it.prompt}" }

        assertTrue(misplaced.isEmpty(), "Polskie tresci poza kategoria Poland: $misplaced")
    }

    /*
     * Byl tu trzeci test - "odpowiedz schowana w tresci pytania". Zostal USUNIETY, choc znalazl
     * realne wady (permafrost -> "grunt trwale zamarzniety", transuranowce -> "ciezsze od uranu",
     * open source -> "kod zrodlowy jest jawny"). Powod: na jeden prawdziwy przypadek wypadaly
     * dwa falszywe - "Jaka jest stolica Meksyku? -> Mexico City" i "Jak nazywa sie logarytm
     * o podstawie e? -> logarytm naturalny" to poprawne pytania, ktore kazda wersja tej reguly
     * zglaszala. Test, ktory wymaga listy dwudziestu wyjatkow, nie chroni juz niczego - zaczyna
     * byc trzecim miejscem, ktore trzeba pamietac przy dopisywaniu pytania.
     *
     * Znalezione tamtym testem pytania zostaly poprawione recznie (patrz historia zmian
     * BiologyQuestionsExtra2, GeographyQuestionsExtra2, ElementQuestionsExtra2,
     * TechnologyQuestionsExtra).
     */

    private companion object {
        /** Powyzej tego pokrycia slow kluczowych dwa pytania o tej samej odpowiedzi to ten sam fakt. */
        const val DUPLICATE_THRESHOLD = 0.55

        /** Postacie i miejsca bez rozpoznawalnosci poza Polska - patrz test wyzej. */
        val POLAND_ONLY = listOf(
            "Malysz", "Stoch", "Radwanska", "Wlodarczyk", "Kubica", "Mickiewicz", "Reymont",
            "Szymborska", "Tokarczuk", "Rysy", "Giewont", "Wawel", "Poniatowski", "Piast",
            "Jagiell", "Sniardwy", "Hancza", "Brzechwa", "Tuwim", "Konopnicka", "Kochanowski"
        )

        val STOP = setOf(
            "the", "a", "an", "of", "in", "to", "is", "are", "was", "were", "and", "or", "for",
            "on", "at", "by", "with", "from", "which", "what", "who", "whose", "how", "many",
            "much", "does", "did", "this", "these", "those", "that", "as", "be", "been", "not",
            "called", "its", "it"
        )

        fun String.keywords(): Set<String> =
            Regex("[a-z']+").findAll(lowercase())
                .map { it.value }
                .filter { it.length > 3 && it !in STOP }
                .toSet()

        fun String.normalizedAnswer(): String = lowercase().removePrefix("the ").trim()

        fun Set<String>.jaccard(other: Set<String>): Double {
            if (isEmpty() || other.isEmpty()) return 0.0
            return (this intersect other).size.toDouble() / (this union other).size
        }
    }
}
