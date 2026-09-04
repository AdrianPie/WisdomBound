package com.lernhero.home.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lernhero.shared.i18n.AppLanguage
import com.lernhero.shared.i18n.AppLanguageStore
import com.lernhero.shared.i18n.strings
import com.lernhero.shared.theme.FontFirst
import com.lernhero.shared.theme.FontPixel
import org.koin.compose.koinInject

private val Ink = Color(0xFF120E0A)
private val Panel = Color(0xFF19120C)
private val Frame = Color(0xFFC8A45A)
private val FrameDim = Color(0xFF6B5A38)
private val Cream = Color(0xFFF4E9D7)
private val Muted = Color(0xFF9D8B6A)
private val Gold = Color(0xFFFFD34D)

/**
 * Ustawienia aplikacji. Jak dziś jedno pole - jezyk - ale to jest wlasciwe miejsce na kazda
 * przyszla preferencje URZADZENIA (nie konta gracza): dzwiek, powiadomienia, itp.
 *
 * Jezyk zmienia sie NATYCHMIAST po wybraniu kafla - [AppLanguageStore.current] jest stanem
 * Compose, wiec cala reszta apki (rowniez ten ekran) recomponuje sie od razu, bez restartu.
 */
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    insets: PaddingValues = PaddingValues(0.dp)
) {
    val languageStore = koinInject<AppLanguageStore>()

    Surface(modifier = Modifier.fillMaxSize(), color = Ink) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(insets)
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "‹",
                    color = Gold,
                    fontFamily = FontPixel(),
                    fontSize = 22.sp,
                    modifier = Modifier.clickable(onClick = onBack)
                )
                Text(
                    text = strings.settingsTitle.uppercase(),
                    color = Gold,
                    fontFamily = FontPixel(),
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(Panel)
                    .border(2.dp, Frame.copy(alpha = 0.7f), RoundedCornerShape(14.dp))
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = strings.settingsLanguage.uppercase(),
                    color = Gold,
                    fontFamily = FontPixel(),
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
                AppLanguage.entries.forEach { language ->
                    LanguageRow(
                        language = language,
                        selected = language == languageStore.current,
                        onClick = { languageStore.set(language) }
                    )
                }
                Text(
                    text = strings.settingsLanguageHint,
                    color = Muted,
                    fontFamily = FontFirst(),
                    fontSize = 10.sp
                )
            }
        }
    }
}

@Composable
private fun LanguageRow(language: AppLanguage, selected: Boolean, onClick: () -> Unit) {
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
            text = language.nativeName,
            color = if (selected) Gold else Cream,
            fontFamily = FontFirst(),
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp
        )
        if (selected) {
            Text(text = "✓", color = Gold, fontSize = 14.sp)
        }
    }
}
