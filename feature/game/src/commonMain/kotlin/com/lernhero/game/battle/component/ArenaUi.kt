package com.lernhero.game.battle.component

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.EaseOutBack
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lernhero.game.battle.BattleSpeed
import com.lernhero.game.battle.BattleUiState
import com.lernhero.game.battle.GamePhase
import com.lernhero.game.battle.effects.CharacterRect
import com.lernhero.game.battle.fx.AttackStyles
import com.lernhero.game.character.CharacterUiModel
import com.lernhero.game.character.ElementEmblem
import com.lernhero.shared.theme.FontFirst
import com.lernhero.shared.theme.StrokedText
import kotlinx.coroutines.delay
import kotlin.math.roundToInt

/* ============================================================== podglad postaci */

/**
 * Karta po tapnieciu w postac: pelne liczby, ktore zniknely z tabliczki nad glowa - HP, atak,
 * pancerz, zywiol i moc bierna. Znika po chwili albo po tapnieciu gdziekolwiek.
 */
@Composable
internal fun InspectCard(
    character: CharacterUiModel,
    anchor: CharacterRect,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    LaunchedEffect(character.id) {
        delay(4000)
        onDismiss()
    }
    val appear = remember(character.id) { Animatable(0f) }
    LaunchedEffect(character.id) { appear.animateTo(1f, tween(220, easing = EaseOutBack)) }
    val noRipple = remember { MutableInteractionSource() }

    BoxWithConstraints(
        modifier = modifier.clickable(interactionSource = noRipple, indication = null, onClick = onDismiss)
    ) {
        val areaWidth = constraints.maxWidth
        var cardSize by remember { mutableStateOf(IntSize.Zero) }
        val accent = if (character.isPlayer) Color(0xFFFFD66B) else Color(0xFFFF8A7A)
        Column(
            modifier = Modifier
                .onGloballyPositioned { cardSize = it.size }
                .offset {
                    val cx = anchor.topLeft.x + anchor.size.width / 2f
                    val x = (cx - cardSize.width / 2f).roundToInt().coerceIn(4, (areaWidth - cardSize.width - 4).coerceAtLeast(4))
                    val above = anchor.topLeft.y - cardSize.height - 18.dp.toPx()
                    val y = if (above > 0) above else anchor.topLeft.y + anchor.size.height + 6.dp.toPx()
                    IntOffset(x, y.roundToInt())
                }
                .graphicsLayer {
                    val a = appear.value
                    scaleX = 0.7f + 0.3f * a
                    scaleY = 0.7f + 0.3f * a
                    alpha = a.coerceIn(0f, 1f)
                }
                .width(170.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Brush.verticalGradient(listOf(Color(0xF2261C14), Color(0xF2120C08))))
                .border(1.5.dp, accent.copy(alpha = 0.8f), RoundedCornerShape(12.dp))
                .padding(horizontal = 10.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                character.element?.let { ElementEmblem(element = it, size = 20.dp) }
                Text(
                    text = character.name,
                    color = Color.White,
                    fontFamily = FontFirst(),
                    fontWeight = FontWeight.Black,
                    fontSize = 13.sp,
                    maxLines = 1,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = "Lv ${character.level}",
                    color = accent,
                    fontFamily = FontFirst(),
                    fontWeight = FontWeight.Black,
                    fontSize = 11.sp
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                InspectStat("HP", "${character.hp.coerceAtLeast(0)}/${character.maxHp}", Color(0xFFFF8A8A))
                InspectStat("ATK", "${character.attack}", Color(0xFFFFC266))
                InspectStat("DEF", "${(character.armor * 100).roundToInt()}%", Color(0xFF9FCBFF))
            }
            character.passive?.let { passive ->
                Text(
                    text = "${passive.icon} ${passive.label}",
                    color = Color(0xFFE8DCC8),
                    fontFamily = FontFirst(),
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
private fun InspectStat(label: String, value: String, color: Color) {
    Column {
        Text(label, color = Color(0xFFB0A28A), fontFamily = FontFirst(), fontWeight = FontWeight.Bold, fontSize = 8.sp, letterSpacing = 0.6.sp)
        Text(value, color = color, fontFamily = FontFirst(), fontWeight = FontWeight.Black, fontSize = 12.sp, maxLines = 1)
    }
}

/* ============================================================== banery */

private data class BannerSpec(val id: Int, val title: String, val subtitle: String?, val accent: Color, val boss: Boolean)

/**
 * Banery przez cala szerokosc areny: "WAVE 3" przy wejsciu fali, "BOSS WAVE" / "ELITE" przy
 * olbrzymach i "FIGHT!" w chwili, gdy konczy sie wejscie na arene i rusza pierwsza tura.
 * Napis wjezdza z prawej z przestrzeleniem, chwile stoi i ucieka w lewo.
 */
@Composable
internal fun ArenaBanners(state: BattleUiState, enemies: List<CharacterUiModel>, modifier: Modifier = Modifier) {
    var banner by remember { mutableStateOf<BannerSpec?>(null) }
    var counter by remember { mutableStateOf(0) }
    val giant = enemies.maxOfOrNull { it.battleScale } ?: 1f

    LaunchedEffect(state.wave) {
        // Na starcie walki wrogowie wchodza dopiero po naszej druzynie (patrz playBattleIntro) -
        // baner fali czeka, az druzyna stanie, i wchodzi razem z wrogami.
        if (state.phase == GamePhase.Intro) {
            val players = state.characters.values.count { it.isPlayer }
            delay(200L + (players - 1).coerceAtLeast(0) * 320L + 420L)
        }
        val bossWave = giant > 2.6f
        val eliteWave = giant > AttackStyles.GIANT_SCALE
        counter += 1
        banner = when {
            bossWave -> BannerSpec(counter, "BOSS WAVE", "Wave ${state.wave}", Color(0xFFFF5A3C), boss = true)
            eliteWave -> BannerSpec(counter, "ELITE", "Wave ${state.wave}", Color(0xFFFFA23C), boss = true)
            else -> BannerSpec(counter, "WAVE ${state.wave}", null, Color(0xFFFFD34D), boss = false)
        }
    }

    var sawIntro by remember { mutableStateOf(state.phase == GamePhase.Intro) }
    LaunchedEffect(state.phase) {
        if (state.phase == GamePhase.Intro) sawIntro = true
        if (state.phase == GamePhase.Battle && sawIntro) {
            sawIntro = false
            delay(BattleSpeed.scale(150L))
            counter += 1
            banner = BannerSpec(counter, "FIGHT!", null, Color(0xFFFFF1C2), boss = false)
        }
    }

    val spec = banner ?: return
    BannerView(spec = spec, modifier = modifier, onDone = { if (banner?.id == spec.id) banner = null })
}

@Composable
private fun BannerView(spec: BannerSpec, modifier: Modifier, onDone: () -> Unit) {
    val progress = remember(spec.id) { Animatable(0f) }
    LaunchedEffect(spec.id) {
        progress.animateTo(1f, tween(BattleSpeed.scale(if (spec.boss) 2100 else 1500), easing = LinearEasing))
        onDone()
    }
    BoxWithConstraints(modifier = modifier) {
        val width = constraints.maxWidth.toFloat()
        val p = progress.value
        val inP = (p / 0.22f).coerceIn(0f, 1f)
        val outP = ((p - 0.8f) / 0.2f).coerceIn(0f, 1f)
        val bandAlpha = (inP * 1.5f).coerceAtMost(1f) * (1f - outP)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(if (spec.boss) 96.dp else 80.dp)
                .align(Alignment.Center)
                .offset(y = (-30).dp)
                .graphicsLayer { alpha = bandAlpha },
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val band = if (spec.boss) Color(0xE0300806) else Color(0xD00E0A14)
                drawRect(Brush.horizontalGradient(listOf(Color.Transparent, band, band, Color.Transparent)))
                val line = Brush.horizontalGradient(listOf(Color.Transparent, spec.accent, Color.Transparent))
                val reach = size.width * inP
                drawRect(line, topLeft = Offset((size.width - reach) / 2f, 0f), size = Size(reach, 2.dp.toPx()))
                drawRect(line, topLeft = Offset((size.width - reach) / 2f, size.height - 2.dp.toPx()), size = Size(reach, 2.dp.toPx()))
            }
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.graphicsLayer {
                    val slideIn = 1f - EaseOutBack.transform(inP)
                    translationX = slideIn * width * 0.6f - outP * outP * width * 0.7f
                    val s = if (spec.boss) 1f + 0.06f * kotlin.math.sin(p * 40f) * (1f - inP) else 1f
                    scaleX = s
                    scaleY = s
                }
            ) {
                StrokedText(
                    text = spec.title,
                    size = if (spec.boss) 40.sp else 34.sp,
                    color = spec.accent,
                    outline = Color(0xFF140A04),
                    strokeWidth = 10f,
                    maxLines = 1
                )
                spec.subtitle?.let {
                    StrokedText(text = it, size = 13.sp, color = Color.White, outline = Color(0xFF140A04), strokeWidth = 5f, maxLines = 1)
                }
            }
        }
    }
}
