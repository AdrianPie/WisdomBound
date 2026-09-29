package com.lernhero.home.progress

import com.lernhero.shared.theme.GlyphText
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lernhero.domain.jewel.Jewels
import com.lernhero.domain.progress.Achievement
import com.lernhero.domain.progress.AchievementCatalog
import com.lernhero.domain.progress.AchievementGroup
import com.lernhero.domain.progress.AchievementTier
import com.lernhero.domain.progress.GameStats
import com.lernhero.domain.quiz.QuizCategory
import com.lernhero.home.navigation.bottomBarSpace
import com.lernhero.home.player.PlayerViewModel
import com.lernhero.home.ui.ChunkyButton
import com.lernhero.home.ui.GlyphBadge
import com.lernhero.home.ui.GrooveBar
import com.lernhero.home.ui.JewelIcon
import com.lernhero.home.ui.JewelRow
import com.lernhero.home.ui.OutlinedText
import com.lernhero.home.ui.PaperCard
import com.lernhero.home.ui.PaperInk
import com.lernhero.home.ui.PaperLabel
import com.lernhero.home.ui.PaperTabs
import com.lernhero.home.ui.PaperTile
import com.lernhero.home.ui.PaperTitle
import com.lernhero.home.ui.PaperValue
import com.lernhero.home.ui.ParchmentPalette
import com.lernhero.reader.ReadingGoalsPanel
import com.lernhero.shared.theme.FontFirst

private val GoldText = Color(0xFFFFD34D)
private val Outline = Color(0xFF120B05)

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

        PaperTabs(
            labels = ProgressTab.entries.map { it.label },
            selected = tab.ordinal,
            onSelect = { tab = ProgressTab.entries[it] }
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                // Kazda zakladka zaczyna od gory - wspolny scroll otwieral Osiagniecia w polowie listy.
                .verticalScroll(remember(tab) { androidx.compose.foundation.ScrollState(0) }),
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

/** Kolor celnosci na papierze: zielony od 75%, bursztyn od 50%, ponizej czerwony. */
private fun accentFor(accuracy: Float): Color = when {
    accuracy >= 0.75f -> PaperInk.Good
    accuracy >= 0.5f -> PaperInk.Warn
    else -> PaperInk.Bad
}

/**
 * Naglowek: pierscien celnosci calego konta. To glowna miara postepu W NAUCE - poziom bohatera
 * mowi, ile sie GRALO, a ten pierscien, ile sie NAUCZYLO.
 */
@Composable
private fun LearningHeader(stats: GameStats) {
    val fill by animateFloatAsState(targetValue = stats.accuracy, animationSpec = tween(900))
    val accent = accentFor(stats.accuracy)

    PaperCard {
        Row(horizontalArrangement = Arrangement.spacedBy(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(88.dp), contentAlignment = Alignment.Center) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val stroke = 10.dp.toPx()
                    val arcSize = Size(size.width - stroke, size.height - stroke)
                    val topLeft = Offset(stroke / 2, stroke / 2)
                    drawArc(PaperInk.RingTrack, -90f, 360f, false, topLeft, arcSize, style = Stroke(stroke))
                    drawArc(accent, -90f, 360f * fill, false, topLeft, arcSize, style = Stroke(stroke, cap = StrokeCap.Round))
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    PaperValue(text = "${stats.accuracyPercent}%", size = 22)
                    PaperLabel("accuracy")
                }
            }
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                PaperLabel("Questions answered right")
                Row(verticalAlignment = Alignment.Bottom) {
                    PaperValue(text = "${stats.questionsCorrect}", size = 26)
                    GlyphText(
                        text = " / ${stats.questionsAnswered}",
                        color = PaperInk.Muted,
                        fontFamily = FontFirst(),
                        fontWeight = FontWeight.Black,
                        fontSize = 16.sp,
                        lineHeight = 22.sp
                    )
                }
                Row(
                    modifier = Modifier
                        .padding(top = 2.dp)
                        .clip(RoundedCornerShape(50))
                        .background(Brush.verticalGradient(listOf(Color(0xFFF08A3A), Color(0xFFC8502A))))
                        .border(1.dp, Color(0xFF8A3418), RoundedCornerShape(50))
                        .padding(horizontal = 10.dp, vertical = 3.dp),
                    horizontalArrangement = Arrangement.spacedBy(5.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    GlyphText("🔥", fontSize = 12.sp, lineHeight = 14.sp)
                    GlyphText(
                        text = "Best streak ${stats.bestAnswerStreak}",
                        color = Color.White,
                        fontFamily = FontFirst(),
                        fontWeight = FontWeight.Black,
                        fontSize = 11.sp,
                        lineHeight = 13.sp
                    )
                }
            }
        }
    }
}

// ------------------------------------------------------------ Statystyki

@Composable
private fun StatsTab(stats: GameStats) {
    Section(title = "Learning") {
        StatGrid(
            listOf(
                Stat("Correct", "${stats.questionsCorrect}", "✓", PaperInk.Good),
                Stat("Wrong", "${stats.questionsWrong}", "✕", PaperInk.Bad),
                Stat("Accuracy", "${stats.accuracyPercent}%", "%", accentFor(stats.accuracy)),
                Stat("Best streak", "${stats.bestAnswerStreak}", "★", Color(0xFFE0782A))
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
                    PaperTile(modifier = Modifier.fillMaxWidth(), accent = PaperInk.Bad) {
                        GlyphBadge(glyph = "!", color = PaperInk.Bad, size = 26.dp)
                        GlyphText(
                            text = "Weakest topic: ${category.label}. A few more rounds will fix it!",
                            color = PaperInk.Ink,
                            fontFamily = FontFirst(),
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            lineHeight = 15.sp
                        )
                    }
                }
            }
            categories.forEach { (category, stat) ->
                CategoryRow(
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
        StatGrid(
            listOf(
                Stat("Won", "${stats.battlesWon}"),
                Stat("Lost", "${stats.battlesLost}"),
                Stat("Endless best", "${stats.bestEndlessWave}"),
                Stat("Bosses", "${stats.bossesDefeated}"),
                Stat("Dungeons", "${stats.dungeonsCleared}"),
                Stat("Waves", "${stats.totalWavesCleared}")
            ),
            columns = 3
        )
    }

    Section(title = "Wealth") {
        StatGrid(
            listOf(
                Stat("Gold earned", compact(stats.goldEarned)),
                Stat("Upgrades", "${stats.itemsCrafted}"),
                Stat("Summoned", "${stats.monstersSummoned}")
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

/** [glyph] + [color] = kafelek z kolkiem jak na karcie atrybutow; bez nich - sama liczba. */
private data class Stat(val label: String, val value: String, val glyph: String? = null, val color: Color? = null)

@Composable
private fun StatGrid(stats: List<Stat>, columns: Int) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        stats.chunked(columns).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                row.forEach { stat ->
                    PaperTile(modifier = Modifier.weight(1f)) {
                        if (stat.glyph != null && stat.color != null) {
                            GlyphBadge(glyph = stat.glyph, color = stat.color, size = 32.dp)
                        }
                        Column {
                            PaperLabel(stat.label)
                            PaperValue(text = stat.value, color = stat.color?.takeIf { stat.glyph == "%" } ?: PaperInk.Ink)
                        }
                    }
                }
                repeat(columns - row.size) { Box(modifier = Modifier.weight(1f)) }
            }
        }
    }
}

/**
 * Celnosc jednej kategorii. Kolor jest informacja, nie ozdoba: ponizej 50% robi sie czerwony,
 * bo to jest wlasnie ta kategoria, ktora trzeba powtorzyc.
 */
@Composable
private fun CategoryRow(icon: String, label: String, correct: Int, answered: Int, accuracy: Float) {
    val color = accentFor(accuracy)
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(34.dp)
                .clip(RoundedCornerShape(9.dp))
                .background(ParchmentPalette.Card)
                .border(1.dp, ParchmentPalette.CardEdge, RoundedCornerShape(9.dp)),
            contentAlignment = Alignment.Center
        ) {
            GlyphText(icon, fontSize = 17.sp, lineHeight = 19.sp)
        }
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                GlyphText(
                    text = label,
                    color = PaperInk.Ink,
                    fontFamily = FontFirst(),
                    fontWeight = FontWeight.Black,
                    fontSize = 13.sp,
                    lineHeight = 15.sp,
                    modifier = Modifier.weight(1f)
                )
                GlyphText(
                    text = "$correct/$answered  ",
                    color = PaperInk.Muted,
                    fontFamily = FontFirst(),
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    lineHeight = 13.sp
                )
                GlyphText(
                    text = "${(accuracy * 100).toInt()}%",
                    color = color,
                    fontFamily = FontFirst(),
                    fontWeight = FontWeight.Black,
                    fontSize = 13.sp,
                    lineHeight = 15.sp
                )
            }
            GrooveBar(progress = accuracy, fill = color, height = 9.dp)
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
    QuizCategory.PopCulture -> "🎬"
    QuizCategory.Poland -> "🇵🇱"
    QuizCategory.Vocabulary -> "🔤"
    QuizCategory.Custom -> "✏️"
}

// ---------------------------------------------------------- Osiagniecia

private fun AchievementTier.color(): Color = when (this) {
    AchievementTier.Bronze -> Color(0xFFB0703A)
    AchievementTier.Silver -> Color(0xFF8FA8C4)
    AchievementTier.Gold -> PaperInk.Gold
}

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
            GlyphBadge(glyph = "★", color = PaperInk.Gold, size = 44.dp)
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(verticalAlignment = Alignment.Bottom) {
                    PaperValue(text = "$unlocked", size = 22)
                    GlyphText(" / $total", color = PaperInk.Muted, fontFamily = FontFirst(), fontWeight = FontWeight.Black, fontSize = 14.sp, lineHeight = 20.sp)
                }
                GrooveBar(
                    progress = unlocked.toFloat() / total.coerceAtLeast(1),
                    fill = PaperInk.Gold,
                    height = 16.dp,
                    label = "${(unlocked * 100 / total.coerceAtLeast(1))}%"
                )
            }
        }
        // Nieodebrane zloto stoi NA GORZE listy - inaczej gracz musialby przewinac caly ekran,
        // zeby w ogole zauwazyc, ze cos mu sie nalezy.
        if (pendingGold > 0) {
            PaperTile(modifier = Modifier.fillMaxWidth(), accent = PaperInk.Gold) {
                GlyphText("💰", fontSize = 18.sp, lineHeight = 20.sp)
                GlyphText(
                    text = "$pendingGold gold waiting to be claimed!",
                    color = PaperInk.Ink,
                    fontFamily = FontFirst(),
                    fontWeight = FontWeight.Black,
                    fontSize = 13.sp,
                    lineHeight = 15.sp
                )
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
    val tier = achievement.tier.color()

    PaperTile(modifier = Modifier.fillMaxWidth(), accent = if (claimable) PaperInk.Good else null) {
        // Medal w kolorze stopnia; zamkniete sa szare, zeby od razu bylo widac, co juz masz.
        GlyphBadge(glyph = "★", color = if (unlocked) tier else Color(0xFFC7B89E), size = 38.dp)
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
            GlyphText(
                text = achievement.title,
                color = if (unlocked) PaperInk.Ink else PaperInk.Muted,
                fontFamily = FontFirst(),
                fontWeight = FontWeight.Black,
                fontSize = 13.sp,
                lineHeight = 15.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            GlyphText(
                text = achievement.description,
                color = PaperInk.Muted,
                fontFamily = FontFirst(),
                fontSize = 11.sp,
                lineHeight = 13.sp
            )
            achievement.jewel?.takeIf { !claimed }?.let { jewel ->
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    JewelIcon(jewel = jewel, size = 16.dp)
                    GlyphText(text = jewel.name, color = Color(jewel.type.colorHex), fontFamily = FontFirst(), fontWeight = FontWeight.Bold, fontSize = 11.sp, lineHeight = 13.sp)
                }
            }
            if (!unlocked) {
                GrooveBar(
                    progress = achievement.fraction(stats),
                    fill = tier,
                    height = 16.dp,
                    label = "${achievement.progress(stats)}/${achievement.target}"
                )
            }
        }
        // Prawa krawedz wiersza ma trzy stany: postep -> przycisk ODBIERZ -> ptaszek.
        when {
            claimable -> ChunkyButton(text = "CLAIM", color = Color(0xFF4FAE3A), onClick = onClaim) {
                GlyphText(
                    text = "💰${achievement.reward} 💎${achievement.gems}",
                    color = Color.White,
                    fontFamily = FontFirst(),
                    fontWeight = FontWeight.Black,
                    fontSize = 10.sp,
                    lineHeight = 12.sp
                )
            }
            unlocked -> GlyphBadge(glyph = "✓", color = PaperInk.Good, size = 26.dp)
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
        GlyphText(
            text = "Jewels come from achievements. Set them into sockets on Uncommon or better gear. " +
                "Combine ${Jewels.COMBINE_COUNT} of the same jewel into a better cut.",
            color = PaperInk.Muted,
            fontFamily = FontFirst(),
            fontSize = 11.sp,
            lineHeight = 14.sp
        )
        if (owned.isEmpty()) {
            GlyphText("Your pouch is empty. Claim achievements to earn jewels.", color = PaperInk.Ink, fontFamily = FontFirst(), fontWeight = FontWeight.Bold, fontSize = 13.sp)
        }
        owned.forEach { jewel ->
            val count = pouch[jewel.id] ?: 0
            val canCombine = count >= Jewels.COMBINE_COUNT && Jewels.upgradeOf(jewel) != null
            JewelRow(
                jewel = jewel,
                count = count,
                trailing = if (canCombine) {
                    { ChunkyButton(text = "COMBINE", color = PaperInk.Purple, textSize = 11, onClick = { onCombine(jewel.id) }) }
                } else null
            )
        }
    }
}

@Composable
private fun Section(title: String, content: @Composable () -> Unit) {
    PaperCard {
        PaperTitle(title)
        content()
    }
}
