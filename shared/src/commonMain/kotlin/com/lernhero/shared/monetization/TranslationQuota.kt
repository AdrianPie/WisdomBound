package com.lernhero.shared.monetization

import com.lernhero.domain.monetization.DailyCounter
import com.lernhero.domain.monetization.PremiumLimits
import com.russhwolf.settings.Settings
import com.russhwolf.settings.get
import com.russhwolf.settings.set

/**
 * Dzienny limit tlumaczen w czytniku bez subskrypcji. Liczony NA URZADZENIU, bo samo tlumaczenie
 * tez dzieje sie na urzadzeniu (ML Kit / Apple Translation) i nic nas nie kosztuje - nie ma tu
 * czego bronic serwerem, wystarczy uczciwy licznik.
 *
 * Liczy sie tylko UDANE tlumaczenie: brak modelu jezykowego albo blad nie zjada proby.
 */
class TranslationQuota(
    private val subscription: SubscriptionService,
    private val settings: Settings = Settings()
) {
    private var counter: DailyCounter = DailyCounter(
        dateKey = settings[KEY_DAY, ""],
        count = settings[KEY_COUNT, 0]
    )

    val isUnlimited: Boolean get() = subscription.isPremium.value

    fun remaining(dateKey: String): Int = counter.remainingOn(dateKey, PremiumLimits.FREE_TRANSLATIONS_PER_DAY)

    fun canTranslate(dateKey: String): Boolean = isUnlimited || remaining(dateKey) > 0

    fun consume(dateKey: String) {
        if (isUnlimited) return
        counter = counter.increment(dateKey)
        settings[KEY_DAY] = counter.dateKey
        settings[KEY_COUNT] = counter.count
    }

    private companion object {
        const val KEY_DAY = "translation_quota_day"
        const val KEY_COUNT = "translation_quota_count"
    }
}
