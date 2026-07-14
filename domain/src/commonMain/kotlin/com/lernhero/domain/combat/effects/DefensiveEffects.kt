package com.lernhero.domain.combat.effects

import com.lernhero.domain.combat.BelowHpFraction
import com.lernhero.domain.combat.Chance
import com.lernhero.domain.combat.CombatAction
import com.lernhero.domain.combat.CombatEvent
import com.lernhero.domain.combat.DamageModification
import com.lernhero.domain.combat.DamageModifierEffect
import com.lernhero.domain.combat.DamagePacket
import com.lernhero.domain.combat.DamagePhase
import com.lernhero.domain.combat.DamageTag
import com.lernhero.domain.combat.EffectCondition
import com.lernhero.domain.combat.EffectContext
import com.lernhero.domain.combat.ExcludeDamageTag
import com.lernhero.domain.combat.OncePerBattle
import com.lernhero.domain.combat.OwnerIsTarget
import com.lernhero.domain.combat.StatusKind
import com.lernhero.domain.combat.Trigger
import com.lernhero.domain.combat.TriggeredEffect

/**
 * Efekty obronne - wchodza w potok obrazen ([DamageModifierEffect]) albo reaguja na oberwanie.
 *
 * Priorytety ustalaja kolejnosc w potoku: im mniejsza liczba, tym wczesniej. Blok musi zadzialac
 * przed odbiciem, bo odbijamy tylko to, co faktycznie doszlo.
 */

/**
 * Pancerz pochlania pierwszy cios w walce. Licznik "raz na walke" trzyma silnik, nie efekt -
 * dlatego ten sam obiekt moze nosic dowolna liczba postaci naraz.
 */
class IgnoreFirstHitEffect : DamageModifierEffect {
    override val id = "ignore_first_hit"
    override val description = "Pochlania pierwszy cios otrzymany w walce."
    override val phase = DamagePhase.Incoming
    override val priority = 10
    override val conditions = listOf(OncePerBattle, ExcludeDamageTag(DamageTag.Reflect))

    override fun modify(packet: DamagePacket, context: EffectContext): DamageModification {
        if (packet.amount <= 0f) return DamageModification(packet)
        // consumed = true zuzywa ladunek; bez tego "raz na walke" nigdy by sie nie skonczylo.
        return DamageModification(packet = packet.nullified(), consumed = true)
    }
}

/**
 * Tarcza odbija czesc otrzymanych obrazen w napastnika.
 *
 * Warunek [ExcludeDamageTag] jest tu zabezpieczeniem, bez ktorego dwie takie tarcze odbijalyby
 * sobie ten sam cios w kolko. Odbite obrazenia nosza tag [DamageTag.Reflect] i nie wracaja dalej.
 */
class ReflectDamageEffect(private val percent: Float) : DamageModifierEffect {
    override val id = "reflect_${(percent * 100).toInt()}"
    override val description = "Odbija ${(percent * 100).toInt()}% otrzymanych obrazen."
    override val phase = DamagePhase.Incoming
    override val priority = 20
    override val conditions = listOf(ExcludeDamageTag(DamageTag.Reflect))

    override fun modify(packet: DamagePacket, context: EffectContext): DamageModification {
        if (packet.amount <= 0f || packet.sourceId == context.ownerId) {
            return DamageModification(packet)
        }
        return DamageModification(
            packet = packet,
            actions = listOf(
                CombatAction.DealDamage(
                    sourceId = context.ownerId,
                    targetIds = listOf(packet.sourceId),
                    amount = packet.amount * percent,
                    canCrit = false,
                    tag = DamageTag.Reflect
                )
            )
        )
    }
}

/**
 * Ostatnia deska ratunku: gdy zdrowie spadnie ponizej progu, przedmiot raz na walke daje tarcze.
 * Pokazuje, jak zlozyc dwa warunki - "tylko ponizej X% HP" i "tylko raz" - bez pisania ich w efekcie.
 */
class LastStandEffect(
    private val hpFraction: Float,
    private val shield: Float
) : TriggeredEffect {
    override val id = "last_stand_${(hpFraction * 100).toInt()}"
    override val description =
        "Ponizej ${(hpFraction * 100).toInt()}% zycia daje ${shield.toInt()} tarczy. Raz na walke."
    override val trigger = Trigger.DamageTaken
    override val conditions: List<EffectCondition> =
        listOf(OwnerIsTarget, BelowHpFraction(hpFraction), OncePerBattle)

    override fun react(event: CombatEvent, context: EffectContext): List<CombatAction> = listOf(
        CombatAction.GrantShield(targetIds = listOf(context.ownerId), amount = shield)
    )
}

/**
 * Kolce: kazde oberwanie ma szanse zamrozic napastnika. Przyklad efektu odpalanego
 * "gdy otrzymujesz obrazenia", z losowa bramka.
 */
class FrostThornsEffect(
    private val chance: Float,
    private val turns: Int
) : TriggeredEffect {
    override val id = "frost_thorns_${(chance * 100).toInt()}"
    override val description = "${(chance * 100).toInt()}% szansy na zamrozenie napastnika."
    override val trigger = Trigger.DamageTaken
    override val conditions: List<EffectCondition> =
        listOf(OwnerIsTarget, ExcludeDamageTag(DamageTag.Reflect), Chance(chance))

    override fun react(event: CombatEvent, context: EffectContext): List<CombatAction> {
        val attacker = (event as? CombatEvent.DamageTaken)?.sourceId ?: return emptyList()
        if (attacker == context.ownerId) return emptyList()

        return listOf(
            CombatAction.ApplyStatus(
                sourceId = context.ownerId,
                targetIds = listOf(attacker),
                kind = StatusKind.Freeze,
                potency = 0f,
                turns = turns
            )
        )
    }
}
