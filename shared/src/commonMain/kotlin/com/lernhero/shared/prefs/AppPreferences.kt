package com.lernhero.shared.prefs

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.lernhero.domain.notification.ReminderKind
import com.lernhero.domain.notification.ReminderPlan
import com.russhwolf.settings.Settings
import com.russhwolf.settings.get
import com.russhwolf.settings.set

/**
 * Preferencje URZADZENIA - wszystko, co gracz przestawia w Ustawieniach, a co NIE jest danymi
 * konta. Zapis idzie w to samo miejsce, co jezyk ([com.lernhero.shared.i18n.AppLanguageStore]):
 * SharedPreferences / NSUserDefaults / Preferences, przez `multiplatform-settings`.
 *
 * Dwie zasady, ktore trzymaja ten ekran uczciwym:
 *
 * 1. Kazde pole tutaj MA swojego czytelnika w kodzie gry. Przelacznik, ktorego nikt nie czyta,
 *    jest gorszy niz jego brak - uczy gracza, ze ustawienia nic nie robia.
 * 2. Kazde pole jest stanem Compose, wiec ekran ustawien nie trzyma wlasnej kopii stanu i nie
 *    da sie go rozjechac z tym, co naprawde zapisane.
 *
 * Czytelnicy (stan na dzis):
 * - [remindersEnabled] / [isReminderEnabled] / [dailyGoalHour] -> PlayerViewModel.rescheduleReminders
 * - [battleSpeed] -> BattleViewModel (tempo, od ktorego startuje kazda walka)
 * - [soundEnabled] -> Sounds (efekty dzwiekowe, podpinane w App)
 */
class AppPreferences(private val settings: Settings = Settings()) {

    /** Glowny wylacznik przypomnien. Wylaczony = kolejka systemowa jest kasowana w calosci. */
    var remindersEnabled: Boolean by mutableStateOf(settings[KEY_REMINDERS, true])
        private set

    /** Godzina (czasu lokalnego) wieczornego przypomnienia o celu dziennym. */
    var dailyGoalHour: Int by mutableStateOf(
        settings[KEY_DAILY_HOUR, ReminderPlan.DAILY_GOAL_HOUR].coerceIn(MIN_HOUR, MAX_HOUR)
    )
        private set

    /** Efekty dzwiekowe gry - czyta je [com.lernhero.shared.audio.Sounds]. */
    var soundEnabled: Boolean by mutableStateOf(settings[KEY_SOUND, true])
        private set

    fun chooseSound(enabled: Boolean) {
        soundEnabled = enabled
        settings[KEY_SOUND] = enabled
    }

    /** Tempo, od ktorego startuje kazda walka. W HUD-zie dalej da sie je zmienic na chwile. */
    var battleSpeed: Float by mutableStateOf(settings[KEY_BATTLE_SPEED, DEFAULT_BATTLE_SPEED])
        private set

    /**
     * Wylaczone rodzaje przypomnien. Trzymamy ODMOWY, a nie zgody, bo dzieki temu rodzaj dopisany
     * do [ReminderKind] w przyszlosci jest domyslnie WLACZONY - nowe przypomnienie nie musi
     * pamietac o migracji zapisu.
     */
    private var mutedReminders: Set<String> by mutableStateOf(
        settings[KEY_MUTED_REMINDERS, ""].split(SEPARATOR).filter { it.isNotBlank() }.toSet()
    )

    /** Czy TEN rodzaj naprawde pojdzie do kolejki - czyli i glowny wylacznik, i wlasny. */
    fun isReminderEnabled(kind: ReminderKind): Boolean =
        remindersEnabled && isReminderAllowed(kind)

    /**
     * Wlasny przelacznik rodzaju, NIEZALEZNIE od glownego wylacznika. Ekran ustawien czyta to,
     * a nie [isReminderEnabled]: gdy gracz zgasi wszystkie przypomnienia, poszczegolne wiersze
     * maja przygasnac, a nie udawac, ze kazdy z osobna zostal wylaczony - inaczej po ponownym
     * wlaczeniu wszystkiego jego wybory bylyby stracone.
     */
    fun isReminderAllowed(kind: ReminderKind): Boolean = kind.id !in mutedReminders

    fun setAllReminders(enabled: Boolean) {
        remindersEnabled = enabled
        settings[KEY_REMINDERS] = enabled
    }

    fun setReminderEnabled(kind: ReminderKind, enabled: Boolean) {
        mutedReminders = if (enabled) mutedReminders - kind.id else mutedReminders + kind.id
        settings[KEY_MUTED_REMINDERS] = mutedReminders.joinToString(SEPARATOR.toString())
    }

    fun chooseDailyGoalHour(hour: Int) {
        dailyGoalHour = hour.coerceIn(MIN_HOUR, MAX_HOUR)
        settings[KEY_DAILY_HOUR] = dailyGoalHour
    }

    /**
     * Czy przy wejsciu do gry pokazac krotki samouczek. Ustawiany TYLKO przy stworzeniu postaci
     * (i przyciskiem "Jak grac" w Ustawieniach) - nie z poziomu gracza, bo ten wczytuje sie z bazy
     * z opoznieniem i doswiadczony gracz mignalby przez chwile samouczkiem. Obecni gracze po
     * aktualizacji go wiec nie zobacza, a nowi - dokladnie raz.
     */
    var introPending: Boolean by mutableStateOf(settings[KEY_INTRO_PENDING, false])
        private set

    fun requestIntro() {
        introPending = true
        settings[KEY_INTRO_PENDING] = true
    }

    fun finishIntro() {
        introPending = false
        settings[KEY_INTRO_PENDING] = false
    }

    fun chooseBattleSpeed(multiplier: Float) {
        battleSpeed = multiplier
        settings[KEY_BATTLE_SPEED] = multiplier
    }

    /**
     * Wraca do stanu, w ktorym apka byla po instalacji - ale TYLKO w zakresie tego pliku.
     * Postep gracza siedzi na koncie i nie ma tu czego skasowac; jezyk jest osobnym zapisem
     * i celowo zostaje, bo gracz, ktory nie czyta po angielsku, nie odzyskalby tego ekranu.
     */
    fun resetToDefaults() {
        setAllReminders(true)
        mutedReminders = emptySet()
        settings[KEY_MUTED_REMINDERS] = ""
        chooseDailyGoalHour(ReminderPlan.DAILY_GOAL_HOUR)
        chooseBattleSpeed(DEFAULT_BATTLE_SPEED)
        chooseSound(true)
    }

    companion object {
        const val DEFAULT_BATTLE_SPEED = 1f

        /** Godziny sensowne dla wieczornego szturchniecia - poza tym oknem to juz budzik. */
        const val MIN_HOUR = 16
        const val MAX_HOUR = 22

        private const val KEY_REMINDERS = "prefs_reminders_enabled"
        private const val KEY_MUTED_REMINDERS = "prefs_reminders_muted"
        private const val KEY_DAILY_HOUR = "prefs_daily_goal_hour"
        private const val KEY_BATTLE_SPEED = "prefs_battle_speed"
        private const val KEY_SOUND = "prefs_sound_enabled"
        private const val KEY_INTRO_PENDING = "prefs_intro_pending"
        private const val SEPARATOR = ','
    }
}
