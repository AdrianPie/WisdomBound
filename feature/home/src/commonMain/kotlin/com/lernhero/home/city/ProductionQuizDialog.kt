package com.lernhero.home.city

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lernhero.domain.quiz.QuizBank
import com.lernhero.domain.quiz.QuizEntry
import com.lernhero.domain.town.ProductionQuiz
import com.lernhero.shared.theme.AppPalette
import com.lernhero.shared.theme.FontFirst
import com.lernhero.shared.theme.FontPixel
import kotlinx.coroutines.delay
import kotlin.random.Random

private val Good = Color(0xFF22A55E)
private val Bad = Color(0xFFD1453D)

/**
 * Quiz produkcyjny: [ProductionQuiz.QUESTION_COUNT] pytan po kolei, a na koncu bonus zalezny od
 * liczby trafien. To on zastapil dawny przycisk, ktory dawal +25% za samo klikniecie.
 *
 * Pytania sa losowane RAZ, przy otwarciu ([remember] na `attemptId`) - inaczej kazda rekompozycja
 * (odpowiedz, tik zegara miasta w tle) podmienialaby pytanie pod palcem gracza.
 *
 * Po kliknieciu odpowiedzi panel na chwile pokazuje, co bylo poprawne, i sam przechodzi dalej -
 * bez tego gracz nie mialby jak sie niczego nauczyc, a to jest cel calej tej mechaniki.
 */
@Composable
internal fun ProductionQuizDialog(
    attemptId: Int,
    onFinished: (correct: Int, boostPercent: Int) -> Unit,
    onDismiss: () -> Unit
) {
    val questions = remember(attemptId) {
        QuizBank.draw(ProductionQuiz.LADDER, category = null, random = Random.Default)
    }
    // Kolejnosc odpowiedzi tez losujemy raz - poprawna nie moze zawsze stac pierwsza.
    val shuffled = remember(attemptId) {
        questions.map { entry -> entry.answers.shuffled(Random.Default) }
    }

    var index by remember(attemptId) { mutableIntStateOf(0) }
    var correctCount by remember(attemptId) { mutableIntStateOf(0) }
    var picked by remember(attemptId) { mutableStateOf<String?>(null) }
    var finished by remember(attemptId) { mutableStateOf(false) }

    // Odsloniecie odpowiedzi -> pauza -> nastepne pytanie (albo wynik).
    LaunchedEffect(attemptId, index, picked) {
        if (picked == null) return@LaunchedEffect
        delay(REVEAL_MILLIS)
        if (index + 1 >= questions.size) {
            finished = true
        } else {
            index += 1
            picked = null
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xCC07050B))
            // Zjedzenie klikniec tla: quiz jest na stawke, wiec nie zamyka sie przypadkiem obok.
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = {}
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 400.dp)
                .padding(20.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(AppPalette.Panel)
                .border(2.dp, AppPalette.GoldFrame, RoundedCornerShape(16.dp))
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (finished) {
                QuizResult(
                    correct = correctCount,
                    total = questions.size,
                    onClaim = { onFinished(correctCount, ProductionQuiz.boostPercentFor(correctCount)) }
                )
                return@Column
            }

            val entry = questions.getOrNull(index) ?: return@Column

            QuizProgress(index = index, total = questions.size, correct = correctCount)

            Text(
                text = entry.prompt,
                color = AppPalette.TextBody,
                fontFamily = FontFirst(),
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp
            )

            shuffled[index].forEach { answer ->
                AnswerTile(
                    text = answer,
                    state = answerState(answer, entry, picked),
                    onClick = {
                        if (picked != null) return@AnswerTile
                        picked = answer
                        if (answer == entry.correct) correctCount += 1
                    }
                )
            }

            Text(
                text = "Backing out does not use your daily try.",
                color = AppPalette.TextFaint,
                fontFamily = FontFirst(),
                fontSize = 10.sp
            )
            Text(
                text = "CLOSE",
                color = AppPalette.TextMuted,
                fontFamily = FontPixel(),
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                modifier = Modifier
                    .align(Alignment.End)
                    .clip(RoundedCornerShape(8.dp))
                    .clickable(onClick = onDismiss)
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            )
        }
    }
}

private enum class AnswerVisual { Idle, Correct, Wrong, Faded }

private fun answerState(answer: String, entry: QuizEntry, picked: String?): AnswerVisual = when {
    picked == null -> AnswerVisual.Idle
    answer == entry.correct -> AnswerVisual.Correct
    answer == picked -> AnswerVisual.Wrong
    else -> AnswerVisual.Faded
}

@Composable
private fun AnswerTile(text: String, state: AnswerVisual, onClick: () -> Unit) {
    val border by animateColorAsState(
        when (state) {
            AnswerVisual.Idle -> AppPalette.GoldDim
            AnswerVisual.Correct -> Good
            AnswerVisual.Wrong -> Bad
            AnswerVisual.Faded -> AppPalette.GoldDim.copy(alpha = 0.3f)
        }
    )
    val content = when (state) {
        AnswerVisual.Correct -> Good
        AnswerVisual.Wrong -> Bad
        AnswerVisual.Faded -> AppPalette.TextFaint
        AnswerVisual.Idle -> AppPalette.TextBody
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(AppPalette.Ink)
            .border(if (state == AnswerVisual.Idle) 1.dp else 2.dp, border, RoundedCornerShape(10.dp))
            .clickable(enabled = state == AnswerVisual.Idle, onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 11.dp)
    ) {
        Text(text = text, color = content, fontFamily = FontFirst(), fontSize = 13.sp)
    }
}

/** Kropki postepu - od razu widac, ile pytan zostalo i ile poszlo dobrze. */
@Composable
private fun QuizProgress(index: Int, total: Int, correct: Int) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "QUESTION ${index + 1}/$total",
            color = AppPalette.Gold,
            fontFamily = FontPixel(),
            fontWeight = FontWeight.Bold,
            fontSize = 11.sp,
            modifier = Modifier.weight(1f)
        )
        repeat(total) { slot ->
            val color = when {
                slot < index && slot < correct -> Good
                slot < index -> Bad
                else -> AppPalette.GoldDim.copy(alpha = 0.45f)
            }
            Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(color))
        }
    }
}

@Composable
private fun QuizResult(correct: Int, total: Int, onClaim: () -> Unit) {
    val percent = ProductionQuiz.boostPercentFor(correct)
    val accent = if (percent > 0) Good else Bad

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "$correct / $total",
            color = accent,
            fontFamily = FontPixel(),
            fontWeight = FontWeight.Bold,
            fontSize = 30.sp
        )
        Text(
            text = if (percent > 0) "+$percent% PRODUCTION for 24h" else "No bonus this time",
            color = accent,
            fontFamily = FontPixel(),
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp,
            textAlign = TextAlign.Center
        )
        Text(
            text = "Next try in 24h.",
            color = AppPalette.TextFaint,
            fontFamily = FontFirst(),
            fontSize = 11.sp
        )
        TownActionButton(
            text = "CLAIM",
            primary = true,
            modifier = Modifier.fillMaxWidth(),
            onClick = onClaim
        )
    }
}

/** Ile czasu widac poprawna odpowiedz, zanim wjedzie kolejne pytanie. */
private const val REVEAL_MILLIS = 1100L
