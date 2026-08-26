package com.lernhero.home.city

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.EaseOutBack
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lernhero.domain.common.Rarity
import com.lernhero.domain.monster.MonsterCard
import com.lernhero.domain.craft.CraftCost
import com.lernhero.domain.craft.SummonCosts
import com.lernhero.domain.monster.MonsterCatalog
import com.lernhero.domain.spell.Spell
import com.lernhero.home.player.PlayerViewModel
import com.lernhero.home.ui.RarityPalette
import com.lernhero.home.ui.icon
import com.lernhero.game.character.MonsterVisualPresets
import com.lernhero.shared.resources.Resources
import com.lernhero.shared.theme.FontFirst
import com.lernhero.shared.theme.FontPixel
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import org.jetbrains.compose.resources.painterResource
import org.koin.compose.viewmodel.koinViewModel
import kotlin.math.sin
import kotlin.math.cos
import kotlin.math.PI
import kotlin.random.Random

/**
 * Rodzaj bannera - jak w idlach: tanszy losuje glownie pospolite, drozszy realnie podbija szanse
 * na rzadkie. Wagi sa per [Rarity], wiec dochodzenie nowych potworow do katalogu niczego tu nie psuje.
 */
private enum class SummonBanner(
    val label: String,
    val icon: String,
    val ticketCost: Int,
    val accent: Color,
    val weights: Map<Rarity, Int>
) {
    Basic(
        label = "Zwykly Summon",
        icon = "🗿",
        ticketCost = 1,
        accent = Color(0xFF9DB2C4),
        weights = mapOf(
            Rarity.Common to 70,
            Rarity.Uncommon to 25,
            Rarity.Rare to 5
        )
    ),
    Heroic(
        label = "Heroiczny Summon",
        icon = "👑",
        ticketCost = 3,
        accent = Color(0xFFFFB020),
        weights = mapOf(
            Rarity.Common to 25,
            Rarity.Uncommon to 45,
            Rarity.Rare to 30
        )
    )
}

/** Etapy rytualu - patrz [SummonCenterScreen]. */
private enum class SummonPhase { Idle, Charging, Flash, Reveal, Result }

/** Dwie polowy budynku: oltarz (zdobywanie kart) i kolekcja (zamiana kart na sile). */
private enum class SummonSection(val label: String) {
    Ritual("Przyzywanie"),
    Collection("Kolekcja")
}

/**
 * Summon Center w stylu mobilnych idle RPG: po lewej wybor bannera, na srodku oltarz, a po
 * nacisnieciu summona odpala sie rytual - oltarz sie laduje, strzela blysk i dopiero z niego
 * WYCHODZI potwor (skala + wyjazd z dolu + aura w kolorze rzadkosci), jak wyjscie zawodnika.
 *
 * Losowanie idzie po prawdziwym [MonsterCatalog], wiec wypadaja te same potwory, ktorymi sie gra -
 * i od teraz KAZDY wylosowany kompan faktycznie laduje w kolekcji (wczesniej caly rytual konczyl sie
 * niczym: `pulled` zylo tylko w pamieci ekranu).
 *
 * Waluta: losy ze skrzyn po bossach placa za kompanow, a esencja z lochow za zaklecia. Obie
 * pochodza wylacznie z wypraw - to one zastapily dawne kupowanie jednego i drugiego za zloto.
 */
@Composable
fun SummonCenterScreen(
    playerViewModel: PlayerViewModel,
    onBack: () -> Unit
) {
    val state by playerViewModel.uiState.collectAsState()

    var section by remember { mutableStateOf(SummonSection.Ritual) }
    var banner by remember { mutableStateOf(SummonBanner.Basic) }
    var phase by remember { mutableStateOf(SummonPhase.Idle) }
    var pulled by remember { mutableStateOf<List<MonsterCard>>(emptyList()) }

    // Jedna os czasu calego rytualu - kazdy etap ma wlasna animacje, ale sterowane sa stad.
    val charge = remember { Animatable(0f) }
    val flash = remember { Animatable(0f) }
    val rise = remember { Animatable(0f) }
    // Fala uderzeniowa - biegnie ROWNOLEGLE do blysku i wyjscia potwora, zeby pierscienie
    // rozchodzily sie jeszcze w trakcie, gdy summon juz wstaje, a nie dopiero po wszystkim.
    val burst = remember { Animatable(0f) }

    LaunchedEffect(phase) {
        when (phase) {
            SummonPhase.Charging -> {
                rise.snapTo(0f)
                flash.snapTo(0f)
                burst.snapTo(0f)
                charge.snapTo(0f)
                charge.animateTo(1f, tween(CHARGE_MS, easing = FastOutSlowInEasing))
                phase = SummonPhase.Flash
            }

            SummonPhase.Flash -> {
                flash.snapTo(1f)
                burst.snapTo(0f)
                launch { burst.animateTo(1f, tween(BURST_MS, easing = FastOutSlowInEasing)) }
                flash.animateTo(0f, tween(FLASH_MS, easing = LinearEasing))
                phase = SummonPhase.Reveal
            }

            SummonPhase.Reveal -> {
                rise.animateTo(1f, tween(RISE_MS, easing = EaseOutBack))
                delay(REVEAL_HOLD_MS)
                phase = SummonPhase.Result
            }

            SummonPhase.Idle, SummonPhase.Result -> Unit
        }
    }

    val busy = phase != SummonPhase.Idle && phase != SummonPhase.Result

    TownScreenScaffold(
        title = "Summon Center",
        resources = state.resources,
        money = state.money,
        toasts = emptyList(),
        onBack = onBack
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            SectionTabs(selected = section, onSelect = { section = it }, enabled = !busy)

            if (section == SummonSection.Collection) {
                MonsterCollectionTab(
                    collection = state.collection,
                    money = state.money,
                    essence = state.resources.essence,
                    onUpgrade = playerViewModel::upgradeMonster,
                    onAscend = playerViewModel::ascendMonster,
                    onDissolve = playerViewModel::dissolveMonster,
                    onUpgradeSpell = playerViewModel::upgradeMonsterSpell,
                    modifier = Modifier.weight(1f)
                )
                state.message?.let { message ->
                    Text(text = message, color = Gold, fontFamily = FontFirst(), fontSize = 12.sp)
                }
                return@Column
            }

            BannerRow(
                selected = banner,
                tickets = state.resources.summonTickets,
                enabled = !busy,
                onSelect = { banner = it }
            )

            AltarStage(
                banner = banner,
                phase = phase,
                pulled = pulled,
                chargeValue = charge.value,
                flashValue = flash.value,
                riseValue = rise.value,
                burstValue = burst.value,
                modifier = Modifier.weight(1f)
            )

            SummonButtons(
                banner = banner,
                tickets = state.resources.summonTickets,
                enabled = !busy,
                onSummon = { count ->
                    // Placimy losami bannera za caly ciag - jeden zapis na cale losowanie.
                    val rolled = List(count) { banner.roll() }
                    pulled = rolled
                    playerViewModel.summonMonsters(rolled, CraftCost(tickets = banner.ticketCost * count))
                    phase = SummonPhase.Charging
                }
            )

            state.message?.let { message ->
                Text(
                    text = message,
                    color = Gold,
                    fontFamily = FontFirst(),
                    fontSize = 12.sp
                )
            }
        }
    }
}

/**
 * Przelacznik polowek budynku. Kolekcja siedzi TUTAJ, a nie w osobnej zakladce na dole ekranu, bo
 * to ta sama czynnosc co przyzywanie - zdobywasz kompana i od razu chcesz zobaczyc, co z nim zrobic.
 */
@Composable
private fun SectionTabs(
    selected: SummonSection,
    onSelect: (SummonSection) -> Unit,
    enabled: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        SummonSection.entries.forEach { entry ->
            val isSelected = entry == selected
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (isSelected) Ink else Ink.copy(alpha = 0.5f))
                    .border(
                        width = if (isSelected) 2.dp else 1.dp,
                        color = if (isSelected) Gold else GoldDim.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(10.dp)
                    )
                    .clickable(enabled = enabled) { onSelect(entry) }
                    .padding(vertical = 9.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = entry.label,
                    color = if (isSelected) Gold else Muted,
                    fontFamily = FontFirst(),
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
            }
        }
    }
}

/** Wybor bannera - poziomy odpowiednik listy z lewej strony w mobilnych gaczach. */
@Composable
private fun BannerRow(
    selected: SummonBanner,
    tickets: Int,
    enabled: Boolean,
    onSelect: (SummonBanner) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        SummonBanner.entries.forEach { entry ->
            val isSelected = entry == selected
            Column(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        if (isSelected) {
                            Brush.verticalGradient(
                                listOf(entry.accent.copy(alpha = 0.30f), entry.accent.copy(alpha = 0.10f))
                            )
                        } else {
                            Brush.verticalGradient(listOf(Ink.copy(alpha = 0.7f), Ink.copy(alpha = 0.7f)))
                        }
                    )
                    .border(
                        width = if (isSelected) 2.dp else 1.dp,
                        color = if (isSelected) entry.accent else GoldDim.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(12.dp)
                    )
                    .clickable(enabled = enabled) { onSelect(entry) }
                    .padding(vertical = 8.dp, horizontal = 6.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                Text(text = entry.icon, fontSize = 20.sp)
                Text(
                    text = entry.label,
                    color = if (isSelected) entry.accent else TextBody,
                    fontFamily = FontPixel(),
                    fontWeight = FontWeight.Bold,
                    fontSize = 10.sp,
                    textAlign = TextAlign.Center,
                    maxLines = 1
                )
                Text(
                    text = "🎟️ ${entry.ticketCost}",
                    color = if (tickets >= entry.ticketCost) TextBody else Muted,
                    fontFamily = FontFirst(),
                    fontSize = 10.sp
                )
            }
        }
    }
}

/**
 * Oltarz + cala scena rytualu. Warstwy od dolu: aura/krag -> oltarz -> potwor -> blysk.
 * Potwor jest rysowany NAD oltarzem i wyjezdza od jego podstawy, wiec czyta sie to jak wylanianie
 * sie z kregu, a nie doklejenie obrazka.
 */
@Composable
private fun AltarStage(
    banner: SummonBanner,
    phase: SummonPhase,
    pulled: List<MonsterCard>,
    chargeValue: Float,
    flashValue: Float,
    riseValue: Float,
    burstValue: Float,
    modifier: Modifier = Modifier
) {
    val hero = pulled.firstOrNull()
    val accent = hero?.rarity?.color() ?: banner.accent
    // Im rzadszy summon, tym wiekszy i mocniejszy caly pokaz - to po tym gracz ma poznac, ze
    // wypadlo mu cos dobrego, jeszcze zanim przeczyta tabliczke.
    val power = hero?.rarity?.effectPower() ?: 1f

    TownPanel(modifier = modifier.fillMaxWidth()) {
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            // ContentScale.Fit na fillMaxSize() dobiera rozmiar SAM, z obu wymiarow naraz - bez
            // recznie dobieranego mnoznika, ktory trzeba przeliczac za kazdym razem, gdy podmienia
            // sie PNG (poprzedni "1.75x" byl skalibrowany pod stary plik, gdzie oltarz zajmowal
            // tylko 52% wlasnego kadru - przy nowym, ktory wypelnia 94%, dawal zle wyniki). Oltarz
            // jest kwadratowy, wiec renderowany bok = krotszy wymiar dostepnej przestrzeni.
            val altarSide = minOf(maxWidth, maxHeight)
            val altarTop = (maxHeight - altarSide) / 2
            // Wysokosc plyty kregu w kadrze (ulamek CALEGO kwadratowego PNG, zmierzone z pliku) -
            // tu maja ladowac stopy przyzwanego potwora.
            val platformY = altarTop + altarSide * ALTAR_PLATFORM_Y_FRACTION
            // Ta sama wysokosc, ale jako ulamek CALEJ sceny - Canvas efektow liczy w pikselach.
            val platformFraction = platformY / maxHeight

            // Poswiata POD oltarzem - tylko delikatne podswietlenie tla sceny.
            RitualGlow(
                accent = accent,
                // Poswiata ZOSTAJE po rytuale - to ona daje wrazenie oswietlonej sceny. Nie mnozymy
                // jej juz przez `power`: to wlasnie ten mnoznik wypychal alfe do maksimum przy
                // rzadszych summonach i zamiast swiatla robil jednolity zielony/niebieski filtr.
                intensity = chargeValue,
                platformFraction = platformFraction,
                modifier = Modifier.fillMaxSize()
            )

            Image(
                painter = painterResource(Resources.CityIsland.altar),
                contentDescription = "Oltarz",
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        // Oltarz lekko "oddycha" w trakcie ladowania rytualu.
                        val pulse = 1f + 0.05f * chargeValue
                        scaleX = pulse
                        scaleY = pulse
                    }
            )

            // Fala uderzeniowa NAD oltarzem - wczesniej cala warstwa efektow szla pod grafika,
            // wiec oltarz ja zasłaniał i po podmianie na nieprzezroczysty PNG zrobila sie
            // praktycznie niewidoczna.
            SummonBurst(
                accent = accent,
                charge = chargeValue,
                burst = burstValue,
                power = power,
                platformFraction = platformFraction,
                modifier = Modifier.fillMaxSize()
            )

            if (hero != null && phase >= SummonPhase.Reveal) {
                val preset = MonsterVisualPresets.forSpecies(hero.spriteKey)
                SummonedMonster(
                    card = hero,
                    rise = riseValue,
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        // Stopy potwora dokladnie na plycie kregu, niezaleznie od tego jak wysoki
                        // jest sprite danego gatunku.
                        .padding(top = (platformY - preset.baseSizeDp).coerceAtLeast(0.dp))
                )
            }

            // Tabliczka na samej gorze sceny - wczesniej siedziala pod potworem i zaslaniala oltarz.
            if (hero != null && phase == SummonPhase.Result) {
                NamePlate(
                    card = hero,
                    accent = accent,
                    modifier = Modifier.align(Alignment.TopCenter).padding(top = 6.dp)
                )
            }

            if (phase == SummonPhase.Idle) {
                Text(
                    text = "Wybierz banner i odpal rytual.",
                    color = Muted,
                    fontFamily = FontFirst(),
                    fontSize = 11.sp,
                    modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 6.dp)
                )
            }

            // Wynik x10 - siatka kart nad oltarzem, gdy rytual sie skonczy.
            if (phase == SummonPhase.Result && pulled.size > 1) {
                MultiPullGrid(
                    pulled = pulled,
                    modifier = Modifier.align(Alignment.BottomCenter).padding(8.dp)
                )
            }

            // Blysk na wierzchu wszystkiego - moment "puf" miedzy ladowaniem a wyjsciem potwora.
            if (flashValue > 0f) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.radialGradient(
                                colors = listOf(
                                    Color.White.copy(alpha = flashValue),
                                    accent.copy(alpha = flashValue * 0.9f),
                                    Color.Transparent
                                )
                            )
                        )
                )
            }
        }
    }
}

/** Miekka poswiata pod oltarzem - jedyna warstwa efektow rysowana POD grafika. */
@Composable
private fun RitualGlow(
    accent: Color,
    intensity: Float,
    platformFraction: Float,
    modifier: Modifier = Modifier
) {
    if (intensity <= 0.01f) return

    val transition = rememberInfiniteTransition(label = "glow")
    val pulse by transition.animateFloat(
        initialValue = 0.92f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glowPulse"
    )

    Canvas(modifier = modifier) {
        val center = Offset(size.width / 2f, size.height * platformFraction)
        val radius = size.minDimension * 0.72f * pulse * (0.7f + 0.3f * intensity)
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(accent.copy(alpha = GLOW_MAX_ALPHA * intensity.coerceIn(0f, 1f)), Color.Transparent),
                center = center,
                radius = radius
            ),
            radius = radius,
            center = center
        )
    }
}

/**
 * Cala "pirotechnika" rytualu, rysowana NAD oltarzem: zbierajaca sie energia w trakcie ladowania,
 * a potem rozchodzace sie pierscienie uderzeniowe, slup swiatla i iskry.
 *
 * Wszystko skaluje sie [power] (rzadkosc summona) - pospolity dostaje jeden skromny pierscien,
 * legendarny pelny pokaz z kilkoma falami i szerokim slupem swiatla.
 */
@Composable
private fun SummonBurst(
    accent: Color,
    charge: Float,
    burst: Float,
    power: Float,
    platformFraction: Float,
    modifier: Modifier = Modifier
) {
    if (charge <= 0.01f && burst <= 0.01f) return

    val transition = rememberInfiniteTransition(label = "beam")
    val breathe by transition.animateFloat(
        initialValue = 0.82f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "beamBreathe"
    )

    Canvas(modifier = modifier) {
        val center = Offset(size.width / 2f, size.height * platformFraction)
        val baseRadius = size.minDimension * 0.5f

        // 1. Ladowanie - jasny rdzen zbierajacy sie na plycie kregu, zanim strzeli.
        if (charge > 0f && burst <= 0f) {
            val coreRadius = baseRadius * (0.10f + 0.22f * charge) * power
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.85f * charge),
                        accent.copy(alpha = 0.7f * charge),
                        Color.Transparent
                    ),
                    center = center,
                    radius = coreRadius
                ),
                radius = coreRadius,
                center = center
            )
        }

        if (burst <= 0f) return@Canvas

        // 2. Slup swiatla nad kregiem - prosty prostokat z OSTRYMI bokami, jak promien teleportu.
        //
        // Swiadomie bez rozmywania krawedzi bocznych i bez stozka: ten twardy, pionowy slup czyta
        // sie jak "cos sie tu wlasnie zmaterializowalo", a proby zmiekczania bokow (gradient
        // poziomy, rozszerzanie ku dolowi) odbieraly mu ten charakter.
        //
        // Caly efekt siedzi w gradiencie PIONOWYM: przy plycie kregu slup jest calkiem
        // przezroczysty i nie zaslania summona, a im wyzej, tym bardziej lity.
        val beamFade = breathe
        val beamWidth = baseRadius * (0.22f + 0.16f * power)

        // Slup WYSUWA sie z sufitu: rosnie w dol od gornej krawedzi az do plyty kregu. Rozciaga
        // sie w pierwszych ~40% fali, potem juz tylko stoi (i delikatnie oddycha).
        val beamExtend = (burst * BEAM_EXTEND_SPEED).coerceIn(0f, 1f)
        val beamBottom = center.y * beamExtend

        if (beamBottom > 1f) {
            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color.White.copy(alpha = BEAM_TOP_ALPHA * beamFade),
                        accent.copy(alpha = BEAM_TOP_ALPHA * 0.75f * beamFade),
                        Color.Transparent
                    ),
                    startY = 0f,
                    // Gradient konczy sie na BIEZACEJ dlugosci slupa, nie na docelowej - dzieki
                    // temu jego czolo jest zawsze wygaszone do zera i "zjezdza" w dol, zamiast
                    // ciac scene twarda pozioma krawedzia w polowie jasnosci.
                    endY = beamBottom
                ),
                topLeft = Offset(center.x - beamWidth / 2f, 0f),
                size = Size(beamWidth, beamBottom)
            )
        }

        // 3. Iskry rozlatujace sie od kregu - proste, ale mocno podbijaja wrazenie wybuchu.
        val sparkCount = (6 * power).toInt().coerceIn(6, 20)
        val sparkAlpha = ((1f - burst) * 0.95f).coerceIn(0f, 1f)
        repeat(sparkCount) { index ->
            val angle = (index.toFloat() / sparkCount) * 2f * PI.toFloat()
            val distance = baseRadius * (0.2f + 1.0f * burst) * (0.8f + 0.35f * power)
            val sparkCenter = Offset(
                x = center.x + cos(angle) * distance,
                y = center.y + sin(angle) * distance * 0.55f
            )
            drawCircle(
                color = Color.White.copy(alpha = sparkAlpha),
                radius = (3f + 3f * power) * (1f - burst * 0.6f),
                center = sparkCenter
            )
        }
    }
}

/**
 * Wychodzacy potwor - jednoczesnie wyjezdza z dolu, rosnie i sie rozjasnia. [rise] 0..1 z
 * [EaseOutBack], wiec na koncu lekko "przestrzeliwuje" i osiada - stad wrazenie wejscia z impetem.
 */
@Composable
private fun SummonedMonster(
    card: MonsterCard,
    rise: Float,
    modifier: Modifier = Modifier
) {
    val preset = MonsterVisualPresets.forSpecies(card.spriteKey)

    Image(
        painter = painterResource(preset.drawable),
        contentDescription = card.name,
        contentScale = ContentScale.Fit,
        modifier = modifier
            .size(preset.baseSizeDp)
            .graphicsLayer {
                val scale = (0.55f + 0.45f * rise) * preset.visualScale
                scaleX = scale
                scaleY = scale
                translationY = (1f - rise) * MONSTER_RISE_DISTANCE_PX
                alpha = rise.coerceIn(0f, 1f)
            }
    )
}

@Composable
private fun NamePlate(card: MonsterCard, accent: Color, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(Ink.copy(alpha = 0.9f))
            .border(2.dp, accent, RoundedCornerShape(10.dp))
            .padding(horizontal = 14.dp, vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Text(
            text = card.rarity.label(),
            color = accent,
            fontFamily = FontPixel(),
            fontWeight = FontWeight.Bold,
            fontSize = 11.sp
        )
        Text(
            text = card.name,
            color = Color.White,
            fontFamily = FontPixel(),
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp
        )
        Text(
            text = "❤ ${card.hp}   ⚔ ${card.attack}",
            color = TextBody.copy(alpha = 0.85f),
            fontFamily = FontFirst(),
            fontSize = 10.sp
        )
    }
}

/**
 * Przyciski losowania. Gdy nie stac - przycisk NIE jest po prostu wygaszony: pod spodem stoi zdanie
 * mowiace, czego brakuje i skad sie to bierze. Wygaszony przycisk bez powodu to najszybszy sposob,
 * zeby gracz uznal, ze ekran jest zepsuty.
 */
@Composable
private fun SummonButtons(
    banner: SummonBanner,
    tickets: Int,
    enabled: Boolean,
    onSummon: (Int) -> Unit
) {
    val singleCost = banner.ticketCost
    val tenCost = banner.ticketCost * 10

    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            TownActionButton(
                text = "SUMMON x1",
                subtitle = "🎟️ $singleCost",
                primary = true,
                enabled = enabled && tickets >= singleCost,
                modifier = Modifier.weight(1f),
                onClick = { onSummon(1) }
            )
            TownActionButton(
                text = "SUMMON x10",
                subtitle = "🎟️ $tenCost",
                enabled = enabled && tickets >= tenCost,
                modifier = Modifier.weight(1f),
                onClick = { onSummon(10) }
            )
        }

        if (tickets < singleCost) {
            Text(
                text = "Brakuje losow (masz $tickets z $singleCost). Losy wypadaja ze skrzyni po bossie lochu.",
                color = Color(0xFFD1453D),
                fontFamily = FontFirst(),
                fontSize = 11.sp
            )
        }
    }
}

/** Wynik summona x10 - male karty wszystkich wylosowanych kompanow, w rzedach po piec. */
@Composable
private fun MultiPullGrid(pulled: List<MonsterCard>, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        pulled.chunked(5).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                row.forEach { card ->
                    val accent = card.rarity.color()
                    Column(
                        modifier = Modifier
                            .size(width = 52.dp, height = 62.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Ink.copy(alpha = 0.9f))
                            .border(1.5.dp, accent, RoundedCornerShape(8.dp))
                            .padding(3.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Image(
                            painter = painterResource(MonsterVisualPresets.forSpecies(card.spriteKey).drawable),
                            contentDescription = null,
                            contentScale = ContentScale.Fit,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            text = card.rarity.label(),
                            color = accent,
                            fontFamily = FontFirst(),
                            fontWeight = FontWeight.Bold,
                            fontSize = 7.sp,
                            maxLines = 1
                        )
                    }
                }
            }
        }
    }
}

/** Losuje rzadkosc wg wag bannera, a potem konkretnego potwora z tej rzadkosci. */
private fun SummonBanner.roll(): MonsterCard {
    val total = weights.values.sum().coerceAtLeast(1)
    var roll = Random.nextInt(total)
    var picked: Rarity = Rarity.Common
    for ((rarity, weight) in weights) {
        if (roll < weight) {
            picked = rarity
            break
        }
        roll -= weight
    }

    val pool = MonsterCatalog.all.filter { it.rarity == picked }
        .ifEmpty { MonsterCatalog.all }
    return pool[Random.nextInt(pool.size)]
}

private fun Rarity.color(): Color = when (this) {
    Rarity.Common -> Color(0xFF9DB2C4)
    Rarity.Uncommon -> Color(0xFF22A55E)
    Rarity.Rare -> Color(0xFF2F8FE0)
    Rarity.Epic -> Color(0xFF8B5CF6)
    Rarity.Legendary -> Color(0xFFFFB020)
}

/** Mnoznik rozmachu efektow - patrz [SummonBurst]. */
private fun Rarity.effectPower(): Float = when (this) {
    Rarity.Common -> 1.0f
    Rarity.Uncommon -> 1.35f
    Rarity.Rare -> 1.75f
    Rarity.Epic -> 2.2f
    Rarity.Legendary -> 2.8f
}

private fun Rarity.label(): String = when (this) {
    Rarity.Common -> "POSPOLITY"
    Rarity.Uncommon -> "NIEZWYKLY"
    Rarity.Rare -> "RZADKI"
    Rarity.Epic -> "EPICKI"
    Rarity.Legendary -> "LEGENDARNY"
}

private const val CHARGE_MS = 750
private const val FLASH_MS = 260

/** Fala uderzeniowa biegnie dluzej niz sam blysk - jeszcze w trakcie wstawania potwora. */
private const val BURST_MS = 900

/** Krycie slupa u samej gory; przy plycie kregu schodzi do zera - patrz [SummonBurst]. */
private const val BEAM_TOP_ALPHA = 0.8f

/** Jak szybko slup wysuwa sie z sufitu wzgledem calej fali (2.5 = pelna dlugosc po ~40% fali). */
private const val BEAM_EXTEND_SPEED = 2.5f

/** Krycie duzej poswiaty na scenie. Powyzej ~0.45 przestaje oswietlac, a zaczyna barwic. */
private const val GLOW_MAX_ALPHA = 0.38f
private const val RISE_MS = 720
private const val REVEAL_HOLD_MS = 500L

/** O ile pikseli nizej startuje potwor, zanim "wyjdzie" z oltarza. */
private const val MONSTER_RISE_DISTANCE_PX = 140f

/** Gdzie w (kwadratowym) kadrze oltarza jest powierzchnia kregu (zmierzone z pliku PNG). */
private const val ALTAR_PLATFORM_Y_FRACTION = 0.62f
