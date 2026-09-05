package com.lernhero.home.talent

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
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
import com.lernhero.shared.theme.AppPalette
import com.lernhero.shared.theme.FontFirst
import com.lernhero.shared.theme.FontPixel

/**
 * Drzewko talentow - trzy sciezki, po kilkanascie wezlow kazda, odblokowywane rzedami.
 *
 * Zastapilo kupowanie zaklec za esencje: rozwoj postaci ma isc jedna osia (poziom -> punkty),
 * a nie dwiema, z ktorych jedna byla zwyklym sklepem. Zaklecia wciaz istnieja - wezel je
 * odblokowuje razem z gotowa szansa na rzut w walce, gracz niczego nie rozdaje.
 *
 * Ekran nie liczy NICZEGO sam - o tym, co wolno wykupic i ile punktow siedzi w drzewku, mowi
 * [TalentRules]. Dzieki temu wygaszony wezel i odrzucony zapis maja dokladnie ten sam powod.
 */
@Composable
fun TalentScreen(
    viewModel: PlayerViewModel,
    onBack: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()

    var selectedTree by remember { mutableStateOf(TalentTree.Dps) }
    var inspected by remember { mutableStateOf<TalentNodeState?>(null) }
    var confirmReset by remember { mutableStateOf(false) }

    val points = state.talentPoints
    val nodes = remember(selectedTree, state.talentRanks, points) {
        TalentRules.statesOf(selectedTree, state.talentRanks, points)
    }

    // Ekran chowa paski aplikacji (patrz HomeNavGraph), wiec sam odsuwa sie od wciec systemowych.
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AppPalette.Background)
            .systemBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Header(
                points = points,
                spent = TalentRules.totalSpent(state.talentRanks),
                onBack = onBack,
                onReset = { confirmReset = true }
            )

            TreeTabs(
                selected = selectedTree,
                ranks = state.talentRanks,
                onSelect = { selectedTree = it }
            )

            TreeGrid(
                tree = selectedTree,
                nodes = nodes,
                pointsInTree = TalentRules.pointsIn(state.talentRanks, selectedTree),
                onNodeClick = { inspected = it }
            )

            BonusSummary(bonuses = state.talentBonuses)

            state.message?.let { HintLine(it) }
            Spacer(modifier = Modifier.height(8.dp))
        }
    }

    inspected?.let { node ->
        // Wezel odczytujemy ZE SWIEZEGO stanu, a nie z zapamietanego kliknięcia - inaczej po
        // wykupieniu stopnia karta pokazywalaby wciaz poprzedni stopien i aktywny przycisk.
        val live = nodes.firstOrNull { it.talent.id == node.talent.id } ?: node
        TalentDetailSheet(
            node = live,
            onLearn = {
                viewModel.spendTalentPoint(live.talent)
                if (live.rank + 1 >= live.talent.maxRank) inspected = null
            },
            onDismiss = { inspected = null }
        )
    }

    if (confirmReset) {
        ConfirmResetSheet(
            spent = TalentRules.totalSpent(state.talentRanks),
            onConfirm = {
                viewModel.resetTalents()
                confirmReset = false
            },
            onDismiss = { confirmReset = false }
        )
    }
}

/* ------------------------------------------ Naglowek ------------------------------------------ */

@Composable
private fun Header(
    points: Int,
    spent: Int,
    onBack: () -> Unit,
    onReset: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "‹",
            color = AppPalette.Gold,
            fontFamily = FontPixel(),
            fontSize = 26.sp,
            modifier = Modifier
                .clip(RoundedCornerShape(10.dp))
                .clickable(onClick = onBack)
                .padding(horizontal = 12.dp, vertical = 2.dp)
        )

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "Talents",
                color = AppPalette.Gold,
                fontFamily = FontPixel(),
                fontWeight = FontWeight.Bold,
                fontSize = 17.sp
            )
            Text(
                text = "$spent pts spent",
                color = AppPalette.TextMuted,
                fontFamily = FontFirst(),
                fontSize = 11.sp
            )
        }

        if (spent > 0) {
            Text(
                text = "Reset",
                color = AppPalette.TextMuted,
                fontFamily = FontFirst(),
                fontSize = 11.sp,
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .border(1.dp, AppPalette.GoldDim, RoundedCornerShape(8.dp))
                    .clickable(onClick = onReset)
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            )
        }

        PointsBadge(points = points)
    }
}

/** Licznik wolnych punktow - swieci, dopoki jest co wydac. */
@Composable
private fun PointsBadge(points: Int) {
    val has = points > 0
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .background(if (has) AppPalette.Gold.copy(alpha = 0.16f) else AppPalette.Panel)
            .border(
                width = 1.dp,
                color = if (has) AppPalette.Gold else AppPalette.GoldDim,
                shape = RoundedCornerShape(10.dp)
            )
            .padding(horizontal = 10.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(5.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = "🌟", fontSize = 13.sp)
        Text(
            text = "$points",
            color = if (has) AppPalette.Gold else AppPalette.TextFaint,
            fontFamily = FontPixel(),
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp
        )
    }
}

/* ------------------------------------------- Zakladki ------------------------------------------ */

/**
 * [ranks] wchodzi tu jako WARTOSC, a nie jako lambda siegajaca po stan z ViewModelu. Czytanie
 * `uiState.value` w srodku composable nie jest odczytem snapshotu, wiec licznik przy zakladce
 * nie odswiezalby sie po wykupieniu talentu - pokazywalby zero mimo wydanych punktow.
 */
@Composable
private fun TreeTabs(
    selected: TalentTree,
    ranks: Map<String, Int>,
    onSelect: (TalentTree) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        TalentTree.entries.forEach { tree ->
            val active = tree == selected
            val accent = tree.accent()
            Column(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (active) accent.copy(alpha = 0.18f) else AppPalette.Panel)
                    .border(
                        width = if (active) 2.dp else 1.dp,
                        color = if (active) accent else AppPalette.GoldDim.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(12.dp)
                    )
                    .clickable { onSelect(tree) }
                    .padding(vertical = 9.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                Text(text = tree.icon, fontSize = 19.sp)
                Text(
                    text = tree.label,
                    color = if (active) accent else AppPalette.TextMuted,
                    fontFamily = FontFirst(),
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp
                )
                Text(
                    text = "${TalentRules.pointsIn(ranks, tree)} pkt",
                    color = if (active) AppPalette.TextBody else AppPalette.TextFaint,
                    fontFamily = FontPixel(),
                    fontSize = 10.sp
                )
            }
        }
    }
}

/* -------------------------------------------- Siatka ------------------------------------------- */

@Composable
private fun TreeGrid(
    tree: TalentTree,
    nodes: List<TalentNodeState>,
    pointsInTree: Int,
    onNodeClick: (TalentNodeState) -> Unit
) {
    val accent = tree.accent()
    val rows = remember(tree) { TalentCatalog.rowsOf(tree) }
    val byId = remember(nodes) { nodes.associateBy { it.talent.id } }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(AppPalette.Panel)
            .border(2.dp, accent.copy(alpha = 0.45f), RoundedCornerShape(14.dp))
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(
            text = tree.tagline,
            color = AppPalette.TextMuted,
            fontFamily = FontFirst(),
            fontSize = 11.sp
        )

        rows.forEachIndexed { tier, row ->
            val required = TalentRules.pointsRequiredFor(tier)
            val open = pointsInTree >= required

            TierLabel(tier = tier, required = required, open = open, accent = accent, pointsInTree = pointsInTree)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Kolumny sa stale (0..2), zeby wezly z roznych rzedow staly jeden pod drugim -
                // bez tego rzad z dwoma talentami rozjezdzalby sie wzgledem rzedu z trzema.
                (0..2).forEach { column ->
                    val talent = row.firstOrNull { it.column == column }
                    if (talent == null) {
                        Spacer(modifier = Modifier.weight(1f))
                    } else {
                        val node = byId[talent.id]
                        if (node == null) {
                            Spacer(modifier = Modifier.weight(1f))
                        } else {
                            TalentNode(
                                node = node,
                                accent = accent,
                                modifier = Modifier.weight(1f),
                                onClick = { onNodeClick(node) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TierLabel(
    tier: Int,
    required: Int,
    open: Boolean,
    accent: Color,
    pointsInTree: Int
) {
    if (tier == 0) return
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .weight(1f)
                .height(1.dp)
                .background(if (open) accent.copy(alpha = 0.4f) else AppPalette.GoldDim.copy(alpha = 0.25f))
        )
        Text(
            text = if (open) "otwarte" else "$pointsInTree/$required pkt",
            color = if (open) accent.copy(alpha = 0.8f) else AppPalette.TextFaint,
            fontFamily = FontPixel(),
            fontSize = 9.sp
        )
        Box(
            modifier = Modifier
                .weight(1f)
                .height(1.dp)
                .background(if (open) accent.copy(alpha = 0.4f) else AppPalette.GoldDim.copy(alpha = 0.25f))
        )
    }
}

/**
 * Jeden wezel. Trzy stany czytelne na pierwszy rzut oka: wykupiony (swieci akcentem),
 * dostepny (jasna ramka), zamkniety (przygaszony). Wymaksowany dostaje zlota ramke - inaczej
 * nie dalo by sie odroznic "mam 1/5" od "mam komplet".
 */
@Composable
private fun TalentNode(
    node: TalentNodeState,
    accent: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val learned = node.isLearned
    val reachable = node.isReachable
    val glow by animateFloatAsState(
        targetValue = if (node.canSpend) 1f else 0f,
        animationSpec = tween(400)
    )

    val frame = when {
        node.isMaxed -> AppPalette.Gold
        learned -> accent
        node.canSpend -> accent.copy(alpha = 0.5f + 0.5f * glow)
        reachable -> AppPalette.GoldDim
        else -> AppPalette.GoldDim.copy(alpha = 0.3f)
    }

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(11.dp))
            .background(if (learned) accent.copy(alpha = 0.14f) else AppPalette.Ink)
            .border(if (learned || node.canSpend) 2.dp else 1.dp, frame, RoundedCornerShape(11.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp, horizontal = 4.dp)
            .alpha(if (reachable) 1f else 0.45f),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(text = node.talent.icon, fontSize = 20.sp)
        Text(
            text = node.talent.name,
            color = if (learned) AppPalette.TextBody else AppPalette.TextMuted,
            fontFamily = FontFirst(),
            fontWeight = FontWeight.Bold,
            fontSize = 9.sp,
            textAlign = TextAlign.Center,
            maxLines = 2
        )
        Text(
            text = "${node.rank}/${node.talent.maxRank}",
            color = when {
                node.isMaxed -> AppPalette.Gold
                learned -> accent
                else -> AppPalette.TextFaint
            },
            fontFamily = FontPixel(),
            fontWeight = FontWeight.Bold,
            fontSize = 10.sp
        )
    }
}

/* ------------------------------------------ Podsumowanie --------------------------------------- */

/** Co drzewko realnie daje TERAZ - jedno miejsce, w ktorym widac efekt wszystkich punktow razem. */
@Composable
private fun BonusSummary(bonuses: TalentBonuses) {
    if (bonuses.isEmpty) return

    val lines = buildList {
        if (bonuses.attack != 0) add("+${bonuses.attack} team attack")
        if (bonuses.maxHp != 0) add("+${bonuses.maxHp} team max HP")
        if (bonuses.critChance != 0f) add("+${(bonuses.critChance * 100).toInt()}% crit chance")
        if (bonuses.quizDamage != 0f) add("+${(bonuses.quizDamage * 100).toInt()}% quiz damage")
        bonuses.buffStacks.forEach { (buff, stacks) -> add("${buff.title} x$stacks") }
        bonuses.spellChances.forEach { (spellId, chance) ->
            val name = SpellCatalog.byId(spellId)?.name ?: spellId
            add("$name: $chance% ciosow")
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(AppPalette.PanelRaised)
            .border(1.dp, AppPalette.GoldFrame.copy(alpha = 0.45f), RoundedCornerShape(12.dp))
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        Text(
            text = "Active Bonuses",
            color = AppPalette.Gold,
            fontFamily = FontPixel(),
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp
        )
        lines.forEach { line ->
            Text(
                text = "• $line",
                color = AppPalette.TextBody,
                fontFamily = FontFirst(),
                fontSize = 11.sp
            )
        }
    }
}

@Composable
private fun HintLine(text: String) {
    Text(
        text = text,
        color = AppPalette.TextMuted,
        fontFamily = FontFirst(),
        fontSize = 11.sp,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(AppPalette.Panel)
            .padding(horizontal = 10.dp, vertical = 7.dp)
    )
}

/* --------------------------------------------- Karty ------------------------------------------- */

@Composable
private fun TalentDetailSheet(
    node: TalentNodeState,
    onLearn: () -> Unit,
    onDismiss: () -> Unit
) {
    val talent = node.talent
    val accent = talent.tree.accent()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xCC07050B))
            .clickable(onClick = onDismiss),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 340.dp)
                .padding(20.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(AppPalette.Panel)
                .border(2.dp, accent, RoundedCornerShape(16.dp))
                .clickable(enabled = false) {}
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = talent.icon, fontSize = 34.sp)
            Text(
                text = talent.name,
                color = AppPalette.Gold,
                fontFamily = FontPixel(),
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                textAlign = TextAlign.Center
            )
            Text(
                text = "${node.rank} / ${talent.maxRank}",
                color = if (node.isMaxed) AppPalette.Gold else accent,
                fontFamily = FontPixel(),
                fontSize = 12.sp
            )
            Text(
                text = talent.description,
                color = AppPalette.TextBody,
                fontFamily = FontFirst(),
                fontSize = 12.sp,
                textAlign = TextAlign.Center
            )

            // Wymagania pokazujemy WPROST - wygaszony wezel bez powodu to najczestsza przyczyna
            // "czemu nie moge tego kliknac".
            node.block?.let { block ->
                val detail = when (block) {
                    TalentBlock.TierLocked ->
                        "${block.reason} (${TalentRules.pointsRequiredFor(talent.tier)} pkt w: ${talent.tree.label})"
                    TalentBlock.ParentMissing ->
                        "${block.reason}: ${TalentCatalog.byId(talent.requires.orEmpty())?.name ?: "?"}"
                    else -> block.reason
                }
                Text(
                    text = detail,
                    color = if (block == TalentBlock.MaxRank) AppPalette.Success else AppPalette.Warning,
                    fontFamily = FontFirst(),
                    fontSize = 11.sp,
                    textAlign = TextAlign.Center
                )
            }

            SheetButton(
                label = if (node.isMaxed) "Maxed" else "Learn (1 pt)",
                enabled = node.canSpend || node.isMaxed,
                accent = if (node.canSpend) AppPalette.Action else AppPalette.GoldDim,
                onClick = { if (node.canSpend) onLearn() else onDismiss() }
            )
        }
    }
}

@Composable
private fun ConfirmResetSheet(
    spent: Int,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
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
                .widthIn(max = 320.dp)
                .padding(20.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(AppPalette.Panel)
                .border(2.dp, AppPalette.Danger, RoundedCornerShape(16.dp))
                .clickable(enabled = false) {}
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Reset the tree?",
                color = AppPalette.Gold,
                fontFamily = FontPixel(),
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp
            )
            Text(
                text = "$spent pts will return to spend. Spells unlocked by talents will stop working in battle.",
                color = AppPalette.TextBody,
                fontFamily = FontFirst(),
                fontSize = 12.sp,
                textAlign = TextAlign.Center
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                SheetButton(
                    label = "Cancel",
                    enabled = true,
                    accent = AppPalette.GoldDim,
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f)
                )
                SheetButton(
                    label = "Reset",
                    enabled = true,
                    accent = AppPalette.Danger,
                    onClick = onConfirm,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun SheetButton(
    label: String,
    enabled: Boolean,
    accent: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(accent.copy(alpha = if (enabled) 0.9f else 0.3f))
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = 18.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = if (enabled) AppPalette.TextBody else AppPalette.TextFaint,
            fontFamily = FontPixel(),
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp
        )
    }
}

/** Kolor sciezki. Trzyma sie palety aplikacji - drzewka maja sie roznic odcieniem, nie stylem. */
private fun TalentTree.accent(): Color = when (this) {
    TalentTree.Dps -> AppPalette.ActionBright
    TalentTree.Tank -> AppPalette.Info
    TalentTree.Healer -> AppPalette.Success
}
