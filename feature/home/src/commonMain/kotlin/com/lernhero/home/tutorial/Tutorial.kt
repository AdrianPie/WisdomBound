package com.lernhero.home.tutorial

import com.lernhero.shared.resources.Resources
import androidx.compose.foundation.Image
import org.jetbrains.compose.resources.painterResource
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.ClipOp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lernhero.home.ui.ChunkyButton
import com.lernhero.shared.i18n.AppStrings
import com.lernhero.shared.i18n.strings
import com.lernhero.shared.prefs.AppPreferences
import com.lernhero.shared.theme.FontFirst
import com.lernhero.shared.theme.GlyphText
import kotlin.math.roundToInt

/**
 * Samouczek nowego gracza: medrzec prowadzi za reke od pustej postaci do pierwszej walki -
 * darmowy summon, towarzysz w druzynie, miecz w reku i START.
 *
 * Jak to dziala:
 *  - kazdy podswietlany przycisk nosi [tutorialTarget] - podaje swoje polozenie i sam zglasza
 *    tapniecie, wiec ekrany nie musza nic wiedziec o samouczku poza tym jednym modyfikatorem,
 *  - [TutorialOverlay] przyciemnia ekran z dziura na biezacy cel i blokuje dotyk wszedzie poza nia,
 *  - krok idzie dalej po tapnieciu celu ([Advance.Tap]) albo gdy spelni sie warunek stanu gry
 *    ([Advance.State] - towarzysz jest w druzynie, miecz jest zalozony), sprawdzany w [TutorialHost].
 *
 * Krok jest zapisany w [AppPreferences.tutorialStep] - zamkniecie apki w polowie wraca tam, gdzie
 * gracz skonczyl. Nazwa pierwszego wpisu musi zgadzac sie z [AppPreferences.TUTORIAL_FIRST_STEP].
 */
enum class TutorialStep(val target: TutorialTarget?, val advance: Advance, val text: AppStrings.() -> String) {
    Welcome(null, Advance.Button, { "" }),
    SummonAltar(TutorialTarget.SummonAltar, Advance.Tap, { tutorialSummonAltar }),
    FreeSummon(TutorialTarget.FreeSummon, Advance.Tap, { tutorialFreeSummon }),
    LeaveSummon(TutorialTarget.TownBack, Advance.Tap, { tutorialLeaveSummon }),
    FightTab(TutorialTarget.NavFight, Advance.Tap, { tutorialFightTab }),
    OpenParty(TutorialTarget.PartyButton, Advance.Tap, { tutorialOpenParty }),
    PlaceMonster(TutorialTarget.ReserveMonster, Advance.State, { tutorialPlaceMonster }),
    CloseParty(TutorialTarget.PartyClose, Advance.Tap, { tutorialCloseParty }),
    OpenHero(TutorialTarget.Portrait, Advance.Tap, { tutorialOpenHero }),
    WeaponSlot(TutorialTarget.WeaponSlot, Advance.Tap, { tutorialWeaponSlot }),
    EquipSword(TutorialTarget.EquipButton, Advance.State, { tutorialEquip }),
    CloseHero(TutorialTarget.HeroClose, Advance.Tap, { tutorialCloseHero }),
    StartBattle(TutorialTarget.StartButton, Advance.Tap, { tutorialStart });

    enum class Advance { Button, Tap, State }

    companion object {
        fun parse(name: String?): TutorialStep? = entries.firstOrNull { it.name == name }
    }
}

/** Miejsca, ktore samouczek umie podswietlic. Kazde nosi [tutorialTarget] w swoim ekranie. */
enum class TutorialTarget {
    SummonAltar, FreeSummon, TownBack, NavFight, PartyButton, ReserveMonster, PartyClose,
    Portrait, WeaponSlot, EquipButton, HeroClose, StartButton
}

class TutorialController(private val preferences: AppPreferences) {

    val step: TutorialStep? get() = TutorialStep.parse(preferences.tutorialStep)

    /** Polozenie celow w oknie - aktualizowane przez [tutorialTarget] przy kazdym ulozeniu. */
    internal val bounds = mutableStateMapOf<TutorialTarget, Rect>()

    /**
     * Chwilowe schowanie nakladki - np. na czas pokazu summona: pierwszy towarzysz ma wyjsc
     * z oltarza na pelnym ekranie, a nie pod dymkiem medrca.
     */
    var held by mutableStateOf(false)

    fun advance() {
        val current = step ?: return
        moveTo(TutorialStep.entries.getOrNull(current.ordinal + 1))
    }

    fun moveTo(next: TutorialStep?) = preferences.moveTutorialTo(next?.name)

    fun finish() = moveTo(null)

    internal fun onTapped(target: TutorialTarget) {
        val current = step ?: return
        if (current.advance == TutorialStep.Advance.Tap && current.target == target) advance()
    }

    internal fun register(target: TutorialTarget, rect: Rect) {
        if (bounds[target] != rect) bounds[target] = rect
    }

    internal fun unregister(target: TutorialTarget) {
        bounds.remove(target)
    }
}

val LocalTutorial = staticCompositionLocalOf<TutorialController?> { null }

/**
 * Oznacza element jako cel samouczka: zglasza jego polozenie i tapniecie. Tapniecie jest tylko
 * PODSLUCHANE (faza Initial, bez konsumowania) - wlasne klikniecie elementu dziala jak zawsze.
 */
fun Modifier.tutorialTarget(target: TutorialTarget): Modifier = composed {
    val tutorial = LocalTutorial.current ?: return@composed Modifier
    DisposableEffect(tutorial, target) { onDispose { tutorial.unregister(target) } }
    Modifier
        .onGloballyPositioned { tutorial.register(target, it.boundsInWindow()) }
        .pointerInput(tutorial, target) {
            awaitEachGesture {
                awaitFirstDown(requireUnconsumed = false, pass = PointerEventPass.Initial)
                val up = waitForUpOrCancellation(pass = PointerEventPass.Initial)
                if (up != null) tutorial.onTapped(target)
            }
        }
}

/**
 * Pilnuje krokow zaleznych od stanu gry i rysuje nakladke. [partyFilled] i [weaponEquipped]
 * czyta z gracza wywolujacy - samouczek nie siega sam do ViewModelu.
 */
@Composable
fun TutorialHost(
    controller: TutorialController,
    heroName: String,
    /** Czy czeka darmowy los w zwyklym bannerze - nowy gracz ma go zawsze. */
    freeSummonReady: Boolean,
    partyFilled: Boolean,
    weaponEquipped: Boolean,
    onSkip: () -> Unit
) {
    val step = controller.step ?: return

    LaunchedEffect(step, partyFilled, weaponEquipped, freeSummonReady) {
        when {
            // Powtorka z Ustawien, gdy darmowy los jest na cooldownie: nie kazemy placic biletem,
            // przeskakujemy od razu do przygotowan do walki.
            step == TutorialStep.SummonAltar && !freeSummonReady -> controller.moveTo(TutorialStep.FightTab)
            step == TutorialStep.PlaceMonster && partyFilled -> controller.advance()
            step == TutorialStep.EquipSword && weaponEquipped -> controller.advance()
            // Gracz, ktory juz trzyma bron (powtorka samouczka z Ustawien), nie ma czego zakladac.
            step == TutorialStep.WeaponSlot && weaponEquipped -> controller.moveTo(TutorialStep.CloseHero)
        }
    }

    if (controller.held) return

    val t = strings
    TutorialOverlay(
        controller = controller,
        step = step,
        text = if (step == TutorialStep.Welcome) t.tutorialWelcome(heroName.ifBlank { "hero" }) else step.text(t),
        nextLabel = t.tutorialNext,
        skipLabel = t.tutorialSkip,
        onSkip = onSkip
    )
}

@Composable
private fun TutorialOverlay(
    controller: TutorialController,
    step: TutorialStep,
    text: String,
    nextLabel: String,
    skipLabel: String,
    onSkip: () -> Unit
) {
    val density = LocalDensity.current
    var origin by remember { mutableStateOf(Offset.Zero) }
    val target = step.target
    val rect = target?.let { controller.bounds[it] }?.translate(-origin)
    // Cel, ktorego nie ma na ekranie (gracz zboczyl, cel siedzi w oknie systemowym): zostaje sam
    // dymek i nic nie blokujemy - inaczej gracz utknalby pod zaslona bez wyjscia.
    val spotlight = rect != null
    val blocking = step.target == null || spotlight

    val infinite = rememberInfiniteTransition()
    val pulse by infinite.animateFloat(0f, 1f, infiniteRepeatable(tween(900, easing = FastOutSlowInEasing), RepeatMode.Reverse))

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .onGloballyPositioned { origin = it.positionInWindow() }
    ) {
        val widthPx = constraints.maxWidth.toFloat()
        val heightPx = constraints.maxHeight.toFloat()
        val pad = with(density) { 8.dp.toPx() }
        val hole = rect?.let { Rect(it.left - pad, it.top - pad, it.right + pad, it.bottom + pad) }

        if (blocking) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val scrim = Color.Black.copy(alpha = 0.66f)
                if (hole == null) {
                    drawRect(scrim)
                } else {
                    val radius = CornerRadius(with(density) { 14.dp.toPx() })
                    val cut = Path().apply { addRoundRect(androidx.compose.ui.geometry.RoundRect(hole, radius)) }
                    clipPath(cut, ClipOp.Difference) { drawRect(scrim) }
                    val grow = with(density) { (2.dp + 6.dp * pulse).toPx() }
                    drawRoundRect(
                        color = Color(0xFFFFD34D).copy(alpha = 1f - 0.5f * pulse),
                        topLeft = Offset(hole.left - grow, hole.top - grow),
                        size = androidx.compose.ui.geometry.Size(hole.width + grow * 2, hole.height + grow * 2),
                        cornerRadius = CornerRadius(radius.x + grow),
                        style = Stroke(width = with(density) { 3.dp.toPx() })
                    )
                }
            }
            // Blokady dotyku - cztery prostokaty DOOKOLA dziury. W samej dziurze nie ma nic z tej
            // nakladki, wiec tapniecie trafia prosto w podswietlony przycisk pod spodem.
            if (hole == null) {
                TouchBlocker(Rect(0f, 0f, widthPx, heightPx))
            } else {
                TouchBlocker(Rect(0f, 0f, widthPx, hole.top))
                TouchBlocker(Rect(0f, hole.bottom, widthPx, heightPx))
                TouchBlocker(Rect(0f, hole.top, hole.left, hole.bottom))
                TouchBlocker(Rect(hole.right, hole.top, widthPx, hole.bottom))
            }
        }

        // Raczka przy celu: pod nim, gdy cel jest w gornej polowie; nad nim - w dolnej.
        if (hole != null) {
            val below = hole.center.y < heightPx / 2f
            val bob = with(density) { (6.dp * pulse).toPx() }
            val handSize = with(density) { 44.dp.toPx() }
            val x = (hole.center.x - handSize / 2f).coerceIn(0f, widthPx - handSize)
            val y = if (below) hole.bottom + with(density) { 4.dp.toPx() } + bob else hole.top - handSize - with(density) { 4.dp.toPx() } - bob
            Text(
                text = if (below) "👆" else "👇",
                fontSize = 36.sp,
                modifier = Modifier.offset { IntOffset(x.roundToInt(), y.roundToInt()) }
            )
        }

        // Dymek po przeciwnej stronie ekranu niz cel - nigdy go nie zaslania.
        val bubbleAtTop = hole != null && hole.center.y > heightPx / 2f
        SageBubble(
            text = text,
            showNext = step.advance == TutorialStep.Advance.Button,
            nextLabel = nextLabel,
            skipLabel = skipLabel,
            onNext = controller::advance,
            onSkip = onSkip,
            modifier = Modifier
                .align(
                    when {
                        hole == null && blocking -> Alignment.Center
                        bubbleAtTop -> Alignment.TopCenter
                        else -> Alignment.BottomCenter
                    }
                )
                .then(if (bubbleAtTop) Modifier.statusBarsPadding().padding(top = 96.dp) else Modifier.navigationBarsPadding().padding(bottom = 104.dp))
        )
    }
}

@Composable
private fun TouchBlocker(area: Rect) {
    if (area.width <= 0f || area.height <= 0f) return
    val density = LocalDensity.current
    Box(
        modifier = Modifier
            .offset { IntOffset(area.left.roundToInt(), area.top.roundToInt()) }
            .size(with(density) { area.width.toDp() }, with(density) { area.height.toDp() })
            .pointerInput(Unit) {
                awaitEachGesture {
                    awaitFirstDown(requireUnconsumed = false).consume()
                    do {
                        val event = awaitPointerEvent()
                        event.changes.forEach { it.consume() }
                    } while (event.changes.any { it.pressed })
                }
            }
    )
}

@Composable
private fun SageBubble(
    text: String,
    showNext: Boolean,
    nextLabel: String,
    skipLabel: String,
    onNext: () -> Unit,
    onSkip: () -> Unit,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(18.dp)
    Column(
        modifier = modifier
            .padding(horizontal = 14.dp)
            .widthIn(max = 460.dp)
            .fillMaxWidth()
            .clip(shape)
            .background(Brush.verticalGradient(listOf(Color(0xFF342760), Color(0xFF1B1433))))
            .border(2.5.dp, Color(0xFFC8A45A), shape)
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(if (showNext) 72.dp else 54.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFC8A45A))
                    .padding(2.5.dp)
                    .clip(CircleShape)
                    .background(Brush.verticalGradient(listOf(Color(0xFF4A3A7E), Color(0xFF1A1330)))),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(Resources.UiArt.tutorialWizard),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize().padding(3.dp)
                )
            }
            // GlyphText - emoji walut w tresci rysuja sie jako ikony gry.
            GlyphText(
                text = text,
                color = Color(0xFFF4E9D7),
                fontFamily = FontFirst(),
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                lineHeight = 19.sp,
                modifier = Modifier.weight(1f)
            )
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = skipLabel,
                color = Color(0xFF9D8BB8),
                fontFamily = FontFirst(),
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable(onClick = onSkip)
                    .padding(horizontal = 6.dp, vertical = 6.dp)
            )
            Box(modifier = Modifier.weight(1f))
            if (showNext) {
                ChunkyButton(text = nextLabel, color = Color(0xFF3FA84A), textSize = 15, onClick = onNext)
            }
        }
    }
}
