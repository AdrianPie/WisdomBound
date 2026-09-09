package com.lernhero.game.battle.component

import androidx.compose.animation.core.EaseOutBack
import androidx.compose.animation.core.EaseOutCubic
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lernhero.domain.common.Rarity
import com.lernhero.domain.item.Item
import com.lernhero.domain.progress.RunStats
import com.lernhero.game.battle.BattleUiState
import com.lernhero.game.battle.GamePhase
import com.lernhero.shared.theme.FontFirst
import com.lernhero.shared.theme.FontPixel
import kotlinx.coroutines.delay

private val Ink = Color(0xFF19120C)
private val Frame = Color(0xFFC8A45A)
private val Cream = Color(0xFFF4E9D7)
private val Muted = Color(0xFF9D8B6A)
private val GoldText = Color(0xFFFFD34D)
private val Win = Color(0xFF5FD08A)
private val Lose = Color(0xFFE56A5D)

/**
 * Ekran koncowy walki. Zastapil jednolinijkowy dialog, ktory mowil tylko "wygrales, masz X monet" -
 * a wiec ukrywal wszystko, co gracz faktycznie zarobil: XP, awanse, rude, esencje i - co w tej grze
 * najwazniejsze - wynik z pytan.
 *
 * Kolejnosc pozycji jest celowa: najpierw NAUKA (ile pytan, jaka celnosc, jaka seria), potem lup.
 * To jest gra, w ktorej wiedza jest statystyka bojowa, wiec podsumowanie ma to mowic wprost.
 *
 * Pozycje wjezdzaja po kolei ([ROW_STAGGER_MILLIS]) - to ten sam chwyt, co w kazdej grze mobilnej:
 * liczenie nagrody ma trwac chwile dluzej niz jedna klatka, bo inaczej nie ma czego smakowac.
 */
@Composable
fun BattleSummaryScreen(
    state: BattleUiState,
    onLeave: () -> Unit,
    modifier: Modifier = Modifier
) {
    val summary = state.runSummary
    val won = state.phase == GamePhase.Victory
    val accent = if (won) Win else Lose

    var revealed by remember { mutableStateOf(0) }
    val rows = remember(summary) { summaryRows(summary, state.wave) }

    LaunchedEffect(rows) {
        revealed = 0
        rows.indices.forEach { _ ->
            delay(ROW_STAGGER_MILLIS)
            revealed += 1
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xF2070505))
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 460.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Header(won = won, accent = accent, wave = state.wave, endless = summary?.endless == true)

            if (state.levelsGained > 0) {
                LevelUpBanner(levels = state.levelsGained)
            }

            // Lup rzeczowy dostaje WLASNY baner, a nie kolejny wiersz w tabelce - wypada rzadko
            // (patrz [StageLoot.dropChance]) i ma byc widoczny jako wydarzenie, nie jako liczba
            // obok rudy.
            state.droppedItem?.let { item -> LootBanner(item = item) }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(Ink)
                    .border(2.dp, Frame, RoundedCornerShape(14.dp))
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalArrangement = Arrangement.spacedBy(9.dp)
            ) {
                rows.forEachIndexed { index, row ->
                    SummaryRow(row = row, visible = index < revealed, accent = accent)
                }
            }

            state.message?.let { message ->
                Text(
                    text = message,
                    color = Muted,
                    fontFamily = FontFirst(),
                    fontSize = 11.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Text(
                text = "BACK",
                color = GoldText,
                fontFamily = FontPixel(),
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFF3A2A18))
                    .border(2.dp, Frame, RoundedCornerShape(10.dp))
                    .clickable(onClick = onLeave)
                    .padding(horizontal = 30.dp, vertical = 12.dp)
            )
        }
    }
}

@Composable
private fun Header(won: Boolean, accent: Color, wave: Int, endless: Boolean) {
    val pop by animateFloatAsState(targetValue = 1f, animationSpec = tween(420, easing = EaseOutBack))

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp),
        modifier = Modifier.graphicsLayer { scaleX = pop; scaleY = pop }
    ) {
        Text(text = if (won) "🏆" else "💀", fontSize = 44.sp)
        Text(
            text = if (won) "VICTORY" else "RUN OVER",
            color = accent,
            fontFamily = FontPixel(),
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp
        )
        Text(
            text = if (endless) "You reached wave $wave" else "Wave $wave",
            color = Muted,
            fontFamily = FontFirst(),
            fontSize = 12.sp
        )
    }
}

@Composable
private fun LevelUpBanner(levels: Int) {
    val pop by animateFloatAsState(targetValue = 1f, animationSpec = tween(520, easing = EaseOutBack))

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer { scaleX = pop; scaleY = pop }
            .clip(RoundedCornerShape(12.dp))
            .background(Brush.horizontalGradient(listOf(Color(0xFF5A3E12), Color(0xFF2A1E0C))))
            .border(2.dp, GoldText, RoundedCornerShape(12.dp))
            .padding(horizontal = 14.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = "⭐", fontSize = 24.sp)
        Column {
            Text(
                text = if (levels > 1) "LEVEL UP x$levels!" else "LEVEL UP!",
                color = GoldText,
                fontFamily = FontPixel(),
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp
            )
            Text(
                text = "You have new stat points to spend in the Progress tab.",
                color = Cream,
                fontFamily = FontFirst(),
                fontSize = 11.sp
            )
        }
    }
}

/**
 * Kolor ramki wedlug rzadkosci. Wlasna kopia palety, a nie import z `:feature:home` - ten modul
 * nie zalezy od tamtego i nie ma po co zaczynac (patrz ARCHITECTURE.md, kierunek zaleznosci).
 */
private fun rarityColor(rarity: Rarity): Color = when (rarity) {
    Rarity.Common -> Color(0xFFB8B2A6)
    Rarity.Uncommon -> Color(0xFF6FCF6B)
    Rarity.Rare -> Color(0xFF5AA9FF)
    Rarity.Epic -> Color(0xFFB06CFF)
    Rarity.Legendary -> Color(0xFFFFC13D)
}

/**
 * "Wypadl przedmiot!" - to jest ta chwila, dla ktorej gracz powtarza plansze, wiec dostaje ramke
 * w kolorze rzadkosci i wlasne wejscie animacja, tak samo jak awans.
 */
@Composable
private fun LootBanner(item: Item) {
    val accent = rarityColor(item.rarity)
    val pop by animateFloatAsState(targetValue = 1f, animationSpec = tween(520, easing = EaseOutBack))

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer { scaleX = pop; scaleY = pop }
            .clip(RoundedCornerShape(12.dp))
            .background(Brush.horizontalGradient(listOf(accent.copy(alpha = 0.28f), Color(0xFF1A1410))))
            .border(2.dp, accent, RoundedCornerShape(12.dp))
            .padding(horizontal = 14.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = "🎁", fontSize = 24.sp)
        Column {
            Text(
                text = "LOOT!",
                color = accent,
                fontFamily = FontPixel(),
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp
            )
            Text(
                text = "${item.name} (${item.rarity.name}) went into your bag.",
                color = Cream,
                fontFamily = FontFirst(),
                fontSize = 11.sp
            )
        }
    }
}

/** Jedna pozycja podsumowania: ikona, etykieta, wartosc. [emphasis] wyroznia nagrody od liczb. */
private data class SummaryRow(
    val icon: String,
    val label: String,
    val value: String,
    val emphasis: Boolean = false,
    val divider: Boolean = false
)

private fun summaryRows(summary: RunStats?, wave: Int): List<SummaryRow> {
    if (summary == null) {
        return listOf(SummaryRow("⚔", "Wave", "$wave"))
    }

    return buildList {
        add(SummaryRow("📚", "Questions", "${summary.questionsCorrect}/${summary.questionsAnswered}"))
        add(SummaryRow("🎯", "Accuracy", "${summary.accuracyPercent}%"))
        if (summary.bestAnswerStreak > 0) {
            add(SummaryRow("🔥", "Longest Streak", "${summary.bestAnswerStreak}"))
        }
        add(SummaryRow("🌊", "Waves Cleared", "${summary.wavesCleared}", divider = true))
        add(SummaryRow("⭐", "Experience", "+${summary.xp} XP", emphasis = true))
        add(SummaryRow("💰", "Gold", "+${summary.gold}", emphasis = true))
        if (summary.iron > 0) add(SummaryRow("⚙", "Ore", "+${summary.iron}", emphasis = true))
        if (summary.essence > 0) add(SummaryRow("✨", "Essence", "+${summary.essence}", emphasis = true))
        if (summary.summonTickets > 0) add(SummaryRow("🎟", "Tickets", "+${summary.summonTickets}", emphasis = true))
        if (summary.wood > 0) add(SummaryRow("🪵", "Wood", "+${summary.wood}", emphasis = true))
        if (summary.stone > 0) add(SummaryRow("🪨", "Stone", "+${summary.stone}", emphasis = true))
    }
}

@Composable
private fun SummaryRow(row: SummaryRow, visible: Boolean, accent: Color) {
    val slide by animateFloatAsState(
        targetValue = if (visible) 0f else 1f,
        animationSpec = tween(240, easing = EaseOutCubic)
    )

    Column(verticalArrangement = Arrangement.spacedBy(9.dp)) {
        if (row.divider) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(Color(0xFF3A2E22))
            )
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .graphicsLayer {
                    alpha = 1f - slide
                    translationX = slide * 28f
                },
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                Text(text = row.icon, fontSize = 15.sp)
                Text(
                    text = row.label,
                    color = Muted,
                    fontFamily = FontFirst(),
                    fontSize = 13.sp
                )
            }
            Text(
                text = row.value,
                color = if (row.emphasis) accent else Cream,
                fontFamily = FontFirst(),
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
        }
    }
}

private const val ROW_STAGGER_MILLIS = 90L
