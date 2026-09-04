package com.lernhero.shared.i18n

/**
 * UI language. English is the default and the language the game ships with (Google Play launch
 * is US-first) - Polish is the original dev language, kept as a full alternative, not an
 * afterthought.
 *
 * This is a DEVICE preference, not account data: it lives in [AppLanguageStore] (local key-value
 * storage), not in [com.lernhero.domain.player.Player]. It has to be readable before the player
 * is loaded (splash screen, Auth) and shouldn't force a Firestore round trip just to pick a
 * language on first launch.
 */
enum class AppLanguage(val code: String, val nativeName: String) {
    English("en", "English"),
    Polish("pl", "Polski");

    companion object {
        val Default = English

        fun fromCode(code: String?): AppLanguage = entries.firstOrNull { it.code == code } ?: Default
    }
}
