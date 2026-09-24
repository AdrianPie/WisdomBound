package com.lernhero.game.character

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.lernhero.shared.resources.CombatAnchorSet
import com.lernhero.shared.resources.MonsterAnchors
import com.lernhero.shared.resources.Resources
import org.jetbrains.compose.resources.DrawableResource

/**
 * Preset wizualny jednego gatunku potwora - pojedynczy PNG (bez sprite-sheeta), jego anchory
 * efektow ([CombatAnchorSet]) i bazowy rozmiar canvasu. Odpowiednik [Resources.KnightPivots] dla
 * hero, tylko ze jeden sprite zamiast rigu z osobnych czesci ciala.
 *
 * Art wszystkich siedmiu gatunkow jest narysowany patrzac w LEWO (tak jak [Resources.KnightPivots]
 * rycerz patrzy w prawo) - stad domyslnie NIE odbijamy ich po stronie wroga (formacja wrogow stoi
 * po prawej, patrzac w lewo w strone hero - dokladnie tak jak narysowane), a odbijamy dopiero gdy
 * ten sam gatunek staje po stronie gracza jako companion (patrzy wtedy w prawo, w strone wrogow).
 */
data class MonsterVisualPreset(
    val drawable: DrawableResource,
    val anchors: CombatAnchorSet,
    val baseSizeDp: Dp,
    /**
     * Kompensuje rozne "puste marginesy" wewnatrz kazdego PNG (jeden gatunek zajmuje 65% wlasnego
     * canvasu, inny 80%) - bez tego przy tym samym [baseSizeDp]/box'ie jeden gatunek wyglada
     * wyraznie mniejszy od drugiego. Zmierzone z realnych plikow (bounding box nieprzezroczystych
     * pikseli), 1f = bez korekty.
     */
    val visualScale: Float = 1f,
    /**
     * Grafika narysowana patrzac w PRAWO (druga fala kompanow). Walka odbija wtedy sprite odwrotnie
     * niz przy starym arcie - wrog i tak ma patrzec w lewo, sojusznik w prawo.
     */
    val facesRight: Boolean = false,
    /** Animacja spoczynku - oddech, falowanie, czasteczki. Patrz [IdleStyle]. */
    val idle: IdleStyle = IdleStyle.Default
) {
    /** Mnoznik scaleX: 1 = bez odbicia. Wrog patrzy w lewo, sojusznik w prawo - niezaleznie od artu. */
    fun facing(isEnemySide: Boolean): Float {
        val towardEnemy = if (isEnemySide) 1f else -1f
        return if (facesRight) -towardEnemy else towardEnemy
    }
}

object MonsterVisualPresets {
    val skeletonWarrior = MonsterVisualPreset(
        drawable = Resources.MonsterArt.skeletonWarrior,
        anchors = MonsterAnchors.skeletonWarrior,
        baseSizeDp = 100.dp,
        visualScale = 1.20f
    )
    val skeletonArcher = MonsterVisualPreset(
        drawable = Resources.MonsterArt.skeletonArcher,
        anchors = MonsterAnchors.skeletonArcher,
        baseSizeDp = 100.dp,
        visualScale = 1.03f
    )
    val skeletonMage = MonsterVisualPreset(
        drawable = Resources.MonsterArt.skeletonMage,
        anchors = MonsterAnchors.skeletonMage,
        baseSizeDp = 100.dp,
        visualScale = 1.15f
    )
    val orcWarrior = MonsterVisualPreset(
        drawable = Resources.MonsterArt.orcWarrior,
        anchors = MonsterAnchors.orcWarrior,
        baseSizeDp = 105.dp,
        visualScale = 1.10f
    )
    val orcArcher = MonsterVisualPreset(
        drawable = Resources.MonsterArt.orcArcher,
        anchors = MonsterAnchors.orcArcher,
        baseSizeDp = 105.dp,
        visualScale = 1.02f
    )
    val humanArcher = MonsterVisualPreset(
        drawable = Resources.MonsterArt.humanArcher,
        anchors = MonsterAnchors.humanArcher,
        baseSizeDp = 100.dp,
        visualScale = 1.03f
    )
    val bossSkeletonDragon = MonsterVisualPreset(
        drawable = Resources.MonsterArt.bossSkeletonDragon,
        anchors = MonsterAnchors.bossSkeletonDragon,
        baseSizeDp = 220.dp
    )

    /**
     * Druga fala: anchory liczone z prawdziwej sylwetki kazdego PNG (gora i wysokosc zmierzone przy
     * przycinaniu grafik), a rozmiar z klasy S/M/L/XL - wielkie potwory sa wieksze, ale nie 8x.
     */
    private fun secondWavePreset(key: String, top: Float, height: Float, scale: Float): MonsterVisualPreset {
        val drawable = Resources.MonsterArt.secondWave.getValue(key)
        val bottom = top + height
        return MonsterVisualPreset(
            drawable = drawable,
            anchors = com.lernhero.shared.resources.CombatAnchorSet(
                root = androidx.compose.ui.graphics.TransformOrigin(0.5f, bottom),
                effectAnchor = androidx.compose.ui.graphics.TransformOrigin(0.5f, top + height * 0.5f),
                hitPoint = androidx.compose.ui.graphics.TransformOrigin(0.5f, top + height * 0.58f),
                // Art patrzy w prawo - pocisk wychodzi z prawej strony sylwetki (odbicie robi walka).
                projectileOrigin = androidx.compose.ui.graphics.TransformOrigin(0.68f, top + height * 0.38f),
                statusEffectAnchor = androidx.compose.ui.graphics.TransformOrigin(0.5f, (top - 0.03f).coerceAtLeast(0.02f))
            ),
            baseSizeDp = 100.dp,
            visualScale = scale,
            facesRight = true,
            idle = MonsterIdleStyles.byKey[key] ?: IdleStyle.Default
        )
    }

    private val secondWave: Map<String, MonsterVisualPreset> = mapOf(
        "ember_imp" to secondWavePreset("ember_imp", top = 0.08f, height = 0.801f, scale = 0.9f),  // S
        "fire_bat" to secondWavePreset("fire_bat", top = 0.43f, height = 0.451f, scale = 0.9f),  // S
        "lava_slime" to secondWavePreset("lava_slime", top = 0.256f, height = 0.625f, scale = 0.9f),  // S
        "goblin_spearman" to secondWavePreset("goblin_spearman", top = 0.08f, height = 0.801f, scale = 1.1f),  // M
        "mushroom_sprite" to secondWavePreset("mushroom_sprite", top = 0.109f, height = 0.771f, scale = 0.9f),  // S
        "tidecrawler" to secondWavePreset("tidecrawler", top = 0.154f, height = 0.727f, scale = 0.9f),  // S
        "bubble_slime" to secondWavePreset("bubble_slime", top = 0.209f, height = 0.672f, scale = 0.9f),  // S
        "murloc" to secondWavePreset("murloc", top = 0.107f, height = 0.773f, scale = 0.9f),  // S
        "zombie" to secondWavePreset("zombie", top = 0.08f, height = 0.801f, scale = 1.1f),  // M
        "ghost" to secondWavePreset("ghost", top = 0.08f, height = 0.801f, scale = 0.9f),  // S
        "hellhound" to secondWavePreset("hellhound", top = 0.199f, height = 0.682f, scale = 1.1f),  // M
        "flame_cultist" to secondWavePreset("flame_cultist", top = 0.08f, height = 0.801f, scale = 1.1f),  // M
        "forest_fox" to secondWavePreset("forest_fox", top = 0.146f, height = 0.734f, scale = 0.9f),  // S
        "treant_sapling" to secondWavePreset("treant_sapling", top = 0.08f, height = 0.801f, scale = 1.1f),  // M
        "elf_ranger" to secondWavePreset("elf_ranger", top = 0.08f, height = 0.801f, scale = 1.1f),  // M
        "boar_rider" to secondWavePreset("boar_rider", top = 0.08f, height = 0.801f, scale = 1.1f),  // M
        "frost_wolf" to secondWavePreset("frost_wolf", top = 0.193f, height = 0.688f, scale = 1.1f),  // M
        "ice_archer" to secondWavePreset("ice_archer", top = 0.08f, height = 0.801f, scale = 1.1f),  // M
        "ghoul" to secondWavePreset("ghoul", top = 0.152f, height = 0.729f, scale = 1.1f),  // M
        "banshee" to secondWavePreset("banshee", top = 0.08f, height = 0.801f, scale = 1.1f),  // M
        "salamander" to secondWavePreset("salamander", top = 0.094f, height = 0.787f, scale = 1.1f),  // M
        "efreet" to secondWavePreset("efreet", top = 0.08f, height = 0.801f, scale = 1.25f),  // L
        "magma_golem" to secondWavePreset("magma_golem", top = 0.104f, height = 0.777f, scale = 1.25f),  // L
        "centaur_huntress" to secondWavePreset("centaur_huntress", top = 0.08f, height = 0.801f, scale = 1.25f),  // L
        "thorn_beast" to secondWavePreset("thorn_beast", top = 0.146f, height = 0.734f, scale = 1.25f),  // L
        "naga_siren" to secondWavePreset("naga_siren", top = 0.08f, height = 0.801f, scale = 1.25f),  // L
        "yeti" to secondWavePreset("yeti", top = 0.08f, height = 0.801f, scale = 1.25f),  // L
        "vampire" to secondWavePreset("vampire", top = 0.08f, height = 0.801f, scale = 1.1f),  // M
        "minotaur" to secondWavePreset("minotaur", top = 0.08f, height = 0.801f, scale = 1.25f),  // L
        "centaur" to secondWavePreset("centaur", top = 0.082f, height = 0.799f, scale = 1.25f),  // L
        "ancient_treant" to secondWavePreset("ancient_treant", top = 0.08f, height = 0.801f, scale = 1.4f),  // XL
        "frost_giant" to secondWavePreset("frost_giant", top = 0.08f, height = 0.801f, scale = 1.4f),  // XL
        "death_knight" to secondWavePreset("death_knight", top = 0.08f, height = 0.801f, scale = 1.25f),  // L
        "lich" to secondWavePreset("lich", top = 0.08f, height = 0.801f, scale = 1.4f),  // XL
        "phoenix" to secondWavePreset("phoenix", top = 0.08f, height = 0.801f, scale = 1.4f), // XL
    )

    fun forSpecies(key: String): MonsterVisualPreset = secondWave[key] ?: when (key) {
        "skeleton_warrior" -> skeletonWarrior
        "skeleton_archer" -> skeletonArcher
        "skeleton_mage" -> skeletonMage
        "orc_warrior" -> orcWarrior
        "orc_archer" -> orcArcher
        "human_archer" -> humanArcher
        "boss_skeleton_dragon" -> bossSkeletonDragon
        else -> skeletonWarrior
    }
}
