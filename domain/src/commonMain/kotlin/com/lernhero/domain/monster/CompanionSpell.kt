package com.lernhero.domain.monster

import com.lernhero.domain.combat.playback.HitKind
import com.lernhero.domain.spell.SpellTarget
import kotlin.math.roundToInt

/**
 * Co robi zaklecie kompana, gdy zadziala. Celowo waskie - kazdy wariant musi dac sie policzyc
 * w zywej walce bez zadnego nowego systemu (patrz [com.lernhero.domain.spell.LiveSpellSpec]).
 */
enum class CompanionSpellKind(val label: String) {
    /** Leczy najbardziej rannego sojusznika o procent JEGO maks. HP. */
    Heal("Heal"),
    /** Leczy CALA druzyne o procent maks. HP kazdego. */
    HealAll("Group heal"),
    /** Dodatkowe obrazenia w losowego wroga - mnoznik ataku kompana. */
    Strike("Strike"),
    /** Jak [Strike], ale przebija pancerz - dla gatunkow na opancerzonych wrogow. */
    Pierce("Pierce"),
    /** Obrazenia we wszystkich wrogow naraz. */
    Blast("Blast"),
    /** Skacze po [CHAIN_TARGETS] roznych wrogach. */
    Chain("Chain"),
    /** Cios, ktory leczy rzucajacego o zadane obrazenia. */
    Drain("Drain"),
    /** Cios w najbardziej rannego wroga - podwojny, gdy ten ma malo HP. */
    Execute("Execute"),
    /** Cios, po ktorym wrog traci swoj nastepny ruch. */
    Freeze("Freeze"),
    /** Kazdy sojusznik dostaje tarcze, ktora zatrzymuje nastepny cios. */
    Shield("Shield");

    companion object {
        /** Ilu roznych wrogow trafia [Chain]. */
        const val CHAIN_TARGETS = 3
    }
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
            CompanionSpellKind.Heal, CompanionSpellKind.HealAll, CompanionSpellKind.Shield -> SpellTarget.WoundedAlly
            CompanionSpellKind.Blast -> SpellTarget.AllEnemies
            else -> SpellTarget.RandomEnemy
        }

    val hitKind: HitKind
        get() = when (kind) {
            CompanionSpellKind.Heal, CompanionSpellKind.HealAll -> HitKind.Heal
            CompanionSpellKind.Blast, CompanionSpellKind.Chain -> HitKind.Burn
            CompanionSpellKind.Shield -> HitKind.Shield
            else -> HitKind.Skill
        }

    /** Leczy albo oslania wlasna druzyne zamiast bic wroga. */
    val supportsAllies: Boolean
        get() = kind == CompanionSpellKind.Heal || kind == CompanionSpellKind.HealAll ||
            kind == CompanionSpellKind.Shield
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
        val pct = (powerAt(spell, level) * 100).roundToInt()
        val effect = when (spell.kind) {
            CompanionSpellKind.Heal -> "heals the most wounded ally for $pct% max HP"
            CompanionSpellKind.HealAll -> "heals the whole party for $pct% max HP"
            CompanionSpellKind.Strike -> "extra hit for $pct% attack"
            CompanionSpellKind.Pierce -> "armor-piercing hit for $pct% attack"
            CompanionSpellKind.Blast -> "hits all enemies for $pct% attack"
            CompanionSpellKind.Chain -> "jumps between ${CompanionSpellKind.CHAIN_TARGETS} enemies for $pct% attack"
            CompanionSpellKind.Drain -> "drains $pct% attack, healing itself"
            CompanionSpellKind.Execute -> "hits the weakest enemy for $pct% attack, x2 below ${(CompanionPassive.PREDATOR_THRESHOLD * 100).roundToInt()}% HP"
            CompanionSpellKind.Freeze -> "hits for $pct% attack and freezes the target for a turn"
            CompanionSpellKind.Shield -> "shields every ally from the next hit"
        }
        return "$chance%: $effect"
    }
}

/**
 * Katalog zaklec kompanow. Ksztalt jest docelowy: nowe zaklecie to jeden wpis tutaj plus przypisanie go do karty w [MonsterCatalog].
 */
object CompanionSpells {

    val MEND = spell("mend", "Field Dressing", "Tends to the most wounded ally.", CompanionSpellKind.Heal, "💚", 30, 0.05f)
    val BONE_SHARD = spell("bone_shard", "Bone Shard", "A quick jagged shard at a random enemy.", CompanionSpellKind.Strike, "🦴", 25, 0.6f)
    val ARROW_VOLLEY = spell("arrow_volley", "Arrow Volley", "Fires on the entire enemy formation.", CompanionSpellKind.Blast, "🏹", 18, 0.45f)
    val DARK_BOLT = spell("dark_bolt", "Dark Bolt", "A heavy missile aimed at one enemy.", CompanionSpellKind.Strike, "🔮", 22, 0.9f)
    val WAR_CRY = spell("war_cry", "War Cry", "A roaring swing at a random enemy.", CompanionSpellKind.Strike, "📣", 28, 0.5f)
    val FLAME_ARROW = spell("flame_arrow", "Flame Arrow", "Sets the entire enemy formation alight.", CompanionSpellKind.Blast, "🔥", 15, 0.55f)

    // ---- Leczenie i oslona
    val SPORE_CLOUD = spell("spore_cloud", "Spore Cloud", "Soothing spores drift over the whole party.", CompanionSpellKind.HealAll, "🍄", 20, 0.04f)
    val TIDAL_SONG = spell("tidal_song", "Tidal Song", "A song of the sea that washes wounds away.", CompanionSpellKind.HealAll, "🎶", 20, 0.06f)
    val FOREST_EMBRACE = spell("forest_embrace", "Forest's Embrace", "Roots wrap the party and knit it back together.", CompanionSpellKind.HealAll, "🌳", 20, 0.07f)
    val REBIRTH_FLAME = spell("rebirth_flame", "Rebirth Flame", "Healing fire rains over every ally.", CompanionSpellKind.HealAll, "🪶", 18, 0.08f)
    val SHELL_GUARD = spell("shell_guard", "Shell Guard", "Barnacled shells snap over every ally.", CompanionSpellKind.Shield, "🐚", 16, 1f)
    val BUBBLE_SHIELD = spell("bubble_shield", "Bubble Shield", "Wobbling bubbles swallow the next blow.", CompanionSpellKind.Shield, "🫧", 16, 1f)
    val BARK_SKIN = spell("bark_skin", "Bark Skin", "Hard bark grows over the party.", CompanionSpellKind.Shield, "🪵", 15, 1f)
    val MAGMA_SHELL = spell("magma_shell", "Magma Shell", "Cooling lava hardens into armor.", CompanionSpellKind.Shield, "🌋", 14, 1f)
    val ICE_WALL = spell("ice_wall", "Ice Wall", "A wall of ice rises in front of the party.", CompanionSpellKind.Shield, "🧊", 14, 1f)

    // ---- Pojedyncze ciosy
    val CINDER_FLICK = spell("cinder_flick", "Cinder Flick", "Flicks a burning ember at an enemy.", CompanionSpellKind.Strike, "✨", 26, 0.6f)
    val FISH_SPEAR = spell("fish_spear", "Fish Spear", "Pokes an enemy with a tiny, angry spear.", CompanionSpellKind.Strike, "🔱", 26, 0.6f)
    val FIREBALL = spell("fireball", "Fireball", "Hurls a ball of fire at an enemy.", CompanionSpellKind.Strike, "☄️", 22, 1.0f)
    val TUSK_CHARGE = spell("tusk_charge", "Tusk Charge", "Charges straight into the enemy line.", CompanionSpellKind.Strike, "🐗", 24, 0.8f)
    val LAVA_BITE = spell("lava_bite", "Lava Bite", "A burning bite that melts through armor.", CompanionSpellKind.Pierce, "🌋", 24, 0.8f)
    val BLAZING_SLASH = spell("blazing_slash", "Blazing Slash", "A fiery blade that ignores armor.", CompanionSpellKind.Pierce, "🗡️", 22, 1.0f)
    val RUNEBLADE = spell("runeblade", "Runeblade", "A cursed blade that cuts through any plate.", CompanionSpellKind.Pierce, "⚔️", 22, 1.1f)
    val HUNTERS_MARK = spell("hunters_mark", "Hunter's Mark", "Finishes off the weakest enemy.", CompanionSpellKind.Execute, "🎯", 22, 0.7f)
    val RENDING_CLAWS = spell("rending_claws", "Rending Claws", "Tears into whoever is bleeding the most.", CompanionSpellKind.Execute, "🐾", 22, 0.7f)
    val LABRYS_CLEAVE = spell("labrys_cleave", "Labrys Cleave", "A massive axe swing at the weakest foe.", CompanionSpellKind.Execute, "🪓", 20, 1.0f)
    val BLOOD_DRAIN = spell("blood_drain", "Blood Drain", "Drinks an enemy's life to heal itself.", CompanionSpellKind.Drain, "🩸", 24, 0.8f)
    val LIFE_LEECH = spell("life_leech", "Life Leech", "A burning bite that feeds the bat.", CompanionSpellKind.Drain, "🦇", 24, 0.6f)
    val GRAVE_BITE = spell("grave_bite", "Grave Bite", "An infectious bite that mends rotten flesh.", CompanionSpellKind.Drain, "🧟", 20, 0.5f)
    val FROST_FANG = spell("frost_fang", "Frost Fang", "An icy bite that freezes its prey.", CompanionSpellKind.Freeze, "❄️", 20, 0.5f)
    val ICE_ARROW = spell("ice_arrow", "Ice Arrow", "A frozen arrow that pins an enemy in place.", CompanionSpellKind.Freeze, "🏹", 18, 0.6f)
    val WAIL = spell("wail", "Wail", "A chilling wail that paralyses an enemy.", CompanionSpellKind.Freeze, "👻", 18, 0.5f)
    val SIREN_CALL = spell("siren_call", "Siren Call", "An enemy stops to listen - and forgets to attack.", CompanionSpellKind.Freeze, "🧜", 16, 0.4f)
    val GLACIER_SLAM = spell("glacier_slam", "Glacier Slam", "A crushing blow of solid ice.", CompanionSpellKind.Freeze, "🏔️", 16, 0.9f)

    // ---- Obszar
    val MAGMA_BURST = spell("magma_burst", "Magma Burst", "Bursts and splashes lava on every enemy.", CompanionSpellKind.Blast, "💥", 14, 0.4f)
    val SOUL_SCREAM = spell("soul_scream", "Soul Scream", "A scream that hurts every enemy.", CompanionSpellKind.Blast, "😱", 18, 0.5f)
    val AVALANCHE = spell("avalanche", "Avalanche Roar", "A roar that brings the mountain down.", CompanionSpellKind.Blast, "🌨️", 14, 0.5f)
    val FIRESTORM = spell("firestorm", "Firestorm", "Engulfs all enemies in flame.", CompanionSpellKind.Blast, "🌪️", 16, 0.7f)
    val ARROW_STORM = spell("arrow_storm", "Storm of Arrows", "A storm of arrows on every enemy.", CompanionSpellKind.Blast, "🌧️", 18, 0.6f)
    val SOUL_FIRE = spell("soul_fire", "Soul Fire", "Ghostly flames burn all enemies.", CompanionSpellKind.Blast, "💀", 16, 0.75f)
    val FOX_FLURRY = spell("fox_flurry", "Leaf Flurry", "Darts between enemies in a flurry of leaves.", CompanionSpellKind.Chain, "🍃", 22, 0.45f)
    val BRAMBLE_LASH = spell("bramble_lash", "Bramble Lash", "Thorny vines whip through the enemy line.", CompanionSpellKind.Chain, "🌿", 20, 0.5f)
    val BAT_SWARM = spell("bat_swarm", "Bat Swarm", "A cloud of bats bites several enemies.", CompanionSpellKind.Chain, "🦇", 20, 0.45f)
    val SOUL_CHAIN = spell("soul_chain", "Soul Chain", "A chain of souls lashes three enemies.", CompanionSpellKind.Chain, "⛓️", 20, 0.55f)

    val all: List<CompanionSpell> = listOf(
        MEND, BONE_SHARD, ARROW_VOLLEY, DARK_BOLT, WAR_CRY, FLAME_ARROW,
        SPORE_CLOUD, TIDAL_SONG, FOREST_EMBRACE, REBIRTH_FLAME,
        SHELL_GUARD, BUBBLE_SHIELD, BARK_SKIN, MAGMA_SHELL, ICE_WALL,
        CINDER_FLICK, FISH_SPEAR, FIREBALL, TUSK_CHARGE,
        LAVA_BITE, BLAZING_SLASH, RUNEBLADE,
        HUNTERS_MARK, RENDING_CLAWS, LABRYS_CLEAVE,
        BLOOD_DRAIN, LIFE_LEECH, GRAVE_BITE,
        FROST_FANG, ICE_ARROW, WAIL, SIREN_CALL, GLACIER_SLAM,
        MAGMA_BURST, SOUL_SCREAM, AVALANCHE, FIRESTORM, ARROW_STORM, SOUL_FIRE,
        FOX_FLURRY, BRAMBLE_LASH, BAT_SWARM, SOUL_CHAIN
    )

    fun byId(id: String): CompanionSpell? = all.firstOrNull { it.id == id }

    private fun spell(
        id: String,
        name: String,
        description: String,
        kind: CompanionSpellKind,
        icon: String,
        chancePercent: Int,
        power: Float
    ) = CompanionSpell(id, name, description, kind, icon, chancePercent, power)
}
