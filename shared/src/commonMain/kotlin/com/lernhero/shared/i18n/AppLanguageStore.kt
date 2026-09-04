package com.lernhero.shared.i18n

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.russhwolf.settings.Settings
import com.russhwolf.settings.get
import com.russhwolf.settings.set

/**
 * Persists the chosen [AppLanguage] on the device (Android SharedPreferences / iOS NSUserDefaults
 * / JVM Preferences under the hood, via `multiplatform-settings` - no expect/actual needed here).
 *
 * One instance for the whole app (a Koin singleton, held next to [com.lernhero.domain.run.RunSession]
 * in spirit). [current] is Compose snapshot state - any composable reading it recomposes the moment
 * [set] runs, so switching language in Settings updates every screen on the back stack immediately,
 * with no restart and no navigation event needed.
 */
class AppLanguageStore(private val settings: Settings = Settings()) {

    var current: AppLanguage by mutableStateOf(AppLanguage.fromCode(settings[KEY, AppLanguage.Default.code]))
        private set

    fun set(language: AppLanguage) {
        current = language
        settings[KEY] = language.code
    }

    private companion object {
        const val KEY = "app_language"
    }
}
