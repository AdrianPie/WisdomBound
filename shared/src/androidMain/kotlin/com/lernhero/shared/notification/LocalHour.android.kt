package com.lernhero.shared.notification

import java.util.Calendar

actual fun nextLocalHourMillis(hour: Int, now: Long): Long? {
    val calendar = Calendar.getInstance().apply {
        timeInMillis = now
        set(Calendar.HOUR_OF_DAY, hour)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }
    // Godzina na dzis juz minela - celujemy w jutro. Bez tego wieczorne przypomnienie ustawione
    // o 21:00 mialoby moment w przeszlosci i po prostu by nie wyszlo.
    if (calendar.timeInMillis <= now) {
        calendar.add(Calendar.DAY_OF_YEAR, 1)
    }
    return calendar.timeInMillis
}
