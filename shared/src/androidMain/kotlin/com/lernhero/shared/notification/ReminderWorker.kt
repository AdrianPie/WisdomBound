package com.lernhero.shared.notification

import android.app.PendingIntent
import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.Worker
import androidx.work.WorkerParameters

/**
 * Pokazuje jedno przypomnienie. Cala tresc przychodzi w [inputData] - worker nie zaglada do stanu
 * gry, bo w chwili odpalenia (nawet kilkanascie godzin pozniej) nie ma do niego dostepu ani
 * powodu, zeby go budzic.
 */
internal class ReminderWorker(
    context: Context,
    params: WorkerParameters
) : Worker(context, params) {

    override fun doWork(): Result {
        val id = inputData.getString(KEY_ID) ?: return Result.success()
        val title = inputData.getString(KEY_TITLE) ?: return Result.success()
        val body = inputData.getString(KEY_BODY).orEmpty()

        val manager = NotificationManagerCompat.from(applicationContext)
        // Gracz mogl w miedzyczasie cofnac zgode - wtedy po prostu konczymy bez bledu.
        if (!manager.areNotificationsEnabled()) return Result.success()

        val notification = NotificationCompat.Builder(applicationContext, AndroidReminderChannel.ID)
            // Ikone bierzemy z manifestu aplikacji: modul biblioteki nie widzi R apki, a nie ma
            // sensu duplikowac tu drugiej ikonki tylko po to, zeby miec do niej staly identyfikator.
            .setSmallIcon(applicationContext.applicationInfo.icon)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(launchAppIntent())
            .build()

        return try {
            // Id z nazwy rodzaju: dwa przypomnienia tego samego typu nadpisuja sie zamiast
            // ukladac w stos.
            manager.notify(id.hashCode(), notification)
            Result.success()
        } catch (_: SecurityException) {
            // Brak POST_NOTIFICATIONS mimo sprawdzenia wyzej (gracz cofnal zgode w miedzyczasie).
            // To nie jest awaria zadania - nie ma czego ponawiac.
            Result.success()
        }
    }

    /** Tapniecie w powiadomienie otwiera gre tam, gdzie gracz ja zostawil. */
    private fun launchAppIntent(): PendingIntent? {
        val intent = applicationContext.packageManager
            .getLaunchIntentForPackage(applicationContext.packageName) ?: return null
        return PendingIntent.getActivity(
            applicationContext,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    companion object {
        const val KEY_ID = "reminder_id"
        const val KEY_TITLE = "reminder_title"
        const val KEY_BODY = "reminder_body"
    }
}

/** Jedno miejsce na id kanalu - dziela je harmonogram (zaklada) i worker (publikuje). */
internal object AndroidReminderChannel {
    const val ID = "wisdombound_reminders"
}
