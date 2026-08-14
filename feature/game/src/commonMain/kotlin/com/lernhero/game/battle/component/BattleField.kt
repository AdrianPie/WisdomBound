package com.lernhero.game.battle.component

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.runtime.key
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
import com.lernhero.game.battle.FloatingEffect
import com.lernhero.game.battle.HERO_ID
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
    onAttackFinished: (String) -> Unit,
    onHitRecovered: (String) -> Unit,
    onEffectShown: (Long) -> Unit,
    onCapitulate: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val allCharacters = state.characters.values.toList()
    val enemies = allCharacters.filter { !it.isPlayer }.sortedBy { it.id }
    val players = allCharacters.filter { it.isPlayer }.sortedBy { it.id }
    val slotPositions = remember { mutableStateMapOf<String, Offset>() }

    // Piec dobrych odpowiedzi z rzedu odpala ulti - cale pole bitwy mruga na zloto na chwile.
    val ultiFlash = remember { Animatable(0f) }
    LaunchedEffect(state.ultiPulseId) {
        if (state.ultiPulseId == 0L) return@LaunchedEffect
        ultiFlash.snapTo(0.5f)
        ultiFlash.animateTo(0f, animationSpec = tween(durationMillis = 550, easing = LinearEasing))
    }

    BoxWithConstraints(
        modifier = modifier.fillMaxSize()
    ) {
        Image(
            painter = painterResource(Resources.Image.battlegroundFirst),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.FillBounds
        )

        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp, vertical = 12.dp),
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
                    floatingEffects = state.floatingEffects,
                    onPositionMeasured = { id, offset ->
                        if (slotPositions[id] != offset) {
                            slotPositions[id] = offset
                        }
                    },
                    onAttackFinished = onAttackFinished,
                    onHitRecovered = onHitRecovered,
                    onEffectShown = onEffectShown,
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
                    floatingEffects = state.floatingEffects,
                    onPositionMeasured = { id, offset ->
                        if (slotPositions[id] != offset) {
                            slotPositions[id] = offset
                        }
                    },
                    onAttackFinished = onAttackFinished,
                    onHitRecovered = onHitRecovered,
                    onEffectShown = onEffectShown,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }

        // Pocisk to jedyna rzecz, ktora jeszcze potrzebuje globalnych pozycji - wybuch i liczba
        // obrazen siedza teraz lokalnie na kazdej postaci (patrz CharacterSlot), wiec nie moga
        // juz "polecieć" gdzies indziej na ekranie.
        ProjectileOverlay(
            effects = state.floatingEffects,
            slotPositions = slotPositions
        )

        if (ultiFlash.value > 0f) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFFFFD100).copy(alpha = ultiFlash.value))
            )
        }

        val hero = state.characters[HERO_ID]
        val heroHpFraction = if (hero != null && hero.maxHp > 0) {
            hero.hp.toFloat() / hero.maxHp.toFloat()
        } else {
            1f
        }

        WaveBadge(
            wave = state.wave,
            heroHpFraction = heroHpFraction,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(10.dp)
        )

        AnswerCountBadge(
            count = state.answeredCount,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(10.dp)
        )

        BattleMessageBanner(
            message = state.message,
            buffLabel = state.activeCycleBuff?.takeIf { state.elapsedMs < it.expiresAtMs }?.outcome?.label,
            buffPositive = (state.activeCycleBuff?.outcome?.attackMultiplier ?: 1f) >= 1f,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 56.dp)
        )

        BattleControls(
            phase = state.phase,
            onCapitulate = onCapitulate,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(10.dp)
        )
    }
}

@Composable
private fun TeamFormation(
    characters: List<CharacterUiModel>,
    flipCharacters: Boolean = false,
    floatingEffects: List<FloatingEffect>,
    onPositionMeasured: (String, Offset) -> Unit,
    onAttackFinished: (String) -> Unit,
    onHitRecovered: (String) -> Unit,
    onEffectShown: (Long) -> Unit,
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
                // Kazda postac dostaje wlasny, przypiety do id "przedzial" kompozycji - inaczej gdy
                // fala sie zmienia (wrogowie umieraja, nowi dochodza), Compose potrafi podpiac stan
                // animacji (approach, lean) pod NIEWLASCIWA postac, bo trafil w to samo miejsce w
                // kolejnosci wywolan. Std brala sie ta "losowosc" efektow po zmianie fali.
                key(character.id) {
                    // Wrogowie nadciagaja z krawedzi ekranu do swojego miejsca w formacji; gracze
                    // stoja w miejscu (approach = 0 zawsze), wiec dla nich to nie robi nic. Approach
                    // teraz skacze wprost z 1f na 0f (patrz BattleViewModel.staggerEnemyEntrance), wiec
                    // to ten tween - nie kolejne male kroki tickera - musi caly czas trwania wejscia.
                    val animatedApproach by animateFloatAsState(
                        targetValue = character.approach,
                        animationSpec = tween(durationMillis = 900, easing = FastOutSlowInEasing)
                    )
                    val effectiveX = if (flipCharacters) {
                        slot.x + animatedApproach * (ENEMY_SPAWN_X - slot.x)
                    } else {
                        slot.x
                    }

                    CharacterSlot(
                        character = character,
                        cardSize = cardSize,
                        effects = floatingEffects.filter { it.targetId == character.id },
                        onPositionMeasured = onPositionMeasured,
                        onAttackFinished = onAttackFinished,
                        onHitRecovered = onHitRecovered,
                        onEffectShown = onEffectShown,
                        modifier = Modifier.offset(
                            x = maxWidth * effectiveX - cardSize / 2,
                            y = maxHeight * slot.y - cardSize / 2
                        )
                    )
                }
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

/**
 * Wojownik stoi caly czas w swoim miejscu w formacji - atak to tylko zmiana klatek sprite'a
 * (i lekkie "szturchniecie" w strone wroga przez [lean]), nigdy przemieszczenie karty.
 * Tak dziala kazdy porzadny idle-battler: nikt nie biega przez ekran, zeby uderzyc.
 */
@Composable
private fun CharacterSlot(
    character: CharacterUiModel,
    cardSize: Dp,
    effects: List<FloatingEffect>,
    onPositionMeasured: (String, Offset) -> Unit,
    onAttackFinished: (String) -> Unit,
    onHitRecovered: (String) -> Unit,
    onEffectShown: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val leanPx = with(LocalDensity.current) { (cardSize * 0.12f).toPx() }
    val lean by animateFloatAsState(
        targetValue = if (character.state == CharacterState.ATTACK) {
            if (character.isPlayer) leanPx else -leanPx
        } else {
            0f
        },
        animationSpec = tween(durationMillis = BattleSpeed.scale(90), easing = LinearEasing)
    )

    LaunchedEffect(character.state) {
        when (character.state) {
            CharacterState.ATTACK -> {
                val attackSprite = character.sprites.forState(CharacterState.ATTACK)
                val full = attackSprite.totalFrames * attackSprite.animationSpeed + 225L
                delay(BattleSpeed.scale(full))
                onAttackFinished(character.id)
            }

            CharacterState.HIT -> {
                delay(BattleSpeed.scale(HIT_FLASH_MILLIS))
                onHitRecovered(character.id)
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
                .offset { IntOffset(x = lean.toInt(), y = 0) },
            contentAlignment = Alignment.Center
        ) {
            HealthBar(
                character = character,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    // Sprity o wiekszej skali (np. mag 1.9x) rysuja glowe znacznie wyzej nad
                    // srodkiem karty niz sprity o skali 1x - dzielimy przez battleScale, zeby pasek
                    // zawsze wyladowal tuz nad glowa, a nie na piersi wielkich postaci.
                    .offset(y = cardSize * 0.30f / character.battleScale)
                    .width(cardSize * 0.34f)
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

            // Wybuch i liczba obrazen rysowane NA WIERZCHU postaci - stad rendering na samym
            // koncu tej kolejnosci (Box uklada dzieci warstwami, ostatnie jest najwyzej).
            effects.forEach { effect ->
                key(effect.id) {
                    LocalImpactEffect(
                        effect = effect,
                        cardSize = cardSize,
                        hasProjectile = effect.sourceId != null,
                        onDone = { onEffectShown(effect.id) }
                    )
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
            .height(9.dp)
            .clip(RoundedCornerShape(50))
            .background(Color(0xFF120C08))
            .border(1.dp, Color(0xFF000000).copy(alpha = 0.6f), RoundedCornerShape(50))
            .padding(1.5.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(hpFraction)
                .fillMaxSize()
                .clip(RoundedCornerShape(50))
                .background(
                    if (hpFraction > 0.35f) {
                        Color(0xFF6FE04A)
                    } else {
                        Color(0xFFFF4B3E)
                    }
                )
        )
    }
}

private fun calculateCardSize(
    maxWidth: Dp,
    maxHeight: Dp
): Dp {
    val widthBasedSize = maxWidth * 1.05f
    val heightBasedSize = maxHeight * 1.05f
    return minOf(widthBasedSize, heightBasedSize).coerceIn(110.dp, 210.dp)
}

/** Poza prawa krawedzia wlasnej polowki pola - stad wrogowie wchodza na ekran, wyraznie zza jego brzegu. */
private const val ENEMY_SPAWN_X = 1.7f

/** Jak dlugo swieci sie czerwona poswiata trafienia, zanim postac wraca do bezczynnosci. */
private const val HIT_FLASH_MILLIS = 260L
