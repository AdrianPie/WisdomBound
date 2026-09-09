package com.lernhero.domain.story

import com.lernhero.domain.common.Rarity
import com.lernhero.domain.item.Item
import com.lernhero.domain.item.ItemCatalog
import kotlin.random.Random

/**
 * Pula lupu planszy - to, co pokazuje przycisk LOOT. Jawna lista "co moze wypasc", tak jak jawne
 * szanse na kole fortuny ze skrzyni bossa: gracz ma widziec, po co bije akurat te plansze.
 *
 * Rzadsze przedmioty pojawiaja sie w puli dopiero na dalszych planszach i wyzszych tierach -
 * dzieki temu "epicka nagroda" jest realnym powodem, zeby wrocic tu na Koszmarze.
 */
object StageLoot {

    /** Najwyzsza rzadkosc, jaka w ogole moze wypasc na tej planszy. */
    fun maxRarity(globalStage: Int, tier: com.lernhero.domain.run.RunDifficulty): Rarity {
        val depth = globalStage + tier.ordinal * 25
        return when {
            depth >= 80 -> Rarity.Legendary
            depth >= 50 -> Rarity.Epic
            depth >= 25 -> Rarity.Rare
            depth >= 10 -> Rarity.Uncommon
            else -> Rarity.Common
        }
    }

    /**
     * Przedmioty, ktore moga wypasc na tej planszy - deterministycznie z numeru planszy, wiec
     * podglad LOOT pokazuje zawsze to samo, co realnie wpada po walce.
     */
    fun itemPool(globalStage: Int, tier: com.lernhero.domain.run.RunDifficulty): List<Item> {
        val cap = maxRarity(globalStage, tier)
        val pool = ItemCatalog.all.filter { it.rarity.ordinal <= cap.ordinal }
        if (pool.isEmpty()) return emptyList()

        val random = Random(globalStage.toLong() * 977 + tier.ordinal)
        return pool.shuffled(random).take(POOL_SIZE)
    }

    /** Szansa, ze po wygranej wypadnie JAKIKOLWIEK przedmiot (boss ma wyraznie wieksza). */
    fun dropChance(isBoss: Boolean): Float = if (isBoss) 0.35f else 0.08f

    /**
     * Losuje lup po WYGRANEJ walce na planszy kampanii - null, gdy nic nie wypadlo.
     *
     * To jest druga polowa [itemPool]: dialog LOOT pokazuje pule i procent, a to wywolanie
     * faktycznie oddaje przedmiot. Przez dluzszy czas istniala tylko pierwsza polowa - gracz
     * widzial jawna liste nagrod i szanse, a po walce nie dostawal NIGDY nic.
     *
     * Losujemy z tej samej [itemPool], wiec podglad nie moze sie rozjechac z rzeczywistoscia.
     */
    fun roll(
        globalStage: Int,
        tier: com.lernhero.domain.run.RunDifficulty,
        isBoss: Boolean,
        random: Random = Random.Default
    ): Item? {
        if (random.nextFloat() >= dropChance(isBoss)) return null
        val pool = itemPool(globalStage, tier)
        if (pool.isEmpty()) return null
        return pool[random.nextInt(pool.size)]
    }

    private const val POOL_SIZE = 4
}
