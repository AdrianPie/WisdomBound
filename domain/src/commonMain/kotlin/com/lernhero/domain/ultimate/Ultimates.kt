package com.lernhero.domain.ultimate

import com.lernhero.domain.common.Rarity
import com.lernhero.domain.item.Item
import com.lernhero.domain.item.ItemSlot
import kotlin.math.roundToInt

/**
 * Co robi ulti. Kazdy rodzaj to inna odpowiedz na pytanie "czego mi teraz brakuje" - obrazen na
 * cala fale, jednego ciosu w bossa, zycia albo oslony.
 */
enum class UltimateKind(val label: String, val icon: String) {
    /** Obrazenia w KAZDEGO zywego wroga - [UltimateSpec.power] x atak druzyny. */
    Nova("Nova", "💥"),

    /** Jeden potezny cios w wroga z najwieksza liczba HP - bossobojca. */
    Smite("Smite", "☄️"),

    /** Leczy kazdego zywego sojusznika o [UltimateSpec.power] jego max HP. */
    Mend("Mend", "💚"),

    /** Kazdy sojusznik blokuje kolejne [UltimateSpec.wardCharges] ciosy + [UltimateSpec.heal] leczenia. */
    Ward("Ward", "🛡️"),

    /** Obrazenia we wszystkich wrogow i jednoczesnie leczenie druzyny o [UltimateSpec.heal] max HP. */
    Drain("Drain", "🩸")
}

/**
 * Jedna kula mocy - ulti bohatera.
 *
 * [streak] to ile DOBRYCH ODPOWIEDZI Z RZEDU trzeba, zeby ja naladowac. Bledna odpowiedz zeruje
 * ladowanie, a po wystrzale licznik startuje od zera - wiec krotka kula strzela czesto i slabo,
 * dluga rzadko i mocno. Patrz [Ultimates] po uzasadnienie liczb.
 *
 * [colorHex] jest liczba, nie kolorem Compose - domena nie zna UI (tak samo jak [com.lernhero.domain.monster.Element]).
 */
data class UltimateSpec(
    /** Id przedmiotu w katalogu, albo [Ultimates.BASIC_ID] dla wbudowanego ulti bez kuli. */
    val id: String,
    val name: String,
    val kind: UltimateKind,
    val streak: Int,
    /** Obrazenia: ulamek ataku druzyny. Leczenie ([UltimateKind.Mend]): ulamek max HP. */
    val power: Float,
    /** Leczenie przy [UltimateKind.Ward] i [UltimateKind.Drain] (ulamek max HP). */
    val heal: Float = 0f,
    /** Ile ciosow blokuje kazdy sojusznik po [UltimateKind.Ward]. */
    val wardCharges: Int = 0,
    /** [UltimateKind.Mend]: podnosi tez polegych sojusznikow z [heal] ich max HP. */
    val revive: Boolean = false,
    val colorHex: Long
) {
    /** Krotki opis efektu - ten sam na karcie przedmiotu, w liscie wyboru i na ekranie walki. */
    val effectText: String
        get() = when (kind) {
            UltimateKind.Nova -> "Deals ${pct(power)} of party attack to every enemy."
            UltimateKind.Smite -> "Hits the toughest enemy for ${pct(power)} of party attack."
            UltimateKind.Mend -> buildString {
                append("Heals the whole party for ${pct(power)} of max HP.")
                if (revive) append(" Revives fallen allies with ${pct(heal)} HP.")
            }
            UltimateKind.Ward -> "Every ally blocks the next " +
                (if (wardCharges == 1) "hit" else "$wardCharges hits") +
                " and heals ${pct(heal)} of max HP."
            UltimateKind.Drain -> "Deals ${pct(power)} of party attack to every enemy and heals the party for ${pct(heal)} of max HP."
        }

    /** "5 correct in a row" - jak to napisac na slocie i w opisie. */
    val chargeText: String get() = "$streak correct answers in a row"

    val isHealing: Boolean get() = kind == UltimateKind.Mend || kind == UltimateKind.Ward

    private fun pct(value: Float) = "${(value * 100).roundToInt()}%"
}

/**
 * Kule mocy - ulti jako PRZEDMIOTY. Wypadaja jak kazdy inny sprzet (skrzynie, bossowie, lup z
 * planszy), bo siedza w [com.lernhero.domain.item.ItemCatalog] w slocie [ItemSlot.Ultimate].
 * Nie ulepsza sie ich w kuzni - sila kuli to jej rzadkosc, a wybor to kompromis miedzy dlugoscia
 * serii a moca.
 *
 * ## Balans
 *
 * Kula na N z rzedu musi byc WARTA swojej serii. Oczekiwana liczba pytan do serii N przy
 * skutecznosci p to (1 - p^N) / ((1 - p) * p^N):
 *
 * | N | p = 0.70 | p = 0.85 |
 * |---|----------|----------|
 * | 3 |  6.4     |  4.2     |
 * | 5 | 16.5     |  8.4     |
 * | 7 | 37.4     | 14.1     |
 *
 * Moc rosnie z N szybciej niz liniowo ([NOVA_BY_STREAK]: 0.45 / 0.75 / 1.0 / 1.4 / 1.9), wiec:
 *  - przy ~70% trafien krotka kula daje WIECEJ obrazen na pytanie (0.07 vs 0.05 dla N = 7),
 *  - przy ~85% trafien oplaca sie dluga (0.107 vs 0.135).
 *
 * Czyli kula to prawdziwy wybor zalezny od tego, jak dobrze gracz zna temat - a nie "zawsze bierz
 * najdluzsza". Rzadkosc dokłada do tej samej krzywej okolo 10% na stopien.
 *
 * Wczesniej ulti bylo jedno: 5 z rzedu = 0.9 x atak druzyny w kazdego wroga. [BASIC] zostaje
 * dla bohatera bez kuli, troche slabsze, zeby pierwsza znaleziona kula byla odczuwalnym awansem.
 */
object Ultimates {

    const val BASIC_ID = "ulti_basic"

    /** Obrazenia Novy (x atak druzyny) dla serii N - rdzen balansu, patrz KDoc obiektu. */
    private val NOVA_BY_STREAK = mapOf(3 to 0.45f, 4 to 0.75f, 5 to 1.0f, 6 to 1.4f, 7 to 1.9f)

    /** Leczenie Mend (ulamek max HP) dla serii N. */
    private val MEND_BY_STREAK = mapOf(3 to 0.15f, 4 to 0.24f, 5 to 0.33f, 6 to 0.45f, 7 to 0.6f)

    /** Smite bije jeden cel - okolo 2.6x tego, co Nova daje KAZDEMU wrogowi przy tej samej serii. */
    private const val SMITE_FACTOR = 2.6f

    /** Drain dzieli moc: 75% Novy + leczenie. */
    private const val DRAIN_DAMAGE_SHARE = 0.75f

    internal fun rarityBonus(rarity: Rarity): Float = 1f + rarity.ordinal * 0.1f

    private fun nova(streak: Int, rarity: Rarity) = NOVA_BY_STREAK.getValue(streak) * rarityBonus(rarity)
    private fun mend(streak: Int, rarity: Rarity) = MEND_BY_STREAK.getValue(streak) * rarityBonus(rarity)

    /** Wbudowane ulti bohatera bez kuli: 5 z rzedu, obrazenia w cala fale. */
    val BASIC = UltimateSpec(
        id = BASIC_ID,
        name = "Hero's Wrath",
        kind = UltimateKind.Nova,
        streak = 5,
        power = 0.8f,
        colorHex = 0xFFFFC53D
    )

    private class Orb(
        val id: String,
        val name: String,
        val rarity: Rarity,
        val flavor: String,
        val spec: UltimateSpec
    )

    private fun orb(
        id: String,
        name: String,
        rarity: Rarity,
        kind: UltimateKind,
        streak: Int,
        colorHex: Long,
        flavor: String,
        heal: Float = 0f,
        wardCharges: Int = 0,
        revive: Boolean = false
    ): Orb {
        val power = when (kind) {
            UltimateKind.Nova -> nova(streak, rarity)
            UltimateKind.Smite -> nova(streak, rarity) * SMITE_FACTOR
            UltimateKind.Drain -> nova(streak, rarity) * DRAIN_DAMAGE_SHARE
            UltimateKind.Mend -> mend(streak, rarity)
            UltimateKind.Ward -> 0f
        }
        return Orb(
            id = id,
            name = name,
            rarity = rarity,
            flavor = flavor,
            spec = UltimateSpec(
                id = id,
                name = name,
                kind = kind,
                streak = streak,
                power = power,
                heal = heal,
                wardCharges = wardCharges,
                revive = revive,
                colorHex = colorHex
            )
        )
    }

    private val ORBS: List<Orb> = listOf(
        // ------------------------------- Pospolite: 3 z rzedu -------------------------------
        orb("orb_ember", "Ember Orb", Rarity.Common, UltimateKind.Nova, 3, 0xFFFF7A2F,
            "A coal that never went out. Small, but it bursts often."),
        orb("orb_dew", "Dewdrop Orb", Rarity.Common, UltimateKind.Mend, 3, 0xFF5FE0B0,
            "Morning dew caught in glass. Keeps the party on its feet."),

        // ------------------------------- Niepospolite: 3-4 ----------------------------------
        orb("orb_spark", "Spark Orb", Rarity.Uncommon, UltimateKind.Smite, 3, 0xFFFFE45C,
            "One quick bolt, straight at the biggest threat."),
        orb("orb_storm", "Storm Orb", Rarity.Uncommon, UltimateKind.Nova, 4, 0xFF4FA8FF,
            "A thundercloud the size of a fist."),

        // ---------------------------------- Rzadkie: 4-5 ------------------------------------
        orb("orb_aegis", "Aegis Orb", Rarity.Rare, UltimateKind.Ward, 4, 0xFF7FD4FF,
            "Folds a shield of light around every ally.", heal = 0.1f, wardCharges = 1),
        orb("orb_grove", "Grove Orb", Rarity.Rare, UltimateKind.Mend, 4, 0xFF6FD04A,
            "Smells of moss and rain. Wounds close as it glows."),
        orb("orb_blood", "Blood Orb", Rarity.Rare, UltimateKind.Drain, 5, 0xFFE0344A,
            "Takes from the enemy, gives to your party.", heal = 0.12f),

        // ---------------------------------- Epickie: 5-6 ------------------------------------
        orb("orb_meteor", "Meteor Orb", Rarity.Epic, UltimateKind.Smite, 5, 0xFFFF5A1F,
            "Calls a falling star onto a single foe. Bosses hate it."),
        orb("orb_void", "Void Orb", Rarity.Epic, UltimateKind.Nova, 6, 0xFF9B5CFF,
            "Swallows the light around the whole enemy line."),
        orb("orb_bastion", "Bastion Orb", Rarity.Epic, UltimateKind.Ward, 6, 0xFFB8E8FF,
            "An unbreakable wall, for a while.", heal = 0.2f, wardCharges = 2),

        // -------------------------------- Legendarne: 6-7 -----------------------------------
        orb("orb_phoenix", "Phoenix Orb", Rarity.Legendary, UltimateKind.Mend, 6, 0xFFFFA726,
            "Nothing stays dead near this flame.", heal = 0.35f, revive = true),
        orb("orb_starfall", "Starfall Orb", Rarity.Legendary, UltimateKind.Nova, 7, 0xFFFFF3A0,
            "Seven perfect answers, and the sky comes down.")
    )

    private val SPECS: Map<String, UltimateSpec> = ORBS.associate { it.id to it.spec }

    /** Kule jako zwykle przedmioty - rejestrowane w [com.lernhero.domain.item.ItemCatalog]. */
    val items: List<Item> = ORBS.map { orb ->
        Item(
            id = orb.id,
            name = orb.name,
            description = orb.flavor,
            slot = ItemSlot.Ultimate,
            rarity = orb.rarity,
            iconKey = orb.id,
            cost = 120 + orb.rarity.ordinal * 110
        )
    }

    /** Opis ulti dla kuli o tym id, albo null, gdy to nie kula. */
    fun specFor(itemId: String): UltimateSpec? = SPECS[itemId]

    /** Ulti bohatera z zalozonej kuli - albo [BASIC], gdy slot jest pusty. */
    fun forEquipped(item: Item?): UltimateSpec = item?.let { specFor(it.id) } ?: BASIC

    /** Wszystkie kule z opisem - do testow balansu. */
    val allSpecs: List<UltimateSpec> get() = SPECS.values.toList()
}
