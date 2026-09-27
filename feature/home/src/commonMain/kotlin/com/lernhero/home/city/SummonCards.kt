package com.lernhero.home.city

import androidx.compose.animation.core.EaseInCubic
import androidx.compose.animation.core.EaseOutBack
import androidx.compose.animation.core.EaseOutCubic
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lernhero.domain.common.Rarity
import com.lernhero.domain.monster.CompanionPower
import com.lernhero.domain.monster.MonsterCard
import com.lernhero.domain.monster.MonsterLevels
import com.lernhero.domain.monster.MonsterStars
import com.lernhero.game.character.ElementEmblem
import com.lernhero.game.character.MonsterVisualPresets
import com.lernhero.home.ui.ChunkyButton
import com.lernhero.home.ui.RarityPalette
import com.lernhero.home.ui.darkenBy
import com.lernhero.home.ui.lightenBy
import com.lernhero.shared.theme.FontFirst
import com.lernhero.shared.theme.StrokedText
import org.jetbrains.compose.resources.painterResource
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

/*
 * Karty losowania: duza karta kompana (x1, w stylu karty FIFA) i stol z dziesiecioma kartami (x10).
 * Wszystkie pozycje sa w ukladzie nakladki na caly ekran - licza sie z punktow sceny oltarza.
 */

private val BigCardWidth = 214.dp
private val BigCardHeight = 300.dp
private val MiniCardWidth = 80.dp
private val MiniCardHeight = 112.dp

private fun stepOf(details: Float, step: Int) = (details - (step - 1)).coerceIn(0f, 1f)

private fun lerpF(a: Float, b: Float, t: Float) = a + (b - a) * t

/* ------------------------------------------------------------------ x1: duza karta */

/**
 * Karta kompana na srodku ekranu. Wylatuje z sylwetki, obraca sie z rewersu (zlota gwiazda
 * oltarza) na awers, a szczegoly wbijaja sie po kolei jak przy walkoucie w FIFA: rzadkosc,
 * zywiol, imie, statystyki (liczone od zera), na koncu "NEW!" albo "+1 COPY".
 *
 * [origin] - skad wylatuje (srodek sylwetki), [target] - srodek karty, [stowTo] - zakladka
 * Collection, do ktorej karta wpada po tapnieciu. Wszystko w pikselach nakladki.
 */
@Composable
internal fun RevealCardOverlay(
    show: SummonShow,
    card: MonsterCard,
    fresh: Boolean,
    origin: () -> Offset,
    target: () -> Offset,
    stowTo: () -> Offset,
    maxHeightPx: () -> Float,
    time: State<Float>,
    modifier: Modifier = Modifier
) {
    val density = LocalDensity.current
    val noRipple = remember { MutableInteractionSource() }
    Box(modifier = modifier.fillMaxSize()) {
        // Przyciemnienie pod karta - calosc jest jednym wielkim przyciskiem "dalej".
        Box(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    alpha = (show.card.value.coerceIn(0f, 1f) * (1f - show.stow.value)) * 0.6f
                }
                .background(Color.Black)
                .clickable(interactionSource = noRipple, indication = null) { show.tapCard() }
        )

        Box(
            modifier = Modifier
                .size(BigCardWidth, BigCardHeight)
                .graphicsLayer {
                    val p = show.card.value
                    val s = show.stow.value
                    val from = origin()
                    val to = target()
                    val home = stowTo()
                    var cx = lerpF(from.x, to.x, p)
                    var cy = lerpF(from.y, to.y, p)
                    cx = lerpF(cx, home.x, s)
                    cy = lerpF(cy, home.y, s)
                    translationX = cx - size.width / 2f
                    translationY = cy - size.height / 2f
                    val fit = (maxHeightPx() * 0.86f / size.height).coerceAtMost(1f)
                    val scale = (0.16f + 0.84f * p) * (1f - 0.86f * s) * fit
                    scaleX = scale
                    scaleY = scale
                    rotationZ = (1f - p.coerceIn(0f, 1f)) * -14f + s * 12f
                    alpha = (p * 4f).coerceIn(0f, 1f) * (1f - s * s)
                }
                .clickable(interactionSource = noRipple, indication = null) { show.tapCard() }
        ) {
            if (show.flip.value < 0.5f) {
                CardBack(
                    rarity = card.rarity,
                    modifier = Modifier.fillMaxSize().graphicsLayer {
                        rotationY = -show.flip.value * 180f
                        cameraDistance = 14f * density.density
                    }
                )
            } else {
                CardFront(
                    card = card,
                    fresh = fresh,
                    details = show.details.value,
                    time = time,
                    modifier = Modifier.fillMaxSize().graphicsLayer {
                        val t = time.value
                        // Po obrocie karta lekko "plywa" - wyglada na trzymana w reku, a nie naklejona.
                        rotationY = (1f - show.flip.value) * 180f + sin(t * 1.3f) * 5f
                        rotationX = cos(t * 1.1f) * 3.5f
                        cameraDistance = 14f * density.density
                    }
                )
            }
        }

        if (show.cardReady) {
            StrokedText(
                text = "Tap to continue",
                size = 13.sp,
                color = Color.White,
                outline = Color(0xFF120B05),
                strokeWidth = 5f,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .offset {
                        val to = target()
                        val half = (BigCardHeight.toPx() / 2f) * (maxHeightPx() * 0.86f / BigCardHeight.toPx()).coerceAtMost(1f)
                        IntOffset(0, (to.y + half + 14.dp.toPx()).roundToInt())
                    }
                    .graphicsLayer {
                        alpha = (0.55f + 0.45f * sin(time.value * 4f)) * (1f - show.stow.value)
                    }
            )
        }
    }
}

/** Awers duzej karty. */
@Composable
private fun CardFront(card: MonsterCard, fresh: Boolean, details: Float, time: State<Float>, modifier: Modifier = Modifier) {
    val fx = card.rarity.fx()
    val frame = RarityPalette.frame(card.rarity)
    val preset = MonsterVisualPresets.forSpecies(card.spriteKey)
    val hp = CompanionPower.hpAt(card, MonsterStars.MIN_STARS, MonsterLevels.MIN_LEVEL)
    val attack = CompanionPower.attackAt(card, MonsterStars.MIN_STARS, MonsterLevels.MIN_LEVEL)
    val defense = (CompanionPower.armorAt(card) * 100).roundToInt()
    val shiny = card.rarity >= Rarity.Rare

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(18.dp))
            .background(Brush.linearGradient(listOf(fx.lightenBy(0.45f), frame, fx.darkenBy(0.5f), frame.lightenBy(0.2f))))
            .padding(4.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(Brush.verticalGradient(listOf(frame.darkenBy(0.5f), Color(0xFF150F20), Color(0xFF0B0812))))
            .drawWithContent {
                drawContent()
                if (shiny) holoSweep(time.value)
            }
    ) {
        // Promienie i poswiata za kompanem - wewnatrz karty.
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2f, size.height * 0.4f)
            val radius = size.width * 0.75f
            if (shiny) {
                rotate(degrees = time.value * 14f, pivot = center) {
                    for (i in 0 until 12) {
                        val angle = i * (PI.toFloat() / 6f)
                        val half = 0.11f
                        val ray = Path().apply {
                            moveTo(center.x, center.y)
                            lineTo(center.x + cos(angle - half) * radius, center.y + sin(angle - half) * radius)
                            lineTo(center.x + cos(angle + half) * radius, center.y + sin(angle + half) * radius)
                            close()
                        }
                        drawPath(ray, Brush.radialGradient(listOf(fx.copy(alpha = 0.3f), Color.Transparent), center, radius))
                    }
                }
            }
            drawCircle(Brush.radialGradient(listOf(fx.copy(alpha = 0.5f), Color.Transparent), center, radius * 0.7f), radius * 0.7f, center)
        }

        Column(
            modifier = Modifier.fillMaxSize().padding(horizontal = 10.dp, vertical = 9.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(modifier = Modifier.fillMaxWidth().padding(top = 10.dp), verticalAlignment = Alignment.Top) {
                Stamp(stepOf(details, 2)) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        ElementEmblem(element = card.element, size = 32.dp)
                        Text(
                            text = card.element.label.uppercase(),
                            color = Color(card.element.colorHex).lightenBy(0.3f),
                            fontFamily = FontFirst(),
                            fontWeight = FontWeight.Black,
                            fontSize = 8.sp,
                            lineHeight = 9.sp
                        )
                    }
                }
                Spacer(modifier = Modifier.weight(1f))
            }

            Box(modifier = Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                Image(
                    painter = painterResource(preset.drawable),
                    contentDescription = card.name,
                    modifier = Modifier
                        .fillMaxHeight()
                        .graphicsLayer {
                            translationY = sin(time.value * 2f) * 3.dp.toPx()
                            scaleX = preset.visualScale.coerceAtMost(1.15f)
                            scaleY = preset.visualScale.coerceAtMost(1.15f)
                        }
                )
            }

            Stamp(stepOf(details, 3)) {
                StrokedText(
                    text = card.name,
                    size = 19.sp,
                    color = Color.White,
                    outline = Color(0xFF0B0812),
                    strokeWidth = 6f,
                    maxLines = 1
                )
            }
            Box(
                modifier = Modifier
                    .padding(vertical = 4.dp)
                    .fillMaxWidth(0.85f)
                    .height(1.5.dp)
                    .background(Brush.horizontalGradient(listOf(Color.Transparent, fx, Color.Transparent)))
            )
            val statsIn = stepOf(details, 4)
            Row(
                modifier = Modifier.fillMaxWidth().graphicsLayer { alpha = (statsIn * 3f).coerceIn(0f, 1f) },
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatCell("HP", "${(hp * statsIn).roundToInt()}", Color(0xFFFF7A7A))
                StatCell("ATK", "${(attack * statsIn).roundToInt()}", Color(0xFFFFB347))
                StatCell("DEF", "${(defense * statsIn).roundToInt()}%", Color(0xFF8CC4FF))
            }
            card.passive?.let { passive ->
                Text(
                    text = "${passive.icon} ${passive.label}",
                    color = Color(0xFFE8DCC8).copy(alpha = statsIn),
                    fontFamily = FontFirst(),
                    fontWeight = FontWeight.Bold,
                    fontSize = 10.sp,
                    lineHeight = 12.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
        }

        // Rzadkosc jako wstega na gornej krawedzi karty - pierwsza rzecz, ktora sie wbija.
        Box(modifier = Modifier.align(Alignment.TopCenter).padding(top = 6.dp)) {
            Stamp(stepOf(details, 1)) { RarityTag(card.rarity) }
        }

        val badge = stepOf(details, 5)
        if (badge > 0f) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 30.dp, end = 4.dp)
                    .graphicsLayer {
                        rotationZ = 12f
                        val s = 2f - EaseOutBack.transform(badge)
                        scaleX = s
                        scaleY = s
                        alpha = (badge * 2.5f).coerceIn(0f, 1f)
                    }
            ) {
                FreshBadge(fresh = fresh, big = true)
            }
        }
    }
}

/** Blysk przesuwajacy sie po karcie po skosie - "holo" rzadszych kart. */
private fun DrawScope.holoSweep(t: Float) {
    val cycle = (t * 0.45f) % 1.6f
    if (cycle > 1f) return
    val x = -size.width * 0.6f + cycle * size.width * 2.2f
    drawRect(
        brush = Brush.linearGradient(
            listOf(Color.Transparent, Color.White.copy(alpha = 0.28f), Color.Transparent),
            start = Offset(x, 0f),
            end = Offset(x + size.width * 0.45f, size.height * 0.55f)
        )
    )
}

/** Wbijanie elementu karty: wpada z wiekszej skali, lekko sie odbija i osiada. */
@Composable
private fun Stamp(progress: Float, content: @Composable BoxScope.() -> Unit) {
    Box(
        modifier = Modifier.graphicsLayer {
            val s = 1.8f - 0.8f * EaseOutBack.transform(progress)
            scaleX = s
            scaleY = s
            alpha = (progress * 2.5f).coerceIn(0f, 1f)
        },
        content = content
    )
}

@Composable
private fun RarityTag(rarity: Rarity) {
    val fx = rarity.fx()
    Text(
        text = RarityPalette.label(rarity).uppercase(),
        color = Color(0xFF16101E),
        fontFamily = FontFirst(),
        fontWeight = FontWeight.Black,
        fontSize = 10.sp,
        lineHeight = 12.sp,
        letterSpacing = 1.sp,
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(Brush.verticalGradient(listOf(fx.lightenBy(0.35f), fx)))
            .border(1.dp, Color.White.copy(alpha = 0.6f), RoundedCornerShape(6.dp))
            .padding(horizontal = 7.dp, vertical = 3.dp)
    )
}

@Composable
private fun StatCell(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label,
            color = Color(0xFFB9AFC8),
            fontFamily = FontFirst(),
            fontWeight = FontWeight.Bold,
            fontSize = 8.sp,
            lineHeight = 9.sp,
            letterSpacing = 0.8.sp
        )
        StrokedText(text = value, size = 16.sp, color = color, outline = Color(0xFF0B0812), strokeWidth = 5f, maxLines = 1)
    }
}

/** "NEW!" dla pierwszej kopii, "+1 COPY" dla kolejnej - duplikaty sa paliwem do awansu gwiazdek. */
@Composable
private fun FreshBadge(fresh: Boolean, big: Boolean) {
    val colors = if (fresh) listOf(Color(0xFFFF6A3D), Color(0xFFD9262C)) else listOf(Color(0xFF6E5A2E), Color(0xFF3E3218))
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(if (big) 8.dp else 5.dp))
            .background(Brush.verticalGradient(colors))
            .border(if (big) 2.dp else 1.dp, Color.White.copy(alpha = 0.85f), RoundedCornerShape(if (big) 8.dp else 5.dp))
            .padding(horizontal = if (big) 10.dp else 4.dp, vertical = if (big) 3.dp else 1.dp)
    ) {
        StrokedText(
            text = if (fresh) "NEW!" else "+1 COPY",
            size = if (big) 18.sp else 8.sp,
            color = if (fresh) Color.White else Color(0xFFFFE08A),
            outline = colors.last().darkenBy(0.5f),
            strokeWidth = if (big) 6f else 3f,
            tight = !big
        )
    }
}

/** Rewers karty: ciemne pole w zlotej ramce z gwiazda wyryta na oltarzu. */
@Composable
private fun CardBack(rarity: Rarity?, modifier: Modifier = Modifier, small: Boolean = false) {
    val gold = listOf(Color(0xFFFFE9A8), Color(0xFFD9A441), Color(0xFF8A5A14), Color(0xFFE8C06A))
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(if (small) 10.dp else 18.dp))
            .background(Brush.linearGradient(gold))
            .padding(if (small) 3.dp else 4.dp)
            .clip(RoundedCornerShape(if (small) 8.dp else 14.dp))
            .background(Brush.verticalGradient(listOf(Color(0xFF34275A), Color(0xFF1A1333), Color(0xFF0F0B1E)))),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            // Delikatna kratka w tle rewersu.
            val step = size.width / 6f
            for (i in -6..12) {
                drawLine(Color(0x14FFFFFF), Offset(i * step, 0f), Offset(i * step + size.height, size.height), 1f)
                drawLine(Color(0x14FFFFFF), Offset(i * step + size.height, 0f), Offset(i * step, size.height), 1f)
            }
            compassStar(Offset(size.width / 2f, size.height / 2f), size.minDimension * 0.34f, Color(0xFFE8C06A))
        }
    }
}

/** Gwiazda kompasu jak na plycie oltarza. */
private fun DrawScope.compassStar(center: Offset, radius: Float, color: Color) {
    drawCircle(color.copy(alpha = 0.85f), radius, center, style = Stroke(radius * 0.06f))
    drawCircle(color.copy(alpha = 0.6f), radius * 0.78f, center, style = Stroke(radius * 0.03f))
    val star = Path()
    for (i in 0 until 16) {
        val angle = i * (PI.toFloat() / 8f) - PI.toFloat() / 2f
        val r = when {
            i % 4 == 0 -> radius * 1.05f
            i % 2 == 0 -> radius * 0.62f
            else -> radius * 0.2f
        }
        val p = Offset(center.x + cos(angle) * r, center.y + sin(angle) * r)
        if (i == 0) star.moveTo(p.x, p.y) else star.lineTo(p.x, p.y)
    }
    star.close()
    drawPath(star, color)
    drawCircle(Color(0xFF1A1333), radius * 0.14f, center)
    drawCircle(color, radius * 0.08f, center)
}

/* ------------------------------------------------------------------ x10: stol z kartami */

/** Uklad 3-4-3 dla dziesieciu kart (czytelniejszy i wiekszy niz 5+5); inne liczby - po piec. */
private fun rowsFor(count: Int): List<Int> =
    if (count == 10) listOf(3, 4, 3) else List((count + 4) / 5) { row -> minOf(5, count - row * 5) }

/**
 * Stol x10: karty wylatuja z kregu i kladą sie rewersem do gory. Lepsze swieca juz od spodu
 * (jak w Hearthstone), wiec gracz wie, na co czekac. Tapniecie odwraca karte, "Reveal all"
 * odwraca reszte od najslabszej do najlepszej, "Continue" wysyla wszystkie do kolekcji.
 */
@Composable
internal fun GridOverlay(
    show: SummonShow,
    area: () -> Rect,
    origin: () -> Offset,
    stowTo: () -> Offset,
    time: State<Float>,
    modifier: Modifier = Modifier
) {
    val density = LocalDensity.current
    val pulls = show.pulls
    val rows = rowsFor(pulls.size)
    val gapX = with(density) { 8.dp.toPx() }
    val gapY = with(density) { 10.dp.toPx() }
    val cardW = with(density) { MiniCardWidth.toPx() }
    val cardH = with(density) { MiniCardHeight.toPx() }
    val gridW = (rows.maxOrNull() ?: 1) * cardW + ((rows.maxOrNull() ?: 1) - 1) * gapX
    val gridH = rows.size * cardH + (rows.size - 1) * gapY
    val buttonSpace = with(density) { 64.dp.toPx() }

    fun layout(): Pair<Float, List<Offset>> {
        val r = area()
        val fit = minOf(1f, (r.height - buttonSpace) * 0.95f / gridH, r.width * 0.96f / gridW)
        val top = r.top + (r.height - buttonSpace - gridH * fit) / 2f
        val slots = mutableListOf<Offset>()
        rows.forEachIndexed { row, n ->
            val rowW = n * cardW + (n - 1) * gapX
            for (col in 0 until n) {
                val x = r.center.x + (-rowW / 2f + col * (cardW + gapX) + cardW / 2f) * fit
                val y = top + (row * (cardH + gapY) + cardH / 2f) * fit
                slots += Offset(x, y)
            }
        }
        return fit to slots
    }

    val noRipple = remember { MutableInteractionSource() }
    Box(modifier = modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer { alpha = show.deal.value.coerceIn(0f, 1f) * (1f - show.stow.value) * 0.45f }
                .background(Color.Black)
                .clickable(interactionSource = noRipple, indication = null) { }
        )

        pulls.forEachIndexed { index, card ->
            GridCard(
                index = index,
                card = card,
                fresh = show.fresh.getOrElse(index) { false },
                show = show,
                time = time,
                place = { layout().let { (fit, slots) -> fit to slots[index] } },
                origin = origin,
                stowTo = stowTo
            )
        }

        val allFlipped = show.allFlipped
        ChunkyButton(
            text = if (allFlipped) "Continue" else "Reveal all",
            color = if (allFlipped) Color(0xFF4FAE3A) else Color(0xFF8A52CC),
            textSize = 15,
            onClick = { if (allFlipped) show.finishGrid() else show.revealAll() },
            modifier = Modifier
                .width(170.dp)
                .offset {
                    val r = area()
                    IntOffset(
                        (r.center.x - 85.dp.toPx()).roundToInt(),
                        (r.bottom - buttonSpace + 8.dp.toPx()).roundToInt()
                    )
                }
                .graphicsLayer {
                    alpha = ((show.deal.value - 0.8f) / 0.2f).coerceIn(0f, 1f) * (1f - show.stow.value)
                }
        )
    }
}

@Composable
private fun GridCard(
    index: Int,
    card: MonsterCard,
    fresh: Boolean,
    show: SummonShow,
    time: State<Float>,
    place: () -> Pair<Float, Offset>,
    origin: () -> Offset,
    stowTo: () -> Offset
) {
    val density = LocalDensity.current
    val flip = show.flips.getOrNull(index) ?: return
    val noRipple = remember { MutableInteractionSource() }
    val tilt = (sin(index * 12.9898f) * 43758.547f).let { it - kotlin.math.floor(it) } * 50f - 25f
    val glowing = card.rarity >= Rarity.Rare
    val fx = card.rarity.fx()

    Box(
        modifier = Modifier
            .size(MiniCardWidth, MiniCardHeight)
            .graphicsLayer {
                val (fit, slot) = place()
                val p = ((show.deal.value - index * 0.05f) / 0.5f).coerceIn(0f, 1f)
                val e = EaseOutCubic.transform(p)
                val from = origin()
                val home = stowTo()
                val away = EaseInCubic.transform(((show.stow.value - index * 0.035f) / 0.65f).coerceIn(0f, 1f))
                val cx = lerpF(lerpF(from.x, slot.x, e), home.x, away)
                val cy = lerpF(lerpF(from.y, slot.y, e), home.y, away)
                translationX = cx - size.width / 2f
                translationY = cy - size.height / 2f
                // Przy odwracaniu karta "podskakuje" - rzadsza wyzej.
                val f = flip.value
                val pop = 1f + (if (card.rarity >= Rarity.Epic) 0.28f else 0.08f) * sin(PI.toFloat() * f)
                val scale = (0.25f + 0.75f * EaseOutBack.transform(p)) * fit * (1f - 0.8f * away) * pop
                scaleX = scale
                scaleY = scale
                rotationZ = (1f - p) * tilt
                alpha = (p * 4f).coerceIn(0f, 1f) * (1f - away)
            }
            .clickable(interactionSource = noRipple, indication = null) { show.flipCard(index) }
    ) {
        if (glowing) {
            // Poswiata spod karty - lepsze karty zdradzaja sie, zanim je odwrocisz.
            Canvas(
                modifier = Modifier
                    .matchParentSize()
                    .graphicsLayer {
                        val strength = when (card.rarity) {
                            Rarity.Legendary -> 1f
                            Rarity.Epic -> 0.85f
                            else -> 0.55f
                        }
                        alpha = strength * (0.7f + 0.3f * sin(time.value * 4f + index))
                    }
            ) {
                // Poswiata WIEKSZA niz karta - wystaje spod jej krawedzi ze wszystkich stron.
                val r = size.maxDimension * 0.85f
                drawCircle(
                    brush = Brush.radialGradient(
                        listOf(fx.copy(alpha = 0.95f), fx.copy(alpha = 0.55f), fx.copy(alpha = 0.18f), Color.Transparent),
                        center,
                        r
                    ),
                    radius = r,
                    center = center
                )
            }
        }
        if (flip.value < 0.5f) {
            CardBack(
                rarity = card.rarity,
                small = true,
                modifier = Modifier.fillMaxSize().graphicsLayer {
                    rotationY = -flip.value * 180f
                    cameraDistance = 12f * density.density
                }
            )
        } else {
            MiniFront(
                card = card,
                fresh = fresh,
                modifier = Modifier.fillMaxSize().graphicsLayer {
                    rotationY = (1f - flip.value) * 180f
                    cameraDistance = 12f * density.density
                }
            )
            // Pierscien przy odkryciu rzadszej karty.
            if (card.rarity >= Rarity.Epic) {
                Canvas(modifier = Modifier.matchParentSize()) {
                    val v = ((flip.value - 0.5f) * 2f).coerceIn(0f, 1f)
                    if (v < 1f) {
                        drawCircle(
                            color = fx.copy(alpha = (1f - v) * 0.9f),
                            radius = size.minDimension * (0.4f + 1.2f * v),
                            style = Stroke((6.dp * (1f - v) + 1.dp).toPx())
                        )
                    }
                }
            }
        }
    }
}

/** Awers malej karty x10: ramka rzadkosci, kompan, imie i pasek z rzadkoscia. */
@Composable
private fun MiniFront(card: MonsterCard, fresh: Boolean, modifier: Modifier = Modifier) {
    val fx = card.rarity.fx()
    val frame = RarityPalette.frame(card.rarity)
    val preset = MonsterVisualPresets.forSpecies(card.spriteKey)
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(Brush.linearGradient(listOf(fx.lightenBy(0.4f), frame, fx.darkenBy(0.5f))))
            .padding(3.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(Brush.verticalGradient(listOf(frame.darkenBy(0.45f), Color(0xFF120C1C))))
    ) {
        Column(modifier = Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
            Box(modifier = Modifier.fillMaxWidth().weight(1f).padding(top = 4.dp), contentAlignment = Alignment.Center) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    drawCircle(Brush.radialGradient(listOf(fx.copy(alpha = 0.45f), Color.Transparent)))
                }
                Image(painter = painterResource(preset.drawable), contentDescription = card.name, modifier = Modifier.fillMaxSize())
            }
            Text(
                text = card.name,
                color = Color.White,
                fontFamily = FontFirst(),
                fontWeight = FontWeight.Black,
                fontSize = 9.sp,
                lineHeight = 10.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(horizontal = 3.dp)
            )
            Box(
                modifier = Modifier
                    .padding(top = 2.dp)
                    .fillMaxWidth()
                    .background(Brush.verticalGradient(listOf(fx.lightenBy(0.3f), fx)))
                    .padding(vertical = 1.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = RarityPalette.label(card.rarity).uppercase(),
                    color = Color(0xFF16101E),
                    fontFamily = FontFirst(),
                    fontWeight = FontWeight.Black,
                    fontSize = 7.sp,
                    lineHeight = 8.sp,
                    letterSpacing = 0.6.sp
                )
            }
        }
        if (fresh) {
            Box(modifier = Modifier.align(Alignment.TopEnd).padding(2.dp).wrapContentSize()) {
                FreshBadge(fresh = true, big = false)
            }
        }
    }
}
