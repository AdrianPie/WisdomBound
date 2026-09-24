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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lernhero.domain.progress.CategoryLevels
import com.lernhero.domain.quiz.QuizCategory
import com.lernhero.domain.quiz.WordQuizBank
import com.lernhero.domain.quiz.custom.CustomDeck
import com.lernhero.domain.quiz.custom.DeckOrigin
import com.lernhero.home.story.CategoryMasteryList
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
        title = "STUDY MATERIAL",
        subtitle = "What the run will ask you about.",
        onClose = onClose
    ) {
        SheetSectionLabel("Your material")

        val wordsReady = eligibleWordCount >= WordQuizBank.MIN_WORDS
        MaterialRow(
            icon = "🗂",
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
                icon = "✍",
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
        SheetHint("Each topic keeps its own level: higher level starts harder and pays more.")

        CategoryMasteryList(
            categories = QuizCategory.builtinFor(strings.languageCode),
            levels = levels,
            selected = selected.topic(),
            // Wiersz "wszystkie tematy" jest zaznaczony TYLKO wtedy, gdy naprawde nim gramy -
            // przy wybranej talii czy fiszkach zaden temat nie moze udawac wybranego.
            mixedSelected = selected == RunMaterial.Mixed,
            onSelect = { category ->
                onSelect(category?.let { RunMaterial.Topic(it) } ?: RunMaterial.Mixed)
            }
        )

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
 * Jeden wiersz wyboru. Ten sam ksztalt co wiersz tematu ([CategoryMasteryList]) - material gracza
 * i wbudowany bank sa dwoma rodzajami TEGO SAMEGO wyboru, wiec nie moga wygladac jak dwa systemy.
 */
@Composable
private fun MaterialRow(
    icon: String,
    title: String,
    subtitle: String,
    selected: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
    subtitleTint: Color = SheetMuted
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(if (selected) SheetHeaderBg else SheetInk)
            .border(
                width = if (selected) 2.dp else 1.dp,
                color = if (selected) SheetGold else SheetFrameDim,
                shape = RoundedCornerShape(12.dp)
            )
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(CircleShape)
                .background(SheetHeaderBg)
                .border(2.dp, if (selected) SheetGold else SheetFrameDim, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(text = icon, fontSize = 16.sp)
        }
        Column(
            modifier = Modifier.padding(start = 10.dp).weight(1f),
            verticalArrangement = Arrangement.spacedBy(1.dp)
        ) {
            Text(
                text = title,
                color = if (enabled) SheetCream else SheetMuted,
                fontFamily = FontFirst(),
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = subtitle,
                color = subtitleTint,
                fontFamily = FontFirst(),
                fontSize = 10.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
        if (selected) {
            Text(
                text = "✓",
                color = SheetGold,
                fontFamily = FontFirst(),
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
        }
    }
}
