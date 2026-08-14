package com.lernhero.game.battle

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.lernhero.game.FightScreenOrientation
import com.lernhero.game.battle.component.BattleField
import com.lernhero.game.battle.component.BattleResultDialog
import com.lernhero.game.battle.component.LiveQuizBar
import com.lernhero.game.battle.component.ResolvingStrip
import org.koin.compose.viewmodel.koinViewModel

/**
 * Ekran walki: fale wrogow nadciagaja bez konca, kazda tura to jedno pytanie quizu. Koniec przychodzi
 * tylko przez porazke albo kapitulacje - stad jedna droga powrotu, [onLeaveBattle].
 */
@Composable
fun BattleScreen(
    onLeaveBattle: () -> Unit = {}
) {
    FightScreenOrientation()

    val viewModel = koinViewModel<BattleViewModel>()
    val state by viewModel.uiState.collectAsState()

    // Ekran walki oddycha razem z tura: gdy czeka na odpowiedz, dol (quiz) sie rozszerza, a gora
    // (pole bitwy) kurczy; gdy druzyny wymieniaja ciosy, jest odwrotnie - widac cala walke.
    val battleWeight by animateFloatAsState(
        targetValue = if (state.turnPhase == TurnPhase.Question) 0.32f else 0.78f,
        animationSpec = tween(durationMillis = 400)
    )

    Column(modifier = Modifier.fillMaxSize()) {
        BattleField(
            state = state,
            onAttackFinished = viewModel::onAttackFinished,
            onHitRecovered = viewModel::onHitRecovered,
            onEffectShown = viewModel::consumeFloatingEffect,
            onCapitulate = viewModel::capitulate,
            modifier = Modifier.weight(if (state.phase == GamePhase.Battle) battleWeight else 1f)
        )

        if (state.phase == GamePhase.Battle) {
            if (state.turnPhase == TurnPhase.Question) {
                LiveQuizBar(
                    state = state,
                    onAnswer = viewModel::answerQuiz,
                    onContinue = viewModel::continueQuiz,
                    modifier = Modifier.weight(1f - battleWeight)
                )
            } else {
                ResolvingStrip(
                    state = state,
                    modifier = Modifier.weight(1f - battleWeight)
                )
            }
        }
    }

    if (state.phase == GamePhase.Defeat || state.phase == GamePhase.Victory) {
        BattleResultDialog(
            state = state,
            onLeave = onLeaveBattle
        )
    }
}
