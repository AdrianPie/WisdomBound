package com.lernhero.domain.monster

import com.lernhero.domain.combat.StatType
import com.lernhero.domain.combat.Stats
import com.lernhero.domain.item.ItemSlot
import com.lernhero.domain.item.ItemCatalog
import com.lernhero.domain.item.catalog.MonsterGear
import com.lernhero.domain.player.HeroBaseStats
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Sprzet kompanow przez dlugi czas nie robil NIC.
 *
 * Caly modul walki ani razu nie wspominal o [MonsterEquipment]: gracz kul 28 pozycji we wlasnej
 * zakladce Kuzni, zakladal je w Summon Center, karta pokazywala wyzsze liczby - a druzyna szla do
 * walki z golym kompanem. Do tego karta zawyzala trzykrotnie, bo dodawala sprzet pisany w skali
 * WALKI do statystyk w skali KARTY.
 *
 * Te testy pilnuja obu polowek: ze sprzet w ogole liczy sie do sily kompana i ze liczy sie w
 * jednej, wlasciwej skali.
 */
class CompanionPowerTest {

    private val card = MonsterCatalog.all.first()

    @Test
    fun `sprzet podnosi sile kompana`() {
        val bare = CompanionPower.hpAt(card, stars = 1, level = 1)
        val geared = CompanionPower.hpAt(card, stars = 1, level = 1, gear = Stats.of(StatType.MaxHp to 100f))

        assertEquals(bare + 100, geared, "sprzet ma dochodzic do gotowej, przeskalowanej wartosci")
    }

    @Test
    fun `sprzet liczy sie w skali WALKI, a nie karty`() {
        // Gdyby sprzet wchodzil przed przeskalowaniem karty, +100 HP zamienialoby sie w +300.
        // To nie jest subtelnosc: pelny komplet dawalby wtedy kompanowi ponad 900 HP ponad baze.
        val gain = CompanionPower.hpAt(card, 1, 1, Stats.of(StatType.MaxHp to 100f)) -
            CompanionPower.hpAt(card, 1, 1)

        assertEquals(100, gain)
        assertTrue(CompanionPower.HP_SCALE > 1f, "karty sa opisane w malych liczbach i musza rosnac")
    }

    @Test
    fun `pelny sprzet druzyny jest mocniejszy od kompletu bohatera, ale nie bez umiaru`() {
        // Proporcja, o ktora tu chodzi: sprzetu kompanow jest SZESC slotow razy PIECIU kompanow
        // (30 sztuk) przeciwko DZIEWIECIU u bohatera, wiec ma byc mocniejszy - ale mniej niz
        // proporcjonalnie, zeby kuc najpierw dla bohatera nadal bylo poprawnym ruchem.
        //
        // Przy pierwotnych liczbach katalogu wychodzilo +102% HP druzyny przy +24% z kompletu
        // bohatera na +10 - czyli ponad cztery razy tyle. Docelowo okolo trzech.
        fun bestIn(items: List<com.lernhero.domain.item.Item>, slot: ItemSlot): Pair<Float, Float> =
            items.filter { it.slot == slot }
                .maxByOrNull { it.stats[StatType.Attack] * 3 + it.stats[StatType.MaxHp] }
                ?.let { it.stats[StatType.Attack] to it.stats[StatType.MaxHp] } ?: (0f to 0f)

        val companionSet = MonsterGear.SLOTS.map { bestIn(MonsterGear.all, it) }
        val gearAttack = companionSet.sumOf { it.first.toDouble() }.toFloat()
        val gearHp = companionSet.sumOf { it.second.toDouble() }.toFloat()

        val heroSet = ItemSlot.entries.map { bestIn(ItemCatalog.all, it) }
        val heroAttack = heroSet.sumOf { it.first.toDouble() }.toFloat()
        val heroHp = heroSet.sumOf { it.second.toDouble() }.toFloat()

        val party = 5
        val baseHp = HeroBaseStats.HP + party * CompanionPower.hpAt(card, 1, 1)
        val baseAttack = HeroBaseStats.ATTACK + party * CompanionPower.attackAt(card, 1, 1)

        // Komplet bohatera liczymy na +10, czyli w jego najmocniejszej postaci (x2.2).
        val heroShareHp = heroHp * 2.2f / baseHp
        val companionShareHp = gearHp * party / baseHp
        val heroShareAttack = heroAttack * 2.2f / baseAttack
        val companionShareAttack = gearAttack * party / baseAttack

        assertTrue(
            companionShareHp > heroShareHp,
            "trzydziesci sztuk ma dawac wiecej niz dziewiec: $companionShareHp vs $heroShareHp"
        )
        assertTrue(
            companionShareHp < heroShareHp * 4f,
            "...ale nie cztery razy tyle: $companionShareHp vs $heroShareHp"
        )
        assertTrue(
            companionShareAttack in heroShareAttack..(heroShareAttack * 4f),
            "to samo dla ataku: $companionShareAttack vs $heroShareAttack"
        )
    }
}
