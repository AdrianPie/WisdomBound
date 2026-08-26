package com.lernhero.domain.monster

import com.lernhero.domain.common.Rarity
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class MonsterLevelsTest {

    private val common = MonsterCatalog.SKELETON_WARRIOR

    @Test
    fun `poziom startowy nie daje zadnego bonusu`() {
        assertEquals(common.hp, MonsterLevels.hpAt(common, MonsterLevels.MIN_LEVEL))
        assertEquals(common.attack, MonsterLevels.attackAt(common, MonsterLevels.MIN_LEVEL))
    }

    @Test
    fun `maksymalny poziom jest wyraznie mocniejszy od startowego`() {
        val maxHp = MonsterLevels.hpAt(common, MonsterLevels.MAX_LEVEL)
        val maxAttack = MonsterLevels.attackAt(common, MonsterLevels.MAX_LEVEL)

        assertTrue(maxHp > common.hp * 3, "HP na 50: $maxHp, bazowe: ${common.hp}")
        assertTrue(maxAttack > common.attack * 3, "ATK na 50: $maxAttack, bazowe: ${common.attack}")
    }

    @Test
    fun `poziom jest przycinany do zakresu`() {
        assertEquals(MonsterLevels.MIN_LEVEL, MonsterLevels.clamp(0))
        assertEquals(MonsterLevels.MIN_LEVEL, MonsterLevels.clamp(-5))
        assertEquals(MonsterLevels.MAX_LEVEL, MonsterLevels.clamp(999))
    }

    @Test
    fun `koszt rosnie z poziomem i z rzadkoscia`() {
        val early = MonsterLevels.upgradeCost(1, Rarity.Common)
        val late = MonsterLevels.upgradeCost(40, Rarity.Common)
        assertTrue(late > early * 10, "poziom 1: $early, poziom 40: $late")

        assertTrue(
            MonsterLevels.upgradeCost(1, Rarity.Legendary) > MonsterLevels.upgradeCost(1, Rarity.Common)
        )
    }

    @Test
    fun `na maksymalnym poziomie nie ma za co placic`() {
        assertTrue(MonsterLevels.isMaxed(MonsterLevels.MAX_LEVEL))
        assertEquals(0, MonsterLevels.upgradeCost(MonsterLevels.MAX_LEVEL, Rarity.Common))
    }

    @Test
    fun `wlozone zloto rosnie z poziomem`() {
        // Sprzedazy duplikatow juz nie ma (duplikat idzie na awans gwiazdki), ale suma wlozonego
        // zlota wciaz musi byc policzalna - to ona mowi, ile kosztowal caly rozwoj karty.
        val early = MonsterLevels.totalInvested(5, common.rarity)
        val late = MonsterLevels.totalInvested(40, common.rarity)
        assertTrue(late > early, "poziom 5: $early, poziom 40: $late")
        assertEquals(0, MonsterLevels.totalInvested(MonsterLevels.MIN_LEVEL, common.rarity))
    }
}
