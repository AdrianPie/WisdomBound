package com.lernhero.game

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lernhero.domain.combat.StatType
import com.lernhero.domain.data.PlayerRepository
import com.lernhero.domain.enum.CharacterState
import com.lernhero.domain.game.BattleFighter
import com.lernhero.domain.game.BattlePlaybackEngine
import com.lernhero.domain.game.BattlePlaybackStep
import com.lernhero.domain.item.ItemSlot
import com.lernhero.domain.item.Loadout
import com.lernhero.domain.item.itemIn
import com.lernhero.domain.item.toLoadout
import com.lernhero.domain.preset.MonsterCatalog
import com.lernhero.domain.preset.PlayerEquipment
import com.lernhero.domain.quiz.QuizBank
import com.lernhero.domain.quiz.QuizEntry
import com.lernhero.game.character.CharacterUiModel
import com.lernhero.game.character.KnightEquipment
import com.lernhero.game.character.MonsterSprites
import com.lernhero.shared.FlagQuizResources
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.random.Random

class GameViewModel(
    private val playerRepository: PlayerRepository
) : ViewModel() {

    private val playbackEngine = BattlePlaybackEngine()

    /** Companion selection loaded from the persisted party (monster ids per slot). */
    private var savedParty: List<String?> = emptyList()

    /** Zalozony ekwipunek bohatera - stad biora sie jego efekty w walce. */
    private var heroLoadout: Loadout = Loadout.Empty

    private val _uiState = MutableStateFlow(createInitialState())
    val uiState = _uiState.asStateFlow()

    init {
        loadSavedPlayer()
    }

    fun playNextBattleStep() {
        _uiState.update { state ->
            if (state.phase != GamePhase.Battle || state.hasActiveAnimation()) {
                return@update state
            }

            when (val step = state.playback?.currentStep) {
                is BattlePlaybackStep.Attack -> {
                    val attacker = state.characters[step.attackerId] ?: return@update state
                    val target = state.characters[step.targetId] ?: return@update state

                    state.copy(
                        battleTurn = if (step.attackerIsPlayer) BattleSide.Players else BattleSide.Enemies,
                        characters = state.characters.plus(
                            attacker.id to attacker.copy(
                                state = CharacterState.MOVING_TO_ATK,
                                currentTargetId = target.id
                            )
                        ),
                        message = "${step.attackerName} atakuje ${step.targetName}"
                    )
                }

                // Trucizna, ogien, odbicie i leczenie nie maja animacji ataku - poprawiaja tylko
                // pasek HP i od razu przewijaja skrypt dalej.
                is BattlePlaybackStep.Tick -> {
                    val target = state.characters[step.targetId] ?: return@update state.advanceStep()
                    state.copy(
                        characters = state.characters.plus(
                            target.id to target.copy(
                                hp = step.targetHpAfter,
                                state = if (step.targetDefeated) CharacterState.DEAD else target.state
                            )
                        ),
                        message = step.label
                    ).advanceStep()
                }

                is BattlePlaybackStep.WaveCleared -> state.startQuizPhase()

                is BattlePlaybackStep.PartyDefeated -> {
                    val reward = calculateRunCoins(state)
                    saveRunReward(reward)
                    state.copy(
                        phase = GamePhase.Defeat,
                        playback = null,
                        rewardCoins = reward,
                        rewardSaved = true,
                        message = "Druzyna przegrala walke. +$reward monet."
                    )
                }

                null -> state.copy(
                    phase = GamePhase.Defeat,
                    playback = null,
                    message = "Brak krokow walki do odtworzenia."
                )
            }
        }
    }

    fun onMoveToAttackFinished(characterId: String) {
        updateCharacterState(characterId, CharacterState.ATTACK)
    }

    fun onAttackAnimationFinished(characterId: String) {
        _uiState.update { state ->
            val attacker = state.characters[characterId] ?: return@update state
            val attackStep = state.playback?.currentStep as? BattlePlaybackStep.Attack
                ?: return@update state
            if (attackStep.attackerId != characterId) return@update state

            val target = state.characters[attackStep.targetId] ?: return@update state
            val updatedTarget = target.copy(
                hp = attackStep.targetHpAfter,
                state = if (attackStep.targetDefeated) CharacterState.DEAD else CharacterState.HIT
            )
            val updatedAttacker = attacker.copy(state = CharacterState.MOVING_BACK)

            state.copy(
                characters = state.characters
                    .plus(attackStep.targetId to updatedTarget)
                    .plus(characterId to updatedAttacker),
                message = "${attackStep.targetName} traci ${attackStep.damage} HP"
            )
        }
    }

    fun onMoveBackFinished(characterId: String) {
        _uiState.update { state ->
            if (state.characters[characterId] == null) return@update state
            val resetCharacters = state.characters.mapValues { (_, character) ->
                when {
                    character.id == characterId -> character.copy(
                        state = if (character.hp <= 0) CharacterState.DEAD else CharacterState.IDLE,
                        currentTargetId = null
                    )

                    character.state == CharacterState.HIT && character.hp > 0 -> character.copy(
                        state = CharacterState.IDLE,
                        currentTargetId = null
                    )

                    else -> character
                }
            }

            state.copy(
                characters = resetCharacters,
                playback = state.playback?.let { playback ->
                    playback.copy(currentStepIndex = playback.currentStepIndex + 1)
                },
                message = "Kolejny krok..."
            )
        }
    }

    /**
     * Przyjmuje odpowiedz i **zatrzymuje** quiz na odslonieciu wyniku - nie przewija od razu
     * do kolejnego pytania. Pusty [answerId] oznacza, ze skonczyl sie czas.
     *
     * Dalej quiz rusza dopiero z [continueQuiz], ktore ekran wywoluje po chwili na obejrzenie
     * poprawnej odpowiedzi.
     */
    fun answerQuiz(answerId: String) {
        _uiState.update { state ->
            val quiz = state.quiz ?: return@update state
            if (state.phase != GamePhase.Quiz || quiz.revealed) return@update state

            val question = quiz.currentQuestion ?: return@update state.startUpgradePhase(quiz)
            val isCorrect = question.answers.firstOrNull { it.id == answerId }?.isCorrect == true

            state.copy(
                // Nagroda idzie za trudnoscia pytania, nie za sama liczba trafien.
                quiz = quiz.copy(
                    correctAnswers = quiz.correctAnswers + if (isCorrect) 1 else 0,
                    earnedPoints = quiz.earnedPoints + if (isCorrect) question.reward else 0,
                    selectedAnswerId = answerId.ifBlank { null },
                    revealed = true
                ),
                message = when {
                    isCorrect -> "Dobra odpowiedz. +${question.reward} pkt"
                    answerId.isBlank() -> "Czas minal."
                    else -> "Zla odpowiedz."
                }
            )
        }
    }

    /** Przechodzi do kolejnego pytania (albo do nagrod) po pokazaniu, ktora odpowiedz byla dobra. */
    fun continueQuiz() {
        _uiState.update { state ->
            val quiz = state.quiz ?: return@update state
            if (state.phase != GamePhase.Quiz || !quiz.revealed) return@update state

            if (quiz.currentQuestionIndex >= quiz.questions.lastIndex) {
                state.startUpgradePhase(quiz)
            } else {
                state.copy(
                    quiz = quiz.copy(
                        currentQuestionIndex = quiz.currentQuestionIndex + 1,
                        selectedAnswerId = null,
                        revealed = false
                    ),
                    message = null
                )
            }
        }
    }

    fun buyUpgrade(choiceId: String) {
        _uiState.update { state ->
            val upgrade = state.upgrade ?: return@update state
            if (state.phase != GamePhase.Upgrade) return@update state

            val choice = upgrade.choices.firstOrNull { it.id == choiceId } ?: return@update state
            if (choice.id in upgrade.selectedChoiceIds || state.rewardPoints < choice.cost) {
                return@update state
            }

            state.copy(
                rewardPoints = state.rewardPoints - choice.cost,
                upgrade = upgrade.copy(
                    selectedChoiceIds = upgrade.selectedChoiceIds + choice.id
                ),
                characters = state.applyUpgrade(choice),
                message = "Ulepszenie: ${choice.title}"
            )
        }
    }

    fun finishUpgradePhase() {
        _uiState.update { state ->
            if (state.phase != GamePhase.Upgrade) return@update state

            val nextWave = state.wave + 1
            val players = state.characters.values
                .filter { it.isPlayer }
                .map {
                    it.copy(
                        state = if (it.hp <= 0) CharacterState.DEAD else CharacterState.IDLE,
                        currentTargetId = null
                    )
                }
            val enemies = buildWaveEnemies(nextWave).map { it.toUiModel() }

            state.copy(
                phase = GamePhase.Battle,
                wave = nextWave,
                battleTurn = BattleSide.Players,
                characters = (players + enemies).associateBy { it.id },
                quiz = null,
                upgrade = null,
                message = "Fala $nextWave"
            ).withPlaybackScript()
        }
    }

    fun restartRun() {
        _uiState.value = createInitialState()
        loadSavedPlayer()
    }

    /** Loads the saved party + equipment and rebuilds the opening battle around them. */
    private fun loadSavedPlayer() {
        viewModelScope.launch(Dispatchers.IO) {
            val player = playerRepository.getPlayer() ?: return@launch
            savedParty = player.party
            val rebuilt = GameStateUi(
                characters = buildInitialCharacters(),
                message = "Fala 1"
            ).applyEquipmentBonuses(player.equipment).withPlaybackScript()
            _uiState.value = rebuilt
        }
    }

    private fun saveRunReward(coins: Int) {
        if (coins <= 0) return
        viewModelScope.launch(Dispatchers.IO) {
            playerRepository.addMoney(coins)
        }
    }

    private fun updateCharacterState(characterId: String, characterState: CharacterState) {
        _uiState.update { state ->
            val character = state.characters[characterId] ?: return@update state
            state.copy(
                characters = state.characters.plus(
                    characterId to character.copy(state = characterState)
                )
            )
        }
    }

    private fun createInitialState(): GameStateUi =
        GameStateUi(
            characters = buildInitialCharacters(),
            message = "Fala 1"
        ).withPlaybackScript()

    private fun GameStateUi.withPlaybackScript(): GameStateUi {
        val script = playbackEngine.createScript(
            wave = wave,
            fighters = characters.values.map { character -> character.toBattleFighter() }
        )

        return copy(
            phase = GamePhase.Battle,
            battleTurn = BattleSide.Players,
            playback = BattlePlaybackUiState(script = script),
            message = message ?: "Odtwarzanie walki"
        )
    }

    private fun GameStateUi.startQuizPhase(): GameStateUi {
        val quiz = createQuizForWave(wave)
        return copy(
            phase = GamePhase.Quiz,
            playback = null,
            quiz = quiz,
            message = "Fala $wave pokonana. Quiz odblokowany."
        )
    }

    private fun GameStateUi.startUpgradePhase(quiz: QuizState): GameStateUi {
        val earnedPoints = calculateQuizReward(quiz)
        return copy(
            phase = GamePhase.Upgrade,
            rewardPoints = rewardPoints + earnedPoints,
            totalScore = totalScore + earnedPoints,
            playback = null,
            quiz = quiz,
            upgrade = createUpgradeState(wave),
            message = "Quiz: ${quiz.correctAnswers}/${quiz.questions.size}. +$earnedPoints punktow."
        )
    }

    private fun GameStateUi.advanceStep(): GameStateUi = copy(
        playback = playback?.let { it.copy(currentStepIndex = it.currentStepIndex + 1) }
    )

    private fun GameStateUi.hasActiveAnimation(): Boolean =
        characters.values.any { character ->
            character.state == CharacterState.MOVING_TO_ATK ||
                character.state == CharacterState.ATTACK ||
                character.state == CharacterState.MOVING_BACK
        }

    private fun GameStateUi.applyUpgrade(choice: UpgradeChoice): Map<CharacterId, CharacterUiModel> {
        return characters.mapValues { (_, character) ->
            if (!character.isPlayer) {
                character
            } else {
                when (choice.type) {
                    UpgradeType.TeamHealth, UpgradeType.Shield -> {
                        val value = choice.powerValue()
                        character.copy(
                            hp = character.hp + value,
                            maxHp = character.maxHp + value
                        )
                    }

                    UpgradeType.TeamAttack, UpgradeType.CriticalFocus -> character.copy(
                        attack = character.attack + choice.powerValue()
                    )

                    UpgradeType.HealTeam -> character.copy(
                        hp = character.maxHp
                    )
                }
            }
        }
    }

    /** Punkty za trafienia (wg trudnosci) plus premia za komplet. */
    private fun calculateQuizReward(quiz: QuizState): Int {
        val perfectBonus = if (quiz.correctAnswers == quiz.questions.size) 150 else 0
        return quiz.earnedPoints + perfectBonus
    }

    private fun calculateRunCoins(state: GameStateUi): Int =
        (25 + (state.wave - 1) * 20 + state.totalScore / 20).coerceAtLeast(10)

    /**
     * Ekwipunek gracza wchodzi do walki jako [Loadout]: statystyki doliczamy do liczb pokazywanych
     * w UI, a efekty (rozciecie na dwoch wrogow, kradziez zycia, blok pierwszego ciosu) jada dalej
     * do silnika razem z wojownikiem.
     */
    private fun GameStateUi.applyEquipmentBonuses(equipment: PlayerEquipment): GameStateUi {
        val loadout = equipment.toLoadout()
        heroLoadout = loadout

        val hpBonus = loadout.stats[StatType.MaxHp].toInt()
        val attackBonus = loadout.stats[StatType.Attack].toInt()

        val updatedCharacters = characters.mapValues { (_, character) ->
            if (character.id != HERO_ID) {
                character
            } else {
                character.copy(
                    maxHp = character.maxHp + hpBonus,
                    hp = character.hp + hpBonus,
                    attack = character.attack + attackBonus,
                    // Rycerz rysuje sie z plyt, wiec pyta tylko, czy w slocie cokolwiek stoi.
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

    private fun createUpgradeState(wave: Int): UpgradeState =
        UpgradeState(
            choices = createUpgradePool(wave)
                .shuffled(Random.Default)
                .take(6)
                .sortedWith(
                    compareBy<UpgradeChoice> { it.rarity.ordinal }
                        .thenBy { it.cost }
                )
        )

    private fun createUpgradePool(wave: Int): List<UpgradeChoice> {
        val waveCost = (wave - 1) * 25
        return listOf(
            UpgradeChoice(
                id = "wave_${wave}_health_common",
                title = "Twarda skora",
                description = "+18 maksymalnego HP dla kazdej postaci.",
                cost = 80 + waveCost,
                rarity = UpgradeRarity.Common,
                type = UpgradeType.TeamHealth
            ),
            UpgradeChoice(
                id = "wave_${wave}_attack_common",
                title = "Ostre klingi",
                description = "+3 ataku dla kazdej postaci.",
                cost = 90 + waveCost,
                rarity = UpgradeRarity.Common,
                type = UpgradeType.TeamAttack
            ),
            UpgradeChoice(
                id = "wave_${wave}_heal_common",
                title = "Bandaze",
                description = "Leczy druzyne do pelnego HP.",
                cost = 70 + waveCost,
                rarity = UpgradeRarity.Common,
                type = UpgradeType.HealTeam
            ),
            UpgradeChoice(
                id = "wave_${wave}_health_rare",
                title = "Runiczna zbroja",
                description = "+32 maksymalnego HP dla kazdej postaci.",
                cost = 140 + waveCost,
                rarity = UpgradeRarity.Rare,
                type = UpgradeType.TeamHealth
            ),
            UpgradeChoice(
                id = "wave_${wave}_attack_rare",
                title = "Bitewny rytm",
                description = "+6 ataku dla kazdej postaci.",
                cost = 165 + waveCost,
                rarity = UpgradeRarity.Rare,
                type = UpgradeType.TeamAttack
            ),
            UpgradeChoice(
                id = "wave_${wave}_shield_rare",
                title = "Bariera",
                description = "+24 HP jako defensywny bonus dla druzyny.",
                cost = 125 + waveCost,
                rarity = UpgradeRarity.Rare,
                type = UpgradeType.Shield
            ),
            UpgradeChoice(
                id = "wave_${wave}_attack_epic",
                title = "Smoczy gniew",
                description = "+10 ataku dla kazdej postaci.",
                cost = 240 + waveCost,
                rarity = UpgradeRarity.Epic,
                type = UpgradeType.TeamAttack
            ),
            UpgradeChoice(
                id = "wave_${wave}_focus_epic",
                title = "Skupienie krytyczne",
                description = "+10 ataku dla kazdej postaci.",
                cost = 260 + waveCost,
                rarity = UpgradeRarity.Epic,
                type = UpgradeType.CriticalFocus
            ),
            UpgradeChoice(
                id = "wave_${wave}_health_epic",
                title = "Serce tytana",
                description = "+48 maksymalnego HP dla kazdej postaci.",
                cost = 230 + waveCost,
                rarity = UpgradeRarity.Epic,
                type = UpgradeType.TeamHealth
            ),
            UpgradeChoice(
                id = "wave_${wave}_legendary",
                title = "Korona zwyciezcy",
                description = "+16 ataku dla calej druzyny.",
                cost = 380 + waveCost,
                rarity = UpgradeRarity.Legendary,
                type = UpgradeType.CriticalFocus
            )
        )
    }

    /** Sila ulepszenia rosnie z rzadkoscia. Jedna tabela zamiast czterech bloków when. */
    private fun UpgradeChoice.powerValue(): Int {
        val base = when (type) {
            UpgradeType.TeamHealth -> 18
            UpgradeType.Shield -> 16
            UpgradeType.TeamAttack -> 3
            UpgradeType.CriticalFocus -> 4
            UpgradeType.HealTeam -> 0
        }
        val multiplier = when (rarity) {
            UpgradeRarity.Common -> 1.0f
            UpgradeRarity.Rare -> 1.8f
            UpgradeRarity.Epic -> 2.7f
            UpgradeRarity.Legendary -> 4.0f
        }
        return (base * multiplier).toInt()
    }

    /**
     * Quiz na dana fale: cztery pytania ustawione w drabinke trudnosci, losowane z banku.
     * Odpowiedzi tasujemy tutaj, wiec bank nigdy nie zdradza, ktora jest poprawna.
     */
    private fun createQuizForWave(wave: Int): QuizState =
        QuizState(
            questions = QuizBank.drawForWave(wave, Random.Default).map { it.toUiQuestion() }
        )

    private fun QuizEntry.toUiQuestion(): QuizQuestion = QuizQuestion(
        id = id,
        prompt = prompt,
        flag = flagCode?.let { FlagQuizResources.flagFor(it) },
        answers = answers.shuffled(Random.Default).map { text ->
            QuizAnswer(
                id = text,
                text = text,
                isCorrect = text == correct
            )
        },
        difficulty = difficulty.label,
        category = category.label,
        reward = difficulty.reward,
        seconds = difficulty.seconds
    )

    private fun buildInitialCharacters(): Map<CharacterId, CharacterUiModel> {
        val characters = buildPlayerParty().map { it.toUiModel() } +
            buildWaveEnemies(wave = 1).map { it.toUiModel() }

        return characters.associateBy { it.id }
    }

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

    private fun buildWaveEnemies(wave: Int): List<CharacterTemplate> {
        val hpBonus = (wave - 1) * 25
        val attackBonus = (wave - 1) * 4
        val enemyCount = if (wave < 3) 2 else 3

        return List(enemyCount) { index ->
            val number = index + 1
            CharacterTemplate(
                id = "ENEMY_${wave}_$number",
                name = "Potwor $number",
                isPlayer = false,
                avatar = if (number % 2 == 0) "wolf" else "skeleton",
                hp = 70 + hpBonus + index * 15,
                maxHp = 70 + hpBonus + index * 15,
                attack = 10 + attackBonus + index * 2
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
            // The hero is drawn as the modular paper-doll knight so equipped gear shows in battle.
            knightEquipment = if (id == HERO_ID) KnightEquipment.Empty else null,
            battleScale = avatarBattleScale(avatar)
        )

    /**
     * Normalises each fighter to a consistent on-screen size. Values are relative to the battle
     * card width; tweak here if a sprite still looks too big/small. "knight" is the paper-doll
     * hero (its art only fills ~55% of its canvas, hence < 1).
     */
    private fun avatarBattleScale(avatar: String): Float = when (avatar) {
        "knight" -> 0.85f
        "mag" -> 1.9f
        "wolf" -> 1.0f
        "skeleton" -> 1.0f
        else -> 1.0f
    }

    private fun CharacterUiModel.toBattleFighter(): BattleFighter =
        BattleFighter(
            id = id,
            name = name,
            isPlayer = isPlayer,
            hp = hp,
            maxHp = maxHp,
            attack = attack,
            loadout = if (id == HERO_ID) heroLoadout else Loadout.Empty
        )

    private fun mapCharacterSprites(key: String) = MonsterSprites.spritesFor(key)
}

/** Id bohatera - jedyna postac, ktora nosi ekwipunek gracza. */
const val HERO_ID = "PLAYER_KNIGHT"

private data class CharacterTemplate(
    val id: String,
    val name: String,
    val isPlayer: Boolean,
    val avatar: String,
    val hp: Int,
    val maxHp: Int,
    val attack: Int
)
