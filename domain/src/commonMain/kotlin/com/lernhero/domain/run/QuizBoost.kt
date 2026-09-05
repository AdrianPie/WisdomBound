package com.lernhero.domain.run

// Zbudowane, jeszcze nie podpiete: nic obecnie nie wola QuizBoost.roll() ani nie czyta wyniku w
// BattleEngine/BattleViewModel. Zostawione swiadomie jako gotowy element pod przyszla integracje
// wyniku quizu z walka - nie kasowac jako "martwy kod".

import com.lernhero.domain.combat.StatType
import com.lernhero.domain.combat.Stats
import kotlin.random.Random

/**
 * Jak poszedl quiz przed walka. Zero trafien nie daje nic - w koncu to za wiedze placi sie
 * boostem, a nie za sama probe. Czesciowy wynik daje mniejszy boost tylko na te walke; komplet
 * daje pelny boost na walke I odrobine tego samego na cala reszte runu.
 */
enum class QuizPerformance {
    Poor,
    Fair,
    Good;

    companion object {
        fun of(correct: Int, total: Int): QuizPerformance = when {
            total <= 0 || correct <= 0 -> Poor
            correct >= total -> Good
            else -> Fair
        }
    }
}

/**
 * Jeden stopien boosta: [attackBonus]/[hpBonus] pokazuja sie wprost na paskach postaci (jak w UI
 * dotad), a [stats] to surowe bonusy (kryt, unik, kradziez zycia...) idace prosto do silnika.
 */
data class QuizBoostEffect(
    val attackBonus: Int = 0,
    val hpBonus: Int = 0,
    val stats: Stats = Stats.Empty,
    val description: String
)

/**
 * Wpis z puli boostow bojowych losowanych po quizie przed kazda walka. [fair] dziala tylko na
 * biezaca walke (czesciowo trafiony quiz), [good] dziala na walke i - przez [goodRunBonus] -
 * zostawia mniejszy slad na reszcie zejscia (komplet trafien).
 */
data class QuizBoost(
    val id: String,
    val title: String,
    val fair: QuizBoostEffect,
    val good: QuizBoostEffect,
    val goodRunBonus: (RunModifiers) -> RunModifiers
) {
    companion object {

        private fun attack(id: String, title: String, fairAtk: Int, goodAtk: Int, runAtk: Int) = QuizBoost(
            id = id,
            title = title,
            fair = QuizBoostEffect(attackBonus = fairAtk, description = "+$fairAtk attack for this fight"),
            good = QuizBoostEffect(attackBonus = goodAtk, description = "+$goodAtk attack for this fight"),
            goodRunBonus = { it.copy(bonusAttack = it.bonusAttack + runAtk) }
        )

        private fun health(id: String, title: String, fairHp: Int, goodHp: Int, runHp: Int) = QuizBoost(
            id = id,
            title = title,
            fair = QuizBoostEffect(hpBonus = fairHp, description = "+$fairHp HP for this fight"),
            good = QuizBoostEffect(hpBonus = goodHp, description = "+$goodHp HP for this fight"),
            goodRunBonus = { it.copy(bonusMaxHp = it.bonusMaxHp + runHp) }
        )

        private fun stat(
            id: String,
            title: String,
            statType: StatType,
            fairValue: Float,
            goodValue: Float,
            runValue: Float,
            describe: (Float) -> String
        ) = QuizBoost(
            id = id,
            title = title,
            fair = QuizBoostEffect(stats = Stats.of(statType to fairValue), description = describe(fairValue)),
            good = QuizBoostEffect(stats = Stats.of(statType to goodValue), description = describe(goodValue)),
            goodRunBonus = { it.copy(bonusStats = it.bonusStats + Stats.of(statType to runValue)) }
        )

        private fun percent(value: Float): String = "${(value * 100).toInt()}%"

        /** Duza pula roznych boostow - losuje sie jeden na kazda walke. */
        val ALL: List<QuizBoost> = listOf(
            attack(
                id = "sharp_mind",
                title = "Bystry umysl",
                fairAtk = 5,
                goodAtk = 10,
                runAtk = 2
            ),
            health(
                id = "iron_body",
                title = "Zelazna kondycja",
                fairHp = 20,
                goodHp = 40,
                runHp = 8
            ),
            stat(
                id = "hawk_eye",
                title = "Sokoli wzrok",
                statType = StatType.CritChance,
                fairValue = 0.06f,
                goodValue = 0.12f,
                runValue = 0.03f,
                describe = { "+${percent(it)} crit chance for this fight" }
            ),
            stat(
                id = "crushing_blow",
                title = "Crushing Blow",
                statType = StatType.CritDamage,
                fairValue = 0.15f,
                goodValue = 0.35f,
                runValue = 0.08f,
                describe = { "+${percent(it)} crit damage for this fight" }
            ),
            stat(
                id = "bloodlust",
                title = "Glod krwi",
                statType = StatType.Lifesteal,
                fairValue = 0.06f,
                goodValue = 0.12f,
                runValue = 0.03f,
                describe = { "+${percent(it)} lifesteal for this fight" }
            ),
            stat(
                id = "nimble_steps",
                title = "Zwinne kroki",
                statType = StatType.Dodge,
                fairValue = 0.05f,
                goodValue = 0.1f,
                runValue = 0.02f,
                describe = { "+${percent(it)} dodge chance for this fight" }
            ),
            stat(
                id = "stone_skin",
                title = "Kamienna skora",
                statType = StatType.DamageReduction,
                fairValue = 0.05f,
                goodValue = 0.1f,
                runValue = 0.02f,
                describe = { "+${percent(it)} damage reduction for this fight" }
            ),
            stat(
                id = "arcane_spark",
                title = "Iskra arkanow",
                statType = StatType.SpellPower,
                fairValue = 0.15f,
                goodValue = 0.3f,
                runValue = 0.08f,
                describe = { "+${percent(it)} spell power for this fight" }
            ),
            stat(
                id = "quickened_reflex",
                title = "Przyspieszony refleks",
                statType = StatType.Speed,
                fairValue = 2f,
                goodValue = 4f,
                runValue = 1f,
                describe = { "+${it.toInt()} speed for this fight" }
            ),
            // Boosty zlozone - dwie mniejsze statystyki naraz, dla odmiany od pojedynczych.
            QuizBoost(
                id = "battle_trance",
                title = "Bojowy trans",
                fair = QuizBoostEffect(
                    attackBonus = 3,
                    stats = Stats.of(StatType.CritChance to 0.04f),
                    description = "+3 attack and +4% crit chance for this fight"
                ),
                good = QuizBoostEffect(
                    attackBonus = 6,
                    stats = Stats.of(StatType.CritChance to 0.08f),
                    description = "+6 attack and +8% crit chance for this fight"
                ),
                goodRunBonus = {
                    it.copy(
                        bonusAttack = it.bonusAttack + 1,
                        bonusStats = it.bonusStats + Stats.of(StatType.CritChance to 0.02f)
                    )
                }
            ),
            QuizBoost(
                id = "stubborn_warrior",
                title = "Uparty wojownik",
                fair = QuizBoostEffect(
                    hpBonus = 12,
                    stats = Stats.of(StatType.DamageReduction to 0.04f),
                    description = "+12 HP and +4% damage reduction for this fight"
                ),
                good = QuizBoostEffect(
                    hpBonus = 25,
                    stats = Stats.of(StatType.DamageReduction to 0.08f),
                    description = "+25 HP and +8% damage reduction for this fight"
                ),
                goodRunBonus = {
                    it.copy(
                        bonusMaxHp = it.bonusMaxHp + 5,
                        bonusStats = it.bonusStats + Stats.of(StatType.DamageReduction to 0.02f)
                    )
                }
            ),
            QuizBoost(
                id = "rune_master",
                title = "Mistrz run",
                fair = QuizBoostEffect(
                    stats = Stats.of(StatType.SpellPower to 0.1f, StatType.CritDamage to 0.1f),
                    description = "+10% spell power and +10% crit damage for this fight"
                ),
                good = QuizBoostEffect(
                    stats = Stats.of(StatType.SpellPower to 0.22f, StatType.CritDamage to 0.22f),
                    description = "+22% spell power and +22% crit damage for this fight"
                ),
                goodRunBonus = {
                    it.copy(
                        bonusSpellPower = it.bonusSpellPower + 0.05f,
                        bonusStats = it.bonusStats + Stats.of(StatType.CritDamage to 0.05f)
                    )
                }
            )
        )

        /** Losuje jeden boost z puli - kazda walka dostaje inny zestaw mozliwosci. */
        fun roll(random: Random): QuizBoost = ALL[random.nextInt(ALL.size)]
    }
}
