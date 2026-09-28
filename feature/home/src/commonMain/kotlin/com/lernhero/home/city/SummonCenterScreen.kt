package com.lernhero.home.city

import androidx.compose.runtime.DisposableEffect
import com.lernhero.home.tutorial.LocalTutorial
import com.lernhero.home.tutorial.TutorialTarget
import com.lernhero.home.tutorial.tutorialTarget
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.toSize
import com.lernhero.domain.common.Rarity
import com.lernhero.domain.craft.CraftCost
import com.lernhero.domain.monster.MonsterCard
import com.lernhero.domain.monster.MonsterCatalog
import com.lernhero.domain.monster.MonsterEquipment
import com.lernhero.domain.summon.FreeSummon
import com.lernhero.game.character.ElementEmblem
import com.lernhero.game.character.MonsterVisualPresets
import com.lernhero.home.player.PlayerViewModel
import com.lernhero.home.rememberAmbienceClock
import com.lernhero.home.ui.RarityPalette
import com.lernhero.shared.resources.Resources
import com.lernhero.shared.theme.FontFirst
import com.lernhero.shared.theme.FontPixel
import com.lernhero.shared.theme.StrokedText
import kotlinx.coroutines.delay
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random
import kotlin.time.Clock

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

/** Dwie polowy budynku: oltarz (zdobywanie kart) i kolekcja (zamiana kart na sile). */
private enum class SummonSection(val label: String) {
    Ritual("Summon"),
    Collection("Collection"),
    /** Cala pula bannerow w tabeli - statystyki, moce i zaklecia (patrz [MonsterIndexTab]). */
    Index("Index")
}

/**
 * Summon Center w stylu mobilnych idle RPG: u gory wybor bannera, na srodku oltarz, a po
 * nacisnieciu summona odpala sie pokaz rezyserowany przez [SummonShow] - energia zbiera sie
 * w kule, slup swiatla zdradza (i potrafi podbic) rzadkosc, wybuch, sylweta w swietle jak
 * zawodnik przy walkoucie w FIFA, a na koniec karta kompana odkrywana po kawalku.
 *
 * Losowanie idzie po prawdziwym [MonsterCatalog], wiec wypadaja te same potwory, ktorymi sie gra -
 * i KAZDY wylosowany kompan faktycznie laduje w kolekcji.
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

    val scope = rememberCoroutineScope()
    val show = remember { SummonShow(scope) }
    val anchors = remember { SummonAnchors() }
    val time = rememberAmbienceClock()
    // Kolor calej pirotechniki: w spoczynku kolor bannera, w trakcie pokazu - biezacy kolor slupa.
    val fxColor = animateColorAsState(
        targetValue = if (show.stage == ShowStage.Idle) banner.accent else show.tease.fx(),
        animationSpec = tween(240),
        label = "summonFx"
    )
    val busy = show.busy

    // Pokaz summona gra na pelnym ekranie - samouczek chowa sie na ten czas (patrz TutorialController.held).
    val tutorial = LocalTutorial.current
    LaunchedEffect(tutorial, busy) { tutorial?.held = busy }
    DisposableEffect(tutorial) { onDispose { tutorial?.held = false } }

    // Zegar odliczania darmowych summonow. Tyka tylko na tym ekranie i tylko w sekcji rytualu -
    // nie ma po co budzic rekompozycji, gdy gracz oglada kolekcje.
    var now by remember { mutableStateOf(Clock.System.now().toEpochMilliseconds()) }
    LaunchedEffect(section) {
        while (section == SummonSection.Ritual) {
            now = Clock.System.now().toEpochMilliseconds()
            delay(1000)
        }
    }

    val noRipple = remember { MutableInteractionSource() }

    // Tlo CALEGO Summon Center - pod naglowkiem, zakladkami i obiema sekcjami. Siedzi POZA
    // scaffoldem, zeby nie bylo nad nim widac globalnego tla aplikacji.
    Box(modifier = Modifier.fillMaxSize().onGloballyPositioned { anchors.root = it }) {
      Image(
          painter = painterResource(Resources.Image.summonerCollectionBackground),
          contentDescription = null,
          contentScale = ContentScale.Crop,
          modifier = Modifier.matchParentSize()
      )
      // W trakcie pokazu wszystko poza oltarzem gasnie - reflektor na krag.
      Box(
          modifier = Modifier
              .matchParentSize()
              .graphicsLayer { alpha = show.dim.value * 0.62f }
              .background(Color.Black)
      )

      // Trzesienie obejmuje caly ekran z oltarzem; nakladki (blysk, karta) juz nie.
      Box(
          modifier = Modifier
              .fillMaxSize()
              .graphicsLayer {
                  val amp = show.shake.value * density
                  if (amp > 0f) {
                      val t = time.value
                      translationX = amp * (sin(t * 53f) + 0.5f * sin(t * 31f)) / 1.5f
                      translationY = amp * (cos(t * 47f) + 0.5f * sin(t * 23f)) / 1.5f
                  } else {
                      translationX = 0f
                      translationY = 0f
                  }
              }
      ) {
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
            // Zakladki, bannery i przyciski przygasaja razem ze scena - wzrok ma isc na krag.
            val fade = Modifier.graphicsLayer { alpha = 1f - 0.8f * show.dim.value }

            Box(modifier = fade) {
                SectionTabs(
                    selected = section,
                    onSelect = { section = it },
                    enabled = !busy,
                    bump = show.collectionBump,
                    arrived = show.arrived,
                    onCollectionPlaced = { anchors.collectionTab = it }
                )
            }

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

            Box(modifier = fade) {
                BannerList(
                    selected = banner,
                    tickets = state.resources.summonTickets,
                    claims = state.freeSummonClaims,
                    now = now,
                    enabled = !busy,
                    onSelect = { banner = it }
                )
            }

            // Scena rytualu bez ramki i bez panelu - tlo jest wlasna grafika, a obwodka tylko
            // dzielila ekran na pudelka.
            AltarStage(
                show = show,
                time = time,
                fxColor = fxColor,
                anchors = anchors,
                modifier = Modifier
                    .weight(1f)
                    .onGloballyPositioned { anchors.stage = it }
            )

            val freeReady = FreeSummon.isReady(state.freeSummonClaims[banner.id], banner.cooldownMs, now)

            Box(modifier = fade) {
                SummonButtons(
                    banner = banner,
                    tickets = state.resources.summonTickets,
                    freeReady = freeReady,
                    enabled = !busy,
                    onSummon = { count ->
                        val rolled = List(count) { banner.roll() }
                        // "NEW!" liczymy PRZED zapisem: pierwsza kopia w tym losowaniu jest nowa,
                        // druga tego samego kompana w tym samym x10 juz nie.
                        val owned = state.collection.mapTo(mutableSetOf()) { it.card.id }
                        val fresh = rolled.map { owned.add(it.id) }
                        // Darmowy summon x1 idzie osobna sciezka - bez kosztu, ze stemplem czasu.
                        if (count == 1 && freeReady) {
                            playerViewModel.claimFreeSummon(banner.id, banner.cooldownMs, rolled.first(), now)
                        } else {
                            // Placimy losami bannera za caly ciag - jeden zapis na cale losowanie.
                            playerViewModel.summonMonsters(rolled, CraftCost(tickets = banner.ticketCost * count))
                        }
                        show.start(rolled, fresh)
                    }
                )
            }
        }

        // Komunikat jako NAKLADKA, nie element ukladu - wczesniej pojawiajaca sie linijka tekstu
        // spychala cala zawartosc w gore i ekran skakal przy kazdym summonie. W trakcie pokazu
        // milczy: "Joining your team: ..." w polowie rytualu zdradzalo wynik.
        state.message?.takeIf { !busy }?.let { message ->
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

      // ---- nakladki pokazu: na calym ekranie, nad naglowkiem i zakladkami ----
      if (section == SummonSection.Ritual && show.stage != ShowStage.Idle) {
          ScreenFlash(show = show, color = fxColor, focus = { anchors.orb() }, modifier = Modifier.fillMaxSize())

          val hero = show.hero
          if (hero != null && !show.isMulti && hero.rarity >= Rarity.Epic) {
              RarityShout(
                  rarity = hero.rarity,
                  show = show,
                  centerY = { anchors.stageRect().let { it.top + it.height * 0.2f } },
                  modifier = Modifier.fillMaxSize()
              )
          }

          ConfettiRain(show = show, modifier = Modifier.fillMaxSize())

          if (show.stage == ShowStage.Card && hero != null) {
              RevealCardOverlay(
                  show = show,
                  card = hero,
                  fresh = show.heroFresh,
                  origin = { anchors.chest(show) },
                  target = { anchors.stageRect().let { Offset(it.center.x, it.top + it.height * 0.47f) } },
                  stowTo = { anchors.collectionCenter() },
                  maxHeightPx = { anchors.stageRect().height },
                  time = time
              )
          }

          if (show.stage == ShowStage.Grid) {
              GridOverlay(
                  show = show,
                  area = { anchors.stageRect() },
                  origin = { anchors.orb() },
                  stowTo = { anchors.collectionCenter() },
                  time = time
              )
          }

          // Tapniecie w trakcie rytualu przewija od razu do wyniku.
          if (show.stage == ShowStage.Ritual || show.stage == ShowStage.Walkout) {
              Box(
                  modifier = Modifier
                      .fillMaxSize()
                      .clickable(interactionSource = noRipple, indication = null) { show.skip() }
              )
          }
      }
    }
}

/**
 * Gdzie na ekranie sa oltarz i zakladka kolekcji. Trzymane jako ZWYKLE referencje, nie stan:
 * przy trzesieniu ekranem pozycje zmieniaja sie co klatke, a nakladki i tak czytaja je dopiero
 * przy rysowaniu - jako stan przebudowywalyby caly ekran 60 razy na sekunde.
 */
private class SummonAnchors {
    var root: LayoutCoordinates? = null
    var stage: LayoutCoordinates? = null
    var collectionTab: LayoutCoordinates? = null
    var geo: AltarGeometry? = null

    private fun offsetOf(child: LayoutCoordinates?): Offset {
        val r = root ?: return Offset.Zero
        val c = child ?: return Offset.Zero
        if (!r.isAttached || !c.isAttached) return Offset.Zero
        return r.localPositionOf(c, Offset.Zero)
    }

    fun stageRect(): Rect = Rect(offsetOf(stage), stage?.size?.toSize() ?: Size.Zero)

    fun orb(): Offset = geo?.let { offsetOf(stage) + it.orb } ?: stageRect().center

    /** Srodek sylwetki z poprawka na najazd kamery (skala wokol kuli). */
    fun chest(show: SummonShow): Offset {
        val g = geo ?: return stageRect().center
        val scale = 1f + 0.07f * show.zoom.value + 0.045f * show.punch.value
        val scaled = g.orb + (g.chest - g.orb) * scale
        return offsetOf(stage) + scaled
    }

    fun collectionCenter(): Offset {
        val tab = collectionTab ?: return Offset(stageRect().center.x, 0f)
        return offsetOf(tab) + Offset(tab.size.width / 2f, tab.size.height / 2f)
    }
}

/**
 * Przelacznik polowek budynku. Kolekcja siedzi TUTAJ, a nie w osobnej zakladce na dole ekranu, bo
 * to ta sama czynnosc co przyzywanie - zdobywasz kompana i od razu chcesz zobaczyc, co z nim zrobic.
 *
 * Zakladka Collection podskakuje z dymkiem "+N", gdy wpadaja do niej karty z losowania.
 */
@Composable
private fun SectionTabs(
    selected: SummonSection,
    onSelect: (SummonSection) -> Unit,
    enabled: Boolean,
    bump: Animatable<Float, AnimationVector1D>,
    arrived: Int,
    onCollectionPlaced: (LayoutCoordinates) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        SummonSection.entries.forEach { entry ->
            val isSelected = entry == selected
            val isCollection = entry == SummonSection.Collection
            Box(
                modifier = Modifier
                    .weight(1f)
                    .then(
                        if (isCollection) {
                            Modifier
                                .onGloballyPositioned(onCollectionPlaced)
                                .graphicsLayer {
                                    // Szybki "pop" w gore i sprezyste osiadanie.
                                    val p = bump.value
                                    val s = if (p <= 0f || p >= 1f) 1f
                                    else 1f + 0.3f * sin(PI.toFloat() * (p * 3f).coerceAtMost(1f)) * (1f - p)
                                    scaleX = s
                                    scaleY = s
                                }
                        } else Modifier
                    )
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
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
                if (isCollection && arrived > 0) {
                    StrokedText(
                        text = "+$arrived",
                        size = 13.sp,
                        color = Color(0xFF7CF08A),
                        outline = Color(0xFF0B1A0E),
                        strokeWidth = 5f,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .offset(x = 4.dp, y = (-12).dp)
                            .graphicsLayer {
                                val p = bump.value
                                alpha = if (p <= 0f || p >= 1f) 0f else ((1f - p) / 0.3f).coerceIn(0f, 1f)
                                translationY = -p * 16.dp.toPx()
                                val pop = 1f + 0.4f * (1f - (p * 4f).coerceAtMost(1f))
                                scaleX = pop
                                scaleY = pop
                            }
                    )
                }
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
 * Oltarz + cala scena pokazu. Warstwy od dolu: oltarz -> swiatlo, krag, promienie, slup, kula
 * ([RitualUnderFx]) -> kompan -> iskry, fale, kurz ([RitualOverFx]). Najazd kamery skaluje wszystko
 * razem wokol kuli energii, wiec efekty nie rozjezdzaja sie z kamieniem.
 */
@Composable
private fun AltarStage(
    show: SummonShow,
    time: State<Float>,
    fxColor: State<Color>,
    anchors: SummonAnchors,
    modifier: Modifier = Modifier
) {
    val hero = show.hero
    val stage = show.stage

    Box(modifier = modifier.fillMaxWidth()) {
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val density = LocalDensity.current
            // Krag jest WIEKSZY niz krotszy bok sceny ([ALTAR_SCALE]) - przy zwyklym Fit w wysokiej,
            // waskiej scenie oltarz zajmowal tylko jej srodek i rytual wygladal jak miniaturka.
            val altarSide = minOf(maxWidth, maxHeight) * ALTAR_SCALE
            // Plyta kregu (= stopy kompana) na TEJ SAMEJ wysokosci sceny co kamien z kompanem
            // w zakladce Collection - przelaczanie zakladek nie przesuwa postaci.
            val platformY = maxHeight * ALTAR_PLATFORM_SCENE_FRACTION
            val altarTop = platformY - altarSide * ALTAR_PLATFORM_Y_FRACTION
            val preset = hero?.let { MonsterVisualPresets.forSpecies(it.spriteKey) }
            val monsterSize = (preset?.baseSizeDp ?: 100.dp) * SUMMONED_MONSTER_SCALE
            val stageWidth = maxWidth

            val geo = remember(maxWidth, maxHeight, hero?.spriteKey) {
                with(density) {
                    val w = maxWidth.toPx()
                    val h = maxHeight.toPx()
                    val side = altarSide.toPx()
                    val top = altarTop.toPx()
                    val circleCenter = Offset(w / 2f, top + side * CIRCLE_Y_IN_ALTAR)
                    val feet = Offset(w / 2f, platformY.toPx())
                    val body = monsterSize.toPx() * (preset?.visualScale ?: 1f) * (1f - SPRITE_EDGE_PADDING)
                    AltarGeometry(
                        size = Size(w, h),
                        circleCenter = circleCenter,
                        circleRx = side * CIRCLE_RADIUS_IN_ALTAR,
                        feet = feet,
                        orb = Offset(w / 2f, circleCenter.y - side * ORB_LIFT_IN_ALTAR),
                        chest = Offset(w / 2f, feet.y - body * 0.5f),
                        base = minOf(w, h) * 0.5f * EFFECT_SCALE
                    )
                }
            }
            SideEffect { anchors.geo = geo }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        // Najazd kamery na krag + szarpniecie w chwili wybuchu.
                        val s = 1f + 0.07f * show.zoom.value + 0.045f * show.punch.value
                        scaleX = s
                        scaleY = s
                        transformOrigin = TransformOrigin(0.5f, (geo.orb.y / geo.size.height).coerceIn(0f, 1f))
                    }
            ) {
                Image(
                    painter = painterResource(Resources.CityIsland.altar),
                    contentDescription = "Altar",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .offset(y = altarTop)
                        .size(altarSide)
                )

                RitualUnderFx(show = show, geo = geo, color = fxColor, time = time, modifier = Modifier.fillMaxSize())

                val heroVisible = hero != null && when (stage) {
                    ShowStage.Walkout, ShowStage.Card -> !show.isMulti
                    ShowStage.Done -> true
                    else -> false
                }
                if (heroVisible && hero != null) {
                    RevealedMonster(
                        card = hero,
                        show = show,
                        glow = fxColor,
                        time = time,
                        size = monsterSize,
                        feetPivot = 1f - SPRITE_EDGE_PADDING,
                        silhouette = stage == ShowStage.Walkout,
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            // Stopy dokladnie na plycie kregu. Kadr sprite'a ma pod stopami wlasny
                            // pusty margines ([SPRITE_EDGE_PADDING]) - bez poprawki kompan wisi.
                            .offset(y = platformY - monsterSize * (1f - SPRITE_EDGE_PADDING))
                    )
                    if (hero.rarity >= Rarity.Rare && (stage == ShowStage.Walkout || stage == ShowStage.Card)) {
                        ElementClue(
                            element = hero.element,
                            show = show,
                            modifier = Modifier
                                .align(Alignment.TopCenter)
                                .offset(
                                    x = -minOf(monsterSize * 0.42f + 46.dp, stageWidth / 2 - 50.dp),
                                    y = platformY - monsterSize * 0.98f
                                )
                        )
                    }
                }

                RitualOverFx(show = show, geo = geo, color = fxColor, time = time, modifier = Modifier.fillMaxSize())
            }

            if (stage == ShowStage.Idle) {
                Text(
                    text = "Pick a banner and start the ritual.",
                    color = Muted,
                    fontFamily = FontFirst(),
                    fontSize = 11.sp,
                    modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 6.dp)
                )
            }

            // Po pokazie: tabliczka z imieniem nad kompanem, ktory zostal na oltarzu.
            AnimatedVisibility(
                visible = stage == ShowStage.Done && hero != null,
                enter = fadeIn(tween(400)) + scaleIn(tween(400), initialScale = 0.85f),
                modifier = Modifier.align(Alignment.TopCenter).padding(top = 6.dp)
            ) {
                hero?.let { NamePlate(card = it) }
            }
        }
    }
}

@Composable
private fun NamePlate(card: MonsterCard, modifier: Modifier = Modifier) {
    val accent = card.rarity.fx()
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(Ink.copy(alpha = 0.88f))
            .border(2.dp, accent, RoundedCornerShape(10.dp))
            .padding(start = 6.dp, end = 12.dp, top = 4.dp, bottom = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        ElementEmblem(element = card.element, size = 26.dp)
        Column {
            Text(
                text = RarityPalette.label(card.rarity).uppercase(),
                color = accent,
                fontFamily = FontFirst(),
                fontWeight = FontWeight.Black,
                fontSize = 9.sp,
                lineHeight = 10.sp,
                letterSpacing = 0.8.sp
            )
            Text(
                text = card.name,
                color = Color.White,
                fontFamily = FontFirst(),
                fontWeight = FontWeight.Black,
                fontSize = 13.sp,
                lineHeight = 15.sp
            )
        }
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
            onClick = { onSummon(1) },
            modifier = Modifier.tutorialTarget(TutorialTarget.FreeSummon)
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
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val top = if (highlighted) Color(0xFFFFD98A) else Color(0xFFE9C069)
    val bottom = if (highlighted) Color(0xFFEDA429) else Color(0xFFC38C24)

    Row(
        modifier = modifier
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

/** Gdzie w (kwadratowym) kadrze oltarza stoja stopy kompana - srodek-przod plyty kregu. */
private const val ALTAR_PLATFORM_Y_FRACTION = 0.66f

/** Srodek wyrytego kregu na plycie oltarza (ulamek kadru) - zmierzone na summon_center.webp. */
private const val CIRCLE_Y_IN_ALTAR = 0.60f

/** Promien wyrytego kregu (ulamek szerokosci kadru oltarza). */
private const val CIRCLE_RADIUS_IN_ALTAR = 0.215f

/** O ile nad srodkiem kregu wisi kula energii (ulamek kadru oltarza). */
private const val ORB_LIFT_IN_ALTAR = 0.085f

/** O ile krag jest wiekszy od krotszego boku sceny - powyzej 1 celowo wychodzi poza kadr. */
private const val ALTAR_SCALE = 1.25f

/**
 * Na jakiej wysokosci sceny lezy plyta kregu. Ta sama wartosc co srodek grupy
 * w [com.lernhero.home.ui.FloatingIsland], zeby przelaczenie Summon <-> Collection nie
 * przesuwalo postaci po ekranie.
 */
private const val ALTAR_PLATFORM_SCENE_FRACTION = 0.60f

/** O ile wiekszy jest przyzwany kompan od swojego bazowego rozmiaru z presetu. */
private const val SUMMONED_MONSTER_SCALE = 1.55f

/** Przezroczysty margines w kadrze sprite'a potwora - zmierzone na plikach, jak w FloatingIsland. */
private const val SPRITE_EDGE_PADDING = 0.13f

/** Mnoznik skali efektow rytualu - maja byc widoczne, nie subtelne. */
private const val EFFECT_SCALE = 1.35f

/** Ile miejsca zostawic pod komunikatem, zeby nie wszedl na rzad przyciskow summona. */
private val MESSAGE_BOTTOM_GAP = 52.dp
