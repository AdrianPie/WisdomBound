package com.lernhero.domain.story

import com.lernhero.domain.run.BattleKind
import com.lernhero.domain.run.RunDifficulty
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class StoryStageTest {

    @Test
    fun `numeracja globalna i wewnatrz krainy sa spojne`() {
        val secondWorld = StoryWorldCatalog.all[1]
        val start = StoryWorldCatalog.startIndexOf(secondWorld.id)

        assertEquals(secondWorld, StoryWorldCatalog.worldForGlobalStage(start))
        assertEquals(1, StoryWorldCatalog.stageInWorld(start))
        assertEquals(secondWorld.stageCount, StoryWorldCatalog.stageInWorld(start + secondWorld.stageCount - 1))
    }

    @Test
    fun `kazda plansza nalezy do dokladnie jednej krainy`() {
        (1..StoryWorldCatalog.totalStages).forEach { global ->
            val world = StoryWorldCatalog.worldForGlobalStage(global)
            assertTrue(world != null, "plansza $global nie ma krainy")
        }
        assertEquals(null, StoryWorldCatalog.worldForGlobalStage(StoryWorldCatalog.totalStages + 1))
    }

    @Test
    fun `ostatnia plansza krainy jest zawsze bossem`() {
        StoryWorldCatalog.all.forEach { world ->
            assertTrue(StoryStageCatalog.isBossStage(world.stageCount, world.stageCount))
        }
    }

    @Test
    fun `tylko ostatnia plansza krainy jest bossem`() {
        (1 until 6).forEach { assertFalse(StoryStageCatalog.isBossStage(it, 6), "plansza $it nie powinna byc bossem") }
        assertTrue(StoryStageCatalog.isBossStage(6, 6))
    }

    @Test
    fun `kraina ma szesc plansz`() {
        StoryWorldCatalog.all.forEach { assertEquals(6, it.stageCount) }
    }

    @Test
    fun `stage boss ma jednego wroga i BattleKind Boss`() {
        val world = StoryWorldCatalog.all.first()
        val bossGlobal = StoryWorldCatalog.startIndexOf(world.id) + world.stageCount - 1
        val stage = StoryStageCatalog.stageAt(bossGlobal, RunDifficulty.Normal)

        assertTrue(stage != null)
        assertEquals(BattleKind.Boss, stage!!.kind)
        assertEquals(1, stage.enemyPreview.size)
        assertTrue(stage.isBoss)
    }

    @Test
    fun `zwykla plansza ma od dwoch do pieciu wrogow`() {
        val world = StoryWorldCatalog.all.first()
        val stage = StoryStageCatalog.stageAt(StoryWorldCatalog.startIndexOf(world.id), RunDifficulty.Normal)
        assertTrue(stage != null)
        assertTrue(stage!!.enemyPreview.size in 2..5)
        assertFalse(stage.isBoss)
    }

    /** Droga przez kraine ma sie zaostrzac, a nie falowac - patrz [StoryStageCatalog.stageAt]. */
    @Test
    fun `liczba fal rosnie w kolejnych planszach krainy`() {
        val world = StoryWorldCatalog.all.first()
        val start = StoryWorldCatalog.startIndexOf(world.id)
        val waves = (0 until world.stageCount - 1).map {
            StoryStageCatalog.stageAt(start + it, RunDifficulty.Normal)!!.waves
        }

        assertEquals(waves.sorted(), waves, "Plansza glebiej w krainie nie moze byc krotsza: $waves")
        assertTrue(waves.last() > waves.first(), "Ostatnia zwykla plansza ma byc dluzsza niz pierwsza")
    }

    @Test
    fun `wyzszy tier daje wyzsza nagrode za te sama plansze`() {
        val normal = StoryStageCatalog.stageAt(1, RunDifficulty.Normal)!!
        val death = StoryStageCatalog.stageAt(1, RunDifficulty.Death)!!
        assertTrue(death.rewardGold > normal.rewardGold)
    }

    @Test
    fun `label sklada sie z numeru krainy i planszy`() {
        val world = StoryWorldCatalog.all[2]
        val stage = StoryStageCatalog.stageAt(StoryWorldCatalog.startIndexOf(world.id) + 3, RunDifficulty.Normal)!!
        assertEquals("${world.order}-4", stage.label)
    }
}
