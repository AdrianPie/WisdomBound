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
 * Kawalki UI dzielone przez ekrany wlasnych talii. Te same kolory, co panel endless
 * ([com.lernhero.home.story.EndlessSetupPanel]) - talie sa jego przedluzeniem, nie osobnym swiatem.
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

@Composable
internal fun DeckSectionLabel(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text.uppercase(),
        color = DeckGold,
        fontFamily = FontPixel(),
        fontWeight = FontWeight.Bold,
        fontSize = 11.sp,
        modifier = modifier
    )
}

@Composable
internal fun DeckChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Text(
        text = label,
        color = if (selected) Color(0xFF0B0805) else DeckCream,
        fontFamily = FontFirst(),
        fontWeight = FontWeight.Bold,
        fontSize = 11.sp,
        modifier = modifier
            .clip(RoundedCornerShape(50))
            .background(if (selected) DeckGold else DeckInk)
            .border(
                width = 1.dp,
                color = if (selected) DeckGold else DeckFrameDim,
                shape = RoundedCornerShape(50)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 7.dp)
    )
}

/** Przycisk akcji. [enabled] = false przygasza go i odcina klikanie, zamiast go chowac. */
@Composable
internal fun DeckButton(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    color: Color = DeckAccent,
    textColor: Color = Color.White,
    enabled: Boolean = true,
    fontSize: Int = 13
) {
    Text(
        text = label,
        color = if (enabled) textColor else DeckMuted,
        fontFamily = FontPixel(),
        fontWeight = FontWeight.Bold,
        fontSize = fontSize.sp,
        textAlign = TextAlign.Center,
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(if (enabled) color else Color(0xFF241E18))
            .border(
                width = if (enabled) 2.dp else 1.dp,
                color = if (enabled) color.copy(alpha = 0.55f) else DeckFrameDim,
                shape = RoundedCornerShape(10.dp)
            )
            .then(if (enabled) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(vertical = 11.dp, horizontal = 12.dp)
    )
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
