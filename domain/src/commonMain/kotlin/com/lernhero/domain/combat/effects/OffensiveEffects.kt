package com.lernhero.domain.combat.effects

import com.lernhero.domain.combat.Combatant
import com.lernhero.domain.monster.Element
import com.lernhero.domain.combat.DamagePacket
import com.lernhero.domain.combat.DamageModifierEffect
import com.lernhero.domain.combat.DamageModification
import com.lernhero.domain.combat.DamagePhase
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
    override val description = "Sets the target on fire: ${potency.toInt()} damage over $turns turns."
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
    override val description = "Poisons the target: ${potency.toInt()} damage over $turns turns, ignores armor."
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
    override val description = "Critical hits freeze the target for $turns turn(s)."
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
    override val description = "After killing an enemy you attack again."
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
    override val description = "A slain enemy explodes, hitting the rest for ${damage.toInt()}."
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

/**
 * Szansa na DRUGI atak w tej samej turze - klasyczne "double strike" z RPG.
 *
 * Rozni sie od [ExtraAttackOnKillEffect] warunkiem, a nie skutkiem: tamten wymaga zabicia wroga,
 * ten wystarczy, ze trafisz. Dlatego jest tez znacznie mocniejszy przy tej samej szansie i broni
 * z nim powinny miec nizsze statystyki bazowe.
 *
 * [ExcludeDamageTag] odsiewa trucizne, podpalenie i odbicie: bez tego bron zaczynalaby dodatkowe
 * ataki od wlasnych obrazen obszarowych, a lancuch napedzalby sam siebie (przed nieskonczonoscia
 * broni tylko limit glebokosci w silniku - to za slaba gwarancja jak na regule przedmiotu).
 */
class DoubleAttackChanceEffect(private val chance: Float) : TriggeredEffect {
    override val id = "double_attack_${(chance * 100).toInt()}"
    override val description = "${(chance * 100).toInt()}% chance to strike twice."
    override val trigger = Trigger.DamageDealt
    override val conditions: List<EffectCondition> = listOf(
        OwnerIsSource,
        ExcludeDamageTag(DamageTag.Poison),
        ExcludeDamageTag(DamageTag.Burn),
        ExcludeDamageTag(DamageTag.Reflect),
        Chance(chance)
    )

    override fun react(event: CombatEvent, context: EffectContext): List<CombatAction> {
        if (context.enemies().isEmpty()) return emptyList()
        return listOf(CombatAction.ExtraAttack(attackerId = context.ownerId))
    }
}

/**
 * Bron "przeciw konkretnemu typowi": mocniejsza wylacznie wtedy, gdy cel ma zywiol [against].
 *
 * To jedyny efekt, ktory czyta [Combatant.element], i dlatego jest modyfikatorem obrazen, a nie
 * bonusem do statystyk: premia ma zalezec od TEGO, w kogo akurat lecisz, a nie od tego, co masz
 * na sobie. Srebrny miecz w plecaku nie czyni cie silniejszym - czyni cie silniejszym wobec
 * nieumarlych.
 */
class ElementBaneEffect(
    private val against: Element,
    private val bonus: Float
) : DamageModifierEffect {
    override val id = "bane_${against.name.lowercase()}_${(bonus * 100).toInt()}"
    override val description =
        "+${(bonus * 100).toInt()}% damage against ${against.label} enemies."
    override val phase = DamagePhase.Outgoing

    override fun modify(packet: DamagePacket, context: EffectContext): DamageModification {
        val target = context.state.combatants[packet.targetId]
        if (target?.element != against) return DamageModification(packet)
        return DamageModification(packet.scaled(1f + bonus))
    }
}
