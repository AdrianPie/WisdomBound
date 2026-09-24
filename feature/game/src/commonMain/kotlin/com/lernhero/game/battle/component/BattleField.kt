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
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.lernhero.game.character.CharacterState
import com.lernhero.game.battle.BattleSpeed
import com.lernhero.game.battle.BattleUiState
import com.lernhero.game.battle.FloatingEffect
import com.lernhero.game.battle.HERO_ID
import com.lernhero.game.battle.TurnPhase
import com.lernhero.domain.combat.playback.HitKind
import com.lernhero.game.battle.effects.CharacterRect
import com.lernhero.game.battle.effects.DamageNumberController
import com.lernhero.game.battle.effects.DamageNumbersOverlay
import com.lernhero.game.battle.effects.EffectCatalog
import com.lernhero.game.battle.effects.EffectMotion
import com.lernhero.game.battle.effects.EffectsController
import com.lernhero.game.battle.effects.EffectsOverlay
import com.lernhero.game.battle.effects.damageNumberColor
import com.lernhero.domain.monster.Element
import com.lernhero.game.character.CharacterUiModel
import com.lernhero.game.character.ModularKnight
import com.lernhero.game.character.MonsterCharacter
import com.lernhero.game.character.AnimSlot
import com.lernhero.game.character.MonsterAnimations
import com.lernhero.game.character.MonsterRigs
import com.lernhero.game.character.MonsterVisualPresets
import com.lernhero.game.character.RiggedMonster
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

    // Kamera/notch/pasek statusu potrafia siegac dobry kawalek w dol ekranu - HUD u gory
    // (numer fali, licznik pytan, baner komunikatu) NIE moze zaczynac sie od stalego 10 dp,
    // bo na takich telefonach wchodzi wprost pod wycięcie. Arena pod spodem zostaje fillMaxSize
    // (ma wygladac na pelnoekranowa), przesuwa sie tylko HUD, ktory na niej stoi.
    val topInset = WindowInsets.safeDrawing.asPaddingValues().calculateTopPadding()

    BoxWithConstraints(
        modifier = modifier.fillMaxSize()
    ) {
        // Tlo krainy tej walki (kampania) albo generyczne (endless/boss) - patrz
        // [BattleUiState.battleBackground]. FillBounds, bo pole bitwy to caly ekran, a nie waski
        // pasek podgladu - tu nie ma czego "przycinac od gory".
        Image(
            painter = painterResource(state.battleBackground),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.FillBounds
        )

        // Warstwy sceny zdarzenia bossa sa CELOWO rozbite na trzy przebiegi, zeby uzyskac glebie:
        //
        //   tlo areny  ->  NASZA DRUZYNA  ->  mgla + iskry  ->  BOSS  ->  liczby  ->  quiz
        //
        // Dzieki temu boss stoi na pierwszym planie, a wlasna druzyna tonie w mgle razem z tlem -
        // dokladnie tak, jak wyglada to w kadrach anime, gdy ktos laduje atak. Poza zdarzeniem
        // bossa wszystkie trzy przebiegi rysuja sie jeden po drugim i uklad jest nie do odroznienia
        // od zwyklego (te same wagi, ten sam padding).
        val bossEvent = state.bossEvent

        SideRow {
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

        if (bossEvent != null) {
            BossChargeBackdrop(event = bossEvent, modifier = Modifier.fillMaxSize())
        }

        SideRow(alignEnd = true) {
            TeamFormation(
                characters = enemies,
                isEnemySide = true,
                onRectMeasured = { id, rect -> characterRects[id] = rect },
                onAttackFinished = onAttackFinished,
                onHitRecovered = onHitRecovered,
                modifier = Modifier.fillMaxSize()
            )
        }

        // Warstwa efektow - calkowicie osobna od postaci, patrz com.lernhero.game.battle.effects.
        EffectsOverlay(controller = effectsController, modifier = Modifier.fillMaxSize())
        DamageNumbersOverlay(controller = damageNumberController, modifier = Modifier.fillMaxSize())
        // Mnozniki ataku NA TE TURE - od rozliczenia odpowiedzi do nastepnego pytania. Tabliczki
        // pokazuja je od razu przy kazdej postaci (patrz [UnitPlate]).
        val turnActive = state.turnPhase == TurnPhase.Impact || state.turnPhase == TurnPhase.Resolving
        val outcome = state.turnOutcome.takeIf { turnActive }
        HealthBarsOverlay(
            characters = allCharacters,
            characterRects = characterRects,
            boostedElements = state.boostedElements,
            playerAttackMultiplier = outcome?.damageMultiplier ?: 1f,
            enemyAttackMultiplier = if (outcome != null && !outcome.correct) com.lernhero.game.battle.WRONG_ANSWER_ENEMY_BONUS else 1f,
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

        // Gora ekranu nalezy do JEDNEJ rzeczy naraz. W trakcie zdarzenia bossa cala ta strefa jest
        // scena: stawka zaklecia, werdykt i to, co z niego wynika ([BossChargeOverlay]) - a numer
        // fali, licznik pytan, pasek ulepszen i baner komunikatu rysowaly sie DOKLADNIE pod nimi.
        // Nie tylko zaslanialy: kazda z tych rzeczy mowi o czyms innym niz to, na co gracz wlasnie
        // patrzy. Wracaja w tej samej klatce, w ktorej zdarzenie sie konczy.
        val bossSceneUp = bossEvent != null

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

        // Stan druzyny (buff/debuff cyklu, passa, ogluszenie) - tuz nad naszymi postaciami, po ich
        // stronie pola bitwy, zeby bylo widac przez cala walke, a nie tylko w chwili odpowiedzi.
        TeamStatusBar(
            state = state,
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(start = 10.dp, bottom = 8.dp, end = 60.dp)
        )

        // Rozliczenie ostatniej odpowiedzi - na srodku, w takcie [TurnPhase.Impact], czyli ZANIM
        // ktokolwiek uderzy. Znika w chwili, gdy rusza salwa, zeby nie zaslaniac samej walki.
        TurnImpactOverlay(
            outcome = state.turnOutcome,
            visible = state.turnPhase == TurnPhase.Impact,
            // Cala arena, nie sam srodek: nakladka gasi tlo pod soba, a to musi siegac krawedzi.
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
    boostedElements: Set<Element>,
    playerAttackMultiplier: Float,
    enemyAttackMultiplier: Float,
    modifier: Modifier = Modifier
) {
    var layerOrigin by remember { mutableStateOf(Offset.Zero) }
    val density = LocalDensity.current

    Box(
        modifier = modifier.onGloballyPositioned { layerOrigin = it.positionInRoot() }
    ) {
        characters.forEach { character ->
            val rect = characterRects[character.id] ?: return@forEach
            // Trup nie ma paska - po smierci zostawal pusty prostokat, bo martwe postacie zostaja
            // w mapie stanu (przygaszony sprite lezy dalej na ziemi).
            if (character.hp <= 0 || character.state == CharacterState.DEAD) return@forEach
            key(character.id) {
                val world = rect.resolve(anchorsFor(character).hpBarAnchor)
                val local = world - layerOrigin
                val barWidthPx = rect.size.width * 0.42f
                val barWidthDp = with(density) { barWidthPx.toDp() }
                // Zywiol przy kazdym, kto go ma - u naszych pulsuje, gdy kafel tematu go wzmocnil.
                val showBadge = character.element != null
                val boosted = character.isPlayer && character.element in boostedElements

                var plateSize by remember { mutableStateOf(IntSize.Zero) }
                UnitPlate(
                    character = character,
                    attackMultiplier = if (character.isPlayer) playerAttackMultiplier else enemyAttackMultiplier,
                    barWidth = barWidthDp,
                    elementBadge = if (showBadge) {
                        { ElementBadge(element = character.element!!, boosted = boosted) }
                    } else null,
                    modifier = Modifier
                        .onGloballyPositioned { plateSize = it.size }
                        .offset {
                            IntOffset(
                                // Wysrodkowana nad postacia; DOLNA krawedz na czubku glowy, wiec
                                // tabliczka nigdy nie przykrywa sylwetki.
                                x = (local.x - plateSize.width / 2f).roundToInt(),
                                y = (local.y - plateSize.height).roundToInt()
                            )
                        }
                )
            }
        }
    }
}

/**
 * Odznaka zywiolu przy pasku HP - jedna kropka w kolorze zywiolu, zeby gracz od razu widzial,
 * ktora jednostka jest jakiego typu (do tej pory bylo to zapisane tylko w danych).
 *
 * [boosted] = ten zywiol zostal wybrany kaflem tematu na te ture: odznaka pulsuje jasna poswiata,
 * a przy pierwszym wlaczeniu robi krotki "pop" - to jedyny sygnal, po ktorym gracz widzi, ze jego
 * wybor kategorii realnie wzmocnil TE konkretne postacie.
 */
@Composable
private fun ElementBadge(element: Element, boosted: Boolean) {
    val color = Color(element.colorHex)

    val pulse = rememberInfiniteTransition(label = "elementPulse")
    val glow by pulse.animateFloat(
        initialValue = if (boosted) 0.35f else 0f,
        targetValue = if (boosted) 1f else 0f,
        animationSpec = infiniteRepeatable(tween(760, easing = EaseInOutSine), RepeatMode.Reverse),
        label = "elementGlow"
    )
    // "Pop" przy wlaczeniu boosta - animateFloatAsState kluczowane na `boosted` rusza tylko przy zmianie.
    val pop by animateFloatAsState(
        targetValue = if (boosted) 1f else 0f,
        animationSpec = tween(320, easing = EaseInOutSine),
        label = "elementPop"
    )
    val scale = 1f + 0.35f * pop * (1f - pop) * 4f  // parabola 0->1->0, szczyt w polowie

    Box(
        modifier = Modifier
            .size(ELEMENT_BADGE_DP)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            },
        contentAlignment = Alignment.Center
    ) {
        if (boosted) {
            Box(
                modifier = Modifier
                    .size(ELEMENT_BADGE_DP)
                    .graphicsLayer { alpha = glow }
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.55f))
            )
        }
        com.lernhero.game.character.ElementEmblem(element = element, size = ELEMENT_BADGE_DP * 0.9f)
    }
}

private val ELEMENT_BADGE_DP = 16.dp


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
        FormationSlot(x = 0.50f, y = 0.28f),
        // Szoste miejsce - druzyna to bohater + pieciu kompanow (COMPANION_SLOTS), a fale wroga
        // tez dochodza do szesciu. Przy pieciu miejscach szosta postac staje DOKLADNIE na
        // pierwszej (slots[index % size]), bo indeks sie zawija.
        FormationSlot(x = 0.66f, y = 0.46f)
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
                        look = character.heroLook,
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
            } else if (MonsterRigs.forSpecies(character.avatarKey).isFlat.not()) {
                // Potwor z rigiem wieloczesciowym (np. boss-smok) - gra nazwane animacje
                // z [MonsterAnimations] zamiast matematycznego zamachu [MonsterCharacter].
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .shake(character.hp),
                    contentAlignment = Alignment.BottomCenter
                ) {
                    RiggedCharacter(
                        character = character,
                        cardSize = cardSize,
                        isEnemySide = isEnemySide,
                        onRectMeasured = onRectMeasured,
                        onAttackFinished = onAttackFinished
                    )
                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .shake(character.hp),
                    contentAlignment = Alignment.BottomCenter
                ) {
                    val preset = MonsterVisualPresets.forSpecies(character.avatarKey)
                    MonsterCharacter(
                        preset = preset,
                        state = character.state,
                        attackTrigger = attackTrigger,
                        isEnemySide = isEnemySide,
                        sizeDp = cardSize * character.battleScale,
                        // Boss (battleScale >> 1) ma prawo wyjsc poza kwadrat karty formacji.
                        unbounded = character.battleScale > 2.5f,
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
                                // Anchory odbijamy dokladnie tam, gdzie odbijany jest sprite - a to
                                // zalezy tez od tego, w ktora strone narysowano art (facesRight).
                                CharacterRect(coordinates.positionInRoot(), coordinates.size, flipped = preset.facing(isEnemySide) < 0f)
                            )
                        }
                    )
                }
            }
        }
    }
}

/**
 * Jedna polowa areny (lewa = druzyna, prawa = wrogowie) w pelnowymiarowym wierszu.
 *
 * Obie strony musza miec DOKLADNIE te same wagi i padding, bo sa rysowane w osobnych przebiegach
 * (patrz komentarz o warstwach w [BattleField]) - inaczej rozjechalyby sie wzgledem siebie.
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
 *
 * Playhead jedzie od 0 do 1 przez [MonsterAnimationClip.durationMs] i ZATRZYMUJE SIE na koncu -
 * animacje w tej grze sa jednorazowymi zagraniami ("otworz pysk i laduj pocisk"), a nie petlami;
 * po zakonczeniu poza ma zostac taka, jaka ustawila ostatnia klatka.
 *
 * Uwaga na [CharacterState.ATTACK]: ta sciezka MUSI zaraportowac koniec ciosu przez
 * [onAttackFinished], inaczej kolejka salwy ([BattleViewModel.startNextVolley]) czekalaby
 * w nieskonczonosc na animacje, ktora nigdy nie zglosila, ze sie skonczyla.
 */
@Composable
private fun RiggedCharacter(
    character: CharacterUiModel,
    cardSize: Dp,
    isEnemySide: Boolean,
    onRectMeasured: (String, CharacterRect) -> Unit,
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
        // Czas zgrany z czyms na ekranie (np. licznikiem quizu) idzie BEZ skalowania predkoscia
        // walki - inaczej animacja rozjechalaby sie z paskiem, ktory tez nie jest skalowany.
        val durationMs = character.animDurationMs
            ?: BattleSpeed.scale(clip.durationMs.toLong()).toInt()
        playhead.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMs, easing = LinearEasing)
        )
    }

    // Zamach bez wlasnej animacji rigu - zglaszamy koniec po stalym czasie, zeby salwa szla dalej.
    LaunchedEffect(character.state) {
        if (character.state == CharacterState.ATTACK) {
            // Sufit jest tu celowo: dlugi, efektowny klip nie moze zatrzymac calej salwy na
            // kilka sekund tylko dlatego, ze ktos podpial go pod gniazdo ataku.
            val attackClip = MonsterAnimations.clipFor(character.avatarKey, AnimSlot.Attack)
            val attackMs = (attackClip?.durationMs ?: RIGGED_ATTACK_FALLBACK_MS)
                .coerceAtMost(MAX_RIGGED_ATTACK_MS)
            delay(BattleSpeed.scale(attackMs.toLong()))
            onAttackFinished(character.id)
        }
    }

    // Najazd kamery na bossa podczas ladowania - plynny, zeby nie "skakal" w kadr.
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
                // Punkt zaczepienia w okolicy czaszki (gora-lewo sylwetki smoka), zeby najazd
                // wypychal na ekran GLOWE, a nie ogon.
                transformOrigin = TransformOrigin(0.28f, 0.34f)
            }
            .onGloballyPositioned { coordinates ->
                onRectMeasured(
                    character.id,
                    CharacterRect(coordinates.positionInRoot(), coordinates.size, flipped = !isEnemySide)
                )
            }
    )
}

/** Ile trwa cios rigowanego potwora, ktory nie ma wlasnej animacji ATAK. */
private const val RIGGED_ATTACK_FALLBACK_MS = 420

/** Twardy sufit na czekanie na animacje ciosu - salwa ma isc dalej nawet przy bardzo dlugim klipie. */
private const val MAX_RIGGED_ATTACK_MS = 1200

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
