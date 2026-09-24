package com.lernhero.home.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.TextUnit
import com.lernhero.shared.theme.FontFirst

/**
 * Gruby napis z ciemnym obrysem - "growy" naglowek i napis na przyciskach, ten sam co w sklepie.
 * Obrys to drugi Text narysowany pod spodem stylem Stroke, wiec nie zalezy od cienia platformy.
 */
@Composable
fun OutlinedText(
    text: String,
    size: TextUnit,
    color: Color,
    modifier: Modifier = Modifier,
    outline: Color = Color(0xFF1A1208),
    strokeWidth: Float = 6f,
    maxLines: Int = Int.MAX_VALUE
) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Text(
            text = text,
            color = outline,
            fontFamily = FontFirst(),
            fontWeight = FontWeight.Black,
            fontSize = size,
            textAlign = TextAlign.Center,
            maxLines = maxLines,
            style = TextStyle(drawStyle = Stroke(width = strokeWidth, join = StrokeJoin.Round))
        )
        Text(
            text = text,
            color = color,
            fontFamily = FontFirst(),
            fontWeight = FontWeight.Black,
            fontSize = size,
            textAlign = TextAlign.Center,
            maxLines = maxLines
        )
    }
}
