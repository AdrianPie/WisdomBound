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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lernhero.domain.story.IdleReward
import com.lernhero.domain.story.StoryProgress
import com.lernhero.domain.story.StoryStage
import com.lernhero.domain.story.StoryStageCatalog
import com.lernhero.domain.story.StoryWorld
import com.lernhero.domain.story.StoryWorldCatalog
import com.lernhero.game.character.MonsterVisualPresets
import com.lernhero.shared.theme.FontFirst
import com.lernhero.shared.theme.FontPixel
import org.jetbrains.compose.resources.painterResource

private val Ink = Color(0xFF120E0A)
private val Panel = Color(0xFF19120C)
private val Frame = Color(0xFFC8A45A)
private val FrameDim = Color(0xFF6B5A38)
private val Cream = Color(0xFFF4E9D7)
private val Muted = Color(0xFF9D8B6A)
private val GoldText = Color(0xFFFFD34D)
private val BattleRed = Color(0xFFB4451F)
private val CollectGreen = Color(0xFF3FA96A)

/**
 * Panel kampanii - siedzi WEWNATRZ zakladki "Walcz", pod skladem druzyny. Uklad wzorowany na
 * Idle Heroes: tlo krainy z przeciwnikami, pasek nagrod idle z przyciskiem GET u gory, rzad
 * przyciskow (MAPA / BATTLE / LOOT) i ciag ponumerowanych plansz na dole.
 *
 * Swiadomie NIE ma tu przycisku "Team" z oryginalu - druzyna stoi juz nad tym panelem, w tej samej
 * zakladce, wiec osobne przejscie do niej byloby droga donikad.
 */
@Composable
fun CampaignPanel(
    world: StoryWorld,
    stage: StoryStage,
    cleared: Map<String, Int>,
    idleReward: IdleReward,
    idleStorageFraction: Float,
    onSelectStage: (Int) -> Unit,
    onOpenMap: () -> Unit,
    onOpenLoot: () -> Unit,
    onCollectIdle: () -> Unit,
    onBattle: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Panel)
            .border(2.dp, Frame, RoundedCornerShape(14.dp)),
        verticalArrangement = Arrangement.spacedBy(0.dp)
    ) {
        IdleHeader(
            worldName = world.name,
            stageLabel = stage.label,
            reward = idleReward,
            storageFraction = idleStorageFraction,
            onCollect = onCollectIdle
        )

        StageScene(stage = stage, world = world)

        ActionRow(
            isBoss = stage.isBoss,
            onOpenMap = onOpenMap,
            onOpenLoot = onOpenLoot,
            onBattle = onBattle
        )

        StageStrip(
            world = world,
            stage = stage,
            cleared = cleared,
            onSelectStage = onSelectStage
        )
    }
}

/**
 * Pasek nagrod idle. Sam pokazuje, ile nazbieralo sie od ostatniego odbioru i jak pelny jest
 * magazyn - po [com.lernhero.domain.story.IdleRewards.CAP_HOURS] przestaje kapac, i to widac na
 * pasku, zeby gracz wiedzial, ze warto zajrzec.
 */
@Composable
private fun IdleHeader(
    worldName: String,
    stageLabel: String,
    reward: IdleReward,
    storageFraction: Float,
    onCollect: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Ink)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = worldName,
                    color = GoldText,
                    fontFamily = FontPixel(),
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    maxLines = 1
                )
                Text(
                    text = "Stage $stageLabel",
                    color = Muted,
                    fontFamily = FontFirst(),
                    fontSize = 10.sp
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                IdleChip("💰", reward.gold)
                IdleChip("⭐", reward.xp)
                IdleChip("⚙", reward.iron)
                IdleChip("✨", reward.essence)
            }

            Text(
                text = "GET",
                color = if (reward.isEmpty) Color(0xFF4A4038) else Color.White,
                fontFamily = FontPixel(),
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (reward.isEmpty) Color(0xFF241E18) else CollectGreen)
                    .border(
                        1.5.dp,
                        if (reward.isEmpty) Color(0xFF3A2E22) else CollectGreen,
                        RoundedCornerShape(8.dp)
                    )
                    .clickable(enabled = !reward.isEmpty, onClick = onCollect)
                    .padding(horizontal = 12.dp, vertical = 7.dp)
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .clip(RoundedCornerShape(50))
                .background(Color(0xFF241E18))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(storageFraction)
                    .fillMaxSize()
                    .clip(RoundedCornerShape(50))
                    .background(if (storageFraction >= 1f) BattleRed else CollectGreen)
            )
        }
    }
}

@Composable
private fun IdleChip(icon: String, value: Int) {
    Row(horizontalArrangement = Arrangement.spacedBy(2.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(text = icon, fontSize = 10.sp)
        Text(
            text = value.compactAmount(),
            color = if (value > 0) Cream else Muted,
            fontFamily = FontFirst(),
            fontWeight = FontWeight.Bold,
            fontSize = 10.sp
        )
    }
}

internal fun Int.compactAmount(): String = when {
    this >= 1_000_000 -> "${this / 1_000_000}.${(this % 1_000_000) / 100_000}M"
    this >= 1_000 -> "${this / 1_000}.${(this % 1_000) / 100}K"
    else -> toString()
}

/**
 * Scena planszy: tlo krainy (placeholder do czasu PNG - patrz [StoryWorldArt]) i stojacy na nim
 * przeciwnicy. To jest to samo, co wejdzie do walki - podglad liczy sie z tego samego seeda co
 * prawdziwa walka ([StoryStage]), wiec nie da sie tych dwoch rozjechac.
 */
@Composable
private fun StageScene(stage: StoryStage, world: StoryWorld) {
    val art = world.art()

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(140.dp)
            .background(art.gradient())
    ) {
        art.background?.let { background ->
            // Podglad to niski pasek (140dp), a pelna mapa jest wysoka - pokazujemy GORE grafiki
            // (niebo/horyzont krainy, nie ziemia pod nogami), stad alignment = TopCenter przy Crop.
            Image(
                painter = painterResource(background),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                alignment = Alignment.TopCenter,
                modifier = Modifier.fillMaxSize()
            )
        }

        // Brak PNG tla - zostaje nazwa oczekiwanego pliku, zeby bylo wiadomo, co podmienic.
        if (art.background == null) {
            Text(
                text = art.expectedFile,
                color = Color(0xFF6E6152),
                fontFamily = FontFirst(),
                fontSize = 8.sp,
                modifier = Modifier.align(Alignment.TopStart).padding(6.dp)
            )
        }

        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterHorizontally),
            verticalAlignment = Alignment.Bottom
        ) {
            stage.enemyPreview.forEach { species ->
                Image(
                    painter = painterResource(MonsterVisualPresets.forSpecies(species).drawable),
                    contentDescription = null,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.size(if (stage.isBoss) 104.dp else 62.dp)
                )
            }
        }

        if (stage.isBoss) {
            Text(
                text = "BOSS",
                color = Color.White,
                fontFamily = FontPixel(),
                fontWeight = FontWeight.Bold,
                fontSize = 10.sp,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(BattleRed)
                    .padding(horizontal = 8.dp, vertical = 3.dp)
            )
        }
    }
}

/** MAPA / BATTLE / LOOT - BATTLE jest srodkowy i najwiekszy, bo to jedyna akcja, ktora cos zaczyna. */
@Composable
private fun ActionRow(
    isBoss: Boolean,
    onOpenMap: () -> Unit,
    onOpenLoot: () -> Unit,
    onBattle: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Ink)
            .padding(horizontal = 10.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        SideAction(icon = "🗺", label = "MAP", onClick = onOpenMap, modifier = Modifier.weight(1f))

        Text(
            text = "BATTLE",
            color = Color.White,
            fontFamily = FontPixel(),
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .weight(1.4f)
                .clip(RoundedCornerShape(10.dp))
                .background(if (isBoss) BattleRed else Color(0xFF8A5A20))
                .border(2.dp, if (isBoss) Color(0xFFE0654F) else Frame, RoundedCornerShape(10.dp))
                .clickable(onClick = onBattle)
                .padding(vertical = 14.dp)
        )

        SideAction(icon = "💎", label = "LOOT", onClick = onOpenLoot, modifier = Modifier.weight(1f))
    }
}

@Composable
private fun SideAction(icon: String, label: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(Panel)
            .border(1.5.dp, FrameDim, RoundedCornerShape(10.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Text(text = icon, fontSize = 15.sp)
        Text(
            text = label,
            color = Muted,
            fontFamily = FontFirst(),
            fontWeight = FontWeight.Bold,
            fontSize = 9.sp
        )
    }
}

/**
 * Ciag plansz krainy na dole panelu. Wybrana jest wieksza i zlota (to ona pojdzie do walki),
 * zdobyte maja ptaszka, kolejna niedostepna - klodke.
 */
@Composable
private fun StageStrip(
    world: StoryWorld,
    stage: StoryStage,
    cleared: Map<String, Int>,
    onSelectStage: (Int) -> Unit
) {
    val start = StoryWorldCatalog.startIndexOf(world.id)
    val stages = remember(world.id, stage.tier) {
        (1..world.stageCount).mapNotNull { StoryStageCatalog.stageAt(start + it - 1, stage.tier) }
    }
    val listState = rememberLazyListState()

    LaunchedEffect(world.id, stage.globalIndex) {
        listState.animateScrollToItem((stage.globalIndex - start - 1).coerceAtLeast(0))
    }

    LazyRow(
        state = listState,
        modifier = Modifier
            .fillMaxWidth()
            .background(Panel)
            .padding(horizontal = 10.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        items(stages, key = { it.globalIndex }) { entry ->
            val playable = StoryProgress.isStagePlayable(cleared, stage.tier, entry.globalIndex)
            val isCleared = entry.globalIndex <= (cleared[stage.tier.name] ?: 0)
            StageBubble(
                stage = entry,
                selected = entry.globalIndex == stage.globalIndex,
                playable = playable,
                cleared = isCleared,
                onClick = { if (playable) onSelectStage(entry.globalIndex) }
            )
        }
    }
}

@Composable
private fun StageBubble(
    stage: StoryStage,
    selected: Boolean,
    playable: Boolean,
    cleared: Boolean,
    onClick: () -> Unit
) {
    val size = if (selected) 54.dp else 44.dp
    val background = when {
        !playable -> Color(0xFF0E0B08)
        selected -> Color(0xFF4A3A18)
        cleared -> Color(0xFF1C2E1C)
        else -> Ink
    }
    val border = when {
        !playable -> Color(0xFF2A241C)
        selected -> GoldText
        stage.isBoss -> Color(0xFFE0654F)
        cleared -> Color(0xFF5FD08A)
        else -> FrameDim
    }

    Box(
        modifier = Modifier
            .size(size)
            .clip(RoundedCornerShape(50))
            .background(background)
            .border(if (selected) 3.dp else 2.dp, border, RoundedCornerShape(50))
            .clickable(enabled = playable, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        when {
            !playable -> Text(text = "🔒", fontSize = 14.sp)
            stage.isBoss -> Text(text = "👑", fontSize = if (selected) 22.sp else 18.sp)
            cleared && !selected -> Text(
                text = "✓",
                color = Color(0xFF5FD08A),
                fontFamily = FontPixel(),
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
            else -> Text(
                text = stage.label,
                color = if (selected) GoldText else Cream,
                fontFamily = FontPixel(),
                fontWeight = FontWeight.Bold,
                fontSize = if (selected) 12.sp else 10.sp
            )
        }
    }
}
