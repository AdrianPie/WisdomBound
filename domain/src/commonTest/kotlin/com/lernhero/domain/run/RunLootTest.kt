package com.lernhero.domain.run

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Krzywa wyplat kampanii. Jeden test pilnuje rzeczy, ktorej w grze nie bylo widac, a ktora
 * rozstrzygala o calej progresji: **plansza placila tyle samo niezaleznie od glebokosci**.
 *
 * Skutek nie byl "malo zlota". Skutek byl taki, ze poziom bohatera rosnie liniowo z XP, a wrogowie
 * mnoznikowo z numerem planszy - wiec przy plaskiej wyplacie kampania musiala sie zaciac, i to
 * dokladnie w polowie. Symulacja walki stawiala te sciane na planszy 7-9 Normalnego.
 */
class RunLootTest {

    @Test
    fun `glebsza plansza placi wiecej za te sama fale`() {
        val shallow = RunLoot.xpForWave(wave = 1, kind = BattleKind.Normal, difficulty = RunDifficulty.Normal, stage = 1)
        val deep = RunLoot.xpForWave(wave = 1, kind = BattleKind.Normal, difficulty = RunDifficulty.Normal, stage = 36)

        assertTrue(deep > shallow * 3, "Plansza 36 ma placic wyraznie wiecej niz plansza 1, a placi $deep vs $shallow")
    }

    @Test
    fun `materialy tez ida za glebokoscia`() {
        val ironShallow = RunLoot.ironForWave(wave = 1, kind = BattleKind.Normal, stage = 1)
        val ironDeep = RunLoot.ironForWave(wave = 1, kind = BattleKind.Normal, stage = 30)
        val essenceShallow = RunLoot.essenceForWave(wave = 1, kind = BattleKind.Normal, stage = 1)
        val essenceDeep = RunLoot.essenceForWave(wave = 1, kind = BattleKind.Normal, stage = 30)

        assertTrue(ironDeep > ironShallow, "Ruda ma rosnac z glebokoscia")
        assertTrue(essenceDeep > essenceShallow, "Esencja ma rosnac z glebokoscia")
    }

    /** Endless nie ma glebokosci - tam za skalowanie odpowiada numer fali i nic sie nie zmienia. */
    @Test
    fun `poza kampania mnoznik glebokosci nic nie robi`() {
        assertEquals(1f, RunLoot.depthMultiplier(RunLoot.NO_STAGE))
        assertEquals(1f, RunLoot.depthMultiplier(1))
        assertEquals(
            RunLoot.xpForWave(5, BattleKind.Normal, RunDifficulty.Normal),
            RunLoot.xpForWave(5, BattleKind.Normal, RunDifficulty.Normal, RunLoot.NO_STAGE)
        )
    }

    /**
     * **Losy z bossa.** Kolo fortuny bylo ich jedynym zrodlem, a daje 0.23-0.42 losa na skrzynie -
     * okolo dziewieciu z calej gry. Piata gwiazdka JEDNEJ karty kosztuje 19 kopii, wiec gwiazdki,
     * rozpuszczanie duplikatow i caly tor poziomow kompanow staly na walucie, ktorej praktycznie
     * nie bylo. Ten test pilnuje, zeby pewny przydzial za bossa zostal pewny.
     */
    @Test
    fun `boss zawsze daje losy, i wiecej w glab kampanii`() {
        val shallow = RunLoot.bossBonus(BattleKind.Boss, dungeonLevel = 1).tickets
        val deep = RunLoot.bossBonus(BattleKind.Boss, dungeonLevel = 36).tickets

        assertTrue(shallow >= 2, "Kazdy boss ma dac pewne losy, dal $shallow")
        assertTrue(deep > shallow, "Glebszy boss ma dac wiecej losow: $deep vs $shallow")
        assertTrue(RunLoot.bossBonus(BattleKind.Elite, 1).tickets >= 1, "Elita tez cos daje")
        assertEquals(0, RunLoot.bossBonus(BattleKind.Normal, 1).tickets, "Zwykla fala losow nie daje")
    }

    /**
     * Sens liczbowy: jeden tier kampanii (szesc bossow) ma otwierac droge do piatej gwiazdki
     * jednej karty (19 kopii), a nie zostawiac gracza z pojedynczym losem.
     */
    @Test
    fun `tier kampanii daje losy na realny postep kolekcji`() {
        val perTier = listOf(6, 12, 18, 24, 30, 36).sumOf { RunLoot.bossBonus(BattleKind.Boss, it).tickets }
        assertTrue(perTier >= 18, "Szesc bossow tieru dalo tylko $perTier losow")
    }

    /** Mnoznik ma isc w tym samym tempie, co puchniecie wrogow - inaczej to jest tylko inflacja. */
    @Test
    fun `mnoznik glebokosci rosnie w tempie krzywej wrogow`() {
        // BattleViewModel.LEVEL_HP_STEP = 0.12 na plansze.
        assertEquals(1f + 35 * 0.12f, RunLoot.depthMultiplier(36), absoluteTolerance = 0.001f)
    }
}
