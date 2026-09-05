package com.lernhero.auth

import kotlin.experimental.ExperimentalNativeApi

actual fun supportsAppleSignIn(): Boolean = true

/**
 * [kotlin.native.Platform] jest oznaczone jako eksperymentalne API Native - bez jawnego opt-in
 * caly modul :feature:auth nie kompiluje sie na iOS (a wiec i cala apka na iOS).
 */
@OptIn(ExperimentalNativeApi::class)
actual fun isDebugBuild(): Boolean = kotlin.native.Platform.isDebugBinary
