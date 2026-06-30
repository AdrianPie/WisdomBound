package com.lernhero.domain.game

class BattlePlaybackEngine {

    fun createScript(
        wave: Int,
        fighters: List<BattleFighter>
    ): BattlePlaybackScript {
        val mutableFighters = fighters
            .map { fighter -> fighter.id to fighter.copy() }
            .toMap()
            .toMutableMap()

        val steps = mutableListOf<BattlePlaybackStep>()
        var side = BattleSideId.Players
        var stepNumber = 1

        while (
            mutableFighters.values.any { it.isPlayer && it.hp > 0 } &&
            mutableFighters.values.any { !it.isPlayer && it.hp > 0 }
        ) {
            val attackers = mutableFighters.values
                .filter { it.isPlayer == (side == BattleSideId.Players) && it.hp > 0 }
                .sortedBy { it.id }

            val targets = mutableFighters.values
                .filter { it.isPlayer != (side == BattleSideId.Players) && it.hp > 0 }
                .sortedBy { it.id }

            val attacker = attackers.randomOrNull() ?: break
            val target = targets.randomOrNull() ?: break
            val damage = attacker.attack.coerceAtLeast(1)
            val targetHpAfter = (target.hp - damage).coerceAtLeast(0)

            mutableFighters[target.id] = target.copy(hp = targetHpAfter)

            steps += BattlePlaybackStep.Attack(
                id = "wave_${wave}_step_$stepNumber",
                attackerId = attacker.id,
                attackerName = attacker.name,
                attackerIsPlayer = attacker.isPlayer,
                targetId = target.id,
                targetName = target.name,
                damage = damage,
                targetHpAfter = targetHpAfter,
                targetDefeated = targetHpAfter <= 0
            )

            stepNumber += 1
            side = if (side == BattleSideId.Players) {
                BattleSideId.Enemies
            } else {
                BattleSideId.Players
            }
        }

        val terminalStep = if (mutableFighters.values.any { it.isPlayer && it.hp > 0 }) {
            BattlePlaybackStep.WaveCleared(
                id = "wave_${wave}_cleared",
                wave = wave
            )
        } else {
            BattlePlaybackStep.PartyDefeated(
                id = "wave_${wave}_defeat",
                wave = wave
            )
        }

        return BattlePlaybackScript(
            id = "wave_$wave",
            wave = wave,
            steps = steps + terminalStep
        )
    }
}

data class BattleFighter(
    val id: String,
    val name: String,
    val isPlayer: Boolean,
    val hp: Int,
    val maxHp: Int,
    val attack: Int
)

data class BattlePlaybackScript(
    val id: String,
    val wave: Int,
    val steps: List<BattlePlaybackStep>
)

sealed class BattlePlaybackStep {
    abstract val id: String

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
