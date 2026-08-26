package com.lernhero.domain.spell

import com.lernhero.domain.combat.Combatant
import com.lernhero.domain.combat.DamageTag
import com.lernhero.domain.combat.CombatEvent
import com.lernhero.domain.combat.SpellChoice
import com.lernhero.domain.combat.Stats
import com.lernhero.domain.combat.StatType
import com.lernhero.domain.combat.Team
import com.lernhero.domain.combat.engine.BattleEngine
import kotlin.math.abs
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Pasek zdolnosci steruje tym, jak czesto zamiast ciosu pada zaklecie. Te testy pilnuja dwoch
 * rzeczy: matematyki podzialu (nie da sie przypisac wiecej niz pojemnosc) i tego, ze losowanie
 * w silniku faktycznie trzyma sie zadanych procentow.
 */
class AbilitySystemTest {

    /* ----------------------------- podzial procentow ----------------------------- */

    @Test
    fun `nie da sie przypisac wiecej niz pojemnosc`() {
        val bar = AbilityLoadout(capacity = 30)
            .withPercent("fireball", 25)
            .withPercent("heal", 20) // zostaje tylko 5 wolnego

        assertEquals(25, bar.percentOf("fireball"))
        assertEquals(5, bar.percentOf("heal"), "Drugie zaklecie docina sie do wolnej pojemnosci")
        assertEquals(30, bar.spentPercent)
        assertEquals(70, bar.attackPercent)
    }

    @Test
    fun `zejscie pojemnosci obcina nadmiarowe przypisania`() {
        val bar = AbilityLoadout(capacity = 40)
            .withPercent("fireball", 25)
            .withPercent("heal", 15)
            .withCapacity(20)

        assertTrue(bar.spentPercent <= 20, "Po zejsciu pojemnosci suma nie moze jej przekroczyc")
        assertEquals(20, bar.capacity)
    }

    @Test
    fun `zerowy procent zdejmuje zaklecie z paska`() {
        val bar = AbilityLoadout(capacity = 30)
            .withPercent("fireball", 20)
            .withPercent("fireball", 0)

        assertEquals(0, bar.percentOf("fireball"))
        assertTrue(bar.weights.isEmpty())
    }

    /* ------------------------------ losowanie tury ------------------------------ */

    private fun hero(spells: List<SpellChoice>) = Combatant(
        id = "hero",
        name = "hero",
        team = Team.Players,
        baseStats = Stats.baseline(maxHp = 100000, attack = 10, speed = 100f).with(StatType.CritChance, 0f),
        hp = 100000,
        spells = spells
    )

    private fun dummy() = Combatant(
        id = "enemy",
        name = "enemy",
        team = Team.Enemies,
        baseStats = Stats.baseline(maxHp = 100000, attack = 1, speed = 1f).with(StatType.CritChance, 0f),
        hp = 100000
    )

    /** Zaklecie-znacznik: zadaje obrazenia tagiem [DamageTag.Skill], zeby odroznic je od ciosu. */
    private fun markerSpell(percent: Int) = SpellChoice(
        spellId = "marker",
        chancePercent = percent,
        cast = { ctx ->
            listOf(
                com.lernhero.domain.combat.CombatAction.DealDamage(
                    sourceId = ctx.ownerId,
                    targetIds = ctx.enemies().map { it.id },
                    amount = 1f,
                    canCrit = false,
                    tag = DamageTag.Skill
                )
            )
        }
    )

    @Test
    fun `pusty pasek to zawsze zwykly atak`() {
        val result = BattleEngine(random = Random(1), maxTurns = 200)
            .simulate(listOf(hero(emptyList()), dummy()))

        val heroDamage = result.events.filterIsInstance<CombatEvent.DamageDealt>().filter { it.sourceId == "hero" }
        assertTrue(heroDamage.isNotEmpty())
        assertTrue(heroDamage.all { it.tag == DamageTag.Attack }, "Bez zaklec kazda tura to cios")
    }

    @Test
    fun `sto procent zaklecia to zawsze zaklecie`() {
        val result = BattleEngine(random = Random(2), maxTurns = 200)
            .simulate(listOf(hero(listOf(markerSpell(100))), dummy()))

        val heroDamage = result.events.filterIsInstance<CombatEvent.DamageDealt>().filter { it.sourceId == "hero" }
        assertTrue(heroDamage.isNotEmpty())
        assertTrue(heroDamage.all { it.tag == DamageTag.Skill }, "Przy 100% pasek zawsze rzuca zaklecie")
    }

    @Test
    fun `30 procent zaklecia pada mniej wiecej co trzecia ci tura`() {
        // Bohater bije mocno, ale wrog ma miliony HP, wiec walka trwa maxTurns i mamy duza probe.
        val result = BattleEngine(random = Random(99), maxTurns = 4000)
            .simulate(listOf(hero(listOf(markerSpell(30))), dummy()))

        val heroTurns = result.events.filterIsInstance<CombatEvent.DamageDealt>().filter { it.sourceId == "hero" }
        val spells = heroTurns.count { it.tag == DamageTag.Skill }
        val ratio = spells.toFloat() / heroTurns.size

        assertTrue(heroTurns.size > 500, "Potrzebujemy duzej proby - bylo ${heroTurns.size} tur")
        assertTrue(
            abs(ratio - 0.30f) < 0.05f,
            "Zaklecie powinno padac ~30% tur, a padlo ${(ratio * 100).toInt()}%"
        )
    }

    @Test
    fun `ten sam seed daje ten sam ciag akcji`() {
        fun run() = BattleEngine(random = Random(7), maxTurns = 300)
            .simulate(listOf(hero(listOf(markerSpell(40))), dummy()))
            .events.filterIsInstance<CombatEvent.DamageDealt>().filter { it.sourceId == "hero" }.map { it.tag }

        assertEquals(run(), run(), "Pasek zdolnosci musi byc odtwarzalny z seeda")
    }

    @Test
    fun `kazde zaklecie ma opis dla ZYWEJ walki, nie tylko dla symulacji`() {
        // Bez tego zaklecie dziala w BattleEngine, a na ekranie nie robi nic - dokladnie tak przez
        // dluzszy czas wygladal caly pasek zdolnosci.
        SpellCatalog.all.forEach { spell ->
            val live = spell.live
            val doesSomething = live.damageMultiplier > 0f || live.healMultiplier > 0f || live.freezeTurns > 0
            assertTrue(doesSomething, "${spell.id} nie robi nic w zywej walce")
        }
    }

    @Test
    fun `zaklecie leczace celuje w sojusznika, raniace we wroga`() {
        SpellCatalog.all.forEach { spell ->
            if (spell.live.heals) {
                assertEquals(SpellTarget.WoundedAlly, spell.live.target, "${spell.id} leczy, wiec cel to sojusznik")
            } else {
                assertTrue(
                    spell.live.target != SpellTarget.WoundedAlly,
                    "${spell.id} zadaje obrazenia, wiec nie moze celowac w sojusznika"
                )
            }
        }
    }
}
