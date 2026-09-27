package com.lernhero.domain.craft

import com.lernhero.domain.combat.StatType
import com.lernhero.domain.item.catalog.Armors
import com.lernhero.domain.item.catalog.Weapons
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ItemUpgradesTest {

    @Test
    fun poziom_zero_nie_rusza_statystyk() {
        val item = Weapons.PLATE_SWORD
        assertEquals(
            item.stats[StatType.Attack],
            ItemUpgrades.upgraded(item, 0).stats[StatType.Attack]
        )
    }

    @Test
    fun kazdy_poziom_dokłada_ten_sam_procent() {
        val item = Weapons.PLATE_SWORD
        val base = item.stats[StatType.Attack]

        val plusOne = ItemUpgrades.upgraded(item, 1).stats[StatType.Attack]
        val maxed = ItemUpgrades.upgraded(item, ItemUpgrades.MAX_LEVEL).stats[StatType.Attack]

        assertEquals(base * (1f + ItemUpgrades.BONUS_PER_LEVEL), plusOne, absoluteTolerance = 0.001f)
        assertEquals(
            base * (1f + ItemUpgrades.BONUS_PER_LEVEL * ItemUpgrades.MAX_LEVEL),
            maxed,
            absoluteTolerance = 0.001f
        )
    }

    /** Poziom spoza toru nie moze przemycic bonusu ani ujemnego, ani ponad maksymalny. */
    @Test
    fun poziom_jest_przycinany_do_toru() {
        assertEquals(1f, ItemUpgrades.multiplierFor(-5))
        assertEquals(
            ItemUpgrades.multiplierFor(ItemUpgrades.MAX_LEVEL),
            ItemUpgrades.multiplierFor(ItemUpgrades.MAX_LEVEL + 5)
        )
        assertEquals(ItemUpgrades.MAX_LEVEL, ItemUpgrades.levelOf(mapOf("x" to 99), "x"))
        assertEquals(ItemUpgrades.MIN_LEVEL, ItemUpgrades.levelOf(emptyMap(), "x"))
    }

    @Test
    fun na_maksie_nie_ma_kolejnego_kosztu() {
        assertNull(ItemUpgrades.nextCost(Weapons.PLATE_SWORD, ItemUpgrades.MAX_LEVEL))
        assertNotNull(ItemUpgrades.nextCost(Weapons.PLATE_SWORD, ItemUpgrades.MAX_LEVEL - 1))
    }

    @Test
    fun kolejny_poziom_kosztuje_wiecej_niz_poprzedni() {
        val item = Weapons.PLATE_SWORD
        val first = ItemUpgrades.costFor(item, 1)!!
        val later = ItemUpgrades.costFor(item, 5)!!

        assertTrue(later.iron > first.iron)
        assertTrue(later.stone > first.stone)
        assertTrue(later.gold > first.gold)
    }

    /** Esencja - najrzadszy surowiec - wchodzi dopiero w drugiej polowie toru. */
    @Test
    fun esencja_dochodzi_dopiero_od_progu() {
        val item = Weapons.PLATE_SWORD
        assertEquals(0, ItemUpgrades.costFor(item, ItemUpgrades.ESSENCE_FROM_LEVEL - 1)!!.essence)
        assertTrue(ItemUpgrades.costFor(item, ItemUpgrades.ESSENCE_FROM_LEVEL)!!.essence > 0)
    }

    /** Rzadszy przedmiot to wieksza sciana materialow na tym samym poziomie. */
    @Test
    fun rzadkosc_podbija_koszt() {
        val common = ItemUpgrades.costFor(Armors.PLATE_BOOTS, 3)!!
        val epic = ItemUpgrades.costFor(Armors.AEGIS_SHIELD, 3)!!
        assertTrue(epic.iron > common.iron)
    }

    /** Ulepszenie podbija LICZBY, a nie to, co przedmiot robi. */
    @Test
    fun efekty_zostaja_nietkniete() {
        val item = Weapons.PLATE_SWORD
        assertEquals(item.effects, ItemUpgrades.upgraded(item, ItemUpgrades.MAX_LEVEL).effects)
    }
}
