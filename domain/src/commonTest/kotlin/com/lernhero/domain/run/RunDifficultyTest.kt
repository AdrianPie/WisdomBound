package com.lernhero.domain.run

import com.lernhero.domain.quiz.QuizDifficulty
import com.lernhero.domain.story.StoryWorldCatalog
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Tier kampanii obiecuje w swoim opisie "pytania wyraznie trudniejsze". Przez dluzszy czas byla to
 * nieprawda: trudnosc pytania brala sie WYLACZNIE z numeru fali, a plansza kampanii ma ich od
 * jednej do trzech - wiec cala kampania, na kazdym tierze, chodzila po poczatku drabinki.
 *
 * Te testy pilnuja trzech rzeczy, od ktorych zalezy, czy ta obietnica jest prawdziwa: ze poczatek
 * gry sie nie zaostrzyl, ze glebokosc i tier realnie podnosza poprzeczke, i ze nic nie wychodzi
 * poza cztery istniejace szczeble.
 */
class RunDifficultyTest {

    private val lastStage get() = StoryWorldCatalog.totalStages

    @Test
    fun `pierwsza plansza na Normalnym zostaje rozgrzewka`() {
        assertEquals(
            listOf(QuizDifficulty.Easy, QuizDifficulty.Easy, QuizDifficulty.Medium, QuizDifficulty.Medium),
            RunDifficulty.Normal.campaignLadder(BattleKind.Normal, globalStage = 1)
        )
    }

    @Test
    fun `glebokosc kampanii podnosi poprzeczke`() {
        val first = RunDifficulty.Normal.campaignLadder(BattleKind.Normal, globalStage = 1)
        val last = RunDifficulty.Normal.campaignLadder(BattleKind.Normal, globalStage = lastStage)

        assertTrue(
            last.first().ordinal > first.first().ordinal,
            "Ostatnia plansza kampanii ma pytac trudniej niz pierwsza"
        )
        assertTrue(last.none { it == QuizDifficulty.Easy }, "Na koncu kampanii nie ma juz taryfy ulgowej")
    }

    @Test
    fun `wyzszy tier pyta trudniej na tej samej planszy`() {
        val stage = 1
        val rungs = RunDifficulty.entries.map {
            it.campaignLadder(BattleKind.Normal, stage).first().ordinal
        }

        assertTrue(rungs[0] < rungs[1], "Koszmar ma pytac trudniej niz Normalny")
        assertTrue(rungs[1] < rungs[2], "Pieklo ma pytac trudniej niz Koszmar")
        // Smierc stoi na tym samym szczeblu co Pieklo - pytanie ma tylko cztery poziomy, a te dwa
        // tiery rozni juz sila wrogow, nie trudnosc wiedzy.
        assertEquals(rungs[2], rungs[3])
    }

    @Test
    fun `boss pyta trudniej niz zwykla plansza`() {
        val stage = 6
        val normal = RunDifficulty.Normal.campaignLadder(BattleKind.Normal, stage)
        val boss = RunDifficulty.Normal.campaignLadder(BattleKind.Boss, stage)

        assertTrue(boss.first().ordinal > normal.first().ordinal, "Boss ma podnosic poprzeczke")
    }

    /** Drabinka nigdy nie moze wyjsc poza cztery istniejace szczeble - takze daleko poza kampania. */
    @Test
    fun `drabinka trzyma sie czterech szczebli i zawsze ma cztery pozycje`() {
        RunDifficulty.entries.forEach { tier ->
            BattleKind.entries.forEach { kind ->
                listOf(1, lastStage, lastStage * 10).forEach { stage ->
                    val ladder = tier.campaignLadder(kind, stage)
                    assertEquals(4, ladder.size, "$tier/$kind/$stage")
                    assertTrue(ladder.zipWithNext().all { (a, b) -> a.ordinal <= b.ordinal }, "$tier/$kind/$stage")
                }
            }
        }
    }
}
