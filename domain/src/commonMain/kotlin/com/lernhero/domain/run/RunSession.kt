package com.lernhero.domain.run

import com.lernhero.domain.combat.Stats
import com.lernhero.domain.quiz.QuizCategory
import kotlin.math.roundToInt

/**
 * Bonusy zebrane w trakcie jednego zejscia do lochu - z obozowisk i perkow. Licza sie **tylko**
 * do konca runu; nowy run startuje z czystym kontem.
 *
 * [spellWeightOverride] pozwala przemeblowac pasek zdolnosci przy ognisku, nie ruszajac zapisu
 * z ekwipunku. Null = uzyj tego, co gracz ustawil w EQ.
 */
data class RunModifiers(
    val bonusSpellCapacity: Int = 0,
    val bonusAttack: Int = 0,
    val bonusMaxHp: Int = 0,
    val bonusSpellPower: Float = 0f,
    val spellWeightOverride: Map<String, Int>? = null,
    /**
     * Ogolne bonusy statystyk (szansa na kryta, unik, kradziez zycia, redukcja obrazen...)
     * zebrane z quizow przed walkami - kazdy komplet odpowiedzi dopisuje tu troche na caly run.
     */
    val bonusStats: Stats = Stats.Empty
)

/**
 * Trzyma stan biezacego runu. Jeden obiekt na aplikacje (wstrzykiwany przez Koin): mapa lochu go
 * zeruje na starcie, ogniska go modyfikuja, a walka go czyta. Dzieki temu perki z ogniska docieraja
 * do bitwy bez przepychania ich przez nawigacje.
 */
class RunSession {

    var modifiers: RunModifiers = RunModifiers()
        private set

    /** Wybrana kategoria pytan na ten run. Null = losowe pytania z calego banku. */
    var category: QuizCategory? = null
        private set

    /** Poziom trudnosci runu - skaluje wrogow, pytania i nagrody. */
    var difficulty: RunDifficulty = RunDifficulty.Normal
        private set

    /**
     * Zamowienie na najblizsza walke, skladane tuz przed nawigacja do [BattleRequest] - dziejsza
     * to konkretny wezel lochu (patrz [pendingDungeonSeed]), nie cala lokacja z gory.
     * Walka je czyta; null oznacza stara sciezke "walka bez mapy".
     */
    var pendingBattle: BattleRequest? = null

    /**
     * Seed lochu wybranego na mapie krainy - mapa lochu ([com.lernhero.domain.dungeon.DungeonMapGenerator])
     * czyta go, zeby wygenerowac ten sam uklad wezlow za kazdym razem (np. po obrocie ekranu).
     * Null = wejscie bez mapy krainy (np. test) - ekran lochu wtedy losuje swoj wlasny seed.
     */
    var pendingDungeonSeed: Long? = null

    /** Wynik ostatniej walki: true = zwyciestwo, false = porazka, null = jeszcze zadnej. */
    var lastBattleWon: Boolean? = null

    /**
     * HP druzyny na koniec ostatniej walki (id postaci -> HP). Puste = jeszcze nikt sie nie bil
     * w tym runie, wiec kolejna walka startuje w pelni zdrowia. Dzieki temu rany nie znikaja
     * miedzy polami mapy - leczy tylko ognisko.
     */
    var partyHp: Map<String, Int> = emptyMap()
        private set

    /**
     * Lup zebrany w tym zejsciu (na razie samo zloto), jeszcze niezaksiegowany na koncie gracza.
     * Ksieguje sie dopiero przy bezpiecznym opuszczeniu lochu albo pokonaniu bossa - dzieki temu
     * przegrana albo kapitulacja moga kosztowac czesc lupu, tak jak w Darkest Dungeon.
     */
    var lootGold: Int = 0
        private set

    /** Nowe zejscie do lochu - kasuje wszystkie perki z poprzedniego. */
    fun startNewRun(
        category: QuizCategory? = null,
        difficulty: RunDifficulty = RunDifficulty.Normal
    ) {
        modifiers = RunModifiers()
        this.category = category
        this.difficulty = difficulty
        pendingBattle = null
        pendingDungeonSeed = null
        lastBattleWon = null
        partyHp = emptyMap()
        lootGold = 0
    }

    fun apply(change: (RunModifiers) -> RunModifiers) {
        modifiers = change(modifiers)
    }

    fun overrideSpellWeights(weights: Map<String, Int>) {
        modifiers = modifiers.copy(spellWeightOverride = weights)
    }

    /** Zapamietuje HP druzyny po walce - kolejna walka odczyta je zamiast leczyc do pelna. */
    fun savePartyHp(hp: Map<String, Int>) {
        partyHp = hp
    }

    /** Ognisko: pelny odpoczynek. Kolejna walka znowu zacznie sie w pelni zdrowia. */
    fun healParty() {
        partyHp = emptyMap()
    }

    /** Dopisuje zdobycz do sakwy - jeszcze nie na konto, tylko "w plecaku" na czas runu. */
    fun addLoot(amount: Int) {
        if (amount > 0) lootGold += amount
    }

    /**
     * Ksieguje czesc sakwy na konto gracza i czysci ja do zera. [fraction] = 1.0 przy bezpiecznym
     * wyjsciu z lochu (albo zabiciu bossa), 0.25 przy porazce lub kapitulacji w trakcie walki.
     */
    fun settleLoot(fraction: Float): Int {
        val amount = (lootGold * fraction).roundToInt().coerceAtLeast(0)
        lootGold = 0
        return amount
    }
}
