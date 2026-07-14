package com.lernhero.game

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.lernhero.shared.ButtonPrimary
import com.lernhero.shared.CategoryRed
import com.lernhero.shared.FontFirst
import com.lernhero.shared.FontPixel
import com.lernhero.shared.TextPrimary

@Composable
fun QuizDialog(
    state: GameStateUi,
    onAnswer: (String) -> Unit
) {
    val quiz = state.quiz ?: return
    val question = quiz.currentQuestion ?: return
    val progress = (quiz.currentQuestionIndex + 1).toFloat() / quiz.questions.size.toFloat()

    val timeProgress = remember(question.id) { Animatable(1f) }
    LaunchedEffect(question.id) {
        timeProgress.animateTo(
            targetValue = 0f,
            animationSpec = tween(durationMillis = 5000, easing = LinearEasing)
        )
        onAnswer("")
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
                                Button(
                                    onClick = { onAnswer(answer.id) },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color.White,
                                        contentColor = TextPrimary
                                    )
                                ) {
                                    Text(
                                        text = answer.text,
                                        fontFamily = FontFirst(),
                                        maxLines = 2
                                    )
                                }
                            }
                        }
                    }
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
