package com.lernhero.game.battle.component

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lernhero.domain.ultimate.UltimateKind
import com.lernhero.game.battle.BattleSpeed
import com.lernhero.game.battle.HERO_ID
import com.lernhero.game.battle.UltimateCast
import com.lernhero.game.battle.UltimateTiming
import com.lernhero.game.battle.effects.CharacterRect
import com.lernhero.game.character.CharacterUiModel
import com.lernhero.shared.theme.FontFirst
import com.lernhero.shared.theme.FontPixel
import com.lernhero.shared.theme.StrokedText
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.pow
import kotlin.math.sin

/*
 * Wystrzal ulti jako SCENA, a nie mrugniecie.
 *
 * Wczesniej ulti bylo pol sekundy zoltego blysku i napisem - latwo bylo przegapic, ze w ogole padlo,
 * i nie bylo zadnej nagrody za piec dobrych odpowiedzi z rzedu. Teraz to ~2.3 s w czterech taktach
 * (czasy w [UltimateTiming], wspolne z view modelem):
 *
 *  1. LADOWANIE - tlo gasnie, a swiatlo zostaje na tych, ktorych ulti dotyczy (wrogowie przy
 *     obrazeniach, nasza druzyna przy leczeniu - druga strona przygasa). Kamera najezdza na cel,
 *     nad bohaterem rozblyskuje aura i z niej wznosi sie kula, a do kuli zbiegaja sie iskry.
 *  2. BANER - ukosny pas przez ekran z nazwa kuli i seria ("5 IN A ROW"), jak w grach, z ktorych
 *     ten zwyczaj pochodzi. Napis sunie powoli - to jest to "zwolnienie" przed ciosem.
 *  3. UDERZENIE - kula peka, promienie strzelaja w cele, bialy blysk, fala uderzeniowa pod stopami,
 *     trzesienie i ZWOLNIONE TEMPO samych wybuchow ([com.lernhero.game.battle.fx.VfxEngine.slowMotion]).
 *     W tej samej chwili view model zadaje obrazenia, wiec liczby wyskakuja razem z uderzeniem.
 *  4. POKLOSIE - duza liczba skutku na srodku, kamera wraca, swiat sie rozjasnia.
 *
 * Arena rysuje dwie warstwy ([UltimateBackdrop] pod postaciami, [UltimateForeground] nad nimi),
 * a HUD trzecia ([UltimateCutIn]) - baner i blysk nie trzesa sie i nie zoomuja z kamera.
 */

/**
 * Scena jednego wystrzalu. [t] to czas od wystrzalu w ms przy tempie 1x (porownywalny z
 * [UltimateTiming]) - czytany z zegara LENIWIE, dopiero w rysowaniu. Dzieki temu co klatke
 * odrysowuja sie tylko warstwy sceny, a nie cale pole bitwy, ktore trzyma ten obiekt.
 */
@Stable
class UltimateScene(val cast: UltimateCast, private val clock: Animatable<Float, AnimationVector1D>) {
    val t: Float get() = clock.value

    /** Scena dobiegla konca - warstwy nic juz nie rysuja, zanim view model ja zdejmie. */
    val finished: Boolean get() = t >= UltimateTiming.TOTAL_MS

    val color: Color = cast.spec.color

    /** Postep miedzy dwiema chwilami sceny, 0..1. */
    fun phase(from: Int, to: Int): Float = ((t - from) / (to - from)).coerceIn(0f, 1f)

    /** Jak bardzo swiat jest przygaszony (0 = normalnie, 1 = pelna ciemnosc sceny). */
    val dim: Float
        get() = when {
            t < UltimateTiming.DIM_IN_MS -> easeOut(phase(0, UltimateTiming.DIM_IN_MS))
            t < UltimateTiming.FADE_OUT_START_MS -> 1f
            else -> 1f - easeInOut(phase(UltimateTiming.FADE_OUT_START_MS, UltimateTiming.TOTAL_MS - 120))
        }

    /** Najazd kamery: powoli do przodu przy ladowaniu, szarpniecie przy uderzeniu, powrot na koniec. */
    val zoom: Float
        get() {
            val push = 0.065f * easeInOut(phase(0, 700))
            val punch = if (t >= UltimateTiming.IMPACT_MS) 0.04f * (1f - phase(UltimateTiming.IMPACT_MS, UltimateTiming.IMPACT_MS + 280)) else 0f
            val back = easeInOut(phase(UltimateTiming.FADE_OUT_START_MS, UltimateTiming.TOTAL_MS - 80))
            return 1f + (push + punch) * (1f - back)
        }

    /** Bialy blysk w chwili uderzenia. */
    val flash: Float
        get() = if (t < UltimateTiming.IMPACT_MS) 0f else (1f - phase(UltimateTiming.IMPACT_MS, UltimateTiming.IMPACT_MS + 240)).pow(2)

    val impacted: Boolean get() = t >= UltimateTiming.IMPACT_MS

    val healing: Boolean get() = cast.spec.isHealing
}

/**
 * Zegar sceny dla biezacego [cast]. Null, gdy nic nie leci (albo scena juz sie skonczyla) - wtedy
 * zadna warstwa nic nie rysuje. [onFinished] mowi view modelowi, ze scena zeszla z ekranu.
 */
@Composable
fun rememberUltimateScene(cast: UltimateCast?, onFinished: (Long) -> Unit): UltimateScene? {
    val clock = remember(cast?.id) { Animatable(0f) }
    LaunchedEffect(cast?.id) {
        if (cast == null) return@LaunchedEffect
        clock.animateTo(
            UltimateTiming.TOTAL_MS.toFloat(),
            tween(BattleSpeed.scale(UltimateTiming.TOTAL_MS), easing = LinearEasing)
        )
        onFinished(cast.id)
    }
    if (cast == null) return null
    return remember(cast) { UltimateScene(cast, clock) }
}

/** Punkt, na ktory najezdza kamera - srodek celow sceny, w ulamkach areny (0..1). */
fun UltimateScene.focusPoint(rects: Map<String, CharacterRect>, arena: Size): Offset {
    val points = cast.focusIds.mapNotNull { rects[it]?.center() }
    if (points.isEmpty() || arena.width <= 0f || arena.height <= 0f) return Offset(0.5f, 0.5f)
    val cx = points.map { it.x }.average().toFloat()
    val cy = points.map { it.y }.average().toFloat()
    return Offset((cx / arena.width).coerceIn(0.1f, 0.9f), (cy / arena.height).coerceIn(0.15f, 0.85f))
}

/* ======================================================================== warstwa pod postaciami */

/**
 * Ciemnosc i swiatlo POD postaciami: tlo areny gasnie, a za celami sceny zapala sie poswiata w
 * kolorze kuli (i plama swiatla na ziemi pod stopami) - postacie wychodza z ciemnosci jak na scenie.
 */
@Composable
fun UltimateBackdrop(scene: UltimateScene?, rects: Map<String, CharacterRect>, modifier: Modifier = Modifier) {
    scene ?: return
    Canvas(modifier = modifier.fillMaxSize()) {
        if (scene.finished) return@Canvas
        val dim = scene.dim
        if (dim <= 0f) return@Canvas
        val color = scene.color
        drawRect(Color(0xFF05030A).copy(alpha = 0.76f * dim))
        drawRect(
            brush = Brush.radialGradient(
                colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.55f * dim)),
                center = center,
                radius = size.maxDimension * 0.72f
            )
        )
        val pulse = 0.8f + 0.2f * sin(scene.t / 90f)
        scene.cast.focusIds.forEach { id ->
            val r = rects[id] ?: return@forEach
            val c = r.center()
            val reach = max(r.size.width, r.size.height) * 0.85f
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(color.copy(alpha = 0.6f * dim * pulse), color.copy(alpha = 0.2f * dim), Color.Transparent),
                    center = c,
                    radius = reach
                ),
                radius = reach,
                center = c
            )
            val feet = r.feet()
            val ground = Size(r.size.width * 1.3f, r.size.width * 0.32f)
            drawOval(
                brush = Brush.radialGradient(
                    colors = listOf(color.copy(alpha = 0.55f * dim), Color.Transparent),
                    center = feet,
                    radius = ground.width / 2f
                ),
                topLeft = Offset(feet.x - ground.width / 2f, feet.y - ground.height / 2f),
                size = ground
            )
        }
        // Aura bohatera, dopoki kula nie oderwie sie od niego.
        rects[HERO_ID]?.let { hero ->
            val a = dim * (1f - scene.phase(UltimateTiming.ORB_RISE_MS, UltimateTiming.IMPACT_MS))
            if (a > 0f) {
                val c = hero.center()
                val reach = max(hero.size.width, hero.size.height) * 0.9f
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(color.mix(Color.White, 0.3f).copy(alpha = 0.7f * a), color.copy(alpha = 0.25f * a), Color.Transparent),
                        center = c,
                        radius = reach
                    ),
                    radius = reach,
                    center = c
                )
            }
        }
    }
}

/* ======================================================================== warstwa nad postaciami */

/**
 * Wszystko, co dzieje sie NAD postaciami: przygaszenie drugiej strony, kula wznoszaca sie z
 * bohatera, iskry wciagane w kule, promienie w chwili uderzenia, fala pod stopami celow, a dalej -
 * zaleznie od rodzaju ulti - deszcz swiatla (leczenie), strugi zycia (Drain) albo kopula (Ward).
 */
@Composable
fun UltimateForeground(
    scene: UltimateScene?,
    rects: Map<String, CharacterRect>,
    characters: Collection<CharacterUiModel>,
    modifier: Modifier = Modifier
) {
    scene ?: return
    Canvas(modifier = modifier.fillMaxSize()) {
        if (scene.finished) return@Canvas
        val t = scene.t
        val color = scene.color
        val dim = scene.dim
        val focus = scene.cast.focusIds

        // 1. Druga strona przygasa - tylko cele (i bohater, dopoki trzyma kule) zostaja w swietle.
        characters.forEach { character ->
            if (character.id in focus || character.hp <= 0) return@forEach
            if (character.id == HERO_ID && t < UltimateTiming.ORB_RISE_MS) return@forEach
            val r = rects[character.id] ?: return@forEach
            val reach = max(r.size.width, r.size.height) * 0.62f
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color.Black.copy(alpha = 0.62f * dim), Color.Black.copy(alpha = 0.35f * dim), Color.Transparent),
                    center = r.center(),
                    radius = reach
                ),
                radius = reach,
                center = r.center()
            )
        }

        val orbAt = orbPosition(scene, rects)
        val orbRadius = 7.dp.toPx() + 20.dp.toPx() * easeOut(scene.phase(0, UltimateTiming.ORB_RISE_MS)) +
            7.dp.toPx() * scene.phase(UltimateTiming.ORB_RISE_MS, UltimateTiming.IMPACT_MS)

        // 2. Iskry wciagane w kule - im blizej uderzenia, tym szybciej.
        if (t in 350f..UltimateTiming.IMPACT_MS.toFloat()) {
            val strength = scene.phase(350, 600)
            repeat(22) { i ->
                val cycle = 520f - 200f * scene.phase(350, UltimateTiming.IMPACT_MS)
                val local = ((t + i * 61f) % cycle) / cycle
                val angle = hash(i, 1) * 2f * PI.toFloat() + t / 900f
                val dist = (1f - easeIn(local)) * 120.dp.toPx() * (0.6f + 0.4f * hash(i, 2))
                val p = Offset(orbAt.x + cos(angle) * dist, orbAt.y + sin(angle) * dist)
                drawCircle(
                    color = color.mix(Color.White, 0.5f).copy(alpha = strength * (0.35f + 0.65f * local)),
                    radius = (1.5f + 2.5f * hash(i, 3)).dp.toPx(),
                    center = p
                )
            }
        }

        // 3. Kula: wznosi sie, pulsuje coraz szybciej, w chwili uderzenia peka.
        if (!scene.impacted) {
            val beat = 0.75f + 0.25f * sin(t / (70f - 40f * scene.phase(UltimateTiming.ORB_RISE_MS, UltimateTiming.IMPACT_MS)))
            drawPowerOrb(orbAt, orbRadius, color, glow = beat)
        } else {
            val burst = scene.phase(UltimateTiming.IMPACT_MS, UltimateTiming.IMPACT_MS + 420)
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color.White.copy(alpha = 1f - burst), color.copy(alpha = 0.6f * (1f - burst)), Color.Transparent),
                    center = orbAt,
                    radius = orbRadius * (1f + 3f * burst)
                ),
                radius = orbRadius * (1f + 3f * burst),
                center = orbAt
            )
            listOf(0f, 0.25f).forEach { delay ->
                val ring = ((burst - delay) / (1f - delay)).coerceIn(0f, 1f)
                if (ring in 0.001f..0.999f) {
                    drawCircle(
                        color = color.mix(Color.White, 0.5f).copy(alpha = 1f - ring),
                        radius = orbRadius * (1f + 7f * easeOut(ring)),
                        center = orbAt,
                        style = Stroke(width = (1f - ring) * 7.dp.toPx() + 1.dp.toPx())
                    )
                }
            }
        }

        // 4. Promienie z kuli w kazdy cel - krotkie, grube, w chwili uderzenia.
        val beam = scene.phase(UltimateTiming.IMPACT_MS - 40, UltimateTiming.IMPACT_MS + 260)
        if (beam in 0.001f..0.999f) {
            val a = if (beam < 0.2f) beam / 0.2f else 1f - (beam - 0.2f) / 0.8f
            focus.forEach { id ->
                val target = rects[id]?.center() ?: return@forEach
                drawLine(
                    brush = Brush.linearGradient(listOf(color.copy(alpha = 0.3f * a), color.copy(alpha = 0.9f * a)), orbAt, target),
                    start = orbAt, end = target, strokeWidth = 16.dp.toPx() * a, cap = StrokeCap.Round
                )
                drawLine(
                    color = Color.White.copy(alpha = 0.9f * a),
                    start = orbAt, end = target, strokeWidth = 4.dp.toPx() * a, cap = StrokeCap.Round
                )
            }
        }

        // 5. Fala uderzeniowa po ziemi pod kazdym celem.
        if (scene.impacted) {
            focus.forEach { id ->
                val r = rects[id] ?: return@forEach
                val feet = r.feet()
                listOf(0, 110).forEach { lag ->
                    val w = scene.phase(UltimateTiming.IMPACT_MS + lag, UltimateTiming.IMPACT_MS + lag + 520)
                    if (w in 0.001f..0.999f) {
                        val width = r.size.width * (0.4f + 1.6f * easeOut(w))
                        val height = width * 0.28f
                        drawOval(
                            color = color.mix(Color.White, 0.4f).copy(alpha = 1f - w),
                            topLeft = Offset(feet.x - width / 2f, feet.y - height / 2f),
                            size = Size(width, height),
                            style = Stroke(width = (1f - w) * 6.dp.toPx() + 1.dp.toPx())
                        )
                    }
                }
            }
        }

        // 6. Rodzaj ulti.
        when (scene.cast.spec.kind) {
            UltimateKind.Mend, UltimateKind.Ward -> lightRain(scene, rects)
            UltimateKind.Drain -> lifeStreams(scene, rects, characters)
            else -> Unit
        }
        if (scene.cast.spec.kind == UltimateKind.Ward) wardDome(scene, rects)
    }
}

/** Gdzie jest kula: startuje z piersi bohatera i lukiem wznosi sie nad srodek areny. */
private fun DrawScope.orbPosition(scene: UltimateScene, rects: Map<String, CharacterRect>): Offset {
    val start = rects[HERO_ID]?.let { Offset(it.center().x, it.topLeft.y + it.size.height * 0.4f) }
        ?: Offset(size.width * 0.25f, size.height * 0.6f)
    val end = Offset(size.width * 0.5f, size.height * 0.2f)
    val p = easeInOut(scene.phase(0, UltimateTiming.ORB_RISE_MS))
    val control = Offset(start.x, end.y)
    val a = (1f - p) * (1f - p)
    val b = 2f * (1f - p) * p
    val c = p * p
    val base = Offset(a * start.x + b * control.x + c * end.x, a * start.y + b * control.y + c * end.y)
    // Kula na szczycie nie stoi martwo - lekko faluje.
    val hover = if (p >= 1f) sin(scene.t / 160f) * 3.dp.toPx() else 0f
    return Offset(base.x, base.y + hover)
}

/** Leczenie/oslona: swietliste drobinki opadajace na sojusznikow, gesciej tuz przed uderzeniem. */
private fun DrawScope.lightRain(scene: UltimateScene, rects: Map<String, CharacterRect>) {
    val window = scene.phase(300, UltimateTiming.IMPACT_MS + 700)
    if (window <= 0f || window >= 1f) return
    val fade = if (window < 0.15f) window / 0.15f else if (window > 0.8f) (1f - window) / 0.2f else 1f
    scene.cast.focusIds.forEachIndexed { index, id ->
        val r = rects[id] ?: return@forEachIndexed
        repeat(12) { i ->
            val speed = 700f + 500f * hash(i, index + 7)
            val local = ((scene.t + hash(i, index + 3) * speed) % speed) / speed
            val x = r.topLeft.x + r.size.width * (0.1f + 0.8f * hash(i, index + 11))
            val y = r.topLeft.y - 40.dp.toPx() + (r.size.height + 30.dp.toPx()) * local
            drawCircle(
                color = scene.color.mix(Color.White, 0.6f).copy(alpha = fade * (1f - local) * 0.9f),
                radius = (1.5f + 2f * hash(i, index + 5)).dp.toPx(),
                center = Offset(x, y)
            )
        }
    }
}

/** Drain: strugi zycia plynace od kazdego trafionego wroga do bohatera. */
private fun DrawScope.lifeStreams(scene: UltimateScene, rects: Map<String, CharacterRect>, characters: Collection<CharacterUiModel>) {
    val w = scene.phase(UltimateTiming.IMPACT_MS + 60, UltimateTiming.IMPACT_MS + 1000)
    if (w <= 0f || w >= 1f) return
    val sink = rects[HERO_ID]?.center() ?: return
    val fade = if (w < 0.2f) w / 0.2f else (1f - w) / 0.8f
    val enemies = characters.filter { !it.isPlayer }.mapNotNull { rects[it.id]?.center() }
    enemies.forEachIndexed { e, from ->
        val control = Offset((from.x + sink.x) / 2f, minOf(from.y, sink.y) - 60.dp.toPx())
        repeat(9) { i ->
            val s = ((w * 1.6f + i / 9f + e * 0.13f) % 1f)
            val a = (1f - s) * (1f - s)
            val b = 2f * (1f - s) * s
            val c = s * s
            val p = Offset(a * from.x + b * control.x + c * sink.x, a * from.y + b * control.y + c * sink.y)
            drawCircle(
                color = scene.color.mix(Color.White, 0.3f).copy(alpha = fade * 0.9f),
                radius = (2.5f + 2f * hash(i, e)).dp.toPx(),
                center = p
            )
        }
    }
}

/** Ward: polprzezroczysta kopula nad cala druzyna, gasnaca do konca sceny. */
private fun DrawScope.wardDome(scene: UltimateScene, rects: Map<String, CharacterRect>) {
    if (!scene.impacted) return
    val rs = scene.cast.focusIds.mapNotNull { rects[it] }
    if (rs.isEmpty()) return
    val left = rs.minOf { it.topLeft.x } - 18.dp.toPx()
    val right = rs.maxOf { it.topLeft.x + it.size.width } + 18.dp.toPx()
    val top = rs.minOf { it.topLeft.y } - 26.dp.toPx()
    val bottom = rs.maxOf { it.topLeft.y + it.size.height }
    val grow = easeOut(scene.phase(UltimateTiming.IMPACT_MS, UltimateTiming.IMPACT_MS + 300))
    val fade = 1f - scene.phase(UltimateTiming.FADE_OUT_START_MS, UltimateTiming.TOTAL_MS)
    val domeHeight = (bottom - top) * 2f * grow
    val domeTop = bottom - domeHeight / 2f
    val rect = Size(right - left, domeHeight)
    clipRect(top = 0f, bottom = bottom) {
        drawOval(
            brush = Brush.verticalGradient(
                listOf(scene.color.copy(alpha = 0.28f * fade), scene.color.copy(alpha = 0.06f * fade)),
                startY = domeTop, endY = bottom
            ),
            topLeft = Offset(left, domeTop),
            size = rect
        )
        drawOval(
            color = scene.color.mix(Color.White, 0.5f).copy(alpha = 0.85f * fade),
            topLeft = Offset(left, domeTop),
            size = rect,
            style = Stroke(width = 3.dp.toPx())
        )
    }
}

/* ============================================================================== HUD */

/**
 * Warstwa HUD sceny: ukosny baner z nazwa kuli, bialy blysk uderzenia i duza liczba skutku.
 * Siedzi poza arena - nie trzesie sie i nie zoomuje razem z kamera.
 */
@Composable
fun UltimateCutIn(scene: UltimateScene?, modifier: Modifier = Modifier) {
    scene ?: return
    if (scene.finished) return
    val spec = scene.cast.spec
    val color = scene.color
    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val bandCenterY = maxHeight * 0.34f
        val bandHeight = 78.dp
        val enter = scene.phase(UltimateTiming.CUT_IN_START_MS, UltimateTiming.CUT_IN_START_MS + 170)
        val exit = scene.phase(UltimateTiming.CUT_IN_END_MS - 160, UltimateTiming.CUT_IN_END_MS)
        val bandVisible = enter > 0f && exit < 1f

        if (bandVisible) {
            // Pas: wysuwa sie z lewej, zjezdza w prawo.
            Canvas(modifier = Modifier.fillMaxSize()) {
                val h = bandHeight.toPx()
                val top = bandCenterY.toPx() - h / 2f
                val skew = h * 0.35f
                val from = size.width * easeIn(exit)
                val to = size.width * easeOut(enter)
                clipRect(left = from, right = to) {
                    val path = Path().apply {
                        moveTo(-skew, top + h)
                        lineTo(skew, top)
                        lineTo(size.width + skew, top)
                        lineTo(size.width - skew, top + h)
                        close()
                    }
                    drawPath(
                        path,
                        Brush.horizontalGradient(
                            listOf(color.mix(Color.Black, 0.35f).copy(alpha = 0.95f), Color(0xF0100A16), color.mix(Color.Black, 0.35f).copy(alpha = 0.95f))
                        )
                    )
                    // Smugi pedu - przelatuja przez pas szybciej niz napis, to one robia ruch.
                    repeat(16) { i ->
                        val len = (40f + 80f * hash(i, 21)).dp.toPx()
                        val speed = 1.4f + 1.2f * hash(i, 22)
                        val x = size.width - ((scene.t * speed + hash(i, 23) * size.width) % (size.width + len))
                        val y = top + h * (0.12f + 0.76f * hash(i, 24))
                        drawLine(
                            color = color.mix(Color.White, 0.6f).copy(alpha = 0.18f + 0.2f * hash(i, 25)),
                            start = Offset(x, y), end = Offset(x + len, y), strokeWidth = 1.5.dp.toPx()
                        )
                    }
                    drawLine(color.mix(Color.White, 0.5f), Offset(0f, top), Offset(size.width, top), strokeWidth = 3.dp.toPx())
                    drawLine(color.mix(Color.White, 0.5f), Offset(0f, top + h), Offset(size.width, top + h), strokeWidth = 3.dp.toPx())
                }
            }

            // Tresc banera: kula, a obok nazwa. Napis wjezdza szybko, a potem sunie powoli.
            val slide = (1f - easeOut(enter)) * 60f - 14f * scene.phase(UltimateTiming.CUT_IN_START_MS + 170, UltimateTiming.CUT_IN_END_MS)
            val alpha = if (exit > 0f) 1f - exit else easeOut(enter)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .offset(y = bandCenterY - 48.dp)
                    .height(96.dp)
                    .padding(horizontal = 14.dp)
                    .graphicsLayer { this.alpha = alpha },
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                val pop = easeOutBack(scene.phase(UltimateTiming.CUT_IN_START_MS + 60, UltimateTiming.CUT_IN_START_MS + 320))
                PowerOrb(
                    color = color,
                    size = 96.dp,
                    glow = 0.8f + 0.2f * sin(scene.t / 80f),
                    modifier = Modifier.graphicsLayer {
                        scaleX = pop
                        scaleY = pop
                    }
                )
                Column(
                    modifier = Modifier.graphicsLayer { translationX = slide * density },
                    verticalArrangement = Arrangement.spacedBy(1.dp)
                ) {
                    Text(
                        text = "${spec.kind.icon} ULTIMATE · ${spec.kind.label.uppercase()}",
                        color = Color.White.copy(alpha = 0.85f),
                        fontFamily = FontPixel(),
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp,
                        letterSpacing = 1.5.sp,
                        maxLines = 1
                    )
                    StrokedText(
                        text = spec.name.uppercase(),
                        size = 27.sp,
                        color = color.mix(Color.White, 0.6f),
                        outline = color.mix(Color.Black, 0.8f),
                        strokeWidth = 8f,
                        maxLines = 1
                    )
                    Text(
                        text = "🔥 ${spec.streak} IN A ROW",
                        color = Color(0xFFFFD34D),
                        fontFamily = FontPixel(),
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp,
                        maxLines = 1
                    )
                }
            }
        }

        // Blysk uderzenia: najpierw biel, potem kolor kuli.
        val flash = scene.flash
        if (flash > 0f) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.White.mix(color, 1f - flash).copy(alpha = 0.7f * flash))
            )
        }

        // Duza liczba skutku - to jest nagroda, na ktora gracz pracowal seria odpowiedzi.
        if (scene.cast.released && scene.t >= UltimateTiming.IMPACT_MS + 140) {
            val pop = easeOutBack(scene.phase(UltimateTiming.IMPACT_MS + 140, UltimateTiming.IMPACT_MS + 420))
            val out = scene.phase(UltimateTiming.TOTAL_MS - 380, UltimateTiming.TOTAL_MS)
            val (big, small) = scene.cast.headline()
            Column(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = maxHeight * 0.5f)
                    .graphicsLayer {
                        scaleX = pop
                        scaleY = pop
                        this.alpha = 1f - out
                        translationY = -24f * out * density
                    }
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.Black.copy(alpha = 0.55f))
                    .border(2.dp, color.copy(alpha = 0.9f), RoundedCornerShape(16.dp))
                    .padding(horizontal = 18.dp, vertical = 6.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                StrokedText(
                    text = big,
                    size = 34.sp,
                    color = if (scene.healing) Color(0xFF8CF5A0) else color.mix(Color.White, 0.55f),
                    outline = Color(0xFF120A06),
                    strokeWidth = 9f,
                    maxLines = 1
                )
                Text(
                    text = small,
                    color = Color.White.copy(alpha = 0.9f),
                    fontFamily = FontFirst(),
                    fontWeight = FontWeight.Black,
                    fontSize = 11.sp,
                    letterSpacing = 1.sp,
                    maxLines = 1
                )
            }
        }
    }
}

/** Duza liczba i podpis pod nia - "1240 / TO EVERY ENEMY", "+620 / HP RESTORED"... */
private fun UltimateCast.headline(): Pair<String, String> = when {
    damage > 0 && healed > 0 -> "$damage" to (if (targets > 1) "TO EVERY ENEMY · +$healed HP" else "DAMAGE · +$healed HP")
    damage > 0 -> "$damage" to (if (targets > 1) "TO EVERY ENEMY" else "DAMAGE")
    revived > 0 -> "+$healed" to "HP · $revived REVIVED"
    warded > 0 -> "SHIELD x${spec.wardCharges}" to (if (healed > 0) "EVERY ALLY · +$healed HP" else "EVERY ALLY")
    healed > 0 -> "+$healed" to "HP RESTORED"
    else -> spec.name.uppercase() to "NO TARGETS"
}

/* ============================================================================ narzedzia */

private fun CharacterRect.center() = Offset(topLeft.x + size.width / 2f, topLeft.y + size.height / 2f)
private fun CharacterRect.feet() = Offset(topLeft.x + size.width / 2f, topLeft.y + size.height * 0.92f)

private fun easeOut(t: Float) = 1f - (1f - t.coerceIn(0f, 1f)).pow(3)
private fun easeIn(t: Float) = t.coerceIn(0f, 1f).pow(3)
private fun easeInOut(t: Float): Float {
    val x = t.coerceIn(0f, 1f)
    return if (x < 0.5f) 4f * x * x * x else 1f - (-2f * x + 2f).pow(3) / 2f
}
private fun easeOutBack(t: Float): Float {
    val x = t.coerceIn(0f, 1f)
    val c1 = 1.70158f
    val c3 = c1 + 1f
    return 1f + c3 * (x - 1f).pow(3) + c1 * (x - 1f).pow(2)
}

/** Staly "los" 0..1 dla czasteczki - te same iskry leca w te same strony w kazdej klatce. */
private fun hash(i: Int, salt: Int): Float {
    var x = i * 374_761_393 + salt * 668_265_263
    x = (x xor (x ushr 13)) * 1_274_126_177
    x = x xor (x ushr 16)
    return (x and 0x7FFFFFFF) / 2_147_483_647f
}
