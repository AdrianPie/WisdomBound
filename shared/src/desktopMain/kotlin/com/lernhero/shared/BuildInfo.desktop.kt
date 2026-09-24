package com.lernhero.shared

/** Desktop jest platforma wylacznie deweloperska - nie ma buildu sklepowego, ktory trzeba chronic. */
actual val isDebugBuild: Boolean = true

@androidx.compose.runtime.Composable
actual fun appVersionName(): String = "desktop"
