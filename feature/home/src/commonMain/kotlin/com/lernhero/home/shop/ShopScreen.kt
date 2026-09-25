package com.lernhero.home.shop

import androidx.compose.foundation.background
import kotlinx.coroutines.launch
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import com.lernhero.home.navigation.bottomBarSpace
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.foundation.clickable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lernhero.home.player.PlayerViewModel
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.statusBars
import com.lernhero.home.ui.TOP_BAR_HEIGHT
import com.lernhero.home.ui.TOP_BAR_PLATE_HEIGHT
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.TextStyle
import com.lernhero.domain.monetization.ShopChest
import com.lernhero.domain.monetization.LegendPack
import com.lernhero.domain.monetization.GemPack
import com.lernhero.domain.monetization.TicketPack
import com.lernhero.domain.item.ItemCatalog
import com.lernhero.home.ui.icon
import com.lernhero.shared.monetization.PremiumPaywallContent
import com.lernhero.domain.run.ChestSlot
import com.lernhero.home.story.BossChestOverlay
import com.lernhero.home.story.ChestPrize
import com.lernhero.domain.monetization.Bundle
import com.lernhero.domain.monetization.ChestOpening
import com.lernhero.domain.monetization.EnergyRefill
import com.lernhero.domain.monetization.PremiumLimits
import com.lernhero.domain.monetization.SupplyDeal
import com.lernhero.reader.todayDateKey
import com.lernhero.shared.resources.Resources
import kotlinx.coroutines.delay
import org.jetbrains.compose.resources.painterResource
import org.koin.compose.viewmodel.koinViewModel
import kotlin.math.roundToInt
import kotlin.time.Clock
import com.lernhero.shared.monetization.PremiumPaywall
import com.lernhero.shared.theme.FontFirst
import com.lernhero.shared.theme.StrokedText
import com.lernhero.shared.theme.FontPixel

private val PanelInk = Color(0xFF19120C)
private val Frame = Color(0xFFC8A45A)
private val Cream = Color(0xFFF4E9D7)
private val Muted = Color(0xFF9D8B6A)
private val GoldText = Color(0xFFFFD34D)
private val GemBlue = Color(0xFF6FB7FF)

private enum class ShopTab(val label: String) { Chests("Chests"), Supplies("Supplies"), Gems("Gems"), Premium("Premium") }

/**
 * Sklep w stylu gier mobilnych: saldo przyklejone u gory, zakladki, a na pierwszej - skrzynie ze
 * sprzetem otwierane za reklame albo diamenty (patrz [ShopChest]). Pakiety za prawdziwe pieniadze
 * i subskrypcja siedza w osobnych zakladkach.
 */
@Composable
fun ShopScreen(viewModel: PlayerViewModel) {
    val state by viewModel.uiState.collectAsState()
    val isPremium by viewModel.isPremium.collectAsState()
    var paywallOpen by remember { mutableStateOf(false) }
    var tab by remember { mutableStateOf(ShopTab.Chests) }
    var adConfirm by remember { mutableStateOf<ShopChest?>(null) }
    var opening by remember { mutableStateOf<ChestOpening?>(null) }
    var legendCelebration by remember { mutableStateOf(false) }
    var reward by remember { mutableStateOf<RewardBurst?>(null) }
    LaunchedEffect(Unit) { viewModel.refreshLegendPack() }
    // "+" w gornym pasku prosi o konkretna zakladke - bierzemy ja i czyscimy prosbe.
    LaunchedEffect(state.requestedShopTab) {
        state.requestedShopTab?.let { requested ->
            ShopTab.entries.firstOrNull { it.name == requested }?.let { tab = it }
            viewModel.consumeShopTab()
        }
    }
    val level = state.progress.level
    val today = remember { todayDateKey() }
    var now by remember { mutableStateOf(Clock.System.now().toEpochMilliseconds()) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(30_000)
            now = Clock.System.now().toEpochMilliseconds()
        }
    }

    if (paywallOpen) {
        PremiumPaywall(subscription = viewModel.subscriptionService, onDismiss = { paywallOpen = false })
    }

    val statusTop = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = statusTop + TOP_BAR_HEIGHT + 12.dp)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            TabRow(selected = tab, onSelect = { tab = it })

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                when (tab) {
                    ShopTab.Chests -> {
                        PremiumDailyCard(
                            isPremium = isPremium,
                            claimedToday = state.premiumGemsDay == today,
                            onClaim = { viewModel.claimPremiumGems { reward = RewardBurst("💎", it) } },
                            onSeePremium = { tab = ShopTab.Premium }
                        )
                        if (!state.legendPackClaimed) {
                            LegendPackCard(
                                price = state.legendPackPrice ?: LegendPack.FALLBACK_PRICE,
                                busy = state.buyingLegendPack,
                                onBuy = { viewModel.buyLegendPack { legendCelebration = true } }
                            )
                        }
                        SectionBanner("Free Daily Chest")
                        DailyChestCard(
                            level = level,
                            remaining = state.shopChestAds.dailyRemaining(today),
                            available = state.shopChestAds.canWatch(ShopChest.Daily, today, now) && !state.watchingAd,
                            onAd = { adConfirm = ShopChest.Daily }
                        )
                        SectionBanner("Equipment Chests")
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            ShopChest.entries.filter { it.forGems }.forEach { chest ->
                                ChestCard(
                                    chest = chest,
                                    canAffordGems = state.gems >= chest.gemCost,
                                    level = level,
                                    pity = state.epicChestPity,
                                    onGems = {
                                        viewModel.openShopChest(chest, withAd = false) { opening = it }
                                    },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                        state.message?.let {
                            Text(text = it, color = Muted, fontFamily = FontFirst(), fontSize = 12.sp, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
                        }
                        Text(
                            text = "Chest contents grow with your hero level.",
                            color = Muted,
                            fontFamily = FontFirst(),
                            fontSize = 11.sp,
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center
                        )
                    }
                    ShopTab.Supplies -> {
                        SectionBanner("Energy")
                        EnergyDealCard(
                            refillsToday = viewModel.energyRefillsToday(),
                            gems = state.gems,
                            onBuy = { viewModel.buyEnergyRefill { b -> reward = RewardBurst("⚡", b.energy) } }
                        )
                        SectionBanner("Gold")
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf(SupplyDeal.GoldPouch, SupplyDeal.GoldSack, SupplyDeal.GoldVault).forEach { deal ->
                                SupplyCard(
                                    deal = deal,
                                    bundle = deal.bundleFor(level),
                                    affordable = state.gems >= deal.gems,
                                    onBuy = { viewModel.buySupply(deal) { b -> reward = b.toBurst() } },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                        SectionBanner("Materials")
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf(SupplyDeal.OreCrate, SupplyDeal.EssenceFlask, SupplyDeal.StoneCart).forEach { deal ->
                                SupplyCard(
                                    deal = deal,
                                    bundle = deal.bundleFor(level),
                                    affordable = state.gems >= deal.gems,
                                    onBuy = { viewModel.buySupply(deal) { b -> reward = b.toBurst() } },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                        SectionBanner("Summon Tickets")
                        TicketPackCard(
                            pack = TicketPack.Few,
                            affordable = state.gems >= TicketPack.Few.gems,
                            onBuy = { viewModel.buyTicketPack(TicketPack.Few) { reward = RewardBurst("🎟", it) } }
                        )
                        TicketPack.entries.drop(1).forEach { pack ->
                            TicketPackCard(
                                pack = pack,
                                affordable = state.gems >= pack.gems,
                                onBuy = { viewModel.buyTicketPack(pack) { reward = RewardBurst("🎟", it) } }
                            )
                        }
                        state.message?.let {
                            Text(text = it, color = Muted, fontFamily = FontFirst(), fontSize = 12.sp, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
                        }
                        Text(
                            text = "Gold and material amounts grow with your hero level.",
                            color = Muted,
                            fontFamily = FontFirst(),
                            fontSize = 11.sp,
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center
                        )
                    }
                    ShopTab.Gems -> {
                        SectionBanner("Gems")
                        GemPack.entries.chunked(2).forEach { row ->
                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                row.forEach { pack ->
                                    GemPackCard(
                                        pack = pack,
                                        firstBuy = pack.productId !in state.gemPacksBought,
                                        price = state.gemPackPrices[pack.productId] ?: pack.fallbackPrice,
                                        busy = state.buyingGemPack == pack.productId,
                                        onBuy = { viewModel.buyGemPack(pack) { reward = RewardBurst("💎", it) } },
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }
                        }
                        state.message?.let {
                            Text(text = it, color = Muted, fontFamily = FontFirst(), fontSize = 12.sp, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
                        }
                        Text(
                            text = "Your first purchase of every pack gives DOUBLE gems.",
                            color = GoldText,
                            fontFamily = FontFirst(),
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center
                        )
                    }
                    ShopTab.Premium -> PremiumPaywallContent(subscription = viewModel.subscriptionService)
                }

                // Ogon pod dolny pasek: tresc przewija sie ZA niego, wiec ostatnia karta potrzebuje
                // miejsca, zeby dalo sie ja wysunac spod plyty.
                Spacer(modifier = Modifier.height(bottomBarSpace()))
            }
        }

        adConfirm?.let { chest ->
            AdConfirmDialog(
                chest = chest,
                onCancel = { adConfirm = null },
                onWatch = {
                    adConfirm = null
                    viewModel.openShopChest(chest, withAd = true) { opening = it }
                }
            )
        }

        reward?.let { r -> RewardBurstOverlay(burst = r, onDone = { reward = null }) }

        if (legendCelebration) {
            LegendPackCelebration(onDone = { legendCelebration = false })
        }

        opening?.let { result ->
            val chest = result.chest
            val prize = remember(result) { result.displayPrize() }
            // Scena miedzy paskami: od dolu plyty gornego paska (pod zwisajacym portretem) do gory
            // dolnego paska - oba paski zostaja widoczne, a tlo zakladki nigdzie nie przeswituje.
            BossChestOverlay(
                modifier = Modifier.padding(top = statusTop + TOP_BAR_PLATE_HEIGHT),
                // Tlo schodzi az do dolu ekranu, POD dolny pasek - zadnej szpary nad nim.
                bottomInset = bottomBarSpace(),
                dungeonLevel = 1,
                slot = result.slot,
                prize = prize,
                title = chest.title.uppercase(),
                chanceOf = chest::chanceOf,
                // Wszystko jest juz zapisane (patrz [PlayerViewModel.openShopChest]) - CLAIM tylko zamyka.
                extras = result.guaranteed.entries(),
                onClaim = { opening = null }
            )
        }
    }
}

@Composable
private fun TabRow(selected: ShopTab, onSelect: (ShopTab) -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
        ShopTab.entries.forEach { tab ->
            val active = tab == selected
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (active) Brush.verticalGradient(listOf(Color(0xFF5BB8FF), Color(0xFF2C7BD6))) else Brush.verticalGradient(listOf(Color(0xFFE9D6B4), Color(0xFFC9AE84))))
                    .border(2.dp, if (active) Color(0xFF1B4E8C) else Color(0xFF7A5A34), RoundedCornerShape(10.dp))
                    .clickable { onSelect(tab) }
                    .padding(vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                OutlinedLabel(tab.label, 13.sp, if (active) Color.White else Color(0xFF4A3218), outline = active)
            }
        }
    }
}

@Composable
private fun SectionBanner(text: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Brush.horizontalGradient(listOf(Color.Transparent, Color(0xFF6B4A2A), Color(0xFF6B4A2A), Color.Transparent)))
            .padding(vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        OutlinedLabel(text, 20.sp, Color.White)
    }
}

/** Gruby bialy napis z ciemnym obrysem - jak na przyciskach i naglowkach w referencji. */
@Composable
private fun OutlinedLabel(text: String, size: androidx.compose.ui.unit.TextUnit, color: Color, outline: Boolean = true, modifier: Modifier = Modifier) {
    StrokedText(text, size, color, Color(0xFF1A1208), modifier, strokeWidth = if (outline) 6f else 0f)
}

private fun ShopChest.headerColors(): List<Color> = when (this) {
    ShopChest.Daily -> listOf(Color(0xFF5CC84A), Color(0xFF2F8A2A))
    ShopChest.Basic -> listOf(Color(0xFF3C8FE0), Color(0xFF2361A8))
    ShopChest.Epic -> listOf(Color(0xFF9A4FD6), Color(0xFF6A2DA6))
}

private fun ShopChest.bodyGlow(): Color = when (this) {
    ShopChest.Daily -> Color(0xFFD4F7C4)
    ShopChest.Basic -> Color(0xFFBFE3FF)
    ShopChest.Epic -> Color(0xFFE6C7FF)
}

private fun ShopChest.image() = when (this) {
    ShopChest.Daily, ShopChest.Basic -> Resources.Image.shopChestBasic
    ShopChest.Epic -> Resources.Image.shopChestEpic
}

private fun ShopChest.pitch(): String = when (this) {
    ShopChest.Daily -> "+ 1 small prize: gold, a ticket or gear"
    ShopChest.Basic -> "+ 1 prize: gear, tickets or a Legendary"
    ShopChest.Epic -> "+ 1 prize: Rare+ gear, heroic tickets or a Legendary"
}

/** "x1.5 VALUE" - srednia wartosc otwarcia wzgledem ceny, zaokraglona do najblizszej polowki. */
private fun ShopChest.valueLabel(level: Int): String {
    val halves = (expectedValue(level) / gemCost * 2f).roundToInt()
    return "x${halves / 2}${if (halves % 2 == 1) ".5" else ""} VALUE"
}

/**
 * Darmowa skrzynia za reklame - szeroka, pozioma karta NAD skrzyniami za diamenty. Inny ksztalt niz
 * tamte dwie celowo: to codzienny prezent, a nie towar, i nie moze wygladac jak tansza wersja
 * [ShopChest.Basic].
 */
@Composable
private fun DailyChestCard(
    level: Int,
    remaining: Int,
    available: Boolean,
    onAd: () -> Unit
) {
    val chest = ShopChest.Daily
    val guaranteed = remember(level) { chest.guaranteedFor(level) }
    val bob by rememberInfiniteTransition().animateFloat(0f, 1f, infiniteRepeatable(tween(1400, easing = FastOutSlowInEasing), RepeatMode.Reverse))
    val colors = chest.headerColors()
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Brush.horizontalGradient(listOf(colors.last(), Color(0xFF1F5C1C))))
            .border(3.dp, Color(0xFF14202E), RoundedCornerShape(14.dp))
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Box(
            modifier = Modifier
                .size(92.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(Brush.radialGradient(listOf(Color.White, chest.bodyGlow(), colors.first()))),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(chest.image()),
                contentDescription = chest.title,
                modifier = Modifier
                    .fillMaxWidth(0.82f)
                    .graphicsLayer { translationY = -bob * 4f * density }
            )
            Text(
                text = "FREE",
                color = Color.White,
                fontFamily = FontFirst(),
                fontWeight = FontWeight.Black,
                fontSize = 10.sp,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(4.dp)
                    .clip(RoundedCornerShape(50))
                    .background(Brush.horizontalGradient(listOf(Color(0xFFE5433B), Color(0xFFFF7A45))))
                    .border(1.5.dp, Color(0xFF3A0A06), RoundedCornerShape(50))
                    .padding(horizontal = 6.dp, vertical = 1.dp)
            )
        }
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
            OutlinedLabel(chest.title, 16.sp, Color.White)
            guaranteed.entries().chunked(2).forEach { row ->
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    row.forEach { (icon, amount) ->
                        Text(
                            text = "$icon ${amount.grouped()}",
                            color = Color.White,
                            fontFamily = FontFirst(),
                            fontWeight = FontWeight.Black,
                            fontSize = 12.sp
                        )
                    }
                }
            }
            Text(
                text = chest.pitch(),
                color = Color(0xFFFFE27A),
                fontFamily = FontFirst(),
                fontWeight = FontWeight.Bold,
                fontSize = 10.sp,
                lineHeight = 12.sp
            )
        }
        ChunkyButton(
            top = if (remaining > 0) "▶ AD" else "DONE",
            bottom = if (remaining > 0) "$remaining/${ShopChest.DAILY_ADS_PER_DAY}" else "tomorrow",
            enabled = available,
            colors = listOf(Color(0xFF8BE05A), Color(0xFF3F9E2B)),
            onClick = onAd,
            modifier = Modifier.width(78.dp)
        )
    }
}

@Composable
private fun ChestCard(
    chest: ShopChest,
    canAffordGems: Boolean,
    level: Int,
    pity: Int,
    onGems: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showOdds by remember { mutableStateOf(false) }
    val guaranteed = remember(chest, level) { chest.guaranteedFor(level) }
    val spin by rememberInfiniteTransition().animateFloat(0f, 360f, infiniteRepeatable(tween(16_000, easing = LinearEasing)))
    val bob by rememberInfiniteTransition().animateFloat(0f, 1f, infiniteRepeatable(tween(1400, easing = FastOutSlowInEasing), RepeatMode.Reverse))
    val headerColors = chest.headerColors()
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(headerColors.last())
            .border(3.dp, Color(0xFF14202E), RoundedCornerShape(14.dp))
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Brush.verticalGradient(headerColors))
                .padding(vertical = 8.dp, horizontal = 6.dp),
            contentAlignment = Alignment.Center
        ) {
            OutlinedLabel(chest.title, 16.sp, Color.White)
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1.1f)
                .clipToBounds()
                .background(Brush.radialGradient(listOf(Color.White, chest.bodyGlow(), headerColors.first())))
                .clickable { showOdds = !showOdds },
            contentAlignment = Alignment.Center
        ) {
            if (showOdds) {
                OddsTable(chest)
            } else {
                Canvas(modifier = Modifier.fillMaxSize().rotate(spin)) {
                    val rays = 12
                    val r = size.maxDimension
                    for (i in 0 until rays) {
                        rotate(i * 360f / rays) {
                            val path = Path().apply {
                                moveTo(center.x, center.y)
                                lineTo(center.x - r * 0.12f, center.y - r)
                                lineTo(center.x + r * 0.12f, center.y - r)
                                close()
                            }
                            drawPath(path, Color.White.copy(alpha = 0.35f))
                        }
                    }
                }
                Image(
                    painter = painterResource(chest.image()),
                    contentDescription = chest.title,
                    modifier = Modifier
                        .fillMaxWidth(0.78f)
                        .graphicsLayer { translationY = -bob * 6f * density }
                )
                // Mnoznik wartosci - to jest odpowiedz na "czy to sie oplaca", zanim ktos zapyta.
                Text(
                    text = chest.valueLabel(level),
                    color = Color.White,
                    fontFamily = FontFirst(),
                    fontWeight = FontWeight.Black,
                    fontSize = 10.sp,
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(6.dp)
                        .clip(RoundedCornerShape(50))
                        .background(Brush.horizontalGradient(listOf(Color(0xFFE5433B), Color(0xFFFF7A45))))
                        .border(1.5.dp, Color(0xFF3A0A06), RoundedCornerShape(50))
                        .padding(horizontal = 7.dp, vertical = 2.dp)
                )
                if (chest == ShopChest.Epic) {
                    val left = ShopChest.opensToLegendary(pity)
                    Text(
                        text = if (left == 1) "✨ NEXT = LEGENDARY" else "✨ Legendary in ≤$left",
                        color = Color(0xFFFFE27A),
                        fontFamily = FontFirst(),
                        fontWeight = FontWeight.Black,
                        fontSize = 10.sp,
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 4.dp)
                            .clip(RoundedCornerShape(50))
                            .background(Color(0xDD1C0F2E))
                            .border(1.5.dp, Color(0xFFFFB02E), RoundedCornerShape(50))
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }
            }
            Text(
                text = if (showOdds) "✕" else "i",
                color = Color.White,
                fontWeight = FontWeight.Black,
                fontSize = 13.sp,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(6.dp)
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF14202E))
                    .wrapContentSize(Alignment.Center)
            )
        }
        // Pewna czesc zawartosci - gracz widzi, ze nawet "pech" to konkretna paczka.
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 6.dp, vertical = 6.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0x33000000))
                .padding(vertical = 5.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            Text("ALWAYS INSIDE", color = Color(0xFFBFE3FF), fontFamily = FontFirst(), fontWeight = FontWeight.Black, fontSize = 9.sp)
            guaranteed.entries().chunked(2).forEach { row ->
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    row.forEach { (icon, amount) ->
                        Text(
                            text = "$icon ${amount.grouped()}",
                            color = Color.White,
                            fontFamily = FontFirst(),
                            fontWeight = FontWeight.Black,
                            fontSize = 12.sp
                        )
                    }
                }
            }
            Text(
                text = chest.pitch(),
                color = Color(0xFFFFE27A),
                fontFamily = FontFirst(),
                fontWeight = FontWeight.Bold,
                fontSize = 10.sp,
                textAlign = TextAlign.Center,
                lineHeight = 12.sp,
                modifier = Modifier.padding(horizontal = 4.dp)
            )
        }
        // Tylko diamenty - za reklame otwiera sie [ShopChest.Daily], nie te.
        ChunkyButton(
            top = "OPEN",
            bottom = "💎 ${chest.gemCost}",
            enabled = canAffordGems,
            colors = listOf(Color(0xFFFFD86B), Color(0xFFE79B1E)),
            onClick = onGems,
            modifier = Modifier.fillMaxWidth().padding(start = 6.dp, end = 6.dp, bottom = 8.dp)
        )
    }
}

@Composable
private fun OddsTable(chest: ShopChest) {
    Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text("PRIZE ODDS", color = Color(0xFF1A1208), fontFamily = FontFirst(), fontWeight = FontWeight.Black, fontSize = 10.sp)
        chest.slots.forEach { slot ->
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("${slot.emoji} ${slot.shortLabel}", color = Color(0xFF1A1208), fontFamily = FontFirst(), fontSize = 10.sp)
                Text(
                    text = formatChance(chest.chanceOf(slot)),
                    color = Color(0xFF1A1208),
                    fontFamily = FontFirst(),
                    fontWeight = FontWeight.Bold,
                    fontSize = 10.sp
                )
            }
        }
        if (chest == ShopChest.Epic) {
            Text(
                "Legendary guaranteed at least every ${ShopChest.EPIC_PITY} Epic Chests.",
                color = Color(0xFF6A2DA6),
                fontFamily = FontFirst(),
                fontWeight = FontWeight.Bold,
                fontSize = 9.sp,
                lineHeight = 11.sp
            )
        }
    }
}

private fun formatChance(chance: Float): String {
    val tenths = (chance * 1000).roundToInt()
    return "${tenths / 10}.${tenths % 10}%"
}

@Composable
private fun ChunkyButton(
    top: String,
    bottom: String,
    enabled: Boolean,
    colors: List<Color>,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .alpha(if (enabled) 1f else 0.5f)
            .clip(RoundedCornerShape(10.dp))
            .background(Brush.verticalGradient(if (enabled) colors else listOf(Color(0xFFB8B8B8), Color(0xFF7C7C7C))))
            .border(2.dp, Color(0xFF14202E), RoundedCornerShape(10.dp))
            .clickable(enabled = enabled, onClick = onClick)
            .padding(vertical = 5.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        OutlinedLabel(top, 12.sp, Color.White)
        OutlinedLabel(bottom, 14.sp, Color.White)
    }
}

/** Potwierdzenie przed reklama - jak w referencji: co dostaniesz, Anuluj / Odbierz. */
@Composable
private fun AdConfirmDialog(chest: ShopChest, onCancel: () -> Unit, onWatch: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.7f))
            .clickable(onClick = onCancel),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .padding(28.dp)
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(Color(0xFFD9B98E))
                .border(4.dp, Color(0xFF6B4A2A), RoundedCornerShape(16.dp))
                .clickable(enabled = false) {},
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            OutlinedLabel("Watch an ad", 24.sp, Color.White, modifier = Modifier.padding(vertical = 12.dp))
            Column(
                modifier = Modifier.fillMaxWidth().background(Color(0xFFFFF6E0)).padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text("Watch an ad to open the ${chest.title}", color = Color(0xFF6B4A2A), fontFamily = FontFirst(), fontWeight = FontWeight.Bold, fontSize = 14.sp, textAlign = TextAlign.Center)
                Image(painter = painterResource(chest.image()), contentDescription = null, modifier = Modifier.width(140.dp))
            }
            Row(modifier = Modifier.fillMaxWidth().padding(14.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                ChunkyButton("Cancel", "", true, listOf(Color(0xFFC49A6C), Color(0xFF8C6238)), onCancel, Modifier.weight(1f))
                ChunkyButton("▶ AD", "Claim", true, listOf(Color(0xFFFFD86B), Color(0xFFE79B1E)), onWatch, Modifier.weight(1f))
            }
        }
    }
}


@Composable
private fun Header(money: Int, gems: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(PanelInk)
            .border(2.dp, Frame, RoundedCornerShape(12.dp))
            .padding(horizontal = 14.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedLabel("SHOP", 18.sp, GoldText, modifier = Modifier.weight(1f).wrapContentSize(Alignment.CenterStart))
        CurrencyPill("💎", gems.toString(), GemBlue)
        CurrencyPill("💰", money.toString(), Cream)
    }
}

@Composable
private fun CurrencyPill(icon: String, value: String, color: Color) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(Color(0xFF0E0A06))
            .padding(horizontal = 10.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = icon, fontSize = 14.sp)
        Text(text = value, color = color, fontFamily = FontFirst(), fontWeight = FontWeight.Bold, fontSize = 14.sp)
    }
}



/**
 * Legend Pack - jednorazowa oferta na konto, w stylu banera z gier mobilnych: szkarlatno-zloty
 * baner, skrzynia po lewej, jawna zawartosc po prawej, gwiazda "wartosci" w rogu i zielony
 * przycisk z cena. Skrzynia to na razie grafika epickiej - docelowy PNG podmieni jedna linie.
 */
@Composable
private fun LegendPackCard(price: String, busy: Boolean, onBuy: () -> Unit) {
    val infinite = rememberInfiniteTransition()
    val spin by infinite.animateFloat(0f, 360f, infiniteRepeatable(tween(14_000, easing = LinearEasing)))
    val bob by infinite.animateFloat(0f, 1f, infiniteRepeatable(tween(1300, easing = FastOutSlowInEasing), RepeatMode.Reverse))
    val sweep by infinite.animateFloat(0f, 1f, infiniteRepeatable(tween(2400, easing = LinearEasing)))
    val items = LegendPack.ITEM_IDS.mapNotNull { ItemCatalog.byId(it) }

    Box(modifier = Modifier.fillMaxWidth().padding(top = 6.dp)) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(18.dp))
                .background(Brush.linearGradient(listOf(Color(0xFFB0182E), Color(0xFF5E0A2A), Color(0xFF2A0822))))
                .border(3.dp, Brush.verticalGradient(listOf(Color(0xFFFFE27A), Color(0xFFE08A1E))), RoundedCornerShape(18.dp))
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(start = 14.dp, top = 10.dp, end = 70.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedLabel("Legend Pack", 24.sp, Color(0xFFFFE27A))
                Text(
                    text = "ONE TIME",
                    color = Color.White,
                    fontFamily = FontFirst(),
                    fontWeight = FontWeight.Black,
                    fontSize = 9.sp,
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(Color(0xFF14202E))
                        .padding(horizontal = 7.dp, vertical = 2.dp)
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth().height(150.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier.weight(0.9f).fillMaxSize().clipToBounds(),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.fillMaxSize().rotate(spin)) {
                        val r = size.maxDimension
                        for (i in 0 until 10) {
                            rotate(i * 36f) {
                                val path = Path().apply {
                                    moveTo(center.x, center.y)
                                    lineTo(center.x - r * 0.1f, center.y - r)
                                    lineTo(center.x + r * 0.1f, center.y - r)
                                    close()
                                }
                                drawPath(path, Brush.radialGradient(listOf(Color(0x88FFD34D), Color.Transparent), center, r * 0.55f))
                            }
                        }
                    }
                    Image(
                        painter = painterResource(Resources.Image.shopChestEpic),
                        contentDescription = "Legend Pack",
                        modifier = Modifier
                            .fillMaxWidth(0.85f)
                            .graphicsLayer {
                                translationY = -bob * 5f * density
                                scaleX = 1.1f
                                scaleY = 1.1f
                            }
                    )
                }
                Column(
                    modifier = Modifier.weight(1.1f).padding(end = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items.take(3).forEach { RewardTile(it.icon(), null, Color(0xFFFFB02E), Modifier.weight(1f)) }
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items.drop(3).forEach { RewardTile(it.icon(), null, Color(0xFFFFB02E), Modifier.weight(1f)) }
                        RewardTile("💎", "${LegendPack.GEMS}", GemBlue, Modifier.weight(1f))
                        RewardTile("🎟", "x${LegendPack.TICKETS}", Color(0xFF8BE05A), Modifier.weight(1f))
                    }
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth().padding(start = 14.dp, end = 14.dp, bottom = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "4 Legendary items, ${LegendPack.GEMS} gems and ${LegendPack.TICKETS} summon tickets.",
                    color = Color(0xFFFFE9C9),
                    fontFamily = FontFirst(),
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    modifier = Modifier.weight(1f)
                )
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(Brush.verticalGradient(listOf(Color(0xFF8BE05A), Color(0xFF3F9E2B))))
                        .border(2.5.dp, Color(0xFF14202E), RoundedCornerShape(12.dp))
                        .clickable(enabled = !busy, onClick = onBuy)
                        .clipToBounds()
                        .padding(horizontal = 18.dp, vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(width = 14.dp, height = 60.dp)
                            .graphicsLayer {
                                translationX = (sweep * 2f - 1f) * 90f * density
                                rotationZ = 20f
                            }
                            .background(Brush.horizontalGradient(listOf(Color.Transparent, Color.White.copy(alpha = 0.6f), Color.Transparent)))
                    )
                    OutlinedLabel(if (busy) "..." else price, 18.sp, Color.White)
                }
            }
        }
        ValueStar(modifier = Modifier.align(Alignment.TopEnd).offset(x = 6.dp, y = (-8).dp))
    }
}

@Composable
private fun RewardTile(icon: String, label: String?, rim: Color, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(10.dp))
            .background(Brush.verticalGradient(listOf(Color(0xFF3B1450), Color(0xFF1E0A2A))))
            .border(2.dp, rim, RoundedCornerShape(10.dp)),
        contentAlignment = Alignment.Center
    ) {
        Text(text = icon, fontSize = 22.sp)
        if (label != null) {
            OutlinedLabel(label, 10.sp, Color.White, modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 1.dp))
        }
    }
}

/** Czerwona gwiazda "x5 VALUE" przechylona w rogu banera. */
@Composable
private fun ValueStar(modifier: Modifier = Modifier) {
    val pulse by rememberInfiniteTransition().animateFloat(0.94f, 1.06f, infiniteRepeatable(tween(700), RepeatMode.Reverse))
    Box(
        modifier = modifier.size(70.dp).graphicsLayer { rotationZ = 14f; scaleX = pulse; scaleY = pulse },
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val points = 14
            val outer = size.minDimension / 2f
            val inner = outer * 0.8f
            val path = Path()
            for (i in 0 until points * 2) {
                val r = if (i % 2 == 0) outer else inner
                val a = (i * kotlin.math.PI / points).toFloat()
                val x = center.x + kotlin.math.cos(a) * r
                val y = center.y + kotlin.math.sin(a) * r
                if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
            }
            path.close()
            drawPath(path, Color(0xFFD9322B))
            drawPath(path, Color(0xFF3A0A06), style = Stroke(width = 5f))
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            OutlinedLabel("x5", 18.sp, Color(0xFFFFE27A))
            OutlinedLabel("VALUE", 9.sp, Color.White)
        }
    }
}

/** Po zakupie: pelnoekranowe "odblokowane" z zawartoscia pakietu. */
@Composable
private fun LegendPackCelebration(onDone: () -> Unit) {
    val spin by rememberInfiniteTransition().animateFloat(0f, 360f, infiniteRepeatable(tween(12_000, easing = LinearEasing)))
    val items = LegendPack.ITEM_IDS.mapNotNull { ItemCatalog.byId(it) }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xF00B0714))
            .clickable(onClick = onDone),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(420.dp).rotate(spin)) {
            val r = size.maxDimension / 2f
            for (i in 0 until 12) {
                rotate(i * 30f) {
                    val path = Path().apply {
                        moveTo(center.x, center.y)
                        lineTo(center.x - r * 0.12f, center.y - r)
                        lineTo(center.x + r * 0.12f, center.y - r)
                        close()
                    }
                    drawPath(path, Brush.radialGradient(listOf(Color(0x99FFD34D), Color.Transparent), center, r))
                }
            }
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedLabel("LEGEND PACK", 30.sp, Color(0xFFFFE27A))
            OutlinedLabel("UNLOCKED!", 22.sp, Color.White)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(horizontal = 24.dp)) {
                items.forEach { RewardTile(it.icon(), null, Color(0xFFFFB02E), Modifier.weight(1f)) }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(horizontal = 80.dp)) {
                RewardTile("💎", "${LegendPack.GEMS}", GemBlue, Modifier.weight(1f))
                RewardTile("🎟", "x${LegendPack.TICKETS}", Color(0xFF8BE05A), Modifier.weight(1f))
            }
            Text("Tap to continue", color = Muted, fontFamily = FontFirst(), fontSize = 12.sp)
        }
    }
}

/* ------------------------------------------ Diamenty ------------------------------------------ */

/** Co wlasnie wpadlo na konto - pokazywane pelnoekranowym efektem po zakupie. */
private data class RewardBurst(val icon: String, val amount: Int)

private fun Int.grouped(): String = toString().reversed().chunked(3).joinToString(",").reversed()

/**
 * Karta pakietu diamentow: stos klejnotow rosnie z wielkoscia pakietu, wieksze pakiety maja
 * promienie za stosem, a bonus i plakietka (POPULAR / BEST VALUE) mowia, co sie oplaca.
 */
@Composable
private fun GemPackCard(pack: GemPack, firstBuy: Boolean, price: String, busy: Boolean, onBuy: () -> Unit, modifier: Modifier = Modifier) {
    val tier = pack.ordinal
    val infinite = rememberInfiniteTransition()
    val spin by infinite.animateFloat(0f, 360f, infiniteRepeatable(tween(12_000, easing = LinearEasing)))
    val bob by infinite.animateFloat(0f, 1f, infiniteRepeatable(tween(1400 + tier * 120, easing = FastOutSlowInEasing), RepeatMode.Reverse))
    val sweep by infinite.animateFloat(0f, 1f, infiniteRepeatable(tween(2600 + tier * 200, easing = LinearEasing)))
    val highlighted = pack.tag != null

    Box(modifier = modifier.padding(top = 10.dp)) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(
                    Brush.verticalGradient(
                        if (highlighted) listOf(Color(0xFF7B3FE0), Color(0xFF3A1A86), Color(0xFF1C0F48))
                        else listOf(Color(0xFF3778E0), Color(0xFF1E3F95), Color(0xFF112457))
                    )
                )
                .border(3.dp, if (highlighted) Color(0xFFFFD34D) else Color(0xFF0B1330), RoundedCornerShape(16.dp)),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier.fillMaxWidth().height(104.dp).clipToBounds(),
                contentAlignment = Alignment.Center
            ) {
                if (tier >= 2) {
                    Canvas(modifier = Modifier.fillMaxSize().rotate(spin)) {
                        val r = size.maxDimension
                        for (i in 0 until 10) {
                            rotate(i * 36f) {
                                val path = Path().apply {
                                    moveTo(center.x, center.y)
                                    lineTo(center.x - r * 0.1f, center.y - r)
                                    lineTo(center.x + r * 0.1f, center.y - r)
                                    close()
                                }
                                drawPath(path, Brush.radialGradient(listOf(Color(0x66BFE3FF), Color.Transparent), center, r * 0.55f))
                            }
                        }
                    }
                }
                Box(
                    modifier = Modifier
                        .size(90.dp)
                        .background(Brush.radialGradient(listOf(Color(0x66FFFFFF), Color.Transparent)), CircleShape)
                )
                GemPile(tier = tier, modifier = Modifier.graphicsLayer { translationY = -bob * 4f * density })
            }
            Text(
                text = pack.title.uppercase(),
                color = Color(0xFFBFD3F0),
                fontFamily = FontFirst(),
                fontWeight = FontWeight.Black,
                fontSize = 10.sp
            )
            OutlinedLabel((if (firstBuy) pack.firstPurchaseTotal else pack.total).grouped(), 24.sp, if (firstBuy) Color(0xFFFFE27A) else Color.White)
            Box(modifier = Modifier.height(26.dp), contentAlignment = Alignment.Center) {
            if (firstBuy) {
                // Pierwszy zakup x2 - najmocniejszy argument w calym sklepie, wiec wypiera zwykly bonus.
                Text(
                    text = "x2 FIRST BUY",
                    color = Color.White,
                    fontFamily = FontFirst(),
                    fontWeight = FontWeight.Black,
                    fontSize = 10.sp,
                    modifier = Modifier
                        .padding(top = 2.dp)
                        .clip(RoundedCornerShape(50))
                        .background(Brush.horizontalGradient(listOf(Color(0xFFE5433B), Color(0xFFFF7A45))))
                        .border(1.5.dp, Color(0xFF3A0A06), RoundedCornerShape(50))
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                )
            } else if (pack.bonus > 0) {
                Text(
                    text = "+${pack.bonusPercent}% BONUS",
                    color = Color.White,
                    fontFamily = FontFirst(),
                    fontWeight = FontWeight.Black,
                    fontSize = 10.sp,
                    modifier = Modifier
                        .padding(top = 2.dp)
                        .clip(RoundedCornerShape(50))
                        .background(Brush.horizontalGradient(listOf(Color(0xFF3FAE2A), Color(0xFF6FD04A))))
                        .border(1.5.dp, Color(0xFF14202E), RoundedCornerShape(50))
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                )
            }
            }
            Box(
                modifier = Modifier
                    .padding(10.dp)
                    .fillMaxWidth()
                    .height(42.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Brush.verticalGradient(listOf(Color(0xFF8BE05A), Color(0xFF3F9E2B))))
                    .border(2.5.dp, Color(0xFF14202E), RoundedCornerShape(12.dp))
                    .clickable(enabled = !busy, onClick = onBuy)
                    .clipToBounds(),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(width = 14.dp, height = 70.dp)
                        .graphicsLayer {
                            translationX = (sweep * 2f - 1f) * 110f * density
                            rotationZ = 20f
                        }
                        .background(Brush.horizontalGradient(listOf(Color.Transparent, Color.White.copy(alpha = 0.55f), Color.Transparent)))
                )
                OutlinedLabel(if (busy) "..." else price, 16.sp, Color.White)
            }
        }
        pack.tag?.let { tag ->
            Text(
                text = tag,
                color = Color.White,
                fontFamily = FontFirst(),
                fontWeight = FontWeight.Black,
                fontSize = 10.sp,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .offset(y = (-10).dp)
                    .clip(RoundedCornerShape(50))
                    .background(
                        Brush.horizontalGradient(
                            if (tag == "BEST VALUE") listOf(Color(0xFFE5433B), Color(0xFFFF7A45)) else listOf(Color(0xFFFF9A1F), Color(0xFFFFC94A))
                        )
                    )
                    .border(2.dp, Color(0xFF3A0A06), RoundedCornerShape(50))
                    .padding(horizontal = 10.dp, vertical = 3.dp)
            )
        }
    }
}

/**
 * Stos klejnotow, ktory rosnie z pakietem: od jednego diamentu do piramidy. Kazdy klejnot to
 * emoji w lekko innym rozmiarze i przechyle, zeby stos nie wygladal jak siatka.
 */
@Composable
private fun GemPile(tier: Int, modifier: Modifier = Modifier) {
    // (x, y, rozmiar, obrot) - kolejne warstwy dokladane z kazdym wiekszym pakietem.
    val layout = listOf(
        listOf(Triple(0f, 0f, 44f)),
        listOf(Triple(-14f, 6f, 34f), Triple(14f, 6f, 34f), Triple(0f, -8f, 38f)),
        listOf(Triple(-22f, 10f, 30f), Triple(0f, 12f, 32f), Triple(22f, 10f, 30f), Triple(-10f, -6f, 34f), Triple(12f, -6f, 34f)),
        listOf(Triple(-26f, 14f, 28f), Triple(-6f, 16f, 30f), Triple(16f, 14f, 30f), Triple(32f, 12f, 26f), Triple(-14f, -2f, 32f), Triple(8f, -4f, 34f), Triple(0f, -20f, 30f)),
        listOf(Triple(-32f, 16f, 26f), Triple(-12f, 18f, 30f), Triple(10f, 18f, 30f), Triple(30f, 16f, 26f), Triple(-22f, 0f, 30f), Triple(0f, 0f, 34f), Triple(22f, 0f, 30f), Triple(-10f, -16f, 30f), Triple(12f, -16f, 30f)),
        listOf(Triple(-36f, 18f, 26f), Triple(-16f, 20f, 28f), Triple(4f, 20f, 30f), Triple(24f, 18f, 28f), Triple(40f, 16f, 22f), Triple(-26f, 2f, 28f), Triple(-6f, 2f, 32f), Triple(16f, 2f, 30f), Triple(34f, 0f, 24f), Triple(-14f, -14f, 30f), Triple(8f, -16f, 32f), Triple(-2f, -30f, 30f))
    )[tier.coerceIn(0, 5)]
    Box(modifier = modifier.size(100.dp), contentAlignment = Alignment.Center) {
        layout.forEachIndexed { i, (x, y, size) ->
            Text(
                text = "💎",
                fontSize = size.sp,
                modifier = Modifier.offset(x = x.dp, y = y.dp).rotate(if (i % 2 == 0) -8f else 10f)
            )
        }
    }
}

/** Losy za diamenty: poziomy pasek - stos losow, nazwa i ilosc, zloty przycisk z cena w diamentach. */
@Composable
private fun TicketPackCard(pack: TicketPack, affordable: Boolean, onBuy: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Brush.horizontalGradient(listOf(Color(0xFF7A2E2E), Color(0xFF3E1520))))
            .border(3.dp, Color(0xFF14202E), RoundedCornerShape(16.dp))
            .padding(10.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(60.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Brush.radialGradient(listOf(Color(0x66FFD34D), Color(0x22000000))))
                .border(2.dp, Color(0xFFFFB02E), RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center
        ) {
            repeat((pack.ordinal + 1).coerceAtMost(3)) { i ->
                Text(text = "🎟", fontSize = 26.sp, modifier = Modifier.offset(x = (i * 6 - 6).dp, y = (-i * 4 + 4).dp).rotate(-10f + i * 10f))
            }
        }
        Column(modifier = Modifier.weight(1f)) {
            OutlinedLabel(pack.title, 16.sp, Color(0xFFFFE27A))
            Text("${pack.tickets} summon tickets", color = Color.White, fontFamily = FontFirst(), fontWeight = FontWeight.Bold, fontSize = 12.sp)
            pack.note?.let { Text(it, color = Color(0xFF8BE05A), fontFamily = FontFirst(), fontWeight = FontWeight.Black, fontSize = 11.sp) }
        }
        ChunkyButton(
            top = "💎",
            bottom = "${pack.gems}",
            enabled = affordable,
            colors = listOf(Color(0xFFFFD86B), Color(0xFFE79B1E)),
            onClick = onBuy,
            modifier = Modifier.size(width = 76.dp, height = 56.dp)
        )
    }
}

/** Po zakupie: klejnoty (albo losy) wystrzeliwuja fontanna, a kwota rosnie na srodku ekranu. */
@Composable
private fun RewardBurstOverlay(burst: RewardBurst, onDone: () -> Unit) {
    val infinite = rememberInfiniteTransition()
    val spin by infinite.animateFloat(0f, 360f, infiniteRepeatable(tween(10_000, easing = LinearEasing)))
    val pop = remember { Animatable(0f) }
    val count = remember { Animatable(0f) }
    LaunchedEffect(burst) {
        launch { pop.animateTo(1f, spring(dampingRatio = 0.45f, stiffness = 220f)) }
        count.animateTo(1f, tween(1100, easing = FastOutSlowInEasing))
    }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xE60B0714))
            .clickable(onClick = onDone),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(460.dp).rotate(spin)) {
            val r = size.maxDimension / 2f
            for (i in 0 until 14) {
                rotate(i * 360f / 14) {
                    val path = Path().apply {
                        moveTo(center.x, center.y)
                        lineTo(center.x - r * 0.1f, center.y - r)
                        lineTo(center.x + r * 0.1f, center.y - r)
                        close()
                    }
                    drawPath(path, Brush.radialGradient(listOf(Color(0x886FB7FF), Color.Transparent), center, r))
                }
            }
        }
        // Fontanna: klejnoty rozlatuja sie po lukach z grawitacja.
        repeat(16) { i ->
            val angle = (i / 16f) * 2f * kotlin.math.PI.toFloat()
            Text(
                text = burst.icon,
                fontSize = 22.sp,
                modifier = Modifier.graphicsLayer {
                    val t = count.value
                    translationX = kotlin.math.cos(angle) * 170f * t * density
                    translationY = (kotlin.math.sin(angle) * 150f * t + 180f * t * t - 40f * t) * density
                    alpha = 1f - t * 0.9f
                    rotationZ = 360f * t
                }
            )
        }
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.graphicsLayer { scaleX = pop.value; scaleY = pop.value }
        ) {
            Text(text = burst.icon, fontSize = 88.sp)
            OutlinedLabel("+${(burst.amount * count.value).toInt().grouped()}", 40.sp, Color(0xFFFFE27A))
            Text("Tap to continue", color = Muted, fontFamily = FontFirst(), fontSize = 12.sp, modifier = Modifier.padding(top = 10.dp))
        }
    }
}

/** Glowna nagroda z otwarcia do pokazania w animacji - wszystko jest juz zapisane, wiec [ChestPrize.grant] nic nie robi. */
private fun ChestOpening.displayPrize(): ChestPrize {
    itemId?.let { ItemCatalog.byId(it) }?.let { item ->
        return ChestPrize(
            icon = item.icon(),
            name = item.name,
            label = "${item.name} (${item.rarity.name})" + if (pityTriggered) " - guaranteed!" else "",
            grant = { _, _ -> }
        )
    }
    val tickets = total.tickets - guaranteed.tickets
    if (tickets > 0) {
        return ChestPrize(
            icon = slot.emoji,
            name = if (tickets == 1) "1 summon ticket" else "$tickets summon tickets",
            label = slot.label,
            grant = { _, _ -> }
        )
    }
    val gold = total.gold - guaranteed.gold
    return ChestPrize(icon = "💰", name = "${gold.grouped()} coins", label = "${gold.grouped()} coins", grant = { _, _ -> })
}

private fun Bundle.toBurst(): RewardBurst = entries().firstOrNull()?.let { (icon, amount) -> RewardBurst(icon, amount) } ?: RewardBurst("✨", 0)

/**
 * Codzienne diamenty Premium - na gorze zakladki ze skrzyniami. Subskrybent odbiera je tu co dzien,
 * reszta widzi, ile traci (i jednym tapnieciem przechodzi na zakladke Premium).
 */
@Composable
private fun PremiumDailyCard(isPremium: Boolean, claimedToday: Boolean, onClaim: () -> Unit, onSeePremium: () -> Unit) {
    val infinite = rememberInfiniteTransition()
    val pulse by infinite.animateFloat(0f, 1f, infiniteRepeatable(tween(900, easing = FastOutSlowInEasing), RepeatMode.Reverse))
    val ready = isPremium && !claimedToday
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Brush.horizontalGradient(listOf(Color(0xFF6A2DA6), Color(0xFF2A1260))))
            .border(3.dp, if (ready) Color(0xFFFFD34D) else Color(0xFF14202E), RoundedCornerShape(16.dp))
            .clickable(onClick = if (isPremium) onClaim else onSeePremium)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(modifier = Modifier.size(52.dp), contentAlignment = Alignment.Center) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .alpha(if (ready) 0.5f + 0.4f * pulse else 0.35f)
                    .background(Brush.radialGradient(listOf(Color(0xFFBFE3FF), Color.Transparent)), CircleShape)
            )
            Text(text = "💎", fontSize = 30.sp)
            Text(text = "👑", fontSize = 16.sp, modifier = Modifier.align(Alignment.TopEnd))
        }
        Column(modifier = Modifier.weight(1f)) {
            OutlinedLabel("${PremiumLimits.DAILY_GEMS} gems every day", 15.sp, Color.White)
            Text(
                text = when {
                    !isPremium -> "With Premium: ${PremiumLimits.DAILY_GEMS * 30} gems a month"
                    claimedToday -> "Claimed today - come back tomorrow"
                    else -> "Your Premium gems are waiting"
                },
                color = Color(0xFFE6C7FF),
                fontFamily = FontFirst(),
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp
            )
        }
        Box(
            modifier = Modifier
                .graphicsLayer {
                    val sc = if (ready) 1f + 0.05f * pulse else 1f
                    scaleX = sc
                    scaleY = sc
                }
                .clip(RoundedCornerShape(10.dp))
                .background(
                    Brush.verticalGradient(
                        when {
                            ready -> listOf(Color(0xFF8BE05A), Color(0xFF3F9E2B))
                            isPremium -> listOf(Color(0xFFB8B8B8), Color(0xFF7C7C7C))
                            else -> listOf(Color(0xFFFFD86B), Color(0xFFE79B1E))
                        }
                    )
                )
                .border(2.dp, Color(0xFF14202E), RoundedCornerShape(10.dp))
                .padding(horizontal = 12.dp, vertical = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            OutlinedLabel(
                when {
                    ready -> "CLAIM"
                    isPremium -> "✓"
                    else -> "👑 GET"
                },
                14.sp,
                Color.White
            )
        }
    }
}

/** Doladowanie energii w zakladce Supplies - te same zasady co okno z gornego paska. */
@Composable
private fun EnergyDealCard(refillsToday: Int, gems: Int, onBuy: () -> Unit) {
    val price = EnergyRefill.priceFor(refillsToday)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Brush.horizontalGradient(listOf(Color(0xFF2B6FD6), Color(0xFF16264E))))
            .border(3.dp, Color(0xFF14202E), RoundedCornerShape(16.dp))
            .padding(10.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(60.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Brush.radialGradient(listOf(Color(0x99FFE27A), Color(0x22000000))))
                .border(2.dp, Color(0xFFFFE27A), RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(text = "⚡", fontSize = 34.sp)
        }
        Column(modifier = Modifier.weight(1f)) {
            OutlinedLabel("Energy Refill +${EnergyRefill.AMOUNT}", 16.sp, Color(0xFFFFE27A))
            Text(
                "${EnergyRefill.AMOUNT / com.lernhero.domain.energy.EnergyRules.BATTLE_COST} more battles right now",
                color = Color.White,
                fontFamily = FontFirst(),
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp
            )
            Text(
                text = if (price == null) "Daily limit reached" else "${EnergyRefill.MAX_PER_DAY - refillsToday}/${EnergyRefill.MAX_PER_DAY} left today · price rises",
                color = Color(0xFF8FA3C4),
                fontFamily = FontFirst(),
                fontSize = 11.sp
            )
        }
        ChunkyButton(
            top = "💎",
            bottom = price?.toString() ?: "-",
            enabled = price != null && gems >= price,
            colors = listOf(Color(0xFFFFD86B), Color(0xFFE79B1E)),
            onClick = onBuy,
            modifier = Modifier.size(width = 76.dp, height = 56.dp)
        )
    }
}

/** Kafelek wymiany diamentow na zloto albo material - pewna, jawna ilosc na poziomie gracza. */
@Composable
private fun SupplyCard(deal: SupplyDeal, bundle: Bundle, affordable: Boolean, onBuy: () -> Unit, modifier: Modifier = Modifier) {
    val (icon, amount) = bundle.entries().firstOrNull() ?: (deal.icon to 0)
    val gold = deal.name.startsWith("Gold")
    Box(modifier = modifier.padding(top = 8.dp)) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(
                    Brush.verticalGradient(
                        if (gold) listOf(Color(0xFFB9791F), Color(0xFF6B3F0E)) else listOf(Color(0xFF4A5A6E), Color(0xFF232C38))
                    )
                )
                .border(3.dp, Color(0xFF14202E), RoundedCornerShape(14.dp))
                .padding(top = 10.dp, bottom = 6.dp, start = 6.dp, end = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .background(Brush.radialGradient(listOf(Color(0x66FFFFFF), Color.Transparent)), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(text = deal.icon, fontSize = 32.sp)
            }
            Text(
                text = deal.title.uppercase(),
                color = Color(0xFFF4E9D7),
                fontFamily = FontFirst(),
                fontWeight = FontWeight.Black,
                fontSize = 9.sp,
                maxLines = 1
            )
            OutlinedLabel("$icon ${amount.grouped()}", 15.sp, Color.White)
            ChunkyButton(
                top = "💎",
                bottom = "${deal.gems}",
                enabled = affordable,
                colors = listOf(Color(0xFF8BE05A), Color(0xFF3F9E2B)),
                onClick = onBuy,
                modifier = Modifier.fillMaxWidth().padding(top = 4.dp)
            )
        }
        if (deal.bonusPercent > 0) {
            Text(
                text = "+${deal.bonusPercent}%",
                color = Color.White,
                fontFamily = FontFirst(),
                fontWeight = FontWeight.Black,
                fontSize = 10.sp,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = 2.dp, y = (-6).dp)
                    .clip(RoundedCornerShape(50))
                    .background(Brush.horizontalGradient(listOf(Color(0xFF3FAE2A), Color(0xFF6FD04A))))
                    .border(1.5.dp, Color(0xFF14202E), RoundedCornerShape(50))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            )
        }
    }
}
