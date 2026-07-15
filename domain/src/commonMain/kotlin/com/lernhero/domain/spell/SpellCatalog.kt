package com.lernhero.domain.spell

import com.lernhero.domain.combat.CombatAction
import com.lernhero.domain.combat.DamageTag
import com.lernhero.domain.combat.EffectContext
import com.lernhero.domain.combat.SpellCast
import com.lernhero.domain.combat.StatType
import com.lernhero.domain.combat.StatusKind
import com.lernhero.domain.common.Rarity

/**
 * Rejestr wszystkich zaklec w grze. Jak katalog przedmiotow: nowy spell to jeden wpis zlozony
 * z istniejacych akcji bojowych, a nie linijka w silniku.
 *
 * Obrazenia i leczenie skaluja sie z ataku rzucajacego przez [StatType.SpellPower], wiec ten sam
 * spell rosnie w sile razem z postacia bez recznego przeliczania.
 */
object SpellCatalog {

    /** Mnoznik sily: 1.0 bazowo, wiecej z przedmiotow/perkow podnoszacych SpellPower. */
    private fun EffectContext.power(): Float = 1f + ownerStat(StatType.SpellPower)

    private fun EffectContext.attack(): Float = ownerStat(StatType.Attack).coerceAtLeast(1f)

    val FIREBALL = Spell(
        id = "fireball",
        name = "Kula ognia",
        description = "Ciska ognista kula w losowego wroga i podpala go na 3 tury.",
        rarity = Rarity.Uncommon,
        iconKey = "fireball",
        cost = 180,
        cast = SpellCast { ctx ->
            val target = ctx.enemies().randomOrNull() ?: return@SpellCast emptyList()
            listOf(
                CombatAction.DealDamage(
                    sourceId = ctx.ownerId,
                    targetIds = listOf(target.id),
                    amount = ctx.attack() * 1.7f * ctx.power(),
                    tag = DamageTag.Burn
                ),
                CombatAction.ApplyStatus(
                    sourceId = ctx.ownerId,
                    targetIds = listOf(target.id),
                    kind = StatusKind.Burn,
                    potency = 6f * ctx.power(),
                    turns = 3
                )
            )
        }
    )

    val FIREBOMB = Spell(
        id = "firebomb",
        name = "Bomba ognista",
        description = "Wybucha obszarowo, raniac ogniem wszystkich wrogow.",
        rarity = Rarity.Rare,
        iconKey = "firebomb",
        cost = 300,
        cast = SpellCast { ctx ->
            val enemies = ctx.enemies().map { it.id }
            if (enemies.isEmpty()) return@SpellCast emptyList()
            listOf(
                CombatAction.DealDamage(
                    sourceId = ctx.ownerId,
                    targetIds = enemies,
                    amount = ctx.attack() * 1.1f * ctx.power(),
                    tag = DamageTag.Burn
                )
            )
        }
    )

    val HEAL = Spell(
        id = "heal",
        name = "Leczenie",
        description = "Przywraca zdrowie najbardziej poobijanemu sojusznikowi.",
        rarity = Rarity.Uncommon,
        iconKey = "heal",
        cost = 200,
        cast = SpellCast { ctx ->
            val ally = ctx.woundedAllies(1).firstOrNull { it.hp < it.maxHp } ?: return@SpellCast emptyList()
            listOf(
                CombatAction.Heal(
                    sourceId = ctx.ownerId,
                    targetIds = listOf(ally.id),
                    amount = ctx.attack() * 1.6f * ctx.power()
                )
            )
        }
    )

    val POISON_DART = Spell(
        id = "poison_dart",
        name = "Zatruta strzalka",
        description = "Trafia wroga i zatruwa go na 5 tur - trucizna ignoruje pancerz.",
        rarity = Rarity.Uncommon,
        iconKey = "poison_dart",
        cost = 160,
        cast = SpellCast { ctx ->
            val target = ctx.enemies().randomOrNull() ?: return@SpellCast emptyList()
            listOf(
                CombatAction.DealDamage(
                    sourceId = ctx.ownerId,
                    targetIds = listOf(target.id),
                    amount = ctx.attack() * 0.8f * ctx.power(),
                    tag = DamageTag.Skill
                ),
                CombatAction.ApplyStatus(
                    sourceId = ctx.ownerId,
                    targetIds = listOf(target.id),
                    kind = StatusKind.Poison,
                    potency = 5f * ctx.power(),
                    turns = 5
                )
            )
        }
    )

    val FROST_SHARD = Spell(
        id = "frost_shard",
        name = "Odlamek lodu",
        description = "Rani i zamraza wroga na jedna ture - zamrozony traci kolejke.",
        rarity = Rarity.Rare,
        iconKey = "frost_shard",
        cost = 280,
        cast = SpellCast { ctx ->
            val target = ctx.enemies().randomOrNull() ?: return@SpellCast emptyList()
            listOf(
                CombatAction.DealDamage(
                    sourceId = ctx.ownerId,
                    targetIds = listOf(target.id),
                    amount = ctx.attack() * 1.2f * ctx.power(),
                    tag = DamageTag.Skill
                ),
                CombatAction.ApplyStatus(
                    sourceId = ctx.ownerId,
                    targetIds = listOf(target.id),
                    kind = StatusKind.Freeze,
                    potency = 0f,
                    turns = 1
                )
            )
        }
    )

    val all: List<Spell> = listOf(FIREBALL, FIREBOMB, HEAL, POISON_DART, FROST_SHARD)

    fun byId(id: String): Spell? = all.firstOrNull { it.id == id }
}
