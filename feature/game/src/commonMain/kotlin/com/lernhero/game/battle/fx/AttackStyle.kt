package com.lernhero.game.battle.fx

import com.lernhero.domain.combat.playback.HitKind

/**
 * Jak dana postac zadaje cios - od tego zalezy CALA inscenizacja trafienia:
 *
 * - [Melee] - zamach w miejscu i lecaca fala ciecia (miecz, pazury), ktora tnie cel.
 * - [Arrow] - strzala leci lukiem od luku do celu, wbija sie z drzazgami.
 * - [Bolt] - magiczna kula w kolorze zywiolu (ogien, woda, natura, smierc) ze smuga czasteczek.
 * - [Smash] - olbrzym (boss, elita) uderza w ziemie, a fala uderzeniowa biegnie po podlozu do celu.
 *
 * Nikt nie podbiega do celu - druzyny strzelaja do siebie z dystansu, a atakujacy tylko lekko sie
 * wysuwa (patrz BattleField.CharacterSlot).
 *
 * Wczesniej kazdy cios w grze - lucznika, maga, wilka i bohatera z mieczem - byl tym samym
 * fioletowym pociskiem i tym samym wybuchem.
 */
enum class AttackStyle {
    Melee,
    Arrow,
    Bolt,
    Smash;

}

/**
 * Pocisk, ktory leci od atakujacego do celu. Zwykle ciosy biora go z [AttackStyle] postaci,
 * a zaklecia z rodzaju trafienia ([HitKind]) - kula ognia zostaje kula ognia niezaleznie od tego,
 * kto ja rzucil.
 */
enum class ProjectileKind(val flightMs: Int) {
    /** Polksiezyc energii z miecza albo pazurow. */
    SlashWave(250),
    /** Fala uderzeniowa olbrzyma biegnaca po ziemi. */
    GroundWave(380),
    Arrow(270),
    Bolt(320),
    Fireball(380),
    PoisonGlob(360),
    Arcane(340);
}

object AttackStyles {

    private val archers = setOf(
        "skeleton_archer", "orc_archer", "human_archer", "elf_ranger", "ice_archer", "centaur_huntress"
    )

    private val casters = setOf(
        "skeleton_mage", "flame_cultist", "lich", "naga_siren", "banshee", "ghost", "efreet", "ember_imp",
        "mushroom_sprite", "fire_bat", "bubble_slime", "lava_slime", "phoenix", "salamander",
        "boss_skeleton_dragon"
    )

    /**
     * Styl zwyklego ciosu postaci. [isHero] - bohater zawsze tnie mieczem. [battleScale] powyzej
     * [GIANT_SCALE] to boss/elita: taki olbrzym nie biega po polu, tylko bije w miejscu ([AttackStyle.Smash]),
     * chyba ze strzela albo czaruje.
     */
    fun of(avatarKey: String, isHero: Boolean, battleScale: Float = 1f): AttackStyle = when {
        isHero -> AttackStyle.Melee
        avatarKey in archers -> AttackStyle.Arrow
        avatarKey in casters -> AttackStyle.Bolt
        battleScale > GIANT_SCALE -> AttackStyle.Smash
        else -> AttackStyle.Melee
    }

    /**
     * Pocisk dla konkretnego trafienia albo null, gdy nic nie leci (leczenie, tarcza, kolce).
     * Zaklecia maja wlasny pocisk niezaleznie od stylu rzucajacego.
     */
    fun projectileFor(style: AttackStyle, kind: HitKind): ProjectileKind? = when (kind) {
        HitKind.Heal, HitKind.Shield, HitKind.Reflect -> null
        HitKind.Burn -> ProjectileKind.Fireball
        HitKind.Poison -> ProjectileKind.PoisonGlob
        HitKind.Skill -> ProjectileKind.Arcane
        HitKind.Hit, HitKind.Crit, HitKind.Block, HitKind.Dodge -> when (style) {
            AttackStyle.Arrow -> ProjectileKind.Arrow
            AttackStyle.Bolt -> ProjectileKind.Bolt
            AttackStyle.Melee -> ProjectileKind.SlashWave
            AttackStyle.Smash -> ProjectileKind.GroundWave
        }
    }

    /** Powyzej tej skali postac to olbrzym - bije w miejscu, bez doskoku. */
    const val GIANT_SCALE = 1.9f
}
