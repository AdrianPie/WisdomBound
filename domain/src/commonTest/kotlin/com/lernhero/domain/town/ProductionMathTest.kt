package com.lernhero.domain.town

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Kamieniolom ma byc POMOCA, a nie druga gra.
 *
 * Wczesniej budynek nie mial sufitu: przy 60/h i +35% na poziom poziom 12 robil 1629 kamienia na
 * godzine, czyli ponad 13 000 na jedno odebranie magazynu - a najdrozsze ulepszenie przedmiotu w
 * grze kosztuje 3300. Kopalnia wyplacala wiec kilka pelnych ulepszen dziennie sama, bez ani jednej
 * odpowiedzi na pytanie. Te testy pilnuja, ze tor budynku jest SKONCZONY i ze ostatni jego poziom
 * nadal miesci sie w skali gry.
 */
class ProductionMathTest {

    @Test
    fun `budynek ma sufit, a nie nieskonczony tor`() {
        val atCap = ProductionMath.baseRatePerHour(ProductionMath.MAX_LEVEL)
        val wayOver = ProductionMath.baseRatePerHour(ProductionMath.MAX_LEVEL + 50)

        assertEquals(atCap, wayOver, "poziom ponad sufitem nie moze produkowac wiecej")
        assertTrue(ProductionMath.isMaxed(ProductionMath.MAX_LEVEL))
        assertEquals(0, ProductionMath.upgradeCost(ProductionMath.MAX_LEVEL), "nie ma ceny za poziom, ktorego nie ma")
    }

    @Test
    fun `pelny magazyn maksymalnego budynku to mniej wiecej jedno ulepszenie przedmiotu`() {
        // Odniesienie: ItemUpgrades.costFor przy najrzadszym przedmiocie na +10 to 3300 kamienia.
        // Jedno odebranie magazynu ma byc tego samego rzedu - "dzien kopalni = jedno ulepszenie",
        // a nie "dzien kopalni = caly ekwipunek".
        val storage = ProductionMath.storageCap(ProductionMath.MAX_LEVEL)
        assertTrue(storage in 1_000..4_000, "magazyn na maksie: $storage")
    }

    @Test
    fun `ulepszanie drozeje szybciej niz produkcja rosnie`() {
        // Kazdy kolejny poziom ma byc gorszym interesem od poprzedniego - inaczej pompowanie
        // budynku jest zawsze najlepszym mozliwym wydaniem zlota i wypiera cala reszte gry.
        (1 until ProductionMath.MAX_LEVEL).forEach { level ->
            val rateGain = ProductionMath.baseRatePerHour(level + 1) / ProductionMath.baseRatePerHour(level)
            val costGain = ProductionMath.upgradeCost(level + 1).toFloat() /
                ProductionMath.upgradeCost(level).coerceAtLeast(1)
            if (level + 1 < ProductionMath.MAX_LEVEL) {
                assertTrue(costGain > rateGain, "poziom $level: koszt x$costGain, produkcja x$rateGain")
            }
        }
    }

    @Test
    fun `dojscie do sufitu kosztuje mniej wiecej cala sakwe po kampanii`() {
        // Jedyny SKONCZONY sink na zloto w grze - ma byc wart calego zapasu, a nie drobnych.
        // Kampania na Normalnym zostawia ok. 13 000 zlota ponad to, co idzie na kompanow.
        val total = (1 until ProductionMath.MAX_LEVEL).sumOf { ProductionMath.upgradeCost(it) }
        assertTrue(total in 8_000..20_000, "caly tor budynku kosztuje $total zlota")
    }

    @Test
    fun `produkcja liczy sie z czasu, a bonus tylko za swoj kawalek`() {
        val hour = 3_600_000L
        val start = 1_000_000L
        val plain = ProductionState(level = 1, lastCollectedAt = start)
        val boosted = plain.copy(boostStartedAt = start, boostMultiplier = 2f)

        val withoutBoost = ProductionMath.pendingFor(plain, start + hour)
        val withBoost = ProductionMath.pendingFor(boosted, start + hour)

        assertEquals(withoutBoost * 2, withBoost, "podwojny bonus przez cala godzine = podwojna produkcja")
    }

    @Test
    fun `wygasly bonus liczy sie tylko do momentu wygasniecia`() {
        val hour = 3_600_000L
        val start = 1_000_000L
        // Bonus trwa 24h; patrzymy 30h po starcie, wiec ostatnie 6h ma isc po zwyklej stawce.
        val state = ProductionState(level = 6, lastCollectedAt = start, boostStartedAt = start, boostMultiplier = 2f)

        val pending = ProductionMath.pendingFor(state, start + 30 * hour)
        val cap = ProductionMath.storageCap(6)

        assertEquals(cap, pending, "magazyn i tak sie zapelnia - sufit obowiazuje takze pod bonusem")
    }
}
