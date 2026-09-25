package com.lernhero.home.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.TextUnit
import com.lernhero.shared.theme.StrokedText

/** Gruby napis z ciemnym obrysem - "growy" naglowek i napis na przyciskach, ten sam co w sklepie. */
@Composable
fun OutlinedText(
    text: String,
    size: TextUnit,
    color: Color,
    modifier: Modifier = Modifier,
    outline: Color = Color(0xFF1A1208),
    strokeWidth: Float = 6f,
    maxLines: Int = Int.MAX_VALUE
) = StrokedText(text, size, color, outline, modifier, strokeWidth, maxLines)
