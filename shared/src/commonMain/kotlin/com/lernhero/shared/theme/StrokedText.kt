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
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextLayoutResult
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
    maxLines: Int = Int.MAX_VALUE
) {
    var layout by remember { mutableStateOf<TextLayoutResult?>(null) }
    Text(
        text = text,
        color = color,
        fontFamily = FontFirst(),
        fontWeight = FontWeight.Black,
        fontSize = size,
        textAlign = TextAlign.Center,
        maxLines = maxLines,
        onTextLayout = { layout = it },
        modifier = modifier.drawBehind {
            if (strokeWidth > 0f) {
                layout?.let {
                    drawText(it, color = outline, drawStyle = Stroke(width = strokeWidth, join = StrokeJoin.Round))
                }
            }
        }
    )
}
