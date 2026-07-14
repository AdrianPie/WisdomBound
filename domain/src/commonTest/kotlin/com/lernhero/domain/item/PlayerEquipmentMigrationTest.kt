package com.lernhero.domain.item

import com.lernhero.domain.combat.StatType
import com.lernhero.domain.item.catalog.Armors
import com.lernhero.domain.item.catalog.Weapons
import com.lernhero.domain.player.PlayerEquipment
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Zapis gracza zmienil format: piec flag zamienilo sie w mape slot -> id przedmiotu.
 * Te testy pilnuja, ze nikt nie obudzi sie rano bez ekwipunku.
 */
class PlayerEquipmentMigrationTest {

    @Test
    fun `stary zapis z flagami zamienia sie na przedmioty`() {
        val legacy = PlayerEquipment(head = true, mainHand = true)

        val migrated = legacy.migrated()

        assertEquals(Armors.PLATE_HELM.id, migrated.equipped[ItemSlot.Head.name])
        assertEquals(Weapons.PLATE_SWORD.id, migrated.equipped[ItemSlot.MainHand.name])
        assertEquals(2, migrated.equipped.size, "Reszta slotow zostaje pusta")
    }

    @Test
    fun `nowy zapis przechodzi przez migracje nietkniety`() {
        val current = PlayerEquipment(
            equipped = mapOf(ItemSlot.MainHand.name to Weapons.REAPER_GLAIVE.id)
        )

        assertEquals(current, current.migrated())
    }

    @Test
    fun `zalozenie przedmiotu wypycha poprzedni z tego samego slotu`() {
        val equipment = PlayerEquipment()
            .equip(Weapons.PLATE_SWORD)
            .equip(Weapons.REAPER_GLAIVE)

        assertEquals(Weapons.REAPER_GLAIVE, equipment.itemIn(ItemSlot.MainHand))
        assertEquals(1, equipment.equipped.size, "Slot glownej reki miesci jeden przedmiot")
    }

    @Test
    fun `zdjecie przedmiotu oproznia slot`() {
        val equipment = PlayerEquipment().equip(Armors.PLATE_HELM).unequip(ItemSlot.Head)

        assertNull(equipment.itemIn(ItemSlot.Head))
    }

    @Test
    fun `ekwipunek gracza sklada sie do statystyk i efektow walki`() {
        val loadout = PlayerEquipment()
            .equip(Weapons.REAPER_GLAIVE)
            .equip(Armors.PLATE_HELM)
            .toLoadout()

        assertEquals(9f, loadout.stats[StatType.Attack], "Atak z kosy")
        assertEquals(12f, loadout.stats[StatType.MaxHp], "Zdrowie z helmu")
        assertTrue(
            loadout.effects.size >= 3,
            "Kosa (2 efekty) i helm (1 efekt) wnosza swoje zachowania do walki"
        )
    }

    @Test
    fun `stary zapis dziala w walce dokladnie tak jak przed migracja`() {
        val loadout = PlayerEquipment(head = true, hands = true, mainHand = true).toLoadout()

        assertEquals(Armors.PLATE_HELM, loadout[ItemSlot.Head])
        assertEquals(Armors.PLATE_GAUNTLETS, loadout[ItemSlot.Hands])
        assertEquals(Weapons.PLATE_SWORD, loadout[ItemSlot.MainHand])
    }
}
