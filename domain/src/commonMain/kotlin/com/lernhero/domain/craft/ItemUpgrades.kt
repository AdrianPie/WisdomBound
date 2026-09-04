package com.lernhero.domain.craft

import com.lernhero.domain.item.Item
import com.lernhero.domain.item.ItemCatalog
import com.lernhero.domain.item.catalog.MonsterGear

/**
 * Ulepszanie WYKUTEGO przedmiotu: ten sam miecz, tylko mocniejszy. Poziom trzymamy per id
 * przedmiotu ([com.lernhero.domain.player.Player.itemUpgrades]), a nie jako osobna kopie
 * przedmiotu - dzieki temu przestrojenie liczb w katalogu dziala od razu na wszystkich zapisach,
 * tak samo jak przy talentach i poziomach kompanow.
 *
 * Ulepszanie NIE zjada duplikatow przedmiotow, bo plecak bohatera ich nie ma - `ownedItems` to
 * zbior id, a [com.lernhero.domain.repository.PlayerRepository.grantItem] odrzuca powtorki. Zamiast
 * tego kosztem sa surowce, ktore gracz i tak zbiera na wyprawach.
 */
object ItemUpgrades {

    const val MIN_LEVEL = 0
    const val MAX_LEVEL = 10

    /**
     * Ile procent statystyk dokłada JEDEN poziom. Liniowo, a nie wykladniczo: +10 ma byc
     * odczuwalnym celem, a nie skokiem, po ktorym reszta systemow przestaje mieć znaczenie.
     */
    const val BONUS_PER_LEVEL = 0.12f

    /** Od tego poziomu do kosztu dochodzi esencja - to jest ta "sciana" w drugiej polowie toru. */
    const val ESSENCE_FROM_LEVEL = 6

    fun levelOf(upgrades: Map<String, Int>, itemId: String): Int =
        (upgrades[itemId] ?: MIN_LEVEL).coerceIn(MIN_LEVEL, MAX_LEVEL)

    fun isMaxed(level: Int): Boolean = level >= MAX_LEVEL

    /** Mnoznik statystyk przedmiotu na danym poziomie: 0 -> 1.0x, MAX -> 2.2x. */
    fun multiplierFor(level: Int): Float =
        1f + BONUS_PER_LEVEL * level.coerceIn(MIN_LEVEL, MAX_LEVEL)

    /**
     * Koszt PRZEJSCIA na [targetLevel] (czyli z targetLevel-1). Kilka skladnikow naraz, bo o to
     * chodzi w ulepszaniu: kamien i ruda rosna z poziomem i rzadkoscia, zloto placi za robote,
     * a od [ESSENCE_FROM_LEVEL] dochodzi esencja - najrzadszy surowiec w grze.
     *
     * Zwraca null, gdy [targetLevel] wypada poza torem - nie ma kosztu wejscia na poziom, ktorego
     * nie ma.
     */
    fun costFor(item: Item, targetLevel: Int): CraftCost? {
        if (targetLevel !in (MIN_LEVEL + 1)..MAX_LEVEL) return null
        val tier = item.rarity.ordinal + 1
        return CraftCost(
            stone = 12 * tier * targetLevel,
            iron = 6 * tier * targetLevel,
            essence = if (targetLevel >= ESSENCE_FROM_LEVEL) 2 * (targetLevel - ESSENCE_FROM_LEVEL + 1) else 0,
            gold = (item.cost * 0.15f * targetLevel).toInt()
        )
    }

    /** Koszt kolejnego poziomu dla przedmiotu stojacego na [currentLevel]. Null na maksie. */
    fun nextCost(item: Item, currentLevel: Int): CraftCost? = costFor(item, currentLevel + 1)

    /**
     * Ten sam przedmiot ze statystykami przeskalowanymi poziomem. Efekty ([Item.effects]) zostaja
     * nietkniete - ulepszenie ma podbijac LICZBY, a nie zmieniac tego, co przedmiot robi.
     */
    fun upgraded(item: Item, level: Int): Item {
        if (level <= MIN_LEVEL) return item
        return item.copy(stats = item.stats.scaled(multiplierFor(level)))
    }

    /** Przedmiot z dowolnego katalogu (bohater albo kompani) juz po naliczeniu poziomu. */
    fun upgradedById(itemId: String, upgrades: Map<String, Int>): Item? {
        val item = ItemCatalog.byId(itemId) ?: MonsterGear.byId(itemId) ?: return null
        return upgraded(item, levelOf(upgrades, itemId))
    }
}
