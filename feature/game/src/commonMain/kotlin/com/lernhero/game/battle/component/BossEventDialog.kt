package com.lernhero.game.battle.component

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
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
                // Wartosc jako duza, "wykuta" tabliczka - to jest odpowiedz gracza i ma byc
                // najmocniejszym elementem panelu, a nie podpisem nad suwakiem.
                GuessReadout(
                    value = event.guess,
                    unit = event.question.unit,
                    accent = accent
                )

                DialSlider(
                    value = event.guess,
                    min = event.sliderMin,
                    max = event.sliderMax,
                    accent = accent,
                    onValueChange = onGuessChange,
                    modifier = Modifier.fillMaxWidth()
                )

                // Sam suwak NIE WYSTARCZA do trafienia w konkretna wartosc: przy zakresie
                // -1000..2025 jeden piksel to kilkanascie lat, wiec "ustaw dokladnie 2020" bylo
                // fizycznie niewykonalne. Suwak sluzy do zgrubnego dojazdu, te przyciski do
                // dostrojenia co do jednostki.
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Kroki skalowane do tolerancji pytania - przy premierze iPhone'a sensowny
                    // skok to 1 rok, przy starozytnosci 25.
                    val big = (event.question.tolerance / 2).coerceAtLeast(2)
                    listOf(-big, -1, 1, big).forEach { step ->
                        StepButton(
                            label = if (step > 0) "+$step" else "$step",
                            accent = accent,
                            modifier = Modifier.weight(1f)
                        ) {
                            onGuessChange(
                                (event.guess + step).coerceIn(event.sliderMin, event.sliderMax)
                            )
                        }
                    }
                }

                Text(
                    text = "${bossEventHint(event.kind)}  •  +/-${event.question.tolerance} = trafienie",
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
                        text = "Your answer: ${event.guess}",
                        color = InkMuted,
                        fontFamily = FontFirst(),
                        fontSize = 11.sp
                    )
                    Text(
                        text = "Correct: ${event.question.correctValue} (${if (diff == 0) "spot on!" else "off by $diff"})",
                        color = InkStrong,
                        fontFamily = FontFirst(),
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp
                    )
                }

                event.outcome?.let { outcome ->
                    val badOutcome = outcome.healAmount > 0 || outcome.poisonDamage > 0 ||
                        outcome.strikeDamage > 0 || !outcome.interrupted
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
    BossEventKind.Strike -> "POTEZNY CIOS"
}

private fun bossEventHint(kind: BossEventKind): String = when (kind) {
    BossEventKind.Heal -> "The better the quiz, the less HP the boss recovers."
    BossEventKind.Poison -> "The better the quiz, the less HP the party loses."
    BossEventKind.StunGamble -> "The better the quiz, the weaker the stun will be."
    BossEventKind.Strike -> "The better the quiz, the less damage the party takes."
}

internal fun bossEventAccentColor(kind: BossEventKind): Color = when (kind) {
    BossEventKind.Heal -> Color(0xFF22A55E)
    BossEventKind.Poison -> Color(0xFFD1453D)
    BossEventKind.StunGamble -> Color(0xFFB4451F)
    BossEventKind.Strike -> Color(0xFFD1453D)
}

/** Przycisk dostrajania zgadywanej wartosci co do jednostki - patrz komentarz przy suwaku. */
/**
 * Aktualna wartosc jako wykuta tabliczka. To odpowiedz gracza, wiec dostaje najwiekszy typ i
 * wlasna oprawe - wczesniej byla zwyklym wierszem tekstu i gubila sie miedzy suwakiem a podpisami.
 */
@Composable
private fun GuessReadout(value: Int, unit: String, accent: Color) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFF2A1D12))
                .border(2.dp, accent, RoundedCornerShape(12.dp))
                .padding(horizontal = 18.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = unit.uppercase(),
                color = accent.copy(alpha = 0.8f),
                fontFamily = FontFirst(),
                fontWeight = FontWeight.Bold,
                fontSize = 10.sp
            )
            Text(
                text = "$value",
                color = Color(0xFFF6EEDD),
                fontFamily = FontFirst(),
                fontWeight = FontWeight.Bold,
                fontSize = 22.sp
            )
        }
    }
}

/**
 * Suwak w oprawie gry zamiast [androidx.compose.material3.Slider].
 *
 * Materialowy suwak (cienka kreska + okragly "punkcik") to jedyny element Material Design na calym
 * ekranie walki - obok kutej ramki i pixelowego typu wygladal jak wklejka z innej aplikacji.
 * Ten rysuje sie sam: wglebiony rowek, wypelnienie w kolorze zdarzenia, podzialka i solidny,
 * prostokatny uchwyt.
 *
 * Obsluga dotyku jest WLASNA (nie `draggable`), bo suwak ma reagowac takze na samo tapniecie w
 * rowek - przy dojezdzaniu do konkretnego roku to szybsze niz przeciaganie palcem przez pol ekranu.
 */
@Composable
private fun DialSlider(
    value: Int,
    min: Int,
    max: Int,
    accent: Color,
    onValueChange: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val span = (max - min).coerceAtLeast(1)
    var widthPx by remember { mutableStateOf(1f) }

    fun emit(x: Float) {
        val fraction = (x / widthPx).coerceIn(0f, 1f)
        onValueChange(min + (fraction * span).roundToInt())
    }

    Box(
        modifier = modifier
            .height(46.dp)
            .onSizeChanged { widthPx = it.width.toFloat().coerceAtLeast(1f) }
            .pointerInput(min, max) {
                detectTapGestures { offset -> emit(offset.x) }
            }
            .pointerInput(min, max) {
                detectHorizontalDragGestures { change, _ ->
                    change.consume()
                    emit(change.position.x)
                }
            },
        contentAlignment = Alignment.CenterStart
    ) {
        val fraction = ((value - min).toFloat() / span).coerceIn(0f, 1f)

        Canvas(modifier = Modifier.fillMaxSize()) {
            val trackH = 14.dp.toPx()
            val cy = size.height / 2f
            val r = trackH / 2f

            // Rowek - ciemniejszy od panelu, z cienka kreska u gory, zeby wygladal na wglebiony.
            drawRoundRect(
                color = Color(0xFFD9C79E),
                topLeft = Offset(0f, cy - r),
                size = Size(size.width, trackH),
                cornerRadius = CornerRadius(r, r)
            )
            drawRoundRect(
                color = Color(0xFFB59B66).copy(alpha = 0.55f),
                topLeft = Offset(0f, cy - r),
                size = Size(size.width, trackH * 0.45f),
                cornerRadius = CornerRadius(r, r)
            )

            // Wypelnienie do biezacej wartosci.
            val fillW = size.width * fraction
            if (fillW > 0f) {
                drawRoundRect(
                    brush = Brush.verticalGradient(
                        listOf(accent.copy(alpha = 0.95f), accent.copy(alpha = 0.65f)),
                        startY = cy - r,
                        endY = cy + r
                    ),
                    topLeft = Offset(0f, cy - r),
                    size = Size(fillW, trackH),
                    cornerRadius = CornerRadius(r, r)
                )
            }

            // Podzialka - 8 kresek, zeby bylo widac, ze suwak ma skale, a nie jest ciagly.
            val ticks = 8
            repeat(ticks + 1) { i ->
                val x = size.width * (i.toFloat() / ticks)
                drawLine(
                    color = Color(0xFF8A6B34).copy(alpha = 0.35f),
                    start = Offset(x, cy + r + 2.dp.toPx()),
                    end = Offset(x, cy + r + 6.dp.toPx()),
                    strokeWidth = 1.5.dp.toPx()
                )
            }

            // Uchwyt - prostokatny "klocek" z jasnym obrysem, jak przyciski w reszcie gry.
            val handleW = 16.dp.toPx()
            val handleH = 30.dp.toPx()
            val hx = (size.width * fraction).coerceIn(handleW / 2f, size.width - handleW / 2f)
            drawRoundRect(
                color = Color(0xFF2A1D12),
                topLeft = Offset(hx - handleW / 2f, cy - handleH / 2f),
                size = Size(handleW, handleH),
                cornerRadius = CornerRadius(5.dp.toPx(), 5.dp.toPx())
            )
            drawRoundRect(
                color = accent,
                topLeft = Offset(hx - handleW / 2f + 2.dp.toPx(), cy - handleH / 2f + 2.dp.toPx()),
                size = Size(handleW - 4.dp.toPx(), handleH - 4.dp.toPx()),
                cornerRadius = CornerRadius(3.dp.toPx(), 3.dp.toPx())
            )
        }

        // Krance skali - pod rowkiem, po obu stronach. Lekkie wciecie, zeby liczby nie dotykaly
        // krawedzi panelu (przy czterocyfrowym roku "1980" wchodzilo w ramke).
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(horizontal = 2.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = "$min", color = InkMuted, fontFamily = FontFirst(), fontSize = 9.sp)
            Text(text = "$max", color = InkMuted, fontFamily = FontFirst(), fontSize = 9.sp)
        }
    }
}

@Composable
private fun StepButton(
    label: String,
    accent: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(accent.copy(alpha = 0.14f))
            .border(1.dp, accent.copy(alpha = 0.55f), RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 7.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = accent,
            fontFamily = FontFirst(),
            fontWeight = FontWeight.Bold,
            fontSize = 11.sp
        )
    }
}
