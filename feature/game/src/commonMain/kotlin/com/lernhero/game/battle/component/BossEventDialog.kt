package com.lernhero.game.battle.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lernhero.domain.combat.live.AnswerTimer
import com.lernhero.game.battle.BossEventKind
import com.lernhero.game.battle.BossEventState
import com.lernhero.shared.theme.FontFirst
import kotlinx.coroutines.delay
import kotlin.math.roundToInt

/**
 * Dol ekranu w trakcie specjalnego zdarzenia elity/bossa - wyglada dokladnie jak normalny quiz
 * ([LiveQuizBar]), ta sama "kartowa" oprawa i ten sam pasek czasu w tym samym miejscu
 * ([AnswerCountdownBar]). Wszystkie trzy [BossEventKind] uzywaja TEGO SAMEGO suwaka do oszacowania
 * wartosci - boss nigdy nie atakuje "od razu": dopiero po zatwierdzeniu zgadywania ViewModel liczy,
 * jak dobrze poszlo, i skaluje tym efekt (im gorszy quiz, tym gorszy skutek - patrz
 * [com.lernhero.game.battle.BattleViewModel.resolveBossEvent]).
 */
@Composable
fun BossEventQuizBar(
    event: BossEventState,
    onGuessChange: (Int) -> Unit,
    onConfirm: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Auto-zatwierdzenie, gdy czas minie - gracz nie moze zablokowac walki w nieskonczonosc.
    LaunchedEffect(event.question.id, event.resolved) {
        if (event.resolved) return@LaunchedEffect
        delay(AnswerTimer.WINDOW_MS)
        onConfirm()
    }

    val accent = bossEventAccentColor(event.kind)

    Box(
        modifier = modifier
            .fillMaxSize()
            .clip(PanelShape)
            .background(PanelGradient)
            .border(3.dp, FrameGradient, PanelShape)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 10.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = bossEventTitle(event.kind),
                color = accent,
                fontFamily = FontFirst(),
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp
            )

            AnswerCountdownBar(
                questionId = event.question.id,
                revealed = event.resolved,
                label = "WYNIK QUIZU"
            )

            QuestionBubble(prompt = event.question.prompt)

            if (!event.resolved) {
                Text(
                    text = "${event.question.unit.replaceFirstChar { it.uppercase() }}: ${event.guess}",
                    color = InkStrong,
                    fontFamily = FontFirst(),
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )

                Slider(
                    value = event.guess.toFloat(),
                    onValueChange = { onGuessChange(it.roundToInt()) },
                    valueRange = event.question.minValue.toFloat()..event.question.maxValue.toFloat(),
                    colors = SliderDefaults.colors(
                        thumbColor = accent,
                        activeTrackColor = accent,
                        inactiveTrackColor = Color(0xFFE0D2B0)
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "${event.question.minValue}", color = InkMuted, fontFamily = FontFirst(), fontSize = 10.sp)
                    Text(text = "rok 0", color = InkMuted, fontFamily = FontFirst(), fontWeight = FontWeight.Bold, fontSize = 10.sp)
                    Text(text = "${event.question.maxValue}", color = InkMuted, fontFamily = FontFirst(), fontSize = 10.sp)
                }

                Text(
                    text = bossEventHint(event.kind),
                    color = InkMuted,
                    fontFamily = FontFirst(),
                    fontSize = 10.sp
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(accent)
                        .clickable(onClick = onConfirm)
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "ZATWIERDZ",
                        color = Color.White,
                        fontFamily = FontFirst(),
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }
            } else {
                // Ile brakowalo do poprawnej wartosci - zeby bylo widac, czy zgadywanie w ogole
                // mialo znaczenie, a nie tylko szybkosc zatwierdzenia.
                val diff = kotlin.math.abs(event.guess - event.question.correctValue)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Twoja odpowiedz: ${event.guess}",
                        color = InkMuted,
                        fontFamily = FontFirst(),
                        fontSize = 11.sp
                    )
                    Text(
                        text = "Poprawnie: ${event.question.correctValue} (${if (diff == 0) "w punkt!" else "roznica $diff"})",
                        color = InkStrong,
                        fontFamily = FontFirst(),
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp
                    )
                }

                event.outcome?.let { outcome ->
                    val badOutcome = outcome.healAmount > 0 || outcome.poisonDamage > 0 || !outcome.interrupted
                    val color = if (badOutcome) Color(0xFFD1453D) else Color(0xFF22A55E)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(color.copy(alpha = 0.14f))
                            .border(1.dp, color, RoundedCornerShape(12.dp))
                            .padding(horizontal = 12.dp, vertical = 10.dp)
                    ) {
                        Text(
                            text = outcome.message,
                            color = color,
                            fontFamily = FontFirst(),
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }
    }
}

private fun bossEventTitle(kind: BossEventKind): String = when (kind) {
    BossEventKind.Heal -> "BOSS SIE LECZY"
    BossEventKind.Poison -> "TRUJACY ODDECH"
    BossEventKind.StunGamble -> "ZAKLECIE BOSSA"
}

private fun bossEventHint(kind: BossEventKind): String = when (kind) {
    BossEventKind.Heal -> "Im lepszy quiz, tym mniej HP odzyska boss."
    BossEventKind.Poison -> "Im lepszy quiz, tym mniej HP straci druzyna."
    BossEventKind.StunGamble -> "Im lepszy quiz, tym slabsze bedzie oszolomienie."
}

private fun bossEventAccentColor(kind: BossEventKind): Color = when (kind) {
    BossEventKind.Heal -> Color(0xFF22A55E)
    BossEventKind.Poison -> Color(0xFFD1453D)
    BossEventKind.StunGamble -> Color(0xFFB4451F)
}
