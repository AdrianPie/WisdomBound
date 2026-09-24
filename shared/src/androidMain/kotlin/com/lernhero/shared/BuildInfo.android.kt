package com.lernhero.shared

import org.apie.shared.BuildConfig

/**
 * Typ buildu tego modulu. AGP buduje `:shared` w tym samym wariancie, co konsumujaca go apke,
 * wiec debug apki = debug tutaj, a release (to, co idzie na Google Play) = false.
 */
actual val isDebugBuild: Boolean = BuildConfig.DEBUG

@androidx.compose.runtime.Composable
actual fun appVersionName(): String {
    val context = androidx.compose.ui.platform.LocalContext.current
    return androidx.compose.runtime.remember(context) {
        runCatching { context.packageManager.getPackageInfo(context.packageName, 0).versionName }
            .getOrNull() ?: "?"
    }
}
