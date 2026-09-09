package com.lernhero.domain.player

import com.lernhero.domain.craft.ItemTier
import com.lernhero.domain.item.Inventory
import com.lernhero.domain.item.ItemCatalog
import com.lernhero.domain.item.catalog.MonsterGear
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class AdminKitTest {

    @Test
    fun `daje po jednej sztuce kazdego przedmiotu bohatera`() {
        val granted = AdminKit.grant(Player())
        ItemCatalog.all.forEach { item ->
            assertTrue(
                Inventory.countOf(granted.heroInventory, item.id) >= 1,
                "Brak przedmiotu '${item.id}'"
            )
        }
    }

    /** Sprzet kompanow ma starczyc dla CALEJ druzyny - po to sa egzemplarze. */
    @Test
    fun `daje sprzet kompanow w liczbie miejsc w druzynie`() {
        val granted = AdminKit.grant(Player())
        MonsterGear.all.forEach { item ->
            assertEquals(
                AdminKit.MONSTER_COPIES,
                Inventory.countOf(granted.monsterInventory, item.id),
                "Zla liczba sztuk '${item.id}'"
            )
        }
    }

    @Test
    fun `otwiera wszystkie miejsca w druzynie`() {
        val granted = AdminKit.grant(Player())
        assertEquals(PartySlots.TOTAL, PartySlots.unlockedCount(granted.progress.level))
    }

    /** Bez postepu kampanii polowa katalogu kuzni stalaby pod klodka mimo pelnego plecaka. */
    @Test
    fun `otwiera wszystkie progi kuzni`() {
        val granted = AdminKit.grant(Player())
        ItemTier.entries.forEach { tier ->
            assertTrue(tier.isUnlocked(granted.storyProgress), "Prog ${tier.label} zamkniety")
        }
    }

    @Test
    fun `nie gubi tego co gracz juz mial`() {
        val before = Player(ownedMonsters = listOf("skeleton_warrior"))
        val granted = AdminKit.grant(before)
        assertTrue("skeleton_warrior" in granted.ownedMonsters)
    }

    @Test
    fun `pusta lista adresow nikogo nie wpuszcza przypadkiem`() {
        assertFalse(AdminAccounts.isAdmin(null))
        assertFalse(AdminAccounts.isAdmin(""))
        assertFalse(AdminAccounts.isAdmin("ktokolwiek@example.com"))
    }
}
