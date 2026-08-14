package com.lernhero.game.battle

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lernhero.domain.combat.StatType
import com.lernhero.domain.combat.live.AnswerTimer
import com.lernhero.domain.combat.live.CYCLE_BUFF_DURATION_MS
import com.lernhero.domain.combat.live.record
import com.lernhero.domain.combat.playback.HitKind
import com.lernhero.game.character.CharacterState
import com.lernhero.domain.item.ItemSlot
import com.lernhero.domain.run.BattleKind
import com.lernhero.domain.run.BattleRequest
import com.lernhero.domain.run.RunSession
import com.lernhero.domain.item.itemIn
import com.lernhero.domain.item.toLoadout
import com.lernhero.domain.monster.MonsterCatalog
import com.lernhero.domain.player.PlayerEquipment
import com.lernhero.domain.quiz.QuizBank
import com.lernhero.domain.quiz.QuizEntry
import com.lernhero.domain.repository.PlayerRepository
import com.lernhero.game.character.CharacterUiModel
import com.lernhero.game.character.KnightEquipment
import com.lernhero.game.character.MonsterSprites
import com.lernhero.shared.resources.FlagResources
import kotlin.math.roundToInt
import kotlin.random.Random
import kotlin.time.TimeSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

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
    private val runSession: RunSession
) : ViewModel() {

    /** Zamowienie zlozone przez mape. Brak = szybka walka testowa. */
    private val request: BattleRequest =
        runSession.pendingBattle ?: BattleRequest(BattleKind.Normal, waves = 1, seed = Random.nextLong())

    private var savedParty: List<String?> = emptyList()

    private val combatRandom = Random(request.seed)

    /** Cios policzony przy budowaniu salwy, ale jeszcze nie pokazany - czeka na koniec animacji ataku. */
    private var pendingHits: Map<String, PendingHit> = emptyMap()

    private var nextEffectId: Long = 0L

    private val quizRandom = Random(request.seed * 31 + 7)
    private var quizCounter: Int = 0

    /** Znacznik czasu narysowania biezacego pytania - do niego liczy sie pasek czasu na odpowiedz. */
    private var questionShownAt: TimeSource.Monotonic.ValueTimeMark = TimeSource.Monotonic.markNow()

    /** Mnoznik obrazen zablokowany w chwili odpowiedzi - obowiazuje przez cala salwe tej tury. */
    private var currentTurnMultiplier: Float = 1f

    /** Ci, ktorzy jeszcze nie skonczyli animacji ataku w biezacym kroku salwy. */
    private var pendingVolleyAttackers: MutableSet<String> = mutableSetOf()

    /** Pozostale kroki salwy biezacej tury - opróżniane jeden po drugim przez [startNextVolley]. */
    private var volleyPlan: List<VolleyStep> = emptyList()

    private val _uiState = MutableStateFlow(createInitialState())
    val uiState = _uiState.asStateFlow()

    init {
        loadSavedPlayer()
    }

    /**
     * Animacja ataku w miejscu dobiegla konca: cel dostaje obrazenia i krotki blysk trafienia,
     * a atakujacy wraca prosto do bezczynnosci. Gdy caly krok salwy sie skonczyl, ruszamy dalej.
     */
    fun onAttackFinished(characterId: String) {
        _uiState.update { state ->
            val attacker = state.characters[characterId] ?: return@update state
            val hit = pendingHits[characterId]
            pendingHits = pendingHits - characterId

            var characters = state.characters.plus(characterId to attacker.copy(state = CharacterState.IDLE))
            var next = state.copy(characters = characters)

            if (hit != null) {
                val target = characters[hit.targetId]
                if (target != null) {
                    characters = characters.plus(
                        hit.targetId to target.copy(
                            hp = hit.targetHpAfter,
                            state = if (hit.killed) CharacterState.DEAD else CharacterState.HIT
                        )
                    )
                    next = next.copy(characters = characters)
                        .spawnEffect(
                            targetId = hit.targetId,
                            amount = hit.amount,
                            kind = if (hit.isCrit) HitKind.Crit else HitKind.Hit,
                            sourceId = characterId
                        )
                }
            }

            next
        }

        pendingVolleyAttackers -= characterId
        if (pendingVolleyAttackers.isEmpty()) {
            settleVolleyStep()
        }
    }

    /**
     * Caly krok salwy (np. wszyscy gracze naraz) skonczyl animacje - sprawdzamy czy walka sie
     * rozstrzygnela, a jesli nie, ruszamy do kolejnego kroku po krotkiej pauzie na tempo.
     */
    private fun settleVolleyStep() {
        val state = _uiState.value
        if (state.phase != GamePhase.Battle) return

        val playersAlive = state.characters.values.any { it.isPlayer && it.hp > 0 }
        val enemiesAlive = state.characters.values.any { !it.isPlayer && it.hp > 0 }
        // Czekamy, az kazda postac dokonczy swoja animacje (blysk trafienia, padniecie) zanim
        // podmienimy obsade albo pokazemy ekran porazki.
        val animationsSettled = state.characters.values.none {
            it.state != CharacterState.IDLE && it.state != CharacterState.DEAD
        }

        when {
            !playersAlive && animationsSettled -> {
                volleyPlan = emptyList()
                _uiState.update { it.finishDefeat() }
            }
            !enemiesAlive && animationsSettled -> {
                volleyPlan = emptyList()
                _uiState.update { if (state.wave >= request.waves) it.finishVictory() else it.spawnNextWave() }
            }
            else -> viewModelScope.launch {
                delay(BattleSpeed.scale(VOLLEY_PAUSE_MILLIS))
                startNextVolley()
            }
        }
    }

    /**
     * Wystrzeliwuje kolejny pojedynczy cios zaplanowanej kolejki - jedna postac na krok, nigdy
     * cala strona naraz - albo, jesli kolejka sie skonczyla, konczy ture.
     */
    private fun startNextVolley() {
        val step = volleyPlan.firstOrNull()
        if (step == null) {
            endTurn()
            return
        }
        volleyPlan = volleyPlan.drop(1)

        _uiState.update { state ->
            if (state.phase != GamePhase.Battle) return@update state
            val attacker = state.characters[step.attackerId] ?: return@update state
            if (attacker.hp <= 0) return@update state

            val target = if (step.isPlayer) {
                state.characters.values.filter { !it.isPlayer && it.hp > 0 }.minByOrNull { it.id }
            } else {
                state.characters.values.filter { it.isPlayer && it.hp > 0 }.randomOrNull(combatRandom)
            } ?: return@update state

            val isCrit = step.isPlayer && combatRandom.nextFloat() < CRIT_CHANCE
            val multiplier = if (step.isPlayer) {
                currentTurnMultiplier * (if (isCrit) CRIT_DAMAGE_MULTIPLIER else 1f)
            } else {
                1f
            }
            val amount = (attacker.attack * multiplier).roundToInt().coerceAtLeast(1)
            val after = (target.hp - amount).coerceAtLeast(0)

            pendingHits = pendingHits + (
                attacker.id to PendingHit(
                    targetId = target.id,
                    amount = amount,
                    targetHpAfter = after,
                    killed = after <= 0,
                    isCrit = isCrit
                )
            )
            pendingVolleyAttackers = mutableSetOf(attacker.id)

            val characters = state.characters
                .plus(target.id to target.copy(hp = after))
                .plus(attacker.id to attacker.copy(state = CharacterState.ATTACK, currentTargetId = target.id))
            state.copy(characters = characters)
        }

        if (pendingVolleyAttackers.isEmpty()) {
            // Cel juz nie zyl / nikt nie zaatakowal w tym kroku - od razu dalej.
            startNextVolley()
        }
    }

    /** Salwa tej tury sie skonczyla - wraca faza pytania, leci kolejne pytanie. */
    private fun endTurn() {
        _uiState.update { state ->
            if (state.phase != GamePhase.Battle) return@update state
            questionShownAt = TimeSource.Monotonic.markNow()
            state.copy(
                elapsedMs = state.elapsedMs + TURN_DURATION_ESTIMATE_MS,
                turnPhase = TurnPhase.Question,
                quiz = drawNextQuestion(state.wave),
                message = null
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
            if (state.phase != GamePhase.Battle || quiz.revealed) return@update state

            val isCorrect = quiz.question.answers.firstOrNull { it.id == answerId }?.isCorrect == true
            val streakOutcome = state.streak.record(isCorrect)

            // Im wiecej segmentow paska zostalo w chwili odpowiedzi, tym mocniejsza bedzie salwa
            // tej tury - zablokowane teraz, obowiazuje przez cala ture.
            val remainingFraction = 1f -
                (questionShownAt.elapsedNow().inWholeMilliseconds.toFloat() / AnswerTimer.WINDOW_MS).coerceIn(0f, 1f)
            currentTurnMultiplier = if (isCorrect) {
                AnswerTimer.multiplierFor(remainingFraction) * state.activeCycleMultiplier()
            } else {
                WRONG_ANSWER_MULTIPLIER
            }

            var next = state.copy(
                quiz = quiz.copy(selectedAnswerId = answerId, revealed = true),
                streak = streakOutcome.tracker,
                totalScore = state.totalScore + if (isCorrect) quiz.question.reward else 0,
                answeredCount = state.answeredCount + 1,
                message = if (isCorrect) {
                    "Dobra odpowiedz! +${quiz.question.reward} pkt"
                } else {
                    "Zla odpowiedz."
                }
            )

            if (streakOutcome.ultiTriggered) next = next.fireUlti()

            streakOutcome.cycleResult?.let { outcome ->
                next = next.copy(
                    activeCycleBuff = CycleBuffState(outcome = outcome, expiresAtMs = next.elapsedMs + CYCLE_BUFF_DURATION_MS),
                    message = outcome.label
                )
            }

            next
        }
    }

    /** Piec dobrych odpowiedzi z rzedu: cala druzyna wali raz w kazdego wroga widocznego na ekranie. */
    private fun BattleUiState.fireUlti(): BattleUiState {
        val teamAttack = characters.values.filter { it.isPlayer && it.hp > 0 }.sumOf { it.attack }
        if (teamAttack <= 0) return this
        val amount = (teamAttack * activeCycleMultiplier() * ULTI_FACTOR).roundToInt().coerceAtLeast(1)

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
            message = "ULTI! Cala fala oberwala za $amount"
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

        val playerIds = state.characters.values.filter { it.isPlayer }.sortedBy { it.id }.map { it.id }
        val enemyIds = state.characters.values.filter { !it.isPlayer }.sortedBy { it.id }.map { it.id }
        val round = playerIds.map { VolleyStep(it, isPlayer = true) } + enemyIds.map { VolleyStep(it, isPlayer = false) }
        volleyPlan = List(ROUNDS_PER_TURN) { round }.flatten()

        _uiState.update { it.copy(turnPhase = TurnPhase.Resolving, message = null) }
        startNextVolley()
    }

    private fun drawNextQuestion(wave: Int): LiveQuizState {
        val ladder = QuizBank.ladderFor(wave)
        val difficulty = ladder[quizCounter % ladder.size]
        quizCounter += 1
        val entry = QuizBank.draw(listOf(difficulty), category = runSession.category, random = quizRandom).firstOrNull()
            ?: QuizBank.all(quizRandom).random(quizRandom)
        return LiveQuizState(question = entry.toUiQuestion())
    }

    /** Kapitulacja w trakcie walki: druzyna ucieka zywa, ale bez lupu z biezacej fali. */
    fun capitulate() {
        _uiState.update { state ->
            if (state.phase != GamePhase.Battle) return@update state

            runSession.savePartyHp(state.playerHp())
            runSession.lastBattleWon = false
            val bagTotal = runSession.lootGold
            val banked = runSession.settleLoot(CAPITULATE_LOOT_FRACTION)
            saveRunReward(banked)

            state.copy(
                phase = GamePhase.Defeat,
                rewardCoins = banked,
                rewardSaved = true,
                message = "Poddales sie na fali ${state.wave}. Ratujesz $banked z $bagTotal monet w sakwie (25%)."
            )
        }
    }

    /** HP graczy na koniec biezacego stanu - to, co zapisuje sie do [RunSession] miedzy walkami. */
    private fun BattleUiState.playerHp(): Map<String, Int> =
        characters.values.filter { it.isPlayer }.associate { it.id to it.hp.coerceAtLeast(0) }

    /** Fala oczyszczona: lup do sakwy, nastepna fala wchodzi bez przerwy - to jest endless run. */
    private fun BattleUiState.spawnNextWave(): BattleUiState {
        val nextWave = wave + 1
        runSession.savePartyHp(playerHp())
        runSession.addLoot(calculateWaveCoins(this))

        val survivors = characters.values
            .filter { it.isPlayer }
            .map { it.copy(state = if (it.hp <= 0) CharacterState.DEAD else CharacterState.IDLE, currentTargetId = null) }

        questionShownAt = TimeSource.Monotonic.markNow()
        return copy(
            wave = nextWave,
            characters = buildWave(nextWave, survivors),
            turnPhase = TurnPhase.Question,
            quiz = drawNextQuestion(nextWave),
            message = "Fala $nextWave nadciaga..."
        )
    }

    /** Ostatnia fala z [BattleRequest.waves] oczyszczona - walka wygrana, caly lup wraca na konto. */
    private fun BattleUiState.finishVictory(): BattleUiState {
        runSession.savePartyHp(playerHp())
        runSession.lastBattleWon = true
        runSession.addLoot(calculateWaveCoins(this))
        val bagTotal = runSession.lootGold
        val banked = runSession.settleLoot(VICTORY_LOOT_FRACTION)
        saveRunReward(banked)

        return copy(
            phase = GamePhase.Victory,
            rewardCoins = banked,
            rewardSaved = true,
            message = "Walka wygrana! Zgarniasz caly lup - $banked monet."
        )
    }

    /** Cala druzyna padla - koniec biegu, rozliczamy czesc sakwy. */
    private fun BattleUiState.finishDefeat(): BattleUiState {
        runSession.savePartyHp(playerHp())
        runSession.lastBattleWon = false
        runSession.addLoot(calculateDefeatCoins(this))
        val bagTotal = runSession.lootGold
        val banked = runSession.settleLoot(DEFEAT_LOOT_FRACTION)
        saveRunReward(banked)

        return copy(
            phase = GamePhase.Defeat,
            rewardCoins = banked,
            rewardSaved = true,
            message = "Druzyna padla na fali $wave. Ratujesz $banked z $bagTotal monet w sakwie (25%)."
        )
    }

    /**
     * Sklada mape postaci dla jednej fali - wrogowie startuja poza ekranem ([CharacterUiModel.approach]
     * = 1f) i sa po kolei wpuszczani na pozycje (patrz [staggerEnemyEntrance]).
     */
    private fun buildWave(waveNumber: Int, players: List<CharacterUiModel>): Map<CharacterId, CharacterUiModel> {
        val enemies = buildWaveEnemies(waveNumber).map { it.toUiModel().copy(approach = 1f) }
        staggerEnemyEntrance(enemies.map { it.id })
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
                delay((index + 1) * ENEMY_STAGGER_MS)
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
            savedParty = player.party
            _uiState.update { current ->
                val players = buildPlayerParty().map { it.toUiModel() }
                current.copy(characters = buildWave(current.wave, players))
                    .applyEquipmentBonuses(player.equipment)
                    .applyPersistedHp()
            }
        }
    }

    private fun saveRunReward(coins: Int) {
        if (coins <= 0) return
        viewModelScope.launch(Dispatchers.IO) {
            playerRepository.addMoney(coins)
        }
    }

    /** Start walki: pierwsza fala juz nadciaga, quiz kreci sie od razu. */
    private fun createInitialState(): BattleUiState {
        val characters = buildWave(1, buildPlayerParty().map { it.toUiModel() })
        return BattleUiState(
            wave = 1,
            characters = characters,
            quiz = drawNextQuestion(1),
            message = "Fala nadciaga..."
        )
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
     * Ekwipunek gracza wchodzi do walki jako proste bonusy do ataku/HP - efekty specjalne
     * (rozciecie na dwoch wrogow, odbicie, zaklecia z paska zdolnosci) czekaja na kolejna iteracje,
     * bo silnik czasu rzeczywistego jeszcze ich nie rozumie.
     */
    private fun BattleUiState.applyEquipmentBonuses(equipment: PlayerEquipment): BattleUiState {
        val loadout = equipment.toLoadout()
        val runMods = runSession.modifiers
        val hpBonus = loadout.stats[StatType.MaxHp].toInt() + runMods.bonusMaxHp
        val attackBonus = loadout.stats[StatType.Attack].toInt() + runMods.bonusAttack

        val updatedCharacters = characters.mapValues { (_, character) ->
            if (character.id != HERO_ID) {
                character
            } else {
                character.copy(
                    maxHp = character.maxHp + hpBonus,
                    hp = character.hp + hpBonus,
                    attack = character.attack + attackBonus,
                    knightEquipment = KnightEquipment(
                        head = equipment.itemIn(ItemSlot.Head) != null,
                        hands = equipment.itemIn(ItemSlot.Hands) != null,
                        legs = equipment.itemIn(ItemSlot.Legs) != null,
                        feet = equipment.itemIn(ItemSlot.Feet) != null,
                        mainHand = equipment.itemIn(ItemSlot.MainHand) != null
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

    private fun QuizEntry.toUiQuestion(): QuizQuestion = QuizQuestion(
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

    private fun buildPlayerParty(): List<CharacterTemplate> {
        val hero = CharacterTemplate(
            id = HERO_ID,
            name = "Knight",
            isPlayer = true,
            avatar = "knight",
            hp = 130,
            maxHp = 130,
            attack = 44
        )

        val companions = savedParty
            .filterNotNull()
            .mapIndexedNotNull { index, monsterId ->
                MonsterCatalog.byId(monsterId)?.let { card ->
                    CharacterTemplate(
                        id = "PLAYER_COMPANION_${index}_${card.id}",
                        name = card.name,
                        isPlayer = true,
                        avatar = card.spriteKey,
                        hp = card.hp,
                        maxHp = card.hp,
                        attack = card.attack
                    )
                }
            }

        return listOf(hero) + companions
    }

    /** Wrogowie danej fali - zawsze strumien podstawowych bestii, coraz mocniejszych i liczniejszych. */
    private fun buildWaveEnemies(wave: Int): List<CharacterTemplate> {
        val difficulty = runSession.difficulty
        val random = Random(request.seed * 17 + wave)

        fun scaledHp(base: Int) = (base * difficulty.enemyHpMultiplier).roundToInt()
        fun scaledAttack(base: Int) = (base * difficulty.enemyAttackMultiplier).roundToInt()

        val enemyCount = (2 + random.nextInt(2) + wave / 5).coerceAtMost(6)
        return List(enemyCount) { index ->
            val number = index + 1
            CharacterTemplate(
                id = "ENEMY_${wave}_$number",
                name = "Potwor $number",
                isPlayer = false,
                avatar = if (number % 2 == 0) "wolf" else "skeleton",
                hp = scaledHp(70 + (wave - 1) * 14 + index * 10),
                maxHp = scaledHp(70 + (wave - 1) * 14 + index * 10),
                attack = scaledAttack(10 + (wave - 1) * 2 + index * 2)
            )
        }
    }

    private fun CharacterTemplate.toUiModel(): CharacterUiModel =
        CharacterUiModel(
            id = id,
            name = name,
            hp = hp,
            maxHp = maxHp,
            attack = attack,
            isPlayer = isPlayer,
            sprites = mapCharacterSprites(avatar),
            flipSprite = false,
            knightEquipment = if (id == HERO_ID) KnightEquipment.Empty else null,
            battleScale = avatarBattleScale(avatar)
        )

    private fun avatarBattleScale(avatar: String): Float = when (avatar) {
        "knight" -> 0.85f
        "mag" -> 1.9f
        "wolf" -> 1.0f
        "skeleton" -> 1.0f
        else -> 1.0f
    }

    private fun mapCharacterSprites(key: String) = MonsterSprites.spritesFor(key)
}

/** Cios zaplanowany przy budowaniu salwy, jeszcze nie odegrany - konsumowany w [BattleViewModel.onAttackFinished]. */
private data class PendingHit(
    val targetId: String,
    val amount: Int,
    val targetHpAfter: Int,
    val killed: Boolean,
    val isCrit: Boolean
)

/** Jeden krok zaplanowanej salwy tury - cala zywa strona atakuje naraz. */
/** Jeden krok kolejki ataku tej tury - jedna konkretna postac atakuje raz. */
private data class VolleyStep(val attackerId: String, val isPlayer: Boolean)

/** Id bohatera - jedyna postac, ktora nosi ekwipunek gracza. */
const val HERO_ID = "PLAYER_KNIGHT"

/** Ile rund (salwa gracza + salwa wrogow) rozgrywa sie po kazdej odpowiedzi. */
private const val ROUNDS_PER_TURN = 3

private const val CRIT_CHANCE = 0.12f
private const val CRIT_DAMAGE_MULTIPLIER = 1.5f

/** Zla odpowiedz nie blokuje salwy calkiem, ale mocno ja oslabia. */
private const val WRONG_ANSWER_MULTIPLIER = 0.3f

/** Ile milisekund "czasu gry" dolicza kazda zakonczona tura - od tego zalezy wygasanie [CYCLE_BUFF_DURATION_MS]. */
private const val TURN_DURATION_ESTIMATE_MS = 4000L

/** Pauza miedzy krokami salwy, zeby bylo widac kolejne uderzenia, a nie wszystko naraz. */
private const val VOLLEY_PAUSE_MILLIS = 250L

private const val ENEMY_STAGGER_MS = 900L

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
    val attack: Int
)
