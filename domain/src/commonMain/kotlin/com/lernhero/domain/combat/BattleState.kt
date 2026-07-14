package com.lernhero.domain.combat

/** Klucz licznika uzyc: konkretny efekt u konkretnego wlasciciela. */
data class UsageKey(val ownerId: CombatantId, val effectId: String)

/**
 * Ile razy efekt juz zadzialal i kiedy ostatnio. Trzymamy to w stanie bitwy, a nie w efekcie,
 * zeby efekty pozostaly bezstanowe - ten sam obiekt efektu moze nosic dziesieciu graczy naraz.
 */
data class EffectUsage(
    val timesUsed: Int = 0,
    val lastTurnUsed: Int = -1
) {
    companion object {
        val Unused = EffectUsage()
    }
}

/**
 * Caly stan walki w jednym niezmiennym obiekcie. Silnik przepuszcza go przez kolejne kroki
 * i zwraca nowa wersje - zaden efekt nie moze go po cichu zepsuc.
 */
data class BattleState(
    val turn: Int = 1,
    val combatants: Map<CombatantId, Combatant>,
    val usage: Map<UsageKey, EffectUsage> = emptyMap(),
    val log: List<CombatEvent> = emptyList()
) {

    fun combatant(id: CombatantId): Combatant? = combatants[id]

    fun aliveOn(team: Team): List<Combatant> =
        combatants.values.filter { it.team == team && it.isAlive }.sortedBy { it.id }

    val isOver: Boolean
        get() = aliveOn(Team.Players).isEmpty() || aliveOn(Team.Enemies).isEmpty()

    val winner: Team?
        get() = when {
            aliveOn(Team.Enemies).isEmpty() && aliveOn(Team.Players).isNotEmpty() -> Team.Players
            aliveOn(Team.Players).isEmpty() && aliveOn(Team.Enemies).isNotEmpty() -> Team.Enemies
            else -> null
        }

    /**
     * Wartosc statystyki po zsumowaniu wszystkich zrodel: baza, przedmioty, czasowe buffy
     * i aury innych walczacych.
     *
     * Aury licza sie tutaj, a nie w [Combatant], bo zaleza od calego pola bitwy - noszacy
     * nie ma jak sam wiedziec, ze stoi obok sojusznika ze sztandarem.
     */
    fun stat(id: CombatantId, stat: StatType): Float {
        val target = combatants[id] ?: return 0f

        var value = target.baseStats[stat] + target.itemStats[stat]

        // Efekty pasywne sa pelnoprawnym zrodlem statystyk - bez tego "miecz na dwoch wrogow"
        // i kradziez zycia byly tylko opisem w karcie przedmiotu.
        target.effectsOf<StatEffect>().forEach { effect -> value += effect.bonus[stat] }

        target.statuses.forEach { status ->
            if (status.stat != stat) return@forEach
            val delta = status.potency * status.stacks
            value += if (status.kind == StatusKind.Debuff) -delta else delta
        }

        combatants.values.forEach { other ->
            if (!other.isAlive || other.id == id) return@forEach
            other.effectsOf<AuraEffect>().forEach { aura ->
                val hits = when (aura.scope) {
                    AuraScope.Allies -> other.team == target.team
                    AuraScope.Enemies -> other.team != target.team
                }
                if (hits) value += aura.bonus[stat]
            }
        }

        return value
    }

    fun usageOf(ownerId: CombatantId, effectId: String): EffectUsage =
        usage[UsageKey(ownerId, effectId)] ?: EffectUsage.Unused

    fun withCombatant(combatant: Combatant): BattleState =
        copy(combatants = combatants + (combatant.id to combatant))

    fun withEvent(event: CombatEvent): BattleState = copy(log = log + event)

    fun markUsed(ownerId: CombatantId, effectId: String): BattleState {
        val key = UsageKey(ownerId, effectId)
        val current = usage[key] ?: EffectUsage.Unused
        return copy(
            usage = usage + (key to current.copy(
                timesUsed = current.timesUsed + 1,
                lastTurnUsed = turn
            ))
        )
    }
}
