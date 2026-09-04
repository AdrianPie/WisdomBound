package com.lernhero.domain.craft

import com.lernhero.domain.common.Rarity
import com.lernhero.domain.item.ItemCatalog
import com.lernhero.domain.town.TownResources
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class CraftingTest {

    @Test
    fun `kazdy przedmiot z katalogu ma recepture`() {
        assertEquals(ItemCatalog.all.size, Forge.recipes.size)
        ItemCatalog.all.forEach { item ->
            assertTrue(Forge.recipeFor(item.id) != null, "brak receptury dla ${item.id}")
        }
    }

    @Test
    fun `rzadszy przedmiot kosztuje wiecej rudy`() {
        val byRarity = Forge.recipes.groupBy { it.item.rarity }
        val cheapest = byRarity.minByOrNull { it.key.ordinal }?.value?.first()?.cost?.iron ?: 0
        val priciest = byRarity.maxByOrNull { it.key.ordinal }?.value?.first()?.cost?.iron ?: 0
        assertTrue(priciest > cheapest, "zwykly: $cheapest, najrzadszy: $priciest")
    }

    @Test
    fun `koszt sprawdza wszystkie surowce naraz`() {
        val cost = CraftCost(wood = 10, stone = 10, iron = 5, gold = 100)
        val enough = TownResources(wood = 10, stone = 10, iron = 5)

        assertTrue(cost.canAfford(enough, money = 100))
        assertFalse(cost.canAfford(enough, money = 99))
        assertFalse(cost.canAfford(enough.copy(iron = 4), money = 100))
    }

    @Test
    fun `odjecie kosztu nie rusza surowcow spoza niego`() {
        val cost = CraftCost(iron = 5, essence = 3)
        val after = cost.deductFrom(TownResources(wood = 50, stone = 40, iron = 12, essence = 10, summonTickets = 2))

        assertEquals(7, after.iron)
        assertEquals(7, after.essence)
        assertEquals(50, after.wood)
        assertEquals(2, after.summonTickets)
    }

    @Test
    fun `kazda rzecz ma dokladnie jedna walute`() {
        // Regula projektowa: kompani ida za losy, przedmioty za surowce. Zaklec juz sie NIE kupuje -
        // wpadaja z drzewka talentow (patrz TalentEffect.UnlockSpell).
        // Podwojna bramka (losy I esencja na tym samym przycisku) zrobila kiedys martwy ekran -
        // gracz z pelnym workiem losow nie mogl zrobic nic.
        Rarity.entries.forEach { rarity ->
            val cost = SummonCosts.forMonsterRarity(rarity)
            assertTrue(cost.tickets > 0, "kompan $rarity powinien kosztowac losy")
            assertEquals(0, cost.essence, "kompan $rarity nie moze wymagac esencji")
            assertEquals(0, cost.gold, "kompan $rarity nie moze wymagac zlota")
        }
    }

    @Test
    fun `pozycje kosztu pomijaja zera`() {
        assertEquals(listOf("⚙" to 5), CraftCost(iron = 5).entries())
        assertTrue(CraftCost().entries().isEmpty())
    }
}
