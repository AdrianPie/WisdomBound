package com.lernhero.home.fitness

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import com.lernhero.domain.fitness.FitnessAvailability
import com.lernhero.domain.fitness.FitnessSnapshot
import com.lernhero.domain.fitness.WorkoutSummary
import kotlin.coroutines.resume
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import platform.Foundation.NSCalendar
import platform.Foundation.NSDate
import platform.Foundation.NSDateFormatter
import platform.Foundation.NSUserDefaults
// Wildcard, bo sporo z tych funkcji (predicateForSamplesWithStartDate, countUnit...) to
// rozszerzenia pakietowe wygenerowane z kategorii Objective-C - samo zaimportowanie klasy
// (np. HKQuery) ich nie sciaga do zasiegu, trzeba zaimportowac caly pakiet.
import platform.HealthKit.*

/**
 * HealthKit odmawia mowic apce wprost "odmowiono odczytu" (prywatnosc - inaczej apka moglaby
 * wnioskowac o stanie zdrowia z samej odmowy). Jedyny sygnal, jaki dostajemy, to czy prosba
 * o zgode zostala w ogole wyslana - to zapamietujemy lokalnie. Jesli user faktycznie odmowil,
 * zapytania po prostu wroca zerami, a ekran pokaze puste cele zamiast klamac.
 */
private const val PERMISSION_REQUESTED_KEY = "lernhero_fitness_permission_requested"

private val healthStore by lazy { HKHealthStore() }
private val stepType by lazy { HKQuantityType.quantityTypeForIdentifier(HKQuantityTypeIdentifierStepCount)!! }
private val workoutType by lazy { HKObjectType.workoutType() }

actual object FitnessTracker {

    private fun isAvailable(): Boolean = HKHealthStore.isHealthDataAvailable()

    actual suspend fun checkAvailability(): FitnessAvailability =
        if (isAvailable()) FitnessAvailability.Available else FitnessAvailability.NeedsInstall

    actual suspend fun hasPermission(): Boolean =
        isAvailable() && NSUserDefaults.standardUserDefaults.boolForKey(PERMISSION_REQUESTED_KEY)

    actual suspend fun today(): FitnessSnapshot {
        val dateKey = todayDateKey()
        val availability = checkAvailability()
        if (availability != FitnessAvailability.Available) return FitnessSnapshot.empty(dateKey = dateKey, availability = availability)
        if (!hasPermission()) return FitnessSnapshot.empty(dateKey = dateKey, availability = availability)

        val workouts = readWorkoutsToday()
        return FitnessSnapshot(
            dateKey = dateKey,
            steps = readStepsToday(),
            activeMinutes = workouts.sumOf { it.durationMinutes },
            workouts = workouts,
            availability = availability,
            permissionGranted = true
        )
    }

    /** Na iOS nie ma czego instalowac - Zdrowie jest wbudowane albo go nie ma na tym urzadzeniu. */
    actual fun openInstallPage() = Unit
}

private fun todayDateKey(): String {
    val formatter = NSDateFormatter()
    formatter.dateFormat = "yyyy-MM-dd"
    return formatter.stringFromDate(NSDate())
}

private fun startOfToday(): NSDate = NSCalendar.currentCalendar.startOfDayForDate(NSDate())

private suspend fun readStepsToday(): Int = suspendCancellableCoroutine { continuation ->
    val predicate = HKQuery.predicateForSamplesWithStartDate(
        startOfToday(),
        NSDate(),
        HKQueryOptionStrictStartDate
    )
    val query = HKStatisticsQuery(
        stepType,
        predicate,
        HKStatisticsOptionCumulativeSum
    ) { _, statistics, _ ->
        val sum = statistics?.sumQuantity()?.doubleValueForUnit(HKUnit.countUnit())
        continuation.resume((sum ?: 0.0).toInt())
    }
    healthStore.executeQuery(query)
}

private suspend fun readWorkoutsToday(): List<WorkoutSummary> = suspendCancellableCoroutine { continuation ->
    val predicate = HKQuery.predicateForSamplesWithStartDate(
        startOfToday(),
        NSDate(),
        HKQueryOptionStrictStartDate
    )
    val query = HKSampleQuery(
        workoutType,
        predicate,
        0uL,
        null
    ) { _, samples, _ ->
        val workouts = samples.orEmpty().filterIsInstance<HKWorkout>().map { workout ->
            WorkoutSummary(type = "Workout", durationMinutes = (workout.duration / 60.0).toInt())
        }
        continuation.resume(workouts)
    }
    healthStore.executeQuery(query)
}

private suspend fun requestAuthorization(): Boolean = suspendCancellableCoroutine { continuation ->
    healthStore.requestAuthorizationToShareTypes(
        null,
        setOf(stepType, workoutType)
    ) { success, _ -> continuation.resume(success) }
}

@Composable
actual fun rememberFitnessPermissionRequester(onResult: (Boolean) -> Unit): () -> Unit {
    val scope = rememberCoroutineScope()
    return remember {
        {
            scope.launch {
                val granted = requestAuthorization()
                NSUserDefaults.standardUserDefaults.setBool(true, PERMISSION_REQUESTED_KEY)
                NSUserDefaults.standardUserDefaults.synchronize()
                onResult(granted)
            }
        }
    }
}
