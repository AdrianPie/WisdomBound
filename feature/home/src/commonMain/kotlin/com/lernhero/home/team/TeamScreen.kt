package com.lernhero.home.team

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lernhero.domain.monster.MonsterCard
import com.lernhero.domain.monster.MonsterLevels
import com.lernhero.domain.monster.MonsterStars
import com.lernhero.domain.quiz.QuizCategory
import com.lernhero.domain.quiz.QuizDifficulty
import com.lernhero.domain.run.BattleKind
import com.lernhero.domain.run.BattleRequest
import com.lernhero.domain.run.ChestSlot
import com.lernhero.domain.run.ChestWheel
import com.lernhero.domain.run.RunDifficulty
import com.lernhero.domain.run.RunSession
import com.lernhero.domain.story.CampaignSelection
import com.lernhero.domain.story.IdleRewards
import com.lernhero.domain.story.StoryStageCatalog
import com.lernhero.home.city.TownViewModel
import com.lernhero.home.story.BossChestOverlay
import com.lernhero.home.story.CampaignPanel
import com.lernhero.home.story.EndlessSetupPanel
import com.lernhero.home.story.QuestionSource
import com.lernhero.home.story.StageLootDialog
import com.lernhero.home.story.resolveChestPrize
import com.lernhero.game.character.KnightEquipment
import com.lernhero.game.character.ModularKnight
import com.lernhero.game.character.MonsterVisualPresets
import com.lernhero.home.player.PlayerViewModel
import com.lernhero.home.ui.MonsterCardView
import com.lernhero.home.ui.RarityPalette
import org.jetbrains.compose.resources.painterResource
import com.lernhero.shared.theme.FontFirst
import com.lernhero.shared.theme.FontPixel
import kotlin.random.Random
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import kotlin.time.Clock

private val PanelDark = Color(0xFF1A1410)
private val Gold = Color(0xFFC8A45A)
private val GoldDim = Color(0xFF6B5A38)
private val TextGold = Color(0xFFFFD100)
private val TextMuted = Color(0xFF9D8B6A)
private val SlotEmpty = Color(0xFF120E0A)
private val HeroGlow = Color(0xFF3B9DFF)

/**
 * Ekran zakladki "Walcz" - jedno miejsce na wszystko, co poprzedza walke:
 *
 * - u gory sklad druzyny (bohater w pierwszym, zablokowanym slocie, obok kompani wybierani
 *   kliknieciem - bez przeciagania),
 * - pod nim przelacznik KAMPANIA / ENDLESS,
 * - a pod nim wlasciwy panel: [com.lernhero.home.story.CampaignPanel] (tlo krainy, przeciwnicy,
 *   nagrody idle z GET, przyciski MAPA/BATTLE/LOOT i rzad plansz) albo
 *   [com.lernhero.home.story.EndlessSetupPanel] (poziom pytan + kategoria + miejsce na wlasne talie).
 *
 * Wybor KRAINY i tieru trudnosci nie dzieje sie tutaj - do tego jest osobny ekran Mapy
 * ([com.lernhero.home.story.WorldMapScreen]), a wybor konkretnej planszy to rzad na dole panelu.
 */
@Composable
fun TeamScreen(
    viewModel: PlayerViewModel,
    onOpenMap: () -> Unit,
    onEnterBattle: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    val runSession = koinInject<RunSession>()
    val selection = koinInject<CampaignSelection>()
    val townViewModel = koinViewModel<TownViewModel>()

    var monsterPickerOpen by remember { mutableStateOf(false) }
    var mode by rememberSaveable { mutableStateOf<String>(PlayMode.Campaign.name) }
    val hasFreeSlot = state.partyMonsters.any { it == null }

    // Ustawienia endless - zyja miedzy wejsciami w zakladke, zeby nie trzeba bylo ich klikac
    // od nowa po kazdej walce.
    var endlessSource by rememberSaveable { mutableStateOf<String>(QuestionSource.Builtin.name) }
    var endlessDifficulty by rememberSaveable { mutableStateOf<String?>(null) }
    var endlessCategory by rememberSaveable { mutableStateOf<String?>(null) }

    val cleared = state.storyProgress
    val world = selection.resolveWorld(cleared)
    val stageIndex = selection.resolveStage(cleared)
    val stage = StoryStageCatalog.stageAt(stageIndex, selection.tier)

    var lootOpen by remember { mutableStateOf(false) }

    // Plansza wyslana do walki - zaliczamy ja dopiero PO powrocie ze zwyciestwem, nie w chwili
    // kliknieia BATTLE (przegrana nie moze odblokowywac kolejnej planszy).
    var pendingStage by rememberSaveable { mutableStateOf<Int?>(null) }
    var pendingTier by rememberSaveable { mutableStateOf<String?>(null) }
    var chestStage by rememberSaveable { mutableStateOf<Int?>(null) }
    var chestSlotName by rememberSaveable { mutableStateOf<String?>(null) }

    // Nagrody idle licza sie z roznicy czasu - jedno odczytanie zegara przy wejsciu wystarczy,
    // bo panel i tak odswiezy sie przy nastepnym wejsciu w zakladke.
    val now = remember { Clock.System.now().toEpochMilliseconds() }
    val bestCleared = cleared.values.maxOrNull() ?: 0
    val idleReward = remember(bestCleared, state.idleCollectedAt, now) {
        IdleRewards.pendingFor(bestCleared, state.idleCollectedAt, now)
    }
    val idleFraction = remember(state.idleCollectedAt, now) {
        IdleRewards.storageFraction(state.idleCollectedAt, now)
    }

    LaunchedEffect(Unit) {
        val index = pendingStage
        val tierName = pendingTier
        pendingStage = null
        pendingTier = null
        if (index == null || tierName == null) return@LaunchedEffect
        if (runSession.lastBattleWon != true) return@LaunchedEffect

        val tier = RunDifficulty.valueOf(tierName)
        viewModel.clearStoryStage(tier, index)
        // Po wygranej przesuwamy sie na kolejna plansze zamiast tkwic na zdobytej.
        selection.clearStage()

        if (StoryStageCatalog.stageAt(index, tier)?.isBoss == true) {
            chestStage = index
            chestSlotName = ChestWheel.roll(Random.Default, index).name
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        TeamPanel(
            equipment = state.knightEquipment,
            companions = state.partyMonsters,
            levelOf = state::levelOf,
            starsOf = state::starsOf,
            onAddCompanion = { monsterPickerOpen = true },
            onRemoveCompanion = viewModel::removeFromParty
        )

        ModeTabs(selected = PlayMode.valueOf(mode), onSelect = { mode = it.name })

        when (PlayMode.valueOf(mode)) {
            PlayMode.Campaign -> {
                if (stage == null) {
                    Text(
                        text = "Kampania ukonczona na tym poziomie trudnosci. Wybierz wyzszy w Mapie.",
                        color = TextMuted,
                        fontFamily = FontFirst(),
                        fontSize = 12.sp
                    )
                } else {
                    CampaignPanel(
                        world = world,
                        stage = stage,
                        cleared = cleared,
                        idleReward = idleReward,
                        idleStorageFraction = idleFraction,
                        onSelectStage = selection::selectStage,
                        onOpenMap = onOpenMap,
                        onOpenLoot = { lootOpen = true },
                        onCollectIdle = {
                            viewModel.collectIdleRewards(Clock.System.now().toEpochMilliseconds())
                        },
                        onBattle = {
                            runSession.startNewRun(
                                category = null,
                                difficulty = stage.tier,
                                storyStage = stage.globalIndex
                            )
                            // Kazda plansza startuje z pelnym HP - nie ma juz korytarza, ktory
                            // przenosilby rany miedzy walkami.
                            runSession.healParty()
                            runSession.pendingBattle = BattleRequest(
                                kind = stage.kind,
                                waves = stage.waves,
                                seed = stage.seed,
                                isEndless = false
                            )
                            pendingStage = stage.globalIndex
                            pendingTier = stage.tier.name
                            onEnterBattle()
                        }
                    )
                }
            }

            PlayMode.Endless -> EndlessSetupPanel(
                source = QuestionSource.valueOf(endlessSource),
                difficulty = endlessDifficulty?.let { QuizDifficulty.valueOf(it) },
                category = endlessCategory?.let { QuizCategory.valueOf(it) },
                onSourceChange = { endlessSource = it.name },
                onDifficultyChange = { endlessDifficulty = it?.name },
                onCategoryChange = { endlessCategory = it?.name },
                onStart = {
                    runSession.startNewRun(
                        category = endlessCategory?.let { QuizCategory.valueOf(it) },
                        difficulty = RunDifficulty.Normal,
                        quizDifficulty = endlessDifficulty?.let { QuizDifficulty.valueOf(it) }
                    )
                    runSession.pendingBattle = BattleRequest(
                        kind = BattleKind.Normal,
                        waves = 0,
                        seed = Random.nextLong(),
                        isEndless = true
                    )
                    onEnterBattle()
                }
            )
        }

        state.message?.let { message ->
            Text(
                text = message,
                color = TextGold,
                fontFamily = FontFirst(),
                fontSize = 11.sp,
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(PanelDark.copy(alpha = 0.9f))
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            )
        }
    }

    if (monsterPickerOpen) {
        MonsterPickerDialog(
            available = state.companionInventory,
            levelOf = state::levelOf,
            starsOf = state::starsOf,
            hasFreeSlot = hasFreeSlot,
            onDismiss = { monsterPickerOpen = false },
            onPick = { card ->
                val free = state.partyMonsters.indexOfFirst { it == null }
                if (free >= 0) viewModel.placeInParty(card.id, free)
                monsterPickerOpen = false
            }
        )
    }

    if (lootOpen && stage != null) {
        StageLootDialog(stage = stage, onDismiss = { lootOpen = false })
    }

    chestSlotName?.let { slotName ->
        val slot = ChestSlot.valueOf(slotName)
        val index = chestStage ?: 1
        val prize = remember(slotName, index) { resolveChestPrize(slot, Random(index.toLong() * 7 + 1)) }
        BossChestOverlay(
            dungeonLevel = index,
            prize = slot,
            prizeLabel = prize.label,
            onClaim = {
                prize.grant(viewModel, townViewModel)
                chestSlotName = null
                chestStage = null
            }
        )
    }
}

/** Kampania albo endless - dwa zupelnie rozne powody, zeby wejsc do walki. */
private enum class PlayMode(val label: String) {
    Campaign("KAMPANIA"),
    Endless("ENDLESS")
}

@Composable
private fun ModeTabs(selected: PlayMode, onSelect: (PlayMode) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .border(2.dp, GoldDim.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        PlayMode.entries.forEach { entry ->
            val isSelected = entry == selected
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(7.dp))
                    .background(if (isSelected) TextGold.copy(alpha = 0.22f) else Color.Transparent)
                    .border(
                        1.5.dp,
                        if (isSelected) TextGold else Color.Transparent,
                        RoundedCornerShape(7.dp)
                    )
                    .clickable { onSelect(entry) }
                    .padding(vertical = 9.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = entry.label,
                    color = if (isSelected) TextGold else TextMuted,
                    fontFamily = FontPixel(),
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
            }
        }
    }
}

@Composable
private fun TeamPanel(
    equipment: KnightEquipment,
    companions: List<MonsterCard?>,
    levelOf: (String) -> Int,
    starsOf: (String) -> Int,
    onAddCompanion: () -> Unit,
    onRemoveCompanion: (Int) -> Unit
) {
    // Przeswitujaca ramka - tlo apki widoczne wokol slotow, ktore maja wlasne, solidne tla.
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .border(2.dp, GoldDim.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "YOUR TEAM",
                color = TextGold,
                fontFamily = FontPixel(),
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                modifier = Modifier.weight(1f).padding(start = 10.dp, top = 8.dp, bottom = 8.dp)
            )
            Box(
                modifier = Modifier
                    .padding(end = 8.dp)
                    .size(36.dp)
                    .background(SlotEmpty)
                    .border(1.5.dp, GoldDim)
                    .clickable(onClick = onAddCompanion),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "+", color = TextGold, fontFamily = FontPixel(), fontSize = 18.sp)
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            HeroSlotCard(equipment = equipment, modifier = Modifier.weight(1f).height(78.dp))
            companions.forEachIndexed { index, card ->
                CompanionSlotCard(
                    card = card,
                    level = card?.let { levelOf(it.id) } ?: 1,
                    stars = card?.let { starsOf(it.id) } ?: 1,
                    onClick = {
                        if (card == null) onAddCompanion() else onRemoveCompanion(index)
                    },
                    modifier = Modifier.weight(1f).height(78.dp)
                )
            }
        }
    }
}

@Composable
private fun HeroSlotCard(
    equipment: KnightEquipment,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(
                Brush.verticalGradient(
                    listOf(HeroGlow.copy(alpha = 0.22f), Color(0xFF0E0A08))
                )
            )
            .border(2.dp, HeroGlow, RoundedCornerShape(10.dp)),
        contentAlignment = Alignment.BottomCenter
    ) {
        Box(modifier = Modifier.fillMaxWidth(0.94f)) {
            ModularKnight(
                modifier = Modifier.align(Alignment.BottomCenter),
                equipment = equipment
            )
        }
        Text(
            text = "🔒",
            fontSize = 10.sp,
            modifier = Modifier.align(Alignment.TopEnd).padding(5.dp)
        )
    }
}

@Composable
private fun CompanionSlotCard(
    card: MonsterCard?,
    level: Int,
    stars: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .then(
                if (card == null) {
                    Modifier
                        .background(SlotEmpty)
                        .border(1.5.dp, GoldDim.copy(alpha = 0.7f), RoundedCornerShape(10.dp))
                } else {
                    Modifier
                }
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        if (card == null) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(text = "＋", color = GoldDim, fontSize = 26.sp)
                Text(
                    text = "Wolne\nmiejsce",
                    color = TextMuted,
                    fontFamily = FontFirst(),
                    fontSize = 9.sp,
                    textAlign = TextAlign.Center
                )
            }
        } else {
            MonsterCardView(
                card = card,
                level = level,
                stars = stars,
                portraitHeight = 72,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

/**
 * Wybor kompana - wlasny panel zamiast Material [AlertDialog]: dawny uzywal go z siatka pelnych
 * kart (108dp, portret 84dp) i na telefonie AlertDialog dawal tylko dwie kolumny, wiec kazda
 * karta wychodzila ogromna, a mimo to nieczytelna.
 *
 * Duplikaty tej samej karty (gracz moze miec kilka kopii - patrz [MonsterUiState.companionInventory])
 * grupuja sie w JEDNA kafle z plakietka "x2": to jedna postac, tylko posiadana wielokrotnie, wiec
 * pokazywanie jej jako dwoch identycznych, pelnowymiarowych kart bylo czystym marnowaniem miejsca.
 * Kazde tapniecie zabiera jedna sztuke ze stosu - reszta zostaje do kolejnego slotu albo do
 * sprzedania w Kolekcji (Summon Center), gdzie duplikaty zamieniaja sie w zloto.
 */
@Composable
private fun MonsterPickerDialog(
    available: List<MonsterCard>,
    levelOf: (String) -> Int,
    starsOf: (String) -> Int,
    hasFreeSlot: Boolean,
    onDismiss: () -> Unit,
    onPick: (MonsterCard) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xCC07050B))
            .clickable(onClick = onDismiss),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 380.dp)
                .padding(20.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(PanelDark)
                .border(2.dp, Gold, RoundedCornerShape(16.dp))
                .clickable(enabled = false) {}
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "WYBIERZ KOMPANA",
                color = TextGold,
                fontFamily = FontPixel(),
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp
            )

            when {
                !hasFreeSlot -> Text(
                    text = "Wszystkie miejsca zajete. Kliknij kompana w druzynie, zeby go odeslac.",
                    color = TextMuted,
                    fontFamily = FontFirst(),
                    fontSize = 12.sp
                )

                available.isEmpty() -> Text(
                    text = "Nie masz wolnych potworow. Przyzwij ich w Summon Center.",
                    color = TextMuted,
                    fontFamily = FontFirst(),
                    fontSize = 12.sp
                )

                else -> {
                    MonsterPickerGrid(
                        available = available,
                        levelOf = levelOf,
                        starsOf = starsOf,
                        onPick = onPick
                    )
                    Text(
                        text = "Masz kilka tej samej karty? Zapasowe sprzedasz w Kolekcji (Summon Center) za zloto.",
                        color = TextMuted,
                        fontFamily = FontFirst(),
                        fontSize = 10.sp
                    )
                }
            }

            Text(
                text = "ZAMKNIJ",
                color = TextGold,
                fontFamily = FontPixel(),
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                modifier = Modifier
                    .align(Alignment.End)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF3A2A18))
                    .border(1.5.dp, GoldDim, RoundedCornerShape(8.dp))
                    .clickable(onClick = onDismiss)
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            )
        }
    }
}

/** Jedna karta (id) moze byc posiadana kilka razy - grupujemy je do jednego kafla z licznikiem. */
private data class StackedMonster(val card: MonsterCard, val count: Int)

@Composable
private fun MonsterPickerGrid(
    available: List<MonsterCard>,
    levelOf: (String) -> Int,
    starsOf: (String) -> Int,
    onPick: (MonsterCard) -> Unit
) {
    val stacks = remember(available) {
        available.groupingBy { it.id }.eachCount()
            .mapNotNull { (id, count) -> available.first { it.id == id }.let { StackedMonster(it, count) } }
    }

    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        stacks.forEach { stack ->
            CompanionPickTile(
                stack = stack,
                level = levelOf(stack.card.id),
                stars = starsOf(stack.card.id),
                onClick = { onPick(stack.card) }
            )
        }
    }
}

/**
 * Kompaktowa kafelka pickera - portret wyraznie mniejszy niz w [com.lernhero.home.ui.MonsterCardView]
 * (48dp zamiast 84dp), bo tu chodzi o szybki wybor z rzedu wielu opcji, nie o podziwianie karty.
 */
@Composable
private fun CompanionPickTile(
    stack: StackedMonster,
    level: Int,
    stars: Int,
    onClick: () -> Unit
) {
    val frame = RarityPalette.frame(stack.card.rarity)

    Column(
        modifier = Modifier
            .width(78.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(RarityPalette.cardBackground(stack.card.rarity))
            .border(1.5.dp, frame, RoundedCornerShape(10.dp))
            .clickable(onClick = onClick)
            .padding(6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xFF17120D)),
            contentAlignment = Alignment.BottomCenter
        ) {
            Image(
                painter = painterResource(MonsterVisualPresets.forSpecies(stack.card.spriteKey).drawable),
                contentDescription = null,
                contentScale = ContentScale.Fit,
                modifier = Modifier.fillMaxWidth(0.75f).padding(bottom = 2.dp)
            )
            if (level > MonsterLevels.MIN_LEVEL) {
                Text(
                    text = "LV$level",
                    color = TextGold,
                    fontFamily = FontPixel(),
                    fontWeight = FontWeight.Bold,
                    fontSize = 8.sp,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(2.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(Color(0xFF17120D).copy(alpha = 0.85f))
                        .padding(horizontal = 3.dp, vertical = 1.dp)
                )
            }
            if (stack.count > 1) {
                Text(
                    text = "x${stack.count}",
                    color = Color.White,
                    fontFamily = FontPixel(),
                    fontWeight = FontWeight.Bold,
                    fontSize = 9.sp,
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(2.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(Color(0xFFB4451F))
                        .padding(horizontal = 4.dp, vertical = 1.dp)
                )
            }
        }

        Text(
            text = stack.card.name,
            color = TextGold,
            fontFamily = FontFirst(),
            fontWeight = FontWeight.Bold,
            fontSize = 10.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Row(horizontalArrangement = Arrangement.spacedBy(1.dp)) {
            (1..MonsterStars.MAX_STARS).forEach { index ->
                Text(
                    text = "★",
                    color = if (index <= stars) TextGold else Color(0xFF3A2E22),
                    fontSize = 7.sp
                )
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = "${MonsterStars.hpAt(stack.card, stars, level)}",
                color = Color(0xFFEF4444),
                fontFamily = FontFirst(),
                fontWeight = FontWeight.Bold,
                fontSize = 9.sp
            )
            Text(
                text = "${MonsterStars.attackAt(stack.card, stars, level)}",
                color = Color(0xFFF97316),
                fontFamily = FontFirst(),
                fontWeight = FontWeight.Bold,
                fontSize = 9.sp
            )
        }
    }
}

