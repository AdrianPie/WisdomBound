package com.lernhero.domain.repository

import com.lernhero.domain.craft.CraftCost
import com.lernhero.domain.player.HeroAppearance
import com.lernhero.domain.player.HeroClass
import com.lernhero.domain.player.HeroStat
import com.lernhero.domain.player.Player
import com.lernhero.domain.player.PlayerEquipment
import com.lernhero.domain.player.StarterGift
import com.lernhero.domain.progress.RunStats
import com.lernhero.domain.quiz.custom.CustomDeck
import com.lernhero.domain.reading.Book
import com.lernhero.domain.reading.ReaderSettings
import com.lernhero.domain.reading.SavedWord
import com.lernhero.domain.town.ProductionBuilding
import com.lernhero.domain.town.ProductionState
import com.lernhero.domain.town.TownResources
import dev.gitlive.firebase.auth.FirebaseUser

interface PlayerRepository {
    fun getUserId(): String?
    /**
     * Zaklada dokument gracza po pierwszym zalogowaniu.
     *
     * [appearance] i [starterGift] pochodza wprost z ekranu tworzenia postaci. Dar startowy
     * ląduje od razu W PLECAKU I NA POSTACI - inaczej gracz wybieralby przedmiot, ktorego potem
     * musi szukac w ekwipunku, a pierwsza walka wygladalaby tak samo bez wzgledu na wybor.
     */
    suspend fun createPlayer(
        user: FirebaseUser?,
        name: String,
        character: HeroClass?,
        appearance: HeroAppearance = HeroAppearance(),
        starterGift: StarterGift? = null,
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
     * Nagroda-przedmiot (skrzynia po bossie) - dopisuje NOWA SZTUKE bez zadnego kosztu.
     *
     * Duplikaty sa dozwolone: druga kopia to osobny egzemplarz, ktory mozna zalozyc innemu
     * kompanowi albo ulepszyc niezaleznie od pierwszej.
     */
    suspend fun grantItem(itemId: String): Player?

    /** Nagroda-kompan. Duplikaty sa dozwolone, wiec to zawsze sie udaje (poza bledem zapisu). */
    suspend fun grantMonster(monsterId: String): Player?

    /**
     * Wsypuje zestaw testowy ([com.lernhero.domain.player.AdminKit]) - wszystkie przedmioty, wysoki
     * poziom, zaliczona kampania. Zwraca null takze wtedy, gdy konto NIE jest na liscie
     * [com.lernhero.domain.player.AdminAccounts], wiec wywolanie tego z UI przez zwyklego gracza
     * nic nie robi.
     */
    suspend fun grantAdminKit(): Player?

    /**
     * Wykuwa przedmiot w Kuzni: sprawdza surowce I zloto, odejmuje jedno i drugie, dopisuje NOWA
     * SZTUKE oraz podbija licznik [com.lernhero.domain.progress.GameStats.itemsCrafted]. Wszystko
     * jednym zapisem - inaczej dalo by sie stracic surowce bez przedmiotu (tak dzialala stara kuznia).
     *
     * Kazde wywolanie daje kolejny egzemplarz - dziesiec wykuc to dziesiec noszalnych pazurow.
     */
    suspend fun craftItem(itemId: String, cost: CraftCost): Player?

    /**
     * Podnosi KONKRETNA SZTUKE o jeden poziom ulepszenia (patrz
     * [com.lernhero.domain.craft.ItemUpgrades]). [uid] to identyfikator egzemplarza
     * ([com.lernhero.domain.item.ItemInstance.uid]), a nie id z katalogu - dzieki temu ulepszenie
     * jednego nozyka nie podnosi wszystkich pozostalych.
     *
     * Zwraca null, gdy gracza nie stac, sztuki nie ma w plecaku albo stoi juz na maksie.
     *
     * Koszt liczy repozytorium z zapisanego poziomu, a nie z ekranu - inaczej dwa szybkie
     * tapniecia platilyby dwa razy za ten sam, najtanszy poziom.
     */
    suspend fun upgradeItem(uid: String): Player?

    /**
     * Przyzywa kompanow w Summon Center - zuzywa losy/esencje i ZAPISUJE ich do kolekcji.
     *
     * Lista, a nie pojedynczy potwor, bo summon x10 musi byc JEDNYM zapisem z JEDNYM lacznym
     * kosztem. Dziesiec osobnych wywolan sprawdzaloby stan sprzed poprzedniego zapisu i czesc
     * potworow po prostu by przepadla.
     */
    suspend fun summonMonsters(monsterIds: List<String>, cost: CraftCost): Player?

    /**
     * Odbiera DARMOWY summon danego bannera: dopisuje kompana i stempluje czas odbioru, bez kosztu.
     *
     * Gotowosc sprawdza repozytorium ([com.lernhero.domain.summon.FreeSummon]), a nie ekran - inaczej
     * dwa szybkie tapniecia albo cofniecie zegara w telefonie dawaloby dwa darmowe losowania.
     * Zwraca null, gdy licznik jeszcze nie doszedl do zera.
     */
    suspend fun claimFreeSummon(bannerId: String, cooldownMs: Long, monsterId: String, now: Long): Player?


    /**
     * Rozlicza cale zejscie JEDNYM zapisem: zloto, surowce, XP (razem z awansami i punktami
     * statystyk) i liczniki statystyk. Te rzeczy zawsze zmieniaja sie razem, wiec osobne wywolania
     * tylko mnozylyby okazje do rozjechania sie stanu.
     */
    suspend fun applyRunResult(run: RunStats): Player?

    /**
     * Odbiera nagrode za zdobyte osiagniecie: dopisuje zloto i stempluje id jako odebrane.
     *
     * Warunek "zdobyte i jeszcze nieodebrane" sprawdza REPOZYTORIUM, a nie ekran - liczy go z
     * zapisanych [com.lernhero.domain.progress.GameStats], wiec dwa szybkie tapniecia nie zaplaca
     * dwa razy, i nie da sie odebrac nagrody za cel, do ktorego gracz nie doszedl. Kwota tez
     * pochodzi z katalogu, a nie z UI - inaczej ekran moglby zazadac dowolnej.
     *
     * Zwraca null, gdy nagrody nie wolno wyplacic (nieznane id, cel nieosiagniety, juz odebrane).
     */
    suspend fun claimAchievement(achievementId: String): Player?

    /**
     * Osadza kamien z sakiewki w sztuce z plecaka bohatera albo kompanow. Null, gdy nie ma kamienia,
     * sztuki albo wolnego slotu - reguly liczy [com.lernhero.domain.jewel.JewelBench].
     */
    suspend fun socketJewel(uid: String, jewelId: String): Player?

    /** Wyjmuje kamien ze slotu [index] i oddaje go do sakiewki. Darmowe. */
    suspend fun unsocketJewel(uid: String, index: Int): Player?

    /** Laczy [com.lernhero.domain.jewel.Jewels.COMBINE_COUNT] kamienie w jeden szlifu wyzej. */
    suspend fun combineJewels(jewelId: String): Player?

    /** Rozdaje jeden punkt statystyki. Zwraca null, gdy nie ma nierozdanych punktow. */
    suspend fun spendStatPoint(stat: HeroStat): Player?

    /**
     * Dokłada jeden stopien talentu. Zwraca null, gdy regul nie da sie spelnic - progu rzedu,
     * rodzica albo braku punktu pilnuje [com.lernhero.domain.talent.TalentRules], a nie ekran,
     * bo to samo zadanie moze przyjsc z pominieciem UI.
     *
     * Zaklecia licza sie ZAWSZE z calego drzewka ([com.lernhero.domain.talent.TalentRules.spellChances]),
     * wiec nie ma czego dopisywac do zapisu osobno.
     */
    suspend fun spendTalentPoint(talentId: String): Player?

    /**
     * Zeruje drzewko i oddaje WSZYSTKIE wydane punkty. Zaklecia odblokowane talentami znikaja
     * razem z nimi - inaczej reset byłby darmowym sposobem na zatrzymanie czarow.
     */
    suspend fun resetTalents(): Player?

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

    /**
     * Zapisuje CALY ekwipunek jednego kompana naraz (mapa slot -> id przedmiotu).
     *
     * Caly, a nie slot po slocie, bo auto-equip zmienia szesc slotow jednoczesnie - szesc
     * osobnych zapisow czytaloby stan sprzed poprzedniego i czesc zmian by przepadla.
     */
    suspend fun updateMonsterEquipment(monsterId: String, equipment: Map<String, String>): Player?

    /**
     * Odbiera nagrode za cel dzienny (fitness albo czytanie) na dany dzien. Zwraca null, gdy
     * nagroda za ten cel byla juz odebrana dzisiaj (albo gdy zapis sie nie udal) - w obu
     * przypadkach nie placi drugi raz.
     */
    suspend fun claimDailyGoalReward(goalId: String, dateKey: String, reward: Int, gems: Int = 0, tickets: Int = 0): Player?

    /**
     * Zloto za obejrzana reklame. Null, gdy dzienny limit [maxPerDay] jest juz wyczerpany (albo
     * zapis sie nie udal) - wtedy nic nie placi.
     */
    suspend fun claimAdGold(dateKey: String, maxPerDay: Int, reward: Int): Player?

    /**
     * Placi za otwarcie skrzyni ze sklepu: diamentami ([withAd] = false) albo zapisuje zuzyta
     * reklame. Null, gdy brakuje diamentow, limit reklam jest wyczerpany albo zapis sie nie udal.
     * Sama nagroda przyznawana jest osobno, po animacji otwarcia.
     */
    /**
     * Wydaje zawartosc Legend Packa po zakupie. Idempotentne: gdy pakiet juz wydano, zwraca gracza
     * bez zmian - ponowne przywrocenie zakupu nie dubluje nagrod.
     */
    suspend fun claimLegendPack(): Player?

    /**
     * Wydaje diamenty zakupionego pakietu. Pierwszy zakup danego pakietu jest podwojony
     * ([com.lernhero.domain.monetization.GemPack.grantFor]) - o tym, czy to pierwszy, decyduje zapis.
     */
    suspend fun grantGemPack(pack: com.lernhero.domain.monetization.GemPack): Player?

    /**
     * Zakup za diamenty: odejmuje [gems] i dopisuje [bundle] w JEDNYM zapisie. Gdy [dealId] nie jest
     * null, zakup liczy sie do dziennego licznika tej oferty i nie przejdzie ponad [maxPerDay].
     * Null, gdy brakuje diamentow, limit jest wyczerpany albo zapis sie nie udal.
     */
    suspend fun spendGems(
        gems: Int,
        bundle: com.lernhero.domain.monetization.Bundle,
        now: Long,
        dealId: String? = null,
        dateKey: String = "",
        maxPerDay: Int = 0
    ): Player?

    /**
     * Otwarcie skrzyni ze sklepu: zaplata (diamenty albo licznik reklam) i CALA zawartosc w jednym
     * zapisie - zabicie aplikacji w trakcie animacji nie moze zabrac diamentow bez nagrody.
     */
    suspend fun openShopChest(
        opening: com.lernhero.domain.monetization.ChestOpening,
        withAd: Boolean,
        dateKey: String,
        now: Long
    ): Player?

    /** Codzienne diamenty subskrybenta - raz dziennie, pilnowane przez zapis. */
    suspend fun claimPremiumGems(dateKey: String): Player?

    /**
     * Dopisuje kawalek aktywnego czytania: minuty/strony na dzisiaj (resetuja sie same, gdy
     * [dateKey] zmieni sie na kolejny dzien) i sumaryczne statystyki na zawsze.
     */
    suspend fun recordReading(
        dateKey: String,
        secondsDelta: Int,
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
     * Odnotowuje jedna odpowiedz na fiszke: seria, liczniki i ewentualny awans do archiwum
     * "opanowane" (patrz [com.lernhero.domain.reading.VocabularyDeck.recordAnswer]). Wolane z walki
     * po kazdym pytaniu ze slowek, wiec musi byc tanie i odporne na nieznane id (fiszka mogla
     * zostac usunieta w trakcie walki) - nieznane id nie jest bledem, po prostu nic nie zmienia.
     */
    suspend fun recordWordAnswer(wordId: String, correct: Boolean, now: Long): Player?

    /* ---- Wlasne talie pytan (endless "Wlasne") ---- */

    /**
     * Talie gracza. Zyja w PODKOLEKCJI `player/{uid}/decks`, a nie w dokumencie gracza: setka
     * pytan wygenerowana przez AI to kilkadziesiat kilobajtow, a dokument gracza i tak niesie juz
     * fiszki, kolekcje kompanow i caly postep. Stad tez osobne wczytywanie zamiast pola w [Player].
     */
    suspend fun getCustomDecks(): List<CustomDeck>

    /** Zapisuje talie w calosci (nowa albo zmieniona) - zwraca false, gdy zapis sie nie udal. */
    suspend fun saveCustomDeck(deck: CustomDeck): Boolean

    suspend fun deleteCustomDeck(deckId: String): Boolean

    /** Podpina okladke do ksiazki w bibliotece - [coverLocator] wskazuje na plik wybrany przez gracza. */
    suspend fun setBookCover(bookId: String, coverLocator: String): Player?

    /** Usuwa ksiazke wgrana przez gracza z biblioteki (bundlowanych nie da sie usunac). */
    suspend fun removeImportedBook(bookId: String): Player?

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
        stone: Int,
        iron: Int,
        essence: Int,
        tickets: Int,
        xp: Int,
        gems: Int = 0
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

    /**
     * Placi [cost] energii wedlug stanu na [now] - albo zwraca null, gdy nie ma z czego.
     *
     * Naliczenie tego, co naroslo od ostatniego razu, dzieje sie TUTAJ, przy zapisie: gdyby
     * liczyl je ekran, dwa urzadzenia zalogowane na to samo konto doliczylyby ten sam przyrost
     * dwa razy. Patrz [com.lernhero.domain.energy.EnergyRules].
     */
    suspend fun spendEnergy(cost: Int, now: Long): Player?
}
