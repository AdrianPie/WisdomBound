package com.lernhero.game.battle.fx

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.lerp
import com.lernhero.domain.monster.Element
import kotlinx.coroutines.flow.collectLatest
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * Rysuje wszystkie zywe efekty z [engine] na JEDNYM Canvasie. Petla klatek chodzi tylko wtedy,
 * gdy jest co rysowac - pusta arena nie budzi ekranu 60 razy na sekunde.
 *
 * Kazdy efekt to czysta funkcja postepu (0..1) i ziarna ([Vfx.id]): te same iskry leca w te same
 * strony w kazdej klatce, bez trzymania stanu per czasteczka.
 */
@Composable
fun VfxLayer(engine: VfxEngine, modifier: Modifier = Modifier) {
    LaunchedEffect(engine) {
        snapshotFlow { engine.active.isNotEmpty() }.collectLatest { busy ->
            while (busy) withFrameNanos { engine.onFrame() }
        }
    }
    Canvas(modifier = modifier) {
        val now = engine.nowNs
        val seconds = now / 1_000_000_000f
        engine.active.forEach { fx ->
            val p = fx.progress(now)
            when (fx) {
                is ProjectileVfx -> drawProjectile(fx, p, seconds)
                is ImpactVfx -> drawImpact(fx, p, seconds)
            }
        }
    }
}

/* ============================================================ narzedzia */

private val Dust = Color(0xFFDCCFB6)
private val Smoke = Color(0xFF3A3440)

private fun rnd(seed: Long, i: Int, salt: Int = 0): Float {
    var x = (seed * 1_103_515_245L + i * 12_345L + salt * 668_265_263L).toInt()
    x = (x xor (x ushr 13)) * 1_274_126_177
    x = x xor (x ushr 16)
    return (x and 0x7FFFFFFF) / 2_147_483_647f
}

private fun easeOut(t: Float) = 1f - (1f - t.coerceIn(0f, 1f)).pow(3)
private fun easeOutBack(t: Float): Float {
    val x = t.coerceIn(0f, 1f)
    val c1 = 1.70158f
    val c3 = c1 + 1f
    return 1f + c3 * (x - 1f).pow(3) + c1 * (x - 1f).pow(2)
}

/** 1 do [from], potem liniowo do 0 na koncu zycia efektu. */
private fun tail(p: Float, from: Float) = if (p < from) 1f else (1f - (p - from) / (1f - from)).coerceIn(0f, 1f)

/** Szybkie wejscie i wygaszenie: 0 -> 1 do [inEnd], potem 1 do [outStart], potem do 0. */
private fun envelope(p: Float, inEnd: Float, outStart: Float): Float = when {
    p < inEnd -> p / inEnd
    p > outStart -> ((1f - p) / (1f - outStart)).coerceIn(0f, 1f)
    else -> 1f
}

private fun polar(c: Offset, angle: Float, r: Float) = Offset(c.x + cos(angle) * r, c.y + sin(angle) * r)

private fun DrawScope.glow(center: Offset, radius: Float, color: Color, alpha: Float) {
    if (alpha <= 0.01f || radius <= 0.5f) return
    drawCircle(
        brush = Brush.radialGradient(listOf(color.copy(alpha = alpha), color.copy(alpha = alpha * 0.35f), Color.Transparent), center, radius),
        radius = radius,
        center = center
    )
}

/** Czteroramienna gwiazdka - iskra magii. */
private fun DrawScope.sparkle(c: Offset, r: Float, color: Color, rotation: Float = 0f) {
    if (r <= 0.3f) return
    val path = Path().apply {
        for (k in 0 until 8) {
            val a = rotation + k * (PI.toFloat() / 4f)
            val rr = if (k % 2 == 0) r else r * 0.28f
            val pt = polar(c, a, rr)
            if (k == 0) moveTo(pt.x, pt.y) else lineTo(pt.x, pt.y)
        }
        close()
    }
    drawPath(path, color)
}

/* ============================================================ pociski */

private fun arcPoint(from: Offset, to: Offset, p: Float, lift: Float) =
    Offset(from.x + (to.x - from.x) * p, from.y + (to.y - from.y) * p - lift * 4f * p * (1f - p))

private fun arcAngle(from: Offset, to: Offset, p: Float, lift: Float): Float {
    val dx = to.x - from.x
    val dy = (to.y - from.y) - lift * 4f * (1f - 2f * p)
    return atan2(dy, dx)
}

private fun distance(a: Offset, b: Offset): Float {
    val dx = b.x - a.x
    val dy = b.y - a.y
    return sqrt(dx * dx + dy * dy)
}

private fun DrawScope.drawProjectile(fx: ProjectileVfx, p: Float, t: Float) {
    when (fx.kind) {
        ProjectileKind.SlashWave -> slashWave(fx, p, t)
        ProjectileKind.GroundWave -> groundWave(fx, p, t)
        ProjectileKind.Arrow -> arrow(fx, p)
        ProjectileKind.Bolt -> bolt(fx, p, t)
        ProjectileKind.Fireball -> fireball(fx, p, t)
        ProjectileKind.PoisonGlob -> poisonGlob(fx, p, t)
        ProjectileKind.Arcane -> arcaneBolt(fx, p, t)
    }
}

/**
 * Fala ciecia: jasny polksiezyc energii wypuszczony z miecza albo pazurow, leci prosto na cel,
 * lekko rosnie i zostawia za soba dwie blednace kopie.
 */
private fun DrawScope.slashWave(fx: ProjectileVfx, p: Float, t: Float) {
    val u = density
    val angle = atan2(fx.to.y - fx.from.y, fx.to.x - fx.from.x)
    val degrees = angle * 180f / PI.toFloat()
    for (ghost in 2 downTo 0) {
        val q = (p - ghost * 0.07f).coerceAtLeast(0f)
        if (ghost > 0 && q <= 0f) continue
        val pos = Offset(fx.from.x + (fx.to.x - fx.from.x) * q, fx.from.y + (fx.to.y - fx.from.y) * q)
        val grow = 0.75f + 0.45f * q
        val alpha = if (ghost == 0) 1f else 0.35f / ghost
        val r = 20f * u * grow
        rotate(degrees, pivot = pos) {
            // Polksiezyc wygiety W STRONE lotu - tak, jak wyglada ciecie z zamachu.
            val topLeft = Offset(pos.x - r * 0.9f, pos.y - r)
            val size = Size(r * 1.1f, r * 2f)
            drawArc(fx.tint.copy(alpha = 0.55f * alpha), -80f, 160f, false, topLeft, size, style = Stroke(9f * u, cap = StrokeCap.Round))
            drawArc(Color.White.copy(alpha = 0.95f * alpha), -70f, 140f, false, topLeft, size, style = Stroke(3.2f * u, cap = StrokeCap.Round))
        }
    }
    val head = Offset(fx.from.x + (fx.to.x - fx.from.x) * p, fx.from.y + (fx.to.y - fx.from.y) * p)
    glow(head, 16f * u, fx.tint, 0.4f)
}

/** Fala uderzeniowa olbrzyma: gruz i kurz pedzace po ziemi od olbrzyma do celu. */
private fun DrawScope.groundWave(fx: ProjectileVfx, p: Float, t: Float) {
    val u = density
    val ground = 30f * u
    val from = Offset(fx.from.x, fx.from.y + ground * 1.6f)
    val to = Offset(fx.to.x, fx.to.y + ground)
    val pos = Offset(from.x + (to.x - from.x) * p, from.y + (to.y - from.y) * p)
    for (k in 0 until 9) {
        val q = p - k * 0.04f
        if (q <= 0f) break
        val pt = Offset(from.x + (to.x - from.x) * q, from.y + (to.y - from.y) * q)
        val age = k / 10f
        drawCircle(Dust.copy(alpha = 0.5f * (1f - age)), (8f + 10f * age) * u, pt + Offset(0f, -age * 8f * u))
    }
    for (i in 0 until 5) {
        val jump = sin((p * 6f + rnd(fx.id, i)) * PI.toFloat()).coerceAtLeast(0f)
        val rock = pos + Offset((rnd(fx.id, i, 1) - 0.5f) * 20f * u, -jump * 14f * u)
        val sz = (2.5f + 2.5f * rnd(fx.id, i, 2)) * u
        drawRect(Color(0xFF5A4632), Offset(rock.x - sz, rock.y - sz), Size(sz * 2f, sz * 2f))
    }
    scale(1f, 0.35f, pivot = pos) {
        drawCircle(fx.tint.copy(alpha = 0.6f), 16f * u, pos, style = Stroke(4f * u))
    }
}

/** Strzala leci lukiem i obraca sie po torze; ze smuga powietrza i poswiata zywiolu. */
private fun DrawScope.arrow(fx: ProjectileVfx, p: Float) {
    val u = density
    val lift = min(distance(fx.from, fx.to) * 0.2f, 64f * u)
    val pos = arcPoint(fx.from, fx.to, p, lift)
    val angle = arcAngle(fx.from, fx.to, p, lift)

    // Smuga za lotka.
    var prev = pos
    for (k in 1..7) {
        val q = p - k * 0.028f
        if (q <= 0f) break
        val pt = arcPoint(fx.from, fx.to, q, lift)
        drawLine(
            color = Color.White.copy(alpha = 0.42f * (1f - k / 8f)),
            start = prev,
            end = pt,
            strokeWidth = 2.2f * u * (1f - k / 9f),
            cap = StrokeCap.Round
        )
        prev = pt
    }

    val degrees = angle * 180f / PI.toFloat()
    rotate(degrees, pivot = pos) {
        if (fx.element != null) {
            drawLine(
                color = fx.tint.copy(alpha = 0.55f),
                start = Offset(pos.x - 20f * u, pos.y),
                end = Offset(pos.x + 14f * u, pos.y),
                strokeWidth = 8f * u,
                cap = StrokeCap.Round
            )
        }
        // Brzechwa.
        drawLine(Color(0xFF6E4222), Offset(pos.x - 21f * u, pos.y), Offset(pos.x + 8f * u, pos.y), 2.6f * u, StrokeCap.Round)
        // Grot.
        val head = Path().apply {
            moveTo(pos.x + 7f * u, pos.y - 4.2f * u)
            lineTo(pos.x + 17f * u, pos.y)
            lineTo(pos.x + 7f * u, pos.y + 4.2f * u)
            close()
        }
        drawPath(head, Color(0xFFE9EEF4))
        drawPath(head, Color(0xFF2C2F36), style = Stroke(0.9f * u))
        // Lotki.
        for (side in listOf(-1f, 1f)) {
            val feather = Path().apply {
                moveTo(pos.x - 13f * u, pos.y)
                lineTo(pos.x - 23f * u, pos.y + side * 5.5f * u)
                lineTo(pos.x - 25f * u, pos.y + side * 1.2f * u)
                lineTo(pos.x - 17f * u, pos.y)
                close()
            }
            drawPath(feather, if (side < 0) Color(0xFFF4EFE4) else Color(0xFFD64A3C))
        }
    }
}

/** Magiczna kula w kolorze zywiolu - kazdy zywiol zostawia inna smuge. */
private fun DrawScope.bolt(fx: ProjectileVfx, p: Float, t: Float, big: Float = 1f) {
    val u = density * big
    val lift = min(distance(fx.from, fx.to) * 0.07f, 22f * density)
    val angle = arcAngle(fx.from, fx.to, p, lift)
    val normal = angle + PI.toFloat() / 2f
    fun at(q: Float): Offset {
        val base = arcPoint(fx.from, fx.to, q, lift)
        val wobble = sin(q * PI.toFloat() * 3f) * 3f * u
        return Offset(base.x + cos(normal) * wobble, base.y + sin(normal) * wobble)
    }
    val pos = at(p)
    val c = fx.tint

    // Smuga czasteczek - kazdy zywiol po swojemu.
    for (k in 1..12) {
        val q = p - k * 0.024f
        if (q <= 0f) break
        val age = k / 13f
        val jitter = Offset((rnd(fx.id, k) - 0.5f) * 7f * u, (rnd(fx.id, k, 1) - 0.5f) * 7f * u)
        val pt = at(q) + jitter
        when (fx.element) {
            Element.Fire -> {
                glow(pt, 9f * u * (1f - age), lerp(Color(0xFFFFE08A), Color(0xFFE0301A), age), 0.8f * (1f - age))
            }
            Element.Water -> {
                drawCircle(Color(0xFF8FD3FF).copy(alpha = 0.8f * (1f - age)), 3.2f * u * (1f - age * 0.6f), pt)
                drawCircle(Color.White.copy(alpha = 0.7f * (1f - age)), 1.1f * u, pt + Offset(-1f * u, -1f * u))
            }
            Element.Nature -> {
                val spin = t * 9f + k
                rotate(spin * 57f, pivot = pt) {
                    drawOval(
                        color = lerp(Color(0xFF9BF06B), Color(0xFF2E8B3A), rnd(fx.id, k, 2)).copy(alpha = 0.9f * (1f - age)),
                        topLeft = Offset(pt.x - 4f * u, pt.y - 1.8f * u),
                        size = Size(8f * u, 3.6f * u)
                    )
                }
            }
            Element.Death -> {
                drawCircle(Smoke.copy(alpha = 0.35f * (1f - age)), (4f + 8f * age) * u, pt)
                drawCircle(c.copy(alpha = 0.5f * (1f - age)), 2f * u * (1f - age), pt)
            }
            null -> sparkle(pt, 4f * u * (1f - age), Color.White.copy(alpha = 0.9f * (1f - age)), t * 4f + k)
        }
    }

    glow(pos, 22f * u, c, 0.6f)
    glow(pos, 11f * u, c, 0.95f)
    drawCircle(
        brush = Brush.radialGradient(listOf(Color.White, c.copy(alpha = 0.9f), c.copy(alpha = 0f)), pos, 8f * u),
        radius = 8f * u,
        center = pos
    )
    when (fx.element) {
        // Jezyki ognia ciagnace sie za kula.
        Element.Fire -> for (k in 0 until 3) {
            val back = angle + PI.toFloat() + (k - 1) * 0.35f + sin(t * 20f + k) * 0.15f
            val tip = polar(pos, back, (13f + 4f * sin(t * 25f + k * 2f)) * u)
            drawLine(Color(0xFFFFB347).copy(alpha = 0.8f), pos, tip, 4.5f * u, StrokeCap.Round)
        }
        // Dwa listki krazace wokol nasiona.
        Element.Nature -> for (k in 0 until 2) {
            val a = t * 10f + k * PI.toFloat()
            val pt = polar(pos, a, 9f * u)
            drawCircle(Color(0xFFCFFFB0), 2.2f * u, pt)
        }
        // Ciemne jadro - kula smierci ma w srodku czern, nie biel.
        Element.Death -> drawCircle(Color(0xFF1A0B26).copy(alpha = 0.8f), 3.2f * u, pos)
        Element.Water -> drawCircle(Color.White.copy(alpha = 0.6f), 9f * u, pos, style = Stroke(1.2f * u))
        null -> Unit
    }
}

private fun DrawScope.fireball(fx: ProjectileVfx, p: Float, t: Float) {
    val u = density
    val lift = distance(fx.from, fx.to) * 0.1f
    val pos = arcPoint(fx.from, fx.to, p, lift)
    val angle = arcAngle(fx.from, fx.to, p, lift)
    // Dym i plomien za kula.
    for (k in 1..14) {
        val q = p - k * 0.022f
        if (q <= 0f) break
        val age = k / 15f
        val pt = arcPoint(fx.from, fx.to, q, lift) +
            Offset((rnd(fx.id, k) - 0.5f) * 10f * u, (rnd(fx.id, k, 3) - 0.5f) * 10f * u - age * 10f * u)
        if (age > 0.45f) {
            drawCircle(Smoke.copy(alpha = 0.3f * (1f - age)), (6f + 10f * age) * u, pt)
        } else {
            glow(pt, (14f - 8f * age) * u, lerp(Color(0xFFFFE9A0), Color(0xFFE8421C), age * 2f), 0.85f)
        }
    }
    for (k in 0 until 4) {
        val back = angle + PI.toFloat() + (k - 1.5f) * 0.3f + sin(t * 22f + k * 1.7f) * 0.18f
        val tip = polar(pos, back, (20f + 6f * sin(t * 27f + k)) * u)
        drawLine(Color(0xFFFF8A2A).copy(alpha = 0.85f), pos, tip, 7f * u, StrokeCap.Round)
        drawLine(Color(0xFFFFE08A).copy(alpha = 0.9f), pos, polar(pos, back, 10f * u), 4f * u, StrokeCap.Round)
    }
    glow(pos, 30f * u, Color(0xFFFF6A1A), 0.65f)
    drawCircle(
        brush = Brush.radialGradient(listOf(Color.White, Color(0xFFFFE08A), Color(0xFFFF7A1A), Color(0x00FF7A1A)), pos, 12f * u),
        radius = 12f * u,
        center = pos
    )
}

private fun DrawScope.poisonGlob(fx: ProjectileVfx, p: Float, t: Float) {
    val u = density
    val lift = distance(fx.from, fx.to) * 0.24f
    val pos = arcPoint(fx.from, fx.to, p, lift)
    // Krople kapiace z lecacej kuli - spadaja z grawitacja od miejsca, w ktorym oderwaly sie od kuli.
    for (k in 1..8) {
        val q = p - k * 0.05f
        if (q <= 0f) break
        val fall = (k * 0.05f).pow(2) * 900f * u
        val pt = arcPoint(fx.from, fx.to, q, lift) + Offset(0f, fall)
        drawCircle(Color(0xFF9BE84A).copy(alpha = 0.8f * (1f - k / 9f)), 2.4f * u, pt)
    }
    glow(pos, 20f * u, Color(0xFF8BE03A), 0.6f)
    val wob = 1f + 0.12f * sin(t * 18f)
    drawOval(
        color = Color(0xFF2E5E12),
        topLeft = Offset(pos.x - 8.5f * u * wob, pos.y - 7.5f * u / wob),
        size = Size(17f * u * wob, 15f * u / wob)
    )
    drawOval(
        brush = Brush.radialGradient(listOf(Color(0xFFD8FF9A), Color(0xFF7AD02A)), pos, 8f * u),
        topLeft = Offset(pos.x - 7f * u * wob, pos.y - 6f * u / wob),
        size = Size(14f * u * wob, 12f * u / wob)
    )
    drawCircle(Color.White.copy(alpha = 0.7f), 2f * u, pos + Offset(-2.5f * u, -2.5f * u))
}

private fun DrawScope.arcaneBolt(fx: ProjectileVfx, p: Float, t: Float) {
    val u = density
    val lift = distance(fx.from, fx.to) * 0.06f
    val pos = arcPoint(fx.from, fx.to, p, lift)
    for (k in 1..10) {
        val q = p - k * 0.03f
        if (q <= 0f) break
        val age = k / 11f
        val pt = arcPoint(fx.from, fx.to, q, lift) +
            Offset((rnd(fx.id, k) - 0.5f) * 12f * u, (rnd(fx.id, k, 5) - 0.5f) * 12f * u)
        sparkle(pt, 5f * u * (1f - age), lerp(Color.White, fx.tint, age).copy(alpha = 1f - age), t * 3f + k)
    }
    glow(pos, 28f * u, fx.tint, 0.7f)
    rotate(t * 360f, pivot = pos) { sparkle(pos, 15f * u, Color.White.copy(alpha = 0.9f)) }
    rotate(-t * 240f, pivot = pos) { sparkle(pos, 10f * u, fx.tint.copy(alpha = 0.9f), PI.toFloat() / 4f) }
    drawCircle(Color.White, 4f * u, pos)
}

/* ============================================================ trafienia */

private fun DrawScope.drawImpact(fx: ImpactVfx, p: Float, t: Float) {
    when (fx.type) {
        ImpactType.Slash -> slash(fx, p)
        ImpactType.Pierce -> pierce(fx, p)
        ImpactType.Burst -> burst(fx, p, t)
        ImpactType.Smash -> smash(fx, p, heavy = false)
        ImpactType.Heal -> heal(fx, p, t)
        ImpactType.Shield -> shieldBubble(fx, p, t)
        ImpactType.Block -> block(fx, p)
        ImpactType.Dodge -> dodge(fx, p)
        ImpactType.Thorns -> thorns(fx, p)
        ImpactType.SkyStrike -> skyStrike(fx, p, t)
        ImpactType.Poison -> poisonCloud(fx, p, t)
        ImpactType.Death -> death(fx, p, t)
        ImpactType.Dust -> dustPuff(fx, p)
        ImpactType.Landing -> landing(fx, p, heavy = false)
        ImpactType.BossLanding -> landing(fx, p, heavy = true)
    }
}

/** Krzyzujace sie smugi ciecia, iskry lecace w strone ciosu i bialy blysk. */
private fun DrawScope.slash(fx: ImpactVfx, p: Float) {
    val u = density
    val s = fx.scale * (if (fx.crit) 1.3f else 1f)
    val r = 36f * u * s
    val c = fx.at
    val draw = (p / 0.28f).coerceAtMost(1f)
    val fade = tail(p, 0.3f)
    val count = if (fx.crit) 3 else 2

    if (p < 0.22f) glow(c, r * 0.9f, Color.White, 0.9f * (1f - p / 0.22f))

    for (k in 0 until count) {
        val tilt = (if (fx.dir > 0) 32f else -32f) + k * (if (fx.dir > 0) -64f else 64f) + (k - 1) * 8f
        val local = ((draw - k * 0.18f) / (1f - k * 0.18f)).coerceIn(0f, 1f)
        if (local <= 0f) continue
        val grow = 1f + 0.18f * easeOut(p)
        rotate(tilt, pivot = c) {
            val topLeft = Offset(c.x - r * grow, c.y - r * 0.42f * grow)
            val size = Size(2f * r * grow, 0.84f * r * grow)
            val start = if (fx.dir > 0) 180f else 0f
            val sweep = (if (fx.dir > 0) 180f else -180f) * local
            drawArc(fx.tint.copy(alpha = 0.55f * fade), start, sweep, false, topLeft, size, style = Stroke(10f * u * s * fade, cap = StrokeCap.Round))
            drawArc(Color.White.copy(alpha = 0.95f * fade), start, sweep, false, topLeft, size, style = Stroke(3.6f * u * s * fade, cap = StrokeCap.Round))
        }
    }

    val sparks = if (fx.crit) 14 else 9
    val travel = easeOut(p) * r * 1.7f
    for (i in 0 until sparks) {
        val a = (if (fx.dir > 0) 0f else PI.toFloat()) + (rnd(fx.id, i) - 0.5f) * PI.toFloat() * 0.95f
        val d = travel * (0.5f + 0.7f * rnd(fx.id, i, 1))
        val head = polar(c, a, d)
        val tailPt = polar(c, a, d - 9f * u * (1f - p))
        drawLine(
            color = (if (rnd(fx.id, i, 2) < 0.5f) Color.White else Color(0xFFFFE08A)).copy(alpha = tail(p, 0.2f)),
            start = tailPt,
            end = head,
            strokeWidth = 2.2f * u,
            cap = StrokeCap.Round
        )
    }
    if (fx.crit) starburst(c, r * 1.9f, p, fx.tint)
}

/** Blysk krytyka: dlugie, cienkie promienie na chwile rozchodza sie od punktu trafienia. */
private fun DrawScope.starburst(c: Offset, reach: Float, p: Float, tint: Color) {
    val grow = easeOut(p / 0.3f)
    val alpha = tail(p, 0.15f)
    for (k in 0 until 10) {
        val a = k * (PI.toFloat() / 5f) + 0.2f
        val len = reach * grow * (if (k % 2 == 0) 1f else 0.6f)
        val path = Path().apply {
            val side = polar(c, a + PI.toFloat() / 2f, 3f * density)
            val side2 = polar(c, a - PI.toFloat() / 2f, 3f * density)
            moveTo(side.x, side.y)
            val tip = polar(c, a, len)
            lineTo(tip.x, tip.y)
            lineTo(side2.x, side2.y)
            close()
        }
        drawPath(path, lerp(Color.White, tint, 0.3f).copy(alpha = 0.85f * alpha))
    }
}

private fun DrawScope.pierce(fx: ImpactVfx, p: Float) {
    val u = density
    val c = fx.at
    val s = fx.scale * (if (fx.crit) 1.3f else 1f)
    glow(c, 20f * u * s, if (fx.crit) Color(0xFFFFE08A) else fx.tint, 0.9f * tail(p, 0.05f) * (1f - p))
    drawCircle(Color.White.copy(alpha = (1f - p) * 0.8f), 8f * u * s + 26f * u * s * easeOut(p), c, style = Stroke(2.5f * u * (1f - p)))
    // Drzazgi odbijaja sie DO TYLU - strzala przyszla z przeciwnej strony.
    for (i in 0 until 8) {
        val a = (if (fx.dir > 0) PI.toFloat() else 0f) + (rnd(fx.id, i) - 0.5f) * 2.2f - 0.4f
        val sec = p * 0.5f
        val v = (90f + 110f * rnd(fx.id, i, 1)) * u * s
        val pos = Offset(c.x + cos(a) * v * sec, c.y + sin(a) * v * sec + 400f * u * sec * sec)
        drawLine(
            color = (if (i % 3 == 0) Color(0xFFF0E6D0) else Color(0xFF8A5A30)).copy(alpha = 1f - p),
            start = pos,
            end = pos + Offset(cos(a) * 5f * u, sin(a) * 5f * u),
            strokeWidth = 2f * u,
            cap = StrokeCap.Round
        )
    }
    if (fx.crit) starburst(c, 50f * u * s, p, Color(0xFFFFE08A))
}

/** Wybuch kuli - blysk, fala i czasteczki zaleznie od zywiolu. */
private fun DrawScope.burst(fx: ImpactVfx, p: Float, t: Float) {
    val u = density
    val s = fx.scale * (if (fx.crit) 1.35f else 1f)
    val r = 40f * u * s
    val c = fx.at
    val e = easeOut(p)
    val tint = fx.tint

    val core = (1f - p / 0.4f).coerceIn(0f, 1f)
    drawCircle(
        brush = Brush.radialGradient(
            listOf(Color.White.copy(alpha = core), tint.copy(alpha = core * 0.9f), tint.copy(alpha = 0f)),
            c,
            r * (0.45f + 0.7f * e)
        ),
        radius = r * (0.45f + 0.7f * e),
        center = c
    )
    drawCircle(tint.copy(alpha = (1f - p) * 0.9f), r * (0.3f + 1.15f * e), c, style = Stroke((9f * (1f - e) + 1f) * u * s))

    val count = if (fx.crit) 22 else 15
    when (fx.element) {
        Element.Fire -> {
            for (i in 0 until count) {
                val a = -PI.toFloat() / 2f + (rnd(fx.id, i) - 0.5f) * PI.toFloat() * 1.9f
                val sec = p * 0.9f
                val v = (120f + 140f * rnd(fx.id, i, 1)) * u * s
                val pos = Offset(c.x + cos(a) * v * sec, c.y + sin(a) * v * sec + 160f * u * sec * sec)
                drawCircle(lerp(Color(0xFFFFE08A), Color(0xFFE8421C), rnd(fx.id, i, 2)).copy(alpha = 1f - p), (1.6f + 2.4f * rnd(fx.id, i, 3)) * u * (1f - p * 0.6f), pos)
            }
            // Dym zostaje po wybuchu i wznosi sie.
            for (i in 0 until 5) {
                val local = ((p - 0.2f) / 0.8f).coerceIn(0f, 1f)
                if (local <= 0f) continue
                val pos = Offset(c.x + (rnd(fx.id, i, 4) - 0.5f) * r, c.y - local * 30f * u * s - rnd(fx.id, i, 5) * 10f * u)
                drawCircle(Smoke.copy(alpha = 0.35f * (1f - local)), (10f + 16f * local) * u * s, pos)
            }
        }
        Element.Water -> {
            for (i in 0 until count) {
                val a = -PI.toFloat() / 2f + (rnd(fx.id, i) - 0.5f) * PI.toFloat() * 1.3f
                val sec = p * 0.8f
                val v = (140f + 120f * rnd(fx.id, i, 1)) * u * s
                val pos = Offset(c.x + cos(a) * v * sec, c.y + sin(a) * v * sec + 520f * u * sec * sec)
                drawCircle(Color(0xFF8FD3FF).copy(alpha = 1f - p), (2f + 2.5f * rnd(fx.id, i, 2)) * u, pos)
                drawCircle(Color.White.copy(alpha = (1f - p) * 0.8f), 0.9f * u, pos + Offset(-0.8f * u, -0.8f * u))
            }
            scale(1f, 0.35f, pivot = Offset(c.x, c.y + r * 0.6f)) {
                drawCircle(Color(0xFFBFE6FF).copy(alpha = (1f - p) * 0.8f), r * (0.4f + 1.3f * e), Offset(c.x, c.y + r * 0.6f), style = Stroke(4f * u))
            }
        }
        Element.Nature -> {
            for (i in 0 until count) {
                val a = rnd(fx.id, i) * 2f * PI.toFloat()
                val d = r * (0.3f + 1.2f * rnd(fx.id, i, 1)) * e
                val drift = p * p * 30f * u
                val pos = polar(c, a, d) + Offset(0f, drift)
                rotate(t * 200f + i * 40f, pivot = pos) {
                    drawOval(
                        color = lerp(Color(0xFFB9F57A), Color(0xFF2E8B3A), rnd(fx.id, i, 2)).copy(alpha = 1f - p),
                        topLeft = Offset(pos.x - 5f * u, pos.y - 2.2f * u),
                        size = Size(10f * u, 4.4f * u)
                    )
                }
            }
            for (i in 0 until 6) sparkle(polar(c, rnd(fx.id, i, 7) * 6.28f, r * e * 0.8f), 5f * u * (1f - p), Color(0xFFE8FFD0).copy(alpha = 1f - p), t * 3f)
        }
        Element.Death -> {
            for (i in 0 until 9) {
                val a = rnd(fx.id, i) * 2f * PI.toFloat()
                val pos = polar(c, a, r * 0.9f * e) + Offset(0f, -p * 22f * u)
                drawCircle(Smoke.copy(alpha = 0.45f * (1f - p)), (8f + 14f * e) * u * s, pos)
                drawCircle(tint.copy(alpha = 0.3f * (1f - p)), (4f + 8f * e) * u * s, pos)
            }
            for (i in 0 until count) {
                val a = rnd(fx.id, i, 3) * 2f * PI.toFloat()
                val pos = polar(c, a + p * 1.5f, r * (0.2f + 1.1f * e))
                drawCircle(Color(0xFFE0C2FF).copy(alpha = 1f - p), 1.8f * u, pos)
            }
        }
        null -> for (i in 0 until count) {
            val a = rnd(fx.id, i) * 2f * PI.toFloat()
            val pos = polar(c, a, r * (0.3f + 1.2f * rnd(fx.id, i, 1)) * e)
            sparkle(pos, (4f + 4f * rnd(fx.id, i, 2)) * u * (1f - p), lerp(Color.White, tint, rnd(fx.id, i, 3)).copy(alpha = 1f - p), t * 5f + i)
        }
    }
    if (fx.crit) starburst(c, r * 2f, p, tint)
}

/** Olbrzym uderza w ziemie: fala po podlozu, pekniecia, gruz i kurz. */
private fun DrawScope.smash(fx: ImpactVfx, p: Float, heavy: Boolean) {
    val u = density
    val s = fx.scale * (if (fx.crit) 1.25f else 1f) * (if (heavy) 1.6f else 1f)
    val ground = Offset(fx.at.x, fx.at.y + 26f * u * s)
    val e = easeOut(p)
    glow(fx.at, 44f * u * s, Color.White, 0.9f * (1f - p / 0.25f).coerceIn(0f, 1f))
    scale(1f, 0.3f, pivot = ground) {
        drawCircle(Color(0xFFF2E6CC).copy(alpha = (1f - p) * 0.9f), 20f * u * s + 110f * u * s * e, ground, style = Stroke(10f * u * (1f - e) + 1.5f * u))
        drawCircle(fx.tint.copy(alpha = (1f - p) * 0.6f), 10f * u * s + 70f * u * s * e, ground, style = Stroke(6f * u * (1f - e) + 1f * u))
    }
    // Pekniecia rozchodzace sie po ziemi.
    val cracks = (p / 0.2f).coerceAtMost(1f)
    for (k in 0 until 6) {
        val side = if (k % 2 == 0) -1f else 1f
        var pt = ground
        val path = Path().apply { moveTo(pt.x, pt.y) }
        for (seg in 1..4) {
            pt = Offset(pt.x + side * (9f + 8f * rnd(fx.id, k * 4 + seg)) * u * s * cracks, pt.y + (rnd(fx.id, k * 4 + seg, 1) - 0.5f) * 8f * u)
            path.lineTo(pt.x, pt.y)
        }
        drawPath(path, Color(0xFF1E140C).copy(alpha = 0.8f * tail(p, 0.5f)), style = Stroke(2f * u, cap = StrokeCap.Round))
    }
    for (i in 0 until 10) {
        val a = -PI.toFloat() / 2f + (rnd(fx.id, i) - 0.5f) * PI.toFloat() * 1.2f
        val sec = p * 0.9f
        val v = (130f + 150f * rnd(fx.id, i, 1)) * u * s
        val pos = Offset(ground.x + cos(a) * v * sec, ground.y + sin(a) * v * sec + 700f * u * sec * sec)
        val sz = (2.5f + 3f * rnd(fx.id, i, 2)) * u
        drawRect(Color(0xFF5A4632).copy(alpha = 1f - p), Offset(pos.x - sz, pos.y - sz), Size(sz * 2f, sz * 2f))
    }
    for (i in 0 until 8) {
        val side = if (i % 2 == 0) -1f else 1f
        val pos = Offset(ground.x + side * (20f + 60f * rnd(fx.id, i, 5)) * u * s * e, ground.y - 8f * u * rnd(fx.id, i, 6) * e)
        drawCircle(Dust.copy(alpha = 0.45f * (1f - p)), (8f + 14f * e) * u * s, pos)
    }
}

private fun DrawScope.heal(fx: ImpactVfx, p: Float, t: Float) {
    val u = density
    val s = fx.scale
    val c = fx.at
    val env = envelope(p, 0.15f, 0.55f)
    val green = Color(0xFF6BF08A)
    // Slup swiatla nad celem.
    val w = 36f * u * s
    drawRect(
        brush = Brush.verticalGradient(
            listOf(Color.Transparent, green.copy(alpha = 0.38f * env), Color.White.copy(alpha = 0.3f * env)),
            startY = c.y - 130f * u * s,
            endY = c.y + 28f * u * s
        ),
        topLeft = Offset(c.x - w / 2f, c.y - 130f * u * s),
        size = Size(w, 158f * u * s)
    )
    val feet = Offset(c.x, c.y + 28f * u * s)
    scale(1f, 0.32f, pivot = feet) {
        drawCircle(green.copy(alpha = 0.8f * (1f - p)), 16f * u * s + 34f * u * s * easeOut(p), feet, style = Stroke(3.5f * u))
    }
    // Wznoszace sie krzyzyki.
    for (i in 0 until 8) {
        val local = ((p - rnd(fx.id, i) * 0.35f) / 0.65f).coerceIn(0f, 1f)
        if (local <= 0f || local >= 1f) continue
        val pos = Offset(
            c.x + (rnd(fx.id, i, 1) - 0.5f) * 56f * u * s + sin(t * 4f + i) * 4f * u,
            c.y + 20f * u * s - local * (60f + 40f * rnd(fx.id, i, 2)) * u * s
        )
        val arm = (4f + 3f * rnd(fx.id, i, 3)) * u
        val a = sin(local * PI.toFloat())
        val col = (if (i % 3 == 0) Color.White else green).copy(alpha = a)
        drawLine(col, Offset(pos.x - arm, pos.y), Offset(pos.x + arm, pos.y), 2.6f * u, StrokeCap.Round)
        drawLine(col, Offset(pos.x, pos.y - arm), Offset(pos.x, pos.y + arm), 2.6f * u, StrokeCap.Round)
    }
    glow(c, 34f * u * s, green, 0.4f * env)
}

private fun DrawScope.shieldBubble(fx: ImpactVfx, p: Float, t: Float) {
    val u = density
    val s = fx.scale
    val c = fx.at
    val pop = easeOutBack((p / 0.3f).coerceAtMost(1f))
    val alpha = tail(p, 0.55f)
    val r = 40f * u * s * pop
    val blue = Color(0xFF7FC8FF)
    drawCircle(
        brush = Brush.radialGradient(listOf(Color.Transparent, blue.copy(alpha = 0.12f * alpha), blue.copy(alpha = 0.4f * alpha)), c, r.coerceAtLeast(1f)),
        radius = r.coerceAtLeast(1f),
        center = c
    )
    drawCircle(Color.White.copy(alpha = 0.8f * alpha), r.coerceAtLeast(1f), c, style = Stroke(2.5f * u))
    // Szesciokat i szprychy - tarcza ma wyglad magicznej bariery, nie baniaka.
    val hex = Path().apply {
        for (k in 0..5) {
            val pt = polar(c, k * PI.toFloat() / 3f + t * 0.8f, r * 0.8f)
            if (k == 0) moveTo(pt.x, pt.y) else lineTo(pt.x, pt.y)
        }
        close()
    }
    drawPath(hex, blue.copy(alpha = 0.7f * alpha), style = Stroke(1.6f * u))
    // Przesuwajacy sie polysk po obwodzie.
    drawArc(Color.White.copy(alpha = 0.9f * alpha), t * 220f, 50f, false, Offset(c.x - r, c.y - r), Size(r * 2f, r * 2f), style = Stroke(4f * u, cap = StrokeCap.Round))
}

/** Garda: jasna bariera po stronie ciosu i iskry odbite z powrotem w napastnika. */
private fun DrawScope.block(fx: ImpactVfx, p: Float) {
    val u = density
    val s = fx.scale
    val side = -fx.dir
    val c = Offset(fx.at.x + side * 18f * u * s, fx.at.y)
    val alpha = tail(p, 0.25f)
    val pop = easeOutBack((p / 0.2f).coerceAtMost(1f))
    val h = 34f * u * s * pop
    drawArc(Color(0xFF9FD8FF).copy(alpha = 0.5f * alpha), if (side > 0) -70f else 110f, 140f, false, Offset(c.x - h * 0.6f, c.y - h), Size(h * 1.2f, h * 2f), style = Stroke(12f * u, cap = StrokeCap.Round))
    drawArc(Color.White.copy(alpha = 0.95f * alpha), if (side > 0) -70f else 110f, 140f, false, Offset(c.x - h * 0.6f, c.y - h), Size(h * 1.2f, h * 2f), style = Stroke(3.5f * u, cap = StrokeCap.Round))
    for (i in 0 until 12) {
        val a = (if (side > 0) 0f else PI.toFloat()) + (rnd(fx.id, i) - 0.5f) * 2.4f
        val sec = p * 0.5f
        val v = (160f + 140f * rnd(fx.id, i, 1)) * u
        val pos = Offset(c.x + cos(a) * v * sec, c.y + sin(a) * v * sec + 500f * u * sec * sec)
        drawLine(Color(0xFFFFF0B0).copy(alpha = 1f - p), pos, pos - Offset(cos(a) * 7f * u, sin(a) * 7f * u), 2f * u, StrokeCap.Round)
    }
}

/** Unik: smugi szybkosci przy celu - "swist", ktory minal. */
private fun DrawScope.dodge(fx: ImpactVfx, p: Float) {
    val u = density
    val c = fx.at
    val alpha = tail(p, 0.2f)
    for (k in 0 until 5) {
        val y = c.y + (k - 2) * 9f * u
        val len = (30f + 20f * rnd(fx.id, k)) * u
        val x0 = c.x - fx.dir * (p * 40f * u) + (rnd(fx.id, k, 1) - 0.5f) * 20f * u
        drawLine(Color.White.copy(alpha = 0.8f * alpha), Offset(x0, y), Offset(x0 - fx.dir * len, y), 2f * u, StrokeCap.Round)
    }
}

private fun DrawScope.thorns(fx: ImpactVfx, p: Float) {
    val u = density
    val c = fx.at
    val grow = if (p < 0.35f) easeOut(p / 0.35f) else 1f - (p - 0.35f) / 0.65f
    glow(c, 30f * u, Color(0xFFFF4A3A), 0.6f * (1f - p))
    for (k in 0 until 10) {
        val a = k * (PI.toFloat() / 5f) + rnd(fx.id, k) * 0.3f
        val len = (22f + 10f * rnd(fx.id, k, 1)) * u * grow
        val base1 = polar(c, a + 0.18f, 8f * u)
        val base2 = polar(c, a - 0.18f, 8f * u)
        val tip = polar(c, a, 8f * u + len)
        val path = Path().apply { moveTo(base1.x, base1.y); lineTo(tip.x, tip.y); lineTo(base2.x, base2.y); close() }
        drawPath(path, Color(0xFF7A1A12).copy(alpha = 1f - p * 0.5f))
        drawLine(Color(0xFFFF8A6B).copy(alpha = 1f - p), c, tip, 1.2f * u)
    }
}

/** Ulti: slup swiatla wbija sie z nieba w cel, potem zloty wybuch. */
private fun DrawScope.skyStrike(fx: ImpactVfx, p: Float, t: Float) {
    val u = density
    val c = fx.at
    val beamIn = (p / 0.22f).coerceAtMost(1f)
    val beamAlpha = if (p < 0.22f) 1f else tail(p, 0.3f)
    val w = 30f * u * fx.scale * (1f - 0.5f * p)
    val top = c.y - (c.y + 400f) * (1f - beamIn) - 2000f
    drawRect(
        brush = Brush.horizontalGradient(
            listOf(Color.Transparent, fx.tint.copy(alpha = 0.7f * beamAlpha), Color.White.copy(alpha = beamAlpha), fx.tint.copy(alpha = 0.7f * beamAlpha), Color.Transparent),
            startX = c.x - w,
            endX = c.x + w
        ),
        topLeft = Offset(c.x - w, top),
        size = Size(w * 2f, c.y - top)
    )
    if (p > 0.18f) {
        val local = (p - 0.18f) / 0.82f
        burst(
            ImpactVfx(fx.id, fx.startNs, fx.durationNs, ImpactType.Burst, c, fx.tint, fx.dir, fx.scale * 1.2f, false, null),
            local,
            t
        )
    }
}

private fun DrawScope.poisonCloud(fx: ImpactVfx, p: Float, t: Float) {
    val u = density
    val c = fx.at
    val env = envelope(p, 0.2f, 0.5f)
    for (i in 0 until 7) {
        val a = rnd(fx.id, i) * 2f * PI.toFloat()
        val pos = polar(c, a, (10f + 22f * p) * u) + Offset(0f, -p * 20f * u)
        drawCircle(Color(0xFF6FAE2A).copy(alpha = 0.35f * env), (12f + 10f * p) * u * fx.scale, pos)
    }
    for (i in 0 until 9) {
        val local = ((p - rnd(fx.id, i, 1) * 0.4f) / 0.6f).coerceIn(0f, 1f)
        if (local <= 0f || local >= 1f) continue
        val pos = Offset(c.x + (rnd(fx.id, i, 2) - 0.5f) * 40f * u + sin(t * 5f + i) * 3f * u, c.y + 10f * u - local * 50f * u)
        drawCircle(Color(0xFFC8FF7A).copy(alpha = 1f - local), (2f + 2.5f * rnd(fx.id, i, 3)) * u, pos, style = Stroke(1.3f * u))
    }
}

/** Smierc: wybuch czasteczek w kolorze zywiolu, dusza unoszaca sie do gory i kurz u stop. */
private fun DrawScope.death(fx: ImpactVfx, p: Float, t: Float) {
    val u = density
    val c = fx.at
    val s = fx.scale
    val e = easeOut(p)
    glow(c, 50f * u * s, fx.tint, 0.7f * (1f - p / 0.3f).coerceIn(0f, 1f))
    for (i in 0 until 18) {
        val a = rnd(fx.id, i) * 2f * PI.toFloat()
        val d = (20f + 50f * rnd(fx.id, i, 1)) * u * s * e
        val pos = polar(c, a, d) + Offset(0f, -p * 26f * u)
        drawCircle(lerp(Color.White, fx.tint, rnd(fx.id, i, 2)).copy(alpha = (1f - p) * 0.9f), (1.5f + 2.5f * rnd(fx.id, i, 3)) * u * (1f - p * 0.5f), pos)
    }
    val soul = ((p - 0.12f) / 0.88f).coerceIn(0f, 1f)
    if (soul > 0f) {
        val pos = Offset(c.x + sin(t * 3f) * 6f * u, c.y - soul * 90f * u * s)
        val alpha = envelope(soul, 0.2f, 0.55f)
        for (k in 1..5) {
            drawCircle(Color.White.copy(alpha = 0.25f * alpha * (1f - k / 6f)), (7f - k) * u, pos + Offset(0f, k * 6f * u))
        }
        glow(pos, 18f * u, Color.White, 0.8f * alpha)
        drawCircle(Color.White.copy(alpha = alpha), 4.5f * u, pos)
    }
    val feet = Offset(c.x, c.y + 30f * u * s)
    scale(1f, 0.3f, pivot = feet) {
        drawCircle(Dust.copy(alpha = 0.6f * (1f - p)), 20f * u * s + 40f * u * s * e, feet, style = Stroke(5f * u * (1f - e) + 1f))
    }
}

private fun DrawScope.dustPuff(fx: ImpactVfx, p: Float) {
    val u = density
    val e = easeOut(p)
    for (i in 0 until 3) {
        val pos = Offset(
            fx.at.x - fx.dir * (4f + 10f * rnd(fx.id, i)) * u * (0.4f + e),
            fx.at.y - (2f + 8f * rnd(fx.id, i, 1)) * u * e
        )
        drawCircle(Dust.copy(alpha = 0.35f * (1f - p)), (4f + 6f * e) * u * fx.scale, pos)
    }
}

private fun DrawScope.landing(fx: ImpactVfx, p: Float, heavy: Boolean) {
    val u = density
    val s = fx.scale * (if (heavy) 2.2f else 1f)
    val e = easeOut(p)
    val feet = fx.at
    scale(1f, 0.28f, pivot = feet) {
        drawCircle(Dust.copy(alpha = 0.7f * (1f - p)), 10f * u * s + 46f * u * s * e, feet, style = Stroke((7f * (1f - e) + 1f) * u))
    }
    val puffs = if (heavy) 16 else 8
    for (i in 0 until puffs) {
        val side = if (i % 2 == 0) -1f else 1f
        val pos = Offset(feet.x + side * (8f + 34f * rnd(fx.id, i)) * u * s * e, feet.y - (3f + 10f * rnd(fx.id, i, 1)) * u * e)
        drawCircle(Dust.copy(alpha = 0.5f * (1f - p)), (5f + 9f * e) * u * s * (0.6f + 0.4f * rnd(fx.id, i, 2)), pos)
    }
    if (heavy) {
        smash(ImpactVfx(fx.id, fx.startNs, fx.durationNs, ImpactType.Smash, Offset(feet.x, feet.y - 26f * u * 1.6f), fx.tint, 1f, 1.2f, false, null), p, heavy = true)
    }
}
