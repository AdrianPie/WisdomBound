package com.lernhero.home.dungeon

import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lernhero.domain.dungeon.DungeonMap
import com.lernhero.domain.dungeon.DungeonMapGenerator
import com.lernhero.domain.dungeon.DungeonNode
import com.lernhero.shared.FontFirst
import com.lernhero.shared.FontPixel
import kotlin.random.Random
import org.jetbrains.compose.resources.painterResource

private val MapTop = Color(0xFF1B2340)
private val MapBottom = Color(0xFF0B0E1A)
private val PathLocked = Color(0xFF39406B)
private val PathWalked = Color(0xFFC8A45A)
private val TextGold = Color(0xFFFFD100)
private val TextMuted = Color(0xFF8E93B5)
private val NodeLocked = Color(0xFF1C2138)

private val NodeSize = 48.dp
private val FloorHeight = 98.dp
private val MapPadding = 28.dp

/**
 * Stan jednego zejscia do lochu. Cala mapa jest odtwarzana z seeda, wiec do przetrwania
 * obrotu ekranu albo wyjscia do walki wystarczy zapisac trzy rzeczy: seed, gdzie stoimy
 * i ktore pola juz zaliczylismy.
 */
class DungeonRunState(
    val seed: Long,
    currentNodeId: String? = null,
    visitedIds: List<String> = emptyList()
) {
    val map: DungeonMap = DungeonMapGenerator.generate(seed)

    var currentNodeId: String? by mutableStateOf(currentNodeId)
        private set

    private val visited = mutableStateListOf<String>().apply { addAll(visitedIds) }

    /** Pola, na ktore mozna teraz wejsc. Na starcie - cale dolne pietro. */
    val available: List<DungeonNode> get() = map.reachableFrom(currentNodeId)

    fun isVisited(node: DungeonNode): Boolean = node.id in visited

    fun isAvailable(node: DungeonNode): Boolean = available.any { it.id == node.id }

    fun enter(node: DungeonNode) {
        if (!isAvailable(node)) return
        currentNodeId = node.id
        if (node.id !in visited) visited += node.id
    }

    companion object {
        val Saver = listSaver<DungeonRunState, String>(
            save = { state ->
                listOf(
                    state.seed.toString(),
                    state.currentNodeId.orEmpty(),
                    state.visited.joinToString(",")
                )
            },
            restore = { saved ->
                DungeonRunState(
                    seed = saved[0].toLongOrNull() ?: 0L,
                    currentNodeId = saved[1].ifBlank { null },
                    visitedIds = saved[2].split(",").filter { it.isNotBlank() }
                )
            }
        )
    }
}

@Composable
fun DungeonMapScreen(
    seed: Long,
    onEnterFight: () -> Unit,
    onAbandon: () -> Unit
) {
    val run = rememberSaveable(seed, saver = DungeonRunState.Saver) { DungeonRunState(seed) }
    var pendingNode by remember { mutableStateOf<DungeonNode?>(null) }
    val scrollState = rememberScrollState()

    // Wchodzimy do lochu od dolu - tam stoi bohater, tam sa pierwsze wybory.
    LaunchedEffect(Unit) { scrollState.scrollTo(scrollState.maxValue) }

    Surface(modifier = Modifier.fillMaxSize(), color = MapBottom) {
        Column(modifier = Modifier.fillMaxSize()) {
            DungeonHeader(
                floor = run.currentNodeId?.let { run.map.node(it)?.floor?.plus(1) } ?: 0,
                floors = run.map.floors,
                seed = seed,
                onAbandon = onAbandon
            )

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Brush.verticalGradient(listOf(MapTop, MapBottom)))
                    .verticalScroll(scrollState)
            ) {
                DungeonMapCanvas(
                    run = run,
                    onNodeClick = { node -> pendingNode = node }
                )
            }
        }
    }

    pendingNode?.let { node ->
        NodeConfirmDialog(
            node = node,
            onDismiss = { pendingNode = null },
            onConfirm = {
                run.enter(node)
                pendingNode = null
                if (node.type.isCombat) onEnterFight()
            }
        )
    }
}

@Composable
private fun DungeonHeader(
    floor: Int,
    floors: Int,
    seed: Long,
    onAbandon: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MapBottom)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = "Loch",
                color = TextGold,
                fontFamily = FontPixel(),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Pietro $floor / $floors  -  seed $seed",
                color = TextMuted,
                fontFamily = FontFirst(),
                fontSize = 11.sp
            )
        }
        TextButton(onClick = onAbandon) {
            Text(text = "Porzuc", color = TextMuted, fontFamily = FontFirst())
        }
    }
}

/**
 * Rysuje graf: najpierw krawedzie na [Canvas], potem pola jako klikalne kafelki nad nim.
 * Pietro 0 jest na dole, boss na gorze - dlatego wspolrzedna Y jest odwracana.
 */
@Composable
private fun DungeonMapCanvas(
    run: DungeonRunState,
    onNodeClick: (DungeonNode) -> Unit
) {
    val map = run.map
    val density = LocalDensity.current

    BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
        val mapHeight = FloorHeight * map.floors
        val columnWidth = (maxWidth - MapPadding * 2) / map.columns

        /** Lekki, ale powtarzalny rozrzut pol - bez niego mapa wyglada jak arkusz kalkulacyjny. */
        fun jitter(node: DungeonNode): Pair<Dp, Dp> {
            val random = Random(map.seed * 31 + node.id.hashCode())
            return random.nextInt(-9, 10).dp to random.nextInt(-7, 8).dp
        }

        fun centerOf(node: DungeonNode): Pair<Dp, Dp> {
            val (jx, jy) = jitter(node)
            val x = MapPadding + columnWidth * (node.column + 0.5f) + jx
            val y = mapHeight - FloorHeight * (node.floor + 0.5f) + jy
            return x to y
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(mapHeight)
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                map.nodes.forEach { node ->
                    val (fromX, fromY) = centerOf(node)
                    val start = with(density) { Offset(fromX.toPx(), fromY.toPx()) }
                    val walked = run.isVisited(node)

                    node.next.mapNotNull { map.node(it) }.forEach { next ->
                        val (toX, toY) = centerOf(next)
                        val end = with(density) { Offset(toX.toPx(), toY.toPx()) }

                        // Sciezka juz przebyta swieci na zloto; reszta zostaje przygaszona.
                        val lit = walked && run.isVisited(next)
                        drawLine(
                            color = if (lit) PathWalked else PathLocked,
                            start = start,
                            end = end,
                            strokeWidth = with(density) { (if (lit) 3.dp else 2.dp).toPx() },
                            cap = StrokeCap.Round,
                            pathEffect = if (lit) null else PathEffect.dashPathEffect(
                                floatArrayOf(
                                    with(density) { 7.dp.toPx() },
                                    with(density) { 7.dp.toPx() }
                                )
                            )
                        )
                    }
                }
            }

            map.nodes.forEach { node ->
                val (x, y) = centerOf(node)
                DungeonNodeView(
                    node = node,
                    visited = run.isVisited(node),
                    available = run.isAvailable(node),
                    isCurrent = run.currentNodeId == node.id,
                    onClick = { onNodeClick(node) },
                    modifier = Modifier.offset(x = x - NodeSize / 2, y = y - NodeSize / 2)
                )
            }
        }
    }
}

@Composable
private fun DungeonNodeView(
    node: DungeonNode,
    visited: Boolean,
    available: Boolean,
    isCurrent: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val art = node.type.art()
    val active = available || visited

    Box(
        modifier = modifier
            .size(NodeSize)
            .clip(CircleShape)
            .background(if (active) art.tint.copy(alpha = if (available) 0.30f else 0.16f) else NodeLocked)
            .border(
                width = if (available) 2.5.dp else 1.5.dp,
                color = when {
                    isCurrent -> TextGold
                    available -> art.tint
                    visited -> art.tint.copy(alpha = 0.5f)
                    else -> PathLocked
                },
                shape = CircleShape
            )
            .then(if (available) Modifier.clickable(onClick = onClick) else Modifier),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            painter = painterResource(art.icon),
            contentDescription = art.label,
            modifier = Modifier.size(22.dp),
            tint = when {
                available -> Color.White
                visited -> Color.White.copy(alpha = 0.65f)
                else -> PathLocked
            }
        )
    }
}

@Composable
private fun NodeConfirmDialog(
    node: DungeonNode,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    val art = node.type.art()

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF1A1410),
        title = {
            Text(
                text = art.label,
                color = TextGold,
                fontFamily = FontPixel(),
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Text(
                text = if (node.type.isCombat) {
                    "Wchodzisz na pietro ${node.floor + 1}. Twoja druzyna staje do walki."
                } else {
                    "Wchodzisz na pietro ${node.floor + 1}. Zawartosc tego pola dopiero powstaje."
                },
                color = TextMuted,
                fontFamily = FontFirst(),
                style = MaterialTheme.typography.bodyMedium
            )
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(text = "Idziemy", color = TextGold, fontFamily = FontFirst())
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = "Jeszcze nie", color = TextMuted, fontFamily = FontFirst())
            }
        }
    )
}
