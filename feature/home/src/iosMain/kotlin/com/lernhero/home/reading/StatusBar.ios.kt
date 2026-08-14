package com.lernhero.home.reading

import platform.Foundation.NSNotificationCenter

/**
 * UIApplication straci prawo do bezposredniego chowania paska statusu od iOS 13 (SwiftUI
 * przejelo kontrole nad UIWindow) - najprostszy niezawodny most to powiadomienie, na ktore
 * nasluchuje `ContentView.swift` i przelacza `.statusBarHidden(_:)`.
 */
actual fun setStatusBarHidden(hidden: Boolean) {
    NSNotificationCenter.defaultCenter.postNotificationName(
        aName = STATUS_BAR_NOTIFICATION,
        `object` = null,
        userInfo = mapOf<Any?, Any?>("hidden" to hidden)
    )
}

const val STATUS_BAR_NOTIFICATION = "LernHeroStatusBarHidden"
