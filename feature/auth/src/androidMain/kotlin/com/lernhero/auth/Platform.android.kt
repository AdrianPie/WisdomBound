package com.lernhero.auth

actual fun supportsAppleSignIn(): Boolean = false

actual fun isDebugBuild(): Boolean = org.apie.auth.BuildConfig.DEBUG
