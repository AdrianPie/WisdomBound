package com.lernhero.game.character

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.lernhero.game.battle.effects.LayeredEffectCatalog
import com.lernhero.game.battle.effects.LayeredEffectView
import com.lernhero.game.spritesheet.SpriteSheetFrame
import com.lernhero.game.spritesheet.frameForProgress
import org.jetbrains.compose.resources.painterResource
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

/**
 * Rysuje potwora z [MonsterRig] w dowolnym momencie [MonsterAnimationClip] - jeden, wspolny
 * renderer dla laba animacji i (docelowo) dla walki. Zero logiki "co teraz gramy": wolajacy podaje
 * gotowy [progress] 0..1, a ten composable tylko wystawia odpowiednia klatke.
 *
 * Wszystkie czesci rysowane sa w TYM SAMYM pudelku (kazdy PNG ma ten sam canvas), wiec czesc
 * wycieta z oryginalu siedzi sama z siebie na wlasciwym miejscu - nie trzeba jej pozycjonowac,
 * wystarczy podac jej pivot.
 *
 * Przesuniecia i rozmiary w [Keyframe]/[ClipEffect] sa ulamkami rozmiaru sprite'a, wiec ta sama
 * animacja wyglada identycznie na podgladzie 1x i na bossie powiekszonym 3x - to jest cala
 * odpowiedz na "zeby potem sie to nie gryzlo" przy skalowaniu potwora.
 */
@Composable
fun RiggedMonster(
    rig: MonsterRig,
    preset: MonsterVisualPreset,
    clip: MonsterAnimationClip?,
    progress: Float,
    modifier: Modifier = Modifier,
    sizeDp: Dp = preset.baseSizeDp,
    /** Art potworow jest rysowany patrzac w LEWO - po stronie gracza odbijamy (jak [MonsterCharacter]). */
    isEnemySide: Boolean = true,
    /** Pozwala sylwetce wyjsc poza constrainty rodzica - patrz [MonsterCharacter]. */
    unbounded: Boolean = false,
    /** Rysuje krzyzyk w pivocie tej czesci - uzywane tylko przez lab, w grze zawsze null. */
    debugPivotPartId: String? = null,
    /**
     * Nadpisuje kolor WSZYSTKICH efektow warstwowych klipu (null = kolor z samego [ClipEffect]).
     * Walka podaje tu kolor pasujacy do sytuacji - patrz [CharacterUiModel.animEffectTintArgb].
     */
    effectTintOverride: Color? = null
) {
    val boxSize = sizeDp * preset.visualScale
    val facing = preset.facing(isEnemySide)

    Box(
        modifier = modifier.let { if (unbounded) it.requiredSize(boxSize) else it.size(boxSize) },
        contentAlignment = Alignment.BottomCenter
    ) {
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val widthPx = with(LocalDensity.current) { maxWidth.toPx() }
            val heightPx = with(LocalDensity.current) { maxHeight.toPx() }

            rig.parts.forEach { part ->
                val pose = clip?.tracks?.firstOrNull { it.partId == part.id }?.poseAt(progress) ?: PartPose()

                Image(
                    painter = painterResource(part.drawable),
                    contentDescription = null,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer {
                            transformOrigin = part.pivot
                            rotationZ = pose.rotationDeg
                            // Odbicie calej sylwetki mnozy sie przez skale czesci - dzieki temu
                            // companion (odbity) animuje sie tak samo jak wrog, tylko lustrzanie.
                            scaleX = facing * pose.scale
                            scaleY = pose.scale
                            alpha = pose.alpha
                            translationX = pose.offsetX * widthPx * facing
                            translationY = pose.offsetY * heightPx
                        }
                )
            }

            clip?.activeEffectsAt(progress)?.forEach { (effect, localProgress) ->
                ClipEffectLayer(
                    effect = effect,
                    localProgress = localProgress,
                    rig = rig,
                    clip = clip,
                    clipProgress = progress,
                    clipDurationMs = clip.durationMs,
                    widthPx = widthPx,
                    heightPx = heightPx,
                    facing = facing,
                    tintOverride = effectTintOverride
                )
            }

            if (debugPivotPartId != null) {
                rig.part(debugPivotPartId)?.let { part ->
                    PivotCross(
                        xPx = part.pivot.pivotFractionX * widthPx,
                        yPx = part.pivot.pivotFractionY * heightPx
                    )
                }
            }
        }
    }
}

/**
 * Jeden naklacany efekt w danej chwili. Gdy efekt jest przypiety do czesci ([ClipEffect.anchorPartId]),
 * jego punkt zaczepienia przechodzi przez DOKLADNIE te sama transformacje co ta czesc - kula ognia
 * wlozona w szczeke jedzie razem z jej obrotem, zamiast wisiec w powietrzu obok.
 */
@Composable
private fun ClipEffectLayer(
    effect: ClipEffect,
    localProgress: Float,
    rig: MonsterRig,
    clip: MonsterAnimationClip,
    clipProgress: Float,
    clipDurationMs: Int,
    widthPx: Float,
    heightPx: Float,
    facing: Float,
    tintOverride: Color? = null
) {
    var anchorXPx = effect.anchorX * widthPx
    var anchorYPx = effect.anchorY * heightPx

    val part = effect.anchorPartId?.let { rig.part(it) }
    if (part != null) {
        val pose = clip.tracks.firstOrNull { it.partId == part.id }?.poseAt(clipProgress) ?: PartPose()
        val pivotXPx = part.pivot.pivotFractionX * widthPx
        val pivotYPx = part.pivot.pivotFractionY * heightPx

        // p' = pivot + R(theta) * scale * (p - pivot) + translacja. Liczone w PIKSELACH, bo obrot
        // we wspolrzednych ulamkowych przekrzywialby efekt na kazdym niekwadratowym canvasie.
        val dx = anchorXPx - pivotXPx
        val dy = anchorYPx - pivotYPx
        val radians = pose.rotationDeg * PI_OVER_180
        val cos = cos(radians)
        val sin = sin(radians)

        anchorXPx = pivotXPx + (dx * cos - dy * sin) * pose.scale + pose.offsetX * widthPx
        anchorYPx = pivotYPx + (dx * sin + dy * cos) * pose.scale + pose.offsetY * heightPx
    }

    // Wlasna os czasu efektu: skala/krycie/obrot rosna niezaleznie od animacji potwora, wiec
    // "prawie niewidoczny na starcie, duzy na koncu" to zwykle dwie klatki na tej scieżce.
    val effectPose = effect.poseAt(localProgress)
    val sizePx = effect.sizeFraction * widthPx * effectPose.scale
    if (sizePx <= 0f || effectPose.alpha <= 0f) return

    val sizeInDp = with(LocalDensity.current) { sizePx.toDp() }

    // Odbicie po stronie gracza ma przesunac efekt na druga strone canvasu, tak samo jak sprite.
    val drawXPx = if (facing < 0f) widthPx - anchorXPx else anchorXPx

    val layered = LayeredEffectCatalog.byId(effect.effectKey)
    val sheet = if (layered == null) effectSheetFor(effect.effectKey) else null
    val drawable = if (layered == null) effectDrawableFor(effect.effectKey) else null
    if (layered == null && drawable == null) return

    Box(
        modifier = Modifier
            .offset {
                IntOffset(
                    x = (drawXPx - sizePx / 2f).roundToInt(),
                    y = (anchorYPx - sizePx / 2f).roundToInt()
                )
            }
            .size(sizeInDp)
            .graphicsLayer {
                rotationZ = effectPose.rotationDeg
                alpha = effectPose.alpha
            }
    ) {
        when {
            layered != null -> {
                // Kat obrotu warstw liczy sie z CZASU trwania efektu, a nie z narastajacego stanu -
                // dzieki temu przewijanie osi w labie pokazuje dokladnie te sama poze co odtwarzanie.
                val effectSeconds = (effect.to - effect.from) * clipDurationMs / 1000f
                LayeredEffectView(
                    spec = layered,
                    elapsedSeconds = localProgress * effectSeconds,
                    spinOverrides = effect.layerSpins,
                    tint = tintOverride ?: effect.tintArgb?.let { Color(it) },
                    modifier = Modifier.fillMaxSize()
                )
            }

            sheet != null -> SpriteSheetFrame(
                spec = sheet,
                frame = sheet.frameForProgress(localProgress),
                modifier = Modifier.fillMaxSize()
            )

            else -> Image(
                painter = painterResource(drawable!!),
                contentDescription = null,
                contentScale = ContentScale.Fit,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

/** Krzyzyk pokazujacy pivot edytowanej czesci - wylacznie na potrzeby laba. */
@Composable
private fun PivotCross(xPx: Float, yPx: Float) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val arm = 10.dp.toPx()
        val center = Offset(xPx, yPx)
        drawLine(
            color = Color(0xFF35E07A),
            start = center.copy(x = xPx - arm),
            end = center.copy(x = xPx + arm),
            strokeWidth = 2f
        )
        drawLine(
            color = Color(0xFF35E07A),
            start = center.copy(y = yPx - arm),
            end = center.copy(y = yPx + arm),
            strokeWidth = 2f
        )
        drawCircle(color = Color(0xFF35E07A), radius = arm * 0.35f, center = center, style = Stroke(width = 2f))
    }
}

private const val PI_OVER_180 = 0.017453292f
