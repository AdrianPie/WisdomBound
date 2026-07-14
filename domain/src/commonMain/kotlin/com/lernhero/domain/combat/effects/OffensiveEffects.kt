package com.lernhero.domain.combat.effects

import com.lernhero.domain.combat.Chance
import com.lernhero.domain.combat.CombatAction
import com.lernhero.domain.combat.CombatEvent
import com.lernhero.domain.combat.DamageTag
import com.lernhero.domain.combat.EffectCondition
import com.lernhero.domain.combat.EffectContext
import com.lernhero.domain.combat.ExcludeDamageTag
import com.lernhero.domain.combat.OnCriticalOnly
import com.lernhero.domain.combat.OwnerIsSource
import com.lernhero.domain.combat.StatusKind
import com.lernhero.domain.combat.Trigger
import com.lernhero.domain.combat.TriggeredEffect

/**
 * Efekty zaczepne - odpalaja sie, gdy noszacy zadaje obrazenia, trafia krytycznie albo zabija.
 *
 * Wszystkie wygladaja tak samo: warunki decyduja "kiedy", metoda react opisuje "co". Zaden
 * z nich nie dotyka stanu bitwy - zwraca intencje, ktore wykona silnik.
 */

/** Podpala cel przy trafieniu. Ogien tyka co ture i kumuluje sie w stosy. */
class BurnOnHitEffect(
    private val potency: Float,
    private val turns: Int,
    private val chance: Float = 1f
) : TriggeredEffect {
    override val id = "burn_on_hit_${potency.toInt()}_$turns"
    override val description = "Podpala cel: ${potency.toInt()} obrazen przez $turns tury."
    override val trigger = Trigger.DamageDealt
    override val conditions: List<EffectCondition> =
        listOf(OwnerIsSource, ExcludeDamageTag(DamageTag.Burn), Chance(chance))

    override fun react(event: CombatEvent, context: EffectContext): List<CombatAction> {
        val damage = event as? CombatEvent.DamageDealt ?: return emptyList()
        if (damage.killed) return emptyList()

        return listOf(
            CombatAction.ApplyStatus(
                sourceId = context.ownerId,
                targetIds = listOf(damage.targetId),
                kind = StatusKind.Burn,
                potency = potency,
                turns = turns
            )
        )
    }
}

/** Zatruwa cel. Trucizna przebija obrone - tag [DamageTag.Poison] zalatwia to w potoku obrazen. */
class PoisonOnHitEffect(
    private val potency: Float,
    private val turns: Int,
    private val chance: Float = 1f
) : TriggeredEffect {
    override val id = "poison_on_hit_${potency.toInt()}_$turns"
    override val description = "Zatruwa cel: ${potency.toInt()} obrazen przez $turns tur, ignoruje pancerz."
    override val trigger = Trigger.DamageDealt
    override val conditions: List<EffectCondition> =
        listOf(OwnerIsSource, ExcludeDamageTag(DamageTag.Poison), Chance(chance))

    override fun react(event: CombatEvent, context: EffectContext): List<CombatAction> {
        val damage = event as? CombatEvent.DamageDealt ?: return emptyList()
        if (damage.killed) return emptyList()

        return listOf(
            CombatAction.ApplyStatus(
                sourceId = context.ownerId,
                targetIds = listOf(damage.targetId),
                kind = StatusKind.Poison,
                potency = potency,
                turns = turns
            )
        )
    }
}

/**
 * Mrozi cel, ale tylko przy trafieniu krytycznym. Zadnej logiki krytyka tu nie ma -
 * caly warunek to [OnCriticalOnly].
 */
class FreezeOnCriticalEffect(private val turns: Int) : TriggeredEffect {
    override val id = "freeze_on_crit_$turns"
    override val description = "Trafienie krytyczne zamraza cel na $turns ture."
    override val trigger = Trigger.DamageDealt
    override val conditions: List<EffectCondition> = listOf(OwnerIsSource, OnCriticalOnly)

    override fun react(event: CombatEvent, context: EffectContext): List<CombatAction> {
        val damage = event as? CombatEvent.DamageDealt ?: return emptyList()
        if (damage.killed) return emptyList()

        return listOf(
            CombatAction.ApplyStatus(
                sourceId = context.ownerId,
                targetIds = listOf(damage.targetId),
                kind = StatusKind.Freeze,
                potency = 0f,
                turns = turns
            )
        )
    }
}

/**
 * Dodatkowy atak po zabiciu wroga. Silnik pilnuje glebokosci lancucha, wiec nawet gdy dodatkowy
 * atak znowu kogos zabije, symulacja nie wpadnie w nieskonczona petle.
 */
class ExtraAttackOnKillEffect(private val chance: Float = 1f) : TriggeredEffect {
    override val id = "extra_attack_on_kill"
    override val description = "Po zabiciu przeciwnika atakujesz jeszcze raz."
    override val trigger = Trigger.Kill
    override val conditions: List<EffectCondition> = listOf(OwnerIsSource, Chance(chance))

    override fun react(event: CombatEvent, context: EffectContext): List<CombatAction> {
        if (context.enemies().isEmpty()) return emptyList()
        return listOf(CombatAction.ExtraAttack(attackerId = context.ownerId))
    }
}

/** Wybuch przy zabiciu - obrazenia obszarowe w pozostalych wrogow. */
class DeathExplosionEffect(private val damage: Float) : TriggeredEffect {
    override val id = "death_explosion_${damage.toInt()}"
    override val description = "Zabity przeciwnik wybucha, raniac pozostalych za ${damage.toInt()}."
    override val trigger = Trigger.Kill
    override val conditions: List<EffectCondition> = listOf(OwnerIsSource)

    override fun react(event: CombatEvent, context: EffectContext): List<CombatAction> {
        val victim = (event as? CombatEvent.Killed)?.victimId ?: return emptyList()
        val others = context.enemies().filter { it.id != victim }.map { it.id }
        if (others.isEmpty()) return emptyList()

        return listOf(
            CombatAction.DealDamage(
                sourceId = context.ownerId,
                targetIds = others,
                amount = damage,
                canCrit = false
            )
        )
    }
}
