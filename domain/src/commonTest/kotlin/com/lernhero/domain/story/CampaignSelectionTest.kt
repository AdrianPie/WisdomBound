package com.lernhero.domain.story

import com.lernhero.domain.run.RunDifficulty
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class CampaignSelectionTest {

    @Test
    fun `bez wyboru trzyma sie krainy, w ktorej lezy frontier`() {
        val selection = CampaignSelection()
        val secondWorld = StoryWorldCatalog.all[1]
        val cleared = mapOf(RunDifficulty.Normal.name to StoryWorldCatalog.startIndexOf(secondWorld.id))

        assertEquals(secondWorld.id, selection.resolveWorld(cleared).id)
    }

    @Test
    fun `zmiana krainy kasuje recznie wybrana plansze`() {
        val selection = CampaignSelection()
        selection.selectStage(3)
        assertEquals(3, selection.stage)

        selection.select(StoryWorldCatalog.all[1].id, RunDifficulty.Normal)
        assertNull(selection.stage, "numer planszy nalezal do innej krainy")
    }

    @Test
    fun `plansza jest przycinana do widocznej krainy`() {
        val selection = CampaignSelection()
        val world = StoryWorldCatalog.all[1]
        selection.select(world.id, RunDifficulty.Normal)

        val start = StoryWorldCatalog.startIndexOf(world.id)
        val end = start + world.stageCount - 1
        val resolved = selection.resolveStage(emptyMap())

        assertTrue(resolved in start..end, "plansza $resolved poza krainą $start..$end")
    }

    @Test
    fun `po wygranej wraca do frontiera zamiast zostac na zdobytej`() {
        val selection = CampaignSelection()
        selection.selectStage(4)
        selection.clearStage()

        val cleared = mapOf(RunDifficulty.Normal.name to 4)
        assertEquals(5, selection.resolveStage(cleared), "powinno przesunac sie na kolejna plansze")
    }
}
