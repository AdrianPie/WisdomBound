package com.lernhero.shared.monetization

import android.app.Application
import com.revenuecat.purchases.Purchases
import com.revenuecat.purchases.PurchasesConfiguration

/**
 * Wolane raz z MyApp.onCreate - konfiguruje RevenueCat i startuje SDK reklam. Musi stac przed
 * Koinem, bo to z [MonetizationContext] budowane sa serwisy wstrzykiwane do grafu.
 */
fun Application.initMonetization() {
    MonetizationContext.application = this
    if (MonetizationConfig.REVENUECAT_ANDROID_KEY.isNotBlank() && !Purchases.isConfigured) {
        Purchases.configure(PurchasesConfiguration.Builder(this, MonetizationConfig.REVENUECAT_ANDROID_KEY).build())
    }
    // SDK reklam startuje dopiero po zgodzie RODO - patrz [AdConsent].
}

internal object MonetizationContext {
    @Volatile
    var application: Application? = null
}

actual fun platformSubscriptionService(): SubscriptionService =
    if (Purchases.isConfigured) RevenueCatSubscriptionService() else SubscriptionService.None

actual fun platformRewardedAds(): RewardedAds =
    MonetizationContext.application?.let(::AdMobRewardedAds) ?: RewardedAds.None

actual fun platformOneTimePurchases(): OneTimePurchases =
    if (Purchases.isConfigured) RevenueCatOneTimePurchases() else OneTimePurchases.None
