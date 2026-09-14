package com.lernhero.home.player

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lernhero.domain.repository.PlayerRepository
import com.lernhero.domain.progress.CategoryLevels
import com.lernhero.domain.item.Item
import com.lernhero.domain.item.Inventory
import com.lernhero.domain.item.ItemInstance
import com.lernhero.domain.item.resolve
import com.lernhero.domain.item.heroBag
import com.lernhero.domain.item.monsterBag
import com.lernhero.domain.item.migratedHeroEquipment
import com.lernhero.domain.item.migratedMonsterEquipment
import com.lernhero.domain.item.instanceIn
import com.lernhero.domain.item.equippedUids
import com.lernhero.domain.item.base
import com.lernhero.domain.item.ItemCatalog
import com.lernhero.domain.item.catalog.MonsterGear
import com.lernhero.domain.item.ItemSlot
import com.lernhero.domain.item.equip
import com.lernhero.domain.item.itemIn
import com.lernhero.domain.item.migrated
import com.lernhero.domain.craft.ItemUpgrades
import com.lernhero.domain.item.toLoadout
import com.lernhero.domain.item.unequip
import com.lernhero.domain.monster.MonsterCard
import com.lernhero.domain.monster.MonsterCatalog
import com.lernhero.domain.monster.CompanionPower
import com.lernhero.domain.monster.MonsterEquipment
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
import com.lernhero.domain.player.AdminAccounts
import com.lernhero.domain.player.PartySlots
import com.lernhero.domain.player.Player
import kotlin.time.Clock
import com.lernhero.reader.todayDateKey
import com.lernhero.domain.progress.Achievement
import com.lernhero.domain.progress.GameStats
import com.lernhero.domain.progress.RunStats
import com.lernhero.domain.notification.ReminderPlan
import com.lernhero.domain.reading.SavedWord
import com.lernhero.shared.notification.ReminderScheduler
import com.lernhero.shared.prefs.AppPreferences
import com.lernhero.shared.notification.nextLocalHourMillis
import com.lernhero.domain.town.TownResources
import com.lernhero.domain.talent.Talent
import com.lernhero.domain.talent.TalentBonuses
import com.lernhero.domain.talent.TalentRules
import com.lernhero.domain.talent.TalentTree
import com.lernhero.domain.player.HeroAppearance
import com.lernhero.game.character.HeroLook
import com.lernhero.game.character.KnightEquipment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Miejsca na kompanow obok bohatera. Cala druzyna to SZESC postaci, a bohater zajmuje jedno
 * miejsce - stad piec, a nie szesc. Walka bierze sklad wprost z tej listy, wiec sama liczba
 * wystarczy, zeby na polu bitwy stanelo szesciu swoich.
 *
 * Liczba miejsc jest CALKOWITA - ile z nich stoi otworem, mowi [PartySlots.unlockedCount].
 */
const val COMPANION_SLOTS = PartySlots.TOTAL

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
    // Liczby w skali WALKI, a nie karty katalogowej - patrz [CompanionPower]. Kompan z 70 HP
    // na karcie stawal obok bohatera z 420 i wygladal na pomylke; prawdziwa jego wartoscia w
    // walce jest 210. Sprzet dolicza sie do tego samego, wiec nie ma dwoch roznych "HP kompana".
    val hp: Int get() = CompanionPower.hpAt(card, stars, level)
    val attack: Int get() = CompanionPower.attackAt(card, stars, level)

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
    /** Konto z lista [com.lernhero.domain.player.AdminAccounts] - jedyne, ktore widzi zestaw testowy. */
    val isAdmin: Boolean = false,
    /** Wyglad wybrany przy tworzeniu postaci - z niego biora sie kolory rysowanego bohatera. */
    val appearance: HeroAppearance = HeroAppearance(),
    val money: Int = 0,
    val equipment: PlayerEquipment = PlayerEquipment(),
    /**
     * Plecak bohatera jako SZTUKI - poziom ulepszenia siedzi w egzemplarzu, nie w osobnej mapie
     * obok (patrz [ItemInstance]). Dzieki temu dwie kopie tego samego miecza moga byc na roznych
     * poziomach, a slot wskazuje na konkretna z nich.
     */
    val heroBag: List<ItemInstance> = emptyList(),
    /** Sprzet kompanow - osobna pula od plecaka bohatera, patrz [MonsterGear]. */
    val monsterBag: List<ItemInstance> = emptyList(),
    /** Ekwipunek kompanow: idKarty -> (slot -> uid sztuki). Patrz [MonsterEquipment]. */
    val monsterEquipment: Map<String, Map<String, String>> = emptyMap(),
    val ownedMonsters: List<String> = emptyList(),
    /** Poziom kompana per karta (brak wpisu = [MonsterLevels.MIN_LEVEL]). */
    val monsterLevels: Map<String, Int> = emptyMap(),
    /** Gwiazdki kompana per karta - podnoszone duplikatami (brak wpisu = 1). */
    val monsterStars: Map<String, Int> = emptyMap(),
    /** Poziomy zaklec kompanow ("idKarty:idZaklecia" -> poziom). */
    val monsterSpellLevels: Map<String, Int> = emptyMap(),
    val party: List<String?> = List(COMPANION_SLOTS) { null },
    /** Kiedy ostatnio odebrano darmowy summon per banner - patrz [com.lernhero.domain.summon.FreeSummon]. */
    val freeSummonClaims: Map<String, Long> = emptyMap(),
    /** Wykupione stopnie talentow (id wezla -> stopien) - patrz [TalentRules]. */
    val talentRanks: Map<String, Int> = emptyMap(),
    /** Postep w kampanii per tier trudnosci - patrz [StoryProgress]. */
    val storyProgress: Map<String, Int> = emptyMap(),
    /** Ile razy kazda plansza byla juz zdobyta - patrz [StageRepeats]. */
    val stageClears: Map<String, Int> = emptyMap(),
    /** Kiedy ostatnio odebrano nagrody idle (epoch millis, 0 = nigdy) - patrz [IdleRewards]. */
    val idleCollectedAt: Long = 0L,
    /** Poziom, XP i rozdane punkty - zrodlo dla zakladki Postep i dla bonusow w walce. */
    val progress: PlayerProgress = PlayerProgress(),
    /** Liczniki calego konta - z nich licza sie statystyki i osiagniecia. */
    val stats: GameStats = GameStats(),
    /**
     * Id osiagniec, za ktore odebrano juz nagrode. Razem ze [stats] wystarcza, zeby ekran
     * Postepu wiedzial, co jest do odebrania - patrz [Achievement.isClaimable].
     */
    val claimedAchievements: Set<String> = emptySet(),
    /** Surowce miasta - potrzebne tu, bo Kuznia i Summon Center placa nimi, nie zlotem. */
    val resources: TownResources = TownResources(),
    /** Fiszki zapisane w czytniku - surowiec pod endless "Wlasne" (patrz [com.lernhero.domain.quiz.WordQuizBank]). */
    val savedWords: List<SavedWord> = emptyList(),
    /** Poziomy opanowania tematow quizu - patrz [com.lernhero.domain.progress.CategoryMastery]. */
    val categoryLevels: CategoryLevels = CategoryLevels(),
    val message: String? = null
) {
    /**
     * Rycerz na ekranie rysuje sie z plyt, wiec interesuje go tylko to, czy w slocie cokolwiek
     * stoi - nie ktory to przedmiot. Tlumaczenie modelu domenowego na model rysowania siedzi tutaj.
     */
    val heroLook: HeroLook
        get() = HeroLook.of(appearance)

    val knightEquipment: KnightEquipment
        get() = KnightEquipment(
            head = equipment.itemIn(ItemSlot.Head, heroBag) != null,
            chest = equipment.itemIn(ItemSlot.Chest, heroBag) != null,
            hands = equipment.itemIn(ItemSlot.Hands, heroBag) != null,
            legs = equipment.itemIn(ItemSlot.Legs, heroBag) != null,
            feet = equipment.itemIn(ItemSlot.Feet, heroBag) != null,
            mainHand = equipment.itemIn(ItemSlot.MainHand, heroBag) != null
        )

    /**
     * Gotowe liczby "kim jestem w walce" - atak, HP, krytyk. Skladane w [HeroCombatStats] z bazy,
     * ekwipunku i punktow z awansow, wiec karta postaci pokazuje dokladnie to, czym walczy walka.
     */
    val combatStats: HeroCombatStats
        get() = HeroCombatStats.of(equipment.toLoadout(heroBag).stats, progress.stats, talentBonuses)

    /** Zsumowane efekty drzewka - ekran talentow pokazuje z tego podsumowanie na dole. */
    val talentBonuses: TalentBonuses
        get() = TalentRules.bonusesOf(talentRanks)

    /** Nierozdane punkty talentow - liczone z poziomu, nie przechowywane osobno. */
    val talentPoints: Int get() = TalentRules.availablePoints(progress.level, talentRanks)

    /** Ile punktow siedzi w danym drzewku - progi rzedow licza sie wlasnie z tego. */
    fun talentPointsIn(tree: TalentTree): Int = TalentRules.pointsIn(talentRanks, tree)

    /**
     * Sztuki, ktore gracz NOSI PRZY SOBIE, sparowane z gotowym przedmiotem (juz z poziomem
     * ulepszenia tej sztuki). Nierozpoznane id wypadaja - usuniety z katalogu przedmiot nie moze
     * wysadzic ekwipunku.
     */
    val ownedEquipment: List<Pair<ItemInstance, Item>>
        get() = heroBag.mapNotNull { instance -> instance.resolve()?.let { instance to it } }

    /** Plecak: posiadane, ale NIEZALOZONE sztuki. Porownujemy uid, bo kopii moze byc wiele. */
    val backpack: List<Pair<ItemInstance, Item>>
        get() {
            val worn = equipment.equippedUids()
            return ownedEquipment.filterNot { (instance, _) -> instance.uid in worn }
        }

    fun itemIn(slot: ItemSlot): Item? = equipment.itemIn(slot, heroBag)

    fun instanceIn(slot: ItemSlot): ItemInstance? = equipment.instanceIn(slot, heroBag)

    /** Wolne sztuki pasujace do slotu - lista do wyboru po kliknieciu pustego miejsca. */
    fun ownedFor(slot: ItemSlot): List<Pair<ItemInstance, Item>> =
        backpack.filter { (_, item) -> item.slot == slot }

    /** Monsters resolved for each companion slot (null = empty slot). */
    val partyMonsters: List<MonsterCard?>
        get() = List(COMPANION_SLOTS) { index ->
            party.getOrNull(index)
                ?.takeIf { isSlotUnlocked(index) }
                ?.let { MonsterCatalog.byId(it) }
        }

    /** Czy to miejsce jest juz otwarte poziomem bohatera - patrz [PartySlots]. */
    fun isSlotUnlocked(slotIndex: Int): Boolean = PartySlots.isUnlocked(slotIndex, progress.level)

    /** Poziom, ktory otwiera to miejsce - na klodce w [com.lernhero.home.team.TeamPanel]. */
    fun levelForSlot(slotIndex: Int): Int = PartySlots.requiredLevel(slotIndex)

    /** Czy jest gdzie postawic kolejnego kompana - liczone TYLKO po otwartych miejscach. */
    val hasFreeCompanionSlot: Boolean
        get() = (0 until COMPANION_SLOTS).any { isSlotUnlocked(it) && party.getOrNull(it) == null }

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

    /**
     * Owned monsters available to add to the party. Each distinct companion can stand in the party
     * only ONCE, so any id already placed is dropped from the picker entirely - duplicate copies
     * stay in the Collection (to sell or level), they are not extra party bodies.
     */
    val companionInventory: List<MonsterCard>
        get() {
            val inParty = party.filterNotNull().toSet()
            return ownedMonsters.asSequence()
                .distinct()
                .filter { it !in inParty }
                .mapNotNull { MonsterCatalog.byId(it) }
                .toList()
        }
}

class PlayerViewModel(
    private val playerRepository: PlayerRepository,
    private val reminders: ReminderScheduler,
    private val preferences: AppPreferences
) : ViewModel() {

    private val _uiState = MutableStateFlow(PlayerUiState())
    val uiState = _uiState.asStateFlow()

    /**
     * Ostatnio wczytany gracz - trzymany WYLACZNIE po to, zeby dalo sie przeliczyc przypomnienia
     * po zmianie w Ustawieniach ([remindersPreferencesChanged]) bez kolejnego strzalu po siec.
     * Ekrany czytaja [uiState], nigdy tego pola.
     */
    private var lastPlayer: Player? = null

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
                        isAdmin = AdminAccounts.isAdmin(player.email),
                        appearance = player.appearance,
                        money = player.money,
                        // Stary zapis przerabiamy na nowy JUZ PRZY WCZYTANIU - dalej w ekranach
                        // istnieje tylko format sztuk (patrz [withMigratedItems]).
                        equipment = player.migratedHeroEquipment(),
                        heroBag = player.heroBag(),
                        monsterBag = player.monsterBag(),
                        monsterEquipment = player.migratedMonsterEquipment(),
                        ownedMonsters = player.ownedMonsters,
                        monsterLevels = player.monsterLevels,
                        monsterStars = player.monsterStars,
                        monsterSpellLevels = player.monsterSpellLevels,
                        party = player.party.normalizedParty(),
                        freeSummonClaims = player.freeSummonClaims,
                        talentRanks = player.talentRanks,
                        storyProgress = player.storyProgress,
                        stageClears = player.stageClears,
                        idleCollectedAt = player.idleCollectedAt,
                        progress = player.progress,
                        stats = player.stats,
                        claimedAchievements = player.claimedAchievements.toSet(),
                        resources = player.townResources,
                        savedWords = player.savedWords,
                        categoryLevels = CategoryLevels(player.categoryLevels)
                    )
                }
            }
            if (player != null) {
                lastPlayer = player
                rescheduleReminders(player)
            }
        }
    }

    /**
     * Przelicza plan przypomnien od nowa i nadpisuje nim kolejke systemowa.
     *
     * Wolane przy KAZDYM wczytaniu gracza, bo caly plan wynika z roznicy czasu (tak samo jak
     * nagrody idle i produkcja w miescie) - nie ma zadnego stanu w tle, ktory moglby sie rozjechac.
     * Nadpisywanie jest bezpieczne: kazde przypomnienie ma staly klucz, wiec dziesiec wejsc do gry
     * daje jedno przypomnienie, a nie dziesiec.
     */
    /**
     * Wolane z Ustawien po kazdej zmianie przelacznika przypomnien. Bez tego nowy stan wszedlby
     * w zycie dopiero przy nastepnym wczytaniu gracza - a gracz, ktory wlasnie cos wylaczyl,
     * ma prawo oczekiwac, ze to juz sie stalo.
     */
    fun remindersPreferencesChanged() {
        lastPlayer?.let(::rescheduleReminders)
    }

    private fun rescheduleReminders(player: Player) {
        if (!reminders.isPermitted) return
        // Wylaczenie w Ustawieniach ma kasowac to, co JUZ stoi w kolejce - inaczej przelacznik
        // dzialalby dopiero od nastepnego przypomnienia, czyli z punktu widzenia gracza: wcale.
        if (!preferences.remindersEnabled) {
            reminders.cancelAll()
            return
        }

        val now = Clock.System.now().toEpochMilliseconds()
        // "Cel dzienny odhaczony" poznajemy po tym, ze cokolwiek zostalo dzis odebrane -
        // dokladnej daty z krokomierza ten ViewModel nie ma, a i tak chodzi o to, zeby NIE
        // szturchac kogos, kto juz dzis cos zrobil.
        val todayGoalDone = player.dailyGoalClaims.values.any { it == todayDateKey() }

        // Plan liczy sie w calosci, a dopiero potem odsiewamy wyciszone rodzaje - dzieki temu
        // regula "co i kiedy" zostaje jedna, w domenie, a Ustawienia sa zwyklym filtrem na koncu.
        val plan = ReminderPlan.forPlayer(
            player = player,
            now = now,
            todayGoalDone = todayGoalDone,
            nextDailyGoalSlotMs = nextLocalHourMillis(preferences.dailyGoalHour, now)
        ).filter { preferences.isReminderEnabled(it.kind) }

        // Pusty plan tez wysylamy: [ReminderScheduler.schedule] nadpisuje CALA kolejke, wiec
        // wyciszenie ostatniego rodzaju naprawde ja oprozni.
        reminders.schedule(plan)
    }

    // region Ekwipunek

    /** Zaklada KONKRETNA sztuke. Cokolwiek stalo w tym slocie, wraca do plecaka. */
    fun equipItem(instance: ItemInstance) {
        val state = _uiState.value
        val item = instance.resolve()
        if (item == null || Inventory.byUid(state.heroBag, instance.uid) == null) {
            _uiState.update { it.copy(message = "Forge this item first.") }
            return
        }
        persistEquipment(state.equipment.equip(instance, item.slot))
    }

    fun unequipSlot(slot: ItemSlot) {
        val state = _uiState.value
        if (state.equipment.instanceIn(slot, state.heroBag) == null) return
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

    /**
     * Places an owned monster into [slotIndex]. A companion can stand in the party only once -
     * having several copies does not let the same monster fill several slots.
     */
    fun placeInParty(monsterId: String, slotIndex: Int) {
        if (slotIndex !in 0 until COMPANION_SLOTS) return
        val state = _uiState.value
        // Bramka jest tez TUTAJ, nie tylko w UI: ekran moze wystawic przycisk, ale to model
        // odpowiada za to, co trafi do zapisu i stad na pole bitwy.
        if (!state.isSlotUnlocked(slotIndex)) {
            _uiState.update {
                it.copy(message = "That slot opens at level ${PartySlots.requiredLevel(slotIndex)}.")
            }
            return
        }
        val party = state.party.normalizedParty().toMutableList()

        if (state.ownedMonsters.none { it == monsterId }) return
        if (party.filterIndexed { i, id -> i != slotIndex && id == monsterId }.isNotEmpty()) {
            _uiState.update { it.copy(message = "That companion is already in your team.") }
            return
        }

        party[slotIndex] = monsterId
        persistParty(party)
    }

    fun swapPartySlots(a: Int, b: Int) {
        if (a == b) return
        if (a !in 0 until COMPANION_SLOTS || b !in 0 until COMPANION_SLOTS) return
        val state = _uiState.value
        if (!state.isSlotUnlocked(a) || !state.isSlotUnlocked(b)) return
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

    /**
     * Wsypuje zestaw testowy. Repozytorium i tak sprawdza uprawnienie, wiec ten brak sprawdzenia
     * tutaj jest swiadomy - jedno miejsce decyduje, zamiast dwoch, ktore moga sie rozjechac.
     */
    fun grantAdminKit() {
        viewModelScope.launch(Dispatchers.IO) {
            val granted = playerRepository.grantAdminKit()
            if (granted == null) {
                _uiState.update { it.copy(message = "Admin kit unavailable for this account.") }
                return@launch
            }
            refresh()
            _uiState.update { it.copy(message = "Admin kit granted.") }
        }
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

    // region Shop

    /**
     * Wykuwa przedmiot w Kuzni. Ekwipunku nie kupuje sie juz za zloto - jest zrobiony z materialow
     * przyniesionych z lochu, wiec sklep przestal byc sposobem na omijanie rozgrywki.
     */
    fun craftItem(item: Item, cost: CraftCost) {
        val state = _uiState.value
        // Zadnej blokady "juz to masz": kazde wykucie daje NOWA SZTUKE. To po to sa egzemplarze -
        // dziesieciu kompanow moze nosic ten sam typ pazura, jesli wykujesz dziesiec sztuk.
        if (!cost.canAfford(state.resources, state.money)) {
            _uiState.update { it.copy(message = "Not enough materials for this item.") }
            return
        }

        viewModelScope.launch(Dispatchers.IO) {
            val updated = playerRepository.craftItem(item.id, cost)
            _uiState.update { current ->
                if (updated == null) {
                    current.copy(message = "Forging failed.")
                } else {
                    current.copy(
                        money = updated.money,
                        resources = updated.townResources,
                        heroBag = updated.heroBag(),
                        monsterBag = updated.monsterBag(),
                        stats = updated.stats,
                        message = "Forged: ${item.name}"
                    )
                }
            }
        }
    }

    /**
     * Podnosi przedmiot o jeden poziom ulepszenia. Kosztu NIE bierzemy z ekranu - liczy go
     * repozytorium z zapisanego poziomu, wiec podwojne tapniecie nie kupi dwa razy tego samego,
     * najtanszego progu. Tutaj sprawdzamy tylko to, co da sie powiedziec od razu, zeby gracz
     * dostal komunikat bez podrozy do bazy.
     */
    fun upgradeItem(instance: ItemInstance) {
        val state = _uiState.value
        // Szukamy KONKRETNEJ sztuki - moze lezec w ktorymkolwiek z dwoch plecakow.
        val owned = Inventory.byUid(state.heroBag, instance.uid)
            ?: Inventory.byUid(state.monsterBag, instance.uid)
        val item = owned?.base()
        if (owned == null || item == null) {
            _uiState.update { it.copy(message = "You have to forge this item first.") }
            return
        }
        val cost = ItemUpgrades.nextCost(item, owned.level)
        if (cost == null) {
            _uiState.update { it.copy(message = "Already at max level.") }
            return
        }
        if (!cost.canAfford(state.resources, state.money)) {
            _uiState.update { it.copy(message = "Not enough materials for this upgrade.") }
            return
        }

        viewModelScope.launch(Dispatchers.IO) {
            val updated = playerRepository.upgradeItem(instance.uid)
            _uiState.update { current ->
                if (updated == null) {
                    current.copy(message = "Upgrade failed.")
                } else {
                    val bags = updated.heroBag() + updated.monsterBag()
                    val level = Inventory.byUid(bags, instance.uid)?.level ?: owned.level
                    current.copy(
                        money = updated.money,
                        resources = updated.townResources,
                        heroBag = updated.heroBag(),
                        monsterBag = updated.monsterBag(),
                        message = "${item.name} +$level"
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
            _uiState.update { it.copy(message = "Not enough tickets or essence.") }
            return
        }

        viewModelScope.launch(Dispatchers.IO) {
            val updated = playerRepository.summonMonsters(cards.map { it.id }, cost)
            _uiState.update { current ->
                if (updated == null) {
                    current.copy(message = "Summon failed.")
                } else {
                    current.copy(
                        money = updated.money,
                        resources = updated.townResources,
                        ownedMonsters = updated.ownedMonsters,
                        stats = updated.stats,
                        message = if (cards.size == 1) {
                            "Joining your team: ${cards.first().name}"
                        } else {
                            "Summoned ${cards.size} companions"
                        }
                    )
                }
            }
        }
    }

    /**
     * Odbiera darmowy summon danego bannera. O tym, czy licznik doszedl do zera, decyduje
     * repozytorium - tutaj tylko przekazujemy wylosowana karte i biezacy czas.
     */
    fun claimFreeSummon(bannerId: String, cooldownMs: Long, card: MonsterCard, now: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            val updated = playerRepository.claimFreeSummon(bannerId, cooldownMs, card.id, now)
            _uiState.update { current ->
                if (updated == null) {
                    current.copy(message = "The free summon is not ready yet.")
                } else {
                    current.copy(
                        ownedMonsters = updated.ownedMonsters,
                        freeSummonClaims = updated.freeSummonClaims,
                        stats = updated.stats,
                        message = "Free summon: ${card.name}"
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
                it.copy(message = "Level ${monster.levelCap} is the ${monster.stars}★ cap - ascend with duplicates.")
            }
            return
        }
        if (state.money < monster.levelUpCost) {
            _uiState.update { it.copy(message = "Not enough coins for the next level.") }
            return
        }

        viewModelScope.launch(Dispatchers.IO) {
            val updated = playerRepository.upgradeMonster(monster.card.id)
            _uiState.update { current ->
                if (updated == null) {
                    current.copy(message = "Upgrade failed.")
                } else {
                    current.copy(
                        money = updated.money,
                        monsterLevels = updated.monsterLevels,
                        message = "${monster.card.name} reaches level ${updated.monsterLevels[monster.card.id]}"
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
            _uiState.update { it.copy(message = "${monster.card.name} is already at max stars.") }
            return
        }
        if (!monster.canAscend) {
            _uiState.update {
                it.copy(message = "You need ${monster.ascendCost} copies (you have ${monster.duplicates}).")
            }
            return
        }

        viewModelScope.launch(Dispatchers.IO) {
            val updated = playerRepository.ascendMonster(monster.card.id)
            _uiState.update { current ->
                if (updated == null) {
                    current.copy(message = "Ascension failed.")
                } else {
                    val stars = updated.monsterStars[monster.card.id] ?: monster.stars
                    current.copy(
                        ownedMonsters = updated.ownedMonsters,
                        monsterStars = updated.monsterStars,
                        message = "${monster.card.name} ascends to $stars★! New level cap: " +
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
                        "Duplicates are needed to ascend - you can dissolve them at ${MonsterStars.MAX_STARS}★."
                    } else {
                        "You have no spare copies."
                    }
                )
            }
            return
        }

        viewModelScope.launch(Dispatchers.IO) {
            val updated = playerRepository.dissolveMonster(monster.card.id)
            _uiState.update { current ->
                if (updated == null) {
                    current.copy(message = "Dissolving failed.")
                } else {
                    current.copy(
                        ownedMonsters = updated.ownedMonsters,
                        resources = updated.townResources,
                        message = "Copy dissolved: +${monster.dissolveValue} essence"
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
            _uiState.update { it.copy(message = "Not enough essence (need $cost).") }
            return
        }

        viewModelScope.launch(Dispatchers.IO) {
            val updated = playerRepository.upgradeMonsterSpell(monster.card.id, spell.id)
            _uiState.update { current ->
                if (updated == null) {
                    current.copy(message = "Spell upgrade failed.")
                } else {
                    current.copy(
                        resources = updated.townResources,
                        monsterSpellLevels = updated.monsterSpellLevels,
                        message = "${spell.name} at level ${level + 1}"
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

    /**
     * Wykupuje jeden stopien talentu. Regul (prog rzedu, rodzic, wolny punkt) pilnuje
     * [TalentRules] w repozytorium - tutaj jest tylko szybki odsiew, zeby nie strzelac zapytaniem
     * po kazdym tapnieciu w zamkniety wezel.
     */
    fun spendTalentPoint(talent: Talent) {
        val state = _uiState.value
        if (!TalentRules.canSpend(talent, state.talentRanks, state.talentPoints)) return

        viewModelScope.launch(Dispatchers.IO) {
            val updated = playerRepository.spendTalentPoint(talent.id) ?: return@launch
            _uiState.update {
                it.copy(
                    talentRanks = updated.talentRanks,
                    progress = updated.progress,
                    message = "${talent.name} ${updated.talentRanks[talent.id] ?: 0}/${talent.maxRank}"
                )
            }
        }
    }

    /** Zeruje drzewko i oddaje wszystkie punkty - razem z zakleciami, ktore z niego wyszly. */
    fun resetTalents() {
        if (TalentRules.totalSpent(_uiState.value.talentRanks) <= 0) return

        viewModelScope.launch(Dispatchers.IO) {
            val updated = playerRepository.resetTalents() ?: return@launch
            _uiState.update {
                it.copy(
                    talentRanks = updated.talentRanks,
                    progress = updated.progress,
                    message = "Talent tree reset - your points are back."
                )
            }
        }
    }

    /* ------------------------------ Ekwipunek kompanow ------------------------------ */

    /** Ekwipunek jednego kompana - pusta mapa, gdy nic jeszcze nie zalozyl. */
    fun equipmentOf(monsterId: String): Map<String, String> =
        _uiState.value.monsterEquipment[monsterId] ?: MonsterEquipment.EMPTY

    fun equipMonsterItem(monsterId: String, instance: ItemInstance) {
        val item = instance.resolve() ?: return
        val current = equipmentOf(monsterId)
        persistMonsterEquipment(
            monsterId,
            MonsterEquipment.with(current, instance, item.slot),
            "Zalozono: ${item.name}"
        )
    }

    fun unequipMonsterSlot(monsterId: String, slot: ItemSlot) {
        val current = equipmentOf(monsterId)
        if (slot.name !in current) return
        persistMonsterEquipment(monsterId, MonsterEquipment.without(current, slot), "Gear unequipped")
    }

    /** Zdejmuje kompanowi WSZYSTKO - odpowiednik przycisku "Zdejmij" na ekranie kompana. */
    fun unequipMonsterAll(monsterId: String) {
        if (equipmentOf(monsterId).isEmpty()) return
        persistMonsterEquipment(monsterId, MonsterEquipment.EMPTY, "All gear unequipped")
    }

    /**
     * Dobiera kompanowi najlepszy dostepny sprzet do kazdego slotu. Przedmioty noszone przez
     * INNYCH kompanow sa pomijane - pula liczy sztuki, wiec ten sam pazur nie moze wisiec
     * na dwoch naraz.
     */
    fun autoEquipMonster(monsterId: String) {
        val state = _uiState.value
        val element = MonsterCatalog.byId(monsterId)?.element
        val taken = MonsterEquipment.equippedElsewhere(state.monsterEquipment, monsterId)
        val next = MonsterEquipment.autoEquip(state.monsterBag, element, taken)

        if (next == equipmentOf(monsterId)) {
            _uiState.update { it.copy(message = "Nothing to upgrade this companion with.") }
            return
        }
        persistMonsterEquipment(monsterId, next, "Gear auto-equipped")
    }

    private fun persistMonsterEquipment(
        monsterId: String,
        equipment: Map<String, String>,
        message: String
    ) {
        // Optymistycznie: ekran ma zareagowac natychmiast, a zapis dogonic w tle. Odpowiedz
        // z repozytorium i tak nadpisze stan, wiec rozjazd trwa najwyzej jeden oddech.
        _uiState.update { current ->
            val map =
                if (equipment.isEmpty()) current.monsterEquipment - monsterId
                else current.monsterEquipment + (monsterId to equipment)
            current.copy(monsterEquipment = map, message = message)
        }
        viewModelScope.launch(Dispatchers.IO) {
            val updated = playerRepository.updateMonsterEquipment(monsterId, equipment) ?: return@launch
            _uiState.update { it.copy(monsterEquipment = updated.monsterEquipment) }
        }
    }

    // endregion

    /** Placi zlotem za zakup w lochu. Zwraca false, gdy monet nie starcza. */
    fun spendGold(amount: Int, label: String): Boolean {
        if (_uiState.value.money < amount) {
            _uiState.update { it.copy(message = "Not enough coins.") }
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
     * Odbiera nagrode za zdobyte osiagniecie. Czy wolno - decyduje repozytorium (liczy z
     * zapisanych statystyk), wiec tapniecie w przycisk, ktory akurat nie powinien byc aktywny,
     * po prostu nic nie robi zamiast wyplacac drugi raz.
     */
    fun claimAchievement(achievement: Achievement) {
        viewModelScope.launch(Dispatchers.IO) {
            val updated = playerRepository.claimAchievement(achievement.id) ?: return@launch
            _uiState.update {
                it.copy(
                    money = updated.money,
                    claimedAchievements = updated.claimedAchievements.toSet(),
                    message = "${achievement.title}: +${achievement.reward} gold"
                )
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
                    it.copy(heroBag = updated.heroBag(), message = "Obtained: ${item.name}")
                }
                return@launch
            }
            val balance = playerRepository.addMoney(fallbackGold)
            _uiState.update { current ->
                current.copy(
                    money = balance ?: current.money,
                    message = "You already own ${item.name} - +$fallbackGold coins instead."
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
                    message = "Joining your team: ${card.name}"
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
                    message = "DEV: resources granted"
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
                    message = "+$amount coins (test)"
                )
            }
        }
    }

    fun clearMessage() {
        _uiState.update { it.copy(message = null) }
    }
}

/**
 * Pads/truncates a stored party to exactly [COMPANION_SLOTS] slots and drops repeated ids -
 * a companion belongs in the party only once, and older saves may still hold duplicates.
 */
private fun List<String?>.normalizedParty(): List<String?> {
    val seen = mutableSetOf<String>()
    val unique = map { id -> if (id != null && seen.add(id)) id else null }
    return List(COMPANION_SLOTS) { index -> unique.getOrNull(index) }
}
