package com.lernhero.shared.notification

/**
 * Desktop nie dostaje przypomnien - tak samo jak nie ma czytnika PDF ani fitnessu. To platforma
 * deweloperska, na ktorej gra sie uruchamia zeby cos sprawdzic, a nie taka, na ktora ktos czeka
 * z powiadomieniem.
 */
actual fun platformReminderScheduler(): ReminderScheduler = ReminderScheduler.None

/** Bez przypomnien nie ma czego planowac - patrz [platformReminderScheduler]. */
actual fun nextLocalHourMillis(hour: Int, now: Long): Long? = null

actual fun requestNotificationAccess() = Unit
