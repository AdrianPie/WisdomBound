package com.lernhero.home.settings

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lernhero.shared.theme.FontFirst
import com.lernhero.shared.theme.FontPixel

/**
 * Klocki, z ktorych zlozony jest ekran Ustawien - i tylko on.
 *
 * Siedza w osobnym pliku, bo ekran ma juz cztery sekcje i kazda kolejna preferencja ma byc
 * DOPISANIEM JEDNEJ LINIJKI w [SettingsScreen], a nie kolejna kopia ramki, tytulu i przelacznika.
 * Gdy ktorys z tych klockow przyda sie poza Ustawieniami, jego miejsce jest w `:shared`.
 */

internal val Ink = Color(0xFF120E0A)
internal val Panel = Color(0xFF19120C)
internal val Frame = Color(0xFFC8A45A)
internal val FrameDim = Color(0xFF6B5A38)
internal val Cream = Color(0xFFF4E9D7)
internal val Muted = Color(0xFF9D8B6A)
internal val Gold = Color(0xFFFFD34D)

/** Ramka z tytulem. Wszystko, co ekran pokazuje, mieszka w ktorejs z nich. */
@Composable
internal fun SettingsSection(
    title: String,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Panel)
            .border(2.dp, Frame.copy(alpha = 0.7f), RoundedCornerShape(14.dp))
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(
            text = title.uppercase(),
            color = Gold,
            fontFamily = FontPixel(),
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp
        )
        content()
    }
}

/** Zdanie wyjasniajace pod tytulem albo pod wierszem - male, przygaszone, nigdy klikalne. */
@Composable
internal fun SettingsHint(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        color = Muted,
        fontFamily = FontFirst(),
        fontSize = 10.sp,
        modifier = modifier
    )
}

/**
 * Wiersz z przelacznikiem. [enabled] = false przygasza CALY wiersz i odcina tapniecie - tak
 * wygladaja przelaczniki rodzajow przypomnien, dopoki nie ma zgody systemu: widac, ze istnieja,
 * i widac, ze dzis nic nie zrobia.
 */
@Composable
internal fun SettingsToggleRow(
    label: String,
    hint: String?,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    enabled: Boolean = true
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .alpha(if (enabled) 1f else 0.45f)
            .clip(RoundedCornerShape(10.dp))
            .clickable(enabled = enabled) { onCheckedChange(!checked) }
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = label,
                color = Cream,
                fontFamily = FontFirst(),
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp
            )
            if (hint != null) SettingsHint(hint)
        }
        PixelSwitch(checked = checked)
    }
}

/** Przelacznik rysowany recznie - Material3 Switch wyglada tu jak wklejka z innej gry. */
@Composable
private fun PixelSwitch(checked: Boolean) {
    val track by animateColorAsState(if (checked) Gold.copy(alpha = 0.35f) else Ink)
    val knobColor by animateColorAsState(if (checked) Gold else FrameDim)
    val knobOffset by animateDpAsState(if (checked) 18.dp else 2.dp)

    Box(
        modifier = Modifier
            .width(40.dp)
            .height(22.dp)
            .clip(RoundedCornerShape(11.dp))
            .background(track)
            .border(
                width = 1.dp,
                color = if (checked) Gold else FrameDim.copy(alpha = 0.7f),
                shape = RoundedCornerShape(11.dp)
            )
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .offset(x = knobOffset)
                .size(18.dp)
                .clip(CircleShape)
                .background(knobColor)
        )
    }
}

/**
 * Wiersz wyboru jednej wartosci z kilku - jezyk, tempo walki, godzina. Kafle sa na tyle male,
 * ze mieszcza sie w rzedzie; przy dluzszych listach lepszym klockiem bedzie osobny wiersz na opcje
 * (tak jak [SettingsOptionRow] dla jezyka).
 */
@Composable
internal fun <T> SettingsChoiceRow(
    options: List<T>,
    selected: T,
    label: (T) -> String,
    onSelect: (T) -> Unit,
    enabled: Boolean = true
) {
    Row(
        modifier = Modifier.fillMaxWidth().alpha(if (enabled) 1f else 0.45f),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        options.forEach { option ->
            val active = option == selected
            Text(
                text = label(option),
                color = if (active) Ink else Cream,
                fontFamily = FontFirst(),
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (active) Gold else Ink)
                    .border(
                        width = 1.dp,
                        color = if (active) Gold else FrameDim.copy(alpha = 0.6f),
                        shape = RoundedCornerShape(10.dp)
                    )
                    .clickable(enabled = enabled) { onSelect(option) }
                    .padding(vertical = 10.dp),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}

/** Pelnej szerokosci wiersz wyboru z ptaszkiem - dla list, ktore maja rosnac (jezyki). */
@Composable
internal fun SettingsOptionRow(label: String, selected: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(if (selected) Color(0xFF2A1F14) else Ink)
            .border(
                width = if (selected) 2.dp else 1.dp,
                color = if (selected) Gold else FrameDim.copy(alpha = 0.6f),
                shape = RoundedCornerShape(10.dp)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            color = if (selected) Gold else Cream,
            fontFamily = FontFirst(),
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp
        )
        if (selected) Text(text = "✓", color = Gold, fontSize = 14.sp)
    }
}

/** Przycisk akcji. Jeden wyglad na zgode na powiadomienia i na przywracanie domyslnych. */
@Composable
internal fun SettingsButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    filled: Boolean = true
) {
    Text(
        text = text.uppercase(),
        color = if (filled) Color(0xFF120C08) else Cream,
        fontFamily = FontFirst(),
        fontWeight = FontWeight.Bold,
        fontSize = 12.sp,
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(if (filled) Gold else Ink)
            .border(1.dp, if (filled) Gold else FrameDim.copy(alpha = 0.7f), RoundedCornerShape(10.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 10.dp)
    )
}
