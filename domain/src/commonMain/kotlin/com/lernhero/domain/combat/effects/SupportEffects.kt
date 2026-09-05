package com.lernhero.domain.combat.effects

import com.lernhero.domain.combat.CombatAction
import com.lernhero.domain.combat.CombatEvent
import com.lernhero.domain.combat.EffectCondition
import com.lernhero.domain.combat.EffectContext
import com.lernhero.domain.combat.EveryNTurns
import com.lernhero.domain.combat.OwnerIsActor
import com.lernhero.domain.combat.StatType
import com.lernhero.domain.combat.StatusKind
import com.lernhero.domain.combat.Trigger
import com.lernhero.domain.combat.TriggeredEffect

/**
 * Efekty wspierajace - leczenie, buffy, oczyszczanie. To one pokazuja, czym rozni sie efekt
 * "aktywny" od pasywnego: aktywny ma trigger (wlasna tura) i cooldown ([EveryNTurns]).
 */

/**
 * Kostur leczy [targets] najbardziej poobijanych sojusznikow co [everyTurns] tur.
 *
 * Cooldown skraca sie automatycznie o [StatType.CooldownReduction] noszacego, wiec przedmiot
 * "skracajacy cooldowny" dziala i na to zaklecie, choc nic o nim nie wie.
 */
class HealAlliesEffect(
    private val targets: Int,
    private val amount: Float,
    private val everyTurns: Int = 2
) : TriggeredEffect {
    override val id = "heal_allies_${targets}_${amount.toInt()}"
    override val description =
        "Every $everyTurns turns heals $targets allies for ${amount.toInt()} health."
    override val trigger = Trigger.TurnStart
    override val conditions: List<EffectCondition> = listOf(OwnerIsActor, EveryNTurns(everyTurns))

    override fun react(event: CombatEvent, context: EffectContext): List<CombatAction> {
        val wounded = context.woundedAllies(targets).filter { it.hp < it.maxHp }
        if (wounded.isEmpty()) return emptyList()

        return listOf(
            CombatAction.Heal(
                sourceId = context.ownerId,
                targetIds = wounded.map { it.id },
                amount = amount
            )
        )
    }
}

/** Okrzyk bojowy: na starcie walki wzmacnia cala druzyne na kilka tur. */
class BattleCryEffect(
    private val stat: StatType,
    private val potency: Float,
    private val turns: Int
) : TriggeredEffect {
    override val id = "battle_cry_${stat.name.lowercase()}_${potency.toInt()}"
    override val description = "At the start of battle buffs the team: +${potency.toInt()} ${stat.name}."
    override val trigger = Trigger.BattleStart

    override fun react(event: CombatEvent, context: EffectContext): List<CombatAction> = listOf(
        CombatAction.ApplyStatus(
            sourceId = context.ownerId,
            targetIds = context.allies().map { it.id },
            kind = StatusKind.Buff,
            potency = potency,
            turns = turns,
            stat = stat
        )
    )
}

/** Osłabia wrogow na starcie walki - to samo co [BattleCryEffect], tylko w druga strone. */
class WarHornEffect(
    private val stat: StatType,
    private val potency: Float,
    private val turns: Int
) : TriggeredEffect {
    override val id = "war_horn_${stat.name.lowercase()}_${potency.toInt()}"
    override val description = "At the start of battle weakens enemies: -${potency.toInt()} ${stat.name}."
    override val trigger = Trigger.BattleStart

    override fun react(event: CombatEvent, context: EffectContext): List<CombatAction> = listOf(
        CombatAction.ApplyStatus(
            sourceId = context.ownerId,
            targetIds = context.enemies().map { it.id },
            kind = StatusKind.Debuff,
            potency = potency,
            turns = turns,
            stat = stat
        )
    )
}

/** Zdejmuje trucizne i ogien z druzyny co kilka tur. */
class CleanseAlliesEffect(private val everyTurns: Int = 3) : TriggeredEffect {
    override val id = "cleanse_allies_$everyTurns"
    override val description = "Every $everyTurns turns clears poison and burn from the team."
    override val trigger = Trigger.TurnStart
    override val conditions: List<EffectCondition> = listOf(OwnerIsActor, EveryNTurns(everyTurns))

    override fun react(event: CombatEvent, context: EffectContext): List<CombatAction> {
        val poisoned = context.allies().filter { ally ->
            ally.statuses.any { it.kind == StatusKind.Poison || it.kind == StatusKind.Burn }
        }
        if (poisoned.isEmpty()) return emptyList()

        return listOf(
            CombatAction.Cleanse(
                targetIds = poisoned.map { it.id },
                kinds = setOf(StatusKind.Poison, StatusKind.Burn)
            )
        )
    }
}
