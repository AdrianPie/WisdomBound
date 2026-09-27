package com.lernhero.home.story

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lernhero.domain.story.CampaignSelection
import com.lernhero.domain.story.IdleRewards
import com.lernhero.domain.story.StageRepeats
import com.lernhero.domain.story.StoryProgress
import com.lernhero.domain.story.StoryStageCatalog
import com.lernhero.home.player.PlayerViewModel
import com.lernhero.shared.theme.FontFirst
import kotlin.time.Clock
import org.koin.compose.koinInject

private val PanelDark = Color(0xFF1A1410)
private val TextGold = Color(0xFFFFD100)
private val TextMuted = Color(0xFF9D8B6A)

/**
 * Okno kampanii - plansze krainy, ich przeciwnicy, nagrody idle i lup. Otwiera je tabliczka miejsca
 * na scenie wyprawy; walke zaczyna BATTLE w srodku albo START na scenie (oba ida przez ten sam
 * [CampaignLauncher], wiec rozliczaja sie identycznie).
 *
 * ## Dlaczego OKNO, a nie ekran
 *
 * Panel odpowiada na jedno pytanie - "ktora plansza?" - i pokazuje jedna rzecz, ktorej nie widac
 * nigdzie indziej: kto na niej czeka. Na caly ekran rozciagal sie tylko dlatego, ze kiedys byl
 * ekranem. Teraz stoi na scenie jak reszta okien wyprawy (sklad, material): przygaszone tlo, panel
 * posrodku, wyjscie krzyzykiem w jego wlasnym gornym pasku.
 *
 * Krzyzyk MUSI byc w panelu. Wczesniej byl plywajacym przyciskiem nad ekranem i lezal dokladnie na
 * pasku plansz - przechwytywal tapniecia, wiec srodkowych plansz nie dalo sie wybrac w ogole.
 */
@Composable
fun CampaignBoard(
    viewModel: PlayerViewModel,
    launcher: CampaignLauncher,
    onOpenMap: () -> Unit,
    onClose: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    val selection = koinInject<CampaignSelection>()

    val cleared = state.storyProgress
    val world = selection.resolveWorld(cleared)

    // Wybrana plansza musi byc STANEM kompozycji, a nie samym polem w [CampaignSelection].
    //
    // To jest powod, dla ktorego tapanie kolek nic nie robilo: `selection` to zwykly obiekt (zyje
    // dluzej niz ekran, bo wybor ma przetrwac przejscie na Mape i z powrotem), wiec zapis do niego
    // nie budzi Compose. Panel dalej czytal STARA wartosc i rysowal stara plansze - klikniecie bylo
    // zapisywane, tylko nie bylo tego widac.
    //
    // Teraz numer planszy trzyma tutejszy stan, a `selection` dostaje kopie przy kazdej zmianie:
    // scena po zamknieciu okna czyta juz nowy wybor, a panel odswieza sie natychmiast.
    var stageIndex by remember(world.id, selection.tier) {
        mutableStateOf(selection.resolveStage(cleared))
    }
    val stage = StoryStageCatalog.stageAt(stageIndex, selection.tier)

    var lootOpen by remember { mutableStateOf(false) }

    // Nagrody idle licza sie z roznicy czasu - jedno odczytanie zegara przy wejsciu wystarczy,
    // bo panel i tak odswiezy sie przy nastepnym wejsciu.
    val now = remember { Clock.System.now().toEpochMilliseconds() }
    val bestCleared = StoryProgress.bestClearedStage(cleared)
    val idleReward = remember(bestCleared, state.idleCollectedAt, now, state.progress.level) {
        IdleRewards.pendingFor(bestCleared, state.idleCollectedAt, now, state.progress.level)
    }
    val idleFraction = remember(state.idleCollectedAt, now) {
        IdleRewards.storageFraction(state.idleCollectedAt, now)
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        // Przygaszenie sceny pod oknem. Tapniecie obok panelu zamyka - bez podswietlenia, bo to
        // nie jest przycisk, tylko "kliknij gdzie indziej".
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xCC07050B))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onClose
                )
        )

        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .systemBarsPadding()
                .padding(horizontal = 16.dp, vertical = 12.dp)
                // Okno, nie ekran - i realnie mniejsze od niego z kazdej strony, zeby bylo widac,
                // ze scena wyprawy czeka pod spodem.
                .fillMaxWidth(PANEL_WIDTH_FRACTION)
                .widthIn(max = PANEL_MAX_WIDTH)
                .height(maxHeight * PANEL_HEIGHT_FRACTION),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            if (stage == null) {
                Text(
                    text = "Campaign completed at this difficulty. Pick a higher one on the Map.",
                    color = TextMuted,
                    fontFamily = FontFirst(),
                    fontSize = 12.sp
                )
            } else {
                CampaignPanel(
                    modifier = Modifier.weight(1f),
                    world = world,
                    stage = stage,
                    cleared = cleared,
                    idleReward = idleReward,
                    idleStorageFraction = idleFraction,
                    clearsOf = { entry ->
                        StageRepeats.clearsOf(state.stageClears, entry.tier, entry.globalIndex)
                    },
                    onSelectStage = { picked ->
                        stageIndex = picked
                        selection.selectStage(picked)
                    },
                    onOpenMap = onOpenMap,
                    onOpenLoot = { lootOpen = true },
                    onClose = onClose,
                    onCollectIdle = {
                        viewModel.collectIdleRewards(Clock.System.now().toEpochMilliseconds())
                    },
                    onBattle = { launcher.launch(stage) }
                )
            }

            state.message?.let { message ->
                Text(
                    text = message,
                    color = TextGold,
                    fontFamily = FontFirst(),
                    fontSize = 11.sp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(PanelDark.copy(alpha = 0.9f))
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                )
            }
        }
    }

    if (lootOpen && stage != null) {
        StageLootDialog(
            stage = stage,
            onDismiss = { lootOpen = false },
            firstClear = StageRepeats.isFirstClear(state.stageClears, stage.tier, stage.globalIndex)
        )
    }
}

/**
 * Ile ekranu zajmuje okno. Nie caly (to je robilo ekranem) i nie polowa - scena krainy z
 * przeciwnikami ma zostac scena, a nie paskiem, a pod nia stoja jeszcze trzy rzedy: nagroda,
 * przyciski i plansze.
 */
private const val PANEL_HEIGHT_FRACTION = 0.64f
private const val PANEL_WIDTH_FRACTION = 0.94f
private val PANEL_MAX_WIDTH = 420.dp
