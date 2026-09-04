package com.lernhero.auth

actual fun supportsAppleSignIn(): Boolean = true

actual fun isDebugBuild(): Boolean = kotlin.native.Platform.isDebugBinary
