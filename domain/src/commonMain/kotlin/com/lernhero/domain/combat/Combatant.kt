package com.lernhero.domain.combat

typealias CombatantId = String

enum class Team {
    Players,
    Enemies;

    val opposite: Team get() = if (this == Players) Enemies else Players
}

/** Rodzaje stanow nakladanych na walczacego. Nowy rodzaj = nowa wartosc + obsluga w tickStatuses. */
enum class StatusKind {
    /** Obrazenia co ture, skaluja sie ze stosami. */
    Burn,

    /** Obrazenia co ture, ignoruja obrone. */
    Poison,

    /** Zamrozony nie wykonuje tury. */
    Freeze,

    /** Czasowy bonus do statystyki. */
    Buff,

    /** Czasowa kara do statystyki. */
    Debuff
}

/**
 * Stan aktywny na walczacym. Buffy i debuffy niosa [stat] + [potency]; trucizna i ogien
 * uzywaja [potency] jako obrazen na ture.
 */
data class ActiveStatus(
    val kind: StatusKind,
    val sourceId: CombatantId,
    val potency: Float,
    val turnsLeft: Int,
    val stacks: Int = 1,
    val stat: StatType? = null
)

/**
 * Walczacy w trakcie symulacji. Niezmienny - silnik produkuje nowe kopie, nigdy nie mutuje.
 *
 * [effects] to jedyne miejsce, z ktorego bierze sie zachowanie przedmiotow: ekwipunek jest
 * splaszczany do listy efektow zanim walka wystartuje. Silnik nie wie, ze przedmioty istnieja.
 */
data class Combatant(
    val id: CombatantId,
    val name: String,
    val team: Team,
    val baseStats: Stats,
    val hp: Int,
    val shield: Int = 0,
    val statuses: List<ActiveStatus> = emptyList(),
    val effects: List<Effect> = emptyList(),
    /** Statystyki z zalozonych przedmiotow, zsumowane raz przy starcie walki. */
    val itemStats: Stats = Stats.Empty
) {
    val isAlive: Boolean get() = hp > 0

    val maxHp: Int get() = (baseStats[StatType.MaxHp] + itemStats[StatType.MaxHp]).toInt().coerceAtLeast(1)

    val isFrozen: Boolean get() = statuses.any { it.kind == StatusKind.Freeze }

    fun withHp(value: Int): Combatant = copy(hp = value.coerceIn(0, maxHp))

    inline fun <reified T : Effect> effectsOf(): List<T> = effects.filterIsInstance<T>()
}
