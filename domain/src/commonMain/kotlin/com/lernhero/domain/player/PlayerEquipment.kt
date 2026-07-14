package com.lernhero.domain.player

import kotlinx.serialization.Serializable

/**
 * Zalozony ekwipunek gracza: nazwa slotu -> id przedmiotu z katalogu.
 *
 * Wczesniej bylo tu piec flag ("head = true"), przez co w kazdym slocie miescil sie dokladnie
 * jeden przedmiot w calej grze. Teraz slot trzyma id, wiec katalog moze rosnac bez konca.
 *
 * Piec starych pol zostaje **wylacznie** po to, by dokumenty zapisane przed zmiana wciaz dawaly
 * sie wczytac z Firestore. Przerabia je na nowy format [com.lernhero.domain.item.migrated];
 * nowy zapis ich nie uzywa i po pierwszym zapisaniu gracza znikaja z dokumentu.
 */
@Serializable
data class PlayerEquipment(
    val equipped: Map<String, String> = emptyMap(),

    val head: Boolean = false,
    val hands: Boolean = false,
    val legs: Boolean = false,
    val feet: Boolean = false,
    val mainHand: Boolean = false
)
