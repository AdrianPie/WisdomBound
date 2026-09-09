package com.lernhero.shared.notification

import platform.Foundation.NSCalendar
import platform.Foundation.NSCalendarUnitDay
import platform.Foundation.NSCalendarUnitMonth
import platform.Foundation.NSCalendarUnitYear
import platform.Foundation.NSDate
import platform.Foundation.dateByAddingTimeInterval
import platform.Foundation.dateWithTimeIntervalSince1970
import platform.Foundation.timeIntervalSince1970

private const val SECONDS_PER_DAY = 24.0 * 60 * 60

/**
 * Skladamy date z komponentow (rok/miesiac/dzien z dzisiaj + zadana godzina), zamiast uzywac
 * `nextDateAfterDate` z opcjami dopasowania - ta druga droga zalezy od stalych, ktore w bindingach
 * Kotlin/Native nazywaja sie inaczej niz w dokumentacji Apple. Tu wystarczaja symbole, ktore sa
 * jednoznaczne.
 */
actual fun nextLocalHourMillis(hour: Int, now: Long): Long? {
    val calendar = NSCalendar.currentCalendar
    val nowDate = NSDate.dateWithTimeIntervalSince1970(now / 1000.0)

    val components = calendar.components(
        unitFlags = NSCalendarUnitYear or NSCalendarUnitMonth or NSCalendarUnitDay,
        fromDate = nowDate
    )
    components.setHour(hour.toLong())
    components.setMinute(0)
    components.setSecond(0)

    val today = calendar.dateFromComponents(components) ?: return null
    // Godzina na dzis juz minela - celujemy w jutro. Bez tego wieczorne przypomnienie ustawione
    // po tej godzinie mialoby moment w przeszlosci i po prostu by nie wyszlo.
    val target = if (today.timeIntervalSince1970 <= nowDate.timeIntervalSince1970) {
        today.dateByAddingTimeInterval(SECONDS_PER_DAY)
    } else {
        today
    }

    return (target.timeIntervalSince1970 * 1000.0).toLong()
}
