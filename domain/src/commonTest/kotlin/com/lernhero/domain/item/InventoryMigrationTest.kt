package com.lernhero.domain.item

import com.lernhero.domain.item.catalog.Armors
import com.lernhero.domain.item.catalog.MonsterGear
import com.lernhero.domain.item.catalog.Weapons
import com.lernhero.domain.player.Player
import com.lernhero.domain.player.PlayerEquipment
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

/**
 * Przejscie plecaka z "listy ID" na SZTUKI ([ItemInstance]).
 *
 * Zmiana dotyka zapisu wydanej juz gry, wiec najwazniejszy tu jest nie nowy format, tylko to, ze
 * STARY wciaz sie czyta i nikomu nic nie ginie.
 */
class InventoryMigrationTest {

    private val sword = Weapons.PLATE_SWORD.id
    private val helm = Armors.PLATE_HELM.id

    @Test
    fun `stary plecak czyta sie jako sztuki, z zachowaniem poziomow ulepszen`() {
        val legacy = Player(
            ownedItems = listOf(sword, helm),
            itemUpgrades = mapOf(sword to 4)
        )

        val bag = legacy.heroBag()

        assertEquals(2, bag.size)
        assertEquals(4, bag.first { it.itemId == sword }.level, "Poziom ulepszenia przepadl")
        assertEquals(0, bag.first { it.itemId == helm }.level)
    }

    @Test
    fun `powtorzone id w starym zapisie nie rozmnaza przedmiotow`() {
        // Stary format byl zbiorem, wiec duplikat to slad po bledzie zapisu, a nie druga sztuka.
        val legacy = Player(ownedItems = listOf(sword, sword, sword))

        assertEquals(1, legacy.heroBag().size)
    }

    @Test
    fun `nowy plecak wygrywa ze starym i nie jest przerabiany`() {
        val instance = ItemInstance(uid = "$sword#7", itemId = sword, level = 9)
        val player = Player(
            heroInventory = listOf(instance),
            // Stare pola zostaly w dokumencie po poprzedniej wersji - maja byc zignorowane.
            ownedItems = listOf(helm),
            itemUpgrades = mapOf(helm to 3)
        )

        assertEquals(listOf(instance), player.heroBag())
    }

    @Test
    fun `sloty ze starego zapisu wskazuja na pierwsza sztuke`() {
        val legacy = Player(
            equipment = PlayerEquipment(equipped = mapOf(ItemSlot.MainHand.name to sword)),
            monsterEquipment = mapOf("wolf" to mapOf(ItemSlot.MainHand.name to "claw"))
        )

        assertEquals("$sword#0", legacy.migratedHeroEquipment().equipped[ItemSlot.MainHand.name])
        assertEquals("claw#0", legacy.migratedMonsterEquipment()["wolf"]?.get(ItemSlot.MainHand.name))
    }

    @Test
    fun `sloty w nowym formacie zostaja nietkniete`() {
        val player = Player(
            equipment = PlayerEquipment(equipped = mapOf(ItemSlot.MainHand.name to "$sword#3")),
            monsterEquipment = mapOf("wolf" to mapOf(ItemSlot.MainHand.name to "claw#2"))
        )

        assertEquals("$sword#3", player.migratedHeroEquipment().equipped[ItemSlot.MainHand.name])
        assertEquals("claw#2", player.migratedMonsterEquipment()["wolf"]?.get(ItemSlot.MainHand.name))
    }

    @Test
    fun `plecaki bohatera i kompanow zostaja rozdzielone`() {
        val player = Player(ownedItems = listOf(sword), ownedMonsterItems = listOf("claw"))

        assertEquals(listOf(sword), player.heroBag().map { it.itemId })
        assertEquals(listOf("claw"), player.monsterBag().map { it.itemId })
    }

    /* ------------------------------- Sam model sztuk ------------------------------- */

    @Test
    fun `kazda dolozona sztuka dostaje wlasny uid`() {
        val bag = Inventory.add(emptyList(), itemId = "knife", count = 10)

        assertEquals(10, bag.size, "Dziesiec nozykow to dziesiec sztuk")
        assertEquals(10, bag.map { it.uid }.distinct().size, "Uid sie powtorzyly")
        assertTrue(bag.all { it.itemId == "knife" })
    }

    @Test
    fun `ulepszenie dotyka jednej sztuki, nie calego typu`() {
        // To byl caly powod tej zmiany: wczesniej poziom siedzial w mapie kluczowanej ID
        // przedmiotu, wiec podniesienie jednego nozyka podnosilo wszystkie.
        val bag = Inventory.add(emptyList(), itemId = "knife", count = 3)
        val upgraded = Inventory.withLevel(bag, bag[1].uid, level = 5)

        assertEquals(listOf(0, 5, 0), upgraded.map { it.level })
    }

    @Test
    fun `uid nie zderza sie po usunieciu sztuki ze srodka`() {
        val bag = Inventory.add(emptyList(), "knife", count = 3)
        val afterRemoval = Inventory.remove(bag, "knife#1")
        val refilled = Inventory.add(afterRemoval, "knife")

        assertEquals(3, refilled.size)
        assertEquals(3, refilled.map { it.uid }.distinct().size, "Nowa sztuka przejela cudze uid")
    }

    @Test
    fun `liczenie sztuk danego typu`() {
        val bag = Inventory.add(Inventory.add(emptyList(), "knife", 4), "shield", 2)

        assertEquals(4, Inventory.countOf(bag, "knife"))
        assertEquals(2, Inventory.countOf(bag, "shield"))
        assertEquals(0, Inventory.countOf(bag, "bow"))
    }

    @Test
    fun `zaden przedmiot w katalogu nie ma separatora w id`() {
        // Na tym stoi rozroznianie starego i nowego formatu w slotach: gdyby id zawieralo "#",
        // stary zapis czytalby sie jako nowy i sloty by opustoszaly.
        val ids = ItemCatalog.all.map { it.id } + MonsterGear.all.map { it.id }

        assertTrue(
            InventoryMigrationInvariant.holdsFor(ids),
            "Przedmiot z '${Inventory.UID_SEPARATOR}' w id: " +
                ids.filter { it.contains(Inventory.UID_SEPARATOR) }
        )
    }

    @Test
    fun `uid niesie id przedmiotu`() {
        assertEquals("knife", Inventory.itemIdOf("knife#12"))
        // Id z myslnikiem czy kropka ma przejsc bez szwanku - tniemy po OSTATNIM separatorze.
        assertEquals("plate-sword.v2", Inventory.itemIdOf("plate-sword.v2#3"))
        assertNotEquals("knife#12", Inventory.itemIdOf("knife#12"))
    }
}
