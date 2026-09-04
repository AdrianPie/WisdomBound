package com.lernhero.home.city

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lernhero.domain.repository.PlayerRepository
import com.lernhero.domain.town.ProductionBuilding
import com.lernhero.domain.town.ProductionMath
import com.lernhero.domain.town.ProductionState
import com.lernhero.domain.town.TownResources
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.time.Clock

/** Krotki komunikat wyskakujacy na ekranie ("+25% PRODUKCJI", "+50 drewna") - patrz TownToasts. */
data class TownToast(
    val id: Long,
    val text: String,
    val emphasis: Boolean = false
)

data class TownUiState(
    val loading: Boolean = true,
    val money: Int = 0,
    val resources: TownResources = TownResources(),
    val sawmill: ProductionState = ProductionState(),
    val quarry: ProductionState = ProductionState(),
    /** Rosnie co tyknięcie - wymusza przeliczenie paska/licznikow w UI. */
    val now: Long = 0L,
    val toasts: List<TownToast> = emptyList()
) {
    fun stateOf(building: ProductionBuilding): ProductionState = when (building) {
        ProductionBuilding.Sawmill -> sawmill
        ProductionBuilding.Quarry -> quarry
    }
}

/**
 * Stan miasta: surowce i budynki produkcyjne. Produkcja NIE jest tu tykana ani doliczana w tle -
 * caly czas liczy ja [ProductionMath] z roznicy czasu, wiec 10 godzin offline i 10 sekund z
 * otwartym ekranem przechodza dokladnie ta sama sciezka. Tick sluzy wylacznie do odswiezania UI.
 */
class TownViewModel(
    private val playerRepository: PlayerRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(TownUiState(now = nowMillis()))
    val uiState = _uiState.asStateFlow()

    private var toastCounter = 0L

    init {
        refresh()
        startTicking()
    }

    fun refresh() {
        viewModelScope.launch(Dispatchers.IO) {
            val player = playerRepository.getPlayer()
            _uiState.update { state ->
                if (player == null) {
                    state.copy(loading = false)
                } else {
                    state.copy(
                        loading = false,
                        money = player.money,
                        resources = player.townResources,
                        // Budynek nigdy nie uruchomiony ma lastCollectedAt = 0 - startujemy go od
                        // TERAZ, zeby pierwsze wejscie nie naliczylo produkcji od epoki Uniksa.
                        sawmill = player.sawmill.startedIfNeeded(),
                        quarry = player.quarry.startedIfNeeded()
                    )
                }
            }
        }
    }

    private fun startTicking() {
        viewModelScope.launch {
            while (isActive) {
                _uiState.update { it.copy(now = nowMillis()) }
                delay(TICK_MILLIS)
            }
        }
    }

    /** Odbiera to, co budynek wyprodukowal, i przesuwa punkt startu produkcji na teraz. */
    fun collect(building: ProductionBuilding) {
        val state = _uiState.value
        val production = state.stateOf(building)
        val now = nowMillis()
        val pending = ProductionMath.pendingFor(production, now)
        if (pending <= 0) {
            pushToast("Nothing produced yet")
            return
        }

        val updatedProduction = production.copy(lastCollectedAt = now)
        val updatedResources = state.resources.plus(building.resource, pending)
        applyProduction(building, updatedProduction, updatedResources)
        pushToast("+$pending ${building.resourceLabel()}", emphasis = true)
    }

    /**
     * Bonus z quizu: podbija produkcje o [percent]% na najblizsze 24h. Odbiera przy okazji to, co
     * budynek zdazyl juz wyprodukowac na starej stawce - inaczej nowy mnoznik dzialalby wstecz na
     * caly niezebrany czas.
     */
    fun applyQuizBoost(building: ProductionBuilding, percent: Int) {
        val state = _uiState.value
        val production = state.stateOf(building)
        val now = nowMillis()

        val pending = ProductionMath.pendingFor(production, now)
        val updatedProduction = production.copy(
            lastCollectedAt = now,
            boostStartedAt = now,
            boostMultiplier = 1f + percent / 100f
        )
        val updatedResources = state.resources.plus(building.resource, pending)
        applyProduction(building, updatedProduction, updatedResources)

        if (pending > 0) pushToast("+$pending ${building.resourceLabel()}")
        pushToast("+$percent% PRODUKCJI na 24h", emphasis = true)
    }

    /** Ulepszenie budynku za zloto - kazdy poziom to +35% produkcji. */
    fun upgrade(building: ProductionBuilding) {
        val state = _uiState.value
        val production = state.stateOf(building)
        val cost = ProductionMath.upgradeCost(production.level)
        if (state.money < cost) {
            pushToast("Not enough gold (need $cost)")
            return
        }

        val now = nowMillis()
        val pending = ProductionMath.pendingFor(production, now)
        val updatedProduction = production.copy(
            level = production.level + 1,
            lastCollectedAt = now
        )
        val updatedResources = state.resources.plus(building.resource, pending)

        _uiState.update { it.copy(money = it.money - cost) }
        applyProduction(building, updatedProduction, updatedResources)
        viewModelScope.launch(Dispatchers.IO) { playerRepository.addMoney(-cost) }
        pushToast("Level ${updatedProduction.level}!", emphasis = true)
    }

    private fun applyProduction(
        building: ProductionBuilding,
        production: ProductionState,
        resources: TownResources
    ) {
        _uiState.update { state ->
            when (building) {
                ProductionBuilding.Sawmill -> state.copy(sawmill = production, resources = resources)
                ProductionBuilding.Quarry -> state.copy(quarry = production, resources = resources)
            }
        }
        viewModelScope.launch(Dispatchers.IO) {
            playerRepository.updateProduction(building, production, resources)
        }
    }

    /** Losy z nagrody (skrzynia po bossie w story mode) - pierwsze prawdziwe zrodlo losow w grze. */
    fun grantSummonTickets(count: Int, label: String) {
        if (count <= 0) return
        val updated = _uiState.value.resources.let { it.copy(summonTickets = it.summonTickets + count) }
        _uiState.update { it.copy(resources = updated) }
        viewModelScope.launch(Dispatchers.IO) { playerRepository.updateTownResources(updated) }
        pushToast(label, emphasis = true)
    }

    fun pushToast(text: String, emphasis: Boolean = false) {
        val toast = TownToast(id = toastCounter++, text = text, emphasis = emphasis)
        _uiState.update { it.copy(toasts = it.toasts + toast) }
        viewModelScope.launch {
            delay(TOAST_DURATION_MILLIS)
            _uiState.update { state -> state.copy(toasts = state.toasts.filterNot { it.id == toast.id }) }
        }
    }

    private fun ProductionState.startedIfNeeded(): ProductionState =
        if (lastCollectedAt <= 0L) copy(lastCollectedAt = nowMillis()) else this

    private fun nowMillis(): Long = Clock.System.now().toEpochMilliseconds()
}

private fun ProductionBuilding.resourceLabel(): String = when (this) {
    ProductionBuilding.Sawmill -> "drewna"
    ProductionBuilding.Quarry -> "kamienia"
}

/** Co ile odswieza sie pasek/licznik na ekranie. */
private const val TICK_MILLIS = 250L

private const val TOAST_DURATION_MILLIS = 2200L
