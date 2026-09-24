package com.lernhero.shared.monetization

/** Reklamy z nagroda (AdMob). Pelnoekranowe, zawsze na zyczenie gracza - nigdy same z siebie. */
interface RewardedAds {
    val isAvailable: Boolean

    suspend fun show(): AdOutcome

    object None : RewardedAds {
        override val isAvailable: Boolean = false
        override suspend fun show(): AdOutcome = AdOutcome.Unavailable
    }
}

enum class AdOutcome {
    /** Obejrzana do konca - nagroda sie nalezy. */
    Rewarded,
    /** Zamknieta przed koncem. */
    Skipped,
    /** Nie bylo czego pokazac (brak sieci, brak wypelnienia, brak SDK). */
    Unavailable
}

expect fun platformRewardedAds(): RewardedAds

/**
 * Jedyna droga do nagrody "za reklame": subskrybent dostaje ja od razu, bez reklamy - to jest
 * jedna z korzysci premium. Kazdy przycisk "obejrzyj reklame" w grze idzie przez to.
 */
class RewardedAction(
    private val ads: RewardedAds,
    private val subscription: SubscriptionService
) {
    val isPremium: Boolean get() = subscription.isPremium.value

    /** Czy przycisk ma sens - premium albo dzialajace reklamy. */
    val canOffer: Boolean get() = isPremium || ads.isAvailable

    suspend fun earn(): AdOutcome = if (isPremium) AdOutcome.Rewarded else ads.show()
}
