package com.lernhero.shared.monetization

import android.app.Application
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import com.lernhero.shared.notification.CurrentActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.coroutines.resume

/**
 * Reklama z nagroda przez AdMob. Jedna reklama czeka zaladowana w tle - gracz, ktory tapnie
 * "obejrzyj", nie ma patrzec na kilkusekundowe ladowanie. Po kazdym pokazie od razu laduje sie
 * nastepna.
 */
internal class AdMobRewardedAds(private val app: Application) : RewardedAds {

    private var ready: RewardedAd? = null
    private var loading = false

    override val isAvailable: Boolean = true

    init {
        AdConsent.whenReady(::preload)
    }

    override suspend fun show(): AdOutcome = withContext(Dispatchers.Main) {
        val activity = CurrentActivity.value ?: return@withContext AdOutcome.Unavailable
        if (!AdConsent.canRequestAds) return@withContext AdOutcome.Unavailable
        val ad = ready ?: withTimeoutOrNull(LOAD_TIMEOUT_MS) { load() } ?: run {
            preload()
            return@withContext AdOutcome.Unavailable
        }
        ready = null

        suspendCancellableCoroutine { cont ->
            var earned = false
            ad.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    preload()
                    if (cont.isActive) cont.resume(if (earned) AdOutcome.Rewarded else AdOutcome.Skipped)
                }

                override fun onAdFailedToShowFullScreenContent(error: AdError) {
                    preload()
                    if (cont.isActive) cont.resume(AdOutcome.Unavailable)
                }
            }
            ad.show(activity) { earned = true }
        }
    }

    private fun preload() {
        if (ready != null || loading || !AdConsent.canRequestAds) return
        loading = true
        RewardedAd.load(app, adUnit, AdRequest.Builder().build(), object : RewardedAdLoadCallback() {
            override fun onAdLoaded(ad: RewardedAd) {
                loading = false
                ready = ad
            }

            override fun onAdFailedToLoad(error: LoadAdError) {
                loading = false
            }
        })
    }

    private suspend fun load(): RewardedAd? = suspendCancellableCoroutine { cont ->
        RewardedAd.load(app, adUnit, AdRequest.Builder().build(), object : RewardedAdLoadCallback() {
            override fun onAdLoaded(ad: RewardedAd) {
                if (cont.isActive) cont.resume(ad)
            }

            override fun onAdFailedToLoad(error: LoadAdError) {
                if (cont.isActive) cont.resume(null)
            }
        })
    }

    private val adUnit get() = MonetizationConfig.REWARDED_AD_UNIT_ANDROID

    private companion object {
        const val LOAD_TIMEOUT_MS = 8_000L
    }
}
