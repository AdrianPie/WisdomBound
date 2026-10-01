package com.lernhero.shared.monetization

import com.lernhero.shared.theme.GlyphText
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
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.lernhero.domain.monetization.PremiumLimits
import com.lernhero.shared.theme.FontFirst
import com.lernhero.shared.theme.FontPixel
import kotlinx.coroutines.launch
import kotlin.math.cos
import kotlin.math.sin

private val Ink = Color(0xFF120B1F)
private val Gold = Color(0xFFFFD34D)
private val GoldDeep = Color(0xFFE08A1E)
private val Cream = Color(0xFFF4E9D7)
private val Muted = Color(0xFFA99BC4)
private val Green = Color(0xFF5FD08A)
private val Violet = Color(0xFF6B2FC4)

/**
 * Co przyprowadzilo gracza na paywall. Od tego zalezy naglowek i to, ktora korzysc stoi na liscie
 * PIERWSZA - gracz, ktory chcial napisac kolejna talie, ma od razu zobaczyc "bez limitu talii",
 * a nie ogolnik o premium. To jest pierwsza rada z wytycznych RevenueCat: paywall kontekstowy
 * sprzedaje to, czego gracz wlasnie potrzebowal.
 */
enum class PaywallTrigger(
    val headline: String,
    val subline: String,
    val focus: PremiumBenefit?,
    /**
     * Placement w RevenueCat - w panelu (Targeting -> Placements) mozna pod kazde wejscie podpiac
     * inna oferte i inny paywall. Bez konfiguracji placementu leci biezaca oferta (Offerings.current).
     */
    val placement: String
) {
    General("Learn faster. Play without limits.", "Everything in WisdomBound, unlocked.", null, "shop"),
    QuizCreation("Create unlimited quizzes", "Turn anything you learn into your next battle.", PremiumBenefit.Decks, "quiz_creation"),
    AiDecks("Let AI write your decks", "Give a topic - get a full quiz in seconds.", PremiumBenefit.Ai, "ai_decks"),
    Translations("Translate without limits", "Read anything and look up every word.", PremiumBenefit.Translations, "translations")
}

/** Korzysci z subskrypcji - krotko, jezykiem gracza (co dostaje), a nie tabelka funkcji. */
enum class PremiumBenefit(val icon: String, val title: String) {
    Decks("✍️", "Unlimited quiz decks"),
    Ai("✨", "AI writes decks for you"),
    Gems("💎", "${PremiumLimits.DAILY_GEMS} gems every day"),
    Translations("📖", "Unlimited translations"),
    NoAds("⚡", "Rewards without ads");

    val detail: String
        get() = when (this) {
            Decks -> "Write as many of your own decks as you like"
            Ai -> "Up to ${PremiumLimits.PREMIUM_AI_QUIZZES_PER_MONTH} AI decks every month"
            Gems -> "${thousands(PremiumLimits.DAILY_GEMS * 30)} gems a month - the best deal in the shop"
            Translations -> "Free players get ${PremiumLimits.FREE_TRANSLATIONS_PER_DAY} a day in the reader"
            NoAds -> "Chests, gold and revives - instantly, no videos"
        }
}

/** 1500 -> "1,500" - tak jak liczby w reszcie sklepu. */
private fun thousands(value: Int): String = value.toString().reversed().chunked(3).joinToString(",").reversed()

/**
 * Okno subskrypcji premium - jedno dla calej gry.
 *
 * Pokazuje paywall zaprojektowany w panelu RevenueCat ([RevenueCatPaywall]) dla placementu
 * [PaywallTrigger.placement] - wyglad, ceny i testy A/B ustawia sie tam, bez nowego builda. Nasz
 * wlasny paywall ([CustomPaywallDialog]) zostaje tylko jako zapas: gdy na tej platformie nie ma
 * RevenueCat albo oferty nie daly sie wczytac.
 */
@Composable
fun PremiumPaywall(
    subscription: SubscriptionService,
    onDismiss: () -> Unit,
    reason: String? = null,
    trigger: PaywallTrigger = PaywallTrigger.General
) {
    RevenueCatPaywall(placementId = trigger.placement, onDismiss = onDismiss) {
        CustomPaywallDialog(subscription = subscription, onDismiss = onDismiss, reason = reason, trigger = trigger)
    }
}

/**
 * Wlasny paywall w oknie - zapas dla [PremiumPaywall]. [trigger] mowi, skad gracz przyszedl
 * (naglowek i kolejnosc korzysci), [reason] to opcjonalne zdanie kontekstu.
 */
@Composable
private fun CustomPaywallDialog(
    subscription: SubscriptionService,
    onDismiss: () -> Unit,
    reason: String?,
    trigger: PaywallTrigger
) {
    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.82f))
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
                PremiumPaywallContent(subscription = subscription, reason = reason, trigger = trigger, onClose = onDismiss, plansInline = true)
            }
        }
    }
}

/**
 * Tresc paywalla, ulozona wedlug wytycznych RevenueCat:
 *
 * 1. **Wartosc na gorze** - naglowek mowi, co gracz dostanie ([PaywallTrigger.headline]), nie "PREMIUM".
 * 2. **Kilka korzysci zamiast tabeli** - piec krotkich linijek; ta, po ktora gracz przyszedl, pierwsza.
 * 3. **Plan roczny wybrany z gory** i opisany uczciwie: oszczednosc w procentach i cena za miesiac.
 * 4. **Jeden przycisk**, a tuz pod nim jawna informacja: ile, za jaki okres, ze sie odnawia i gdzie
 *    anulowac - bez drobnego druku schowanego na dole.
 * 5. **Stopka**: przywracanie zakupow, Terms of Use, Privacy Policy (App Store 3.1.2).
 *
 * Ceny i okresy zawsze przychodza z RevenueCat ([SubscriptionService.offers]) w walucie gracza.
 */
@Composable
fun PremiumPaywallContent(
    subscription: SubscriptionService,
    reason: String? = null,
    onClose: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
    trigger: PaywallTrigger = PaywallTrigger.General,
    /**
     * true = plany i zakup tutaj, w naszym UI (zapas). false = jeden przycisk, ktory otwiera paywall
     * z RevenueCat - tak wyglada zakladka Premium w sklepie, gdy RevenueCat jest dostepny.
     */
    plansInline: Boolean = !revenueCatPaywallAvailable
) {
    val isPremium by subscription.isPremium.collectAsState()
    /** Paywall RevenueCat otwarty z przycisku (tryb bez planow na miejscu). */
    var storeOpen by remember { mutableStateOf(false) }
    /** RevenueCat zawiodl - wracamy do planow na miejscu, zeby dalo sie cokolwiek kupic. */
    var inline by remember(plansInline) { mutableStateOf(plansInline) }
    val scope = rememberCoroutineScope()
    val uriHandler = LocalUriHandler.current
    var offers by remember { mutableStateOf<List<SubscriptionOffer>?>(null) }
    var busy by remember { mutableStateOf(false) }
    var note by remember { mutableStateOf<String?>(null) }
    var yearly by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        offers = if (subscription.isAvailable) subscription.offers().also { StorePriceCache.putOffers(it) } else emptyList()
    }
    val monthOffer = offers?.firstOrNull { it.period == "month" }
    val yearOffer = offers?.firstOrNull { it.period == "year" }
    // Nie ma rocznego w ofercie - nie udajemy, ze jest wybrany.
    val yearlyChosen = yearly && !(offers != null && yearOffer == null && monthOffer != null)
    val chosen = if (yearlyChosen) yearOffer else monthOffer
    // Ceny zawsze ze sklepu (waluta gracza); zanim odpowie - ostatnie zapamietane, nigdy zlotowki na sztywno.
    val monthPrice = monthOffer?.price ?: StorePriceCache.get(StorePriceCache.SUB_MONTH) ?: StorePriceCache.PENDING
    val yearPrice = yearOffer?.price ?: StorePriceCache.get(StorePriceCache.SUB_YEAR) ?: StorePriceCache.PENDING
    val yearPerMonth = yearOffer?.pricePerMonth ?: StorePriceCache.get(StorePriceCache.SUB_YEAR_PER_MONTH)
    val discount = StorePriceCache.yearlyDiscount(monthOffer, yearOffer) ?: StorePriceCache.get(StorePriceCache.SUB_DISCOUNT)

    val infinite = rememberInfiniteTransition()
    val spin by infinite.animateFloat(0f, 360f, infiniteRepeatable(tween(24_000, easing = LinearEasing)))
    val glow by infinite.animateFloat(0f, 1f, infiniteRepeatable(tween(1500), RepeatMode.Reverse))
    val sweep by infinite.animateFloat(0f, 1f, infiniteRepeatable(tween(2600, easing = LinearEasing)))

    // Korzysc, po ktora gracz przyszedl, stoi pierwsza.
    val benefits = remember(trigger) {
        val focus = trigger.focus
        if (focus == null) PremiumBenefit.entries else listOf(focus) + (PremiumBenefit.entries - focus)
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(Brush.verticalGradient(listOf(Color(0xFF2B1150), Ink, Color(0xFF0B0714))))
            .border(3.dp, Brush.verticalGradient(listOf(Gold, GoldDeep, Violet)), RoundedCornerShape(22.dp)),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // ---------------- 1. Naglowek: wartosc, nie nazwa produktu ----------------
        Box(modifier = Modifier.fillMaxWidth().clipToBounds(), contentAlignment = Alignment.TopCenter) {
            Canvas(modifier = Modifier.matchParentSize()) {
                val c = Offset(size.width / 2f, 78.dp.toPx())
                val r = size.maxDimension
                rotate(spin, c) {
                    for (i in 0 until 14) {
                        val a = (i * 2 * kotlin.math.PI / 14).toFloat()
                        val path = Path().apply {
                            moveTo(c.x, c.y)
                            lineTo(c.x + cos(a - 0.08f) * r, c.y + sin(a - 0.08f) * r)
                            lineTo(c.x + cos(a + 0.08f) * r, c.y + sin(a + 0.08f) * r)
                            close()
                        }
                        drawPath(path, Brush.radialGradient(listOf(Gold.copy(alpha = 0.28f), Color.Transparent), c, r * 0.5f))
                    }
                }
                drawCircle(Brush.radialGradient(listOf(Gold.copy(alpha = 0.5f), Color.Transparent), c, 90.dp.toPx()), 90.dp.toPx(), c)
                // Iskierki wokol korony.
                for (i in 0 until 10) {
                    val a = i * 0.63f + spin / 60f
                    val d = (54 + (i * 37) % 60).dp.toPx()
                    val tw = ((sin(i * 1.7f + glow * 3.14f) + 1f) / 2f)
                    drawCircle(Color.White.copy(alpha = 0.25f + 0.6f * tw), (1.2f + 1.6f * tw).dp.toPx(), Offset(c.x + cos(a) * d, c.y + sin(a) * d * 0.7f))
                }
            }
            Column(
                modifier = Modifier.fillMaxWidth().padding(top = 18.dp, start = 18.dp, end = 18.dp, bottom = 6.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                GlyphText(
                    text = "👑",
                    fontSize = 58.sp,
                    modifier = Modifier.graphicsLayer {
                        translationY = -glow * 6f * density
                        rotationZ = (glow - 0.5f) * 6f
                    }
                )
                GlyphText(
                    text = "WISDOMBOUND PREMIUM",
                    color = Ink,
                    fontFamily = FontPixel(),
                    fontWeight = FontWeight.Bold,
                    fontSize = 10.sp,
                    letterSpacing = 1.5.sp,
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(Brush.horizontalGradient(listOf(Gold, Color(0xFFFFE9A3), Gold)))
                        .padding(horizontal = 12.dp, vertical = 3.dp)
                )
                Outlined(trigger.headline, 25.sp, Color.White, textAlign = TextAlign.Center)
                GlyphText(
                    text = trigger.subline,
                    color = Cream,
                    fontFamily = FontFirst(),
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center
                )
                reason?.let {
                    GlyphText(
                        text = it,
                        color = Gold,
                        fontFamily = FontFirst(),
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .background(Gold.copy(alpha = 0.12f))
                            .border(1.dp, Gold.copy(alpha = 0.4f), RoundedCornerShape(50))
                            .padding(horizontal = 12.dp, vertical = 4.dp)
                    )
                }
            }
            if (onClose != null) {
                // Zamkniecie widoczne, ale nie krzykliwe - gracz nie moze miec wrazenia, ze jest w pulapce.
                GlyphText(
                    text = "✕",
                    color = Cream.copy(alpha = 0.8f),
                    fontWeight = FontWeight.Black,
                    fontSize = 15.sp,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(10.dp)
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.35f))
                        .clickable(onClick = onClose)
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                )
            }
        }

        Column(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).padding(bottom = 14.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // ---------------- 2. Korzysci ----------------
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                benefits.forEach { benefit ->
                    BenefitRow(benefit = benefit, highlighted = benefit == trigger.focus)
                }
            }

            if (isPremium) {
                // ---------------- Premium juz jest ----------------
                GlyphText(
                    text = "✓ PREMIUM IS ACTIVE",
                    color = Green,
                    fontFamily = FontPixel(),
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    modifier = Modifier.padding(top = 4.dp)
                )
                if (manageSubscriptionsUrl.isNotBlank()) {
                    FooterLink("Manage subscription") { uriHandler.openUri(manageSubscriptionsUrl) }
                }
            } else if (!inline) {
                // ---------------- 3-4. Plany i zakup w paywallu RevenueCat ----------------
                GoldCta(text = "START PREMIUM", enabled = true, glow = glow, sweep = sweep) { storeOpen = true }
                GlyphText(
                    text = "See plans and prices on the next screen. Cancel anytime in $storeDisplayName.",
                    color = Muted,
                    fontFamily = FontFirst(),
                    fontSize = 10.sp,
                    textAlign = TextAlign.Center
                )
                if (storeOpen) {
                    RevenueCatPaywall(placementId = trigger.placement, onDismiss = { storeOpen = false }) {
                        LaunchedEffect(Unit) {
                            storeOpen = false
                            inline = true
                        }
                    }
                }
            } else {
                // ---------------- 3. Plany ----------------
                Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    if (yearOffer != null || offers == null) {
                        PlanOption(
                            title = "Yearly",
                            price = yearPrice,
                            period = "/ year",
                            detail = yearPerMonth?.let { "Just $it / month" } ?: "Billed once a year",
                            badge = listOfNotNull("BEST VALUE", discount?.let { "SAVE ${it.removePrefix("-")}" }).joinToString(" · "),
                            selected = yearlyChosen,
                            onClick = { yearly = true }
                        )
                    }
                    if (monthOffer != null || offers == null) {
                        PlanOption(
                            title = "Monthly",
                            price = monthPrice,
                            period = "/ month",
                            detail = "Billed every month",
                            badge = null,
                            selected = !yearlyChosen,
                            onClick = { yearly = false }
                        )
                    }
                }

                // ---------------- 4. Jeden przycisk + jawne warunki ----------------
                val canBuy = chosen != null && !busy
                GoldCta(text = if (busy) "..." else "START PREMIUM", enabled = canBuy, glow = glow, sweep = sweep) {
                    val offer = chosen ?: return@GoldCta
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

                when {
                    offers == null -> GlyphText("Loading prices...", color = Muted, fontFamily = FontFirst(), fontSize = 11.sp)
                    chosen == null -> GlyphText(
                        "Subscriptions aren't available on this device right now.",
                        color = Muted,
                        fontFamily = FontFirst(),
                        fontSize = 11.sp,
                        textAlign = TextAlign.Center
                    )
                    else -> GlyphText(
                        text = "${chosen.price} ${if (yearlyChosen) "per year" else "per month"}. Renews automatically " +
                            "until cancelled. Cancel anytime in $storeDisplayName.",
                        color = Muted,
                        fontFamily = FontFirst(),
                        fontSize = 10.sp,
                        lineHeight = 13.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 6.dp)
                    )
                }
            }

            note?.let { GlyphText(text = it, color = Color(0xFFE7A08A), fontFamily = FontFirst(), fontSize = 11.sp, textAlign = TextAlign.Center) }

            // ---------------- 5. Stopka ----------------
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                val links = buildList {
                    if (subscription.isAvailable && !isPremium && inline) {
                        add("Restore" to {
                            if (!busy) {
                                busy = true
                                scope.launch {
                                    note = if (subscription.restore()) null else "No active subscription found."
                                    busy = false
                                }
                            }
                        })
                    }
                    if (termsOfUseUrl.isNotBlank()) add("Terms of Use" to { uriHandler.openUri(termsOfUseUrl) })
                    if (LegalLinks.PRIVACY_POLICY_URL.isNotBlank()) {
                        add("Privacy Policy" to { uriHandler.openUri(LegalLinks.PRIVACY_POLICY_URL) })
                    }
                }
                links.forEachIndexed { index, (label, action) ->
                    if (index > 0) GlyphText("·", color = Muted.copy(alpha = 0.6f), fontSize = 11.sp)
                    FooterLink(label, onClick = action)
                }
            }
        }
    }
}

/** Zloty przycisk zakupu z przelatujacym blyskiem - jeden na caly paywall. */
@Composable
private fun GoldCta(text: String, enabled: Boolean, glow: Float, sweep: Float, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(58.dp)
            .graphicsLayer {
                val s = if (enabled) 1f + glow * 0.02f else 1f
                scaleX = s
                scaleY = s
            }
            .clip(RoundedCornerShape(16.dp))
            .background(
                Brush.verticalGradient(
                    if (enabled) listOf(Color(0xFFFFE27A), GoldDeep) else listOf(Color(0xFF6D6480), Color(0xFF443D55))
                )
            )
            .border(3.dp, Color(0xFF3A1E00), RoundedCornerShape(16.dp))
            .clickable(enabled = enabled, onClick = onClick)
            .clipToBounds(),
        contentAlignment = Alignment.Center
    ) {
        if (enabled) {
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
        Outlined(text, 20.sp, Color.White)
    }
}

@Composable
private fun BenefitRow(benefit: PremiumBenefit, highlighted: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(if (highlighted) Gold.copy(alpha = 0.14f) else Color.White.copy(alpha = 0.04f))
            .border(if (highlighted) 1.5.dp else 1.dp, if (highlighted) Gold.copy(alpha = 0.7f) else Color.White.copy(alpha = 0.08f), RoundedCornerShape(14.dp))
            .padding(horizontal = 10.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(
                    if (highlighted) Brush.verticalGradient(listOf(Color(0xFFFFE27A), GoldDeep))
                    else Brush.verticalGradient(listOf(Color(0xFF5B2BA8), Color(0xFF2A1250)))
                ),
            contentAlignment = Alignment.Center
        ) {
            GlyphText(text = benefit.icon, fontSize = 17.sp)
        }
        Column(modifier = Modifier.weight(1f)) {
            GlyphText(
                text = benefit.title,
                color = if (highlighted) Gold else Cream,
                fontFamily = FontFirst(),
                fontWeight = FontWeight.Black,
                fontSize = 14.sp
            )
            GlyphText(
                text = benefit.detail,
                color = Muted,
                fontFamily = FontFirst(),
                fontSize = 11.sp,
                lineHeight = 14.sp
            )
        }
        GlyphText(text = "✓", color = Green, fontWeight = FontWeight.Black, fontSize = 16.sp)
    }
}

/** Plan jako wiersz z kolkiem wyboru: nazwa i "ile to na miesiac" po lewej, cena po prawej. */
@Composable
private fun PlanOption(
    title: String,
    price: String,
    period: String,
    detail: String,
    badge: String?,
    selected: Boolean,
    onClick: () -> Unit
) {
    Box(modifier = Modifier.fillMaxWidth().padding(top = if (badge != null) 8.dp else 0.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(
                    if (selected) Brush.verticalGradient(listOf(Color(0xFF5B2BA8), Color(0xFF2A1250)))
                    else Brush.verticalGradient(listOf(Color(0xFF241A36), Color(0xFF17101F)))
                )
                .border(if (selected) 2.5.dp else 1.5.dp, if (selected) Gold else Muted.copy(alpha = 0.35f), RoundedCornerShape(16.dp))
                .clickable(onClick = onClick)
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Kolko wyboru.
            Box(
                modifier = Modifier
                    .size(22.dp)
                    .clip(CircleShape)
                    .border(2.dp, if (selected) Gold else Muted, CircleShape)
                    .padding(4.dp)
                    .clip(CircleShape)
                    .background(if (selected) Gold else Color.Transparent)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                GlyphText(
                    text = title,
                    color = if (selected) Color.White else Cream,
                    fontFamily = FontFirst(),
                    fontWeight = FontWeight.Black,
                    fontSize = 16.sp
                )
                GlyphText(
                    text = detail,
                    color = if (selected) Green else Muted,
                    fontFamily = FontFirst(),
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Outlined(price, 20.sp, Color.White)
                GlyphText(text = period, color = Cream, fontFamily = FontFirst(), fontSize = 11.sp)
            }
        }
        if (badge != null) {
            GlyphText(
                text = badge,
                color = Color.White,
                fontFamily = FontFirst(),
                fontWeight = FontWeight.Black,
                fontSize = 10.sp,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = (-12).dp, y = (-9).dp)
                    .clip(RoundedCornerShape(50))
                    .background(Brush.horizontalGradient(listOf(Color(0xFFE5433B), Color(0xFFFF7A45))))
                    .border(1.5.dp, Color(0xFF3A0A06), RoundedCornerShape(50))
                    .padding(horizontal = 10.dp, vertical = 3.dp)
            )
        }
    }
}

@Composable
private fun FooterLink(label: String, onClick: () -> Unit) {
    GlyphText(
        text = label,
        color = Muted,
        fontFamily = FontFirst(),
        fontWeight = FontWeight.Bold,
        fontSize = 11.sp,
        style = TextStyle(textDecoration = TextDecoration.Underline),
        modifier = Modifier.clip(RoundedCornerShape(6.dp)).clickable(onClick = onClick).padding(horizontal = 4.dp, vertical = 6.dp)
    )
}

/** Gruby napis z ciemnym obrysem. */
@Composable
private fun Outlined(text: String, size: TextUnit, color: Color, textAlign: TextAlign? = null) {
    Box(contentAlignment = Alignment.Center) {
        GlyphText(
            text = text,
            color = Color(0xFF1A0E02),
            fontFamily = FontFirst(),
            fontWeight = FontWeight.Black,
            fontSize = size,
            textAlign = textAlign,
            style = TextStyle(drawStyle = Stroke(width = 8f, join = StrokeJoin.Round))
        )
        // Jawne Fill - patrz StrokedText: bez niego stary Android rysuje i ten napis konturem.
        GlyphText(
            text = text, color = color, fontFamily = FontFirst(), fontWeight = FontWeight.Black, fontSize = size,
            textAlign = textAlign,
            style = TextStyle(drawStyle = Fill)
        )
    }
}
