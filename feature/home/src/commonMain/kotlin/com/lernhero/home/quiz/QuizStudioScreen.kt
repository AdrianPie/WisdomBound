package com.lernhero.home.quiz

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
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
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lernhero.domain.monetization.PremiumLimits
import com.lernhero.domain.quiz.custom.CustomDeck
import com.lernhero.domain.quiz.custom.DeckOrigin
import com.lernhero.home.navigation.bottomBarSpace
import com.lernhero.home.ui.CountChip
import com.lernhero.home.ui.GrooveBar
import com.lernhero.home.ui.OutlinedText
import com.lernhero.home.ui.PaperCard
import com.lernhero.home.ui.PaperInk
import com.lernhero.home.ui.ParchmentPalette
import com.lernhero.home.ui.darkenBy
import com.lernhero.home.ui.lightenBy
import com.lernhero.shared.monetization.PremiumPaywall
import com.lernhero.shared.monetization.SubscriptionService
import com.lernhero.shared.theme.FontFirst
import org.koin.compose.koinInject


/**
 * Biblioteka Medrca - miejsce, w ktorym gracz robi material do nauki, zamiast tylko go zuzywac.
 *
 * Na gorze scena z medrcem (placeholder do podmiany na grafike budynku/postaci), pod nia dwa
 * wejscia: "napisz sam" i "zwoj AI" (Premium - bez subskrypcji otwiera od razu paywall, zamiast
 * wpuszczac do edytora, zeby tam dopiero powiedziec "nie"). Nizej polka z taliami jak ksiegi.
 */
@Composable
fun QuizStudioScreen(
    viewModel: CustomQuizViewModel,
    onEditDeck: (deckId: String, startWithAi: Boolean) -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    val isPremium by viewModel.isPremium.collectAsState()
    var paywallOpen by remember { mutableStateOf(false) }

    if (paywallOpen) {
        PremiumPaywall(
            subscription = koinInject<SubscriptionService>(),
            reason = "The AI Scroll writes whole decks for you - up to ${PremiumLimits.PREMIUM_AI_QUIZZES_PER_MONTH} a month.",
            onDismiss = { paywallOpen = false }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 14.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        SageScene(deckCount = state.decks.size)

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            CreateButton(
                icon = "🪶",
                title = "Write",
                subtitle = "Your questions,\nyour rules",
                color = Color(0xFF3C82D0),
                badge = null,
                onClick = { onEditDeck(viewModel.newDeck(), false) },
                modifier = Modifier.weight(1f)
            )
            CreateButton(
                icon = "📜",
                title = "AI Scroll",
                subtitle = "Give a topic,\nget a deck",
                color = Color(0xFF8A52CC),
                badge = if (isPremium) null else "PREMIUM",
                onClick = {
                    if (isPremium) onEditDeck(viewModel.newDeck(), true) else paywallOpen = true
                },
                modifier = Modifier.weight(1f)
            )
        }

        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedText(text = "Your Tomes", size = 20.sp, color = DeckGold, strokeWidth = 5f)
            if (state.decks.isNotEmpty()) CountChip("${state.decks.size}")
        }

        when {
            state.loading -> Hint("Dusting off the shelves...")
            state.decks.isEmpty() -> EmptyShelf()
            else -> state.decks.forEach { deck ->
                TomeCard(deck = deck, onClick = { onEditDeck(deck.id, false) })
            }
        }

        // Ogon pod dolny pasek - lista przewija sie ZA niego, patrz [bottomBarSpace].
        Spacer(modifier = Modifier.height(bottomBarSpace()))
    }
}

/**
 * Naglowek Biblioteki: medrzec w okraglym "oknie" (obracajace sie promienie zostaja - sa lubiane)
 * i jego dymek na pergaminie. Wczesniej to byla fioletowa scena ze zlota ramka, polprzezroczystymi
 * ksiazkami i poswiata - wygladala jak z generatora, a nie jak czesc gry.
 */
@Composable
private fun SageScene(deckCount: Int) {
    val infinite = rememberInfiniteTransition()
    val spin by infinite.animateFloat(0f, 360f, infiniteRepeatable(tween(24_000, easing = LinearEasing)))
    val bob by infinite.animateFloat(0f, 1f, infiniteRepeatable(tween(1800, easing = FastOutSlowInEasing), RepeatMode.Reverse))

    PaperCard {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
            // TODO grafika: podmienic emoji medrca na Image(painterResource(Resources.Image.sage)).
            Box(
                modifier = Modifier
                    .size(104.dp)
                    .clip(CircleShape)
                    .background(ParchmentPalette.Frame)
                    .padding(3.dp)
                    .clip(CircleShape)
                    .background(Brush.verticalGradient(listOf(Color(0xFF34285A), Color(0xFF1A1330))))
                    .border(1.5.dp, Color(0xFFE0B75A), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize().rotate(spin)) {
                    val r = size.minDimension * 0.7f
                    for (i in 0 until 12) {
                        rotate(i * 30f) {
                            val path = Path().apply {
                                moveTo(center.x, center.y)
                                lineTo(center.x - r * 0.1f, center.y - r)
                                lineTo(center.x + r * 0.1f, center.y - r)
                                close()
                            }
                            drawPath(path, Brush.radialGradient(listOf(Color(0x55FFD98A), Color.Transparent), center, r))
                        }
                    }
                }
                Text(
                    text = "🧙",
                    fontSize = 60.sp,
                    modifier = Modifier.graphicsLayer { translationY = (4f - bob * 6f) * density }
                )
            }
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "The Sage's Library",
                    color = ParchmentPalette.Ink,
                    fontFamily = FontFirst(),
                    fontWeight = FontWeight.Black,
                    fontSize = 17.sp,
                    lineHeight = 19.sp
                )
                SpeechBubble(
                    text = when {
                        deckCount == 0 -> "Every hero writes their own spellbook. Shall we start yours?"
                        deckCount < 3 -> "A fine start! The more you write, the sharper your mind."
                        else -> "$deckCount tomes already! The library grows wiser with you."
                    }
                )
            }
        }
    }
}

/** Dymek z ogonkiem wskazujacym w lewo, na medrca. */
@Composable
private fun SpeechBubble(text: String) {
    Box {
        Text(
            text = text,
            color = ParchmentPalette.Ink,
            fontFamily = FontFirst(),
            fontWeight = FontWeight.Bold,
            fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
            fontSize = 12.sp,
            lineHeight = 15.sp,
            modifier = Modifier
                .padding(start = 7.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(ParchmentPalette.Card)
                .border(1.dp, ParchmentPalette.CardEdge, RoundedCornerShape(12.dp))
                .padding(horizontal = 10.dp, vertical = 8.dp)
        )
        Canvas(modifier = Modifier.size(width = 9.dp, height = 14.dp).align(Alignment.CenterStart)) {
            val tail = Path().apply {
                moveTo(size.width + 1f, 0f)
                lineTo(0f, size.height / 2f)
                lineTo(size.width + 1f, size.height)
                close()
            }
            drawPath(tail, ParchmentPalette.Card)
            drawLine(ParchmentPalette.CardEdge, androidx.compose.ui.geometry.Offset(size.width, 0f), androidx.compose.ui.geometry.Offset(0f, size.height / 2f), 1.dp.toPx())
            drawLine(ParchmentPalette.CardEdge, androidx.compose.ui.geometry.Offset(0f, size.height / 2f), androidx.compose.ui.geometry.Offset(size.width, size.height), 1.dp.toPx())
        }
    }
}

/**
 * Wejscie "napisz" / "zwoj AI" jako gruby przycisk z warga pod spodem - wyglada na wciskany.
 * Jedna ikona w ciemnym okienku, bez poswiaty i migajacych gwiazdek.
 */
@Composable
private fun CreateButton(
    icon: String,
    title: String,
    subtitle: String,
    color: Color,
    badge: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(16.dp)
    Box(modifier = modifier.padding(top = 8.dp)) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(shape)
                .background(color.darkenBy(0.45f))
                .padding(bottom = 4.dp)
                .clip(shape)
                .background(Brush.verticalGradient(listOf(color.lightenBy(0.18f), color, color.darkenBy(0.12f))))
                .border(2.dp, color.darkenBy(0.5f), shape)
                .clickable(onClick = onClick)
                .padding(vertical = 12.dp, horizontal = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(color.darkenBy(0.4f))
                    .border(1.5.dp, Color.White.copy(alpha = 0.35f), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(text = icon, fontSize = 28.sp, lineHeight = 30.sp)
            }
            OutlinedText(text = title, size = 18.sp, color = Color.White, strokeWidth = 5f)
            Text(
                text = subtitle,
                color = Color.White.copy(alpha = 0.9f),
                fontFamily = FontFirst(),
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp,
                lineHeight = 13.sp,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
        badge?.let {
            Text(
                text = it,
                color = Color(0xFF3A1E00),
                fontFamily = FontFirst(),
                fontWeight = FontWeight.Black,
                fontSize = 10.sp,
                lineHeight = 12.sp,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = 4.dp, y = (-8).dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(Brush.verticalGradient(listOf(Color(0xFFFFE27A), Color(0xFFE0A21E))))
                    .border(1.5.dp, Color(0xFF8A5A0E), RoundedCornerShape(6.dp))
                    .padding(horizontal = 7.dp, vertical = 2.dp)
            )
        }
    }
}

/**
 * Talia jako ksiega: skorzany grzbiet z inicjalem (AI - fiolet, reczna - braz), a obok pergamin
 * z tytulem, tematem i paskiem "gotowa do walki". Liczba gotowych pytan jest tu wazniejsza od
 * nazwy, bo mowi, czy talia nadaje sie juz do boju; niedokonczone pytania pokazujemy na czerwono.
 */
@Composable
private fun TomeCard(deck: CustomDeck, onClick: () -> Unit) {
    val ai = deck.origin == DeckOrigin.Ai
    val leather = if (ai) Color(0xFF6E3FB0) else Color(0xFF8A4E22)
    val ready = deck.turnReady.size
    val playable = deck.isPlayable
    val outer = RoundedCornerShape(14.dp)
    val inner = RoundedCornerShape(11.5.dp)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(92.dp)
            .clip(outer)
            .background(ParchmentPalette.Frame)
            .padding(2.5.dp)
            .clip(inner)
            .background(Brush.verticalGradient(listOf(ParchmentPalette.Paper, ParchmentPalette.PaperDark)))
            .clickable(onClick = onClick),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Grzbiet ksiegi: skora, dwa zlote paski i inicjal tytulu.
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .width(52.dp)
                .background(Brush.horizontalGradient(listOf(leather.darkenBy(0.25f), leather, leather.darkenBy(0.35f)))),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier.fillMaxHeight().padding(vertical = 12.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Box(modifier = Modifier.width(52.dp).height(3.dp).background(Color(0xFFE0B75A)))
                Box(modifier = Modifier.width(52.dp).height(3.dp).background(Color(0xFFE0B75A)))
            }
            OutlinedText(
                text = if (ai) "AI" else deck.name.trim().firstOrNull()?.uppercase() ?: "?",
                size = 20.sp,
                color = Color(0xFFFFE08A),
                strokeWidth = 5f
            )
        }
        Column(
            modifier = Modifier.weight(1f).padding(horizontal = 12.dp),
            verticalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            Text(
                text = deck.name.ifBlank { "Untitled tome" },
                color = ParchmentPalette.Ink,
                fontFamily = FontFirst(),
                fontWeight = FontWeight.Black,
                fontSize = 15.sp,
                lineHeight = 17.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = deck.topic.ifBlank { "No topic" },
                color = ParchmentPalette.InkMuted,
                fontFamily = FontFirst(),
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp,
                lineHeight = 13.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            GrooveBar(
                progress = (ready.toFloat() / CustomDeck.MIN_QUESTIONS).coerceAtMost(1f),
                fill = if (playable) PaperInk.Good else PaperInk.Warn,
                height = 17.dp,
                label = if (playable) "$ready questions · ready!" else "$ready / ${CustomDeck.MIN_QUESTIONS} to battle"
            )
            if (deck.brokenCount > 0) {
                Text(
                    text = "${deck.brokenCount} unfinished",
                    color = PaperInk.Bad,
                    fontFamily = FontFirst(),
                    fontWeight = FontWeight.Bold,
                    fontSize = 10.sp,
                    lineHeight = 12.sp
                )
            }
        }
        Text(
            text = "›",
            color = ParchmentPalette.Frame,
            fontSize = 26.sp,
            fontWeight = FontWeight.Black,
            modifier = Modifier.padding(end = 12.dp)
        )
    }
}

@Composable
private fun EmptyShelf() {
    PaperCard {
        Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text("The shelf is empty", color = ParchmentPalette.Ink, fontFamily = FontFirst(), fontWeight = FontWeight.Black, fontSize = 15.sp)
            Text(
                text = "Write your first tome above - a school subject, a driving test, anything you want to remember.",
                color = ParchmentPalette.InkMuted,
                fontFamily = FontFirst(),
                fontSize = 12.sp,
                lineHeight = 15.sp,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}

@Composable
private fun Hint(text: String) {
    Text(
        text = text,
        color = DeckMuted,
        fontFamily = FontFirst(),
        fontSize = 12.sp,
        modifier = Modifier.padding(vertical = 8.dp)
    )
}
