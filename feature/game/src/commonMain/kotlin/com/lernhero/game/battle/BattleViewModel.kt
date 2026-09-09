package com.lernhero.game.battle

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lernhero.domain.combat.StatType
import com.lernhero.domain.story.StageLoot
import com.lernhero.domain.story.StoryWorldCatalog
import com.lernhero.shared.resources.Resources
import com.lernhero.domain.combat.live.AnswerTimer
import com.lernhero.domain.combat.live.CYCLE_BUFF_DURATION_MS
import com.lernhero.domain.combat.live.record
import com.lernhero.domain.combat.playback.HitKind
import com.lernhero.domain.item.heroBag
import com.lernhero.domain.item.migratedHeroEquipment
import com.lernhero.domain.item.ItemInstance
import com.lernhero.domain.item.ItemSlot
import com.lernhero.domain.item.itemIn
import com.lernhero.domain.item.toLoadout
import com.lernhero.domain.monster.EnemySpecies
import com.lernhero.domain.monster.CompanionSpell
import com.lernhero.domain.monster.CompanionSpellKind
import com.lernhero.domain.monster.Element
import com.lernhero.domain.monster.MonsterCatalog
import com.lernhero.domain.monster.MonsterLevels
import com.lernhero.domain.monster.MonsterSpellLevels
import com.lernhero.domain.monster.MonsterStars
import com.lernhero.domain.player.HeroBaseStats
import com.lernhero.domain.player.HeroStats
import com.lernhero.domain.player.PartySlots
import com.lernhero.domain.player.PlayerEquipment
import com.lernhero.domain.progress.CategoryStat
import com.lernhero.domain.progress.RunStats
import com.lernhero.domain.quiz.CategoryOffer
import com.lernhero.domain.quiz.MultiSelectBank
import com.lernhero.domain.quiz.MultiSelectEntry
import com.lernhero.domain.quiz.CategoryOption
import com.lernhero.domain.quiz.EstimationBank
import com.lernhero.domain.quiz.OrderingBank
import com.lernhero.domain.quiz.OrderingEntry
import com.lernhero.domain.quiz.QuizBank
import com.lernhero.domain.quiz.QuizCategory
import com.lernhero.shared.i18n.AppLanguageStore
import com.lernhero.domain.quiz.QuizDifficulty
import com.lernhero.domain.quiz.QuizEntry
import com.lernhero.domain.quiz.WordQuizBank
import com.lernhero.domain.quiz.custom.CustomDeck
import com.lernhero.domain.quiz.custom.CustomDeckBank
import com.lernhero.domain.quiz.custom.CustomQuestionType
import com.lernhero.domain.repository.PlayerRepository
import com.lernhero.domain.spell.Spell
import com.lernhero.domain.spell.SpellCatalog
import com.lernhero.domain.spell.SpellTarget
import com.lernhero.domain.run.BattleKind
import com.lernhero.domain.run.BattleRequest
import com.lernhero.domain.run.EndlessBuff
import com.lernhero.domain.talent.TalentBonuses
import com.lernhero.domain.talent.TalentRules
import com.lernhero.domain.run.RunDifficulty
import com.lernhero.domain.run.RunLoot
import com.lernhero.domain.run.RunSession
import com.lernhero.game.character.AnimSlot
import com.lernhero.game.character.CharacterState
import com.lernhero.game.character.CharacterUiModel
import com.lernhero.domain.player.HeroAppearance
import com.lernhero.game.character.HeroLook
import com.lernhero.game.character.KnightEquipment
import com.lernhero.shared.resources.FlagResources
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.math.roundToInt
import kotlin.random.Random
import kotlin.time.Clock
import kotlin.time.TimeSource

/**
 * Turowa walka bez konca: fale wrogow nadciagaja z prawej, ale kazda tura to jedno pytanie quizu -
 * dopoki gracz nie odpowie, walka stoi w miejscu. Po odpowiedzi druzyny wymieniaja sie [ROUNDS_PER_TURN]
 * rundami ciosow (jedna "salwa" na strone na runde), a dopiero potem leci kolejne pytanie.
 *
 * Fizyczna animacja (cios, blysk trafienia, powrot) zostaje ta sama co w starym systemie -
 * tylko to, co ja odpala, zmienilo sie z "zegar tego wojownika doszedl do zera" na "kolejny krok
 * zaplanowanej salwy tej tury".
 */
class BattleViewModel(
    private val playerRepository: PlayerRepository,
    private val runSession: RunSession,
    /** Jezyk gracza - decyduje, czy kafle tematow moga pokazac kategorie lokalna (patrz [offerableCategories]). */
    private val languageStore: AppLanguageStore
) : ViewModel() {

    /**
     * Tematy, ktore wolno zaproponowac TEMU graczowi: caly bank minus kategorie lokalne innych
     * jezykow (np. "Poland" dla grajacego po angielsku).
     */
    private val offerableCategories: List<QuizCategory>
        get() = QuizCategory.builtinFor(languageStore.current.code)

    /** Zamowienie zlozone przez mape. Brak = szybka walka testowa. */
    private val request: BattleRequest =
        runSession.pendingBattle ?: BattleRequest(BattleKind.Normal, waves = 1, seed = Random.nextLong())

    /**
     * Pojedynek z bossem: brak zwyklego quizu tury. Cala walka to naprzemiennie atak specjalny bossa
     * (quiz-oszacowanie skalujacy jego moc) i salwa druzyny - patrz [beginBossTurn]/[startPlayerTurn].
     */
    private val isBossDuel: Boolean = request.kind == BattleKind.Boss

    /**
     * Wybor tematu pytania przed tura ([CategoryOffer]) - tylko kampania. Endless ma wlasny wybor
     * kategorii JUZ przed walka (EndlessSetupPanel), a pojedynek z bossem nie ma zwyklego quizu.
     */
    private val categoryPickEnabled: Boolean = !request.isEndless && !isBossDuel

    private var savedParty: List<String?> = emptyList()

    /** Poziomy kompanow kupione za zloto - patrz [MonsterLevels]. Puste = wszyscy na poziomie 1. */
    private var savedMonsterLevels: Map<String, Int> = emptyMap()

    /** Gwiazdki kompanow - zdejmuja sufit poziomu i same daja bonus do statystyk. */
    private var savedMonsterStars: Map<String, Int> = emptyMap()

    /** Poziomy zaklec kompanow ("idKarty:idZaklecia" -> poziom). */
    private var savedSpellLevels: Map<String, Int> = emptyMap()

    /** Ktora karta stoi w ktorym slocie druzyny - potrzebne, zeby z id postaci wrocic do zaklec. */
    private var companionCardOf: MutableMap<String, String> = mutableMapOf()

    private val combatRandom = Random(request.seed)

    /**
     * Ciosy policzone przy budowaniu kroku salwy, jeszcze nie pokazane - czekaja na koniec animacji
     * ataku. LISTA na atakujacego, nie pojedynczy cios: jedno machniecie moze wypuscic kilka
     * pociskow ([EndlessBuff.MultiShot]/[EndlessBuff.Volley]) i siegnac kilku celow
     * ([EndlessBuff.Cleave]) - wszystkie leca po tej samej, JEDNEJ animacji, jeden po drugim.
     */
    private var pendingHits: Map<String, List<PendingHit>> = emptyMap()

    private var nextEffectId: Long = 0L

    private val quizRandom = Random(request.seed * 31 + 7)
    private var quizCounter: Int = 0

    /** Znacznik czasu narysowania biezacego pytania - do niego liczy sie pasek czasu na odpowiedz. */
    private var questionShownAt: TimeSource.Monotonic.ValueTimeMark = TimeSource.Monotonic.markNow()

    /** To samo co [questionShownAt], ale dla specjalnego zdarzenia elity/bossa - patrz [BattleUiState.bossEvent]. */
    private var bossEventShownAt: TimeSource.Monotonic.ValueTimeMark = TimeSource.Monotonic.markNow()

    /** Mnoznik obrazen zablokowany w chwili odpowiedzi - obowiazuje przez cala salwe tej tury. */
    private var currentTurnMultiplier: Float = 1f

    /**
     * Mnoznik obrazen WROGOW na te ture - lustrzane odbicie [currentTurnMultiplier]. Bez tego zla
     * odpowiedz osiabiala tylko atak druzyny, a wrogowie i tak bili normalnie - quiz w praktyce nie
     * decydowal o wyniku walki, tylko o tym, jak szybko sie ja wygrywa. Zla odpowiedz teraz "otwiera"
     * druzyne na mocniejszy kontratak, wiec seria bledow realnie moze przesadzic o porazce.
     */
    private var currentEnemyMultiplier: Float = 1f

    /** Ci, ktorzy jeszcze nie skonczyli animacji ataku w biezacym kroku salwy. */
    private var pendingVolleyAttackers: MutableSet<String> = mutableSetOf()

    /** Pozostale kroki salwy biezacej tury - opróżniane jeden po drugim przez [startNextVolley]. */
    private var volleyPlan: List<VolleyStep> = emptyList()

    /**
     * Numer biezacej salwy. Kazde jej porzucenie ([abandonVolley] - koniec fali, porazka, nowa tura,
     * oszolomiona runda) podbija licznik, a kazda zaplanowana kontynuacja ([scheduleVolley]) zapamietuje
     * swoja wartosc i milczy, gdy ta juz nie jest aktualna. Bez tego opoznione `delay(...) ->
     * startNextVolley()` z PORZUCONEJ tury budzilo sie w srodku nastepnej i - majac pusta kolejke -
     * wchodzilo prosto w [endTurn], podmieniajac dopiero co pokazane pytanie na nowe (razem z
     * wyzerowanym paskiem czasu). Std brala sie czesc "rozjechanego" flow tury.
     */
    private var volleyToken: Int = 0

    /** Trwa juz rozstrzyganie konca fali/walki - patrz [resolveOutcomeIfOver]. Blokuje podwojne rozliczenie. */
    private var resolvingOutcome: Boolean = false

    /** Numer rundy w biezacej turze - zmiana odnawia garde z [EndlessBuff.Aegis] (patrz [refreshGuards]). */
    private var currentRoundIndex: Int = -1

    /**
     * Rozdane punkty statystyk bohatera. Wczytywane razem z reszta gracza ([loadSavedPlayer]) i
     * doliczane do walki w trzech miejscach: atak/HP w [applyEquipmentBonuses], szansa na krytyka
     * i mnoznik obrazen z dobrej odpowiedzi w [damageHit].
     */
    private var heroStats: HeroStats = HeroStats()

    /**
     * Bonusy z drzewka talentow. Wieksza ich czesc to staki mechanik, ktore walka juz umiala
     * obslugiwac dla trybu endless ([EndlessBuff]) - wystarczylo zasiedlic nimi
     * [BattleUiState.endlessBuffs] na starcie, zeby garda, kolce czy rozped dzialaly w KAZDEJ
     * walce, bez ani jednej nowej galezi w silniku. Reszta (plaski atak/HP/krytyk/quiz) wchodzi
     * tymi samymi kanalami, co [heroStats].
     */
    private var talentBonuses: TalentBonuses = TalentBonuses.EMPTY

    /**
     * Zaklecia bohatera z drzewka talentow: id -> szansa (procent ciosow) na rzut. Wczytywane
     * razem z reszta gracza; puste = bohater tylko bije.
     */
    private var heroSpellChances: Map<String, Int> = emptyMap()

    /** Wrogowie zamrozeni [SpellCatalog.FROST_SHARD] - traca tyle najblizszych krokow salwy. */
    private var frozenTurns: MutableMap<String, Int> = mutableMapOf()

    /** Zbierany na biezaco wynik tego zejscia - na koniec leci do repozytorium i na ekran koncowy. */
    private var runTally: RunTally = RunTally()

    /** Kategoria i poziom biezacego pytania - potrzebne do statystyk per kategoria i do XP za odpowiedz. */
    private var currentQuestionCategory: QuizCategory? = null
    private var currentQuestionDifficulty: QuizDifficulty = QuizDifficulty.Easy

    /**
     * Czy [playBattleIntro] wpuscilo juz bohaterow na pozycje.
     *
     * Potrzebne, bo intro trwa DLUZEJ niz samo wejscie druzyny, a [loadSavedPlayer] moze skonczyc
     * sie w dowolnym momencie - patrz komentarz przy podmianie druzyny.
     */
    private var introPlayersEntered: Boolean = false

    private val _uiState = MutableStateFlow(createInitialState())
    val uiState = _uiState.asStateFlow()

    init {
        loadSavedPlayer()
        playBattleIntro()
    }

    /**
     * Animacja ataku w miejscu dobiegla konca: cel dostaje obrazenia i krotki blysk trafienia,
     * a atakujacy wraca prosto do bezczynnosci. Gdy caly krok salwy sie skonczyl, ruszamy dalej.
     */
    fun onAttackFinished(characterId: String) {
        // Animacja moze zglosic koniec, gdy tego ciosu nikt juz nie oczekuje: postac wrocila do
        // kompozycji (LaunchedEffect(attackTrigger) startuje od nowa) albo salwa zostala w
        // miedzyczasie porzucona ([abandonVolley]). Taki callback wolno tylko odstawic postac do
        // bezczynnosci - gdyby szedl dalej, pchnalby sekwencer o krok do przodu drugi raz i
        // uruchomil DRUGI, rownolegly lancuch salwy obok tego wlasciwego.
        if (characterId !in pendingVolleyAttackers) {
            _uiState.update { state ->
                val stale = state.characters[characterId] ?: return@update state
                if (stale.state != CharacterState.ATTACK) return@update state
                state.copy(characters = state.characters.plus(characterId to stale.copy(state = CharacterState.IDLE)))
            }
            return
        }

        val hits = pendingHits[characterId].orEmpty()
        pendingHits = pendingHits - characterId

        // Atakujacy wraca do bezczynnosci od razu po SWOIM machnieciu. Kolejne pociski tego samego
        // ciosu (MultiShot/Volley/Cleave) leca juz "z reki", bez powtarzania animacji zamachu -
        // dwa pociski pod rzad wygladaja jak seria, a nie jak dwa osobne ataki tej samej postaci.
        _uiState.update { state ->
            val attacker = state.characters[characterId] ?: return@update state
            if (attacker.state != CharacterState.ATTACK) return@update state
            state.copy(characters = state.characters.plus(characterId to attacker.copy(state = CharacterState.IDLE)))
        }

        if (hits.isEmpty()) {
            finishAttacker(characterId)
            return
        }

        val token = volleyToken
        viewModelScope.launch {
            hits.forEachIndexed { index, hit ->
                if (token != volleyToken) return@launch
                if (index > 0) delay(BattleSpeed.scale(PROJECTILE_STAGGER_MILLIS))
                _uiState.update { it.applyHit(characterId, hit) }
            }
            if (token != volleyToken) return@launch
            finishAttacker(characterId)
        }
    }

    /** Atakujacy skonczyl caly swoj krok (wszystkie pociski doleciały) - czas na kolejny krok salwy. */
    private fun finishAttacker(characterId: String) {
        pendingVolleyAttackers -= characterId
        if (pendingVolleyAttackers.isEmpty()) {
            settleVolleyStep()
        }
    }

    /**
     * Jeden pocisk doleciał do celu: obrazenia, ewentualna garda, kolce, wampiryzm i drugi oddech.
     * HP jest liczone TERAZ (a nie z gory, przy budowaniu salwy) - dzieki temu kolejne pociski tej
     * samej serii poprawnie nakladaja sie na siebie i nie da sie "cofnac" HP celu absolutna wartoscia
     * policzona przed poprzednim trafieniem.
     */
    private fun BattleUiState.applyHit(attackerId: String, hit: PendingHit): BattleUiState {
        val attacker = characters[attackerId] ?: return this
        // Cel moglo juz dobic poprzednie trafienie tej samej serii - wtedy kolejny pocisk przenosi
        // sie na nastepnego zywego przeciwnika, zamiast wbic sie w trupa i przepasc. Bez tego
        // [EndlessBuff.MultiShot] tracil polowe mocy dokladnie wtedy, gdy dzialal najlepiej.
        val target = characters[hit.targetId]?.takeIf { it.hp > 0 }
            ?: characters.values
                .filter { it.isPlayer != attacker.isPlayer && it.hp > 0 }
                .let { foes -> if (attacker.isPlayer) foes.minByOrNull { it.id } else foes.randomOrNull(combatRandom) }
            ?: return this

        // Garda ([EndlessBuff.Guard]/[EndlessBuff.Aegis]): cios po prostu nie dochodzi.
        val charges = guardCharges[target.id] ?: 0
        if (!attacker.isPlayer && target.isPlayer && charges > 0) {
            var blocked = copy(guardCharges = guardCharges + (target.id to charges - 1))
                .spawnEffect(targetId = target.id, amount = 0, kind = HitKind.Block, sourceId = attackerId)
            val riposteStacks = endlessBuffs[EndlessBuff.Riposte] ?: 0
            if (riposteStacks > 0) {
                blocked = blocked.dealReflected(attackerId, (hit.amount * RIPOSTE_DAMAGE_FACTOR).roundToInt())
            }
            return blocked
        }

        // Zaklecie leczace nie zadaje obrazen - laduje wprost w [healCharacter], razem z bonusem
        // z [EndlessBuff.Blessing], tak samo jak kazde inne leczenie w grze.
        if (hit.heals) {
            return healCharacter(hit.targetId, hit.amount.toFloat())
        }

        if (hit.freezeTurns > 0) frozenTurns[target.id] = hit.freezeTurns

        val raw = (target.hp - hit.amount).coerceAtLeast(0)
        // Drugi oddech: zamiast paść, bohater zostaje na resztce HP - raz na fale za kazdy stak.
        val secondWindStacks = if (target.isPlayer) endlessBuffs[EndlessBuff.SecondWind] ?: 0 else 0
        val alreadyUsed = secondWindUses[target.id] ?: 0
        val rescued = raw <= 0 && secondWindStacks > alreadyUsed
        val finalHp = if (rescued) {
            (target.maxHp * SECOND_WIND_HP_FRACTION).roundToInt().coerceAtLeast(1)
        } else {
            raw
        }

        var next = copy(
            characters = characters.plus(
                target.id to target.copy(
                    hp = finalHp,
                    state = if (finalHp <= 0) CharacterState.DEAD else CharacterState.HIT
                )
            ),
            secondWindUses = if (rescued) secondWindUses + (target.id to alreadyUsed + 1) else secondWindUses,
            message = if (rescued) "${target.name} lapie drugi oddech!" else message
        ).spawnEffect(targetId = target.id, amount = hit.amount, kind = hit.kind, sourceId = attackerId)

        if (rescued) next = next.spawnEffect(targetId = target.id, amount = finalHp, kind = HitKind.Heal)

        // Wampiryzm: kazdy cios gracza (takze dodatkowe pociski) leczy atakujacego za czesc
        // REALNIE zadanych obrazen, nie za atak bazowy.
        if (attacker.isPlayer) {
            val vampStacks = endlessBuffs[EndlessBuff.Vampirism] ?: 0
            if (vampStacks > 0) {
                next = next.healCharacter(attackerId, hit.amount * vampStacks * VAMPIRISM_HEAL_PER_STACK)
            }
        }

        // Kolce: to wrog oberwał od pancerza, wiec efekt leci na NIEGO, nie na bohatera.
        if (!attacker.isPlayer) {
            val thornStacks = endlessBuffs[EndlessBuff.Thorns] ?: 0
            if (thornStacks > 0) {
                next = next.dealReflected(attackerId, (hit.amount * thornStacks * THORNS_REFLECT_PER_STACK).roundToInt())
            }
        }

        return next
    }

    /** Obrazenia zwrotne (kolce, riposta) prosto w napastnika - bez animacji ataku, sam efekt. */
    private fun BattleUiState.dealReflected(targetId: String, amount: Int): BattleUiState {
        if (amount <= 0) return this
        val victim = characters[targetId] ?: return this
        if (victim.hp <= 0) return this
        val after = (victim.hp - amount).coerceAtLeast(0)
        return copy(
            characters = characters.plus(
                targetId to victim.copy(
                    hp = after,
                    state = if (after <= 0) CharacterState.DEAD else victim.state
                )
            )
        ).spawnEffect(targetId = targetId, amount = amount, kind = HitKind.Reflect)
    }

    /** Leczenie w jednym miejscu - razem z bonusem z [EndlessBuff.Blessing] i efektem na ekranie. */
    private fun BattleUiState.healCharacter(characterId: String, rawAmount: Float): BattleUiState {
        val character = characters[characterId] ?: return this
        if (character.hp <= 0) return this
        val amount = (rawAmount * healingMultiplier()).roundToInt()
        if (amount <= 0) return this
        val healed = (character.hp + amount).coerceAtMost(character.maxHp)
        if (healed == character.hp) return this
        return copy(characters = characters.plus(characterId to character.copy(hp = healed)))
            .spawnEffect(targetId = characterId, amount = healed - character.hp, kind = HitKind.Heal)
    }

    /** Mnoznik KAZDEGO leczenia druzyny - patrz [EndlessBuff.Blessing]. */
    private fun BattleUiState.healingMultiplier(): Float =
        1f + (endlessBuffs[EndlessBuff.Blessing] ?: 0) * BLESSING_HEAL_BONUS_PER_STACK

    /**
     * Caly krok salwy (np. wszyscy gracze naraz) skonczyl animacje - sprawdzamy czy walka sie
     * rozstrzygnela, a jesli nie, ruszamy do kolejnego kroku po krotkiej pauzie na tempo.
     */
    private fun settleVolleyStep() {
        val state = _uiState.value
        if (state.phase != GamePhase.Battle) return
        if (resolveOutcomeIfOver(state)) return
        scheduleVolley(BattleSpeed.scale(VOLLEY_PAUSE_MILLIS))
    }

    /**
     * JEDYNE miejsce, ktore stwierdza "fala oczyszczona / druzyna padla" - wolane po kazdym kroku
     * salwy ORAZ na koncu tury ([endTurn]). Zwraca true, gdy walka jest rozstrzygnieta i kolejka
     * salwy zostala porzucona; wolajacy ma wtedy nie robic juz nic wiecej.
     *
     * Dawniej ten sam warunek siedzial tylko w [settleVolleyStep] i dodatkowo wymagal, zeby ZADNA
     * postac nie byla w trakcie animacji. Wystarczylo, ze ktos inny mial jeszcze zapalony blysk
     * trafienia w chwili smiertelnego ciosu, a koniec fali byl przegapiony - reszta kolejki
     * przelatywala na sucho (nie ma juz kogo bic), kolejka sie konczyla i [endTurn] rysowal
     * PYTANIE do pustego pola bitwy, zamiast wpuscic kolejna fale.
     */
    private fun resolveOutcomeIfOver(state: BattleUiState): Boolean {
        val playersAlive = state.characters.values.any { it.isPlayer && it.hp > 0 }
        val enemiesAlive = state.characters.values.any { !it.isPlayer && it.hp > 0 }
        if (playersAlive && enemiesAlive) return false
        if (resolvingOutcome) return true

        resolvingOutcome = true
        abandonVolley()

        viewModelScope.launch {
            // Smiertelny cios musi zdazyc sie NARYSOWAC. [MutableStateFlow] sklada (conflate) stany
            // wyemitowane w tej samej chwili, wiec podmiana obsady w tym samym "ticku", w ktorym
            // spadlo ostatnie HP, kasowala wroga z ekranu ZANIM UI w ogole zobaczylo jego nowe HP -
            // stad wrazenie, ze przeciwnik "z 1/3 zycia po prostu znika, a leci kolejna runda".
            // Ta pauza to jednoczesnie czas na animacje smierci (MonsterCharacter: DEAD -> zanik ~420ms).
            delay(BattleSpeed.scale(DEATH_SETTLE_MILLIS))

            val settled = _uiState.value
            if (settled.phase != GamePhase.Battle) {
                resolvingOutcome = false
                return@launch
            }

            val teamWiped = settled.characters.values.none { it.isPlayer && it.hp > 0 }
            val lastWaveCleared = !request.isEndless && settled.wave >= request.waves
            when {
                teamWiped -> _uiState.update { it.finishDefeat() }
                lastWaveCleared -> _uiState.update { it.finishVictory() }
                else -> {
                    _uiState.update { it.spawnNextWave() }
                    // Pytanie dopiero PO wejsciu nowej fali: pasek czasu na odpowiedz startowal
                    // dawniej razem z podmiana obsady, czyli w chwili, gdy wrogowie dopiero
                    // maszerowali z krawedzi ekranu ([staggerEnemyEntrance]) - gracz tracil mnoznik
                    // sily ciosu na ogladanie animacji wejscia.
                    delay(waveEntranceDurationMs())
                    resolvingOutcome = false
                    // Lezy oferta ulepszen? Walka czeka - pytanie ruszy dopiero z [chooseUpgrade].
                    val ready = _uiState.value
                    if (ready.phase == GamePhase.Battle && ready.upgradeOffer == null) {
                        askNextQuestion(keepMessage = true)
                    }
                    return@launch
                }
            }
            resolvingOutcome = false
        }
        return true
    }

    /** Ile trwa wpuszczenie calej biezacej fali na pozycje (patrz [staggerEnemyEntrance]), z sufitem na duze fale. */
    private fun waveEntranceDurationMs(): Long {
        val enemies = _uiState.value.characters.values.count { !it.isPlayer }
        val full = (enemies * ENEMY_STAGGER_MS + WAVE_SETTLE_MILLIS).coerceAtMost(MAX_WAVE_ENTRANCE_MILLIS)
        return BattleSpeed.scale(full)
    }

    /**
     * Porzuca reszte zaplanowanej salwy razem ze wszystkim, co do niej nalezy - i uniewaznia
     * ([volleyToken]) kazda jej kontynuacje, ktora akurat czeka w `delay`.
     */
    private fun abandonVolley() {
        volleyPlan = emptyList()
        pendingHits = emptyMap()
        pendingVolleyAttackers.clear()
        frozenTurns.clear()
        volleyToken += 1
    }

    /** Kolejny krok salwy po pauzie na tempo - milczy, jesli w miedzyczasie salwa zostala porzucona. */
    private fun scheduleVolley(delayMillis: Long) {
        val token = volleyToken
        viewModelScope.launch {
            delay(delayMillis)
            if (token != volleyToken) return@launch
            startNextVolley()
        }
    }

    /**
     * Wystrzeliwuje kolejny pojedynczy cios zaplanowanej kolejki - jedna postac na krok, nigdy
     * cala strona naraz - albo, jesli kolejka sie skonczyla, konczy ture.
     */
    private fun startNextVolley() {
        if (_uiState.value.phase != GamePhase.Battle || resolvingOutcome) return

        val step = volleyPlan.firstOrNull()
        if (step == null) {
            endTurn()
            return
        }

        // Elita/boss czasem zamiast zwyklego ciosu odpala specjalne zdarzenie (patrz triggerBossEvent) -
        // to sprawdzamy PRZED normalnym rozstrzygnieciem kroku, bo moze je calkiem zastapic. Warunek
        // stunTurnsRemaining == 0 jest kluczowy: w trakcie oszolomionej rundy (patrz runStunnedRound)
        // boss ma PO PROSTU BIC, a nie w kolko losowac nowe zdarzenia - inaczej caly ekran zapetla
        // sie w kolejnych oknach zamiast realnie walczyc, dopoki gracz nie zauwazy, ze "nic sie nie dzieje".
        if (!step.isPlayer && request.kind != BattleKind.Normal) {
            val currentState = _uiState.value
            val canTriggerEvent = currentState.bossEvent == null && currentState.stunTurnsRemaining == 0
            val attacker = currentState.characters[step.attackerId]
            val event = if (canTriggerEvent) {
                attacker?.takeIf { it.hp > 0 }?.let { rollBossSpecialEvent(quizRandom) }
            } else {
                null
            }
            if (event != null) {
                volleyPlan = volleyPlan.drop(1)
                triggerBossEvent(event, step.attackerId)
                return
            }
        }

        // Zamrozony wrog ([SpellCatalog.FROST_SHARD]) traci swoj krok - zdejmujemy mu jedna ture
        // zamrozenia i idziemy dalej, jakby go w tej kolejce nie bylo.
        val frozen = frozenTurns[step.attackerId] ?: 0
        if (frozen > 0) {
            frozenTurns[step.attackerId] = frozen - 1
            volleyPlan = volleyPlan.drop(1)
            _uiState.update { it.copy(message = "${it.characters[step.attackerId]?.name ?: "The enemy"} is frozen!") }
            startNextVolley()
            return
        }

        // Pasek zdolnosci: czesc ciosow bohatera zamienia sie w zaklecie. Los pada RAZ na krok,
        // wiec procent z paska jest doslownie "ile procent moich ciosow to ten spell".
        if (step.isPlayer && step.attackerId == HERO_ID) {
            val spell = rollSpell()
            if (spell != null) {
                volleyPlan = volleyPlan.drop(1)
                castSpell(spell, step.attackerId)
                return
            }
        }

        // Kompani maja wlasne zaklecia - szansa jest CECHA KARTY (nie rozdaje jej gracz), a poziom
        // zaklecia podnosi i szanse, i sile. Patrz [CompanionSpells].
        if (step.isPlayer && step.attackerId != HERO_ID) {
            val cast = rollCompanionSpell(step.attackerId)
            if (cast != null) {
                volleyPlan = volleyPlan.drop(1)
                castCompanionSpell(cast, step.attackerId)
                return
            }
        }

        volleyPlan = volleyPlan.drop(1)

        // Nowa runda w tej samej turze - [EndlessBuff.Aegis] odnawia garde kazdej rundy, zwykla
        // [EndlessBuff.Guard] tylko raz na ture (patrz [refreshGuards] w [continueQuiz]).
        if (step.round != currentRoundIndex) {
            currentRoundIndex = step.round
            if (step.round > 0) refreshGuards(turnStart = false)
        }

        _uiState.update { state ->
            if (state.phase != GamePhase.Battle) return@update state
            val attacker = state.characters[step.attackerId] ?: return@update state
            if (attacker.hp <= 0) return@update state

            val hits = state.buildHits(attacker)
            if (hits.isEmpty()) return@update state

            pendingHits = pendingHits + (attacker.id to hits)
            pendingVolleyAttackers = mutableSetOf(attacker.id)

            // HP celu NIE zmienia sie tutaj - dopiero gdy pocisk doleci (patrz [applyHit]), po
            // zakonczeniu animacji ataku. Zmiana HP juz w tym miejscu (przed odegraniem ciecia) byla
            // bugiem: pasek HP i .shake(character.hp) odpalaly sie NATYCHMIAST, w momencie
            // rozpoczecia zamachu, a reakcja na trafienie (odskok, czerwony blysk) dochodzila
            // dopiero ~360ms pozniej - te dwa efekty rozjezdzaly sie w czasie, wygladajac jak
            // przypadkowe drgania niepowiazane z samym ciosem.
            val characters = state.characters
                .plus(attacker.id to attacker.copy(state = CharacterState.ATTACK, currentTargetId = hits.first().targetId))
            state.copy(characters = characters)
        }

        if (pendingVolleyAttackers.isEmpty()) {
            // Cel juz nie zyl / nikt nie zaatakowal w tym kroku - od razu dalej.
            startNextVolley()
        }
    }

    /**
     * Salwa tej tury sie skonczyla. Jesli druzyna jest jeszcze oszolomiona po przegranym zakladzie
     * z bossem ([BattleUiState.stunTurnsRemaining]), rusza kolejna runda, w ktorej oszolomieni stoja
     * bezczynnie - dopiero gdy licznik spadnie do zera, wraca normalna faza pytania.
     */
    private fun endTurn() {
        val state = _uiState.value
        if (state.phase != GamePhase.Battle) return
        // Tura konczy sie WYLACZNIE z fazy rozstrzygania. Inaczej spozniona kontynuacja salwy
        // (albo pusta kolejka po porzuconej turze) potrafila wejsc tu w trakcie fazy pytania i
        // podmienic pokazane wlasnie pytanie na nowe, zerujac przy okazji pasek czasu.
        if (state.turnPhase != TurnPhase.Resolving) return
        // Fala mogla zostac oczyszczona krokiem, ktory nikogo juz nie zaatakowal (wszystkie
        // pozostale kroki przelecialy na sucho) - wtedy zamiast pytania nalezy wpuscic kolejna fale.
        if (resolveOutcomeIfOver(state)) return

        // Pojedynek z bossem: po salwie druzyny wraca jego kolejny atak specjalny, nie quiz tury.
        if (isBossDuel) {
            beginBossTurn()
            return
        }

        if (state.stunTurnsRemaining > 0) {
            runStunnedRound(state)
            return
        }

        askNextQuestion(extraElapsedMs = TURN_DURATION_ESTIMATE_MS)
    }

    /**
     * Rysuje kolejne pytanie i przelacza ekran w faze pytania. [questionShownAt] jest ustawiane
     * dokladnie tutaj - od tego momentu (a nie od podmiany fali) liczy sie pasek czasu na odpowiedz.
     */
    private fun askNextQuestion(extraElapsedMs: Long = 0L, keepMessage: Boolean = false) {
        // Kampania: zanim padnie pytanie, gracz wybiera z czterech kafli - temat plus bonus
        // frakcyjny na cala nadchodzaca ture (patrz [chooseCategory]). Pasek czasu na odpowiedz
        // NIE startuje tutaj: zaczyna sie dopiero w chwili wyboru, zeby namysl nad kaflami nie
        // zjadal mnoznika sily ciosu.
        if (categoryPickEnabled) {
            _uiState.update { s ->
                if (s.phase != GamePhase.Battle) return@update s
                s.copy(
                    elapsedMs = s.elapsedMs + extraElapsedMs,
                    turnPhase = TurnPhase.CategoryPick,
                    categoryOffer = CategoryOffer.roll(quizRandom, offerableCategories),
                    boostedElements = emptySet(),
                    quiz = null,
                    stunnedCharacterIds = emptySet(),
                    message = if (keepMessage) s.message else null
                )
            }
            return
        }

        questionShownAt = TimeSource.Monotonic.markNow()
        _uiState.update { s ->
            if (s.phase != GamePhase.Battle) return@update s
            s.copy(
                elapsedMs = s.elapsedMs + extraElapsedMs,
                turnPhase = TurnPhase.Question,
                quiz = drawNextQuestion(s.wave),
                stunnedCharacterIds = emptySet(),
                message = if (keepMessage) s.message else null
            )
        }
    }

    /**
     * Gracz wybral kafel: zapamietujemy wzmocnione zywioly na te ture i DOPIERO TERAZ rysujemy
     * pytanie - z kategorii, ktora sam wskazal. Od tego momentu leci pasek czasu na odpowiedz.
     */
    fun chooseCategory(option: CategoryOption) {
        val state = _uiState.value
        if (state.phase != GamePhase.Battle || state.turnPhase != TurnPhase.CategoryPick) return

        questionShownAt = TimeSource.Monotonic.markNow()
        val boosted = option.elements.toSet()
        _uiState.update { s ->
            if (s.phase != GamePhase.Battle) return@update s
            s.copy(
                turnPhase = TurnPhase.Question,
                categoryOffer = null,
                boostedElements = boosted,
                quiz = drawNextQuestion(s.wave, forcedCategory = option.category),
                message = "Boost: " + option.elements.joinToString(" + ") { it.label }
            )
        }
    }

    /**
     * Jedna runda oszolomienia: oszolomieni gracze ([BattleUiState.stunnedCharacterIds]) nie
     * atakuja, reszta druzyny (jesli ktos akurat uniknal zakladu) bije normalnie, a wrogowie biją
     * bez przeszkod. [BattleUiState.stunnedCharacterIds] zostaje niezmienione az do ostatniej rundy -
     * dopiero [endTurn] je czysci, gdy druzyna wraca do normalnego pytania.
     */
    private fun runStunnedRound(state: BattleUiState) {
        val enemyIds = state.characters.values.filter { !it.isPlayer && it.hp > 0 }.sortedBy { it.id }.map { it.id }
        val activePlayerIds = state.characters.values
            .filter { it.isPlayer && it.hp > 0 && it.id !in state.stunnedCharacterIds }
            .sortedBy { it.id }
            .map { it.id }
        abandonVolley()
        volleyPlan = state.buildTurnPlan(activePlayerIds, enemyIds)
        currentRoundIndex = -1
        refreshGuards(turnStart = true)

        val remaining = state.stunTurnsRemaining - 1
        _uiState.update {
            it.copy(
                stunTurnsRemaining = remaining,
                turnPhase = TurnPhase.Resolving,
                message = if (remaining > 0) "The party is still stunned..." else "The party shakes off the stun..."
            )
        }
        startNextVolley()
    }

    /**
     * Kolejka calej tury: [ROUNDS_PER_TURN] rund (plus [EndlessBuff.Frenzy]) po jednym kroku na
     * kazda zywa postac. JEDEN krok = JEDNA animacja ataku - to, ile pociskow z niej wyleci i w ilu
     * celow trafia, rozstrzyga dopiero [buildHits] w chwili ciosu.
     */
    private fun BattleUiState.buildTurnPlan(playerIds: List<String>, enemyIds: List<String>): List<VolleyStep> {
        val rounds = ROUNDS_PER_TURN + (endlessBuffs[EndlessBuff.Frenzy] ?: 0)
        return (0 until rounds).flatMap { round ->
            playerIds.map { VolleyStep(it, isPlayer = true, round = round) } +
                enemyIds.map { VolleyStep(it, isPlayer = false, round = round) }
        }
    }

    /**
     * Co wylatuje z jednego machniecia. Bazowo jeden pocisk w jeden cel, ale ulepszenia to mnoza:
     * [EndlessBuff.MultiShot] (bohater) i [EndlessBuff.Volley] (cala druzyna) dokladaja KOLEJNE
     * pociski w ten sam cel - lecace jeden po drugim po tej samej animacji (patrz [onAttackFinished]),
     * a [EndlessBuff.Cleave] dokłada slabsze trafienia w innych wrogow.
     *
     * Pusta lista = nie ma kogo bic; krok salwy przelatuje wtedy na sucho.
     */
    private fun BattleUiState.buildHits(attacker: CharacterUiModel): List<PendingHit> {
        val foes = characters.values.filter { it.isPlayer != attacker.isPlayer && it.hp > 0 }
        if (foes.isEmpty()) return emptyList()

        // Druzyna skupia ogien na jednym wrogu (stabilne id), wrogowie wybieraja cel losowo.
        val primary = if (attacker.isPlayer) {
            foes.minByOrNull { it.id }
        } else {
            foes.randomOrNull(combatRandom)
        } ?: return emptyList()

        val extraProjectiles = if (attacker.isPlayer) {
            val multiShot = if (attacker.id == HERO_ID) endlessBuffs[EndlessBuff.MultiShot] ?: 0 else 0
            val volley = endlessBuffs[EndlessBuff.Volley] ?: 0
            multiShot + volley
        } else {
            0
        }

        val hits = MutableList(1 + extraProjectiles) { damageHit(attacker, primary, factor = 1f) }

        val cleaveStacks = if (attacker.isPlayer) endlessBuffs[EndlessBuff.Cleave] ?: 0 else 0
        if (cleaveStacks > 0) {
            foes.filter { it.id != primary.id }
                .shuffled(combatRandom)
                .take(cleaveStacks)
                .forEach { hits += damageHit(attacker, it, factor = CLEAVE_DAMAGE_FACTOR) }
        }

        return hits
    }

    /**
     * Jeden konkretny pocisk: rzut na krytyka i cala matematyka obrazen (mnoznik z quizu, ulepszenia
     * druzyny, redukcja obrazen wrogow). [factor] to oslabienie dodatkowych trafien - patrz
     * [CLEAVE_DAMAGE_FACTOR].
     */
    private fun BattleUiState.damageHit(
        attacker: CharacterUiModel,
        target: CharacterUiModel,
        factor: Float
    ): PendingHit {
        val precisionStacks = endlessBuffs[EndlessBuff.Precision] ?: 0
        val isCrit = attacker.isPlayer &&
            combatRandom.nextFloat() <
            (CRIT_CHANCE + precisionStacks * PRECISION_CRIT_BONUS_PER_STACK +
                heroStats.critChanceBonus + talentBonuses.critChance)

        val multiplier = if (attacker.isPlayer) {
            val brutalityStacks = endlessBuffs[EndlessBuff.Brutality] ?: 0
            val critMultiplier = if (isCrit) {
                CRIT_DAMAGE_MULTIPLIER + brutalityStacks * BRUTALITY_CRIT_BONUS_PER_STACK
            } else {
                1f
            }
            val mightStacks = endlessBuffs[EndlessBuff.Might] ?: 0
            val avatarStacks = endlessBuffs[EndlessBuff.Avatar] ?: 0
            val statBonus = 1f + mightStacks * MIGHT_ATTACK_BONUS_PER_STACK + avatarStacks * AVATAR_BONUS_PER_STACK

            // Rozped: liczy sie passa dobrych odpowiedzi w chwili ciosu, wiec efekt rosnie w trakcie
            // dobrze idacej walki i znika w chwili pierwszej pomylki.
            val momentumStacks = endlessBuffs[EndlessBuff.Momentum] ?: 0
            val momentum = 1f + momentumStacks *
                (streak.consecutiveCorrect * MOMENTUM_BONUS_PER_ANSWER).coerceAtMost(MAX_MOMENTUM_BONUS)

            // Kat: dobijanie rannych idzie duzo szybciej.
            val executionerStacks = endlessBuffs[EndlessBuff.Executioner] ?: 0
            val wounded = target.maxHp > 0 && target.hp.toFloat() / target.maxHp < EXECUTIONER_HP_THRESHOLD
            val execution = if (wounded) 1f + executionerStacks * EXECUTIONER_BONUS_PER_STACK else 1f

            // [HeroStat.Focus] wzmacnia WYLACZNIE mnoznik z quizu - punkty w skupienie oplacaja sie
            // tylko temu, kto faktycznie odpowiada dobrze, a nie temu, kto przeklikuje pytania.
            currentTurnMultiplier * heroStats.quizDamageMultiplier * talentBonuses.quizDamageMultiplier *
                critMultiplier * statBonus * momentum * execution
        } else {
            val fortressStacks = endlessBuffs[EndlessBuff.Fortress] ?: 0
            val fortressReduction = (fortressStacks * FORTRESS_REDUCTION_PER_STACK).coerceAtMost(MAX_FORTRESS_REDUCTION)
            currentEnemyMultiplier * (1f - fortressReduction)
        }

        // Przewaga zywiolu dziala w OBIE strony i mnozy sie z reszta - wchodzi tu, a nie w
        // gałęzi gracza, bo ten sam trojkat ma obowiazywac takze wrogow bijacych w kompanow.
        val elemental = Element.multiplier(attacker.element, target.element)

        // Bonus z kafla wybranego przed tura ([CategoryOffer]) - dotyczy WYLACZNIE jednostek gracza
        // o wzmocnionym zywiole i tylko do konca tej tury (boostedElements czysci sie w askNextQuestion).
        val factionBoost = if (attacker.isPlayer && attacker.element != null &&
            attacker.element in boostedElements
        ) {
            1f + CategoryOffer.ELEMENT_BOOST
        } else {
            1f
        }

        val amount = (attacker.attack * multiplier * elemental * factionBoost * factor)
            .roundToInt().coerceAtLeast(1)
        return PendingHit(
            targetId = target.id,
            amount = amount,
            isCrit = isCrit,
            kind = if (isCrit) HitKind.Crit else HitKind.Hit
        )
    }

    /**
     * Odnawia garde druzyny. [turnStart] = true na poczatku tury (zwykla [EndlessBuff.Guard] plus
     * [EndlessBuff.Aegis]), false na poczatku kazdej kolejnej rundy - wtedy doladowuje sie juz
     * tylko Egida, i tylko w gore, zeby nie skasowac ladunku, ktorego gracz jeszcze nie zuzyl.
     */
    private fun refreshGuards(turnStart: Boolean) {
        _uiState.update { state ->
            val guardStacks = state.endlessBuffs[EndlessBuff.Guard] ?: 0
            val aegisStacks = state.endlessBuffs[EndlessBuff.Aegis] ?: 0
            val fresh = if (turnStart) guardStacks + aegisStacks else aegisStacks
            if (fresh <= 0) return@update if (turnStart) state.copy(guardCharges = emptyMap()) else state

            val charges = state.characters.values
                .filter { it.isPlayer && it.hp > 0 }
                .associate { player ->
                    val kept = if (turnStart) 0 else (state.guardCharges[player.id] ?: 0)
                    player.id to maxOf(kept, fresh)
                }
            state.copy(guardCharges = charges)
        }
    }

    /**
     * Rzut na zaklecia bohatera. Kazde odblokowane zaklecie ma swoja szanse (procent ciosow);
     * reszta to zwykly atak. Zwraca null, gdy wypadl zwykly cios - najczestszy wynik, bo suma
     * szans zaklec jest wyraznie ponizej 100%.
     */
    private fun rollSpell(): Spell? {
        if (heroSpellChances.isEmpty()) return null
        var roll = combatRandom.nextInt(100)
        for ((spellId, percent) in heroSpellChances) {
            if (percent <= 0) continue
            roll -= percent
            if (roll < 0) return SpellCatalog.byId(spellId)
        }
        return null
    }

    /** Wylosowane zaklecie kompana razem z jego poziomem - patrz [rollCompanionSpell]. */
    private data class CompanionCast(val spell: CompanionSpell, val level: Int)

    /**
     * Rzut na zaklecia kompana stojacego w danym slocie. Kazde zaklecie karty ma wlasna szanse;
     * sprawdzamy je po kolei, wiec kompan z dwoma zakleciami ma po prostu dwie szanse na cos
     * ciekawszego niz zwykly cios.
     */
    private fun rollCompanionSpell(characterId: String): CompanionCast? {
        val cardId = companionCardOf[characterId] ?: return null
        val card = MonsterCatalog.byId(cardId) ?: return null

        card.spells.forEach { spell ->
            val level = MonsterSpellLevels.clamp(
                savedSpellLevels["$cardId:${spell.id}"] ?: MonsterSpellLevels.MIN_LEVEL
            )
            if (combatRandom.nextInt(100) < MonsterSpellLevels.chanceAt(spell, level)) {
                return CompanionCast(spell, level)
            }
        }
        return null
    }

    /**
     * Rzuca zaklecie kompana. Leczenie liczy sie od maks. HP CELU (procent), a obrazenia od ataku
     * rzucajacego - tak jak opisuje to [CompanionSpellKind].
     */
    private fun castCompanionSpell(cast: CompanionCast, casterId: String) {
        val state = _uiState.value
        val caster = state.characters[casterId] ?: run { startNextVolley(); return }
        val spell = cast.spell
        val power = MonsterSpellLevels.powerAt(spell, cast.level)

        val targets = when (spell.kind) {
            CompanionSpellKind.Heal ->
                state.characters.values
                    .filter { it.isPlayer && it.hp > 0 && it.hp < it.maxHp }
                    .minByOrNull { it.hp.toFloat() / it.maxHp }
                    ?.let { listOf(it) }.orEmpty()
            CompanionSpellKind.Strike ->
                state.characters.values.filter { !it.isPlayer && it.hp > 0 }
                    .randomOrNull(combatRandom)?.let { listOf(it) }.orEmpty()
            CompanionSpellKind.Blast ->
                state.characters.values.filter { !it.isPlayer && it.hp > 0 }.sortedBy { it.id }
        }

        if (targets.isEmpty()) {
            startNextVolley()
            return
        }

        val hits = targets.map { target ->
            val amount = when (spell.kind) {
                CompanionSpellKind.Heal -> (target.maxHp * power).roundToInt().coerceAtLeast(1)
                else -> (caster.attack * power * currentTurnMultiplier).roundToInt().coerceAtLeast(1)
            }
            PendingHit(
                targetId = target.id,
                amount = amount,
                isCrit = false,
                kind = spell.hitKind,
                heals = spell.kind == CompanionSpellKind.Heal
            )
        }

        pendingHits = pendingHits + (casterId to hits)
        pendingVolleyAttackers = mutableSetOf(casterId)

        _uiState.update {
            it.copy(
                characters = it.characters.plus(
                    casterId to caster.copy(state = CharacterState.ATTACK, currentTargetId = hits.first().targetId)
                ),
                message = "${caster.name}: ${spell.name}!"
            )
        }
    }

    /**
     * Rzuca zaklecie zamiast zwyklego ciosu. Animacja jest ta sama co przy ataku (bohater macha),
     * ale skutek liczy sie z [com.lernhero.domain.spell.LiveSpellSpec] - dzieki temu zaklecia
     * wchodza w istniejacy przeplyw salwy (pendingHits -> onAttackFinished) i nie trzeba dla nich
     * osobnego toru sekwencera.
     */
    private fun castSpell(spell: Spell, casterId: String) {
        val state = _uiState.value
        val caster = state.characters[casterId] ?: run { startNextVolley(); return }
        val spec = spell.live

        val targets = when (spec.target) {
            SpellTarget.RandomEnemy ->
                state.characters.values.filter { !it.isPlayer && it.hp > 0 }
                    .randomOrNull(combatRandom)?.let { listOf(it) }.orEmpty()
            SpellTarget.AllEnemies ->
                state.characters.values.filter { !it.isPlayer && it.hp > 0 }.sortedBy { it.id }
            SpellTarget.WoundedAlly ->
                state.characters.values
                    .filter { it.isPlayer && it.hp > 0 && it.hp < it.maxHp }
                    .minByOrNull { it.hp.toFloat() / it.maxHp }
                    ?.let { listOf(it) }.orEmpty()
        }

        // Nie ma kogo trafic (albo nikt nie jest ranny) - zaklecie sie marnuje, ale tura leci dalej.
        if (targets.isEmpty()) {
            startNextVolley()
            return
        }

        val spellPower = 1f + runSession.modifiers.bonusSpellPower
        val hits = targets.map { target ->
            val amount = if (spec.heals) {
                (caster.attack * spec.healMultiplier * spellPower).roundToInt().coerceAtLeast(1)
            } else {
                (caster.attack * spec.damageMultiplier * spellPower * currentTurnMultiplier)
                    .roundToInt().coerceAtLeast(1)
            }
            PendingHit(
                targetId = target.id,
                amount = amount,
                isCrit = false,
                kind = spec.hitKind,
                heals = spec.heals,
                freezeTurns = spec.freezeTurns
            )
        }

        pendingHits = pendingHits + (casterId to hits)
        pendingVolleyAttackers = mutableSetOf(casterId)

        _uiState.update {
            it.copy(
                characters = it.characters.plus(
                    casterId to caster.copy(state = CharacterState.ATTACK, currentTargetId = hits.first().targetId)
                ),
                message = "${spell.name}!"
            )
        }
    }

    /** Krotki blysk trafienia minal - cel wraca do bezczynnosci (chyba ze juz padl). */
    fun onHitRecovered(characterId: String) {
        _uiState.update { state ->
            val character = state.characters[characterId] ?: return@update state
            if (character.state != CharacterState.HIT) return@update state
            state.copy(characters = state.characters.plus(characterId to character.copy(state = CharacterState.IDLE)))
        }
    }

    /**
     * Rodzi pływajacy efekt na celu - jesli podano [sourceId], najpierw leci tam pocisk od atakujacego,
     * dopiero potem wybucha. Id rosnie, wiec kolejne trafienia sie nie zlewaja.
     */
    private fun BattleUiState.spawnEffect(
        targetId: String,
        amount: Int,
        kind: HitKind,
        sourceId: String? = null
    ): BattleUiState =
        copy(floatingEffects = floatingEffects + FloatingEffect(nextEffectId++, targetId, amount, kind, sourceId))

    /** Ekran zdejmuje efekt, gdy jego animacja dobiegnie konca. */
    fun consumeFloatingEffect(id: Long) {
        _uiState.update { it.copy(floatingEffects = it.floatingEffects.filterNot { fx -> fx.id == id }) }
    }

    /**
     * Przyjmuje odpowiedz na biezace pytanie: liczy mnoznik obrazen tej tury z paska czasu na
     * odpowiedz, dopisuje kratke do serii i - jesli akurat wypadlo - odpala ulti albo rozlicza
     * zamkniety cykl dziesieciu odpowiedzi. Sama salwa ruszy dopiero po [continueQuiz].
     */
    fun answerQuiz(answerId: String) {
        _uiState.update { state ->
            val quiz = state.quiz ?: return@update state
            val question = quiz.question as? MultipleChoiceQuestion ?: return@update state
            if (state.phase != GamePhase.Battle || quiz.revealed) return@update state

            val isCorrect = question.answers.firstOrNull { it.id == answerId }?.isCorrect == true
            state.resolveQuizOutcome(
                quiz = quiz.copy(selectedAnswerId = answerId, revealed = true),
                isCorrect = isCorrect,
                reward = question.reward
            )
        }
    }

    /**
     * Klikniecie jednego elementu pytania "ulóz w kolejnosci" - dopisuje go na koniec dotychczas
     * ulozonej sekwencji. Dopiero gdy wszystkie elementy sa juz ulozone, pytanie jest rozstrzygane -
     * czesciowo poprawna kolejnosc nie liczy sie jako trafienie, jak przy pojedynczym wyborze.
     */
    fun pickOrderItem(itemId: String) {
        _uiState.update { state ->
            val quiz = state.quiz ?: return@update state
            val question = quiz.question as? OrderingQuestion ?: return@update state
            if (state.phase != GamePhase.Battle || quiz.revealed || itemId in quiz.orderedSelection) {
                return@update state
            }

            val selection = quiz.orderedSelection + itemId
            if (selection.size < question.items.size) {
                return@update state.copy(quiz = quiz.copy(orderedSelection = selection))
            }

            val isCorrect = selection == question.items.sortedBy { it.correctPosition }.map { it.id }
            state.resolveQuizOutcome(
                quiz = quiz.copy(orderedSelection = selection, revealed = true),
                isCorrect = isCorrect,
                reward = question.reward
            )
        }
    }

    /**
     * Wspolna czesc rozstrzygania pytania (obojetnie, wyboru czy ukladanki): mnoznik obrazen z
     * paska czasu, kratka serii, punkty, ewentualne ulti albo zamkniecie cyklu dziesieciu odpowiedzi.
     * [quiz] to juz gotowy, odsloniety stan pytania - tylko on rozni sie miedzy wariantami quizu.
     */
    private fun BattleUiState.resolveQuizOutcome(quiz: LiveQuizState, isCorrect: Boolean, reward: Int): BattleUiState {
        val streakOutcome = streak.record(isCorrect)
        runTally.recordAnswer(
            correct = isCorrect,
            category = currentQuestionCategory,
            difficulty = currentQuestionDifficulty,
            consecutiveCorrect = streakOutcome.tracker.consecutiveCorrect
        )
        recordSavedWordAnswer(quiz.question.id, isCorrect)

        // Im wiecej segmentow paska zostalo w chwili odpowiedzi, tym mocniejsza bedzie salwa
        // tej tury - zablokowane teraz, obowiazuje przez cala ture.
        val answeredAfterMs = questionShownAt.elapsedNow().inWholeMilliseconds
        val remainingFraction = 1f -
            (answeredAfterMs.toFloat() / AnswerTimer.WINDOW_MS).coerceIn(0f, 1f)
        val speedMultiplier = AnswerTimer.multiplierFor(remainingFraction)
        val cycleMultiplier = activeCycleMultiplier()
        currentTurnMultiplier = if (isCorrect) {
            speedMultiplier * cycleMultiplier
        } else {
            WRONG_ANSWER_MULTIPLIER
        }
        currentEnemyMultiplier = if (isCorrect) 1f else WRONG_ANSWER_ENEMY_BONUS

        // Rownanie pokazywane na TurnImpactOverlay - liczy sie z ataku ZYWYCH, bo tylko oni
        // faktycznie ruszaja w tej salwie. Same liczby sa juz policzone wyzej; tu je tylko zbieramy,
        // zeby overlay nie musial znac zadnej reguly walki.
        val baseAttack = characters.values.filter { it.isPlayer && it.hp > 0 }.sumOf { it.attack }
        val boostedAttack = (baseAttack * currentTurnMultiplier).roundToInt()
        val deltaPercent = ((currentTurnMultiplier - 1f) * 100).roundToInt()

        var next = copy(
            quiz = quiz,
            streak = streakOutcome.tracker,
            totalScore = totalScore + if (isCorrect) reward else 0,
            answeredCount = answeredCount + 1,
            correctCount = correctCount + if (isCorrect) 1 else 0,
            message = if (isCorrect) "Correct! +$reward pts" else "Wrong answer.",
            // Komplet liczb dla banera na polu bitwy - patrz [TurnOutcome].
            turnOutcome = TurnOutcome(
                id = answeredCount + 1L,
                correct = isCorrect,
                damageMultiplier = currentTurnMultiplier,
                reward = if (isCorrect) reward else 0,
                ultiTriggered = streakOutcome.ultiTriggered,
                cycleLabel = streakOutcome.cycleResult?.label,
                answerSeconds = answeredAfterMs / 1000f,
                speedMultiplier = speedMultiplier,
                cycleMultiplier = cycleMultiplier,
                baseAttack = baseAttack,
                boostedAttack = boostedAttack,
                attackDeltaPercent = deltaPercent
            )
        )

        if (streakOutcome.ultiTriggered) next = next.fireUlti()

        streakOutcome.cycleResult?.let { outcome ->
            next = next.copy(
                activeCycleBuff = CycleBuffState(outcome = outcome, expiresAtMs = next.elapsedMs + CYCLE_BUFF_DURATION_MS),
                message = outcome.label
            )
        }

        return next
    }

    /**
     * Losuje, czy atak elity/bossa zamienia sie w specjalne zdarzenie - i jakiego rodzaju. Null =
     * zwykly cios. WAZNE: sam rzut ustala tylko FLAVOR zdarzenia (leczenie/trucizna/oszolomienie) -
     * jego SILA zalezy dopiero od tego, jak pojdzie quiz (patrz [resolveBossEvent]). Boss nigdy nie
     * atakuje "od razu" - zawsze czeka na wynik.
     *
     * Razem [BOSS_HEAL_CHANCE] + [BOSS_POISON_CHANCE] + [BOSS_STUN_GAMBLE_CHANCE] = 30% na atak -
     * elita/boss bije normalnie w pozostalych 70% przypadkow.
     */
    /**
     * Pojedynek z bossem: rozpoczyna jego kolejny atak specjalny. Boss NIE bije zwyklymi ciosami -
     * kazda jego tura to jedno zdarzenie ([BossEventKind]), ktorego sile ustala quiz-oszacowanie
     * (patrz [resolveBossEvent]). Po zatwierdzeniu quizu rusza salwa druzyny ([startPlayerTurn]).
     */
    private fun beginBossTurn() {
        val state = _uiState.value
        if (state.phase != GamePhase.Battle) return
        val bossId = state.characters.values.firstOrNull { !it.isPlayer && it.hp > 0 }?.id ?: return
        abandonVolley()
        val kind = BOSS_DUEL_EVENTS.random(quizRandom)
        triggerBossEvent(kind, bossId)
    }

    /**
     * Pojedynek z bossem: salwa druzyny po jego ataku specjalnym. Sam boss nie ma tu kroku - bije
     * wylacznie zdarzeniami ([beginBossTurn]). Oszolomieni ([BattleUiState.stunnedCharacterIds])
     * pomijaja te salwe, a licznik [BattleUiState.stunTurnsRemaining] spada o jeden.
     */
    private fun startPlayerTurn() {
        val state = _uiState.value
        if (state.phase != GamePhase.Battle) return
        abandonVolley()

        val stunned = state.stunTurnsRemaining > 0
        val playerIds = state.characters.values
            .filter { it.isPlayer && it.hp > 0 && (!stunned || it.id !in state.stunnedCharacterIds) }
            .sortedBy { it.id }
            .map { it.id }

        volleyPlan = state.buildTurnPlan(playerIds, emptyList())
        currentRoundIndex = -1
        refreshGuards(turnStart = true)

        val remainingStun = (state.stunTurnsRemaining - 1).coerceAtLeast(0)
        _uiState.update {
            it.copy(
                turnPhase = TurnPhase.Resolving,
                stunTurnsRemaining = remainingStun,
                stunnedCharacterIds = if (remainingStun == 0) emptySet() else it.stunnedCharacterIds,
                message = if (stunned) "Part of the party is still stunned..." else null
            )
        }
        startNextVolley()
    }

    private fun rollBossSpecialEvent(random: Random): BossEventKind? {
        val roll = random.nextFloat()
        return when {
            roll < BOSS_HEAL_CHANCE -> BossEventKind.Heal
            roll < BOSS_HEAL_CHANCE + BOSS_POISON_CHANCE -> BossEventKind.Poison
            roll < BOSS_HEAL_CHANCE + BOSS_POISON_CHANCE + BOSS_STUN_GAMBLE_CHANCE -> BossEventKind.StunGamble
            else -> null
        }
    }

    /**
     * Odpala wylosowany FLAVOR zdarzenia zamiast zwyklego ciosu elity/bossa - boss NIE robi nic
     * jeszcze, tylko stawia [BattleUiState.bossEvent] z pytaniem do oszacowania. Dol ekranu przelacza
     * sie na [com.lernhero.game.battle.component.BossEventQuizBar] (ten sam suwak-quiz dla kazdego
     * z trzech rodzajow) i caly efekt (ile leczenia/trucizny/oszolomienia) liczy sie dopiero w
     * [confirmBossEventGuess], gdy gracz zatwierdzi zgadywanie.
     */
    private fun triggerBossEvent(kind: BossEventKind, attackerId: String) {
        _uiState.update { state ->
            // Boss odgrywa swoja animacje rzucania (rozwarcie pyska + ladowanie pocisku) ROZCIAGNIETA
            // na caly czas na odpowiedz - dzieki temu kulminacja animacji zbiega sie z koncem paska
            // quizu, zamiast konczyc sie po sekundzie i zostawiac bossa nieruchomo na resztę rundy.
            val boss = state.characters[attackerId]
            val characters = if (boss != null) {
                state.characters + (attackerId to boss.copy(
                    animSlot = AnimSlot.Cast,
                    animTrigger = boss.animTrigger + 1,
                    animDurationMs = (BOSS_EVENT_CHARGE_MS + AnswerTimer.WINDOW_MS).toInt(),
                    // Pocisk w pysku przybiera kolor pasujacy do tego, co boss wlasnie robi.
                    animEffectTintArgb = bossEventTintArgb(kind),
                    // Najazd kamery - pysk bossa wchodzi na ekran na czas calego zdarzenia.
                    cameraZoom = BOSS_EVENT_CAMERA_ZOOM
                ))
            } else {
                state.characters
            }

            state.copy(
                characters = characters,
                bossEvent = run {
                    // Wlasna talia moze niesc swoje pytania "oszacuj" - gdy nie niesie zadnego,
                    // boss i tak musi miec czym zaatakowac, wiec wracamy do banku wbudowanego.
                    val question = runSession.customDeck
                        ?.let { CustomDeckBank.drawEstimation(it, quizRandom) }
                        ?: EstimationBank.random(quizRandom, languageStore.current.code)
                    val range = question.sliderRange(quizRandom)
                    BossEventState(
                        bossId = attackerId,
                        kind = kind,
                        question = question,
                        sliderMin = range.first,
                        sliderMax = range.last,
                        phase = BossEventPhase.Charging,
                        projection = state.projectBossEvent(kind, attackerId)
                    )
                },
                message = bossEventAnnounceMessage(kind)
            )
        }

        // Ladowanie leci BEZ quizu - dopiero po nim panel wjezdza od dolu i rusza licznik czasu.
        // Wczesniej wszystko pojawialo sie naraz i nie bylo widac, co boss wlasciwie robi.
        viewModelScope.launch {
            delay(BOSS_EVENT_CHARGE_MS)
            if (_uiState.value.bossEvent?.phase != BossEventPhase.Charging) return@launch
            bossEventShownAt = TimeSource.Monotonic.markNow()
            _uiState.update { state ->
                val event = state.bossEvent ?: return@update state
                state.copy(bossEvent = event.copy(phase = BossEventPhase.Quiz))
            }
        }
    }

    /**
     * Liczy NAJGORSZY mozliwy skutek zdarzenia (fatalny quiz, performance = 0) - to samo, co
     * policzylyby resolve*Event dla performance = 0. Sluzy wylacznie do pokazania stawki na
     * liczniku podczas ladowania; faktyczny wynik liczy sie normalnie po zatwierdzeniu.
     */
    private fun BattleUiState.projectBossEvent(kind: BossEventKind, bossId: String): BossEventProjection {
        val boss = characters[bossId]
        val alivePlayers = characters.values.count { it.isPlayer && it.hp > 0 }
        return when (kind) {
            BossEventKind.Heal -> {
                val missingHp = boss?.let { (it.maxHp - it.hp).coerceAtLeast(0) } ?: 0
                BossEventProjection(
                    attackName = "PIESN KOSCI",
                    valueLabel = "LECZENIE",
                    chargedValue = (missingHp * BOSS_HEAL_MAX_FRACTION).roundToInt()
                )
            }

            BossEventKind.Poison -> {
                val total = characters.values
                    .filter { it.isPlayer && it.hp > 0 }
                    .sumOf { (it.maxHp * BOSS_POISON_MAX_FRACTION).roundToInt().coerceAtLeast(1) }
                BossEventProjection(
                    attackName = "TRUJACY ODDECH",
                    valueLabel = "OBRAZENIA",
                    chargedValue = total
                )
            }

            BossEventKind.Strike -> {
                val perPlayer = ((boss?.attack ?: 0) * BOSS_STRIKE_MAX_ATTACK_MULTIPLIER).roundToInt()
                BossEventProjection(
                    attackName = "HEAVY BLOW",
                    valueLabel = "OBRAZENIA",
                    chargedValue = perPlayer * alivePlayers
                )
            }

            BossEventKind.StunGamble -> BossEventProjection(
                attackName = "KLATWA BEZRUCHU",
                valueLabel = "OGLUSZENIE",
                chargedValue = (BOSS_STUN_MAX_FRACTION * 100f).roundToInt(),
                unit = "%"
            )
        }
    }

    /**
     * Kolor pocisku ladowanego w pysku bossa - czytelny sygnal, co za chwile zrobi. Odcienie
     * wziete z palety zywiolow ([com.lernhero.domain.monster.Element]), zeby caly UI mowil jednym
     * jezykiem kolorow.
     */
    private fun bossEventTintArgb(kind: BossEventKind): Long = when (kind) {
        BossEventKind.Heal -> 0xFF4ADE80        // zielen - jak Natura
        BossEventKind.Poison -> 0xFF9B6BD6      // fiolet - jak Smierc (i tak wyglada dzis PNG)
        BossEventKind.StunGamble -> 0xFFFFD34D  // zolc - "uwazaj"
        BossEventKind.Strike -> 0xFFE0654F      // czerwien - jak Ogien
    }

    private fun bossEventAnnounceMessage(kind: BossEventKind): String = when (kind) {
        BossEventKind.Heal -> "The boss prepares to heal..."
        BossEventKind.Poison -> "The boss prepares a poisonous breath..."
        BossEventKind.StunGamble -> "The boss prepares a powerful spell..."
        BossEventKind.Strike -> "The boss winds up for a powerful strike..."
    }

    /** Suwak w [com.lernhero.game.battle.component.BossEventQuizBar] przesunal sie - zapamietujemy wartosc bez rozstrzygania. */
    fun submitBossEventGuess(value: Int) {
        _uiState.update { state ->
            val event = state.bossEvent ?: return@update state
            if (event.resolved) return@update state
            state.copy(bossEvent = event.copy(guess = value.coerceIn(event.sliderMin, event.sliderMax)))
        }
    }

    /**
     * Gracz zatwierdzil zgadywana wartosc (albo czas sie skonczyl) - DOPIERO TERAZ liczymy i
     * aplikujemy efekt zdarzenia (patrz [resolveBossEvent]), i po krotkiej chwili na przeczytanie
     * wyniku wracamy do przerwanej salwy.
     */
    fun confirmBossEventGuess() {
        val event = _uiState.value.bossEvent ?: return
        if (event.resolved) return
        // Ladowanie jeszcze trwa - nie ma czego zatwierdzac (auto-confirm z paska tez tu nie wejdzie).
        if (event.phase == BossEventPhase.Charging) return

        _uiState.update { state ->
            val current = state.bossEvent ?: return@update state
            if (current.resolved) return@update state
            // Wynik jest juz policzony i zastosowany, ale zanim wroci walka, pokazujemy WERDYKT:
            // ile bylo naprawde, ile dal gracz i o ile to scielo cios.
            state.resolveBossEvent(current).let { resolved ->
                resolved.copy(bossEvent = resolved.bossEvent?.copy(phase = BossEventPhase.Judging))
            }
        }

        // Zaklecie ogluszylo kogos: reszta biezacej salwy zostala zbudowana ZANIM ktokolwiek byl
        // oszolomiony (patrz continueQuiz), wiec ciagnieciem jej dalej ogluszeni i tak by atakowali.
        // Zamiast dokonczyc te nieaktualna kolejke, odrzucamy ja i od razu wchodzimy w pierwsza
        // oszolomiona runde - dokladnie tak samo jak wtedy, gdy stun trwa juz miedzy turami.
        val justStunned = _uiState.value.stunTurnsRemaining > 0

        viewModelScope.launch {
            // Werdykt: gracz czyta, o ile sciął cios, i widzi jak licznik zjezdza z najgorszego
            // przypadku do faktycznego wyniku.
            delay(BOSS_EVENT_JUDGING_MS)
            _uiState.update { state ->
                val current = state.bossEvent ?: return@update state
                state.copy(bossEvent = current.copy(phase = BossEventPhase.Payoff))
            }

            // Kulminacja: kula wychodzi z pyska i leci w cel (albo wybucha na bossie przy leczeniu).
            delay(BOSS_EVENT_PAYOFF_MS)
            _uiState.update { state ->
                // Zdarzenie zamkniete - boss wraca do spoczynku, zeby pysk nie zostal otwarty
                // na cala nastepna ture.
                val boss = state.characters[event.bossId]
                state.copy(
                    bossEvent = null,
                    characters = if (boss != null) {
                        state.characters + (event.bossId to boss.copy(
                            animSlot = null,
                            animDurationMs = null,
                            animEffectTintArgb = null,
                            cameraZoom = 1f
                        ))
                    } else {
                        state.characters
                    }
                )
            }

            // Trucizna / potezny cios mogly rozniesc cala druzyne - sprawdzamy to przed dalsza walka.
            if (resolveOutcomeIfOver(_uiState.value)) return@launch

            if (isBossDuel) {
                // Zdarzenie bossa juz naliczylo swoj efekt (w tym stun) - teraz salwa druzyny.
                startPlayerTurn()
            } else if (justStunned) {
                runStunnedRound(_uiState.value)
            } else {
                settleVolleyStep()
            }
        }
    }

    /**
     * Liczy [BossEventOutcome] PO zatwierdzeniu quizu i od razu aplikuje jego skutek na postaci -
     * ani leczenie, ani trucizna, ani oszolomienie nie sa naliczone wczesniej. [performance] (0..1,
     * 0 = fatalnie, 1 = idealnie) to srednia szybkosci (ile segmentow paska zostalo) i celnosci
     * zgadywania - to ona skaluje kazdy z trzech wariantow: im lepszy quiz, tym slabszy efekt bossa.
     */
    private fun BattleUiState.resolveBossEvent(event: BossEventState): BattleUiState {
        // 1) CZAS laduje zaklecie. Licznik na ekranie rosnie razem z ta wartoscia, wiec zwlekanie
        //    kosztuje wprost i widac to na biezaco. Podloga sprawia, ze nawet blyskawiczna
        //    odpowiedz cos kosztuje - inaczej sama szybkosc kasowalaby atak i celnosc nie mialaby
        //    juz znaczenia.
        val elapsedFraction =
            (bossEventShownAt.elapsedNow().inWholeMilliseconds.toFloat() / AnswerTimer.WINDOW_MS).coerceIn(0f, 1f)
        val timeScale = BOSS_TIME_FLOOR + (1f - BOSS_TIME_FLOOR) * elapsedFraction

        // 2) CELNOSC mnozy to, co sie naladowalo - w OBIE strony. Trafiona data tnie cios,
        //    strzal w kosmos realnie go wzmacnia (stad "+190%" na ekranie werdyktu).
        // Celnosc liczona wzgledem TOLERANCJI TEGO pytania, a nie szerokosci suwaka - inaczej
        // 10 lat pomylki przy Mahomecie i przy premierze iPhone'a dostawaloby te sama ocene.
        val closeness = event.question.closeness(event.guess)
        val accuracyMultiplier = BOSS_ACCURACY_WORST_MULTIPLIER +
            (BOSS_ACCURACY_BEST_MULTIPLIER - BOSS_ACCURACY_WORST_MULTIPLIER) * closeness

        val severity = (timeScale * accuracyMultiplier).coerceAtLeast(0f)
        // Zachowane dla ekranow, ktore mowia jezykiem "jak dobrze poszlo" (1 = idealnie).
        val performance = (1f - (severity / BOSS_ACCURACY_WORST_MULTIPLIER)).coerceIn(0f, 1f)

        val ctx = BossEventMath(
            severity = severity,
            accuracyMultiplier = accuracyMultiplier,
            timeScale = timeScale,
            closeness = closeness,
            performance = performance
        )

        return when (event.kind) {
            BossEventKind.Heal -> resolveHealEvent(event, ctx)
            BossEventKind.Poison -> resolvePoisonEvent(event, ctx)
            BossEventKind.StunGamble -> resolveStunGambleEvent(event, ctx)
            BossEventKind.Strike -> resolveStrikeEvent(event, ctx)
        }
    }

    /**
     * Rozlozony na czynniki wynik quizu bossa. [severity] to jedyna liczba, ktorej potrzebuja
     * poszczegolne resolve*Event - reszta jedzie z nia tylko po to, zeby ekran werdyktu mogl
     * pokazac, SKAD sie wziela.
     */
    private data class BossEventMath(
        /** Ile razy wartosc bazowa - iloczyn naladowanego czasu i mnoznika z celnosci. */
        val severity: Float,
        val accuracyMultiplier: Float,
        val timeScale: Float,
        val closeness: Float,
        val performance: Float
    )

    /**
     * Fatalny quiz (performance=0) tnie cala druzyne za [BOSS_STRIKE_MAX_ATTACK_MULTIPLIER] ataku
     * bossa kazdego; idealny (performance=1) - cios calkiem chybia. To jedyny sposob, w jaki boss
     * zadaje obrazenia w pojedynku (patrz [isBossDuel]).
     */
    private fun BattleUiState.resolveStrikeEvent(event: BossEventState, math: BossEventMath): BattleUiState {
        val boss = characters[event.bossId]
        val bossAttack = boss?.attack ?: 0
        val damagePerPlayer = (bossAttack * BOSS_STRIKE_MAX_ATTACK_MULTIPLIER * math.severity).roundToInt()

        val alivePlayers = characters.values.filter { it.isPlayer && it.hp > 0 }
        val damageByPlayer = if (damagePerPlayer <= 0) {
            emptyMap()
        } else {
            alivePlayers.associate { it.id to damagePerPlayer.coerceAtLeast(1) }
        }
        val totalDamage = damageByPlayer.values.sum()

        var updatedCharacters = characters
        var effects = floatingEffects
        damageByPlayer.forEach { (playerId, dmg) ->
            val player = updatedCharacters[playerId] ?: return@forEach
            val after = (player.hp - dmg).coerceAtLeast(0)
            updatedCharacters = updatedCharacters.plus(
                playerId to player.copy(hp = after, state = if (after <= 0) CharacterState.DEAD else player.state)
            )
            effects = effects + FloatingEffect(nextEffectId++, playerId, dmg, HitKind.Hit)
        }

        val message = if (totalDamage <= 0) {
            "Perfect! The boss's strike hit nothing."
        } else {
            "A powerful strike hits the party for a total of $totalDamage HP."
        }
        val outcome = BossEventOutcome(
            performance = math.performance,
            message = message,
            accuracyMultiplier = math.accuracyMultiplier,
            chargedValue = (bossAttack * BOSS_STRIKE_MAX_ATTACK_MULTIPLIER * math.timeScale).roundToInt() *
                characters.values.count { it.isPlayer && it.hp > 0 },
            strikeDamage = totalDamage
        )

        return copy(
            characters = updatedCharacters,
            floatingEffects = effects,
            bossEvent = event.copy(resolved = true, outcome = outcome),
            message = message
        )
    }

    /** Fatalny quiz (performance=0) leczy bossa za [BOSS_HEAL_MAX_FRACTION] brakujacego HP; idealny (performance=1) - za nic. */
    private fun BattleUiState.resolveHealEvent(event: BossEventState, math: BossEventMath): BattleUiState {
        val boss = characters[event.bossId]
        val missingHp = boss?.let { (it.maxHp - it.hp).coerceAtLeast(0) } ?: 0
        val healFraction = BOSS_HEAL_MAX_FRACTION * math.severity
        val healAmount = (missingHp * healFraction).roundToInt().coerceAtLeast(0)

        val message = if (healAmount <= 0) {
            "Perfect! The boss couldn't heal in time."
        } else {
            "The boss heals for $healAmount HP."
        }
        val outcome = BossEventOutcome(
            performance = math.performance,
            message = message,
            accuracyMultiplier = math.accuracyMultiplier,
            chargedValue = (missingHp * BOSS_HEAL_MAX_FRACTION * math.timeScale).roundToInt(),
            healAmount = healAmount
        )

        var updatedCharacters = characters
        var effects = floatingEffects
        if (healAmount > 0 && boss != null) {
            updatedCharacters = updatedCharacters.plus(
                event.bossId to boss.copy(hp = (boss.hp + healAmount).coerceAtMost(boss.maxHp))
            )
            effects = effects + FloatingEffect(nextEffectId++, event.bossId, healAmount, HitKind.Heal)
        }

        return copy(
            characters = updatedCharacters,
            floatingEffects = effects,
            bossEvent = event.copy(resolved = true, outcome = outcome),
            message = message
        )
    }

    /** Fatalny quiz (performance=0) zatruwa druzyne za [BOSS_POISON_MAX_FRACTION] HP kazdego; idealny (performance=1) - za nic. */
    private fun BattleUiState.resolvePoisonEvent(event: BossEventState, math: BossEventMath): BattleUiState {
        val damageFraction = BOSS_POISON_MAX_FRACTION * math.severity
        val alivePlayers = characters.values.filter { it.isPlayer && it.hp > 0 }
        val damageByPlayer = if (damageFraction <= 0f) {
            emptyMap()
        } else {
            alivePlayers.associate { it.id to (it.hp * damageFraction).roundToInt().coerceAtLeast(1) }
        }
        val totalDamage = damageByPlayer.values.sum()

        var updatedCharacters = characters
        var effects = floatingEffects
        damageByPlayer.forEach { (playerId, dmg) ->
            val player = updatedCharacters[playerId] ?: return@forEach
            val after = (player.hp - dmg).coerceAtLeast(0)
            updatedCharacters = updatedCharacters.plus(
                playerId to player.copy(hp = after, state = if (after <= 0) CharacterState.DEAD else player.state)
            )
            effects = effects + FloatingEffect(nextEffectId++, playerId, dmg, HitKind.Poison)
        }

        val message = if (totalDamage <= 0) {
            "Perfect! The poison didn't have time to work."
        } else {
            "The poison hits the party for a total of $totalDamage HP."
        }
        val outcome = BossEventOutcome(
            performance = math.performance,
            message = message,
            accuracyMultiplier = math.accuracyMultiplier,
            chargedValue = characters.values
                .filter { it.isPlayer && it.hp > 0 }
                .sumOf { (it.hp * BOSS_POISON_MAX_FRACTION * math.timeScale).roundToInt() },
            poisonDamage = totalDamage
        )

        return copy(
            characters = updatedCharacters,
            floatingEffects = effects,
            bossEvent = event.copy(resolved = true, outcome = outcome),
            message = message
        )
    }

    /**
     * Fatalny quiz oszalamia az 85% druzyny (uzywajac szybkosci), a im dalej
     * od poprawnej wartosci (uzywajac celnosci), tym dluzej to trwa. Idealna szybkosc (performance
     * >= 0.85) zawsze przerywa zaklecie calkiem, bez wzgledu na to, jak bardzo chybiona byla zgadywana wartosc.
     */
    private fun BattleUiState.resolveStunGambleEvent(event: BossEventState, math: BossEventMath): BattleUiState {
        // Ta sama skala co reszta zdarzen: czas laduje, celnosc mnozy. Sufit 85% zostaje - nawet
        // najgorszy wynik ma zostawic graczowi kogos, kto sie rusza.
        val affectedFraction = (BOSS_STUN_MAX_FRACTION * math.severity).coerceIn(0f, BOSS_STUN_MAX_FRACTION)
        val interrupted = affectedFraction < BOSS_STUN_INTERRUPT_THRESHOLD
        val stunTurns = when {
            math.closeness >= 0.9f -> 1
            math.closeness >= 0.6f -> 2
            else -> 3
        }

        val alivePlayers = characters.values.filter { it.isPlayer && it.hp > 0 }
        val affectedCount = if (interrupted) 0 else {
            (alivePlayers.size * affectedFraction).roundToInt().coerceIn(0, alivePlayers.size)
        }
        // Najpierw ci z najnizszym HP - to oni najbardziej odczuja utrate tury bez obrony.
        val affectedIds = alivePlayers.sortedBy { it.hp }.take(affectedCount).map { it.id }.toSet()

        val message = if (interrupted) {
            "You interrupted the boss's spell!"
        } else {
            val percent = (affectedFraction * 100).roundToInt()
            val turnsWord = if (stunTurns == 1) "turn" else "turns"
            "The boss stuns $percent% of the party for $stunTurns $turnsWord!"
        }

        val outcome = BossEventOutcome(
            performance = math.performance,
            message = message,
            accuracyMultiplier = math.accuracyMultiplier,
            chargedValue = ((BOSS_STUN_MAX_FRACTION * math.timeScale).coerceAtMost(BOSS_STUN_MAX_FRACTION) * 100f)
                .roundToInt(),
            affectedFraction = if (interrupted) 0f else affectedFraction,
            stunTurns = stunTurns,
            interrupted = interrupted
        )

        return copy(
            bossEvent = event.copy(resolved = true, outcome = outcome),
            stunTurnsRemaining = if (interrupted) stunTurnsRemaining else stunTurns,
            stunnedCharacterIds = if (interrupted) stunnedCharacterIds else affectedIds,
            message = message
        )
    }

    /** Piec dobrych odpowiedzi z rzedu: cala druzyna wali raz w kazdego wroga widocznego na ekranie. */
    private fun BattleUiState.fireUlti(): BattleUiState {
        val teamAttack = characters.values.filter { it.isPlayer && it.hp > 0 }.sumOf { it.attack }
        if (teamAttack <= 0) return this
        val mightStacks = endlessBuffs[EndlessBuff.Might] ?: 0
        val avatarStacks = endlessBuffs[EndlessBuff.Avatar] ?: 0
        val overloadStacks = endlessBuffs[EndlessBuff.Overload] ?: 0
        val statBonus = 1f + mightStacks * MIGHT_ATTACK_BONUS_PER_STACK + avatarStacks * AVATAR_BONUS_PER_STACK
        val overload = 1f + overloadStacks * OVERLOAD_ULTI_BONUS_PER_STACK
        val amount = (teamAttack * activeCycleMultiplier() * ULTI_FACTOR * statBonus * overload)
            .roundToInt().coerceAtLeast(1)

        var updatedCharacters = characters
        var effects = floatingEffects
        characters.values.filter { !it.isPlayer && it.hp > 0 }.forEach { enemy ->
            val after = (enemy.hp - amount).coerceAtLeast(0)
            updatedCharacters = updatedCharacters.plus(
                enemy.id to enemy.copy(hp = after, state = if (after <= 0) CharacterState.DEAD else enemy.state)
            )
            effects = effects + FloatingEffect(nextEffectId++, enemy.id, amount, HitKind.Skill)
        }

        return copy(
            characters = updatedCharacters,
            floatingEffects = effects,
            ultiPulseId = ultiPulseId + 1,
            message = "ULTI! The whole wave took $amount"
        )
    }

    /**
     * Odpowiedz zostala pokazana - czas ruszyc faze rozstrzygania. Kazda postac atakuje po kolei,
     * jedna na raz: najpierw wszyscy zywi gracze w ustalonej kolejnosci, potem wszyscy zywi
     * wrogowie, i tak przez [ROUNDS_PER_TURN] rund.
     */
    fun continueQuiz() {
        val state = _uiState.value
        val quiz = state.quiz ?: return
        if (state.phase != GamePhase.Battle || !quiz.revealed) return
        // Panel quizu sam odpala [continueQuiz] po chwili na przeczytanie odpowiedzi - gdyby zrobil
        // to drugi raz (rekompozycja, powrot na ekran), ruszylaby DRUGA salwa obok trwajacej.
        if (state.turnPhase != TurnPhase.Question) return

        // Nowa tura = koniec wszystkiego, co zostalo po poprzedniej (i po jej opoznionych `delay`).
        abandonVolley()

        // Najpierw sam takt rozliczenia odpowiedzi: pole bitwy juz sie rozsuwa, ale nikt jeszcze nie
        // bije - gracz ma chwile, zeby zobaczyc, co jego odpowiedz zrobila z sila druzyny.
        _uiState.update { it.copy(turnPhase = TurnPhase.Impact, message = null) }
        // Ten sam straznik co w [scheduleVolley]: gdy tura zostanie w miedzyczasie porzucona
        // (koniec fali, przegrana, wyjscie z ekranu), takt Impact nie odpali juz salwy.
        val token = volleyToken
        viewModelScope.launch {
            delay(BattleSpeed.scale(IMPACT_HOLD_MILLIS))
            if (token != volleyToken) return@launch
            startResolving()
        }
    }

    /**
     * Wlasciwy start salwy - odpalany dopiero PO [TurnPhase.Impact], zeby animacja rozliczenia
     * odpowiedzi nie scigala sie z lecacymi pociskami.
     */
    private fun startResolving() {
        val state = _uiState.value
        if (state.phase != GamePhase.Battle || state.turnPhase != TurnPhase.Impact) return

        // Tylko zywi - martwe kroki i tak przelatuja na sucho, ale zasmiecaja kolejke i przez to
        // koniec tury (a wiec i wykrycie oczyszczonej fali) potrafil sie o nie oprzec.
        val playerIds = state.characters.values.filter { it.isPlayer && it.hp > 0 }.sortedBy { it.id }.map { it.id }
        val enemyIds = state.characters.values.filter { !it.isPlayer && it.hp > 0 }.sortedBy { it.id }.map { it.id }
        volleyPlan = state.buildTurnPlan(playerIds, enemyIds)
        currentRoundIndex = -1
        refreshGuards(turnStart = true)

        _uiState.update { it.copy(turnPhase = TurnPhase.Resolving, message = null) }
        startNextVolley()
    }

    private fun drawNextQuestion(wave: Int, forcedCategory: QuizCategory? = null): LiveQuizState {
        // Endless pozwala wybrac STALY poziom pytan przed walka (patrz EndlessSetupPanel) - wtedy
        // drabinka rosnaca z fala nie obowiazuje, bo gracz przyszedl cwiczyc konkretny poziom.
        val fixed = runSession.quizDifficulty
        val difficulty = if (fixed != null) {
            fixed
        } else {
            val ladder = QuizBank.ladderFor(wave)
            ladder[quizCounter % ladder.size]
        }
        quizCounter += 1
        currentQuestionDifficulty = difficulty

        // Wlasne slowka to zawsze wybor z czterech (ulozenie w kolejnosci nie pasuje do fiszek)
        // i zawsze pytaja WYLACZNIE o slowka gracza - inaczej "trenuje slownictwo" bylby klamstwem,
        // gdyby co druga runda wracala do wbudowanego banku.
        if (runSession.customWords.isNotEmpty()) {
            return LiveQuizState(question = drawMultipleChoiceQuestion(difficulty))
        }

        // Wlasna talia gracza dziala tak samo jak fiszki: skoro gracz przyszedl cwiczyc SWOJ
        // material, to wbudowany bank nie ma tu czego szukac. Talia moze niesc kazdy z typow
        // pytan, wiec o rodzaj tury decyduje wylosowane pytanie, nie rzut kostka jak nizej.
        runSession.customDeck?.let { deck ->
            deckQuestion(deck, difficulty)?.let { return LiveQuizState(question = it) }
        }

        // Temat wskazany przez gracza na kaflu ma pierwszenstwo przed kategoria runu.
        val category = forcedCategory ?: runSession.category

        // "Zaznacz wszystkie prawdziwe" wchodzi jako trzeci typ pytania obok wyboru i ukladanki.
        val multiRoll = quizRandom.nextFloat()
        if (multiRoll < MULTI_SELECT_QUESTION_CHANCE) {
            val multi = MultiSelectBank.draw(difficulty, category = category, random = quizRandom)
            if (multi != null) {
                currentQuestionCategory = multi.category
                return LiveQuizState(question = multi.toUiQuestion())
            }
        }

        // Na razie testowo pol na pol - tyle wystarczy, zeby sprawdzic, jak ukladanka gra sie w boju.
        val question = if (quizRandom.nextFloat() < ORDERING_QUESTION_CHANCE) {
            val ordering = OrderingBank.draw(difficulty, category = category, random = quizRandom)
            if (ordering != null) {
                currentQuestionCategory = ordering.category
                ordering.toUiQuestion()
            } else {
                drawMultipleChoiceQuestion(difficulty, category)
            }
        } else {
            drawMultipleChoiceQuestion(difficulty, category)
        }
        return LiveQuizState(question = question)
    }

    /**
     * Jedno pytanie z wlasnej talii, zamienione na to, co rozumie ekran. Null tylko wtedy, gdy
     * talia nie ma ani jednego gotowego pytania na ture - wtedy walka wraca do zwyklego losowania
     * zamiast stanac (gracz mogl usunac pytania w trakcie trwania walki).
     */
    private fun deckQuestion(deck: CustomDeck, difficulty: QuizDifficulty): QuizQuestion? {
        val question = CustomDeckBank.draw(deck, difficulty, quizRandom) ?: return null
        currentQuestionCategory = QuizCategory.Custom
        currentQuestionDifficulty = question.difficulty
        return when (question.type) {
            CustomQuestionType.Choice -> question.toQuizEntry(deck.id)?.toUiQuestion()
            CustomQuestionType.MultiSelect -> question.toMultiSelectEntry(deck.id)?.toUiQuestion()
            CustomQuestionType.Ordering -> question.toOrderingEntry(deck.id)?.toUiQuestion()
            // Do tury nie trafia (patrz CustomDeck.turnReady) - pada wylacznie jako zdarzenie bossa.
            CustomQuestionType.Estimation -> null
        }
    }

    private fun drawMultipleChoiceQuestion(
        difficulty: QuizDifficulty,
        category: QuizCategory? = runSession.category
    ): QuizQuestion {
        val customWords = runSession.customWords
        val entry = if (customWords.isNotEmpty()) {
            WordQuizBank.draw(customWords, quizRandom)
        } else {
            null
        } ?: QuizBank.draw(listOf(difficulty), category = category, random = quizRandom).firstOrNull()
            ?: QuizBank.all(quizRandom).random(quizRandom)
        currentQuestionCategory = entry.category
        return entry.toUiQuestion()
    }

    /** Kapitulacja w trakcie walki: druzyna ucieka zywa, ale bez lupu z biezacej fali. */
    fun capitulate() {
        _uiState.update { state ->
            if (state.phase != GamePhase.Battle) return@update state

            runSession.savePartyHp(state.playerHp(), state.playerMaxHp())
            runSession.lastBattleWon = false
            val bagTotal = runSession.lootGold
            val banked = runSession.settleLoot(CAPITULATE_LOOT_FRACTION)

            val summary = runTally.toRunStats(
                won = false,
                endless = request.isEndless,
                gold = banked,
                bossDefeated = false
            )
            submitRunResult(summary)

            state.copy(
                phase = GamePhase.Defeat,
                rewardCoins = banked,
                rewardSaved = true,
                runSummary = summary,
                message = "You surrendered on wave ${state.wave}. You save $banked of $bagTotal coins from your bag (25%)."
            )
        }
    }

    /** HP graczy na koniec biezacego stanu - to, co zapisuje sie do [RunSession] miedzy walkami. */
    private fun BattleUiState.playerHp(): Map<String, Int> =
        characters.values.filter { it.isPlayer }.associate { it.id to it.hp.coerceAtLeast(0) }

    /** MaxHP graczy (z bonusami juz doliczonymi) - zapisywane razem z [playerHp] do [RunSession]. */
    private fun BattleUiState.playerMaxHp(): Map<String, Int> =
        characters.values.filter { it.isPlayer }.associate { it.id to it.maxHp }

    /** Fala oczyszczona: lup do sakwy, nastepna fala wchodzi bez przerwy - to jest endless run. */
    private fun BattleUiState.spawnNextWave(): BattleUiState {
        val nextWave = wave + 1
        runSession.savePartyHp(playerHp(), playerMaxHp())
        runSession.addLoot(calculateWaveCoins(this))
        runTally.recordWaveCleared(wave, request.kind, runSession.difficulty)

        val survivors = characters.values
            .filter { it.isPlayer }
            .map { it.copy(state = if (it.hp <= 0) CharacterState.DEAD else CharacterState.IDLE, currentTargetId = null) }

        // Endless: co ENDLESS_BUFF_INTERVAL oczyszczonych fal gracz dostaje do wyboru komplet kart
        // ([EndlessBuff.offer]) i bierze jedna na stale, do konca biegu - patrz [chooseUpgrade].
        // Im lepiej szedl quiz do tej pory, tym czesciej w ofercie sa te mocniejsze karty.
        // Story mode nigdy tego nie widzi (request.isEndless).
        val quality = EndlessBuff.qualityFor(correctCount, answeredCount)
        val offer = if (request.isEndless && wave % ENDLESS_BUFF_INTERVAL == 0) {
            EndlessBuff.offer(endlessBuffs, quality, combatRandom)
                .takeIf { it.isNotEmpty() }
                ?.let { UpgradeOffer(wave = nextWave, choices = it, quality = quality, ownedStacks = endlessBuffs) }
        } else {
            null
        }

        // Pytania tu NIE MA - fala najpierw wchodzi na ekran, dopiero potem [askNextQuestion]
        // (patrz [resolveOutcomeIfOver]) rysuje pytanie i startuje pasek czasu na odpowiedz.
        return copy(
            wave = nextWave,
            characters = buildWave(nextWave, survivors),
            turnPhase = TurnPhase.Resolving,
            quiz = null,
            floatingEffects = emptyList(),
            turnOutcome = null,
            // Garda i drugi oddech odnawiaja sie z kazda fala - patrz [refreshGuards]/[EndlessBuff.SecondWind].
            guardCharges = emptyMap(),
            secondWindUses = emptyMap(),
            upgradeOffer = offer,
            message = "Wave $nextWave incoming..."
        ).applyWaveRegeneration()
    }

    /** [EndlessBuff.Regeneration]: kazda oczyszczona fala oddaje druzynie czesc maks. HP. */
    private fun BattleUiState.applyWaveRegeneration(): BattleUiState {
        val stacks = endlessBuffs[EndlessBuff.Regeneration] ?: 0
        if (stacks <= 0) return this
        var next = this
        characters.values.filter { it.isPlayer && it.hp > 0 }.forEach { player ->
            next = next.healCharacter(player.id, player.maxHp * stacks * REGENERATION_PER_STACK)
        }
        return next
    }

    /**
     * Gracz wybral karte ulepszenia - dopisujemy stak (z sufitem [EndlessBuff.maxStacks]) i dopiero
     * TERAZ ruszamy pytanie nowej fali. Dopoki oferta lezy na stole, walka stoi.
     */
    fun chooseUpgrade(buff: EndlessBuff) {
        val offer = _uiState.value.upgradeOffer ?: return
        if (buff !in offer.choices) return

        _uiState.update { state ->
            if (state.upgradeOffer == null) return@update state
            state.grantUpgrade(buff).copy(upgradeOffer = null)
        }
        askNextQuestion(keepMessage = true)
    }

    /**
     * Dopisuje jeden stak ulepszenia. Wiekszosc bonusow dziala "z mapy" (czyta je matematyka
     * obrazen/leczenia), ale te, ktore ruszaja MAKS. HP ([EndlessBuff.Vitality], [EndlessBuff.Avatar]),
     * musza od razu podniesc statystyki postaci - i o tyle samo je podleczyc, zeby bonus bylo czuc
     * natychmiast, a nie dopiero po nastepnym leczeniu.
     */
    private fun BattleUiState.grantUpgrade(buff: EndlessBuff): BattleUiState {
        val stacks = ((endlessBuffs[buff] ?: 0) + 1).coerceAtMost(buff.maxStacks)
        var next = copy(
            endlessBuffs = endlessBuffs + (buff to stacks),
            message = "Upgrade: ${buff.title}! ${buff.description}"
        )

        val hpBonus = when (buff) {
            EndlessBuff.Vitality -> VITALITY_MAX_HP_PER_STACK
            EndlessBuff.Avatar -> AVATAR_BONUS_PER_STACK
            else -> 0f
        }
        if (hpBonus > 0f) {
            next.characters.values.filter { it.isPlayer }.forEach { player ->
                val extra = (player.maxHp * hpBonus).roundToInt().coerceAtLeast(1)
                val boosted = player.copy(
                    maxHp = player.maxHp + extra,
                    hp = if (player.hp > 0) player.hp + extra else player.hp
                )
                next = next.copy(characters = next.characters.plus(player.id to boosted))
            }
        }

        return next
    }

    /** Ostatnia fala z [BattleRequest.waves] oczyszczona - walka wygrana, caly lup wraca na konto. */
    private fun BattleUiState.finishVictory(): BattleUiState {
        runSession.savePartyHp(playerHp(), playerMaxHp())
        runSession.lastBattleWon = true
        runSession.addLoot(calculateWaveCoins(this))
        runTally.recordWaveCleared(wave, request.kind, runSession.difficulty)
        runTally.recordBossBonus(request.kind, runSession.activeStoryStage ?: 1)
        val banked = runSession.settleLoot(VICTORY_LOOT_FRACTION)

        // Lup rzeczowy - to samo losowanie, ktore przycisk LOOT zapowiada graczowi przed walka
        // (patrz [StageLoot.roll]). Tylko kampania: w endless nie ma planszy, z ktorej pula
        // mialaby sie wziac, a jego wlasna nagroda to stakujace sie bonusy fal.
        val storyStage = runSession.activeStoryStage
        val dropped = if (!request.isEndless && storyStage != null) {
            StageLoot.roll(
                globalStage = storyStage,
                tier = runSession.difficulty,
                isBoss = request.kind == BattleKind.Boss
            )
        } else {
            null
        }

        val summary = runTally.toRunStats(
            won = true,
            endless = request.isEndless,
            gold = banked,
            bossDefeated = request.kind == BattleKind.Boss,
            droppedItemIds = listOfNotNull(dropped?.id)
        )
        submitRunResult(summary)

        return copy(
            phase = GamePhase.Victory,
            rewardCoins = banked,
            rewardSaved = true,
            runSummary = summary,
            droppedItem = dropped,
            message = if (dropped != null) {
                "Battle won! $banked coins - and ${dropped.name} dropped."
            } else {
                "Battle won! You claim all the loot - $banked coins."
            }
        )
    }

    /** Cala druzyna padla - koniec biegu, rozliczamy czesc sakwy. */
    private fun BattleUiState.finishDefeat(): BattleUiState {
        runSession.savePartyHp(playerHp(), playerMaxHp())
        runSession.lastBattleWon = false
        runSession.addLoot(calculateDefeatCoins(this))
        val bagTotal = runSession.lootGold
        val banked = runSession.settleLoot(DEFEAT_LOOT_FRACTION)

        val summary = runTally.toRunStats(
            won = false,
            endless = request.isEndless,
            gold = banked,
            bossDefeated = false
        )
        submitRunResult(summary)

        return copy(
            phase = GamePhase.Defeat,
            rewardCoins = banked,
            rewardSaved = true,
            runSummary = summary,
            message = "The party fell on wave $wave. You save $banked of $bagTotal coins from your bag (25%)."
        )
    }

    /**
     * Sklada mape postaci dla jednej fali - wrogowie startuja poza ekranem ([CharacterUiModel.approach]
     * = 1f) i sa po kolei wpuszczani na pozycje (patrz [staggerEnemyEntrance]).
     *
     * [autoStagger] = false pomija automatyczne wpuszczanie wrogow - uzywane TYLKO przy starcie
     * walki ([createInitialState]/[loadSavedPlayer], dopoki trwa [GamePhase.Intro]), bo tam
     * kolejnoscia i tempem wejscia steruje calosciowo [playBattleIntro] (najpierw bohaterowie,
     * potem wrogowie, potem quiz) - domyslne `true` zachowuje dawne zachowanie dla kazdej kolejnej
     * fali ([spawnNextWave]), gdzie wrogowie nadal wchodza natychmiast, bez czekania na nic.
     */
    private fun buildWave(
        waveNumber: Int,
        players: List<CharacterUiModel>,
        autoStagger: Boolean = true
    ): Map<CharacterId, CharacterUiModel> {
        val enemies = buildWaveEnemies(waveNumber).map { it.toUiModel().copy(approach = 1f) }
        if (autoStagger) staggerEnemyEntrance(enemies.map { it.id })
        return (players + enemies).associateBy { it.id }
    }

    /**
     * Wpuszcza wrogow na pole bitwy jeden po drugim - kazdy dostaje swoj moment na wejscie z krawedzi
     * ekranu. Delay zawsze > 0 (indeks liczony od 1) - `delay(0)` w kotlinx.coroutines nie zawiesza
     * wcale, wiec przy pierwszym wrogu kod wykonalby sie synchronicznie w tym samym wywolaniu co
     * [buildWave], czyli jeszcze w trakcie inicjalizacji `_uiState` (przed jego przypisaniem - NPE).
     */
    private fun staggerEnemyEntrance(enemyIds: List<String>) {
        enemyIds.forEachIndexed { index, enemyId ->
            viewModelScope.launch {
                delay(BattleSpeed.scale((index + 1) * ENEMY_STAGGER_MS))
                _uiState.update { state ->
                    val enemy = state.characters[enemyId] ?: return@update state
                    state.copy(characters = state.characters.plus(enemyId to enemy.copy(approach = 0f)))
                }
            }
        }
    }

    /** Loads the saved party + equipment and folds their bonuses into the running battle. */
    private fun loadSavedPlayer() {
        viewModelScope.launch(Dispatchers.IO) {
            val player = playerRepository.getPlayer() ?: return@launch
            // Sklad przycinamy do miejsc OTWARTYCH poziomem bohatera. Zapis moze zawierac wiecej
            // (powstal przed progami albo po zmianie tabeli), a walka czyta druzyne wprost stad -
            // bez tego stary zapis omijalby caly system odblokowywania.
            savedParty = PartySlots.enforce(player.party, player.progress.level)
            savedMonsterLevels = player.monsterLevels
            savedMonsterStars = player.monsterStars
            savedSpellLevels = player.monsterSpellLevels
            heroStats = player.progress.stats
            talentBonuses = TalentRules.bonusesOf(player.talentRanks)
            // Zaklecia bohatera i ich szanse biora sie w calosci z drzewka talentow.
            heroSpellChances = talentBonuses.spellChances
            _uiState.update { current ->
                // Ukryta wchodzi tylko druzyna, ktorej intro jeszcze NIE wpuscilo.
                //
                // Warunkiem bylo kiedys samo `phase == Intro` i to byl blad wyscigu: intro trwa
                // dalej po wejsciu bohaterow (wchodza wrogowie, potem pauza przed quizem), wiec
                // odczyt z repo, ktory trafil w to okno, podmienial WPUSZCZONYCH juz bohaterow na
                // swiezych z approach = 1f - a petla, ktora miala ich odslonic, byla juz za nimi.
                // Efekt: walka z niewidzialna druzyna, po stronie gracza pusty ekran.
                val hidden = current.phase == GamePhase.Intro && !introPlayersEntered
                val players = buildPlayerParty().map { it.toUiModel().copy(approach = if (hidden) 1f else 0f) }
                // Wrogowie zostaja DOKLADNIE tacy, jacy sa - razem ze stanem wejscia i obrazeniami.
                // Skladanie fali od nowa gubilo jedno i drugie, a nic nie wnosilo: pula wrogow
                // zalezy tylko od ziarna i numeru fali ([buildWaveEnemies]), nie od druzyny.
                val enemies = current.characters.filterValues { !it.isPlayer }
                current.copy(characters = players.associateBy { it.id } + enemies)
                    .applyTalentBuffs()
                    .applyEquipmentBonuses(player.migratedHeroEquipment(), player.appearance, player.heroBag())
                    .applyPersistedHp()
            }
        }
    }

    /**
     * Rozlicza cale zejscie JEDNYM zapisem: zloto, surowce, XP z awansami i liczniki statystyk
     * (patrz [PlayerRepository.applyRunResult]). Wczesniej lecialo tylko `addMoney` - reszta wyniku
     * po prostu przepadala, wiec ani poziom, ani statystyki nie mialy skad rosnac.
     *
     * Liczba zdobytych poziomow wraca do stanu, zeby ekran koncowy mogl pokazac "AWANS".
     */
    /**
     * Pytanie ze slowek trafia z powrotem na fiszke: seria, liczniki i awans do archiwum
     * "opanowane" (patrz [com.lernhero.domain.reading.VocabularyDeck]). Pytania z wbudowanego
     * banku nie maja id fiszki i po prostu tedy nie przechodza.
     *
     * Zapis leci osobno od [submitRunResult], bo dotyczy JEDNEJ odpowiedzi, a nie calego runu -
     * gracz musi zobaczyc przesuniete liczniki nawet wtedy, gdy walke przerwie w polowie.
     */
    private fun recordSavedWordAnswer(quizEntryId: String, correct: Boolean) {
        val wordId = WordQuizBank.savedWordIdOf(quizEntryId) ?: return
        val now = Clock.System.now().toEpochMilliseconds()
        viewModelScope.launch(Dispatchers.IO) {
            playerRepository.recordWordAnswer(wordId, correct, now)
        }
    }

    private fun submitRunResult(summary: RunStats) {
        viewModelScope.launch(Dispatchers.IO) {
            val before = playerRepository.getPlayer()?.progress?.level ?: 1
            val after = playerRepository.applyRunResult(summary)?.progress?.level ?: before
            val gained = (after - before).coerceAtLeast(0)
            if (gained > 0) _uiState.update { it.copy(levelsGained = gained) }

            // Zaliczenie planszy kampanii idzie TYM SAMYM zapisem co reszta wyniku, od razu po
            // wygranej. Wczesniej robil to ekran druzyny dopiero po powrocie z walki i scigal sie
            // z jego wlasnym refresh() - przez co kazda plansze trzeba bylo przechodzic 2x, zanim
            // "kliknela".
            val storyStage = runSession.activeStoryStage
            if (summary.won && !request.isEndless && storyStage != null) {
                playerRepository.clearStoryStage(runSession.difficulty.name, storyStage)
            }
        }
    }

    /**
     * Start walki: WSZYSCY (bohaterowie i wrogowie) startuja ukryci poza ekranem, quiz jeszcze nie
     * istnieje. [playBattleIntro] (odpalane z `init`) po kolei ich wpuszcza i dopiero na koncu
     * tworzy pierwsze pytanie - patrz tam po pelna sekwencje.
     */
    /**
     * Tlo krainy dla biezacej walki. W endless/boss-test nie ma zadnej krainy - wtedy zostaje
     * domyslne generyczne tlo z [BattleUiState.battleBackground].
     */
    private fun resolveBattleBackground(): org.jetbrains.compose.resources.DrawableResource {
        val stage = runSession.activeStoryStage ?: return Resources.Image.battlegroundFirst
        val worldId = StoryWorldCatalog.worldForGlobalStage(stage)?.id ?: return Resources.Image.battlegroundFirst
        return Resources.Image.worldBackground(worldId) ?: Resources.Image.battlegroundFirst
    }

    private fun createInitialState(): BattleUiState {
        val players = buildPlayerParty().map { it.toUiModel().copy(approach = 1f) }
        val characters = buildWave(1, players, autoStagger = false)
        return BattleUiState(
            phase = GamePhase.Intro,
            wave = 1,
            characters = characters,
            quiz = null,
            message = null,
            battleBackground = resolveBattleBackground()
        )
    }

    /**
     * Efektowne wejscie na pole bitwy: najpierw po kolei wchodza nasi bohaterowie (od hero, potem
     * kompani), chwila pauzy, potem po kolei wrogowie, kolejna chwila, i dopiero wtedy pojawia sie
     * pierwszy quiz - [GamePhase] wraca do [GamePhase.Battle], co odblokowuje caly UI (patrz guardy
     * `phase != GamePhase.Battle` w calym pliku) i zwija pole bitwy z powrotem, robiac miejsce na
     * dol ekranu (BattleScreen.kt czyta phase dokladnie tak samo jak przy Defeat/Victory).
     *
     * ID-ki graczy/wrogow czytane sa NA BIEZACO z `_uiState.value` w momencie kazdego kroku (a nie
     * raz, na starcie) - dzieki temu jesli [loadSavedPlayer] (async, IO) zdazy podmienic
     * placeholder-owa druzyne na prawdziwa zanim animacja doturze do graczy, wpuszczana jest juz
     * kompletna, prawdziwa druzyna, a nie pusty placeholder sprzed wczytania.
     */
    private fun playBattleIntro() {
        viewModelScope.launch {
            delay(INTRO_START_DELAY_MS)

            val playerIds = _uiState.value.characters.values
                .filter { it.isPlayer }
                .sortedBy { it.id }
                .map { it.id }
            playerIds.forEachIndexed { index, id ->
                if (index > 0) delay(PLAYER_ENTRANCE_STAGGER_MS)
                revealCharacter(id)
            }
            introPlayersEntered = true

            delay(PRE_ENEMY_ENTRANCE_PAUSE_MS)

            val enemyIds = _uiState.value.characters.values
                .filter { !it.isPlayer }
                .sortedBy { it.id }
                .map { it.id }
            enemyIds.forEachIndexed { index, id ->
                if (index > 0) delay(ENEMY_STAGGER_MS)
                revealCharacter(id)
            }

            delay(PRE_QUIZ_REVEAL_PAUSE_MS)
            if (isBossDuel) {
                // Walka z bossem nie ma zwyklego quizu tury - od razu rusza jego pierwszy atak
                // specjalny (patrz [beginBossTurn]).
                _uiState.update { state ->
                    state.copy(phase = GamePhase.Battle, turnPhase = TurnPhase.Resolving, message = "The boss enters the fight...")
                }
                beginBossTurn()
            } else if (categoryPickEnabled) {
                // Kampania: pierwsza tura tez zaczyna sie od wyboru tematu, a nie od gotowego pytania.
                _uiState.update { state ->
                    state.copy(
                        phase = GamePhase.Battle,
                        turnPhase = TurnPhase.CategoryPick,
                        categoryOffer = CategoryOffer.roll(quizRandom, offerableCategories),
                        quiz = null,
                        message = "Wave incoming..."
                    )
                }
            } else {
                _uiState.update { state ->
                    state.copy(phase = GamePhase.Battle, quiz = drawNextQuestion(state.wave), message = "Wave incoming...")
                }
            }
        }
    }

    /** Wpuszcza jedna postac (gracza albo wroga) na jej miejsce - patrz [playBattleIntro]/[staggerEnemyEntrance]. */
    private fun revealCharacter(characterId: String) {
        _uiState.update { state ->
            val character = state.characters[characterId] ?: return@update state
            state.copy(characters = state.characters.plus(characterId to character.copy(approach = 0f)))
        }
    }

    /** Monety za oczyszczona fale: baza + odrobina za trudnosc fali i zebrane punkty z quizu. */
    private fun calculateWaveCoins(state: BattleUiState): Int {
        val kindBonus = when (request.kind) {
            BattleKind.Normal -> 0
            BattleKind.Elite -> 20
            BattleKind.Boss -> 60
        }
        val raw = 18 + kindBonus + state.wave * 6 + state.totalScore / 12
        return (raw * runSession.difficulty.coinMultiplier).roundToInt().coerceAtLeast(8)
    }

    /** Pocieszenie za koniec biegu - kilka monet za dotarcie do fali i wiedze z quizu. */
    private fun calculateDefeatCoins(state: BattleUiState): Int =
        ((8 + (state.wave - 1) * 6 + state.totalScore / 20) *
            runSession.difficulty.coinMultiplier).roundToInt().coerceAtLeast(5)

    /**
     * Wpuszcza staki z drzewka talentow do [BattleUiState.endlessBuffs].
     *
     * To jest caly "silnik" talentow bojowych: mapa bonusow byla juz czytana bezwarunkowo przez
     * cala walke (endless tylko ja ZAPELNIAL kartami), wiec talenty po prostu ustawiaja jej stan
     * poczatkowy. Karty z biegu doliczaja sie normalnie na wierzch - a sufit [EndlessBuff.maxStacks]
     * pilnuje, zeby suma nie przebila granicy, ktora silnik uznaje za maksimum.
     */
    private fun BattleUiState.applyTalentBuffs(): BattleUiState {
        if (talentBonuses.buffStacks.isEmpty()) return this
        val merged = endlessBuffs.toMutableMap()
        talentBonuses.buffStacks.forEach { (buff, stacks) ->
            merged[buff] = ((merged[buff] ?: 0) + stacks).coerceAtMost(buff.maxStacks)
        }
        return copy(endlessBuffs = merged)
    }

    /**
     * Ekwipunek gracza wchodzi do walki jako proste bonusy do ataku/HP - efekty specjalne
     * (rozciecie na dwoch wrogow, odbicie, zaklecia z paska zdolnosci) czekaja na kolejna iteracje,
     * bo silnik czasu rzeczywistego jeszcze ich nie rozumie.
     */
    private fun BattleUiState.applyEquipmentBonuses(
        equipment: PlayerEquipment,
        appearance: HeroAppearance,
        /** Plecak bohatera - sloty trzymaja uid sztuk, wiec bez niego nie wiadomo, co w nich lezy. */
        heroBag: List<ItemInstance>
    ): BattleUiState {
        val loadout = equipment.toLoadout(heroBag)
        val runMods = runSession.modifiers
        // Statystyki z awansow ([HeroStat.Strength]/[HeroStat.Vitality]) dzialaja na CALA druzyne -
        // to bohater prowadzi wyprawe, wiec jego rozwoj podnosi tez kompanow. Ekwipunek i perki
        // z runu zostaja przy samym bohaterze, tak jak dotad.
        val partyHpBonus = heroStats.maxHpBonus + talentBonuses.maxHp
        val partyAttackBonus = heroStats.attackBonus + talentBonuses.attack
        val hpBonus = loadout.stats[StatType.MaxHp].toInt() + runMods.bonusMaxHp + partyHpBonus
        val attackBonus = loadout.stats[StatType.Attack].toInt() + runMods.bonusAttack + partyAttackBonus

        val updatedCharacters = characters.mapValues { (_, character) ->
            if (character.id != HERO_ID) {
                if (!character.isPlayer) {
                    character
                } else {
                    character.copy(
                        maxHp = character.maxHp + partyHpBonus,
                        hp = character.hp + partyHpBonus,
                        attack = character.attack + partyAttackBonus
                    )
                }
            } else {
                character.copy(
                    maxHp = character.maxHp + hpBonus,
                    hp = character.hp + hpBonus,
                    attack = character.attack + attackBonus,
                    heroLook = HeroLook.of(appearance),
                    knightEquipment = KnightEquipment(
                        head = equipment.itemIn(ItemSlot.Head, heroBag) != null,
                        chest = equipment.itemIn(ItemSlot.Chest, heroBag) != null,
                        hands = equipment.itemIn(ItemSlot.Hands, heroBag) != null,
                        legs = equipment.itemIn(ItemSlot.Legs, heroBag) != null,
                        feet = equipment.itemIn(ItemSlot.Feet, heroBag) != null,
                        mainHand = equipment.itemIn(ItemSlot.MainHand, heroBag) != null
                    )
                )
            }
        }
        return copy(characters = updatedCharacters)
    }

    /** Nadpisuje HP graczy wartosciami zapamietanymi po poprzedniej walce w tym runie. */
    private fun BattleUiState.applyPersistedHp(): BattleUiState {
        val persisted = runSession.partyHp
        if (persisted.isEmpty()) return this

        val updated = characters.mapValues { (id, character) ->
            if (!character.isPlayer) return@mapValues character
            val hp = (persisted[id] ?: character.maxHp).coerceIn(0, character.maxHp)
            character.copy(hp = hp, state = if (hp <= 0) CharacterState.DEAD else character.state)
        }
        return copy(characters = updated)
    }

    private fun QuizEntry.toUiQuestion(): QuizQuestion = MultipleChoiceQuestion(
        id = id,
        prompt = prompt,
        flag = flagCode?.let { FlagResources.flagFor(it) },
        answers = answers.shuffled(Random.Default).map { text ->
            QuizAnswer(id = text, text = text, isCorrect = text == correct)
        },
        difficulty = difficulty.label,
        category = category.label,
        reward = difficulty.reward
    )

    /** [OrderingEntry.items] jest zapisane juz w poprawnej kolejnosci - tu tylko tasujemy je do wyswietlenia. */
    private fun OrderingEntry.toUiQuestion(): QuizQuestion {
        val displayOrder = items.indices.shuffled(Random.Default)
        return OrderingQuestion(
            id = id,
            prompt = prompt,
            items = displayOrder.map { originalIndex ->
                OrderingItem(
                    id = "${id}_$originalIndex",
                    text = items[originalIndex],
                    correctPosition = originalIndex
                )
            },
            difficulty = difficulty.label,
            category = category.label,
            reward = difficulty.reward
        )
    }

    /** [MultiSelectEntry.correct] i `.wrong` sa w banku rozdzielone - tu je laczymy i tasujemy. */
    private fun MultiSelectEntry.toUiQuestion(): QuizQuestion = MultiSelectQuestion(
        id = id,
        prompt = prompt,
        options = (correct.map { it to true } + wrong.map { it to false })
            .shuffled(Random.Default)
            .map { (text, isCorrect) ->
                MultiSelectOption(id = text, text = text, isCorrect = isCorrect)
            },
        difficulty = difficulty.label,
        category = category.label,
        reward = difficulty.reward
    )

    /** Przelacza jedna opcje w pytaniu wielokrotnego wyboru - rozstrzygniecie dopiero po ZATWIERDZ. */
    fun toggleMultiSelect(optionId: String) {
        _uiState.update { state ->
            val quiz = state.quiz ?: return@update state
            if (quiz.revealed || quiz.question !is MultiSelectQuestion) return@update state
            val next = if (optionId in quiz.multiSelection) {
                quiz.multiSelection - optionId
            } else {
                quiz.multiSelection + optionId
            }
            state.copy(quiz = quiz.copy(multiSelection = next))
        }
    }

    /** Gracz zatwierdzil komplet zaznaczen - stad dalej idzie ta sama sciezka co inne typy pytan. */
    fun confirmMultiSelect() {
        _uiState.update { state ->
            val quiz = state.quiz ?: return@update state
            val question = quiz.question as? MultiSelectQuestion ?: return@update state
            if (state.phase != GamePhase.Battle || quiz.revealed) return@update state

            val revealed = quiz.copy(revealed = true)
            state.resolveQuizOutcome(
                quiz = revealed,
                isCorrect = revealed.answeredCorrectly,
                reward = question.reward
            )
        }
    }

    private fun buildPlayerParty(): List<CharacterTemplate> {
        val hero = CharacterTemplate(
            id = HERO_ID,
            name = "Knight",
            isPlayer = true,
            avatar = "knight",
            hp = HERO_BASE_HP,
            maxHp = HERO_BASE_HP,
            attack = HERO_BASE_ATTACK
        )

        val companions = savedParty
            .filterNotNull()
            .mapIndexedNotNull { index, monsterId ->
                MonsterCatalog.byId(monsterId)?.let { card ->
                    // Karty kompanow sa opisane w "malych" liczbach z katalogu (55-100 HP) - w walce
                    // musza byc przeskalowane do tej samej ligi co bohater, inaczej padaja w pierwszej
                    // salwie i kupowanie ich nie ma sensu. Skalujemy tu, a nie w katalogu, zeby
                    // proporcje miedzy potworami (tank / glass cannon) zostaly nietkniete.
                    // Poziom kompana (kupiony za zloto) mnozy sie z przeskalowaniem karty - to
                    // jest jedyne miejsce, w ktorym zloto wlozone w kolekcje zamienia sie w sile.
                    val stars = MonsterStars.clampStars(savedMonsterStars[card.id] ?: MonsterStars.MIN_STARS)
                    // Poziom przycinamy do sufitu GWIAZDKI, a nie do absolutnego maksimum - inaczej
                    // zapisany wyzszy poziom dzialalby mimo braku awansu.
                    val level = MonsterLevels.clampToStars(
                        savedMonsterLevels[card.id] ?: MonsterLevels.MIN_LEVEL,
                        stars
                    )
                    val hp = (MonsterStars.hpAt(card, stars, level) * COMPANION_HP_SCALE).roundToInt()
                    // Zapamietujemy, ktora KARTA stoi w tym slocie - z samego id postaci nie da sie
                    // wrocic do jej zaklec, a to one decyduja, co kompan robi w walce.
                    companionCardOf["PLAYER_COMPANION_${index}_${card.id}"] = card.id
                    CharacterTemplate(
                        id = "PLAYER_COMPANION_${index}_${card.id}",
                        name = if (level > MonsterLevels.MIN_LEVEL) "${card.name} (${level})" else card.name,
                        isPlayer = true,
                        avatar = card.spriteKey,
                        hp = hp,
                        maxHp = hp,
                        attack = (MonsterStars.attackAt(card, stars, level) * COMPANION_ATTACK_SCALE).roundToInt()
                    )
                }
            }

        return listOf(hero) + companions
    }

    /**
     * Wrogowie danej fali. Elita/boss to zawsze JEDEN, duzo wiekszy i mocniejszy wrog zamiast fali
     * zwyklych potworow - "bez fal, tylko on" (patrz [buildEliteOrBoss]).
     */
    /**
     * Wrogowie danej fali. Trzy niezalezne dzwignie trudnosci mnoza sie przez siebie:
     * [RunDifficulty] (tier kampanii wybrany na mapie),
     * [levelHpMultiplier]/[levelAttackMultiplier] (ciagly wzrost z numerem poziomu story-mode, null
     * w endless - tam za skalowanie odpowiada wylacznie rosnaca [wave]) i [SPECIES_STAT_MULTIPLIER]
     * (kazdy gatunek to inny archetyp: tank/zwykly/glass cannon - te same proporcje co ich karty
     * kompanow w [com.lernhero.domain.monster.MonsterCatalog], zeby wrog "orka wojownika" grał sie
     * jak spuchniety kompan-orka, a nie identyczny szkielet w innej skorce).
     */
    private fun buildWaveEnemies(wave: Int): List<CharacterTemplate> {
        val difficulty = runSession.difficulty
        val level = runSession.activeStoryStage
        val random = Random(request.seed * 17 + wave)

        if (request.kind != BattleKind.Normal) {
            return listOf(buildEliteOrBoss(wave, random, difficulty, level))
        }

        // Story mode: wiecej wrogow na fale im wyzszy poziom lochu (tlum, nie tylko twardsze ciosy).
        val levelCountBonus = level?.let { (it - 1) / 3 } ?: 0
        val enemyCount = (2 + random.nextInt(2) + wave / 5 + levelCountBonus).coerceAtMost(6)
        return List(enemyCount) { index ->
            val number = index + 1
            val species = WAVE_ENEMY_SPECIES[random.nextInt(WAVE_ENEMY_SPECIES.size)]
            val archetype = SPECIES_STAT_MULTIPLIER.getValue(species)
            val baseHp = ENEMY_BASE_HP + (wave - 1) * ENEMY_HP_PER_WAVE + index * ENEMY_HP_PER_INDEX
            val baseAttack = ENEMY_BASE_ATTACK + (wave - 1) * ENEMY_ATTACK_PER_WAVE + index * ENEMY_ATTACK_PER_INDEX
            val hp = (baseHp * difficulty.enemyHpMultiplier * levelHpMultiplier(level) * archetype.first).roundToInt()
            val attack = (baseAttack * difficulty.enemyAttackMultiplier * levelAttackMultiplier(level) * archetype.second).roundToInt()
            CharacterTemplate(
                id = "ENEMY_${wave}_$number",
                name = "Monster $number",
                isPlayer = false,
                avatar = species,
                hp = hp,
                maxHp = hp,
                attack = attack
            )
        }
    }

    private fun buildEliteOrBoss(
        wave: Int,
        random: Random,
        difficulty: RunDifficulty,
        level: Int?
    ): CharacterTemplate {
        val isBoss = request.kind == BattleKind.Boss
        val hpMultiplier = if (isBoss) BOSS_HP_MULTIPLIER else ELITE_HP_MULTIPLIER
        val attackMultiplier = if (isBoss) BOSS_ATTACK_MULTIPLIER else ELITE_ATTACK_MULTIPLIER
        // Boss ma wlasny, stale wyrozniajacy sie avatar - nie nalezy do zadnego archetypu gatunku
        // (fallback 1f/1f), bo jego sila juz w calosci siedzi w BOSS_*_MULTIPLIER powyzej.
        val species = if (isBoss) EnemySpecies.BOSS_SPECIES else WAVE_ENEMY_SPECIES[random.nextInt(WAVE_ENEMY_SPECIES.size)]
        val archetype = SPECIES_STAT_MULTIPLIER[species] ?: (1f to 1f)
        val baseHp = ENEMY_BASE_HP + (wave - 1) * ENEMY_HP_PER_WAVE
        val baseAttack = ENEMY_BASE_ATTACK + (wave - 1) * ENEMY_ATTACK_PER_WAVE
        val hp = (baseHp * difficulty.enemyHpMultiplier * levelHpMultiplier(level) * archetype.first * hpMultiplier * request.enemyHpMultiplier).roundToInt()
        val attack = (baseAttack * difficulty.enemyAttackMultiplier * levelAttackMultiplier(level) * archetype.second * attackMultiplier).roundToInt()

        return CharacterTemplate(
            id = if (isBoss) "BOSS_$wave" else "ELITE_$wave",
            name = if (isBoss) "Dungeon Boss" else "Elite Hunter",
            isPlayer = false,
            avatar = species,
            hp = hp,
            maxHp = hp,
            attack = attack,
            isElite = true
        )
    }

    private fun CharacterTemplate.toUiModel(): CharacterUiModel =
        CharacterUiModel(
            id = id,
            name = name,
            hp = hp,
            maxHp = maxHp,
            attack = attack,
            isPlayer = isPlayer,
            avatarKey = avatar,
            // Zywiol bierze sie z KARTY o tym samym kluczu - gatunki wrogow ([EnemySpecies.all])
            // i id kompanow to ten sam zbior, wiec nie trzeba przepychac typu przez szablony.
            element = MonsterCatalog.byId(avatar)?.element,
            knightEquipment = if (id == HERO_ID) KnightEquipment.Empty else null,
            battleScale = when {
                // Boss ma byc kolosem - stoi znacznie wiecej niz elita, wystajac sylwetka poza pole.
                avatar == EnemySpecies.BOSS_SPECIES -> BOSS_BATTLE_SCALE
                isElite -> avatarBattleScale(avatar) * ELITE_SCALE_MULTIPLIER
                else -> avatarBattleScale(avatar)
            }
        )

    private fun avatarBattleScale(avatar: String): Float = when (avatar) {
        "knight" -> 0.85f
        EnemySpecies.BOSS_SPECIES -> 1.5f
        else -> 1.0f
    }
}

/**
 * Jeden pocisk zaplanowany przy budowaniu kroku salwy, jeszcze nie odegrany - konsumowany po
 * animacji ataku w [BattleViewModel.onAttackFinished]. Trzyma tylko OBRAZENIA, a nie docelowe HP:
 * ile celowi zostanie, liczy sie dopiero w chwili trafienia ([BattleViewModel.applyHit]), zeby
 * kolejne pociski tej samej serii nakladaly sie na siebie zamiast nadpisywac wynik poprzedniego.
 */
private data class PendingHit(
    val targetId: String,
    val amount: Int,
    val isCrit: Boolean,
    val kind: HitKind,
    /** Zaklecie leczace - [amount] idzie wtedy na plus, nie na minus (patrz applyHit). */
    val heals: Boolean = false,
    /** Ile tur cel traci kolejke po tym trafieniu (zamrozenie). */
    val freezeTurns: Int = 0
)

/**
 * Wynik zejscia zbierany na biezaco - jedyne miejsce, w ktorym walka liczy, ile z niej wyszlo.
 * Zwykla klasa mutowalna, a nie stan Compose: nic tego nie rysuje w trakcie, a na koniec zamienia
 * sie w niemutowalny [RunStats], ktory idzie i na ekran koncowy, i do repozytorium.
 */
private class RunTally {
    private var questionsAnswered = 0
    private var questionsCorrect = 0
    private var bestStreak = 0
    private var xp = 0
    private var iron = 0
    private var essence = 0
    private var wood = 0
    private var stone = 0
    private var wavesCleared = 0
    private var bestWave = 1
    private val perCategory = mutableMapOf<String, CategoryStat>()

    fun recordAnswer(
        correct: Boolean,
        category: QuizCategory?,
        difficulty: QuizDifficulty,
        consecutiveCorrect: Int
    ) {
        questionsAnswered += 1
        if (correct) questionsCorrect += 1
        bestStreak = maxOf(bestStreak, consecutiveCorrect)
        xp += RunLoot.xpForAnswer(correct, difficulty)

        // Pytania bez rozpoznanej kategorii (fallback z calego banku) licza sie do sumy, ale nie
        // zasmiecaja rozbicia per kategoria bledna etykieta.
        val key = category?.name ?: return
        val current = perCategory[key] ?: CategoryStat()
        perCategory[key] = current.copy(
            answered = current.answered + 1,
            correct = current.correct + if (correct) 1 else 0
        )
    }

    fun recordWaveCleared(wave: Int, kind: BattleKind, difficulty: RunDifficulty) {
        wavesCleared += 1
        bestWave = maxOf(bestWave, wave)
        xp += RunLoot.xpForWave(wave, kind, difficulty)
        iron += RunLoot.ironForWave(wave, kind)
        essence += RunLoot.essenceForWave(wave, kind)
    }

    fun recordBossBonus(kind: BattleKind, dungeonLevel: Int) {
        val bonus = RunLoot.bossBonus(kind, dungeonLevel)
        iron += bonus.iron
        essence += bonus.essence
        wood += bonus.wood
        stone += bonus.stone
        xp += bonus.xp
    }

    fun toRunStats(
        won: Boolean,
        endless: Boolean,
        gold: Int,
        bossDefeated: Boolean,
        droppedItemIds: List<String> = emptyList()
    ): RunStats = RunStats(
        won = won,
        endless = endless,
        wavesCleared = wavesCleared,
        // Rekord fali dotyczy tylko endless - w story mode fale sa z gory ograniczone zamowieniem
        // walki, wiec porownywanie ich z endless nie mialoby sensu.
        bestEndlessWave = if (endless) bestWave else 0,
        bossDefeated = bossDefeated,
        dungeonCleared = false,
        questionsAnswered = questionsAnswered,
        questionsCorrect = questionsCorrect,
        bestAnswerStreak = bestStreak,
        perCategory = perCategory.toMap(),
        gold = gold,
        xp = xp,
        iron = iron,
        essence = essence,
        wood = wood,
        stone = stone,
        droppedItemIds = droppedItemIds
    )
}

/**
 * Jeden krok kolejki tej tury - jedna konkretna postac odgrywa JEDNA animacje ataku. Ile pociskow
 * z niej wyleci, decyduje dopiero [BattleViewModel.buildHits] w chwili ciosu. [round] to numer rundy
 * wymiany ciosow w turze - po nim poznajemy poczatek nowej rundy (odnowienie gardy z Egidy).
 */
private data class VolleyStep(val attackerId: String, val isPlayer: Boolean, val round: Int = 0)

/** Id bohatera - jedyna postac, ktora nosi ekwipunek gracza. */
const val HERO_ID = "PLAYER_KNIGHT"

/** Ile rund (salwa gracza + salwa wrogow) rozgrywa sie po kazdej odpowiedzi. */
private const val ROUNDS_PER_TURN = 3

/**
 * Dlugosc taktu [TurnPhase.Impact] - tyle pole bitwy stoi, zanim ruszy salwa, zeby gracz zdazyl
 * przeczytac rozliczenie swojej odpowiedzi (TurnImpactOverlay animuje sie w tym samym oknie).
 * Skalowane przez [BattleSpeed], wiec na 4x/8x nie jest to zadnym hamulcem.
 */
private const val IMPACT_HOLD_MILLIS = 2400L

/**
 * Bohater - jedyna postac, ktora gracz ma OD RAZU, wiec caly early game musi dac sie przejsc nim
 * samym. Wczesniej 130 HP / 44 ataku, co przy ~144 obrazeniach przyjmowanych na ture oznaczalo
 * smierc w pierwszej turze u kazdego nowego gracza (patrz [ENEMY_BASE_HP]).
 */
/** Baza bohatera mieszka w domenie ([HeroBaseStats]) - te same liczby czyta karta postaci. */
private val HERO_BASE_HP get() = HeroBaseStats.HP
private val HERO_BASE_ATTACK get() = HeroBaseStats.ATTACK

/** Przeskalowanie kart kompanow z [MonsterCatalog] do ligi bohatera - patrz buildPlayerParty. */
private const val COMPANION_HP_SCALE = 3.0f
private const val COMPANION_ATTACK_SCALE = 2.0f

private val CRIT_CHANCE get() = HeroBaseStats.CRIT_CHANCE
private val CRIT_DAMAGE_MULTIPLIER get() = HeroBaseStats.CRIT_MULTIPLIER

/**
 * Zla odpowiedz oslabia salwe, ale nie zatrzymuje postepu - gracz ma miec "jestem dobry, wiec ide
 * szybciej", a nie "nie trafilem, wiec nie gram". 0.3f oznaczalo praktycznie stracona ture.
 */
internal const val WRONG_ANSWER_MULTIPLIER = 0.55f

/** Zla odpowiedz nie tylko oslabia atak druzyny (patrz wyzej) - wrogowie tez bija mocniej ta ture. */
internal const val WRONG_ANSWER_ENEMY_BONUS = 1.15f

/** Testowo pol na pol - jak sie sprawdzi, mozna dostroic w dol/gore albo powiazac z fala/kategoria. */
private const val ORDERING_QUESTION_CHANCE = 0.5f

/**
 * Bazowe HP/atak zwyklego wroga na fali 1, index 0 (przed mnoznikami trudnosci/poziomu/archetypu).
 *
 * Poprzednie liczby (190hp/16atk) byly policzone przy zalozeniu, ze druzyna to bohater (~44 ataku)
 * PLUS dwoch kompanow - czyli ~80 surowego ataku na runde. Tyle ze kompanow trzeba najpierw kupic w
 * sklepie, wiec KAZDY nowy gracz wchodzil do walki sam, z polowa zakladanego ataku, i jednoczesnie
 * z 130 HP przeciwko ~144 obrazeniom na ture (3 wrogow x [ROUNDS_PER_TURN] x 16) - ginal w pierwszej
 * turze niezaleznie od tego, jak odpowiedzial. Stad "nie da sie grac".
 *
 * Nowe wartosci sa liczone dla SAMEGO bohatera jako punktu odniesienia (kompani to bonus, nie
 * wymog): fala 1 to ~2-3 wrogow po ~120-160 HP, czyli ~1.5-2 tury dobrych odpowiedzi, przy ~80-90
 * obrazeniach przyjmowanych na ture - bohater wytrzymuje kilka tur, a zla odpowiedz boli, ale nie
 * konczy gry. Skalowanie na fale/poziom zostaje, tylko startuje z duzo nizszego pulapu.
 */
/*
 * Wrogowie byli o rzad wielkosci za slabi na starcie kampanii: nowa postac traci ~6% HP na plansze
 * przy dobrych odpowiedziach i ~24% przy zlych - czyli zle granie nie mialo zadnych konsekwencji.
 * Zrodlem byla nie tyle sila ciosu, co HP: wrogowie ginęli w pierwszej rundzie tury (bohater bije
 * ~106 za cios, wrog mial ~150 HP), wiec w ogole nie zdazali oddac.
 *
 * Liczby ponizej sa dobrane symulacja calej tury (3 rundy x cala druzyna, focus fire graczy,
 * losowe cele wrogow, krytyki, mnoznik quizu) tak, zeby dla NOWEJ postaci na planszy 1-3:
 *   - dobre odpowiedzi: ~22% traconego HP, zero zgonow (wygrywasz, ale to czujesz),
 *   - zle odpowiedzi:   ~63% traconego HP i realne ryzyko smierci.
 *
 * Krzywa (LEVEL_HP_STEP/LEVEL_ATK_STEP) zostaje nietknieta - zmienia sie sam poziom bazowy, wiec
 * proporcje miedzy planszami sa te same co dotad, tylko caly wykres jest podniesiony ~1.5x.
 */
private const val ENEMY_BASE_HP = 180
private const val ENEMY_HP_PER_WAVE = 40
private const val ENEMY_HP_PER_INDEX = 25
private const val ENEMY_BASE_ATTACK = 14
private const val ENEMY_ATTACK_PER_WAVE = 3
private const val ENEMY_ATTACK_PER_INDEX = 2

/** Elita/boss to jeden, duzo wiekszy wrog - mnozniki HP/ataku wzgledem zwyklego potwora na tej fali. */
private const val ELITE_HP_MULTIPLIER = 3.2f
private const val ELITE_ATTACK_MULTIPLIER = 1.6f
private const val BOSS_HP_MULTIPLIER = 6f
private const val BOSS_ATTACK_MULTIPLIER = 2.1f

/** Dodatkowe przeskalowanie sylwetki elity ponad zwykle [BattleViewModel.avatarBattleScale]. */
private const val ELITE_SCALE_MULTIPLIER = 2.2f

/**
 * Skala sylwetki bossa na polu bitwy. Dawniej mnoznik bossa (3.3) i tak byl przycinany przez
 * constrainty karty formacji do ~1x - teraz sprite rysowany jest [requiredSize] (patrz
 * [com.lernhero.game.character.MonsterCharacter] param `unbounded`), wiec ta liczba dziala wprost:
 * ~4x wiekszy niz zwykly potwor, sylwetka wychodzi poza krawedz pola.
 */
private const val BOSS_BATTLE_SCALE = 3f

/** Gatunki losowane na zwyklych wrogow fali oraz elite (boss ma zawsze [MonsterCatalog] wlasny, staly avatar). */
/** Gatunki i archetypy wroga - patrz [EnemySpecies], dzielone z podgladem plansz kampanii. */
private val WAVE_ENEMY_SPECIES get() = EnemySpecies.all
private val SPECIES_STAT_MULTIPLIER get() = EnemySpecies.statMultiplier

/**
 * Ciagly wzrost sily wrogow z globalnym numerem planszy kampanii - poza dyskretnymi skokami
 * [RunDifficulty] (4 tiery, za malo na dziesiatki plansz). Null (endless albo szybka walka
 * testowa bez kampanii) = brak dodatkowego mnoznika, tam skaluje sie tylko [wave].
 */
private fun levelHpMultiplier(level: Int?): Float = level?.let { 1f + (it - 1) * LEVEL_HP_STEP } ?: 1f
private fun levelAttackMultiplier(level: Int?): Float = level?.let { 1f + (it - 1) * LEVEL_ATK_STEP } ?: 1f

private const val LEVEL_HP_STEP = 0.12f
private const val LEVEL_ATK_STEP = 0.09f

/** Co ile oczyszczonych fal w endless mode gracz dostaje do wyboru karty ulepszen ([EndlessBuff.offer]). */
private const val ENDLESS_BUFF_INTERVAL = 3

private const val MIGHT_ATTACK_BONUS_PER_STACK = 0.08f
private const val PRECISION_CRIT_BONUS_PER_STACK = 0.08f
private const val FORTRESS_REDUCTION_PER_STACK = 0.07f
private const val MAX_FORTRESS_REDUCTION = 0.60f
private const val VAMPIRISM_HEAL_PER_STACK = 0.05f
private const val VITALITY_MAX_HP_PER_STACK = 0.10f
private const val REGENERATION_PER_STACK = 0.07f

/** Dodatkowe cele z [EndlessBuff.Cleave] obrywaja slabiej niz cel glowny. */
private const val CLEAVE_DAMAGE_FACTOR = 0.6f

private const val THORNS_REFLECT_PER_STACK = 0.25f
private const val RIPOSTE_DAMAGE_FACTOR = 1.5f
private const val BLESSING_HEAL_BONUS_PER_STACK = 0.30f
private const val BRUTALITY_CRIT_BONUS_PER_STACK = 0.40f
private const val OVERLOAD_ULTI_BONUS_PER_STACK = 0.60f
private const val SECOND_WIND_HP_FRACTION = 0.25f
private const val AVATAR_BONUS_PER_STACK = 0.20f

/** [EndlessBuff.Executioner]: prog "rannego" celu i premia do obrazen w niego. */
private const val EXECUTIONER_HP_THRESHOLD = 0.35f
private const val EXECUTIONER_BONUS_PER_STACK = 0.35f

/** [EndlessBuff.Momentum]: ile daje jedna dobra odpowiedz z rzedu i ile maksymalnie na stak. */
private const val MOMENTUM_BONUS_PER_ANSWER = 0.06f
private const val MAX_MOMENTUM_BONUS = 0.30f

/** Odstep miedzy kolejnymi pociskami TEGO SAMEGO ciosu - seria, nie jednoczesny wystrzal. */
private const val PROJECTILE_STAGGER_MILLIS = 170L

/** Twardy sufit na dodatkowe ciosy z endless bonusow - inaczej wysoko ustakowany run rozciagalby ture w nieskonczonosc. */
/**
 * Szanse na kazde specjalne zdarzenie przy pojedynczym ataku elity/bossa - razem 30%
 * (10% + 10% + 10%), reszta to zwykly cios. Latwo dostroic albo dodac kolejne zdarzenia pozniej.
 */
private const val BOSS_HEAL_CHANCE = 0.10f
private const val BOSS_POISON_CHANCE = 0.10f
private const val BOSS_STUN_GAMBLE_CHANCE = 0.10f

/** Najgorszy mozliwy skutek (fatalny quiz, performance=0) - skalowany w dol az do zera przy idealnym wyniku. */
private const val BOSS_HEAL_MAX_FRACTION = 0.30f
private const val BOSS_POISON_MAX_FRACTION = 0.10f

/**
 * Fatalny quiz przy zdarzeniu [com.lernhero.game.battle.BossEventKind.Strike]: kazdy bohater
 * przyjmuje tyle razy atak bossa. Idealny quiz sprowadza to do zera.
 */
private const val BOSS_STRIKE_MAX_ATTACK_MULTIPLIER = 1.4f

/**
 * Pula zdarzen w pojedynku z bossem ([BattleViewModel.isBossDuel]) - losowana po rowno przy kazdej
 * jego turze. To jedyne, czym boss dziala na druzyne; zwyklych ciosow nie zadaje.
 */
private val BOSS_DUEL_EVENTS = BossEventKind.entries

/** Ile trwa "przeczytanie" wyniku zaklecia bossa, zanim overlay zniknie i walka rusza dalej. */
/** Dluzej niz dawniej - teraz w tym oknie widac tez zgadywana wartosc na tle poprawnej, nie tylko werdykt. */
/** Ile boss laduje atak, zanim wjedzie quiz - czas na zobaczenie, co szykuje i o jaka stawke gra. */
/** Jak czesto zamiast zwyklego pytania wypada "zaznacz wszystkie prawdziwe". */
private const val MULTI_SELECT_QUESTION_CHANCE = 0.22f

private const val BOSS_EVENT_CHARGE_MS = 2200L

/** Ile trwa werdykt (poprawna wartosc, zgadywana, redukcja) - licznik w tym czasie zjezdza do wyniku. */
private const val BOSS_EVENT_JUDGING_MS = 2400L

/**
 * Ile zaklecia laduje sie od razu, niezaleznie od czasu. Bez tej podlogi natychmiastowa odpowiedz
 * kasowalaby atak do zera i celnosc przestalaby cokolwiek znaczyc.
 */
private const val BOSS_TIME_FLOOR = 0.28f

/** Mnoznik przy IDEALNIE trafionej odpowiedzi - zaklecie niemal gasnie. */
private const val BOSS_ACCURACY_BEST_MULTIPLIER = 0.1f

/** Mnoznik przy calkowitym pudle - odpowiedz realnie WZMACNIA cios (stad "+190%" na werdykcie). */
private const val BOSS_ACCURACY_WORST_MULTIPLIER = 2.9f

/** Najwieksza czesc druzyny, jaka moze zostac ogluszona - reszta zawsze ma sie czym bronic. */
private const val BOSS_STUN_MAX_FRACTION = 0.85f

/** Ponizej tego progu zaklecie ogluszenia liczy sie za przerwane. */
private const val BOSS_STUN_INTERRUPT_THRESHOLD = 0.12f

/** Jak mocno pysk bossa najezdza na ekran podczas zdarzenia specjalnego. */
private const val BOSS_EVENT_CAMERA_ZOOM = 1.75f

/** Ile trwa kulminacja: lot kuli z pyska w cel. */
private const val BOSS_EVENT_PAYOFF_MS = 1300L

/** Ile milisekund "czasu gry" dolicza kazda zakonczona tura - od tego zalezy wygasanie [CYCLE_BUFF_DURATION_MS]. */
private const val TURN_DURATION_ESTIMATE_MS = 4000L

/** Pauza miedzy krokami salwy, zeby bylo widac kolejne uderzenia, a nie wszystko naraz. */
private const val VOLLEY_PAUSE_MILLIS = 250L

/**
 * Ile trwa "opadniecie kurzu" po ciosie konczacym fale (albo walke), zanim obsada zostanie podmieniona
 * na kolejna fale lub pokaze sie ekran konca. Musi byc dluzsze niz animacja smierci potwora
 * (~420ms, patrz MonsterCharacter) - to ono sprawia, ze widac, jak przeciwnik traci ostatnie HP i pada,
 * zamiast zniknac w polowie paska zycia.
 */
private const val DEATH_SETTLE_MILLIS = 780L

/** Zapas po wejsciu ostatniego wroga nowej fali, zanim pojawi sie pytanie. */
private const val WAVE_SETTLE_MILLIS = 250L

/** Sufit czekania na wejscie fali - przy szesciu wrogach [ENEMY_STAGGER_MS] uzbieralby ponad 3.5s. */
private const val MAX_WAVE_ENTRANCE_MILLIS = 1900L

private const val ENEMY_STAGGER_MS = 600L

/** Patrz [BattleViewModel.playBattleIntro] - kolejnosc "bohaterowie -> wrogowie -> quiz" na starcie walki. */
private const val INTRO_START_DELAY_MS = 200L
private const val PLAYER_ENTRANCE_STAGGER_MS = 320L
private const val PRE_ENEMY_ENTRANCE_PAUSE_MS = 350L
private const val PRE_QUIZ_REVEAL_PAUSE_MS = 350L

/** Jaki ulamek lacznego ataku druzyny leci w kazdego wroga przy ulti. */
private const val ULTI_FACTOR = 0.9f

/** Czesc sakwy uratowana przy porazce - reszta zostaje w lochu. */
/** Cala sakwa wraca na konto po wygranej - to jedyna droga do 100% lupu. */
private const val VICTORY_LOOT_FRACTION = 1f

private const val DEFEAT_LOOT_FRACTION = 0.25f

/** Czesc sakwy uratowana przy dobrowolnej kapitulacji - tyle samo, co przy porazce. */
private const val CAPITULATE_LOOT_FRACTION = 0.25f

private data class CharacterTemplate(
    val id: String,
    val name: String,
    val isPlayer: Boolean,
    val avatar: String,
    val hp: Int,
    val maxHp: Int,
    val attack: Int,
    /** Elita/boss - dostaje dodatkowe przeskalowanie sylwetki (patrz [BattleViewModel.ELITE_SCALE_MULTIPLIER]). */
    val isElite: Boolean = false
)

