package com.lernhero.shared.i18n

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.staticCompositionLocalOf

/**
 * Provided ONCE, at the app root (see `App()` in `composeApp`) - everything below reads through
 * [strings]. Kept as `staticCompositionLocalOf` (not derived per-screen) so the value always
 * reflects [AppLanguageStore.current] live, without every screen re-subscribing to the store.
 */
val LocalAppStrings: ProvidableCompositionLocal<AppStrings> = staticCompositionLocalOf { EnStrings }

/** Wraps [content] so every composable inside can call [strings] to read the active [AppStrings]. */
@Composable
fun ProvideAppStrings(store: AppLanguageStore, content: @Composable () -> Unit) {
    CompositionLocalProvider(LocalAppStrings provides stringsFor(store.current), content = content)
}

/** Shorthand for `LocalAppStrings.current` - read inside any `@Composable`. */
val strings: AppStrings
    @Composable get() = LocalAppStrings.current
