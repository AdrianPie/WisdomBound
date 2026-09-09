package com.lernhero.domain.player

import com.lernhero.domain.progress.GameStats
import com.lernhero.domain.reading.Book
import com.lernhero.domain.reading.ReaderSettings
import com.lernhero.domain.reading.ReadingDayProgress
import com.lernhero.domain.reading.ReadingStats
import com.lernhero.domain.reading.SavedWord
import com.lernhero.domain.town.ProductionState
import com.lernhero.domain.town.TownResources
import kotlinx.serialization.Serializable

@Serializable
data class Player(
    val id: String? = null,
    val name: String? = null,
    val email: String? = null,
    val character: HeroClass? = null,
    /**
     * Wyglad wybrany przy tworzeniu postaci - preset i kolory skory/wlosow. Zapisy sprzed tej
     * zmiany nie maja tego pola i dostana [HeroAppearance] domyslny, czyli dokladnie tego
     * bohatera, ktorego widzialy do tej pory.
     */
    val appearance: HeroAppearance = HeroAppearance(),
    val money: Int = 0,
    /** Poziom, XP i rozdane punkty statystyk - patrz [PlayerProgress]. */
    val progress: PlayerProgress = PlayerProgress(),
    /**
     * Liczniki calego konta (pytania, celnosc per kategoria, walki, rekordy). Z nich licza sie
     * osiagniecia - patrz [com.lernhero.domain.progress.AchievementCatalog] - wiec nie ma osobnego
     * zapisu "co juz odblokowane", ktory moglby sie rozjechac ze statystykami.
     */
    val stats: GameStats = GameStats(),
    val equipment: PlayerEquipment = PlayerEquipment(),

    /**
     * Plecak bohatera jako SZTUKI - patrz [com.lernhero.domain.item.ItemInstance]. Zastapil
     * [ownedItems]; te dwa pola nigdy nie sa uzywane naraz, bo zapis czyta sie przez
     * [com.lernhero.domain.item.heroBag], ktore samo wybiera nowy format albo przerabia stary.
     */
    val heroInventory: List<com.lernhero.domain.item.ItemInstance> = emptyList(),

    /** To samo dla sprzetu kompanow - zastepuje [ownedMonsterItems]. */
    val monsterInventory: List<com.lernhero.domain.item.ItemInstance> = emptyList(),

    /**
     * STARY format plecaka bohatera: lista ID z katalogu, po jednej sztuce na przedmiot. Zostaje
     * WYLACZNIE po to, zeby dokumenty zapisane przed przejsciem na egzemplarze wciaz dawaly sie
     * wczytac - patrz [com.lernhero.domain.item.heroBag]. Nowy zapis go nie wypelnia.
     */
    val ownedItems: List<String> = emptyList(),
    /**
     * STARY format poziomow ulepszen: id przedmiotu -> 0..10. Wlasnie ta mapa powodowala, ze
     * ulepszenie jednego nozyka ulepszalo kazdy nozyk w grze - poziom nalezy teraz do SZTUKI
     * ([com.lernhero.domain.item.ItemInstance.level]). Zostaje tylko do wczytania starych zapisow.
     */
    val itemUpgrades: Map<String, Int> = emptyMap(),
    /** STARY format plecaka kompanow - zastapiony przez [monsterInventory]. */
    val ownedMonsterItems: List<String> = emptyList(),
    /** Multiset of [MonsterCard] ids the player owns (duplicates allowed). */
    val ownedMonsters: List<String> = emptyList(),
    /**
     * Poziom kompana per KARTA (id potwora -> poziom 1..50, brak wpisu = poziom 1). Kupowany za
     * zloto - patrz [com.lernhero.domain.monster.MonsterLevels]. Trzymany per karta, a nie per
     * sztuka, wiec duplikaty sluza do sprzedazy, a nie do mnozenia poziomow.
     */
    val monsterLevels: Map<String, Int> = emptyMap(),
    /**
     * Gwiazdki kompana per karta (id -> 1..5, brak wpisu = 1). Podnoszone DUPLIKATAMI - patrz
     * [com.lernhero.domain.monster.MonsterStars]. To gwiazdki, a nie zloto, zdejmuja sufit poziomu.
     */
    val monsterStars: Map<String, Int> = emptyMap(),
    /**
     * Poziomy zaklec kompanow: "idKarty:idZaklecia" -> poziom (brak wpisu = 1). Kupowane za
     * esencje - patrz [com.lernhero.domain.monster.MonsterSpellLevels].
     */
    val monsterSpellLevels: Map<String, Int> = emptyMap(),
    /**
     * Ekwipunek kompanow: idKarty -> (nazwaSlotu -> uid SZTUKI). Puste stringi zamiast typow, bo
     * tak to laduje w Firestore - patrz [com.lernhero.domain.monster.MonsterEquipment].
     *
     * W starych zapisach stoi tu ID Z KATALOGU zamiast uid; rozroznia je
     * [com.lernhero.domain.item.Inventory.UID_SEPARATOR] i przerabia
     * [com.lernhero.domain.item.migratedMonsterEquipment].
     */
    val monsterEquipment: Map<String, Map<String, String>> = emptyMap(),
    /** Companion slots for the pre-battle party. Hero is implicit; entries are [MonsterCard] ids or null. */
    val party: List<String?> = emptyList(),
    /**
     * Kiedy ostatnio odebrano DARMOWY summon danego bannera (id bannera -> epoch millis).
     * Trzymamy moment odbioru, a nie pozostaly czas - patrz [com.lernhero.domain.summon.FreeSummon].
     */
    val freeSummonClaims: Map<String, Long> = emptyMap(),
    /**
     * Wykupione stopnie talentow: id wezla -> stopien (brak wpisu = 0). Patrz
     * [com.lernhero.domain.talent.TalentRules]. Trzymamy SAME stopnie, a nie policzone bonusy -
     * dzieki temu przestrojenie liczb w katalogu dziala od razu na wszystkich zapisach, zamiast
     * zostawiac graczy z bonusami wyliczonymi wedlug starej wersji.
     */
    val talentRanks: Map<String, Int> = emptyMap(),
    /**
     * Ostatni dzien, w ktorym odebrano nagrode za dany cel dzienny - fitness albo czytanie
     * (id celu -> "yyyy-MM-dd"). Odswieza sie samo: cel jest znowu do odebrania, gdy zapisana
     * data przestaje byc dzisiejsza. Oba systemy nagrod dziela ta sama mape.
     */
    val dailyGoalClaims: Map<String, String> = emptyMap(),
    /**
     * Id osiagniec, za ktore ODEBRANO juz nagrode (patrz [com.lernhero.domain.progress.Achievement]).
     *
     * To jedyny stan, jaki osiagniecia w ogole zapisuja - sam postep liczy sie w locie z
     * [GameStats], wiec nie da sie go rozjechac, a dopisanie nowego celu dziala wstecz. Lista,
     * nie Set, bo tak leci do Firestore; kod czytajacy zamienia ja na Set.
     */
    val claimedAchievements: List<String> = emptyList(),
    /** Sumaryczne statystyki czytania - licza sie od poczatku, nie resetuja sie codziennie. */
    val readingStats: ReadingStats = ReadingStats(),
    /** Aktywne czytanie z dzisiaj - reset nastepuje, gdy [ReadingDayProgress.dateKey] przestaje byc dzisiejszy. */
    val readingToday: ReadingDayProgress = ReadingDayProgress(),
    /** Ostatnia przeczytana strona per ksiazka (id ksiazki -> numer strony). */
    val bookProgress: Map<String, Int> = emptyMap(),
    /** Ksiazki wgrane przez gracza - te wbudowane (BundledBooks) nie musza tu siedziec. */
    val importedBooks: List<Book> = emptyList(),
    /** Slowa zapisane z czytnika przyciskiem "zakladka" - surowiec pod quiz ze slowek. */
    val savedWords: List<SavedWord> = emptyList(),
    /** Typografia i motyw czytnika - wspolne dla calej biblioteki, nie per ksiazka. */
    val readerSettings: ReaderSettings = ReaderSettings(),
    /**
     * Postep w kampanii: poziom trudnosci ([com.lernhero.domain.run.RunDifficulty.name]) -> najwyzszy
     * oczyszczony globalny numer planszy. Brak wpisu = nic jeszcze nie zdobyte na tym tierze.
     * Patrz [com.lernhero.domain.story.StoryProgress].
     */
    val storyProgress: Map<String, Int> = emptyMap(),
    /**
     * Kiedy ostatnio odebrano nagrody idle z kampanii (epoch millis, 0 = nigdy). Od tego momentu
     * liczy sie kolejne naliczenie - patrz [com.lernhero.domain.story.IdleRewards].
     */
    val idleCollectedAt: Long = 0L,
    /** Surowce miasta - drewno, kamien, losy do summonow. */
    val townResources: TownResources = TownResources(),
    /** Stan tartaku (produkuje drewno) - patrz [com.lernhero.domain.town.ProductionMath]. */
    val sawmill: ProductionState = ProductionState(),
    /** Stan kamieniolomu (produkuje kamien). */
    val quarry: ProductionState = ProductionState()
)

