package com.lernhero.domain.jewel

import com.lernhero.domain.combat.StatType
import com.lernhero.domain.item.ItemCatalog
import com.lernhero.domain.item.ItemInstance
import com.lernhero.domain.item.resolve
import com.lernhero.domain.progress.AchievementCatalog
import com.lernhero.domain.progress.CategoryStreakRun
import com.lernhero.domain.progress.GameStats
import com.lernhero.domain.progress.RunStats
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class JewelTest {

    private val ruby = Jewel(JewelType.Ruby, JewelGrade.Chipped)
    private val epicItem = ItemCatalog.all.first { Sockets.countFor(it) == 2 }
    private val commonItem = ItemCatalog.all.first { Sockets.countFor(it) == 0 }

    @Test
    fun `socketing moves a jewel from the pouch into the item`() {
        val state = JewelState(listOf(ItemInstance("a", epicItem.id)), mapOf(ruby.id to 2))
        val result = assertNotNull(JewelBench.socket(state, "a", ruby.id, capacity = 2))
        assertEquals(listOf(ruby.id), result.bag.single().jewels)
        assertEquals(1, result.pouch[ruby.id])
    }

    @Test
    fun `a full item or a missing jewel refuses the socket`() {
        val full = JewelState(listOf(ItemInstance("a", epicItem.id, jewels = listOf(ruby.id, ruby.id))), mapOf(ruby.id to 1))
        assertNull(JewelBench.socket(full, "a", ruby.id, capacity = 2))
        val empty = JewelState(listOf(ItemInstance("a", epicItem.id)), emptyMap())
        assertNull(JewelBench.socket(empty, "a", ruby.id, capacity = 2))
    }

    @Test
    fun `unsocketing returns the jewel to the pouch`() {
        val state = JewelState(listOf(ItemInstance("a", epicItem.id, jewels = listOf(ruby.id))), emptyMap())
        val result = assertNotNull(JewelBench.unsocket(state, "a", 0))
        assertTrue(result.bag.single().jewels.isEmpty())
        assertEquals(1, result.pouch[ruby.id])
    }

    @Test
    fun `three jewels combine into one of the next cut`() {
        val pouch = assertNotNull(JewelBench.combine(mapOf(ruby.id to 4), ruby.id))
        assertEquals(1, pouch[ruby.id])
        assertEquals(1, pouch[Jewel(JewelType.Ruby, JewelGrade.Flawed).id])
        assertNull(JewelBench.combine(mapOf(ruby.id to 2), ruby.id))
        val perfect = Jewel(JewelType.Ruby, JewelGrade.Perfect)
        assertNull(JewelBench.combine(mapOf(perfect.id to 3), perfect.id))
    }

    @Test
    fun `socketed jewels add their stats in combat`() {
        val plain = assertNotNull(ItemInstance("a", epicItem.id).resolve())
        val jeweled = assertNotNull(ItemInstance("a", epicItem.id, jewels = listOf(ruby.id)).resolve())
        assertEquals(plain.stats[StatType.Attack] + 1f, jeweled.stats[StatType.Attack])
    }

    @Test
    fun `jewels beyond the item's sockets are ignored`() {
        val plain = assertNotNull(ItemInstance("a", commonItem.id).resolve())
        val cheat = assertNotNull(ItemInstance("a", commonItem.id, jewels = listOf(ruby.id)).resolve())
        assertEquals(plain.stats, cheat.stats)
    }

    @Test
    fun `a topic streak carries over between battles until a wrong answer in that topic`() {
        var run = CategoryStreakRun()
        repeat(12) { run = run.record(true) }
        val afterFirst = GameStats() + RunStats(categoryStreaks = mapOf("Capitals" to run))

        var second = CategoryStreakRun()
        repeat(20) { second = second.record(true) }
        second = second.record(false)
        repeat(3) { second = second.record(true) }
        val afterSecond = afterFirst + RunStats(categoryStreaks = mapOf("Capitals" to second))

        assertEquals(32, afterSecond.bestCategoryStreaks["Capitals"])
        assertEquals(3, afterSecond.categoryStreaks["Capitals"])
    }

    @Test
    fun `every achievement id is unique and topic achievements pay a jewel`() {
        val ids = AchievementCatalog.all.map { it.id }
        assertEquals(ids.size, ids.toSet().size)
        assertTrue(AchievementCatalog.all.filter { it.topic != null }.all { it.jewel != null })
    }
}
