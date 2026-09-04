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
    val visualScale: Float = 1f
)

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

    fun forSpecies(key: String): MonsterVisualPreset = when (key) {
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
