package com.lernhero.home.story

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.Stable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import com.lernhero.domain.run.BattleRequest
import com.lernhero.domain.run.ChestSlot
import com.lernhero.domain.run.ChestWheel
import com.lernhero.domain.run.RunDifficulty
import com.lernhero.domain.run.RunSession
import com.lernhero.domain.story.CampaignSelection
import com.lernhero.domain.story.StageRepeats
import com.lernhero.domain.story.StoryStage
import com.lernhero.home.city.TownViewModel
import com.lernhero.home.player.PlayerViewModel
import kotlin.random.Random
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

/**
 * Wejscie na plansze kampanii i cala ksiegowosc, ktora sie z nim wiaze - w JEDNYM miejscu.
 *
 * ## Po co to powstalo
 *
 * Wczesniej ksiegowosc siedziala wewnatrz [CampaignBoard]: tam ladowal `pendingStage`, tam czekal
 * efekt rozliczajacy powrot z walki i tam wyskakiwala skrzynia po bossie. Mialo to dwie wady i obie
 * bylo widac w grze:
 *
 *  1. Panel znikal z kompozycji w tej samej chwili, w ktorej gracz szedl do walki (zamykal sie,
 *     zanim nawigacja ruszyla), wiec jego wlasny zapis "z ktorej planszy wyszedlem" przepadal - a
 *     razem z nim skrzynia za pierwsze pokonanie bossa.
 *  2. Zaliczenie planszy szlo DRUGI raz, mimo ze [com.lernhero.game.battle.BattleViewModel] robi to
 *     juz wlasnym zapisem tuz po wygranej. Licznik powtorek ([StageRepeats]) rosl przez to o dwa na
 *     jedno zejscie, czyli stawka za plansze spadala dwa razy szybciej, niz mowi balans.
 *
 * Teraz startem plansz zajmuje sie ten obiekt, a jego stan siedzi na poziomie ZAKLADKI (przezywa
 * wyjscie do walki i powrot), nie na poziomie okna, ktore moze sie zamknac. Dzieki temu plansze da
 * sie odpalac z dwoch miejsc naraz - wprost przyciskiem START na scenie i z panelu kampanii - a obie
 * drogi rozliczaja sie identycznie.
 */
@Stable
class CampaignLauncher internal constructor(
    private val runSession: RunSession,
    private val viewModel: PlayerViewModel,
    internal val pending: MutableState<String?>,
    internal val chest: MutableState<String?>
) {

    /** Aktualny licznik zdobyc - podmieniany przy kazdej kompozycji, patrz [rememberCampaignLauncher]. */
    internal var stageClears: Map<String, Int> = emptyMap()

    /** Nawigacja do ekranu walki. Trzymana jako pole, zeby launcher nie zamrozil pierwszej wersji. */
    internal var onEnterBattle: () -> Unit = {}

    /**
     * Placi energie i wchodzi na plansze. Gdy energii brakuje, [PlayerViewModel.enterBattle] nie
     * odpala nic - dlatego CALE przygotowanie runu siedzi w srodku jego callbacku, a nie przed nim.
     */
    fun launch(stage: StoryStage) {
        viewModel.enterBattle {
            runSession.startNewRun(
                category = null,
                difficulty = stage.tier,
                storyStage = stage.globalIndex
            )
            // Kazda plansza startuje z pelnym HP - nie ma juz korytarza przenoszacego rany.
            runSession.healParty()
            runSession.pendingBattle = BattleRequest(
                kind = stage.kind,
                waves = stage.waves,
                seed = stage.seed,
                isEndless = false
            )
            // Licznik powtorek czytamy TERAZ, przed walka - po powrocie jest juz podniesiony zapisem
            // z ekranu walki, wiec "pierwsze przejscie" nie dalo by sie z niego odczytac.
            pending.value = PendingRun(
                tier = stage.tier,
                globalStage = stage.globalIndex,
                firstClear = StageRepeats.isFirstClear(stageClears, stage.tier, stage.globalIndex),
                isBoss = stage.isBoss
            ).encode()
            onEnterBattle()
        }
    }
}

/** Co trzeba zapamietac na czas walki, zeby po powrocie bylo wiadomo, co wlasnie sie stalo. */
private data class PendingRun(
    val tier: RunDifficulty,
    val globalStage: Int,
    val firstClear: Boolean,
    val isBoss: Boolean
) {
    // Zwykly String, bo tylko takie typy przechodza przez rememberSaveable bez wlasnego Savera - a
    // ten stan MUSI przezyc zabicie procesu w tle w trakcie dlugiej walki.
    fun encode(): String = "${tier.name}|$globalStage|$firstClear|$isBoss"

    companion object {
        fun decode(raw: String): PendingRun? {
            val parts = raw.split("|")
            if (parts.size != 4) return null
            val tier = RunDifficulty.entries.firstOrNull { it.name == parts[0] } ?: return null
            return PendingRun(
                tier = tier,
                globalStage = parts[1].toIntOrNull() ?: return null,
                firstClear = parts[2].toBooleanStrictOrNull() ?: return null,
                isBoss = parts[3].toBooleanStrictOrNull() ?: return null
            )
        }
    }
}

/**
 * Launcher zakladki - wolany TAM, gdzie kompozycja przezywa przejscie do ekranu walki i powrot
 * (czyli w samej zakladce, nie w oknie, ktore po drodze sie zamyka).
 *
 * Sam nic nie rysuje: nagrode po powrocie pokazuje [CampaignRewards], ktore stawia sie na koncu
 * ekranu, nad wszystkim innym.
 */
@Composable
fun rememberCampaignLauncher(
    viewModel: PlayerViewModel,
    onEnterBattle: () -> Unit
): CampaignLauncher {
    val runSession = koinInject<RunSession>()
    val selection = koinInject<CampaignSelection>()
    val state by viewModel.uiState.collectAsState()

    val pending = rememberSaveable { mutableStateOf<String?>(null) }
    val chest = rememberSaveable { mutableStateOf<String?>(null) }

    val launcher = remember(viewModel) {
        CampaignLauncher(runSession = runSession, viewModel = viewModel, pending = pending, chest = chest)
    }
    launcher.stageClears = state.stageClears
    launcher.onEnterBattle = onEnterBattle

    // Powrot z walki: zakladka wraca do kompozycji, wiec ten efekt odpala sie dokladnie raz na kazde
    // wyjscie i wejscie.
    LaunchedEffect(Unit) {
        val run = pending.value?.let(PendingRun::decode)
        pending.value = null
        if (run == null || runSession.lastBattleWon != true) return@LaunchedEffect

        // Zdobycie NOWEJ planszy przesuwa wybor dalej - po to gracz tu przyszedl. Powtorka zostawia
        // go tam, gdzie farmi: inaczej po kazdym zejsciu musialby wybierac plansze od nowa.
        if (run.firstClear) selection.clearStage()

        // Skrzynia nalezy sie WYLACZNIE za pierwsze zdobycie bossa - patrz [StageRepeats].
        if (run.firstClear && run.isBoss) {
            chest.value = "${ChestWheel.roll(Random.Default, run.globalStage).name}|${run.globalStage}"
        }
    }

    return launcher
}

/**
 * Skrzynia za pierwsze pokonanie bossa. Osobny composable, bo musi stac na SAMYM KONCU ekranu -
 * inaczej scena wyprawy narysowalaby sie na niej.
 */
@Composable
fun CampaignRewards(launcher: CampaignLauncher, viewModel: PlayerViewModel) {
    val townViewModel = koinViewModel<TownViewModel>()
    val raw = launcher.chest.value ?: return
    val parts = raw.split("|")
    val slot = ChestSlot.entries.firstOrNull { it.name == parts.firstOrNull() } ?: return
    val index = parts.getOrNull(1)?.toIntOrNull() ?: 1
    val prize = remember(raw) { resolveChestPrize(slot, Random(index.toLong() * 7 + 1)) }

    BossChestOverlay(
        dungeonLevel = index,
        slot = slot,
        prize = prize,
        onClaim = {
            prize.grant(viewModel, townViewModel)
            launcher.chest.value = null
        }
    )
}
