package com.lernhero.home.progress

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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lernhero.domain.player.HeroStat
import com.lernhero.domain.player.PlayerProgress
import com.lernhero.domain.progress.Achievement
import com.lernhero.domain.progress.AchievementCatalog
import com.lernhero.domain.progress.AchievementGroup
import com.lernhero.domain.progress.AchievementTier
import com.lernhero.domain.progress.GameStats
import com.lernhero.domain.quiz.QuizCategory
import com.lernhero.home.player.PlayerViewModel
import com.lernhero.shared.theme.FontFirst
import com.lernhero.shared.theme.FontPixel

private val Ink = Color(0xFF19120C)
private val Frame = Color(0xFFC8A45A)
private val Cream = Color(0xFFF4E9D7)
private val Muted = Color(0xFF9D8B6A)
private val GoldText = Color(0xFFFFD34D)
private val Good = Color(0xFF5FD08A)
private val Bad = Color(0xFFE56A5D)

private enum class ProgressTab(val label: String) {
    Stats("Statystyki"),
    Achievements("Osiagniecia")
}

/**
 * Zakladka "Postep": liczby z nauki (ile pytan, jaka celnosc, w czym jestes slaby) i osiagniecia.
 *
 * Poziom bohatera i rozdawanie punktow NIE sa tutaj - siedza w zakladce Postac, razem z ekwipunkiem
 * i paskiem zdolnosci. Byly przez chwile w obu miejscach naraz i to byl blad: jedna postac nie moze
 * miec dwoch kart postaci, bo gracz nigdy nie wie, na ktora patrzec.
 *
 * Osiagniecia licza sie w locie z [GameStats] - nie ma osobnego zapisu "co odblokowane", wiec nie
 * da sie tego rozjechac ze statystykami ani stracic przy zmianie katalogu.
 */
@Composable
fun ProgressScreen(viewModel: PlayerViewModel) {
    val state by viewModel.uiState.collectAsState()
    var tab by remember { mutableStateOf(ProgressTab.Stats) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        LearningHeader(stats = state.stats)

        TabRow(selected = tab, onSelect = { tab = it })

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            when (tab) {
                ProgressTab.Stats -> StatsTab(stats = state.stats)
                ProgressTab.Achievements -> AchievementsTab(stats = state.stats)
            }
        }
    }
}

/**
 * Naglowek zakladki: celnosc calego konta na pasku. To jest glowna miara postepu W NAUCE - poziom
 * bohatera mowi, ile sie GRALO, a ten pasek, ile sie NAUCZYLO, i to jego gracz ma widziec tutaj.
 */
@Composable
private fun LearningHeader(stats: GameStats) {
    val fill by animateFloatAsState(
        targetValue = stats.accuracy,
        animationSpec = tween(600)
    )
    val accent = when {
        stats.accuracy >= 0.75f -> Good
        stats.accuracy >= 0.5f -> GoldText
        else -> Bad
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Brush.horizontalGradient(listOf(Color(0xFF2A1E0C), Ink)))
            .border(2.dp, Frame, RoundedCornerShape(14.dp))
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "CELNOSC ${stats.accuracyPercent}%",
                    color = accent,
                    fontFamily = FontPixel(),
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                Text(
                    text = "${stats.questionsCorrect} z ${stats.questionsAnswered} pytan",
                    color = Muted,
                    fontFamily = FontFirst(),
                    fontSize = 11.sp
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "🔥 ${stats.bestAnswerStreak}",
                    color = GoldText,
                    fontFamily = FontPixel(),
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
                Text(
                    text = "najdluzsza seria",
                    color = Muted,
                    fontFamily = FontFirst(),
                    fontSize = 10.sp
                )
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(9.dp)
                .clip(RoundedCornerShape(50))
                .background(Color(0xFF120C08))
                .border(1.dp, Color(0xFF3A2E22), RoundedCornerShape(50))
                .padding(1.5.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(fill)
                    .fillMaxSize()
                    .clip(RoundedCornerShape(50))
                    .background(accent)
            )
        }
    }
}

@Composable
private fun TabRow(selected: ProgressTab, onSelect: (ProgressTab) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        ProgressTab.entries.forEach { entry ->
            val isSelected = entry == selected
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (isSelected) Color(0xFF3A2A18) else Ink)
                    .border(
                        width = if (isSelected) 2.dp else 1.dp,
                        color = if (isSelected) Frame else Color(0xFF3A2E22),
                        shape = RoundedCornerShape(10.dp)
                    )
                    .clickable { onSelect(entry) }
                    .padding(vertical = 9.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = entry.label,
                    color = if (isSelected) GoldText else Muted,
                    fontFamily = FontFirst(),
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
            }
        }
    }
}

// ------------------------------------------------------------ Statystyki

@Composable
private fun StatsTab(stats: GameStats) {
    Panel(title = "NAUKA") {
        BigNumberRow(
            left = "${stats.questionsCorrect}/${stats.questionsAnswered}" to "Poprawne pytania",
            right = "${stats.accuracyPercent}%" to "Celnosc"
        )
        StatLine("Bledne odpowiedzi", "${stats.questionsWrong}")
        StatLine("Najdluzsza seria", "${stats.bestAnswerStreak}")
    }

    val categories = remember(stats) {
        QuizCategory.entries
            .map { it to stats.statFor(it) }
            .filter { (_, stat) -> stat.answered > 0 }
            .sortedByDescending { (_, stat) -> stat.answered }
    }
    if (categories.isNotEmpty()) {
        Panel(title = "CELNOSC WEDLUG KATEGORII") {
            categories.forEach { (category, stat) ->
                CategoryBar(
                    label = category.label,
                    correct = stat.correct,
                    answered = stat.answered,
                    accuracy = stat.accuracy
                )
            }
        }
    }

    Panel(title = "WALKA") {
        BigNumberRow(
            left = "${stats.battlesWon}" to "Wygrane walki",
            right = "${stats.bestEndlessWave}" to "Rekord endless"
        )
        StatLine("Przegrane walki", "${stats.battlesLost}")
        StatLine("Oczyszczone fale", "${stats.totalWavesCleared}")
        StatLine("Pokonani bossowie", "${stats.bossesDefeated}")
        StatLine("Ukonczone lochy", "${stats.dungeonsCleared}")
    }

    Panel(title = "DOROBEK") {
        StatLine("Zdobyte zloto", "${stats.goldEarned}")
        StatLine("Wykute przedmioty", "${stats.itemsCrafted}")
        StatLine("Przyzwani kompani", "${stats.monstersSummoned}")
    }
}

@Composable
private fun BigNumberRow(left: Pair<String, String>, right: Pair<String, String>) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        listOf(left, right).forEach { (value, label) ->
            Column(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFF241A12))
                    .padding(vertical = 10.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = value,
                    color = GoldText,
                    fontFamily = FontPixel(),
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
                Text(text = label, color = Muted, fontFamily = FontFirst(), fontSize = 10.sp)
            }
        }
    }
}

@Composable
private fun StatLine(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, color = Muted, fontFamily = FontFirst(), fontSize = 12.sp)
        Text(
            text = value,
            color = Cream,
            fontFamily = FontFirst(),
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp
        )
    }
}

/**
 * Pasek celnosci jednej kategorii. Kolor jest informacja, nie ozdoba: ponizej 50% robi sie czerwony,
 * bo to jest wlasnie ta kategoria, ktora trzeba powtorzyc.
 */
@Composable
private fun CategoryBar(label: String, correct: Int, answered: Int, accuracy: Float) {
    val color = when {
        accuracy >= 0.75f -> Good
        accuracy >= 0.5f -> GoldText
        else -> Bad
    }

    Column(verticalArrangement = Arrangement.spacedBy(3.dp), modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = label, color = Cream, fontFamily = FontFirst(), fontSize = 12.sp)
            Text(
                text = "$correct/$answered",
                color = color,
                fontFamily = FontFirst(),
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp
            )
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(50))
                .background(Color(0xFF241A12))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(accuracy.coerceIn(0f, 1f))
                    .fillMaxSize()
                    .clip(RoundedCornerShape(50))
                    .background(color)
            )
        }
    }
}

// ---------------------------------------------------------- Osiagniecia

@Composable
private fun AchievementsTab(stats: GameStats) {
    val unlocked = remember(stats) { AchievementCatalog.unlockedCount(stats) }
    val total = AchievementCatalog.all.size

    Panel(title = "ODBLOKOWANE") {
        BigNumberRow(
            left = "$unlocked/$total" to "Osiagniecia",
            right = "${(unlocked * 100 / total.coerceAtLeast(1))}%" to "Ukonczenie"
        )
    }

    AchievementGroup.entries.forEach { group ->
        val items = AchievementCatalog.byGroup(group)
        if (items.isEmpty()) return@forEach
        Panel(title = group.label.uppercase()) {
            items.forEach { achievement ->
                AchievementRow(achievement = achievement, stats = stats)
            }
        }
    }
}

@Composable
private fun AchievementRow(achievement: Achievement, stats: GameStats) {
    val unlocked = achievement.isUnlocked(stats)
    val fraction = achievement.fraction(stats)
    val tierColor = when (achievement.tier) {
        AchievementTier.Bronze -> Color(0xFFB87333)
        AchievementTier.Silver -> Color(0xFFB9C4CC)
        AchievementTier.Gold -> GoldText
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(Color(0xFF241A12).copy(alpha = if (unlocked) 1f else 0.55f))
            .border(
                width = if (unlocked) 1.5.dp else 1.dp,
                color = if (unlocked) tierColor else Color(0xFF3A2E22),
                shape = RoundedCornerShape(10.dp)
            )
            .padding(horizontal = 10.dp, vertical = 9.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = if (unlocked) "🏅" else "🔒",
            fontSize = 18.sp
        )
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Text(
                text = achievement.title,
                color = if (unlocked) Cream else Muted,
                fontFamily = FontFirst(),
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp
            )
            Text(
                text = achievement.description,
                color = Muted,
                fontFamily = FontFirst(),
                fontSize = 10.sp
            )
            if (!unlocked) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp)
                        .clip(RoundedCornerShape(50))
                        .background(Color(0xFF120C08))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(fraction)
                            .fillMaxSize()
                            .clip(RoundedCornerShape(50))
                            .background(tierColor)
                    )
                }
            }
        }
        Text(
            text = if (unlocked) "✓" else "${achievement.progress(stats)}/${achievement.target}",
            color = if (unlocked) Good else Muted,
            fontFamily = FontFirst(),
            fontWeight = FontWeight.Bold,
            fontSize = 11.sp
        )
    }
}

@Composable
private fun Panel(title: String, content: @Composable () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Ink)
            .border(1.5.dp, Frame.copy(alpha = 0.55f), RoundedCornerShape(12.dp))
            .padding(horizontal = 12.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(9.dp)
    ) {
        Text(
            text = title,
            color = GoldText,
            fontFamily = FontPixel(),
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp
        )
        content()
    }
}
