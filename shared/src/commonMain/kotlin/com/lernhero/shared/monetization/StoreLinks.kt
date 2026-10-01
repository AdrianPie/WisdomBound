package com.lernhero.shared.monetization

/**
 * Linki, ktore paywall MUSI miec pod przyciskiem zakupu - wymagaja ich App Store (wytyczna 3.1.2:
 * Terms of Use i Privacy Policy przy kazdej subskrypcji) i dobre praktyki RevenueCat.
 *
 * Pusty adres = link sie nie pokazuje (lepiej brak niz martwy link), ale przed wysylka do review
 * [PRIVACY_POLICY_URL] musi byc uzupelniony - bez niego Apple odrzuci wersje z subskrypcja.
 */
object LegalLinks {
    /** Polityka prywatnosci aplikacji - ten sam adres co w App Store Connect i Google Play Console. */
    const val PRIVACY_POLICY_URL: String = ""
}

/** Regulamin (Terms of Use / EULA). Na iOS standardowa licencja Apple, na Androidzie - wlasny, jesli jest. */
expect val termsOfUseUrl: String

/** Gdzie gracz zarzadza subskrypcja (anuluje, zmienia plan) - ekran sklepu, nie nasz. */
expect val manageSubscriptionsUrl: String

/** Nazwa sklepu do zdania o odnawianiu ("Cancel anytime in the App Store"). */
expect val storeDisplayName: String
