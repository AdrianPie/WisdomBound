package com.lernhero.home.city

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.EaseInCubic
import androidx.compose.animation.core.EaseOutBack
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.lernhero.domain.common.Rarity
import com.lernhero.domain.monster.MonsterCard
import com.lernhero.shared.audio.Sfx
import com.lernhero.shared.audio.Sounds
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.random.Random

/** Etapy pokazu - patrz [SummonShow]. */
internal enum class ShowStage {
    /** Pusty oltarz, nic sie nie dzieje. */
    Idle,

    /** Energia sie zbiera, slup zmienia kolor, wybuch. Tapniecie przewija do wyniku. */
    Ritual,

    /** Sylweta w slupie swiatla, wskazowka zywiolu, potem kolor i imie. */
    Walkout,

    /** Karta kompana na srodku ekranu - odkrywa sie po kawalku, tapniecie chowa ja do kolekcji. */
    Card,

    /** x10: dziesiec zakrytych kart, odkrywanych jedna po drugiej. */
    Grid,

    /** Po wszystkim: kompan stoi na oltarzu, mozna losowac dalej. */
    Done
}

/**
 * Rezyseria losowania - jedna os czasu dla calego pokazu, wzorowana na tym, jak robia to gry,
 * ktore gracze nagrywaja i wrzucaja do sieci:
 *
 * - **FIFA (walkout):** zanim zobaczysz zawodnika, dostajesz wskazowki po kolei - flage, pozycje,
 *   herb klubu - a on sam wychodzi najpierw jako ciemna sylwetka w swietle. U nas: sylweta
 *   w slupie swiatla i emblemat zywiolu, dopiero potem kolor i karta z imieniem.
 * - **Genshin / Raid:** kolor swiatla zdradza rzadkosc, ale potrafi PRZESKOCZYC w gore w ostatniej
 *   chwili (niebieski -> fiolet -> zloto). To najbardziej "klipowalny" moment calego losowania,
 *   wiec tu tez jest - i nie zawsze: czasem rzadki zaczyna od zielonego, zeby przeskok nie
 *   oznaczal automatycznie epika.
 * - **Hearthstone:** przy x10 karty leza zakryte, a te lepsze swieca juz od spodu - gracz sam
 *   decyduje, kiedy odkryc te, na ktore czeka.
 *
 * Kazdy kanal animacji to osobny [Animatable] 0..1 czytany przez rysowanie (a nie przez
 * kompozycje), wiec klatki nie przebudowuja calego ekranu. Tapniecie w trakcie przewija od razu
 * do karty - nikt nie musi ogladac calego rytualu przy dziesiatym losowaniu z rzedu.
 */
@Stable
internal class SummonShow(private val scope: CoroutineScope) {

    var stage by mutableStateOf(ShowStage.Idle)
        private set

    /** Wylosowane karty w kolejnosci losowania. */
    var pulls by mutableStateOf<List<MonsterCard>>(emptyList())
        private set

    /** Czy dana karta (po indeksie) jest nowa w kolekcji - druga kopia w tym samym x10 juz nie. */
    var fresh by mutableStateOf<List<Boolean>>(emptyList())
        private set

    /** Kolor slupa W TEJ CHWILI - w trakcie rytualu potrafi przeskoczyc na rzadszy. */
    var tease by mutableStateOf(Rarity.Common)
        private set

    /** Karta odkryla juz wszystkie szczegoly - kolejne tapniecie chowa ja do kolekcji. */
    var cardReady by mutableStateOf(false)
        private set

    /** Karty x10 (po jednej animacji obrotu na karte). */
    var flips by mutableStateOf<List<Animatable<Float, AnimationVector1D>>>(emptyList())
        private set

    /** Ile kart wlasnie przybylo do kolekcji - dymek "+N" nad zakladka Collection. */
    var arrived by mutableStateOf(0)
        private set

    val best: Rarity get() = pulls.maxOfOrNull { it.rarity } ?: Rarity.Common

    /** Kompan, ktory wychodzi na oltarz: jedyny przy x1, najrzadszy przy x10. */
    val hero: MonsterCard? get() = pulls.maxByOrNull { it.rarity.ordinal }
    val heroFresh: Boolean get() = pulls.indexOf(hero).let { it >= 0 && fresh.getOrElse(it) { false } }
    val isMulti: Boolean get() = pulls.size > 1

    /** Czy przyciski i zakladki maja byc zablokowane. */
    val busy: Boolean get() = stage != ShowStage.Idle && stage != ShowStage.Done

    // ---- kanaly animacji (0..1, chyba ze opisano inaczej) ----------------------------------

    /** Przyciemnienie wszystkiego poza oltarzem. */
    val dim = Animatable(0f)
    /** Najazd kamery na oltarz. */
    val zoom = Animatable(0f)
    /** Krotkie "szarpniecie" kamery w chwili wybuchu. */
    val punch = Animatable(0f)
    /** Zbieranie energii: czasteczki splywajace do kuli, rosnaca kula. */
    val gather = Animatable(0f)
    /** Kula zapada sie tuz przed wybuchem ("wdech"). */
    val inhale = Animatable(0f)
    /** Slup swiatla nad kregiem. */
    val beam = Animatable(0f)
    /** Magiczny krag na plycie oltarza. */
    val circle = Animatable(0f)
    /** Blysk przy przeskoku koloru slupa. */
    val tierPulse = Animatable(0f)
    /** Bialy blysk wybuchu (1 -> 0). */
    val flash = Animatable(0f)
    /** Fala uderzeniowa i iskry po wybuchu. */
    val burst = Animatable(0f)
    /** Amplituda trzesienia ekranem w dp. */
    val shake = Animatable(0f)
    /** Wyjscie kompana z kregu. */
    val rise = Animatable(0f)
    /** Ladowanie na plycie - kurz spod stop. */
    val land = Animatable(0f)
    /** Sylweta -> kolor. */
    val fill = Animatable(0f)
    /** Wskazowka zywiolu przy sylwecie. */
    val clue = Animatable(0f)
    /** Obracajace sie promienie za kompanem. */
    val rays = Animatable(0f)
    /** Napis "EPIC!" / "LEGENDARY!" (0 -> 1 = wejscie, trzymanie, wyjscie). */
    val shout = Animatable(0f)
    /** Deszcz konfetti przy legendzie (0 -> 1 = caly spadek). */
    val confetti = Animatable(0f)
    /** Karta leci z kompana na srodek ekranu. */
    val card = Animatable(0f)
    /** Obrot karty z rewersu na awers. */
    val flip = Animatable(0f)
    /** Szczegoly karty odkrywane po kolei: 0..[DETAIL_STEPS]. */
    val details = Animatable(0f)
    /** Karta (albo karty x10) odlatuje do zakladki kolekcji. */
    val stow = Animatable(0f)
    /** x10: rozdanie kart z kregu na stol. */
    val deal = Animatable(0f)
    /** Podskok zakladki Collection, gdy wpadaja do niej karty (0 -> 1; poza tym zakresem nieaktywny). */
    val collectionBump = Animatable(0f)

    private var job: Job? = null
    private var revealing = false
    private var stowing = false

    // ---- wejscia z ekranu ------------------------------------------------------------------

    fun start(rolled: List<MonsterCard>, freshFlags: List<Boolean>) {
        if (rolled.isEmpty()) return
        job?.cancel()
        pulls = rolled
        fresh = freshFlags
        flips = List(rolled.size) { Animatable(0f) }
        job = scope.launch {
            reset()
            stage = ShowStage.Ritual
            ritual()
            if (isMulti) {
                deal()
            } else {
                walkout()
                presentCard()
            }
        }
    }

    /** Tapniecie w trakcie rytualu albo wyjscia - od razu do wyniku. */
    fun skip() {
        if (stage != ShowStage.Ritual && stage != ShowStage.Walkout) return
        job?.cancel()
        job = scope.launch {
            settleRitual()
            if (isMulti) {
                deal()
            } else {
                rise.snapTo(1f)
                land.snapTo(1f)
                fill.snapTo(1f)
                clue.snapTo(0f)
                shout.snapTo(1f)
                rays.snapTo(if (best >= Rarity.Rare) 1f else 0f)
                if (best == Rarity.Legendary) rainConfetti()
                presentCard()
            }
        }
    }

    /** Tapniecie w karte: najpierw dokancza odkrywanie, potem chowa karte do kolekcji. */
    fun tapCard() {
        if (stage != ShowStage.Card || stowing) return
        if (!cardReady) {
            job?.cancel()
            job = scope.launch {
                card.snapTo(1f)
                flip.snapTo(1f)
                details.snapTo(DETAIL_STEPS.toFloat())
                clue.snapTo(0f)
                shout.snapTo(1f)
                cardReady = true
            }
            return
        }
        stowing = true
        job = scope.launch {
            Sounds.play(Sfx.Whoosh)
            // Scena rozjasnia sie JUZ w locie - zakladka musi byc widoczna, gdy karta do niej wpada.
            launch { dim.animateTo(0f, tween(STOW_MS)) }
            stow.animateTo(1f, tween(STOW_MS, easing = EaseInCubic))
            bumpCollection(1)
            rest()
        }
    }

    /** x10: odkrycie jednej karty (tapniecie w nia). */
    fun flipCard(index: Int) {
        if (stage != ShowStage.Grid || deal.value < 1f) return
        val anim = flips.getOrNull(index) ?: return
        if (anim.targetValue >= 1f) return
        scope.launch { turnOver(index) }
    }

    /** x10: odkrycie wszystkich - od najslabszej do najlepszej, na lepsze karty jest pauza. */
    fun revealAll() {
        if (stage != ShowStage.Grid || deal.value < 1f || revealing) return
        revealing = true
        scope.launch {
            val order = pulls.indices
                .filter { flips[it].targetValue < 1f }
                .sortedBy { pulls[it].rarity.ordinal }
            for (index in order) {
                val big = pulls[index].rarity >= Rarity.Epic
                if (big) delay(420)
                launch { turnOver(index) }
                delay(if (big) 700L else 150L)
            }
            revealing = false
        }
    }

    val allFlipped: Boolean get() = flips.isNotEmpty() && flips.all { it.value >= 0.999f }

    /** x10: karty leca do kolekcji, a najlepszy kompan wychodzi na oltarz. */
    fun finishGrid() {
        if (stage != ShowStage.Grid || stowing) return
        stowing = true
        job = scope.launch {
            Sounds.play(Sfx.Whoosh)
            launch { dim.animateTo(0f, tween(STOW_MS + 180)) }
            stow.animateTo(1f, tween(STOW_MS + 180, easing = EaseInCubic))
            bumpCollection(pulls.size)
            rise.snapTo(0f)
            land.snapTo(0f)
            fill.snapTo(1f)
            stage = ShowStage.Done
            launch { rise.animateTo(1f, tween(560, easing = EaseOutBack)) }
            launch { delay(330); land.animateTo(1f, tween(600)) }
            rest()
        }
    }

    // ---- os czasu ------------------------------------------------------------------------

    private suspend fun reset() {
        listOf(
            dim, zoom, punch, gather, inhale, beam, circle, tierPulse, flash, burst, shake, rise,
            land, fill, clue, rays, shout, confetti, card, flip, details, stow, deal
        ).forEach { it.snapTo(0f) }
        tease = Rarity.Common
        cardReady = false
        revealing = false
        stowing = false
    }

    /** Zbieranie energii, slup (z mozliwym przeskokiem koloru), wdech i wybuch. */
    private suspend fun CoroutineScope.ritual() {
        val steps = teaseSteps(best)
        tease = steps.first()
        val surgeMs = SURGE_FIRST_MS + (steps.size - 1) * SURGE_STEP_MS
        val impactAt = GATHER_MS + surgeMs + INHALE_MS

        // Narastajacy dzwiek konczy sie DOKLADNIE w chwili wybuchu, niezaleznie od tego, ile razy
        // slup zmieni kolor po drodze.
        launch {
            delay((impactAt - CHARGE_SFX_MS).coerceAtLeast(0).toLong())
            Sounds.play(Sfx.SummonCharge)
        }
        launch { dim.animateTo(1f, tween(520)) }
        launch { zoom.animateTo(1f, tween(GATHER_MS + surgeMs, easing = FastOutSlowInEasing)) }
        launch { shake.animateTo(1.2f, tween(GATHER_MS, easing = LinearEasing)) }
        gather.animateTo(1f, tween(GATHER_MS, easing = EaseInCubic))

        // Slup strzela w niebo.
        Sounds.play(Sfx.Whoosh)
        launch { beam.animateTo(1f, tween(300, easing = FastOutSlowInEasing)) }
        launch { circle.animateTo(1f, tween(380)) }
        launch { kick(3f, 260) }
        delay(SURGE_FIRST_MS.toLong())

        // Przeskoki koloru - kazdy z wlasnym blyskiem, dzwiekiem i szarpnieciem.
        for (next in steps.drop(1)) {
            tease = next
            Sounds.play(Sfx.SummonTier)
            launch { tierPulse.snapTo(1f); tierPulse.animateTo(0f, tween(560)) }
            launch { kick(4f + next.ordinal * 1.6f, 420) }
            delay(SURGE_STEP_MS.toLong())
        }

        // Wdech i wybuch.
        inhale.animateTo(1f, tween(INHALE_MS, easing = EaseInCubic))
        Sounds.play(Sfx.SummonBoom)
        flash.snapTo(1f)
        launch { flash.animateTo(0f, tween(560, easing = LinearOutSlowInEasing)) }
        launch { burst.animateTo(1f, tween(BURST_MS, easing = LinearEasing)) }
        launch { punch.snapTo(1f); punch.animateTo(0f, tween(480, easing = FastOutSlowInEasing)) }
        launch { kick(9f + 3.5f * best.ordinal, 700) }
        launch { beam.animateTo(0.5f, tween(520)) }
    }

    /** Stan sceny zaraz po wybuchu - do przewijania tapnieciem. */
    private suspend fun settleRitual() {
        tease = best
        dim.snapTo(1f)
        zoom.snapTo(1f)
        gather.snapTo(1f)
        inhale.snapTo(1f)
        circle.snapTo(1f)
        beam.snapTo(0f)
        tierPulse.snapTo(0f)
        flash.snapTo(0f)
        burst.snapTo(1f)
        punch.snapTo(0f)
        shake.snapTo(0f)
    }

    /** x1: sylweta w slupie, wskazowka zywiolu, kolor, okrzyk rzadkosci. */
    private suspend fun CoroutineScope.walkout() {
        val hero = hero ?: return
        val rarity = hero.rarity
        stage = ShowStage.Walkout
        launch { rise.animateTo(1f, tween(RISE_MS, easing = EaseOutBack)) }
        launch { delay((RISE_MS * 0.55f).toLong()); land.animateTo(1f, tween(620)) }

        if (rarity >= Rarity.Rare) {
            delay(300)
            Sounds.play(Sfx.Hit)
            launch { clue.animateTo(1f, tween(420, easing = EaseOutBack)) }
            delay(silhouetteHold(rarity))
        } else {
            delay(RISE_MS - 200L + silhouetteHold(rarity))
        }

        // Kolor wraca - to jest moment "to on!".
        playReveal(rarity)
        launch { beam.animateTo(0f, tween(700)) }
        if (rarity >= Rarity.Rare) launch { rays.animateTo(1f, tween(700)) }
        launch { kick(3f + rarity.ordinal, 320) }
        fill.animateTo(1f, tween(FILL_MS, easing = FastOutSlowInEasing))

        if (rarity >= Rarity.Epic) {
            launch { shout.animateTo(1f, tween(SHOUT_MS, easing = LinearEasing)) }
            if (rarity == Rarity.Legendary) rainConfetti()
            delay(SHOUT_MS - 260L)
        } else {
            delay(380)
        }
    }

    /** Karta leci z kompana na srodek, obraca sie i odkrywa szczegoly jak w FIFA. */
    private suspend fun CoroutineScope.presentCard() {
        stage = ShowStage.Card
        Sounds.play(Sfx.Whoosh)
        launch { clue.animateTo(0f, tween(260)) }
        launch {
            delay(170)
            Sounds.play(Sfx.CardFlip)
            flip.animateTo(1f, tween(500, easing = FastOutSlowInEasing))
        }
        card.animateTo(1f, tween(620, easing = EaseOutBack))
        delay(80)
        for (step in 1..DETAIL_STEPS) {
            Sounds.play(if (step == DETAIL_STEPS && heroFresh) Sfx.Hit else Sfx.Tap)
            details.animateTo(step.toFloat(), tween(DETAIL_STEP_MS, easing = LinearEasing))
        }
        cardReady = true
    }

    /** x10: karty wylatuja z kregu i ukladaja sie na stole, rewersem do gory. */
    private suspend fun CoroutineScope.deal() {
        stage = ShowStage.Grid
        launch { beam.animateTo(0f, tween(600)) }
        launch { circle.animateTo(0.55f, tween(600)) }
        launch {
            repeat(4) {
                Sounds.play(Sfx.CardFlip)
                delay(190)
            }
        }
        deal.animateTo(1f, tween(DEAL_MS, easing = LinearEasing))
    }

    private suspend fun CoroutineScope.turnOver(index: Int) {
        val anim = flips.getOrNull(index) ?: return
        val rarity = pulls[index].rarity
        val big = rarity >= Rarity.Epic
        Sounds.play(Sfx.CardFlip)
        anim.animateTo(1f, tween(if (big) 560 else 320, easing = FastOutSlowInEasing))
        when (rarity) {
            Rarity.Legendary -> {
                playReveal(rarity)
                launch { kick(12f, 600) }
                launch { tierPulse.snapTo(0.8f); tierPulse.animateTo(0f, tween(600)) }
                rainConfetti()
            }
            Rarity.Epic -> {
                playReveal(rarity)
                launch { kick(7f, 420) }
                launch { tierPulse.snapTo(0.6f); tierPulse.animateTo(0f, tween(500)) }
            }
            Rarity.Rare -> Sounds.play(Sfx.Correct)
            else -> Unit
        }
    }

    /** Po pokazie: swiatla gasna, kamera wraca, kompan zostaje na oltarzu. */
    private suspend fun CoroutineScope.rest() {
        stage = ShowStage.Done
        launch { dim.animateTo(0f, tween(650)) }
        launch { zoom.animateTo(0f, tween(800, easing = FastOutSlowInEasing)) }
        launch { circle.animateTo(0f, tween(600)) }
        launch { rays.animateTo(if (best >= Rarity.Rare) RAYS_AT_REST else 0f, tween(800)) }
        stowing = false
    }

    private fun CoroutineScope.bumpCollection(count: Int) {
        arrived = count
        launch {
            collectionBump.snapTo(0f)
            collectionBump.animateTo(1f, tween(BUMP_MS, easing = LinearEasing))
        }
    }

    /** Konfetti leci niezaleznie od reszty - przewiniecie pokazu nie ma go urywac w polowie. */
    private fun rainConfetti() {
        scope.launch {
            confetti.snapTo(0f)
            confetti.animateTo(1f, tween(CONFETTI_MS, easing = LinearEasing))
        }
    }

    private suspend fun kick(amplitude: Float, ms: Int) {
        shake.snapTo(amplitude)
        shake.animateTo(0f, tween(ms, easing = LinearOutSlowInEasing))
    }

    private fun playReveal(rarity: Rarity) {
        when (rarity) {
            Rarity.Legendary -> {
                Sounds.play(Sfx.Victory)
                scope.launch { delay(260); Sounds.play(Sfx.Coins) }
            }
            Rarity.Epic -> Sounds.play(Sfx.LevelUp)
            else -> Sounds.play(Sfx.Summon)
        }
    }

    companion object {
        const val DETAIL_STEPS = 5
        const val RAYS_AT_REST = 0.45f

        private const val GATHER_MS = 1050
        private const val SURGE_FIRST_MS = 460
        private const val SURGE_STEP_MS = 640
        private const val INHALE_MS = 170
        private const val BURST_MS = 1150
        private const val RISE_MS = 720
        private const val FILL_MS = 400
        private const val SHOUT_MS = 1200
        private const val DETAIL_STEP_MS = 150
        private const val STOW_MS = 460
        private const val DEAL_MS = 950
        private const val CONFETTI_MS = 4200
        private const val BUMP_MS = 1200

        /** Dlugosc `summon_charge.wav` - koniec narastania ma trafic w wybuch. */
        private const val CHARGE_SFX_MS = 1630

        /** Ile sylweta stoi w swietle, zanim nabierze koloru - im rzadszy, tym dluzej trzyma w napieciu. */
        private fun silhouetteHold(rarity: Rarity): Long = when (rarity) {
            Rarity.Common -> 160
            Rarity.Uncommon -> 220
            Rarity.Rare -> 560
            Rarity.Epic -> 800
            Rarity.Legendary -> 1000
        }

        /**
         * Kolejne kolory slupa. Rzadsze zaczynaja nizej i "przeskakuja" - a czesc zwyklych tez
         * dostaje przeskok, zeby sam przeskok nie zdradzal od razu epika.
         */
        fun teaseSteps(best: Rarity, random: Random = Random): List<Rarity> = when (best) {
            Rarity.Common -> listOf(Rarity.Common)
            Rarity.Uncommon ->
                if (random.nextFloat() < 0.25f) listOf(Rarity.Common, Rarity.Uncommon) else listOf(Rarity.Uncommon)
            Rarity.Rare ->
                if (random.nextFloat() < 0.5f) listOf(Rarity.Uncommon, Rarity.Rare) else listOf(Rarity.Rare)
            Rarity.Epic ->
                if (random.nextFloat() < 0.3f) listOf(Rarity.Uncommon, Rarity.Rare, Rarity.Epic)
                else listOf(Rarity.Rare, Rarity.Epic)
            Rarity.Legendary -> listOf(Rarity.Rare, Rarity.Epic, Rarity.Legendary)
        }
    }
}
