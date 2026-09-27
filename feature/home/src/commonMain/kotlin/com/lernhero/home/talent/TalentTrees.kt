package com.lernhero.home.talent

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lernhero.domain.talent.TalentBlock
import com.lernhero.domain.talent.TalentCatalog
import com.lernhero.domain.talent.TalentNodeState
import com.lernhero.domain.talent.TalentRules
import com.lernhero.domain.talent.TalentTree
import com.lernhero.shared.resources.Resources
import com.lernhero.shared.theme.FontFirst
import com.lernhero.shared.theme.StrokedText
import org.jetbrains.compose.resources.painterResource

/**
 * Jedno drzewko talentow na cala szerokosc - jak okno talentow w WoW Classic. Ktore drzewko,
 * wybiera sie zakladkami nad nim.
 *
 * - Tlo drzewka: `drawable/talent_bg_<dps|tank|healer>` - rysuje je cale okno talentow ([TalentDialog]).
 * - Wezel to kwadratowa ikona `drawable/talent_<id talentu>` (np. `talent_dps_might`). Bez pliku -
 *   emoji z katalogu.
 * - Ramka mowi o stanie: szara i odbarwiona ikona = jeszcze zamkniety; zielona = mozna wziac albo
 *   jest w trakcie; zlota = wymaksowany. W prawym dolnym rogu wykupiony stopien.
 * - Strzalki to wymagania ([com.lernhero.domain.talent.Talent.requires]) - zlote, gdy wymaganie
 *   jest spelnione, szare, gdy nie.
 *
 * Wszystkie grafiki szukane sa po NAZWIE ([Resources.drawableNamed]) - wrzucenie PNG wystarczy.
 */
@Composable
internal fun TalentTreePanel(
    tree: TalentTree,
    ranks: Map<String, Int>,
    points: Int,
    onNodeClick: (TalentNodeState) -> Unit,
    modifier: Modifier = Modifier
) {
    val nodes = remember(tree, ranks, points) { TalentRules.statesOf(tree, ranks, points) }
    TreePanel(
        tree = tree,
        nodes = nodes,
        pointsInTree = TalentRules.pointsIn(ranks, tree),
        onNodeClick = onNodeClick,
        modifier = modifier.fillMaxWidth()
    )
}

@Composable
private fun TreePanel(
    tree: TalentTree,
    nodes: List<TalentNodeState>,
    pointsInTree: Int,
    onNodeClick: (TalentNodeState) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
            val cell = maxWidth / COLUMNS
            // Ikona ma staly sufit - na pelnej szerokosci kolumna jest szeroka, a ikona nie ma
            // puchnac w kafel; odstepy miedzy rzedami rosna razem z nia, jak w WoW.
            val icon = minOf(cell * 0.62f, ICON_MAX)
            val rowHeight = icon * 1.6f
            val boardHeight = rowHeight * ROWS + TOP_PAD * 2

            Box(modifier = Modifier.fillMaxWidth().height(boardHeight)) {
                fun center(tier: Int, column: Int) = Offset(
                    x = ((cell * column) + cell / 2).value,
                    y = (TOP_PAD + rowHeight * tier + rowHeight / 2).value
                )

                // Strzalki wymagan - pod ikonami.
                Canvas(modifier = Modifier.fillMaxSize()) {
                    nodes.forEach { child ->
                        val parent = child.talent.requires?.let { id -> nodes.firstOrNull { it.talent.id == id } } ?: return@forEach
                        val met = child.block != TalentBlock.ParentMissing
                        drawRequirementArrow(
                            from = center(parent.talent.tier, parent.talent.column) * density,
                            to = center(child.talent.tier, child.talent.column) * density,
                            halfIcon = (icon / 2).toPx(),
                            color = if (met) ArrowGold else ArrowGrey
                        )
                    }
                }

                nodes.forEach { node ->
                    TalentIcon(
                        node = node,
                        size = icon,
                        onClick = { onNodeClick(node) },
                        modifier = Modifier.offset(
                            x = cell * node.talent.column + (cell - icon) / 2,
                            y = TOP_PAD + rowHeight * node.talent.tier + (rowHeight - icon) / 2
                        )
                    )
                }
            }
        }

    }
}

@Composable
private fun TalentIcon(node: TalentNodeState, size: Dp, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val locked = !node.isReachable
    val frame = when {
        node.isMaxed -> FrameGold
        locked -> FrameGrey
        else -> FrameGreen
    }
    val shape = RoundedCornerShape(3.dp)
    Box(modifier = modifier.size(size)) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(shape)
                .background(Color(0xFF0B0A08))
                .border(if (node.isLearned || !locked) 2.dp else 1.dp, frame, shape)
                .padding(2.dp)
                .clip(RoundedCornerShape(2.dp))
                .clickable(onClick = onClick),
            contentAlignment = Alignment.Center
        ) {
            val art = remember(node.talent.id) { Resources.drawableNamed("talent_${node.talent.id}") }
            if (art != null) {
                Image(
                    painter = painterResource(art),
                    contentDescription = node.talent.name,
                    contentScale = ContentScale.Crop,
                    colorFilter = if (locked) Desaturate else null,
                    modifier = Modifier.fillMaxSize().alpha(if (locked) 0.6f else 1f)
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Brush.verticalGradient(listOf(Color(0xFF3A342A), Color(0xFF16130E)))),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = node.talent.icon,
                        fontSize = (size.value * 0.5f).sp,
                        lineHeight = (size.value * 0.55f).sp,
                        modifier = Modifier.alpha(if (locked) 0.35f else 1f)
                    )
                }
            }
        }
        if (node.isLearned) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .offset(x = 4.dp, y = 4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(Color(0xE6000000))
                    .border(0.5.dp, frame, RoundedCornerShape(2.dp))
                    .padding(horizontal = 2.dp)
            ) {
                StrokedText(
                    text = "${node.rank}",
                    size = (size.value * 0.26f).coerceAtLeast(8f).sp,
                    color = if (node.isMaxed) FrameGold else FrameGreen,
                    outline = Color.Black,
                    strokeWidth = 2f,
                    tight = true
                )
            }
        }
    }
}

/**
 * Strzalka "wymaga": pionowo w dol od rodzica do dziecka; gdy sa w roznych kolumnach - najpierw
 * w bok na wysokosci rodzica, potem w dol (jak w WoW).
 */
private fun DrawScope.drawRequirementArrow(from: Offset, to: Offset, halfIcon: Float, color: Color) {
    val width = 4.dp.toPx()
    val head = 7.dp.toPx()
    val start = if (from.x == to.x) Offset(from.x, from.y + halfIcon) else Offset(from.x + if (to.x > from.x) halfIcon else -halfIcon, from.y)
    val corner = Offset(to.x, from.y)
    val end = Offset(to.x, to.y - halfIcon - head * 0.6f)
    if (from.x != to.x) drawLine(color, start, corner, width)
    drawLine(color, if (from.x == to.x) start else corner, end, width)
    val tip = Offset(to.x, to.y - halfIcon)
    drawPath(
        Path().apply {
            moveTo(tip.x, tip.y)
            lineTo(tip.x - head, tip.y - head)
            lineTo(tip.x + head, tip.y - head)
            close()
        },
        color
    )
}

private operator fun Offset.times(factor: Float) = Offset(x * factor, y * factor)

internal fun TalentTree.fileKey(): String = when (this) {
    TalentTree.Dps -> "dps"
    TalentTree.Tank -> "tank"
    TalentTree.Healer -> "healer"
}

private fun TalentTree.fallbackBackground(): Brush = when (this) {
    TalentTree.Dps -> Brush.verticalGradient(listOf(Color(0xFF5A2A12), Color(0xFF2A120A)))
    TalentTree.Tank -> Brush.verticalGradient(listOf(Color(0xFF3A2A22), Color(0xFF1A1310)))
    TalentTree.Healer -> Brush.verticalGradient(listOf(Color(0xFF1E3A4A), Color(0xFF0E1A22)))
}

private val Desaturate = ColorFilter.colorMatrix(ColorMatrix().apply { setToSaturation(0f) })

private val FrameGold = Color(0xFFFFD100)
private val FrameGreen = Color(0xFF3FD23F)
private val FrameGrey = Color(0xFF5E5E5E)
private val ArrowGold = Color(0xFFE8B923)
private val ArrowGrey = Color(0xFF6B6B6B)

private const val COLUMNS = 3

/** Rzedy drzewka - o jeden wiecej niz najglebszy talent w katalogu. */
private val ROWS = TalentCatalog.all.maxOf { it.tier } + 1

private val TOP_PAD = 12.dp

/** Sufit rozmiaru ikony talentu. */
private val ICON_MAX = 62.dp
