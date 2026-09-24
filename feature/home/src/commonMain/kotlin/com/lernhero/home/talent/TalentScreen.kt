package com.lernhero.home.talent

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lernhero.domain.spell.SpellCatalog
import com.lernhero.domain.talent.Talent
import com.lernhero.domain.talent.TalentBlock
import com.lernhero.domain.talent.TalentBonuses
import com.lernhero.domain.talent.TalentCatalog
import com.lernhero.domain.talent.TalentNodeState
import com.lernhero.domain.talent.TalentRules
import com.lernhero.domain.talent.TalentTree
import com.lernhero.home.player.PlayerViewModel
import com.lernhero.home.ui.DIALOG_CLOSE_LANE
import com.lernhero.home.ui.DialogCloseButton
import com.lernhero.home.ui.DialogScrim
import com.lernhero.home.ui.OutlinedText
import com.lernhero.shared.theme.FontFirst

/**
 * Drzewko talentow - trzy sciezki, po kilkanascie wezlow kazda, odblokowywane rzedami.
 *
 * Wyglad jak w grach mobilnych: kazde drzewko ma wlasny kolor, wezly to medaliony polaczone
 * liniami (zwykly przeplyw rzad pod rzedem + zlote linie wymagan), zamkniety rzad niesie klodke
 * z postepem "3/6", a wezel, na ktory stac gracza, pulsuje zielonym plusem.
 *
 * Ekran nie liczy NICZEGO sam - o tym, co wolno wykupic i ile punktow siedzi w drzewku, mowi
 * [TalentRules]. Dzieki temu wygaszony wezel i odrzucony zapis maja dokladnie ten sam powod.
 */
@Composable
fun TalentDialog(
    viewModel: PlayerViewModel,
    onClose: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()

    var selectedTree by remember { mutableStateOf(TalentTree.Dps) }
    var inspected by remember { mutableStateOf<String?>(null) }
    var confirmReset by remember { mutableStateOf(false) }

    val points = state.talentPoints
    val nodes = remember(selectedTree, state.talentRanks, points) {
        TalentRules.statesOf(selectedTree, state.talentRanks, points)
    }
    val spent = TalentRules.totalSpent(state.talentRanks)
    val colors = selectedTree.colors()

    Box(modifier = Modifier.fillMaxSize()) {
        DialogScrim(onDismiss = onClose)

        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .widthIn(max = DIALOG_MAX_WIDTH)
                .fillMaxHeight()
                .navigationBarsPadding()
                .statusBarsPadding()
                .padding(horizontal = 12.dp, vertical = 12.dp)
                .padding(bottom = DIALOG_CLOSE_LANE)
                .clip(RoundedCornerShape(20.dp))
                .background(Brush.verticalGradient(listOf(colors.deep, Color(0xFF0C0A10))))
                .border(3.dp, Color(0xFF05040A), RoundedCornerShape(20.dp))
                .border(1.5.dp, colors.light.copy(alpha = 0.35f), RoundedCornerShape(20.dp))
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Header(points = points, spent = spent, onReset = { confirmReset = true })

            TreeTabs(selected = selectedTree, ranks = state.talentRanks, onSelect = { selectedTree = it })

            // Licznik punktow i zakladki stoja w miejscu - przewija sie samo drzewo.
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                TreeBoard(
                    tree = selectedTree,
                    nodes = nodes,
                    pointsInTree = TalentRules.pointsIn(state.talentRanks, selectedTree),
                    onNodeClick = { inspected = it.talent.id }
                )

                BonusSummary(bonuses = state.talentBonuses)

                state.message?.let {
                    Text(
                        text = it,
                        color = Color(0xFFBFB3D6),
                        fontFamily = FontFirst(),
                        fontSize = 11.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
            }
        }

        DialogCloseButton(
            onClick = onClose,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(bottom = 14.dp)
        )
    }

    inspected?.let { id ->
        // Wezel czytamy ZE SWIEZEGO stanu - po wykupieniu stopnia karta od razu pokazuje nowy stopien.
        nodes.firstOrNull { it.talent.id == id }?.let { live ->
            TalentDetailSheet(
                node = live,
                points = points,
                onLearn = { viewModel.spendTalentPoint(live.talent) },
                onDismiss = { inspected = null }
            )
        }
    }

    if (confirmReset) {
        ConfirmResetSheet(
            spent = spent,
            onConfirm = {
                viewModel.resetTalents()
                confirmReset = false
            },
            onDismiss = { confirmReset = false }
        )
    }
}

/* ------------------------------------------ Kolory ------------------------------------------ */

/** Paleta jednego drzewka: jasny akcent, srodek gradientu przyciskow i gleboki kolor tla. */
private data class TreeColors(val light: Color, val mid: Color, val dark: Color, val deep: Color)

private fun TalentTree.colors(): TreeColors = when (this) {
    TalentTree.Dps -> TreeColors(Color(0xFFFF8A5B), Color(0xFFE5533B), Color(0xFF9A2A18), Color(0xFF3A1410))
    TalentTree.Tank -> TreeColors(Color(0xFF7FC3FF), Color(0xFF3C8FE0), Color(0xFF1E4F95), Color(0xFF111F3E))
    TalentTree.Healer -> TreeColors(Color(0xFF8BE8A0), Color(0xFF4FC46A), Color(0xFF1F7A3A), Color(0xFF0F2A1A))
}

private val GoldLight = Color(0xFFFFE27A)
private val GoldDark = Color(0xFFC8901E)
private val Outline = Color(0xFF05040A)
private val LockedGrey = Color(0xFF4A4658)

/* ------------------------------------------ Naglowek ------------------------------------------ */

@Composable
private fun Header(points: Int, spent: Int, onReset: () -> Unit) {
    val pulse by rememberInfiniteTransition().animateFloat(
        0f, 1f, infiniteRepeatable(tween(900, easing = FastOutSlowInEasing), RepeatMode.Reverse)
    )
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            OutlinedText(text = "TALENTS", size = 24.sp, color = GoldLight)
            Text(
                text = "$spent points spent",
                color = Color(0xFF9D93B5),
                fontFamily = FontFirst(),
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp
            )
        }

        if (spent > 0) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Brush.verticalGradient(listOf(Color(0xFF4A4658), Color(0xFF26232F))))
                    .border(2.dp, Outline, CircleShape)
                    .clickable(onClick = onReset),
                contentAlignment = Alignment.Center
            ) {
                OutlinedText(text = "↺", size = 20.sp, color = Color.White)
            }
        }

        // Wolne punkty - swieca i pulsuja, dopoki jest co wydac.
        val has = points > 0
        Row(
            modifier = Modifier
                .graphicsLayer {
                    val s = if (has) 1f + 0.05f * pulse else 1f
                    scaleX = s
                    scaleY = s
                }
                .clip(RoundedCornerShape(14.dp))
                .background(
                    Brush.verticalGradient(
                        if (has) listOf(GoldLight, GoldDark) else listOf(Color(0xFF4A4658), Color(0xFF26232F))
                    )
                )
                .border(2.5.dp, Outline, RoundedCornerShape(14.dp))
                .padding(horizontal = 12.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "🌟", fontSize = 20.sp)
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                OutlinedText(text = "$points", size = 20.sp, color = Color.White)
                Text(
                    text = if (points == 1) "POINT" else "POINTS",
                    color = if (has) Color(0xFF3A2405) else Color(0xFF9D93B5),
                    fontFamily = FontFirst(),
                    fontWeight = FontWeight.Black,
                    fontSize = 8.sp
                )
            }
        }
    }
}

/* ------------------------------------------- Zakladki ------------------------------------------ */

/**
 * [ranks] wchodzi tu jako WARTOSC, a nie jako lambda siegajaca po stan z ViewModelu - inaczej
 * licznik przy zakladce nie odswiezalby sie po wykupieniu talentu.
 */
@Composable
private fun TreeTabs(selected: TalentTree, ranks: Map<String, Int>, onSelect: (TalentTree) -> Unit) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        TalentTree.entries.forEach { tree ->
            val active = tree == selected
            val c = tree.colors()
            Column(
                modifier = Modifier
                    .weight(1f)
                    .graphicsLayer { translationY = if (active) 0f else 3f * density }
                    .clip(RoundedCornerShape(14.dp))
                    .background(
                        Brush.verticalGradient(
                            if (active) listOf(c.light, c.mid, c.dark) else listOf(c.deep, Color(0xFF14111A))
                        )
                    )
                    .border(2.5.dp, if (active) Outline else c.dark, RoundedCornerShape(14.dp))
                    .clickable { onSelect(tree) }
                    .padding(vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(1.dp)
            ) {
                Text(text = tree.icon, fontSize = 20.sp, modifier = Modifier.alpha(if (active) 1f else 0.7f))
                OutlinedText(
                    text = tree.label,
                    size = 13.sp,
                    color = if (active) Color.White else c.light,
                    strokeWidth = if (active) 6f else 3f
                )
                Text(
                    text = "${TalentRules.pointsIn(ranks, tree)} pts",
                    color = if (active) Color.White else c.light.copy(alpha = 0.7f),
                    fontFamily = FontFirst(),
                    fontWeight = FontWeight.Black,
                    fontSize = 10.sp
                )
            }
        }
    }
}

/* -------------------------------------------- Plansza ------------------------------------------ */

/**
 * Plansza drzewka. Pozycje medalionow zbieramy przy ukladaniu ([onGloballyPositioned]) i dopiero
 * z nich rysujemy linie POD wezlami - dzieki temu polaczenia zawsze trafiaja w srodki, niezaleznie
 * od szerokosci ekranu i wysokosci podpisow.
 */
@Composable
private fun TreeBoard(
    tree: TalentTree,
    nodes: List<TalentNodeState>,
    pointsInTree: Int,
    onNodeClick: (TalentNodeState) -> Unit
) {
    val c = tree.colors()
    val rows = remember(tree) { TalentCatalog.rowsOf(tree) }
    val byId = remember(nodes) { nodes.associateBy { it.talent.id } }
    val centers = remember(tree) { mutableStateMapOf<String, Offset>() }
    var board by remember(tree) { mutableStateOf<LayoutCoordinates?>(null) }
    val links = remember(tree) { linksOf(TalentCatalog.forTree(tree)) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Brush.radialGradient(listOf(c.dark.copy(alpha = 0.55f), c.deep.copy(alpha = 0.4f), Color(0x00000000))))
            .border(2.dp, c.mid.copy(alpha = 0.35f), RoundedCornerShape(16.dp))
            .onGloballyPositioned { board = it }
            .drawBehind {
                links.forEach { link ->
                    val from = centers[link.from] ?: return@forEach
                    val to = centers[link.to] ?: return@forEach
                    val child = byId[link.to]
                    val lit = child?.isLearned == true
                    val open = child?.isReachable == true
                    val color = when {
                        lit -> if (link.requirement) GoldLight else c.light
                        open -> (if (link.requirement) GoldLight else c.mid).copy(alpha = 0.55f)
                        else -> LockedGrey.copy(alpha = 0.6f)
                    }
                    val width = (if (lit) 5f else 3.5f) * density
                    // Obrys pod linia, zeby odcinala sie od tla jak reszta grubych konturow w grze.
                    drawLine(Outline, from, to, strokeWidth = width + 3f * density, cap = StrokeCap.Round)
                    drawLine(
                        color,
                        from,
                        to,
                        strokeWidth = width,
                        cap = StrokeCap.Round,
                        pathEffect = if (link.requirement && !lit) PathEffect.dashPathEffect(floatArrayOf(10f * density, 7f * density)) else null
                    )
                }
            }
            .padding(horizontal = 8.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(
            text = tree.tagline,
            color = c.light,
            fontFamily = FontFirst(),
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp)
        )

        rows.forEachIndexed { tier, row ->
            val required = TalentRules.pointsRequiredFor(tier)
            val open = pointsInTree >= required
            if (tier > 0) TierGate(open = open, have = pointsInTree, required = required, tree = tree)

            Row(modifier = Modifier.fillMaxWidth()) {
                // Kolumny sa stale (0..2), zeby wezly z roznych rzedow staly jeden pod drugim.
                (0..2).forEach { column ->
                    val node = row.firstOrNull { it.column == column }?.let { byId[it.id] }
                    Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.TopCenter) {
                        if (node != null) {
                            TalentNode(
                                node = node,
                                colors = c,
                                onClick = { onNodeClick(node) },
                                onCenter = { coords ->
                                    val parent = board ?: return@TalentNode
                                    if (!parent.isAttached || !coords.isAttached) return@TalentNode
                                    val size = coords.size
                                    centers[node.talent.id] = parent.localPositionOf(
                                        coords,
                                        Offset(size.width / 2f, size.height / 2f)
                                    )
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

/** Polaczenie na planszy: [requirement] = zlota strzalka wymagania, inaczej zwykly przeplyw w kolumnie. */
private data class Link(val from: String, val to: String, val requirement: Boolean)

/**
 * Kazdy wezel laczy sie z najblizszym wezlem NAD nim w tej samej kolumnie (przeplyw drzewka),
 * a wezel z wymaganiem - dodatkowo ze swoim rodzicem. Gdy rodzic jest akurat wezlem nad nim,
 * zostaje jedna, zlota linia zamiast dwoch.
 */
private fun linksOf(talents: List<Talent>): List<Link> = buildList {
    talents.forEach { talent ->
        val parent = talent.requires
        val above = talents
            .filter { it.column == talent.column && it.tier < talent.tier }
            .maxByOrNull { it.tier }
        if (above != null && above.id != parent) add(Link(above.id, talent.id, requirement = false))
        if (parent != null) add(Link(parent, talent.id, requirement = true))
    }
}

/** Brama rzedu: otwarty = cienka linia w kolorze drzewka, zamkniety = klodka z postepem. */
@Composable
private fun TierGate(open: Boolean, have: Int, required: Int, tree: TalentTree) {
    val c = tree.colors()
    Box(modifier = Modifier.fillMaxWidth().height(22.dp), contentAlignment = Alignment.Center) {
        if (!open) {
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(Color(0xE6100D16))
                    .border(1.5.dp, LockedGrey, RoundedCornerShape(50))
                    .padding(horizontal = 10.dp, vertical = 2.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "🔒", fontSize = 10.sp)
                Text(
                    text = "$have / $required pts in ${tree.label}",
                    color = Color(0xFFBFB3D6),
                    fontFamily = FontFirst(),
                    fontWeight = FontWeight.Black,
                    fontSize = 10.sp
                )
                // Maly pasek postepu - ile jeszcze do otwarcia rzedu.
                Box(
                    modifier = Modifier
                        .width(36.dp)
                        .height(5.dp)
                        .clip(RoundedCornerShape(50))
                        .background(Color(0xFF26232F))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .fillMaxWidth((have.toFloat() / required).coerceIn(0f, 1f))
                            .background(c.mid)
                    )
                }
            }
        }
    }
}

/**
 * Medalion talentu. Stany na pierwszy rzut oka:
 * - wymaksowany: zloty pierscien i poswiata,
 * - wykupiony: pierscien w kolorze drzewka,
 * - do kupienia teraz: pulsujacy pierscien i zielony plus,
 * - osiagalny, ale bez punktu: przygaszony kolor drzewka,
 * - zamkniety: szary, z klodka.
 */
@Composable
private fun TalentNode(
    node: TalentNodeState,
    colors: TreeColors,
    onClick: () -> Unit,
    onCenter: (LayoutCoordinates) -> Unit
) {
    val infinite = rememberInfiniteTransition()
    val pulse by infinite.animateFloat(0f, 1f, infiniteRepeatable(tween(850, easing = FastOutSlowInEasing), RepeatMode.Reverse))
    val spin by infinite.animateFloat(0f, 360f, infiniteRepeatable(tween(9000, easing = LinearEasing)))

    val talent = node.talent
    val ring = when {
        node.isMaxed -> listOf(GoldLight, GoldDark)
        node.isLearned -> listOf(colors.light, colors.dark)
        node.canSpend -> listOf(colors.light, colors.mid)
        node.isReachable -> listOf(colors.mid.copy(alpha = 0.6f), colors.dark.copy(alpha = 0.6f))
        else -> listOf(LockedGrey, Color(0xFF26232F))
    }
    val capstone = talent.maxRank == 1 && talent.tier >= 3

    Column(
        modifier = Modifier
            .width(96.dp)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            ),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(modifier = Modifier.size(MEDALLION + 12.dp), contentAlignment = Alignment.Center) {
            // Poswiata za medalionem - zlota dla kompletu, pulsujaca dla "stac cie teraz".
            if (node.isMaxed || node.canSpend) {
                Box(
                    modifier = Modifier
                        .size(MEDALLION + 12.dp)
                        .rotate(spin)
                        .alpha(if (node.isMaxed) 0.55f else 0.25f + 0.45f * pulse)
                        .background(
                            Brush.sweepGradient(
                                List(12) { if (it % 2 == 0) (if (node.isMaxed) GoldLight else colors.light) else Color.Transparent }
                            ),
                            CircleShape
                        )
                )
            }
            Box(
                modifier = Modifier
                    .size(MEDALLION)
                    .onGloballyPositioned(onCenter)
                    .graphicsLayer {
                        val s = if (node.canSpend) 1f + 0.04f * pulse else 1f
                        scaleX = s
                        scaleY = s
                    }
                    .clip(if (capstone) RoundedCornerShape(16.dp) else CircleShape)
                    .background(Brush.verticalGradient(ring))
                    .border(2.5.dp, Outline, if (capstone) RoundedCornerShape(16.dp) else CircleShape)
                    .padding(4.dp)
                    .clip(if (capstone) RoundedCornerShape(12.dp) else CircleShape)
                    .background(
                        Brush.radialGradient(
                            if (node.isReachable) listOf(colors.mid.copy(alpha = 0.55f), colors.deep, Color(0xFF08070C))
                            else listOf(Color(0xFF2A2733), Color(0xFF121017))
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = talent.icon,
                    fontSize = 26.sp,
                    modifier = Modifier.alpha(if (node.isReachable) 1f else 0.35f)
                )
            }

            if (!node.isReachable) {
                Text(text = "🔒", fontSize = 13.sp, modifier = Modifier.align(Alignment.TopEnd).padding(top = 2.dp, end = 4.dp))
            } else if (node.canSpend) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(top = 2.dp, end = 2.dp)
                        .size(20.dp)
                        .graphicsLayer {
                            val s = 1f + 0.15f * pulse
                            scaleX = s
                            scaleY = s
                        }
                        .clip(CircleShape)
                        .background(Brush.verticalGradient(listOf(Color(0xFF8BE05A), Color(0xFF3F9E2B))))
                        .border(2.dp, Outline, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "+", color = Color.White, fontWeight = FontWeight.Black, fontSize = 14.sp, lineHeight = 14.sp)
                }
            }

            // Stopien w pigulce na dole medalionu.
            Text(
                text = "${node.rank}/${talent.maxRank}",
                color = when {
                    node.isMaxed -> Color(0xFF3A2405)
                    node.isLearned -> Color.White
                    else -> Color(0xFF9D93B5)
                },
                fontFamily = FontFirst(),
                fontWeight = FontWeight.Black,
                fontSize = 10.sp,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .clip(RoundedCornerShape(50))
                    .background(
                        when {
                            node.isMaxed -> Brush.verticalGradient(listOf(GoldLight, GoldDark))
                            node.isLearned -> Brush.verticalGradient(listOf(colors.mid, colors.dark))
                            else -> Brush.verticalGradient(listOf(Color(0xFF26232F), Color(0xFF14111A)))
                        }
                    )
                    .border(1.5.dp, Outline, RoundedCornerShape(50))
                    .padding(horizontal = 7.dp, vertical = 1.dp)
            )
        }
        Text(
            text = talent.name,
            color = when {
                node.isMaxed -> GoldLight
                node.isLearned -> Color.White
                node.isReachable -> Color(0xFFD9D1EA)
                else -> Color(0xFF6E6880)
            },
            fontFamily = FontFirst(),
            fontWeight = FontWeight.Black,
            fontSize = 10.sp,
            lineHeight = 12.sp,
            textAlign = TextAlign.Center,
            maxLines = 2,
            // Ciemna pigulka pod podpisem - linie polaczen biegna POD nia, a nie przez litery.
            modifier = Modifier
                .padding(top = 1.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(Color(0xE60C0A10))
                .padding(horizontal = 5.dp, vertical = 1.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
    }
}

/* ------------------------------------------ Podsumowanie --------------------------------------- */

/** Co drzewko realnie daje TERAZ - chipy ze wszystkimi bonusami naraz. */
@Composable
private fun BonusSummary(bonuses: TalentBonuses) {
    if (bonuses.isEmpty) return

    val chips = buildList {
        if (bonuses.attack != 0) add("⚔️" to "+${bonuses.attack} attack")
        if (bonuses.maxHp != 0) add("❤️" to "+${bonuses.maxHp} max HP")
        if (bonuses.critChance != 0f) add("🎯" to "+${(bonuses.critChance * 100).toInt()}% crit")
        if (bonuses.quizDamage != 0f) add("🧠" to "+${(bonuses.quizDamage * 100).toInt()}% quiz dmg")
        bonuses.buffStacks.forEach { (buff, stacks) -> add("✦" to "${buff.title} x$stacks") }
        bonuses.spellChances.forEach { (spellId, chance) ->
            add("🔮" to "${SpellCatalog.byId(spellId)?.name ?: spellId} $chance%")
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Brush.verticalGradient(listOf(Color(0xFF2A2436), Color(0xFF17131F))))
            .border(2.dp, Outline, RoundedCornerShape(16.dp))
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        OutlinedText(text = "ACTIVE BONUSES", size = 14.sp, color = GoldLight, modifier = Modifier.fillMaxWidth())
        chips.chunked(2).forEach { pair ->
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                pair.forEach { (icon, label) ->
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0x33FFFFFF))
                            .padding(horizontal = 8.dp, vertical = 5.dp),
                        horizontalArrangement = Arrangement.spacedBy(5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = icon, fontSize = 12.sp, color = GoldLight)
                        Text(
                            text = label,
                            color = Color.White,
                            fontFamily = FontFirst(),
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            maxLines = 1
                        )
                    }
                }
                if (pair.size == 1) Spacer(modifier = Modifier.weight(1f))
            }
        }
        Text(
            text = "Talents fill at most half of each battle effect - the rest comes from run upgrades.",
            color = Color(0xFF9D93B5),
            fontFamily = FontFirst(),
            fontSize = 10.sp,
            lineHeight = 12.sp
        )
    }
}

/* --------------------------------------------- Karty ------------------------------------------- */

@Composable
private fun TalentDetailSheet(
    node: TalentNodeState,
    points: Int,
    onLearn: () -> Unit,
    onDismiss: () -> Unit
) {
    val talent = node.talent
    val c = talent.tree.colors()
    val pop = remember { Animatable(0.85f) }
    LaunchedEffect(Unit) { pop.animateTo(1f, spring(dampingRatio = 0.55f, stiffness = 320f)) }
    val infinite = rememberInfiniteTransition()
    val spin by infinite.animateFloat(0f, 360f, infiniteRepeatable(tween(12_000, easing = LinearEasing)))
    val sweep by infinite.animateFloat(0f, 1f, infiniteRepeatable(tween(2400, easing = LinearEasing)))

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xCC07050B))
            .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null, onClick = onDismiss),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 340.dp)
                .padding(20.dp)
                .graphicsLayer { scaleX = pop.value; scaleY = pop.value }
                .clip(RoundedCornerShape(20.dp))
                .background(Brush.verticalGradient(listOf(c.deep, Color(0xFF0C0A10))))
                .border(3.dp, Outline, RoundedCornerShape(20.dp))
                .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) {},
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Naglowek: medalion w promieniach na tle w kolorze drzewka.
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp)
                    .clipToBounds()
                    .background(Brush.verticalGradient(listOf(c.mid, c.dark))),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(200.dp)
                        .alpha(0.35f)
                        .background(Brush.radialGradient(listOf(Color.White, Color.Transparent)), CircleShape)
                )
                Box(
                    modifier = Modifier
                        .size(84.dp)
                        .clip(CircleShape)
                        .background(Brush.verticalGradient(if (node.isMaxed) listOf(GoldLight, GoldDark) else listOf(c.light, c.dark)))
                        .border(3.dp, Outline, CircleShape)
                        .padding(5.dp)
                        .clip(CircleShape)
                        .background(Brush.radialGradient(listOf(c.mid, c.deep))),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = talent.icon, fontSize = 38.sp)
                }
                Text(
                    text = talent.tree.label.uppercase() + " · ROW ${talent.tier + 1}",
                    color = Color.White.copy(alpha = 0.85f),
                    fontFamily = FontFirst(),
                    fontWeight = FontWeight.Black,
                    fontSize = 9.sp,
                    modifier = Modifier.align(Alignment.TopStart).padding(8.dp)
                )
            }

            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedText(text = talent.name, size = 20.sp, color = if (node.isMaxed) GoldLight else Color.White)

                // Stopnie jako kropki - czytelniejsze niz "2 / 3".
                Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                    repeat(talent.maxRank) { i ->
                        Box(
                            modifier = Modifier
                                .size(width = 22.dp, height = 8.dp)
                                .clip(RoundedCornerShape(50))
                                .background(
                                    if (i < node.rank) Brush.horizontalGradient(if (node.isMaxed) listOf(GoldLight, GoldDark) else listOf(c.light, c.mid))
                                    else Brush.horizontalGradient(listOf(Color(0xFF26232F), Color(0xFF26232F)))
                                )
                                .border(1.dp, Outline, RoundedCornerShape(50))
                        )
                    }
                }

                Text(
                    text = talent.description,
                    color = Color(0xFFE6E0F2),
                    fontFamily = FontFirst(),
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    lineHeight = 17.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0x33FFFFFF))
                        .padding(12.dp)
                )

                // Wymagania pokazujemy WPROST - wygaszony wezel bez powodu to "czemu nie moge tego kliknac".
                node.block?.takeIf { it != TalentBlock.MaxRank }?.let { block ->
                    val detail = when (block) {
                        TalentBlock.TierLocked ->
                            "🔒 Spend ${TalentRules.pointsRequiredFor(talent.tier)} points in ${talent.tree.label} to open this row"
                        TalentBlock.ParentMissing ->
                            "🔗 Requires ${TalentCatalog.byId(talent.requires.orEmpty())?.name ?: "?"} at max rank"
                        TalentBlock.NoPoints -> "🌟 No free points - level up to earn more"
                        TalentBlock.MaxRank -> ""
                    }
                    Text(
                        text = detail,
                        color = Color(0xFFFFB27A),
                        fontFamily = FontFirst(),
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        textAlign = TextAlign.Center
                    )
                }

                val canLearn = node.canSpend
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(
                            Brush.verticalGradient(
                                when {
                                    node.isMaxed -> listOf(GoldLight, GoldDark)
                                    canLearn -> listOf(Color(0xFF8BE05A), Color(0xFF3F9E2B))
                                    else -> listOf(Color(0xFF4A4658), Color(0xFF26232F))
                                }
                            )
                        )
                        .border(2.5.dp, Outline, RoundedCornerShape(14.dp))
                        .clickable { if (canLearn) onLearn() else onDismiss() }
                        .clipToBounds(),
                    contentAlignment = Alignment.Center
                ) {
                    if (canLearn) {
                        Box(
                            modifier = Modifier
                                .size(width = 16.dp, height = 90.dp)
                                .graphicsLayer {
                                    translationX = (sweep * 2f - 1f) * 170f * density
                                    rotationZ = 20f
                                }
                                .background(Brush.horizontalGradient(listOf(Color.Transparent, Color.White.copy(alpha = 0.5f), Color.Transparent)))
                        )
                    }
                    OutlinedText(
                        text = when {
                            node.isMaxed -> "✓ MAXED"
                            canLearn -> "LEARN  ·  1 🌟"
                            else -> "LOCKED"
                        },
                        size = 18.sp,
                        color = Color.White
                    )
                }
                Text(
                    text = "You have $points free ${if (points == 1) "point" else "points"}",
                    color = Color(0xFF9D93B5),
                    fontFamily = FontFirst(),
                    fontSize = 11.sp
                )
            }
        }
    }
}

@Composable
private fun ConfirmResetSheet(spent: Int, onConfirm: () -> Unit, onDismiss: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xCC07050B))
            .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null, onClick = onDismiss),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 320.dp)
                .padding(20.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(Brush.verticalGradient(listOf(Color(0xFF3A1410), Color(0xFF120E14))))
                .border(3.dp, Outline, RoundedCornerShape(20.dp))
                .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) {}
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = "↺", fontSize = 36.sp, color = GoldLight)
            OutlinedText(text = "Reset all talents?", size = 18.sp, color = Color.White)
            Text(
                text = "All $spent points come back to spend again. Spells from talents stop working until you learn them again.",
                color = Color(0xFFE6E0F2),
                fontFamily = FontFirst(),
                fontSize = 12.sp,
                textAlign = TextAlign.Center
            )
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                ChunkyButton("Cancel", listOf(Color(0xFF4A4658), Color(0xFF26232F)), onDismiss, Modifier.weight(1f))
                ChunkyButton("Reset", listOf(Color(0xFFE5433B), Color(0xFF9A2A18)), onConfirm, Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun ChunkyButton(label: String, colors: List<Color>, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(Brush.verticalGradient(colors))
            .border(2.5.dp, Outline, RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 11.dp),
        contentAlignment = Alignment.Center
    ) {
        OutlinedText(text = label, size = 15.sp, color = Color.White)
    }
}

private val MEDALLION = 62.dp

/** Ta sama szerokosc co karta postaci - okna gry maja byc jednym systemem, nie kolekcja rozmiarow. */
private val DIALOG_MAX_WIDTH = 420.dp
