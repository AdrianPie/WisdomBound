package com.lernhero.domain.story

import com.lernhero.domain.run.BattleKind
import com.lernhero.domain.run.RunDifficulty
import com.lernhero.domain.run.RunLoot
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Powtarzanie zdobytej planszy nie moze byc najlepszym sposobem grania.
 *
 * Bylo dokladnie odwrotnie: nic w kodzie nie odrozniało pierwszego przejscia od setnego, a boss
 * ostatniej planszy to JEDNA fala placaca piec losow. Cztery powtorki tego jednego bossa byly
 * warte tyle, co przejscie wszystkich trzydziestu szesciu plansz tieru - wiec zadna sciana nie
 * byla sciana, a endless nie mial jak konkurowac z farmieniem znanego pola.
 */
class StageRepeatsTest {

    private val tier = RunDifficulty.Normal

    @Test
    fun `pierwsze przejscie placi pelna stawke, kolejne coraz mniej`() {
        assertEquals(1f, StageRepeats.payoutFraction(0))
        assertTrue(StageRepeats.payoutFraction(1) < 1f)
        assertTrue(StageRepeats.payoutFraction(2) < StageRepeats.payoutFraction(1))
        assertTrue(StageRepeats.payoutFraction(3) < StageRepeats.payoutFraction(2))
    }

    @Test
    fun `stawka schodzi do podlogi i tam zostaje`() {
        // Farmienie ma byc nieoplacalne, a nie ZABRONIONE - gracz, ktory chce powtorzyc plansze
        // dla samej walki, wciaz cos dostaje.
        assertEquals(StageRepeats.FLOOR, StageRepeats.payoutFraction(3))
        assertEquals(StageRepeats.FLOOR, StageRepeats.payoutFraction(50))
        assertTrue(StageRepeats.FLOOR > 0f)
    }

    @Test
    fun `licznik rosnie osobno dla kazdej planszy i kazdego tieru`() {
        var clears = emptyMap<String, Int>()
        clears = StageRepeats.withClear(clears, RunDifficulty.Normal, 36)
        clears = StageRepeats.withClear(clears, RunDifficulty.Normal, 36)
        clears = StageRepeats.withClear(clears, RunDifficulty.Nightmare, 36)

        assertEquals(2, StageRepeats.clearsOf(clears, RunDifficulty.Normal, 36))
        assertEquals(1, StageRepeats.clearsOf(clears, RunDifficulty.Nightmare, 36))
        assertEquals(0, StageRepeats.clearsOf(clears, RunDifficulty.Normal, 35))
    }

    @Test
    fun `skrzynia nalezy sie tylko za pierwsze zdobycie`() {
        val fresh = emptyMap<String, Int>()
        assertTrue(StageRepeats.isFirstClear(fresh, tier, 36))

        val after = StageRepeats.withClear(fresh, tier, 36)
        assertTrue(!StageRepeats.isFirstClear(after, tier, 36), "powtorka nie odpala kola fortuny")
    }

    @Test
    fun `spamowanie jednego bossa przestaje bic przejscie calego tieru`() {
        // Liczby, ktore zrodzily te zmiane: boss planszy 36 placi piec losow, a caly tier
        // dwadziescia jeden. Bez wyceny powtorek cztery szybkie powtorki jednej planszy
        // rownaly sie calej kampanii.
        val bossTickets = RunLoot.bossBonus(BattleKind.Boss, dungeonLevel = 36).tickets
        val firstClear = bossTickets * StageRepeats.payoutFraction(0)
        val tenRepeats = (1..10).sumOf { (bossTickets * StageRepeats.payoutFraction(it)).toDouble() }

        assertTrue(
            tenRepeats < firstClear * 3,
            "dziesiec powtorek ma byc warte mniej niz trzy pierwsze przejscia, a dalo $tenRepeats"
        )
    }
}
