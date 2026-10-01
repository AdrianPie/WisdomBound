package com.lernhero.domain.monster

import com.lernhero.domain.craft.Forge
import com.lernhero.domain.craft.ForgeTrack
import com.lernhero.domain.item.catalog.MonsterGear
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Kolo zywiolow latwo zepsuc jedna literowka w [Element.beats] - a skutek nie jest bledem, tylko
 * cichym zaburzeniem balansu: typ, ktory bije sam siebie, albo taki, ktorego nikt nie kontruje.
 * Te testy pilnuja ksztaltu kola, a nie konkretnych liczb.
 */
class ElementTest {

    @Test
    fun `kazdy zywiol bije dokladnie jeden inny`() {
        Element.entries.forEach { element ->
            assertTrue(element.beats != element, "${element.label} bije sam siebie")
        }
        // Cel przewagi musi byc unikalny - inaczej ktorys typ jest bity dwa razy, a inny wcale.
        assertEquals(
            Element.entries.size,
            Element.entries.map { it.beats }.distinct().size,
            "Dwa zywioly bija ten sam typ"
        )
    }

    @Test
    fun `kazdy zywiol ma dokladnie jednego kontrujacego`() {
        Element.entries.forEach { element ->
            val counters = Element.entries.filter { it.beats == element }
            assertEquals(1, counters.size, "${element.label} ma ${counters.size} kontr zamiast jednej")
            assertEquals(counters.single(), element.losesTo)
        }
    }

    @Test
    fun `kolo domyka sie po czterech krokach`() {
        // Ogien -> Natura -> Woda -> Smierc -> Ogien. Przejscie krotsze niz pelna petla oznaczaloby,
        // ze czesc typow siedzi w osobnym, mniejszym cyklu i nigdy sie nie spotyka.
        var current = Element.Fire
        repeat(Element.entries.size) { current = current.beats }
        assertEquals(Element.Fire, current)
    }

    @Test
    fun `przewaga podbija a slabosc obniza obrazenia`() {
        val attacker = Element.Fire
        assertEquals(1f + Element.ADVANTAGE_BONUS, Element.multiplier(attacker, attacker.beats))
        assertEquals(1f - Element.DISADVANTAGE_PENALTY, Element.multiplier(attacker, attacker.losesTo))
    }

    @Test
    fun `starcie neutralne i brak zywiolu nie zmieniaja obrazen`() {
        val neutral = Element.entries.first { it != Element.Fire.beats && it != Element.Fire.losesTo && it != Element.Fire }
        assertEquals(1f, Element.multiplier(Element.Fire, neutral))

        // Bohater nie ma zywiolu - nie moze przez to obrywac ani bic mocniej.
        assertEquals(1f, Element.multiplier(null, Element.Water))
        assertEquals(1f, Element.multiplier(Element.Water, null))
        assertEquals(1f, Element.multiplier(null, null))
    }

    @Test
    fun `ten sam zywiol jest neutralny`() {
        Element.entries.forEach { element ->
            assertEquals(1f, Element.multiplier(element, element), "${element.label} kontruje sam siebie")
        }
    }

    @Test
    fun `etykieta starcia pojawia sie tylko przy przewadze i slabosci`() {
        assertEquals("Advantage", Element.relationLabel(Element.Fire, Element.Fire.beats))
        assertEquals("Weakness", Element.relationLabel(Element.Fire, Element.Fire.losesTo))
        assertNull(Element.relationLabel(Element.Fire, Element.Fire))
        assertNull(Element.relationLabel(null, Element.Fire))
    }

    /* ------------------------------- Karty i sprzet kompanow ------------------------------- */

    @Test
    fun `kazda karta kompana ma zywiol i uzywane sa wszystkie cztery`() {
        val used = MonsterCatalog.all.map { it.element }.toSet()
        assertEquals(
            Element.entries.toSet(),
            used,
            "Nieuzywany zywiol to typ, ktorego gracz nigdy nie zdobedzie: brakuje ${Element.entries.toSet() - used}"
        )
    }

    @Test
    fun `sprzet kompanow jest w osobnej puli niz sprzet bohatera`() {
        MonsterGear.all.forEach { item ->
            assertTrue(
                MonsterGear.isMonsterGear(item.id),
                "${item.id} nie ma prefiksu ${MonsterGear.ID_PREFIX}, wiec kuznia wrzuci go do plecaka bohatera"
            )
            assertNotNull(MonsterGear.byId(item.id))
        }
    }

    @Test
    fun `sprzet kompanow uzywa tylko slotow ktore kompan nosi`() {
        MonsterGear.all.forEach { item ->
            assertTrue(
                item.slot in MonsterGear.SLOTS,
                "${item.id} siedzi w slocie ${item.slot}, ktorego kompan nie ma - nie da sie go zalozyc"
            )
        }
    }

    @Test
    fun `dopasowanie zywiolu przedmiotu podbija a niedopasowanie obniza`() {
        val elemental = MonsterGear.all.first { MonsterGear.elementFor(it.id) != null }
        val itemElement = MonsterGear.elementFor(elemental.id)!!

        assertEquals(MonsterGear.MATCH_BONUS, MonsterGear.matchBonus(elemental.id, itemElement))
        assertEquals(MonsterGear.MISMATCH_PENALTY, MonsterGear.matchBonus(elemental.id, itemElement.beats))

        // Przedmiot uniwersalny (bez zywiolu) nigdy nie karze - inaczej kazdy kompan mialby
        // polowe katalogu oslabiona bez powodu.
        val universal = MonsterGear.all.first { MonsterGear.elementFor(it.id) == null }
        Element.entries.forEach { assertEquals(1f, MonsterGear.matchBonus(universal.id, it)) }
    }

    @Test
    fun `kuznia ma osobne recepty dla obu torow`() {
        val hero = Forge.recipesFor(ForgeTrack.Hero).map { it.id }.toSet()
        val monster = Forge.recipesFor(ForgeTrack.Monster).map { it.id }.toSet()

        assertTrue(hero.isNotEmpty() && monster.isNotEmpty())
        assertTrue(
            (hero intersect monster).isEmpty(),
            "Te same recepty w obu zakladkach: ${hero intersect monster}"
        )
        assertTrue(monster.all { MonsterGear.isMonsterGear(it) })
    }

    @Test
    fun `kazda recepta kompana da sie odnalezc po id`() {
        // recipeFor siega do OBU katalogow - gdyby siegal tylko do bohaterskiego, przycisk
        // "wykuj" na zakladce summonow nie mialby czego wykonac.
        MonsterGear.all.forEach { item ->
            assertNotNull(Forge.recipeFor(item.id), "Brak receptury dla ${item.id}")
        }
    }
}
