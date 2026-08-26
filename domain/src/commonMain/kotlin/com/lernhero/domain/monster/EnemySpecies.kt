package com.lernhero.domain.monster

/**
 * Gatunki losowane na zwyklych wrogow walki i elity (boss ma zawsze wlasny, staly avatar -
 * patrz [BOSS_SPECIES]). Wydzielone z BattleViewModel, zeby ten sam pool i te same archetypy
 * widzial rowniez podglad przeciwnikow na mapie kampanii ([com.lernhero.domain.story]) - inaczej
 * podglad i prawdziwa walka moglyby pokazywac dwie rozne druzyny wrogow.
 */
object EnemySpecies {

    val all: List<String> = listOf(
        "skeleton_warrior",
        "skeleton_archer",
        "skeleton_mage",
        "orc_warrior",
        "orc_archer",
        "human_archer"
    )

    /** Avatar bossa - jedyny gatunek spoza [all], nie ma wlasnej karty kompana. */
    const val BOSS_SPECIES = "boss_skeleton_dragon"

    /**
     * Archetyp kazdego gatunku jako (mnoznik HP, mnoznik ataku) - wyliczone z proporcji ich WLASNYCH
     * kart kompanow w [MonsterCatalog] (np. orc_warrior 100hp/18atk vs skeleton_mage 55hp/22atk),
     * znormalizowane do sredniej calej puli. Dzieki temu wrog i kompan tego samego gatunku "graja
     * sie" tak samo - tank ma duzo HP i slaby cios, glass cannon odwrotnie.
     */
    val statMultiplier: Map<String, Pair<Float, Float>> = mapOf(
        "skeleton_warrior" to (0.95f to 0.78f),
        "skeleton_archer" to (0.82f to 0.88f),
        "skeleton_mage" to (0.75f to 1.20f),
        "orc_warrior" to (1.35f to 1.00f),
        "orc_archer" to (1.10f to 1.10f),
        "human_archer" to (1.00f to 1.05f)
    )

    fun archetypeOf(species: String): Pair<Float, Float> = statMultiplier[species] ?: (1f to 1f)
}
