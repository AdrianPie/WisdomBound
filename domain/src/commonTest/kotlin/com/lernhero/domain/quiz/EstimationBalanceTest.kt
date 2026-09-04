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
        val modern = all.count { it.correctValue >= 1900 }
        val share = modern.toFloat() / all.size
        assertTrue(share <= 0.5f, "Lata 1900+ to ${(share * 100).toInt()}% puli - za duzo")
    }

    @Test
    fun `dawne epoki maja realna reprezentacje`() {
        val ancient = all.count { it.correctValue < 0 }
        val earlyMedieval = all.count { it.correctValue in 0..999 }
        val medieval = all.count { it.correctValue in 1000..1499 }

        assertTrue(ancient >= 20, "Starozytnosc p.n.e.: tylko $ancient pytan")
        assertTrue(earlyMedieval >= 10, "Lata 0-999: tylko $earlyMedieval pytan")
        assertTrue(medieval >= 15, "Lata 1000-1499: tylko $medieval pytan")
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
}
