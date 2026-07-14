package com.lernhero.domain.combat.engine

import com.lernhero.domain.combat.CombatAction
import com.lernhero.domain.combat.CombatEvent
import com.lernhero.domain.combat.Combatant
import com.lernhero.domain.combat.BattleState
import com.lernhero.domain.combat.DamageModifierEffect
import com.lernhero.domain.combat.DamagePacket
import com.lernhero.domain.combat.DamagePhase
import com.lernhero.domain.combat.DamageTag
import com.lernhero.domain.combat.EffectContext
import com.lernhero.domain.combat.StatType
import kotlin.math.roundToInt
import kotlin.random.Random

/**
 * Wynik rozliczenia jednego ciosu: nowy stan, zdarzenia do rozeslania i akcje wtorne
 * (odbicie, kradziez zycia), ktore silnik wykona osobno.
 */
data class DamageOutcome(
    val state: BattleState,
    val events: List<CombatEvent>,
    val followUps: List<CombatAction>
)

/**
 * Potok obrazen. Kolejnosc krokow jest sercem balansu, wiec siedzi w jednym miejscu:
 *
 *   krytyk -> modyfikatory wychodzace -> modyfikatory przychodzace -> obrona -> tarcza -> HP
 *
 * Unik i blok sa modyfikatorami przychodzacymi, nie osobnymi galeziami if-a. Dzieki temu
 * przedmiot moze wejsc w dowolny punkt tego potoku, a silnik nie wie, jaki to przedmiot.
 */
internal class DamageResolver(private val random: Random) {

    fun resolve(state: BattleState, packet: DamagePacket): DamageOutcome {
        val source = state.combatant(packet.sourceId)
        val target = state.combatant(packet.targetId)
        if (target == null || !target.isAlive) {
            return DamageOutcome(state, emptyList(), emptyList())
        }

        var working = rollCritical(state, packet)
        var currentState = state
        val followUps = mutableListOf<CombatAction>()

        if (source != null) {
            val outgoing = applyModifiers(currentState, source, DamagePhase.Outgoing, working)
            currentState = outgoing.state
            working = outgoing.packet
            followUps += outgoing.actions
        }

        // Unik rozstrzyga sie przed modyfikatorami przychodzacymi. Inaczej pancerz "blokujacy
        // pierwszy cios" zuzylby sie na ciosie, ktory i tak przelecial obok.
        working = rollDodge(currentState, working)
        if (working.dodged) {
            return DamageOutcome(
                state = currentState,
                events = listOf(CombatEvent.Dodged(targetId = target.id, sourceId = packet.sourceId)),
                followUps = followUps
            )
        }

        val incoming = applyModifiers(currentState, target, DamagePhase.Incoming, working)
        currentState = incoming.state
        working = incoming.packet
        followUps += incoming.actions

        if (working.dodged) {
            return DamageOutcome(
                state = currentState,
                events = listOf(CombatEvent.Dodged(targetId = target.id, sourceId = packet.sourceId)),
                followUps = followUps
            )
        }

        val finalAmount = mitigate(currentState, working)
        if (finalAmount <= 0) {
            return DamageOutcome(currentState, emptyList(), followUps)
        }

        val liveTarget = currentState.combatant(target.id) ?: target
        val absorbed = minOf(liveTarget.shield, finalAmount)
        val toHp = finalAmount - absorbed
        val hpAfter = (liveTarget.hp - toHp).coerceAtLeast(0)

        val updated = liveTarget.copy(shield = liveTarget.shield - absorbed, hp = hpAfter)
        currentState = currentState.withCombatant(updated)

        val killed = hpAfter <= 0
        val events = buildList {
            add(
                CombatEvent.DamageDealt(
                    sourceId = packet.sourceId,
                    targetId = target.id,
                    amount = finalAmount,
                    targetHpAfter = hpAfter,
                    isCritical = working.isCritical,
                    killed = killed,
                    tag = working.tag
                )
            )
            add(
                CombatEvent.DamageTaken(
                    targetId = target.id,
                    sourceId = packet.sourceId,
                    amount = finalAmount,
                    isCritical = working.isCritical,
                    tag = working.tag
                )
            )
            if (working.isCritical) {
                add(CombatEvent.CriticalHit(packet.sourceId, target.id, finalAmount))
            }
            if (killed) {
                add(CombatEvent.Killed(killerId = packet.sourceId, victimId = target.id))
                add(CombatEvent.Died(victimId = target.id))
            }
        }

        // Kradziez zycia jest statystyka, nie przedmiotem - kazde zrodlo lifestealu sumuje sie samo.
        val lifesteal = currentState.stat(packet.sourceId, StatType.Lifesteal)
        if (source != null && lifesteal > 0f && toHp > 0) {
            followUps += CombatAction.Heal(
                sourceId = source.id,
                targetIds = listOf(source.id),
                amount = toHp * lifesteal
            )
        }

        return DamageOutcome(currentState, events, followUps)
    }

    /** Uniki dotycza tylko ciosow - od trucizny i ognia nie da sie uchylic. */
    private fun rollDodge(state: BattleState, packet: DamagePacket): DamagePacket {
        if (packet.tag != DamageTag.Attack && packet.tag != DamageTag.Skill) return packet
        val chance = state.stat(packet.targetId, StatType.Dodge)
        if (chance <= 0f || random.nextFloat() >= chance) return packet
        return packet.copy(dodged = true, amount = 0f)
    }

    private fun rollCritical(state: BattleState, packet: DamagePacket): DamagePacket {
        if (!packet.canCrit || packet.isCritical) return packet
        val chance = state.stat(packet.sourceId, StatType.CritChance)
        if (chance <= 0f || random.nextFloat() >= chance) return packet

        val multiplier = state.stat(packet.sourceId, StatType.CritDamage).coerceAtLeast(1f)
        return packet.copy(amount = packet.amount * multiplier, isCritical = true)
    }

    private data class ModifierPass(
        val state: BattleState,
        val packet: DamagePacket,
        val actions: List<CombatAction>
    )

    /**
     * Przepuszcza pakiet przez modyfikatory jednego walczacego. Warunki dostaja probne zdarzenie
     * [CombatEvent.DamageTaken], bo prawdziwe jeszcze nie istnieje - cios wciaz jest w powietrzu.
     */
    private fun applyModifiers(
        state: BattleState,
        owner: Combatant,
        phase: DamagePhase,
        packet: DamagePacket
    ): ModifierPass {
        val modifiers = owner.effectsOf<DamageModifierEffect>()
            .filter { it.phase == phase }
            .sortedBy { it.priority }
        if (modifiers.isEmpty()) return ModifierPass(state, packet, emptyList())

        var currentState = state
        var working = packet
        val actions = mutableListOf<CombatAction>()

        modifiers.forEach { modifier ->
            val probe = CombatEvent.DamageTaken(
                targetId = working.targetId,
                sourceId = working.sourceId,
                amount = working.amount.roundToInt(),
                isCritical = working.isCritical,
                tag = working.tag
            )
            val context = EffectContext(
                state = currentState,
                ownerId = owner.id,
                random = random,
                usage = currentState.usageOf(owner.id, modifier.id)
            )
            if (!modifier.conditions.all { it.isMet(probe, context) }) return@forEach

            val result = modifier.modify(working, context)
            working = result.packet
            actions += result.actions
            if (result.consumed) {
                currentState = currentState.markUsed(owner.id, modifier.id)
            }
        }

        return ModifierPass(currentState, working, actions)
    }

    /** Obrona i redukcja procentowa. Cios, ktory doszedl, zawsze zabiera minimum 1 HP. */
    private fun mitigate(state: BattleState, packet: DamagePacket): Int {
        var amount = packet.amount
        if (amount <= 0f) return 0

        if (!packet.ignoresDefense) {
            amount -= state.stat(packet.targetId, StatType.Defense)
        }
        val reduction = state.stat(packet.targetId, StatType.DamageReduction).coerceIn(0f, 0.9f)
        amount *= (1f - reduction)

        return amount.roundToInt().coerceAtLeast(1)
    }
}
