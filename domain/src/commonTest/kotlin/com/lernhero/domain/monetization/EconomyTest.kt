package com.lernhero.domain.monetization

import com.lernhero.domain.item.ItemCatalog
import com.lernhero.domain.common.Rarity
import com.lernhero.domain.player.LevelGoldBonus
import com.lernhero.domain.player.Player
import com.lernhero.domain.run.ChestSlot
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Zasady ekonomii z [GemValue] zapisane jako testy - zmiana ceny, ktora zrobi ze skrzyni gorszy
 * zakup niz paczka zlota, ma sie wywalic tutaj, a nie w recenzjach sklepu.
 */
class EconomyTest {

    private val levels = listOf(1, 10, 25, 50)

    @Test
    fun basicChestIsWorthMoreThanItCosts() {
        levels.forEach { level ->
            val ratio = ShopChest.Basic.expectedValue(level) / ShopChest.Basic.gemCost
            assertTrue(ratio >= 1.4f, "Basic chest on level $level returns only ${ratio}x its price")
        }
    }

    @Test
    fun epicChestIsWorthAboutTwiceItsPrice() {
        levels.forEach { level ->
            val ratio = ShopChest.Epic.expectedValue(level) / ShopChest.Epic.gemCost
            assertTrue(ratio >= 1.9f, "Epic chest on level $level returns only ${ratio}x its price")
        }
    }

    @Test
    fun epicChestGivesMoreValuePerGemThanBasic() {
        levels.forEach { level ->
            val basic = ShopChest.Basic.expectedValue(level) / ShopChest.Basic.gemCost
            val epic = ShopChest.Epic.expectedValue(level) / ShopChest.Epic.gemCost
            assertTrue(epic > basic, "Epic ($epic) should beat Basic ($basic) per gem on level $level")
        }
    }

    @Test
    fun chestBeatsBuyingGoldDirectly() {
        levels.forEach { level ->
            val goldDeal = SupplyDeal.GoldVault
            val dealRatio = GemValue.of(goldDeal.bundleFor(level), level) / goldDeal.gems
            val chestRatio = ShopChest.Basic.expectedValue(level) / ShopChest.Basic.gemCost
            assertTrue(chestRatio > dealRatio, "Chest ($chestRatio) should beat the best gold deal ($dealRatio)")
        }
    }

    @Test
    fun biggerSupplyDealsAreBetterPerGem() {
        val level = 20
        val gold = listOf(SupplyDeal.GoldPouch, SupplyDeal.GoldSack, SupplyDeal.GoldVault)
            .map { it.bundleFor(level).gold.toFloat() / it.gems }
        assertTrue(gold.zipWithNext().all { (a, b) -> b > a }, "Gold per gem should grow with the deal: $gold")
    }

    @Test
    fun biggerTicketPacksAreCheaperPerTicket() {
        val perTicket = TicketPack.entries.map { it.gems.toFloat() / it.tickets }
        assertTrue(perTicket.zipWithNext().all { (a, b) -> b < a }, "Price per ticket should fall: $perTicket")
    }

    @Test
    fun biggerGemPacksGiveMoreGemsPerZloty() {
        val perZloty = GemPack.entries.map { it.total / it.fallbackPrice.substringBefore(" ").replace(",", ".").toFloat() }
        assertTrue(perZloty.zipWithNext().all { (a, b) -> b > a }, "Gems per zloty should grow: $perZloty")
    }

    @Test
    fun firstPurchaseOfEachPackIsDoubled() {
        val pack = GemPack.Sack
        assertEquals(pack.total * 2, pack.grantFor(emptyList()))
        assertEquals(pack.total, pack.grantFor(listOf(pack.productId)))
        assertEquals(pack.total * 2, pack.grantFor(listOf(GemPack.Handful.productId)))
    }

    @Test
    fun epicPityGuaranteesLegendaryWithinTenOpens() {
        val random = Random(7)
        var pity = 0
        var sinceLegendary = 0
        repeat(500) {
            val opening = ShopChest.Epic.open(heroLevel = 10, pity = pity, random = random)
            sinceLegendary = if (opening.slot == ChestSlot.ItemLegendary) 0 else sinceLegendary + 1
            assertTrue(sinceLegendary < ShopChest.EPIC_PITY, "Went $sinceLegendary epic chests without a legendary")
            pity = opening.nextPity
        }
    }

    @Test
    fun pityCountsDownAndShowsOnTheCard() {
        assertEquals(10, ShopChest.opensToLegendary(0))
        assertEquals(1, ShopChest.opensToLegendary(9))
        val forced = ShopChest.Epic.open(heroLevel = 1, pity = 9, random = Random(1))
        assertEquals(ChestSlot.ItemLegendary, forced.slot)
        assertTrue(forced.pityTriggered)
        assertEquals(0, forced.nextPity)
    }

    @Test
    fun basicChestDoesNotTouchEpicPity() {
        val opening = ShopChest.Basic.open(heroLevel = 1, pity = 4, random = Random(3))
        assertEquals(4, opening.nextPity)
    }

    @Test
    fun openingAlwaysContainsTheGuaranteedPart() {
        val random = Random(11)
        repeat(100) {
            ShopChest.entries.forEach { chest ->
                val opening = chest.open(heroLevel = 15, pity = 0, random = random)
                val guaranteed = chest.guaranteedFor(15)
                assertTrue(opening.total.gold >= guaranteed.gold)
                assertTrue(opening.total.iron >= guaranteed.iron)
                assertTrue(opening.total.tickets >= guaranteed.tickets)
                opening.itemId?.let { id ->
                    val item = ItemCatalog.byId(id)
                    assertTrue(item != null && opening.slot.itemRarities!!.contains(item.rarity))
                }
            }
        }
    }

    @Test
    fun energyRefillGetsPricierAndHasADailyCap() {
        val prices = (0 until EnergyRefill.MAX_PER_DAY).map { EnergyRefill.priceFor(it)!! }
        assertTrue(prices.zipWithNext().all { (a, b) -> b > a })
        assertEquals(null, EnergyRefill.priceFor(EnergyRefill.MAX_PER_DAY))
    }

    @Test
    fun boughtEnergyCanGoAboveTheBar() {
        val now = 1_000_000L
        val player = Player(energy = com.lernhero.domain.energy.EnergyState(amount = 30, updatedAt = now))
        val after = player.plusBundle(Bundle(energy = EnergyRefill.AMOUNT, gold = 100, tickets = 2), now)
        assertEquals(30 + EnergyRefill.AMOUNT, after.energy.amount)
        assertEquals(100, after.money)
        assertEquals(2, after.townResources.summonTickets)
    }

    @Test
    fun levelGoldBonusIsSmall() {
        assertEquals(1f, LevelGoldBonus.multiplier(1))
        assertEquals(19, LevelGoldBonus.percent(20))
        assertEquals(1.5f, LevelGoldBonus.multiplier(200))
        // Sklep i reklamy rosna z poziomem lagodnie: 50. poziom to mniej niz 3x pierwszego.
        assertTrue(GemValue.progressFactor(50) < 3f)
    }

    @Test
    fun legendaryItemsExistForChests() {
        assertTrue(ItemCatalog.all.any { it.rarity == Rarity.Legendary })
    }
}
