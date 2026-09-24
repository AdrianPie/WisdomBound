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

/**
 * Rola pojedynczego wroga w fali - druga, niezalezna od gatunku os roznorodnosci.
 *
 * ## Po co
 *
 * Gatunkow jest szesc i kazdy ma STALY archetyp, wiec fala dziesiata rozni sie od pierwszej
 * wylacznie liczbami: te same sylwetki, te same proporcje, tylko wieksze HP. Dwadziescia fal
 * takiego czegos to dwadziescia razy ta sama walka.
 *
 * Rola mnozy sie przez archetyp gatunku i zmienia to, JAK sie danego wroga zabija: Kolos ma
 * podwojne HP przy slabym ciosie (trzeba go przetrwac), Lowca odwrotnie - pada od jednego trafienia,
 * ale bije jak elita (trzeba go ubic PIERWSZEGO). To sa decyzje w walce, a nie kolejne cyferki.
 *
 * Nie wymaga ani jednej nowej grafiki - role odkladaja sie na istniejacych sprite'ach i widac je
 * w nazwie wroga na pasku zycia.
 */
enum class EnemyRole(
    /** Dopisek do nazwy wroga - pusty dla zwyklego. */
    val label: String,
    val hpMultiplier: Float,
    val attackMultiplier: Float,
    /** Od ktorej fali ta rola w ogole wchodzi do puli - pierwsze fale maja byc rozgrzewka. */
    val fromWave: Int,
    /** Waga w losowaniu, gdy juz wejdzie. */
    val weight: Int
) {
    Grunt("", 1f, 1f, fromWave = 1, weight = 10),
    Brute("Brute", 1.85f, 0.80f, fromWave = 3, weight = 5),
    Stalker("Stalker", 0.62f, 1.45f, fromWave = 5, weight = 5),
    Warden("Warden", 1.45f, 1.15f, fromWave = 9, weight = 4),
    Ravager("Ravager", 0.85f, 1.70f, fromWave = 13, weight = 4);

    companion object {

        /**
         * Losuje role dla wroga na danej fali. Zwykly wrog zostaje najczestszy przez cala walke -
         * role maja urozmaicac fale, a nie zamieniac ja w galerie potworow, w ktorej nie wiadomo,
         * na co patrzec.
         */
        fun roll(wave: Int, roll: Float): EnemyRole {
            val pool = entries.filter { wave >= it.fromWave }
            val total = pool.sumOf { it.weight }
            var pick = (roll.coerceIn(0f, 0.999f) * total).toInt()
            for (role in pool) {
                pick -= role.weight
                if (pick < 0) return role
            }
            return Grunt
        }
    }
}
