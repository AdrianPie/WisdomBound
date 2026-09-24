package com.lernhero.home.story

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.statusBarsPadding
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lernhero.domain.run.RunDifficulty
import com.lernhero.domain.story.CampaignSelection
import com.lernhero.domain.story.StoryProgress
import com.lernhero.domain.story.StoryWorld
import com.lernhero.domain.story.StoryWorldCatalog
import com.lernhero.home.player.PlayerViewModel
import com.lernhero.shared.theme.FontFirst
import com.lernhero.shared.theme.FontPixel
import org.jetbrains.compose.resources.painterResource
import org.koin.compose.koinInject

private val Ink = Color(0xFF120E0A)
private val Panel = Color(0xFF19120C)
private val Frame = Color(0xFFC8A45A)
private val FrameDim = Color(0xFF6B5A38)
private val Cream = Color(0xFFF4E9D7)
private val Muted = Color(0xFF9D8B6A)
private val GoldText = Color(0xFFFFD34D)

/**
 * Ekran Mapy - osobne miejsce, do ktorego wchodzi sie przyciskiem MAPA z panelu kampanii. Tu NIE
 * wybiera sie planszy (to robi rzad na dole zakladki "Walcz"), tylko GDZIE JESTESMY: ktora kraina
 * i na jakim poziomie trudnosci.
 *
 * Zakladki trudnosci dzialaja jak w Idle Heroes: Koszmar otwiera sie dopiero po ukonczeniu calej
 * kampanii na Normalnym, i tak dalej - te same krainy, mocniejsze i lepiej platne.
 */
@Composable
fun WorldMapScreen(
    playerViewModel: PlayerViewModel,
    onBack: () -> Unit
) {
    val selection = koinInject<CampaignSelection>()
    val state by playerViewModel.uiState.collectAsState()
    val cleared = state.storyProgress

    var tier by remember { mutableStateOf(selection.tier) }
    var worldId by remember { mutableStateOf(selection.resolveWorld(cleared).id) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0B0E1A))
            // Ekran ma teraz WLASNY powrot i tytul - globalny pasek gry sie tu nie pokazuje
            // (patrz [HomeNavGraph.ownsTopChrome]), wiec to on musi odsunac sie od notcha sam.
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Ink)
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(Panel)
                    .border(1.5.dp, FrameDim, RoundedCornerShape(10.dp))
                    .clickable(onClick = onBack)
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Text(text = "←", color = GoldText, fontFamily = FontPixel(), fontSize = 15.sp)
            }
            Text(
                text = "MAPA SWIATA",
                color = GoldText,
                fontFamily = FontPixel(),
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center
            )
            Box(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                Text(text = "←", color = Color.Transparent, fontFamily = FontPixel(), fontSize = 15.sp)
            }
        }

        TierTabs(
            selected = tier,
            cleared = cleared,
            onSelect = { picked ->
                tier = picked
                selection.select(worldId, picked)
            }
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            StoryWorldCatalog.all.forEach { world ->
                val start = StoryWorldCatalog.startIndexOf(world.id)
                val reached = StoryProgress.frontier(cleared, tier) >= start
                val clearedInWorld = ((cleared[tier.name] ?: 0) - start + 1).coerceIn(0, world.stageCount)

                WorldCard(
                    world = world,
                    selected = world.id == worldId,
                    locked = !reached,
                    clearedStages = clearedInWorld,
                    onClick = {
                        if (reached) {
                            worldId = world.id
                            selection.select(world.id, tier)
                            onBack()
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun TierTabs(
    selected: RunDifficulty,
    cleared: Map<String, Int>,
    onSelect: (RunDifficulty) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Ink)
            .padding(horizontal = 10.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        RunDifficulty.entries.forEach { entry ->
            val unlocked = StoryProgress.isTierUnlocked(cleared, entry)
            val isSelected = entry == selected
            Column(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(9.dp))
                    .background(if (isSelected) Panel else Color.Transparent)
                    .border(
                        width = if (isSelected) 2.dp else 1.dp,
                        color = when {
                            !unlocked -> Color(0xFF2A241C)
                            isSelected -> GoldText
                            else -> FrameDim
                        },
                        shape = RoundedCornerShape(9.dp)
                    )
                    .clickable(enabled = unlocked) { onSelect(entry) }
                    .padding(vertical = 7.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(1.dp)
            ) {
                Text(text = if (unlocked) entry.icon else "🔒", fontSize = 14.sp)
                Text(
                    text = entry.label,
                    color = when {
                        !unlocked -> Color(0xFF4A4038)
                        isSelected -> GoldText
                        else -> Muted
                    },
                    fontFamily = FontFirst(),
                    fontWeight = FontWeight.Bold,
                    fontSize = 9.sp
                )
            }
        }
    }
}

/** Kafel krainy - duzy, bo to jest wlasciwa tresc tego ekranu. Tlo to placeholder do czasu PNG. */
@Composable
private fun WorldCard(
    world: StoryWorld,
    selected: Boolean,
    locked: Boolean,
    clearedStages: Int,
    onClick: () -> Unit
) {
    val art = world.art()

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(110.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(art.gradient())
            .border(
                width = if (selected) 3.dp else 1.5.dp,
                color = when {
                    locked -> Color(0xFF2A241C)
                    selected -> GoldText
                    else -> FrameDim
                },
                shape = RoundedCornerShape(14.dp)
            )
            .clickable(enabled = !locked, onClick = onClick)
    ) {
        art.background?.let { background ->
            Image(
                painter = painterResource(background),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                alignment = Alignment.TopCenter,
                modifier = Modifier.fillMaxSize()
            )
        }

        Column(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(text = if (locked) "🔒" else art.emoji, fontSize = 26.sp)
                Column {
                    Text(
                        text = world.name,
                        color = if (locked) Color(0xFF6E6152) else Cream,
                        fontFamily = FontPixel(),
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                    Text(
                        text = if (locked) {
                            "Clear the previous land to enter here"
                        } else {
                            "$clearedStages / ${world.stageCount} plansz"
                        },
                        color = Muted,
                        fontFamily = FontFirst(),
                        fontSize = 10.sp
                    )
                }
            }
        }

        if (art.background == null) {
            Text(
                text = art.expectedFile,
                color = Color(0xFF6E6152),
                fontFamily = FontFirst(),
                fontSize = 8.sp,
                modifier = Modifier.align(Alignment.BottomEnd).padding(6.dp)
            )
        }

        if (selected && !locked) {
            Text(
                text = "TU JESTES",
                color = Color(0xFF0B0805),
                fontFamily = FontPixel(),
                fontWeight = FontWeight.Bold,
                fontSize = 9.sp,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(GoldText)
                    .padding(horizontal = 8.dp, vertical = 3.dp)
            )
        }
    }
}
