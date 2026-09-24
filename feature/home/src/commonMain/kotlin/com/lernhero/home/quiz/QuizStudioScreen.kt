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
import com.lernhero.home.city.TownProgressBar
import com.lernhero.home.navigation.bottomBarSpace
import com.lernhero.home.ui.OutlinedText
import com.lernhero.shared.monetization.PremiumPaywall
import com.lernhero.shared.monetization.SubscriptionService
import com.lernhero.shared.theme.FontFirst
import org.koin.compose.koinInject

private val Outline = Color(0xFF120B05)
private val Parchment = Color(0xFFF1DFB8)

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
            CreateCard(
                icon = "🪶",
                title = "Write",
                subtitle = "Your questions,\nyour rules",
                colors = listOf(Color(0xFF3C8FE0), Color(0xFF1E4F95)),
                badge = null,
                onClick = { onEditDeck(viewModel.newDeck(), false) },
                modifier = Modifier.weight(1f)
            )
            CreateCard(
                icon = "📜",
                title = "AI Scroll",
                subtitle = "Give a topic,\nget a deck",
                colors = listOf(Color(0xFF9A4FD6), Color(0xFF4A1E86)),
                badge = if (isPremium) null else "👑 PREMIUM",
                sparkle = true,
                onClick = {
                    if (isPremium) onEditDeck(viewModel.newDeck(), true) else paywallOpen = true
                },
                modifier = Modifier.weight(1f)
            )
        }

        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedText(text = "Your Tomes", size = 20.sp, color = DeckGold, strokeWidth = 5f)
            if (state.decks.isNotEmpty()) {
                Text(
                    text = "${state.decks.size}",
                    color = Color.White,
                    fontFamily = FontFirst(),
                    fontWeight = FontWeight.Black,
                    fontSize = 12.sp,
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(Color(0xE60C0806))
                        .border(1.5.dp, Outline, RoundedCornerShape(50))
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                )
            }
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
 * Scena medrca. Grafika postaci/budynku jeszcze nie istnieje - do tego czasu medrca gra emoji na
 * tle magicznego kregu, a podmiana na PNG to jedna linia w miejscu oznaczonym ponizej.
 */
@Composable
private fun SageScene(deckCount: Int) {
    val infinite = rememberInfiniteTransition()
    val spin by infinite.animateFloat(0f, 360f, infiniteRepeatable(tween(24_000, easing = LinearEasing)))
    val bob by infinite.animateFloat(0f, 1f, infiniteRepeatable(tween(1800, easing = FastOutSlowInEasing), RepeatMode.Reverse))
    val glow by infinite.animateFloat(0.5f, 1f, infiniteRepeatable(tween(1400), RepeatMode.Reverse))

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(210.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(Brush.verticalGradient(listOf(Color(0xFF2B1B4E), Color(0xFF1A1030), Color(0xFF0F0A1C))))
            .border(3.dp, Brush.verticalGradient(listOf(DeckGold, Color(0xFFE08A1E))), RoundedCornerShape(20.dp))
            .clipToBounds()
    ) {
        // Magiczny krag za medrcem.
        Canvas(modifier = Modifier.size(260.dp).align(Alignment.CenterStart).offset(x = (-50).dp).rotate(spin)) {
            val r = size.minDimension / 2f
            for (i in 0 until 12) {
                rotate(i * 30f) {
                    val path = Path().apply {
                        moveTo(center.x, center.y)
                        lineTo(center.x - r * 0.08f, center.y - r)
                        lineTo(center.x + r * 0.08f, center.y - r)
                        close()
                    }
                    drawPath(path, Brush.radialGradient(listOf(Color(0x66B48BFF), Color.Transparent), center, r))
                }
            }
            drawCircle(Color(0x55FFD34D), radius = r * 0.62f, style = androidx.compose.ui.graphics.drawscope.Stroke(width = 3f))
            drawCircle(Color(0x33FFD34D), radius = r * 0.5f, style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2f))
        }
        // Polki z ksiegami w tle po prawej.
        Text(
            text = "📚",
            fontSize = 64.sp,
            modifier = Modifier.align(Alignment.BottomEnd).padding(end = 10.dp, bottom = 6.dp).graphicsLayer { alpha = 0.35f }
        )

        // TODO grafika: podmienic emoji medrca na Image(painterResource(Resources.Image.sage)).
        Box(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(start = 18.dp)
                .size(140.dp)
                .graphicsLayer { translationY = -bob * 6f * density },
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .graphicsLayer { alpha = glow }
                    .background(Brush.radialGradient(listOf(Color(0x88FFD34D), Color.Transparent)), CircleShape)
            )
            Text(text = "🧙", fontSize = 92.sp)
        }

        // Dymek medrca.
        Column(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 16.dp, end = 14.dp)
                .width(170.dp)
        ) {
            Text(
                text = when {
                    deckCount == 0 -> "Every hero writes their own spellbook. Shall we start yours?"
                    deckCount < 3 -> "A fine start! The more you write, the sharper your mind."
                    else -> "$deckCount tomes already! The library grows wiser with you."
                },
                color = Color(0xFF3A2410),
                fontFamily = FontFirst(),
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                modifier = Modifier
                    .clip(RoundedCornerShape(14.dp))
                    .background(Parchment)
                    .border(2.5.dp, Outline, RoundedCornerShape(14.dp))
                    .padding(horizontal = 12.dp, vertical = 10.dp)
            )
        }
        OutlinedText(
            text = "The Sage's Library",
            size = 20.sp,
            color = DeckGold,
            strokeWidth = 5f,
            modifier = Modifier.align(Alignment.BottomEnd).padding(end = 14.dp, bottom = 12.dp)
        )
    }
}

@Composable
private fun CreateCard(
    icon: String,
    title: String,
    subtitle: String,
    colors: List<Color>,
    badge: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    sparkle: Boolean = false
) {
    val infinite = rememberInfiniteTransition()
    val bob by infinite.animateFloat(0f, 1f, infiniteRepeatable(tween(1500, easing = FastOutSlowInEasing), RepeatMode.Reverse))
    val twinkle by infinite.animateFloat(0f, 1f, infiniteRepeatable(tween(900), RepeatMode.Reverse))

    Box(modifier = modifier.padding(top = 8.dp)) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(18.dp))
                .background(Brush.verticalGradient(colors))
                .border(3.dp, Outline, RoundedCornerShape(18.dp))
                .clickable(onClick = onClick)
                .padding(vertical = 14.dp, horizontal = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.size(64.dp)) {
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .background(Brush.radialGradient(listOf(Color(0x66FFFFFF), Color.Transparent)), CircleShape)
                )
                Text(text = icon, fontSize = 40.sp, modifier = Modifier.graphicsLayer { translationY = -bob * 4f * density })
                if (sparkle) {
                    Text("✨", fontSize = 14.sp, modifier = Modifier.align(Alignment.TopEnd).graphicsLayer { alpha = twinkle })
                    Text("✨", fontSize = 10.sp, modifier = Modifier.align(Alignment.BottomStart).graphicsLayer { alpha = 1f - twinkle })
                }
            }
            OutlinedText(text = title, size = 20.sp, color = Color.White)
            Text(
                text = subtitle,
                color = Color.White.copy(alpha = 0.85f),
                fontFamily = FontFirst(),
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp,
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
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .offset(y = (-8).dp)
                    .clip(RoundedCornerShape(50))
                    .background(Brush.horizontalGradient(listOf(Color(0xFFFFE27A), Color(0xFFE0A21E))))
                    .border(2.dp, Outline, RoundedCornerShape(50))
                    .padding(horizontal = 10.dp, vertical = 3.dp)
            )
        }
    }
}

/**
 * Talia jako ksiega na polce: kolorowy grzbiet (AI - fiolet, reczna - braz), tytul, temat i pasek
 * "gotowa do walki". Liczba gotowych pytan jest tu wazniejsza od nazwy, bo mowi, czy talia nadaje
 * sie juz do boju; niedokonczone pytania pokazujemy na czerwono.
 */
@Composable
private fun TomeCard(deck: CustomDeck, onClick: () -> Unit) {
    val ai = deck.origin == DeckOrigin.Ai
    val spine = if (ai) listOf(Color(0xFF9A4FD6), Color(0xFF4A1E86)) else listOf(Color(0xFFB8702E), Color(0xFF6B3A12))
    val ready = deck.turnReady.size
    val playable = deck.isPlayable

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(Brush.horizontalGradient(listOf(Color(0xFF3A2A1C), Color(0xFF1C130C))))
            .border(2.5.dp, Outline, RoundedCornerShape(14.dp))
            .clickable(onClick = onClick),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Grzbiet ksiegi.
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .width(58.dp)
                .background(Brush.horizontalGradient(spine)),
            contentAlignment = Alignment.Center
        ) {
            Column(modifier = Modifier.fillMaxHeight().padding(vertical = 10.dp), verticalArrangement = Arrangement.SpaceBetween) {
                Box(modifier = Modifier.width(42.dp).height(3.dp).background(DeckGold.copy(alpha = 0.8f)))
                Box(modifier = Modifier.width(42.dp).height(3.dp).background(DeckGold.copy(alpha = 0.8f)))
            }
            Text(text = if (ai) "📜" else "📕", fontSize = 26.sp)
        }
        Column(
            modifier = Modifier.weight(1f).padding(horizontal = 12.dp),
            verticalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            Text(
                text = deck.name.ifBlank { "Untitled tome" },
                color = Color.White,
                fontFamily = FontFirst(),
                fontWeight = FontWeight.Black,
                fontSize = 16.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = deck.topic.ifBlank { "No topic" },
                color = DeckMuted,
                fontFamily = FontFirst(),
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            TownProgressBar(
                progress = (ready.toFloat() / CustomDeck.MIN_QUESTIONS).coerceAtMost(1f),
                fill = if (playable) Color(0xFF6FD04A) else Color(0xFFFFB02E),
                height = 22.dp,
                label = if (playable) "$ready questions · ready!" else "$ready / ${CustomDeck.MIN_QUESTIONS} to battle"
            )
            if (deck.brokenCount > 0) {
                Text(
                    text = "${deck.brokenCount} unfinished",
                    color = DeckRed,
                    fontFamily = FontFirst(),
                    fontWeight = FontWeight.Bold,
                    fontSize = 10.sp
                )
            }
        }
        Text(
            text = "›",
            color = DeckGold,
            fontSize = 28.sp,
            fontWeight = FontWeight.Black,
            modifier = Modifier.padding(end = 12.dp)
        )
    }
}

@Composable
private fun EmptyShelf() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Color(0x99000000))
            .border(2.dp, DeckFrameDim, RoundedCornerShape(14.dp))
            .padding(22.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text("📖", fontSize = 36.sp)
        OutlinedText(text = "The shelf is empty", size = 16.sp, color = Color.White, strokeWidth = 4f)
        Text(
            text = "Write your first tome above - a school subject, a driving test, anything you want to remember.",
            color = DeckMuted,
            fontFamily = FontFirst(),
            fontSize = 12.sp,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
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
