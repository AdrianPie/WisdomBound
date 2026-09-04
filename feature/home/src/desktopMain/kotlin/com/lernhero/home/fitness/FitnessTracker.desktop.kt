package com.lernhero.home.fitness

import androidx.compose.runtime.Composable
import com.lernhero.domain.fitness.FitnessAvailability
import com.lernhero.domain.fitness.FitnessSnapshot

/** Desktop nie ma zadnego API zdrowia - zakladka pokaze wprost, ze nie ma tu czego laczyc. */
actual object FitnessTracker {
    actual suspend fun checkAvailability(): FitnessAvailability = FitnessAvailability.Unsupported
    actual suspend fun hasPermission(): Boolean = false
    actual suspend fun today(): FitnessSnapshot = FitnessSnapshot.empty(availability = FitnessAvailability.Unsupported)
    actual fun openInstallPage() = Unit
}

@Composable
actual fun rememberFitnessPermissionRequester(onResult: (Boolean) -> Unit): () -> Unit =
    { onResult(false) }
