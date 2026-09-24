package com.lernhero.shared

import kotlin.experimental.ExperimentalNativeApi
import kotlin.native.Platform

/** Kotlin/Native wie, czy binarka zostala zbudowana w trybie debug - nie trzeba tego zgadywac. */
@OptIn(ExperimentalNativeApi::class)
actual val isDebugBuild: Boolean = Platform.isDebugBinary

@androidx.compose.runtime.Composable
actual fun appVersionName(): String =
    platform.Foundation.NSBundle.mainBundle.objectForInfoDictionaryKey("CFBundleShortVersionString") as? String ?: "?"
