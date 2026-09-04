package com.lernhero.shared.resources

import androidx.compose.ui.graphics.TransformOrigin

/**
 * Punkty zaczepienia efektow walki na pojedynczym sprite'cie (monster albo hero), jako ulamkowe
 * wspolrzedne 0..1 na canvasie assetu - dokladnie ten sam koncept co [Resources.KnightPivots],
 * tylko dla warstwy efektow zamiast rigu konczyn.
 */
data class CombatAnchorSet(
    /** Pivot przy stopach - wokol niego kolysze sie caly sprite w idle. */
    val root: TransformOrigin,
    /** Ogolny punkt spelli/buffow/debuffow (zwykle tors). */
    val effectAnchor: TransformOrigin,
    /** Trafienie melee ORAZ punkt docelowy nadlatujacych pociskow. */
    val hitPoint: TransformOrigin,
    /** Skad wychodzi wlasny pocisk tej postaci (dlon/luk/pysk). */
    val projectileOrigin: TransformOrigin,
    /** Ikonka statusu (poison/buff/debuff) - zwykle nad glowa. */
    val statusEffectAnchor: TransformOrigin,
    /** Pasek HP - tuz nad glowa, dostrajany w "Monster Anchor Lab" tak samo jak pozostale anchory. */
    val hpBarAnchor: TransformOrigin = statusEffectAnchor
)

/**
 * Startowe wartosci per gatunek - szacowane na oko z grafiki, do dostrojenia wizualnie w
 * "Monster Anchor Lab" (feature/home/.../dev/MonsterAnchorLabScreen.kt), tym samym flow co
 * [Resources.KnightPivots] przy rycerzu.
 */
object MonsterAnchors {
    val skeletonWarrior = CombatAnchorSet(
        root = TransformOrigin(0.5f, 0.90f),
        effectAnchor = TransformOrigin(0.5f, 0.45f),
        hitPoint = TransformOrigin(0.614f, 0.66f),
        projectileOrigin = TransformOrigin(0.60f, 0.35f),
        statusEffectAnchor = TransformOrigin(0.5f, 0.08f)
    )

    val skeletonArcher = CombatAnchorSet(
        root = TransformOrigin(0.5f, 0.90f),
        effectAnchor = TransformOrigin(0.5f, 0.45f),
        hitPoint = TransformOrigin(0.535f, 0.712f),
        projectileOrigin = TransformOrigin(0.15f, 0.35f),
        statusEffectAnchor = TransformOrigin(0.5f, 0.08f)
    )

    val skeletonMage = CombatAnchorSet(
        root = TransformOrigin(0.5f, 0.90f),
        effectAnchor = TransformOrigin(0.5f, 0.45f),
        hitPoint = TransformOrigin(0.508f, 0.702f),
        projectileOrigin = TransformOrigin(0.72f, 0.50f),
        statusEffectAnchor = TransformOrigin(0.5f, 0.08f)
    )

    val orcWarrior = CombatAnchorSet(
        root = TransformOrigin(0.5f, 0.90f),
        effectAnchor = TransformOrigin(0.5f, 0.45f),
        hitPoint = TransformOrigin(0.519f, 0.686f),
        projectileOrigin = TransformOrigin(0.30f, 0.28f),
        statusEffectAnchor = TransformOrigin(0.5f, 0.08f)
    )

    val orcArcher = CombatAnchorSet(
        root = TransformOrigin(0.5f, 0.90f),
        effectAnchor = TransformOrigin(0.5f, 0.45f),
        hitPoint = TransformOrigin(0.573f, 0.732f),
        projectileOrigin = TransformOrigin(0.12f, 0.35f),
        statusEffectAnchor = TransformOrigin(0.5f, 0.08f)
    )

    val humanArcher = CombatAnchorSet(
        root = TransformOrigin(0.5f, 0.90f),
        effectAnchor = TransformOrigin(0.5f, 0.45f),
        hitPoint = TransformOrigin(0.527f, 0.686f),
        projectileOrigin = TransformOrigin(0.15f, 0.32f),
        statusEffectAnchor = TransformOrigin(0.5f, 0.08f)
    )

    val bossSkeletonDragon = CombatAnchorSet(
        root = TransformOrigin(0.5f, 0.85f),
        effectAnchor = TransformOrigin(0.5f, 0.50f),
        hitPoint = TransformOrigin(0.519f, 0.686f),
        projectileOrigin = TransformOrigin(0.12f, 0.35f),
        statusEffectAnchor = TransformOrigin(0.35f, 0.15f)
    )

    fun forSpecies(key: String): CombatAnchorSet = when (key) {
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
