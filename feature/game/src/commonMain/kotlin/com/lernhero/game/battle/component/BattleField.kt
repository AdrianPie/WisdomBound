package com.lernhero.game.battle.component

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.EaseInOutSine
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.material3.Text
import androidx.compose.ui.unit.sp
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
import androidx.compose.ui.graphics.Brush
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
import com.lernhero.domain.combat.playback.HitKind
import com.lernhero.game.battle.effects.CharacterRect
import com.lernhero.game.battle.effects.DamageNumberController
import com.lernhero.game.battle.effects.DamageNumbersOverlay
import com.lernhero.game.battle.effects.EffectCatalog
import com.lernhero.game.battle.effects.EffectMotion
import com.lernhero.game.battle.effects.EffectsController
import com.lernhero.game.battle.effects.EffectsOverlay
import com.lernhero.game.battle.effects.damageNumberColor
import com.lernhero.game.character.CharacterUiModel
import com.lernhero.game.character.ModularKnight
import com.lernhero.game.character.MonsterCharacter
import com.lernhero.game.character.MonsterVisualPresets
import com.lernhero.shared.resources.CombatAnchorSet
import com.lernhero.shared.resources.Resources
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource
import kotlin.math.roundToInt

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

    // Miejsce i rozmiar KAZDEJ postaci na ekranie (root wspolrzedne) - jedyne, czego potrzebuje
    // warstwa efektow, zeby zamienic ulamkowe anchory ([CombatAnchorSet]) na prawdziwe piksele.
    val characterRects = remember { mutableStateMapOf<String, CharacterRect>() }
    val effectsController = remember { EffectsController() }
    val damageNumberController = remember { DamageNumberController() }

    // Piec dobrych odpowiedzi z rzedu odpala ulti - cale pole bitwy mruga na zloto na chwile.
    val ultiFlash = remember { Animatable(0f) }
    LaunchedEffect(state.ultiPulseId) {
        if (state.ultiPulseId == 0L) return@LaunchedEffect
        ultiFlash.snapTo(0.5f)
        ultiFlash.animateTo(0f, animationSpec = tween(durationMillis = 550, easing = LinearEasing))
    }

    CombatEffectsCoordinator(
        characters = state.characters,
        floatingEffects = state.floatingEffects,
        characterRects = characterRects,
        effectsController = effectsController,
        damageNumberController = damageNumberController,
        onEffectShown = onEffectShown
    )

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
                    isEnemySide = false,
                    stunnedIds = state.stunnedCharacterIds,
                    guardCharges = state.guardCharges,
                    onRectMeasured = { id, rect -> characterRects[id] = rect },
                    onAttackFinished = onAttackFinished,
                    onHitRecovered = onHitRecovered,
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
                    isEnemySide = true,
                    onRectMeasured = { id, rect -> characterRects[id] = rect },
                    onAttackFinished = onAttackFinished,
                    onHitRecovered = onHitRecovered,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }

        // Warstwa efektow - calkowicie osobna od postaci, patrz com.lernhero.game.battle.effects.
        EffectsOverlay(controller = effectsController, modifier = Modifier.fillMaxSize())
        DamageNumbersOverlay(controller = damageNumberController, modifier = Modifier.fillMaxSize())
        HealthBarsOverlay(
            characters = allCharacters,
            characterRects = characterRects,
            modifier = Modifier.fillMaxSize()
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

        EndlessBuffRow(
            buffs = state.endlessBuffs,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(start = 10.dp, top = 66.dp, end = 90.dp)
        )

        // Stan druzyny (buff/debuff cyklu, passa, ogluszenie) - tuz nad naszymi postaciami, po ich
        // stronie pola bitwy, zeby bylo widac przez cala walke, a nie tylko w chwili odpowiedzi.
        TeamStatusBar(
            state = state,
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(start = 10.dp, bottom = 8.dp, end = 60.dp)
        )

        // Wynik ostatniej odpowiedzi - na srodku, nie do przeoczenia.
        AnswerOutcomeBanner(
            outcome = state.turnOutcome,
            modifier = Modifier
                .align(Alignment.Center)
                .padding(horizontal = 16.dp)
        )

        AnswerCountBadge(
            count = state.answeredCount,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(10.dp)
        )

        // Zaklecie bossa w toku: powiekszony, przyblizony portret siedzi tam, gdzie normalnie widac
        // kawalek pola bitwy nad rozszerzonym dolem quizu - gora prawa, jak przy zwyklym pytaniu.
        state.bossEvent?.let { bossEvent ->
            state.characters[bossEvent.bossId]?.let { boss ->
                BossPortrait(
                    boss = boss,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(top = 46.dp, end = 8.dp)
                )
            }
        }

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

/** Ktory [CombatAnchorSet] pasuje do danej postaci - hero ma swoj staly, monstery jeden na gatunek. */
private fun anchorsFor(character: CharacterUiModel): CombatAnchorSet =
    if (character.knightEquipment != null) {
        Resources.KnightCombatAnchors
    } else {
        MonsterVisualPresets.forSpecies(character.avatarKey).anchors
    }

/**
 * Paski HP w OSOBNEJ warstwie nad calym polem bitwy, pozycjonowane z [CharacterRect] (prawdziwy,
 * zmierzony prostokat sprite'a na ekranie - onGloballyPositioned) + [CombatAnchorSet.hpBarAnchor],
 * dokladnie tak samo jak hitPoint/projectileOrigin. Dzieki temu pasek trzyma sie realnej glowy
 * postaci niezaleznie od cardSize/battleScale/dodatkowego skalowania sprite'a (np. preset.visualScale
 * u potworow) - zero zgadywania stalych w kodzie. Anchor tunowany wizualnie w Monster Anchor Lab.
 */
@Composable
private fun HealthBarsOverlay(
    characters: List<CharacterUiModel>,
    characterRects: Map<String, CharacterRect>,
    modifier: Modifier = Modifier
) {
    var layerOrigin by remember { mutableStateOf(Offset.Zero) }
    val density = LocalDensity.current

    Box(
        modifier = modifier.onGloballyPositioned { layerOrigin = it.positionInRoot() }
    ) {
        characters.forEach { character ->
            val rect = characterRects[character.id] ?: return@forEach
            key(character.id) {
                val world = rect.resolve(anchorsFor(character).hpBarAnchor)
                val local = world - layerOrigin
                val barWidthPx = rect.size.width * 0.5f
                val barWidthDp = with(density) { barWidthPx.toDp() }
                HealthBar(
                    character = character,
                    modifier = Modifier
                        .offset {
                            IntOffset(
                                x = (local.x - barWidthPx / 2f).roundToInt(),
                                y = local.y.roundToInt()
                            )
                        }
                        .width(barWidthDp)
                )
            }
        }
    }
}

/**
 * Niewidzialny "dyrygent": dla kazdego nowego [FloatingEffect] (Game Logic -> Combat Action, patrz
 * [com.lernhero.game.battle.BattleViewModel.spawnEffect]) sprawdza czy [EffectCatalog] ma dla niego
 * PNG. Jesli tak - odtwarza go na wlasciwym anchorze przez [EffectsController] (Animation/Event ->
 * Visual Effect). Jesli nie (katalog jeszcze pusty) - po prostu odhacza efekt jako pokazany, zeby
 * lista w stanie nie rosla w nieskonczonosc. Gra logiczna (ViewModel) nigdy nie dowiaduje sie o
 * pikselach ani anchorach - tylko o tym, ze jakis efekt ma sie wydarzyc na jakims celu.
 */
@Composable
private fun CombatEffectsCoordinator(
    characters: Map<String, CharacterUiModel>,
    floatingEffects: List<FloatingEffect>,
    characterRects: Map<String, CharacterRect>,
    effectsController: EffectsController,
    damageNumberController: DamageNumberController,
    onEffectShown: (Long) -> Unit
) {
    floatingEffects.forEach { effect ->
        key(effect.id) {
            LaunchedEffect(effect.id) {
                val targetRect = characterRects[effect.targetId]
                val targetCharacter = characters[effect.targetId]
                val hitPoint = if (targetRect != null && targetCharacter != null) {
                    targetRect.resolve(anchorsFor(targetCharacter).hitPoint)
                } else {
                    null
                }

                if (hitPoint != null) {
                    val label = when (effect.kind) {
                        // Garda ([EndlessBuff.Guard]) zatrzymuje cios w calosci - "-0" nic by nie mowilo.
                        HitKind.Block -> "BLOK"
                        HitKind.Heal -> "+${effect.amount}"
                        else -> "-${effect.amount}"
                    }
                    // Fire-and-forget: liczba zyje wlasnym zyciem (~950ms), nie moze blokowac
                    // reszty tej coroutine (ani onEffectShown ponizej).
                    launch { damageNumberController.show(hitPoint, label, effect.kind.damageNumberColor()) }
                }

                val spec = EffectCatalog.byHitKind[effect.kind]
                if (spec != null && hitPoint != null) {
                    val sourceRect = effect.sourceId?.let { characterRects[it] }
                    val sourceCharacter = effect.sourceId?.let { characters[it] }

                    if (spec.motion == EffectMotion.TRAVEL && sourceRect != null && sourceCharacter != null) {
                        val origin = sourceRect.resolve(anchorsFor(sourceCharacter).projectileOrigin)
                        effectsController.playTravel(origin, hitPoint, spec)
                    } else {
                        effectsController.playAt(hitPoint, spec)
                    }
                }

                onEffectShown(effect.id)
            }
        }
    }
}

@Composable
private fun TeamFormation(
    characters: List<CharacterUiModel>,
    isEnemySide: Boolean,
    stunnedIds: Set<String> = emptySet(),
    /** Ile ciosow blokuje jeszcze kazdy bohater - patrz [com.lernhero.domain.run.EndlessBuff.Guard]. */
    guardCharges: Map<String, Int> = emptyMap(),
    onRectMeasured: (String, CharacterRect) -> Unit,
    onAttackFinished: (String) -> Unit,
    onHitRecovered: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    BoxWithConstraints(
        modifier = modifier.padding(vertical = 8.dp)
    ) {
        val cardSize = calculateCardSize(maxWidth = maxWidth, maxHeight = maxHeight)
        val slots = formationSlots(isEnemySide)

        characters
            .mapIndexed { index, character -> character to slots[index % slots.size] }
            .sortedBy { (_, slot) -> slot.y }
            .forEach { (character, slot) ->
                // Kazda postac dostaje wlasny, przypiety do id "przedzial" kompozycji - inaczej gdy
                // fala sie zmienia (wrogowie umieraja, nowi dochodza), Compose potrafi podpiac stan
                // animacji (approach, lean) pod NIEWLASCIWA postac, bo trafil w to samo miejsce w
                // kolejnosci wywolan. Std brala sie ta "losowosc" efektow po zmianie fali.
                key(character.id) {
                    // Approach skacze wprost z 1f (poza ekranem) na 0f (na miejscu) - raz przy
                    // starcie walki (BattleViewModel.playBattleIntro: najpierw gracze, potem
                    // wrogowie), raz co fale dla nowych wrogow (staggerEnemyEntrance). To spring, nie
                    // tween - lekkie przestrzelenie na koncu ("wpada z impetem") wyglada bardziej
                    // efektownie niz plaskie zwolnienie, a wciaz ladnie sie zatrzymuje bez oscylacji.
                    val animatedApproach by animateFloatAsState(
                        targetValue = character.approach,
                        animationSpec = spring(dampingRatio = 0.65f, stiffness = 380f)
                    )
                    val spawnX = if (isEnemySide) ENEMY_SPAWN_X else PLAYER_SPAWN_X
                    val effectiveX = slot.x + animatedApproach * (spawnX - slot.x)

                    // Wjazd z lekkim "pop" - startuje troche pomniejszona i przezroczysta, rosnie
                    // do pelnego rozmiaru dokladnie w chwili ladowania na miejscu (animatedApproach=0).
                    val entrancePop = 1f - animatedApproach
                    val entranceScale = 0.78f + 0.22f * entrancePop
                    val entranceAlpha = (0.25f + 0.75f * entrancePop).coerceIn(0f, 1f)

                    CharacterSlot(
                        character = character,
                        cardSize = cardSize,
                        isEnemySide = isEnemySide,
                        isStunned = character.id in stunnedIds,
                        guardCharges = guardCharges[character.id] ?: 0,
                        onRectMeasured = onRectMeasured,
                        onAttackFinished = onAttackFinished,
                        onHitRecovered = onHitRecovered,
                        modifier = Modifier
                            .offset(
                                x = maxWidth * effectiveX - cardSize / 2,
                                y = maxHeight * slot.y - cardSize / 2
                            )
                            .graphicsLayer {
                                scaleX = entranceScale
                                scaleY = entranceScale
                                alpha = entranceAlpha
                            }
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
 * Wojownik stoi caly czas w swoim miejscu w formacji - atak to tylko krotka animacja matematyczna
 * na jego wlasnym PNG (i lekkie "szturchniecie" w strone wroga przez [lean]), nigdy przemieszczenie
 * karty. Tak dziala kazdy porzadny idle-battler: nikt nie biega przez ekran, zeby uderzyc.
 */
@Composable
private fun CharacterSlot(
    character: CharacterUiModel,
    cardSize: Dp,
    isEnemySide: Boolean,
    isStunned: Boolean = false,
    guardCharges: Int = 0,
    onRectMeasured: (String, CharacterRect) -> Unit,
    onAttackFinished: (String) -> Unit,
    onHitRecovered: (String) -> Unit,
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

    // Sam przelacznik "graj animacje ataku" - CZAS TRWANIA nie jest juz tutaj zgadywany (dawniej z
    // liczby klatek sprite'a). Animacja (ModularKnight/MonsterCharacter) sama zglasza koniec przez
    // onAttackAnimationFinished, patrz nizej.
    var attackTrigger by remember(character.id) { mutableStateOf(0) }
    LaunchedEffect(character.state) {
        when (character.state) {
            CharacterState.ATTACK -> attackTrigger += 1
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
            .zIndex(if (character.state != CharacterState.IDLE) 10f else 0f),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .offset { IntOffset(x = lean.toInt(), y = 0) },
            contentAlignment = Alignment.Center
        ) {
            if (isStunned) {
                DizzyStars(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .offset(y = cardSize * 0.06f / character.battleScale)
                )
            }
            if (guardCharges > 0 && character.hp > 0) {
                GuardShield(
                    charges = guardCharges,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .offset(y = cardSize * 0.06f / character.battleScale)
                )
            }
            val knightEquipment = character.knightEquipment
            if (knightEquipment != null) {
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
                        // battleScale normalises the hero to the same height as the other fighters.
                        sizeDp = cardSize * character.battleScale,
                        onAttackAnimationFinished = { onAttackFinished(character.id) },
                        modifier = Modifier.onGloballyPositioned { coordinates ->
                            onRectMeasured(
                                character.id,
                                CharacterRect(coordinates.positionInRoot(), coordinates.size, flipped = false)
                            )
                        }
                    )
                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .shake(character.hp),
                    contentAlignment = Alignment.BottomCenter
                ) {
                    MonsterCharacter(
                        preset = MonsterVisualPresets.forSpecies(character.avatarKey),
                        state = character.state,
                        attackTrigger = attackTrigger,
                        isEnemySide = isEnemySide,
                        sizeDp = cardSize * character.battleScale,
                        attackSpeed = BattleSpeed.multiplier,
                        onAttackAnimationFinished = { onAttackFinished(character.id) },
                        modifier = Modifier.onGloballyPositioned { coordinates ->
                            onRectMeasured(
                                character.id,
                                // BYLO odwrotnie: [MonsterCharacter.facing] odbija sprite (scaleX) gdy
                                // isEnemySide=FALSE (art rysowany patrzac w lewo - wrog stoi bez
                                // odbicia, sojusznik/companion dostaje odbicie, zeby patrzec w prawo).
                                // Anchor musial byc mirrorowany DOKLADNIE tam, gdzie mirrorowany jest
                                // sam sprite - stara wersja (flipped = isEnemySide) mirrorowala
                                // anchory dla WROGOW (ktorzy wizualnie NIE sa odbici) i zostawiala
                                // nieodbite anchory sojusznikom (ktorzy WIZUALNIE SA odbici) -
                                // dokladnie na odwrot. Std kazdy anchor odsuniety od 0.5 (np. hitPoint
                                // 0.605) trafial ~20px w zla strone.
                                CharacterRect(coordinates.positionInRoot(), coordinates.size, flipped = !isEnemySide)
                            )
                        }
                    )
                }
            }
        }
    }
}

/**
 * Powiekszony, przyblizony portret bossa podczas jego zaklecia ([BattleUiState.bossEvent]) - widac
 * tylko gorna czesc sylwetki (twarz/glowa), obcieta przez wasko przycieta ramke, otoczona pulsujaca
 * czerwona poswiata. Content jest znaczaco szerszy niz ramka i wyrownany do gory - to samo "kadrowanie",
 * ktorym fotografowie przybliozaja portret, tylko w Compose przez clip + oversized child.
 */
@Composable
private fun BossPortrait(boss: CharacterUiModel, modifier: Modifier = Modifier) {
    val pulse = rememberInfiniteTransition()
    val glow by pulse.animateFloat(
        initialValue = 0.45f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(900, easing = EaseInOutSine), RepeatMode.Reverse)
    )
    val frameShape = RoundedCornerShape(topStart = 10.dp, topEnd = 10.dp, bottomStart = 22.dp, bottomEnd = 22.dp)

    Box(
        modifier = modifier
            .size(width = 140.dp, height = 118.dp)
            .clip(frameShape)
            .background(
                Brush.verticalGradient(listOf(Color(0xFF3A140D), Color(0xFF160805)))
            )
            .border(2.dp, Color(0xFFE0654F).copy(alpha = glow), frameShape),
        contentAlignment = Alignment.TopCenter
    ) {
        Image(
            painter = painterResource(MonsterVisualPresets.forSpecies(boss.avatarKey).drawable),
            contentDescription = null,
            modifier = Modifier.width(210.dp).offset(y = (-6).dp)
        )
    }
}

/**
 * Tarcza nad bohaterem - tyle ciosow zatrzyma jeszcze jego garda ([com.lernhero.domain.run.EndlessBuff.Guard]/
 * [com.lernhero.domain.run.EndlessBuff.Aegis]). Bez tego znacznika blok wygladalby jak przypadkowe
 * "nic sie nie stalo" - a to jedno z niewielu ulepszen, ktorego dzialanie widac tylko wtedy, gdy
 * wrog akurat uderzy.
 */
@Composable
private fun GuardShield(charges: Int, modifier: Modifier = Modifier) {
    val infinite = rememberInfiniteTransition()
    val pulse by infinite.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(tween(900, easing = EaseInOutSine), RepeatMode.Reverse)
    )

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier.graphicsLayer {
            scaleX = pulse
            scaleY = pulse
        }
    ) {
        Text(text = "\uD83D\uDEE1", fontSize = 13.sp)
        if (charges > 1) {
            Text(
                text = "x$charges",
                color = Color(0xFFBFD4E8),
                fontSize = 10.sp
            )
        }
    }
}

/** "Krecioly nad glowa" - oszolomiona postac stoi bezczynnie, dopoki [BattleUiState.stunTurnsRemaining] nie wygasnie. */
@Composable
private fun DizzyStars(modifier: Modifier = Modifier) {
    val infinite = rememberInfiniteTransition()
    val angle by infinite.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(1400, easing = LinearEasing))
    )

    Text(
        text = "💫",
        fontSize = 16.sp,
        modifier = modifier.graphicsLayer { rotationZ = angle }
    )
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
            .height(4.dp)
            .clip(RoundedCornerShape(50))
            .background(Color(0xFF120C08).copy(alpha = 0.75f))
            .border(0.75.dp, Color.Black.copy(alpha = 0.55f), RoundedCornerShape(50))
            .padding(0.75.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(hpFraction)
                .fillMaxSize()
                .clip(RoundedCornerShape(50))
                .background(
                    if (hpFraction > 0.35f) {
                        Color(0xFF3DDC1F)
                    } else {
                        Color(0xFFFF2E1F)
                    }
                )
        )
    }
}

private fun calculateCardSize(
    maxWidth: Dp,
    maxHeight: Dp
): Dp {
    val widthBasedSize = maxWidth * 0.525f
    val heightBasedSize = maxHeight * 0.525f
    return minOf(widthBasedSize, heightBasedSize).coerceIn(55.dp, 105.dp)
}

/** Poza prawa krawedzia wlasnej polowki pola - stad wrogowie wchodza na ekran, wyraznie zza jego brzegu. */
private const val ENEMY_SPAWN_X = 1.7f

/** Lustrzane odbicie [ENEMY_SPAWN_X] - stad wchodza bohaterowie na poczatku walki (patrz BattleViewModel.playBattleIntro). */
private const val PLAYER_SPAWN_X = -0.7f

/** Jak dlugo swieci sie czerwona poswiata trafienia, zanim postac wraca do bezczynnosci. */
private const val HIT_FLASH_MILLIS = 260L
