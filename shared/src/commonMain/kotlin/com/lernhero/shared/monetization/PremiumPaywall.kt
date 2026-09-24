package com.lernhero.shared.monetization

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.lernhero.domain.monetization.PremiumLimits
import com.lernhero.shared.theme.FontFirst
import com.lernhero.shared.theme.FontPixel
import kotlinx.coroutines.launch

private val Ink = Color(0xFF120B1F)
private val Gold = Color(0xFFFFD34D)
private val GoldDeep = Color(0xFFE08A1E)
private val Cream = Color(0xFFF4E9D7)
private val Muted = Color(0xFFA99BC4)
private val Green = Color(0xFF5FD08A)
private val Violet = Color(0xFF6B2FC4)

/** Ceny zapasowe - pokazywane, dopoki sklep nie poda wlasnych (np. przed podpieciem RevenueCat). */
private const val FALLBACK_MONTH = "29,99 zł"
private const val FALLBACK_YEAR = "199,99 zł"
private const val YEAR_DISCOUNT = "-44%"

/**
 * Okno subskrypcji premium - jedno dla calej gry. Otwieraja je panel generowania AI i czytnik po
 * wyczerpaniu tlumaczen, wiec [reason] mowi graczowi, CO go tu przyprowadzilo. Sklep pokazuje te
 * sama tresc bez okna - patrz [PremiumPaywallContent].
 */
@Composable
fun PremiumPaywall(
    subscription: SubscriptionService,
    onDismiss: () -> Unit,
    reason: String? = null
) {
    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.8f))
                .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null, onClick = onDismiss)
                .safeDrawingPadding()
                .padding(12.dp),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .widthIn(max = 440.dp)
                    .fillMaxWidth()
                    .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) {}
                    .verticalScroll(rememberScrollState())
            ) {
                PremiumPaywallContent(subscription = subscription, reason = reason, onClose = onDismiss)
            }
        }
    }
}

/**
 * Tresc paywalla: naglowek z korona w promieniach, tabela Free vs Premium (zeby bylo jasne, CO sie
 * dostaje) i dwa plany - miesieczny i roczny z wyroznieniem oszczednosci.
 */
@Composable
fun PremiumPaywallContent(
    subscription: SubscriptionService,
    reason: String? = null,
    onClose: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val isPremium by subscription.isPremium.collectAsState()
    val scope = rememberCoroutineScope()
    var offers by remember { mutableStateOf<List<SubscriptionOffer>?>(null) }
    var busy by remember { mutableStateOf(false) }
    var note by remember { mutableStateOf<String?>(null) }
    var yearly by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        offers = if (subscription.isAvailable) subscription.offers() else emptyList()
    }
    val monthOffer = offers?.firstOrNull { it.period == "month" }
    val yearOffer = offers?.firstOrNull { it.period == "year" }
    val chosen = if (yearly) yearOffer else monthOffer

    val infinite = rememberInfiniteTransition()
    val spin by infinite.animateFloat(0f, 360f, infiniteRepeatable(tween(20_000, easing = LinearEasing)))
    val glow by infinite.animateFloat(0f, 1f, infiniteRepeatable(tween(1500), RepeatMode.Reverse))
    val sweep by infinite.animateFloat(0f, 1f, infiniteRepeatable(tween(2600, easing = LinearEasing)))

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(Brush.verticalGradient(listOf(Color(0xFF2B1150), Ink, Color(0xFF0B0714))))
            .border(3.dp, Brush.verticalGradient(listOf(Gold, GoldDeep, Violet)), RoundedCornerShape(22.dp)),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // ---------------- Naglowek: korona w promieniach ----------------
        Box(
            modifier = Modifier.fillMaxWidth().height(190.dp).clipToBounds(),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val c = Offset(size.width / 2f, size.height * 0.42f)
                val r = size.maxDimension
                rotate(spin, c) {
                    for (i in 0 until 14) {
                        val a = (i * 2 * kotlin.math.PI / 14).toFloat()
                        val path = Path().apply {
                            moveTo(c.x, c.y)
                            lineTo(c.x + kotlin.math.cos(a - 0.09f) * r, c.y + kotlin.math.sin(a - 0.09f) * r)
                            lineTo(c.x + kotlin.math.cos(a + 0.09f) * r, c.y + kotlin.math.sin(a + 0.09f) * r)
                            close()
                        }
                        drawPath(path, Brush.radialGradient(listOf(Gold.copy(alpha = 0.35f), Color.Transparent), c, r * 0.6f))
                    }
                }
                drawCircle(Brush.radialGradient(listOf(Gold.copy(alpha = 0.45f), Color.Transparent), c, 110f * density), 110f * density, c)
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "👑",
                    fontSize = 64.sp,
                    modifier = Modifier.graphicsLayer {
                        translationY = -glow * 6f * density
                        rotationZ = (glow - 0.5f) * 6f
                    }
                )
                Outlined("PREMIUM", 34.sp, Gold)
                Text(
                    text = reason ?: "Learn faster. Play without limits.",
                    color = Cream,
                    fontFamily = FontFirst(),
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 20.dp)
                )
            }
            if (onClose != null) {
                Text(
                    text = "✕",
                    color = Cream,
                    fontWeight = FontWeight.Black,
                    fontSize = 16.sp,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(10.dp)
                        .clip(RoundedCornerShape(50))
                        .background(Color.Black.copy(alpha = 0.4f))
                        .clickable(onClick = onClose)
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                )
            }
        }

        Column(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).padding(bottom = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // ---------------- Co daje: Free vs Premium ----------------
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color.White.copy(alpha = 0.05f))
                    .border(1.5.dp, Gold.copy(alpha = 0.35f), RoundedCornerShape(14.dp))
                    .padding(vertical = 6.dp)
            ) {
                CompareRow(icon = "", title = "", free = "FREE", premium = "PREMIUM", header = true)
                CompareRow("💎", "Daily gems", "—", "${PremiumLimits.DAILY_GEMS}/day")
                CompareRow("✨", "AI quiz decks", "—", "${PremiumLimits.PREMIUM_AI_QUIZZES_PER_MONTH}/mo")
                CompareRow("📖", "Reader translations", "${PremiumLimits.FREE_TRANSLATIONS_PER_DAY}/day", "∞")
                CompareRow("📺", "Ads for rewards", "Watch", "Skip")
                CompareRow("💰", "Daily ad gold", "Ad each", "Instant")
                CompareRow("❤️", "Party revive", "Ad / 💎", "Instant")
                CompareRow("🎁", "Chest ad opens", "Ad", "Instant")
            }

            when {
                isPremium -> Text(
                    text = "✓ PREMIUM IS ACTIVE",
                    color = Green,
                    fontFamily = FontPixel(),
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    modifier = Modifier.padding(vertical = 8.dp)
                )

                else -> {
                    // ---------------- Plany ----------------
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        PlanCard(
                            title = "Monthly",
                            price = monthOffer?.price ?: FALLBACK_MONTH,
                            per = "/ month",
                            sub = "Cancel anytime",
                            selected = !yearly,
                            badge = null,
                            onClick = { yearly = false },
                            modifier = Modifier.weight(1f)
                        )
                        PlanCard(
                            title = "Yearly",
                            price = yearOffer?.price ?: FALLBACK_YEAR,
                            per = "/ year",
                            sub = "Only 16,67 zł / month",
                            selected = yearly,
                            badge = "BEST VALUE $YEAR_DISCOUNT",
                            onClick = { yearly = true },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    val canBuy = chosen != null && !busy
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(58.dp)
                            .graphicsLayer {
                                val s = if (canBuy) 1f + glow * 0.025f else 1f
                                scaleX = s
                                scaleY = s
                            }
                            .clip(RoundedCornerShape(16.dp))
                            .background(
                                Brush.verticalGradient(
                                    if (canBuy) listOf(Color(0xFFFFE27A), GoldDeep) else listOf(Color(0xFF6D6480), Color(0xFF443D55))
                                )
                            )
                            .border(3.dp, Color(0xFF3A1E00), RoundedCornerShape(16.dp))
                            .clickable(enabled = canBuy) {
                                val offer = chosen ?: return@clickable
                                busy = true
                                note = null
                                scope.launch {
                                    note = when (val outcome = subscription.purchase(offer.id)) {
                                        PurchaseOutcome.Success, PurchaseOutcome.Cancelled -> null
                                        is PurchaseOutcome.Failed -> outcome.reason
                                    }
                                    busy = false
                                }
                            }
                            .clipToBounds(),
                        contentAlignment = Alignment.Center
                    ) {
                        if (canBuy) {
                            Box(
                                modifier = Modifier
                                    .size(width = 30.dp, height = 120.dp)
                                    .graphicsLayer {
                                        translationX = (sweep * 2f - 1f) * size.width * 8f
                                        rotationZ = 20f
                                    }
                                    .background(Brush.horizontalGradient(listOf(Color.Transparent, Color.White.copy(alpha = 0.55f), Color.Transparent)))
                            )
                        }
                        Outlined(if (busy) "..." else "START PREMIUM", 20.sp, Color.White)
                    }

                    when {
                        offers == null -> Text("Loading prices...", color = Muted, fontFamily = FontFirst(), fontSize = 11.sp)
                        chosen == null -> Text(
                            "Subscriptions open soon - the store isn't connected yet.",
                            color = Muted,
                            fontFamily = FontFirst(),
                            fontSize = 11.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            note?.let { Text(text = it, color = Color(0xFFE7A08A), fontFamily = FontFirst(), fontSize = 11.sp, textAlign = TextAlign.Center) }

            if (subscription.isAvailable && !isPremium) {
                Text(
                    text = "Restore purchases",
                    color = Muted,
                    fontFamily = FontFirst(),
                    fontSize = 12.sp,
                    modifier = Modifier.clickable(enabled = !busy) {
                        busy = true
                        scope.launch {
                            note = if (subscription.restore()) null else "No active subscription found."
                            busy = false
                        }
                    }.padding(4.dp)
                )
            }
            Text(
                text = "Renews automatically until cancelled in your store account.",
                color = Muted.copy(alpha = 0.7f),
                fontFamily = FontFirst(),
                fontSize = 9.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun CompareRow(icon: String, title: String, free: String, premium: String, header: Boolean = false) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = icon, fontSize = 15.sp, modifier = Modifier.widthIn(min = 24.dp))
        Text(
            text = title,
            color = Cream,
            fontFamily = FontFirst(),
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = free,
            color = if (header) Muted else Muted,
            fontFamily = FontFirst(),
            fontWeight = FontWeight.Bold,
            fontSize = if (header) 10.sp else 12.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.widthIn(min = 64.dp)
        )
        Text(
            text = premium,
            color = Gold,
            fontFamily = FontFirst(),
            fontWeight = FontWeight.Black,
            fontSize = if (header) 10.sp else 13.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .widthIn(min = 72.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Gold.copy(alpha = 0.12f))
                .padding(vertical = 2.dp)
        )
    }
}

@Composable
private fun PlanCard(
    title: String,
    price: String,
    per: String,
    sub: String,
    selected: Boolean,
    badge: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.padding(top = 10.dp)) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(
                    if (selected) Brush.verticalGradient(listOf(Color(0xFF5B2BA8), Color(0xFF2A1250)))
                    else Brush.verticalGradient(listOf(Color(0xFF241A36), Color(0xFF17101F)))
                )
                .border(if (selected) 3.dp else 1.5.dp, if (selected) Gold else Muted.copy(alpha = 0.4f), RoundedCornerShape(16.dp))
                .clickable(onClick = onClick)
                .padding(top = 18.dp, bottom = 12.dp, start = 8.dp, end = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(text = title.uppercase(), color = if (selected) Gold else Muted, fontFamily = FontPixel(), fontWeight = FontWeight.Bold, fontSize = 12.sp)
            Outlined(price, 24.sp, Color.White)
            Text(text = per, color = Cream, fontFamily = FontFirst(), fontSize = 11.sp)
            Text(text = sub, color = if (selected) Green else Muted, fontFamily = FontFirst(), fontWeight = FontWeight.Bold, fontSize = 10.sp, textAlign = TextAlign.Center)
        }
        if (badge != null) {
            Text(
                text = badge,
                color = Color.White,
                fontFamily = FontFirst(),
                fontWeight = FontWeight.Black,
                fontSize = 10.sp,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .offset(y = (-10).dp)
                    .clip(RoundedCornerShape(50))
                    .background(Brush.horizontalGradient(listOf(Color(0xFFE5433B), Color(0xFFFF7A45))))
                    .border(1.5.dp, Color(0xFF3A0A06), RoundedCornerShape(50))
                    .padding(horizontal = 10.dp, vertical = 3.dp)
            )
        }
        if (selected) {
            Text(
                text = "✓",
                color = Color(0xFF1C1408),
                fontWeight = FontWeight.Black,
                fontSize = 12.sp,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = 4.dp, y = (-4).dp)
                    .clip(RoundedCornerShape(50))
                    .background(Gold)
                    .padding(horizontal = 7.dp, vertical = 2.dp)
            )
        }
    }
}

/** Gruby napis z ciemnym obrysem. */
@Composable
private fun Outlined(text: String, size: TextUnit, color: Color) {
    Box(contentAlignment = Alignment.Center) {
        Text(
            text = text,
            color = Color(0xFF1A0E02),
            fontFamily = FontFirst(),
            fontWeight = FontWeight.Black,
            fontSize = size,
            style = TextStyle(drawStyle = Stroke(width = 8f, join = StrokeJoin.Round))
        )
        Text(text = text, color = color, fontFamily = FontFirst(), fontWeight = FontWeight.Black, fontSize = size)
    }
}
