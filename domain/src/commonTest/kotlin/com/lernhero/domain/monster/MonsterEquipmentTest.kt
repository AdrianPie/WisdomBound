package com.lernhero.domain.monster

import com.lernhero.domain.combat.StatType
import com.lernhero.domain.item.Inventory
import com.lernhero.domain.item.Item
import com.lernhero.domain.item.ItemInstance
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

    /** Plecak z jedna sztuka kazdego przedmiotu - sloty wskazuja teraz na uid, nie na typ. */
    private fun bagOf(vararg items: Item): List<ItemInstance> =
        items.fold(emptyList()) { bag, item -> Inventory.add(bag, item.id) }

    private fun List<ItemInstance>.of(item: Item): ItemInstance = first { it.itemId == item.id }

    private fun equip(bag: List<ItemInstance>, item: Item, base: Map<String, String> = MonsterEquipment.EMPTY) =
        MonsterEquipment.with(base, bag.of(item), item.slot)

    @Test
    fun `zalozenie wpisuje przedmiot pod jego wlasny slot`() {
        val bag = bagOf(fang)
        val eq = equip(bag, fang)

        assertEquals(bag.of(fang).uid, eq[ItemSlot.MainHand.name])
        assertEquals(fang, MonsterEquipment.itemIn(eq, ItemSlot.MainHand, bag))
        assertNull(MonsterEquipment.itemIn(eq, ItemSlot.Chest, bag))
    }

    @Test
    fun `zalozenie w zajety slot podmienia przedmiot`() {
        val bag = bagOf(claw, fang)
        val eq = equip(bag, fang, equip(bag, claw))

        assertEquals(1, eq.size, "Dwa przedmioty w jednym slocie")
        assertEquals(bag.of(fang).uid, eq[ItemSlot.MainHand.name])
    }

    @Test
    fun `zdjecie usuwa wpis`() {
        val eq = equip(bagOf(fang), fang)
        assertTrue(MonsterEquipment.without(eq, ItemSlot.MainHand).isEmpty())
    }

    @Test
    fun `nieznane id w zapisie jest ignorowane`() {
        // Przedmiot usuniety z katalogu miedzy wersjami gry nie moze wywrocic ekranu kompana.
        val eq = mapOf(ItemSlot.MainHand.name to "mg_nie_ma_takiego#0")
        assertNull(MonsterEquipment.itemIn(eq, ItemSlot.MainHand, emptyList()))
        assertTrue(MonsterEquipment.itemsOf(eq, emptyList()).isEmpty())
    }

    /* ---------------------------------- Zywiol i statystyki ---------------------------------- */

    @Test
    fun `zgodny zywiol daje wiecej statystyk niz niezgodny`() {
        val bag = bagOf(fang, claw)
        val matching = MonsterEquipment.statsOf(equip(bag, fang), fire, bag)
        val mismatched = MonsterEquipment.statsOf(equip(bag, claw), fire, bag)

        // Kiel ognia ma bazowo 6 ataku, pazur smierci 4 - ale liczy sie, ze premia i kara
        // faktycznie wchodza do sumy, a nie tylko do opisu.
        assertEquals(fang.stats[StatType.Attack] * MonsterGear.MATCH_BONUS, matching[StatType.Attack])
        assertEquals(claw.stats[StatType.Attack] * MonsterGear.MISMATCH_PENALTY, mismatched[StatType.Attack])
    }

    @Test
    fun `przedmiot uniwersalny nie jest skalowany`() {
        val bag = bagOf(wrap)
        val stats = MonsterEquipment.statsOf(equip(bag, wrap), fire, bag)
        assertEquals(wrap.stats[StatType.MaxHp], stats[StatType.MaxHp])
    }

    @Test
    fun `pusty ekwipunek nie daje zadnych statystyk`() {
        val stats = MonsterEquipment.statsOf(MonsterEquipment.EMPTY, fire, emptyList())
        StatType.entries.forEach { assertEquals(0f, stats[it]) }
    }

    /* -------------------------------------- Auto-equip -------------------------------------- */

    @Test
    fun `auto equip wybiera bron pasujaca do zywiolu`() {
        // Oba to MainHand: kiel ognia (6 atk, zgodny) kontra pazur smierci (4 atk, niezgodny).
        val bag = bagOf(claw, fang)
        val eq = MonsterEquipment.autoEquip(bag, fire)
        assertEquals(bag.of(fang).uid, eq[ItemSlot.MainHand.name])
    }

    @Test
    fun `auto equip zapelnia kazdy slot ktory ma czym`() {
        val eq = MonsterEquipment.autoEquip(bagOf(fang, wrap), fire)

        assertEquals(2, eq.size)
        assertNotNull(eq[ItemSlot.MainHand.name])
        assertNotNull(eq[ItemSlot.Chest.name])
    }

    @Test
    fun `auto equip pomija sprzet noszony przez innych kompanow`() {
        // Pula liczy SZTUKI, wiec ten sam kiel nie moze wisiec na dwoch kompanach naraz.
        val bag = bagOf(claw, fang)
        val eq = MonsterEquipment.autoEquip(
            bag = bag,
            element = fire,
            takenBy = setOf(bag.of(fang).uid)
        )
        assertEquals(bag.of(claw).uid, eq[ItemSlot.MainHand.name])
    }

    @Test
    fun `auto equip bez sprzetu zwraca pusty ekwipunek`() {
        assertTrue(MonsterEquipment.autoEquip(emptyList(), fire).isEmpty())
    }

    @Test
    fun `auto equip zaklada tylko sloty kompana`() {
        val eq = MonsterEquipment.autoEquip(bagOf(*MonsterGear.all.toTypedArray()), fire)
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
        val bag = bagOf(fang, wrap)
        val all = mapOf(
            "orc_warrior" to mapOf(ItemSlot.MainHand.name to bag.of(fang).uid),
            "skeleton_mage" to mapOf(ItemSlot.Chest.name to bag.of(wrap).uid)
        )
        val taken = MonsterEquipment.equippedElsewhere(all, "orc_warrior")

        assertTrue(bag.of(fang).uid !in taken, "Wlasny sprzet kompana zostal uznany za zajety")
        assertTrue(bag.of(wrap).uid in taken)
    }
}
