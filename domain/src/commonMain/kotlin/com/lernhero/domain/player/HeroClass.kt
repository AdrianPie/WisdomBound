package com.lernhero.domain.player

import kotlinx.serialization.Serializable

/**
 * Postac wybrana przy tworzeniu konta. Many tu nie ma - walka jej nie uzywa, a statystyki
 * bojowe (obrona, krytyk, szybkosc) biora sie z ekwipunku i efektow.
 */
@Serializable
data class HeroClass(
    val id: String = "",
    val isPlayer: Boolean = false,
    val avatar: String = "",
    val armor: String = "",
    val hp: Int = 0,
    val maxHp: Int = 0,
    val attack: Int = 0
)
