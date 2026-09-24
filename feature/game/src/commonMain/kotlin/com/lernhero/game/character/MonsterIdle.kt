package com.lernhero.game.character

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import kotlin.math.PI
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.math.sin

/**
 * Animacja spoczynku potwora z JEDNEGO PNG - bez klatek, bez rigu, bez nowych grafik.
 *
 * Trzy warstwy, kazda wlaczana per gatunek w [IdleStyle]:
 *  1. **Cialo** - oddech (skala od stop), kolysanie, galaretowate spłaszczanie (szlamy), unoszenie
 *     sie (duchy) i trzepot skrzydel (nietoperz, feniks).
 *  2. **Falowanie** - obraz ciety na poziome paski, przesuwane sinusem tym mocniej, im nizej. Od
 *     [IdleStyle.waveFrom] w dol szaty, peleryny, plomienie i ogony zaczynaja sie ruszac.
 *  3. **Czasteczki** - ogien z dloni, szron z paszczy, dusze znad kostura, liscie, zarodniki,
 *     babelki. Punkty zrodel ([Emit]) sa w ulamkach grafiki TAK JAK NARYSOWANA - odbicie strony
 *     robi warstwa wyzej (graphicsLayer), wiec ogien zostaje w tej samej dloni po obu stronach walki.
 *
 * Wszystko jest funkcja czasu - zadnego stanu czasteczek. Kazda czasteczka wie, gdzie jest, z
 * samego czasu i swojego numeru (patrz [hash]), wiec setki czasteczek nic nie kosztuja poza
 * rysowaniem i nie ma czego czyscic, gdy potwor znika z ekranu.
 */
data class IdleStyle(
    /** Amplituda oddechu - ulamek wysokosci (0.02 = 2%). */
    val breathe: Float = 0.018f,
    /** Kolysanie calej postaci od stop, w stopniach. */
    val swayDeg: Float = 0.8f,
    /** Okres oddechu/kolysania. Ciezkie potwory wolniej, male szybciej. */
    val periodMs: Int = 2600,
    /** Galareta: dodatkowe, szybsze spłaszczanie-rozciaganie (szlamy). */
    val wobble: Float = 0f,
    /** Unoszenie sie w powietrzu - ulamek wysokosci (duchy, feniks, nietoperz). */
    val hover: Float = 0f,
    /** Trzepot skrzydel: amplituda skali w pionie wokol [flapPivotY], szybki okres [flapMs]. */
    val flap: Float = 0f,
    val flapPivotY: Float = 0.6f,
    val flapMs: Int = 320,
    /** Od jakiej wysokosci (0 = gora) obraz zaczyna falowac. null = bez falowania. */
    val waveFrom: Float? = null,
    /** Najwieksze przesuniecie fali - ulamek szerokosci, przy samym dole. */
    val waveAmp: Float = 0.014f,
    /** Ile "grzbietow" fali miesci sie na wysokosci grafiki. */
    val waveCycles: Float = 1.4f,
    val wavePeriodMs: Int = 1700,
    val emitters: List<Emit> = emptyList()
) {
    companion object {
        val Default = IdleStyle()
    }
}

/** Zrodlo czasteczek - punkt na grafice (ulamki 0..1 jak narysowana) i rodzaj efektu. */
data class Emit(
    val x: Float,
    val y: Float,
    val fx: Fx,
    /** Rozrzut punktu startu - ulamek szerokosci. */
    val spread: Float = 0.03f,
    /** Mnoznik wielkosci czasteczek. */
    val scale: Float = 1f,
    /** Mnoznik liczby czasteczek. */
    val density: Float = 1f,
    /** Kolor dla efektow, ktore go przyjmuja (poswiata oczu). */
    val tint: Color? = null
)

/**
 * Rodzaje czasteczek. [rise] > 0 leci w gore, < 0 opada; [push] to dryf w bok (w strone, w ktora
 * patrzy art); rozmiary w ulamkach szerokosci grafiki.
 */
enum class Fx(
    val lifeMs: Int,
    val count: Int,
    val rise: Float,
    val push: Float,
    val drift: Float,
    val sizeStart: Float,
    val sizeEnd: Float,
    val alpha: Float
) {
    Flame(lifeMs = 640, count = 11, rise = 0.12f, push = 0f, drift = 0.012f, sizeStart = 0.046f, sizeEnd = 0.006f, alpha = 0.95f),
    Ember(lifeMs = 1500, count = 6, rise = 0.26f, push = 0f, drift = 0.035f, sizeStart = 0.009f, sizeEnd = 0.003f, alpha = 1f),
    Frost(lifeMs = 1700, count = 6, rise = 0.04f, push = 0.08f, drift = 0.02f, sizeStart = 0.018f, sizeEnd = 0.05f, alpha = 0.55f),
    Snow(lifeMs = 2800, count = 7, rise = -0.3f, push = 0f, drift = 0.03f, sizeStart = 0.008f, sizeEnd = 0.006f, alpha = 0.9f),
    Soul(lifeMs = 1900, count = 5, rise = 0.2f, push = 0f, drift = 0.03f, sizeStart = 0.024f, sizeEnd = 0.004f, alpha = 0.85f),
    GreenSoul(lifeMs = 1600, count = 6, rise = 0.16f, push = 0f, drift = 0.02f, sizeStart = 0.026f, sizeEnd = 0.004f, alpha = 0.9f),
    Leaf(lifeMs = 3000, count = 3, rise = -0.34f, push = 0.02f, drift = 0.06f, sizeStart = 0.016f, sizeEnd = 0.014f, alpha = 0.95f),
    Spore(lifeMs = 2800, count = 6, rise = 0.14f, push = 0f, drift = 0.03f, sizeStart = 0.006f, sizeEnd = 0.004f, alpha = 0.95f),
    Bubble(lifeMs = 1700, count = 4, rise = 0.22f, push = 0f, drift = 0.02f, sizeStart = 0.008f, sizeEnd = 0.014f, alpha = 0.85f),
    Stink(lifeMs = 2200, count = 3, rise = 0.1f, push = 0f, drift = 0.02f, sizeStart = 0.02f, sizeEnd = 0.05f, alpha = 0.28f),
    Drip(lifeMs = 1300, count = 3, rise = -0.14f, push = 0f, drift = 0f, sizeStart = 0.011f, sizeEnd = 0.007f, alpha = 0.95f),

    /** Pulsujaca poswiata w miejscu (oczy, klejnoty) - nie leci nigdzie. */
    Glow(lifeMs = 1, count = 1, rise = 0f, push = 0f, drift = 0f, sizeStart = 0.03f, sizeEnd = 0.03f, alpha = 0.8f)
}

/**
 * Rysuje potwora z animacja spoczynku. [t] = czas w ms (rosnacy), [pivotY] = wysokosc stop jako
 * ulamek (z anchorow presetu). [colorFilter]/[alpha] sluza blyskowi trafienia - ten sam ksztalt,
 * ta sama fala, tylko przebarwiony.
 */
internal fun DrawScope.drawIdleMonster(
    image: ImageBitmap,
    style: IdleStyle,
    t: Float,
    pivotY: Float,
    alpha: Float = 1f,
    colorFilter: ColorFilter? = null,
    particles: Boolean = true
) {
    val w = size.width
    val h = size.height
    val feet = Offset(w / 2f, h * pivotY)
    val phase = (t / style.periodMs) * 2f * PI.toFloat()

    val breath = sin(phase)
    var sy = 1f + style.breathe * breath
    var sx = 1f - style.breathe * 0.45f * breath
    if (style.wobble > 0f) {
        val jelly = sin(phase * 2.3f)
        sy += style.wobble * jelly
        sx -= style.wobble * 0.9f * jelly
    }
    val sway = style.swayDeg * sin(phase * 0.5f + 1f)
    val hoverY = -style.hover * h * (0.5f + 0.5f * sin(phase * 0.8f))

    withTransform({
        translate(top = hoverY)
        rotate(sway, feet)
        scale(sx, sy, feet)
        if (style.flap > 0f) {
            val f = sin(t / style.flapMs * 2f * PI.toFloat())
            scale(1f + style.flap * 0.3f * f, 1f - style.flap * f, Offset(w / 2f, h * style.flapPivotY))
        }
    }) {
        drawSprite(image, style, t, alpha, colorFilter)
    }

    if (particles && style.emitters.isNotEmpty() && alpha > 0.05f) {
        withTransform({ translate(top = hoverY) }) {
            style.emitters.forEachIndexed { index, emit -> drawEmitter(emit, index, t, alpha) }
        }
    }
}

private fun DrawScope.drawSprite(image: ImageBitmap, style: IdleStyle, t: Float, alpha: Float, colorFilter: ColorFilter?) {
    val w = size.width
    val h = size.height
    val from = style.waveFrom
    if (from == null) {
        drawImage(
            image = image,
            dstSize = IntSize(w.roundToInt(), h.roundToInt()),
            alpha = alpha,
            colorFilter = colorFilter
        )
        return
    }
    // Paski: gora do [from] stoi, nizej kazdy pasek przesuwa sie w bok po sinusie. Paski nachodza
    // na siebie o 1 px, zeby na krawedziach nie przeswitywaly szczeliny.
    val strips = WAVE_STRIPS
    val stripH = h / strips
    val srcStripH = image.height.toFloat() / strips
    val wave = (t / style.wavePeriodMs) * 2f * PI.toFloat()
    for (i in 0 until strips) {
        val yCenter = (i + 0.5f) / strips
        val ramp = ((yCenter - from) / (1f - from)).coerceIn(0f, 1f)
        val dx = if (ramp > 0f) {
            style.waveAmp * w * ramp.pow(1.3f) * sin(wave - yCenter * style.waveCycles * 2f * PI.toFloat())
        } else 0f
        val srcTop = (i * srcStripH).toInt()
        val srcBottom = ((i + 1) * srcStripH).toInt().coerceAtMost(image.height)
        drawImage(
            image = image,
            srcOffset = IntOffset(0, srcTop),
            srcSize = IntSize(image.width, (srcBottom - srcTop).coerceAtLeast(1)),
            dstOffset = IntOffset(dx.roundToInt(), (i * stripH).roundToInt()),
            dstSize = IntSize(w.roundToInt(), (stripH + 1.5f).roundToInt()),
            alpha = alpha,
            colorFilter = colorFilter
        )
    }
}

private fun DrawScope.drawEmitter(emit: Emit, index: Int, t: Float, alpha: Float) {
    val w = size.width
    val h = size.height
    val fx = emit.fx

    if (fx == Fx.Glow) {
        val pulse = 0.65f + 0.35f * sin(t / 420f + index)
        val r = fx.sizeStart * w * emit.scale * (0.85f + 0.3f * pulse)
        val c = emit.tint ?: Color(0xFFFFD34D)
        val center = Offset(emit.x * w, emit.y * h)
        drawCircle(
            brush = Brush.radialGradient(listOf(c.copy(alpha = fx.alpha * pulse * alpha), Color.Transparent), center, r * 2.2f),
            radius = r * 2.2f,
            center = center
        )
        drawCircle(color = Color.White.copy(alpha = 0.5f * pulse * alpha), radius = r * 0.35f, center = center)
        return
    }

    val count = (fx.count * emit.density).roundToInt().coerceAtLeast(1)
    val life = fx.lifeMs.toFloat()
    for (k in 0 until count) {
        val tt = t + k * life / count + index * 137f
        val cycle = (tt / life).toInt()
        val p = (tt % life) / life
        val r1 = hash(k + index * 31, cycle)
        val r2 = hash(cycle * 3 + 1, k * 7 + index)
        val startX = emit.x * w + (r1 - 0.5f) * 2f * emit.spread * w
        val startY = emit.y * h + (r2 - 0.5f) * emit.spread * h
        val x = startX + sin(p * PI.toFloat() * 2f * 0.8f + r1 * 6f) * fx.drift * w + fx.push * p * w
        val y = startY - fx.rise * p * h
        val size = (fx.sizeStart + (fx.sizeEnd - fx.sizeStart) * p) * w * emit.scale * (0.75f + 0.5f * r2)
        val fade = (sin(p * PI.toFloat()).coerceAtLeast(0f)).pow(0.6f) * fx.alpha * alpha
        if (fade <= 0.01f || size <= 0.2f) continue
        val at = Offset(x, y)
        when (fx) {
            Fx.Flame -> {
                // Nasycony pomarancz -> czerwien, dodawany (Plus): na ciemnym tle swieci jak ogien,
                // zamiast szarzec jak dym przy zwyklym mieszaniu.
                val c = lerp(Color(0xFFFFB21E), Color(0xFFE8300E), p)
                drawCircle(
                    brush = Brush.radialGradient(listOf(c.copy(alpha = fade * 0.9f), c.copy(alpha = 0f)), at, size * 1.3f),
                    radius = size * 1.3f,
                    center = at,
                    blendMode = BlendMode.Plus
                )
                drawCircle(
                    color = Color(0xFFFFE066).copy(alpha = fade * (1f - p) * 0.9f),
                    radius = size * 0.4f,
                    center = at,
                    blendMode = BlendMode.Plus
                )
            }
            Fx.Ember -> {
                drawCircle(color = Color(0xFFFF8A1E).copy(alpha = fade * 0.45f), radius = size * 2.2f, center = at, blendMode = BlendMode.Plus)
                drawCircle(color = Color(0xFFFFD86B).copy(alpha = fade), radius = size, center = at, blendMode = BlendMode.Plus)
            }
            Fx.Frost -> drawCircle(
                brush = Brush.radialGradient(listOf(Color(0xFFDDF4FF).copy(alpha = fade), Color(0x00BDE6FF)), at, size),
                radius = size,
                center = at
            )
            Fx.Snow -> drawCircle(color = Color.White.copy(alpha = fade), radius = size, center = at)
            Fx.Soul, Fx.GreenSoul -> {
                val c = if (fx == Fx.Soul) Color(0xFFB36BFF) else Color(0xFF4DFFB0)
                drawCircle(
                    brush = Brush.radialGradient(listOf(c.copy(alpha = fade), c.copy(alpha = 0f)), at, size * 1.8f),
                    radius = size * 1.8f,
                    center = at
                )
                drawCircle(color = Color.White.copy(alpha = fade * 0.7f), radius = size * 0.35f, center = at)
            }
            Fx.Leaf -> {
                val c = if (r1 > 0.5f) Color(0xFF6BBF3A) else Color(0xFF9AD14B)
                rotate(degrees = sin(p * 9f + r1 * 6f) * 70f, pivot = at) {
                    drawOval(
                        color = c.copy(alpha = fade),
                        topLeft = Offset(at.x - size, at.y - size * 0.45f),
                        size = Size(size * 2f, size * 0.9f)
                    )
                }
            }
            Fx.Spore -> {
                drawCircle(color = Color(0xFFE6FF8A).copy(alpha = fade * 0.4f), radius = size * 2.4f, center = at)
                drawCircle(color = Color(0xFFF6FFCF).copy(alpha = fade), radius = size, center = at)
            }
            Fx.Bubble -> {
                drawCircle(color = Color(0xFFBFEFFF).copy(alpha = fade * 0.25f), radius = size, center = at)
                drawCircle(color = Color(0xFFE8FAFF).copy(alpha = fade), radius = size, center = at, style = Stroke(width = size * 0.22f))
                drawCircle(color = Color.White.copy(alpha = fade), radius = size * 0.25f, center = Offset(at.x - size * 0.35f, at.y - size * 0.35f))
            }
            Fx.Stink -> drawCircle(
                brush = Brush.radialGradient(listOf(Color(0xFF9BC53D).copy(alpha = fade), Color(0x009BC53D)), at, size),
                radius = size,
                center = at
            )
            Fx.Drip -> {
                drawCircle(color = Color(0xFFFF7A1A).copy(alpha = fade * 0.45f), radius = size * 1.8f, center = at)
                drawCircle(color = Color(0xFFFFC23D).copy(alpha = fade), radius = size, center = at)
            }
            Fx.Glow -> Unit
        }
    }
}

/** Deterministyczny "los" 0..1 z dwoch liczb - ta sama czasteczka w tym samym cyklu zawsze ta sama. */
private fun hash(a: Int, b: Int): Float {
    var x = a * 374761393 + b * 668265263
    x = (x xor (x ushr 13)) * 1274126177
    x = x xor (x ushr 16)
    return (x and 0x7fffffff) % 10_000 / 10_000f
}

private const val WAVE_STRIPS = 44
