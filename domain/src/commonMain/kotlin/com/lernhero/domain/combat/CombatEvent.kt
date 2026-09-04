package com.lernhero.domain.combat

/**
 * Momenty, w ktorych silnik pyta efekty o reakcje. Nowy trigger dopisuje sie tutaj i emituje
 * w silniku - istniejace efekty nic o tym nie wiedza i dzialaja dalej.
 */
enum class Trigger {
    BattleStart,
    TurnStart,
    TurnEnd,
    DamageDealt,
    DamageTaken,
    CriticalHit,
    Dodged,
    Kill,
    Death,
    Healed,
    StatusApplied
}

/**
 * Co sie wydarzylo. Ten sam strumien zdarzen zasila trzy rzeczy naraz: reakcje efektow,
 * log bitwy i skrypt odtwarzany przez UI.
 */
sealed interface CombatEvent {
    val trigger: Trigger

    data object BattleStarted : CombatEvent {
        override val trigger get() = Trigger.BattleStart
    }

    data class TurnStarted(val turn: Int, val actorId: CombatantId) : CombatEvent {
        override val trigger get() = Trigger.TurnStart
    }

    data class TurnEnded(val turn: Int, val actorId: CombatantId) : CombatEvent {
        override val trigger get() = Trigger.TurnEnd
    }

    /**
     * Zadano obrazenia. Nosi [targetHpAfter], bo warstwa odtwarzania rysuje z tego paski HP -
     * nie musi powtarzac obliczen silnika.
     */
    data class DamageDealt(
        val sourceId: CombatantId,
        val targetId: CombatantId,
        val amount: Int,
        val targetHpAfter: Int,
        val isCritical: Boolean,
        val killed: Boolean,
        val tag: DamageTag
    ) : CombatEvent {
        override val trigger get() = Trigger.DamageDealt
    }

    /** To samo zdarzenie z perspektywy obrywajacego - silnik wysyla oba, efekty wybieraja swoje. */
    data class DamageTaken(
        val targetId: CombatantId,
        val sourceId: CombatantId,
        val amount: Int,
        val isCritical: Boolean,
        val tag: DamageTag
    ) : CombatEvent {
        override val trigger get() = Trigger.DamageTaken
    }

    data class CriticalHit(
        val sourceId: CombatantId,
        val targetId: CombatantId,
        val amount: Int
    ) : CombatEvent {
        override val trigger get() = Trigger.CriticalHit
    }

    data class Dodged(val targetId: CombatantId, val sourceId: CombatantId) : CombatEvent {
        override val trigger get() = Trigger.Dodged
    }

    data class Killed(val killerId: CombatantId, val victimId: CombatantId) : CombatEvent {
        override val trigger get() = Trigger.Kill
    }

    data class Died(val victimId: CombatantId) : CombatEvent {
        override val trigger get() = Trigger.Death
    }

    data class Healed(
        val sourceId: CombatantId,
        val targetId: CombatantId,
        val amount: Int,
        val targetHpAfter: Int
    ) : CombatEvent {
        override val trigger get() = Trigger.Healed
    }

    data class StatusApplied(
        val sourceId: CombatantId,
        val targetId: CombatantId,
        val kind: StatusKind,
        val turns: Int
    ) : CombatEvent {
        override val trigger get() = Trigger.StatusApplied
    }
}
