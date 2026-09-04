package com.lernhero.domain.combat

import com.lernhero.domain.combat.effects.AllyAuraEffect
import com.lernhero.domain.combat.effects.ExtraAttackOnKillEffect
import com.lernhero.domain.combat.effects.HealAlliesEffect
import com.lernhero.domain.combat.effects.IgnoreFirstHitEffect
import com.lernhero.domain.combat.effects.LifestealEffect
import com.lernhero.domain.combat.effects.MultiTargetAttackEffect
import com.lernhero.domain.combat.effects.PoisonOnHitEffect
import com.lernhero.domain.combat.effects.ReflectDamageEffect
import com.lernhero.domain.combat.effects.StatBonusEffect
import com.lernhero.domain.combat.engine.BattleEngine
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Kazdy test dowodzi, ze jeden klocek systemu dziala w prawdziwej symulacji - nie w izolacji.
 * Jesli ktos kiedys zepsuje potok obrazen, to tu zapali sie czerwone swiatlo.
 */
class BattleEngineTest {

    private fun stats(
        hp: Int,
        attack: Int,
        speed: Float,
        defense: Float = 0f
    ) = Stats.of(
        StatType.MaxHp to hp.toFloat(),
        StatType.Attack to attack.toFloat(),
        StatType.Defense to defense,
        StatType.Speed to speed,
        // Krytyki wylaczone: testy maja sprawdzac efekty, nie rzuty koscmi.
        StatType.CritChance to 0f,
        StatType.CritDamage to 1.5f,
        StatType.AttackTargets to 1f
    )

    private fun fighter(
        id: String,
        team: Team,
        hp: Int = 100,
        attack: Int = 10,
        speed: Float = 10f,
        defense: Float = 0f,
        effects: List<Effect> = emptyList()
    ) = Combatant(
        id = id,
        name = id,
        team = team,
        baseStats = stats(hp, attack, speed, defense),
        hp = hp,
        effects = effects
    )

    private fun engine(seed: Long = 7L) = BattleEngine(random = Random(seed))

    private fun BattleResultEvents.damageBy(id: CombatantId) =
        filterIsInstance<CombatEvent.DamageDealt>().filter { it.sourceId == id }

    @Test
    fun `miecz wielocelowy trafia dwoch wrogow w jednej turze`() {
        val hero = fighter(
            id = "hero",
            team = Team.Players,
            hp = 500,
            attack = 20,
            speed = 100f,
            effects = listOf(MultiTargetAttackEffect(targets = 2))
        )
        val enemies = listOf("e1", "e2", "e3").map {
            fighter(it, Team.Enemies, hp = 200, attack = 1, speed = 1f)
        }

        val events = engine().simulate(listOf(hero) + enemies).events
        val firstTwo = events.damageBy("hero").take(2)

        assertEquals(2, firstTwo.size, "Bohater powinien uderzyc dwa razy w pierwszej turze")
        assertEquals(
            2,
            firstTwo.map { it.targetId }.distinct().size,
            "Oba ciosy powinny trafic w roznych wrogow"
        )
    }

    @Test
    fun `pierscien dokladajacy cel sumuje sie z mieczem`() {
        val hero = fighter(
            id = "hero",
            team = Team.Players,
            hp = 500,
            attack = 20,
            speed = 100f,
            effects = listOf(
                MultiTargetAttackEffect(targets = 2),
                StatBonusEffect(
                    id = "ring_extra_target",
                    description = "+1 cel",
                    bonus = Stats.of(StatType.AttackTargets to 1f)
                )
            )
        )
        val enemies = listOf("e1", "e2", "e3").map {
            fighter(it, Team.Enemies, hp = 200, attack = 1, speed = 1f)
        }

        val firstThree = engine().simulate(listOf(hero) + enemies).events.damageBy("hero").take(3)

        assertEquals(
            3,
            firstThree.map { it.targetId }.distinct().size,
            "2 cele z miecza + 1 z pierscienia = 3 rozni wrogowie"
        )
    }

    @Test
    fun `pancerz pochlania pierwszy cios i tylko pierwszy`() {
        val hero = fighter(
            id = "hero",
            team = Team.Players,
            hp = 100,
            attack = 1,
            speed = 1f,
            effects = listOf(IgnoreFirstHitEffect())
        )
        val enemy = fighter("e1", Team.Enemies, hp = 1000, attack = 10, speed = 100f)

        val result = engine().simulate(listOf(hero, enemy))
        val hitsOnHero = result.events
            .filterIsInstance<CombatEvent.DamageDealt>()
            .filter { it.targetId == "hero" }

        val enemyAttacks = result.finalState.turn - 1
        assertTrue(enemyAttacks >= 2, "Test wymaga co najmniej dwoch ciosow wroga")
        assertEquals(
            enemyAttacks - 1,
            hitsOnHero.size,
            "Dokladnie jeden cios powinien zniknac - pierwszy"
        )
    }

    @Test
    fun `tarcza odbija polowe obrazen w napastnika`() {
        val hero = fighter(
            id = "hero",
            team = Team.Players,
            hp = 500,
            attack = 1,
            speed = 1f,
            effects = listOf(ReflectDamageEffect(percent = 0.5f))
        )
        val enemy = fighter("e1", Team.Enemies, hp = 500, attack = 20, speed = 100f)

        val events = engine().simulate(listOf(hero, enemy)).events
        val reflected = events
            .filterIsInstance<CombatEvent.DamageDealt>()
            .filter { it.tag == DamageTag.Reflect }

        assertTrue(reflected.isNotEmpty(), "Odbicie powinno zadac obrazenia")
        assertEquals("hero", reflected.first().sourceId)
        assertEquals("e1", reflected.first().targetId)
        assertEquals(10, reflected.first().amount, "Polowa z 20 obrazen")
    }

    @Test
    fun `dwie odbijajace tarcze nie zapetlaja walki`() {
        val hero = fighter(
            id = "hero",
            team = Team.Players,
            hp = 200,
            attack = 15,
            speed = 50f,
            effects = listOf(ReflectDamageEffect(percent = 0.5f))
        )
        val enemy = fighter(
            id = "e1",
            team = Team.Enemies,
            hp = 200,
            attack = 15,
            speed = 10f,
            effects = listOf(ReflectDamageEffect(percent = 0.5f))
        )

        // Gdyby odbicie odbijalo odbicie, ta symulacja nigdy by sie nie skonczyla.
        // Wynik moze byc dowolny (lacznie z obopolna smiercia - kolce dzialaja tez z grobu);
        // liczy sie to, ze walka w ogole dobiega konca.
        val result = engine().simulate(listOf(hero, enemy))
        assertTrue(result.finalState.isOver, "Walka musi sie zakonczyc, a nie zapetlic")
    }

    @Test
    fun `kradziez zycia leczy napastnika`() {
        val hero = Combatant(
            id = "hero",
            name = "hero",
            team = Team.Players,
            baseStats = stats(hp = 100, attack = 20, speed = 100f),
            hp = 50,
            effects = listOf(LifestealEffect(percent = 0.5f))
        )
        val enemy = fighter("e1", Team.Enemies, hp = 500, attack = 1, speed = 1f)

        val healed = engine().simulate(listOf(hero, enemy)).events
            .filterIsInstance<CombatEvent.Healed>()
            .filter { it.targetId == "hero" }

        assertTrue(healed.isNotEmpty(), "Kradziez zycia powinna wyleczyc bohatera")
        assertEquals(10, healed.first().amount, "50% z 20 zadanych obrazen")
    }

    @Test
    fun `trucizna omija pancerz`() {
        val hero = fighter(
            id = "hero",
            team = Team.Players,
            hp = 500,
            attack = 10,
            speed = 100f,
            effects = listOf(PoisonOnHitEffect(potency = 6f, turns = 5, chance = 1f))
        )
        // Pancerz wroga zeruje zwykly cios (zostaje minimalne 1 HP), ale trucizny nie zatrzyma.
        val enemy = fighter("e1", Team.Enemies, hp = 500, attack = 1, speed = 1f, defense = 100f)

        val events = engine().simulate(listOf(hero, enemy)).events
        val poison = events
            .filterIsInstance<CombatEvent.DamageDealt>()
            .filter { it.tag == DamageTag.Poison }
        val normalHits = events
            .filterIsInstance<CombatEvent.DamageDealt>()
            .filter { it.tag == DamageTag.Attack && it.targetId == "e1" }

        assertTrue(poison.isNotEmpty(), "Trucizna powinna tykac co ture")
        assertEquals(6, poison.first().amount, "Trucizna ignoruje obrone")
        assertEquals(1, normalHits.first().amount, "Cios odbil sie od pancerza do minimum")
    }

    @Test
    fun `kostur leczy rannych sojusznikow`() {
        val healer = fighter(
            id = "healer",
            team = Team.Players,
            hp = 200,
            attack = 5,
            speed = 100f,
            effects = listOf(HealAlliesEffect(targets = 2, amount = 20f, everyTurns = 2))
        )
        val wounded = Combatant(
            id = "ally",
            name = "ally",
            team = Team.Players,
            baseStats = stats(hp = 100, attack = 5, speed = 50f),
            hp = 20
        )
        val enemy = fighter("e1", Team.Enemies, hp = 400, attack = 5, speed = 1f)

        val healed = engine().simulate(listOf(healer, wounded, enemy)).events
            .filterIsInstance<CombatEvent.Healed>()
            .filter { it.targetId == "ally" }

        assertTrue(healed.isNotEmpty(), "Ranny sojusznik powinien zostac wyleczony")
        assertEquals(20, healed.first().amount)
    }

    @Test
    fun `aura wzmacnia sojusznika`() {
        val bannerman = fighter(
            id = "banner",
            team = Team.Players,
            hp = 200,
            attack = 1,
            speed = 1f,
            effects = listOf(
                AllyAuraEffect(
                    id = "aura_valor",
                    description = "+10 ataku",
                    bonus = Stats.of(StatType.Attack to 10f)
                )
            )
        )
        val ally = fighter("ally", Team.Players, hp = 200, attack = 10, speed = 100f)
        val enemy = fighter("e1", Team.Enemies, hp = 500, attack = 1, speed = 50f)

        val allyHit = engine().simulate(listOf(bannerman, ally, enemy)).events
            .filterIsInstance<CombatEvent.DamageDealt>()
            .first { it.sourceId == "ally" }

        assertEquals(20, allyHit.amount, "10 wlasnego ataku + 10 z aury sojusznika")
    }

    @Test
    fun `dodatkowy atak po zabiciu nie zapetla walki`() {
        val hero = fighter(
            id = "hero",
            team = Team.Players,
            hp = 500,
            attack = 999,
            speed = 100f,
            effects = listOf(ExtraAttackOnKillEffect())
        )
        val enemies = (1..5).map { fighter("e$it", Team.Enemies, hp = 10, attack = 1, speed = 1f) }

        val result = engine().simulate(listOf(hero) + enemies)

        assertEquals(Team.Players, result.winner)
        assertTrue(
            result.events.filterIsInstance<CombatEvent.Killed>().size == 5,
            "Kazdy wrog powinien zginac dokladnie raz"
        )
    }

    @Test
    fun `ten sam seed daje ta sama walke`() {
        fun run() = BattleEngine(random = Random(99L)).simulate(
            listOf(
                fighter("hero", Team.Players, hp = 120, attack = 15, speed = 10f),
                fighter("e1", Team.Enemies, hp = 90, attack = 12, speed = 9f),
                fighter("e2", Team.Enemies, hp = 90, attack = 12, speed = 8f)
            )
        ).events

        assertEquals(run(), run(), "Walka musi byc odtwarzalna z seeda - inaczej MMO nie zweryfikuje wyniku")
    }
}

private typealias BattleResultEvents = List<CombatEvent>
