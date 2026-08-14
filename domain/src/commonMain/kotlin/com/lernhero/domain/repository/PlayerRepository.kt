package com.lernhero.domain.repository

import com.lernhero.domain.player.HeroClass
import com.lernhero.domain.player.Player
import com.lernhero.domain.player.PlayerEquipment
import com.lernhero.domain.reading.Book
import dev.gitlive.firebase.auth.FirebaseUser

interface PlayerRepository {
    fun getUserId(): String?
    suspend fun createPlayer(
        user: FirebaseUser?,
        name: String,
        character: HeroClass?,
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    )

    /** Returns the signed-in user's player document, or null when missing/signed out. */
    suspend fun getPlayer(): Player?

    suspend fun updateEquipment(equipment: PlayerEquipment)

    /** Persists the pre-battle companion party (list of monster ids or null per slot). */
    suspend fun updateParty(party: List<String?>)

    /** Adds (or subtracts) coins and returns the new balance, or null on failure. */
    suspend fun addMoney(amount: Int): Int?

    /**
     * Buys an item: checks balance, deducts the cost and stores the item id.
     * Returns the updated player, or null when it failed (not enough money,
     * already owned, signed out).
     */
    suspend fun purchaseItem(itemId: String, cost: Int): Player?

    /**
     * Buys a monster: checks balance, deducts the cost and appends the monster id to the
     * owned multiset (duplicates allowed). Returns the updated player, or null on failure.
     */
    suspend fun purchaseMonster(monsterId: String, cost: Int): Player?

    /**
     * Buys a spell: checks balance, deducts the cost and stores the spell id.
     * Returns the updated player, or null on failure (not enough money, already owned, signed out).
     */
    suspend fun purchaseSpell(spellId: String, cost: Int): Player?

    /** Persists the ability bar: spell id -> assigned percent of a turn. */
    suspend fun updateSpellWeights(weights: Map<String, Int>)

    /**
     * Odbiera nagrode za cel dzienny (fitness albo czytanie) na dany dzien. Zwraca null, gdy
     * nagroda za ten cel byla juz odebrana dzisiaj (albo gdy zapis sie nie udal) - w obu
     * przypadkach nie placi drugi raz.
     */
    suspend fun claimDailyGoalReward(goalId: String, dateKey: String, reward: Int): Player?

    /**
     * Dopisuje kawalek aktywnego czytania: minuty/strony na dzisiaj (resetuja sie same, gdy
     * [dateKey] zmieni sie na kolejny dzien) i sumaryczne statystyki na zawsze.
     */
    suspend fun recordReading(
        dateKey: String,
        minutesDelta: Int,
        pagesDelta: Int,
        wordsDelta: Int,
        charactersDelta: Int
    ): Player?

    /** Zapamietuje ostatnia przeczytana strone danej ksiazki. */
    suspend fun updateBookProgress(bookId: String, page: Int)

    /** Dopisuje ksiazke wgrana przez gracza do biblioteki. */
    suspend fun addImportedBook(book: Book)
}
