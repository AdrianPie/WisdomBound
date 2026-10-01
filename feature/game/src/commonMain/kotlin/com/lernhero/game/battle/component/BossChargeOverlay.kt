package com.lernhero.game.battle.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.EaseOutBack
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
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lernhero.game.battle.BossEventKind
import com.lernhero.game.battle.BossEventPhase
import com.lernhero.game.battle.BossEventState
import com.lernhero.shared.theme.FontFirst
import com.lernhero.shared.theme.FontPixel
import kotlin.math.abs
import kotlinx.coroutines.delay
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
 * PIERWSZY PLAN sceny - stawka zaklecia, werdykt i kula kulminacji. Rysowany nad postaciami, ale
 * pod panelem quizu, wiec kolejnosc planow to: tlo areny -> przyciemnienie + iskry -> smok ->
 * liczby -> quiz.
 *
 * ## Dlaczego wszystko stoi w JEDNEJ kolumnie u gory
 *
 * Wczesniej licznik wisial przy lewej krawedzi, mniej wiecej na wysokosci pyska bossa, a werdykt
 * wjezdzal osobna karta pod gornym brzegiem ekranu. Byly to dwie polowy tego samego zdania ("tyle
 * zaraz dostaniesz" i "tyle uratowala twoja odpowiedz") lezace w dwoch roznych miejscach, na dodatek
 * licznik nachodzil na wlasna druzyne i na jej paski zycia. Efekt: w chwili rozstrzygniecia oko nie
 * mialo gdzie patrzec i cala scena przelatywala niezauwazona.
 *
 * Teraz stawka i werdykt to jeden slup u gory sceny: liczba zjezdza (albo skacze) dokladnie nad
 * zdaniem, ktore mowi, dlaczego. Gora ekranu jest do tego jedynym wolnym miejscem - dol nalezy do
 * panelu quizu, prawa strona do bossa, lewa i spod do druzyny.
 */
@Composable
fun BossChargeOverlay(
    event: BossEventState,
    modifier: Modifier = Modifier
) {
    val accent = bossEventAccentColor(event.kind)
    val phase = event.phase

    // Werdykt czyta sie jak ZDANIE, wiec wchodzi po kawalku: najpierw prawidlowa wartosc, potem
    // twoja, potem roznica miedzy nimi, a dopiero na koncu to, co z tej roznicy wynika dla walki.
    // Wczesniej wszystkie cztery rzeczy pojawialy sie w jednej klatce razem ze skaczacym licznikiem
    // - bylo na co patrzec, ale nie bylo czego przeczytac.
    var beat by remember(event.phase) { mutableIntStateOf(0) }
    LaunchedEffect(event.phase) {
        if (event.phase != BossEventPhase.Judging) {
            // Payoff dziedziczy komplet - karta ma juz wszystko pokazane, gdy leci kula.
            beat = if (event.phase == BossEventPhase.Payoff) VERDICT_BEATS else 0
            return@LaunchedEffect
        }
        beat = 1
        while (beat < VERDICT_BEATS) {
            delay(VERDICT_BEAT_MS)
            beat += 1
        }
    }

    // Ten sam powod co przy zwyklym HUD-zie walki: kamera/notch potrafia siegac dobry kawalek
    // w dol ekranu, a stawka zaklecia stoi dokladnie tam, gdzie normalnie byloby WAVE/QUESTIONS.
    val topInset = WindowInsets.safeDrawing.asPaddingValues().calculateTopPadding()

    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(start = 12.dp, top = topInset + 10.dp, end = 12.dp, bottom = 10.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Ciemna plyta pod liczbami - arena pod spodem bywa jasna (lawa, snieg), a to jest
            // liczba, ktora ma sie czytac z drugiego konca pokoju.
            Column(
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFF0E0B08).copy(alpha = 0.82f))
                    .border(2.dp, accent.copy(alpha = 0.75f), RoundedCornerShape(16.dp))
                    .padding(horizontal = 18.dp, vertical = 10.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = event.projection.attackName,
                    color = accent,
                    fontFamily = FontPixel(),
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = event.projection.valueLabel,
                    color = Color(0xFFBFB098),
                    fontFamily = FontFirst(),
                    fontSize = 10.sp,
                    textAlign = TextAlign.Center
                )
                // Licznik zjezdza dopiero na ostatnim takcie - razem ze zdaniem, ktore
                // tlumaczy, DLACZEGO zjechal.
                StakeCounter(event = event, accent = accent, settleNow = beat >= VERDICT_BEATS)
            }

        }

        // Werdykt NIE stoi juz pod licznikiem - ma srodek ekranu i cala jego szerokosc. Tam jest
        // miejsce na liczby, ktore da sie przeczytac z drugiego konca pokoju, i tam patrzy oko,
        // gdy walka staje.
        AnimatedVisibility(
            visible = phase == BossEventPhase.Judging,
            enter = fadeIn(tween(200)),
            exit = fadeOut(tween(250)),
            modifier = Modifier.align(Alignment.Center)
        ) {
            VerdictCard(event = event, beat = beat)
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
private fun StakeCounter(event: BossEventState, accent: Color, settleNow: Boolean) {
    val charged = event.projection.chargedValue
    val actual = event.actualValue()

    val shown = remember { Animatable(0f) }
    LaunchedEffect(event.phase, charged, actual, settleNow) {
        when (event.phase) {
            // Ladowanie + quiz to JEDEN ciagly ruch licznika od zera do pelnego naladowania:
            // najpierw rosnie sam z siebie, potem dalej rosnie, dopoki gracz zwleka z odpowiedzia.
            BossEventPhase.Charging ->
                shown.animateTo(charged * BOSS_CHARGE_SHARE, tween(2100, easing = LinearEasing))

            BossEventPhase.Quiz ->
                shown.animateTo(charged.toFloat(), tween(ANSWER_WINDOW_MS, easing = LinearEasing))

            // Werdykt: zjazd na faktyczna wartosc - ale dopiero, gdy karta pod spodem doczytala
            // do konca. To jest pointa calej sceny i ma paść jako ostatnia, a nie razem z reszta.
            BossEventPhase.Judging, BossEventPhase.Payoff ->
                if (settleNow) shown.animateTo(actual.toFloat(), tween(1000, easing = EaseOutCubic))
        }
    }

    val settled = settleNow &&
        (event.phase == BossEventPhase.Judging || event.phase == BossEventPhase.Payoff)
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
        textAlign = TextAlign.Center,
        modifier = Modifier.graphicsLayer {
            val amp = if (settled) 0f else fill * 5f
            translationX = (jitter - 0.5f) * 2f * amp
            translationY = (0.5f - jitter) * 2f * amp
            val s = if (settled) 1f else beat
            scaleX = s
            scaleY = s
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
            fontSize = 14.sp,
            textAlign = TextAlign.Center
        )
    }
}

/** Ile calosci licznik nabija jeszcze przed wjazdem quizu - reszta dochodzi w trakcie odpowiadania. */
private const val BOSS_CHARGE_SHARE = 0.35f

/** Musi odpowiadac [com.lernhero.domain.combat.live.AnswerTimer.WINDOW_MS] - licznik ma isc rowno z paskiem. */
private const val ANSWER_WINDOW_MS = 18000

/**
 * Werdykt oszacowania - scena na srodku ekranu, rozpisana na cztery takty.
 *
 * ## Co sie w niej dzieje
 *
 *  1. **JAK BLISKO** wielkimi literami i pod tym prawidlowa wartosc - "1650". Sama, przez pol
 *     sekundy: to jest liczba, ktora gracz mial odgadnac i ktora ma zapamietac.
 *  2. **TWOJA ODPOWIEDZ** - "1660" wjezdza OBOK, tak samo duza. Obie wisza chwile obok siebie,
 *     zeby dalo sie je porownac wzrokiem, a nie w pamieci.
 *  3. **ODEJMOWANIE** - obie liczby zjezdzaja ku sobie i miedzy nimi wybija roznica: "10".
 *     To jest ta jedna liczba, od ktorej zalezy cala reszta.
 *  4. **CO TO ZNACZY** - dopiero teraz zdanie o ciosie bossa, i w tej samej chwili licznik stawki
 *     u gory zjezdza do faktycznej wartosci.
 *
 * Wczesniej wszystko to bylo mala karta przyklejona pod licznikiem, u samej gory ekranu, i
 * pojawialo sie praktycznie naraz. Informacja byla ta sama - tylko nie bylo jej widac.
 */
@Composable
private fun VerdictCard(event: BossEventState, beat: Int) {
    val correct = event.question.correctValue
    val guess = event.guess
    val diff = abs(guess - correct)
    val range = (event.question.maxValue - event.question.minValue).coerceAtLeast(1)
    val closeness = 1f - (diff.toFloat() / range).coerceIn(0f, 1f)

    val verdict = when {
        diff == 0 -> "SPOT ON!"
        closeness >= 0.97f -> "ALMOST PERFECT"
        closeness >= 0.85f -> "CLOSE"
        closeness >= 0.6f -> "SO-SO"
        else -> "MISS"
    }
    val verdictColor = when {
        closeness >= 0.85f -> Color(0xFF4ADE80)
        closeness >= 0.6f -> Color(0xFFFFD34D)
        else -> Color(0xFFD1453D)
    }

    val mult = event.outcome?.accuracyMultiplier ?: 1f
    val deltaPercent = ((mult - 1f) * 100f).roundToInt()

    // Takt 3 zsuwa obie liczby ku sobie - to jest cala "animacja odejmowania". Dopoki nie padnie,
    // stoja rozsuniete na tyle, zeby bylo widac, ze sa DWIE.
    val converge by animateFloatAsState(
        targetValue = if (beat >= 3) 1f else 0f,
        animationSpec = tween(420, easing = EaseOutCubic),
        label = "verdictConverge"
    )
    val spread = (NUMBER_SPREAD * (1f - converge)).dp

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(Color(0xFF0B0805).copy(alpha = 0.92f))
            .border(3.dp, verdictColor, RoundedCornerShape(20.dp))
            .padding(horizontal = 16.dp, vertical = 18.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Takt 1 - werdykt wielkimi literami. Jedna linia, ktora sama maleje do szerokosci karty:
        // "ALMOST PERFECT" w pikselowej czcionce 30 sp wychodzilo na waskim telefonie poza ekran
        // ("MOST PERFE").
        VerdictBeat(visible = beat >= 1) {
            BasicText(
                text = verdict,
                style = TextStyle(
                    color = verdictColor,
                    fontFamily = FontPixel(),
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                ),
                maxLines = 1,
                autoSize = TextAutoSize.StepBased(minFontSize = 14.sp, maxFontSize = 30.sp, stepSize = 1.sp),
                modifier = Modifier.fillMaxWidth()
            )
        }

        // Takty 1-3: prawidlowa wartosc, twoja odpowiedz obok niej, potem zjazd ku sobie.
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            VerdictBeat(visible = beat >= 1) {
                BigNumber(
                    label = "CORRECT",
                    value = "$correct",
                    color = Color(0xFFFFD34D),
                    modifier = Modifier.offset(x = -spread)
                )
            }
            VerdictBeat(visible = beat >= 2) {
                BigNumber(
                    label = "YOUR ANSWER",
                    value = "$guess",
                    color = verdictColor,
                    modifier = Modifier.offset(x = spread)
                )
            }
        }

        // Takt 3 - roznica. Wybija w skali, bo to ona rozstrzyga cale zdarzenie.
        VerdictBeat(visible = beat >= 3, punch = true) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = if (diff == 0) "PERFECT HIT" else "OFF BY",
                    color = Color(0xFF8A7A5E),
                    fontFamily = FontFirst(),
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp
                )
                Text(
                    text = "$diff",
                    color = verdictColor,
                    fontFamily = FontPixel(),
                    fontWeight = FontWeight.Bold,
                    fontSize = 46.sp
                )
            }
        }

        // Takt 4 - co ta roznica robi z ciosem bossa. Razem z nia rusza licznik stawki u gory.
        VerdictBeat(visible = beat >= 4) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = when {
                        deltaPercent < 0 -> "BOSS ATTACK WEAKENED $deltaPercent%"
                        deltaPercent > 0 -> "BOSS ATTACK STRENGTHENED +$deltaPercent%"
                        else -> "BOSS ATTACK UNCHANGED"
                    },
                    color = if (deltaPercent < 0) Color(0xFF4ADE80) else Color(0xFFD1453D),
                    fontFamily = FontPixel(),
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    textAlign = TextAlign.Center
                )
                event.outcome?.message?.let { msg ->
                    Text(
                        text = msg,
                        color = Color(0xFFBFB098),
                        fontFamily = FontFirst(),
                        fontSize = 11.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

/** Jedna z dwoch wielkich liczb werdyktu - podpis maly, liczba duza. */
@Composable
private fun BigNumber(label: String, value: String, color: Color, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.padding(horizontal = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = label,
            color = Color(0xFF8A7A5E),
            fontFamily = FontFirst(),
            fontWeight = FontWeight.Bold,
            fontSize = 10.sp
        )
        Text(
            text = value,
            color = color,
            fontFamily = FontPixel(),
            fontWeight = FontWeight.Bold,
            fontSize = 34.sp,
            maxLines = 1
        )
    }
}

/** Jak daleko od srodka startuja obie liczby, zanim zjada sie w takcie odejmowania. */
private const val NUMBER_SPREAD = 26f

/**
 * Jeden takt werdyktu - wjezdza i zostaje. [punch] to mocniejsze wejscie ze sprezyna, zarezerwowane
 * dla roznicy: to jedyna liczba, ktora ma sie w tej scenie "wybic", reszta ma po prostu wejsc.
 */
@Composable
private fun VerdictBeat(visible: Boolean, punch: Boolean = false, content: @Composable () -> Unit) {
    AnimatedVisibility(
        visible = visible,
        enter = if (punch) {
            fadeIn(tween(160)) +
                scaleIn(initialScale = 0.45f, animationSpec = tween(420, easing = EaseOutBack))
        } else {
            fadeIn(tween(220)) +
                slideInVertically(tween(280, easing = EaseOutCubic)) { it / 2 } +
                scaleIn(initialScale = 0.9f, animationSpec = tween(280, easing = EaseOutCubic))
        }
    ) {
        content()
    }
}

/**
 * Ile taktow ma werdykt i co ile wchodzi kolejny.
 *
 * 650 ms to nie jest "wolno" - to jest czas, w ktorym da sie przeczytac liczbe i przeniesc wzrok
 * na nastepna. Cztery takty zajmuja ~2s, a faza werdyktu trwa [BOSS_EVENT_JUDGING_MS], wiec na
 * koncu zostaje jeszcze chwila na obejrzenie calosci.
 */
private const val VERDICT_BEATS = 4
private const val VERDICT_BEAT_MS = 650L

@Composable
private fun VerdictValue(
    label: String,
    value: String,
    color: Color,
    size: androidx.compose.ui.unit.TextUnit = 17.sp
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, color = Color(0xFF8A7A5E), fontFamily = FontFirst(), fontSize = 8.sp)
        Text(value, color = color, fontFamily = FontPixel(), fontWeight = FontWeight.Bold, fontSize = size)
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
