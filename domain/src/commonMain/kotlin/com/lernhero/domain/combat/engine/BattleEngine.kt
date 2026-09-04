package com.lernhero.domain.combat.engine

import com.lernhero.domain.combat.ActiveStatus
import com.lernhero.domain.combat.BattleState
import com.lernhero.domain.combat.CombatAction
import com.lernhero.domain.combat.CombatEvent
import com.lernhero.domain.combat.Combatant
import com.lernhero.domain.combat.CombatantId
import com.lernhero.domain.combat.DamagePacket
import com.lernhero.domain.combat.DamageTag
import com.lernhero.domain.combat.EffectContext
import com.lernhero.domain.combat.SpellChoice
import com.lernhero.domain.combat.StatType
import com.lernhero.domain.combat.StatusKind
import com.lernhero.domain.combat.Team
import com.lernhero.domain.combat.TriggeredEffect
import kotlin.math.roundToInt
import kotlin.random.Random

data class BattleResult(
    val events: List<CombatEvent>,
    val winner: Team?,
    val finalState: BattleState
)

/**
 * Symuluje cala walke z gory i zwraca strumien zdarzen. UI tylko odtwarza ten strumien -
 * tak samo, jak dzialalo to wczesniej, tyle ze teraz kazdy krok przechodzi przez efekty.
 *
 * Silnik nie wie nic o przedmiotach. Zna wylacznie efekty, zdarzenia i akcje, wiec setny miecz
 * doda sie bez jednej linijki zmian tutaj.
 *
 * [random] jest zasiany z zewnatrz: ta sama walka z tym samym seedem zawsze konczy sie tak samo,
 * co jest warunkiem odtwarzania powtorek i weryfikacji walk po stronie serwera.
 */
class BattleEngine(
    private val random: Random = Random.Default,
    private val maxTurns: Int = 40,
    /**
     * Ile razy efekt moze wywolac kolejny efekt. Bez tego dwie tarcze odbijajace obrazenia
     * albo "dodatkowy atak po zabiciu" potrafia zapetlic symulacje.
     */
    private val maxChainDepth: Int = 3
) {
    private val resolver = DamageResolver(random)

    fun simulate(combatants: List<Combatant>): BattleResult {
        var state = BattleState(combatants = combatants.associateBy { it.id })
        state = dispatch(state, CombatEvent.BattleStarted, depth = 0)

        while (!state.isOver && state.turn <= maxTurns) {
            for (actorId in initiativeOrder(state)) {
                if (state.isOver) break
                val actor = state.combatant(actorId) ?: continue
                if (!actor.isAlive) continue
                if (actor.isFrozen) continue

                state = dispatch(state, CombatEvent.TurnStarted(state.turn, actorId), depth = 0)
                state = takeTurn(state, actorId, depth = 0)
                state = dispatch(state, CombatEvent.TurnEnded(state.turn, actorId), depth = 0)
            }

            state = tickStatuses(state)
            state = state.copy(turn = state.turn + 1)
        }

        return BattleResult(events = state.log, winner = state.winner, finalState = state)
    }

    /** Szybsi bija pierwsi; przy remisie decyduje id, zeby symulacja byla powtarzalna. */
    private fun initiativeOrder(state: BattleState): List<CombatantId> =
        state.combatants.values
            .filter { it.isAlive }
            .sortedWith(
                compareByDescending<Combatant> { state.stat(it.id, StatType.Speed) }
                    .thenBy { it.id }
            )
            .map { it.id }

    /**
     * Akcja tury z paska zdolnosci. Losuje 0..99: kolejne zaklecia zjadaja swoje procenty, a jesli
     * los nie trafi w zadne, pada zwykly atak. Suma szans zaklec to procent tur bez zwyklego ciosu.
     *
     * Kolejnosc [Combatant.spells] jest ustalona (malejaco), wiec przy tym samym seedzie wynik
     * jest powtarzalny - tak samo jak reszta symulacji.
     */
    private fun takeTurn(state: BattleState, actorId: CombatantId, depth: Int): BattleState {
        val actor = state.combatant(actorId) ?: return state
        if (actor.spells.isNotEmpty()) {
            val roll = random.nextInt(100)
            var acc = 0
            for (choice in actor.spells) {
                acc += choice.chancePercent
                if (roll < acc) return castSpell(state, actorId, choice, depth)
            }
        }
        return performAttack(state, actorId, depth)
    }

    /** Rzuca zaklecie: buduje kontekst rzucajacego, zbiera intencje i wykonuje je jak kazde akcje. */
    private fun castSpell(
        state: BattleState,
        actorId: CombatantId,
        choice: SpellChoice,
        depth: Int
    ): BattleState {
        val owner = state.combatant(actorId) ?: return state
        if (!owner.isAlive) return state
        val context = EffectContext(
            state = state,
            ownerId = actorId,
            random = random,
            usage = state.usageOf(actorId, choice.spellId)
        )
        val actions = choice.cast.cast(context)
        if (actions.isEmpty()) {
            // Zaklecie nie mialo w kogo/kim celowac (np. brak rannych do leczenia) - runda nie przepada.
            return performAttack(state, actorId, depth)
        }
        return execute(state, actions, depth)
    }

    /**
     * Zwykly atak. Liczba celow to statystyka [StatType.AttackTargets], wiec "miecz bijacy dwoch"
     * i "miecz bijacy trzech" to ten sam kod - rozni je tylko liczba w przedmiocie.
     */
    private fun performAttack(
        state: BattleState,
        attackerId: CombatantId,
        depth: Int,
        forcedTargetId: CombatantId? = null
    ): BattleState {
        val attacker = state.combatant(attackerId) ?: return state
        if (!attacker.isAlive) return state

        val enemies = state.aliveOn(attacker.team.opposite)
        if (enemies.isEmpty()) return state

        val targetCount = state.stat(attackerId, StatType.AttackTargets).roundToInt().coerceAtLeast(1)
        val primary = forcedTargetId?.let { state.combatant(it) }?.takeIf { it.isAlive }
            ?: enemies[random.nextInt(enemies.size)]
        val targets = (listOf(primary) + enemies.filter { it.id != primary.id }).take(targetCount)

        val damage = state.stat(attackerId, StatType.Attack).coerceAtLeast(1f)

        var current = state
        targets.forEach { target ->
            if (current.combatant(target.id)?.isAlive != true) return@forEach
            current = strike(
                state = current,
                packet = DamagePacket(
                    sourceId = attackerId,
                    targetId = target.id,
                    amount = damage,
                    tag = DamageTag.Attack
                ),
                depth = depth
            )
        }
        return current
    }

    /** Jeden cios: potok obrazen, rozeslanie zdarzen, wykonanie akcji wtornych. */
    private fun strike(state: BattleState, packet: DamagePacket, depth: Int): BattleState {
        val outcome = resolver.resolve(state, packet)
        var current = outcome.state

        outcome.events.forEach { event ->
            current = current.withEvent(event)
            current = dispatch(current, event, depth)
        }
        return execute(current, outcome.followUps, depth)
    }

    /**
     * Rozsyla zdarzenie do wszystkich zywych i pyta ich efekty o reakcje.
     *
     * Efekt odpala sie tylko wtedy, gdy jego trigger pasuje i wszystkie warunki sa spelnione;
     * licznik uzyc rosnie dopiero, gdy efekt naprawde cos zwrocil. Inaczej "raz na walke"
     * zuzywaloby sie na zdarzeniu, ktore efekt i tak zignorowal.
     */
    private fun dispatch(state: BattleState, event: CombatEvent, depth: Int): BattleState {
        if (depth > maxChainDepth) return state

        var current = state
        val listeners = current.combatants.values
            .filter { it.isAlive }
            .sortedBy { it.id }

        listeners.forEach { listener ->
            listener.effectsOf<TriggeredEffect>()
                .filter { it.trigger == event.trigger }
                .forEach { effect ->
                    val owner = current.combatant(listener.id) ?: return@forEach
                    if (!owner.isAlive) return@forEach

                    val context = EffectContext(
                        state = current,
                        ownerId = owner.id,
                        random = random,
                        usage = current.usageOf(owner.id, effect.id)
                    )
                    if (!effect.conditions.all { it.isMet(event, context) }) return@forEach

                    val actions = effect.react(event, context)
                    if (actions.isEmpty()) return@forEach

                    current = current.markUsed(owner.id, effect.id)
                    current = execute(current, actions, depth + 1)
                }
        }
        return current
    }

    private fun execute(state: BattleState, actions: List<CombatAction>, depth: Int): BattleState {
        var current = state
        actions.forEach { action -> current = execute(current, action, depth) }
        return current
    }

    private fun execute(state: BattleState, action: CombatAction, depth: Int): BattleState = when (action) {
        is CombatAction.DealDamage -> {
            var current = state
            action.targetIds.forEach { targetId ->
                current = strike(
                    state = current,
                    packet = DamagePacket(
                        sourceId = action.sourceId,
                        targetId = targetId,
                        amount = action.amount,
                        tag = action.tag,
                        canCrit = action.canCrit,
                        ignoresDefense = action.tag == DamageTag.Poison
                    ),
                    depth = depth + 1
                )
            }
            current
        }

        is CombatAction.Heal -> {
            var current = state
            val power = 1f + current.stat(action.sourceId, StatType.HealPower)
            val amount = (action.amount * power).roundToInt().coerceAtLeast(1)

            action.targetIds.forEach { targetId ->
                val target = current.combatant(targetId) ?: return@forEach
                if (!target.isAlive) return@forEach

                val hpAfter = (target.hp + amount).coerceAtMost(target.maxHp)
                if (hpAfter == target.hp) return@forEach

                current = current.withCombatant(target.withHp(hpAfter))
                val event = CombatEvent.Healed(action.sourceId, targetId, hpAfter - target.hp, hpAfter)
                current = current.withEvent(event)
                current = dispatch(current, event, depth + 1)
            }
            current
        }

        is CombatAction.ApplyStatus -> {
            var current = state
            action.targetIds.forEach { targetId ->
                val target = current.combatant(targetId) ?: return@forEach
                if (!target.isAlive) return@forEach

                val existing = target.statuses.firstOrNull {
                    it.kind == action.kind && it.stat == action.stat && it.sourceId == action.sourceId
                }
                val updated = if (existing == null) {
                    target.statuses + ActiveStatus(
                        kind = action.kind,
                        sourceId = action.sourceId,
                        potency = action.potency,
                        turnsLeft = action.turns,
                        stat = action.stat
                    )
                } else {
                    // Ponowne nalozenie odswieza czas trwania i dokłada stos - klasyka DoT-ow.
                    target.statuses - existing + existing.copy(
                        turnsLeft = maxOf(existing.turnsLeft, action.turns),
                        stacks = existing.stacks + 1
                    )
                }

                current = current.withCombatant(target.copy(statuses = updated))
                val event = CombatEvent.StatusApplied(action.sourceId, targetId, action.kind, action.turns)
                current = current.withEvent(event)
                current = dispatch(current, event, depth + 1)
            }
            current
        }

        is CombatAction.GrantShield -> {
            var current = state
            val amount = action.amount.roundToInt().coerceAtLeast(0)
            action.targetIds.forEach { targetId ->
                val target = current.combatant(targetId) ?: return@forEach
                current = current.withCombatant(target.copy(shield = target.shield + amount))
            }
            current
        }

        is CombatAction.ExtraAttack ->
            if (depth >= maxChainDepth) state
            else performAttack(state, action.attackerId, depth + 1, action.targetId)

        is CombatAction.Cleanse -> {
            var current = state
            action.targetIds.forEach { targetId ->
                val target = current.combatant(targetId) ?: return@forEach
                current = current.withCombatant(
                    target.copy(statuses = target.statuses.filterNot { it.kind in action.kinds })
                )
            }
            current
        }
    }

    /**
     * Koniec rundy: stany tykaja. Ogien i trucizna zabieraja HP (trucizna przebija obrone),
     * wszystkim skraca sie czas trwania, wygasle znikaja.
     */
    private fun tickStatuses(state: BattleState): BattleState {
        var current = state

        current.combatants.values.filter { it.isAlive }.sortedBy { it.id }.forEach { combatant ->
            combatant.statuses.forEach { status ->
                val tag = when (status.kind) {
                    StatusKind.Burn -> DamageTag.Burn
                    StatusKind.Poison -> DamageTag.Poison
                    else -> null
                } ?: return@forEach

                current = strike(
                    state = current,
                    packet = DamagePacket(
                        sourceId = status.sourceId,
                        targetId = combatant.id,
                        amount = status.potency * status.stacks,
                        tag = tag,
                        canCrit = false,
                        ignoresDefense = status.kind == StatusKind.Poison
                    ),
                    depth = 0
                )
            }
        }

        current.combatants.values.forEach { combatant ->
            val ticked = combatant.statuses
                .map { it.copy(turnsLeft = it.turnsLeft - 1) }
                .filter { it.turnsLeft > 0 }
            if (ticked != combatant.statuses) {
                current = current.withCombatant(
                    current.combatant(combatant.id)?.copy(statuses = ticked) ?: return@forEach
                )
            }
        }

        return current
    }
}
