package com.lernhero.domain.craft

import com.lernhero.domain.common.Rarity
import com.lernhero.domain.item.Item
import com.lernhero.domain.item.ItemCatalog
import com.lernhero.domain.spell.Spell
import com.lernhero.domain.spell.SpellCatalog
import com.lernhero.domain.town.TownResources

/**
 * Koszt jednego wytworzenia. Jeden ksztalt dla kuzni i dla przyzywania, zeby sprawdzanie "czy stac"
 * i odejmowanie zasobow bylo napisane RAZ ([canAfford]/[deductFrom]) - wczesniej kazdy ekran liczyl
 * to sobie sam i kuznia potrafila zabrac surowce nie dajac nic w zamian.
 */
data class CraftCost(
    val wood: Int = 0,
    val stone: Int = 0,
    val iron: Int = 0,
    val essence: Int = 0,
    val tickets: Int = 0,
    val gold: Int = 0
) {
    fun canAfford(resources: TownResources, money: Int): Boolean =
        resources.wood >= wood &&
            resources.stone >= stone &&
            resources.iron >= iron &&
            resources.essence >= essence &&
            resources.summonTickets >= tickets &&
            money >= gold

    fun deductFrom(resources: TownResources): TownResources = resources.copy(
        wood = resources.wood - wood,
        stone = resources.stone - stone,
        iron = resources.iron - iron,
        essence = resources.essence - essence,
        summonTickets = resources.summonTickets - tickets
    )

    /** Pozycje kosztu, ktore faktycznie cos kosztuja - do wyswietlenia bez pustych zer. */
    fun entries(): List<Pair<String, Int>> = buildList {
        if (wood > 0) add("🪵" to wood)
        if (stone > 0) add("🪨" to stone)
        if (iron > 0) add("⚙" to iron)
        if (essence > 0) add("✨" to essence)
        if (tickets > 0) add("🎟" to tickets)
        if (gold > 0) add("💰" to gold)
    }
}

/**
 * Receptura kuzni: PRAWDZIWY przedmiot z [ItemCatalog] za surowce z lochu. To zastapilo sklep -
 * ekwipunku nie kupuje sie juz za zloto, tylko wykuwa z tego, co przyniesie sie z wyprawy.
 *
 * Koszt liczy sie automatycznie z rzadkosci i ceny katalogowej przedmiotu ([costFor]), wiec dolozenie
 * miecza do katalogu od razu daje recepture - bez dopisywania czegokolwiek tutaj.
 */
data class ForgeRecipe(
    val item: Item,
    val cost: CraftCost
) {
    val id: String get() = item.id
}

object Forge {

    /**
     * Cena wykucia. Rzadkosc podbija zapotrzebowanie na rude (material "jakosciowy"), a cena
     * katalogowa przedmiotu tylko dokłada zloto - dzieki temu legendarny przedmiot jest scianą
     * z materialow, a nie po prostu drozszym zakupem.
     */
    fun costFor(item: Item): CraftCost {
        val tier = item.rarity.ordinal + 1
        return CraftCost(
            wood = 15 * tier,
            stone = 20 * tier,
            iron = 8 * tier * tier,
            gold = (item.cost * 0.35f).toInt()
        )
    }

    val recipes: List<ForgeRecipe>
        get() = ItemCatalog.all
            .sortedWith(compareBy({ it.rarity.ordinal }, { it.slot.ordinal }))
            .map { ForgeRecipe(item = it, cost = costFor(it)) }

    fun recipeFor(itemId: String): ForgeRecipe? = ItemCatalog.byId(itemId)?.let { ForgeRecipe(it, costFor(it)) }
}

/**
 * Koszty przyzywania i odblokowywania zaklec.
 *
 * KAZDA rzecz ma DOKLADNIE JEDNA walute: kompani ida za losy (ze skrzyn po bossach), zaklecia za
 * esencje (z lochow). Pierwsza wersja brala za kompana losy I esencje naraz - efekt byl taki, ze
 * gracz z pelnym workiem losow nie mogl zrobic nic, bo brakowalo mu drugiej waluty. Podwojna
 * bramka na tym samym przycisku zawsze konczy sie martwym ekranem; jedna waluta = jasna odpowiedz
 * na pytanie "czego mi brakuje".
 */
object SummonCosts {

    /** Rzadszy kompan kosztuje wiecej LOSOW - i tylko losow. */
    fun forMonsterRarity(rarity: Rarity): CraftCost = when (rarity) {
        Rarity.Common -> CraftCost(tickets = 1)
        Rarity.Uncommon -> CraftCost(tickets = 1)
        Rarity.Rare -> CraftCost(tickets = 2)
        Rarity.Epic -> CraftCost(tickets = 3)
        Rarity.Legendary -> CraftCost(tickets = 5)
    }

    fun forSpell(spell: Spell): CraftCost = CraftCost(
        essence = 40 + spell.rarity.ordinal * 55
    )

    val spellOffers: List<Pair<Spell, CraftCost>>
        get() = SpellCatalog.all.sortedBy { it.rarity.ordinal }.map { it to forSpell(it) }
}
