package com.lernhero.domain.monster

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class CompanionSpellTest {

    @Test
    fun `kazde zaklecie przypisane do karty istnieje w katalogu`() {
        MonsterCatalog.all.forEach { card ->
            card.spellIds.forEach { id ->
                assertTrue(CompanionSpells.byId(id) != null, "${card.id} wskazuje na nieznane zaklecie $id")
            }
            assertEquals(card.spellIds.size, card.spells.size, "${card.id}: zaklecia sie nie rozwiazaly")
        }
    }

    @Test
    fun `kazdy kompan ma przynajmniej jedno zaklecie`() {
        MonsterCatalog.all.forEach { card ->
            assertTrue(card.spells.isNotEmpty(), "${card.id} nie ma zadnego zaklecia")
        }
    }

    @Test
    fun `poziom zaklecia podnosi i szanse, i sile`() {
        val spell = CompanionSpells.MEND
        val low = MonsterSpellLevels.chanceAt(spell, 1)
        val high = MonsterSpellLevels.chanceAt(spell, MonsterSpellLevels.MAX_LEVEL)
        assertTrue(high > low, "szansa: $low -> $high")

        assertTrue(
            MonsterSpellLevels.powerAt(spell, MonsterSpellLevels.MAX_LEVEL) >
                MonsterSpellLevels.powerAt(spell, 1)
        )
    }

    @Test
    fun `pierwszy poziom to dokladnie wartosci z katalogu`() {
        val spell = CompanionSpells.BONE_SHARD
        assertEquals(spell.chancePercent, MonsterSpellLevels.chanceAt(spell, MonsterSpellLevels.MIN_LEVEL))
        assertEquals(spell.power, MonsterSpellLevels.powerAt(spell, MonsterSpellLevels.MIN_LEVEL))
    }

    @Test
    fun `na maksymalnym poziomie nie ma za co placic`() {
        assertTrue(MonsterSpellLevels.isMaxed(MonsterSpellLevels.MAX_LEVEL))
        assertEquals(0, MonsterSpellLevels.upgradeCost(MonsterSpellLevels.MAX_LEVEL))
        assertFalse(MonsterSpellLevels.isMaxed(1))
        assertTrue(MonsterSpellLevels.upgradeCost(1) > 0)
    }

    @Test
    fun `szansa nigdy nie dobija do stu procent`() {
        // Zaklecie kompana ma byc bonusem, a nie zamiennikiem zwyklego ciosu - inaczej kompan
        // przestalby w ogole atakowac.
        CompanionSpells.all.forEach { spell ->
            val maxChance = MonsterSpellLevels.chanceAt(spell, MonsterSpellLevels.MAX_LEVEL)
            assertTrue(maxChance < 100, "${spell.id} dobija do $maxChance%")
        }
    }

    @Test
    fun `leczenie i tarcze celuja w sojusznikow, reszta we wrogow`() {
        CompanionSpells.all.forEach { spell ->
            val expected = when (spell.kind) {
                CompanionSpellKind.Heal, CompanionSpellKind.HealAll, CompanionSpellKind.Shield ->
                    com.lernhero.domain.spell.SpellTarget.WoundedAlly
                CompanionSpellKind.Blast -> com.lernhero.domain.spell.SpellTarget.AllEnemies
                else -> com.lernhero.domain.spell.SpellTarget.RandomEnemy
            }
            assertEquals(expected, spell.target, spell.id)
        }
    }

    @Test
    fun `kazda karta ma moc bierna i znane zaklecia`() {
        MonsterCatalog.all.forEach { card ->
            assertTrue(card.passive != null, "${card.id} bez mocy biernej")
            assertEquals(card.spellIds.size, card.spells.size, "${card.id} ma nieznane zaklecie")
            assertTrue(card.spells.isNotEmpty(), "${card.id} bez zaklec")
            assertTrue(card.defense in 0..40, "${card.id} ma obrone ${card.defense}")
        }
    }

    @Test
    fun `id zaklec sa unikalne`() {
        val ids = CompanionSpells.all.map { it.id }
        assertEquals(ids.size, ids.toSet().size)
    }
}
