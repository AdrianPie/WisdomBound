package com.lernhero.home.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lernhero.shared.theme.FontFirst
import com.lernhero.shared.theme.StrokedText

/**
 * Jezyk "karty postaci" dla zwyklych ekranow z trescia (Swiatynia, Biblioteka Medrca...).
 *
 * Wczesniej kazdy taki ekran mial wlasny fioletowo-zloty gradient, emoji w kazdym kafelku i
 * drobny liliowy tekst - wszystko wygladalo jak z generatora. Tu jest JEDEN zestaw: pergamin w
 * brazowej ramce (jak karta postaci), jasniejsze karty w srodku, ciemny rowek pod paski postepu
 * i ciemny tusz zamiast bialego tekstu z obrysem. Kolor pojawia sie tylko tam, gdzie cos znaczy
 * (dobrze / zle / nagroda), a nie jako tlo calego panelu.
 */
object PaperInk {
    val Ink = ParchmentPalette.Ink
    val Muted = ParchmentPalette.InkMuted
    val Frame = ParchmentPalette.Frame

    /** Kolory "znaczace", przyciemnione tak, zeby byly czytelne na kremowym papierze. */
    val Good = Color(0xFF3E8A2E)
    val Warn = Color(0xFFC08A1E)
    val Bad = Color(0xFFC0452F)
    val Gold = Color(0xFFD9A21E)
    val Blue = Color(0xFF3F79C2)
    val Purple = Color(0xFF7B4FC0)

    /** Rowek pod paskiem postepu i pierscieniem. */
    val Groove = Color(0xFF2A2118)
    val RingTrack = Color(0x334A3620)
}

/** Panel-pergamin: ciemna ramka, papier z gradientem, cienka jasna linia w srodku. */
@Composable
fun PaperCard(
    modifier: Modifier = Modifier,
    contentPadding: Dp = 12.dp,
    spacing: Dp = 10.dp,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(ParchmentPalette.Frame)
            .padding(3.dp)
            .clip(RoundedCornerShape(15.dp))
            .background(Brush.verticalGradient(listOf(ParchmentPalette.Paper, ParchmentPalette.PaperDark)))
            .border(1.5.dp, ParchmentPalette.FrameLight.copy(alpha = 0.6f), RoundedCornerShape(15.dp))
            .padding(contentPadding),
        verticalArrangement = Arrangement.spacedBy(spacing),
        content = content
    )
}

/** Tytul sekcji na papierze - ciemny tusz po lewej, opcjonalnie cos po prawej. */
@Composable
fun PaperTitle(text: String, trailing: (@Composable RowScope.() -> Unit)? = null) {
    Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 2.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = text,
            color = PaperInk.Ink,
            fontFamily = FontFirst(),
            fontWeight = FontWeight.Black,
            fontSize = 16.sp,
            lineHeight = 18.sp,
            modifier = Modifier.weight(1f)
        )
        trailing?.invoke(this)
    }
}

/** Karta w srodku panelu - jasniejsza od papieru, z cienka krawedzia. [accent] barwi krawedz. */
@Composable
fun PaperTile(
    modifier: Modifier = Modifier,
    accent: Color? = null,
    onClick: (() -> Unit)? = null,
    content: @Composable RowScope.() -> Unit
) {
    val shape = RoundedCornerShape(12.dp)
    Row(
        modifier = modifier
            .clip(shape)
            .background(
                if (accent != null) Brush.verticalGradient(listOf(ParchmentPalette.Card, accent.copy(alpha = 0.12f).compositeOver(ParchmentPalette.Card)))
                else Brush.verticalGradient(listOf(ParchmentPalette.Card, ParchmentPalette.Card))
            )
            .border(if (accent != null) 1.5.dp else 1.dp, accent?.copy(alpha = 0.7f) ?: ParchmentPalette.CardEdge, shape)
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(horizontal = 10.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically,
        content = content
    )
}

/** Kolko w kolorze znaczenia z jednym bialym znakiem (✓, ✕, ★...) - zamiast emoji w kafelkach. */
@Composable
fun GlyphBadge(glyph: String, color: Color, size: Dp = 34.dp, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(Brush.verticalGradient(listOf(color.lightenBy(0.3f), color, color.darkenBy(0.15f))))
            .border(1.5.dp, Color.White.copy(alpha = 0.75f), CircleShape),
        contentAlignment = Alignment.Center
    ) {
        StrokedText(
            text = glyph,
            size = (size.value * 0.46f).sp,
            color = Color.White,
            outline = color.darkenBy(0.45f),
            strokeWidth = 4f,
            tight = true
        )
    }
}

/** Mala etykieta NAD liczba (jak na karcie atrybutow): drukowane litery, przygaszony tusz. */
@Composable
fun PaperLabel(text: String, color: Color = PaperInk.Muted) {
    Text(
        text = text.uppercase(),
        color = color,
        fontFamily = FontFirst(),
        fontWeight = FontWeight.Bold,
        fontSize = 9.sp,
        lineHeight = 10.sp,
        letterSpacing = 0.6.sp,
        maxLines = 1
    )
}

/** Duza liczba ciemnym tuszem. */
@Composable
fun PaperValue(text: String, color: Color = PaperInk.Ink, size: Int = 20) {
    Text(
        text = text,
        color = color,
        fontFamily = FontFirst(),
        fontWeight = FontWeight.Black,
        fontSize = size.sp,
        lineHeight = (size + 2).sp,
        maxLines = 1
    )
}

/**
 * Pasek postepu w ciemnym rowku (jak pasek XP na karcie postaci). Przy [label] i wysokosci od
 * 16 dp napis siedzi na srodku paska.
 */
@Composable
fun GrooveBar(
    progress: Float,
    fill: Color,
    modifier: Modifier = Modifier,
    height: Dp = 10.dp,
    label: String? = null
) {
    val animated by animateFloatAsState(progress.coerceIn(0f, 1f), tween(500), label = "groove")
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .clip(RoundedCornerShape(50))
            .background(PaperInk.Groove)
            .border(1.5.dp, Color(0xFF1A140E), RoundedCornerShape(50))
            .padding(1.5.dp),
        contentAlignment = Alignment.Center
    ) {
        if (animated > 0f) {
            Box(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .fillMaxWidth(animated)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(50))
                    .background(Brush.verticalGradient(listOf(fill.lightenBy(0.35f), fill, fill.darkenBy(0.2f))))
            )
        }
        if (label != null) {
            StrokedText(text = label, size = 10.sp, color = Color.White, outline = Color(0xFF1A140E), strokeWidth = 4f, tight = true)
        }
    }
}

/**
 * Gruby przycisk z "wargą" pod spodem - wyglada na wciskany, jak w grach mobilnych, a nie jak
 * plaski prostokat z gradientem.
 */
@Composable
fun ChunkyButton(
    text: String,
    color: Color,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    textSize: Int = 13,
    onClick: () -> Unit,
    below: (@Composable () -> Unit)? = null
) {
    val face = if (enabled) color else Color(0xFFA89A82)
    val shape = RoundedCornerShape(11.dp)
    Box(
        modifier = modifier
            .clip(shape)
            .background(face.darkenBy(0.35f))
            .padding(bottom = 3.dp)
            .clip(shape)
            .background(Brush.verticalGradient(listOf(face.lightenBy(0.25f), face)))
            .border(1.5.dp, face.darkenBy(0.45f), shape)
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 7.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            StrokedText(text = text, size = textSize.sp, color = Color.White, outline = face.darkenBy(0.55f), strokeWidth = 5f, maxLines = 1)
            below?.invoke()
        }
    }
}

/**
 * Zakladki segmentowe: ciemne drewno, aktywna zakladka to zolty "guzik" z ciemnym tuszem -
 * te same kolory co przyciski na karcie postaci, zamiast niebieskiego i bezowego naraz.
 */
@Composable
fun PaperTabs(labels: List<String>, selected: Int, onSelect: (Int) -> Unit, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Brush.verticalGradient(listOf(Color(0xFF5A3D22), Color(0xFF3E2A17))))
            .border(2.dp, Color(0xFF2A1C0F), RoundedCornerShape(14.dp))
            .padding(3.dp),
        horizontalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        labels.forEachIndexed { index, label ->
            val active = index == selected
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(11.dp))
                    .then(
                        if (active) Modifier
                            .background(Brush.verticalGradient(listOf(Color(0xFFFFE08A), ParchmentPalette.Button)))
                            .border(1.5.dp, ParchmentPalette.ButtonEdge, RoundedCornerShape(11.dp))
                        else Modifier
                    )
                    .clickable { onSelect(index) }
                    .padding(vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = label,
                    color = if (active) PaperInk.Ink else ParchmentPalette.Paper.copy(alpha = 0.8f),
                    fontFamily = FontFirst(),
                    fontWeight = FontWeight.Black,
                    fontSize = 12.sp,
                    lineHeight = 14.sp,
                    maxLines = 1
                )
            }
        }
    }
}

/** Mala liczba w pigulce przy tytule na niebie ("Your Tomes 2") - ciemna, jak liczniki na gorze. */
@Composable
fun CountChip(text: String) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(Color(0xE61A130C))
            .border(1.5.dp, ParchmentPalette.Frame, RoundedCornerShape(50))
            .padding(horizontal = 9.dp, vertical = 2.dp)
    ) {
        Text(text = text, color = Color(0xFFFFE08A), fontFamily = FontFirst(), fontWeight = FontWeight.Black, fontSize = 12.sp, lineHeight = 14.sp)
    }
}

fun Color.lightenBy(factor: Float): Color =
    Color(red + (1f - red) * factor, green + (1f - green) * factor, blue + (1f - blue) * factor, alpha)

fun Color.darkenBy(factor: Float): Color =
    Color(red * (1f - factor), green * (1f - factor), blue * (1f - factor), alpha)

private fun Color.compositeOver(background: Color): Color {
    val a = alpha
    return Color(
        red * a + background.red * (1f - a),
        green * a + background.green * (1f - a),
        blue * a + background.blue * (1f - a),
        1f
    )
}
