package com.lernhero.game.battle.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.EaseOutBack
import androidx.compose.animation.core.EaseOutCubic
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lernhero.game.battle.BattleSpeed
import com.lernhero.game.battle.TurnOutcome
import com.lernhero.game.battle.WRONG_ANSWER_ENEMY_BONUS
import com.lernhero.shared.theme.FontFirst
import com.lernhero.shared.theme.FontPixel
import kotlinx.coroutines.delay
import kotlin.math.roundToInt

private val Good = Color(0xFF22A55E)
private val Bad = Color(0xFFD1453D)
private val Accent = Color(0xFFE0A93A)
private val Ink = Color(0xFF120E0A)
private val Muted = Color(0xFF9D8B6A)

/**
 * Rozliczenie ostatniej odpowiedzi - to, co gra sie na srodku pola bitwy w takcie
 * [com.lernhero.game.battle.TurnPhase.Impact], ZANIM ktokolwiek uderzy.
 *
 * Odpowiada na jedno pytanie, na ktore gracz wczesniej nie mial jak sobie odpowiedziec: "co
 * wlasciwie dala mi ta odpowiedz?". Dlatego zamiast golego "SALWA x1.4" pokazuje cale rownanie, w
 * kolejnosci, w jakiej sie o nim mysli:
 *
 *  1. werdykt (dobrze/zle),
 *  2. skad wzial sie mnoznik - czas odpowiedzi i ewentualne buffy, kazdy jako osobny zeton,
 *  3. co z tego wynika dla ataku druzyny - liczba, ktora DOLICZA SIE w gore od bazowej do koncowej,
 *     razem z paskiem pokazujacym roznice.
 *
 * Etapy wchodza po kolei ([stage]), a nie wszystkie naraz - dzieki temu czyta sie to jak zdanie, a
 * nie jak tabelka. Wszystkie czasy przechodza przez [BattleSpeed], wiec przy 4x/8x animacja jest
 * odpowiednio krotsza i nie hamuje przewijania walki.
 */
@Composable
internal fun TurnImpactOverlay(
    outcome: TurnOutcome?,
    visible: Boolean,
    modifier: Modifier = Modifier
) {
    // Trzymamy ostatni wynik osobno, zeby karta mogla spokojnie wyjsc (fade-out) juz po tym, jak
    // stan tury poszedl dalej - inaczej znikalaby w jednej klatce, w polowie animacji.
    var shown by remember { mutableStateOf<TurnOutcome?>(null) }
    LaunchedEffect(outcome?.id) { outcome?.let { shown = it } }

    val entry = shown ?: return
    val accent = if (entry.correct) Good else Bad

    // 0 - nic, 1 - werdykt, 2 - zetony mnoznikow, 3 - rownanie ataku.
    var stage by remember(entry.id) { mutableIntStateOf(0) }
    LaunchedEffect(entry.id) {
        stage = 1
        delay(BattleSpeed.scale(STAGE_GAP_MILLIS))
        stage = 2
        delay(BattleSpeed.scale(STAGE_GAP_MILLIS))
        stage = 3
    }

    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        // Przyciemnienie CALEJ areny pod karta. Bez niego rozliczenie konkurowalo o wzrok z tlem
        // krainy, paskami zycia i postaciami - i przegrywalo: to jest ta jedna chwila w turze,
        // w ktorej gracz ma zobaczyc, co zrobila jego odpowiedz, wiec reszta ma na nia poczekac.
        val scrim by animateFloatAsState(
            targetValue = if (visible) SCRIM_ALPHA else 0f,
            animationSpec = tween(BattleSpeed.scale(220)),
            label = "impactScrim"
        )
        if (scrim > 0.01f) {
            Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = scrim)))
        }

        AnimatedVisibility(
            visible = visible,
            enter = fadeIn(tween(BattleSpeed.scale(140))) +
                scaleIn(initialScale = 0.86f, animationSpec = tween(BattleSpeed.scale(260), easing = EaseOutBack)) +
                slideInVertically(tween(BattleSpeed.scale(260), easing = EaseOutCubic)) { it / 4 },
            exit = fadeOut(tween(BattleSpeed.scale(220)))
        ) {
            Column(
                modifier = Modifier
                    // Margines karty siedzi TUTAJ, a nie na calej nakladce - przyciemnienie ma
                    // siegac krawedzi ekranu, inaczej po bokach zostaja dwa jasne paski.
                    .padding(horizontal = 16.dp)
                    .widthIn(max = 340.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(Ink.copy(alpha = 0.96f))
                    .border(3.dp, accent, RoundedCornerShape(18.dp))
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(9.dp)
            ) {
                VerdictLine(correct = entry.correct, accent = accent, seconds = entry.answerSeconds)

                AnimatedVisibility(
                    visible = stage >= 2,
                    enter = fadeIn(tween(BattleSpeed.scale(200)))
                ) {
                    MultiplierChips(entry = entry)
                }

                AnimatedVisibility(
                    visible = stage >= 3,
                    enter = fadeIn(tween(BattleSpeed.scale(200))) +
                        slideInVertically(tween(BattleSpeed.scale(240), easing = EaseOutCubic)) { it / 3 }
                ) {
                    AttackEquation(entry = entry, accent = accent)
                }

                // Jedno zdanie po ludzku, pod rownaniem: liczby mowia ILE, a to mowi CO z tego
                // wynika. Bez niego caly overlay czytalo sie jak paragon, a nie jak skutek.
                AnimatedVisibility(
                    visible = stage >= 3,
                    enter = fadeIn(tween(BattleSpeed.scale(240)))
                ) {
                    Text(
                        text = meaningOf(entry),
                        color = accent,
                        fontFamily = FontFirst(),
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center
                    )
                }

                if (entry.ultiTriggered) {
                    AnimatedVisibility(
                        visible = stage >= 3,
                        enter = fadeIn(tween(BattleSpeed.scale(220))) +
                            scaleIn(initialScale = 0.6f, animationSpec = tween(BattleSpeed.scale(320), easing = EaseOutBack))
                    ) {
                        Text(
                            text = "⚡ ULTI!",
                            color = Accent,
                            fontFamily = FontPixel(),
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp
                        )
                    }
                }

                entry.cycleLabel?.let { label ->
                    AnimatedVisibility(visible = stage >= 3, enter = fadeIn(tween(BattleSpeed.scale(220)))) {
                        Text(
                            text = label,
                            color = Color.White.copy(alpha = 0.85f),
                            fontFamily = FontFirst(),
                            fontSize = 10.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}

/** Werdykt + ile czasu zajela odpowiedz - to z tego czasu bierze sie mnoznik ponizej. */
@Composable
private fun VerdictLine(correct: Boolean, accent: Color, seconds: Float) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = if (correct) "CORRECT!" else "MISSED!",
            color = accent,
            fontFamily = FontPixel(),
            fontWeight = FontWeight.Bold,
            fontSize = 26.sp
        )
        Text(
            text = if (correct) "answered in ${seconds.oneDecimal()}s" else "your volley lands weaker",
            color = Muted,
            fontFamily = FontFirst(),
            fontSize = 10.sp
        )
    }
}

/**
 * Skladniki mnoznika, kazdy osobno - zeby bylo widac, ze koncowe "+84%" to nie jedna magiczna
 * liczba, tylko szybkosc odpowiedzi zlozona z aktywnym buffem cyklu.
 *
 * Procenty, a nie "x1.36": po pierwsze zgadzaja sie forma z wynikiem rownania nizej, po drugie
 * przy jednym miejscu po przecinku dwa rozne mnozniki (1.36 i 1.35) wyswietlaly sie identycznie
 * jako "x1.4" i wygladaly jak zdublowany zeton.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun MultiplierChips(entry: TurnOutcome) {
    // FlowRow, bo przy dwoch zetonach naraz (szybkosc + passa) jeden rzad potrafi nie zmiescic sie
    // w szerokosci karty - wtedy drugi zeton schodzi nizej zamiast lamac tekst w srodku.
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        if (entry.correct) {
            Chip(icon = "⚡", label = "SPEED", value = entry.speedMultiplier.asPercent(), accent = Accent)
            // Neutralny cykl (x1.0) nie wnosi nic do rownania - pokazywanie go tylko rozpraszaloby.
            if (entry.cycleMultiplier != 1f) {
                val buffed = entry.cycleMultiplier > 1f
                Chip(
                    icon = if (buffed) "🔥" else "🔻",
                    label = if (buffed) "STREAK" else "SLUMP",
                    value = entry.cycleMultiplier.asPercent(),
                    accent = if (buffed) Good else Bad
                )
            }
        } else {
            // Sama kara do salwy jest juz w rownaniu ponizej - tutaj tylko to, czego rownanie NIE
            // pokazuje: ze w tej turze wrogowie tez uderza mocniej.
            Chip(icon = "🛡", label = "ENEMIES", value = "+${enemyBonusPercent()}%", accent = Bad)
        }
    }
}

@Composable
private fun Chip(icon: String, label: String, value: String, accent: Color) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(accent.copy(alpha = 0.14f))
            .border(1.5.dp, accent.copy(alpha = 0.7f), RoundedCornerShape(20.dp))
            .padding(horizontal = 9.dp, vertical = 5.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = icon, fontSize = 11.sp)
        Text(
            text = label,
            color = Muted,
            fontFamily = FontFirst(),
            fontWeight = FontWeight.Bold,
            fontSize = 8.sp
        )
        Text(
            text = value,
            color = accent,
            fontFamily = FontPixel(),
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp
        )
    }
}

/**
 * Wlasciwa pointa calego overlaya: ATAK bazowy -> ATAK po mnozniku, z liczba DOLICZAJACA SIE w gore
 * (albo w dol) zamiast podmiany w jednej klatce, i paskiem pokazujacym te roznice na oko.
 */
@Composable
private fun AttackEquation(entry: TurnOutcome, accent: Color) {
    // Sam licznik: ruszamy od bazy i plynnie dochodzimy do wartosci po mnozniku. `progress` jest
    // znormalizowany (0..1), wiec ta sama animacja obsluguje i wzmocnienie, i oslabienie.
    var run by remember(entry.id) { mutableStateOf(false) }
    LaunchedEffect(entry.id) { run = true }
    val progress by animateFloatAsState(
        targetValue = if (run) 1f else 0f,
        animationSpec = tween(BattleSpeed.scale(COUNTER_MILLIS), easing = EaseOutCubic),
        label = "attackCounter"
    )

    val current = (entry.baseAttack + (entry.boostedAttack - entry.baseAttack) * progress).roundToInt()
    val ratio = if (entry.baseAttack > 0) entry.boostedAttack.toFloat() / entry.baseAttack else 1f

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        Text(
            text = "TEAM ATTACK",
            color = Muted,
            fontFamily = FontFirst(),
            fontWeight = FontWeight.Bold,
            fontSize = 9.sp
        )

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "${entry.baseAttack}",
                color = Color.White.copy(alpha = 0.45f),
                fontFamily = FontPixel(),
                fontSize = 14.sp
            )
            Text(text = "→", color = Muted, fontSize = 13.sp)
            // Liczba lekko "pompuje" w trakcie liczenia i siada na 1.0 na koncu - drobiazg, ale to
            // on sprawia, ze wynik czuje sie jak zdobyty, a nie jak wypisany.
            Text(
                text = "$current",
                color = accent,
                fontFamily = FontPixel(),
                fontWeight = FontWeight.Bold,
                fontSize = 34.sp,
                modifier = Modifier.scale(1f + 0.12f * (1f - progress))
            )
            Text(
                text = if (entry.attackDeltaPercent >= 0) "+${entry.attackDeltaPercent}%" else "${entry.attackDeltaPercent}%",
                color = accent,
                fontFamily = FontFirst(),
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp
            )
        }

        ImpactBar(ratio = ratio, progress = progress, accent = accent)
    }
}

/**
 * Pasek roznicy: szara czesc to atak bazowy, kolorowa - to, co dolozyl (albo zabral) mnoznik.
 * Skala jest wspolna dla obu przypadkow ([MAX_BAR_RATIO]), zeby "x1.5" zawsze wygladalo tak samo.
 */
@Composable
private fun ImpactBar(ratio: Float, progress: Float, accent: Color) {
    val baseFraction = (1f / MAX_BAR_RATIO).coerceIn(0f, 1f)
    val targetFraction = (ratio / MAX_BAR_RATIO).coerceIn(0f, 1f)
    val filled = baseFraction + (targetFraction - baseFraction) * progress

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(12.dp)
            .clip(RoundedCornerShape(5.dp))
            .background(Color.White.copy(alpha = 0.10f))
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(filled)
                .height(12.dp)
                .clip(RoundedCornerShape(5.dp))
                .background(Brush.horizontalGradient(listOf(accent.copy(alpha = 0.65f), accent)))
        )
        // Znacznik "tyle bylo bez mnoznika" - bez niego pasek pokazuje sile, ale nie pokazuje zysku.
        Box(
            modifier = Modifier
                .fillMaxWidth(baseFraction)
                .height(12.dp),
            contentAlignment = Alignment.CenterEnd
        ) {
            Box(
                modifier = Modifier
                    .width(2.dp)
                    .height(12.dp)
                    .background(Color.White.copy(alpha = 0.55f))
            )
        }
    }
}

/**
 * O ile mocniej bija wrogowie po blednej odpowiedzi - druga polowa kary, ktorej gracz wczesniej
 * nie widzial nigdzie poza kodem.
 */
private fun enemyBonusPercent(): Int = ((WRONG_ANSWER_ENEMY_BONUS - 1f) * 100).roundToInt()

/** 1.36f -> "+36%", 0.75f -> "-25%" - mnoznik w tej samej formie co wynik rownania. */
private fun Float.asPercent(): String {
    val delta = ((this - 1f) * 100).roundToInt()
    return if (delta >= 0) "+$delta%" else "$delta%"
}

/** 1.35f -> "1.4" - bez kotlin.String.format, ktorego nie ma w common. */
private fun Float.oneDecimal(): String {
    val scaled = (this * 10).roundToInt()
    return "${scaled / 10}.${scaled % 10}"
}

/**
 * Skutek odpowiedzi jednym zdaniem - to, co gracz ma z karty zapamietac, gdy juz nie patrzy na
 * procenty. Kara za pudlo jest DWUCZLONOWA (slabsza salwa + mocniejsi wrogowie) i tylko tutaj widac
 * oba czlony naraz.
 */
private fun meaningOf(entry: TurnOutcome): String {
    val delta = entry.attackDeltaPercent
    return when {
        !entry.correct -> "Weaker volley, and enemies hit +${enemyBonusPercent()}% this turn"
        delta > 0 -> "Your team hits $delta% harder this turn"
        delta < 0 -> "Your team hits ${-delta}% weaker this turn"
        else -> "Your volley lands at full strength"
    }
}

/**
 * Odstep miedzy kolejnymi etapami wchodzacymi na karte (werdykt -> zetony -> rownanie).
 *
 * Etapy razem z licznikiem musza zmiescic sie w takcie [TurnPhase.Impact] - gdy ten czas rosnie
 * albo maleje, te dwie stale ida za nim, inaczej karta albo urywa sie w polowie liczenia, albo stoi
 * gotowa i tylko czeka.
 */
private const val STAGE_GAP_MILLIS = 520L

/** Ile trwa doliczanie sie liczby ataku do wartosci koncowej. */
private const val COUNTER_MILLIS = 1100

/** Jak mocno arena gasnie pod karta rozliczenia. */
private const val SCRIM_ALPHA = 0.62f

/**
 * Gorna granica skali paska: najmocniejsza mozliwa salwa to ~1.5 (pasek czasu) x 1.35 (passa) ~ 2.0.
 * Stala skala oznacza, ze ten sam mnoznik zawsze rysuje sie tak samo dlugi - pasek da sie porownywac
 * miedzy turami.
 */
private const val MAX_BAR_RATIO = 2.0f
