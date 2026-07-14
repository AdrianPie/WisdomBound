package com.lernhero.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lernhero.domain.data.PlayerRepository
import com.lernhero.domain.preset.MonsterCard
import com.lernhero.domain.preset.MonsterCatalog
import com.lernhero.domain.preset.PlayerEquipment
import com.lernhero.domain.shop.ShopCatalog
import com.lernhero.domain.shop.ShopItem
import com.lernhero.domain.shop.ShopItemSlot
import com.lernhero.game.character.KnightEquipment
import com.lernhero.game.character.KnightEquipmentSlot
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
    val knightEquipment: KnightEquipment
        get() = KnightEquipment(
            head = equipment.head,
            hands = equipment.hands,
            legs = equipment.legs,
            feet = equipment.feet,
            mainHand = equipment.mainHand
        )

    /** Equipment the player owns (from the shop catalog), for the inventory drag panel. */
    val ownedEquipment: List<ShopItem>
        get() = ownedItems.mapNotNull { ShopCatalog.byId(it) }

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

    fun ownsItemFor(slot: KnightEquipmentSlot): Boolean =
        slot.toShopSlot()?.let { shopSlot ->
            ShopCatalog.itemForSlot(shopSlot)?.id in ownedItems
        } ?: false
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
                        equipment = player.equipment,
                        ownedItems = player.ownedItems,
                        ownedMonsters = player.ownedMonsters,
                        party = player.party.normalizedParty()
                    )
                }
            }
        }
    }

    // region Equipment (drag & drop)

    /** Equips an owned item into its slot. No-op if the item is not owned. */
    fun equipSlot(slot: KnightEquipmentSlot) {
        if (!slot.isEquippable) return
        if (!_uiState.value.ownsItemFor(slot)) {
            _uiState.update { it.copy(message = "Najpierw kup ten przedmiot w sklepie.") }
            return
        }
        setSlotEquipped(slot, equipped = true)
    }

    fun unequipSlot(slot: KnightEquipmentSlot) {
        if (!slot.isEquippable) return
        setSlotEquipped(slot, equipped = false)
    }

    private fun setSlotEquipped(slot: KnightEquipmentSlot, equipped: Boolean) {
        val current = _uiState.value.equipment
        val newEquipment = when (slot) {
            KnightEquipmentSlot.Head -> current.copy(head = equipped)
            KnightEquipmentSlot.Hands -> current.copy(hands = equipped)
            KnightEquipmentSlot.Legs -> current.copy(legs = equipped)
            KnightEquipmentSlot.Feet -> current.copy(feet = equipped)
            KnightEquipmentSlot.MainHand -> current.copy(mainHand = equipped)
            else -> current
        }
        if (newEquipment == current) return

        _uiState.update { it.copy(equipment = newEquipment, message = null) }
        viewModelScope.launch(Dispatchers.IO) {
            playerRepository.updateEquipment(newEquipment)
        }
    }

    // endregion

    // region Party (drag & drop)

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

    fun buyItem(item: ShopItem) {
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

fun KnightEquipmentSlot.toShopSlot(): ShopItemSlot? = when (this) {
    KnightEquipmentSlot.Head -> ShopItemSlot.Head
    KnightEquipmentSlot.Hands -> ShopItemSlot.Hands
    KnightEquipmentSlot.Legs -> ShopItemSlot.Legs
    KnightEquipmentSlot.Feet -> ShopItemSlot.Feet
    KnightEquipmentSlot.MainHand -> ShopItemSlot.MainHand
    else -> null
}

fun ShopItemSlot.toKnightSlot(): KnightEquipmentSlot = when (this) {
    ShopItemSlot.Head -> KnightEquipmentSlot.Head
    ShopItemSlot.Hands -> KnightEquipmentSlot.Hands
    ShopItemSlot.Legs -> KnightEquipmentSlot.Legs
    ShopItemSlot.Feet -> KnightEquipmentSlot.Feet
    ShopItemSlot.MainHand -> KnightEquipmentSlot.MainHand
}
