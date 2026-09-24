package com.lernhero.domain.run

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class RunLadderTest {

    @Test
    fun bossStandsEveryTenWavesAndFinishesTheTierAtTwenty() {
        assertFalse(RunLadder.isBossWave(9))
        assertTrue(RunLadder.isBossWave(10))
        assertTrue(RunLadder.isBossWave(20))
        assertFalse(RunLadder.isTierBoss(10))
        assertTrue(RunLadder.isTierBoss(20))
        assertEquals(BattleKind.Boss, RunLadder.kindFor(20))
        assertEquals(BattleKind.Normal, RunLadder.kindFor(19))
    }

    @Test
    fun theRunToTheFinishIsAGentleClimb() {
        // Liczy sie trudnosc WZGLEDNA - wrogowie rosna, ale druzyna zbiera po drodze karty
        // ulepszen. Meta ma byc wyrazna, a nie zaporowa.
        val finish = RunLadder.relativeDifficulty(tier = 0, wave = RunLadder.WAVES_PER_TIER)
        assertTrue(finish in 1.3f..2.1f, "meta wypada na ${finish}x trudnosci pierwszej fali")

        // Po drodze nie moze byc zadnego skoku - kazda fala odrobine trudniejsza od poprzedniej.
        (2..RunLadder.WAVES_PER_TIER).forEach { wave ->
            val step = RunLadder.relativeDifficulty(0, wave) / RunLadder.relativeDifficulty(0, wave - 1)
            assertTrue(step < 1.10f, "fala $wave skacze o ${step}x wzgledem poprzedniej")
        }
    }

    @Test
    fun buffsPickedDuringTheRunCountTowardsTheBalance() {
        // Zalozenie balansu spisane wprost: do mety wpada szesc kart ulepszen i one realnie licza sie
        // w trudnosci. Bez tego krzywa wrogow bylaby strojona pod druzyne, ktora nie rosnie.
        assertEquals(6, (RunLadder.WAVES_PER_TIER - 1) / RunLadder.BUFF_EVERY_WAVES)
        assertTrue(RunLadder.partyPowerAt(RunLadder.WAVES_PER_TIER) > 1.5f)
    }

    @Test
    fun pastTheFinishTheWallComesFast() {
        // Wlasciwosc, o ktora chodzi: kazda fala ZA meta dokłada duzo wiecej trudnosci niz fala
        // przed nia. Sam mnoznik nie wystarcza - karty ulepszen lecą dalej, wiec liczy sie roznica.
        val before = RunLadder.relativeDifficulty(0, RunLadder.WAVES_PER_TIER) /
            RunLadder.relativeDifficulty(0, RunLadder.WAVES_PER_TIER - 4)
        val after = RunLadder.relativeDifficulty(0, RunLadder.WAVES_PER_TIER + 4) /
            RunLadder.relativeDifficulty(0, RunLadder.WAVES_PER_TIER)
        assertTrue(after > before, "za meta rosnie o ${after}x, przed nia o ${before}x")

        // Bieg konczy sie kilka fal za meta - jest po co tam isc, ale nikt nie dojdzie do setnej.
        val wall = RunLadder.estimatedWall(tier = 0)
        assertTrue(wall in 24..34, "sciana wypadla na fali $wall")
    }

    @Test
    fun theTierIsFiveDotsOfFourWaves() {
        assertEquals(RunLadder.WAVES_PER_TIER, RunLadder.STAGES_PER_TIER * RunLadder.WAVES_PER_STAGE)
        assertEquals(0, RunLadder.stagesCleared(3))
        assertEquals(1, RunLadder.stagesCleared(4))
        assertEquals(4, RunLadder.stagesCleared(19))
        assertEquals(RunLadder.STAGES_PER_TIER, RunLadder.stagesCleared(20))
        // Rekord 23 fal to trzy fale DRUGIEGO poziomu, a nie dwadziescia trzy pierwszego.
        assertEquals(3, RunLadder.waveInTier(23))
    }

    @Test
    fun everyTierRaisesTheWholeRun() {
        val tier0 = RunLadder.enemyMultiplier(0, 1)
        val tier1 = RunLadder.enemyMultiplier(1, 1)
        assertEquals(RunLadder.TIER_STEP, tier1 / tier0, absoluteTolerance = 0.001f)
        // Wyzszy poziom jest trudniejszy OD PIERWSZEJ fali, a nie dopiero przy mecie.
        assertTrue(RunLadder.enemyMultiplier(1, 1) > RunLadder.enemyMultiplier(0, 1))
    }

    @Test
    fun tierComesFromTheBestWaveEverReached() {
        assertEquals(0, RunLadder.tierFor(0))
        assertEquals(0, RunLadder.tierFor(19))
        assertEquals(1, RunLadder.tierFor(20))
        assertEquals(1, RunLadder.tierFor(39))
        assertEquals(2, RunLadder.tierFor(40))
    }

    @Test
    fun lootGrowsSlowerThanEnemies() {
        // Inaczej oplacaloby sie wylacznie pchac za mete, zamiast przechodzic poziomy.
        val wave = RunLadder.WAVES_PER_TIER + 5
        val power = RunLadder.enemyMultiplier(0, wave) / RunLadder.enemyMultiplier(0, 1)
        val loot = RunLadder.rewardMultiplier(0, wave) / RunLadder.rewardMultiplier(0, 1)
        assertTrue(loot < power, "lup rosnie szybciej ($loot) niz wrogowie ($power)")
    }

    @Test
    fun clearingTwentyWavesCompletesTheTier() {
        assertFalse(RunLadder.clearsTier(tier = 0, wave = 19))
        assertTrue(RunLadder.clearsTier(tier = 0, wave = 20))
        assertEquals(1, RunLadder.wavesToFinish(19))
        assertTrue(RunLadder.wavesToFinish(25) < 0)
    }
}
