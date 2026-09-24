package com.lernhero.home.settings

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lernhero.shared.theme.FontFirst
import com.lernhero.shared.theme.FontPixel

/**
 * Klocki ekranu Ustawien - i tylko jego.
 *
 * Uklad jak w porzadnych ustawieniach systemowych: maly naglowek grupy NAD karta, w karcie wiersze
 * (ikona w kafelku, tytul, podpis, cos po prawej) rozdzielone cienka linia. Kazda nowa preferencja
 * to jeden [SettingsRow] w [SettingsScreen] - nie kolejna ramka.
 */

internal val Ink = Color(0xFF110D09)
internal val Panel = Color(0xFF1C150F)
internal val PanelHi = Color(0xFF261C13)
internal val Divider = Color(0xFF2E2419)
internal val FrameDim = Color(0xFF4A3D29)
internal val Cream = Color(0xFFF4E9D7)
internal val Muted = Color(0xFF9D8B6A)
internal val Gold = Color(0xFFFFD34D)
internal val Danger = Color(0xFFFF6B5E)

/** Naglowek grupy i karta z wierszami. Wiersze same rysuja linie miedzy soba ([divider]). */
@Composable
internal fun SettingsGroup(
    title: String?,
    footer: String? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        if (title != null) {
            Text(
                text = title.uppercase(),
                color = Muted,
                fontFamily = FontPixel(),
                fontSize = 10.sp,
                letterSpacing = 1.sp,
                modifier = Modifier.padding(start = 6.dp)
            )
        }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(Brush.verticalGradient(listOf(PanelHi, Panel)))
                .border(1.dp, FrameDim.copy(alpha = 0.6f), RoundedCornerShape(16.dp)),
            content = content
        )
        if (footer != null) {
            Text(
                text = footer,
                color = Muted,
                fontFamily = FontFirst(),
                fontSize = 11.sp,
                lineHeight = 15.sp,
                modifier = Modifier.padding(horizontal = 6.dp)
            )
        }
    }
}

/**
 * Jeden wiersz karty. [trailing] to przelacznik, wartosc albo nic - wtedy przy [onClick] pojawia
 * sie strzalka. [below] to miejsce na kontrolke pelnej szerokosci pod tekstem (np. segmenty).
 */
@Composable
internal fun SettingsRow(
    icon: String,
    iconTint: Color,
    title: String,
    subtitle: String? = null,
    titleColor: Color = Cream,
    divider: Boolean = true,
    enabled: Boolean = true,
    onClick: (() -> Unit)? = null,
    trailing: (@Composable () -> Unit)? = null,
    below: (@Composable () -> Unit)? = null
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .alpha(if (enabled) 1f else 0.4f)
            .then(if (onClick != null) Modifier.clickable(enabled = enabled, onClick = onClick) else Modifier)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconTile(icon, iconTint)
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = title,
                        color = titleColor,
                        fontFamily = FontFirst(),
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (subtitle != null) {
                        Text(
                            text = subtitle,
                            color = Muted,
                            fontFamily = FontFirst(),
                            fontSize = 11.sp,
                            lineHeight = 14.sp
                        )
                    }
                }
                when {
                    trailing != null -> trailing()
                    onClick != null -> Text(text = "›", color = Muted, fontSize = 22.sp)
                }
            }
            below?.invoke()
        }
        if (divider) {
            Box(
                modifier = Modifier
                    .padding(start = 62.dp)
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(Divider)
            )
        }
    }
}

@Composable
private fun IconTile(icon: String, tint: Color) {
    Box(
        modifier = Modifier
            .size(34.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(tint.copy(alpha = 0.18f))
            .border(1.dp, tint.copy(alpha = 0.45f), RoundedCornerShape(10.dp)),
        contentAlignment = Alignment.Center
    ) {
        Text(text = icon, fontSize = 16.sp)
    }
}

/** Wartosc tekstowa po prawej stronie wiersza (np. numer wersji). */
@Composable
internal fun SettingsValue(text: String) {
    Text(text = text, color = Muted, fontFamily = FontFirst(), fontWeight = FontWeight.Bold, fontSize = 13.sp)
}

/** Przelacznik rysowany recznie - Material3 Switch wyglada tu jak wklejka z innej gry. */
@Composable
internal fun SettingsSwitch(checked: Boolean) {
    val track by animateColorAsState(if (checked) Gold else Color(0xFF2B2219))
    val knobOffset by animateDpAsState(if (checked) 22.dp else 3.dp)

    Box(
        modifier = Modifier
            .width(48.dp)
            .height(28.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(track)
            .border(1.dp, if (checked) Gold else FrameDim, RoundedCornerShape(14.dp))
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .offset(x = knobOffset)
                .size(22.dp)
                .clip(CircleShape)
                .background(if (checked) Color(0xFF2A1D08) else Muted)
        )
    }
}

/** Segmenty z przesuwanym podswietleniem - jezyk, tempo walki, godzina. */
@Composable
internal fun <T> SettingsSegmented(
    options: List<T>,
    selected: T,
    label: (T) -> String,
    onSelect: (T) -> Unit,
    enabled: Boolean = true
) {
    val index = options.indexOf(selected).coerceAtLeast(0)
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
            .height(38.dp)
            .alpha(if (enabled) 1f else 0.45f)
            .clip(RoundedCornerShape(11.dp))
            .background(Ink)
            .border(1.dp, FrameDim.copy(alpha = 0.7f), RoundedCornerShape(11.dp))
            .padding(3.dp)
    ) {
        val segment = maxWidth / options.size
        val offset by animateDpAsState(segment * index)
        Box(
            modifier = Modifier
                .offset(x = offset)
                .width(segment)
                .fillMaxHeight()
                .clip(RoundedCornerShape(8.dp))
                .background(Brush.verticalGradient(listOf(Color(0xFFFFE07A), Color(0xFFF2B928))))
        )
        Row(modifier = Modifier.fillMaxWidth().fillMaxHeight()) {
            options.forEachIndexed { i, option ->
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(8.dp))
                        .clickable(enabled = enabled) { onSelect(option) },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = label(option),
                        color = if (i == index) Color(0xFF2A1D08) else Cream,
                        fontFamily = FontFirst(),
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center,
                        maxLines = 1
                    )
                }
            }
        }
    }
}

/** Przycisk w wierszu (np. zgoda na powiadomienia). */
@Composable
internal fun SettingsPillButton(text: String, onClick: () -> Unit) {
    Text(
        text = text,
        color = Color(0xFF2A1D08),
        fontFamily = FontFirst(),
        fontWeight = FontWeight.Bold,
        fontSize = 12.sp,
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(Gold)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 8.dp)
    )
}
