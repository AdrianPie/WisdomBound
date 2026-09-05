package com.lernhero.home.quiz

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lernhero.domain.quiz.WordQuizBank
import com.lernhero.domain.quiz.custom.CustomDeck
import com.lernhero.domain.quiz.custom.DeckOrigin
import com.lernhero.shared.i18n.strings
import com.lernhero.shared.theme.FontFirst
import com.lernhero.shared.theme.FontPixel

/**
 * Zakladka "Wlasne" w konfiguracji endless: z czego gracz chce byc odpytany, gdy nie chce
 * wbudowanego banku.
 *
 * Dwa rodzaje wlasnego materialu stoja tu obok siebie jako rownorzedne kafle:
 * fiszki z czytelni (powstaja same, przy czytaniu) i talie pisane albo generowane w edytorze.
 * Wybor jest jeden - walka pyta z JEDNEGO zrodla, bo mieszanie "prawa jazdy" ze slowkami
 * z angielskiej powiesci nie jest zadnym trybem nauki.
 */
@Composable
fun CustomSourcePanel(
    eligibleWordCount: Int,
    decks: List<CustomDeck>,
    loading: Boolean,
    /** Null = wybrane sa fiszki z czytelni. */
    selectedDeckId: String?,
    onSelectWords: () -> Unit,
    onSelectDeck: (String) -> Unit,
    /** Nowa talia; parametr mowi, czy otworzyc edytor od razu z rozwinietym generowaniem. */
    onCreateDeck: (withAi: Boolean) -> Unit,
    onEditDeck: (String) -> Unit,
    onStart: () -> Unit,
    modifier: Modifier = Modifier
) {
    val wordsReady = eligibleWordCount >= WordQuizBank.MIN_WORDS
    val selectedDeck = decks.firstOrNull { it.id == selectedDeckId }
    val canStart = if (selectedDeckId == null) wordsReady else selectedDeck?.isPlayable == true

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        DeckSectionLabel(strings.deckSourceWordsTitle)
        SourceCard(
            icon = "🔖",
            title = strings.deckSourceWordsTitle,
            subtitle = if (wordsReady) {
                strings.deckSourceWordsReady(eligibleWordCount)
            } else {
                strings.deckSourceWordsNotReady(eligibleWordCount, WordQuizBank.MIN_WORDS)
            },
            selected = selectedDeckId == null,
            enabled = wordsReady,
            onClick = onSelectWords
        )

        DeckSectionLabel(strings.deckMyDecksLabel)

        // Wlasny, ograniczony wysokoscia kwadrat na liste talii - scrolluje sie TYLKO on, gdy
        // talii jest duzo. Reszta panelu (karta, przyciski, START) ma zostac na miejscu, zamiast
        // calej strony trzeba bylo przewijac, zeby dojsc do START - a wysokosc jest scietniej
        // dobrana, zeby caly panel (razem z dolnym paskiem nawigacji) miescil sie na ekranie bez
        // przewijania.
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 172.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            when {
                loading -> Hint(strings.deckLoading)
                decks.isEmpty() -> Hint(strings.deckEmptyHint)
                else -> decks.forEach { deck ->
                    DeckCard(
                        deck = deck,
                        selected = deck.id == selectedDeckId,
                        onSelect = { onSelectDeck(deck.id) },
                        onEdit = { onEditDeck(deck.id) }
                    )
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            DeckButton(
                label = strings.deckNewDeck,
                onClick = { onCreateDeck(false) },
                color = Color(0xFF3A2A18),
                textColor = DeckGold,
                fontSize = 11,
                modifier = Modifier.weight(1f)
            )
            DeckButton(
                label = strings.deckNewDeckWithAi,
                onClick = { onCreateDeck(true) },
                color = Color(0xFF2E3B6B),
                fontSize = 11,
                modifier = Modifier.weight(1f)
            )
        }

        DeckButton(
            label = strings.endlessStart,
            onClick = onStart,
            enabled = canStart,
            fontSize = 15,
            modifier = Modifier.fillMaxWidth()
        )

        if (!canStart) {
            Text(
                text = if (selectedDeckId == null) {
                    strings.deckWordsNotReadyHint(WordQuizBank.MIN_WORDS - eligibleWordCount)
                } else {
                    strings.deckNotReadyHint(CustomDeck.MIN_QUESTIONS)
                },
                color = DeckMuted,
                fontFamily = FontFirst(),
                fontSize = 10.sp
            )
        }
    }
}

@Composable
private fun SourceCard(
    icon: String,
    title: String,
    subtitle: String,
    selected: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
    trailing: @Composable (() -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(if (selected) Color(0xFF2A1F14) else DeckInk)
            .border(
                width = if (selected) 2.dp else 1.dp,
                color = if (selected) DeckGold else DeckFrameDim.copy(alpha = 0.6f),
                shape = RoundedCornerShape(10.dp)
            )
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 7.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = icon, fontSize = 16.sp)
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(1.dp)
        ) {
            Text(
                text = title,
                color = if (enabled) DeckGold else DeckMuted,
                fontFamily = FontPixel(),
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp
            )
            Text(
                text = subtitle,
                color = DeckMuted,
                fontFamily = FontFirst(),
                fontSize = 9.sp
            )
        }
        trailing?.invoke()
    }
}

@Composable
private fun DeckCard(
    deck: CustomDeck,
    selected: Boolean,
    onSelect: () -> Unit,
    onEdit: () -> Unit
) {
    val ready = deck.turnReady.size
    SourceCard(
        icon = if (deck.origin == DeckOrigin.Manual) "📝" else "✨",
        title = deck.displayName.uppercase(),
        subtitle = buildString {
            append(strings.deckReadyCount(ready))
            if (deck.brokenCount > 0) append(" | ${strings.deckBrokenCount(deck.brokenCount)}")
            append(" | ${deck.origin.label}")
        },
        selected = selected,
        enabled = true,
        onClick = onSelect,
        trailing = {
            // Olowek jest osobnym celem dotyku, bo klikniecie w karte ma WYBIERAC talie do walki,
            // a nie wchodzic w edycje - to dwie zupelnie rozne intencje w tym samym miejscu.
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF2A2018))
                    .border(1.dp, DeckFrameDim, RoundedCornerShape(8.dp))
                    .clickable(onClick = onEdit)
                    .padding(horizontal = 8.dp, vertical = 6.dp)
            ) {
                Text(text = "✎", color = DeckGold, fontSize = 12.sp)
            }
        }
    )
}

@Composable
private fun Hint(text: String) {
    Text(
        text = text,
        color = DeckMuted,
        fontFamily = FontFirst(),
        fontSize = 11.sp,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(DeckInk)
            .border(1.dp, DeckFrameDim.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
            .padding(10.dp)
    )
}
