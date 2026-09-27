package com.lernhero.game.battle.component

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.EaseInOutSine
import androidx.compose.animation.core.EaseOutBack
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lernhero.domain.run.EndlessBuff
import com.lernhero.domain.run.UpgradeRarity
import com.lernhero.game.battle.UpgradeOffer
import com.lernhero.game.battle.fx.VfxEngine
import com.lernhero.shared.audio.Sfx
import com.lernhero.shared.audio.Sounds
import com.lernhero.shared.theme.FontFirst
import com.lernhero.shared.theme.StrokedText
import kotlinx.coroutines.delay
import kotlin.math.roundToInt

/** Kolor klasy ulepszenia - ramka, pierscien medalionu, kropki stakow. */
internal fun UpgradeRarity.color(): Color = when (this) {
    UpgradeRarity.Common -> Color(0xFFC9CED6)
    UpgradeRarity.Rare -> Color(0xFF5FAEFF)
    UpgradeRarity.Epic -> Color(0xFFC77DFF)
    UpgradeRarity.Legendary -> Color(0xFFFFC04D)
}

/** Ikona ulepszenia w medalionie - jedno spojrzenie mowi, czego karta dotyczy. */
internal fun EndlessBuff.icon(): String = when (this) {
    EndlessBuff.Might -> "💪"
    EndlessBuff.Fortress -> "🏰"
    EndlessBuff.Precision -> "🎯"
    EndlessBuff.Vampirism -> "🦇"
    EndlessBuff.Vitality -> "❤️"
    EndlessBuff.Regeneration -> "🌿"
    EndlessBuff.MultiShot -> "🏹"
    EndlessBuff.Cleave -> "🪓"
    EndlessBuff.Guard -> "🛡️"
    EndlessBuff.Thorns -> "🌵"
    EndlessBuff.Executioner -> "💀"
    EndlessBuff.Blessing -> "✨"
    EndlessBuff.Momentum -> "📈"
    EndlessBuff.Volley -> "🎇"
    EndlessBuff.Frenzy -> "⚔️"
    EndlessBuff.SecondWind -> "🕊️"
    EndlessBuff.Brutality -> "💥"
    EndlessBuff.Overload -> "⚡"
    EndlessBuff.Avatar -> "👑"
    EndlessBuff.Aegis -> "🔰"
    EndlessBuff.Riposte -> "↩️"
}

/**
 * Roguelike'owy wybor ulepszenia po oczyszczonej fali (endless). Walka stoi, dopoki gracz nie
 * wezmie jednej z kart - [com.lernhero.game.battle.BattleViewModel.chooseUpgrade] jest jedynym
 * wyjsciem z tego ekranu.
 *
 * Wyglada jak dobor karty w roguelike'ach: wstega "WAVE X CLEARED", miernik jakosci kart (to
 * celnosc odpowiedzi wylosowala te oferte), a pod nia trzy karty z medalionem, klasa, opisem
 * i kropkami stakow. Karty wjezdzaja po kolei; wybrana podskakuje i rozblyska, reszta odplywa.
 */
@Composable
fun UpgradeChoiceDialog(
    offer: UpgradeOffer,
    onPick: (EndlessBuff) -> Unit,
    modifier: Modifier = Modifier
) {
    var picked by remember(offer.wave) { mutableStateOf<EndlessBuff?>(null) }
    LaunchedEffect(picked) {
        val choice = picked ?: return@LaunchedEffect
        Sounds.play(if (choice.rarity >= UpgradeRarity.Epic) Sfx.LevelUp else Sfx.Correct)
        delay(460)
        onPick(choice)
    }
    val backdrop = remember(offer.wave) { Animatable(0f) }
    LaunchedEffect(offer.wave) { backdrop.animateTo(1f, tween(300)) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .graphicsLayer { alpha = backdrop.value }
            .background(Brush.radialGradient(listOf(Color(0xE0201408), Color(0xF20A0604)))),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 440.dp)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            OfferHeader(offer)

            offer.choices.forEachIndexed { index, buff ->
                var visible by remember(offer.wave, buff) { mutableStateOf(false) }
                LaunchedEffect(offer.wave, buff) {
                    delay(250L + CARD_STAGGER_MILLIS * index)
                    visible = true
                    Sounds.play(Sfx.CardFlip)
                }
                UpgradeCard(
                    buff = buff,
                    ownedStacks = offer.ownedStacks[buff] ?: 0,
                    visible = visible,
                    picked = picked,
                    onPick = { if (picked == null) picked = buff }
                )
            }
        }
    }
}

@Composable
private fun OfferHeader(offer: UpgradeOffer) {
    val appear = remember(offer.wave) { Animatable(0f) }
    LaunchedEffect(offer.wave) { appear.animateTo(1f, tween(420, easing = EaseOutBack)) }
    Column(
        modifier = Modifier.graphicsLayer {
            val a = appear.value
            scaleX = 0.6f + 0.4f * a
            scaleY = 0.6f + 0.4f * a
            alpha = a.coerceIn(0f, 1f)
        },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        // Wstega z tytulem - ciemny pas ze zlotymi krawedziami, napis z grubym obrysem.
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(58.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val band = Color(0xE6170F08)
                drawRect(Brush.horizontalGradient(listOf(Color.Transparent, band, band, Color.Transparent)))
                val line = Brush.horizontalGradient(listOf(Color.Transparent, Color(0xFFE8C56A), Color.Transparent))
                drawRect(line, size = androidx.compose.ui.geometry.Size(size.width, 2.dp.toPx()))
                drawRect(line, topLeft = Offset(0f, size.height - 2.dp.toPx()), size = androidx.compose.ui.geometry.Size(size.width, 2.dp.toPx()))
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                StrokedText(
                    text = "WAVE ${offer.wave} CLEARED",
                    size = 24.sp,
                    color = Color(0xFFFFD34D),
                    outline = Color(0xFF140A04),
                    strokeWidth = 8f,
                    maxLines = 1
                )
                Text(
                    text = "Choose an upgrade for this run",
                    color = Color(0xFFF3E6C8).copy(alpha = 0.8f),
                    fontFamily = FontFirst(),
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp
                )
            }
        }
        QualityMeter(offer.quality)
    }
}

/** Miernik jakosci kart: im wiecej dobrych odpowiedzi, tym dalej w strone legendy. */
@Composable
private fun QualityMeter(quality: Float) {
    val fill by animateFloatAsState(quality.coerceIn(0.03f, 1f), tween(900, easing = FastOutSlowInEasing), label = "quality")
    Row(
        modifier = Modifier.fillMaxWidth(0.86f),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "CARD QUALITY",
            color = Color(0xFFE8C56A),
            fontFamily = FontFirst(),
            fontWeight = FontWeight.Black,
            fontSize = 9.sp,
            letterSpacing = 0.8.sp
        )
        Box(
            modifier = Modifier
                .weight(1f)
                .height(10.dp)
                .clip(RoundedCornerShape(50))
                .background(Color(0xFF140D08))
                .border(1.dp, Color(0xFF6A4E2A), RoundedCornerShape(50))
                .padding(1.5.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(fill)
                    .fillMaxSize()
                    .clip(RoundedCornerShape(50))
                    .background(
                        Brush.horizontalGradient(
                            listOf(UpgradeRarity.Common.color(), UpgradeRarity.Rare.color(), UpgradeRarity.Epic.color(), UpgradeRarity.Legendary.color())
                        )
                    )
            )
        }
        Text(
            text = "${(quality * 100).roundToInt()}%",
            color = Color(0xFFF3E6C8),
            fontFamily = FontFirst(),
            fontWeight = FontWeight.Black,
            fontSize = 11.sp
        )
    }
}

@Composable
private fun UpgradeCard(
    buff: EndlessBuff,
    ownedStacks: Int,
    visible: Boolean,
    picked: EndlessBuff?,
    onPick: () -> Unit
) {
    val accent = buff.rarity.color()
    val entrance by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(durationMillis = 380, easing = EaseOutBack),
        label = "cardIn"
    )
    val chosen = picked == buff
    val dismissed = picked != null && !chosen
    val choice by animateFloatAsState(if (chosen) 1f else 0f, tween(260, easing = EaseOutBack), label = "chosen")
    val away by animateFloatAsState(if (dismissed) 1f else 0f, tween(320, easing = FastOutSlowInEasing), label = "away")
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val press by animateFloatAsState(if (pressed) 0.96f else 1f, label = "press")
    val special = buff.rarity >= UpgradeRarity.Epic
    val glow by rememberInfiniteTransition(label = "cardGlow").animateFloat(
        initialValue = 0.45f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(1100, easing = EaseInOutSine), RepeatMode.Reverse),
        label = "cardGlowAlpha"
    )
    val shape = RoundedCornerShape(16.dp)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer {
                val s = (0.85f + 0.15f * entrance) * press * (1f + 0.06f * choice) * (1f - 0.1f * away)
                scaleX = s
                scaleY = s
                alpha = entrance.coerceIn(0f, 1f) * (1f - away)
                translationY = (1f - entrance) * 80f * density + away * 40f * density
                rotationZ = (1f - entrance) * 6f
            }
    ) {
        // Poswiata spod karty - rzadsze karty oddychaja swiatlem, wybrana rozblyska.
        if (special || chosen) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .graphicsLayer {
                        val a = if (chosen) 1f else glow * 0.7f
                        alpha = a
                        scaleX = 1.04f + 0.02f * a
                        scaleY = 1.12f + 0.04f * a
                    }
                    .clip(shape)
                    .background(Brush.radialGradient(listOf(accent.copy(alpha = 0.55f), Color.Transparent)))
            )
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(shape)
                .background(Brush.linearGradient(listOf(accent, accent.copy(alpha = 0.55f), accent)))
                .padding(2.5.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(Brush.verticalGradient(listOf(Color(0xFF2E2116), Color(0xFF1A110A))))
                .background(Brush.radialGradient(listOf(accent.copy(alpha = 0.22f), Color.Transparent), center = Offset(0f, 0f), radius = 520f))
                .drawWithContent {
                    drawContent()
                    if (special || chosen) {
                        // Blysk przesuwajacy sie po skosie - jak po holograficznej karcie. Odczyt
                        // [glow] trzyma rysowanie w petli klatek (inaczej blysk stalby w miejscu).
                        if (glow < 0f) return@drawWithContent
                        val t = VfxEngine.monoNow() / 1_000_000_000f
                        val cycle = (t * 0.5f) % 1.4f
                        if (cycle < 1f) {
                            val x = -size.width * 0.4f + cycle * size.width * 1.8f
                            drawRect(
                                Brush.linearGradient(
                                    listOf(Color.Transparent, Color.White.copy(alpha = 0.16f), Color.Transparent),
                                    start = Offset(x, 0f),
                                    end = Offset(x + size.width * 0.3f, size.height)
                                )
                            )
                        }
                    }
                    if (chosen) drawRect(Color.White.copy(alpha = 0.25f * (1f - choice).coerceIn(0f, 1f)))
                }
                .clickable(interactionSource = interaction, indication = null, enabled = visible && picked == null, onClick = onPick)
                .padding(horizontal = 12.dp, vertical = 11.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Medalion z ikona ulepszenia.
            Box(
                modifier = Modifier
                    .size(58.dp)
                    .clip(CircleShape)
                    .background(Brush.radialGradient(listOf(accent.copy(alpha = 0.5f), Color(0xFF120B06))))
                    .border(2.5.dp, Brush.verticalGradient(listOf(Color.White.copy(alpha = 0.9f), accent, accent.copy(alpha = 0.6f))), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(text = buff.icon(), fontSize = 28.sp)
            }
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    StrokedText(
                        text = buff.title,
                        size = 16.sp,
                        color = Color(0xFFFFF4DC),
                        outline = Color(0xFF0E0804),
                        strokeWidth = 5f,
                        maxLines = 1,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    Text(
                        text = buff.rarity.label,
                        color = Color(0xFF140C06),
                        fontFamily = FontFirst(),
                        fontWeight = FontWeight.Black,
                        fontSize = 8.sp,
                        letterSpacing = 0.8.sp,
                        modifier = Modifier
                            .clip(RoundedCornerShape(5.dp))
                            .background(accent)
                            .padding(horizontal = 5.dp, vertical = 1.dp)
                    )
                }
                Text(
                    text = buff.description,
                    color = Color(0xFFE6D8BE),
                    fontFamily = FontFirst(),
                    fontSize = 12.sp,
                    lineHeight = 15.sp
                )
                StackPips(owned = ownedStacks, max = buff.maxStacks, accent = accent)
            }
        }
    }
}

/**
 * Kropki stakow: pelne = juz masz, pulsujaca = ta, ktora dostaniesz teraz, puste = zostaly.
 * "NEW" zamiast kropek, gdy karty jeszcze w ogole nie masz.
 */
@Composable
private fun StackPips(owned: Int, max: Int, accent: Color) {
    val next by rememberInfiniteTransition(label = "nextPip").animateFloat(
        initialValue = 0.35f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(600, easing = LinearEasing), RepeatMode.Reverse),
        label = "nextPipAlpha"
    )
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        if (owned == 0) {
            Text(
                text = "NEW",
                color = Color.White,
                fontFamily = FontFirst(),
                fontWeight = FontWeight.Black,
                fontSize = 8.sp,
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(Color(0xFFD9262C))
                    .padding(horizontal = 4.dp, vertical = 1.dp)
            )
        }
        repeat(max) { i ->
            val filled = i < owned
            val isNext = i == owned
            Box(
                modifier = Modifier
                    .size(9.dp)
                    .graphicsLayer { if (isNext) alpha = next }
                    .clip(CircleShape)
                    .background(if (filled || isNext) accent else Color(0xFF3A2A1C))
                    .border(1.dp, if (filled) Color.White.copy(alpha = 0.7f) else accent.copy(alpha = 0.5f), CircleShape)
            )
        }
        Text(
            text = if (owned == 0) "stacks x$max" else "$owned → ${owned + 1} of $max",
            color = Color(0xFFB9A98A),
            fontFamily = FontFirst(),
            fontWeight = FontWeight.Bold,
            fontSize = 9.sp,
            textAlign = TextAlign.Start
        )
    }
}

private const val CARD_STAGGER_MILLIS = 140L
