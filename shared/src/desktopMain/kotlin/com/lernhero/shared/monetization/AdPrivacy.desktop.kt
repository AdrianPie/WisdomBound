package com.lernhero.shared.monetization

actual object AdPrivacy {
    actual val optionsRequired: Boolean = false

    actual fun showOptions() = Unit
}
