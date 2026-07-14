package com.lernhero.game.battle

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.lernhero.game.FightScreenOrientation
import com.lernhero.game.battle.component.BattleField
import com.lernhero.game.battle.component.DefeatDialog
import com.lernhero.game.battle.component.QuizDialog
import com.lernhero.game.battle.component.UpgradeDialog
import kotlinx.coroutines.delay
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun BattleScreen() {
    FightScreenOrientation()

    val viewModel = koinViewModel<BattleViewModel>()
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(
        state.phase,
        state.playback?.currentStepIndex,
        state.playback?.currentStep?.id,
        state.isBattleBusy()
    ) {
        if (state.phase == GamePhase.Battle && !state.isBattleBusy()) {
            delay(BattleSpeed.scale(125L))
            viewModel.playNextBattleStep()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        BattleField(
            state = state,
            onMoveToAttackFinished = viewModel::onMoveToAttackFinished,
            onAttackAnimationFinished = viewModel::onAttackAnimationFinished,
            onMoveBackFinished = viewModel::onMoveBackFinished
        )

        when (state.phase) {
            GamePhase.Battle -> Unit

            GamePhase.Quiz -> QuizDialog(
                state = state,
                onAnswer = viewModel::answerQuiz,
                onContinue = viewModel::continueQuiz
            )

            GamePhase.Upgrade -> UpgradeDialog(
                state = state,
                onBuyUpgrade = viewModel::buyUpgrade,
                onFinish = viewModel::finishUpgradePhase
            )

            GamePhase.Defeat -> DefeatDialog(
                state = state,
                onRestart = viewModel::restartRun
            )
        }
    }
}
