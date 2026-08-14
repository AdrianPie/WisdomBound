package com.lernhero.domain.player

import com.lernhero.domain.reading.Book
import com.lernhero.domain.reading.ReadingDayProgress
import com.lernhero.domain.reading.ReadingStats
import kotlinx.serialization.Serializable

@Serializable
data class Player(
    val id: String? = null,
    val name: String? = null,
    val email: String? = null,
    val character: HeroClass? = null,
    val money: Int = 0,
    val equipment: PlayerEquipment = PlayerEquipment(),
    val ownedItems: List<String> = emptyList(),
    /** Multiset of [MonsterCard] ids the player owns (duplicates allowed). */
    val ownedMonsters: List<String> = emptyList(),
    /** Companion slots for the pre-battle party. Hero is implicit; entries are [MonsterCard] ids or null. */
    val party: List<String?> = emptyList(),
    /** Zaklecia kupione w sklepie (id z katalogu zaklec). */
    val ownedSpells: List<String> = emptyList(),
    /** Pasek zdolnosci: id zaklecia -> przypisany procent tury. Reszta do 100% to zwykly atak. */
    val spellWeights: Map<String, Int> = emptyMap(),
    /**
     * Ostatni dzien, w ktorym odebrano nagrode za dany cel dzienny - fitness albo czytanie
     * (id celu -> "yyyy-MM-dd"). Odswieza sie samo: cel jest znowu do odebrania, gdy zapisana
     * data przestaje byc dzisiejsza. Oba systemy nagrod dziela ta sama mape.
     */
    val dailyGoalClaims: Map<String, String> = emptyMap(),
    /** Sumaryczne statystyki czytania - licza sie od poczatku, nie resetuja sie codziennie. */
    val readingStats: ReadingStats = ReadingStats(),
    /** Aktywne czytanie z dzisiaj - reset nastepuje, gdy [ReadingDayProgress.dateKey] przestaje byc dzisiejszy. */
    val readingToday: ReadingDayProgress = ReadingDayProgress(),
    /** Ostatnia przeczytana strona per ksiazka (id ksiazki -> numer strony). */
    val bookProgress: Map<String, Int> = emptyMap(),
    /** Ksiazki wgrane przez gracza - te wbudowane (BundledBooks) nie musza tu siedziec. */
    val importedBooks: List<Book> = emptyList()
)

