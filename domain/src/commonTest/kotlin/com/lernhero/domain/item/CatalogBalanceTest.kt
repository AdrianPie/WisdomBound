package com.lernhero.domain.item

import com.lernhero.domain.combat.StatType
import com.lernhero.domain.common.Rarity
import com.lernhero.domain.craft.ItemTier
import com.lernhero.domain.item.catalog.MonsterGear
import kotlin.test.Test
import kotlin.test.assertTrue

/**
 * Katalog urosl z 14 do kilkudziesieciu pozycji, wiec bledy przestaly byc widoczne przy czytaniu.
 * Te testy pilnuja rzeczy, ktorych nie widac na ekranie, a ktore psuja rozgrywke po cichu.
 */
class CatalogBalanceTest {

    private val hero = ItemCatalog.all
    private val monster = MonsterGear.all

    @Test
    fun `kazdy prog ma czym obsluzyc obie kuznie`() {
        // Odkad tiery bramkuja kucie, pusty prog to ekran, na ktorym nie ma NIC do zrobienia.
        ItemTier.entries.forEach { tier ->
            assertTrue(
                hero.any { it.rarity == tier.rarity },
                "Prog ${tier.label} nie ma ani jednego przedmiotu bohatera"
            )
            assertTrue(
                monster.any { it.rarity == tier.rarity },
                "Prog ${tier.label} nie ma ani jednego przedmiotu kompana"
            )
        }
    }

    @Test
    fun `kazdy slot bohatera ma z czego wybierac`() {
        // Jeden przedmiot w slocie to nie wybor, tylko obowiazek.
        ItemSlot.ACTIVE.forEach { slot ->
            val count = hero.count { it.slot == slot }
            assertTrue(count >= 2, "Slot $slot ma tylko $count przedmiotow")
        }
    }

    @Test
    fun `kompani dostaja liczby, bohater reguly`() {
        // Swiadoma granica projektowa: kompanow bywa szesciu, wiec efekt na ich sprzecie mnozy sie
        // razy szesc. Zdolnosci zostaja przy bohaterze.
        val withEffects = monster.filter { it.effects.isNotEmpty() }
        assertTrue(withEffects.isEmpty(), "Sprzet kompana z efektem: ${withEffects.map { it.id }}")

        assertTrue(
            hero.count { it.effects.isNotEmpty() } >= hero.size / 2,
            "Wiekszosc sprzetu bohatera ma miec jakas zdolnosc, nie tylko statystyki"
        )
    }

    @Test
    fun `mocniejszy prog kosztuje wiecej`() {
        val averageCost = Rarity.entries.associateWith { rarity ->
            hero.filter { it.rarity == rarity }.map { it.cost }.average()
        }
        val ordered = Rarity.entries.map { averageCost.getValue(it) }
        assertTrue(
            ordered.zipWithNext().all { (lower, higher) -> lower < higher },
            "Sredni koszt ma rosnac z rzadkoscia, a jest: $averageCost"
        )
    }

    @Test
    fun `zaden przedmiot nie jest pusty`() {
        (hero + monster).forEach { item ->
            val hasStats = StatType.entries.any { item.stats[it] != 0f }
            assertTrue(
                hasStats || item.effects.isNotEmpty(),
                "Przedmiot '${item.id}' nie daje niczego"
            )
            assertTrue(item.cost > 0, "Przedmiot '${item.id}' jest za darmo")
        }
    }

    @Test
    fun `identyfikatory sie nie powtarzaja`() {
        val all = hero + monster
        val duplicates = all.groupBy { it.id }.filter { it.value.size > 1 }.keys
        assertTrue(duplicates.isEmpty(), "Zdublowane id: $duplicates")
    }

    /**
     * Filtr polek w kuzni ma sens tylko wtedy, gdy kazda polka cos zawiera - pusty przycisk
     * "bron" wyglada jak zepsuty ekran. Kategorie ukrywaja sie same, gdy sa puste, wiec ten test
     * pilnuje odwrotnej strony: ze zaden przedmiot bohatera nie wypada poza wszystkie polki.
     */
    @Test
    fun `kazda polka katalogu ma zawartosc`() {
        val byCategory = hero.groupBy { ItemCategory.of(it) }
        ItemCategory.entries.forEach { category ->
            assertTrue(
                byCategory[category].orEmpty().isNotEmpty(),
                "Polka '${category.label}' jest pusta"
            )
        }
    }
}
