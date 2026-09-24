package com.lernhero.home.fight

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import kotlin.math.roundToInt
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lernhero.domain.progress.CategoryLevels
import com.lernhero.domain.quiz.QuizCategory
import com.lernhero.domain.quiz.WordQuizBank
import com.lernhero.domain.quiz.custom.CustomDeck
import com.lernhero.domain.quiz.custom.DeckOrigin
import com.lernhero.domain.progress.CategoryMastery
import com.lernhero.shared.i18n.strings
import com.lernhero.shared.isDebugBuild
import com.lernhero.shared.theme.FontFirst
import com.lernhero.shared.theme.FontPixel

/**
 * Wybor materialu na wyprawe - jedna lista, jedno tapniecie, okno sie zamyka.
 *
 * Cala trudnosc byla nie w tym, CO pokazac, tylko ile na raz. Poprzedni panel endless mial dwie
 * zakladki zrodla, cztery szczeble trudnosci, liste trzynastu tematow i osobna liste wlasnych
 * talii - wszystko widoczne naraz, na ekranie, przez ktory trzeba bylo przejsc PRZED kazda walka.
 *
 * Tutaj wybor jest jeden, a okno otwiera sie tylko wtedy, gdy gracz sam po nie siegnie (pigulka
 * nad przyciskiem START). Kolejnosc sekcji jest wazniejsza, niz wyglada: WLASNY material stoi
 * na gorze, bo to on jest powodem, dla ktorego ktos pisze wlasne quizy - gdyby stal pod trzynastoma
 * tematami, nikt by do niego nie doscrollowal.
 */
@Composable
fun MaterialSheet(
    selected: RunMaterial,
    decks: List<CustomDeck>,
    decksLoading: Boolean,
    /** Ile fiszek z czytelni nadaje sie na pytania - patrz [WordQuizBank.eligible]. */
    eligibleWordCount: Int,
    levels: CategoryLevels,
    onSelect: (RunMaterial) -> Unit,
    /** Talia bez kompletu pytan prowadzi do edytora, a nie w slepy zaulek. */
    onEditDeck: (deckId: String) -> Unit,
    onOpenQuizzes: () -> Unit,
    onClose: () -> Unit,
    /** Debug: walka z bossem od razu, z pominieciem fal. */
    onBossTest: () -> Unit = {}
) {
    FightSheet(
        title = "Study material",
        subtitle = "What the run will ask you about.",
        onClose = onClose
    ) {
        SheetSectionLabel("Your material")

        val wordsReady = eligibleWordCount >= WordQuizBank.MIN_WORDS
        MaterialRow(
            icon = "🗂️",
            tint = Color(0xFF6FD08C),
            title = "Saved words",
            subtitle = if (wordsReady) {
                "$eligibleWordCount flashcards from the reader"
            } else {
                "Save ${WordQuizBank.MIN_WORDS - eligibleWordCount} more words in the reader"
            },
            selected = selected == RunMaterial.Words,
            enabled = wordsReady,
            onClick = { onSelect(RunMaterial.Words) }
        )

        when {
            decksLoading -> SheetHint("Loading your decks...")

            decks.isEmpty() -> MaterialRow(
                icon = "✍️",
                tint = Color(0xFFB38CFF),
                title = "Write your own quiz",
                subtitle = "Your questions, your run - opens the Quizzes tab",
                selected = false,
                enabled = true,
                onClick = onOpenQuizzes
            )

            else -> decks.forEach { deck ->
                val playable = deck.isPlayable
                MaterialRow(
                    icon = if (deck.origin == DeckOrigin.Manual) "📝" else "✨",
                    tint = Color(0xFFB38CFF),
                    title = deck.displayName,
                    subtitle = if (playable) {
                        "${deck.turnReady.size} questions ready" +
                            if (deck.brokenCount > 0) " · ${deck.brokenCount} unfinished" else ""
                    } else {
                        "Needs ${CustomDeck.MIN_QUESTIONS} ready questions - tap to finish it"
                    },
                    subtitleTint = if (playable) SheetMuted else SheetRed,
                    selected = (selected as? RunMaterial.Deck)?.deckId == deck.id,
                    enabled = true,
                    // Talia bez kompletu pytan nie jest "zepsuta" - jest niedokonczona, wiec
                    // tapniecie prowadzi tam, gdzie da sie ja dokonczyc.
                    onClick = {
                        if (playable) onSelect(RunMaterial.Deck(deck.id)) else onEditDeck(deck.id)
                    }
                )
            }
        }

        SheetSectionLabel("Topics")

        // "Wszystkie tematy" jest zaznaczone TYLKO wtedy, gdy naprawde nim gramy - przy wybranej
        // talii czy fiszkach zaden temat nie moze udawac wybranego.
        MaterialRow(
            icon = "🎲",
            title = "All topics",
            subtitle = "Mixed questions - no topic level",
            selected = selected == RunMaterial.Mixed,
            enabled = true,
            tint = SheetGold,
            onClick = { onSelect(RunMaterial.Mixed) }
        )

        TopicGrid(
            categories = QuizCategory.builtinFor(strings.languageCode),
            levels = levels,
            selected = selected.topic(),
            onSelect = { onSelect(RunMaterial.Topic(it)) }
        )
        SheetHint("Each topic levels up on its own - a higher level starts harder and pays more.")

        if (isDebugBuild) {
            Text(
                text = "DEV · BOSS TEST",
                color = SheetInk,
                fontFamily = FontPixel(),
                fontWeight = FontWeight.Bold,
                fontSize = 10.sp,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(SheetGold)
                    .clickable(onClick = onBossTest)
                    .padding(vertical = 8.dp),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}

/**
 * Jeden wiersz wyboru - karta z ikona w kafelku. Wybrana dostaje zlota ramke i znaczek.
 */
@Composable
private fun MaterialRow(
    icon: String,
    title: String,
    subtitle: String,
    selected: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
    subtitleTint: Color = SheetMuted,
    tint: Color = Color(0xFF5AA9FF)
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(if (selected) CardSelected else Card)
            .border(
                width = if (selected) 1.5.dp else 1.dp,
                color = if (selected) SheetGold else CardEdge,
                shape = RoundedCornerShape(16.dp)
            )
            .clickable(enabled = enabled, onClick = onClick)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconTile(icon = icon, tint = if (enabled) tint else SheetMuted)
        Column(
            modifier = Modifier.padding(start = 12.dp).weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = title,
                color = if (enabled) SheetCream else SheetMuted,
                fontFamily = FontFirst(),
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = subtitle,
                color = subtitleTint,
                fontFamily = FontFirst(),
                fontSize = 11.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
        SelectMark(selected)
    }
}

@Composable
private fun IconTile(icon: String, tint: Color, size: Int = 42) {
    Box(
        modifier = Modifier
            .size(size.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(tint.copy(alpha = 0.16f))
            .border(1.dp, tint.copy(alpha = 0.4f), RoundedCornerShape(12.dp)),
        contentAlignment = Alignment.Center
    ) {
        Text(text = icon, fontSize = (size * 0.45f).sp)
    }
}

/** Kolko po prawej: puste obramowanie albo zloty znaczek - wybor widac bez czytania. */
@Composable
private fun SelectMark(selected: Boolean) {
    Box(
        modifier = Modifier
            .size(22.dp)
            .clip(CircleShape)
            .background(if (selected) SheetGold else Color.Transparent)
            .border(1.5.dp, if (selected) SheetGold else CardEdge, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        if (selected) {
            Text(text = "✓", color = SheetInk, fontWeight = FontWeight.Bold, fontSize = 12.sp)
        }
    }
}

/**
 * Tematy w dwoch kolumnach. Kafel: ikona tematu, nazwa, poziom tematu w kolorze progu trudnosci
 * i premia do nagrod - wszystko, co wczesniej stalo w trzynastu dlugich wierszach.
 */
@Composable
private fun TopicGrid(
    categories: List<QuizCategory>,
    levels: CategoryLevels,
    selected: QuizCategory?,
    onSelect: (QuizCategory) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        categories.chunked(2).forEach { pair ->
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                pair.forEach { category ->
                    TopicTile(
                        category = category,
                        level = levels.levelOf(category),
                        selected = selected == category,
                        onClick = { onSelect(category) },
                        modifier = Modifier.weight(1f)
                    )
                }
                if (pair.size == 1) Spacer(modifier = Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun TopicTile(
    category: QuizCategory,
    level: Int,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val difficulty = CategoryMastery.startingDifficulty(level)
    val tier = TierColors[difficulty.ordinal.coerceIn(0, TierColors.lastIndex)]
    val bonusPercent = ((CategoryMastery.rewardMultiplier(level) - 1f) * 100).roundToInt()

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(
                if (selected) Brush.verticalGradient(listOf(CardSelected, Card))
                else Brush.verticalGradient(listOf(Card, Card))
            )
            .border(
                width = if (selected) 1.5.dp else 1.dp,
                color = if (selected) SheetGold else CardEdge,
                shape = RoundedCornerShape(16.dp)
            )
            .clickable(onClick = onClick)
            .padding(10.dp),
        verticalArrangement = Arrangement.spacedBy(0.dp)
    ) {
        // Wybor widac po zlotej ramce - w ciasnym kaflu nie ma miejsca na osobny znaczek.
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconTile(icon = category.icon, tint = tier, size = 38)
            Column(
                modifier = Modifier.padding(start = 10.dp).weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = category.label,
                    color = if (selected) SheetGold else SheetCream,
                    fontFamily = FontFirst(),
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Row(horizontalArrangement = Arrangement.spacedBy(5.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Lv $level",
                        color = Color.White,
                        fontFamily = FontFirst(),
                        fontWeight = FontWeight.Bold,
                        fontSize = 9.sp,
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .background(tier.copy(alpha = 0.85f))
                            .padding(horizontal = 6.dp, vertical = 1.dp)
                    )
                    Text(
                        text = if (bonusPercent > 0) "+$bonusPercent%" else difficulty.label,
                        color = if (bonusPercent > 0) SheetGold else tier,
                        fontFamily = FontFirst(),
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp,
                        maxLines = 1
                    )
                }
            }
        }
    }
}

/** Barwa progu, od ktorego startuja pytania tematu - zielony latwy, czerwony ekspert. */
private val TierColors = listOf(
    Color(0xFF4FA96B), // Easy
    Color(0xFF2F8FE0), // Medium
    Color(0xFF9B6BE0), // Hard
    Color(0xFFE0654F) // Expert
)

private val Card = Color(0xFF1E1610)
private val CardSelected = Color(0xFF33261A)
private val CardEdge = Color(0xFF3A2E20)
