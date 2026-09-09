package com.lernhero.shared.notification

import com.lernhero.domain.notification.GameReminder
import com.lernhero.domain.notification.ReminderKind

/**
 * Systemowy harmonogram powiadomien. Domena mowi CO i KIEDY ([GameReminder]), ta warstwa wie JAK -
 * kanaly, uprawnienia, WorkManager na Androidzie, UNUserNotificationCenter na iOS.
 *
 * Kontrakt jest celowo "nadpisujacy": [schedule] dostaje CALY plan naraz i zastepuje nim to, co
 * stalo w kolejce. Dzieki temu przeliczenie planu przy kazdym wejsciu do gry jest bezpieczne -
 * nie da sie nazbierac dziesieciu kopii tego samego przypomnienia, bo kazde ma staly klucz
 * ([ReminderKind.id]).
 *
 * Zadna implementacja NIE prosi sama o uprawnienie - o to pyta ekran, w momencie, w ktorym gracz
 * rozumie po co (patrz Ustawienia). Brak zgody nie jest bledem: harmonogram po prostu nic nie robi.
 */
interface ReminderScheduler {

    /** Czy powiadomienia sa w ogole mozliwe (kanal istnieje, gracz nie odmowil na poziomie systemu). */
    val isPermitted: Boolean

    /**
     * Zastepuje cala kolejke podanym planem. Przypomnienia z przeszlosci i rodzaje nieobecne
     * w [reminders] sa kasowane - plan jest jedynym zrodlem prawdy.
     */
    fun schedule(reminders: List<GameReminder>)

    /** Kasuje wszystko - wolane, gdy gracz wylaczy powiadomienia w Ustawieniach. */
    fun cancelAll()

    /**
     * Nic nie robiaca implementacja - desktop i kazda platforma, ktora nie ma (jeszcze) swojej.
     * Nie jest [ReminderScheduler] "na chwile": brak powiadomien na desktopie to decyzja, nie luka.
     */
    object None : ReminderScheduler {
        override val isPermitted: Boolean = false
        override fun schedule(reminders: List<GameReminder>) = Unit
        override fun cancelAll() = Unit
    }
}

/** Platformowa implementacja wstrzykiwana w Koinie - patrz ReminderScheduler.<platforma>.kt. */
expect fun platformReminderScheduler(): ReminderScheduler

/**
 * Najblizszy moment, w ktorym lokalny zegar pokaze [hour]:00 - albo null, gdy platforma nie ma
 * przypomnien (desktop).
 *
 * Zegar LOKALNY, wiec potrzebny jest kalendarz i strefa czasowa - dokladnie te rzeczy, ktorych
 * [com.lernhero.domain.notification.ReminderPlan] celowo nie zna. Domena dostaje gotowa liczbe
 * i zostaje bez kalendarza, tak samo jak zostaje bez Compose.
 */
expect fun nextLocalHourMillis(hour: Int, now: Long): Long?

/**
 * Prosi gracza o zgode na powiadomienia. Wolane WYLACZNIE z Ustawien, po tapnieciu w przelacznik -
 * nigdy przy starcie gry.
 *
 * Systemowe okienko z prosba o zgode pokazuje sie raz w zyciu instalacji; jesli gracz juz odmowil,
 * implementacja otwiera ustawienia aplikacji, zeby dalo sie to cofnac. Nie zwraca wyniku, bo
 * odpowiedz przychodzi asynchronicznie i tak czy tak trzeba ja odczytac przez
 * [ReminderScheduler.isPermitted] przy nastepnym zlozeniu ekranu.
 */
expect fun requestNotificationAccess()
