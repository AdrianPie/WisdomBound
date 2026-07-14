package com.lernhero.domain.game

import com.lernhero.domain.combat.CombatEvent
import com.lernhero.domain.combat.Combatant
import com.lernhero.domain.combat.DamageTag
import com.lernhero.domain.combat.StatType
import com.lernhero.domain.combat.Stats
import com.lernhero.domain.combat.Team
import com.lernhero.domain.combat.engine.BattleEngine
import com.lernhero.domain.item.Loadout
import kotlin.random.Random

/**
 * Adapter miedzy silnikiem walki a warstwa odtwarzania.
 *
 * Walka jest symulowana z gory ([BattleEngine]), a UI przewija gotowy strumien zdarzen. Ten podzial
 * byl tu od poczatku - zmienilo sie tylko to, ze kroki nie wypadaja juz z prostej petli "kto kogo",
 * tylko z systemu efektow. Dzieki temu miecz tnacy trzech wrogow "po prostu dziala" w animacji.
 */
class BattlePlaybackEngine(
    private val random: Random = Random.Default
) {

    fun createScript(
        wave: Int,
        fighters: List<BattleFighter>
    ): BattlePlaybackScript {
        val result = BattleEngine(random = random).simulate(fighters.map { it.toCombatant() })

        val names = fighters.associate { it.id to it.name }
        val playerIds = fighters.filter { it.isPlayer }.map { it.id }.toSet()

        var stepNumber = 1
        val steps = mutableListOf<BattlePlaybackStep>()
        result.events.forEach { event ->
            val step = event.toStep(wave, stepNumber, names, playerIds) ?: return@forEach
            steps += step
            stepNumber += 1
        }

        val terminalStep = if (result.winner == Team.Players) {
            BattlePlaybackStep.WaveCleared(id = "wave_${wave}_cleared", wave = wave)
        } else {
            BattlePlaybackStep.PartyDefeated(id = "wave_${wave}_defeat", wave = wave)
        }

        return BattlePlaybackScript(
            id = "wave_$wave",
            wave = wave,
            steps = steps + terminalStep
        )
    }

    /**
     * Nie kazde zdarzenie da sie zanimowac. Zwykly cios zostaje [BattlePlaybackStep.Attack]
     * (postac podbiega, tnie, wraca), a to, co dzieje sie samo - trucizna, ogien, odbicie,
     * leczenie - trafia do [BattlePlaybackStep.Tick], ktory tylko poprawia paski HP.
     */
    private fun CombatEvent.toStep(
        wave: Int,
        stepNumber: Int,
        names: Map<String, String>,
        playerIds: Set<String>
    ): BattlePlaybackStep? = when (this) {
        is CombatEvent.DamageDealt -> {
            val targetName = names[targetId] ?: targetId

            if (tag == DamageTag.Attack) {
                BattlePlaybackStep.Attack(
                    id = "wave_${wave}_step_$stepNumber",
                    attackerId = sourceId,
                    attackerName = names[sourceId] ?: sourceId,
                    attackerIsPlayer = sourceId in playerIds,
                    targetId = targetId,
                    targetName = targetName,
                    damage = amount,
                    targetHpAfter = targetHpAfter,
                    targetDefeated = killed
                )
            } else {
                BattlePlaybackStep.Tick(
                    id = "wave_${wave}_step_$stepNumber",
                    targetId = targetId,
                    targetHpAfter = targetHpAfter,
                    targetDefeated = killed,
                    label = "$targetName: ${tag.label()} -$amount HP"
                )
            }
        }

        is CombatEvent.Healed -> BattlePlaybackStep.Tick(
            id = "wave_${wave}_step_$stepNumber",
            targetId = targetId,
            targetHpAfter = targetHpAfter,
            targetDefeated = false,
            label = "${names[targetId] ?: targetId}: +$amount HP"
        )

        else -> null
    }

    private fun DamageTag.label(): String = when (this) {
        DamageTag.Attack -> "cios"
        DamageTag.Burn -> "ogien"
        DamageTag.Poison -> "trucizna"
        DamageTag.Reflect -> "odbicie"
        DamageTag.Skill -> "efekt"
    }

    private fun BattleFighter.toCombatant(): Combatant = Combatant(
        id = id,
        name = name,
        team = if (isPlayer) Team.Players else Team.Enemies,
        baseStats = Stats.baseline(maxHp = maxHp, attack = attack),
        hp = hp,
        // Bonusy do HP i ataku sa juz wliczone w hp/attack przez warstwe UI, wiec tutaj je odsiewamy.
        // Inaczej ten sam helm dodawalby zdrowie dwa razy.
        itemStats = loadout.stats.without(StatType.MaxHp, StatType.Attack),
        effects = loadout.effects
    )
}

/**
 * Walczacy widziany przez warstwe UI. [loadout] to jedyny kanal, ktorym przedmioty wchodza do walki -
 * silnik nie zna innego zrodla zachowan.
 */
data class BattleFighter(
    val id: String,
    val name: String,
    val isPlayer: Boolean,
    val hp: Int,
    val maxHp: Int,
    val attack: Int,
    val loadout: Loadout = Loadout.Empty
)

data class BattlePlaybackScript(
    val id: String,
    val wave: Int,
    val steps: List<BattlePlaybackStep>
)

sealed class BattlePlaybackStep {
    abstract val id: String

    /** Cios z animacja: napastnik podbiega, uderza, wraca. */
    data class Attack(
        override val id: String,
        val attackerId: String,
        val attackerName: String,
        val attackerIsPlayer: Boolean,
        val targetId: String,
        val targetName: String,
        val damage: Int,
        val targetHpAfter: Int,
        val targetDefeated: Boolean
    ) : BattlePlaybackStep()

    /** Zmiana HP bez animacji ataku: trucizna, ogien, odbicie, leczenie. */
    data class Tick(
        override val id: String,
        val targetId: String,
        val targetHpAfter: Int,
        val targetDefeated: Boolean,
        val label: String
    ) : BattlePlaybackStep()

    data class WaveCleared(
        override val id: String,
        val wave: Int
    ) : BattlePlaybackStep()

    data class PartyDefeated(
        override val id: String,
        val wave: Int
    ) : BattlePlaybackStep()
}

enum class BattleSideId {
    Players,
    Enemies
}
