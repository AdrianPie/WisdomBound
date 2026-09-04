package com.lernhero.domain.combat

import kotlin.random.Random

/**
 * Efekt to najmniejszy klocek zachowania w grze. Przedmiot nie ma wlasnej logiki - jest
 * lista efektow, a te sa wielokrotnego uzytku.
 *
 * Sa cztery rodzaje, bo silnik pyta o nie w czterech roznych momentach:
 *
 * - [StatEffect]          - stala zmiana statystyk noszacego (pasywne).
 * - [AuraEffect]          - zmiana statystyk innych walczacych (aury).
 * - [TriggeredEffect]     - reakcja na zdarzenie; zwraca intencje, nie zmienia stanu.
 * - [DamageModifierEffect]- przeksztalca obrazenia w locie (uniki, blok, odbicie, redukcja).
 *
 * Interfejsy sa otwarte: nowy typ efektu dopisuje sie bez dotykania silnika (OCP).
 */
interface Effect {
    /** Stabilny identyfikator - trafia do logow i do stanu uzyc (raz na walke, co X tur). */
    val id: String

    val description: String
}

/** Pasywny bonus do statystyk noszacego. Np. pierscien +15% krytyka, buty +5 szybkosci. */
interface StatEffect : Effect {
    val bonus: Stats
}

enum class AuraScope {
    Allies,
    Enemies
}

/** Bonus rozlewajacy sie na innych. Sojusznicy dostaja go bez wiedzy o zrodle. */
interface AuraEffect : Effect {
    val bonus: Stats
    val scope: AuraScope
}

/**
 * Reaguje na zdarzenie w walce i zwraca **intencje** ([CombatAction]), ktore wykona silnik.
 *
 * Efekt nigdy nie modyfikuje stanu sam. To celowe: dzieki temu jest czysta funkcja
 * (zdarzenie, kontekst) -> lista akcji, wiec da sie go przetestowac bez calej bitwy
 * i nie da sie nim popsuc stanu w polowie tury.
 */
interface TriggeredEffect : Effect {
    val trigger: Trigger

    /** Bramki aktywacji: ponizej 30% HP, tylko na krytyku, raz na walke, co 3 tury... */
    val conditions: List<EffectCondition> get() = emptyList()

    fun react(event: CombatEvent, context: EffectContext): List<CombatAction>
}

enum class DamagePhase {
    /** Obrazenia wychodzace od noszacego efekt. */
    Outgoing,

    /** Obrazenia lecace w noszacego efekt. */
    Incoming
}

/**
 * Przeksztalca pakiet obrazen w trakcie rozliczania. Tu mieszkaja rzeczy, ktorych nie da sie
 * zrobic reakcja "po fakcie": zignorowanie pierwszego ciosu, unik, odbicie polowy obrazen.
 *
 * [priority] ustala kolejnosc (mniejsza liczba = wczesniej). Unik musi zadzialac przed
 * pancerzem, bo nie ma czego blokowac, gdy cios nie trafil.
 */
interface DamageModifierEffect : Effect {
    val phase: DamagePhase

    val priority: Int get() = 0

    val conditions: List<EffectCondition> get() = emptyList()

    fun modify(packet: DamagePacket, context: EffectContext): DamageModification
}

/**
 * Wynik pracy [DamageModifierEffect]: zmieniony pakiet, ewentualne akcje uboczne (odbicie
 * to nic innego jak nowe obrazenia w druga strone) oraz informacja, czy efekt sie zuzyl.
 *
 * [consumed] = true zwieksza licznik uzyc, dzieki czemu warunki w rodzaju "raz na walke"
 * dzialaja bez trzymania stanu wewnatrz efektu.
 */
data class DamageModification(
    val packet: DamagePacket,
    val actions: List<CombatAction> = emptyList(),
    val consumed: Boolean = false
)

/**
 * Okno na swiat dla efektu: stan bitwy tylko do odczytu, wlasciciel, losowosc i licznik uzyc.
 *
 * [random] jest wstrzykiwany z zewnatrz i zasiany - cala walka jest wiec odtwarzalna z seeda,
 * tak samo jak mapa lochu. W trybie MMO serwer moze przeslac sam seed zamiast calej bitwy.
 */
class EffectContext(
    val state: BattleState,
    val ownerId: CombatantId,
    val random: Random,
    val usage: EffectUsage
) {
    val owner: Combatant get() = state.combatants.getValue(ownerId)

    fun stat(id: CombatantId, stat: StatType): Float = state.stat(id, stat)

    fun ownerStat(stat: StatType): Float = state.stat(ownerId, stat)

    fun allies(): List<Combatant> = state.aliveOn(owner.team)

    fun enemies(): List<Combatant> = state.aliveOn(owner.team.opposite)

    /** Sojusznicy z najnizszym HP - typowy cel leczenia. */
    fun woundedAllies(count: Int): List<Combatant> =
        allies().sortedBy { it.hp.toFloat() / it.maxHp }.take(count)
}
