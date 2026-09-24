package com.lernhero.home.progress

import com.lernhero.home.ui.JewelRow
import com.lernhero.home.ui.JewelIcon
import com.lernhero.domain.jewel.Jewels
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
import androidx.compose.foundation.layout.Spacer
import com.lernhero.home.navigation.bottomBarSpace
import androidx.compose.foundation.layout.systemBarsPadding
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
import com.lernhero.reader.ReadingGoalsPanel
import com.lernhero.shared.theme.FontFirst
import com.lernhero.shared.theme.FontPixel
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.width
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import com.lernhero.home.city.TownPanel
import com.lernhero.home.city.TownProgressBar
import com.lernhero.home.ui.OutlinedText

private val Ink = Color(0xFF19120C)
private val Frame = Color(0xFFC8A45A)
private val Cream = Color(0xFFF4E9D7)
private val Muted = Color(0xFF9D8B6A)
private val GoldText = Color(0xFFFFD34D)
private val Good = Color(0xFF5FD08A)
private val Bad = Color(0xFFE56A5D)

private enum class ProgressTab(val label: String) {
    Stats("Stats"),
    Achievements("Achievements"),
    /** Sakiewka kamieni z osiagniec i laczenie 3 -> 1. */
    Jewels("Jewels"),
    /** Cele dzienne za czytanie - przeniesione tu z Czytelni, patrz [ReadingGoalsPanel]. */
    Reading("Reading")
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
fun ProgressScreen(viewModel: PlayerViewModel, onBack: () -> Unit) {
    val state by viewModel.uiState.collectAsState()
    var tab by remember { mutableStateOf(ProgressTab.Stats) }

    // Swiatynia jest PELNYM ekranem (wchodzi sie z wyspy na Domu), wiec sama odsuwa sie od
    // krawedzi i sama niesie powrot.
    Column(
        modifier = Modifier
            .fillMaxSize()
            .systemBarsPadding()
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            RoundBack(onBack)
            OutlinedText(text = "Temple", size = 26.sp, color = GoldText, modifier = Modifier.weight(1f))
            Box(modifier = Modifier.size(42.dp))
        }

        LearningHeader(stats = state.stats)

        TabRow(selected = tab, onSelect = { tab = it })

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            when (tab) {
                ProgressTab.Stats -> StatsTab(stats = state.stats)
                ProgressTab.Achievements -> AchievementsTab(
                    stats = state.stats,
                    claimed = state.claimedAchievements,
                    onClaim = viewModel::claimAchievement
                )
                ProgressTab.Jewels -> JewelsTab(pouch = state.jewels, onCombine = viewModel::combineJewels)
                ProgressTab.Reading -> ReadingGoalsPanel(onRewardClaimed = viewModel::refresh)
            }

            // Ogon pod dolny pasek - patrz [bottomBarSpace].
            Spacer(modifier = Modifier.height(bottomBarSpace()))
        }
    }
}

@Composable
private fun RoundBack(onBack: () -> Unit) {
    Box(
        modifier = Modifier
            .size(42.dp)
            .clip(CircleShape)
            .background(Brush.verticalGradient(listOf(Color(0xFF5A3F24), Color(0xFF2C1D10))))
            .border(2.5.dp, Outline, CircleShape)
            .clickable(onClick = onBack),
        contentAlignment = Alignment.Center
    ) {
        OutlinedText(text = "‹", size = 26.sp, color = GoldText)
    }
}

private val Outline = Color(0xFF120B05)

private fun accentFor(accuracy: Float): Color = when {
    accuracy >= 0.75f -> Good
    accuracy >= 0.5f -> GoldText
    else -> Bad
}

/**
 * Naglowek: pierscien celnosci calego konta. To glowna miara postepu W NAUCE - poziom bohatera
 * mowi, ile sie GRALO, a ten pierscien, ile sie NAUCZYLO.
 */
@Composable
private fun LearningHeader(stats: GameStats) {
    val fill by animateFloatAsState(targetValue = stats.accuracy, animationSpec = tween(900))
    val accent = accentFor(stats.accuracy)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(Brush.linearGradient(listOf(Color(0xFF4A2A6E), Color(0xFF24133A), Color(0xFF160C22))))
            .border(3.dp, Brush.verticalGradient(listOf(GoldText, Color(0xFFE08A1E))), RoundedCornerShape(18.dp))
            .padding(14.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(modifier = Modifier.size(104.dp), contentAlignment = Alignment.Center) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val stroke = 12.dp.toPx()
                val arcSize = Size(size.width - stroke, size.height - stroke)
                val topLeft = Offset(stroke / 2, stroke / 2)
                drawArc(Color(0xFF0B0714), -90f, 360f, false, topLeft, arcSize, style = Stroke(stroke))
                drawArc(
                    Brush.sweepGradient(listOf(accent.copy(alpha = 0.7f), accent, accent.copy(alpha = 0.7f))),
                    -90f, 360f * fill, false, topLeft, arcSize,
                    style = Stroke(stroke, cap = StrokeCap.Round)
                )
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                OutlinedText(text = "${stats.accuracyPercent}%", size = 24.sp, color = Color.White)
                Text("accuracy", color = Color(0xFFBFA9DE), fontFamily = FontFirst(), fontWeight = FontWeight.Bold, fontSize = 10.sp)
            }
        }
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedText(text = "${stats.questionsCorrect} / ${stats.questionsAnswered}", size = 22.sp, color = GoldText)
            Text("questions answered right", color = Color(0xFFBFA9DE), fontFamily = FontFirst(), fontWeight = FontWeight.Bold, fontSize = 11.sp)
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(Brush.horizontalGradient(listOf(Color(0xFFFF7A2E), Color(0xFFD9322B))))
                    .border(2.dp, Outline, RoundedCornerShape(50))
                    .padding(horizontal = 12.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("🔥", fontSize = 14.sp)
                OutlinedText(text = "${stats.bestAnswerStreak} best streak", size = 13.sp, color = Color.White, strokeWidth = 4f)
            }
        }
    }
}

@Composable
private fun TabRow(selected: ProgressTab, onSelect: (ProgressTab) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        ProgressTab.entries.forEach { entry ->
            val active = entry == selected
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        if (active) Brush.verticalGradient(listOf(Color(0xFF5BB8FF), Color(0xFF2C7BD6)))
                        else Brush.verticalGradient(listOf(Color(0xFFE9D6B4), Color(0xFFC9AE84)))
                    )
                    .border(2.5.dp, if (active) Color(0xFF123A6B) else Color(0xFF6B4A24), RoundedCornerShape(12.dp))
                    .clickable { onSelect(entry) }
                    .padding(vertical = 9.dp),
                contentAlignment = Alignment.Center
            ) {
                if (active) {
                    OutlinedText(text = entry.label, size = 12.sp, color = Color.White, strokeWidth = 5f)
                } else {
                    Text(entry.label, color = Color(0xFF4A3218), fontFamily = FontFirst(), fontWeight = FontWeight.Black, fontSize = 12.sp, maxLines = 1)
                }
            }
        }
    }
}

// ------------------------------------------------------------ Statystyki

@Composable
private fun StatsTab(stats: GameStats) {
    Section(title = "Learning") {
        TileGrid(
            listOf(
                Tile("✅", "${stats.questionsCorrect}", "Correct", Good),
                Tile("❌", "${stats.questionsWrong}", "Wrong", Bad),
                Tile("🎯", "${stats.accuracyPercent}%", "Accuracy", accentFor(stats.accuracy)),
                Tile("🔥", "${stats.bestAnswerStreak}", "Best streak", Color(0xFFFF9A3C))
            ),
            columns = 2
        )
    }

    val categories = remember(stats) {
        QuizCategory.entries
            .map { it to stats.statFor(it) }
            .filter { (_, stat) -> stat.answered > 0 }
            .sortedByDescending { (_, stat) -> stat.answered }
    }
    if (categories.isNotEmpty()) {
        val weakest = categories.filter { it.second.answered >= 3 }.minByOrNull { it.second.accuracy }
        Section(title = "Topics") {
            weakest?.let { (category, stat) ->
                if (stat.accuracy < 0.5f) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(Bad.copy(alpha = 0.15f))
                            .border(1.5.dp, Bad.copy(alpha = 0.6f), RoundedCornerShape(10.dp))
                            .padding(horizontal = 10.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(category.icon(), fontSize = 18.sp)
                        Text(
                            text = "Weakest topic: ${category.label}. A few more rounds will fix it!",
                            color = Cream,
                            fontFamily = FontFirst(),
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                }
            }
            categories.forEach { (category, stat) ->
                CategoryBar(
                    icon = category.icon(),
                    label = category.label,
                    correct = stat.correct,
                    answered = stat.answered,
                    accuracy = stat.accuracy
                )
            }
        }
    }

    Section(title = "Combat") {
        TileGrid(
            listOf(
                Tile("⚔️", "${stats.battlesWon}", "Won"),
                Tile("💀", "${stats.battlesLost}", "Lost"),
                Tile("🌊", "${stats.bestEndlessWave}", "Endless best"),
                Tile("👹", "${stats.bossesDefeated}", "Bosses"),
                Tile("🏰", "${stats.dungeonsCleared}", "Dungeons"),
                Tile("🧱", "${stats.totalWavesCleared}", "Waves")
            ),
            columns = 3
        )
    }

    Section(title = "Wealth") {
        TileGrid(
            listOf(
                Tile("💰", compact(stats.goldEarned), "Gold earned", GoldText),
                Tile("⚒️", "${stats.itemsCrafted}", "Forged"),
                Tile("🔮", "${stats.monstersSummoned}", "Summoned")
            ),
            columns = 3
        )
    }
}

private fun compact(value: Long): String = when {
    value >= 1_000_000 -> "${value / 100_000 / 10.0}M"
    value >= 10_000 -> "${value / 1_000}K"
    else -> "$value"
}

private fun compact(value: Int): String = compact(value.toLong())

private data class Tile(val icon: String, val value: String, val label: String, val color: Color = Color.White)

@Composable
private fun TileGrid(tiles: List<Tile>, columns: Int) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        tiles.chunked(columns).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                row.forEach { tile ->
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Brush.verticalGradient(listOf(Color(0xFF2A1E14), Color(0xFF140E09))))
                            .border(2.dp, Outline, RoundedCornerShape(12.dp))
                            .padding(vertical = 10.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Text(tile.icon, fontSize = 18.sp)
                        OutlinedText(text = tile.value, size = 20.sp, color = tile.color, maxLines = 1)
                        Text(tile.label.uppercase(), color = Muted, fontFamily = FontFirst(), fontWeight = FontWeight.Bold, fontSize = 9.sp, maxLines = 1)
                    }
                }
                repeat(columns - row.size) { Box(modifier = Modifier.weight(1f)) }
            }
        }
    }
}

/**
 * Pasek celnosci jednej kategorii. Kolor jest informacja, nie ozdoba: ponizej 50% robi sie czerwony,
 * bo to jest wlasnie ta kategoria, ktora trzeba powtorzyc.
 */
@Composable
private fun CategoryBar(icon: String, label: String, correct: Int, answered: Int, accuracy: Float) {
    val color = accentFor(accuracy)
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(color.copy(alpha = 0.2f))
                .border(2.dp, color.copy(alpha = 0.8f), RoundedCornerShape(10.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(icon, fontSize = 18.sp)
        }
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(label, color = Cream, fontFamily = FontFirst(), fontWeight = FontWeight.Black, fontSize = 14.sp)
                Text(
                    text = "$correct/$answered  ·  ${(accuracy * 100).toInt()}%",
                    color = color,
                    fontFamily = FontFirst(),
                    fontWeight = FontWeight.Black,
                    fontSize = 12.sp
                )
            }
            TownProgressBar(progress = accuracy, fill = color, height = 14.dp)
        }
    }
}

private fun QuizCategory.icon(): String = when (this) {
    QuizCategory.Biology -> "🧬"
    QuizCategory.Animals -> "🦊"
    QuizCategory.History -> "📜"
    QuizCategory.Elements -> "⚗️"
    QuizCategory.Capitals -> "🏛️"
    QuizCategory.Science -> "🔭"
    QuizCategory.Flags -> "🚩"
    QuizCategory.Geography -> "🗺️"
    QuizCategory.Sport -> "⚽"
    QuizCategory.Math -> "➗"
    QuizCategory.Literature -> "📚"
    QuizCategory.Technology -> "💻"
    QuizCategory.Mythology -> "⚡"
    QuizCategory.Poland -> "🇵🇱"
    QuizCategory.Vocabulary -> "🔤"
    QuizCategory.Custom -> "✏️"
}

// ---------------------------------------------------------- Osiagniecia

@Composable
private fun AchievementsTab(
    stats: GameStats,
    claimed: Set<String>,
    onClaim: (Achievement) -> Unit
) {
    val unlocked = remember(stats) { AchievementCatalog.unlockedCount(stats) }
    val total = AchievementCatalog.all.size
    val pendingGold = remember(stats, claimed) { AchievementCatalog.claimableGold(stats, claimed) }

    Section(title = "Unlocked") {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("🏆", fontSize = 34.sp)
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                OutlinedText(text = "$unlocked / $total", size = 22.sp, color = GoldText)
                TownProgressBar(
                    progress = unlocked.toFloat() / total.coerceAtLeast(1),
                    fill = GoldText,
                    height = 18.dp,
                    label = "${(unlocked * 100 / total.coerceAtLeast(1))}%"
                )
            }
        }
        // Nieodebrane zloto stoi NA GORZE listy - inaczej gracz musialby przewinac caly ekran,
        // zeby w ogole zauwazyc, ze cos mu sie nalezy.
        if (pendingGold > 0) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(Brush.horizontalGradient(listOf(Color(0xFF6B4A0E), Color(0xFF3A2808))))
                    .border(2.dp, GoldText, RoundedCornerShape(10.dp))
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("💰", fontSize = 18.sp)
                OutlinedText(text = "$pendingGold gold waiting to be claimed!", size = 13.sp, color = GoldText, strokeWidth = 4f)
            }
        }
    }

    AchievementGroup.entries.forEach { group ->
        val items = AchievementCatalog.byGroup(group).let { all ->
            if (group == AchievementGroup.Topics) currentTopicSteps(all, claimed) else all
        }
        if (items.isEmpty()) return@forEach
        Section(title = group.label) {
            items.forEach { achievement ->
                AchievementRow(
                    achievement = achievement,
                    stats = stats,
                    claimed = achievement.id in claimed,
                    onClaim = { onClaim(achievement) }
                )
            }
        }
    }
}

@Composable
private fun AchievementRow(
    achievement: Achievement,
    stats: GameStats,
    claimed: Boolean,
    onClaim: () -> Unit
) {
    val unlocked = achievement.isUnlocked(stats)
    val claimable = unlocked && !claimed
    val fraction = achievement.fraction(stats)
    val tierColor = when (achievement.tier) {
        AchievementTier.Bronze -> Color(0xFFCD8A4E)
        AchievementTier.Silver -> Color(0xFFC9D4DC)
        AchievementTier.Gold -> GoldText
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(
                if (claimable) Brush.horizontalGradient(listOf(Color(0xFF3F2E0E), Color(0xFF1E150B)))
                else Brush.horizontalGradient(listOf(Color(0xFF241A12), Color(0xFF17100A)))
            )
            .border(2.dp, if (unlocked) tierColor else Outline, RoundedCornerShape(12.dp))
            .padding(horizontal = 10.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(42.dp)
                .clip(CircleShape)
                .background(
                    if (unlocked) Brush.radialGradient(listOf(tierColor, tierColor.copy(alpha = 0.5f)))
                    else Brush.radialGradient(listOf(Color(0xFF3A3028), Color(0xFF1E1812)))
                )
                .border(2.5.dp, Outline, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(text = if (unlocked) "🏅" else "🔒", fontSize = 20.sp)
        }
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Text(
                text = achievement.title,
                color = if (unlocked) Cream else Color(0xFFC8B89A),
                fontFamily = FontFirst(),
                fontWeight = FontWeight.Black,
                fontSize = 14.sp
            )
            Text(text = achievement.description, color = Muted, fontFamily = FontFirst(), fontSize = 11.sp)
            achievement.jewel?.takeIf { !claimed }?.let { jewel ->
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    JewelIcon(jewel = jewel, size = 18.dp)
                    Text(text = jewel.name, color = Color(jewel.type.colorHex), fontFamily = FontFirst(), fontWeight = FontWeight.Bold, fontSize = 11.sp)
                }
            }
            if (!unlocked) {
                TownProgressBar(
                    progress = fraction,
                    fill = tierColor,
                    height = 16.dp,
                    label = "${achievement.progress(stats)}/${achievement.target}"
                )
            }
        }
        // Prawa krawedz wiersza ma trzy stany: postep -> przycisk ODBIERZ -> ptaszek.
        when {
            claimable -> Column(
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(Brush.verticalGradient(listOf(Color(0xFF8BE05A), Color(0xFF3F9E2B))))
                    .border(2.dp, Outline, RoundedCornerShape(10.dp))
                    .clickable(onClick = onClaim)
                    .padding(horizontal = 10.dp, vertical = 5.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                OutlinedText(text = "CLAIM", size = 12.sp, color = Color.White, strokeWidth = 4f)
                OutlinedText(text = "💰${achievement.reward} 💎${achievement.gems}", size = 11.sp, color = GoldText, strokeWidth = 4f)
                achievement.jewel?.let { JewelIcon(jewel = it, size = 26.dp) }
            }
            unlocked -> Text("✓", color = Good, fontWeight = FontWeight.Black, fontSize = 20.sp)
        }
    }
}

/**
 * Z trzech stopni serii w kazdym temacie pokazujemy jeden: najnizszy jeszcze nieodebrany, a gdy
 * wszystkie sa odebrane - ostatni. Pelna lista to ponad 40 wierszy, wiekszosc nieosiagalna na razie.
 */
private fun currentTopicSteps(topicAchievements: List<Achievement>, claimed: Set<String>): List<Achievement> =
    topicAchievements
        .groupBy { it.topic }
        .values
        .map { steps -> steps.firstOrNull { it.id !in claimed } ?: steps.last() }

// ---------------------------------------------------------------- Kamienie

@Composable
private fun JewelsTab(pouch: Map<String, Int>, onCombine: (String) -> Unit) {
    val owned = Jewels.all.filter { (pouch[it.id] ?: 0) > 0 }
    Section(title = "Jewel pouch") {
        Text(
            text = "Jewels come from achievements. Set them into sockets on Uncommon or better gear. " +
                "Combine ${Jewels.COMBINE_COUNT} of the same jewel into a better cut.",
            color = Muted,
            fontFamily = FontFirst(),
            fontSize = 11.sp
        )
        if (owned.isEmpty()) {
            Text("Your pouch is empty. Claim achievements to earn jewels.", color = Cream, fontFamily = FontFirst(), fontSize = 13.sp)
        }
        owned.forEach { jewel ->
            val count = pouch[jewel.id] ?: 0
            val canCombine = count >= Jewels.COMBINE_COUNT && Jewels.upgradeOf(jewel) != null
            JewelRow(
                jewel = jewel,
                count = count,
                trailing = if (canCombine) {
                    {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(Brush.verticalGradient(listOf(Color(0xFFB98CFF), Color(0xFF6B3FC4))))
                                .border(2.dp, Outline, RoundedCornerShape(10.dp))
                                .clickable { onCombine(jewel.id) }
                                .padding(horizontal = 8.dp, vertical = 6.dp)
                        ) {
                            OutlinedText(text = "COMBINE", size = 11.sp, color = Color.White, strokeWidth = 4f)
                        }
                    }
                } else null
            )
        }
    }
}

@Composable
private fun Section(title: String, content: @Composable () -> Unit) {
    TownPanel(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            OutlinedText(text = title, size = 17.sp, color = GoldText, strokeWidth = 5f)
            content()
        }
    }
}
