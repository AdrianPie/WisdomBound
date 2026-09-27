package com.lernhero.shared.theme

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.TextUnit

/**
 * Gruby napis Roboto z ciemnym obrysem - wspolna baza "growych" napisow (naglowki, przyciski).
 *
 * Obrys rysujemy z TEGO SAMEGO ukladu tekstu co wypelnienie (drawBehind + drawText). Wczesniej
 * byl to drugi Text pod spodem, ale Roboto nie ma wagi Black i jest ona pogrubiana sztucznie -
 * obrys i wypelnienie ukladaly sie wtedy inaczej i napis wygladal na zdublowany.
 */
@Composable
fun StrokedText(
    text: String,
    size: TextUnit,
    color: Color,
    outline: Color,
    modifier: Modifier = Modifier,
    strokeWidth: Float = 6f,
    maxLines: Int = Int.MAX_VALUE,
    /**
     * Linia dokladnie na wysokosc liter, wysrodkowana - do liter w malych kolkach (ranga, licznik).
     * Bez tego tekst dziedziczy wysoka linie z motywu i siedzi wyraznie ponizej srodka.
     */
    tight: Boolean = false
) {
    var layout by remember { mutableStateOf<TextLayoutResult?>(null) }
    // Emoji walut w napisie ("REFILL 💎 30") rysujemy obrazkami - patrz CurrencyGlyph.
    val rich = remember(text) { currencyAnnotated(text) }
    val lineHeight = if (tight) size else TextUnit.Unspecified
    val style = if (tight) {
        TextStyle(lineHeightStyle = LineHeightStyle(LineHeightStyle.Alignment.Center, LineHeightStyle.Trim.Both))
    } else {
        TextStyle.Default
    }
    val strokeModifier = modifier.drawBehind {
        if (strokeWidth > 0f) {
            layout?.let {
                drawText(it, color = outline, drawStyle = Stroke(width = strokeWidth, join = StrokeJoin.Round))
                // Obrys i wypelnienie dziela jeden Paint ukladu tekstu, a Text rysujac wypelnienie
                // NIE ustawia stylu z powrotem - na starszych Androidach styl "obrys" przeciekal
                // i caly napis wychodzil grubym konturem w kolorze wypelnienia (biale kluchy,
                // bialy tekst na bialym tle). Jawne Fill przywraca pedzel przed wlasciwym tekstem.
                drawText(it, color = color, drawStyle = Fill)
            }
        }
    }
    if (rich == null) {
        Text(
            text = text,
            color = color,
            fontFamily = FontFirst(),
            fontWeight = FontWeight.Black,
            fontSize = size,
            textAlign = TextAlign.Center,
            maxLines = maxLines,
            lineHeight = lineHeight,
            style = style,
            onTextLayout = { layout = it },
            modifier = strokeModifier
        )
    } else {
        Text(
            text = rich.first,
            inlineContent = rich.second,
            color = color,
            fontFamily = FontFirst(),
            fontWeight = FontWeight.Black,
            fontSize = size,
            textAlign = TextAlign.Center,
            maxLines = maxLines,
            lineHeight = lineHeight,
            style = style,
            onTextLayout = { layout = it },
            modifier = strokeModifier
        )
    }
}
