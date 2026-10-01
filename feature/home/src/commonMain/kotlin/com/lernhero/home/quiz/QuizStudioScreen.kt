package com.lernhero.home.quiz

import androidx.compose.ui.draw.drawBehind
import androidx.compose.foundation.layout.requiredSize
import com.lernhero.shared.resources.Resources
import androidx.compose.foundation.Image
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
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
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lernhero.domain.monetization.PremiumLimits
import com.lernhero.domain.quiz.custom.CustomDeck
import com.lernhero.domain.quiz.custom.DeckOrigin
import com.lernhero.home.navigation.bottomBarSpace
import com.lernhero.home.ui.ChunkyButton
import com.lernhero.home.ui.CountChip
import com.lernhero.home.ui.GrooveBar
import com.lernhero.home.ui.OutlinedText
import com.lernhero.home.ui.darkenBy
import com.lernhero.home.ui.lightenBy
import com.lernhero.shared.monetization.PremiumPaywall
import com.lernhero.shared.monetization.PaywallTrigger
import com.lernhero.shared.monetization.SubscriptionService
import com.lernhero.shared.theme.FontFirst
import com.lernhero.shared.theme.StrokedText
import org.koin.compose.koinInject

/**
 * Warsztat quizow - miejsce, w ktorym gracz robi material do nauki, a potem nim walczy.
 *
 * Poprzednia wersja ("Biblioteka Medrca") byla ladna jako scena, ale nie mowila, PO CO tu jestesmy:
 * medrzec rzucal ogolnik w dymku, a z gotowa talia nie dalo sie tu nic zrobic poza edycja - zeby nia
 * zagrac, trzeba bylo wiedziec, ze wybiera sie ja w Walce -> Endless -> pigulka materialu. Teraz:
 *  - naglowek mowi wprost, do czego sluzy ekran, i pokazuje trzy kroki, ktore same sie odhaczaja,
 *  - kazda gotowa talia ma przycisk BATTLE, ktory ustawia ja jako material wyprawy i przenosi na
 *    Walke ([onPlayDeck]); niegotowa - "dodaj pytania", czyli jedyna rzecz, jakiej jej brakuje.
 */
@Composable
fun QuizStudioScreen(
    viewModel: CustomQuizViewModel,
    onEditDeck: (deckId: String, startWithAi: Boolean) -> Unit,
    onPlayDeck: (deckId: String) -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    val isPremium by viewModel.isPremium.collectAsState()
    /** Paywall otwarty z konkretnego powodu - null = zamkniety. */
    var paywall by remember { mutableStateOf<PaywallTrigger?>(null) }

    paywall?.let { trigger ->
        PremiumPaywall(
            subscription = koinInject<SubscriptionService>(),
            trigger = trigger,
            reason = if (trigger == PaywallTrigger.QuizCreation) {
                "Free players can keep ${PremiumLimits.FREE_CUSTOM_DECKS} own deck - you've already made yours."
            } else {
                null
            },
            onDismiss = { paywall = null }
        )
    }

    // Wlasna talia: pierwsza [PremiumLimits.FREE_CUSTOM_DECKS] za darmo, kolejne z Premium. Istniejace
    // talie zawsze da sie edytowac i nimi grac - blokujemy tylko TWORZENIE nowych.
    val freeDecksLeft = (PremiumLimits.FREE_CUSTOM_DECKS - state.decks.size).coerceAtLeast(0)
    val canWrite = isPremium || freeDecksLeft > 0
    val startAi = { if (isPremium) onEditDeck(viewModel.newDeck(), true) else paywall = PaywallTrigger.AiDecks }
    val startManual = { if (canWrite) onEditDeck(viewModel.newDeck(), false) else paywall = PaywallTrigger.QuizCreation }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 14.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Miejsce na czubek kapelusza czarodzieja, ktory wystaje ponad panel.
        Spacer(modifier = Modifier.height(WIZARD_LIFT))
        WorkshopHeader(decks = state.decks)

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            CreateButton(
                icon = Resources.UiArt.writeQuiz,
                title = "Write",
                subtitle = "Type your own\nquestions",
                color = Color(0xFF3C82D0),
                badge = when {
                    isPremium -> null
                    canWrite -> "$freeDecksLeft FREE"
                    else -> "PREMIUM"
                },
                onClick = startManual,
                modifier = Modifier.weight(1f)
            )
            CreateButton(
                icon = Resources.UiArt.aiQuiz,
                title = "AI Deck",
                subtitle = "Give a topic,\nAI writes it",
                color = Color(0xFF8A52CC),
                badge = if (isPremium) null else "PREMIUM",
                onClick = startAi,
                modifier = Modifier.weight(1f)
            )
        }

        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedText(text = "Your Decks", size = 20.sp, color = DeckGold, strokeWidth = 5f)
            if (state.decks.isNotEmpty()) CountChip("${state.decks.size}")
        }

        when {
            state.loading -> Hint("Dusting off the shelves...")
            state.decks.isEmpty() -> EmptyShelf(onCreate = startManual)
            else -> state.decks.forEach { deck ->
                DeckCard(
                    deck = deck,
                    onEdit = { onEditDeck(deck.id, false) },
                    onPlay = { onPlayDeck(deck.id) }
                )
            }
        }

        // Ogon pod dolny pasek - lista przewija sie ZA niego, patrz [bottomBarSpace].
        Spacer(modifier = Modifier.height(bottomBarSpace()))
    }
}

/**
 * Naglowek: medrzec w okraglym oknie (obracajace sie promienie zostaja - sa lubiane), tytul z jednym
 * zdaniem "po co" i trzy kroki od zera do walki. Kroki odhaczaja sie z samych talii, wiec nowy gracz
 * widzi, co zrobic dalej, a stary - ze wszystko ma juz za soba.
 */
@Composable
private fun WorkshopHeader(decks: List<CustomDeck>) {
    val shape = RoundedCornerShape(18.dp)
    Column(
        // Bez clip - czarodziej celowo wychodzi poza ramke panelu (tlo i obwodka rysuja sie ksztaltem).
        modifier = Modifier
            .fillMaxWidth()
            .background(Brush.verticalGradient(listOf(Color(0xFF342760), Color(0xFF1B1433))), shape)
            // Obwodka POD trescia (drawBehind), nie border - border rysuje sie na wierzchu
            // i przecinal czarodzieja, ktory wychodzi poza panel.
            .drawBehind {
                val stroke = 2.dp.toPx()
                drawRoundRect(
                    color = DeckFrame,
                    topLeft = androidx.compose.ui.geometry.Offset(stroke / 2, stroke / 2),
                    size = androidx.compose.ui.geometry.Size(size.width - stroke, size.height - stroke),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(18.dp.toPx()),
                    style = androidx.compose.ui.graphics.drawscope.Stroke(stroke)
                )
            }
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
            SagePortrait()
            // Odstep od czarodzieja, ktory wychodzi poza swoje kolko w prawo.
            Column(modifier = Modifier.weight(1f).padding(start = 46.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                StrokedText(
                    text = "Your Quizzes",
                    size = 21.sp,
                    color = DeckGold,
                    outline = Color(0xFF120B05),
                    strokeWidth = 5f,
                    maxLines = 1
                )
                Text(
                    text = "Make your own questions - then fight monsters with them.",
                    color = DeckCream,
                    fontFamily = FontFirst(),
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    lineHeight = 15.sp
                )
            }
        }

        val hasDeck = decks.isNotEmpty()
        val hasPlayable = decks.any { it.isPlayable }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0x66000000))
                .padding(horizontal = 6.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Step(number = 1, label = "Create\na deck", done = hasDeck, current = !hasDeck, modifier = Modifier.weight(1f))
            StepArrow()
            Step(
                number = 2,
                label = "Add ${CustomDeck.MIN_QUESTIONS}+\nquestions",
                done = hasPlayable,
                current = hasDeck && !hasPlayable,
                modifier = Modifier.weight(1f)
            )
            StepArrow()
            Step(number = 3, label = "Battle\nwith it", done = false, current = hasPlayable, modifier = Modifier.weight(1f))
        }
    }
}

@Composable
private fun SagePortrait() {
    val infinite = rememberInfiniteTransition()
    val bob by infinite.animateFloat(0f, 1f, infiniteRepeatable(tween(1800, easing = FastOutSlowInEasing), RepeatMode.Reverse))

    // Sam czarodziej, bez ramki - wiekszy niz jego miejsce w ukladzie (requiredSize) i wychodzi
    // ponad gorna krawedz panelu.
    Box(modifier = Modifier.size(82.dp), contentAlignment = Alignment.Center) {
        Image(
            painter = painterResource(Resources.UiArt.quizWizard),
            contentDescription = null,
            modifier = Modifier
                .requiredSize(WIZARD_SIZE)
                .graphicsLayer {
                    translationX = 12f * density
                    translationY = (-WIZARD_LIFT.value + 3f - bob * 5f) * density
                }
        )
    }
}

/** Jeden krok przewodnika: kolko z numerem (albo ptaszkiem) i podpis. Biezacy krok swieci na zloto. */
@Composable
private fun Step(number: Int, label: String, done: Boolean, current: Boolean, modifier: Modifier = Modifier) {
    val fill = when {
        done -> Brush.verticalGradient(listOf(Color(0xFF7BE06A), Color(0xFF2E9A3E)))
        current -> Brush.verticalGradient(listOf(Color(0xFFFFE27A), Color(0xFFE0A21E)))
        else -> Brush.verticalGradient(listOf(Color(0xFF3A3150), Color(0xFF241D36)))
    }
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(30.dp)
                .clip(CircleShape)
                .background(fill)
                .border(2.dp, Color(0xFF120B05), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            StrokedText(
                text = if (done) "✓" else "$number",
                size = 14.sp,
                color = if (done || current) Color.White else DeckMuted,
                outline = Color(0xFF120B05),
                strokeWidth = if (done || current) 4f else 0f,
                tight = true
            )
        }
        Text(
            text = label,
            color = when {
                current -> DeckGold
                done -> DeckCream
                else -> DeckMuted
            },
            fontFamily = FontFirst(),
            fontWeight = FontWeight.Bold,
            fontSize = 10.sp,
            lineHeight = 12.sp,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun StepArrow() {
    Text(
        text = "›",
        color = DeckFrame,
        fontSize = 22.sp,
        fontWeight = FontWeight.Black,
        modifier = Modifier.padding(bottom = 18.dp)
    )
}

/**
 * Wejscie "napisz" / "AI" jako gruby przycisk z warga pod spodem - wyglada na wciskany.
 * Jedna ikona w ciemnym okienku, bez poswiaty i migajacych gwiazdek.
 */
@Composable
private fun CreateButton(
    icon: DrawableResource,
    title: String,
    subtitle: String,
    color: Color,
    badge: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(16.dp)
    Box(modifier = modifier.padding(top = ICON_LIFT + 6.dp)) {
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
                .padding(top = 0.dp, bottom = 12.dp, start = 10.dp, end = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            // Miejsce na ikone - sama ikona lezy NAD przyciskiem (nizej), bo przycisk jest
            // przyciety do ksztaltu i ucialby jej czesc wystajaca ponad krawedz.
            Spacer(modifier = Modifier.height(ICON_SIZE - ICON_LIFT))
            OutlinedText(text = title, size = 18.sp, color = Color.White, strokeWidth = 5f)
            Text(
                text = subtitle,
                color = Color.White.copy(alpha = 0.9f),
                fontFamily = FontFirst(),
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp,
                lineHeight = 13.sp,
                textAlign = TextAlign.Center
            )
        }
        // Duza ikona wystaje ponad gorna krawedz przycisku - to ona ma przyciagac wzrok.
        Image(
            painter = painterResource(icon),
            contentDescription = null,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .offset(y = -ICON_LIFT)
                .size(ICON_SIZE)
        )
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
 * Talia jako ksiega: okladka w kolorze pochodzenia (AI - fiolet, wlasna - niebieski), obok nazwa,
 * temat i stan. Najwazniejsza rzecz na karcie to jedno pytanie "czy moge tym juz walczyc?":
 * gotowa talia ma zielony BATTLE, niegotowa - pasek do [CustomDeck.MIN_QUESTIONS] i przycisk
 * dodania pytan. Tapniecie w reszte karty otwiera edytor, jak dotad.
 */
@Composable
private fun DeckCard(deck: CustomDeck, onEdit: () -> Unit, onPlay: () -> Unit) {
    val cover = when (deck.origin) {
        DeckOrigin.Manual -> Color(0xFF2F7FC8)
        DeckOrigin.Ai -> Color(0xFF7B4FC9)
        DeckOrigin.AiEdited -> Color(0xFF5B5FD0)
    }
    val ready = deck.turnReady.size
    val playable = deck.isPlayable
    val shape = RoundedCornerShape(16.dp)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(Brush.verticalGradient(listOf(Color(0xFF2B2016), DeckPanel)))
            .border(2.dp, if (playable) DeckFrame else DeckFrameDim, shape)
            .clickable(onClick = onEdit)
            .padding(10.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        DeckCover(color = cover, origin = deck.origin)

        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(5.dp)) {
            Text(
                text = deck.displayName,
                color = DeckCream,
                fontFamily = FontFirst(),
                fontWeight = FontWeight.Black,
                fontSize = 16.sp,
                lineHeight = 18.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = deck.topic.ifBlank { deck.origin.label },
                color = DeckMuted,
                fontFamily = FontFirst(),
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp,
                lineHeight = 13.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (playable) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    StatusChip(text = "✓ READY", color = Color(0xFF2E9A3E))
                    Text(
                        text = "$ready questions",
                        color = DeckCream,
                        fontFamily = FontFirst(),
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp
                    )
                }
            } else {
                GrooveBar(
                    progress = ready.toFloat() / CustomDeck.MIN_QUESTIONS,
                    fill = Color(0xFFE0A21E),
                    height = 17.dp,
                    label = "$ready / ${CustomDeck.MIN_QUESTIONS} questions"
                )
            }
            if (deck.brokenCount > 0) {
                Text(
                    text = "${deck.brokenCount} unfinished - tap to fix",
                    color = DeckRed,
                    fontFamily = FontFirst(),
                    fontWeight = FontWeight.Bold,
                    fontSize = 10.sp,
                    lineHeight = 12.sp
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(top = 2.dp)) {
                if (playable) {
                    ChunkyButton(
                        text = "⚔️ BATTLE",
                        color = Color(0xFF3FA84A),
                        textSize = 14,
                        onClick = onPlay,
                        modifier = Modifier.weight(1f)
                    )
                    ChunkyButton(
                        text = "EDIT",
                        color = Color(0xFF8A6A44),
                        textSize = 13,
                        onClick = onEdit
                    )
                } else {
                    ChunkyButton(
                        text = "+ ADD QUESTIONS",
                        color = Color(0xFFE0852A),
                        textSize = 13,
                        onClick = onEdit,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

/** Okladka ksiegi: skora w kolorze pochodzenia talii, dwa zlote paski i znak pochodzenia. */
@Composable
private fun DeckCover(color: Color, origin: DeckOrigin) {
    val shape = RoundedCornerShape(10.dp)
    Box(
        modifier = Modifier
            .width(62.dp)
            .height(86.dp)
            .clip(shape)
            .background(Brush.horizontalGradient(listOf(color.darkenBy(0.35f), color, color.darkenBy(0.25f))))
            .border(2.dp, color.darkenBy(0.55f), shape),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.fillMaxHeight().padding(vertical = 9.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Box(modifier = Modifier.width(62.dp).height(3.dp).background(Color(0xFFE0B75A)))
            Box(modifier = Modifier.width(62.dp).height(3.dp).background(Color(0xFFE0B75A)))
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Image(
                painter = painterResource(if (origin == DeckOrigin.Manual) Resources.UiArt.writeQuiz else Resources.UiArt.aiQuiz),
                contentDescription = null,
                modifier = Modifier.size(40.dp)
            )
            StrokedText(
                text = if (origin == DeckOrigin.Manual) "OWN" else "AI",
                size = 11.sp,
                color = Color(0xFFFFE08A),
                outline = color.darkenBy(0.6f),
                strokeWidth = 4f,
                tight = true
            )
        }
    }
}

@Composable
private fun StatusChip(text: String, color: Color) {
    Text(
        text = text,
        color = Color.White,
        fontFamily = FontFirst(),
        fontWeight = FontWeight.Black,
        fontSize = 10.sp,
        lineHeight = 12.sp,
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(color)
            .border(1.dp, color.darkenBy(0.45f), RoundedCornerShape(6.dp))
            .padding(horizontal = 6.dp, vertical = 2.dp)
    )
}

@Composable
private fun EmptyShelf(onCreate: () -> Unit) {
    val shape = RoundedCornerShape(16.dp)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(Brush.verticalGradient(listOf(Color(0xFF2B2016), DeckPanel)))
            .border(2.dp, DeckFrameDim, shape)
            .padding(18.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(text = "📚", fontSize = 40.sp)
        Text("No decks yet", color = DeckCream, fontFamily = FontFirst(), fontWeight = FontWeight.Black, fontSize = 16.sp)
        Text(
            text = "A school subject, a language, a driving test - anything you want to remember. " +
                "Write ${CustomDeck.MIN_QUESTIONS} questions and it's ready for battle.",
            color = DeckMuted,
            fontFamily = FontFirst(),
            fontSize = 12.sp,
            lineHeight = 15.sp,
            textAlign = TextAlign.Center
        )
        ChunkyButton(
            text = "CREATE MY FIRST DECK",
            color = Color(0xFF3C82D0),
            textSize = 13,
            onClick = onCreate,
            modifier = Modifier.padding(top = 4.dp)
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

/** Ikona przycisku tworzenia i to, o ile wystaje ponad jego gorna krawedz. */
private val ICON_SIZE = 104.dp
private val ICON_LIFT = 26.dp

/** Czarodziej w naglowku - dwa razy wiekszy od kolka i o tyle podniesiony ponad panel. */
private val WIZARD_SIZE = 160.dp
private val WIZARD_LIFT = 30.dp
