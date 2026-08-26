package com.lernhero.domain.story

/**
 * Jedna kraina kampanii (jak wyspy na mapie w Idle Heroes). [art] to klucz grafiki tla - warstwa
 * UI go tlumaczy na PNG (patrz feature/home/story/StoryWorldArt.kt); dopoki go nie ma, ekran
 * rysuje zastepcza plansze z nazwa i tym kluczem, wiec podmiana nie wymaga zmiany tutaj.
 *
 * [stageCount] to liczba plansz w tej krainie. Co [BOSS_INTERVAL] plansz stoi boss - patrz
 * [StoryStageCatalog.isBossStage].
 */
data class StoryWorld(
    val id: String,
    val order: Int,
    val name: String,
    val art: String,
    val stageCount: Int
)

/**
 * Wszystkie krainy kampanii, w kolejnosci przechodzenia. Nazwy i grafiki sa PLACEHOLDERAMI -
 * podmieniasz je tu, w jednym miejscu, gdy dojda prawdziwe lokacje.
 */
object StoryWorldCatalog {

    val all: List<StoryWorld> = listOf(
        StoryWorld("forest", 1, "Zapomniany Las", "world_forest", stageCount = 15),
        StoryWorld("mine", 2, "Krwawa Kopalnia", "world_mine", stageCount = 15),
        StoryWorld("icefield", 3, "Lodowe Pustkowia", "world_icefield", stageCount = 15),
        StoryWorld("heat", 4, "Kraina Zaru", "world_heat", stageCount = 15),
        StoryWorld("heaven", 5, "Wysokie Niebiosa", "world_heaven", stageCount = 15),
        StoryWorld("abyss", 6, "Otchlan", "world_abyss", stageCount = 15)
    )

    val totalStages: Int get() = all.sumOf { it.stageCount }

    fun byId(id: String): StoryWorld? = all.firstOrNull { it.id == id }

    /** Pierwszy globalny numer planszy tej krainy (1-indeksowany, ciagly przez cala kampanie). */
    fun startIndexOf(worldId: String): Int {
        var index = 1
        for (world in all) {
            if (world.id == worldId) return index
            index += world.stageCount
        }
        return index
    }

    /** Kraina, do ktorej nalezy dany globalny numer planszy - null, gdy wyjdzie poza kampanie. */
    fun worldForGlobalStage(globalStage: Int): StoryWorld? {
        var index = 1
        for (world in all) {
            val end = index + world.stageCount - 1
            if (globalStage in index..end) return world
            index += world.stageCount
        }
        return null
    }

    /** Numer planszy WEWNATRZ krainy (1..stageCount) dla danego globalnego numeru. */
    fun stageInWorld(globalStage: Int): Int {
        val world = worldForGlobalStage(globalStage) ?: return 1
        return globalStage - startIndexOf(world.id) + 1
    }
}
