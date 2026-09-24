package com.lernhero.domain.run

import com.lernhero.domain.combat.Stats
import com.lernhero.domain.progress.CategoryMastery
import com.lernhero.domain.quiz.QuizCategory
import com.lernhero.domain.quiz.QuizDifficulty
import com.lernhero.domain.quiz.custom.CustomDeck
import com.lernhero.domain.reading.SavedWord
import kotlin.math.roundToInt

/**
 * Bonusy zebrane w trakcie jednego zejscia do lochu - z obozowisk i perkow. Licza sie **tylko**
 * do konca runu; nowy run startuje z czystym kontem.
 */
data class RunModifiers(
    val bonusAttack: Int = 0,
    val bonusMaxHp: Int = 0,
    val bonusSpellPower: Float = 0f,
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

    /**
     * Pytania zadane w tym uruchomieniu gry - wszystkie typy, wszystkie walki. Dzieki temu kolejna
     * plansza nie zaczyna od tych samych pytan co poprzednia. Patrz [com.lernhero.domain.quiz.QuestionHistory].
     */
    val questionHistory = com.lernhero.domain.quiz.QuestionHistory()

    var modifiers: RunModifiers = RunModifiers()
        private set

    /**
     * Poziom drabinki wypraw, na ktorym idzie ten bieg - patrz [RunLadder]. Zero, gdy bieg nie
     * jest wyprawa (kampania), bo tam o sile wrogow decyduje tier planszy i jej numer.
     */
    var ladderTier: Int = 0
        private set

    /** Wybrana kategoria pytan na ten run. Null = losowe pytania z calego banku. */
    var category: QuizCategory? = null
        private set

    /**
     * Staly poziom pytan wybrany przed walka (endless). Null = trudnosc rosnie sama z numerem fali
     * (drabinka [com.lernhero.domain.quiz.QuizBank.ladderFor]) - tak dziala kampania, gdzie o
     * poziomie pytan decyduje tier planszy, a nie gracz.
     */
    var quizDifficulty: QuizDifficulty? = null
        private set

    /**
     * Fiszki gracza do trenowania w wyprawie (wybor materialu - [com.lernhero.home.fight.MaterialSheet]).
     * Niepuste = walka pyta WYLACZNIE o te slowka (patrz [com.lernhero.domain.quiz.WordQuizBank]),
     * z pominieciem wbudowanego banku i pytan typu "ulóz w kolejnosci".
     */
    var customWords: List<SavedWord> = emptyList()
        private set

    /**
     * Wlasna talia pytan wybrana przed walka (endless -> "Wlasne" -> talia gracza). Niepusta =
     * walka pyta WYLACZNIE z niej, dokladnie tak samo jak przy fiszkach powyzej - patrz
     * [com.lernhero.domain.quiz.custom.CustomDeckBank].
     */
    var customDeck: CustomDeck? = null
        private set

    /** Poziom trudnosci runu - skaluje wrogow, pytania i nagrody. */
    var difficulty: RunDifficulty = RunDifficulty.Normal
        private set

    /**
     * Poziom opanowania tematu wybranego na to wejscie w endless (patrz
     * [com.lernhero.domain.progress.CategoryMastery]). Steruje startowa trudnoscia pytan, sila
     * wrogow i mnoznikiem nagrod. Poza endless zostaje na [CategoryMastery.FIRST_LEVEL] i nic
     * nie zmienia.
     */
    var categoryLevel: Int = CategoryMastery.FIRST_LEVEL
        private set

    /**
     * Zamowienie na najblizsza walke, skladane tuz przed nawigacja do [BattleRequest] - dla
     * kampanii to konkretna plansza mapy (patrz [com.lernhero.domain.story.StoryStage.seed]), nie
     * cala kraina z gory. Walka je czyta; null oznacza stara sciezke "walka bez mapy".
     */
    var pendingBattle: BattleRequest? = null

    /**
     * Globalny numer planszy kampanii aktualnie rozgrywanej walki - null poza kampania (np. endless).
     * Ustawiany razem z [startNewRun]; po zwyciestwie zapisuje sie jako zdobyty na [difficulty]
     * (patrz [com.lernhero.domain.story.StoryProgress]).
     */
    var activeStoryStage: Int? = null
        private set

    /** Wynik ostatniej walki: true = zwyciestwo, false = porazka, null = jeszcze zadnej. */
    var lastBattleWon: Boolean? = null

    /**
     * HP druzyny na koniec ostatniej walki (id postaci -> HP). Kazda plansza kampanii startuje
     * z pelnym HP (patrz [healParty], wolane przed kazda walka wywolana z mapy) - to pole ma wiec
     * znaczenie realnie tylko wewnatrz jednej wielofalowej walki (endless), nie miedzy planszami.
     */
    var partyHp: Map<String, Int> = emptyMap()
        private set

    /**
     * MaxHP druzyny na koniec ostatniej walki (id postaci -> maxHP), zapisywane razem z [partyHp].
     */
    var partyMaxHp: Map<String, Int> = emptyMap()
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
        difficulty: RunDifficulty = RunDifficulty.Normal,
        storyStage: Int? = null,
        quizDifficulty: QuizDifficulty? = null,
        customWords: List<SavedWord> = emptyList(),
        customDeck: CustomDeck? = null,
        categoryLevel: Int = CategoryMastery.FIRST_LEVEL,
        /** Poziom drabinki wypraw - patrz [ladderTier]. */
        ladderTier: Int = 0
    ) {
        modifiers = RunModifiers()
        this.category = category
        this.difficulty = difficulty
        this.activeStoryStage = storyStage
        this.quizDifficulty = quizDifficulty
        this.customWords = customWords
        this.customDeck = customDeck
        this.categoryLevel = categoryLevel
        this.ladderTier = ladderTier
        pendingBattle = null
        lastBattleWon = null
        partyHp = emptyMap()
        partyMaxHp = emptyMap()
        lootGold = 0
    }

    fun apply(change: (RunModifiers) -> RunModifiers) {
        modifiers = change(modifiers)
    }

    /** Zapamietuje HP/maxHP druzyny po walce - kolejna walka odczyta je zamiast leczyc do pelna. */
    fun savePartyHp(hp: Map<String, Int>, maxHp: Map<String, Int>) {
        partyHp = hp
        partyMaxHp = maxHp
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
