package com.lernhero.game.battle.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.EaseOutCubic
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lernhero.game.battle.BossEventKind
import com.lernhero.game.battle.BossEventPhase
import com.lernhero.game.battle.BossEventState
import com.lernhero.shared.theme.FontFirst
import com.lernhero.shared.theme.FontPixel
import kotlin.math.abs
import kotlin.math.roundToInt
import kotlin.math.sin

/**
 * Filmowa nakladka zdarzenia bossa - to ona robi z "cyferki zmienily sie w mgnieniu oka" scene
 * z czterema czytelnymi etapami ([BossEventPhase]).
 *
 * Co robi kazdy etap:
 *  - **Charging**: tlo ciemnieje, w gore leca iskry energii, licznik NAJGORSZEGO skutku nabija sie
 *    od zera. Quizu jeszcze nie ma - gracz ma chwile, zeby zobaczyc, co go czeka i o ile gra.
 *  - **Quiz**: wszystko zostaje na ekranie, tylko od dolu wjezdza panel z pytaniem.
 *  - **Judging**: karta werdyktu (poprawna wartosc / odpowiedz gracza / ocena / redukcja), a licznik
 *    ZJEZDZA z najgorszego przypadku do faktycznego wyniku - widac wprost, ile odpowiedz uratowala.
 *  - **Payoff**: kula wystrzeliwuje z pyska w cel (przy leczeniu implodouje na samym bossie).
 *
 * Rysowana jest NAD polem bitwy i nie zabiera mu miejsca - sam boss zostaje tam, gdzie stal.
 * Wszystkie efekty (iskry, poswiata, kula) sa rysowane kodem i celowo trzymane w osobnych,
 * malych composable'ach - podmiana ktoregokolwiek na PNG/spritesheet nie rusza reszty sceny.
 */
/**
 * TLO sceny - rysowane POD postaciami, zaraz nad grafika areny. Dzieki temu smok zostaje na
 * drugim planie: przyciemnienie i iskry sa za nim, a nie na nim. Wczesniej cala nakladka szla
 * nad polem bitwy i mgla klada sie na samego bossa, gaszac go razem z tlem.
 */
@Composable
fun BossChargeBackdrop(
    event: BossEventState,
    modifier: Modifier = Modifier
) {
    val accent = bossEventAccentColor(event.kind)
    val dim by animateFloatAsState(
        targetValue = when (event.phase) {
            BossEventPhase.Charging -> 0.78f
            BossEventPhase.Quiz -> 0.70f
            BossEventPhase.Judging -> 0.82f
            BossEventPhase.Payoff -> 0.40f
        },
        animationSpec = tween(900, easing = EaseOutCubic),
        label = "bossDim"
    )

    Box(modifier = modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.horizontalGradient(
                        listOf(
                            Color.Black.copy(alpha = dim),
                            Color.Black.copy(alpha = dim * 0.92f),
                            accent.copy(alpha = dim * 0.34f)
                        )
                    )
                )
        )
        if (event.phase == BossEventPhase.Charging || event.phase == BossEventPhase.Quiz) {
            EnergySparks(
                accent = accent,
                intensity = if (event.phase == BossEventPhase.Charging) 1f else 0.7f,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

/**
 * PIERWSZY PLAN sceny - licznik stawki, werdykt i kula kulminacji. Rysowany nad postaciami, ale
 * pod panelem quizu, wiec kolejnosc planow to: tlo areny -> przyciemnienie + iskry -> smok ->
 * liczby -> quiz.
 */
@Composable
fun BossChargeOverlay(
    event: BossEventState,
    modifier: Modifier = Modifier
) {
    val accent = bossEventAccentColor(event.kind)
    val phase = event.phase

    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        // Licznik po LEWEJ, w pionie mniej wiecej na wysokosci pyska. Celowo moze lekko nachodzic
        // na wlasna druzyne - to najwazniejsza liczba na ekranie i ma byc widoczna z daleka.
        Column(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(start = 10.dp)
                .fillMaxWidth(0.52f),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = event.projection.attackName,
                color = accent,
                fontFamily = FontPixel(),
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
            Text(
                text = event.projection.valueLabel,
                color = Color(0xFFBFB098),
                fontFamily = FontFirst(),
                fontSize = 10.sp
            )
            StakeCounter(event = event, accent = accent)
        }

        AnimatedVisibility(
            visible = phase == BossEventPhase.Judging,
            enter = fadeIn(tween(260)) + slideInHorizontally(tween(320)) { -it / 3 },
            exit = fadeOut(tween(200)),
            modifier = Modifier.align(Alignment.TopCenter).padding(top = 8.dp)
        ) {
            VerdictCard(event = event, accent = accent)
        }

        if (phase == BossEventPhase.Payoff) {
            PayoffOrb(kind = event.kind, accent = accent, modifier = Modifier.fillMaxSize())
        }
    }
}

/**
 * Wielki licznik stawki - to on ma nieść cala scene.
 *
 * Podczas ladowania i quizu rosnie W CZASIE RZECZYWISTYM: zaklecie sie laduje, wiec kazda sekunda
 * zwlekania widocznie dokłada obrazen. Po zatwierdzeniu odpowiedzi licznik przeskakuje na wynik
 * koncowy - i moze skoczyc W DOL (trafiona odpowiedz) albo W GORE (pudlo realnie wzmacnia cios).
 *
 * Im wyzej licznik, tym mocniej drga i tym bardziej czerwienieje - to jedyny sygnal, ktory gracz
 * ma szanse zauwazyc katem oka, nie odrywajac sie od suwaka.
 */
@Composable
private fun StakeCounter(event: BossEventState, accent: Color) {
    val charged = event.projection.chargedValue
    val actual = event.actualValue()

    val shown = remember { Animatable(0f) }
    LaunchedEffect(event.phase, charged, actual) {
        when (event.phase) {
            // Ladowanie + quiz to JEDEN ciagly ruch licznika od zera do pelnego naladowania:
            // najpierw rosnie sam z siebie, potem dalej rosnie, dopoki gracz zwleka z odpowiedzia.
            BossEventPhase.Charging ->
                shown.animateTo(charged * BOSS_CHARGE_SHARE, tween(2100, easing = LinearEasing))

            BossEventPhase.Quiz ->
                shown.animateTo(charged.toFloat(), tween(ANSWER_WINDOW_MS, easing = LinearEasing))

            // Werdykt: skok na faktyczna wartosc. W gore albo w dol - to jest ta chwila, w ktorej
            // widac, co dala odpowiedz.
            BossEventPhase.Judging, BossEventPhase.Payoff ->
                shown.animateTo(actual.toFloat(), tween(900, easing = EaseOutCubic))
        }
    }

    val settled = event.phase == BossEventPhase.Judging || event.phase == BossEventPhase.Payoff
    val fill = if (charged > 0) (shown.value / charged).coerceIn(0f, 2f) else 0f

    // Drganie rosnie razem z liczba - przy pelnym naladowaniu cyfry wyraznie sie trzesa.
    val shake = rememberInfiniteTransition(label = "stakeShake")
    val jitter by shake.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(90, easing = LinearEasing), RepeatMode.Reverse),
        label = "stakeJitter"
    )
    val beat by shake.animateFloat(
        initialValue = 1f,
        targetValue = 1.06f,
        animationSpec = infiniteRepeatable(tween(420, easing = LinearEasing), RepeatMode.Reverse),
        label = "stakeBeat"
    )

    // Zolty -> pomaranczowy -> czerwony, im blizej pelnego naladowania.
    val heat = when {
        settled && actual <= 0 -> Color(0xFF4ADE80)
        fill >= 1f -> Color(0xFFFF4433)
        fill >= 0.6f -> Color(0xFFFF8C42)
        else -> accent
    }

    Text(
        text = "${shown.value.roundToInt()}${event.projection.unit}",
        color = heat,
        fontFamily = FontPixel(),
        fontWeight = FontWeight.Bold,
        fontSize = 54.sp,
        modifier = Modifier.graphicsLayer {
            val amp = if (settled) 0f else fill * 5f
            translationX = (jitter - 0.5f) * 2f * amp
            translationY = (0.5f - jitter) * 2f * amp
            val s = if (settled) 1f else beat
            scaleX = s
            scaleY = s
            transformOrigin = androidx.compose.ui.graphics.TransformOrigin(0f, 0.5f)
        }
    )

    // Roznica wzgledem naladowanej wartosci - "ile odpowiedz uratowala" albo "ile dolozyla".
    if (settled && charged > 0) {
        val delta = actual - charged
        val percent = ((delta.toFloat() / charged) * 100f).roundToInt()
        Text(
            text = when {
                delta < 0 -> "$percent% thanks to your answer"
                delta > 0 -> "+$percent% from the miss"
                else -> "no change"
            },
            color = if (delta < 0) Color(0xFF4ADE80) else Color(0xFFD1453D),
            fontFamily = FontPixel(),
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp
        )
    }
}

/** Ile calosci licznik nabija jeszcze przed wjazdem quizu - reszta dochodzi w trakcie odpowiadania. */
private const val BOSS_CHARGE_SHARE = 0.35f

/** Musi odpowiadac [com.lernhero.domain.combat.live.AnswerTimer.WINDOW_MS] - licznik ma isc rowno z paskiem. */
private const val ANSWER_WINDOW_MS = 18000

/**
 * Karta werdyktu: co bylo poprawnie, co dal gracz, jak blisko byl i o ile to scielo cios.
 * To jest odpowiedz na "nie wiadomo tak naprawde co sie stalo".
 */
@Composable
private fun VerdictCard(event: BossEventState, accent: Color) {
    val correct = event.question.correctValue
    val guess = event.guess
    val diff = abs(guess - correct)
    val range = (event.question.maxValue - event.question.minValue).coerceAtLeast(1)
    val closeness = 1f - (diff.toFloat() / range).coerceIn(0f, 1f)

    val verdict = when {
        diff == 0 -> "W PUNKT!"
        closeness >= 0.97f -> "PRAWIE IDEALNIE"
        closeness >= 0.85f -> "BLISKO"
        closeness >= 0.6f -> "TAK SOBIE"
        else -> "MISS"
    }
    val verdictColor = when {
        closeness >= 0.85f -> Color(0xFF4ADE80)
        closeness >= 0.6f -> Color(0xFFFFD34D)
        else -> Color(0xFFD1453D)
    }

    // Mnoznik z celnosci, pokazany tak, jak go czuje gracz: "-40%" albo "+190%".
    val mult = event.outcome?.accuracyMultiplier ?: 1f
    val deltaPercent = ((mult - 1f) * 100f).roundToInt()

    Column(
        modifier = Modifier
            .padding(horizontal = 20.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFF16120C).copy(alpha = 0.94f))
            .border(2.dp, verdictColor, RoundedCornerShape(16.dp))
            .padding(horizontal = 18.dp, vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        Text(
            text = verdict,
            color = verdictColor,
            fontFamily = FontPixel(),
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp
        )
        Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            VerdictValue("PRAWIDLOWO", "$correct", Color(0xFFF4E9D7))
            VerdictValue("TWOJA", "$guess", verdictColor)
        }
        Text(
            text = when {
                deltaPercent < 0 -> "ATAK OSLABIONY $deltaPercent%"
                deltaPercent > 0 -> "ATAK WZMOCNIONY +$deltaPercent%"
                else -> "ATAK BEZ ZMIAN"
            },
            color = if (deltaPercent < 0) Color(0xFF4ADE80) else Color(0xFFD1453D),
            fontFamily = FontFirst(),
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp,
            textAlign = TextAlign.Center
        )
        event.outcome?.message?.let { msg ->
            Text(
                text = msg,
                color = Color(0xFFBFB098),
                fontFamily = FontFirst(),
                fontSize = 10.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun VerdictValue(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, color = Color(0xFF8A7A5E), fontFamily = FontFirst(), fontSize = 8.sp)
        Text(value, color = color, fontFamily = FontPixel(), fontWeight = FontWeight.Bold, fontSize = 17.sp)
    }
}

/**
 * Iskry energii unoszace sie w gore - "anime charge". Rysowane kodem jako male, wydluzone kreski
 * z poswiata; kazda ma wlasna sciezke i tempo, wiec caly rzut nigdy nie wraca do tej samej pozy.
 *
 * PLACEHOLDER pod PNG: cala grafika iskry siedzi w jednym `drawLine`/`drawCircle` ponizej -
 * podmiana na sprite'y nie rusza ani rozkladu czastek, ani reszty sceny.
 */
@Composable
fun EnergySparks(
    accent: Color,
    modifier: Modifier = Modifier,
    intensity: Float = 1f,
    count: Int = 34
) {
    val transition = rememberInfiniteTransition(label = "sparks")
    val t by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(3400, easing = LinearEasing)),
        label = "sparkTime"
    )

    Canvas(modifier = modifier) {
        for (i in 0 until count) {
            // Deterministyczny "szum" z indeksu - bez Random, wiec czastki nie skacza przy
            // kazdej rekompozycji i caly efekt jest powtarzalny.
            val seedA = ((i * 73) % 100) / 100f
            val seedB = ((i * 149) % 100) / 100f
            val seedC = ((i * 227) % 100) / 100f

            // Kazda czastka ma wlasna faze i predkosc, wiec nie leca jednym rzedem.
            val life = (t * (0.55f + seedB * 0.8f) + seedA) % 1f

            // Iskry gesciej po stronie bossa (prawa polowa) - to stamtad bije energia.
            val baseX = size.width * (0.42f + seedA * 0.58f)
            // Delikatne falowanie w bok, zeby tor nie byl idealnie pionowy.
            val sway = sin((life * 6.28f) + seedC * 6.28f) * size.width * 0.035f
            val x = baseX + sway
            val y = size.height * (1.02f - life * (0.75f + seedC * 0.35f))

            val fade = (1f - life) * intensity
            if (fade <= 0.02f) continue

            val len = size.height * (0.014f + seedB * 0.028f)
            val alpha = (fade * 0.85f).coerceIn(0f, 1f)

            // Poswiata + rdzen - dwie kreski wystarcza, zeby iskra "swiecila" bez blura.
            drawLine(
                color = accent.copy(alpha = alpha * 0.35f),
                start = Offset(x, y + len),
                end = Offset(x, y - len),
                strokeWidth = 6f
            )
            drawLine(
                color = Color.White.copy(alpha = alpha * 0.9f),
                start = Offset(x, y + len * 0.5f),
                end = Offset(x, y - len * 0.5f),
                strokeWidth = 2f
            )
            drawCircle(
                color = accent.copy(alpha = alpha),
                radius = 2.5f + seedB * 2.5f,
                center = Offset(x, y - len)
            )
        }
    }
}

/**
 * Kulminacja: kula wychodzi z pyska bossa (prawa strona) i leci w cel.
 *
 * Leczenie implodouje na samym bossie - kula nie leci nigdzie, tylko kurczy sie i rozblyskuje
 * w miejscu; kazde inne zdarzenie wystrzeliwuje ja w lewo, w strone druzyny.
 */
@Composable
private fun PayoffOrb(kind: BossEventKind, accent: Color, modifier: Modifier = Modifier) {
    val travel = remember { Animatable(0f) }
    LaunchedEffect(kind) {
        travel.snapTo(0f)
        travel.animateTo(1f, tween(1100, easing = EaseOutCubic))
    }

    val heals = kind == BossEventKind.Heal
    val p = travel.value

    Canvas(modifier = modifier) {
        // Pysk bossa jest po prawej, mniej wiecej w gornej trzeciej ekranu.
        val fromX = size.width * 0.78f
        val fromY = size.height * 0.34f

        if (heals) {
            // Implozja: kula kurczy sie w bossa, a rozblysk rozchodzi sie na zewnatrz.
            val r = size.minDimension * 0.16f * (1f - p)
            drawCircle(accent.copy(alpha = (1f - p) * 0.9f), radius = r, center = Offset(fromX, fromY))
            drawCircle(
                color = accent.copy(alpha = (1f - p) * 0.45f),
                radius = size.minDimension * 0.18f * p + r,
                center = Offset(fromX, fromY),
                style = androidx.compose.ui.graphics.drawscope.Stroke(width = 6f)
            )
        } else {
            // Lot w lewo, w strone druzyny, z lekkim opadaniem i smuga za soba.
            val toX = size.width * 0.12f
            val toY = size.height * 0.55f
            val x = fromX + (toX - fromX) * p
            val y = fromY + (toY - fromY) * p
            val r = size.minDimension * (0.07f + 0.05f * p)

            // Smuga - kilka slabnacych kopii za kula.
            for (k in 1..5) {
                val lag = (p - k * 0.05f).coerceAtLeast(0f)
                drawCircle(
                    color = accent.copy(alpha = 0.16f * (1f - k / 6f)),
                    radius = r * (1f - k * 0.1f),
                    center = Offset(
                        fromX + (toX - fromX) * lag,
                        fromY + (toY - fromY) * lag
                    )
                )
            }
            drawCircle(accent.copy(alpha = 0.55f), radius = r * 1.5f, center = Offset(x, y))
            drawCircle(Color.White.copy(alpha = 0.85f), radius = r * 0.55f, center = Offset(x, y))
        }
    }
}

/** Faktyczna wartosc skutku po rozstrzygnieciu - ta sama jednostka, co [BossEventState.projection]. */
private fun BossEventState.actualValue(): Int {
    val o = outcome ?: return projection.chargedValue
    return when (kind) {
        BossEventKind.Heal -> o.healAmount
        BossEventKind.Poison -> o.poisonDamage
        BossEventKind.Strike -> o.strikeDamage
        BossEventKind.StunGamble -> (o.affectedFraction * 100f).roundToInt()
    }
}
