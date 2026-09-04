package com.lernhero.game.battle.effects

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.unit.IntSize

/**
 * Miejsce i rozmiar jednej postaci na ekranie (globalne wspolrzedne root), zebrane przez
 * `onGloballyPositioned` na TYM SAMYM Boxie, ktory faktycznie renderuje [com.lernhero.game.character.MonsterCharacter]
 * / [com.lernhero.game.character.ModularKnight] - dzieki temu anchor fractions z [com.lernhero.shared.resources.CombatAnchorSet]
 * mozna bezposrednio przeliczyc na piksele.
 *
 * [flipped] mowi, czy sprite jest odbity (scaleX = -1) wzgledem strony formacji - odbicie nie
 * zmienia layoutowego rozmiaru/pozycji (graphicsLayer maluje tylko), wiec trzeba osobno odbic
 * fractionX przy przeliczaniu anchoru.
 */
data class CharacterRect(
    val topLeft: Offset,
    val size: IntSize,
    val flipped: Boolean = false
) {
    fun resolve(anchor: TransformOrigin): Offset {
        val xFraction = if (flipped) 1f - anchor.pivotFractionX else anchor.pivotFractionX
        return Offset(
            x = topLeft.x + size.width * xFraction,
            y = topLeft.y + size.height * anchor.pivotFractionY
        )
    }
}
