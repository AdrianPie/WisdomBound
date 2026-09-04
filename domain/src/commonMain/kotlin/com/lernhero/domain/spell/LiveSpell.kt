package com.lernhero.domain.spell

import com.lernhero.domain.combat.playback.HitKind

/** Kogo trafia zaklecie rzucone w zywej walce. */
enum class SpellTarget {
    /** Jeden losowy zywy wrog. */
    RandomEnemy,
    /** Wszyscy zywi wrogowie naraz. */
    AllEnemies,
    /** Najbardziej poobijany sojusznik (najnizszy ulamek HP). */
    WoundedAlly
}

/**
 * Opis zaklecia w jezyku ZYWEJ walki (BattleViewModel), a nie symulacji [com.lernhero.domain.combat.engine.BattleEngine].
 *
 * Dlaczego osobno od [Spell.cast]: `cast` zwraca [com.lernhero.domain.combat.CombatAction] dla
 * silnika symulacyjnego, ktory liczy cala walke z gory. Ekran walki dziala inaczej - tura po turze,
 * sterowany quizem - i nie ma [com.lernhero.domain.combat.EffectContext], ktorego `cast` wymaga.
 * Zamiast przepisywac jeden system na drugi, kazde zaklecie niesie OBA opisy: ten sam wpis w
 * katalogu, dwa sposoby odczytania. Dzieki temu nie da sie dodac zaklecia, ktore dziala w jednym
 * miejscu, a w drugim nie.
 *
 * [damageMultiplier] i [healMultiplier] mnoza sie przez atak rzucajacego - tak samo jak w `cast`.
 */
data class LiveSpellSpec(
    val target: SpellTarget,
    val damageMultiplier: Float = 0f,
    val healMultiplier: Float = 0f,
    /** Ile tur cel traci kolejke (zamrozenie). 0 = zaklecie nie odbiera tury. */
    val freezeTurns: Int = 0,
    /** Rodzaj efektu na ekranie - patrz [HitKind]. */
    val hitKind: HitKind = HitKind.Skill
) {
    val heals: Boolean get() = healMultiplier > 0f
    val isAreaOfEffect: Boolean get() = target == SpellTarget.AllEnemies
}
