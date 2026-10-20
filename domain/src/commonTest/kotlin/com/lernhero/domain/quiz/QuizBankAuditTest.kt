package com.lernhero.domain.quiz

import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertTrue

/**
 * Testy z AUDYTU banku - kazdy z nich powstal po tym, jak dana wada realnie siedziala w grze
 * i przeszla przez [QuizBankTest].
 *
 * [QuizBankTest] pilnuje rzeczy mechanicznych (zdublowane id, cztery odpowiedzi, identyczna
 * tresc). Tutaj sa wady, ktorych ono NIE widzialo, a gracz zobaczylby od razu - albo, co gorsza,
 * nauczylby sie je wykorzystywac zamiast odpowiadac.
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
        // Pytania losowane z danych (flagi, panstwa) maja wspolny wzor tresci - "ktore z tych ma
        // najwiecej ludzi?" z China jako odpowiedzia wraca z kazdym zestawem i nie jest powtorka.
        val collisions = all
            .filter { !it.generated }
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
        // Tylko pytanie i POPRAWNE odpowiedzi. Polskie nazwisko wsrod dystraktorow
        // ("Kto stworzyl Muminki?" z Tuwimem do odrzucenia) nie czyni pytania lokalnym.
        // Typy specjalne tez: ukladanka "polscy krolowie od najstarszego" siedziala w globalnej
        // Historii, bo ten test patrzyl dotad tylko na pytania z wyborem.
        val texts: List<Triple<String, QuizCategory, String>> =
            all.map { Triple(it.id, it.category, it.prompt + " " + it.correct) } +
                TrueFalseBank.all.map { Triple(it.id, it.category, it.statement) } +
                MultiSelectBank.all.map { Triple(it.id, it.category, it.prompt + " " + it.correct.joinToString(" ")) } +
                OrderingBank.all.map { Triple(it.id, it.category, it.prompt + " " + it.items.joinToString(" ")) }

        val misplaced = texts
            .filter { (_, category, _) -> category != QuizCategory.Poland }
            .filter { (_, _, text) ->
                POLAND_ONLY.any { marker -> Regex("\\b$marker", RegexOption.IGNORE_CASE).containsMatchIn(text) }
            }
            .map { (id, category, text) -> "$id (${category.label}): $text" }

        assertTrue(misplaced.isEmpty(), "Polskie tresci poza kategoria Poland: $misplaced")
    }

    /**
     * **Poprawna odpowiedz nie wyroznia sie sama dlugoscia.**
     *
     * Najtansza sciaga w kazdym quizie: jedna odpowiedz rozwlekla i wyjasniajaca, trzy urwane.
     * Gracz, ktory nie ma pojecia o temacie, klika najdluzsza i trafia duzo czesciej niz raz na
     * cztery - a pytanie, ktore da sie wygrac bez wiedzy, nie uczy niczego i psuje kazdy
     * mechanizm, ktory od tej wiedzy zalezy (mnoznik obrazen, drabinka trudnosci, poziom tematu).
     *
     * Audyt znalazl 182 takie pytania w banku, ktory przechodzil wszystkie pozostale testy - nie
     * przez niechlujstwo, tylko dlatego, ze pisze sie je naturalnie: poprawna odpowiedz zna sie
     * dokladnie, a dystraktory dopisuje sie w trzech slowach.
     *
     * Prog jest CELOWO luzny. Nie chodzi o to, zeby cztery odpowiedzi mialy rowna dlugosc (przy
     * nazwie wlasnej to niewykonalne - "Sri Jayawardenepura Kotte" nie ma jak sie skrocic), tylko
     * o to, zeby zadna nie odstawala tak, ze widac ja z drugiego konca ekranu.
     *
     * Flagi i pytania z danych o panstwach sa poza testem: tam wszystkie cztery odpowiedzi to
     * nazwy z tej samej puli, a poprawna wybiera tabela, nie autor - dlugosc niczego nie zdradza.
     */
    @Test
    fun `poprawna odpowiedz nie wyroznia sie sama dlugoscia`() {
        val giveaways = all
            .filter { !it.generated }
            .filter { entry -> entry.wrong.all { entry.correct.length - it.length >= LENGTH_GIVEAWAY } }
            .map { "${it.id}: '${it.correct}' vs ${it.wrong}" }

        assertTrue(giveaways.isEmpty(), "Poprawna odpowiedz zdradza sie dlugoscia: $giveaways")
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
     * dawnych plikow BiologyQuestionsExtra2, GeographyQuestionsExtra2, ElementQuestionsExtra2
     * i TechnologyQuestionsExtra - dzis wchlonietych przez pliki swoich tematow).
     */

    private companion object {
        /** Powyzej tego pokrycia slow kluczowych dwa pytania o tej samej odpowiedzi to ten sam fakt. */
        const val DUPLICATE_THRESHOLD = 0.55

        /**
         * O tyle znakow poprawna odpowiedz moze byc dluzsza od KAZDEJ blednej, zanim zacznie sie
         * sama zdradzac. Liczone wzgledem najdluzszego dystraktora, nie sredniej - wystarczy jedna
         * bledna odpowiedz podobnej dlugosci, zeby wybor przestal byc darmowy.
         */
        const val LENGTH_GIVEAWAY = 15

        /** Postacie i miejsca bez rozpoznawalnosci poza Polska - patrz test wyzej. */
        val POLAND_ONLY = listOf(
            "Malysz", "Stoch", "Radwanska", "Wlodarczyk", "Kubica", "Mickiewicz", "Reymont",
            "Szymborska", "Tokarczuk", "Rysy", "Giewont", "Wawel", "Poniatowski", "Piast",
            "Jagiell", "Sniardwy", "Hancza", "Brzechwa", "Tuwim", "Konopnicka", "Kochanowski",
            "Matejko", "Mieszko", "Swiatek", "Wigilia"
        )

        val STOP = setOf(
            "the", "a", "an", "of", "in", "to", "is", "are", "was", "were", "and", "or", "for",
            "on", "at", "by", "with", "from", "which", "what", "who", "whose", "how", "many",
            "much", "does", "did", "this", "these", "those", "that", "as", "be", "been", "not",
            "called", "its", "it"
        )

        // Apostrof nie skleja sie ze slowem: "'powerhouse'" i "powerhouse" to to samo slowo, a przy
        // [a-z']+ byly dwoma roznymi - i dubel rozniacy sie samym cudzyslowem przechodzil test.
        fun String.keywords(): Set<String> =
            Regex("[a-z]+").findAll(lowercase().replace('\'', ' '))
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
