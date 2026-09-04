package com.lernhero.domain.combat

/**
 * Bramka aktywacji efektu. Warunki sa osobnym klockiem, bo inaczej kazdy efekt musialby sam
 * powtarzac "ale tylko raz na walke", "ale tylko przy krytyku", "ale tylko ponizej 30% HP".
 *
 * Efekt sklada sie wiec z dwoch rzeczy: co robi (react) i kiedy wolno mu to zrobic (conditions).
 */
fun interface EffectCondition {
    fun isMet(event: CombatEvent, context: EffectContext): Boolean
}

/** Zdarzenie dotyczy noszacego jako sprawcy: to on uderzyl, on zabil, on leczyl. */
object OwnerIsSource : EffectCondition {
    override fun isMet(event: CombatEvent, context: EffectContext): Boolean =
        event.sourceId() == context.ownerId
}

/** Zdarzenie dotyczy noszacego jako celu: to on oberwal. */
object OwnerIsTarget : EffectCondition {
    override fun isMet(event: CombatEvent, context: EffectContext): Boolean =
        event.targetId() == context.ownerId
}

/** Tura noszacego - dla efektow aktywnych odpalanych na wlasnej turze. */
object OwnerIsActor : EffectCondition {
    override fun isMet(event: CombatEvent, context: EffectContext): Boolean = when (event) {
        is CombatEvent.TurnStarted -> event.actorId == context.ownerId
        is CombatEvent.TurnEnded -> event.actorId == context.ownerId
        else -> false
    }
}

object OnCriticalOnly : EffectCondition {
    override fun isMet(event: CombatEvent, context: EffectContext): Boolean = when (event) {
        is CombatEvent.DamageDealt -> event.isCritical
        is CombatEvent.DamageTaken -> event.isCritical
        is CombatEvent.CriticalHit -> true
        else -> false
    }
}

/** Aktywuje sie dopiero, gdy noszacemu zostalo mniej niz [fraction] zycia (0.3 = 30%). */
class BelowHpFraction(private val fraction: Float) : EffectCondition {
    override fun isMet(event: CombatEvent, context: EffectContext): Boolean {
        val owner = context.owner
        return owner.hp.toFloat() / owner.maxHp < fraction
    }
}

class AboveHpFraction(private val fraction: Float) : EffectCondition {
    override fun isMet(event: CombatEvent, context: EffectContext): Boolean {
        val owner = context.owner
        return owner.hp.toFloat() / owner.maxHp > fraction
    }
}

/** Klasyczne "raz na walke". Licznik siedzi w stanie bitwy, nie w efekcie. */
object OncePerBattle : EffectCondition {
    override fun isMet(event: CombatEvent, context: EffectContext): Boolean =
        context.usage.timesUsed == 0
}

class MaxUsesPerBattle(private val uses: Int) : EffectCondition {
    override fun isMet(event: CombatEvent, context: EffectContext): Boolean =
        context.usage.timesUsed < uses
}

/**
 * Odpala sie co [turns] tur. Skraca sie o statystyke [StatType.CooldownReduction] noszacego,
 * wiec przedmiot "skracajacy cooldowny" dziala na wszystkie takie efekty naraz - i nie musi
 * nic o nich wiedziec.
 */
class EveryNTurns(private val turns: Int) : EffectCondition {
    override fun isMet(event: CombatEvent, context: EffectContext): Boolean {
        val reduction = context.ownerStat(StatType.CooldownReduction).toInt()
        val interval = (turns - reduction).coerceAtLeast(1)
        val last = context.usage.lastTurnUsed
        if (last < 0) return true
        return context.state.turn - last >= interval
    }
}

/** Szansa procentowa (0.0 - 1.0). Losowosc idzie z zasianego generatora, wiec walka jest odtwarzalna. */
class Chance(private val probability: Float) : EffectCondition {
    override fun isMet(event: CombatEvent, context: EffectContext): Boolean =
        context.random.nextFloat() < probability
}

/** Dziala tylko, gdy cel zdarzenia ma dany stan - np. bonus do obrazen w podpalonych. */
class TargetHasStatus(private val kind: StatusKind) : EffectCondition {
    override fun isMet(event: CombatEvent, context: EffectContext): Boolean {
        val targetId = event.targetId() ?: return false
        return context.state.combatant(targetId)?.statuses?.any { it.kind == kind } == true
    }
}

/** Odsiewa obrazenia oznaczone tagiem - np. odbicie nie reaguje na odbicie. */
class ExcludeDamageTag(private val tag: DamageTag) : EffectCondition {
    override fun isMet(event: CombatEvent, context: EffectContext): Boolean = when (event) {
        is CombatEvent.DamageDealt -> event.tag != tag
        is CombatEvent.DamageTaken -> event.tag != tag
        else -> true
    }
}

/* --------------------------------- Odczyt stron zdarzenia --------------------------------- */

fun CombatEvent.sourceId(): CombatantId? = when (this) {
    is CombatEvent.DamageDealt -> sourceId
    is CombatEvent.DamageTaken -> sourceId
    is CombatEvent.CriticalHit -> sourceId
    is CombatEvent.Dodged -> sourceId
    is CombatEvent.Killed -> killerId
    is CombatEvent.Healed -> sourceId
    is CombatEvent.StatusApplied -> sourceId
    is CombatEvent.TurnStarted -> actorId
    is CombatEvent.TurnEnded -> actorId
    is CombatEvent.Died -> null
    CombatEvent.BattleStarted -> null
}

fun CombatEvent.targetId(): CombatantId? = when (this) {
    is CombatEvent.DamageDealt -> targetId
    is CombatEvent.DamageTaken -> targetId
    is CombatEvent.CriticalHit -> targetId
    is CombatEvent.Dodged -> targetId
    is CombatEvent.Killed -> victimId
    is CombatEvent.Healed -> targetId
    is CombatEvent.StatusApplied -> targetId
    is CombatEvent.Died -> victimId
    is CombatEvent.TurnStarted -> null
    is CombatEvent.TurnEnded -> null
    CombatEvent.BattleStarted -> null
}
