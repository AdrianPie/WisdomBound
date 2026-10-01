package com.lernhero.domain.ultimate

import com.lernhero.domain.combat.live.StreakTracker
import com.lernhero.domain.combat.live.record
import com.lernhero.domain.craft.Forge
import com.lernhero.domain.item.ItemCatalog
import com.lernhero.domain.item.ItemSlot
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class UltimatesTest {

    @Test
    fun everyOrbIsARegisteredUltimateItemWithASpec() {
        val orbs = ItemCatalog.all.filter { it.slot == ItemSlot.Ultimate }
        assertTrue(orbs.size >= 10, "powinno byc co najmniej 10 kul, jest ${orbs.size}")
        orbs.forEach { item ->
            val spec = assertNotNull(Ultimates.specFor(item.id), "brak opisu ulti dla ${item.id}")
            assertTrue(spec.streak in 3..7, "${item.id}: seria ${spec.streak} poza 3..7")
        }
    }

    @Test
    fun forgeNeverOffersOrbs() {
        assertFalse(Forge.recipes.any { it.item.slot == ItemSlot.Ultimate })
    }

    @Test
    fun longerStreakNovaHitsHarderPerCast() {
        val novas = Ultimates.allSpecs.filter { it.kind == UltimateKind.Nova }.sortedBy { it.streak }
        novas.zipWithNext().forEach { (short, long) ->
            if (long.streak > short.streak) {
                assertTrue(long.power > short.power, "${long.id} (${long.streak}) powinna bic mocniej niz ${short.id}")
            }
        }
    }

    /**
     * Sedno balansu: na TEJ SAMEJ rzadkosci krotka kula wygrywa przy slabej skutecznosci, dluga -
     * przy dobrej. Rzadkosc (+10% na stopien) liczymy tu na zero, bo ona ma po prostu dawac wiecej.
     */
    @Test
    fun shortOrbsSuitWeakPlayersLongOrbsSuitStrongOnes() {
        val ember = Ultimates.specFor("orb_ember")!!
        val starfall = Ultimates.specFor("orb_starfall")!!
        assertTrue(basePerAnswer(ember, 0.7) > basePerAnswer(starfall, 0.7))
        assertTrue(basePerAnswer(starfall, 0.85) > basePerAnswer(ember, 0.85))
    }

    private fun basePerAnswer(spec: UltimateSpec, p: Double): Double {
        val rarity = ItemCatalog.byId(spec.id)!!.rarity
        return perAnswer(spec, p) / Ultimates.rarityBonus(rarity)
    }

    @Test
    fun chargeFiresAtOrbStreakAndResets() {
        var tracker = StreakTracker()
        val fired = mutableListOf<Boolean>()
        repeat(7) {
            val outcome = tracker.record(correct = true, ultiStreak = 3)
            fired += outcome.ultiTriggered
            tracker = outcome.tracker
        }
        assertEquals(listOf(false, false, true, false, false, true, false), fired)
        assertEquals(1, tracker.ultiCharge)
    }

    @Test
    fun wrongAnswerEmptiesTheCharge() {
        var tracker = StreakTracker()
        repeat(4) { tracker = tracker.record(correct = true, ultiStreak = 6).tracker }
        assertEquals(4, tracker.ultiCharge)
        tracker = tracker.record(correct = false, ultiStreak = 6).tracker
        assertEquals(0, tracker.ultiCharge)
    }

    @Test
    fun emptySlotFallsBackToBasicUltimate() {
        assertEquals(Ultimates.BASIC, Ultimates.forEquipped(null))
        assertEquals(5, Ultimates.BASIC.streak)
    }

    /** Oczekiwana moc na jedno pytanie przy skutecznosci [p] - patrz tabela w KDoc [Ultimates]. */
    private fun perAnswer(spec: UltimateSpec, p: Double): Double {
        var pn = 1.0
        repeat(spec.streak) { pn *= p }
        val expectedQuestions = (1 - pn) / ((1 - p) * pn)
        return spec.power / expectedQuestions
    }
}
