package com.lernhero.game.battle.component

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.EaseInOutSine
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.lernhero.domain.combat.playback.HitKind
import com.lernhero.domain.monster.Element
import com.lernhero.game.battle.BattleSpeed
import com.lernhero.game.battle.BattleUiState
import com.lernhero.game.battle.GamePhase
import com.lernhero.game.battle.HERO_ID
import com.lernhero.game.battle.TurnPhase
import com.lernhero.game.battle.effects.CharacterRect
import com.lernhero.game.battle.fx.AttackStyle
import com.lernhero.game.battle.fx.AttackStyles
import com.lernhero.game.battle.fx.DamagePopupController
import com.lernhero.game.battle.fx.DamagePopupsLayer
import com.lernhero.game.battle.fx.ImpactType
import com.lernhero.game.battle.fx.VfxEngine
import com.lernhero.game.battle.fx.VfxLayer
import com.lernhero.game.battle.fx.popupLook
import com.lernhero.game.battle.fx.vfxColor
import com.lernhero.game.character.AnimSlot
import com.lernhero.game.character.CharacterState
import com.lernhero.game.character.CharacterUiModel
import com.lernhero.game.character.ModularKnight
import com.lernhero.game.character.MonsterAnimations
import com.lernhero.game.character.MonsterCharacter
import com.lernhero.game.character.MonsterRigs
import com.lernhero.game.character.MonsterVisualPresets
import com.lernhero.game.character.RiggedMonster
import com.lernhero.shared.resources.CombatAnchorSet
import com.lernhero.shared.resources.Resources
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.roundToInt
import kotlin.math.sign
import kotlin.math.sin

/**
 * Pole bitwy. Warstwy od dolu: tlo -> nasza druzyna -> (tlo zdarzenia bossa) -> wrogowie ->
 * efekty ([VfxLayer]) -> tabliczki -> wyskakujace liczby; nad tym HUD, ktory NIE trzesie sie
 * razem z arena.
 *
 * Wszystkie pozycje postaci ([CharacterRect]) sa w ukladzie ARENY, a nie calego ekranu: arena
 * trzesie sie przy mocnych ciosach, a gdyby pozycje liczyly sie od korzenia ekranu, kazda klatka
 * trzesienia przepisywalaby je wszystkie i przebudowywala tabliczki.
 */
@Composable
internal fun BattleField(
    state: BattleUiState,
    onAttackFinished: (String) -> Unit,
    onHitRecovered: (String) -> Unit,
    onEffectShown: (Long) -> Unit,
    onProjectileShown: (Long) -> Unit = {},
    onCapitulate: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val allCharacters = state.characters.values.toList()
    val enemies = allCharacters.filter { !it.isPlayer }.sortedBy { it.id }
    val players = allCharacters.filter { it.isPlayer }.sortedBy { it.id }

    val arena = remember { ArenaSpace() }
    val characterRects = remember { mutableStateMapOf<String, CharacterRect>() }
    val vfx = remember { VfxEngine() }
    val popups = remember { DamagePopupController() }
    val scope = rememberCoroutineScope()
    val shake = remember { Animatable(0f) }
    fun kick(amplitudeDp: Float, ms: Int) {
        scope.launch {
            shake.snapTo(max(amplitudeDp, shake.value))
            shake.animateTo(0f, tween(BattleSpeed.scale(ms), easing = LinearOutSlowInEasing))
        }
    }
    val measure: (String, CharacterRect) -> Unit = { id, rect -> if (characterRects[id] != rect) characterRects[id] = rect }
    var inspected by remember { mutableStateOf<String?>(null) }

    // Piec dobrych odpowiedzi z rzedu odpala ulti - cale pole bitwy mruga na zloto na chwile.
    val ultiFlash = remember { Animatable(0f) }
    LaunchedEffect(state.ultiPulseId) {
        if (state.ultiPulseId == 0L) return@LaunchedEffect
        ultiFlash.snapTo(0.5f)
        ultiFlash.animateTo(0f, animationSpec = tween(durationMillis = 550, easing = LinearEasing))
    }

    CombatFxCoordinator(
        state = state,
        rects = characterRects,
        vfx = vfx,
        popups = popups,
        kick = ::kick,
        onEffectShown = onEffectShown,
        onProjectileShown = onProjectileShown
    )

    // Kamera/notch potrafia siegac dobry kawalek w dol ekranu - HUD u gory musi sie od nich odsunac.
    val topInset = WindowInsets.safeDrawing.asPaddingValues().calculateTopPadding()

    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        // ----- ARENA (trzesie sie) -----
        Box(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    val amp = shake.value * density
                    if (amp > 0.05f) {
                        val t = VfxEngine.monoNow() / 1_000_000_000f
                        translationX = amp * (sin(t * 57f) + 0.5f * sin(t * 31f)) / 1.5f
                        translationY = amp * (cos(t * 49f) + 0.5f * sin(t * 23f)) / 1.5f
                    } else {
                        translationX = 0f
                        translationY = 0f
                    }
                }
        ) {
            Box(modifier = Modifier.fillMaxSize().onGloballyPositioned { arena.coords = it }) {
                Image(
                    painter = painterResource(state.battleBackground),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.FillBounds
                )

                val bossEvent = state.bossEvent
                val formationCallbacks = FormationCallbacks(
                    arena = arena,
                    onRectMeasured = measure,
                    onAttackFinished = onAttackFinished,
                    onHitRecovered = onHitRecovered,
                    onDust = { id, dir ->
                        characterRects[id]?.let { vfx.impact(ImpactType.Dust, it.feet(), Color.White, dir = dir) }
                    },
                    onLanded = { id, giant ->
                        characterRects[id]?.let { rect ->
                            if (giant) {
                                vfx.impact(ImpactType.BossLanding, rect.feet(), Color(0xFFFF6A3A))
                                kick(13f, 700)
                            } else {
                                vfx.impact(ImpactType.Landing, rect.feet(), Color.White, scale = rect.scaleHint())
                                kick(1.5f, 160)
                            }
                        }
                    },
                    onTap = { id -> inspected = if (inspected == id) null else id }
                )

                SideRow {
                    TeamFormation(
                        characters = players,
                        isEnemySide = false,
                        stunnedIds = state.stunnedCharacterIds,
                        guardCharges = state.guardCharges,
                        callbacks = formationCallbacks,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                if (bossEvent != null) {
                    BossChargeBackdrop(event = bossEvent, modifier = Modifier.fillMaxSize())
                }

                SideRow(alignEnd = true) {
                    TeamFormation(
                        characters = enemies,
                        isEnemySide = true,
                        callbacks = formationCallbacks,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                VfxLayer(engine = vfx, modifier = Modifier.fillMaxSize())

                val turnActive = state.turnPhase == TurnPhase.Impact || state.turnPhase == TurnPhase.Resolving
                val outcome = state.turnOutcome.takeIf { turnActive }
                PlatesOverlay(
                    characters = allCharacters,
                    characterRects = characterRects,
                    boostedElements = state.boostedElements,
                    playerAttackMultiplier = outcome?.damageMultiplier ?: 1f,
                    enemyAttackMultiplier = if (outcome != null && !outcome.correct) com.lernhero.game.battle.WRONG_ANSWER_ENEMY_BONUS else 1f,
                    modifier = Modifier.fillMaxSize()
                )

                DamagePopupsLayer(controller = popups, modifier = Modifier.fillMaxSize())

                val inspectedCharacter = inspected?.let { state.characters[it] }?.takeIf { it.hp > 0 }
                val inspectedRect = inspected?.let { characterRects[it] }
                if (inspectedCharacter != null && inspectedRect != null) {
                    InspectCard(
                        character = inspectedCharacter,
                        anchor = inspectedRect,
                        onDismiss = { inspected = null },
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }

        // ----- HUD (nie trzesie sie) -----
        if (ultiFlash.value > 0f) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFFFFD100).copy(alpha = ultiFlash.value))
            )
        }

        val hero = state.characters[HERO_ID]
        val heroHpFraction = if (hero != null && hero.maxHp > 0) hero.hp.toFloat() / hero.maxHp.toFloat() else 1f
        val bossSceneUp = state.bossEvent != null

        if (!bossSceneUp) {
            WaveBadge(
                wave = state.wave,
                heroHpFraction = heroHpFraction,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(start = 10.dp, top = topInset + 10.dp, end = 10.dp, bottom = 10.dp)
            )
            EndlessBuffRow(
                buffs = state.endlessBuffs,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(start = 10.dp, top = topInset + 66.dp, end = 90.dp)
            )
        }

        TeamStatusBar(
            state = state,
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(start = 10.dp, bottom = 8.dp, end = 60.dp)
        )

        TurnImpactOverlay(
            outcome = state.turnOutcome,
            visible = state.turnPhase == TurnPhase.Impact,
            modifier = Modifier.fillMaxSize()
        )

        if (!bossSceneUp) {
            AnswerCountBadge(
                count = state.answeredCount,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(start = 10.dp, top = topInset + 10.dp, end = 10.dp, bottom = 10.dp)
            )
        }

        BattleMessageBanner(
            message = state.message.takeUnless { bossSceneUp },
            buffLabel = state.activeCycleBuff?.takeIf { state.elapsedMs < it.expiresAtMs }?.outcome?.label,
            buffPositive = (state.activeCycleBuff?.outcome?.attackMultiplier ?: 1f) >= 1f,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = topInset + 56.dp)
        )

        ArenaBanners(state = state, enemies = enemies, modifier = Modifier.fillMaxSize())

        BattleControls(
            phase = state.phase,
            onCapitulate = onCapitulate,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(10.dp)
        )
    }
}

/** Uklad wspolrzednych areny - pozycje postaci liczone wzgledem niego nie zmieniaja sie przy trzesieniu. */
private class ArenaSpace {
    var coords: LayoutCoordinates? = null

    fun local(child: LayoutCoordinates): Offset {
        val root = coords
        return if (root != null && root.isAttached && child.isAttached) root.localPositionOf(child, Offset.Zero)
        else child.positionInRoot()
    }
}

private fun CharacterRect.center() = Offset(topLeft.x + size.width / 2f, topLeft.y + size.height / 2f)
private fun CharacterRect.feet() = Offset(topLeft.x + size.width / 2f, topLeft.y + size.height * 0.92f)

/** Wielkosc postaci wzgledem zwyklej (~90 px na dp=1) - efekty skaluja sie z celem. */
private fun CharacterRect.scaleHint(): Float = (size.height / 220f).coerceIn(0.7f, 2.4f)

/** Ktory [CombatAnchorSet] pasuje do danej postaci - hero ma swoj staly, monstery jeden na gatunek. */
private fun anchorsFor(character: CharacterUiModel): CombatAnchorSet =
    if (character.knightEquipment != null) Resources.KnightCombatAnchors
    else MonsterVisualPresets.forSpecies(character.avatarKey).anchors

private fun styleOf(character: CharacterUiModel): AttackStyle =
    AttackStyles.of(character.avatarKey, character.knightEquipment != null, character.battleScale)

/**
 * Dyrygent efektow: zamienia zdarzenia z view modelu (pocisk wystrzelony, trafienie, leczenie,
 * blok...) na obraz - pocisk w locie, wybuch na celu, wyskakujaca liczbe i trzesienie. Kazdy
 * rodzaj ciosu wyglada inaczej: miecz tnie, strzala wbija sie z drzazgami, kula zywiolu wybucha
 * w swoim kolorze, olbrzym rozbija ziemie.
 */
@Composable
private fun CombatFxCoordinator(
    state: BattleUiState,
    rects: Map<String, CharacterRect>,
    vfx: VfxEngine,
    popups: DamagePopupController,
    kick: (Float, Int) -> Unit,
    onEffectShown: (Long) -> Unit,
    onProjectileShown: (Long) -> Unit
) {
    state.projectiles.forEach { event ->
        key(event.id) {
            LaunchedEffect(event.id) {
                val source = state.characters[event.sourceId]
                val target = state.characters[event.targetId]
                val sourceRect = rects[event.sourceId]
                val targetRect = rects[event.targetId]
                if (source != null && target != null && sourceRect != null && targetRect != null) {
                    val from = sourceRect.resolve(anchorsFor(source).projectileOrigin)
                    val to = targetRect.resolve(anchorsFor(target).hitPoint)
                    vfx.projectile(event.kind, from, to, event.flightMs, source.element)
                }
                onProjectileShown(event.id)
            }
        }
    }

    state.floatingEffects.forEach { effect ->
        key(effect.id) {
            LaunchedEffect(effect.id) {
                val target = state.characters[effect.targetId]
                val rect = rects[effect.targetId]
                if (target != null && rect != null) {
                    playImpact(effect.kind, effect.amount, target, rect, effect.sourceId?.let { state.characters[it] }, vfx, popups, kick)
                }
                onEffectShown(effect.id)
            }
        }
    }

    // Smierc: wybuch w kolorze zywiolu, dusza w gore i kurz - w chwili, w ktorej HP spada do zera.
    val deadIds = state.characters.values.filter { it.hp <= 0 }.map { it.id }.toSet()
    var knownDead by remember { mutableStateOf(emptySet<String>()) }
    LaunchedEffect(deadIds) {
        (deadIds - knownDead).forEach { id ->
            val character = state.characters[id] ?: return@forEach
            val rect = rects[id] ?: return@forEach
            vfx.impact(ImpactType.Death, rect.center(), character.element.vfxColor(), scale = rect.scaleHint(), element = character.element)
            kick(if (character.battleScale > AttackStyles.GIANT_SCALE) 10f else 3f, 380)
        }
        knownDead = deadIds
    }
}

private fun playImpact(
    kind: HitKind,
    amount: Int,
    target: CharacterUiModel,
    rect: CharacterRect,
    source: CharacterUiModel?,
    vfx: VfxEngine,
    popups: DamagePopupController,
    kick: (Float, Int) -> Unit
) {
    val anchors = anchorsFor(target)
    val hit = rect.resolve(anchors.hitPoint)
    val body = rect.resolve(anchors.effectAnchor)
    val dir = when {
        source != null -> if (source.isPlayer) 1f else -1f
        else -> if (target.isPlayer) -1f else 1f
    }
    val scale = rect.scaleHint()
    val crit = kind == HitKind.Crit
    val style = source?.let(::styleOf)
    val giantSource = (source?.battleScale ?: 1f) > AttackStyles.GIANT_SCALE

    when (kind) {
        HitKind.Heal -> vfx.impact(ImpactType.Heal, body, Color(0xFF6BF08A), scale = scale)
        HitKind.Shield -> vfx.impact(ImpactType.Shield, body, Color(0xFF7FC8FF), scale = scale)
        HitKind.Block -> vfx.impact(ImpactType.Block, hit, Color(0xFF9FD8FF), dir = dir, scale = scale)
        HitKind.Dodge -> vfx.impact(ImpactType.Dodge, hit, Color.White, dir = dir, scale = scale)
        HitKind.Reflect -> vfx.impact(ImpactType.Thorns, hit, Color(0xFFFF4A3A), dir = -dir, scale = scale)
        HitKind.Poison ->
            if (source == null) vfx.impact(ImpactType.Poison, body, Color(0xFF8BE03A), scale = scale)
            else {
                vfx.impact(ImpactType.Burst, hit, Color(0xFF8BE03A), dir = dir, scale = scale, element = Element.Nature)
                vfx.impact(ImpactType.Poison, body, Color(0xFF8BE03A), scale = scale)
            }
        HitKind.Burn -> vfx.impact(ImpactType.Burst, hit, Color(0xFFFF7A1A), dir = dir, scale = scale * 1.2f, element = Element.Fire)
        HitKind.Skill ->
            if (source == null) {
                vfx.impact(ImpactType.SkyStrike, hit, Color(0xFFFFD34D), scale = scale)
                kick(5f, 360)
            } else {
                vfx.impact(ImpactType.Burst, hit, Color(0xFFB98CFF), dir = dir, scale = scale * 1.15f)
            }
        HitKind.Hit, HitKind.Crit -> if (source == null) {
            vfx.impact(ImpactType.Burst, hit, Color(0xFFFF8A5A), dir = dir, scale = scale, crit = crit, element = Element.Fire)
        } else when (styleOf(source)) {
            AttackStyle.Melee -> vfx.impact(
                ImpactType.Slash, hit,
                if (source.knightEquipment != null) Color(0xFFFFE08A) else source.element.vfxColor(),
                dir = dir, scale = scale, crit = crit
            )
            AttackStyle.Arrow -> vfx.impact(ImpactType.Pierce, hit, source.element?.vfxColor() ?: Color.White, dir = dir, scale = scale, crit = crit)
            AttackStyle.Bolt -> vfx.impact(ImpactType.Burst, hit, source.element.vfxColor(), dir = dir, scale = scale, crit = crit, element = source.element)
            AttackStyle.Smash -> vfx.impact(ImpactType.Smash, hit, source.element.vfxColor(), dir = dir, scale = scale, crit = crit)
        }
    }

    val label = when (kind) {
        HitKind.Block -> "BLOCK"
        HitKind.Dodge -> "MISS"
        HitKind.Shield -> "SHIELD"
        HitKind.Heal -> "+$amount"
        else -> "-$amount"
    }
    popups.show(target.id, Offset(hit.x, hit.y - 18f), label, kind.popupLook())

    when {
        crit -> kick(6f, 300)
        style == AttackStyle.Smash || giantSource -> kick(7f, 360)
        kind == HitKind.Burn -> kick(3f, 220)
    }
}

/* ============================================================== formacja */

/** Wszystko, co formacja zglasza wyzej - w jednym miejscu, zeby nie ciagnac osmiu lambd przez trzy poziomy. */
private class FormationCallbacks(
    val arena: ArenaSpace,
    val onRectMeasured: (String, CharacterRect) -> Unit,
    val onAttackFinished: (String) -> Unit,
    val onHitRecovered: (String) -> Unit,
    /** Kurz spod stop w biegu; [dir] = kierunek biegu. */
    val onDust: (String, Float) -> Unit,
    /** Postac dobiegla na miejsce (albo olbrzym spadl z nieba). */
    val onLanded: (String, Boolean) -> Unit,
    val onTap: (String) -> Unit
)

@Composable
private fun TeamFormation(
    characters: List<CharacterUiModel>,
    isEnemySide: Boolean,
    callbacks: FormationCallbacks,
    stunnedIds: Set<String> = emptySet(),
    /** Ile ciosow blokuje jeszcze kazdy bohater - patrz [com.lernhero.domain.run.EndlessBuff.Guard]. */
    guardCharges: Map<String, Int> = emptyMap(),
    modifier: Modifier = Modifier
) {
    BoxWithConstraints(modifier = modifier.padding(vertical = 8.dp)) {
        // Pelna druzyna w zygzaku ciasno zachodzi na siebie - przy 5-6 postaciach kazda jest mniejsza.
        val crowding = when {
            characters.size >= 5 -> 0.8f
            characters.size == 4 -> 0.9f
            else -> 1f
        }
        val cardSize = calculateCardSize(maxWidth = maxWidth, maxHeight = maxHeight) * crowding
        val fieldHeight = maxHeight
        formationSlots(characters, isEnemySide)
            .sortedBy { (_, slot) -> slot.y }
            .forEach { (character, slot) ->
                // Kazda postac dostaje wlasny, przypiety do id "przedzial" kompozycji - inaczej po
                // zmianie fali stan animacji potrafil trafic pod NIEWLASCIWA postac.
                key(character.id) {
                    val giant = character.battleScale > AttackStyles.GIANT_SCALE
                    val animatedApproach by animateFloatAsState(
                        targetValue = character.approach,
                        animationSpec = if (giant) spring(dampingRatio = 0.5f, stiffness = 260f)
                        else spring(dampingRatio = 0.65f, stiffness = 380f)
                    )

                    // Wejscie na arene: zwykli wbiegaja z boku z kurzem spod nog, olbrzym SPADA
                    // z nieba i rozbija ziemie.
                    var away by remember { mutableStateOf(character.approach > 0f) }
                    LaunchedEffect(character.approach) {
                        if (character.approach > 0f) {
                            away = true
                            return@LaunchedEffect
                        }
                        if (!away) return@LaunchedEffect
                        away = false
                        if (giant) {
                            delay(BattleSpeed.scale(430L))
                            callbacks.onLanded(character.id, true)
                        } else {
                            val run = if (isEnemySide) -1f else 1f
                            repeat(4) {
                                delay(BattleSpeed.scale(70L))
                                callbacks.onDust(character.id, run)
                            }
                            delay(BattleSpeed.scale(120L))
                            callbacks.onLanded(character.id, false)
                        }
                    }

                    val spawnX = if (isEnemySide) ENEMY_SPAWN_X else PLAYER_SPAWN_X
                    val effectiveX = if (giant) slot.x else slot.x + animatedApproach * (spawnX - slot.x)
                    val dropY = if (giant) -animatedApproach * 1.25f else 0f
                    val entrancePop = 1f - animatedApproach
                    val entranceScale = if (giant) 1f else 0.78f + 0.22f * entrancePop
                    val entranceAlpha = if (giant) 1f else (0.25f + 0.75f * entrancePop).coerceIn(0f, 1f)

                    CharacterSlot(
                        character = character,
                        cardSize = cardSize,
                        isEnemySide = isEnemySide,
                        isStunned = character.id in stunnedIds,
                        guardCharges = guardCharges[character.id] ?: 0,
                        callbacks = callbacks,
                        modifier = Modifier
                            .offset(
                                x = maxWidth * effectiveX - cardSize / 2,
                                y = fieldHeight * (slot.y + dropY) - cardSize / 2
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

private data class FormationSlot(val x: Float, val y: Float)

/**
 * Zygzak przy boku ekranu: postacie ida od gory do dolu, na przemian blizej i dalej od krawedzi,
 * wiec nikt nie stoi na srodku pola i nikt nikogo nie zaslania. Najwieksi stoja na samej gorze.
 */
private fun formationSlots(
    characters: List<CharacterUiModel>,
    isRightSide: Boolean
): List<Pair<CharacterUiModel, FormationSlot>> {
    val ordered = characters.sortedByDescending { character ->
        val species = if (character.knightEquipment != null) 1f
        else MonsterVisualPresets.forSpecies(character.avatarKey).visualScale
        character.battleScale * species
    }
    val count = ordered.size
    return ordered.mapIndexed { index, character ->
        val t = if (count <= 1) 0.5f else index / (count - 1f)
        val x = if (index % 2 == 0) ZIGZAG_INNER_X else ZIGZAG_OUTER_X
        val y = ZIGZAG_TOP_Y + (ZIGZAG_BOTTOM_Y - ZIGZAG_TOP_Y) * t
        character to FormationSlot(x = if (isRightSide) 1f - x else x, y = y)
    }
}

private const val ZIGZAG_INNER_X = 0.52f
private const val ZIGZAG_OUTER_X = 0.24f
private const val ZIGZAG_TOP_Y = 0.40f
private const val ZIGZAG_BOTTOM_Y = 0.74f

/**
 * Jedna postac w formacji. Nikt nie biega po polu: atakujacy lekko sie wysuwa, a jego cios leci
 * jako pocisk (fala ciecia, strzala, kula zywiolu - patrz [CombatFxCoordinator]). Trafiony cel
 * dostaje ODRZUT w strone od napastnika i wraca na miejsce.
 */
@Composable
private fun CharacterSlot(
    character: CharacterUiModel,
    cardSize: Dp,
    isEnemySide: Boolean,
    isStunned: Boolean = false,
    guardCharges: Int = 0,
    callbacks: FormationCallbacks,
    modifier: Modifier = Modifier
) {
    val density = LocalDensity.current
    val leanPx = with(density) { (cardSize * 0.12f).toPx() }
    // Atak to tylko lekkie wysuniecie w strone wroga - nikt nie podbiega, bo wszyscy strzelaja.
    val lean by animateFloatAsState(
        targetValue = if (character.state == CharacterState.ATTACK) {
            if (character.isPlayer) leanPx else -leanPx
        } else 0f,
        animationSpec = tween(durationMillis = BattleSpeed.scale(90), easing = LinearEasing)
    )
    val knock = remember { Animatable(0f) }

    // Sam przelacznik "graj animacje ataku" - czas trwania zglasza animacja przez onAttackAnimationFinished.
    var attackTrigger by remember(character.id) { mutableStateOf(0) }
    LaunchedEffect(character.state) {
        when (character.state) {
            CharacterState.ATTACK -> attackTrigger += 1
            CharacterState.HIT -> {
                // Odrzut w strone od napastnika i powrot na miejsce.
                launch {
                    knock.snapTo(1f)
                    knock.animateTo(0f, tween(BattleSpeed.scale(280), easing = FastOutSlowInEasing))
                }
                delay(BattleSpeed.scale(HIT_FLASH_MILLIS))
                callbacks.onHitRecovered(character.id)
            }
            else -> Unit
        }
    }
    val attackDone: () -> Unit = { callbacks.onAttackFinished(character.id) }

    val knockPx = with(density) { 10.dp.toPx() } * (if (character.isPlayer) -1f else 1f)
    val noRipple = remember { MutableInteractionSource() }
    val measured: (LayoutCoordinates, Boolean) -> Unit = { coordinates, flipped ->
        callbacks.onRectMeasured(
            character.id,
            CharacterRect(callbacks.arena.local(coordinates), coordinates.size, flipped = flipped)
        )
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
                .graphicsLayer { translationX = lean + knockPx * knock.value }
                .clickable(interactionSource = noRipple, indication = null) { callbacks.onTap(character.id) },
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
                    modifier = Modifier.fillMaxSize().shake(character.hp),
                    contentAlignment = Alignment.BottomCenter
                ) {
                    ModularKnight(
                        equipment = knightEquipment,
                        look = character.heroLook,
                        attackTrigger = attackTrigger,
                        interactive = false,
                        attackSpeed = BattleSpeed.multiplier,
                        sizeDp = cardSize * character.battleScale,
                        onAttackAnimationFinished = attackDone,
                        modifier = Modifier.onGloballyPositioned { measured(it, false) }
                    )
                }
            } else if (MonsterRigs.forSpecies(character.avatarKey).isFlat.not()) {
                Box(
                    modifier = Modifier.fillMaxSize().shake(character.hp),
                    contentAlignment = Alignment.BottomCenter
                ) {
                    RiggedCharacter(
                        character = character,
                        cardSize = cardSize,
                        isEnemySide = isEnemySide,
                        onMeasured = measured,
                        onAttackFinished = { callbacks.onAttackFinished(it) }
                    )
                }
            } else {
                Box(
                    modifier = Modifier.fillMaxSize().shake(character.hp),
                    contentAlignment = Alignment.BottomCenter
                ) {
                    val preset = MonsterVisualPresets.forSpecies(character.avatarKey)
                    MonsterCharacter(
                        preset = preset,
                        state = character.state,
                        attackTrigger = attackTrigger,
                        isEnemySide = isEnemySide,
                        sizeDp = cardSize * character.battleScale,
                        unbounded = character.battleScale > 2.5f,
                        attackSpeed = BattleSpeed.multiplier,
                        onAttackAnimationFinished = attackDone,
                        // Klasa wielkosci gatunku moze byc wieksza od karty - rosnie w gore, stopy na ziemi.
                        modifier = Modifier
                            .let { if (character.battleScale > 2.5f) it else it.wrapContentSize(Alignment.BottomCenter, unbounded = true) }
                            // Anchory odbijamy dokladnie tam, gdzie odbijany jest sprite.
                            .onGloballyPositioned { measured(it, preset.facing(isEnemySide) < 0f) }
                    )
                }
            }
        }
    }
}

/**
 * Jedna polowa areny (lewa = druzyna, prawa = wrogowie) w pelnowymiarowym wierszu. Obie strony
 * maja DOKLADNIE te same wagi i padding, bo sa rysowane w osobnych przebiegach.
 */
@Composable
private fun SideRow(
    alignEnd: Boolean = false,
    content: @Composable () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 12.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (alignEnd) Box(modifier = Modifier.weight(1f).fillMaxSize())
        Box(modifier = Modifier.weight(1f).fillMaxSize()) { content() }
        if (!alignEnd) Box(modifier = Modifier.weight(1f).fillMaxSize())
    }
}

/**
 * Potwor rysowany z rigu ([MonsterRigs]) i odtwarzajacy nazwane animacje ([MonsterAnimations]).
 * Ta sciezka MUSI zaraportowac koniec ciosu przez [onAttackFinished], inaczej salwa czekalaby
 * w nieskonczonosc.
 */
@Composable
private fun RiggedCharacter(
    character: CharacterUiModel,
    cardSize: Dp,
    isEnemySide: Boolean,
    onMeasured: (LayoutCoordinates, Boolean) -> Unit,
    onAttackFinished: (String) -> Unit
) {
    val rig = remember(character.avatarKey) { MonsterRigs.forSpecies(character.avatarKey) }
    val preset = remember(character.avatarKey) { MonsterVisualPresets.forSpecies(character.avatarKey) }
    val clip = remember(character.avatarKey, character.animSlot) {
        character.animSlot?.let { MonsterAnimations.clipFor(character.avatarKey, it) }
    }

    val playhead = remember { Animatable(0f) }
    LaunchedEffect(character.animTrigger, clip, character.animDurationMs) {
        if (clip == null) {
            playhead.snapTo(0f)
            return@LaunchedEffect
        }
        playhead.snapTo(0f)
        val durationMs = character.animDurationMs
            ?: BattleSpeed.scale(clip.durationMs.toLong()).toInt()
        playhead.animateTo(targetValue = 1f, animationSpec = tween(durationMs, easing = LinearEasing))
    }

    LaunchedEffect(character.state) {
        if (character.state == CharacterState.ATTACK) {
            val attackClip = MonsterAnimations.clipFor(character.avatarKey, AnimSlot.Attack)
            val attackMs = (attackClip?.durationMs ?: RIGGED_ATTACK_FALLBACK_MS).coerceAtMost(MAX_RIGGED_ATTACK_MS)
            delay(BattleSpeed.scale(attackMs.toLong()))
            onAttackFinished(character.id)
        }
    }

    val zoom by animateFloatAsState(
        targetValue = character.cameraZoom,
        animationSpec = tween(durationMillis = 900, easing = FastOutSlowInEasing),
        label = "bossCameraZoom"
    )

    RiggedMonster(
        rig = rig,
        preset = preset,
        clip = clip,
        progress = playhead.value,
        sizeDp = cardSize * character.battleScale,
        isEnemySide = isEnemySide,
        unbounded = character.battleScale > 2.5f,
        effectTintOverride = character.animEffectTintArgb?.let { Color(it) },
        modifier = Modifier
            .graphicsLayer {
                scaleX = zoom
                scaleY = zoom
                transformOrigin = TransformOrigin(0.28f, 0.34f)
            }
            .onGloballyPositioned { onMeasured(it, !isEnemySide) }
    )
}

private const val RIGGED_ATTACK_FALLBACK_MS = 420
private const val MAX_RIGGED_ATTACK_MS = 1200

/** Tarcza nad bohaterem - tyle ciosow zatrzyma jeszcze jego garda. */
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
        Text(text = "🛡", fontSize = 13.sp)
        if (charges > 1) {
            Text(text = "x$charges", color = Color(0xFFBFD4E8), fontSize = 10.sp)
        }
    }
}

/** "Krecioly nad glowa" - oszolomiona postac stoi bezczynnie. */
@Composable
private fun DizzyStars(modifier: Modifier = Modifier) {
    val infinite = rememberInfiniteTransition()
    val angle by infinite.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(1400, easing = LinearEasing))
    )
    Text(text = "💫", fontSize = 16.sp, modifier = modifier.graphicsLayer { rotationZ = angle })
}

private fun calculateCardSize(maxWidth: Dp, maxHeight: Dp): Dp {
    val widthBasedSize = maxWidth * 0.45f
    val heightBasedSize = maxHeight * 0.45f
    return minOf(widthBasedSize, heightBasedSize).coerceIn(48.dp, 90.dp)
}

/** Poza prawa krawedzia wlasnej polowki pola - stad wrogowie wchodza na ekran. */
private const val ENEMY_SPAWN_X = 1.7f

/** Lustrzane odbicie [ENEMY_SPAWN_X] - stad wchodza bohaterowie na poczatku walki. */
private const val PLAYER_SPAWN_X = -0.7f

/** Jak dlugo trwa reakcja na trafienie, zanim postac wraca do bezczynnosci. */
private const val HIT_FLASH_MILLIS = 260L

/* ============================================================== tabliczki */

/**
 * Tabliczki nad glowami: poziom, pasek HP i zywiol - i nic wiecej. Pelne statystyki (HP w liczbach,
 * atak, pancerz, moc bierna) sa pod tapnieciem w postac ([InspectCard]); na tabliczce przy szesciu
 * potworach po stronie byly nieczytelnym szumem.
 */
@Composable
private fun PlatesOverlay(
    characters: List<CharacterUiModel>,
    characterRects: Map<String, CharacterRect>,
    boostedElements: Set<Element>,
    playerAttackMultiplier: Float,
    enemyAttackMultiplier: Float,
    modifier: Modifier = Modifier
) {
    var layerWidth by remember { mutableStateOf(0) }
    Box(modifier = modifier.onGloballyPositioned { layerWidth = it.size.width }) {
        characters.forEach { character ->
            val rect = characterRects[character.id] ?: return@forEach
            if (character.hp <= 0 || character.state == CharacterState.DEAD) return@forEach
            // Postac jeszcze nie weszla na arene - tabliczka przyklejona do krawedzi ekranu
            // wisialaby w powietrzu nad niczym.
            if (character.approach > 0.01f) return@forEach
            key(character.id) {
                val anchor = rect.resolve(anchorsFor(character).hpBarAnchor)
                var plateSize by remember { mutableStateOf(IntSize.Zero) }
                UnitPlate(
                    character = character,
                    attackMultiplier = if (character.isPlayer) playerAttackMultiplier else enemyAttackMultiplier,
                    boosted = character.isPlayer && character.element in boostedElements,
                    modifier = Modifier
                        .onGloballyPositioned { plateSize = it.size }
                        .offset {
                            IntOffset(
                                x = (anchor.x - plateSize.width / 2f).roundToInt()
                                    .coerceIn(0, (layerWidth - plateSize.width).coerceAtLeast(0)),
                                y = (anchor.y - plateSize.height).roundToInt()
                            )
                        }
                )
            }
        }
    }
}
