package com.lernhero.domain.item

import com.lernhero.domain.combat.StatType
import com.lernhero.domain.item.catalog.Armors
import com.lernhero.domain.item.catalog.Weapons
import com.lernhero.domain.player.Player
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
        val legacy = PlayerEquipment(hands = true, mainHand = true)

        val migrated = legacy.migrated()

        assertEquals(Armors.PLATE_GAUNTLETS.id, migrated.equipped[ItemSlot.Hands.name])
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

    /** Plecak z jedna sztuka kazdego z podanych przedmiotow - skrot na potrzeby testow. */
    private fun bagOf(vararg items: Item): List<ItemInstance> =
        items.fold(emptyList()) { bag, item -> Inventory.add(bag, item.id) }

    private fun List<ItemInstance>.of(item: Item): ItemInstance = first { it.itemId == item.id }

    @Test
    fun `zalozenie przedmiotu wypycha poprzedni z tego samego slotu`() {
        val bag = bagOf(Weapons.PLATE_SWORD, Weapons.REAPER_GLAIVE)
        val equipment = PlayerEquipment()
            .equip(bag.of(Weapons.PLATE_SWORD), ItemSlot.MainHand)
            .equip(bag.of(Weapons.REAPER_GLAIVE), ItemSlot.MainHand)

        assertEquals(Weapons.REAPER_GLAIVE, equipment.itemIn(ItemSlot.MainHand, bag))
        assertEquals(1, equipment.equipped.size, "Slot glownej reki miesci jeden przedmiot")
    }

    @Test
    fun `dwie kopie tego samego miecza to dwa rozne przedmioty`() {
        // Sedno przejscia na egzemplarze: slot wskazuje na SZTUKE, wiec da sie odroznic
        // miecz na +5 od surowego miecza tego samego typu.
        val bag = Inventory.withLevel(
            Inventory.add(emptyList(), Weapons.PLATE_SWORD.id, count = 2),
            "${Weapons.PLATE_SWORD.id}#1",
            level = 5
        )
        val raw = bag.first { it.level == 0 }
        val upgraded = bag.first { it.level == 5 }

        val withRaw = PlayerEquipment().equip(raw, ItemSlot.MainHand)
        val withUpgraded = PlayerEquipment().equip(upgraded, ItemSlot.MainHand)

        val rawAttack = withRaw.itemIn(ItemSlot.MainHand, bag)!!.stats[StatType.Attack]
        val upgradedAttack = withUpgraded.itemIn(ItemSlot.MainHand, bag)!!.stats[StatType.Attack]
        assertTrue(upgradedAttack > rawAttack, "Ulepszona sztuka ma bic mocniej niz surowa")
    }

    @Test
    fun `zdjecie przedmiotu oproznia slot`() {
        val bag = bagOf(Armors.PLATE_GAUNTLETS)
        val equipment = PlayerEquipment()
            .equip(bag.of(Armors.PLATE_GAUNTLETS), ItemSlot.Hands)
            .unequip(ItemSlot.Hands)

        assertNull(equipment.itemIn(ItemSlot.Hands, bag))
    }

    @Test
    fun `ekwipunek gracza sklada sie do statystyk i efektow walki`() {
        val bag = bagOf(Weapons.REAPER_GLAIVE, Armors.AEGIS_SHIELD)
        val loadout = PlayerEquipment()
            .equip(bag.of(Weapons.REAPER_GLAIVE), ItemSlot.MainHand)
            .equip(bag.of(Armors.AEGIS_SHIELD), ItemSlot.OffHand)
            .toLoadout(bag)

        assertEquals(9f, loadout.stats[StatType.Attack], "Atak z kosy")
        assertEquals(20f, loadout.stats[StatType.MaxHp], "Zdrowie z tarczy")
        assertTrue(
            loadout.effects.size >= 4,
            "Kosa (2 efekty) i tarcza (2 efekty) wnosza swoje zachowania do walki"
        )
    }

    @Test
    fun `stary zapis dziala w walce dokladnie tak jak przed migracja`() {
        // Pelna sciezka najstarszego zapisu: piec flag -> mapa slotow -> uid sztuk -> walka.
        val player = Player(
            equipment = PlayerEquipment(head = true, hands = true, mainHand = true),
            ownedItems = listOf(
                Armors.PLATE_GAUNTLETS.id,
                Weapons.PLATE_SWORD.id
            )
        )
        val loadout = player.migratedHeroEquipment().toLoadout(player.heroBag())

        assertEquals(Armors.PLATE_GAUNTLETS, loadout[ItemSlot.Hands])
        assertEquals(null, loadout[ItemSlot.Head], "Helmy wycofane - flaga head nic juz nie zaklada")
        assertEquals(Weapons.PLATE_SWORD, loadout[ItemSlot.MainHand])
    }
}
