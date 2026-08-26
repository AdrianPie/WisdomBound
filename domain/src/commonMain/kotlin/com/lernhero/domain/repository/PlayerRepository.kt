package com.lernhero.domain.repository

import com.lernhero.domain.craft.CraftCost
import com.lernhero.domain.player.HeroClass
import com.lernhero.domain.player.HeroStat
import com.lernhero.domain.player.Player
import com.lernhero.domain.player.PlayerEquipment
import com.lernhero.domain.progress.RunStats
import com.lernhero.domain.reading.Book
import com.lernhero.domain.reading.ReaderSettings
import com.lernhero.domain.reading.SavedWord
import com.lernhero.domain.town.ProductionBuilding
import com.lernhero.domain.town.ProductionState
import com.lernhero.domain.town.TownResources
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
     * Nagroda-przedmiot (skrzynia po bossie) - dopisuje id bez zadnego kosztu. Zwraca null takze
     * wtedy, gdy gracz juz go ma; wolajacy zamienia to wtedy na inna nagrode.
     */
    suspend fun grantItem(itemId: String): Player?

    /** Nagroda-kompan. Duplikaty sa dozwolone, wiec to zawsze sie udaje (poza bledem zapisu). */
    suspend fun grantMonster(monsterId: String): Player?

    /**
     * Wykuwa przedmiot w Kuzni: sprawdza surowce I zloto, odejmuje jedno i drugie, dopisuje przedmiot
     * oraz podbija licznik [com.lernhero.domain.progress.GameStats.itemsCrafted]. Wszystko jednym
     * zapisem - inaczej dalo by sie stracic surowce bez przedmiotu (tak dzialala stara kuznia).
     */
    suspend fun craftItem(itemId: String, cost: CraftCost): Player?

    /**
     * Przyzywa kompanow w Summon Center - zuzywa losy/esencje i ZAPISUJE ich do kolekcji.
     *
     * Lista, a nie pojedynczy potwor, bo summon x10 musi byc JEDNYM zapisem z JEDNYM lacznym
     * kosztem. Dziesiec osobnych wywolan sprawdzaloby stan sprzed poprzedniego zapisu i czesc
     * potworow po prostu by przepadla.
     */
    suspend fun summonMonsters(monsterIds: List<String>, cost: CraftCost): Player?

    /** Odblokowuje zaklecie za esencje z lochow (dawniej zakup za zloto w sklepie). */
    suspend fun unlockSpell(spellId: String, cost: CraftCost): Player?

    /**
     * Rozlicza cale zejscie JEDNYM zapisem: zloto, surowce, XP (razem z awansami i punktami
     * statystyk) i liczniki statystyk. Te rzeczy zawsze zmieniaja sie razem, wiec osobne wywolania
     * tylko mnozylyby okazje do rozjechania sie stanu.
     */
    suspend fun applyRunResult(run: RunStats): Player?

    /** Rozdaje jeden punkt statystyki. Zwraca null, gdy nie ma nierozdanych punktow. */
    suspend fun spendStatPoint(stat: HeroStat): Player?

    /**
     * Podnosi poziom kompana za zloto. Zwraca null, gdy brakuje monet, gracz nie ma tej karty albo
     * jest juz na maksymalnym poziomie - koszt liczy repozytorium ze stanu zapisanego, nie z ekranu.
     */
    suspend fun upgradeMonster(monsterId: String): Player?

    /**
     * Awansuje kompana na kolejna gwiazdke, zjadajac wymagane DUPLIKATY
     * ([com.lernhero.domain.monster.MonsterStars.ascendCost]). Zwraca null, gdy kopii nie starcza
     * albo kompan jest juz na maksymalnej gwiazdce.
     */
    suspend fun ascendMonster(monsterId: String): Player?

    /**
     * Rozpuszcza JEDEN nadmiarowy duplikat kompana w esencje. Dziala tylko na maksymalnej gwiazdce
     * (wczesniej duplikaty sa potrzebne na awans) i nigdy nie rusza ostatniej sztuki.
     */
    suspend fun dissolveMonster(monsterId: String): Player?

    /** Podnosi poziom zaklecia kompana za esencje - patrz [com.lernhero.domain.monster.MonsterSpellLevels]. */
    suspend fun upgradeMonsterSpell(monsterId: String, spellId: String): Player?

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

    /** Zapisuje slowo/zdanie z czytnika do fiszek - surowiec pod quiz ze slowek. */
    suspend fun saveWord(word: SavedWord): Player?

    /** Usuwa fiszke, np. gdy gracz juz umie dane slowo. */
    suspend fun removeSavedWord(wordId: String): Player?

    /**
     * Zapisuje typografie czytnika. Wolane RAZ, przy zamknieciu ksiazki - nie przy kazdym
     * kliknieciu "A+", zeby regulacja rozmiaru czcionki nie zamieniala sie w serie zapisow.
     */
    suspend fun updateReaderSettings(settings: ReaderSettings)

    /**
     * Odhacza plansze kampanii jako zdobyta na danym tierze - zapisuje max(obecny, [globalStage]),
     * nigdy nie cofa juz zdobytego postepu (patrz [com.lernhero.domain.story.StoryProgress.withCleared]).
     */
    suspend fun clearStoryStage(tier: String, globalStage: Int): Player?

    /**
     * Odbiera nagrody idle z kampanii: dopisuje zloto/XP/materialy i przesuwa znacznik czasu na
     * [now]. Repozytorium liczy kwote SAMO (z zapisanego [Player.idleCollectedAt] i postepu), zeby
     * ekran nie mogl podac wlasnej - inaczej "Get" bylby darmowym generatorem zlota.
     */
    suspend fun collectIdleRewards(now: Long): Player?

    /**
     * DEV: dosypuje wszystkiego naraz (zloto, surowce, losy, XP) do testowania. Jedno wywolanie,
     * bo przy testach chodzi o to, zeby miec czym probowac, a nie o osobne przyciski na kazdy zasob.
     */
    suspend fun grantTestResources(
        gold: Int,
        wood: Int,
        stone: Int,
        iron: Int,
        essence: Int,
        tickets: Int,
        xp: Int
    ): Player?

    /**
     * Zapisuje stan budynku produkcyjnego (poziom, bonus z quizu, moment ostatniego odbioru) razem
     * z aktualnym stanem surowcow - jednym zapisem, bo te dwie rzeczy zawsze zmieniaja sie razem
     * (odbior surowca przesuwa `lastCollectedAt` i dopisuje surowiec).
     */
    suspend fun updateProduction(
        building: ProductionBuilding,
        state: ProductionState,
        resources: TownResources
    ): Player?

    /** Dopisuje surowce miasta (moze tez odejmowac, np. koszt craftu w kuzni). */
    suspend fun updateTownResources(resources: TownResources): Player?
}
