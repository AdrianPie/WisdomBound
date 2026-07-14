package com.lernhero.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lernhero.domain.data.PlayerRepository
import com.lernhero.domain.item.Item
import com.lernhero.domain.item.ItemCatalog
import com.lernhero.domain.item.ItemSlot
import com.lernhero.domain.item.equip
import com.lernhero.domain.item.itemIn
import com.lernhero.domain.item.migrated
import com.lernhero.domain.item.unequip
import com.lernhero.domain.preset.MonsterCard
import com.lernhero.domain.preset.MonsterCatalog
import com.lernhero.domain.preset.PlayerEquipment
import com.lernhero.game.character.KnightEquipment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Number of companion slots next to the hero in the pre-battle party. */
const val COMPANION_SLOTS = 2

data class PlayerUiState(
    val loading: Boolean = true,
    val name: String = "",
    val money: Int = 0,
    val equipment: PlayerEquipment = PlayerEquipment(),
    val ownedItems: List<String> = emptyList(),
    val ownedMonsters: List<String> = emptyList(),
    val party: List<String?> = List(COMPANION_SLOTS) { null },
    val message: String? = null
) {
    /**
     * Rycerz na ekranie rysuje sie z plyt, wiec interesuje go tylko to, czy w slocie cokolwiek
     * stoi - nie ktory to przedmiot. Tlumaczenie modelu domenowego na model rysowania siedzi tutaj.
     */
    val knightEquipment: KnightEquipment
        get() = KnightEquipment(
            head = equipment.itemIn(ItemSlot.Head) != null,
            hands = equipment.itemIn(ItemSlot.Hands) != null,
            legs = equipment.itemIn(ItemSlot.Legs) != null,
            feet = equipment.itemIn(ItemSlot.Feet) != null,
            mainHand = equipment.itemIn(ItemSlot.MainHand) != null
        )

    /** Wszystko, co gracz kupil. */
    val ownedEquipment: List<Item>
        get() = ownedItems.mapNotNull { ItemCatalog.byId(it) }

    /** Plecak: posiadane, ale niezalozone. */
    val backpack: List<Item>
        get() = ownedEquipment.filter { item -> equipment.itemIn(item.slot)?.id != item.id }

    fun itemIn(slot: ItemSlot): Item? = equipment.itemIn(slot)

    /** Posiadane przedmioty pasujace do slotu - lista do wyboru po kliknieciu pustego miejsca. */
    fun ownedFor(slot: ItemSlot): List<Item> =
        ownedEquipment.filter { it.slot == slot && equipment.itemIn(slot)?.id != it.id }

    /** Monsters resolved for each companion slot (null = empty slot). */
    val partyMonsters: List<MonsterCard?>
        get() = List(COMPANION_SLOTS) { index ->
            party.getOrNull(index)?.let { MonsterCatalog.byId(it) }
        }

    /** Owned monsters not currently placed in the party (respects duplicate ownership). */
    val companionInventory: List<MonsterCard>
        get() {
            val remaining = ownedMonsters.toMutableList()
            party.filterNotNull().forEach { remaining.remove(it) }
            return remaining.mapNotNull { MonsterCatalog.byId(it) }
        }
}

class PlayerViewModel(
    private val playerRepository: PlayerRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(PlayerUiState())
    val uiState = _uiState.asStateFlow()

    init {
        refresh()
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
                        name = player.name.orEmpty(),
                        money = player.money,
                        // Stary zapis (piec flag) przerabiamy na mape slot -> przedmiot od razu przy wczytaniu.
                        equipment = player.equipment.migrated(),
                        ownedItems = player.ownedItems,
                        ownedMonsters = player.ownedMonsters,
                        party = player.party.normalizedParty()
                    )
                }
            }
        }
    }

    // region Ekwipunek

    /** Zaklada przedmiot. Cokolwiek stalo w tym slocie, wraca do plecaka. */
    fun equipItem(item: Item) {
        val state = _uiState.value
        if (item.id !in state.ownedItems) {
            _uiState.update { it.copy(message = "Najpierw kup ten przedmiot w sklepie.") }
            return
        }
        persistEquipment(state.equipment.equip(item))
    }

    fun unequipSlot(slot: ItemSlot) {
        val state = _uiState.value
        if (state.equipment.itemIn(slot) == null) return
        persistEquipment(state.equipment.unequip(slot))
    }

    private fun persistEquipment(equipment: PlayerEquipment) {
        _uiState.update { it.copy(equipment = equipment, message = null) }
        viewModelScope.launch(Dispatchers.IO) {
            playerRepository.updateEquipment(equipment)
        }
    }

    // endregion

    // region Party

    /** Places an owned monster into [slotIndex]. Blocks placing more copies than are owned. */
    fun placeInParty(monsterId: String, slotIndex: Int) {
        if (slotIndex !in 0 until COMPANION_SLOTS) return
        val state = _uiState.value
        val party = state.party.normalizedParty().toMutableList()

        val placedElsewhere = party.filterIndexed { i, id -> i != slotIndex && id == monsterId }.size
        val owned = state.ownedMonsters.count { it == monsterId }
        if (placedElsewhere >= owned) {
            _uiState.update { it.copy(message = "Nie masz kolejnej kopii tego potwora.") }
            return
        }

        party[slotIndex] = monsterId
        persistParty(party)
    }

    fun swapPartySlots(a: Int, b: Int) {
        if (a == b) return
        if (a !in 0 until COMPANION_SLOTS || b !in 0 until COMPANION_SLOTS) return
        val party = _uiState.value.party.normalizedParty().toMutableList()
        val tmp = party[a]
        party[a] = party[b]
        party[b] = tmp
        persistParty(party)
    }

    fun removeFromParty(slotIndex: Int) {
        if (slotIndex !in 0 until COMPANION_SLOTS) return
        val party = _uiState.value.party.normalizedParty().toMutableList()
        if (party[slotIndex] == null) return
        party[slotIndex] = null
        persistParty(party)
    }

    private fun persistParty(party: List<String?>) {
        _uiState.update { it.copy(party = party, message = null) }
        viewModelScope.launch(Dispatchers.IO) {
            playerRepository.updateParty(party)
        }
    }

    // endregion

    // region Shop

    fun buyItem(item: Item) {
        val state = _uiState.value
        if (item.id in state.ownedItems) {
            _uiState.update { it.copy(message = "Masz juz ten przedmiot.") }
            return
        }
        if (state.money < item.cost) {
            _uiState.update { it.copy(message = "Za malo monet. Zdobadz je w walce.") }
            return
        }

        viewModelScope.launch(Dispatchers.IO) {
            val updated = playerRepository.purchaseItem(item.id, item.cost)
            _uiState.update { current ->
                if (updated == null) {
                    current.copy(message = "Zakup nie powiodl sie.")
                } else {
                    current.copy(
                        money = updated.money,
                        ownedItems = updated.ownedItems,
                        message = "Kupiono: ${item.name}"
                    )
                }
            }
        }
    }

    fun buyMonster(card: MonsterCard) {
        val state = _uiState.value
        if (state.money < card.cost) {
            _uiState.update { it.copy(message = "Za malo monet na tego potwora.") }
            return
        }

        viewModelScope.launch(Dispatchers.IO) {
            val updated = playerRepository.purchaseMonster(card.id, card.cost)
            _uiState.update { current ->
                if (updated == null) {
                    current.copy(message = "Zakup nie powiodl sie.")
                } else {
                    current.copy(
                        money = updated.money,
                        ownedMonsters = updated.ownedMonsters,
                        message = "Kupiono: ${card.name}"
                    )
                }
            }
        }
    }

    // endregion

    /** Dev/testing helper: grants coins and persists the new balance. */
    fun addGold(amount: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            val newBalance = playerRepository.addMoney(amount)
            _uiState.update { current ->
                if (newBalance == null) current else current.copy(
                    money = newBalance,
                    message = "+$amount monet (test)"
                )
            }
        }
    }

    fun clearMessage() {
        _uiState.update { it.copy(message = null) }
    }
}

/** Pads/truncates a stored party to exactly [COMPANION_SLOTS] slots. */
private fun List<String?>.normalizedParty(): List<String?> =
    List(COMPANION_SLOTS) { index -> getOrNull(index) }
