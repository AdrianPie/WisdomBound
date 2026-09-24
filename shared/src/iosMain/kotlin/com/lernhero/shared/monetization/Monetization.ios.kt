package com.lernhero.shared.monetization

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

/**
 * RevenueCat i Google Mobile Ads na iOS to biblioteki Swift z SPM - Kotlin/Native nie wola ich
 * wprost. Most idzie w odwrotna strone, tak jak przy tlumaczeniu w czytniku: `iosApp`
 * (MonetizationBridge.swift) implementuje te dwa interfejsy i podpina je tutaj przy starcie,
 * PRZED Koinem. Bez podpietych providerow gra dziala jak bez sklepu i reklam.
 */
object IosMonetizationBridge {
    var ads: IosAdsProvider? = null
    var purchases: IosPurchasesProvider? = null

    /** Wolane ze Swifta przy kazdej zmianie CustomerInfo (zakup, odnowienie, wygasniecie). */
    fun setPremium(active: Boolean) {
        premium.value = active
    }

    internal val premium = MutableStateFlow(false)
}

interface IosAdsProvider {
    /** [completion] dostaje "rewarded", "skipped" albo "unavailable". */
    fun showRewarded(adUnitId: String, completion: (String) -> Unit)
}

interface IosPurchasesProvider {
    fun logIn(userId: String, completion: () -> Unit)
    fun logOut(completion: () -> Unit)
    fun loadOffers(completion: (List<SubscriptionOffer>) -> Unit)
    /** [completion] dostaje "success", "cancelled" albo tresc bledu. */
    fun purchase(offerId: String, completion: (String) -> Unit)
    fun restore(completion: () -> Unit)
}

actual fun platformSubscriptionService(): SubscriptionService =
    IosMonetizationBridge.purchases?.let(::IosSubscriptionService) ?: SubscriptionService.None

actual fun platformRewardedAds(): RewardedAds =
    IosMonetizationBridge.ads?.let(::IosRewardedAds) ?: RewardedAds.None

private class IosSubscriptionService(private val provider: IosPurchasesProvider) : SubscriptionService {
    override val isAvailable: Boolean = true
    override val isPremium: StateFlow<Boolean> = IosMonetizationBridge.premium.asStateFlow()

    override suspend fun identify(userId: String?) = suspendCancellableCoroutine { cont ->
        val done = { if (cont.isActive) cont.resume(Unit) }
        if (userId == null) provider.logOut(done) else provider.logIn(userId, done)
    }

    override suspend fun offers(): List<SubscriptionOffer> = suspendCancellableCoroutine { cont ->
        provider.loadOffers { if (cont.isActive) cont.resume(it) }
    }

    override suspend fun purchase(offerId: String): PurchaseOutcome = suspendCancellableCoroutine { cont ->
        provider.purchase(offerId) { result ->
            if (!cont.isActive) return@purchase
            cont.resume(
                when (result) {
                    "success" -> PurchaseOutcome.Success
                    "cancelled" -> PurchaseOutcome.Cancelled
                    else -> PurchaseOutcome.Failed(result)
                }
            )
        }
    }

    override suspend fun restore(): Boolean {
        suspendCancellableCoroutine { cont -> provider.restore { if (cont.isActive) cont.resume(Unit) } }
        return isPremium.value
    }
}

private class IosRewardedAds(private val provider: IosAdsProvider) : RewardedAds {
    override val isAvailable: Boolean = true

    override suspend fun show(): AdOutcome = suspendCancellableCoroutine { cont ->
        provider.showRewarded(MonetizationConfig.REWARDED_AD_UNIT_IOS) { result ->
            if (!cont.isActive) return@showRewarded
            cont.resume(
                when (result) {
                    "rewarded" -> AdOutcome.Rewarded
                    "skipped" -> AdOutcome.Skipped
                    else -> AdOutcome.Unavailable
                }
            )
        }
    }
}

/** Jednorazowe zakupy na iOS jeszcze nie sa podpiete do mostu Swift. */
actual fun platformOneTimePurchases(): OneTimePurchases = OneTimePurchases.None
