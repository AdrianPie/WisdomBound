package com.lernhero.domain.monster

import com.lernhero.domain.common.Rarity
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class MonsterStarsTest {

    private val common = MonsterCatalog.SKELETON_WARRIOR

    @Test
    fun `swiezy kompan startuje na jednej gwiazdce z niskim sufitem`() {
        assertEquals(1, MonsterStars.MIN_STARS)
        assertEquals(10, MonsterStars.levelCapFor(1))
        assertFalse(MonsterStars.isMaxStars(1))
    }

    @Test
    fun `kazda gwiazdka podnosi sufit poziomu`() {
        val caps = (1..MonsterStars.MAX_STARS).map { MonsterStars.levelCapFor(it) }
        assertEquals(caps.sorted(), caps, "sufit musi rosnac monotonicznie: $caps")
        assertEquals(MonsterLevels.MAX_LEVEL, caps.last(), "ostatnia gwiazdka odblokowuje pelny sufit")
    }

    @Test
    fun `pelna sciezka to dokladnie dziewietnascie kopii`() {
        // 1 odblokowujaca + 2+3+5+8 duplikatow. To jest DEKLARACJA balansu - gdy ktos zmieni
        // koszty awansu, ten test powie mu, jak bardzo wydluzyl grind.
        assertEquals(19, MonsterStars.totalCopiesFor(MonsterStars.MAX_STARS))
        assertEquals(1, MonsterStars.totalCopiesFor(1))
        assertEquals(3, MonsterStars.totalCopiesFor(2))
    }

    @Test
    fun `awans jest coraz drozszy, a na maksie kosztuje zero`() {
        val costs = (1 until MonsterStars.MAX_STARS).map { MonsterStars.ascendCost(it) }
        assertEquals(costs.sorted(), costs, "koszt awansu ma rosnac: $costs")
        assertEquals(0, MonsterStars.ascendCost(MonsterStars.MAX_STARS))
    }

    @Test
    fun `gwiazdki podnosza statystyki niezaleznie od poziomu`() {
        val oneStar = MonsterStars.hpAt(common, stars = 1, level = 1)
        val maxStar = MonsterStars.hpAt(common, stars = MonsterStars.MAX_STARS, level = 1)

        assertEquals(common.hp, oneStar, "na 1★ i 1 poziomie to maja byc gole staty z katalogu")
        assertTrue(maxStar > oneStar, "$maxStar vs $oneStar")
    }

    @Test
    fun `poziom ponad sufit gwiazdki nie dziala`() {
        // Zapisany poziom 40 przy 1★ (sufit 10) nie moze dawac statystyk 40. poziomu - inaczej
        // awans nie byl by do niczego potrzebny.
        val capped = MonsterLevels.clampToStars(40, stars = 1)
        assertEquals(MonsterStars.levelCapFor(1), capped)
        assertTrue(MonsterLevels.isAtStarCap(level = 10, stars = 1))
        assertFalse(MonsterLevels.isAtStarCap(level = 10, stars = 2))
    }

    @Test
    fun `rzadszy duplikat rozpuszcza sie w wiecej esencji`() {
        val values = Rarity.entries.map { MonsterStars.dissolveValue(it) }
        assertEquals(values.sorted(), values, "wartosc ma rosnac z rzadkoscia: $values")
        assertTrue(values.all { it > 0 })
    }
}
