package com.lernhero.shared.notification

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.lernhero.domain.notification.GameReminder
import com.lernhero.domain.notification.ReminderKind
import java.util.concurrent.TimeUnit

/**
 * Kontekst aplikacji dla warstwy powiadomien. Ustawia go [MyApp] przy starcie.
 *
 * Osobny uchwyt, a nie wstrzykiwanie Contextu w konstruktor, bo [platformReminderScheduler] jest
 * `expect fun` bez argumentow - taki sam ksztalt musi dzialac na iOS, gdzie zadnego Contextu nie ma.
 */
object ReminderContext {
    @Volatile
    var application: Context? = null
}

actual fun platformReminderScheduler(): ReminderScheduler =
    ReminderContext.application?.let(::AndroidReminderScheduler) ?: ReminderScheduler.None

/**
 * Przypomnienia na Androidzie przez WorkManager.
 *
 * WorkManager, a nie AlarmManager: od Androida 12 dokladne alarmy wymagaja osobnego uprawnienia
 * (SCHEDULE_EXACT_ALARM), a zadne z naszych trzech przypomnien nie potrzebuje precyzji co do
 * sekundy - "magazyn sie zapelnil" moze przyjsc kwadrans pozniej i nic to nie zmienia. W zamian
 * dostajemy zadanie, ktore przezywa restart telefonu.
 *
 * Kazde przypomnienie ma STALA nazwe zadania ([ReminderKind.id]) i wchodzi z
 * [ExistingWorkPolicy.REPLACE], wiec przeliczenie planu przy kazdym wejsciu do gry nadpisuje
 * kolejke zamiast dokladac do niej kolejne kopie.
 */
private class AndroidReminderScheduler(context: Context) : ReminderScheduler {

    private val appContext = context.applicationContext
    private val workManager get() = WorkManager.getInstance(appContext)

    override val isPermitted: Boolean
        get() {
            // Do Androida 12 wlacznie nie ma o co pytac - powiadomienia sa domyslnie dozwolone.
            // Od 13 (TIRAMISU) potrzebna jest zgoda POST_NOTIFICATIONS, o ktora prosi ekran.
            val granted = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                ContextCompat.checkSelfPermission(
                    appContext,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
            } else {
                true
            }
            // Gracz mogl tez wylaczyc powiadomienia aplikacji w ustawieniach systemu - wtedy
            // uprawnienie formalnie jest, a i tak nic sie nie pokaze.
            return granted && NotificationManagerCompat.from(appContext).areNotificationsEnabled()
        }

    override fun schedule(reminders: List<GameReminder>) {
        ensureChannel()

        // Plan jest jedynym zrodlem prawdy: rodzaje, ktorych w nim NIE ma, znikaja z kolejki.
        // Bez tego przypomnienie o darmowym summonie zostaloby zaplanowane po jego odebraniu.
        val planned = reminders.associateBy { it.kind }
        ReminderKind.entries.forEach { kind ->
            val reminder = planned[kind]
            if (reminder == null) {
                workManager.cancelUniqueWork(kind.id)
                return@forEach
            }

            val delayMs = reminder.triggerAtMs - System.currentTimeMillis()
            if (delayMs <= 0L) {
                // Moment juz minal - nie pokazujemy powiadomienia o przeszlosci.
                workManager.cancelUniqueWork(kind.id)
                return@forEach
            }

            val request = OneTimeWorkRequestBuilder<ReminderWorker>()
                .setInitialDelay(delayMs, TimeUnit.MILLISECONDS)
                .setInputData(
                    Data.Builder()
                        .putString(ReminderWorker.KEY_ID, kind.id)
                        .putString(ReminderWorker.KEY_TITLE, reminder.title)
                        .putString(ReminderWorker.KEY_BODY, reminder.body)
                        .build()
                )
                .build()

            workManager.enqueueUniqueWork(kind.id, ExistingWorkPolicy.REPLACE, request)
        }
    }

    override fun cancelAll() {
        ReminderKind.entries.forEach { workManager.cancelUniqueWork(it.id) }
    }

    /** Kanal trzeba zalozyc zanim cokolwiek wyladuje w kolejce - inaczej powiadomienie nie wyjdzie. */
    private fun ensureChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val manager = appContext.getSystemService(NotificationManager::class.java) ?: return
        if (manager.getNotificationChannel(AndroidReminderChannel.ID) != null) return

        manager.createNotificationChannel(
            NotificationChannel(
                AndroidReminderChannel.ID,
                "Reminders",
                // DEFAULT, nie HIGH: to sa szturchniecia, a nie alarmy. Powiadomienie z gry,
                // ktore dzwoni i wibruje jak wiadomosc, wylatuje razem z cala kategoria.
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Idle rewards, free summons and daily goals"
            }
        )
    }
}
