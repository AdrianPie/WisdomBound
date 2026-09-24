package com.lernhero.domain.story

import com.lernhero.domain.combat.live.AnswerTimer
import com.lernhero.domain.monster.EnemyRole
import com.lernhero.domain.run.BattleKind
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Balans kampanii jako biegu dwudziestofalowego. Te testy pilnuja UMOWY, a nie implementacji:
 * kazda liczba, ktora tu stoi, jest decyzja projektowa, ktora ma sie nie rozjechac przy strojeniu.
 */
class CampaignLadderTest {

    @Test
    fun `plansza to dwadziescia fal i dopiero ostatnia ja zalicza`() {
        assertEquals(20, CampaignLadder.WAVES_PER_STAGE)
        assertTrue(CampaignLadder.clearsStage(20))
        assertTrue(CampaignLadder.clearsStage(21))
        for (wave in 1..19) {
            assertTrue(!CampaignLadder.clearsStage(wave), "fala $wave nie moze zaliczac planszy")
        }
    }

    @Test
    fun `rytm fal - elita co piata, boss co dziesiata`() {
        assertEquals(BattleKind.Normal, CampaignLadder.kindFor(1))
        assertEquals(BattleKind.Normal, CampaignLadder.kindFor(4))
        assertEquals(BattleKind.Elite, CampaignLadder.kindFor(5))
        assertEquals(BattleKind.Boss, CampaignLadder.kindFor(10))
        assertEquals(BattleKind.Elite, CampaignLadder.kindFor(15))
        assertEquals(BattleKind.Boss, CampaignLadder.kindFor(20))
    }

    @Test
    fun `do mety wypada szesc kart ulepszen`() {
        val picks = (1..CampaignLadder.WAVES_PER_STAGE).count { CampaignLadder.offersUpgrade(it) }
        assertEquals(6, picks, "krzywa trudnosci jest policzona przy szesciu kartach do fali 20")
    }

    /**
     * Najwazniejszy test w tym pliku. [CampaignLadder.relativeDifficulty] mowi, o ile wrogowie
     * wyprzedzili druzyne rosnaca na kartach - i to ona, a nie surowa sila wroga, decyduje, czy
     * bieg da sie przejsc.
     */
    @Test
    fun `krzywa rosnie lagodnie do mety i dopiero tam robi sie ciezko`() {
        val start = CampaignLadder.relativeDifficulty(1)
        val half = CampaignLadder.relativeDifficulty(10)
        val finish = CampaignLadder.relativeDifficulty(20)

        assertEquals(1f, start, absoluteTolerance = 0.01f)
        // Polowa biegu ma byc jeszcze rozgrzewka - inaczej nie ma gdzie zbudowac przewagi.
        assertTrue(half in 1.2f..1.6f, "polowa biegu: $half")
        // Meta: ciezko, ale w zasiegu dobrych odpowiedzi (mnoznik czasu 1.5 x passa 1.35 = ~2.0).
        assertTrue(finish in 1.9f..2.4f, "meta: $finish")
    }

    @Test
    fun `za meta bieg konczy sie w kilka fal`() {
        val wall = CampaignLadder.estimatedWall()
        assertTrue(wall > CampaignLadder.WAVES_PER_STAGE, "sciana nie moze wypadac przed meta: $wall")
        assertTrue(wall <= 26, "za meta ma byc kilka fal bonusu, a nie druga kampania: $wall")
    }

    @Test
    fun `glebokosc planszy podnosi caly bieg, a plansza z bossem dodatkowo`() {
        val first = CampaignLadder.stageHpMultiplier(1)
        val deep = CampaignLadder.stageHpMultiplier(36)
        assertEquals(1f, first, absoluteTolerance = 0.001f)
        assertTrue(deep > first * 4f, "ostatnia plansza ma byc wyraznie grubsza: $deep")

        val boss = CampaignLadder.stageHpMultiplier(10, isBossStage = true)
        val plain = CampaignLadder.stageHpMultiplier(10, isBossStage = false)
        assertTrue(boss > plain)
    }
}

/** Czas na odpowiedz przestal byc stala - te testy pilnuja, w ktora strone sie zmienia. */
class AnswerWindowTest {

    @Test
    fun `dluzsze pytanie dostaje wiecej czasu`() {
        val four = AnswerTimer.windowFor(optionCount = 4, wave = 1)
        val seven = AnswerTimer.windowFor(optionCount = 7, wave = 1)
        assertEquals(AnswerTimer.WINDOW_MS, four)
        assertTrue(seven > four, "siedem opcji to wiecej czytania niz cztery")
    }

    @Test
    fun `glebsza fala tnie czas, ale nigdy ponizej podlogi`() {
        val early = AnswerTimer.windowFor(optionCount = 4, wave = 1)
        val finish = AnswerTimer.windowFor(optionCount = 4, wave = 20)
        val absurd = AnswerTimer.windowFor(optionCount = 4, wave = 500)

        assertTrue(finish < early, "meta ma wymagac szybszej odpowiedzi")
        assertTrue(finish >= 12_000, "13s to presja, 5s to loteria: $finish")
        assertTrue(absurd >= 8_000, "podloga czasu musi trzymac: $absurd")
    }
}

/** Role wrogow - druga, obok gatunku, os roznorodnosci fali. */
class EnemyRoleTest {

    @Test
    fun `pierwsze fale to sam tlum, mocniejsze role wchodza pozniej`() {
        val early = (0 until 100).map { EnemyRole.roll(wave = 1, roll = it / 100f) }.toSet()
        assertEquals(setOf(EnemyRole.Grunt), early, "fala 1 ma byc rozgrzewka")

        val late = (0 until 100).map { EnemyRole.roll(wave = 20, roll = it / 100f) }.toSet()
        assertTrue(EnemyRole.Ravager in late)
        assertTrue(EnemyRole.Brute in late)
        assertTrue(EnemyRole.Grunt in late, "zwykly wrog ma zostac najczestszy")
    }

    @Test
    fun `kazda rola to inny sposob na zabicie`() {
        assertTrue(EnemyRole.Brute.hpMultiplier > 1.5f && EnemyRole.Brute.attackMultiplier < 1f)
        assertTrue(EnemyRole.Stalker.hpMultiplier < 0.7f && EnemyRole.Stalker.attackMultiplier > 1.4f)
    }
}
