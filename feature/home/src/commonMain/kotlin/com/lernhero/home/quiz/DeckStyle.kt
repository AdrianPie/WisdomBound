package com.lernhero.home.quiz

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lernhero.shared.theme.FontFirst
import com.lernhero.shared.theme.FontPixel

/**
 * Kawalki UI dzielone przez ekrany wlasnych talii. Te same kolory, co okna wyprawy
 * ([com.lernhero.home.fight.MaterialSheet]) - talia wybrana przed walka i talia otwarta w edytorze
 * maja byc ta sama rzecza, a nie dwoma osobnymi swiatami.
 */
internal val DeckInk = Color(0xFF120E0A)
internal val DeckPanel = Color(0xFF19120C)
internal val DeckFrame = Color(0xFFC8A45A)
internal val DeckFrameDim = Color(0xFF6B5A38)
internal val DeckCream = Color(0xFFF4E9D7)
internal val DeckMuted = Color(0xFF9D8B6A)
internal val DeckGold = Color(0xFFFFD34D)
internal val DeckAccent = Color(0xFFE0654F)
internal val DeckGreen = Color(0xFF4ADE80)
internal val DeckRed = Color(0xFFE56A5D)

private val DeckOutline = Color(0xFF120B05)

@Composable
internal fun DeckSectionLabel(text: String, modifier: Modifier = Modifier) {
    com.lernhero.home.ui.OutlinedText(text = text, size = 15.sp, color = DeckGold, strokeWidth = 5f, modifier = modifier)
}

/** Wybor z listy - aktywny swieci na niebiesko jak zakladki w reszcie gry. */
@Composable
internal fun DeckChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    androidx.compose.foundation.layout.Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(
                if (selected) androidx.compose.ui.graphics.Brush.verticalGradient(listOf(Color(0xFF5BB8FF), Color(0xFF2C6FC4)))
                else androidx.compose.ui.graphics.Brush.verticalGradient(listOf(Color(0xFF2A1E14), Color(0xFF17100A)))
            )
            .border(2.dp, DeckOutline, RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 8.dp)
    ) {
        if (selected) {
            com.lernhero.home.ui.OutlinedText(text = label, size = 13.sp, color = Color.White, strokeWidth = 4f)
        } else {
            Text(text = label, color = DeckCream, fontFamily = FontFirst(), fontWeight = FontWeight.Black, fontSize = 13.sp)
        }
    }
}

/**
 * Przycisk akcji w stylu gry: gradient z [color], gruby ciemny obrys, polysk u gory i napis
 * z obrysem. [enabled] = false przygasza go i odcina klikanie, zamiast go chowac.
 */
@Composable
internal fun DeckButton(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    color: Color = DeckAccent,
    textColor: Color = Color.White,
    enabled: Boolean = true,
    fontSize: Int = 14
) {
    val base = if (enabled) color else Color(0xFF4F483F)
    val top = Color(
        red = base.red + (1f - base.red) * 0.3f,
        green = base.green + (1f - base.green) * 0.3f,
        blue = base.blue + (1f - base.blue) * 0.3f
    )
    androidx.compose.foundation.layout.Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(androidx.compose.ui.graphics.Brush.verticalGradient(listOf(top, base)))
            .border(2.5.dp, DeckOutline, RoundedCornerShape(12.dp))
            .then(if (enabled) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(vertical = 11.dp, horizontal = 14.dp),
        contentAlignment = androidx.compose.ui.Alignment.Center
    ) {
        com.lernhero.home.ui.OutlinedText(
            text = label,
            size = fontSize.sp,
            color = if (enabled) textColor else DeckMuted,
            strokeWidth = 5f
        )
    }
}

/** Pole tekstowe w zlotej ramce - jedyny styl pisania w calej aplikacji poza tworzeniem postaci. */
@Composable
internal fun DeckField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    singleLine: Boolean = true,
    minLines: Int = 1
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        singleLine = singleLine,
        minLines = minLines,
        label = {
            Text(text = label, fontFamily = FontFirst(), fontSize = 11.sp)
        },
        textStyle = TextStyle(fontFamily = FontFirst(), fontSize = 13.sp),
        shape = RoundedCornerShape(10.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = DeckCream,
            unfocusedTextColor = DeckCream,
            focusedContainerColor = DeckInk,
            unfocusedContainerColor = DeckInk,
            focusedBorderColor = DeckGold,
            unfocusedBorderColor = DeckFrameDim,
            focusedLabelColor = DeckGold,
            unfocusedLabelColor = DeckMuted,
            cursorColor = DeckGold
        ),
        modifier = modifier
    )
}
