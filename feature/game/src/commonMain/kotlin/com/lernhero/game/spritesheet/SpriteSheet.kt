package com.lernhero.game.spritesheet

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.imageResource

/**
 * Opis jednego spritesheeta: siatka rownych klatek w jednym PNG. Ogolny, wielokrotnego uzytku
 * opis - nie wie nic o efektach walki, animacjach postaci ani niczym innym konkretnym, zeby dalo
 * sie go uzyc wszedzie tam, gdzie ktos wrzuci kolejny spritesheet (efekty, w przyszlosci moze
 * animacje postaci itp).
 *
 * Rozmiar pojedynczej klatki w pikselach NIE jest tu wpisany na sztywno - [SpriteSheetFrame]
 * liczy go sam z rzeczywistego rozmiaru zaladowanego PNG podzielonego przez [columns]/[rows].
 * Dzieki temu klatki moga byc kwadratowe albo nie (np. wybuch szerszy niz wyzszy) i nic sie nie
 * psuje, jesli ktos wyeksportuje sheet o innym rozmiarze niz poprzedni.
 *
 * [frameCount] moze byc mniejszy niz `columns * rows` (ostatnie pola siatki puste/duplikat -
 * typowy eksport ze spritesheet-generatora, ktory dopelnia niepelny wiersz) - wtedy po prostu
 * nie odtwarzamy tych nadmiarowych pol.
 */
data class SpriteSheetSpec(
    val drawable: DrawableResource,
    val frameCount: Int,
    val columns: Int,
    val rows: Int = (frameCount + columns - 1) / columns
)

/** Ktora klatka odpowiada danemu postepowi 0..1 - liniowe mapowanie na cala dlugosc animacji. */
fun SpriteSheetSpec.frameForProgress(progress: Float): Int =
    (progress.coerceIn(0f, 1f) * (frameCount - 1)).toInt().coerceIn(0, frameCount - 1)

/**
 * Rysuje jedna klatke [frame] z [spec] w danym [modifier] (ten okresla docelowy rozmiar - PNG
 * jest skalowany do niego, nie odwrotnie). Uzyj [frameForProgress], zeby zmapowac postep 0..1
 * animacji na indeks klatki.
 */
@Composable
fun SpriteSheetFrame(
    spec: SpriteSheetSpec,
    frame: Int,
    modifier: Modifier = Modifier,
    alpha: Float = 1f,
    colorFilter: ColorFilter? = null
) {
    val sheet = imageResource(spec.drawable)
    val frameWidthPx = sheet.width / spec.columns
    val frameHeightPx = sheet.height / spec.rows
    val clamped = frame.coerceIn(0, spec.frameCount - 1)
    val col = clamped % spec.columns
    val row = clamped / spec.columns

    Canvas(modifier = modifier) {
        drawImage(
            image = sheet,
            srcOffset = IntOffset(col * frameWidthPx, row * frameHeightPx),
            srcSize = IntSize(frameWidthPx, frameHeightPx),
            dstSize = IntSize(size.width.toInt(), size.height.toInt()),
            alpha = alpha,
            colorFilter = colorFilter
        )
    }
}
