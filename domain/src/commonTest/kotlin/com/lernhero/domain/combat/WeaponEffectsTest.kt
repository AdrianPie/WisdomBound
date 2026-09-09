package com.lernhero.domain.combat

import com.lernhero.domain.combat.effects.DoubleAttackChanceEffect
import com.lernhero.domain.combat.effects.ElementBaneEffect
import com.lernhero.domain.combat.effects.PoisonOnHitEffect
import com.lernhero.domain.combat.engine.BattleEngine
import com.lernhero.domain.monster.Element
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Dwa klocki dopisane pod nowy katalog przedmiotow: "szansa na drugi cios" i "bron na konkretny typ".
 *
 * Oba sa sprawdzane w PRAWDZIWEJ symulacji, a nie w izolacji - bo oba dzialaja przez potok
 * obrazen i to wlasnie tam moga sie popsuc (np. dodatkowy atak napedzany wlasna trucizna).
 */
class WeaponEffectsTest {

    private fun stats(hp: Int, attack: Int, speed: Float) = Stats.of(
        StatType.MaxHp to hp.toFloat(),
        StatType.Attack to attack.toFloat(),
        StatType.Defense to 0f,
        StatType.Speed to speed,
        // Krytyki i uniki wylaczone - testujemy efekty, nie rzuty koscmi.
        StatType.CritChance to 0f,
        StatType.CritDamage to 1.5f,
        StatType.Dodge to 0f,
        StatType.AttackTargets to 1f
    )

    private fun fighter(
        id: String,
        team: Team,
        hp: Int = 100,
        attack: Int = 10,
        speed: Float = 10f,
        element: Element? = null,
        effects: List<Effect> = emptyList()
    ) = Combatant(
        id = id,
        name = id,
        team = team,
        baseStats = stats(hp, attack, speed),
        hp = hp,
        effects = effects,
        element = element
    )

    private fun engine(seed: Long = 7L) = BattleEngine(random = Random(seed))

    private fun List<CombatEvent>.hitsBy(id: CombatantId) =
        filterIsInstance<CombatEvent.DamageDealt>()
            .filter { it.sourceId == id && it.tag == DamageTag.Attack }

    /**
     * Ciosy bohatera z PIERWSZEJ tury. Odcinamy na pierwszym uderzeniu przeciwnika, a nie na
     * zdarzeniu konca tury - te ostatnie nie trafiaja do strumienia wynikowego, wiec takeWhile
     * po nich nie odcinalo niczego i test liczyl cala walke.
     */
    private fun List<CombatEvent>.firstTurnHitsByHero() =
        takeWhile { !(it is CombatEvent.DamageDealt && it.sourceId == "e1") }.hitsBy("hero")

    /* --------------------------- Szansa na podwojny atak --------------------------- */

    @Test
    fun `pewny podwojny atak daje dwa trafienia na ture`() {
        val hero = fighter(
            id = "hero",
            team = Team.Players,
            hp = 900,
            attack = 5,
            speed = 100f,
            effects = listOf(DoubleAttackChanceEffect(chance = 1f))
        )
        val enemy = fighter("e1", Team.Enemies, hp = 900, attack = 1, speed = 1f)

        val result = engine().simulate(listOf(hero, enemy))
        val firstTurn = result.events.firstTurnHitsByHero()

        assertEquals(2, firstTurn.size, "Przy szansie 100% tura ma dac cios i dobicie")
    }

    @Test
    fun `zerowa szansa nie dodaje niczego`() {
        val hero = fighter(
            id = "hero",
            team = Team.Players,
            hp = 900,
            attack = 5,
            speed = 100f,
            effects = listOf(DoubleAttackChanceEffect(chance = 0f))
        )
        val enemy = fighter("e1", Team.Enemies, hp = 900, attack = 1, speed = 1f)

        val result = engine().simulate(listOf(hero, enemy))
        val firstTurn = result.events.firstTurnHitsByHero()

        assertEquals(1, firstTurn.size)
    }

    @Test
    fun `trucizna nie napedza dodatkowych atakow`() {
        // Bez wykluczenia tagow bron zaczelaby dobijac z wlasnych obrazen obszarowych, a lancuch
        // napedzalby sam siebie az do limitu glebokosci silnika. To wtedy nie jest "30% szansy",
        // tylko awaria.
        val hero = fighter(
            id = "hero",
            team = Team.Players,
            hp = 900,
            attack = 5,
            speed = 100f,
            effects = listOf(
                DoubleAttackChanceEffect(chance = 1f),
                PoisonOnHitEffect(potency = 6f, turns = 3)
            )
        )
        val enemy = fighter("e1", Team.Enemies, hp = 900, attack = 1, speed = 1f)

        val result = engine().simulate(listOf(hero, enemy))
        val firstTurn = result.events.firstTurnHitsByHero()

        assertEquals(2, firstTurn.size, "Trucizna dolozyla kolejne ciosy - lancuch sie zapetlil")
    }

    /* ------------------------------ Bron na dany typ ------------------------------- */

    @Test
    fun `bron na zywiol bije mocniej tylko w swoj cel`() {
        fun totalDamage(targetElement: Element?): Float {
            val hero = fighter(
                id = "hero",
                team = Team.Players,
                hp = 900,
                attack = 20,
                speed = 100f,
                effects = listOf(ElementBaneEffect(against = Element.Death, bonus = 0.5f))
            )
            val enemy = fighter(
                id = "e1",
                team = Team.Enemies,
                hp = 900,
                attack = 1,
                speed = 1f,
                element = targetElement
            )
            return engine().simulate(listOf(hero, enemy))
                .events
                .hitsBy("hero")
                .take(1)
                .sumOf { it.amount.toDouble() }
                .toFloat()
        }

        val againstDeath = totalDamage(Element.Death)
        val againstFire = totalDamage(Element.Fire)
        val againstNone = totalDamage(null)

        assertTrue(againstDeath > againstFire, "Premia nie zadzialala na wlasciwym typie")
        assertEquals(againstFire, againstNone, "Wrog bez zywiolu ma dostawac tyle co niepasujacy")
        assertEquals(1.5f, againstDeath / againstFire, 0.01f, "Premia ma wynosic dokladnie +50%")
    }

    @Test
    fun `bron na zywiol nie zmienia obrazen, gdy nikt nie ma typu`() {
        val plain = fighter("hero", Team.Players, hp = 900, attack = 20, speed = 100f)
        val armed = fighter(
            id = "hero",
            team = Team.Players,
            hp = 900,
            attack = 20,
            speed = 100f,
            effects = listOf(ElementBaneEffect(against = Element.Water, bonus = 0.5f))
        )
        val enemy = { fighter("e1", Team.Enemies, hp = 900, attack = 1, speed = 1f) }

        val plainHit = engine().simulate(listOf(plain, enemy())).events.hitsBy("hero").first().amount
        val armedHit = engine().simulate(listOf(armed, enemy())).events.hitsBy("hero").first().amount

        assertEquals(plainHit, armedHit, "Bron bez pasujacego celu ma byc zwyklym mieczem")
    }
}
