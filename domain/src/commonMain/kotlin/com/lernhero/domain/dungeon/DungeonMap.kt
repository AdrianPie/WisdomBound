package com.lernhero.domain.dungeon

import kotlinx.serialization.Serializable

/**
 * Rodzaje pol na mapie lochu. Kazdy typ dostaje wlasna grafike w warstwie UI
 * (patrz DungeonNodeArt w feature:home) - tu trzymamy tylko czysta logike.
 */
enum class DungeonNodeType {
    Enemy,
    Elite,
    Event,
    Shop,
    Rest,
    Treasure,
    Boss
}

/**
 * Pojedyncze pole mapy. [next] to id pol na pietrze [floor] + 1, do ktorych mozna z niego przejsc.
 */
@Serializable
data class DungeonNode(
    val id: String,
    val floor: Int,
    val column: Int,
    val type: DungeonNodeType,
    val next: List<String>
)

/**
 * Cala mapa jednego runu. Trzyma [seed], bo to on jest zrodlem prawdy: z tego samego seeda
 * generator zawsze zbuduje identyczna mape. Dzieki temu w trybie MMO wystarczy przeslac
 * graczom jedna liczbe zamiast calego grafu, i kazdy klient odtworzy te sama mape lokalnie.
 */
@Serializable
data class DungeonMap(
    val seed: Long,
    val floors: Int,
    val columns: Int,
    val nodes: List<DungeonNode>
) {
    private val index: Map<String, DungeonNode> = nodes.associateBy { it.id }

    fun node(id: String): DungeonNode? = index[id]

    fun nodesOnFloor(floor: Int): List<DungeonNode> =
        nodes.filter { it.floor == floor }.sortedBy { it.column }

    /** Pola startowe - gracz wybiera jedno z nich na poczatku runu. */
    val entryNodes: List<DungeonNode> get() = nodesOnFloor(0)

    val boss: DungeonNode? get() = nodes.firstOrNull { it.type == DungeonNodeType.Boss }

    /** Pola dostepne z [nodeId]; gdy null - dopiero wchodzimy do lochu, wiec pietro startowe. */
    fun reachableFrom(nodeId: String?): List<DungeonNode> =
        if (nodeId == null) entryNodes
        else node(nodeId)?.next?.mapNotNull { index[it] }.orEmpty()
}

/**
 * Parametry generatora. Zmiana tych liczb to najtansza dzwignia na balans lochu -
 * dluzszy run, wiecej rozgalezien, wczesniejsze elity itd.
 */
data class DungeonConfig(
    /** Laczna liczba pieter, wliczajac pietro bossa. */
    val floors: Int = 12,
    /** Szerokosc mapy - ile kolumn moga zajmowac pola. */
    val columns: Int = 6,
    /** Ile losowych sciezek przechodzi przez mape. Wiecej sciezek = gestszy graf. */
    val paths: Int = 5,
    /** Ponizej tego pietra nie pojawia sie elita. */
    val minEliteFloor: Int = 4,
    /** Ponizej tego pietra nie pojawia sie sklep ani ognisko. */
    val minShopFloor: Int = 2,
    val minRestFloor: Int = 3
) {
    val bossFloor: Int get() = floors - 1

    /** Ognisko tuz przed bossem - klasyczny "odpocznij przed walka". */
    val campfireFloor: Int get() = floors - 2

    /** Skarb w polowie drogi, zeby run mial wyrazny srodkowy punkt. */
    val treasureFloor: Int get() = floors / 2
}
