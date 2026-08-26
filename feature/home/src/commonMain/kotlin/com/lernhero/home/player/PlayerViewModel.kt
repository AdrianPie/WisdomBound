package com.lernhero.home.player

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lernhero.domain.repository.PlayerRepository
import com.lernhero.domain.item.Item
import com.lernhero.domain.item.ItemCatalog
import com.lernhero.domain.item.ItemSlot
import com.lernhero.domain.item.equip
import com.lernhero.domain.item.itemIn
import com.lernhero.domain.item.migrated
import com.lernhero.domain.item.toLoadout
import com.lernhero.domain.item.unequip
import com.lernhero.domain.monster.MonsterCard
import com.lernhero.domain.monster.MonsterCatalog
import com.lernhero.domain.monster.CompanionSpell
import com.lernhero.domain.monster.MonsterLevels
import com.lernhero.domain.monster.MonsterSpellLevels
import com.lernhero.domain.monster.MonsterStars
import com.lernhero.domain.craft.CraftCost
import com.lernhero.domain.player.HeroStat
import com.lernhero.domain.player.PlayerEquipment
import com.lernhero.domain.player.HeroCombatStats
import com.lernhero.domain.player.PlayerProgress
import com.lernhero.domain.run.RunDifficulty
import com.lernhero.domain.story.StoryProgress
import com.lernhero.domain.progress.GameStats
import com.lernhero.domain.progress.RunStats
import com.lernhero.domain.town.TownResources
import com.lernhero.domain.spell.AbilityLoadout
import com.lernhero.domain.spell.Spell
import com.lernhero.domain.spell.SpellCatalog
import com.lernhero.game.character.KnightEquipment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Number of companion slots next to the hero in the pre-battle party. */
const val COMPANION_SLOTS = 2

/**
 * Jedna pozycja kolekcji kompanow: karta, ile sztuk, jaki poziom, ile gwiazdek i ile z nich stoi
 * w druzynie. Duplikaty ([duplicates]) sa paliwem awansu gwiazdki, a na maksymalnej gwiazdce -
 * rozpuszczaja sie w esencje.
 */
data class OwnedMonster(
    val card: MonsterCard,
    val count: Int,
    val level: Int,
    val stars: Int,
    val inParty: Int,
    /** Poziom kazdego zaklecia tej karty (id zaklecia -> poziom). */
    val spellLevels: Map<String, Int> = emptyMap()
) {
    val hp: Int get() = MonsterStars.hpAt(card, stars, level)
    val attack: Int get() = MonsterStars.attackAt(card, stars, level)

    /** Ile kopii ponad te jedna, ktora JEST postacia - to one sa paliwem awansu. */
    val duplicates: Int get() = (count - 1).coerceAtLeast(0)

    /** Sufit poziomu wynikajacy z gwiazdek - to on obowiazuje, nie absolutny [MonsterLevels.MAX_LEVEL]. */
    val levelCap: Int get() = MonsterStars.levelCapFor(stars)
    val atLevelCap: Boolean get() = level >= levelCap
    val levelUpCost: Int get() = MonsterLevels.upgradeCost(level, card.rarity)

    val maxStars: Boolean get() = MonsterStars.isMaxStars(stars)
    val ascendCost: Int get() = MonsterStars.ascendCost(stars)
    val canAscend: Boolean get() = !maxStars && duplicates >= ascendCost

    /** Nadmiarowe kopie da sie rozpuscic dopiero na maksymalnej gwiazdce - wczesniej sa potrzebne. */
    val canDissolve: Boolean get() = maxStars && duplicates > 0
    val dissolveValue: Int get() = MonsterStars.dissolveValue(card.rarity)

    fun spellLevel(spell: CompanionSpell): Int =
        MonsterSpellLevels.clamp(spellLevels[spell.id] ?: MonsterSpellLevels.MIN_LEVEL)
}

data class PlayerUiState(
    val loading: Boolean = true,
    val name: String = "",
    val money: Int = 0,
    val equipment: PlayerEquipment = PlayerEquipment(),
    val ownedItems: List<String> = emptyList(),
    val ownedMonsters: List<String> = emptyList(),
    /** Poziom kompana per karta (brak wpisu = [MonsterLevels.MIN_LEVEL]). */
    val monsterLevels: Map<String, Int> = emptyMap(),
    /** Gwiazdki kompana per karta - podnoszone duplikatami (brak wpisu = 1). */
    val monsterStars: Map<String, Int> = emptyMap(),
    /** Poziomy zaklec kompanow ("idKarty:idZaklecia" -> poziom). */
    val monsterSpellLevels: Map<String, Int> = emptyMap(),
    val party: List<String?> = List(COMPANION_SLOTS) { null },
    val ownedSpells: List<String> = emptyList(),
    val spellWeights: Map<String, Int> = emptyMap(),
    /** Postep w kampanii per tier trudnosci - patrz [StoryProgress]. */
    val storyProgress: Map<String, Int> = emptyMap(),
    /** Kiedy ostatnio odebrano nagrody idle (epoch millis, 0 = nigdy) - patrz [IdleRewards]. */
    val idleCollectedAt: Long = 0L,
    /** Poziom, XP i rozdane punkty - zrodlo dla zakladki Postep i dla bonusow w walce. */
    val progress: PlayerProgress = PlayerProgress(),
    /** Liczniki calego konta - z nich licza sie statystyki i osiagniecia. */
    val stats: GameStats = GameStats(),
    /** Surowce miasta - potrzebne tu, bo Kuznia i Summon Center placa nimi, nie zlotem. */
    val resources: TownResources = TownResources(),
    val message: String? = null
) {
    /** Pasek zdolnosci: pojemnosc bazowa + wagi zaklec. Docina sie sam, wiec suma <= pojemnosci. */
    val ability: AbilityLoadout
        get() = spellWeights.entries
            .fold(AbilityLoadout(capacity = AbilityLoadout.BASE_CAPACITY)) { bar, (id, pct) ->
                bar.withPercent(id, pct)
            }

    /** Zaklecia, ktore gracz posiada (do paska w EQ). */
    val spells: List<Spell>
        get() = ownedSpells.mapNotNull { SpellCatalog.byId(it) }

    /**
     * Rycerz na ekranie rysuje sie z plyt, wiec interesuje go tylko to, czy w slocie cokolwiek
     * stoi - nie ktory to przedmiot. Tlumaczenie modelu domenowego na model rysowania siedzi tutaj.
     */
    val knightEquipment: KnightEquipment
        get() = KnightEquipment(
            head = equipment.itemIn(ItemSlot.Head) != null,
            chest = equipment.itemIn(ItemSlot.Chest) != null,
            hands = equipment.itemIn(ItemSlot.Hands) != null,
            legs = equipment.itemIn(ItemSlot.Legs) != null,
            feet = equipment.itemIn(ItemSlot.Feet) != null,
            mainHand = equipment.itemIn(ItemSlot.MainHand) != null
        )

    /**
     * Gotowe liczby "kim jestem w walce" - atak, HP, krytyk. Skladane w [HeroCombatStats] z bazy,
     * ekwipunku i punktow z awansow, wiec karta postaci pokazuje dokladnie to, czym walczy walka.
     */
    val combatStats: HeroCombatStats
        get() = HeroCombatStats.of(equipment.toLoadout().stats, progress.stats)

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

    fun levelOf(monsterId: String): Int =
        MonsterLevels.clampToStars(monsterLevels[monsterId] ?: MonsterLevels.MIN_LEVEL, starsOf(monsterId))

    fun starsOf(monsterId: String): Int =
        MonsterStars.clampStars(monsterStars[monsterId] ?: MonsterStars.MIN_STARS)

    /**
     * Kolekcja pogrupowana po karcie: ile sztuk, jaki poziom, ile stoi w druzynie. To jest widok
     * dla zakladki "Kolekcja" - lista `ownedMonsters` sama w sobie jest tylko workiem id.
     */
    val collection: List<OwnedMonster>
        get() = ownedMonsters
            .groupingBy { it }
            .eachCount()
            .mapNotNull { (id, count) ->
                MonsterCatalog.byId(id)?.let { card ->
                    OwnedMonster(
                        card = card,
                        count = count,
                        level = levelOf(id),
                        stars = starsOf(id),
                        inParty = party.count { it == id },
                        spellLevels = card.spellIds.associateWith { spellId ->
                            MonsterSpellLevels.clamp(
                                monsterSpellLevels["$id:$spellId"] ?: MonsterSpellLevels.MIN_LEVEL
                            )
                        }
                    )
                }
            }
            .sortedWith(compareByDescending<OwnedMonster> { it.card.rarity.ordinal }.thenBy { it.card.name })

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
                        monsterLevels = player.monsterLevels,
                        monsterStars = player.monsterStars,
                        monsterSpellLevels = player.monsterSpellLevels,
                        party = player.party.normalizedParty(),
                        ownedSpells = player.ownedSpells,
                        spellWeights = player.spellWeights,
                        storyProgress = player.storyProgress,
                        idleCollectedAt = player.idleCollectedAt,
                        progress = player.progress,
                        stats = player.stats,
                        resources = player.townResources
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
            _uiState.update { it.copy(message = "Najpierw wykuj ten przedmiot w Kuzni.") }
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

    // region Story mode progress

    /** Loch story-mode ukonczony (boss pokonany) - odblokowuje kolejny poziom, jesli to byl najwyzszy dotad. */
    /**
     * Odbiera nagrody idle ("GET" na panelu kampanii). Kwote liczy repozytorium z zapisanego stanu -
     * ekran podaje tylko biezacy czas, wiec nie da sie stad wygenerowac zlota.
     */
    fun collectIdleRewards(now: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            val updated = playerRepository.collectIdleRewards(now) ?: return@launch
            _uiState.update {
                it.copy(
                    money = updated.money,
                    progress = updated.progress,
                    resources = updated.townResources,
                    idleCollectedAt = updated.idleCollectedAt
                )
            }
        }
    }

    /** Odhacza plansze kampanii jako zdobyta - patrz [StoryProgress.withCleared]. */
    fun clearStoryStage(tier: RunDifficulty, globalStage: Int) {
        val current = _uiState.value.storyProgress[tier.name] ?: 0
        if (globalStage <= current) return
        _uiState.update { it.copy(storyProgress = StoryProgress.withCleared(it.storyProgress, tier, globalStage)) }
        viewModelScope.launch(Dispatchers.IO) {
            playerRepository.clearStoryStage(tier.name, globalStage)
        }
    }

    // endregion

    // region Ability bar

    /**
     * Ustawia procent zaklecia na pasku. Model docina go do wolnej pojemnosci, wiec nigdy nie
     * przekroczymy limitu. Zmiana zapisuje sie natychmiast, tak jak zakladanie ekwipunku.
     */
    fun setSpellPercent(spellId: String, percent: Int) {
        if (spellId !in _uiState.value.ownedSpells) return
        val next = _uiState.value.ability.withPercent(spellId, percent).weights
        _uiState.update { it.copy(spellWeights = next, message = null) }
        viewModelScope.launch(Dispatchers.IO) {
            playerRepository.updateSpellWeights(next)
        }
    }

    // endregion

    // region Shop

    /**
     * Odblokowuje zaklecie za esencje z lochow (dawniej zakup za zloto w sklepie). Zaklecia,
     * przedmioty i kompani maja teraz osobne waluty i osobne budynki - patrz
     * [com.lernhero.domain.craft.SummonCosts].
     */
    fun unlockSpell(spell: Spell, cost: CraftCost) {
        val state = _uiState.value
        if (spell.id in state.ownedSpells) {
            _uiState.update { it.copy(message = "Masz juz to zaklecie.") }
            return
        }
        if (!cost.canAfford(state.resources, state.money)) {
            _uiState.update { it.copy(message = "Za malo esencji na to zaklecie.") }
            return
        }

        viewModelScope.launch(Dispatchers.IO) {
            val updated = playerRepository.unlockSpell(spell.id, cost)
            _uiState.update { current ->
                if (updated == null) {
                    current.copy(message = "Nie udalo sie odblokowac zaklecia.")
                } else {
                    current.copy(
                        money = updated.money,
                        resources = updated.townResources,
                        ownedSpells = updated.ownedSpells,
                        message = "Odblokowano: ${spell.name}"
                    )
                }
            }
        }
    }

    /**
     * Wykuwa przedmiot w Kuzni. Ekwipunku nie kupuje sie juz za zloto - jest zrobiony z materialow
     * przyniesionych z lochu, wiec sklep przestal byc sposobem na omijanie rozgrywki.
     */
    fun craftItem(item: Item, cost: CraftCost) {
        val state = _uiState.value
        if (item.id in state.ownedItems) {
            _uiState.update { it.copy(message = "Masz juz ten przedmiot.") }
            return
        }
        if (!cost.canAfford(state.resources, state.money)) {
            _uiState.update { it.copy(message = "Brakuje materialow na ten przedmiot.") }
            return
        }

        viewModelScope.launch(Dispatchers.IO) {
            val updated = playerRepository.craftItem(item.id, cost)
            _uiState.update { current ->
                if (updated == null) {
                    current.copy(message = "Wykuwanie sie nie udalo.")
                } else {
                    current.copy(
                        money = updated.money,
                        resources = updated.townResources,
                        ownedItems = updated.ownedItems,
                        stats = updated.stats,
                        message = "Wykuto: ${item.name}"
                    )
                }
            }
        }
    }

    /**
     * Przyzywa kompanow w Summon Center za losy i esencje - caly ciag (x1 albo x10) idzie JEDNYM
     * zapisem z jednym lacznym kosztem, wiec przy szybkim x10 nic nie przepada ani nie dubluje sie.
     */
    fun summonMonsters(cards: List<MonsterCard>, cost: CraftCost) {
        if (cards.isEmpty()) return
        val state = _uiState.value
        if (!cost.canAfford(state.resources, state.money)) {
            _uiState.update { it.copy(message = "Brakuje losow albo esencji.") }
            return
        }

        viewModelScope.launch(Dispatchers.IO) {
            val updated = playerRepository.summonMonsters(cards.map { it.id }, cost)
            _uiState.update { current ->
                if (updated == null) {
                    current.copy(message = "Przyzwanie sie nie udalo.")
                } else {
                    current.copy(
                        money = updated.money,
                        resources = updated.townResources,
                        ownedMonsters = updated.ownedMonsters,
                        stats = updated.stats,
                        message = if (cards.size == 1) {
                            "Do druzyny dolacza: ${cards.first().name}"
                        } else {
                            "Przyzwano ${cards.size} kompanow"
                        }
                    )
                }
            }
        }
    }

    /** Kupuje kolejny poziom kompana za zloto. Sufit wynika z GWIAZDEK, nie z absolutnego maksimum. */
    fun upgradeMonster(monster: OwnedMonster) {
        val state = _uiState.value
        if (monster.atLevelCap) {
            _uiState.update {
                it.copy(message = "Poziom ${monster.levelCap} to sufit ${monster.stars}★ - awansuj duplikatami.")
            }
            return
        }
        if (state.money < monster.levelUpCost) {
            _uiState.update { it.copy(message = "Za malo monet na kolejny poziom.") }
            return
        }

        viewModelScope.launch(Dispatchers.IO) {
            val updated = playerRepository.upgradeMonster(monster.card.id)
            _uiState.update { current ->
                if (updated == null) {
                    current.copy(message = "Ulepszenie sie nie udalo.")
                } else {
                    current.copy(
                        money = updated.money,
                        monsterLevels = updated.monsterLevels,
                        message = "${monster.card.name} awansuje na poziom ${updated.monsterLevels[monster.card.id]}"
                    )
                }
            }
        }
    }

    /**
     * Awans na kolejna gwiazdke - zjada duplikaty. To jest glowne przeznaczenie powtorzonych
     * losowan: duplikat nie jest smieciem, tylko jedynym sposobem na zdjecie sufitu poziomu.
     */
    fun ascendMonster(monster: OwnedMonster) {
        if (monster.maxStars) {
            _uiState.update { it.copy(message = "${monster.card.name} ma juz maksymalne gwiazdki.") }
            return
        }
        if (!monster.canAscend) {
            _uiState.update {
                it.copy(message = "Potrzebujesz ${monster.ascendCost} kopii (masz ${monster.duplicates}).")
            }
            return
        }

        viewModelScope.launch(Dispatchers.IO) {
            val updated = playerRepository.ascendMonster(monster.card.id)
            _uiState.update { current ->
                if (updated == null) {
                    current.copy(message = "Awans sie nie udal.")
                } else {
                    val stars = updated.monsterStars[monster.card.id] ?: monster.stars
                    current.copy(
                        ownedMonsters = updated.ownedMonsters,
                        monsterStars = updated.monsterStars,
                        message = "${monster.card.name} awansuje na $stars★! Nowy sufit: " +
                            "${MonsterStars.levelCapFor(stars)}"
                    )
                }
            }
        }
    }

    /** Rozpuszcza nadmiarowy duplikat w esencje - dziala dopiero na maksymalnej gwiazdce. */
    fun dissolveMonster(monster: OwnedMonster) {
        if (!monster.canDissolve) {
            _uiState.update {
                it.copy(
                    message = if (!monster.maxStars) {
                        "Duplikaty sa potrzebne na awans - rozpuscisz je dopiero na ${MonsterStars.MAX_STARS}★."
                    } else {
                        "Nie masz nadmiarowych kopii."
                    }
                )
            }
            return
        }

        viewModelScope.launch(Dispatchers.IO) {
            val updated = playerRepository.dissolveMonster(monster.card.id)
            _uiState.update { current ->
                if (updated == null) {
                    current.copy(message = "Rozpuszczenie sie nie udalo.")
                } else {
                    current.copy(
                        ownedMonsters = updated.ownedMonsters,
                        resources = updated.townResources,
                        message = "Rozpuszczono kopie: +${monster.dissolveValue} esencji"
                    )
                }
            }
        }
    }

    /** Podnosi poziom zaklecia kompana za esencje. */
    fun upgradeMonsterSpell(monster: OwnedMonster, spell: CompanionSpell) {
        val level = monster.spellLevel(spell)
        if (MonsterSpellLevels.isMaxed(level)) return
        val cost = MonsterSpellLevels.upgradeCost(level)
        if (_uiState.value.resources.essence < cost) {
            _uiState.update { it.copy(message = "Za malo esencji (potrzeba $cost).") }
            return
        }

        viewModelScope.launch(Dispatchers.IO) {
            val updated = playerRepository.upgradeMonsterSpell(monster.card.id, spell.id)
            _uiState.update { current ->
                if (updated == null) {
                    current.copy(message = "Ulepszenie zaklecia sie nie udalo.")
                } else {
                    current.copy(
                        resources = updated.townResources,
                        monsterSpellLevels = updated.monsterSpellLevels,
                        message = "${spell.name} na poziomie ${level + 1}"
                    )
                }
            }
        }
    }

    /** Odhacza ukonczony loch w statystykach konta (bez zadnej nagrody - ta idzie ze skrzyni). */
    fun recordDungeonCleared() {
        viewModelScope.launch(Dispatchers.IO) {
            val updated = playerRepository.applyRunResult(
                RunStats(won = true, dungeonCleared = true, countsAsBattle = false)
            ) ?: return@launch
            _uiState.update { it.copy(stats = updated.stats) }
        }
    }

    /** Rozdaje jeden punkt statystyki po awansie - patrz zakladka Postep. */
    fun spendStatPoint(stat: HeroStat) {
        if (_uiState.value.progress.unspentPoints <= 0) return
        viewModelScope.launch(Dispatchers.IO) {
            val updated = playerRepository.spendStatPoint(stat) ?: return@launch
            _uiState.update { it.copy(progress = updated.progress, message = "+1 ${stat.label}") }
        }
    }

    // endregion

    /** Placi zlotem za zakup w lochu. Zwraca false, gdy monet nie starcza. */
    fun spendGold(amount: Int, label: String): Boolean {
        if (_uiState.value.money < amount) {
            _uiState.update { it.copy(message = "Za malo monet.") }
            return false
        }
        _uiState.update { it.copy(money = it.money - amount, message = label) }
        viewModelScope.launch(Dispatchers.IO) {
            playerRepository.addMoney(-amount)
        }
        return true
    }

    /** Znalezione zloto (skarby na mapie lochu) - dopisuje i utrwala saldo. */
    fun grantGold(amount: Int, label: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val newBalance = playerRepository.addMoney(amount)
            _uiState.update { current ->
                if (newBalance == null) current else current.copy(money = newBalance, message = label)
            }
        }
    }

    /**
     * Nagroda-przedmiot (skrzynia po bossie) - to samo, co zakup, tylko za darmo. Gdy gracz ma juz
     * ten przedmiot, [PlayerRepository.purchaseItem] zwraca null - zamieniamy go wtedy na zloto,
     * zeby pole na kole fortuny nigdy nie bylo "puste".
     */
    fun grantItem(item: Item, fallbackGold: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            val updated = playerRepository.grantItem(item.id)
            if (updated != null) {
                _uiState.update {
                    it.copy(ownedItems = updated.ownedItems, message = "Zdobyto: ${item.name}")
                }
                return@launch
            }
            val balance = playerRepository.addMoney(fallbackGold)
            _uiState.update { current ->
                current.copy(
                    money = balance ?: current.money,
                    message = "${item.name} juz masz - zamiast tego +$fallbackGold monet."
                )
            }
        }
    }

    /** Nagroda-kompan (skrzynia po bossie). Duplikaty sa dozwolone - potwora mozna miec wiecej niz jednego. */
    fun grantMonster(card: MonsterCard) {
        viewModelScope.launch(Dispatchers.IO) {
            val updated = playerRepository.grantMonster(card.id) ?: return@launch
            _uiState.update {
                it.copy(
                    ownedMonsters = updated.ownedMonsters,
                    stats = updated.stats,
                    message = "Do druzyny dolacza: ${card.name}"
                )
            }
        }
    }

    /**
     * DEV: dosypuje wszystkiego naraz do testowania - zloto, surowce miasta, losy i XP.
     * Wolane z panelu deweloperskiego na ekranie Dom; przed wydaniem chowa sie razem z nim.
     */
    fun grantTestResources() {
        viewModelScope.launch(Dispatchers.IO) {
            val updated = playerRepository.grantTestResources(
                gold = 50_000,
                wood = 5_000,
                stone = 5_000,
                iron = 2_000,
                essence = 2_000,
                tickets = 50,
                xp = 5_000
            ) ?: return@launch
            _uiState.update {
                it.copy(
                    money = updated.money,
                    progress = updated.progress,
                    resources = updated.townResources,
                    message = "DEV: dosypano zasobow"
                )
            }
        }
    }

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
