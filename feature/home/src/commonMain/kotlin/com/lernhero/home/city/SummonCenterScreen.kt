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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
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
import com.lernhero.domain.monster.MonsterEquipment
import com.lernhero.domain.craft.CraftCost
import com.lernhero.domain.craft.SummonCosts
import com.lernhero.domain.monster.MonsterCatalog
import com.lernhero.domain.spell.Spell
import com.lernhero.domain.summon.FreeSummon
import kotlin.time.Clock
import org.jetbrains.compose.resources.DrawableResource
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
 *
 * [id] trafia do zapisu ([com.lernhero.domain.player.Player.freeSummonClaims]), wiec NIE zmieniaj go
 * przy zmianie nazwy - inaczej wszystkim graczom zresetuje sie licznik darmowego summona.
 * [art] to na razie placeholder, patrz [Resources.SummonBanner].
 */
private enum class SummonBanner(
    val id: String,
    val label: String,
    val art: DrawableResource,
    val ticketCost: Int,
    val cooldownMs: Long,
    val accent: Color,
    val glow: Color,
    val weights: Map<Rarity, Int>
) {
    Basic(
        id = FreeSummon.BASIC_BANNER,
        label = "Basic Summon",
        art = Resources.SummonBanner.Basic,
        ticketCost = 1,
        cooldownMs = FreeSummon.BASIC_COOLDOWN_MS,
        accent = Color(0xFF7FB3E8),
        glow = Color(0xFF1E3A5F),
        weights = mapOf(
            Rarity.Common to 64,
            Rarity.Uncommon to 27,
            Rarity.Rare to 7,
            Rarity.Epic to 2
        )
    ),
    Heroic(
        id = FreeSummon.HEROIC_BANNER,
        label = "Heroic Summon",
        art = Resources.SummonBanner.Heroic,
        ticketCost = 3,
        cooldownMs = FreeSummon.HEROIC_COOLDOWN_MS,
        accent = Color(0xFFFFC44D),
        glow = Color(0xFF6B3410),
        weights = mapOf(
            Rarity.Common to 22,
            Rarity.Uncommon to 42,
            Rarity.Rare to 26,
            Rarity.Epic to 8,
            Rarity.Legendary to 2
        )
    )
}

/** Etapy rytualu - patrz [SummonCenterScreen]. */
private enum class SummonPhase { Idle, Charging, Flash, Reveal, Result }

/** Dwie polowy budynku: oltarz (zdobywanie kart) i kolekcja (zamiana kart na sile). */
private enum class SummonSection(val label: String) {
    Ritual("Summon"),
    Collection("Collection"),
    /** Cala pula bannerow w tabeli - statystyki, moce i zaklecia (patrz [MonsterIndexTab]). */
    Index("Index")
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

    // Zegar odliczania darmowych summonow. Tyka tylko na tym ekranie i tylko w sekcji rytualu -
    // nie ma po co budzic rekompozycji, gdy gracz oglada kolekcje.
    var now by remember { mutableStateOf(Clock.System.now().toEpochMilliseconds()) }
    LaunchedEffect(section) {
        while (section == SummonSection.Ritual) {
            now = Clock.System.now().toEpochMilliseconds()
            delay(1000)
        }
    }

    // Naglowek jest ZAWSZE zbity (compact), niezaleznie od sekcji. Wczesniej rytual dostawal pelny
    // naglowek z szescioma surowcami, a kolekcja zbity - przez co przelacznik sekcji przeskakiwal
    // w pionie przy kazdym tapnieciu i wygladalo to, jakby zakladki zamienialy sie miejscami.
    // Poza tym drewno/kamien/ruda nie maja tu czego szukac: w Summon Center placi sie LOSAMI,
    // a te licza sie przy samych bannerach.
    // Tlo CALEGO Summon Center - pod naglowkiem, zakladkami i obiema sekcjami. Siedzi POZA
    // scaffoldem, zeby nie bylo nad nim widac globalnego tla aplikacji.
    Box(modifier = Modifier.fillMaxSize()) {
      Image(
          painter = painterResource(Resources.Image.summonerCollectionBackground),
          contentDescription = null,
          contentScale = ContentScale.Crop,
          modifier = Modifier.matchParentSize()
      )

      TownScreenScaffold(
        title = "Summon Center",
        resources = state.resources,
        money = state.money,
        toasts = emptyList(),
        onBack = onBack,
        compact = true
    ) {
      Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 8.dp, end = 8.dp, top = 4.dp, bottom = 6.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            SectionTabs(selected = section, onSelect = { section = it }, enabled = !busy)

            if (section == SummonSection.Collection) {
                MonsterCollectionTab(
                    collection = state.collection,
                    money = state.money,
                    essence = state.resources.essence,
                    equipmentOf = playerViewModel::equipmentOf,
                    bag = state.monsterBag,
                    gearTakenBy = { id ->
                        MonsterEquipment.equippedElsewhere(state.monsterEquipment, id)
                    },
                    onUpgrade = playerViewModel::upgradeMonster,
                    onAscend = playerViewModel::ascendMonster,
                    onDissolve = playerViewModel::dissolveMonster,
                    onUpgradeSpell = playerViewModel::upgradeMonsterSpell,
                    onEquip = playerViewModel::equipMonsterItem,
                    onUnequipSlot = playerViewModel::unequipMonsterSlot,
                    onUnequipAll = playerViewModel::unequipMonsterAll,
                    onAutoEquip = playerViewModel::autoEquipMonster,
                    modifier = Modifier.weight(1f)
                )
                return@Column
            }

            if (section == SummonSection.Index) {
                MonsterIndexTab(
                    ownedCounts = state.collection.associate { it.card.id to it.count },
                    modifier = Modifier.weight(1f)
                )
                return@Column
            }

            BannerList(
                selected = banner,
                tickets = state.resources.summonTickets,
                claims = state.freeSummonClaims,
                now = now,
                enabled = !busy,
                onSelect = { banner = it }
            )

            // Scena rytualu bez ramki i bez panelu - tlo bedzie wlasna grafika, a obwodka tylko
            // dzielila ekran na pudelka.
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

            val freeReady = FreeSummon.isReady(state.freeSummonClaims[banner.id], banner.cooldownMs, now)

            SummonButtons(
                banner = banner,
                tickets = state.resources.summonTickets,
                freeReady = freeReady,
                enabled = !busy,
                onSummon = { count ->
                    val rolled = List(count) { banner.roll() }
                    pulled = rolled
                    // Darmowy summon x1 idzie osobna sciezka - bez kosztu, ze stemplem czasu.
                    if (count == 1 && freeReady) {
                        playerViewModel.claimFreeSummon(banner.id, banner.cooldownMs, rolled.first(), now)
                    } else {
                        // Placimy losami bannera za caly ciag - jeden zapis na cale losowanie.
                        playerViewModel.summonMonsters(rolled, CraftCost(tickets = banner.ticketCost * count))
                    }
                    phase = SummonPhase.Charging
                }
            )
        }

        // Komunikat jako NAKLADKA, nie element ukladu - wczesniej pojawiajaca sie linijka tekstu
        // spychala cala zawartosc w gore i ekran skakal przy kazdym summonie. Siedzi NAD rzedem
        // przyciskow, zeby ich nie przykrywac.
        state.message?.let { message ->
            Text(
                text = message,
                color = Gold,
                fontFamily = FontFirst(),
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp,
                maxLines = 1,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = MESSAGE_BOTTOM_GAP)
                    .clip(RoundedCornerShape(50))
                    .background(Ink.copy(alpha = 0.75f))
                    .padding(horizontal = 12.dp, vertical = 3.dp)
            )
        }
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
                    .padding(vertical = 6.dp),
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

/**
 * Lista bannerow ulozona jak w mobilnych gaczach: posag/figura po lewej, obok nazwa, licznik losow
 * i linijka "Za darmo" z odliczaniem. Wybrany banner ma jasna ramke i mocniejsza poswiate.
 *
 * Grafiki to [Resources.SummonBanner] - na razie placeholdery, podmiana pliku wystarczy.
 */
@Composable
private fun BannerList(
    selected: SummonBanner,
    tickets: Int,
    claims: Map<String, Long>,
    now: Long,
    enabled: Boolean,
    onSelect: (SummonBanner) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        SummonBanner.entries.forEach { entry ->
            BannerCard(
                banner = entry,
                selected = entry == selected,
                tickets = tickets,
                remainingMs = FreeSummon.remainingMs(claims[entry.id], entry.cooldownMs, now),
                enabled = enabled,
                onClick = { onSelect(entry) }
            )
        }
    }
}

@Composable
private fun BannerCard(
    banner: SummonBanner,
    selected: Boolean,
    tickets: Int,
    remainingMs: Long,
    enabled: Boolean,
    onClick: () -> Unit
) {
    val free = remainingMs <= 0L

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(
                Brush.horizontalGradient(
                    if (selected) listOf(banner.glow, banner.glow.copy(alpha = 0.35f))
                    else listOf(Ink.copy(alpha = 0.75f), Ink.copy(alpha = 0.55f))
                )
            )
            .border(
                width = if (selected) 2.dp else 1.dp,
                color = if (selected) banner.accent else GoldDim.copy(alpha = 0.45f),
                shape = RoundedCornerShape(10.dp)
            )
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(9.dp)
    ) {
        Image(
            painter = painterResource(banner.art),
            contentDescription = banner.label,
            contentScale = ContentScale.Fit,
            modifier = Modifier.size(42.dp)
        )

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            Text(
                text = banner.label,
                color = if (selected) Color.White else TextBody,
                fontFamily = FontPixel(),
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                maxLines = 1
            )
            // Pasek z liczba losow - jak w referencji: ciemne wglebienie z iloscia waluty.
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(Color.Black.copy(alpha = 0.45f))
                    .padding(horizontal = 8.dp, vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(text = "🎟️", fontSize = 9.sp)
                Text(
                    text = "${banner.ticketCost}",
                    color = if (tickets >= banner.ticketCost) Color.White else Color(0xFFD1453D),
                    fontFamily = FontPixel(),
                    fontWeight = FontWeight.Bold,
                    fontSize = 10.sp
                )
            }
        }

        // Odliczanie do darmowego summona - dokladnie ta rola co "Free 01:54:39" w referencji.
        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = "Free",
                color = Muted,
                fontFamily = FontFirst(),
                fontSize = 8.sp
            )
            Text(
                text = FreeSummon.format(remainingMs),
                color = if (free) Color(0xFF5FD08A) else TextBody,
                fontFamily = FontPixel(),
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp
            )
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

    // Bez TownPanel: zadnej ramki ani tla. Scena ma byc otwarta - docelowo pod spodem stanie
    // wlasna grafika tla, a obwodka tylko robilaby z niej "pudelko na ekranie".
    Box(modifier = modifier.fillMaxWidth()) {
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            // ContentScale.Fit na fillMaxSize() dobiera rozmiar SAM, z obu wymiarow naraz - bez
            // recznie dobieranego mnoznika, ktory trzeba przeliczac za kazdym razem, gdy podmienia
            // sie PNG (poprzedni "1.75x" byl skalibrowany pod stary plik, gdzie oltarz zajmowal
            // tylko 52% wlasnego kadru - przy nowym, ktory wypelnia 94%, dawal zle wyniki). Oltarz
            // jest kwadratowy, wiec renderowany bok = krotszy wymiar dostepnej przestrzeni.
            // Krag jest teraz WIEKSZY niz krotszy bok sceny ([ALTAR_SCALE]) - przy zwyklym
            // ContentScale.Fit w wysokiej, waskiej scenie oltarz zajmowal tylko jej srodek i
            // ginal w tle, a rytual wygladal jak miniaturka.
            val altarSide = minOf(maxWidth, maxHeight) * ALTAR_SCALE
            // Plyta kregu (= stopy przyzwanego potwora) ladujaca na TEJ SAMEJ wysokosci sceny co
            // kamien z kompanem w zakladce Collection. Wczesniej oltarz byl po prostu wysrodkowany
            // w pionie, wiec przy przelaczeniu zakladki postac przeskakiwala.
            val platformY = maxHeight * ALTAR_PLATFORM_SCENE_FRACTION
            // Wysokosc plyty kregu w kadrze (ulamek CALEGO kwadratowego PNG, zmierzone z pliku).
            val altarTop = platformY - altarSide * ALTAR_PLATFORM_Y_FRACTION
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

            // Oltarz i jego swiatla siedza w JEDNYM pudelku - inaczej znaki musialyby powtarzac
            // caly rachunek pozycji i skali, i rozjezdzalyby sie z grafika przy kazdej zmianie
            // [ALTAR_SCALE] albo rozmiaru sceny.
            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .offset(y = altarTop)
                    .size(altarSide)
                    .graphicsLayer {
                        // Oltarz lekko "oddycha" w trakcie ladowania rytualu.
                        val pulse = 1f + 0.05f * chargeValue
                        scaleX = pulse
                        scaleY = pulse
                    }
            ) {
                Image(
                    painter = painterResource(Resources.CityIsland.altar),
                    contentDescription = "Altar",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.fillMaxSize()
                )

                // Runy, krag i swiece zapalaja sie w kolorze rzadkosci - patrz [AltarRuneLights].
                AltarRuneLights(
                    accent = accent,
                    // W spoczynku znaki tylko sie tla; po kliknieciu summona rozpalaja sie razem z
                    // ladowaniem rytualu i dostaja dodatkowy kop w momencie blysku.
                    intensity = (ALTAR_RUNE_IDLE + (1f - ALTAR_RUNE_IDLE) * chargeValue + flashValue)
                        .coerceIn(0f, 1f),
                    charge = chargeValue,
                    modifier = Modifier.fillMaxSize()
                )
            }

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
                        // jest sprite danego gatunku. +SPRITE_EDGE_PADDING: kadr sprite'a ma pod
                        // stopami wlasny pusty margines (~13% wysokosci), wiec bez tej poprawki
                        // bohater wisi nad kregiem - tym bardziej, im wiekszy go rysujemy.
                        .offset(
                            y = platformY - preset.baseSizeDp * SUMMONED_MONSTER_SCALE *
                                (1f - SPRITE_EDGE_PADDING)
                        )
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
                    text = "Pick a banner and start the ritual.",
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
        val radius = size.minDimension * 0.72f * EFFECT_SCALE * pulse * (0.7f + 0.3f * intensity)
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
        val baseRadius = size.minDimension * 0.5f * EFFECT_SCALE

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
            // Bohater po wyjsciu z kregu byl mniejszy niz sam krag - na scenie, ktora jest
            // punktem kulminacyjnym calego ekranu, to gubilo caly efekt.
            .size(preset.baseSizeDp * SUMMONED_MONSTER_SCALE)
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
 * Dwa male przyciski summona pod scena - jak w referencji: zwarta pastylka z iloscia losow w
 * kolku i podpisem obok, zamiast dwoch szerokich plyt na cala szerokosc ekranu.
 *
 * Gdy licznik darmowego summona doszedl do zera, przycisk x1 zmienia sie w "ZA DARMO" - gracz
 * nie musi nigdzie indziej sprawdzac, czy juz mu sie nalezy.
 */
@Composable
private fun SummonButtons(
    banner: SummonBanner,
    tickets: Int,
    freeReady: Boolean,
    enabled: Boolean,
    onSummon: (Int) -> Unit
) {
    val singleCost = banner.ticketCost
    val tenCost = banner.ticketCost * 10

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically
    ) {
        SummonPill(
            label = if (freeReady) "Free" else "Summon",
            badge = if (freeReady) "★" else "$singleCost",
            highlighted = true,
            enabled = enabled && (freeReady || tickets >= singleCost),
            onClick = { onSummon(1) }
        )
        SummonPill(
            label = "Summon x10",
            badge = "$tenCost",
            highlighted = false,
            enabled = enabled && tickets >= tenCost,
            onClick = { onSummon(10) }
        )
    }
}

@Composable
private fun SummonPill(
    label: String,
    badge: String,
    highlighted: Boolean,
    enabled: Boolean,
    onClick: () -> Unit
) {
    val top = if (highlighted) Color(0xFFFFD98A) else Color(0xFFE9C069)
    val bottom = if (highlighted) Color(0xFFEDA429) else Color(0xFFC38C24)

    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(
                if (enabled) Brush.verticalGradient(listOf(top, bottom))
                else Brush.verticalGradient(listOf(Color(0xFF453B2C), Color(0xFF332B20)))
            )
            .border(
                width = 2.dp,
                color = if (enabled) Color(0xFFFFF0C2) else GoldDim.copy(alpha = 0.5f),
                shape = RoundedCornerShape(50)
            )
            .clickable(enabled = enabled, onClick = onClick)
            .padding(start = 5.dp, end = 16.dp, top = 5.dp, bottom = 5.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(7.dp)
    ) {
        // Kolko z kosztem - ten sam pomysl, co zwiniety zwoj z liczba w referencji.
        Box(
            modifier = Modifier
                .size(26.dp)
                .clip(RoundedCornerShape(50))
                .background(Color(0xFF2A1B08).copy(alpha = if (enabled) 0.75f else 0.5f)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = badge,
                color = if (enabled) Color(0xFFFFD98A) else Muted,
                fontFamily = FontPixel(),
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp
            )
        }
        Text(
            text = label,
            color = if (enabled) Color(0xFF3A2600) else Muted,
            fontFamily = FontPixel(),
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp,
            maxLines = 1
        )
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
// Wysokosc kregu rytualnego w kadrze summon_center.png (ulamek calego kwadratowego PNG) - tu
// laduja stopy przyzwanego potwora. Zmierzone z pliku: srodek gwiazdy ~0.61, przednia krawedz
// plyty ~0.76; stopy na srodku-przodzie kregu.
private const val ALTAR_PLATFORM_Y_FRACTION = 0.66f

/** O ile krag jest wiekszy od krotszego boku sceny - powyzej 1 celowo wychodzi poza kadr. */
private const val ALTAR_SCALE = 1.25f

/**
 * Na jakiej wysokosci sceny lezy plyta kregu. Ta sama wartosc co srodek grupy
 * w [com.lernhero.home.ui.FloatingIsland], zeby przelaczenie Summon <-> Collection nie
 * przesuwalo postaci po ekranie.
 */
private const val ALTAR_PLATFORM_SCENE_FRACTION = 0.60f

/** O ile wiekszy jest przyzwany bohater od swojego bazowego rozmiaru z presetu. */
private const val SUMMONED_MONSTER_SCALE = 1.55f

/** Przezroczysty margines w kadrze sprite'a potwora - zmierzone na plikach, jak w FloatingIsland. */
private const val SPRITE_EDGE_PADDING = 0.13f

/** Mnoznik promieni poswiaty i fali rytualu - efekty maja byc widoczne, nie subtelne. */
private const val EFFECT_SCALE = 1.35f

/** Ile miejsca zostawic pod komunikatem, zeby nie wszedl na rzad przyciskow summona. */
private val MESSAGE_BOTTOM_GAP = 52.dp
