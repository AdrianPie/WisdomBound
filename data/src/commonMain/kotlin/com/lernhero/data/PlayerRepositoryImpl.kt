package com.lernhero.data


import com.lernhero.domain.craft.CraftCost
import com.lernhero.domain.craft.ItemUpgrades
import com.lernhero.domain.item.Inventory
import com.lernhero.domain.player.AdminAccounts
import com.lernhero.domain.player.AdminKit
import com.lernhero.domain.item.base
import com.lernhero.domain.item.withMigratedItems
import com.lernhero.domain.item.ItemCatalog
import com.lernhero.domain.monster.CompanionSpells
import com.lernhero.domain.monster.MonsterCatalog
import com.lernhero.domain.monster.MonsterLevels
import com.lernhero.domain.monster.MonsterSpellLevels
import com.lernhero.domain.monster.MonsterStars
import com.lernhero.domain.repository.PlayerRepository
import com.lernhero.domain.player.HeroAppearance
import com.lernhero.domain.player.HeroClass
import com.lernhero.domain.player.HeroStat
import com.lernhero.domain.item.catalog.MonsterGear
import com.lernhero.domain.player.LevelCurve
import com.lernhero.domain.talent.TalentCatalog
import com.lernhero.domain.talent.TalentRules
import com.lernhero.domain.player.Player
import com.lernhero.domain.player.PlayerEquipment
import com.lernhero.domain.player.StarterGift
import com.lernhero.domain.progress.RunStats
import com.lernhero.domain.quiz.custom.CustomDeck
import com.lernhero.domain.story.IdleRewards
import com.lernhero.domain.summon.FreeSummon
import com.lernhero.domain.town.ProductionBuilding
import com.lernhero.domain.town.ProductionState
import com.lernhero.domain.town.TownResources
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
        appearance: HeroAppearance,
        starterGift: StarterGift?,
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) {
        try {
            if (user != null) {
                val playerCollection = Firebase.firestore.collection(collectionPath = "player")
                // Dar startowy trafia i do plecaka, i do zalozonego ekwipunku - gracz ma go miec
                // na sobie w pierwszej walce, a nie do odszukania w menu.
                val giftItem = starterGift?.item
                val player = Player(
                    id = user.uid,
                    name = name,
                    email = user.email ?: "Unknown",
                    character = character,
                    appearance = appearance,
                    // Nowe konto rodzi sie juz w formacie sztuk - zaden nowy gracz nie przechodzi
                    // przez migracje starego plecaka.
                    heroInventory = giftItem?.let { Inventory.add(emptyList(), it.id) } ?: emptyList(),
                    equipment = PlayerEquipment(
                        equipped = giftItem?.let {
                            mapOf(it.slot.name to "${it.id}${Inventory.UID_SEPARATOR}0")
                        } ?: emptyMap()
                    ),
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

    override suspend fun grantItem(itemId: String): Player? {
        return try {
            val document = playerDocument() ?: return null
            val player = getPlayer()?.withMigratedItems() ?: return null
            // Duplikaty sa teraz DOZWOLONE - druga sztuka tego samego przedmiotu to osobny
            // egzemplarz, ktory mozna zalozyc innemu kompanowi albo ulepszyc niezaleznie.
            val updated = player.copy(heroInventory = Inventory.add(player.heroInventory, itemId))
            document.set(updated)
            updated
        } catch (e: Exception) {
            null
        }
    }

    override suspend fun grantMonster(monsterId: String): Player? {
        return try {
            val document = playerDocument() ?: return null
            val player = getPlayer() ?: return null
            val updated = player.copy(
                ownedMonsters = player.ownedMonsters + monsterId,
                stats = player.stats.copy(monstersSummoned = player.stats.monstersSummoned + 1)
            )
            document.set(updated)
            updated
        } catch (e: Exception) {
            null
        }
    }

    override suspend fun grantAdminKit(): Player? {
        return try {
            val document = playerDocument() ?: return null
            val player = getPlayer() ?: return null
            // Bramka jest TUTAJ, a nie w UI: ekran tylko chowa przycisk, a to zapis decyduje,
            // czyje konto wolno napelnic.
            if (!AdminAccounts.isAdmin(player.email)) return null
            val updated = AdminKit.grant(player)
            document.set(updated)
            updated
        } catch (e: Exception) {
            null
        }
    }

    override suspend fun craftItem(itemId: String, cost: CraftCost): Player? {
        return try {
            val document = playerDocument() ?: return null
            val player = getPlayer()?.withMigratedItems() ?: return null
            // Sprzet kompana idzie do WLASNEJ puli - o tym, ktora to, decyduje samo id
            // (patrz [MonsterGear.isMonsterGear]), wiec ekran nie musi tego przekazywac.
            val forMonster = MonsterGear.isMonsterGear(itemId)
            if (!cost.canAfford(player.townResources, player.money)) return null

            // Brak blokady "juz masz": kazde wykucie daje NOWA SZTUKE. To wlasnie po to sa
            // egzemplarze - zeby dziesieciu kompanow moglo nosic ten sam typ pazura.
            val updated = player.copy(
                money = player.money - cost.gold,
                townResources = cost.deductFrom(player.townResources),
                heroInventory =
                    if (forMonster) player.heroInventory
                    else Inventory.add(player.heroInventory, itemId),
                monsterInventory =
                    if (forMonster) Inventory.add(player.monsterInventory, itemId)
                    else player.monsterInventory,
                stats = player.stats.copy(itemsCrafted = player.stats.itemsCrafted + 1)
            )
            document.set(updated)
            updated
        } catch (e: Exception) {
            null
        }
    }

    /** [uid] to KONKRETNA sztuka - ulepszenie dotyka jej jednej, a nie wszystkich kopii typu. */
    override suspend fun upgradeItem(uid: String): Player? {
        return try {
            val document = playerDocument() ?: return null
            val player = getPlayer()?.withMigratedItems() ?: return null

            // Ulepszac mozna TYLKO to, co sie ma - inaczej kuznia bylaby skrotem do zdobycia
            // przedmiotu z pominieciem wykuwania. Sztuka moze lezec w ktorymkolwiek z plecakow.
            val forMonster = Inventory.byUid(player.monsterInventory, uid) != null
            val bag = if (forMonster) player.monsterInventory else player.heroInventory
            val instance = Inventory.byUid(bag, uid) ?: return null
            val item = instance.base() ?: return null

            val cost = ItemUpgrades.nextCost(item, instance.level) ?: return null
            if (!cost.canAfford(player.townResources, player.money)) return null

            val raised = Inventory.withLevel(bag, uid, instance.level + 1)
            val updated = player.copy(
                money = player.money - cost.gold,
                townResources = cost.deductFrom(player.townResources),
                heroInventory = if (forMonster) player.heroInventory else raised,
                monsterInventory = if (forMonster) raised else player.monsterInventory
            )
            document.set(updated)
            updated
        } catch (e: Exception) {
            null
        }
    }

    override suspend fun summonMonsters(monsterIds: List<String>, cost: CraftCost): Player? {
        if (monsterIds.isEmpty()) return null
        return try {
            val document = playerDocument() ?: return null
            val player = getPlayer() ?: return null
            if (!cost.canAfford(player.townResources, player.money)) return null

            val updated = player.copy(
                money = player.money - cost.gold,
                townResources = cost.deductFrom(player.townResources),
                ownedMonsters = player.ownedMonsters + monsterIds,
                stats = player.stats.copy(
                    monstersSummoned = player.stats.monstersSummoned + monsterIds.size
                )
            )
            document.set(updated)
            updated
        } catch (e: Exception) {
            null
        }
    }

    override suspend fun claimFreeSummon(
        bannerId: String,
        cooldownMs: Long,
        monsterId: String,
        now: Long
    ): Player? {
        return try {
            val document = playerDocument() ?: return null
            val player = getPlayer() ?: return null
            // Gotowosc liczymy z ZAPISU, nie z tego, co przyslal ekran - to jedyny sposob, zeby
            // dwa szybkie tapniecia nie wyplacily dwoch darmowych losowan.
            if (!FreeSummon.isReady(player.freeSummonClaims[bannerId], cooldownMs, now)) return null

            val updated = player.copy(
                ownedMonsters = player.ownedMonsters + monsterId,
                freeSummonClaims = player.freeSummonClaims + (bannerId to now),
                stats = player.stats.copy(monstersSummoned = player.stats.monstersSummoned + 1)
            )
            document.set(updated)
            updated
        } catch (e: Exception) {
            null
        }
    }

    override suspend fun applyRunResult(run: RunStats): Player? {
        return try {
            val document = playerDocument() ?: return null
            val player = getPlayer() ?: return null

            val levelUp = LevelCurve.award(player.progress, run.xp)
            val updated = player.copy(
                money = (player.money + run.gold).coerceAtLeast(0),
                progress = levelUp.progress,
                stats = player.stats + run,
                townResources = player.townResources.plusAll(
                    wood = run.wood,
                    stone = run.stone,
                    iron = run.iron,
                    essence = run.essence,
                    tickets = run.summonTickets
                )
            )
            document.set(updated)
            updated
        } catch (e: Exception) {
            null
        }
    }

    override suspend fun upgradeMonster(monsterId: String): Player? {
        return try {
            val document = playerDocument() ?: return null
            val player = getPlayer() ?: return null
            if (monsterId !in player.ownedMonsters) return null

            val card = MonsterCatalog.byId(monsterId) ?: return null
            val level = MonsterLevels.clamp(player.monsterLevels[monsterId] ?: MonsterLevels.MIN_LEVEL)
            if (MonsterLevels.isMaxed(level)) return null

            val cost = MonsterLevels.upgradeCost(level, card.rarity)
            if (player.money < cost) return null

            val updated = player.copy(
                money = player.money - cost,
                monsterLevels = player.monsterLevels + (monsterId to level + 1)
            )
            document.set(updated)
            updated
        } catch (e: Exception) {
            null
        }
    }

    override suspend fun ascendMonster(monsterId: String): Player? {
        return try {
            val document = playerDocument() ?: return null
            val player = getPlayer() ?: return null
            if (MonsterCatalog.byId(monsterId) == null) return null

            val stars = MonsterStars.clampStars(player.monsterStars[monsterId] ?: MonsterStars.MIN_STARS)
            if (MonsterStars.isMaxStars(stars)) return null

            // Duplikaty to WSZYSTKIE kopie ponad ta jedna, ktora reprezentuje sama postac.
            val owned = player.ownedMonsters.count { it == monsterId }
            val cost = MonsterStars.ascendCost(stars)
            if (owned - 1 < cost) return null

            val remaining = player.ownedMonsters.toMutableList()
            repeat(cost) { remaining.remove(monsterId) }

            val updated = player.copy(
                ownedMonsters = remaining,
                monsterStars = player.monsterStars + (monsterId to stars + 1)
            )
            document.set(updated)
            updated
        } catch (e: Exception) {
            null
        }
    }

    override suspend fun dissolveMonster(monsterId: String): Player? {
        return try {
            val document = playerDocument() ?: return null
            val player = getPlayer() ?: return null
            val card = MonsterCatalog.byId(monsterId) ?: return null

            val stars = MonsterStars.clampStars(player.monsterStars[monsterId] ?: MonsterStars.MIN_STARS)
            // Ponizej maksymalnej gwiazdki duplikaty sa POTRZEBNE na awans - rozpuszczanie ich
            // byloby zamiana lepszej nagrody na gorsza, wiec po prostu nie jest dozwolone.
            if (!MonsterStars.isMaxStars(stars)) return null

            val owned = player.ownedMonsters.count { it == monsterId }
            if (owned <= 1) return null

            val remaining = player.ownedMonsters.toMutableList().apply { remove(monsterId) }
            val essence = MonsterStars.dissolveValue(card.rarity)

            val updated = player.copy(
                ownedMonsters = remaining,
                townResources = player.townResources.plusAll(essence = essence)
            )
            document.set(updated)
            updated
        } catch (e: Exception) {
            null
        }
    }

    override suspend fun upgradeMonsterSpell(monsterId: String, spellId: String): Player? {
        return try {
            val document = playerDocument() ?: return null
            val player = getPlayer() ?: return null
            if (monsterId !in player.ownedMonsters) return null
            val card = MonsterCatalog.byId(monsterId) ?: return null
            if (spellId !in card.spellIds) return null
            if (CompanionSpells.byId(spellId) == null) return null

            val key = "$monsterId:$spellId"
            val level = MonsterSpellLevels.clamp(player.monsterSpellLevels[key] ?: MonsterSpellLevels.MIN_LEVEL)
            if (MonsterSpellLevels.isMaxed(level)) return null

            val cost = MonsterSpellLevels.upgradeCost(level)
            if (player.townResources.essence < cost) return null

            val updated = player.copy(
                townResources = player.townResources.copy(essence = player.townResources.essence - cost),
                monsterSpellLevels = player.monsterSpellLevels + (key to level + 1)
            )
            document.set(updated)
            updated
        } catch (e: Exception) {
            null
        }
    }

    override suspend fun spendStatPoint(stat: HeroStat): Player? {
        return try {
            val document = playerDocument() ?: return null
            val player = getPlayer() ?: return null
            val progress = LevelCurve.spendPoint(player.progress, stat) ?: return null
            val updated = player.copy(progress = progress)
            document.set(updated)
            updated
        } catch (e: Exception) {
            null
        }
    }

    override suspend fun spendTalentPoint(talentId: String): Player? {
        return try {
            val document = playerDocument() ?: return null
            val player = getPlayer() ?: return null
            val talent = TalentCatalog.byId(talentId) ?: return null

            val ranks = TalentRules.spend(
                talent = talent,
                ranks = player.talentRanks,
                availablePoints = TalentRules.availablePoints(player.progress.level, player.talentRanks)
            ) ?: return null

            // Zaklecia licza sie ZAWSZE z calego drzewka (TalentRules.spellChances) - nic nie ma
            // czego dopisywac do zapisu osobno.
            val updated = player.copy(talentRanks = ranks)
            document.set(updated)
            updated
        } catch (e: Exception) {
            null
        }
    }

    override suspend fun resetTalents(): Player? {
        return try {
            val document = playerDocument() ?: return null
            val player = getPlayer() ?: return null
            val spent = TalentRules.totalSpent(player.talentRanks)
            if (spent <= 0) return null

            // Punktow nie trzeba oddawac recznie - wynikaja z poziomu, wiec zeruja sie razem
            // ze stopniami (patrz [TalentRules.availablePoints]). Zaklecia znikaja z nimi, bo
            // drzewko jest ich jedynym zrodlem.
            val updated = player.copy(talentRanks = emptyMap())
            document.set(updated)
            updated
        } catch (e: Exception) {
            null
        }
    }

    override suspend fun updateMonsterEquipment(
        monsterId: String,
        equipment: Map<String, String>
    ): Player? {
        return try {
            val document = playerDocument() ?: return null
            // withMigratedItems: sloty zapisujemy juz jako uid, wiec caly gracz musi byc w nowym
            // formacie - inaczej polowa kompanow mialaby uid, a polowa stare id z katalogu.
            val player = getPlayer()?.withMigratedItems() ?: return null
            // Pusty ekwipunek USUWA wpis zamiast zapisywac pusta mape - inaczej dokument
            // gracza rosnie o klucz na kazdego kompana, ktoremu kiedykolwiek zdjeto sprzet.
            val updatedMap =
                if (equipment.isEmpty()) player.monsterEquipment - monsterId
                else player.monsterEquipment + (monsterId to equipment)

            val updated = player.copy(monsterEquipment = updatedMap)
            document.set(updated)
            updated
        } catch (e: Exception) {
            null
        }
    }

    override suspend fun collectIdleRewards(now: Long): Player? {
        return try {
            val document = playerDocument() ?: return null
            val player = getPlayer() ?: return null

            // Pierwsze wejscie (znacznik = 0) tylko startuje licznik - nie ma jeszcze czego odebrac.
            if (player.idleCollectedAt <= 0L) {
                val started = player.copy(idleCollectedAt = now)
                document.set(started)
                return started
            }

            // Kwote liczy repozytorium z ZAPISANEGO stanu, nie ekran - inaczej "Get" bylby
            // darmowym generatorem zlota dla kazdego, kto podmieni liczbe po stronie klienta.
            val best = player.storyProgress.values.maxOrNull() ?: 0
            val reward = IdleRewards.pendingFor(best, player.idleCollectedAt, now)
            if (reward.isEmpty) return player

            val levelUp = LevelCurve.award(player.progress, reward.xp)
            val updated = player.copy(
                money = player.money + reward.gold,
                progress = levelUp.progress,
                townResources = player.townResources.plusAll(iron = reward.iron, essence = reward.essence),
                idleCollectedAt = now
            )
            document.set(updated)
            updated
        } catch (e: Exception) {
            null
        }
    }

    override suspend fun grantTestResources(
        gold: Int,
        wood: Int,
        stone: Int,
        iron: Int,
        essence: Int,
        tickets: Int,
        xp: Int
    ): Player? {
        return try {
            val document = playerDocument() ?: return null
            val player = getPlayer() ?: return null
            val levelUp = LevelCurve.award(player.progress, xp)
            val updated = player.copy(
                money = player.money + gold,
                progress = levelUp.progress,
                townResources = player.townResources.plusAll(
                    wood = wood,
                    stone = stone,
                    iron = iron,
                    essence = essence,
                    tickets = tickets
                )
            )
            document.set(updated)
            updated
        } catch (e: Exception) {
            null
        }
    }

    override suspend fun clearStoryStage(tier: String, globalStage: Int): Player? {
        return try {
            val document = playerDocument() ?: return null
            val player = getPlayer() ?: return null
            val current = player.storyProgress[tier] ?: 0
            if (globalStage <= current) return player
            val updated = player.copy(storyProgress = player.storyProgress + (tier to globalStage))
            document.set(updated)
            updated
        } catch (e: Exception) {
            null
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

    override suspend fun saveWord(word: com.lernhero.domain.reading.SavedWord): Player? {
        return try {
            val document = playerDocument() ?: return null
            val player = getPlayer() ?: return null
            if (player.savedWords.any { it.text.equals(word.text, ignoreCase = true) && it.bookId == word.bookId }) {
                return player
            }
            val updated = player.copy(savedWords = player.savedWords + word)
            document.set(updated)
            updated
        } catch (e: Exception) {
            null
        }
    }

    override suspend fun removeSavedWord(wordId: String): Player? {
        return try {
            val document = playerDocument() ?: return null
            val player = getPlayer() ?: return null
            val updated = player.copy(savedWords = player.savedWords.filterNot { it.id == wordId })
            document.set(updated)
            updated
        } catch (e: Exception) {
            null
        }
    }

    override suspend fun recordWordAnswer(wordId: String, correct: Boolean, now: Long): Player? {
        return try {
            val document = playerDocument() ?: return null
            val player = getPlayer() ?: return null
            val word = player.savedWords.firstOrNull { it.id == wordId } ?: return player
            val updated = player.copy(
                savedWords = player.savedWords.map { saved ->
                    if (saved.id == wordId) {
                        com.lernhero.domain.reading.VocabularyDeck.recordAnswer(word, correct, now)
                    } else {
                        saved
                    }
                }
            )
            document.set(updated)
            updated
        } catch (e: Exception) {
            null
        }
    }

    override suspend fun setBookCover(bookId: String, coverLocator: String): Player? {
        return try {
            val document = playerDocument() ?: return null
            val player = getPlayer() ?: return null
            if (player.importedBooks.none { it.id == bookId }) return player
            val updated = player.copy(
                importedBooks = player.importedBooks.map { book ->
                    if (book.id == bookId) book.copy(coverLocator = coverLocator) else book
                }
            )
            document.set(updated)
            updated
        } catch (e: Exception) {
            null
        }
    }

    override suspend fun removeImportedBook(bookId: String): Player? {
        return try {
            val document = playerDocument() ?: return null
            val player = getPlayer() ?: return null
            val updated = player.copy(
                importedBooks = player.importedBooks.filterNot { it.id == bookId },
                // Postep bez ksiazki byl by tylko smieciem w zapisie - fiszki zostaja, bo slowko
                // raz nauczone jest warte tyle samo, nawet gdy ksiazka zniknela z polki.
                bookProgress = player.bookProgress - bookId
            )
            document.set(updated)
            updated
        } catch (e: Exception) {
            null
        }
    }

    override suspend fun updateReaderSettings(settings: com.lernhero.domain.reading.ReaderSettings) {
        try {
            val document = playerDocument() ?: return
            val player = getPlayer() ?: return
            if (player.readerSettings == settings) return
            document.set(player.copy(readerSettings = settings))
        } catch (_: Exception) {
        }
    }

    override suspend fun updateProduction(
        building: ProductionBuilding,
        state: ProductionState,
        resources: TownResources
    ): Player? {
        return try {
            val document = playerDocument() ?: return null
            val player = getPlayer() ?: return null
            val updated = when (building) {
                ProductionBuilding.Sawmill -> player.copy(sawmill = state, townResources = resources)
                ProductionBuilding.Quarry -> player.copy(quarry = state, townResources = resources)
            }
            document.set(updated)
            updated
        } catch (e: Exception) {
            null
        }
    }

    /* ---- Wlasne talie pytan: podkolekcja player/{uid}/decks ---- */

    private fun decksCollection() =
        getUserId()?.let { uid ->
            Firebase.firestore.collection("player").document(uid).collection("decks")
        }

    override suspend fun getCustomDecks(): List<CustomDeck> {
        return try {
            val snapshot = decksCollection()?.get() ?: return emptyList()
            snapshot.documents
                // Talia zapisana starsza wersja modelu nie moze wywrocic calej listy - pojedynczy
                // dokument, ktorego nie da sie odczytac, po prostu wypada.
                .mapNotNull { document -> runCatching { document.data<CustomDeck>() }.getOrNull() }
                .sortedByDescending { it.updatedAt }
        } catch (e: Exception) {
            emptyList()
        }
    }

    override suspend fun saveCustomDeck(deck: CustomDeck): Boolean {
        return try {
            val collection = decksCollection() ?: return false
            // Sufit pytan pilnujemy TUTAJ, a nie tylko w edytorze: dokument Firestore ma twardy
            // limit 1 MB, a "wygeneruj jeszcze 30" da sie klikac bez konca.
            val trimmed = if (deck.questions.size > CustomDeck.MAX_QUESTIONS) {
                deck.copy(questions = deck.questions.take(CustomDeck.MAX_QUESTIONS))
            } else {
                deck
            }
            collection.document(trimmed.id).set(trimmed)
            true
        } catch (e: Exception) {
            false
        }
    }

    override suspend fun deleteCustomDeck(deckId: String): Boolean {
        return try {
            val collection = decksCollection() ?: return false
            collection.document(deckId).delete()
            true
        } catch (e: Exception) {
            false
        }
    }

    override suspend fun updateTownResources(resources: TownResources): Player? {
        return try {
            val document = playerDocument() ?: return null
            val player = getPlayer() ?: return null
            val updated = player.copy(townResources = resources)
            document.set(updated)
            updated
        } catch (e: Exception) {
            null
        }
    }
}
