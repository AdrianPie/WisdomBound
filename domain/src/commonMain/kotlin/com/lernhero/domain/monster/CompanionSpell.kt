package com.lernhero.domain.monster

import com.lernhero.domain.combat.playback.HitKind
import com.lernhero.domain.spell.SpellTarget
import kotlin.math.roundToInt

/**
 * Co robi zaklecie kompana, gdy zadziala. Celowo waskie - kazdy wariant musi dac sie policzyc
 * w zywej walce bez zadnego nowego systemu (patrz [com.lernhero.domain.spell.LiveSpellSpec]).
 */
enum class CompanionSpellKind(val label: String) {
    /** Leczy sojusznika o procent JEGO maks. HP. */
    Heal("Heal"),
    /** Dodatkowe obrazenia w cel - mnoznik ataku kompana. */
    Strike("Strike"),
    /** Obrazenia we wszystkich wrogow naraz. */
    Blast("Blast")
}

/**
 * Zaklecie przypisane do karty kompana. W przeciwienstwie do zaklec bohatera (pasek zdolnosci,
 * gdzie gracz sam rozdaje procenty) tutaj szansa jest CECHA KARTY - to ona odrozania kompanow od
 * siebie tak samo jak HP i atak.
 *
 * Ulepszanie: [MonsterSpellLevels] podnosi i szanse, i sile, za esencje. Dzieki temu esencja z
 * rozpuszczonych duplikatow ma dokladnie gdzie wracac.
 *
 * [chancePercent] i [power] to wartosci na 1. poziomie zaklecia.
 */
data class CompanionSpell(
    val id: String,
    val name: String,
    val description: String,
    val kind: CompanionSpellKind,
    val icon: String,
    /** Bazowa szansa na zadzialanie przy ciosie kompana (w procentach). */
    val chancePercent: Int,
    /**
     * Sila na 1. poziomie. Dla [CompanionSpellKind.Heal] to UŁAMEK maks. HP celu (0.05 = 5%),
     * dla pozostalych - mnoznik ataku kompana.
     */
    val power: Float
) {
    val target: SpellTarget
        get() = when (kind) {
            CompanionSpellKind.Heal -> SpellTarget.WoundedAlly
            CompanionSpellKind.Strike -> SpellTarget.RandomEnemy
            CompanionSpellKind.Blast -> SpellTarget.AllEnemies
        }

    val hitKind: HitKind
        get() = when (kind) {
            CompanionSpellKind.Heal -> HitKind.Heal
            CompanionSpellKind.Strike -> HitKind.Skill
            CompanionSpellKind.Blast -> HitKind.Burn
        }
}

/**
 * Poziomy zaklecia kompana - kupowane za ESENCJE. To jest druga, obok gwiazdek, sciezka rozwoju
 * kolekcji: gwiazdki ida z duplikatow (los), zaklecia z esencji (praca). Gracz, ktoremu nie
 * dopisalo w losowaniu, wciaz ma co robic ze swoja kolekcja.
 */
object MonsterSpellLevels {

    const val MIN_LEVEL = 1
    const val MAX_LEVEL = 10

    /** Kazdy poziom dokłada tyle punktow procentowych szansy na zadzialanie. */
    private const val CHANCE_PER_LEVEL = 2

    /** ...i tyle procent sily (leczenia albo obrazen). */
    private const val POWER_PER_LEVEL = 0.15f

    fun clamp(level: Int): Int = level.coerceIn(MIN_LEVEL, MAX_LEVEL)

    fun isMaxed(level: Int): Boolean = clamp(level) >= MAX_LEVEL

    fun chanceAt(spell: CompanionSpell, level: Int): Int =
        spell.chancePercent + (clamp(level) - MIN_LEVEL) * CHANCE_PER_LEVEL

    fun powerAt(spell: CompanionSpell, level: Int): Float =
        spell.power * (1f + (clamp(level) - MIN_LEVEL) * POWER_PER_LEVEL)

    /** Koszt awansu Z [level] na kolejny, w esencji. Rosnie liniowo - to ma byc staly, czytelny sink. */
    fun upgradeCost(level: Int): Int {
        if (isMaxed(level)) return 0
        return 40 + (clamp(level) - MIN_LEVEL) * 35
    }

    /** Opis efektu na danym poziomie - to, co gracz czyta na karcie kompana. */
    fun describe(spell: CompanionSpell, level: Int): String {
        val chance = chanceAt(spell, level)
        val power = powerAt(spell, level)
        return when (spell.kind) {
            CompanionSpellKind.Heal -> "$chance% chance: heals ${(power * 100).roundToInt()}% max HP"
            CompanionSpellKind.Strike -> "$chance% chance: extra hit for ${(power * 100).roundToInt()}% attack"
            CompanionSpellKind.Blast -> "$chance% chance: blast for ${(power * 100).roundToInt()}% attack on all"
        }
    }
}

/**
 * Katalog zaklec kompanow. PLACEHOLDERY - nazwy, ikony i liczby sa do przestrojenia, ale ksztalt
 * jest docelowy: nowe zaklecie to jeden wpis tutaj plus przypisanie go do karty w [MonsterCatalog].
 */
object CompanionSpells {

    val MEND = CompanionSpell(
        id = "mend",
        name = "Field Dressing",
        description = "The companion tends to the most wounded ally.",
        kind = CompanionSpellKind.Heal,
        icon = "💚",
        chancePercent = 30,
        power = 0.05f
    )

    val BONE_SHARD = CompanionSpell(
        id = "bone_shard",
        name = "Bone Shard",
        description = "Adds a quick strike against a random enemy.",
        kind = CompanionSpellKind.Strike,
        icon = "🦴",
        chancePercent = 25,
        power = 0.6f
    )

    val ARROW_VOLLEY = CompanionSpell(
        id = "arrow_volley",
        name = "Arrow Volley",
        description = "Fires on the entire enemy formation.",
        kind = CompanionSpellKind.Blast,
        icon = "🏹",
        chancePercent = 18,
        power = 0.45f
    )

    val DARK_BOLT = CompanionSpell(
        id = "dark_bolt",
        name = "Dark Bolt",
        description = "A heavy missile aimed at one enemy.",
        kind = CompanionSpellKind.Strike,
        icon = "🔮",
        chancePercent = 22,
        power = 0.9f
    )

    val WAR_CRY = CompanionSpell(
        id = "war_cry",
        name = "War Cry",
        description = "Rallies the party - an extra hit against a random enemy.",
        kind = CompanionSpellKind.Strike,
        icon = "📣",
        chancePercent = 28,
        power = 0.5f
    )

    val FLAME_ARROW = CompanionSpell(
        id = "flame_arrow",
        name = "Flame Arrow",
        description = "Sets the entire enemy formation alight.",
        kind = CompanionSpellKind.Blast,
        icon = "🔥",
        chancePercent = 15,
        power = 0.55f
    )

    val all: List<CompanionSpell> = listOf(MEND, BONE_SHARD, ARROW_VOLLEY, DARK_BOLT, WAR_CRY, FLAME_ARROW)

    fun byId(id: String): CompanionSpell? = all.firstOrNull { it.id == id }
}
