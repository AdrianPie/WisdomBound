package com.lernhero.domain.story

import com.lernhero.domain.run.RunDifficulty
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class StoryProgressTest {

    @Test
    fun `frontier na pustym koncie to pierwsza plansza`() {
        assertEquals(1, StoryProgress.frontier(emptyMap(), RunDifficulty.Normal))
    }

    @Test
    fun `frontier to jeden krok za ostatnim zdobytym`() {
        val cleared = mapOf(RunDifficulty.Normal.name to 7)
        assertEquals(8, StoryProgress.frontier(cleared, RunDifficulty.Normal))
    }

    @Test
    fun `mozna grac zdobyte plansze i frontier ale nie dalej`() {
        val cleared = mapOf(RunDifficulty.Normal.name to 5)
        assertTrue(StoryProgress.isStagePlayable(cleared, RunDifficulty.Normal, 1))
        assertTrue(StoryProgress.isStagePlayable(cleared, RunDifficulty.Normal, 5))
        assertTrue(StoryProgress.isStagePlayable(cleared, RunDifficulty.Normal, 6))
        assertFalse(StoryProgress.isStagePlayable(cleared, RunDifficulty.Normal, 7))
    }

    @Test
    fun `normal jest zawsze odblokowany, wyzsze tiery wymagaja pelnego przejscia poprzedniego`() {
        assertTrue(StoryProgress.isTierUnlocked(emptyMap(), RunDifficulty.Normal))
        assertFalse(StoryProgress.isTierUnlocked(emptyMap(), RunDifficulty.Nightmare))

        val almostDone = mapOf(RunDifficulty.Normal.name to StoryWorldCatalog.totalStages - 1)
        assertFalse(StoryProgress.isTierUnlocked(almostDone, RunDifficulty.Nightmare))

        val done = mapOf(RunDifficulty.Normal.name to StoryWorldCatalog.totalStages)
        assertTrue(StoryProgress.isTierUnlocked(done, RunDifficulty.Nightmare))
        assertFalse(StoryProgress.isTierUnlocked(done, RunDifficulty.Hell))
    }

    @Test
    fun `zapis postepu nigdy nie cofa juz zdobytego rekordu`() {
        val cleared = mapOf(RunDifficulty.Normal.name to 10)
        val same = StoryProgress.withCleared(cleared, RunDifficulty.Normal, 4)
        assertEquals(10, same[RunDifficulty.Normal.name])

        val advanced = StoryProgress.withCleared(cleared, RunDifficulty.Normal, 12)
        assertEquals(12, advanced[RunDifficulty.Normal.name])
    }

    @Test
    fun `kampania jest ukonczona dopiero na ostatniej plansze`() {
        val almost = mapOf(RunDifficulty.Normal.name to StoryWorldCatalog.totalStages - 1)
        assertFalse(StoryProgress.isCampaignComplete(almost, RunDifficulty.Normal))

        val complete = mapOf(RunDifficulty.Normal.name to StoryWorldCatalog.totalStages)
        assertTrue(StoryProgress.isCampaignComplete(complete, RunDifficulty.Normal))
    }
}
