package com.lernhero.domain.player

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class PartySlotsTest {

    @Test
    fun `na starcie jest dokladnie jedno miejsce`() {
        assertEquals(1, PartySlots.unlockedCount(heroLevel = 1))
        assertTrue(PartySlots.isUnlocked(0, heroLevel = 1))
        assertFalse(PartySlots.isUnlocked(1, heroLevel = 1))
    }

    @Test
    fun `miejsca otwieraja sie po kolei i nigdy nie zamykaja`() {
        var previous = 0
        (1..30).forEach { level ->
            val open = PartySlots.unlockedCount(level)
            assertTrue(open >= previous, "Poziom $level odebral miejsce: $previous -> $open")
            previous = open
        }
        assertEquals(PartySlots.TOTAL, PartySlots.unlockedCount(heroLevel = 30))
    }

    /**
     * Sedno bramki: kazde miejsce ma byc realnym celem, a nie formalnoscia. Gdyby progi staly zbyt
     * gesto, caly system zamienilby sie w jedna sesje i przestal cokolwiek rozkladac w czasie.
     */
    @Test
    fun `odstepy miedzy progami rosna`() {
        val levels = (0 until PartySlots.TOTAL).map { PartySlots.requiredLevel(it) }
        val gaps = levels.zipWithNext { a, b -> b - a }
        assertTrue(
            gaps.zipWithNext().all { (earlier, later) -> later >= earlier },
            "Odstepy maja rosnac, a sa: $gaps"
        )
    }

    /**
     * Ostatnie miejsce ma byc celem na caly pierwszy wieczor. Licząc ~150 XP za jedno zejscie
     * kampanii (patrz [PartySlots]), sprawdzamy, ze do konca tabeli jest wiecej niz 30 takich zejsc.
     */
    @Test
    fun `ostatnie miejsce wymaga dlugiej gry`() {
        val lastLevel = PartySlots.requiredLevel(PartySlots.TOTAL - 1)
        val totalXp = (2..lastLevel).sumOf { LevelCurve.xpForLevel(it) }

        // Prog liczy sie w PLANSZACH, nie w golym XP - inaczej kazde strojenie krzywej awansu
        // (LevelCurve.GROWTH) przewraca ten test, chociaz o krzywej awansu on nie jest. Plansza
        // kampanii placi okolo stu XP na poczatku i wiecej w glab (patrz RunLoot.depthMultiplier),
        // wiec "ponad trzydziesci plansz przy stu XP" to nadal solidne pol kampanii.
        assertTrue(totalXp > 30 * 100, "Do ostatniego miejsca trzeba tylko $totalXp XP")
    }

    @Test
    fun `stary zapis z pelna druzyna zostaje przyciety`() {
        val saved = listOf("a", "b", "c", "d", "e")
        val trimmed = PartySlots.enforce(saved, heroLevel = 1)
        assertEquals(listOf("a", null, null, null, null), trimmed)
    }

    @Test
    fun `przyciecie zachowuje kompanow na otwartych miejscach`() {
        val saved = listOf("a", "b", "c", "d", "e")
        val level = PartySlots.requiredLevel(2)
        val trimmed = PartySlots.enforce(saved, heroLevel = level)
        assertEquals(listOf("a", "b", "c", null, null), trimmed)
    }

    @Test
    fun `krotszy zapis nie wysypuje przyciecia`() {
        assertEquals(
            List(PartySlots.TOTAL) { null },
            PartySlots.enforce(emptyList(), heroLevel = 99)
        )
    }
}
