package com.lernhero.game

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.lernhero.shared.ButtonPrimary
import com.lernhero.shared.CategoryRed
import com.lernhero.shared.FontFirst
import com.lernhero.shared.FontPixel
import com.lernhero.shared.TextPrimary
import kotlinx.coroutines.delay

@Composable
fun QuizDialog(
    state: GameStateUi,
    onAnswer: (String) -> Unit,
    onContinue: () -> Unit
) {
    val quiz = state.quiz ?: return
    val question = quiz.currentQuestion ?: return
    val progress = (quiz.currentQuestionIndex + 1).toFloat() / quiz.questions.size.toFloat()

    // Trudniejsze pytanie dostaje wiecej czasu - 5 sekund na stolice Bhutanu bylo okrucienstwem.
    // Zegar staje w chwili odpowiedzi: podczas odslony nikt juz nikogo nie goni.
    val timeProgress = remember(question.id) { Animatable(1f) }
    LaunchedEffect(question.id, quiz.revealed) {
        if (quiz.revealed) return@LaunchedEffect
        timeProgress.animateTo(
            targetValue = 0f,
            animationSpec = tween(durationMillis = question.seconds * 1000, easing = LinearEasing)
        )
        onAnswer("")
    }

    // Chwila na zobaczenie, ktora odpowiedz byla poprawna - dopiero potem kolejne pytanie.
    LaunchedEffect(question.id, quiz.revealed) {
        if (!quiz.revealed) return@LaunchedEffect
        delay(QUIZ_REVEAL_MILLIS)
        onContinue()
    }

    Dialog(onDismissRequest = {}) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 460.dp),
            shape = RoundedCornerShape(8.dp),
            color = Color(0xFF171717)
        ) {
            Column(
                modifier = Modifier
                    .heightIn(max = 430.dp)
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                DialogHeader(
                    title = "Quiz",
                    subtitle = "Fala ${state.wave} - pytanie ${quiz.currentQuestionIndex + 1}/${quiz.questions.size}"
                )
                ProgressBar(progress = progress)
                TimeBar(progress = timeProgress.value)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    QuizBadge(text = question.category, color = Color(0xFF3B9DFF))
                    QuizBadge(text = question.difficulty, color = difficultyColor(question.difficulty))
                    QuizBadge(text = "+${question.reward} pkt", color = Color(0xFFFFD100))
                }

                Text(
                    text = question.prompt,
                    color = Color.White,
                    fontFamily = FontFirst(),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                question.flag?.let { flag ->
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        color = Color.White
                    ) {
                        Image(
                            painter = org.jetbrains.compose.resources.painterResource(flag),
                            contentDescription = question.prompt,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(112.dp)
                                .padding(12.dp),
                            contentScale = ContentScale.Fit
                        )
                    }
                }

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    question.answers.chunked(2).forEach { rowAnswers ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            rowAnswers.forEach { answer ->
                                AnswerButton(
                                    answer = answer,
                                    revealed = quiz.revealed,
                                    picked = quiz.selectedAnswerId == answer.id,
                                    onClick = { onAnswer(answer.id) },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }

                if (quiz.revealed) {
                    RevealBanner(
                        correct = quiz.answeredCorrectly,
                        timedOut = quiz.selectedAnswerId == null,
                        correctText = quiz.correctAnswer?.text.orEmpty(),
                        reward = question.reward
                    )
                }
            }
        }
    }
}

@Composable
fun UpgradeDialog(
    state: GameStateUi,
    onBuyUpgrade: (String) -> Unit,
    onFinish: () -> Unit
) {
    val upgrade = state.upgrade ?: return

    Dialog(onDismissRequest = {}) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 520.dp),
            shape = RoundedCornerShape(8.dp),
            color = Color(0xFF171717)
        ) {
            Column(
                modifier = Modifier
                    .heightIn(max = 430.dp)
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                DialogHeader(
                    title = "Nagrody",
                    subtitle = "Punkty: ${state.rewardPoints}   Wynik: ${state.totalScore}"
                )

                upgrade.choices.forEach { choice ->
                    val isSelected = choice.id in upgrade.selectedChoiceIds
                    val canBuy = !isSelected && state.rewardPoints >= choice.cost

                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        color = Color.White
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(3.dp)
                            ) {
                                Text(
                                    text = choice.title,
                                    color = choice.rarity.color(),
                                    fontFamily = FontFirst(),
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = choice.rarity.label,
                                    color = choice.rarity.color(),
                                    fontFamily = FontFirst(),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = choice.description,
                                    color = Color(0xFF666666),
                                    fontFamily = FontFirst(),
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                            OutlinedButton(
                                enabled = canBuy,
                                onClick = { onBuyUpgrade(choice.id) },
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(if (isSelected) "OK" else "${choice.cost}")
                            }
                        }
                    }
                }

                Button(
                    onClick = onFinish,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ButtonPrimary,
                        contentColor = TextPrimary
                    )
                ) {
                    Text("Kolejna fala")
                }
            }
        }
    }
}

private fun UpgradeRarity.color(): Color =
    when (this) {
        UpgradeRarity.Common -> Color(0xFF555555)
        UpgradeRarity.Rare -> Color(0xFF236DFF)
        UpgradeRarity.Epic -> Color(0xFF8B3DFF)
        UpgradeRarity.Legendary -> Color(0xFFF24C00)
    }

@Composable
fun DefeatDialog(
    state: GameStateUi,
    onRestart: () -> Unit
) {
    Dialog(onDismissRequest = {}) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 420.dp),
            shape = RoundedCornerShape(8.dp),
            color = Color(0xFF171717)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                DialogHeader(
                    title = "Defeat",
                    subtitle = "Dotarles do fali ${state.wave}. Wynik: ${state.totalScore}. +${state.rewardCoins} monet"
                )

                Button(
                    onClick = onRestart,
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ButtonPrimary,
                        contentColor = TextPrimary
                    )
                ) {
                    Text("Restart")
                }
            }
        }
    }
}

@Composable
private fun DialogHeader(
    title: String,
    subtitle: String
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = title,
            color = Color.White,
            fontFamily = FontPixel(),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = subtitle,
            color = Color(0xFFE7E7E7),
            fontFamily = FontFirst(),
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
private fun TimeBar(progress: Float) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(6.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFF333333))
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(progress.coerceIn(0f, 1f))
                .height(6.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(CategoryRed)
        )
    }
}

@Composable
private fun ProgressBar(progress: Float) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(8.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFF333333))
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(progress.coerceIn(0f, 1f))
                .height(8.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(ButtonPrimary)
        )
    }
}

/** Mala plakietka nad pytaniem: kategoria, poziom, stawka. */
@Composable
private fun QuizBadge(text: String, color: Color) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(color.copy(alpha = 0.18f))
            .border(1.dp, color, RoundedCornerShape(6.dp))
            .padding(horizontal = 8.dp, vertical = 3.dp)
    ) {
        Text(
            text = text,
            color = color,
            fontFamily = FontFirst(),
            fontWeight = FontWeight.Bold,
            fontSize = 10.sp
        )
    }
}

/** Im wyzszy szczebel drabinki, tym cieplejszy kolor - jak w Milionerach. */
private fun difficultyColor(label: String): Color = when (label) {
    "Latwe" -> Color(0xFF4ADE80)
    "Srednie" -> Color(0xFFFFD100)
    "Trudne" -> Color(0xFFFF8A3D)
    else -> Color(0xFFFF4D4D)
}

/** Ile czasu quiz stoi po odpowiedzi, zebys zdazyl zobaczyc, ktora byla dobra. */
private const val QUIZ_REVEAL_MILLIS = 1600L

private val AnswerGreen = Color(0xFF2E9E52)
private val AnswerRed = Color(0xFFD64545)
private val AnswerFaded = Color(0xFF3A3733)

/**
 * Przycisk odpowiedzi. Do momentu odpowiedzi wyglada tak samo dla wszystkich; po odslonie
 * poprawna zapala sie na zielono, zle klikniecie na czerwono, a reszta gasnie.
 */
@Composable
private fun AnswerButton(
    answer: QuizAnswer,
    revealed: Boolean,
    picked: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val container by animateColorAsState(
        when {
            !revealed -> Color.White
            answer.isCorrect -> AnswerGreen
            picked -> AnswerRed
            else -> AnswerFaded
        }
    )
    val content = when {
        !revealed -> TextPrimary
        answer.isCorrect || picked -> Color.White
        else -> Color(0xFF8C857C)
    }

    Button(
        // Po odslonie klikanie nic nie daje - odpowiedz jest juz zapisana.
        onClick = { if (!revealed) onClick() },
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = container,
            contentColor = content
        )
    ) {
        Text(
            text = answer.text,
            fontFamily = FontFirst(),
            fontWeight = if (revealed && answer.isCorrect) FontWeight.Bold else FontWeight.Normal,
            maxLines = 2
        )
    }
}

/** Jednozdaniowy werdykt pod odpowiedziami. */
@Composable
private fun RevealBanner(
    correct: Boolean,
    timedOut: Boolean,
    correctText: String,
    reward: Int
) {
    val color = if (correct) AnswerGreen else AnswerRed
    val text = when {
        correct -> "Dobrze! +$reward pkt"
        timedOut -> "Czas minal. Poprawna: $correctText"
        else -> "Zle. Poprawna: $correctText"
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(color.copy(alpha = 0.18f))
            .border(1.dp, color, RoundedCornerShape(8.dp))
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
