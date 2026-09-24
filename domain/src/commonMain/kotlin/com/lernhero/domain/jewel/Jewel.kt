package com.lernhero.domain.jewel

import com.lernhero.domain.combat.Effect
import com.lernhero.domain.combat.StatType
import com.lernhero.domain.combat.Stats
import com.lernhero.domain.combat.effects.PoisonOnHitEffect
import com.lernhero.domain.common.Rarity
import com.lernhero.domain.item.Item
import com.lernhero.domain.item.ItemInstance

/**
 * Kamien osadzany w przedmiocie, jak w Diablo. W kodzie "Jewel", bo "gems" to juz waluta premium.
 *
 * Kamienie nie dropia z walk - daja je osiagniecia w Swiatyni, glownie za serie odpowiedzi
 * w danym temacie. Dzieki temu nauka ma wlasny, widoczny wplyw na sile postaci.
 */
enum class JewelType(val label: String, val colorHex: Long, val bonusLabel: String) {
    Ruby("Ruby", 0xFFE0424F, "Attack"),
    Onyx("Onyx", 0xFF8E8AA6, "Health"),
    Sapphire("Sapphire", 0xFF3B7DFF, "Damage reduction"),
    Topaz("Topaz", 0xFFFFB020, "Critical chance"),
    Amethyst("Amethyst", 0xFFB061FF, "Lifesteal"),
    Emerald("Emerald", 0xFF3BC46B, "Poison")
}

/** Szlif. Trzy kamienie jednego szlifu skladaja sie w jeden kolejnego - patrz [Jewels.combine]. */
enum class JewelGrade(val label: String) {
    Chipped("Chipped"),
    Flawed("Flawed"),
    Polished("Polished"),
    Flawless("Flawless"),
    Perfect("Perfect");

    val level: Int get() = ordinal + 1
}

data class Jewel(val type: JewelType, val grade: JewelGrade) {

    val id: String get() = "${type.name.lowercase()}_${grade.level}"

    val name: String get() = "${grade.label} ${type.label}"

    val stats: Stats
        get() {
            val i = grade.ordinal
            return when (type) {
                JewelType.Ruby -> Stats.of(StatType.Attack to RUBY_ATTACK[i])
                JewelType.Onyx -> Stats.of(StatType.MaxHp to ONYX_HP[i])
                JewelType.Sapphire -> Stats.of(StatType.DamageReduction to SAPPHIRE_REDUCTION[i])
                JewelType.Topaz -> Stats.of(StatType.CritChance to TOPAZ_CRIT[i])
                JewelType.Amethyst -> Stats.of(StatType.Lifesteal to AMETHYST_LIFESTEAL[i])
                JewelType.Emerald -> Stats.Empty
            }
        }

    val effects: List<Effect>
        get() = when (type) {
            JewelType.Emerald -> listOf(
                PoisonOnHitEffect(potency = EMERALD_POISON[grade.ordinal], turns = 3, chance = EMERALD_CHANCE)
            )
            else -> emptyList()
        }

    /** Jedna linijka "co to daje" do list i przyciskow. */
    val bonusText: String
        get() {
            val i = grade.ordinal
            return when (type) {
                JewelType.Ruby -> "+${RUBY_ATTACK[i].toInt()} Attack"
                JewelType.Onyx -> "+${ONYX_HP[i].toInt()} Health"
                JewelType.Sapphire -> "-${percent(SAPPHIRE_REDUCTION[i])}% damage taken"
                JewelType.Topaz -> "+${percent(TOPAZ_CRIT[i])}% crit chance"
                JewelType.Amethyst -> "+${percent(AMETHYST_LIFESTEAL[i])}% lifesteal"
                JewelType.Emerald ->
                    "${percent(EMERALD_CHANCE)}% chance to poison (${EMERALD_POISON[i].toInt()} dmg)"
            }
        }

    private companion object {
        // Skala wzgledem sprzetu: bronie daja 4-14 ataku, zbroje 6-34 HP. Idealny kamien to mniej
        // wiecej polowa dobrego przedmiotu - odczuwalny, ale nie wazniejszy od samej rzeczy.
        val RUBY_ATTACK = floatArrayOf(1f, 2f, 4f, 6f, 9f)
        val ONYX_HP = floatArrayOf(5f, 10f, 16f, 24f, 34f)
        val SAPPHIRE_REDUCTION = floatArrayOf(0.01f, 0.02f, 0.03f, 0.045f, 0.06f)
        val TOPAZ_CRIT = floatArrayOf(0.02f, 0.03f, 0.05f, 0.07f, 0.10f)
        val AMETHYST_LIFESTEAL = floatArrayOf(0.02f, 0.03f, 0.05f, 0.07f, 0.09f)
        val EMERALD_POISON = floatArrayOf(2f, 4f, 6f, 9f, 13f)
        const val EMERALD_CHANCE = 0.35f

        fun percent(value: Float): String {
            val scaled = value * 100f
            return if (scaled % 1f == 0f) scaled.toInt().toString() else ((scaled * 10).toInt() / 10f).toString()
        }
    }
}

object Jewels {

    const val COMBINE_COUNT = 3

    val all: List<Jewel> = JewelType.entries.flatMap { type -> JewelGrade.entries.map { Jewel(type, it) } }

    private val byId: Map<String, Jewel> = all.associateBy { it.id }

    fun byId(id: String): Jewel? = byId[id]

    /** Kamien, ktory powstaje z [COMBINE_COUNT] sztuk [jewel]. Null dla szlifu Perfect. */
    fun upgradeOf(jewel: Jewel): Jewel? =
        JewelGrade.entries.getOrNull(jewel.grade.ordinal + 1)?.let { Jewel(jewel.type, it) }
}

/** Ile kamieni zmiesci przedmiot. Rosnie z rzadkoscia - to kolejny powod, zeby gonic lepszy sprzet. */
object Sockets {

    fun countFor(rarity: Rarity): Int = when (rarity) {
        Rarity.Common -> 0
        Rarity.Uncommon -> 1
        Rarity.Rare -> 1
        Rarity.Epic -> 2
        Rarity.Legendary -> 3
    }

    fun countFor(item: Item): Int = countFor(item.rarity)
}

/** Plecak przedmiotow i sakiewka kamieni po operacji na kamieniach. */
data class JewelState(
    val bag: List<ItemInstance>,
    val pouch: Map<String, Int>
)

/**
 * Operacje na kamieniach jako czyste funkcje: dostaja stan, oddaja nowy albo null, gdy ruch jest
 * niedozwolony. Repozytorium tylko je wywoluje i zapisuje wynik - reguly da sie przetestowac
 * bez Firestore.
 *
 * Wyjecie kamienia jest darmowe i kamien wraca do sakiewki. Na telefonie niszczenie kamienia przy
 * przekladaniu (jak w Diablo 2) bardziej by frustrowalo, niz dodawalo decyzji.
 */
object JewelBench {

    fun socket(state: JewelState, uid: String, jewelId: String, capacity: Int): JewelState? {
        if (Jewels.byId(jewelId) == null) return null
        val owned = state.pouch[jewelId] ?: 0
        if (owned <= 0) return null
        val instance = state.bag.firstOrNull { it.uid == uid } ?: return null
        if (instance.jewels.size >= capacity) return null

        return JewelState(
            bag = state.bag.map { if (it.uid == uid) it.copy(jewels = it.jewels + jewelId) else it },
            pouch = state.pouch.minusOne(jewelId)
        )
    }

    fun unsocket(state: JewelState, uid: String, index: Int): JewelState? {
        val instance = state.bag.firstOrNull { it.uid == uid } ?: return null
        val jewelId = instance.jewels.getOrNull(index) ?: return null

        return JewelState(
            bag = state.bag.map {
                if (it.uid == uid) it.copy(jewels = it.jewels.filterIndexed { i, _ -> i != index }) else it
            },
            pouch = state.pouch.plusOne(jewelId)
        )
    }

    /** [Jewels.COMBINE_COUNT] kamienie jednego szlifu -> jeden kamien szlifu wyzej. */
    fun combine(pouch: Map<String, Int>, jewelId: String): Map<String, Int>? {
        val jewel = Jewels.byId(jewelId) ?: return null
        val result = Jewels.upgradeOf(jewel) ?: return null
        val owned = pouch[jewelId] ?: 0
        if (owned < Jewels.COMBINE_COUNT) return null

        val left = owned - Jewels.COMBINE_COUNT
        val spent = if (left > 0) pouch + (jewelId to left) else pouch - jewelId
        return spent.plusOne(result.id)
    }

    fun grant(pouch: Map<String, Int>, jewelId: String): Map<String, Int> = pouch.plusOne(jewelId)

    private fun Map<String, Int>.plusOne(id: String): Map<String, Int> = this + (id to (this[id] ?: 0) + 1)

    private fun Map<String, Int>.minusOne(id: String): Map<String, Int> {
        val left = (this[id] ?: 0) - 1
        return if (left > 0) this + (id to left) else this - id
    }
}
