package com.lernhero.home.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lernhero.shared.i18n.IntroPage
import com.lernhero.shared.i18n.strings
import com.lernhero.shared.theme.FontFirst
import com.lernhero.shared.theme.GlyphText
import com.lernhero.shared.theme.StrokedText
import kotlinx.coroutines.launch

/**
 * Krotki samouczek dla nowego gracza: cztery karty, ktore mozna przewinac palcem albo przeklikac
 * "Dalej", i "Pomin" w kazdej chwili. Celowo tylko tyle - nie prowadzi za reke po ekranach, mowi
 * jedynie, o co chodzi w grze i gdzie czego szukac. Reszte gracz odkryje sam, a dluzszego
 * samouczka nikt nie czyta.
 *
 * Lezy NAD calym ekranem (razem z paskami) i zjada dotyk - pod spodem nic nie da sie kliknac,
 * dopoki gracz nie skonczy albo nie pominie.
 */
@Composable
fun IntroTutorial(onDone: () -> Unit) {
    val t = strings
    val pages = t.introPages
    val pager = rememberPagerState(pageCount = { pages.size })
    val scope = rememberCoroutineScope()
    val last = pager.currentPage == pages.lastIndex

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.78f))
            // Zjada tapniecia, zeby nie przechodzily do ekranu pod spodem.
            .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) {},
        contentAlignment = Alignment.Center
    ) {
        val shape = RoundedCornerShape(22.dp)
        Column(
            modifier = Modifier
                .padding(horizontal = 20.dp)
                .widthIn(max = 420.dp)
                .fillMaxWidth()
                .clip(shape)
                .background(Brush.verticalGradient(listOf(Color(0xFF342760), Color(0xFF1B1433))))
                .border(2.5.dp, Color(0xFFC8A45A), shape)
                .padding(vertical = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            HorizontalPager(state = pager, modifier = Modifier.fillMaxWidth()) { index ->
                IntroCard(page = pages[index])
            }

            PageDots(count = pages.size, current = pager.currentPage, modifier = Modifier.padding(top = 14.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp, top = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Na ostatniej karcie "Pomin" nie ma czego pomijac - zostaje samo "Zaczynamy".
                Box(modifier = Modifier.weight(1f)) {
                    if (!last) {
                        Text(
                            text = t.introSkip,
                            color = Color(0xFFB9A98A),
                            fontFamily = FontFirst(),
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable(onClick = onDone)
                                .padding(horizontal = 10.dp, vertical = 8.dp)
                        )
                    }
                }
                ChunkyButton(
                    text = if (last) t.introStart else t.introNext,
                    color = if (last) Color(0xFF3FA84A) else Color(0xFF3C82D0),
                    textSize = 16,
                    modifier = Modifier.widthIn(min = 140.dp),
                    onClick = {
                        if (last) onDone() else scope.launch { pager.animateScrollToPage(pager.currentPage + 1) }
                    }
                )
            }
        }
    }
}

@Composable
private fun IntroCard(page: IntroPage) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 22.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        IconMedallion(icon = page.icon)
        StrokedText(
            text = page.title,
            size = 24.sp,
            color = Color(0xFFFFD34D),
            outline = Color(0xFF120B05),
            strokeWidth = 6f
        )
        // GlyphText - emoji walut (⚡, 💎) w tresci rysuja sie jako obrazki gry, jak wszedzie indziej.
        GlyphText(
            text = page.body,
            color = Color(0xFFF4E9D7),
            fontFamily = FontFirst(),
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp,
            lineHeight = 21.sp,
            textAlign = TextAlign.Center,
            // Stala wysokosc tresci - karty o krotszym tekscie nie moga podskakiwac przyciskom.
            modifier = Modifier.height(BODY_HEIGHT)
        )
    }
}

/** Duza ikona karty w zlotym medalionie z obracajacymi sie promieniami - jak medrzec w Quizach. */
@Composable
private fun IconMedallion(icon: String) {
    val spin by rememberInfiniteTransition().animateFloat(
        0f, 360f, infiniteRepeatable(tween(24_000, easing = LinearEasing))
    )
    Box(
        modifier = Modifier
            .size(120.dp)
            .clip(CircleShape)
            .background(Color(0xFFC8A45A))
            .padding(3.dp)
            .clip(CircleShape)
            .background(Brush.verticalGradient(listOf(Color(0xFF4A3A7E), Color(0xFF1A1330)))),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize().rotate(spin)) {
            val r = size.minDimension * 0.7f
            for (i in 0 until 12) {
                rotate(i * 30f) {
                    val path = Path().apply {
                        moveTo(center.x, center.y)
                        lineTo(center.x - r * 0.1f, center.y - r)
                        lineTo(center.x + r * 0.1f, center.y - r)
                        close()
                    }
                    drawPath(path, Brush.radialGradient(listOf(Color(0x55FFD98A), Color.Transparent), center, r))
                }
            }
        }
        Text(text = icon, fontSize = 60.sp)
    }
}

@Composable
private fun PageDots(count: Int, current: Int, modifier: Modifier = Modifier) {
    Row(modifier = modifier, horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
        repeat(count) { index ->
            val active = index == current
            val width by animateDpAsState(if (active) 22.dp else 8.dp, label = "dotWidth")
            val color by animateColorAsState(if (active) Color(0xFFFFD34D) else Color(0xFF5A4E78), label = "dotColor")
            Box(
                modifier = Modifier
                    .height(8.dp)
                    .size(width = width, height = 8.dp)
                    .clip(CircleShape)
                    .background(color)
            )
        }
    }
}

private val BODY_HEIGHT = 110.dp
