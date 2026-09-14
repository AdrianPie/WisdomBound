package com.lernhero.game.battle

import com.lernhero.domain.combat.live.CycleOutcome
import com.lernhero.domain.quiz.QuizCategory
import com.lernhero.domain.combat.live.StreakTracker
import com.lernhero.domain.combat.playback.HitKind
import com.lernhero.domain.item.Item
import com.lernhero.domain.progress.RunStats
import com.lernhero.domain.monster.Element
import com.lernhero.domain.quiz.CategoryOffer
import com.lernhero.domain.quiz.EstimationEntry
import com.lernhero.domain.run.EndlessBuff
import com.lernhero.game.character.CharacterUiModel
import com.lernhero.shared.resources.Resources
import org.jetbrains.compose.resources.DrawableResource

typealias CharacterId = String

/**
 * Ulotny efekt bojowy przypiety do walczacego: pocisk lecacy od [sourceId] do [targetId], a potem
 * wybuch i pływajaca liczba obrazen na celu. [sourceId] = null oznacza efekt bez zrodla (np. ulti
 * bijace w cala fale naraz) - wtedy leci prosto do wybuchu, bez lotu pocisku.
 *
 * [id] rosnie monotonicznie, wiec dwa te same trafienia nie zleja sie w jeden efekt.
 */
data class FloatingEffect(
    val id: Long,
    val targetId: String,
    val amount: Int,
    val kind: HitKind,
    val sourceId: String? = null
)

enum class GamePhase {
    /**
     * Wejscie na pole bitwy: najpierw wchodza nasi bohaterowie, potem wrogowie, dopiero na koncu
     * pojawia sie quiz (patrz [com.lernhero.game.battle.BattleViewModel.playBattleIntro]). Tylko
     * przy starcie walki - kolejne fale (spawnNextWave) NIE wracaja do tej fazy, wpuszczaja
     * wrogow od razu jak dotychczas. Dopoki trwa, pole bitwy zajmuje caly ekran (tak samo jak przy
     * [Defeat]/[Victory] - patrz BattleScreen.kt) i quiz jest zablokowany kazdym `phase != Battle` guardem.
     */
    Intro,
    /** Fale nadciagaja az do [com.lernhero.domain.run.BattleRequest.waves], quiz karmi dopalacz i passe. */
    Battle,
    /** Cala druzyna padla - walka skonczona, czas rozliczyc lup. */
    Defeat,
    /** Ostatnia fala oczyszczona - walka wygrana, caly lup wraca na konto. */
    Victory
}

/**
 * Faza biezacej tury: [CategoryPick] - gracz wybiera temat nadchodzacego pytania (tryb story,
 * patrz [CategoryOffer]); [Question] - walka stoi, czeka na odpowiedz, dol ekranu jest rozszerzony;
 * [Impact] - odpowiedz padla, pole bitwy JESZCZE stoi, a na srodku gra sie jej rozliczenie
 * (patrz TurnImpactOverlay); [Resolving] - dopiero teraz druzyny wymieniaja ciosy.
 *
 * [Impact] jest osobnym taktem celowo: wczesniej salwa ruszala w tej samej chwili, w ktorej
 * pokazywal sie wynik odpowiedzi, wiec jedno zaslanialo drugie i nie bylo widac, co ta odpowiedz
 * wlasciwie dala.
 */
enum class TurnPhase { CategoryPick, Question, Impact, Resolving }

/** Efekt zamknietego cyklu dziesieciu odpowiedzi - trzyma sie przez [expiresAtMs] (czas gry). */
data class CycleBuffState(
    val outcome: CycleOutcome,
    val expiresAtMs: Long
)

/**
 * Skutek OSTATNIEJ odpowiedzi, gotowy do pokazania na polu bitwy. Wczesniej gracz widzial tylko
 * jednolinijkowe `message` w pasku na dole i nie mial jak zauwazyc, co konkretnie dala mu dobra
 * odpowiedz - stad tu komplet liczb, ktore i tak juz policzyl BattleViewModel.
 *
 * [id] rosnie z kazda odpowiedzia, zeby dwie takie same z rzedu odpalily animacje dwa razy.
 */
data class TurnOutcome(
    val id: Long,
    val correct: Boolean,
    /** Mnoznik sily salwy tej tury (szybkosc odpowiedzi x buff cyklu, albo kara za bledna). */
    val damageMultiplier: Float,
    val reward: Int,
    val ultiTriggered: Boolean,
    /** Etykieta zamknietego cyklu dziesieciu odpowiedzi, jesli akurat sie domknal. */
    val cycleLabel: String? = null,
    /** Po ilu sekundach padla odpowiedz - to z niej bierze sie [speedMultiplier]. */
    val answerSeconds: Float = 0f,
    /** Sam mnoznik z paska czasu (0.5-1.5), bez buffow. */
    val speedMultiplier: Float = 1f,
    /** Mnoznik z aktywnego buffa/debuffa cyklu (1.35 / 0.75 / 1.0). */
    val cycleMultiplier: Float = 1f,
    /** Suma ataku ZYWYCH czlonkow druzyny przed mnoznikiem - lewa strona rownania na overlayu. */
    val baseAttack: Int = 0,
    /** [baseAttack] po przemnozeniu przez [damageMultiplier] - prawa strona rownania. */
    val boostedAttack: Int = 0,
    /** O ile mocniej (albo slabiej) bije druzyna w tej turze, w procentach: +36, -45... */
    val attackDeltaPercent: Int = 0
)

data class BattleUiState(
    val phase: GamePhase = GamePhase.Battle,
    /** Faza biezacej tury - steruje tym, czy dol ekranu (quiz) czy gora (pole bitwy) jest rozszerzona. */
    val turnPhase: TurnPhase = TurnPhase.Question,
    /** Numer biezacej fali - rosnie bez konca, to endless run. */
    val wave: Int = 1,
    val rewardCoins: Int = 0,
    val rewardSaved: Boolean = false,
    /** Suma punktow z poprawnych odpowiedzi w tym locie - przeklada sie na monety na koniec. */
    val totalScore: Int = 0,
    /** Ile pytan pojawilo sie w tym locie do tej pory - licznik pokazywany w gornym rogu HUD. */
    val answeredCount: Int = 0,
    /**
     * Ile z nich bylo poprawnych. Razem z [answeredCount] decyduje o tym, jak dobre ulepszenia
     * wypadaja w endless mode - patrz [com.lernhero.domain.run.EndlessBuff.qualityFor].
     */
    val correctCount: Int = 0,
    val elapsedMs: Long = 0L,
    val characters: Map<CharacterId, CharacterUiModel> = emptyMap(),
    /** Dziesiec kratek ostatnich odpowiedzi + biezaca seria z rzedu (dla ulti). */
    val streak: StreakTracker = StreakTracker(),
    /** Buff/debuff z ostatnio zamknietego cyklu dziesieciu odpowiedzi, jesli jeszcze aktywny. */
    val activeCycleBuff: CycleBuffState? = null,
    /** Skutek ostatniej odpowiedzi - pokazywany jako baner na polu bitwy. */
    val turnOutcome: TurnOutcome? = null,
    /** Rosnace id - kazda zmiana odpala na ekranie krotki rozblysk ulti. Zero = jeszcze nic. */
    val ultiPulseId: Long = 0L,
    val quiz: LiveQuizState? = null,
    /** Pływajace liczby obrazen czekajace na odegranie. */
    val floatingEffects: List<FloatingEffect> = emptyList(),
    val message: String? = null,
    /** Ile jeszcze tur druzyna stoi oszolomiona po przegranym zakladzie z bossem - patrz [BossEventState]. */
    val stunTurnsRemaining: Int = 0,
    /** Ktorzy gracze akurat "kreca kolkami nad glowa" - tylko oni stoja bezczynnie w stunned turach. */
    val stunnedCharacterIds: Set<CharacterId> = emptySet(),
    /** Specjalne zdarzenie elity/bossa w trakcie zamiast zwyklego ciosu tej tury - patrz [BossEventState]. */
    val bossEvent: BossEventState? = null,
    /**
     * Cztery kafle tematow do wyboru przed tura (tryb story) - dopoki nie jest null, walka czeka
     * na decyzje gracza i nie ma jeszcze pytania. Patrz [com.lernhero.domain.quiz.CategoryOffer].
     */
    val categoryOffer: CategoryOffer? = null,
    /**
     * Zywioly wzmocnione na TE ture wybranym kaflem - jednostki gracza o tym zywiole bija mocniej
     * (patrz [com.lernhero.domain.quiz.CategoryOffer.ELEMENT_BOOST]). Czysci sie z kazda nowa tura.
     */
    val boostedElements: Set<Element> = emptySet(),
    /**
     * Bonusy przyznane w endless mode (patrz [com.lernhero.domain.run.BattleRequest.isEndless]) -
     * bonus -> liczba stakow. Zawsze puste poza endless. Puste = zaden jeszcze nie wypadl.
     */
    val endlessBuffs: Map<EndlessBuff, Int> = emptyMap(),
    /**
     * Ile ciosow moze jeszcze zablokowac kazdy bohater (patrz [EndlessBuff.Guard]/[EndlessBuff.Aegis]).
     * Odnawiane na starcie tury, a przy Egidzie - na starcie kazdej rundy.
     */
    val guardCharges: Map<CharacterId, Int> = emptyMap(),
    /** Ile razy w TEJ fali kazdy bohater byl juz uratowany przez [EndlessBuff.SecondWind]. */
    val secondWindUses: Map<CharacterId, Int> = emptyMap(),
    /** Wybor ulepszenia czekajacy na decyzje gracza - dopoki jest, walka stoi. */
    val upgradeOffer: UpgradeOffer? = null,
    /**
     * Komplet fal endless zebrany - temat awansowal i gracz decyduje, czy wychodzi z nagroda, czy
     * idzie dalej. Dopoki to lezy, walka stoi (tak samo jak przy [upgradeOffer]).
     */
    val masteryOffer: MasteryOffer? = null,
    /**
     * Podsumowanie calego zejscia - wypelniane dopiero w chwili zakonczenia walki i pokazywane na
     * ekranie koncowym (patrz BattleSummaryScreen). Null dopoki walka trwa.
     */
    val runSummary: RunStats? = null,
    /**
     * Przedmiot, ktory wypadl z tej walki, albo null gdy nic nie wypadlo (patrz
     * [com.lernhero.domain.story.StageLoot.roll]). Trzymany jako caly [Item], a nie samo id, bo
     * ekran koncowy pokazuje nazwe, rzadkosc i ikone - a nie ma sensu odpytywac katalogu z UI.
     */
    val droppedItem: Item? = null,
    /** Ile poziomow bohater zdobyl w tym zejsciu - ekran koncowy pokazuje z tego "AWANS". */
    val levelsGained: Int = 0,
    /**
     * Tlo pola bitwy. W kampanii to grafika krainy, w ktorej sie walczy (rozwiazane z
     * [com.lernhero.domain.run.RunSession.activeStoryStage]); w endless/boss-test - domyslne
     * [com.lernhero.shared.resources.Resources.Image.battlegroundFirst], bo tam nie ma krainy.
     */
    val battleBackground: DrawableResource = Resources.Image.battlegroundFirst
)

/**
 * Karty ulepszen wylozone po oczyszczonej fali (endless mode). Gracz bierze dokladnie jedna, reszta
 * przepada - klasyczny roguelike'owy wybor. [quality] to jakosc dotychczasowego quizu, ktora
 * wylosowala te konkretna oferte; pokazujemy ja na panelu, zeby bylo widac, ze dobre odpowiedzi
 * realnie podnosza poziom kart.
 */
data class UpgradeOffer(
    val wave: Int,
    val choices: List<EndlessBuff>,
    val quality: Float,
    val ownedStacks: Map<EndlessBuff, Int> = emptyMap()
)

/**
 * Moment awansu tematu: komplet fal ([com.lernhero.domain.progress.CategoryMastery.WAVES_PER_LEVEL])
 * zebrany. Awans jest juz PEWNY - zapisze sie niezaleznie od tego, czym skonczy sie reszta biegu -
 * a gracz wybiera tylko, czy wychodzi z pelna sakwa, czy gra dalej.
 *
 * Sens tego wyboru jest w [com.lernhero.domain.run.RunSession.settleLoot]: wyjscie teraz ksieguje
 * caly lup, a dalsza gra stawia go pod zastaw - porazka odda juz tylko cwierc.
 */
data class MasteryOffer(
    val category: QuizCategory,
    val wave: Int,
    /** Poziom, ktory temat wlasnie osiagnal. */
    val newLevel: Int,
    /** Ile zlota lezy w sakwie w tej chwili - to jest stawka decyzji. */
    val bagGold: Int
)

/** Aktualnie aktywny mnoznik z zamknietego cyklu dziesieciu odpowiedzi (Empowered/Weakened/Neutral), jesli jeszcze trwa. */
fun BattleUiState.activeCycleMultiplier(): Float =
    activeCycleBuff?.takeIf { elapsedMs < it.expiresAtMs }?.outcome?.attackMultiplier ?: 1f

/** Co elita/boss szykuje zamiast zwyklego ciosu - flavor zdarzenia, patrz [BattleUiState.resolveBossEvent][com.lernhero.game.battle.BattleViewModel]. */
enum class BossEventKind { Heal, Poison, StunGamble, Strike }

/**
 * Specjalne zdarzenie elity/bossa zamiast zwyklego ciosu tej tury - walka staje, dol ekranu
 * pokazuje ten sam suwak-quiz co StunGamble ([com.lernhero.game.battle.component.BossEventQuizBar]),
 * niezaleznie od [kind]. Kluczowe: boss NIE robi nic, dopoki gracz nie zatwierdzi zgadywania - wynik
 * (ile leczenia, ile trucizny, ile ogluszenia) liczy sie DOPIERO wtedy, skalowany tym, jak dobrze
 * poszlo (patrz [BossEventOutcome]). Im lepiej gracz odgadnie i im szybciej zatwierdzi, tym slabszy
 * efekt - fatalny quiz to najgorszy mozliwy skutek danego zdarzenia.
 */
data class BossEventState(
    val bossId: String,
    val kind: BossEventKind,
    val question: EstimationEntry,
    /**
     * Widelki suwaka wylosowane dla TEJ rozgrywki pytania (patrz [EstimationEntry.sliderRange]) -
     * waskie okno wokol poprawnej wartosci, przesuniete losowo, zeby odpowiedz nie siedziala
     * zawsze na srodku. Bez tego suwak obejmowal cala epoke i trafienie w rok bylo loteria.
     */
    val sliderMin: Int = question.minValue,
    val sliderMax: Int = question.maxValue,
    val guess: Int = (sliderMin + sliderMax) / 2,
    val resolved: Boolean = false,
    val outcome: BossEventOutcome? = null,
    /** Na ktorym etapie inscenizacji jestesmy - patrz [BossEventPhase]. */
    val phase: BossEventPhase = BossEventPhase.Charging,
    /**
     * NAJGORSZY mozliwy skutek tego zdarzenia (przy fatalnym quizie). Liczony z gory, w chwili
     * odpalenia, i pokazywany rosnacym licznikiem juz podczas ladowania - dzieki temu gracz WIE,
     * o jaka stawke gra, zanim odpowie, a po odpowiedzi widzi, ile z tego udalo mu sie sciac.
     */
    val projection: BossEventProjection
)

/**
 * Etapy inscenizacji zdarzenia bossa. Wczesniej wszystko dzialo sie naraz i nie bylo widac, co
 * wlasciwie zaszlo - kazdy etap ma teraz wlasny, wyrazny obraz na ekranie i wlasny czas trwania.
 */
enum class BossEventPhase {
    /** Boss laduje atak: zbliza sie do kamery, tlo ciemnieje, iskry lecą w gore, licznik rosnie. Bez quizu. */
    Charging,

    /** Panel quizu wjezdza od dolu, licznik dalej pulsuje, leci czas na odpowiedz. */
    Quiz,

    /** Werdykt: poprawna wartosc kontra zgadywana, ocena celnosci i redukcja - licznik SPADA na oczach gracza. */
    Judging,

    /** Kulminacja: kula z pyska leci w cel (albo wybucha na samym bossie, gdy sie leczy). */
    Payoff
}

/**
 * Zapowiedz skutku zdarzenia - to, co widac na liczniku podczas ladowania.
 *
 * [worstValue] to skutek przy fatalnym quizie (performance = 0), czyli gorna granica tego, co
 * gracz moze jeszcze utracic. Po rozstrzygnieciu licznik zjezdza z niej do faktycznego wyniku.
 */
data class BossEventProjection(
    /** Nazwa ataku pokazywana nad licznikiem, np. "TRUJACY ODDECH". */
    val attackName: String,
    /** Co mierzy licznik, np. "OBRAZENIA" albo "OGLUSZENIE". */
    val valueLabel: String,
    /**
     * Ile zaklecie urosnie, jesli gracz przeczeka CALY czas na odpowiedz, przy neutralnej celnosci
     * (mnoznik 1.0). Licznik na ekranie pnie sie do tej wartosci przez cale ladowanie i quiz - to
     * jest to "zaklecie sie laduje, im dluzej zwlekasz, tym mocniej oberwiesz".
     *
     * Wynik koncowy MOZE ja przekroczyc: kiepska odpowiedz mnozy zaklecie w gore (patrz
     * [BossEventOutcome.accuracyMultiplier]).
     */
    val chargedValue: Int,
    /** Sufiks liczby - "" dla obrazen, "%" dla ogluszenia. */
    val unit: String = ""
)

/**
 * Wynik zdarzenia bossa PO zatwierdzeniu quizu. [performance] (0..1, 0 = fatalnie, 1 = idealnie) to
 * srednia szybkosci (ile paska zostalo) i celnosci zgadywania - to ona skaluje kazdy z ponizszych
 * efektow. Tylko pola pasujace do [BossEventState.kind] sa uzywane; reszta zostaje na wartosci domyslnej.
 */
data class BossEventOutcome(
    val performance: Float,
    val message: String,
    /**
     * O ile odpowiedz przemnozyla naladowane zaklecie. **Ponizej 1.0 = gracz oslabil cios,
     * powyzej 1.0 = wzmocnil.** To jest sedno mechaniki: zla odpowiedz nie jest "brakiem zniżki",
     * tylko realnie DOKLADA obrazen - stad na ekranie werdyktu widac i "-40%", i "+190%".
     */
    val accuracyMultiplier: Float = 1f,
    /** Ile zaklecie naladowalo z samego czasu, zanim odpowiedz je przemnozyla - baza dla "%". */
    val chargedValue: Int = 0,
    /** [BossEventKind.Heal] - juz naliczone i zastosowane HP odzyskane przez bossa. */
    val healAmount: Int = 0,
    /** [BossEventKind.Poison] - juz naliczona i zastosowana suma obrazen na cala druzyne. */
    val poisonDamage: Int = 0,
    /** [BossEventKind.Strike] - juz naliczona i zastosowana suma obrazen z poteznego ciosu. */
    val strikeDamage: Int = 0,
    /** [BossEventKind.StunGamble] - jaka czesc druzyny zostala ogluszona (0 = zaklecie przerwane). */
    val affectedFraction: Float = 0f,
    val stunTurns: Int = 0,
    val interrupted: Boolean = false
)

/**
 * Jedno pytanie tury, podmieniane po kazdej rundzie ciosow. Pasek czasu na odpowiedz liczony jest
 * osobno w widoku (patrz [com.lernhero.game.battle.component.AnswerCountdownBar]) i w view modelu
 * (od momentu narysowania pytania) - tu trzymamy tylko sama tresc pytania i wynik.
 *
 * [orderedSelection] to id elementow [OrderingQuestion.items] w kolejnosci, w jakiej gracz je
 * klikal - dla [MultipleChoiceQuestion] zostaje puste, uzywane jest wtedy [selectedAnswerId].
 */
data class LiveQuizState(
    val question: QuizQuestion,
    val selectedAnswerId: String? = null,
    val orderedSelection: List<String> = emptyList(),
    /** Zaznaczone opcje w [MultiSelectQuestion] - pusty zbior dla pozostalych typow pytan. */
    val multiSelection: Set<String> = emptySet(),
    val revealed: Boolean = false
) {
    val correctAnswer: QuizAnswer?
        get() = (question as? MultipleChoiceQuestion)?.answers?.firstOrNull { it.isCorrect }

    val answeredCorrectly: Boolean
        get() = when (val q = question) {
            is MultipleChoiceQuestion -> selectedAnswerId != null && selectedAnswerId == correctAnswer?.id
            is OrderingQuestion -> orderedSelection.size == q.items.size &&
                orderedSelection == q.items.sortedBy { it.correctPosition }.map { it.id }
            // Zaliczamy TYLKO komplet: wszystkie poprawne i ani jednej blednej. Czesciowe
            // trafienie nie jest "prawie dobrze" - przy zaznaczaniu z listy latwo byloby
            // zaklikac wszystko i zawsze miec polowe punktow.
            is MultiSelectQuestion -> multiSelection == q.correctIds
        }
}

/**
 * Wspolny ksztalt pytania quizu - dwa warianty rozgrywki dzielace ten sam naglowek (tresc, kategoria,
 * poziom trudnosci, nagroda), ale rozne sposoby odpowiadania: [MultipleChoiceQuestion] to jeden
 * wybor z czterech, [OrderingQuestion] to ulozenie wszystkich elementow we wlasciwej kolejnosci.
 */
sealed interface QuizQuestion {
    val id: String
    val prompt: String

    /** "Trudne", "Eksperckie" - drabinka jak w Milionerach. */
    val difficulty: String
    val category: String
    val reward: Int
}

data class MultipleChoiceQuestion(
    override val id: String,
    override val prompt: String,
    val flag: DrawableResource? = null,
    val answers: List<QuizAnswer>,
    override val difficulty: String,
    override val category: String,
    override val reward: Int
) : QuizQuestion

data class QuizAnswer(
    val id: String,
    val text: String,
    val isCorrect: Boolean
)

/**
 * Pytanie "ulóz w kolejnosci": [items] jest juz przetasowane do wyswietlenia, a [OrderingItem.correctPosition]
 * (0-indeksowane) mowi, na ktorym miejscu w prawidlowej kolejnosci powinien wylladowac dany element.
 */
data class OrderingQuestion(
    override val id: String,
    override val prompt: String,
    val items: List<OrderingItem>,
    override val difficulty: String,
    override val category: String,
    override val reward: Int
) : QuizQuestion

data class OrderingItem(
    val id: String,
    val text: String,
    val correctPosition: Int
)

/**
 * Pytanie "zaznacz wszystkie prawdziwe" - gracz przelacza dowolna liczbe opcji i zatwierdza.
 * [options] jest juz przetasowane, wiec poprawne nie stoja obok siebie.
 */
data class MultiSelectQuestion(
    override val id: String,
    override val prompt: String,
    val options: List<MultiSelectOption>,
    override val difficulty: String,
    override val category: String,
    override val reward: Int
) : QuizQuestion {
    val correctIds: Set<String> get() = options.filter { it.isCorrect }.map { it.id }.toSet()
}

data class MultiSelectOption(
    val id: String,
    val text: String,
    val isCorrect: Boolean
)
