package com.lernhero.domain.progress

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Odbior nagrod za osiagniecia. Przez dluzszy czas caly ten system byl SAMYM licznikiem - ekran
 * Postepu rysowal paski, ale nie bylo czego odebrac, wiec petla sie nie domykala.
 *
 * Testy pilnuja trzech rzeczy: ze nagroda istnieje, ze da sie ja odebrac dokladnie raz i ze nie
 * da sie odebrac czegos, do czego gracz nie doszedl.
 */
class AchievementRewardTest {

    /** Osiagniecie, ktore w katalogu na pewno jest i liczy sie z prostego licznika. */
    private val firstQuestions = AchievementCatalog.byId("answers_50")!!

    @Test
    fun `kazdy stopien placi wiecej niz poprzedni`() {
        assertTrue(AchievementTier.Silver.reward > AchievementTier.Bronze.reward)
        assertTrue(AchievementTier.Gold.reward > AchievementTier.Silver.reward)
    }

    @Test
    fun `kazde osiagniecie w katalogu ma niezerowa nagrode`() {
        AchievementCatalog.all.forEach { achievement ->
            assertTrue(achievement.reward > 0, "${achievement.id} placi zero")
        }
    }

    @Test
    fun `nieosiagniety cel nie jest do odebrania`() {
        val stats = GameStats(questionsAnswered = 10)
        assertFalse(firstQuestions.isUnlocked(stats))
        assertFalse(firstQuestions.isClaimable(stats, claimed = emptySet()))
    }

    @Test
    fun `osiagniety i nieodebrany jest do odebrania`() {
        val stats = GameStats(questionsAnswered = 50)
        assertTrue(firstQuestions.isClaimable(stats, claimed = emptySet()))
    }

    @Test
    fun `raz odebranego nie da sie odebrac drugi raz`() {
        val stats = GameStats(questionsAnswered = 5000)
        assertTrue(firstQuestions.isUnlocked(stats), "cel nadal zdobyty")
        assertFalse(
            firstQuestions.isClaimable(stats, claimed = setOf(firstQuestions.id)),
            "odebrane osiagniecie nie moze wrocic do puli"
        )
    }

    @Test
    fun `nieodebrane zloto liczy tylko to, co faktycznie zdobyte`() {
        val nothing = AchievementCatalog.claimableGold(GameStats(), claimed = emptySet())
        assertEquals(0, nothing, "swieze konto nie ma czego odbierac")

        val stats = GameStats(questionsAnswered = 50)
        val gold = AchievementCatalog.claimableGold(stats, claimed = emptySet())
        assertTrue(gold >= firstQuestions.reward, "nagroda za zdobyty cel musi sie liczyc")

        val afterClaim = AchievementCatalog.claimableGold(stats, claimed = setOf(firstQuestions.id))
        assertEquals(gold - firstQuestions.reward, afterClaim)
    }

    @Test
    fun `id w katalogu sa unikalne`() {
        val ids = AchievementCatalog.all.map { it.id }
        assertEquals(ids.size, ids.toSet().size, "zdublowane id rozjechaloby odbior nagrod")
    }
}
