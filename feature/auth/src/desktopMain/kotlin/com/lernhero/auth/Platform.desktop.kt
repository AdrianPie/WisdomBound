package com.lernhero.auth

actual fun supportsAppleSignIn(): Boolean = false

/** Desktop sluzy w tym projekcie wylacznie do testow, wiec zawsze liczy sie jako build dev. */
actual fun isDebugBuild(): Boolean = true
