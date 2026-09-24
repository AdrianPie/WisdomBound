package com.lernhero.home.growth

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lernhero.home.fitness.FitnessScreen
import com.lernhero.reader.ReaderChrome
import com.lernhero.reader.ReadingScreen
import com.lernhero.shared.theme.AppPalette
import com.lernhero.shared.theme.FontFirst

private enum class GrowthTab(val label: String, val icon: String) {
    Books("Books", "📚"),
    Training("Training", "🏋️")
}

/**
 * Zakladka "Rozwoj": czytanie ksiazek i trening z urzadzenia w jednym miejscu - obie te rzeczy
 * robi sie NAPRAWDE, poza gra, i obie daja bonusy. W srodku dwie podzakladki.
 *
 * [tabInsets] to marginesy pod paski aplikacji (z HomeNavGraph). Gorny zjada tutejszy przelacznik
 * podzakladek, dolny idzie w dol do tresci. Gdy czytnik wchodzi na pelny ekran
 * ([ReaderChrome.immersive]), przelacznik znika i ksiazka dostaje caly ekran - tak samo jak
 * chowaja sie wtedy paski nawigacji.
 */
@Composable
fun GrowthScreen(
    tabInsets: PaddingValues,
    onOpenVocabulary: () -> Unit,
    onRewardClaimed: () -> Unit = {}
) {
    var tab by remember { mutableStateOf(GrowthTab.Books) }
    val immersive = ReaderChrome.immersive

    // Podzakladka dostaje tylko dolny margines - gorny zjadl przelacznik nad nia.
    val contentInsets = PaddingValues(bottom = tabInsets.calculateBottomPadding())

    Column(modifier = Modifier.fillMaxSize()) {
        if (!immersive) {
            GrowthTabBar(
                selected = tab,
                onSelect = { tab = it },
                modifier = Modifier.padding(
                    top = tabInsets.calculateTopPadding(),
                    start = 12.dp,
                    end = 12.dp,
                    bottom = 6.dp
                )
            )
        }

        Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
            when (tab) {
                GrowthTab.Books -> ReadingScreen(
                    libraryInsets = contentInsets,
                    onOpenVocabulary = onOpenVocabulary,
                    onRewardClaimed = onRewardClaimed
                )
                GrowthTab.Training -> FitnessScreen(insets = contentInsets)
            }
        }
    }
}

@Composable
private fun GrowthTabBar(
    selected: GrowthTab,
    onSelect: (GrowthTab) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        GrowthTab.entries.forEach { entry ->
            val isSelected = entry == selected
            Row(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        if (isSelected) androidx.compose.ui.graphics.Brush.verticalGradient(listOf(Color(0xFF5BB8FF), Color(0xFF2C7BD6)))
                        else androidx.compose.ui.graphics.Brush.verticalGradient(listOf(Color(0xFFE9D6B4), Color(0xFFC9AE84)))
                    )
                    .border(2.5.dp, if (isSelected) Color(0xFF123A6B) else Color(0xFF6B4A24), RoundedCornerShape(12.dp))
                    .clickable { onSelect(entry) }
                    .padding(vertical = 9.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = entry.icon, fontSize = 15.sp)
                if (isSelected) {
                    com.lernhero.home.ui.OutlinedText(text = "  ${entry.label}", size = 14.sp, color = Color.White, strokeWidth = 5f)
                } else {
                    Text(
                        text = "  ${entry.label}",
                        color = Color(0xFF4A3218),
                        fontFamily = FontFirst(),
                        fontWeight = FontWeight.Black,
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}
