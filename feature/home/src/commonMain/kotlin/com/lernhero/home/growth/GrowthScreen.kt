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

/** Te same zakladki co w Swiatyni ([com.lernhero.home.ui.PaperTabs]) - jeden styl w calej grze. */
@Composable
private fun GrowthTabBar(
    selected: GrowthTab,
    onSelect: (GrowthTab) -> Unit,
    modifier: Modifier = Modifier
) {
    com.lernhero.home.ui.PaperTabs(
        labels = GrowthTab.entries.map { it.label },
        selected = selected.ordinal,
        onSelect = { onSelect(GrowthTab.entries[it]) },
        modifier = modifier
    )
}
