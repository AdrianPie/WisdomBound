package com.lernhero.domain.monster

import com.lernhero.domain.combat.StatType
import com.lernhero.domain.item.ItemSlot
import com.lernhero.domain.item.catalog.MonsterGear
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Ekwipunek kompana zapisuje sie jako mapa stringow, wiec najgrozniejsze bledy sa ciche: przedmiot
 * wpisany pod zly slot, ten sam pazur na dwoch kompanach naraz, albo auto-equip zdejmujacy sprzet
 * zamiast go poprawiac. Zadnego z nich nie widac na ekranie - widac dopiero w walce.
 */
class MonsterEquipmentTest {

    private val fire = Element.Fire
    private val claw = MonsterGear.BONE_CLAW      // Smierc, MainHand
    private val fang = MonsterGear.EMBER_FANG     // Ogien, MainHand
    private val wrap = MonsterGear.HIDE_WRAP      // uniwersalny, Chest

    @Test
    fun `zalozenie wpisuje przedmiot pod jego wlasny slot`() {
        val eq = MonsterEquipment.with(MonsterEquipment.EMPTY, fang)

        assertEquals(fang.id, eq[ItemSlot.MainHand.name])
        assertEquals(fang, MonsterEquipment.itemIn(eq, ItemSlot.MainHand))
        assertNull(MonsterEquipment.itemIn(eq, ItemSlot.Chest))
    }

    @Test
    fun `zalozenie w zajety slot podmienia przedmiot`() {
        val eq = MonsterEquipment.with(MonsterEquipment.with(MonsterEquipment.EMPTY, claw), fang)

        assertEquals(1, eq.size, "Dwa przedmioty w jednym slocie")
        assertEquals(fang.id, eq[ItemSlot.MainHand.name])
    }

    @Test
    fun `zdjecie usuwa wpis`() {
        val eq = MonsterEquipment.with(MonsterEquipment.EMPTY, fang)
        assertTrue(MonsterEquipment.without(eq, ItemSlot.MainHand).isEmpty())
    }

    @Test
    fun `nieznane id w zapisie jest ignorowane`() {
        // Przedmiot usuniety z katalogu miedzy wersjami gry nie moze wywrocic ekranu kompana.
        val eq = mapOf(ItemSlot.MainHand.name to "mg_nie_ma_takiego")
        assertNull(MonsterEquipment.itemIn(eq, ItemSlot.MainHand))
        assertTrue(MonsterEquipment.itemsOf(eq).isEmpty())
    }

    /* ---------------------------------- Zywiol i statystyki ---------------------------------- */

    @Test
    fun `zgodny zywiol daje wiecej statystyk niz niezgodny`() {
        val matching = MonsterEquipment.statsOf(
            MonsterEquipment.with(MonsterEquipment.EMPTY, fang),
            fire
        )
        val mismatched = MonsterEquipment.statsOf(
            MonsterEquipment.with(MonsterEquipment.EMPTY, claw),
            fire
        )

        // Kiel ognia ma bazowo 6 ataku, pazur smierci 4 - ale liczy sie, ze premia i kara
        // faktycznie wchodza do sumy, a nie tylko do opisu.
        assertEquals(fang.stats[StatType.Attack] * MonsterGear.MATCH_BONUS, matching[StatType.Attack])
        assertEquals(claw.stats[StatType.Attack] * MonsterGear.MISMATCH_PENALTY, mismatched[StatType.Attack])
    }

    @Test
    fun `przedmiot uniwersalny nie jest skalowany`() {
        val stats = MonsterEquipment.statsOf(
            MonsterEquipment.with(MonsterEquipment.EMPTY, wrap),
            fire
        )
        assertEquals(wrap.stats[StatType.MaxHp], stats[StatType.MaxHp])
    }

    @Test
    fun `pusty ekwipunek nie daje zadnych statystyk`() {
        val stats = MonsterEquipment.statsOf(MonsterEquipment.EMPTY, fire)
        StatType.entries.forEach { assertEquals(0f, stats[it]) }
    }

    /* -------------------------------------- Auto-equip -------------------------------------- */

    @Test
    fun `auto equip wybiera bron pasujaca do zywiolu`() {
        // Oba to MainHand: kiel ognia (6 atk, zgodny) kontra pazur smierci (4 atk, niezgodny).
        val eq = MonsterEquipment.autoEquip(listOf(claw.id, fang.id), fire)
        assertEquals(fang.id, eq[ItemSlot.MainHand.name])
    }

    @Test
    fun `auto equip zapelnia kazdy slot ktory ma czym`() {
        val eq = MonsterEquipment.autoEquip(listOf(fang.id, wrap.id), fire)

        assertEquals(2, eq.size)
        assertNotNull(eq[ItemSlot.MainHand.name])
        assertNotNull(eq[ItemSlot.Chest.name])
    }

    @Test
    fun `auto equip pomija sprzet noszony przez innych kompanow`() {
        // Pula liczy SZTUKI, wiec ten sam kiel nie moze wisiec na dwoch kompanach naraz.
        val eq = MonsterEquipment.autoEquip(
            ownedIds = listOf(claw.id, fang.id),
            element = fire,
            takenBy = setOf(fang.id)
        )
        assertEquals(claw.id, eq[ItemSlot.MainHand.name])
    }

    @Test
    fun `auto equip bez sprzetu zwraca pusty ekwipunek`() {
        assertTrue(MonsterEquipment.autoEquip(emptyList(), fire).isEmpty())
    }

    @Test
    fun `auto equip zaklada tylko sloty kompana`() {
        val eq = MonsterEquipment.autoEquip(MonsterGear.all.map { it.id }, fire)
        eq.keys.forEach { slotName ->
            assertTrue(
                MonsterGear.SLOTS.any { it.name == slotName },
                "Auto-equip zalozyl cos w slocie '$slotName', ktorego kompan nie ma"
            )
        }
    }

    @Test
    fun `zajete gdzie indziej pomija wlasny ekwipunek kompana`() {
        // Kompan nie moze "blokowac sam sobie" wlasnego sprzetu - inaczej auto-equip
        // zdejmowalby mu to, co juz nosi.
        val all = mapOf(
            "orc_warrior" to mapOf(ItemSlot.MainHand.name to fang.id),
            "skeleton_mage" to mapOf(ItemSlot.Chest.name to wrap.id)
        )
        val taken = MonsterEquipment.equippedElsewhere(all, "orc_warrior")

        assertTrue(fang.id !in taken, "Wlasny sprzet kompana zostal uznany za zajety")
        assertTrue(wrap.id in taken)
    }
}
