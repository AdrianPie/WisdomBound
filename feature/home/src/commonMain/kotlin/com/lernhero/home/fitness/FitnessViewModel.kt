package com.lernhero.home.fitness

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lernhero.domain.fitness.FitnessAvailability
import com.lernhero.domain.fitness.FitnessGoal
import com.lernhero.domain.fitness.FitnessSnapshot
import com.lernhero.domain.fitness.StepChest
import com.lernhero.domain.run.ChestWheel
import com.lernhero.domain.repository.PlayerRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.random.Random
import kotlin.time.Clock

data class FitnessUiState(
    val loading: Boolean = true,
    /** Czy i dlaczego platforma (nie) dostarcza danych - np. brak/za stary Health Connect. */
    val availability: FitnessAvailability = FitnessAvailability.Unsupported,
    val permissionGranted: Boolean = false,
    val snapshot: FitnessSnapshot = FitnessSnapshot.empty(),
    val money: Int = 0,
    /** Id celu -> ostatni dzien odebrania. Z tego samego zrodla co [com.lernhero.domain.player.Player.fitnessClaims]. */
    val fitnessClaims: Map<String, String> = emptyMap(),
    val message: String? = null,
    /**
     * Wylosowane pole kola czekajace na pokazanie graczowi (nazwa `ChestSlot`). Ustawia sie DOPIERO
     * po tym, jak repozytorium potwierdzi zapis dziennego odbioru - patrz [openStepChest].
     */
    val pendingChestSlot: String? = null
) {
    val available: Boolean get() = availability == FitnessAvailability.Available
    fun claimedToday(goal: FitnessGoal): Boolean = fitnessClaims[goal.id] == snapshot.dateKey

    /**
     * Skrzynia za kroki juz dzis otwarta - ten sam mechanizm co przy celach dziennych. Pusty
     * [FitnessSnapshot.dateKey] (brak danych z platformy) NIE moze tu pasowac do niczego: inaczej
     * zapis pod pustym kluczem zamykalby skrzynie na stale.
     */
    val stepChestOpenedToday: Boolean
        get() = snapshot.dateKey.isNotBlank() && fitnessClaims[StepChest.CLAIM_ID] == snapshot.dateKey

    val stepChestUnlocked: Boolean get() = StepChest.isUnlocked(snapshot)
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

    /**
     * Otwiera dzienna skrzynie za kroki. Kolejnosc jest tu istotna: NAJPIERW zapisujemy odbior
     * (repozytorium odmawia drugiego tego samego dnia i wtedy zwraca null), a dopiero potem
     * losujemy nagrode. Odwrotnie - losowanie przed zapisem - dawaloby graczowi darmowe powtorki
     * przy kazdym bledzie zapisu.
     *
     * Sama nagroda nie jest przyznawana tutaj: pole kola idzie do [FitnessUiState.pendingChestSlot],
     * ekran odtwarza animacje kola i dopiero jego "Odbierz" przyznaje lup (ChestPrize.grant).
     */
    fun openStepChest() {
        val state = _uiState.value
        if (!state.stepChestUnlocked || state.stepChestOpenedToday || state.pendingChestSlot != null) return
        // Bez daty nie ma czym oznaczyc odbioru - lepiej nie otworzyc, niz zapisac pusty klucz.
        if (state.snapshot.dateKey.isBlank()) return

        viewModelScope.launch(Dispatchers.IO) {
            val updated = playerRepository.claimDailyGoalReward(
                goalId = StepChest.CLAIM_ID,
                dateKey = state.snapshot.dateKey,
                // Zloto (albo cokolwiek innego) daje dopiero wylosowane pole kola - ten zapis
                // sluzy wylacznie za znacznik "na dzis juz otwarte".
                reward = 0
            )
            _uiState.update { current ->
                if (updated == null) {
                    current.copy(message = "Today's chest is already open.")
                } else {
                    val slot = ChestWheel.roll(Random.Default, StepChest.wheelLevel(current.snapshot.steps))
                    current.copy(
                        fitnessClaims = updated.dailyGoalClaims,
                        pendingChestSlot = slot.name
                    )
                }
            }
        }
    }

    /** Lup odebrany (albo animacja zamknieta) - kolo znika z ekranu. */
    fun consumeStepChest() {
        _uiState.update { it.copy(pendingChestSlot = null) }
    }

    /**
     * DEV: dosypuje kroki bez dotykania Health Connect/HealthKit. Emulatory zwykle nie maja
     * zadnych danych o krokach (ani samego Health Connect skonfigurowanego z danymi) i nie da sie
     * ich tam "nachodzic" - to jedyny sposob, zeby przetestowac skrzynie/cele bez fizycznego
     * telefonu. Dziala natychmiast (in-memory), nic nie zapisuje do platformy ani serwera - po
     * [refresh] wraca prawdziwy (czyli zazwyczaj zerowy/niepodlaczony) stan.
     *
     * Musi dzialac TAKZE gdy zgoda nie jest jeszcze przyznana (ekran pokazuje wtedy tylko
     * "Connect") - inaczej na emulatorze, ktory czesto w ogole nie potrafi przejsc realnego flow
     * zgody, przycisk devowy bylby ukryty za tym samym murem, ktory ma pozwalac obejsc.
     */
    fun addDebugSteps(amount: Int = 1000) {
        _uiState.update { state ->
            val dateKey = state.snapshot.dateKey.ifBlank { devDateKey() }
            state.copy(
                permissionGranted = true,
                availability = FitnessAvailability.Available,
                snapshot = state.snapshot.copy(
                    steps = state.snapshot.steps + amount,
                    dateKey = dateKey,
                    availability = FitnessAvailability.Available,
                    permissionGranted = true
                )
            )
        }
    }

    /**
     * Zastepczy "dzisiejszy" klucz dla [addDebugSteps], gdy platforma jeszcze zadnego nie podala -
     * common nie ma tu java.time/kotlinx-datetime, wiec licznik dni od epoki (Clock, ktory juz
     * jest w projekcie) wystarcza: rozny klucz kazdego realnego dnia, bez formatowania kalendarza.
     */
    private fun devDateKey(): String = "dev-${Clock.System.now().toEpochMilliseconds() / 86_400_000L}"

    fun clearMessage() {
        _uiState.update { it.copy(message = null) }
    }

    /** Otwiera sklep z Health Connect - do zainstalowania albo zaktualizowania. */
    fun openInstallPage() {
        FitnessTracker.openInstallPage()
    }
}
