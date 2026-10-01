package com.lernhero.shared.monetization

import androidx.compose.runtime.Composable

/** Desktop nie ma sklepu - zawsze wlasny paywall (zreszta i tak bez planow do kupienia). */
actual val revenueCatPaywallAvailable: Boolean = false

@Composable
actual fun RevenueCatPaywall(placementId: String, onDismiss: () -> Unit, fallback: @Composable () -> Unit) {
    fallback()
}
