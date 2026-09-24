package com.lernhero.shared.monetization

/**
 * Klucze sklepow i reklam. Wszystkie sa PUBLICZNE (klucze SDK RevenueCat i identyfikatory AdMob
 * trafiaja do kazdej zbudowanej apki), wiec moga stac w repo - tajny klucz RevenueCat zyje
 * wylacznie na serwerze (REVENUECAT_SECRET_KEY), bo to on rozstrzyga o limicie AI.
 *
 * Przed wydaniem trzeba podmienic:
 *  - [REVENUECAT_ANDROID_KEY] / [REVENUECAT_IOS_KEY] - Project settings > API keys w RevenueCat.
 *    Pusty klucz = subskrypcja niedostepna (paywall pokazuje "sklep niedostepny"), apka dziala dalej.
 *  - [REWARDED_AD_UNIT_ANDROID] / [REWARDED_AD_UNIT_IOS] - wlasne jednostki "Rewarded" z AdMob.
 *    Domyslnie stoja TESTOWE jednostki Google - pokazuja prawdziwa reklame testowa, ale nie placa.
 *  - identyfikator aplikacji AdMob w AndroidManifest.xml i Info.plist (GADApplicationIdentifier).
 */
object MonetizationConfig {
    const val REVENUECAT_ANDROID_KEY: String = "goog_ErTlUcrIkcTGLANcFCVjWOSlqxU"
    const val REVENUECAT_IOS_KEY: String = ""

    /** Identyfikator uprawnienia w RevenueCat, ktore odblokowuje premium. */
    const val PREMIUM_ENTITLEMENT: String = "premium"

    const val REWARDED_AD_UNIT_ANDROID: String = "ca-app-pub-5808000840962125/7436387831"
    const val REWARDED_AD_UNIT_IOS: String = "ca-app-pub-3940256099942544/1712485313"
}
