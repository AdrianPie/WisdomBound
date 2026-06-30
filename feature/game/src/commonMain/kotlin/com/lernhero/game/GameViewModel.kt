package com.lernhero.game

import androidx.lifecycle.ViewModel
import com.lernhero.domain.enum.CharacterState
import com.lernhero.domain.game.BattleFighter
import com.lernhero.domain.game.BattlePlaybackEngine
import com.lernhero.domain.game.BattlePlaybackStep
import com.lernhero.game.character.CharacterSprites
import com.lernhero.game.character.CharacterUiModel
import com.lernhero.shared.FlagCountry
import com.lernhero.shared.FlagQuizResources
import com.lernhero.shared.Resources
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlin.random.Random

class GameViewModel : ViewModel() {

    private val playbackEngine = BattlePlaybackEngine()

    private val _uiState = MutableStateFlow(createInitialState())
    val uiState = _uiState.asStateFlow()

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

                is BattlePlaybackStep.WaveCleared -> state.startQuizPhase()

                is BattlePlaybackStep.PartyDefeated -> state.copy(
                    phase = GamePhase.Defeat,
                    playback = null,
                    message = "Druzyna przegrala walke."
                )

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

    fun answerQuiz(answerId: String) {
        _uiState.update { state ->
            val quiz = state.quiz ?: return@update state
            if (state.phase != GamePhase.Quiz) return@update state

            val question = quiz.currentQuestion ?: return@update state.startUpgradePhase(quiz)
            val isCorrect = question.answers.firstOrNull { it.id == answerId }?.isCorrect == true
            val updatedQuiz = quiz.copy(
                correctAnswers = quiz.correctAnswers + if (isCorrect) 1 else 0
            )

            if (quiz.currentQuestionIndex >= quiz.questions.lastIndex) {
                state.startUpgradePhase(updatedQuiz)
            } else {
                state.copy(
                    quiz = updatedQuiz.copy(
                        currentQuestionIndex = quiz.currentQuestionIndex + 1
                    ),
                    message = if (isCorrect) "Dobra odpowiedz." else "Zla odpowiedz."
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
                    UpgradeType.TeamHealth -> {
                        val value = choice.powerValue()
                        character.copy(
                            hp = character.hp + value,
                            maxHp = character.maxHp + value
                        )
                    }

                    UpgradeType.TeamAttack -> character.copy(
                        attack = character.attack + choice.powerValue()
                    )

                    UpgradeType.TeamMana -> character.copy(
                        mana = character.mana + choice.powerValue()
                    )

                    UpgradeType.TeamSpeed -> character.copy(
                        attack = character.attack + (choice.powerValue() / 2).coerceAtLeast(1),
                        mana = character.mana + choice.powerValue()
                    )

                    UpgradeType.Shield -> character.copy(
                        hp = character.hp + choice.powerValue(),
                        maxHp = character.maxHp + choice.powerValue()
                    )

                    UpgradeType.CriticalFocus -> character.copy(
                        attack = character.attack + choice.powerValue(),
                        mana = character.mana + choice.powerValue()
                    )

                    UpgradeType.HealTeam -> character.copy(
                        hp = character.maxHp
                    )
                }
            }
        }
    }

    private fun calculateQuizReward(quiz: QuizState): Int {
        val basePoints = quiz.correctAnswers * 100
        val perfectBonus = if (quiz.correctAnswers == quiz.questions.size) 100 else 0
        return basePoints + perfectBonus
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
                id = "wave_${wave}_mana_common",
                title = "Krotki oddech many",
                description = "+12 many dla kazdej postaci.",
                cost = 75 + waveCost,
                rarity = UpgradeRarity.Common,
                type = UpgradeType.TeamMana
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
                id = "wave_${wave}_speed_rare",
                title = "Szybki start",
                description = "+18 many i lekki bonus do ataku.",
                cost = 135 + waveCost,
                rarity = UpgradeRarity.Rare,
                type = UpgradeType.TeamSpeed
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
                description = "+8 ataku i +8 many dla kazdej postaci.",
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
                description = "+16 ataku i +16 many dla calej druzyny.",
                cost = 380 + waveCost,
                rarity = UpgradeRarity.Legendary,
                type = UpgradeType.CriticalFocus
            )
        )
    }

    private fun UpgradeChoice.powerValue(): Int =
        when (rarity) {
            UpgradeRarity.Common -> when (type) {
                UpgradeType.TeamHealth -> 18
                UpgradeType.TeamAttack -> 3
                UpgradeType.TeamMana -> 12
                UpgradeType.TeamSpeed -> 10
                UpgradeType.Shield -> 16
                UpgradeType.CriticalFocus -> 4
                UpgradeType.HealTeam -> 0
            }

            UpgradeRarity.Rare -> when (type) {
                UpgradeType.TeamHealth -> 32
                UpgradeType.TeamAttack -> 6
                UpgradeType.TeamMana -> 20
                UpgradeType.TeamSpeed -> 18
                UpgradeType.Shield -> 24
                UpgradeType.CriticalFocus -> 6
                UpgradeType.HealTeam -> 0
            }

            UpgradeRarity.Epic -> when (type) {
                UpgradeType.TeamHealth -> 48
                UpgradeType.TeamAttack -> 10
                UpgradeType.TeamMana -> 30
                UpgradeType.TeamSpeed -> 26
                UpgradeType.Shield -> 36
                UpgradeType.CriticalFocus -> 8
                UpgradeType.HealTeam -> 0
            }

            UpgradeRarity.Legendary -> when (type) {
                UpgradeType.TeamHealth -> 72
                UpgradeType.TeamAttack -> 16
                UpgradeType.TeamMana -> 48
                UpgradeType.TeamSpeed -> 40
                UpgradeType.Shield -> 52
                UpgradeType.CriticalFocus -> 16
                UpgradeType.HealTeam -> 0
            }
        }

    private fun createQuizForWave(wave: Int): QuizState =
        QuizState(
            questions = FlagQuizResources.countries
                .shuffled(Random.Default)
                .take(3)
                .mapIndexed { index, country ->
                    country.toFlagQuestion(
                        id = "wave_${wave}_flag_${index + 1}",
                        allCountries = FlagQuizResources.countries
                    )
                }
        )

    private fun FlagCountry.toFlagQuestion(
        id: String,
        allCountries: List<FlagCountry>
    ): QuizQuestion {
        val wrongAnswers = allCountries
            .filter { country -> country.code != code }
            .shuffled(Random.Default)
            .take(3)

        val answers = (wrongAnswers + this)
            .shuffled(Random.Default)
            .map { country ->
                QuizAnswer(
                    id = country.code,
                    text = country.countryName,
                    isCorrect = country.code == code
                )
            }

        return QuizQuestion(
            id = id,
            prompt = "Jaka to flaga?",
            flag = flag,
            answers = answers
        )
    }

    private fun buildInitialCharacters(): Map<CharacterId, CharacterUiModel> {
        val characters = buildPlayerParty().map { it.toUiModel() } +
            buildWaveEnemies(wave = 1).map { it.toUiModel() }

        return characters.associateBy { it.id }
    }

    private fun buildPlayerParty(): List<CharacterTemplate> =
        listOf(
            CharacterTemplate(
                id = "PLAYER_MAGE",
                name = "Skeleton Mage",
                isPlayer = true,
                avatar = "mag",
                hp = 100,
                maxHp = 100,
                mana = 60,
                attack = 18
            ),
            CharacterTemplate(
                id = "PLAYER_KNIGHT",
                name = "Knight",
                isPlayer = true,
                avatar = "knight",
                hp = 130,
                maxHp = 130,
                mana = 25,
                attack = 44
            )
        )

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
                avatar = if (number % 2 == 0) "mag" else "knight",
                hp = 70 + hpBonus + index * 15,
                maxHp = 70 + hpBonus + index * 15,
                mana = 0,
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
            mana = mana,
            isPlayer = isPlayer,
            sprites = mapCharacterSprites(avatar)
        )

    private fun CharacterUiModel.toBattleFighter(): BattleFighter =
        BattleFighter(
            id = id,
            name = name,
            isPlayer = isPlayer,
            hp = hp,
            maxHp = maxHp,
            attack = attack
        )

    private fun mapCharacterSprites(key: String) = when (key) {
        "mag" -> CharacterSprites(
            idle = Resources.Sprite.magSkeletoniIdle,
            attack = Resources.Sprite.magSkeletoniIdle,
            hit = Resources.Sprite.magSkeletoniIdle,
            dead = Resources.Sprite.magSkeletoniIdle
        )

        "knight" -> CharacterSprites(
            idle = Resources.Sprite.knight,
            attack = Resources.Sprite.knightAtk,
            hit = Resources.Sprite.knight,
            dead = Resources.Sprite.knight
        )

        else -> CharacterSprites(
            idle = Resources.Sprite.knight,
            attack = Resources.Sprite.knightAtk,
            hit = Resources.Sprite.knight,
            dead = Resources.Sprite.knight
        )
    }
}

private data class CharacterTemplate(
    val id: String,
    val name: String,
    val isPlayer: Boolean,
    val avatar: String,
    val hp: Int,
    val maxHp: Int,
    val mana: Int,
    val attack: Int
)
