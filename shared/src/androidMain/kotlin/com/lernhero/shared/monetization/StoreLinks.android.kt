package com.lernhero.shared.monetization

/** Google Play nie wymaga osobnego regulaminu przy subskrypcji - pusty = link ukryty. */
actual val termsOfUseUrl: String = ""

actual val manageSubscriptionsUrl: String = "https://play.google.com/store/account/subscriptions"

actual val storeDisplayName: String = "Google Play"
