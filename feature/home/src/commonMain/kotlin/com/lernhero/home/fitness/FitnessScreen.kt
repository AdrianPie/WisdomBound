package com.lernhero.home.fitness

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.ui.draw.scale
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import com.lernhero.shared.resources.Resources
import org.jetbrains.compose.resources.painterResource
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lernhero.domain.fitness.FitnessAvailability
import com.lernhero.domain.fitness.FitnessGoal
import com.lernhero.domain.fitness.FitnessGoalCatalog
import com.lernhero.domain.fitness.FitnessGoalKind
import com.lernhero.domain.fitness.FitnessSnapshot
import com.lernhero.domain.fitness.StepChest
import com.lernhero.domain.run.ChestSlot
import com.lernhero.home.city.TownViewModel
import com.lernhero.home.player.PlayerViewModel
import com.lernhero.home.story.BossChestOverlay
import com.lernhero.home.story.resolveChestPrize
import com.lernhero.shared.theme.FontFirst
import com.lernhero.shared.theme.FontPixel
import org.koin.compose.viewmodel.koinViewModel
import kotlin.random.Random

private val Panel = Color(0xFF1A1410)
private val Ink = Color(0xFF0F0B08)
private val Gold = Color(0xFFFFD100)
private val GoldFrame = Color(0xFFC8A45A)
private val GoldDim = Color(0xFF6B5A38)
private val Muted = Color(0xFF9D8B6A)
private val Green = Color(0xFF4ADE80)
private val Locked = Color(0xFF4A4030)

/**
 * Ten sam wylacznik co SHOW_DEV_TOOLS w HomeScreen.kt (osobna stala, bo to inny plik/modul) -
 * MUSI byc false przed kazdym wydaniem do sklepu. Przelacz recznie na true lokalnie, gdy testujesz
 * na emulatorze bez danych Health Connect, i wroc do false przed buildem release.
 */
private const val SHOW_DEV_TOOLS = true

/**
 * Zakladka "Trening": dzisiejsza aktywnosc z zegarka/telefonu plus cele, za ktore placi sie
 * zlotem. Bez zgody platformy pokazuje tylko przycisk "Polacz" - zadnych fikcyjnych liczb.
 */
@Composable
fun FitnessScreen(insets: PaddingValues = PaddingValues(0.dp)) {
    val viewModel = koinViewModel<FitnessViewModel>()
    val state by viewModel.uiState.collectAsState()

    val requestPermission = rememberFitnessPermissionRequester(onResult = viewModel::onPermissionResult)

    LaunchedEffect(state.message) {
        if (state.message != null) {
            kotlinx.coroutines.delay(2500)
            viewModel.clearMessage()
        }
    }

    val playerViewModel = koinViewModel<PlayerViewModel>()
    val townViewModel = koinViewModel<TownViewModel>()

    // Przezroczyste tlo - niebo Miasta z HomeNavGraph ma przebijac tak samo jak na innych zakladkach.
    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(insets)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = "Training",
                color = Gold,
                fontFamily = FontPixel(),
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.headlineSmall
            )

            // Poza `when` nizej celowo - musi dzialac TAKZE zanim zgoda na Health Connect zostanie
            // przyznana (np. na emulatorze, ktory czesto w ogole nie potrafi przejsc tego flow).
            // SHOW_DEV_TOOLS = ten sam wylacznik-konwencja co na Domu (HomeScreen.kt) - MUSI byc
            // false przed kazdym wydaniem do sklepu.
            if (SHOW_DEV_TOOLS) {
                DevAddStepsButton(onClick = { viewModel.addDebugSteps(1000) })
            }

            when {
                state.loading -> LoadingCard()
                !state.permissionGranted -> ConnectCard(
                    availability = state.availability,
                    onConnect = requestPermission,
                    onOpenStore = viewModel::openInstallPage,
                    onRetry = viewModel::refresh
                )
                else -> {
                    // Skrzynia stoi NAD dzisiejszymi liczbami - to ona jest powodem, dla ktorego
                    // wchodzi sie tu w ciagu dnia ("ile mi jeszcze brakuje"), a nie tabelka krokow.
                    StepChestCard(
                        snapshot = state.snapshot,
                        openedToday = state.stepChestOpenedToday,
                        onOpen = viewModel::openStepChest
                    )

                    TodayCard(snapshot = state.snapshot)
                    Text(
                        text = "Daily Goals",
                        color = Gold,
                        fontFamily = FontPixel(),
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium
                    )
                    FitnessGoalCatalog.DAILY.forEach { goal ->
                        GoalCard(
                            goal = goal,
                            snapshot = state.snapshot,
                            claimed = state.claimedToday(goal),
                            onClaim = { viewModel.claim(goal) }
                        )
                    }
                }
            }

            state.message?.let { message ->
                Text(
                    text = message,
                    color = Green,
                    fontFamily = FontFirst(),
                    fontSize = 12.sp
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
        }

        // Kolo fortuny ze skrzyni - dokladnie to samo, ktore krecisz po bossie (ChestSlot/ChestWheel),
        // wiec dzienna skrzynia za kroki gra w te sama gre co reszta lupu, a nie w osobna, wlasna.
        state.pendingChestSlot?.let { slotName ->
            val slot = ChestSlot.valueOf(slotName)
            // Nagroda musi byc rozwiazana RAZ: inaczej kazda rekompozycja losowalaby inny przedmiot
            // i podpis na kole nie zgadzalby sie z tym, co faktycznie wpada do ekwipunku.
            val prize = remember(slotName) { resolveChestPrize(slot, Random.Default) }
            BossChestOverlay(
                dungeonLevel = StepChest.wheelLevel(state.snapshot.steps),
                prize = slot,
                prizeLabel = prize.label,
                onClaim = {
                    prize.grant(playerViewModel, townViewModel)
                    viewModel.consumeStepChest()
                }
            )
        }
    }
}

/**
 * Dzienna skrzynia za kroki - trzy stany, kazdy mowi jednym spojrzeniem, co robic:
 *
 *  - ZAMKNIETA: ile jeszcze krokow brakuje, pasek postepu, skrzynia przygaszona,
 *  - GOTOWA: skrzynia pulsuje i swieci, jest przycisk OTWORZ,
 *  - OTWARTA DZIS: skrzynia otwarta, przygaszona, i informacja, ze wraca jutro.
 *
 * Pulsowanie chodzi WYLACZNIE w stanie gotowym - to jedyny moment, w ktorym cos od gracza zalezy,
 * wiec tylko wtedy karta zaczepia wzrok.
 */
@Composable
private fun StepChestCard(
    snapshot: FitnessSnapshot,
    openedToday: Boolean,
    onOpen: () -> Unit
) {
    val unlocked = StepChest.isUnlocked(snapshot)
    val ready = unlocked && !openedToday
    val progress = StepChest.progress(snapshot)

    val transition = rememberInfiniteTransition(label = "chestPulse")
    val pulse by transition.animateFloat(
        initialValue = 1f,
        targetValue = if (ready) 1.10f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "chestPulseScale"
    )
    val animatedProgress by animateFloatAsState(targetValue = progress, label = "chestProgress")

    val frame = when {
        ready -> Gold
        openedToday -> GoldDim
        else -> GoldFrame.copy(alpha = 0.5f)
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(if (ready) Color(0xFF241B10) else Panel)
            .border(if (ready) 2.dp else 1.dp, frame, RoundedCornerShape(14.dp))
            .then(if (ready) Modifier.clickable(onClick = onOpen) else Modifier)
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Ta sama grafika skrzyni co na kole fortuny po bossie - jedna skrzynia w calej grze,
            // a nie emoji-zastepnik obok prawdziwego sprite'a. Po otwarciu przygaszona.
            Image(
                painter = painterResource(Resources.Image.treasureChest),
                contentDescription = null,
                alpha = if (openedToday) 0.35f else 1f,
                modifier = Modifier.size(44.dp).scale(pulse)
            )
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = "Daily step chest",
                    color = if (ready) Gold else Muted,
                    fontFamily = FontPixel(),
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
                Text(
                    text = when {
                        openedToday -> "Opened today - comes back tomorrow."
                        ready -> "Unlocked! Spin the wheel for loot."
                        else -> "${StepChest.stepsRemaining(snapshot)} steps to go."
                    },
                    color = Muted,
                    fontFamily = FontFirst(),
                    fontSize = 11.sp
                )
            }
        }

        // Licznik zostaje widoczny w KAZDYM stanie - takze po otwarciu, bo "ile dzis zrobilem"
        // jest ciekawe samo w sobie, nie tylko jako warunek skrzyni.
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "${snapshot.steps} / ${StepChest.STEPS_REQUIRED}",
                    color = if (unlocked) Green else Muted,
                    fontFamily = FontPixel(),
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
                Text(
                    text = "${(progress * 100).toInt()}%",
                    color = if (unlocked) Green else Muted,
                    fontFamily = FontFirst(),
                    fontSize = 11.sp
                )
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(Ink)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(animatedProgress)
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(if (unlocked) Green else GoldFrame)
                )
            }
        }

        if (ready) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(Gold)
                    .clickable(onClick = onOpen)
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "OPEN CHEST",
                    color = Ink,
                    fontFamily = FontPixel(),
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
            }
        }
    }
}

@Composable
private fun DevAddStepsButton(onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(Color(0xFF3A1F1F))
            .border(1.dp, Color(0xFFB05050), RoundedCornerShape(10.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "DEV: +1000 steps",
            color = Color(0xFFFF8A8A),
            fontFamily = FontPixel(),
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp
        )
    }
}

@Composable
private fun LoadingCard() {
    Box(
        modifier = Modifier.fillMaxWidth().padding(vertical = 40.dp),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(color = Gold)
    }
}

/**
 * Brak zgody, brak Health Connect albo za stara wersja - kazdy stan ma wlasny, jasny komunikat
 * i konkretny przycisk do klikniecia, zamiast martwego ekranu bez wyjasnienia.
 */
@Composable
private fun ConnectCard(
    availability: FitnessAvailability,
    onConnect: () -> Unit,
    onOpenStore: () -> Unit,
    onRetry: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Panel)
            .border(2.dp, GoldDim, RoundedCornerShape(12.dp))
            .padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = when (availability) {
                FitnessAvailability.Available -> "Connect phone health"
                FitnessAvailability.NeedsInstall -> "Health Connect missing"
                FitnessAvailability.NeedsUpdate -> "Health Connect needs an update"
                FitnessAvailability.Unsupported -> "No data source"
            },
            color = Gold,
            fontFamily = FontPixel(),
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.titleMedium
        )
        Text(
            text = when (availability) {
                FitnessAvailability.Available ->
                    "Grant access to steps and workouts to claim rewards for activity."
                FitnessAvailability.NeedsInstall ->
                    "Install the free Health Connect app from the Play Store so this tab can see your steps."
                FitnessAvailability.NeedsUpdate ->
                    "You have Health Connect installed, but it's too old. Update it from the Play Store."
                FitnessAvailability.Unsupported ->
                    "This platform doesn't provide activity data."
            },
            color = Muted,
            fontFamily = FontFirst(),
            fontSize = 12.sp,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
        when (availability) {
            FitnessAvailability.Available -> Button(
                onClick = onConnect,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFB4451F), contentColor = Color.White)
            ) {
                Text(text = "Connect", fontFamily = FontFirst(), fontWeight = FontWeight.Bold)
            }

            FitnessAvailability.NeedsInstall, FitnessAvailability.NeedsUpdate -> {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = onOpenStore,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFB4451F), contentColor = Color.White)
                    ) {
                        Text(
                            text = if (availability == FitnessAvailability.NeedsInstall) "Install" else "Update",
                            fontFamily = FontFirst(),
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Button(
                        onClick = onRetry,
                        colors = ButtonDefaults.buttonColors(containerColor = Ink, contentColor = Gold)
                    ) {
                        Text(text = "Check Again", fontFamily = FontFirst(), fontWeight = FontWeight.Bold)
                    }
                }
            }

            FitnessAvailability.Unsupported -> Unit
        }
    }
}

@Composable
private fun TodayCard(snapshot: FitnessSnapshot) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Panel)
            .border(2.dp, GoldFrame, RoundedCornerShape(12.dp))
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        StatBlock(label = "Steps", value = "${snapshot.steps}")
        StatBlock(label = "Active min.", value = "${snapshot.activeMinutes}")
        StatBlock(label = "Workouts", value = "${snapshot.workouts.size}")
    }
}

@Composable
private fun StatBlock(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = value, color = Gold, fontFamily = FontPixel(), fontWeight = FontWeight.Bold, fontSize = 22.sp)
        Text(text = label, color = Muted, fontFamily = FontFirst(), fontSize = 11.sp)
    }
}

@Composable
private fun GoalCard(
    goal: FitnessGoal,
    snapshot: FitnessSnapshot,
    claimed: Boolean,
    onClaim: () -> Unit
) {
    val progress = goal.progress(snapshot).coerceAtMost(goal.threshold)
    val complete = goal.isComplete(snapshot)
    val ratio = if (goal.threshold <= 0) 0f else progress.toFloat() / goal.threshold.toFloat()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(Ink)
            .border(1.5.dp, if (complete && !claimed) Gold else GoldDim, RoundedCornerShape(10.dp))
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(text = goal.title, color = Gold, fontFamily = FontPixel(), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Text(text = goal.description, color = Muted, fontFamily = FontFirst(), fontSize = 11.sp)
            }
            Text(
                text = "+${goal.reward} zl",
                color = GoldFrame,
                fontFamily = FontFirst(),
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp
            )
        }

        GoalProgressBar(ratio = ratio, complete = complete)

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "$progress / ${goal.threshold}${goal.unitSuffix()}",
                color = Muted,
                fontFamily = FontFirst(),
                fontSize = 10.sp
            )
            ClaimButton(complete = complete, claimed = claimed, onClaim = onClaim)
        }
    }
}

private fun FitnessGoal.unitSuffix(): String = when (kind) {
    FitnessGoalKind.Steps -> " krokow"
    FitnessGoalKind.ActiveMinutes -> " min"
    FitnessGoalKind.Workout -> " treningow"
}

@Composable
private fun GoalProgressBar(ratio: Float, complete: Boolean) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(8.dp)
            .clip(RoundedCornerShape(4.dp))
            .background(Locked)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(ratio.coerceIn(0f, 1f))
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(if (complete) Green else GoldFrame)
        )
    }
}

@Composable
private fun ClaimButton(complete: Boolean, claimed: Boolean, onClaim: () -> Unit) {
    val enabled = complete && !claimed
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(
                when {
                    claimed -> Ink
                    enabled -> Color(0xFFB4451F)
                    else -> Ink
                }
            )
            .border(1.dp, if (enabled) Gold else GoldDim, RoundedCornerShape(8.dp))
            .clickable(enabled = enabled, onClick = onClaim)
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(
            text = when {
                claimed -> "Claimed"
                complete -> "Claim"
                else -> "Too Early"
            },
            color = when {
                claimed -> Green
                enabled -> Color.White
                else -> Muted
            },
            fontFamily = FontFirst(),
            fontWeight = FontWeight.Bold,
            fontSize = 11.sp
        )
    }
}
