package com.lernhero.game.battle.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameMillis
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lernhero.domain.combat.live.AnswerTimer
import com.lernhero.domain.combat.live.StreakTracker
import com.lernhero.game.battle.BattleUiState
import com.lernhero.game.battle.QuizAnswer
import com.lernhero.shared.theme.FontFirst
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlin.time.TimeSource

/** Paleta jasnego, "kartowego" panelu quizu - odbite od ciemnej, zlotej reszty gry, zeby pytanie wyraznie odcinalo sie od pola bitwy i bylo jak najlatwiej czytelne. */
private val PanelBg = Color(0xFFF3E8D4)
private val PanelBorder = Color(0xFFC8A45A)
private val InkStrong = Color(0xFF2A1D12)
private val InkMuted = Color(0xFF8A7859)
private val BubbleBg = Color(0xFF241A2E)
private val BubbleText = Color(0xFFF6EEDD)

/** Kolory ponumerowanych krazkow przy odpowiedziach - stale przypisanie do pozycji 1-4, jak w grach z quizami. */
private val OptionColors = listOf(
    Color(0xFF8B5CF6), // 1 - fiolet
    Color(0xFF22A55E), // 2 - zielen
    Color(0xFF2F8FE0), // 3 - blekit
    Color(0xFFE08A2F) // 4 - pomarancz
)

/**
 * Panel quizu pokazywany w fazie [com.lernhero.game.battle.TurnPhase.Question] - walka stoi,
 * dopoki nie padnie odpowiedz. Pasek nad pytaniem odlicza czas na odpowiedz: im szybciej klikniesz,
 * tym mocniejsza salwa poleci w tej turze.
 *
 * Ekran jest teraz pionowy (portrait), wiec panel jest waski i wysoki - pytanie + metryki siedza
 * u gory, a siatka 2x2 odpowiedzi zajmuje cala szerokosc ponizej, zamiast dzielic ja na pol z
 * pytaniem jak w starym, poziomym ukladzie.
 */
@Composable
fun LiveQuizBar(
    state: BattleUiState,
    onAnswer: (String) -> Unit,
    onContinue: () -> Unit,
    modifier: Modifier = Modifier
) {
    val quiz = state.quiz ?: return
    val question = quiz.question

    // Krotka chwila na zobaczenie, ktora odpowiedz byla poprawna - potem od razu kolejne pytanie.
    LaunchedEffect(question.id, quiz.revealed) {
        if (!quiz.revealed) return@LaunchedEffect
        delay(QUIZ_REVEAL_MILLIS)
        onContinue()
    }

    Surface(
        modifier = modifier.fillMaxSize(),
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        color = PanelBg,
        border = androidx.compose.foundation.BorderStroke(2.dp, PanelBorder)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                StreakPipRow(streak = state.streak)

                AnswerCountdownBar(questionId = question.id, revealed = quiz.revealed)

                QuestionBubble(prompt = question.prompt)

                question.flag?.let { flag ->
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        color = Color.White,
                        border = androidx.compose.foundation.BorderStroke(1.dp, PanelBorder)
                    ) {
                        Image(
                            painter = org.jetbrains.compose.resources.painterResource(flag),
                            contentDescription = question.prompt,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(64.dp)
                                .padding(8.dp),
                            contentScale = ContentScale.Fit
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    QuizBadge(text = question.category, color = Color(0xFF2F8FE0))
                    QuizBadge(text = question.difficulty, color = difficultyColor(question.difficulty))
                    QuizBadge(text = "+${question.reward} pkt", color = Color(0xFFE0A93A))
                }

                if (quiz.revealed) {
                    RevealBanner(
                        correct = quiz.answeredCorrectly,
                        correctText = quiz.correctAnswer?.text.orEmpty(),
                        reward = question.reward
                    )
                }
            }

            // Siatka 2x2 na pelnej szerokosci ekranu, ponizej pytania - kazdy wiersz zajmuje
            // polowe pozostalej wysokosci, stabilnie niezaleznie od dlugosci tekstu odpowiedzi.
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                question.answers.chunked(2).forEachIndexed { rowIndex, rowAnswers ->
                    Row(
                        modifier = Modifier.weight(1f).fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        rowAnswers.forEachIndexed { columnIndex, answer ->
                            val number = rowIndex * 2 + columnIndex + 1
                            AnswerTile(
                                number = number,
                                answer = answer,
                                revealed = quiz.revealed,
                                picked = quiz.selectedAnswerId == answer.id,
                                onClick = { onAnswer(answer.id) },
                                modifier = Modifier.weight(1f).fillMaxHeight()
                            )
                        }
                    }
                }
            }
        }
    }
}

/** Dymek pytania - ciemna, kontrastowa "karteczka" nad odpowiedziami, jak w grze z reference. */
@Composable
private fun QuestionBubble(prompt: String, modifier: Modifier = Modifier) {
    Column(modifier = modifier.fillMaxWidth()) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            color = BubbleBg
        ) {
            Text(
                text = prompt,
                color = BubbleText,
                fontFamily = FontFirst(),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp)
            )
        }
        // Ogonek dymka - maly trojkat pod lewa krawedzia, wskazujacy w strone bohatera nad panelem.
        Box(
            modifier = Modifier
                .padding(start = 22.dp)
                .size(14.dp)
                .clip(BubbleTailShape)
                .background(BubbleBg)
        )
    }
}

private val BubbleTailShape = androidx.compose.foundation.shape.GenericShape { size, _ ->
    moveTo(0f, 0f)
    lineTo(size.width, 0f)
    lineTo(size.width / 2f, size.height)
    close()
}

/**
 * Pasek czasu na odpowiedz tuz nad pytaniem - startuje pelny (5 segmentow) w chwili pojawienia
 * sie pytania i opada segment po segmencie. Im wiecej segmentow zostalo w chwili odpowiedzi, tym
 * mocniej drużyna uderzy w tej turze - stad kolor jedzie od zielonego (pelna moc) do czerwonego
 * (ostatni, najslabszy segment).
 */
@Composable
internal fun AnswerCountdownBar(questionId: String, revealed: Boolean, modifier: Modifier = Modifier) {
    var remainingFraction by remember(questionId) { mutableFloatStateOf(1f) }

    LaunchedEffect(questionId, revealed) {
        if (revealed) return@LaunchedEffect
        val start = TimeSource.Monotonic.markNow()
        while (isActive) {
            val elapsedMs = start.elapsedNow().inWholeMilliseconds
            remainingFraction = (1f - elapsedMs.toFloat() / AnswerTimer.WINDOW_MS).coerceIn(0f, 1f)
            if (remainingFraction <= 0f) break
            withFrameMillis {}
        }
    }

    // Po odpowiedzi petla wyzej sie zatrzymuje, wiec remainingFraction zamraza sie dokladnie na
    // tej wartosci, ktora policzyla mnoznik obrazen tej tury - pasek pokazuje to, co faktycznie trafilo.
    val litSegments = AnswerTimer.segmentIndexFor(remainingFraction) + 1
    val leadColor by animateColorAsState(segmentColor(litSegments - 1))

    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = "SILA CIOSU",
            color = InkMuted,
            fontFamily = FontFirst(),
            fontWeight = FontWeight.Bold,
            fontSize = 10.sp
        )

        Row(
            modifier = Modifier
                .padding(top = 4.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            repeat(AnswerTimer.SEGMENTS) { index ->
                val lit = index < litSegments
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(12.dp)
                        .clip(RoundedCornerShape(50))
                        .background(if (lit) segmentColor(index) else Color(0xFFE0D2B0))
                        .border(1.dp, if (lit) leadColor else PanelBorder, RoundedCornerShape(50))
                )
            }
        }
    }
}

/** Kolor kazdego segmentu paska - od czerwonego (ostatni, slaby) po zielony (pelna moc). */
private fun segmentColor(index: Int): Color = when (index) {
    4 -> Color(0xFF22A55E)
    3 -> Color(0xFF7CB342)
    2 -> Color(0xFFE0A93A)
    1 -> Color(0xFFE08A2F)
    else -> Color(0xFFD1453D)
}

/**
 * Skrocony, zwiniety pasek pokazywany zamiast [LiveQuizBar] w trakcie [com.lernhero.game.battle.TurnPhase.Resolving] -
 * druzyny wymieniaja ciosy, quiz jest na chwile schowany, wiec zostaje tylko krotki status i seria.
 */
@Composable
fun ResolvingStrip(state: BattleUiState, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier.fillMaxSize(),
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        color = PanelBg,
        border = androidx.compose.foundation.BorderStroke(2.dp, PanelBorder)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            StreakPipRow(streak = state.streak)
            Text(
                text = state.message ?: "Wymiana ciosow...",
                color = InkStrong,
                fontFamily = FontFirst(),
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp
            )
        }
    }
}

/**
 * Dziesiec kratek ostatnich odpowiedzi - zielona za dobra, czerwona za zla, szara jeszcze pusta.
 * Piec zielonych z rzedu (nawet miedzy cyklami) odpala ulti gdzie indziej w viewmodelu.
 */
@Composable
private fun StreakPipRow(streak: StreakTracker) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        streak.pips.forEach { pip ->
            Box(
                modifier = Modifier
                    .size(13.dp)
                    .clip(CircleShape)
                    .background(
                        when (pip) {
                            true -> Color(0xFF22A55E)
                            false -> Color(0xFFD1453D)
                            null -> Color(0xFFDDCCA8)
                        }
                    )
            )
        }
    }
}

@Composable
private fun QuizBadge(text: String, color: Color) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(color.copy(alpha = 0.14f))
            .border(1.dp, color, RoundedCornerShape(50))
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(
            text = text,
            color = color,
            fontFamily = FontFirst(),
            fontWeight = FontWeight.Bold,
            fontSize = 11.sp
        )
    }
}

/** Im wyzszy szczebel drabinki, tym cieplejszy kolor - jak w Milionerach. */
private fun difficultyColor(label: String): Color = when (label) {
    "Latwe" -> Color(0xFF22A55E)
    "Srednie" -> Color(0xFFE0A93A)
    "Trudne" -> Color(0xFFE08A2F)
    else -> Color(0xFFD1453D)
}

/** Ile czasu quiz stoi po odpowiedzi, zebys zdazyl zobaczyc, ktora byla dobra. */
private const val QUIZ_REVEAL_MILLIS = 550L

private val AnswerGreen = Color(0xFF22A55E)
private val AnswerRed = Color(0xFFD1453D)
private val AnswerFadedBg = Color(0xFFEADFC6)
private val AnswerFadedText = Color(0xFFB1A283)

/**
 * Kafelek odpowiedzi w siatce 2x2: kolorowy ponumerowany krazek nad tekstem, wypelnia cala swoja
 * cwiartke. Do momentu odpowiedzi wszystkie wygladaja tak samo; po odslonie poprawna zapala sie na
 * zielono, zle klikniecie na czerwono, a reszta gasnie.
 */
@Composable
private fun AnswerTile(
    number: Int,
    answer: QuizAnswer,
    revealed: Boolean,
    picked: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val badgeColor = OptionColors[(number - 1).coerceIn(0, OptionColors.size - 1)]

    val container by animateColorAsState(
        when {
            !revealed -> Color.White
            answer.isCorrect -> AnswerGreen
            picked -> AnswerRed
            else -> AnswerFadedBg
        }
    )
    val content = when {
        !revealed -> InkStrong
        answer.isCorrect || picked -> Color.White
        else -> AnswerFadedText
    }
    val borderColor = when {
        !revealed -> PanelBorder
        answer.isCorrect -> AnswerGreen
        picked -> AnswerRed
        else -> AnswerFadedBg
    }

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(container)
            .border(2.dp, borderColor, RoundedCornerShape(14.dp))
            .then(if (!revealed) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(horizontal = 10.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Box(
            modifier = Modifier
                .size(26.dp)
                .clip(CircleShape)
                .background(badgeColor),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "$number",
                color = Color.White,
                fontFamily = FontFirst(),
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp
            )
        }
        Text(
            text = answer.text,
            color = content,
            fontFamily = FontFirst(),
            fontWeight = if (revealed && answer.isCorrect) FontWeight.Bold else FontWeight.Normal,
            fontSize = 13.sp,
            maxLines = 3,
            modifier = Modifier.weight(1f)
        )
    }
}

/** Jednozdaniowy werdykt pod odpowiedziami. */
@Composable
private fun RevealBanner(
    correct: Boolean,
    correctText: String,
    reward: Int
) {
    val color = if (correct) AnswerGreen else AnswerRed
    val text = if (correct) "Dobrze! +$reward pkt" else "Zle. Poprawna: $correctText"

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(color.copy(alpha = 0.14f))
            .border(1.dp, color, RoundedCornerShape(12.dp))
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Text(
            text = text,
            color = color,
            fontFamily = FontFirst(),
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp
        )
    }
}
