package com.lernhero.data


import com.lernhero.domain.repository.PlayerRepository
import com.lernhero.domain.player.HeroClass
import com.lernhero.domain.player.Player
import com.lernhero.domain.player.PlayerEquipment
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.auth.FirebaseUser
import dev.gitlive.firebase.auth.auth
import dev.gitlive.firebase.firestore.DocumentReference
import dev.gitlive.firebase.firestore.firestore

class PlayerRepositoryImpl: PlayerRepository {
    override fun getUserId(): String? {
        return Firebase.auth.currentUser?.uid
    }

    private fun playerDocument(): DocumentReference? =
        getUserId()?.let { uid ->
            Firebase.firestore.collection("player").document(uid)
        }

    override suspend fun createPlayer(
        user: FirebaseUser?,
        name: String,
        character: HeroClass?,
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) {
        try {
            if (user != null) {
                val playerCollection = Firebase.firestore.collection(collectionPath = "player")
                val player = Player(
                    id = user.uid,
                    name = name,
                    email = user.email ?: "Unknown",
                    character = character,
                )

                val playerExists = playerCollection.document(user.uid).get().exists

                if (playerExists) {
                    onSuccess()
                } else {
                    playerCollection.document(user.uid).set(player)
                    playerCollection.document(user.uid)
                        .collection("privateData")
                        .document("role")
                        .set(mapOf("isAdmin" to false))
                    onSuccess()
                }
            } else {
                onFailure("User is not available.")
            }
        } catch (e: Exception) {
            onFailure("Error while creating a Customer: ${e.message}")
        }

    }

    override suspend fun getPlayer(): Player? {
        return try {
            val snapshot = playerDocument()?.get() ?: return null
            if (snapshot.exists) snapshot.data<Player>() else null
        } catch (e: Exception) {
            null
        }
    }

    override suspend fun updateEquipment(equipment: PlayerEquipment) {
        try {
            val document = playerDocument() ?: return
            val player = getPlayer() ?: return
            document.set(player.copy(equipment = equipment))
        } catch (_: Exception) {
        }
    }

    override suspend fun updateParty(party: List<String?>) {
        try {
            val document = playerDocument() ?: return
            val player = getPlayer() ?: return
            document.set(player.copy(party = party))
        } catch (_: Exception) {
        }
    }

    override suspend fun addMoney(amount: Int): Int? {
        return try {
            val document = playerDocument() ?: return null
            val player = getPlayer() ?: return null
            val newBalance = (player.money + amount).coerceAtLeast(0)
            document.set(player.copy(money = newBalance))
            newBalance
        } catch (e: Exception) {
            null
        }
    }

    override suspend fun purchaseItem(itemId: String, cost: Int): Player? {
        return try {
            val document = playerDocument() ?: return null
            val player = getPlayer() ?: return null
            if (itemId in player.ownedItems || player.money < cost) return null
            val updated = player.copy(
                money = player.money - cost,
                ownedItems = player.ownedItems + itemId
            )
            document.set(updated)
            updated
        } catch (e: Exception) {
            null
        }
    }

    override suspend fun purchaseMonster(monsterId: String, cost: Int): Player? {
        return try {
            val document = playerDocument() ?: return null
            val player = getPlayer() ?: return null
            if (player.money < cost) return null
            val updated = player.copy(
                money = player.money - cost,
                ownedMonsters = player.ownedMonsters + monsterId
            )
            document.set(updated)
            updated
        } catch (e: Exception) {
            null
        }
    }

    override suspend fun purchaseSpell(spellId: String, cost: Int): Player? {
        return try {
            val document = playerDocument() ?: return null
            val player = getPlayer() ?: return null
            if (spellId in player.ownedSpells || player.money < cost) return null
            val updated = player.copy(
                money = player.money - cost,
                ownedSpells = player.ownedSpells + spellId
            )
            document.set(updated)
            updated
        } catch (e: Exception) {
            null
        }
    }

    override suspend fun updateSpellWeights(weights: Map<String, Int>) {
        try {
            val document = playerDocument() ?: return
            val player = getPlayer() ?: return
            document.set(player.copy(spellWeights = weights))
        } catch (_: Exception) {
        }
    }

    override suspend fun claimDailyGoalReward(goalId: String, dateKey: String, reward: Int): Player? {
        return try {
            val document = playerDocument() ?: return null
            val player = getPlayer() ?: return null
            if (player.dailyGoalClaims[goalId] == dateKey) return null
            val updated = player.copy(
                money = player.money + reward,
                dailyGoalClaims = player.dailyGoalClaims + (goalId to dateKey)
            )
            document.set(updated)
            updated
        } catch (e: Exception) {
            null
        }
    }

    override suspend fun recordReading(
        dateKey: String,
        minutesDelta: Int,
        pagesDelta: Int,
        wordsDelta: Int,
        charactersDelta: Int
    ): Player? {
        return try {
            val document = playerDocument() ?: return null
            val player = getPlayer() ?: return null
            val today = player.readingToday.forToday(dateKey)
            val updated = player.copy(
                readingToday = today.copy(
                    minutes = today.minutes + minutesDelta,
                    pages = today.pages + pagesDelta
                ),
                readingStats = player.readingStats.copy(
                    totalMinutes = player.readingStats.totalMinutes + minutesDelta,
                    totalPages = player.readingStats.totalPages + pagesDelta,
                    totalWords = player.readingStats.totalWords + wordsDelta,
                    totalCharacters = player.readingStats.totalCharacters + charactersDelta
                )
            )
            document.set(updated)
            updated
        } catch (e: Exception) {
            null
        }
    }

    override suspend fun updateBookProgress(bookId: String, page: Int) {
        try {
            val document = playerDocument() ?: return
            val player = getPlayer() ?: return
            document.set(player.copy(bookProgress = player.bookProgress + (bookId to page)))
        } catch (_: Exception) {
        }
    }

    override suspend fun addImportedBook(book: com.lernhero.domain.reading.Book) {
        try {
            val document = playerDocument() ?: return
            val player = getPlayer() ?: return
            if (player.importedBooks.any { it.id == book.id }) return
            document.set(player.copy(importedBooks = player.importedBooks + book))
        } catch (_: Exception) {
        }
    }
}
