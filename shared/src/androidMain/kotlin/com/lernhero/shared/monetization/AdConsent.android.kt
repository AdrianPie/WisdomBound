package com.lernhero.shared.monetization

import android.app.Activity
import com.google.android.gms.ads.MobileAds
import com.google.android.ump.ConsentInformation
import com.google.android.ump.ConsentRequestParameters
import com.google.android.ump.UserMessagingPlatform
import com.lernhero.shared.notification.CurrentActivity
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Zgoda RODO na reklamy przez Google UMP. Formularz pokazuje sie tylko tam, gdzie prawo go wymaga
 * (UE, UK, Szwajcaria) - reszta swiata przechodzi dalej bez niczego.
 *
 * SDK reklam startuje dopiero, gdy wolno prosic o reklamy. Inaczej pierwsze zadania szlyby
 * przed zgoda, a to jest dokladnie to, czego RODO zabrania.
 */
internal object AdConsent {

    private val started = AtomicBoolean(false)
    private val listeners = mutableListOf<() -> Unit>()

    @Volatile
    var canRequestAds: Boolean = false
        private set

    var info: ConsentInformation? = null
        private set

    fun gather(activity: Activity) {
        val consent = UserMessagingPlatform.getConsentInformation(activity).also { info = it }
        // Zgoda z poprzedniej sesji - reklamy moga ruszyc od razu, zanim wroci odpowiedz z sieci.
        if (consent.canRequestAds()) startAds(activity)
        consent.requestConsentInfoUpdate(
            activity,
            ConsentRequestParameters.Builder().build(),
            {
                UserMessagingPlatform.loadAndShowConsentFormIfRequired(activity) {
                    if (consent.canRequestAds()) startAds(activity)
                }
            },
            { if (consent.canRequestAds()) startAds(activity) }
        )
    }

    fun whenReady(action: () -> Unit) {
        synchronized(listeners) {
            if (canRequestAds) action() else listeners += action
        }
    }

    private fun startAds(activity: Activity) {
        if (!started.compareAndSet(false, true)) return
        MobileAds.initialize(activity.applicationContext) {}
        val pending = synchronized(listeners) {
            canRequestAds = true
            listeners.toList().also { listeners.clear() }
        }
        activity.runOnUiThread { pending.forEach { it() } }
    }
}

/** Wolane z MainActivity.onCreate - zbiera zgode (albo potwierdza zapisana) przy kazdym starcie. */
fun Activity.gatherAdConsent() = AdConsent.gather(this)

actual object AdPrivacy {
    actual val optionsRequired: Boolean
        get() = AdConsent.info?.privacyOptionsRequirementStatus ==
            ConsentInformation.PrivacyOptionsRequirementStatus.REQUIRED

    actual fun showOptions() {
        val activity = CurrentActivity.value ?: return
        UserMessagingPlatform.showPrivacyOptionsForm(activity) {}
    }
}
