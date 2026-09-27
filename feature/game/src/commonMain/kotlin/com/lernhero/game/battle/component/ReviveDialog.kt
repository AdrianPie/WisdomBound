package com.lernhero.game.battle.component

import com.lernhero.shared.theme.GlyphText
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lernhero.domain.monetization.AdRewards
import com.lernhero.game.battle.ReviveOffer
import com.lernhero.shared.theme.FontFirst
import com.lernhero.shared.theme.FontPixel
import kotlin.math.roundToInt

/**
 * Druzyna padla - ostatnia szansa. Scena gasnie, na srodku czaszka w zlotym kole z cicha poswiata i jedna
 * decyzja: obejrzyj reklame i wstan z polowa HP, albo przyjmij porazke. Raz na walke.
 */
@Composable
internal fun ReviveDialog(
    offer: ReviveOffer,
    onRevive: () -> Unit,
    onReviveWithGems: () -> Unit,
    onGiveUp: () -> Unit,
    modifier: Modifier = Modifier
) {
    val enter = remember { Animatable(0f) }
    LaunchedEffect(Unit) { enter.animateTo(1f, tween(ENTER_MS, easing = FastOutSlowInEasing)) }

    val infinite = rememberInfiniteTransition()
    val halo by infinite.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(1300, easing = FastOutSlowInEasing), RepeatMode.Reverse)
    )
    // Cienki pierscien, ktory co chwile rozchodzi sie od czaszki i gasnie.
    val ripple by infinite.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(2200, easing = LinearEasing))
    )

    val percent = (AdRewards.REVIVE_HP_FRACTION * 100).roundToInt()

    Box(
        modifier = modifier
            .background(Color(0xFF07050B).copy(alpha = 0.86f * enter.value))
            // Nic pod spodem nie moze dostac tapniecia, dopoki decyzja nie zapadnie.
            .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) {}
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 380.dp)
                .fillMaxWidth()
                .graphicsLayer {
                    alpha = enter.value
                    val s = 0.85f + 0.15f * enter.value
                    scaleX = s
                    scaleY = s
                },
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Box(modifier = Modifier.size(130.dp), contentAlignment = Alignment.Center) {
                // Miekka, cieplo pulsujaca poswiata za czaszka.
                Box(
                    modifier = Modifier
                        .size(130.dp)
                        .background(
                            Brush.radialGradient(
                                listOf(
                                    Color(0xFFFFD98A).copy(alpha = 0.22f + 0.14f * halo),
                                    Color(0xFFFFB347).copy(alpha = 0.06f),
                                    Color.Transparent
                                )
                            ),
                            CircleShape
                        )
                )
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .graphicsLayer {
                            val s = 1f + 0.55f * ripple
                            scaleX = s
                            scaleY = s
                            alpha = (1f - ripple) * 0.7f
                        }
                        .border(1.5.dp, Color(0xFFFFD98A), CircleShape)
                )
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF1A1420))
                        .border(2.dp, Color(0xFFE0B35A).copy(alpha = 0.7f + 0.3f * halo), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    GlyphText(text = "💀", fontSize = 40.sp)
                }
            }

            GlyphText(
                text = "YOUR PARTY HAS FALLEN",
                color = Color(0xFFE7A08A),
                fontFamily = FontPixel(),
                fontWeight = FontWeight.Bold,
                fontSize = 17.sp,
                textAlign = TextAlign.Center
            )
            GlyphText(
                text = "Bring everyone back at $percent% HP and keep fighting this wave. One revive per battle.",
                color = Color(0xFFCDBB99),
                fontFamily = FontFirst(),
                fontSize = 13.sp,
                textAlign = TextAlign.Center
            )

            // Reklama (albo darmowe wskrzeszenie z subskrypcja) - glowna droga, gdy jest dostepna.
            if (offer.adAvailable || offer.free) {
                GlyphText(
                    text = when {
                        offer.watching -> "⏳ LOADING..."
                        offer.free -> "👑 REVIVE"
                        else -> "📺 WATCH AD & REVIVE"
                    },
                    color = Color(0xFF1C1408),
                    fontFamily = FontPixel(),
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .graphicsLayer {
                            val s = if (offer.watching) 1f else 1f + 0.03f * halo
                            scaleX = s
                            scaleY = s
                        }
                        .clip(RoundedCornerShape(12.dp))
                        .background(Brush.verticalGradient(listOf(Color(0xFFFFE9A8), Color(0xFFD99A1E))))
                        .border(2.dp, Color(0xFFFFF1C2), RoundedCornerShape(12.dp))
                        .clickable(enabled = !offer.watching, onClick = onRevive)
                        .padding(vertical = 14.dp)
                )
            }

            // Diamenty - zawsze jako druga droga (subskrybent jej nie potrzebuje).
            if (!offer.free) {
                val canPay = offer.gems >= AdRewards.REVIVE_GEMS
                GlyphText(
                    text = if (offer.watching && !offer.adAvailable) "⏳ ..." else "💎 ${AdRewards.REVIVE_GEMS}  REVIVE",
                    color = if (canPay) Color.White else Color(0xFF8FA3C4),
                    fontFamily = FontPixel(),
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            Brush.verticalGradient(
                                if (canPay) listOf(Color(0xFF5BB8FF), Color(0xFF2C7BD6)) else listOf(Color(0xFF3A4254), Color(0xFF232937))
                            )
                        )
                        .border(2.dp, if (canPay) Color(0xFFBFE3FF) else Color(0xFF3A4254), RoundedCornerShape(12.dp))
                        .clickable(enabled = canPay && !offer.watching, onClick = onReviveWithGems)
                        .padding(vertical = 12.dp)
                )
                GlyphText(
                    text = "You have 💎 ${offer.gems}",
                    color = Color(0xFF8FA3C4),
                    fontFamily = FontFirst(),
                    fontSize = 11.sp
                )
            }

            offer.note?.let {
                GlyphText(text = it, color = Color(0xFFE7A08A), fontFamily = FontFirst(), fontSize = 12.sp, textAlign = TextAlign.Center)
            }

            GlyphText(
                text = "Give up",
                color = Color(0xFF9D8B6A),
                fontFamily = FontFirst(),
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                modifier = Modifier
                    .clickable(enabled = !offer.watching, onClick = onGiveUp)
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            )
        }
    }
}

private const val ENTER_MS = 420
