package com.lernhero.game.battle.component

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.VectorConverter
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.lernhero.game.character.CharacterState
import com.lernhero.game.battle.BattleSpeed
import com.lernhero.game.battle.BattleUiState
import com.lernhero.game.character.CharacterUiModel
import com.lernhero.game.character.ModularKnight
import com.lernhero.game.character.SpriteCharacter
import com.lernhero.game.battle.component.shake
import com.lernhero.shared.resources.Resources
import kotlinx.coroutines.delay
import org.jetbrains.compose.resources.painterResource

@Composable
internal fun BattleField(
    state: BattleUiState,
    onMoveToAttackFinished: (String) -> Unit,
    onAttackAnimationFinished: (String) -> Unit,
    onMoveBackFinished: (String) -> Unit,
    onEffectShown: (Long) -> Unit
) {
    val allCharacters = state.characters.values.toList()
    val enemies = allCharacters.filter { !it.isPlayer }.sortedBy { it.id }
    val players = allCharacters.filter { it.isPlayer }.sortedBy { it.id }
    val slotPositions = remember { mutableStateMapOf<String, Offset>() }

    BoxWithConstraints(
        modifier = Modifier.fillMaxSize()
    ) {
        // Rozmiar karty jak w szyku (kazda strona zajmuje pol ekranu) - efekty skaluja sie do niego.
        val fxCardSize = calculateCardSize(maxWidth = maxWidth / 2f, maxHeight = maxHeight)

        Image(
            painter = painterResource(Resources.Image.battlegroundFirst),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.FillBounds
        )

        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxSize()
            ) {
                TeamFormation(
                    characters = players,
                    slotPositions = slotPositions,
                    onPositionMeasured = { id, offset ->
                        if (slotPositions[id] != offset) {
                            slotPositions[id] = offset
                        }
                    },
                    onMoveToAttackFinished = onMoveToAttackFinished,
                    onAttackAnimationFinished = onAttackAnimationFinished,
                    onMoveBackFinished = onMoveBackFinished,
                    modifier = Modifier.fillMaxSize()
                )
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxSize()
            ) {
                TeamFormation(
                    characters = enemies,
                    flipCharacters = true,
                    slotPositions = slotPositions,
                    onPositionMeasured = { id, offset ->
                        if (slotPositions[id] != offset) {
                            slotPositions[id] = offset
                        }
                    },
                    onMoveToAttackFinished = onMoveToAttackFinished,
                    onAttackAnimationFinished = onAttackAnimationFinished,
                    onMoveBackFinished = onMoveBackFinished,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }

        // Nakladka efektow lezy nad wszystkim: liczby obrazen i wybuchy nie moga chowac sie za postaciami.
        CombatFxLayer(
            effects = state.floatingEffects,
            slotPositions = slotPositions,
            cardSize = fxCardSize,
            onConsumed = onEffectShown
        )

        BattleHud(
            state = state,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(16.dp)
        )
    }
}

@Composable
private fun TeamFormation(
    characters: List<CharacterUiModel>,
    flipCharacters: Boolean = false,
    slotPositions: Map<String, Offset>,
    onPositionMeasured: (String, Offset) -> Unit,
    onMoveToAttackFinished: (String) -> Unit,
    onAttackAnimationFinished: (String) -> Unit,
    onMoveBackFinished: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    BoxWithConstraints(
        modifier = modifier.padding(vertical = 8.dp)
    ) {
        val cardSize = calculateCardSize(maxWidth = maxWidth, maxHeight = maxHeight)
        val slots = formationSlots(flipCharacters)

        characters
            .mapIndexed { index, character -> character to slots[index % slots.size] }
            .sortedBy { (_, slot) -> slot.y }
            .forEach { (character, slot) ->
                CharacterSlot(
                    character = character,
                    cardSize = cardSize,
                    slotPositions = slotPositions,
                    onPositionMeasured = onPositionMeasured,
                    onMoveToAttackFinished = onMoveToAttackFinished,
                    onAttackAnimationFinished = onAttackAnimationFinished,
                    onMoveBackFinished = onMoveBackFinished,
                    modifier = Modifier.offset(
                        x = maxWidth * slot.x - cardSize / 2,
                        y = maxHeight * slot.y - cardSize / 2
                    )
                )
            }
    }
}

private data class FormationSlot(
    val x: Float,
    val y: Float
)

private fun formationSlots(isRightSide: Boolean): List<FormationSlot> {
    val leftSlots = listOf(
        FormationSlot(x = 0.26f, y = 0.58f),
        FormationSlot(x = 0.42f, y = 0.42f),
        FormationSlot(x = 0.30f, y = 0.32f),
        FormationSlot(x = 0.54f, y = 0.62f),
        FormationSlot(x = 0.50f, y = 0.28f)
    )

    if (!isRightSide) {
        return leftSlots
    }

    return leftSlots.map { slot ->
        slot.copy(x = 1f - slot.x)
    }
}

@Composable
private fun CharacterSlot(
    character: CharacterUiModel,
    cardSize: Dp,
    slotPositions: Map<String, Offset>,
    onPositionMeasured: (String, Offset) -> Unit,
    onMoveToAttackFinished: (String) -> Unit,
    onAttackAnimationFinished: (String) -> Unit,
    onMoveBackFinished: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val density = LocalDensity.current
    val myPosition = slotPositions[character.id]
    val targetPosition = character.currentTargetId?.let { slotPositions[it] }
    val animOffset = remember(character.id) {
        Animatable(Offset.Zero, Offset.VectorConverter)
    }

    LaunchedEffect(character.state, character.currentTargetId, myPosition, targetPosition) {
        when (character.state) {
            CharacterState.MOVING_TO_ATK -> {
                if (targetPosition != null && myPosition != null) {
                    val attackMarginPx = with(density) {
                        cardSize.toPx() * 0.55f
                    }
                    val attackMargin = if (character.isPlayer) -attackMarginPx else attackMarginPx

                    animOffset.animateTo(
                        targetValue = Offset(
                            x = targetPosition.x - myPosition.x + attackMargin,
                            y = targetPosition.y - myPosition.y
                        ),
                        animationSpec = tween(
                            durationMillis = BattleSpeed.scale(110),
                            easing = LinearEasing
                        )
                    )
                    onMoveToAttackFinished(character.id)
                }
            }

            CharacterState.ATTACK -> {
                val attackSprite = character.sprites.forState(CharacterState.ATTACK)
                val full = attackSprite.totalFrames * attackSprite.animationSpeed + 225L
                delay(BattleSpeed.scale(full))
                onAttackAnimationFinished(character.id)
            }

            CharacterState.MOVING_BACK -> {
                animOffset.animateTo(
                    targetValue = Offset.Zero,
                    animationSpec = tween(
                        durationMillis = BattleSpeed.scale(110),
                        easing = LinearEasing
                    )
                )
                onMoveBackFinished(character.id)
            }

            else -> Unit
        }
    }

    Box(
        modifier = modifier
            .size(cardSize)
            .onGloballyPositioned { coordinates ->
                val position = coordinates.positionInRoot()
                val center = Offset(
                    x = position.x + coordinates.size.width / 2f,
                    y = position.y + coordinates.size.height / 2f
                )
                onPositionMeasured(character.id, center)
            }
            .zIndex(if (character.state != CharacterState.IDLE) 10f else 0f),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .offset {
                    IntOffset(
                        x = animOffset.value.x.toInt(),
                        y = animOffset.value.y.toInt()
                    )
                }
                .zIndex(if (character.state == CharacterState.ATTACK) 2f else 0f),
            contentAlignment = Alignment.Center
        ) {
            HealthBar(
                character = character,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .offset(y = cardSize * 0.34f)
                    .width(cardSize * 0.28f)
            )
            val knightEquipment = character.knightEquipment
            if (knightEquipment != null) {
                var attackTrigger by remember(character.id) { mutableStateOf(0) }
                LaunchedEffect(character.state) {
                    if (character.state == CharacterState.ATTACK) attackTrigger += 1
                }
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .shake(character.hp),
                    contentAlignment = Alignment.BottomCenter
                ) {
                    ModularKnight(
                        equipment = knightEquipment,
                        attackTrigger = attackTrigger,
                        interactive = false,
                        attackSpeed = BattleSpeed.multiplier,
                        // battleScale normalises the hero to the same height as the sprite fighters.
                        sizeDp = cardSize * character.battleScale,
                        modifier = Modifier.graphicsLayer {
                            scaleX = if (character.flipSprite) -1f else 1f
                        }
                    )
                }
            } else {
                // Each sprite frames its figure at a different scale; battleScale normalises the
                // rendered width so every fighter appears a consistent size.
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Box(modifier = Modifier.width(cardSize * character.battleScale)) {
                        SpriteCharacter(
                            sprite = character.sprites.forState(character.state),
                            isLooping = character.state != CharacterState.ATTACK && character.state != CharacterState.DEAD,
                            modifier = Modifier
                                .graphicsLayer {
                                    scaleX = if (character.flipSprite) -1f else 1f
                                }
                                .shake(character.hp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun HealthBar(
    character: CharacterUiModel,
    modifier: Modifier = Modifier
) {
    val hpFraction = if (character.maxHp > 0) {
        character.hp.toFloat() / character.maxHp.toFloat()
    } else {
        0f
    }.coerceIn(0f, 1f)

    Box(
        modifier = modifier
            .height(5.dp)
            .clip(RoundedCornerShape(50))
            .background(Color(0xFF2A160E))
            .padding(1.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(hpFraction)
                .fillMaxSize()
                .clip(RoundedCornerShape(50))
                .background(
                    if (hpFraction > 0.35f) {
                        Color(0xFF68D043)
                    } else {
                        Color(0xFFE04736)
                    }
                )
        )
    }
}

private fun calculateCardSize(
    maxWidth: Dp,
    maxHeight: Dp
): Dp {
    val widthBasedSize = maxWidth * 1.35f
    val heightBasedSize = maxHeight * 1.35f
    return minOf(widthBasedSize, heightBasedSize).coerceIn(140.dp, 280.dp)
}
