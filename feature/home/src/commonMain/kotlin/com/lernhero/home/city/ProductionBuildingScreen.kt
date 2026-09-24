package com.lernhero.home.city

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Image
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.graphicsLayer
import com.lernhero.home.ui.OutlinedText
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import kotlinx.coroutines.delay
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lernhero.domain.town.ProductionBuilding
import com.lernhero.domain.town.ProductionMath
import com.lernhero.domain.town.ProductionQuiz
import com.lernhero.shared.resources.Resources
import com.lernhero.shared.theme.AppPalette
import com.lernhero.shared.theme.FontFirst
import com.lernhero.shared.theme.FontPixel
import org.jetbrains.compose.resources.painterResource
import org.koin.compose.viewmodel.koinViewModel
import kotlinx.coroutines.isActive
import kotlin.math.roundToInt

/**
 * Ekran tartaku / kamieniolomu. Rdzen "idle feelingu": widac stawke na godzine, pasek magazynu rosnie
 * w czasie rzeczywistym, a nad nim wyskakuja "+N" w miare jak przybywa surowca.
 *
 * Przycisk QUIZ otwiera prawdziwe podejscie ([ProductionQuizDialog]): piec pytan, a od liczby
 * trafien zalezy sila bonusu na 24h (patrz [ProductionQuiz]). Bonus trwa dobe i to on jest
 * blokada "raz na 24h" - dopoki chodzi, przycisk pokazuje, ile zostalo do kolejnej proby.
 */
@Composable
fun ProductionBuildingScreen(
    building: ProductionBuilding,
    onBack: () -> Unit
) {
    val viewModel = koinViewModel<TownViewModel>()
    val state by viewModel.uiState.collectAsState()

    val production = state.stateOf(building)
    val now = state.now
    val pending = ProductionMath.pendingFor(production, now)
    val cap = ProductionMath.storageCap(production.level)
    val ratePerHour = ProductionMath.effectiveRatePerHour(production, now)
    val baseRate = ProductionMath.baseRatePerHour(production.level)
    val boostActive = ProductionMath.isBoostActive(production, now)
    val boostRemaining = ProductionMath.boostRemainingMillis(production, now)
    val upgradeCost = ProductionMath.upgradeCost(production.level)
    val maxed = ProductionMath.isMaxed(production.level)

    // Nowe `attemptId` = nowy zestaw pytan. Rosnie po kazdym zamknieciu, zeby druga proba
    // (nastepnego dnia) nie dostala tych samych pieciu pytan z pamieci kompozycji.
    var quizOpen by remember { mutableStateOf(false) }
    var quizAttemptId by remember { mutableStateOf(0) }

    TownScreenScaffold(
        title = building.title(),
        resources = state.resources,
        money = state.money,
        toasts = state.toasts,
        onBack = onBack
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 14.dp, vertical = 6.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ProductionStage(
                building = building,
                pending = pending,
                ratePerHour = ratePerHour
            )

            StoragePanel(
                pending = pending,
                cap = cap,
                ratePerHour = ratePerHour,
                resourceIcon = building.resourceIcon(),
                onCollect = { viewModel.collect(building) }
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                StatTile("⭐", "Level", "${production.level}", Modifier.weight(1f))
                StatTile(
                    "⛏",
                    "Rate",
                    "${ratePerHour.roundToInt()}/h",
                    Modifier.weight(1f),
                    valueColor = if (boostActive) AppPalette.Success else Color.White
                )
                StatTile(
                    "⚡",
                    "Boost",
                    if (boostActive) "+${((production.boostMultiplier - 1f) * 100).roundToInt()}%" else "—",
                    Modifier.weight(1f),
                    caption = if (boostActive) formatBoostRemaining(boostRemaining) else "quiz it",
                    valueColor = if (boostActive) AppPalette.Success else Color.White
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                TownActionButton(
                    text = "QUIZ",
                    // Dopoki bonus trwa, dzienna proba jest zuzyta - przycisk mowi wprost, kiedy wroci.
                    subtitle = if (boostActive) {
                        "again in ${formatBoostRemaining(boostRemaining)}"
                    } else {
                        "${ProductionQuiz.QUESTION_COUNT} questions · up to +${ProductionQuiz.MAX_BOOST_PERCENT}%"
                    },
                    enabled = !boostActive,
                    modifier = Modifier.weight(1f),
                    onClick = { quizOpen = true }
                )
                TownActionButton(
                    text = if (maxed) "MAX LEVEL" else "UPGRADE",
                    // Sufit budynku jest CELOWY - patrz [ProductionMath.MAX_LEVEL].
                    subtitle = if (maxed) "level ${ProductionMath.MAX_LEVEL} of ${ProductionMath.MAX_LEVEL}" else "💰 $upgradeCost  ·  +22% rate",
                    primary = true,
                    enabled = !maxed && state.money >= upgradeCost,
                    modifier = Modifier.weight(1f),
                    onClick = { viewModel.upgrade(building) }
                )
            }
        }

        if (quizOpen) {
            ProductionQuizDialog(
                attemptId = quizAttemptId,
                onFinished = { _, boostPercent ->
                    // Bonus zapisuje sie ZAWSZE, takze przy zerze - to on otwiera 24-godzinne
                    // okno, czyli zuzywa dzienna probe (patrz ProductionQuiz).
                    viewModel.applyQuizBoost(building, boostPercent)
                    quizAttemptId += 1
                    quizOpen = false
                },
                onDismiss = {
                    quizAttemptId += 1
                    quizOpen = false
                }
            )
        }
    }
}

/** "23h 35m" / "48m" - to samo, co pokazuje pasek bonusu, tylko krotsze. */
private fun formatBoostRemaining(millis: Long): String {
    val totalMinutes = (millis / 60_000L).coerceAtLeast(0)
    val hours = totalMinutes / 60
    val minutes = totalMinutes % 60
    return if (hours > 0) "${hours}h ${minutes}m" else "${minutes}m"
}

/** Wyspa budynku w poswiacie + wielki licznik tego, co kapie do magazynu, z wyskakujacymi "+N". */
@Composable
private fun ProductionStage(
    building: ProductionBuilding,
    pending: Int,
    ratePerHour: Float
) {
    val infinite = rememberInfiniteTransition()
    val bob by infinite.animateFloat(0f, 1f, infiniteRepeatable(tween(2200, easing = FastOutSlowInEasing), RepeatMode.Reverse))
    val glow by infinite.animateFloat(0.5f, 0.9f, infiniteRepeatable(tween(1800), RepeatMode.Reverse))

    Box(modifier = Modifier.fillMaxWidth().height(270.dp)) {
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .offset(y = 20.dp)
                .fillMaxWidth()
                .height(220.dp)
                .alpha(glow)
                .background(Brush.radialGradient(listOf(Color(0x66FFC76B), Color.Transparent)), RoundedCornerShape(50))
        )
        Image(
            painter = painterResource(building.islandArt()),
            contentDescription = building.title(),
            modifier = Modifier
                .size(250.dp)
                .align(Alignment.Center)
                .offset(y = 22.dp)
                .graphicsLayer { translationY = -bob * 8f * density }
        )

        // Wielki, zywy licznik nad budynkiem - on daje poczucie "leci, pracuje".
        Row(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .clip(RoundedCornerShape(50))
                .background(Color(0xE60C0806))
                .border(2.5.dp, Color(0xFF120B05), RoundedCornerShape(50))
                .padding(start = 12.dp, end = 6.dp, top = 4.dp, bottom = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = building.resourceIcon(), fontSize = 26.sp)
            OutlinedText(text = "$pending", size = 28.sp, color = Gold)
            Text(
                text = "+${ratePerHour.roundToInt()}/h",
                color = Color.White,
                fontFamily = FontFirst(),
                fontWeight = FontWeight.Black,
                fontSize = 13.sp,
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(Brush.verticalGradient(listOf(Color(0xFF6FD04A), Color(0xFF2F8A22))))
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            )
        }

        GainPopups(
            pending = pending,
            icon = building.resourceIcon(),
            modifier = Modifier.align(Alignment.TopCenter).padding(top = 64.dp)
        )
    }
}

/** Kafelek statystyki: ikona, wielka wartosc, maly podpis. */
@Composable
private fun StatTile(
    icon: String,
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    caption: String? = null,
    valueColor: Color = Color.White
) {
    TownPanel(modifier = modifier) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp, horizontal = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(text = icon, fontSize = 18.sp)
            OutlinedText(text = value, size = 18.sp, color = valueColor, maxLines = 1)
            Text(
                text = caption ?: label.uppercase(),
                color = Muted,
                fontFamily = FontFirst(),
                fontWeight = FontWeight.Bold,
                fontSize = 10.sp,
                maxLines = 1
            )
        }
    }
}

/**
 * Pasek "tempa" produkcji - w odroznieniu od [StoragePanel] (ktory pokazuje ile surowca CZEKA
 * w magazynie), ten pokazuje JAK SZYBKO on przybywa: wypelnia sie od 0 do pelna, strzela "+1" i
 * zaczyna od nowa - jeden cykl = jedna jednostka surowca, dokladnie tak jak w idlach.
 *
 * Czysto kosmetyczny - NIE liczy realnej produkcji (ta zawsze liczy [ProductionMath] z czasu,
 * patrz [ProductionBuildingScreen]), wiec offline/desync nie ma tu znaczenia. Dlugosc cyklu =
 * 1h / stawka: przy 60/h to jeden "+1" co sekunde, a przy 10 000/h cykl trwa ~360ms, wiec pasek
 * praktycznie miga i "+1" leci jak z karabinu - dokladnie o to chodzilo.
 */
@Composable
private fun ProductionPulse(
    ratePerHour: Float,
    resourceIcon: String
) {
    // Ponizej pewnego pulapu dalsze przyspieszanie juz nie doda nic do wrazenia (ludzkie oko i tak
    // widzi tylko ciagle miganie), a rozpedzalby sie coroutine loop bez sensu - stad twardy dol.
    val cycleMillis = (3_600_000f / ratePerHour.coerceAtLeast(1f))
        .coerceIn(MIN_PULSE_CYCLE_MS.toFloat(), MAX_PULSE_CYCLE_MS.toFloat())
        .toInt()

    val progress = remember { Animatable(0f) }
    var pulseId by remember { mutableStateOf(0L) }

    LaunchedEffect(cycleMillis) {
        while (isActive) {
            progress.snapTo(0f)
            progress.animateTo(1f, animationSpec = tween(cycleMillis, easing = LinearEasing))
            pulseId++
        }
    }

    val rapidFire = cycleMillis <= RAPID_FIRE_THRESHOLD_MS

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = if (rapidFire) "⚡ MAX SPEED" else "next +1",
            color = if (rapidFire) AppPalette.Warning else Muted,
            fontFamily = FontFirst(),
            fontWeight = FontWeight.Bold,
            fontSize = 11.sp
        )
        Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.CenterEnd) {
            TownProgressBar(
                progress = progress.value,
                fill = if (rapidFire) AppPalette.Warning else AppPalette.Success,
                height = 12.dp
            )
            PulsePop(pulseId = pulseId, icon = resourceIcon)
        }
        Text(
            text = cycleMillis.asPulseLabel(),
            color = Muted,
            fontFamily = FontFirst(),
            fontWeight = FontWeight.Bold,
            fontSize = 11.sp
        )
    }
}

/** "+1" ktore mruga przy kazdym zamknieciu cyklu - przy bardzo krotkich cyklach to wlasnie ten
 * flash powtarzajacy sie kilka/kilkanascie razy na sekunde daje wrazenie "strzelania". */
@Composable
private fun PulsePop(pulseId: Long, icon: String) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(pulseId) {
        if (pulseId == 0L) return@LaunchedEffect
        visible = true
        delay(PULSE_POP_VISIBLE_MS)
        visible = false
    }

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(tween(40)) + scaleIn(initialScale = 0.6f, animationSpec = tween(80)),
        exit = fadeOut(tween(120))
    ) {
        Text(
            text = "+1 $icon",
            color = Color.White,
            fontFamily = FontPixel(),
            fontWeight = FontWeight.Bold,
            fontSize = 11.sp,
            modifier = Modifier.padding(end = 6.dp)
        )
    }
}

/** 850 -> "0.85s", 1500 -> "1.5s" - krotki, czytelny opis dlugosci cyklu. */
private fun Int.asPulseLabel(): String {
    val tenths = (this / 100)
    return "${tenths / 10}.${tenths % 10}s"
}

private const val MIN_PULSE_CYCLE_MS = 45
private const val MAX_PULSE_CYCLE_MS = 4000
private const val RAPID_FIRE_THRESHOLD_MS = 150
private const val PULSE_POP_VISIBLE_MS = 260L

/**
 * Wyskakujace "+N". Nie mamy zadnego zdarzenia "wyprodukowano" - produkcja to funkcja czasu - wiec
 * popup odpalamy po prostu wtedy, gdy licznik [pending] urosnie, i pokazujemy o ile.
 */
@Composable
private fun GainPopups(
    pending: Int,
    icon: String,
    modifier: Modifier = Modifier
) {
    val lastSeen = remember { mutableStateOf(pending) }
    var gain by remember { mutableStateOf(0) }
    var visible by remember { mutableStateOf(false) }

    LaunchedEffect(pending) {
        val previous = lastSeen.value
        lastSeen.value = pending
        when {
            // Przybylo surowca - pokaz o ile.
            pending > previous -> {
                gain = pending - previous
                visible = true
                delay(POPUP_VISIBLE_MILLIS)
                visible = false
            }
            // Zebrano surowiec (licznik wrocil do zera) - nie ma czego pokazywac.
            pending < previous -> visible = false
        }
    }

    Box(modifier = modifier) {
        AnimatedVisibility(
            visible = visible,
            enter = fadeIn(tween(120)),
            exit = fadeOut(tween(600)) + slideOutVertically(tween(600)) { -it }
        ) {
            Text(
                text = "+$gain $icon",
                color = AppPalette.Success,
                fontFamily = FontPixel(),
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
        }
    }
}

private const val POPUP_VISIBLE_MILLIS = 700L

@Composable
private fun StoragePanel(
    pending: Int,
    cap: Int,
    ratePerHour: Float,
    resourceIcon: String,
    onCollect: () -> Unit
) {
    val full = pending >= cap
    TownPanel(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TownSectionLabel("Storage")
                if (full) {
                    OutlinedText(text = "FULL - collect!", size = 13.sp, color = AppPalette.Warning, strokeWidth = 4f)
                }
            }

            TownProgressBar(
                progress = if (cap > 0) pending.toFloat() / cap else 0f,
                fill = if (full) AppPalette.Warning else Color(0xFF4FB3FF),
                height = 28.dp,
                label = "$pending / $cap"
            )

            ProductionPulse(ratePerHour = ratePerHour, resourceIcon = resourceIcon)

            TownActionButton(
                text = "COLLECT  $resourceIcon $pending",
                primary = true,
                enabled = pending > 0,
                modifier = Modifier.fillMaxWidth(),
                onClick = onCollect
            )
        }
    }
}

private fun ProductionBuilding.title(): String = when (this) {
    ProductionBuilding.Quarry -> "Quarry"
}

private fun ProductionBuilding.resourceIcon(): String = when (this) {
    ProductionBuilding.Quarry -> "🪨"
}

private fun ProductionBuilding.islandArt() = when (this) {
    ProductionBuilding.Quarry -> Resources.CityIsland.quarry
}
