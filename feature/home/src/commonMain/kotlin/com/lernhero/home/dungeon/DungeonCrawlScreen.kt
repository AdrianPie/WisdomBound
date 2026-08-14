package com.lernhero.home.dungeon

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.EaseInOutSine
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lernhero.domain.dungeon.DungeonMap
import com.lernhero.domain.dungeon.DungeonMapGenerator
import com.lernhero.domain.dungeon.DungeonNode
import com.lernhero.domain.dungeon.DungeonNodeType
import com.lernhero.domain.dungeon.DungeonConfig
import com.lernhero.domain.monster.MonsterCard
import com.lernhero.domain.quiz.QuizBank
import com.lernhero.domain.quiz.QuizDifficulty
import com.lernhero.domain.quiz.QuizEntry
import com.lernhero.domain.run.BattleKind
import com.lernhero.domain.run.RunSession
import com.lernhero.game.character.KnightEquipment
import com.lernhero.game.character.ModularKnight
import com.lernhero.game.character.MonsterSprites
import com.lernhero.game.character.SpriteCharacter
import com.lernhero.home.player.PlayerViewModel
import com.lernhero.shared.resources.Resources
import com.lernhero.shared.theme.FontFirst
import com.lernhero.shared.theme.FontPixel
import kotlin.random.Random
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.delay
import org.jetbrains.compose.resources.imageResource
import org.jetbrains.compose.resources.painterResource
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

private val PanelDark = Color(0xFF1A1410)
private val GoldDim = Color(0xFF6B5A38)
private val TextGold = Color(0xFFFFD100)
private val TextMuted = Color(0xFF8E93B5)
private val PathLocked = Color(0xFF39406B)
private val PathWalked = Color(0xFFC8A45A)
private val NodeLocked = Color(0xFF1C2138)

/** "6 waznych miejsc": start (Enemy) + 5 pol do przejscia + boss. */
private val CRAWL_CONFIG = DungeonConfig(
    floors = 7,
    columns = 3,
    paths = 3,
    minEliteFloor = 3,
    minShopFloor = 2,
    minRestFloor = 2
)

/** Losowe zdarzenia w drodze - na razie tylko tekst-placeholder, prawdziwa tresc to kolejny krok. */
private val CORRIDOR_EVENTS = listOf(
    "Cos poruszylo sie w cieniach...",
    "Znajdujecie slady po czyms duzym.",
    "Wilgotny wiatr niesie odgłosy z oddali.",
    "Ktos zostawil tu znak ostrzegawczy."
)

/** Ile trwa przejscie jednym odcinkiem korytarza. */
private const val WALK_DURATION_MS = 10_000L

/** Jak daleko za prawa krawedzia zaczyna sie skrzynia, zanim wjedzie na swoje miejsce. */
private val CHEST_SLIDE_DISTANCE = 260.dp

/** TESTOWO: skrzynia pojawia sie zawsze, zeby latwo przetestowac cala petle otwierania. */
private const val TREASURE_CHANCE = 1f
private const val EVENT_CHANCE = 0f

/** Placeholder nagrody za otwarcie skrzyni - realny balans to kolejny krok. */
private const val TREASURE_BASE_REWARD = 10
private const val TREASURE_REWARD_PER_CORRECT = 5
private const val TREASURE_QUESTIONS = 3

/** Kim jest podroz: stoimy w miejscu, idziemy do celu, albo stoimy juz przed drzwiami walki. */
private sealed class TravelPhase {
    data object Idle : TravelPhase()
    data class Walking(val target: DungeonNode) : TravelPhase()
    data class AtDoor(val target: DungeonNode) : TravelPhase()
}

/** Mini-quiz otwierajacy napotkana w korytarzu skrzynie - [TREASURE_QUESTIONS] pytan pod rzad. */
private data class TreasureQuizState(
    val questions: List<QuizEntry>,
    val index: Int = 0,
    val correctCount: Int = 0,
    val selectedAnswer: String? = null,
    val revealed: Boolean = false
) {
    val question: QuizEntry get() = questions[index]
    val isLast: Boolean get() = index == questions.lastIndex
}

private fun DungeonNodeType.battleKind(): BattleKind? = when (this) {
    DungeonNodeType.Enemy -> BattleKind.Normal
    DungeonNodeType.Elite -> BattleKind.Elite
    DungeonNodeType.Boss -> BattleKind.Boss
    else -> null
}

/**
 * Wnetrze wybranego lochu: gora to faktyczny, przewijany korytarz ([CorridorWalkView]) - klikniecie
 * dostepnego wezla na kompaktowej mapie na dole ([DungeonNodeMapPanel]) rusza druzyne w te strone,
 * tlo scrolluje sie w lewo (wrazenie chodzenia w prawo, jak w bijatyce 2D), po drodze moze wypasc
 * losowe zdarzenie, a na miejscu walki trzeba jeszcze kliknac drzwi, zeby faktycznie wejsc.
 *
 * Mapa jest generowana z [RunSession.pendingDungeonSeed] (ustawionego przez mape krainy), wiec
 * ten sam wybor lokacji zawsze odtwarza ten sam uklad wezlow.
 */
@Composable
fun DungeonCrawlScreen(
    onFinished: () -> Unit,
    onEnterCombat: (BattleKind) -> Unit
) {
    val runSession = koinInject<RunSession>()
    val playerViewModel = koinViewModel<PlayerViewModel>()
    val playerState by playerViewModel.uiState.collectAsState()

    // Seed jest jedynym zrodlem prawdy dla ukladu mapy - remember{} (bez Saveable) wystarczy, bo
    // ten sam seed zawsze odtwarza identyczny graf, wiec regeneracja po powrocie z walki jest
    // nieszkodliwa. currentNodeId/visited to jednak PRAWDZIWY postep gracza w tym grafie - musza
    // przezyc nawigacje do BattleScreen i z powrotem (Compose kasuje zwykly remember{}, gdy ekran
    // znika z kompozycji - a znika dokladnie wtedy, gdy wchodzimy do walki), stad rememberSaveable.
    val map = remember { DungeonMapGenerator.generate(runSession.pendingDungeonSeed ?: Random.nextLong(), CRAWL_CONFIG) }
    var currentNodeId by rememberSaveable { mutableStateOf<String?>(null) }
    var visitedCsv by rememberSaveable { mutableStateOf("") }
    val visited = remember(visitedCsv) { visitedCsv.split(",").filter(String::isNotBlank).toSet() }
    fun markVisited(id: String) {
        if (id !in visited) visitedCsv = if (visitedCsv.isEmpty()) id else "$visitedCsv,$id"
    }
    var travelPhase by remember { mutableStateOf<TravelPhase>(TravelPhase.Idle) }
    var eventMessage by remember { mutableStateOf<String?>(null) }
    var treasureQuiz by remember { mutableStateOf<TreasureQuizState?>(null) }
    var treasureDone by remember { mutableStateOf<CompletableDeferred<Unit>?>(null) }

    fun isAvailable(node: DungeonNode) =
        travelPhase == TravelPhase.Idle && map.reachableFrom(currentNodeId).any { it.id == node.id }

    fun answerTreasure(answerId: String) {
        val quiz = treasureQuiz ?: return
        if (quiz.revealed) return
        val correct = answerId == quiz.question.correct
        treasureQuiz = quiz.copy(
            selectedAnswer = answerId,
            revealed = true,
            correctCount = quiz.correctCount + if (correct) 1 else 0
        )
    }

    fun continueTreasure() {
        val quiz = treasureQuiz ?: return
        if (!quiz.isLast) {
            treasureQuiz = quiz.copy(index = quiz.index + 1, selectedAnswer = null, revealed = false)
            return
        }
        val reward = TREASURE_BASE_REWARD + quiz.correctCount * TREASURE_REWARD_PER_CORRECT
        runSession.apply { it.copy(bonusMaxHp = it.bonusMaxHp + reward) }
        treasureQuiz = null
        treasureDone?.complete(Unit)
        treasureDone = null
    }

    // Cala "podroz" do klikanego wezla: przewijamy korytarz przez WALK_DURATION_MS. Po drodze albo
    // nic sie nie dzieje, albo pada krotki tekst-event, albo natrafiamy na skrzynie - wtedy korytarz
    // sie zatrzymuje (patrz isWalking w CorridorWalkView) i czekamy, az gracz przejdzie mini-quiz.
    // Na koniec albo od razu zaliczamy wezel (niebojowy), albo stajemy przed drzwiami i czekamy na
    // osobne klikniecie.
    LaunchedEffect(travelPhase) {
        val target = (travelPhase as? TravelPhase.Walking)?.target ?: return@LaunchedEffect
        val roll = Random.nextFloat()
        when {
            roll < TREASURE_CHANCE -> {
                delay(WALK_DURATION_MS / 2)
                val done = CompletableDeferred<Unit>()
                treasureDone = done
                treasureQuiz = TreasureQuizState(
                    questions = QuizBank.draw(
                        ladder = List(TREASURE_QUESTIONS) { QuizDifficulty.Easy },
                        category = null,
                        random = Random(target.id.hashCode())
                    )
                )
                done.await()
                delay(WALK_DURATION_MS / 2)
            }
            roll < TREASURE_CHANCE + EVENT_CHANCE -> {
                delay(WALK_DURATION_MS / 2)
                eventMessage = CORRIDOR_EVENTS.random()
                delay(900)
                eventMessage = null
                delay(WALK_DURATION_MS / 2)
            }
            else -> delay(WALK_DURATION_MS)
        }

        val kind = target.type.battleKind()
        if (kind != null) {
            travelPhase = TravelPhase.AtDoor(target)
        } else {
            currentNodeId = target.id
            markVisited(target.id)
            travelPhase = TravelPhase.Idle
        }
    }

    Column(modifier = Modifier.fillMaxSize().background(Color(0xFF0B0E1A))) {
        CorridorWalkView(
            isWalking = travelPhase is TravelPhase.Walking && treasureQuiz == null,
            knightEquipment = playerState.knightEquipment,
            companions = playerState.partyMonsters,
            eventMessage = eventMessage,
            doorArt = (travelPhase as? TravelPhase.AtDoor)?.target?.type?.art(),
            onDoorClick = {
                val node = (travelPhase as? TravelPhase.AtDoor)?.target ?: return@CorridorWalkView
                currentNodeId = node.id
                markVisited(node.id)
                travelPhase = TravelPhase.Idle
                node.type.battleKind()?.let(onEnterCombat)
            },
            treasureQuiz = treasureQuiz,
            onTreasureAnswer = ::answerTreasure,
            onTreasureContinue = ::continueTreasure,
            modifier = Modifier.weight(1.2f).fillMaxWidth()
        )

        DungeonNodeMapPanel(
            map = map,
            currentNodeId = currentNodeId,
            visited = visited,
            isAvailable = ::isAvailable,
            onConfirmNode = { node -> travelPhase = TravelPhase.Walking(node) },
            onFinished = onFinished,
            modifier = Modifier.weight(1f).fillMaxWidth()
        )
    }
}

/**
 * Faktyczny korytarz: tlo z [Resources.Image.corridorBrick] powtarza sie i scrolluje w lewo przy
 * [isWalking] (dokladnie ten sam kafelek, uklladany obok siebie tyle razy, ile trzeba, zeby
 * pokryc szerokosc ramki plus margines), druzyna stoi w miejscu blisko lewej krawedzi i tylko
 * bobuje (pelny cykl chodu w [ModularKnight] to osobna robota na pozniej) - razem daje to
 * wrazenie marszu w prawo, jak w bijatyce 2D. Gdy w drodze trafi sie skrzynia, korytarz sie
 * zatrzymuje ([isWalking] = false z zewnatrz) i na srodku wyskakuje [treasureQuiz].
 */
@Composable
private fun CorridorWalkView(
    isWalking: Boolean,
    knightEquipment: KnightEquipment,
    companions: List<MonsterCard?>,
    eventMessage: String?,
    doorArt: DungeonNodeArt?,
    onDoorClick: () -> Unit,
    treasureQuiz: TreasureQuizState?,
    onTreasureAnswer: (String) -> Unit,
    onTreasureContinue: () -> Unit,
    modifier: Modifier = Modifier
) {
    val cameraX = remember { Animatable(0f) }
    LaunchedEffect(isWalking) {
        if (!isWalking) return@LaunchedEffect
        while (true) {
            cameraX.animateTo(cameraX.value + 3000f, animationSpec = tween(6000, easing = LinearEasing))
        }
    }

    val brickTile = imageResource(Resources.Image.corridorBrick)
    val density = LocalDensity.current

    // Skrzynia "wjezdza" z prawej razem ze sceneria zamiast teleportowac sie na miejsce: startuje
    // przesunieta o CHEST_SLIDE_DISTANCE w prawo (poza widoczna ramke) i wjezdza do 0, tym samym
    // ruchem co scrollujace sie tlo, wiec wyglada jak czesc mijanego korytarza, nie jak popup.
    val chestSlide = remember { Animatable(1f) }
    LaunchedEffect(treasureQuiz != null) {
        if (treasureQuiz != null) {
            chestSlide.snapTo(1f)
            chestSlide.animateTo(0f, animationSpec = tween(900, easing = FastOutSlowInEasing))
        }
    }

    Box(
        modifier = modifier.background(
            Brush.verticalGradient(listOf(Color(0xFF241A2E), Color(0xFF0B0E1A)))
        )
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val tileAspect = brickTile.width.toFloat() / brickTile.height.toFloat()
            val tileHeight = size.height
            val tileWidth = tileHeight * tileAspect
            val shift = cameraX.value % tileWidth
            var x = -shift - tileWidth
            while (x < size.width + tileWidth) {
                drawImage(
                    image = brickTile,
                    srcOffset = IntOffset.Zero,
                    srcSize = IntSize(brickTile.width, brickTile.height),
                    dstOffset = IntOffset(x.toInt(), 0),
                    dstSize = IntSize(tileWidth.toInt(), tileHeight.toInt())
                )
                x += tileWidth
            }
        }

        if (treasureQuiz != null) {
            Image(
                painter = painterResource(Resources.Image.treasureChest),
                contentDescription = null,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 8.dp)
                    .graphicsLayer {
                        translationX = chestSlide.value * with(density) { CHEST_SLIDE_DISTANCE.toPx() }
                    }
                    .size(56.dp),
                contentScale = ContentScale.Fit
            )
        }

        Row(
            modifier = Modifier.align(Alignment.CenterStart).padding(start = 24.dp),
            horizontalArrangement = Arrangement.spacedBy((-8).dp),
            verticalAlignment = Alignment.Bottom
        ) {
            Bobbing(delayMillis = 0) {
                ModularKnight(sizeDp = 90.dp, interactive = false, equipment = knightEquipment)
            }
            companions.filterNotNull().forEachIndexed { index, card ->
                Bobbing(delayMillis = 120 * (index + 1)) {
                    Box(modifier = Modifier.size(64.dp)) {
                        SpriteCharacter(sprite = MonsterSprites.spritesFor(card.spriteKey).idle, isLooping = true)
                    }
                }
            }
        }

        doorArt?.let { art ->
            Column(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 24.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF0B0805).copy(alpha = 0.9f))
                    .border(2.dp, art.tint, RoundedCornerShape(12.dp))
                    .clickable(onClick = onDoorClick)
                    .padding(14.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(painter = painterResource(art.icon), contentDescription = art.label, tint = art.tint, modifier = Modifier.size(28.dp))
                Text(text = "WEJDZ", color = TextGold, fontFamily = FontPixel(), fontWeight = FontWeight.Bold, fontSize = 11.sp)
            }
        }

        AnimatedVisibility(
            visible = eventMessage != null,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.align(Alignment.TopCenter).padding(top = 12.dp)
        ) {
            Text(
                text = eventMessage.orEmpty(),
                color = TextGold,
                fontFamily = FontFirst(),
                fontSize = 12.sp,
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF0B0805).copy(alpha = 0.85f))
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            )
        }

        AnimatedVisibility(
            visible = treasureQuiz != null,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.fillMaxSize()
        ) {
            treasureQuiz?.let { quiz ->
                TreasureQuizOverlay(
                    quiz = quiz,
                    onAnswer = onTreasureAnswer,
                    onContinue = onTreasureContinue
                )
            }
        }
    }
}

/**
 * Quiz otwierajacy skrzynie: [TREASURE_QUESTIONS] pytan pod rzad na ciemnym tle nad korytarzem.
 * Poprawna odpowiedz podbija nagrode (patrz [TREASURE_REWARD_PER_CORRECT]) - placeholder na razie
 * zawsze dopisuje przynajmniej [TREASURE_BASE_REWARD] do max HP druzyny na reszte runu.
 */
@Composable
private fun TreasureQuizOverlay(
    quiz: TreasureQuizState,
    onAnswer: (String) -> Unit,
    onContinue: () -> Unit
) {
    val question = quiz.question
    val answers = remember(question.id) { question.answers.shuffled(Random(question.id.hashCode())) }

    LaunchedEffect(question.id, quiz.revealed) {
        if (!quiz.revealed) return@LaunchedEffect
        delay(700)
        onContinue()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0B0805).copy(alpha = 0.92f))
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = "SKRZYNIA - PYTANIE ${quiz.index + 1}/${TREASURE_QUESTIONS}",
                color = Color(0xFFD9B44A),
                fontFamily = FontPixel(),
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp
            )
            Text(
                text = question.prompt,
                color = TextGold,
                fontFamily = FontFirst(),
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
            answers.forEach { answer ->
                val isCorrectAnswer = answer == question.correct
                val picked = answer == quiz.selectedAnswer
                val bg = when {
                    !quiz.revealed -> Color(0xFF1A1410)
                    isCorrectAnswer -> Color(0xFF22A55E).copy(alpha = 0.35f)
                    picked -> Color(0xFFD1453D).copy(alpha = 0.35f)
                    else -> Color(0xFF1A1410)
                }
                val border = when {
                    !quiz.revealed -> GoldDim
                    isCorrectAnswer -> Color(0xFF22A55E)
                    picked -> Color(0xFFD1453D)
                    else -> GoldDim.copy(alpha = 0.4f)
                }
                Text(
                    text = answer,
                    color = TextGold,
                    fontFamily = FontFirst(),
                    fontSize = 13.sp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(bg)
                        .border(1.5.dp, border, RoundedCornerShape(8.dp))
                        .then(if (!quiz.revealed) Modifier.clickable { onAnswer(answer) } else Modifier)
                        .padding(horizontal = 12.dp, vertical = 10.dp)
                )
            }
        }
    }
}

@Composable
private fun Bobbing(delayMillis: Int, content: @Composable () -> Unit) {
    val infinite = rememberInfiniteTransition()
    val bob by infinite.animateFloat(
        initialValue = -1f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(650, delayMillis = delayMillis, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        )
    )
    Box(modifier = Modifier.graphicsLayer { translationY = bob * 5.dp.toPx() }) {
        content()
    }
}

/**
 * Kompaktowa, NIEPRZEWIJANA mapa wezlow - pietro ([DungeonNode.floor]) mapuje sie na X (postep
 * w prawo), kolumna na Y (lekki zygzak). Caly uklad zawsze miesci sie w ramce: pozycja wezla to
 * ulamek szerokosci/wysokosci, wiec nie ma tu zadnej kamery/przesuwania do popsucia.
 */
@Composable
private fun DungeonNodeMapPanel(
    map: DungeonMap,
    currentNodeId: String?,
    visited: Set<String>,
    isAvailable: (DungeonNode) -> Boolean,
    onConfirmNode: (DungeonNode) -> Unit,
    onFinished: () -> Unit,
    modifier: Modifier = Modifier
) {
    var pendingNode by remember { mutableStateOf<DungeonNode?>(null) }
    val density = LocalDensity.current

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
            .background(PanelDark)
            .border(2.dp, GoldDim, RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "LOCH",
                color = TextGold,
                fontFamily = FontPixel(),
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp
            )
            Text(
                text = "${visited.size} / ${map.nodes.size} miejsc",
                color = TextMuted,
                fontFamily = FontFirst(),
                fontSize = 11.sp
            )
        }

        BoxWithConstraints(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 6.dp)
        ) {
            val maxFloor = (map.floors - 1).coerceAtLeast(1)
            val maxColumn = (map.columns - 1).coerceAtLeast(1)
            // Duze kafelki = miejsca warte uwagi (walka, skarb, event...); miedzy nimi drobne
            // kafelki-kropki znacza sama sciezke, po ktorej idziemy - nieklikalne, czysto ozdobne.
            val nodeSize = 42.dp
            val pathTileSize = 5.dp
            val pathTileGap = 11.dp

            fun jitter(node: DungeonNode): Pair<Dp, Dp> {
                val random = Random(map.seed * 31 + node.id.hashCode())
                return random.nextInt(-6, 7).dp to random.nextInt(-8, 9).dp
            }

            /** Srodek kafelka w Dp wzgledem tej ramki - jedyne miejsce, gdzie liczy sie geometria. */
            fun centerOfDp(node: DungeonNode): Pair<Dp, Dp> {
                val (jx, jy) = jitter(node)
                val xFrac = node.floor.toFloat() / maxFloor
                val yFrac = node.column.toFloat() / maxColumn
                val x = (maxWidth - nodeSize) * xFrac + nodeSize / 2 + jx
                val y = (maxHeight - nodeSize) * yFrac + nodeSize / 2 + jy
                return x to y
            }

            fun centerOfPx(node: DungeonNode): Offset {
                val (x, y) = centerOfDp(node)
                return with(density) { Offset(x.toPx(), y.toPx()) }
            }

            Canvas(modifier = Modifier.fillMaxSize()) {
                val tileSizePx = pathTileSize.toPx()
                val tileGapPx = pathTileGap.toPx()
                map.nodes.forEach { node ->
                    val start = centerOfPx(node)
                    val nodeWalked = node.id in visited
                    node.next.mapNotNull { map.node(it) }.forEach { next ->
                        val end = centerOfPx(next)
                        val lit = nodeWalked && next.id in visited
                        val color = if (lit) PathWalked else PathLocked

                        val dx = end.x - start.x
                        val dy = end.y - start.y
                        val distance = kotlin.math.hypot(dx, dy)
                        val steps = (distance / tileGapPx).toInt().coerceAtLeast(1)
                        for (i in 1 until steps) {
                            val t = i / steps.toFloat()
                            drawRect(
                                color = color,
                                topLeft = Offset(
                                    x = start.x + dx * t - tileSizePx / 2,
                                    y = start.y + dy * t - tileSizePx / 2
                                ),
                                size = Size(tileSizePx, tileSizePx)
                            )
                        }
                    }
                }
            }

            map.nodes.forEach { node ->
                val (centerX, centerY) = centerOfDp(node)
                DungeonNodeTile(
                    node = node,
                    nodeSize = nodeSize,
                    isCurrent = currentNodeId == node.id,
                    isVisited = node.id in visited,
                    isAvailable = isAvailable(node),
                    onClick = { pendingNode = node },
                    modifier = Modifier.offset(x = centerX - nodeSize / 2, y = centerY - nodeSize / 2)
                )
            }
        }

        AnimatedVisibility(
            visible = pendingNode != null,
            enter = fadeIn() + slideInVertically(initialOffsetY = { it / 2 }),
            exit = fadeOut() + slideOutVertically(targetOffsetY = { it / 2 })
        ) {
            pendingNode?.let { node ->
                NodeConfirmBar(
                    node = node,
                    onConfirm = {
                        onConfirmNode(node)
                        pendingNode = null
                    },
                    onDismiss = { pendingNode = null }
                )
            }
        }

        val bossCleared = visited.any { map.node(it)?.type == DungeonNodeType.Boss }
        if (bossCleared) {
            Button(
                onClick = onFinished,
                modifier = Modifier.fillMaxWidth().padding(10.dp).height(48.dp),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFB4451F), contentColor = Color.White)
            ) {
                Text(text = "OPUSC LOCH", fontFamily = FontPixel(), fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun DungeonNodeTile(
    node: DungeonNode,
    nodeSize: Dp,
    isCurrent: Boolean,
    isVisited: Boolean,
    isAvailable: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val art = node.type.art()
    val active = isAvailable || isVisited

    Box(
        modifier = modifier
            .size(nodeSize)
            .clip(RoundedCornerShape(6.dp))
            .background(if (active) art.tint.copy(alpha = if (isAvailable) 0.32f else 0.18f) else NodeLocked)
            .border(
                width = if (isAvailable) 2.dp else 1.dp,
                color = when {
                    isCurrent -> TextGold
                    isAvailable -> art.tint
                    isVisited -> art.tint.copy(alpha = 0.5f)
                    else -> PathLocked
                },
                shape = RoundedCornerShape(6.dp)
            )
            .then(if (isAvailable) Modifier.clickable(onClick = onClick) else Modifier),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            painter = painterResource(art.icon),
            contentDescription = art.label,
            modifier = Modifier.size(24.dp),
            tint = when {
                isAvailable -> Color.White
                isVisited -> Color.White.copy(alpha = 0.65f)
                else -> PathLocked
            }
        )
    }
}

@Composable
private fun NodeConfirmBar(
    node: DungeonNode,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    val art = node.type.art()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 10.dp, vertical = 8.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(Color(0xFF0B0805))
            .border(1.5.dp, art.tint, RoundedCornerShape(10.dp))
            .padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = art.label, color = TextGold, fontFamily = FontFirst(), fontWeight = FontWeight.Bold, fontSize = 13.sp)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = "Wroc",
                color = TextMuted,
                fontFamily = FontFirst(),
                fontSize = 12.sp,
                modifier = Modifier.clickable(onClick = onDismiss).padding(6.dp)
            )
            Text(
                text = "IDZ",
                color = TextGold,
                fontFamily = FontFirst(),
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                modifier = Modifier.clickable(onClick = onConfirm).padding(6.dp)
            )
        }
    }
}
