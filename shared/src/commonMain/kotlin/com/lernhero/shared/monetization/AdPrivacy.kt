package com.lernhero.shared.monetization

/**
 * Ustawienia prywatnosci reklam (zgoda RODO z Google UMP). Google wymaga, zeby gracz z UE mogl
 * w kazdej chwili zmienic swoja zgode - stad przycisk w Ustawieniach, widoczny tylko tam,
 * gdzie ta zgoda w ogole obowiazuje.
 */
expect object AdPrivacy {
    val optionsRequired: Boolean

    fun showOptions()
}
