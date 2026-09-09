package com.lernhero.shared.notification

import com.lernhero.domain.notification.GameReminder
import com.lernhero.domain.notification.ReminderKind
import platform.Foundation.NSDate
import platform.Foundation.timeIntervalSince1970
import platform.UserNotifications.UNMutableNotificationContent
import platform.UserNotifications.UNNotificationRequest
import platform.UserNotifications.UNTimeIntervalNotificationTrigger
import platform.UserNotifications.UNUserNotificationCenter

actual fun platformReminderScheduler(): ReminderScheduler = IosReminderScheduler()

/**
 * Przypomnienia na iOS przez [UNUserNotificationCenter].
 *
 * Ten sam kontrakt co na Androidzie: identyfikatorem zadania jest [ReminderKind.id], wiec ponowne
 * dodanie tego samego rodzaju NADPISUJE poprzednie zamiast dokladac kolejna kopie - tak dziala
 * `addNotificationRequest` dla powtorzonego identyfikatora.
 *
 * Wyzwalacz liczony w SEKUNDACH OD TERAZ ([UNTimeIntervalNotificationTrigger]), a nie z komponentow
 * daty - dokladnie tak samo, jak `setInitialDelay` w WorkManagerze po drugiej stronie. Jeden model
 * czasu dla obu platform znaczy, ze plan z domeny nie musi wiedziec, na czym akurat leci.
 */
private class IosReminderScheduler : ReminderScheduler {

    private val center get() = UNUserNotificationCenter.currentNotificationCenter()

    /**
     * iOS nie oddaje stanu zgody synchronicznie (`getNotificationSettings` jest asynchroniczne),
     * a ten kontrakt jest synchroniczny. Zwracamy `true` i pozwalamy systemowi odrzucic zadanie,
     * gdy zgody nie ma - efekt dla gracza jest ten sam, a nie udajemy wiedzy, ktorej nie mamy.
     */
    override val isPermitted: Boolean = true

    override fun schedule(reminders: List<GameReminder>) {
        val planned = reminders.associateBy { it.kind }

        // Rodzaje spoza planu kasujemy - plan jest jedynym zrodlem prawdy, tak samo jak na Androidzie.
        val stale = ReminderKind.entries.filter { it !in planned.keys }.map { it.id }
        if (stale.isNotEmpty()) {
            center.removePendingNotificationRequestsWithIdentifiers(stale)
        }

        val nowSeconds = NSDate().timeIntervalSince1970
        planned.values.forEach { reminder ->
            val delaySeconds = (reminder.triggerAtMs / 1000.0) - nowSeconds
            if (delaySeconds <= 0.0) {
                // Moment juz minal - nie pokazujemy powiadomienia o przeszlosci.
                center.removePendingNotificationRequestsWithIdentifiers(listOf(reminder.kind.id))
                return@forEach
            }

            val content = UNMutableNotificationContent().apply {
                setTitle(reminder.title)
                setBody(reminder.body)
            }

            center.addNotificationRequest(
                request = UNNotificationRequest.requestWithIdentifier(
                    identifier = reminder.kind.id,
                    content = content,
                    trigger = UNTimeIntervalNotificationTrigger.triggerWithTimeInterval(
                        timeInterval = delaySeconds,
                        repeats = false
                    )
                ),
                withCompletionHandler = null
            )
        }
    }

    override fun cancelAll() {
        center.removePendingNotificationRequestsWithIdentifiers(ReminderKind.entries.map { it.id })
    }
}
