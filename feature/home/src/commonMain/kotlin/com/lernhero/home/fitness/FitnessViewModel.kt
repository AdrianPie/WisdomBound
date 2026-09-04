package com.lernhero.home.fitness

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lernhero.domain.fitness.FitnessAvailability
import com.lernhero.domain.fitness.FitnessGoal
import com.lernhero.domain.fitness.FitnessSnapshot
import com.lernhero.domain.repository.PlayerRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class FitnessUiState(
    val loading: Boolean = true,
    /** Czy i dlaczego platforma (nie) dostarcza danych - np. brak/za stary Health Connect. */
    val availability: FitnessAvailability = FitnessAvailability.Unsupported,
    val permissionGranted: Boolean = false,
    val snapshot: FitnessSnapshot = FitnessSnapshot.empty(),
    val money: Int = 0,
    /** Id celu -> ostatni dzien odebrania. Z tego samego zrodla co [com.lernhero.domain.player.Player.fitnessClaims]. */
    val fitnessClaims: Map<String, String> = emptyMap(),
    val message: String? = null
) {
    val available: Boolean get() = availability == FitnessAvailability.Available
    fun claimedToday(goal: FitnessGoal): Boolean = fitnessClaims[goal.id] == snapshot.dateKey
}

/**
 * Ekran "Trening": pokazuje dzisiejsza aktywnosc z Health Connect/HealthKit i pozwala odebrac
 * zloto za ukonczone cele. Kazdy cel placi sie najwyzej raz dziennie - [PlayerRepository]
 * pilnuje tego przez zapisana date, wiec dwa kliknieciapod rzad nie zdublują nagrody.
 */
class FitnessViewModel(
    private val playerRepository: PlayerRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(FitnessUiState())
    val uiState = _uiState.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch(Dispatchers.IO) {
            val result = runCatching {
                val availability = FitnessTracker.checkAvailability()
                val granted = availability == FitnessAvailability.Available && FitnessTracker.hasPermission()
                val snapshot = if (granted) FitnessTracker.today() else FitnessSnapshot.empty(availability = availability)
                Triple(availability, granted, snapshot)
            }
            val player = playerRepository.getPlayer()

            _uiState.update { state ->
                val (availability, granted, snapshot) = result.getOrElse {
                    // Nie chowamy bledu w cichej wiecznej "ladujemy" - widac dokladnie, co sie stalo.
                    return@update state.copy(
                        loading = false,
                        message = "Error reading activity: ${it.message ?: it::class.simpleName}"
                    )
                }
                state.copy(
                    loading = false,
                    availability = availability,
                    permissionGranted = granted,
                    snapshot = snapshot,
                    money = player?.money ?: state.money,
                    fitnessClaims = player?.dailyGoalClaims ?: state.fitnessClaims
                )
            }
        }
    }

    /** Wywolywane po powrocie z prosby o zgode (Health Connect / HealthKit). */
    fun onPermissionResult(granted: Boolean) {
        if (granted) {
            refresh()
        } else {
            _uiState.update { it.copy(message = "Without permission we can't see your steps or workouts.") }
        }
    }

    fun claim(goal: FitnessGoal) {
        val state = _uiState.value
        if (state.claimedToday(goal) || !goal.isComplete(state.snapshot)) return

        viewModelScope.launch(Dispatchers.IO) {
            val updated = playerRepository.claimDailyGoalReward(
                goalId = goal.id,
                dateKey = state.snapshot.dateKey,
                reward = goal.reward
            )
            _uiState.update { current ->
                if (updated == null) {
                    current.copy(message = "Couldn't claim the reward - try again.")
                } else {
                    current.copy(
                        money = updated.money,
                        fitnessClaims = updated.dailyGoalClaims,
                        message = "Claimed +${goal.reward} gold: ${goal.title}"
                    )
                }
            }
        }
    }

    fun clearMessage() {
        _uiState.update { it.copy(message = null) }
    }

    /** Otwiera sklep z Health Connect - do zainstalowania albo zaktualizowania. */
    fun openInstallPage() {
        FitnessTracker.openInstallPage()
    }
}
