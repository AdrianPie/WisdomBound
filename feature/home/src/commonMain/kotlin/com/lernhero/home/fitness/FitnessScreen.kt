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
import com.lernhero.shared.theme.FontFirst
import com.lernhero.shared.theme.FontPixel
import org.koin.compose.viewmodel.koinViewModel

private val Panel = Color(0xFF1A1410)
private val Ink = Color(0xFF0F0B08)
private val Gold = Color(0xFFFFD100)
private val GoldFrame = Color(0xFFC8A45A)
private val GoldDim = Color(0xFF6B5A38)
private val Muted = Color(0xFF9D8B6A)
private val Green = Color(0xFF4ADE80)
private val Locked = Color(0xFF4A4030)

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

            when {
                state.loading -> LoadingCard()
                !state.permissionGranted -> ConnectCard(
                    availability = state.availability,
                    onConnect = requestPermission,
                    onOpenStore = viewModel::openInstallPage,
                    onRetry = viewModel::refresh
                )
                else -> {
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
