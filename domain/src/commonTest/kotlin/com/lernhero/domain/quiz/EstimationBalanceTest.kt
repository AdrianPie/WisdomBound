package com.lernhero.domain.quiz

import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertTrue

/**
 * Balans puli "zgadnij rok".
 *
 * Pytania z tej puli sa oceniane wzgledem tolerancji zaleznej od epoki, wiec sam sklad puli
 * decyduje o tym, jak trudne jest zdarzenie bossa. Gdy pula przechyla sie ku XX wiekowi (tolerancja
 * 2-4 lata), gracz w kolko dostaje najostrzej oceniane pytania - i to nie jest projekt, tylko
 * skutek uboczny tego, ze latwiej wymyslic date z ostatnich stu lat. Te testy pilnuja rozkladu.
 */
class EstimationBalanceTest {

    private val all = EstimationBank.all

    /**
     * Pytania "zgadnij ROK" - jedyne, dla ktorych rozklad epok cokolwiek znaczy. Odkad pula ma
     * takze populacje, wysokosci i odleglosci, liczenie "ile jest z lat 1900+" po CALEJ puli
     * mierzylo populacje Nigerii jako wydarzenie z XX wieku.
     */
    private val years = all.filter { it.unit == "year" }

    @Test
    fun `pula jest duza i bez zdublowanych id`() {
        assertTrue(all.size >= 120, "Pula ma tylko ${all.size} pytan")

        val duplicates = all.groupBy { it.id }.filter { it.value.size > 1 }.keys
        assertTrue(duplicates.isEmpty(), "Zdublowane id: $duplicates")
    }

    @Test
    fun `tresci pytan sie nie powtarzaja`() {
        val duplicates = all.groupBy { it.prompt.lowercase() }.filter { it.value.size > 1 }.keys
        assertTrue(duplicates.isEmpty(), "Powtorzone tresci: $duplicates")
    }

    @Test
    fun `zadna epoka nie zjada wiecej niz polowy puli`() {
        // Wczesniej same lata 1900+ mialy ponad 50% udzialu i zdarzenie bossa sprowadzalo sie
        // do dat z ostatniego stulecia.
        val modern = years.count { it.correctValue >= 1900 }
        val share = modern.toFloat() / years.size
        assertTrue(share <= 0.5f, "Lata 1900+ to ${(share * 100).toInt()}% puli - za duzo")
    }

    @Test
    fun `dawne epoki maja realna reprezentacje`() {
        val ancient = years.count { it.correctValue < 0 }
        val earlyMedieval = years.count { it.correctValue in 0..999 }
        val medieval = years.count { it.correctValue in 1000..1499 }

        assertTrue(ancient >= 20, "Starozytnosc p.n.e.: tylko $ancient pytan")
        assertTrue(earlyMedieval >= 10, "Lata 0-999: tylko $earlyMedieval pytan")
        assertTrue(medieval >= 15, "Lata 1000-1499: tylko $medieval pytan")
    }

    @Test
    fun `pula ma tez szacowania inne niz daty`() {
        // Samo "zgadnij rok" sprowadzalo zdarzenie bossa do jednej umiejetnosci: pamieci do dat.
        // Populacje, odleglosci i wysokosci sprawdzaja wyczucie skali, a nie zapamietana liczbe.
        val nonYear = all.filterNot { it.unit == "year" }
        assertTrue(nonYear.size >= 60, "Szacowan innych niz rok jest tylko ${nonYear.size}")

        val units = nonYear.map { it.unit }.distinct()
        assertTrue(units.size >= 4, "Za malo roznych jednostek: $units")
    }

    @Test
    fun `pytania lokalne sa wyjatkiem, nie regula`() {
        // Gra jest wydana globalnie - pula domyslna (angielska) musi zostac duza po odsianiu
        // tematow lokalnych.
        val global = EstimationBank.forLanguage("en")
        assertTrue(global.size >= 150, "Globalna pula ma tylko ${global.size} pytan")
        assertTrue(
            global.none { it.localeCode != null },
            "Do puli globalnej weszlo pytanie lokalne"
        )
        assertTrue(
            EstimationBank.forLanguage("pl").size > global.size,
            "Polska pula powinna byc nadzbiorem globalnej"
        )
    }

    @Test
    fun `kazde pytanie miesci sie w swojej skali i ma sensowna tolerancje`() {
        all.forEach { entry ->
            assertTrue(
                entry.correctValue in entry.minValue..entry.maxValue,
                "${entry.id}: odpowiedz ${entry.correctValue} poza zakresem ${entry.minValue}..${entry.maxValue}"
            )
            assertTrue(entry.tolerance >= 1, "${entry.id}: tolerancja ${entry.tolerance}")
            // Tolerancja szersza niz cwierc skali oznacza, ze trafic mozna na slepo.
            assertTrue(
                entry.tolerance <= (entry.maxValue - entry.minValue) / 4,
                "${entry.id}: tolerancja ${entry.tolerance} jest absurdalnie szeroka"
            )
        }
    }

    @Test
    fun `suwak zawsze obejmuje poprawna odpowiedz`() {
        // Gdyby okno suwaka wypadlo obok odpowiedzi, pytanie byloby nie do wygrania.
        val random = Random(7)
        all.forEach { entry ->
            repeat(20) {
                val range = entry.sliderRange(random)
                assertTrue(
                    entry.correctValue in range,
                    "${entry.id}: odpowiedz ${entry.correctValue} poza suwakiem $range"
                )
            }
        }
    }

    @Test
    fun `trafienie w tolerancje daje wysoka ocene, a duze pudlo zero`() {
        all.forEach { entry ->
            assertTrue(entry.closeness(entry.correctValue) > 0.99f, "${entry.id}: idealne trafienie nie daje 1.0")

            val onEdge = entry.closeness(entry.correctValue + entry.tolerance)
            assertTrue(onEdge in 0.7f..0.95f, "${entry.id}: granica tolerancji daje $onEdge")

            val wayOff = entry.closeness(entry.correctValue + entry.tolerance * 10)
            assertTrue(wayOff == 0f, "${entry.id}: kompletne pudlo daje $wayOff zamiast 0")
        }
    }

    /**
     * **Jednostka musi byc ta, w ktorej gracz MYSLI.**
     *
     * "Ile osob mieszka w Norwegii?" z odpowiedzia w tysiacach (5600) nie jest pytaniem o wyczucie
     * skali, tylko o przeliczanie: nikt nie trzyma w glowie ludnosci kraju w tysiacach. To samo
     * z powierzchnia Rosji ("17 098 tysiecy km2") i z aglomeracja Tokio ("37 000 tysiecy").
     *
     * Regula: TYSIACE wolno uzyc tylko wtedy, gdy odpowiedz jest mniejsza niz dwa tysiace tysiecy -
     * czyli tam, gdzie w milionach zostalyby same zera albo zaokraglenie zjadloby cala odpowiedz.
     * Powyzej tego progu jednostka to miliony.
     */
    @Test
    fun `tysiace tylko tam, gdzie miliony byłyby zerem`() {
        val wrongUnit = EstimationBank.all
            .filter { it.unit.contains("thousand") && it.correctValue >= THOUSAND_UNIT_CEILING }
            .map { "${it.id}: ${it.correctValue} ${it.unit}" }

        assertTrue(
            wrongUnit.isEmpty(),
            "Odpowiedz w tysiacach tam, gdzie mysli sie w milionach: $wrongUnit"
        )
    }

    /** Powyzej tylu tysiecy pytanie ma pytac w milionach - patrz test wyzej. */
    private val THOUSAND_UNIT_CEILING = 2_000
}
