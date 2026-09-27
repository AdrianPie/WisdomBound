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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.ui.layout.ContentScale
import com.lernhero.shared.resources.Resources
import org.jetbrains.compose.resources.painterResource
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
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
 * Wyglad jak okno talentow w WoW Classic: zakladki drzewek u gory, pod nimi jedno drzewko na
 * wlasnym tle - kwadratowe ikony w siatce, stopien w rogu ikony i strzalki wymagan ([TalentTreePanel]).
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
    val spent = TalentRules.totalSpent(state.talentRanks)

    Box(modifier = Modifier.fillMaxSize()) {
        DialogScrim(onDismiss = onClose)

        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .widthIn(max = DIALOG_MAX_WIDTH)
                .fillMaxHeight()
                .navigationBarsPadding()
                .statusBarsPadding()
                .padding(horizontal = 12.dp, vertical = 12.dp)
                .padding(bottom = DIALOG_CLOSE_LANE)
                .clip(RoundedCornerShape(20.dp))
                .background(Brush.verticalGradient(listOf(Color(0xFF231C12), Color(0xFF0C0A08))))
                .border(3.dp, Color(0xFF05040A), RoundedCornerShape(20.dp))
                .border(1.5.dp, Color(0xFFC8A45A).copy(alpha = 0.45f), RoundedCornerShape(20.dp))
        ) {
            // Tlo wybranego drzewka na calym oknie - pod naglowkiem, zakladkami i drzewkiem.
            val bg = remember(selectedTree) { Resources.drawableNamed("talent_bg_${selectedTree.fileKey()}") }
            if (bg != null) {
                Image(
                    painter = painterResource(bg),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.matchParentSize()
                )
                // Mgla na calym oknie jak w WoW - tlo widac, ale ikony i napisy sie od niego odcinaja.
                Box(modifier = Modifier.matchParentSize().background(Color.Black.copy(alpha = 0.55f)))
            }
        Column(
            modifier = Modifier.fillMaxSize().padding(12.dp),
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
                // Jedno drzewko naraz (wybierane zakladkami), w stylu WoW Classic - patrz [TalentTreePanel].
                TalentTreePanel(
                    tree = selectedTree,
                    ranks = state.talentRanks,
                    points = points,
                    onNodeClick = { inspected = it.talent.id }
                )


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
        val tree = TalentCatalog.byId(id)?.tree
        tree?.let { TalentRules.statesOf(it, state.talentRanks, points) }
            ?.firstOrNull { it.talent.id == id }?.let { live ->
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
            // Przycisk to grafika `talent_tab_<drzewko>` - na niej tylko napis i punkty.
            // Nieaktywne sa przygaszone i lekko opuszczone, aktywny "wyskakuje" w gore.
            val art = remember(tree) { Resources.drawableNamed("talent_tab_${tree.fileKey()}") }
            Box(
                modifier = Modifier
                    .weight(1f)
                    .aspectRatio(224f / 126f)
                    .graphicsLayer {
                        translationY = if (active) 0f else 3f * density
                        val s = if (active) 1f else 0.94f
                        scaleX = s; scaleY = s
                    }
                    .clip(RoundedCornerShape(14.dp))
                    .clickable { onSelect(tree) },
                contentAlignment = Alignment.Center
            ) {
                if (art != null) {
                    Image(
                        painter = painterResource(art),
                        contentDescription = null,
                        contentScale = ContentScale.FillBounds,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Box(
                        modifier = Modifier.fillMaxSize()
                            .background(Brush.verticalGradient(listOf(c.light, c.mid, c.dark)))
                    )
                }
                if (!active) Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.45f)))
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(0.dp)
                ) {
                    OutlinedText(
                        text = tree.label,
                        size = 14.sp,
                        color = if (active) Color.White else Color.White.copy(alpha = 0.75f),
                        strokeWidth = 6f
                    )
                    OutlinedText(
                        text = "${TalentRules.pointsIn(ranks, tree)} pts",
                        size = 11.sp,
                        color = if (active) Color(0xFFFFE08A) else Color(0xFFFFE08A).copy(alpha = 0.7f),
                        strokeWidth = 5f
                    )
                }
            }
        }
    }
}

/* ------------------------------------------ Podsumowanie --------------------------------------- */

/** Co drzewko realnie daje TERAZ - chipy ze wszystkimi bonusami naraz. */
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
