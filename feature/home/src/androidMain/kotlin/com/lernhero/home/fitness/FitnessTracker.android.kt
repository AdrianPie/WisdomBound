package com.lernhero.home.fitness

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.PermissionController
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.ExerciseSessionRecord
import androidx.health.connect.client.records.StepsRecord
import androidx.health.connect.client.request.AggregateRequest
import androidx.health.connect.client.request.ReadRecordsRequest
import androidx.health.connect.client.time.TimeRangeFilter
import com.lernhero.domain.fitness.FitnessAvailability
import com.lernhero.domain.fitness.FitnessSnapshot
import com.lernhero.domain.fitness.WorkoutSummary
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import org.koin.mp.KoinPlatform

/** Pakiet aplikacji Health Connect w Sklepie Play - potrzebny do "zainstaluj/zaktualizuj". */
private const val HEALTH_CONNECT_PACKAGE = "com.google.android.apps.healthdata"

/** Uprawnienia do odczytu, ktorych potrzebuje zakladka "Trening" - kroki i sesje treningowe. */
private val READ_PERMISSIONS = setOf(
    HealthPermission.getReadPermission(StepsRecord::class),
    HealthPermission.getReadPermission(ExerciseSessionRecord::class)
)

/**
 * Health Connect na Androidzie. Kontekst bierze przez Koin (ten sam, ktory MyApp rejestruje
 * przez `androidContext(this)`), wiec obiekt nie potrzebuje wlasnego konstruktora.
 *
 * Kazde wywolanie SDK jest owiniete w try/catch - to biblioteka w wersji alpha i na roznych
 * telefonach/wersjach Health Connect potrafi rzucic tam, gdzie dokumentacja obiecuje spokojny
 * status. Bez tego caly ekran "Trening" utykal w nieskonczonym ladowaniu bez zadnego komunikatu.
 */
actual object FitnessTracker {

    private fun appContext(): Context = KoinPlatform.getKoin().get()

    private fun sdkStatus(): Int = runCatching {
        HealthConnectClient.getSdkStatus(appContext())
    }.getOrDefault(HealthConnectClient.SDK_UNAVAILABLE)

    private fun clientOrNull(): HealthConnectClient? =
        runCatching {
            if (sdkStatus() == HealthConnectClient.SDK_AVAILABLE) HealthConnectClient.getOrCreate(appContext()) else null
        }.getOrNull()

    actual suspend fun checkAvailability(): FitnessAvailability = when (sdkStatus()) {
        HealthConnectClient.SDK_AVAILABLE -> FitnessAvailability.Available
        HealthConnectClient.SDK_UNAVAILABLE_PROVIDER_UPDATE_REQUIRED -> FitnessAvailability.NeedsUpdate
        else -> FitnessAvailability.NeedsInstall
    }

    actual suspend fun hasPermission(): Boolean = runCatching {
        val client = clientOrNull() ?: return false
        client.permissionController.getGrantedPermissions().containsAll(READ_PERMISSIONS)
    }.getOrDefault(false)

    actual suspend fun today(): FitnessSnapshot {
        val dateKey = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)
        val availability = checkAvailability()
        val client = clientOrNull() ?: return FitnessSnapshot.empty(dateKey = dateKey, availability = availability)
        if (!hasPermission()) return FitnessSnapshot.empty(dateKey = dateKey, availability = availability)

        val range = TimeRangeFilter.between(
            LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant(),
            Instant.now()
        )

        val steps = runCatching {
            val aggregate = client.aggregate(
                AggregateRequest(metrics = setOf(StepsRecord.COUNT_TOTAL), timeRangeFilter = range)
            )
            (aggregate[StepsRecord.COUNT_TOTAL] ?: 0L).toInt()
        }.getOrDefault(0)

        val workouts = runCatching {
            client.readRecords(
                ReadRecordsRequest(recordType = ExerciseSessionRecord::class, timeRangeFilter = range)
            ).records.map { record ->
                WorkoutSummary(
                    type = "Trening",
                    durationMinutes = Duration.between(record.startTime, record.endTime).toMinutes().toInt()
                )
            }
        }.getOrDefault(emptyList())

        return FitnessSnapshot(
            dateKey = dateKey,
            steps = steps,
            activeMinutes = workouts.sumOf { it.durationMinutes },
            workouts = workouts,
            availability = availability,
            permissionGranted = true
        )
    }

    /** Otwiera liste Health Connect w Sklepie Play (instalacja albo aktualizacja). */
    actual fun openInstallPage() {
        val context = appContext()
        val marketIntent = Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$HEALTH_CONNECT_PACKAGE"))
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        runCatching { context.startActivity(marketIntent) }.onFailure {
            val webIntent = Intent(
                Intent.ACTION_VIEW,
                Uri.parse("https://play.google.com/store/apps/details?id=$HEALTH_CONNECT_PACKAGE")
            ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            runCatching { context.startActivity(webIntent) }
        }
    }
}

/** Health Connect prosi o zgode przez wlasny ekran, otwierany kontraktem ActivityResult. */
@Composable
actual fun rememberFitnessPermissionRequester(onResult: (Boolean) -> Unit): () -> Unit {
    val launcher = rememberLauncherForActivityResult(
        contract = PermissionController.createRequestPermissionResultContract()
    ) { granted -> onResult(granted.containsAll(READ_PERMISSIONS)) }

    return remember(launcher) {
        { runCatching { launcher.launch(READ_PERMISSIONS) } }
    }
}
