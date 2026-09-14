package com.lernhero.domain.run

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Endless musi miec czym placic, bo to on jest miejscem do farmienia.
 *
 * Wczesniej kazda jego fala byla [BattleKind.Normal], a premia za bossa dla `Normal` to same zera -
 * wiec tryb pomyslany jako farm nie wyplacal ani jednego losa. Sciana po kampanii stoi tymczasem
 * na gwiazdkach kompanow, czyli wlasnie na losach.
 */
class EndlessMilestonesTest {

    @Test
    fun `boss wchodzi co dziesiata fale`() {
        assertEquals(BattleKind.Boss, EndlessMilestones.kindFor(10))
        assertEquals(BattleKind.Boss, EndlessMilestones.kindFor(30))
        assertEquals(BattleKind.Normal, EndlessMilestones.kindFor(9))
        assertEquals(BattleKind.Normal, EndlessMilestones.kindFor(11))
    }

    @Test
    fun `fala zerowa nie jest bossem`() {
        // Inaczej walka zaczynalaby sie od bossa, zanim gracz odpowie na jedno pytanie.
        assertEquals(BattleKind.Normal, EndlessMilestones.kindFor(0))
    }

    @Test
    fun `kazdy boss endless placi losy`() {
        val bonus = RunLoot.bossBonus(EndlessMilestones.kindFor(EndlessMilestones.BOSS_INTERVAL), dungeonLevel = 10)
        assertTrue(bonus.tickets > 0, "bez losow endless nie rusza sciany na gwiazdkach")
        assertTrue(bonus.stone > 0 && bonus.essence > 0 && bonus.iron > 0)
    }

    @Test
    fun `glebiej znaczy hojniej`() {
        val shallow = RunLoot.bossBonus(BattleKind.Boss, dungeonLevel = 10)
        val deep = RunLoot.bossBonus(BattleKind.Boss, dungeonLevel = 60)

        assertTrue(deep.tickets > shallow.tickets, "glebokosc ma podnosic liczbe losow")
        assertTrue(deep.iron > shallow.iron && deep.xp > shallow.xp)
    }

    @Test
    fun `odliczanie do bossa nigdy nie pokazuje zera`() {
        // Zero znaczyloby "juz", a fala bossa jest dopiero nastepna - licznik ma isc 10..1.
        (0 until 40).forEach { wave ->
            val left = EndlessMilestones.wavesUntilBoss(wave)
            assertTrue(left in 1..EndlessMilestones.BOSS_INTERVAL, "fala $wave -> $left")
        }
    }
}
