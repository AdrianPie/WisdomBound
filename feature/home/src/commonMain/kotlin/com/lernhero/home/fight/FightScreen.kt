package com.lernhero.home.fight

import com.lernhero.home.tutorial.TutorialTarget
import com.lernhero.home.tutorial.tutorialTarget
import com.lernhero.shared.theme.GlyphText
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lernhero.domain.energy.EnergyRules
import com.lernhero.domain.monster.MonsterCard
import com.lernhero.domain.player.PartySlots
import com.lernhero.domain.run.RunLadder
import com.lernhero.game.character.HeroLook
import com.lernhero.game.character.KnightEquipment
import com.lernhero.game.character.ModularKnight
import com.lernhero.game.character.MonsterVisualPresets
import com.lernhero.shared.resources.Resources
import com.lernhero.shared.theme.FontFirst
import com.lernhero.shared.theme.FontPixel
import org.jetbrains.compose.resources.painterResource

/**
 * Ekran wyprawy: prawie pusty kadr z bohaterem w drodze, jedna wielka decyzja na dole.
 *
 * Poprzednia wersja byla panelem sterowania - siatka druzyny, zakladki trybu, ramka kampanii z
 * trzema przyciskami i rzedem plansz, wszystko naraz na jednym ekranie. Kazda z tych rzeczy byla
 * potrzebna raz na kilka wejsc, a zajmowaly caly ekran ZAWSZE; samo "zacznij grac" - czyli jedyna
 * rzecz, po ktora gracz tu wchodzi - bylo srednim przyciskiem posrodku tego natloku.
 *
 * Teraz jest odwrotnie: tresci tyle, ile trzeba (gdzie jestem, kim ide), reszta to miejsce na
 * scene, a START jest najwiekszym obiektem na ekranie. Ustawienia schodza pod male kafelki z boku.
 *
 * Miejsce na scene jest CELOWE, nie przypadkowe: pod bohatera wejdzie przewijane tlo, a obok niego
 * kompani - dlatego sylwetka stoi nisko i lekko na lewo, a caly srodek ekranu jest wolny.
 */
@Composable
fun FightScreen(
    knight: KnightEquipment,
    look: HeroLook,
    /** Nazwa miejsca, w ktore gracz idzie - naglowek nad scena. */
    place: String,
    /** Podtytul: numer poziomu drabinki albo planszy kampanii. */
    stage: String,
    /** Najlepszy wynik w tym trybie - "Best: wave 17". */
    best: String,
    mode: FightMode,
    energy: Int,
    /**
     * Postep w kampanii: ile plansz TEJ krainy gracz ma juz za soba i ile ich w niej jest w
     * ogole - z tego biora sie kropki pod przyciskiem. Wyprawa nie ma tu czego pokazac (nie ma
     * "krainy"), wiec w Endless kropki nie stoja wcale - patrz [topicLevel].
     */
    campaignStagesCleared: Int,
    campaignStagesTotal: Int,
    /**
     * Poziom opanowania wybranego tematu wyprawy - null, gdy material to nie temat (fiszki, wlasna
     * talia, wszystkie tematy naraz). W kampanii nigdy nie jest czytany.
     */
    topicLevel: Int? = null,
    /**
     * Warstwy przewijanego tla - patrz [ParallaxStage]. Pusta lista = sama scena bez tla, i tak
     * ma byc, dopoki nie ma grafik: ekran dziala, tylko za bohaterem nic nie jedzie.
     */
    backdrop: List<ParallaxLayer> = emptyList(),
    /**
     * Gdzie w oknie sceny znajduje sie grunt, po ktorym idzie druzyna - ulamek wysokosci okna
     * liczony od gory. To JEDYNE miejsce, w ktorym postac spotyka sie z tlem: deska mostu jest
     * namalowana w konkretnym miejscu grafiki, wiec stopy musza trafic dokladnie tam, inaczej
     * bohater plywa nad mostem albo tonie w lawie pod nim.
     */
    groundLine: Float = 0.70f,
    /**
     * Marginesy pod paski gry. Dotycza WYLACZNIE interfejsu - scena (tlo i bohater) idzie pod
     * pasek gorny i dolny, bo obraz urwany rowno pod paskiem zdradza, ze to obrazek w ramce,
     * a nie swiat, przez ktory sie idzie.
     */
    contentPadding: PaddingValues = PaddingValues(0.dp),
    /**
     * Miejsca kompanow - null to wolne miejsce, ktore da sie zapelnic tapnieciem. Lista jest
     * ZAWSZE pelnej dlugosci (tyle, ile miejsc ma druzyna), bo puste miejsce ma byc widoczne:
     * to ono mowi graczowi, ze idzie w teren slabszym skladem, niz moglby.
     */
    companions: List<MonsterCard?>,
    /** Poziom bohatera - z niego wynika, ktore miejsca sa juz otwarte (patrz [PartySlots]). */
    heroLevel: Int,
    levelOf: (String) -> Int,
    /**
     * Z czego beda pytania w tym wejsciu - ikona i nazwa na pigulce nad przyciskiem. Null chowa
     * pigulke w calosci: w kampanii o materiale decyduje plansza, wiec nie ma tu czego wybierac,
     * a martwy przelacznik jest gorszy niz jego brak.
     */
    materialIcon: String? = null,
    materialLabel: String? = null,
    onMaterial: () -> Unit = {},
    onMode: (FightMode) -> Unit,
    onStart: () -> Unit,
    /**
     * Otwiera wybor planszy kampanii ([com.lernhero.home.story.CampaignBoard]). Null w wyprawie -
     * drabinka nie ma plansz do wybrania, jej poziom wynika wprost z rekordu.
     *
     * To jest JEDYNE wejscie w to okno. START go nie otwiera: START ma zaczynac walke, i tylko to.
     */
    onOpenStages: (() -> Unit)? = null,
    onCompanion: (Int) -> Unit,
    onOpenParty: () -> Unit,
    onOpenMap: () -> Unit,
    modifier: Modifier = Modifier
) {
    val affordable = energy >= EnergyRules.BATTLE_COST

    Box(modifier = modifier.fillMaxSize()) {
        // Tlo jest TLEM CALEGO EKRANU, a nie obrazkiem w ramce posrodku. Wczesniej siedzialo w
        // pudelku sceny i konczylo sie nad przyciskiem - a wtedy nie da sie uwierzyc, ze druzyna
        // idzie przez jaskinie: widac bylo, gdzie jaskinia sie konczy. Teraz UI plywa NA scenie.
        if (backdrop.isNotEmpty()) {
            ParallaxStage(layers = backdrop, modifier = Modifier.matchParentSize())
        }

        // Bohater nalezy do sceny, nie do ukladu interfejsu - stad rysuje sie wprost na tle,
        // na linii gruntu liczonej z CALEJ wysokosci ekranu.
        WalkingHero(
            knight = knight,
            look = look,
            groundLine = groundLine,
            modifier = Modifier.matchParentSize()
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(contentPadding)
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // NAGLOWEK MA STALA WYSOKOSC - i to nie jest kosmetyka, tylko jedyny sposob, zeby
            // przelacznik CAMPAIGN/ENDLESS niczym nie ruszal.
            //
            // Wszystko pod tym blokiem (kafelki Party/Mapa, pasek kompanow) jest przyklejone do
            // GORY sekcji na weight(1f), czyli do dolnej krawedzi naglowka. Naglowek zmienia w
            // trybach cala swoja tresc: inna nazwa miejsca, inny podtytul, inne zdanie o lupie,
            // pigulka materialu raz z tekstem, raz pusta. Probowalem pilnowac tego od srodka -
            // jedna linia na tekst, sztywna wysokosc linii - i to ZA MALO: wysokosc tekstu zalezy
            // jeszcze od tego, ktory font system podstawi pod znaki, ktorych nie ma w foncie
            // pikselowym ("▾", "·", "×"), a tego z gory nie policze.
            //
            // Wiec nie licze. Blok dostaje wysokosc z gory, a jego tresc uklada sie w srodku: co by
            // sie w nim nie zmienilo, dolna krawedz stoi w tym samym miejscu co do piksela.
            Column(
                modifier = Modifier.fillMaxWidth().height(HEADER_HEIGHT),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                PlacePlate(place = place, stage = stage, best = best, onClick = onOpenStages)

                // Material stoi przy naglowku, a nie nad przyciskiem: tam zaslanialby bohatera
                // (scena jest wolna wlasnie po to, zeby bylo go widac), a tutaj dopisuje sie do
                // zdania, ktore naglowek juz mowi - dokad ide, jak daleko doszedlem, z czego bede
                // pytany. W kampanii nie ma jej czym wypelnic, wiec jest niewidoczna.
                MaterialPill(
                    icon = materialIcon ?: "📖",
                    label = materialLabel ?: "",
                    onClick = onMaterial,
                    enabled = materialLabel != null,
                    modifier = Modifier
                        .padding(top = 2.dp)
                        .alpha(if (materialLabel != null) 1f else 0f)
                )
            }

            // Pusta przestrzen miedzy naglowkiem a przyciskiem. Nic tu nie rysujemy - scena
            // jest POD calym ekranem; ten Box tylko odpycha przycisk na dol i trzyma kafelki
            // przy krawedziach.
            Box(modifier = Modifier.fillMaxWidth().weight(1f)) {
                Column(
                    modifier = Modifier.align(Alignment.TopStart),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    SideButton(
                        icon = "🛡",
                        label = "Party",
                        onClick = onOpenParty,
                        modifier = Modifier.tutorialTarget(TutorialTarget.PartyButton)
                    )
                    // Mapa wybiera KRAINE kampanii - w wyprawie nie ma jej czym wypelnic, bo
                    // wyprawa nie zna krain, tylko poziom drabinki. Przycisk, ktory nic by nie
                    // otwieral, jest gorszy niz jego brak - wiec w wyprawie go nie widac.
                    //
                    // Ale MIEJSCE po nim zostaje zarezerwowane (alpha 0, bez dotyku), dokladnie tak
                    // jak przy pigulce materialu. Kafelek, ktory znika razem ze swoja wysokoscia,
                    // przebudowuje kolumne przy kazdym tapnieciu CAMPAIGN/ENDLESS - a wtedy zwykle
                    // przelaczenie trybu wyglada jak przeladowanie polowy ekranu.
                    val hasMap = mode == FightMode.Campaign
                    SideButton(
                        icon = "🗺",
                        label = "Map",
                        onClick = onOpenMap,
                        enabled = hasMap,
                        modifier = Modifier.alpha(if (hasMap) 1f else 0f)
                    )
                }

                // Sklad idzie z bohaterem, wiec widac go NA SCENIE, a nie w osobnym panelu.
                CompanionStrip(
                    companions = companions,
                    heroLevel = heroLevel,
                    levelOf = levelOf,
                    onClick = onCompanion,
                    modifier = Modifier.align(Alignment.TopEnd)
                )
            }

            // Zarezerwowana na STALE wysokosc - to, co w niej stoi, zmienia sie miedzy trybami
            // (kropki kampanii kontra poziom tematu), ale sama wysokosc nie, bo siedzi PRZED
            // przyciskiem START na dnie kolumny: gdyby znikala calkiem, "weight(1f)" nad nia
            // urosloby o tyle samo i START wjezdzalby w inne miejsce przy kazdym przelaczeniu
            // CAMPAIGN/ENDLESS - dokladnie ten sam blad, ktory juz raz naprawilismy przy pigulce
            // materialu.
            Box(
                modifier = Modifier
                    .padding(top = 4.dp)
                    .height(FOOTER_HEIGHT),
                contentAlignment = Alignment.Center
            ) {
                when {
                    mode == FightMode.Campaign -> RunDots(
                        filled = campaignStagesCleared,
                        total = campaignStagesTotal,
                        onClick = onOpenStages
                    )
                    topicLevel != null -> TopicLevelBadge(level = topicLevel)
                    // Wyprawa bez wybranego tematu (fiszki, wlasna talia, wszystkie tematy) -
                    // nie ma tu czego pokazac, ale miejsce zostaje puste, nie zwiniete.
                }
            }

            Row(
                modifier = Modifier.padding(top = 4.dp, bottom = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FightMode.entries.forEach { entry ->
                    ModeChip(
                        mode = entry,
                        selected = entry == mode,
                        onClick = { onMode(entry) }
                    )
                }
            }
            // START na samym dole, pod przelacznikiem trybu. Stal nad nim i na nizszych ekranach
            // (stare telefony z paskiem 3 przyciskow) zaslanial idacego bohatera - teraz caly
            // srodek ekranu zostaje dla sceny.
            StartButton(
                label = "START",
                cost = EnergyRules.BATTLE_COST,
                enabled = affordable,
                onClick = onStart,
                modifier = Modifier.fillMaxWidth(0.82f)
            )
        }
    }
}

/** Tryb, ktory odpali START. Dwa i tylko dwa - wybor ma byc jednym tapnieciem, nie menu. */
enum class FightMode(val label: String) {
    Campaign("CAMPAIGN"),
    Endless("ENDLESS")
}

/**
 * Naglowek sceny: gdzie idziemy i jak daleko doszlismy. Trzy linijki, bo tyle wystarczy - reszta
 * (lista plansz, lup, tiery) siedzi na Mapie, do ktorej jest kafelek z boku.
 *
 * Kazda z tych trzech linijek jest przyciete do JEDNEJ linii. Tresc zmienia sie miedzy trybami
 * ("Endless Depths" / "Best: wave 9" kontra nazwa krainy / "cleared x3 - loot 30%"), a tekst, ktory
 * przy dluzszej nazwie zawinie sie na dwie linie, podnosi cala tabliczke - czyli samo przelaczenie
 * CAMPAIGN/ENDLESS przesuwa naglowek i pigulke materialu pod nim. Wielokropek jest tanszy niz
 * skaczacy uklad.
 */
@Composable
private fun PlacePlate(place: String, stage: String, best: String, onClick: (() -> Unit)?) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        GlyphText(
            text = stage,
            color = PlateMuted,
            fontFamily = FontFirst(),
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp,
            lineHeight = 15.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        GlyphText(
            // Strzalka mowi, ze w tabliczke da sie tapnac - tak samo jak w pigulce materialu.
            text = if (onClick != null) "$place ▾" else place,
            color = Color.White,
            fontFamily = FontPixel(),
            fontWeight = FontWeight.Bold,
            fontSize = 22.sp,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            style = TextStyle(
                shadow = Shadow(color = Color(0xFF120D09), blurRadius = 8f),
                lineHeight = PLACE_LINE_HEIGHT,
                // Stala wysokosc linii + Trim.None: wysokosc nie zalezy od KONKRETNYCH znakow.
                // (includeFontPadding jest API tylko Androida, a tam i tak domyslnie false.)
                lineHeightStyle = LineHeightStyle(
                    alignment = LineHeightStyle.Alignment.Center,
                    trim = LineHeightStyle.Trim.None
                )
            )
        )
        GlyphText(
            text = best,
            color = PlateAccent,
            fontFamily = FontFirst(),
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp,
            lineHeight = 16.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

/**
 * Stala wysokosc linii nazwy miejsca - i to jest wlasnie ten milimetr, o ktory skakal caly naglowek.
 *
 * Domyslnie wysokosc linii liczy sie z metryk znakow, ktore realnie w niej stoja. W kampanii nazwa
 * konczy sie strzalka "▾", ktorej nie ma w foncie pikselowym - system podstawia pod nia wlasny
 * krój, a ten ma inne metryki niz reszta napisu. Linia robila sie przez to o kilka dp wyzsza niz w
 * wyprawie, tabliczka rosla, a wszystko pod nia - pigulka materialu, kafelki Party/Map i pasek
 * kompanow - jechalo w dol. Przy powrocie do wyprawy wracalo w gore, i to widac golym okiem.
 *
 * Sztywna wysokosc linii odcina te zaleznosc: linia jest tak samo wysoka niezaleznie od tego, jakie
 * znaki sie w niej znajda.
 */
private val PLACE_LINE_HEIGHT = 28.sp

/**
 * Pionowy pasek miejsc druzyny przy prawej krawedzi sceny.
 *
 * Maly, bo ma nie konkurowac ze scena ani z przyciskiem - niesie jedna informacje ("kto ze mna
 * idzie") i jedno wejscie (tapniecie = zmiana skladu). Miejsce zamkniete pokazuje poziom, ktory
 * je otworzy: pusta klodka bez liczby mowilaby tylko "nie wolno", a ma mowic "jeszcze nie teraz".
 */
@Composable
private fun CompanionStrip(
    companions: List<MonsterCard?>,
    heroLevel: Int,
    levelOf: (String) -> Int,
    onClick: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(7.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        companions.forEachIndexed { index, card ->
            val unlocked = PartySlots.isUnlocked(index, heroLevel)
            Box(
                modifier = Modifier
                    .size(MINI_SLOT)
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (unlocked) (if (card != null) MiniFill else MiniEmpty) else Color(0xCC0E0A07))
                    .border(
                        width = if (card != null) 1.5.dp else 1.dp,
                        color = if (card != null) MiniEdgeOn else MiniEdge,
                        shape = RoundedCornerShape(12.dp)
                    )
                    // Zamkniete tez reaguje - sklad pokazuje wtedy, od ktorego poziomu sie otwiera.
                    .clickable { onClick(index) },
                contentAlignment = Alignment.Center
            ) {
                when {
                    // Klodka + "Lv N": sama liczba nic nie mowila - wygladala jak licznik.
                    !unlocked -> Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        GlyphText(text = "🔒", fontSize = 15.sp, modifier = Modifier.alpha(0.8f))
                        GlyphText(
                            text = "Lv ${PartySlots.requiredLevel(index)}",
                            color = PlateMuted,
                            fontFamily = FontFirst(),
                            fontWeight = FontWeight.Bold,
                            fontSize = 9.sp
                        )
                    }

                    card == null -> GlyphText(
                        text = "+",
                        color = PlateMuted,
                        fontFamily = FontFirst(),
                        fontWeight = FontWeight.Bold,
                        fontSize = 22.sp
                    )

                    else -> {
                        Image(
                            painter = painterResource(
                                MonsterVisualPresets.forSpecies(card.spriteKey).drawable
                            ),
                            contentDescription = card.name,
                            modifier = Modifier.fillMaxSize().padding(3.dp)
                        )
                        GlyphText(
                            text = "${levelOf(card.id)}",
                            color = Color.White,
                            fontFamily = FontFirst(),
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp,
                            style = TextStyle(shadow = Shadow(color = Color(0xFF120D09), blurRadius = 4f)),
                            modifier = Modifier.align(Alignment.BottomEnd).padding(horizontal = 3.dp)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Bohater w marszu - bez klatek animacji chodu, ktorych sprite nie ma.
 *
 * Ruch sklada sie z trzech tanich skladowych o ROZNYCH okresach: podskok (gora-dol), przechyl
 * (obrot o ulamek stopnia) i dryf w bok. Rozne okresy sa tu calym trikiem - gdyby byly rowne,
 * oko zobaczyloby jedna petle i postac wygladalaby, jakby drgala w miejscu. Tak zlozone daje
 * wrazenie kroku, a kosztuje trzy interpolacje.
 */
@Composable
private fun WalkingHero(
    knight: KnightEquipment,
    look: HeroLook,
    groundLine: Float,
    modifier: Modifier = Modifier
) {
    val walk = rememberInfiniteTransition(label = "walk")
    val bob by walk.animateFloat(
        initialValue = -1f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(520, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bob"
    )
    val tilt by walk.animateFloat(
        initialValue = -1.6f,
        targetValue = 1.6f,
        animationSpec = infiniteRepeatable(
            animation = tween(880, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "tilt"
    )
    val drift by walk.animateFloat(
        initialValue = -1f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1700, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "drift"
    )

    BoxWithConstraints(modifier = modifier, contentAlignment = Alignment.TopCenter) {
        val size = minOf(maxWidth * 0.36f, 170.dp)
        // Stopy laduja na linii gruntu, wiec pudelko postaci konczy sie dokladnie na niej.
        val top = maxHeight * groundLine - size

        Box(
            modifier = Modifier.offset(y = top),
            contentAlignment = Alignment.BottomCenter
        ) {
            // Cien zostaje na ziemi i tylko lekko pulsuje - to on sprawia, ze podskok czyta sie
            // jako krok, a nie jako lewitacja calej postaci.
            Box(
                modifier = Modifier
                    .offset(y = (-6).dp, x = (drift * 3).dp)
                    .width(size * (0.42f - bob * 0.02f))
                    .height(size * 0.05f)
                    .clip(RoundedCornerShape(50))
                    .background(Color(0x44000000))
            )
            ModularKnight(
                equipment = knight,
                look = look,
                sizeDp = size,
                interactive = false,
                modifier = Modifier
                    .offset(x = (drift * 5).dp, y = (bob * 3).dp)
                    .rotate(tilt)
            )
        }
    }
}

/**
 * Wielki przycisk startu - wciskany "na glebokosc", jak w grach mobilnych.
 *
 * Efekt daje CIEMNA PLYTA pod licem przycisku: lico lezy na niej przesuniete o [DEPTH] w gore, a
 * wcisniecie zjezdza nim na plyte. Nie ma tu zadnego cienia ani obrazka - to sama geometria,
 * dlatego dziala w kazdym kolorze i na kazdym tle, i dlatego nie trzeba tego animowac recznie.
 */
@Composable
private fun StartButton(
    label: String,
    cost: Int,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val sink by animateDpAsState(
        if (pressed && enabled) DEPTH else 0.dp,
        tween(70),
        label = "sink"
    )

    val face = if (enabled) FaceTop to FaceBottom else DisabledTop to DisabledBottom
    val base = if (enabled) BaseShade else DisabledBase

    Box(
        modifier = modifier
            .height(BUTTON_HEIGHT + DEPTH)
            .tutorialTarget(TutorialTarget.StartButton)
            // Klikalny ZAWSZE: bez energii tapniecie otwiera okno doladowania (patrz
            // [com.lernhero.home.ui.EnergyRefillDialog]), a nie martwy, szary przycisk.
            .clickable(
                interactionSource = interaction,
                indication = null,
                onClick = onClick
            )
    ) {
        // Plyta - widac ja tylko jako pasek pod licem, i to ona jest "glebia" przycisku.
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .height(BUTTON_HEIGHT)
                .clip(RoundedCornerShape(18.dp))
                .background(base)
        )
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .offset(y = sink)
                .fillMaxWidth()
                .height(BUTTON_HEIGHT)
                .clip(RoundedCornerShape(18.dp))
                .background(Brush.verticalGradient(listOf(face.first, face.second)))
                .border(2.5.dp, if (enabled) FaceEdge else DisabledBase, RoundedCornerShape(18.dp)),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(1.dp)
            ) {
                GlyphText(
                    text = label,
                    color = if (enabled) Color(0xFF3A2405) else Color(0xFF6B6250),
                    fontFamily = FontPixel(),
                    fontWeight = FontWeight.Bold,
                    fontSize = 26.sp
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(3.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    GlyphText(text = "⚡", fontSize = 13.sp)
                    GlyphText(
                        text = "$cost",
                        color = if (enabled) Color(0xFF3A2405) else Color(0xFF6B6250),
                        fontFamily = FontFirst(),
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}

/**
 * Kropki postepu - gdzie w tej krainie jestesmy. Uzywane WYLACZNIE w kampanii: [total] to liczba
 * plansz krainy ([com.lernhero.domain.story.StoryWorld.stageCount]), [filled] to ile ich gracz ma
 * juz za soba, ostatnia kropka to zawsze boss ([com.lernhero.domain.story.StoryStageCatalog.isBossStage]
 * jest prawda tylko dla ostatniej planszy krainy).
 *
 * Kropka, a nie pasek, bo postep jest SKOKOWY: liczy sie doczyszczenie planszy, a nie ulamek fali -
 * pasek obiecywalby ciaglosc, ktorej tu nie ma.
 *
 * Mikro rozmiar jest celowy: to znacznik polozenia, a nie tabela wynikow. Wszystko, co trzeba z
 * niego wyczytac, to "ile jeszcze do bossa".
 */
@Composable
private fun RunDots(
    filled: Int,
    total: Int,
    modifier: Modifier = Modifier,
    /** Kropki sa drugim (obok tabliczki) wejsciem w wybor planszy - to o nich mysli sie "gdzie jestem". */
    onClick: (() -> Unit)? = null
) {
    if (total <= 0) return

    Row(
        modifier = modifier
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(horizontal = 8.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(total) { index ->
            val done = index < filled
            val boss = index == total - 1
            Box(
                modifier = Modifier
                    .size(if (boss) 14.dp else 10.dp)
                    .clip(RoundedCornerShape(50))
                    .background(if (done) DotOn else DotOff)
                    .border(
                        width = if (boss) 2.dp else 1.5.dp,
                        color = if (done) DotEdgeOn else DotEdge,
                        shape = RoundedCornerShape(50)
                    ),
                contentAlignment = Alignment.Center
            ) {
                // Bossa znaczy korona, a nie inny kolor - kolor niesie juz "przeszedlem / nie".
                if (boss) {
                    GlyphText(text = "\uD83D\uDC51", fontSize = 7.sp)
                }
            }
        }
    }
}

/**
 * Poziom wybranego tematu wyprawy - miejsce po kropkach kampanii, ktorych wyprawa nie ma (nie ma
 * "krainy" do policzenia). To ten sam poziom, co w [com.lernhero.home.fight.MaterialSheet]: od
 * niego rusza trudnosc pytan i on placi bonus do nagrod (patrz [com.lernhero.domain.progress.CategoryMastery]).
 */
@Composable
private fun TopicLevelBadge(level: Int, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(50))
            .background(ChipOff)
            .border(1.5.dp, ChipEdge, RoundedCornerShape(50))
            .padding(horizontal = 14.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(5.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        GlyphText(text = "⭐", fontSize = 11.sp)
        GlyphText(
            text = "Level $level",
            color = PlateAccent,
            fontFamily = FontFirst(),
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp
        )
    }
}

/**
 * Pigulka materialu: z czego bedzie quiz. Wyglada jak przycisk, bo nim jest - tapniecie otwiera
 * liste ([com.lernhero.home.fight.MaterialSheet]).
 *
 * Pokazuje WYBOR, nie kategorie ustawienia ("Biology", nie "Topic: Biology"): nazwa tematu albo
 * talii jest jedyna informacja, ktorej gracz tu szuka, a slowo "temat" powtarzalo tylko to, co
 * ikona juz mowi.
 */
@Composable
private fun MaterialPill(
    icon: String,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    /** false w kampanii - pigulka rezerwuje miejsce, ale nie reaguje na dotyk ani nie czyta sie. */
    enabled: Boolean = true
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(50))
            .background(ChipOff)
            .border(1.5.dp, ChipEdge, RoundedCornerShape(50))
            .clickable(enabled = enabled, onClick = onClick)
            .padding(start = 12.dp, end = 10.dp, top = 6.dp, bottom = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        GlyphText(text = icon, fontSize = 12.sp)
        GlyphText(
            text = label,
            color = PlateAccent,
            fontFamily = FontFirst(),
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.widthIn(max = 180.dp)
        )
        // Strzalka w dol, a nie olowek: lista otwiera sie POD pigulka i tak sie ja czyta.
        GlyphText(text = "▾", color = PlateMuted, fontFamily = FontFirst(), fontSize = 12.sp)
    }
}

/** Maly przelacznik trybu pod przyciskiem - dwa stany, zero tlumaczenia. */
@Composable
private fun ModeChip(mode: FightMode, selected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(if (selected) ChipOn else ChipOff)
            .border(
                width = if (selected) 2.dp else 1.5.dp,
                color = if (selected) ChipEdgeOn else ChipEdge,
                shape = RoundedCornerShape(50)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 18.dp, vertical = 7.dp)
    ) {
        GlyphText(
            text = mode.label,
            color = if (selected) Color(0xFF2A1C06) else Color(0xFFB9A98A),
            fontFamily = FontFirst(),
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp
        )
    }
}

/** Kafelek z boku sceny - wejscie do rzeczy, ktore rusza sie rzadko (druzyna, mapa). */
@Composable
private fun SideButton(
    icon: String,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    /** false = kafelek tylko trzyma swoje miejsce w kolumnie i nie reaguje na dotyk. */
    enabled: Boolean = true
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Box(
            modifier = Modifier
                .size(46.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(SideFill)
                .border(2.dp, SideEdge, RoundedCornerShape(14.dp))
                .clickable(enabled = enabled, onClick = onClick),
            contentAlignment = Alignment.Center
        ) {
            GlyphText(text = icon, fontSize = 20.sp)
        }
        GlyphText(
            text = label,
            color = PlateMuted,
            fontFamily = FontFirst(),
            fontWeight = FontWeight.Bold,
            fontSize = 10.sp,
            style = TextStyle(shadow = Shadow(color = Color(0xFF120D09), blurRadius = 4f))
        )
    }
}

/** Bok miniatury kompana. Male male - patrz [CompanionStrip]. */
private val MINI_SLOT = 46.dp

private val MiniFill = Color(0xCC241B12)
private val MiniEmpty = Color(0x99140F0A)
private val MiniEdge = Color(0x996B5A38)
private val MiniEdgeOn = Color(0xFFC8A45A)

/** Ile "glebokosci" ma przycisk - o tyle zjezdza lico przy wcisnieciu. */
private val DEPTH = 6.dp
private val BUTTON_HEIGHT = 76.dp

/** Wysokosc slotu pod przyciskiem START - patrz komentarz przy jego uzyciu w [FightScreen]. */
private val FOOTER_HEIGHT = 26.dp

/**
 * Wysokosc calego naglowka sceny (tabliczka miejsca + pigulka materialu) - patrz komentarz przy
 * jego uzyciu w [FightScreen]. Z zapasem nad realna wysokoscia tresci: lepiej kilka dp pustki,
 * ktorej na scenie nie widac, niz uklad, ktory rusza sie przy przelaczeniu trybu.
 */
private val HEADER_HEIGHT = 112.dp

private val FaceTop = Color(0xFFFFDE72)
private val FaceBottom = Color(0xFFF2B32B)
private val FaceEdge = Color(0xFFFFF2C4)
private val BaseShade = Color(0xFF8A5A12)
private val DisabledTop = Color(0xFF6E6555)
private val DisabledBottom = Color(0xFF574F42)
private val DisabledBase = Color(0xFF3B352C)

private val ChipOn = Color(0xFFF2C14B)
private val ChipOff = Color(0xCC1C1610)
private val ChipEdge = Color(0xFF6B5A38)
private val ChipEdgeOn = Color(0xFFFFF2C4)
private val SideFill = Color(0xCC1C1610)
private val SideEdge = Color(0xFF6B5A38)
private val DotOn = Color(0xFFF2C14B)
private val DotOff = Color(0x99140F0A)
private val DotEdge = Color(0xFF6B5A38)
private val DotEdgeOn = Color(0xFFFFF2C4)
private val PlateMuted = Color(0xFFB9A98A)
private val PlateAccent = Color(0xFFFFD34D)

/**
 * Gotowy zestaw warstw jaskini z lawa - kolejnosc OD NAJDALSZEJ, bo tak sie je rysuje.
 *
 * Wysokosci sa ulamkami wysokosci okna, a nie pikselami: dzieki temu ten sam zestaw sklada sie
 * tak samo na kazdym telefonie, a grafik nie trzeba przycinac pod konkretny ekran. Kazda warstwa
 * siedzi na INNEJ wysokosci - to one buduja glebie, samo tempo by nie wystarczylo.
 *
 * Wszystkie ida w trybie [TileMode.Mirror], bo zadna nie byla rysowana jako kafel laczacy sie
 * sam ze soba. Odbicie daje szew ZAWSZE, kosztem symetrii wzoru - na dalekich warstwach nikt jej
 * nie wylapie, a na moscie o to wlasnie chodzilo.
 */
val LavaBackdrop: List<ParallaxLayer>
    @Composable get() = listOf(
        ParallaxLayer(
            image = Resources.Backdrop.lavaFar,
            speed = 0.13f,
            mode = TileMode.Mirror,
            heightFraction = 1.1f
        ),
        ParallaxLayer(
            image = Resources.Backdrop.lavaMid,
            speed = 0.37f,
            mode = TileMode.Mirror,
            heightFraction = 0.42f,
            verticalBias = -0.24f
        ),
        ParallaxLayer(
            image = Resources.Backdrop.lavaBridge,
            speed = 1f,
            mode = TileMode.Mirror,
            heightFraction = 0.35f,
            verticalBias = -0.08f
        ),
        ParallaxLayer(
            image = Resources.Backdrop.lavaNear,
            speed = 1.71f,
            mode = TileMode.Mirror,
            heightFraction = 0.22f,
            verticalBias = -0.06f
        )
    )

/** Nazwa poziomu drabinki dla naglowka - "Tier 3 · wave 1-20". */
fun ladderStageLabel(tier: Int): String = "Tier ${tier + 1} · waves 1-${RunLadder.WAVES_PER_TIER}"
