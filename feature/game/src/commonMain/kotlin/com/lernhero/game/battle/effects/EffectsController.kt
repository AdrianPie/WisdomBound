package com.lernhero.game.battle.effects

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.geometry.Offset
import kotlinx.coroutines.delay

/** Jedna zywa instancja efektu w trakcie odtwarzania - [from]/[to] sa juz w wspolrzednych root. */
data class ActiveEffect(
    val instanceId: Long,
    val spec: EffectSpec,
    val from: Offset,
    val to: Offset
)

/**
 * Warstwa efektow calkowicie oddzielona od postaci: trzyma liste zywych efektow, kazdy sam sie
 * usuwa po uplywie [EffectSpec.durationMs]. Postac/monster o niej nie wie - wywoluje sie ja z
 * zewnatrz (patrz sekwencer w `BattleField.kt`), dokladnie w duchu `playHitEffect(target)` /
 * `playProjectile(attacker, target)` z brief'u.
 *
 * Nowy efekt = nowy [EffectSpec] w [EffectCatalog] + wywolanie [playAt]/[playTravel] - zero
 * nowego kodu Compose na kazdy kolejny PNG.
 */
class EffectsController {
    private var nextInstanceId = 0L
    val active = mutableStateListOf<ActiveEffect>()

    /** Efekt statyczny (STATIC_FADE) lub zapetlony (LOOP_STATUS) w jednym miejscu. */
    suspend fun playAt(position: Offset, spec: EffectSpec) {
        play(position, position, spec)
    }

    /** Efekt lecacy (TRAVEL) od [from] do [to] - np. pocisk atakujacego do celu. */
    suspend fun playTravel(from: Offset, to: Offset, spec: EffectSpec) {
        play(from, to, spec)
    }

    private suspend fun play(from: Offset, to: Offset, spec: EffectSpec) {
        val id = nextInstanceId++
        active.add(ActiveEffect(id, spec, from, to))
        delay(spec.durationMs.toLong())
        active.removeAll { it.instanceId == id }

        // Lancuch efektow - np. wybuch dokladnie tam, gdzie dolecial pocisk, bez osobnej logiki
        // sekwencji na zewnatrz (patrz EffectSpec.followUpAtTarget).
        spec.followUpAtTarget?.let { followUp -> playAt(to, followUp) }
    }
}
