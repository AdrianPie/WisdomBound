package com.lernhero.shared.monetization

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState

/**
 * Na iOS paywall RevenueCat to UIKit-owy PaywallViewController - pokazuje go Swift
 * ([IosPaywallPresenter] w MonetizationBridge.swift) jako modal nad cala aplikacja. Po stronie
 * Compose nic sie nie rysuje: ten composable tylko go otwiera i zamyka razem ze swoim zyciem.
 */
actual val revenueCatPaywallAvailable: Boolean get() = IosMonetizationBridge.paywall != null

@Composable
actual fun RevenueCatPaywall(placementId: String, onDismiss: () -> Unit, fallback: @Composable () -> Unit) {
    val presenter = IosMonetizationBridge.paywall
    if (presenter == null) {
        fallback()
        return
    }
    val dismiss by rememberUpdatedState(onDismiss)
    DisposableEffect(placementId) {
        presenter.present(placementId) { dismiss() }
        // Ekran, ktory otworzyl paywall, zniknal (nawigacja) - modal nie moze zostac sam na wierzchu.
        onDispose { presenter.dismiss() }
    }
}
