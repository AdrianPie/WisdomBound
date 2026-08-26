package com.lernhero.domain.run

import com.lernhero.domain.common.Rarity
import kotlin.random.Random

/**
 * Jedno pole kola fortuny ze skrzyni po bossie. Kolo ma STALE pola (osiem, zawsze te same i zawsze w
 * tej samej kolejnosci) - losowa jest tylko waga, z jaka wskaznik na ktoryms staje. Dzieki temu
 * gracz widzi caly stol nagrod, zanim zakreci: wie, ze legendarny przedmiot jest na kole, i wie, ze
 * jest jeden na osiem pol - a mimo to trafia go rzadko. To ta sama sztuczka co w gachach: pelna
 * jawnosc puli, ukryte wagi.
 */
enum class ChestSlot(
    val label: String,
    val shortLabel: String,
    val emoji: String,
    /** Waga bazowa. Nie sumuje sie do 100 - liczy sie tylko proporcja (patrz [ChestWheel.roll]). */
    val weight: Float,
    /**
     * Ile z bonusu za poziom lochu lapie to pole. 0 = poziom nic nie zmienia (zloto), 1 = pelny
     * bonus (rzadkie nagrody rosna z glebokoscia lochu, zeby 10. poziom mial po co bic bossa).
     */
    val depthScaling: Float
) {
    GoldSmall("60 monet", "60 zl", "💰", weight = 24f, depthScaling = 0f),
    GoldMedium("150 monet", "150 zl", "💰", weight = 17f, depthScaling = 0f),
    GoldLarge("400 monet", "400 zl", "💰", weight = 6f, depthScaling = 0.5f),
    SummonTicket("Los do Summon Center", "1 los", "🎟", weight = 15f, depthScaling = 0.3f),
    ItemCommon("Przedmiot (zwykly)", "Przedmiot", "🗡", weight = 17f, depthScaling = 0f),
    ItemRare("Przedmiot (rzadki)", "Rzadki", "⚔", weight = 8f, depthScaling = 0.6f),
    HeroicTicket("LOS NA HEROICZNEGO BOHATERA", "3 losy", "👑", weight = 2f, depthScaling = 1f),
    ItemLegendary("LEGENDARNY PRZEDMIOT", "Legenda", "✨", weight = 1f, depthScaling = 1f);

    val isJackpot: Boolean get() = this == HeroicTicket || this == ItemLegendary

    /** Z jakiej puli rzadkosci losuje sie konkretny przedmiot dla tego pola (null = to nie przedmiot). */
    val itemRarities: List<Rarity>?
        get() = when (this) {
            ItemCommon -> listOf(Rarity.Common, Rarity.Uncommon)
            ItemRare -> listOf(Rarity.Rare, Rarity.Epic)
            ItemLegendary -> listOf(Rarity.Legendary)
            else -> null
        }

    val goldAmount: Int
        get() = when (this) {
            GoldSmall -> 60
            GoldMedium -> 150
            GoldLarge -> 400
            else -> 0
        }

    val ticketCount: Int
        get() = when (this) {
            SummonTicket -> 1
            // Trzy losy = dokladnie tyle, ile kosztuje Heroiczny Summon (patrz SummonCenterScreen).
            HeroicTicket -> 3
            else -> 0
        }
}

/**
 * Losowanie nagrody ze skrzyni po bossie. Im glebszy poziom story mode, tym wyzsze wagi najlepszych
 * pol - ale rzad wielkosci sie nie zmienia: legendarny przedmiot na 10. poziomie to wciaz okolo
 * 2% szansy, a nie polowa kola. Chodzi o to, zeby jackpot pozostal jackpotem.
 */
object ChestWheel {

    /** Ile procent wagi dokłada kazdy poziom lochu do pol z [ChestSlot.depthScaling] = 1. */
    private const val DEPTH_BONUS_PER_LEVEL = 0.12f

    fun weightFor(slot: ChestSlot, dungeonLevel: Int): Float {
        val depth = (dungeonLevel - 1).coerceAtLeast(0)
        return slot.weight * (1f + depth * DEPTH_BONUS_PER_LEVEL * slot.depthScaling)
    }

    /** Szansa pola w procentach - pokazywana na karcie nagrody, zeby stol byl w pelni jawny. */
    fun chanceOf(slot: ChestSlot, dungeonLevel: Int): Float {
        val total = ChestSlot.entries.sumOf { weightFor(it, dungeonLevel).toDouble() }.toFloat()
        return weightFor(slot, dungeonLevel) / total
    }

    fun roll(random: Random, dungeonLevel: Int): ChestSlot {
        val total = ChestSlot.entries.sumOf { weightFor(it, dungeonLevel).toDouble() }.toFloat()
        var value = random.nextFloat() * total
        for (slot in ChestSlot.entries) {
            value -= weightFor(slot, dungeonLevel)
            if (value <= 0f) return slot
        }
        return ChestSlot.GoldSmall
    }
}
