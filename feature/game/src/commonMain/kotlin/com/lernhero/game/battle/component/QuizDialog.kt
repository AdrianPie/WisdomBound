package com.lernhero.game.battle.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.EaseInOutSine
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.width
import androidx.compose.ui.graphics.graphicsLayer
import kotlin.math.roundToInt
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameMillis
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lernhero.shared.resources.Resources
import org.jetbrains.compose.resources.painterResource
import com.lernhero.domain.combat.live.AnswerTimer
import com.lernhero.domain.combat.live.StreakTracker
import com.lernhero.game.battle.BattleSpeed
import com.lernhero.game.battle.BattleUiState
import com.lernhero.game.battle.LiveQuizState
import com.lernhero.game.battle.MultiSelectQuestion
import com.lernhero.game.battle.MultipleChoiceQuestion
import com.lernhero.game.battle.OrderingQuestion
import com.lernhero.game.battle.QuizAnswer
import com.lernhero.game.battle.QuizQuestion
import com.lernhero.game.battle.TurnPhase
import com.lernhero.shared.theme.FontFirst
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlin.time.TimeSource

/**
 * Paleta jasnego, "kartowego" panelu quizu - odbite od ciemnej, zlotej reszty gry, zeby pytanie
 * wyraznie odcinalo sie od pola bitwy i bylo jak najlatwiej czytelne. `internal`, bo tej samej
 * "kartowej" oprawy uzywa tez [com.lernhero.game.battle.component.BossEventQuizBar] - specjalne
 * zaklecie bossa ma wygladac jak normalny quiz, tylko z inna zawartoscia w srodku.
 */
/**
 * Czas jednego, wspolnego przejscia calej sceny walki: arena kurczy sie i oddaje miejsce, a panel
 * quizu w tym samym rytmie wjezdza z dolu. Jedna wartosc dla obu, zeby to byl jeden ruch kamery,
 * a nie dwie osobne animacje - patrz [com.lernhero.game.battle.BattleScreen].
 */
internal const val SCENE_TRANSITION_MS = 780

/**
 * Klasyczne "ease-out cubic": szybki start, delikatne wyhamowanie na koncu. Uzywane przez cala
 * scene walki, zeby kazdy jej element zwalnial tak samo.
 */
internal val EaseOutCubic = CubicBezierEasing(0.33f, 1f, 0.68f, 1f)

internal val PanelBg = Color(0xFFF3E8D4)
internal val PanelBorder = Color(0xFFC8A45A)
internal val InkStrong = Color(0xFF2A1D12)
internal val InkMuted = Color(0xFF8A7859)

/**
 * Ciemny "stol wojenny" pod quizem i wyborem tematu: skora/drewno w zlotej ramce. Zlote dymki
 * odpowiedzi i ornament pytania swieca na nim, zamiast zlewac sie z kremowym tlem - a caly dol
 * ekranu ma jeden styl, zamiast kremowego panelu z ciemnym pudelkiem w srodku.
 */
internal val DarkPanelGradient = Brush.verticalGradient(listOf(Color(0xFF3B2A1B), Color(0xFF261A11), Color(0xFF1A110A)))
internal val PanelGold = Color(0xFFE8C56A)
internal val PanelCream = Color(0xFFF3E6C8)

/** Ksztalt panelu i "kutej", zlotej ramki wokol niego - dwie warstwy zlota daja wrazenie ozdobnej oprawy. */
internal val PanelShape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
internal val PanelGradient = Brush.verticalGradient(listOf(Color(0xFFFAF1DE), PanelBg))
internal val FrameGradient = Brush.verticalGradient(listOf(Color(0xFFFFE9A8), PanelBorder, Color(0xFF8A6B34)))

/**
 * Kolory ponumerowanych krazkow przy odpowiedziach - stale przypisanie do pozycji, jak w grach z
 * quizami. Ta sama barwa idzie tez w kamienie na ramce dymka (patrz [QuizOptionTile]), wiec numer i
 * jego dymek zawsze "swieca" tym samym kolorem. Szesc pozycji, bo wielokrotny wybor potrafi miec
 * 6-7 opcji - przy krotszej liscie kolory zaczelyby sie powtarzac.
 */
private val OptionColors = listOf(
    Color(0xFF8B5CF6), // 1/A - fiolet
    Color(0xFF22A55E), // 2/B - zielen
    Color(0xFF2F8FE0), // 3/C - blekit
    Color(0xFFE08A2F), // 4/D - pomarancz
    Color(0xFFE0487A), // 5/E - malina
    Color(0xFF16B8A6) // 6/F - turkus
)

/**
 * Panel quizu pokazywany w fazie [com.lernhero.game.battle.TurnPhase.Question] - walka stoi,
 * dopoki nie padnie odpowiedz. Pasek nad pytaniem odlicza czas na odpowiedz: im szybciej klikniesz,
 * tym mocniejsza salwa poleci w tej turze.
 *
 * Obsluguje dwa rodzaje pytan: [MultipleChoiceQuestion] (jeden wybor z czterech, [onAnswer]) i
 * [OrderingQuestion] (ulozenie wszystkich elementow we wlasciwej kolejnosci, [onOrderPick] - jedno
 * klikniecie dodaje kolejny element na koniec ukladanej sekwencji).
 *
 * Ekran jest pionowy (portrait), wiec panel jest waski i wysoki - pytanie + metryki siedza u gory,
 * a odpowiedzi ida w dol jedna pod druga, pelnej szerokosci "pigulki", jak w grze z reference.
 */
@Composable
fun LiveQuizBar(
    state: BattleUiState,
    onAnswer: (String) -> Unit,
    onOrderPick: (String) -> Unit,
    onMultiToggle: (String) -> Unit,
    onMultiConfirm: () -> Unit,
    onContinue: () -> Unit,
    questionOverhang: Dp,
    onTimerStart: (questionId: String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val quiz = state.quiz ?: return
    val question = quiz.question

    // Krotka chwila na zobaczenie, ktora odpowiedz byla poprawna - potem od razu kolejne pytanie.
    LaunchedEffect(question.id, quiz.revealed) {
        if (!quiz.revealed) return@LaunchedEffect
        delay(BattleSpeed.scale(QUIZ_REVEAL_MILLIS))
        onContinue()
    }

    // Pasek streaka ma wisiec NAD zlota ramka panelu, nad plansza walki - ale rodzic
    // (BattleScreen.SceneBottomPanel) przycina wszystko poza wlasnymi granicami, wiec offsetu
    // "na zewnatrz" nie da sie tu bezpiecznie uzyc (ucina sie w polowie). Zamiast tego zloty
    // panel dostaje padding u gory - to rezerwuje miejsce na streak W GRANICACH tego samego
    // obszaru, ktory i tak juz mamy przydzielony.
    Box(modifier = modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = STREAK_ROW_GAP)
                .clip(PanelShape)
                .background(DarkPanelGradient)
                .border(3.dp, FrameGradient, PanelShape)
        ) {
            PanelInnerLine()
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    // Dolna polowa ramki pytania nachodzi na gore panelu (rysuje ja BattleScreen,
                    // poza tym kontenerem) - to wciecie chroni tresc przed wejsciem pod nia.
                    .padding(top = questionOverhang + 10.dp)
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 10.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Obszar pytania NIE przewija sie w ogole. Drobne elementy (pasek czasu, flaga,
                // baner odslaniajacy) maja swoje wysokosci, a cala reszte zabiera siatka
                // odpowiedzi - to ona sie dopasowuje. Przycisk ZATWIERDZ stoi ponizej, poza tym
                // obszarem, wiec liczba opcji nigdy go nie wypycha poza ekran.
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (question is OrderingQuestion) {
                        SequenceProgressRow(question = question, orderedSelection = quiz.orderedSelection)
                    }

                    // Pasek czasu idzie POD pytaniem, ktore wisi nad panelem - stad jest tu
                    // pierwszy, a samego pytania juz w tej kolumnie nie ma.
                    AnswerCountdownBar(
                        questionId = question.id,
                        revealed = quiz.revealed,
                        windowMs = quiz.windowMs,
                        onStart = { onTimerStart(question.id) }
                    )

                    if (question is MultipleChoiceQuestion) {
                        question.flag?.let { flag ->
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Color.White)
                                    .border(1.dp, PanelBorder, RoundedCornerShape(10.dp))
                            ) {
                                Image(
                                    painter = org.jetbrains.compose.resources.painterResource(flag),
                                    contentDescription = question.prompt,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(64.dp)
                                        .padding(8.dp),
                                    contentScale = ContentScale.Fit
                                )
                            }
                        }
                    }

                    // Bez banera "Wrong. Correct answer: ..." - wjezdzal miedzy pasek a odpowiedzi
                    // i przesuwal caly panel w dol. Werdykt niosa same kafle (zielony = dobra,
                    // czerwony = twoj blad), a skutek pokazuje TurnImpactOverlay na polu bitwy.

                    // Odpowiedzi wypelniaja CALE miejsce, ktore zostalo, i nigdy sie nie przewijaja:
                    // kafle dziela dostepna wysokosc miedzy siebie ([weight]), a gdy przy jednej
                    // kolumnie zrobilyby sie nieczytelnie niskie, siatka przechodzi na DWIE kolumny
                    // (patrz [answerColumns]). Wczesniej caly panel siedzial w verticalScroll i przy
                    // czterech odpowiedziach mieszczil sie co do dp - wystarczyl baner odslaniajacy
                    // albo flaga nad pytaniem, zeby ostatnia opcja zjechala pod krawedz ekranu.
                    //
                    // Ciemna podkladka pod cala lista: jasne dymki na jasnym panelu zlewaly sie w
                    // jedno pole, a na ciemnym tle kazdy z nich wyraznie "stoi" osobno.
                    val options = question.toAnswerOptions(quiz, onAnswer, onOrderPick, onMultiToggle)

                    var visibleCount by remember(question.id) { mutableStateOf(0) }
                    LaunchedEffect(question.id) {
                        visibleCount = 0
                        delay(QUESTION_LEAD_MILLIS)
                        repeat(options.size) {
                            visibleCount += 1
                            delay(TILE_STAGGER_MILLIS)
                        }
                    }

                    AnswerGrid(
                        questionId = question.id,
                        revealed = quiz.revealed,
                        options = options,
                        visibleCount = visibleCount,
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .padding(top = 2.dp)
                            .clip(AnswerListShape)
                            .background(Color.Black.copy(alpha = 0.18f))
                            .padding(horizontal = 6.dp, vertical = 8.dp)
                    )
                }

                // Zatwierdzenie stoi POZA obszarem odpowiedzi - to jedyny sposob, zeby dalo sie
                // potwierdzic odpowiedz niezaleznie od tego, ile opcji ma pytanie.
                if (question is MultiSelectQuestion && !quiz.revealed) {
                    val picked = quiz.multiSelection.size
                    MultiSelectConfirm(
                        pickedCount = picked,
                        enabled = picked > 0,
                        onConfirm = onMultiConfirm
                    )
                }
            }
        }

        // W miejscu zarezerwowanym przez padding(top = STREAK_ROW_GAP) na zlotym panelu wyzej -
        // widocznie NAD panelem, nad plansza walki, bez wychodzenia poza wlasne granice.
        StreakPipRow(
            streak = state.streak,
            modifier = Modifier.align(Alignment.TopCenter)
        )
    }
}

/** Ile miejsca u gory zlotego panelu jest zarezerwowane pod pasek streaka - patrz [LiveQuizBar]. */
private val STREAK_ROW_GAP = 20.dp

/** Ciemna podkladka pod lista odpowiedzi - uzasadnienie przy jej uzyciu w [LiveQuizBar]. */
private val AnswerListBg = Color(0xFF3A2A1C)
private val AnswerListShape = RoundedCornerShape(18.dp)

/** Litera przypisana do elementu ukladanki wedlug jego miejsca w wyswietlonej (przetasowanej) liscie - A, B, C, D... */
private fun itemLabel(displayIndex: Int): String = ('A' + displayIndex).toString()


/**
 * Przycisk konczacy pytanie wielokrotnego wyboru. Licznik zaznaczen jest tu celowo: przy szescioma
 * i wiecej opcjami latwo zgubic rachube, ile sie juz kliknelo.
 */
@Composable
private fun MultiSelectConfirm(pickedCount: Int, enabled: Boolean, onConfirm: () -> Unit) {
    val shape = RoundedCornerShape(14.dp)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 4.dp)
            .clip(shape)
            .background(if (enabled) Color(0xFF8A5A10) else Color(0xFF120C08))
            .padding(bottom = if (enabled) 3.dp else 0.dp)
            .clip(shape)
            .background(
                if (enabled) Brush.verticalGradient(listOf(Color(0xFFFFE9A0), Color(0xFFF2B832), Color(0xFFD9901C)))
                else Brush.verticalGradient(listOf(Color(0xFF2A1D12), Color(0xFF1E150D)))
            )
            .border(1.5.dp, if (enabled) Color(0xFF6A4208) else PanelBorder.copy(alpha = 0.35f), shape)
            .then(if (enabled) Modifier.clickable(onClick = onConfirm) else Modifier)
            .padding(vertical = 11.dp),
        contentAlignment = Alignment.Center
    ) {
        if (enabled) {
            com.lernhero.shared.theme.StrokedText(
                text = "CONFIRM ($pickedCount)",
                size = 15.sp,
                color = Color.White,
                outline = Color(0xFF5A3406),
                strokeWidth = 6f,
                maxLines = 1
            )
        } else {
            Text(
                text = "SELECT THE TRUE ANSWERS",
                color = PanelGold.copy(alpha = 0.55f),
                fontFamily = FontFirst(),
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                letterSpacing = 0.8.sp
            )
        }
    }
}

/**
 * Wejscie samego dymka pytania - pierwszy element, ktory laduje na miejscu w panelu (odpowiedzi ida
 * po nim, patrz [QUESTION_LEAD_MILLIS]). Odtwarzane raz na kazde nowe [questionId].
 */
@Composable
internal fun QuestionEntrance(questionId: String, content: @Composable () -> Unit) {
    val visibleState = remember(questionId) { MutableTransitionState(false).apply { targetState = true } }
    AnimatedVisibility(
        visibleState = visibleState,
        enter = fadeIn(tween(QUESTION_ENTER_MILLIS, easing = EaseOutCubic)) +
            scaleIn(
                initialScale = 0.94f,
                animationSpec = tween(QUESTION_ENTER_MILLIS, easing = EaseOutCubic)
            ) +
            slideInVertically(
                animationSpec = tween(QUESTION_ENTER_MILLIS, easing = EaseOutCubic)
            ) { fullHeight -> -fullHeight / 3 }
    ) {
        content()
    }
}

/**
 * Ramka z pytaniem - jedna gotowa grafika ([Resources.QuizArt.questionFrame]) z wpisanym w nia
 * tekstem, na calej dostepnej szerokosci panelu.
 *
 * Wysokosc wynika z proporcji grafiki (`aspectRatio`), a nie z dlugosci pytania. Daje to dwie
 * rzeczy naraz: zdobienia nigdy sie nie deformuja, a blok pytania ma stala wysokosc - inaczej
 * rosnacy dymek przepychalby odpowiedzi w gore i w dol przy kazdej zmianie pytania, a te wjezdzaja
 * animacja i klika sie w nie na czas.
 */
@Composable
internal fun QuestionBubble(prompt: String, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxWidth().aspectRatio(Resources.QuizArt.QUESTION_FRAME_RATIO),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(Resources.QuizArt.questionFrame),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.FillBounds
        )
        Text(
            text = prompt,
            // Tlo ramki jest jasne, kremowe - napis musi byc ciemny, inaczej znika.
            color = InkStrong,
            fontFamily = FontFirst(),
            fontSize = QUESTION_TEXT_SIZE,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            maxLines = QUESTION_MAX_LINES,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.fillMaxWidth(Resources.QuizArt.QUESTION_TEXT_WIDTH_FRACTION)
        )
    }
}

/**
 * Rozmiar napisu i limit linii dobrane tak, zeby najdluzsze pytanie z banku (97 znakow) miescilo
 * sie w plaskim wnetrzu ramki na KAZDEJ typowej szerokosci telefonu - takze na najwezszych, gdzie
 * ramka jest najnizsza. Wnetrze ma ok. 60% wysokosci ramki, a trzy linie po 14sp to ok. 55 dp przy
 * 58 dp dostepnych na ekranie 320 dp. Dluzsze pytania - wlasne lub generowane - urwa sie
 * wielokropkiem, zamiast wyjsc na zlota oprawe.
 */
private val QUESTION_TEXT_SIZE = 14.sp
private const val QUESTION_MAX_LINES = 3

/**
 * Rzad krazkow-slotow nad pytaniem "ulóz w kolejnosci" - jeden slot na kazdy element, polaczone
 * strzalkami. Puste sloty czekaja na kolejne klikniecie, wypelnione pokazuja litere i kolor
 * elementu, ktory tam wyladowal - dzieki temu widac budowana sekwencje na biezaco.
 */
@Composable
private fun SequenceProgressRow(
    question: OrderingQuestion,
    orderedSelection: List<String>,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        question.items.indices.forEach { slot ->
            val filledId = orderedSelection.getOrNull(slot)
            val filledIndex = filledId?.let { id -> question.items.indexOfFirst { it.id == id } }?.takeIf { it >= 0 }
            val slotColor = filledIndex?.let { OptionColors[it % OptionColors.size] } ?: PanelBorder

            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(if (filledIndex != null) slotColor else Color.White)
                    .border(2.dp, slotColor, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                if (filledIndex != null) {
                    Text(
                        text = itemLabel(filledIndex),
                        color = Color.White,
                        fontFamily = FontFirst(),
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }
            }

            if (slot != question.items.lastIndex) {
                Text(
                    text = "›",
                    color = InkMuted,
                    fontFamily = FontFirst(),
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }
        }
    }
}

/**
 * Licznik czasu na odpowiedz - jeden plynny pasek zamiast osmiu klockow.
 *
 * Pokazuje dwie rzeczy, ktore gracz ma zrozumiec bez czytania instrukcji:
 *  - **ile mocy da odpowiedz TERAZ** - duzy mnoznik po prawej ("x1.40"), liczony ta sama funkcja,
 *    ktora potem liczy salwe ([AnswerTimer.multiplierFor]), wiec obietnica i wynik sa te same;
 *  - **strefa pelnej mocy** - poczatkowy odcinek paska z napisem MAX. Dopoki wypelnienie jest w tej
 *    strefie, czytanie pytania nic nie kosztuje. Potem pasek plynnie gasnie od zieleni do bursztynu.
 *
 * Ostatnie 20% okna pulsuje na czerwono - sygnal "odpowiedz, zanim minie czas", ale bez paniki:
 * nawet wtedy dobra odpowiedz bije pelnym x1.0.
 *
 * Ten sam komponent uzywa zaklecie bossa ([BossEventQuizBar]) - tam mnoznik salwy nie ma sensu,
 * wiec [showMultiplier] = false zostawia tylko pasek i sekundy.
 */
@Composable
internal fun AnswerCountdownBar(
    questionId: String,
    revealed: Boolean,
    /** Okno TEGO pytania - rozne dla roznych pytan i fal, patrz [AnswerTimer.windowFor]. */
    windowMs: Long = AnswerTimer.WINDOW_MS,
    label: String = "HIT POWER",
    showMultiplier: Boolean = true,
    /** Pasek ruszyl - od tej chwili liczy sie czas odpowiedzi (patrz BattleViewModel.markQuestionShown). */
    onStart: () -> Unit = {},
    /** Ciemny panel quizu (zlote napisy) albo jasny panel zaklecia bossa (ciemny tusz). */
    dark: Boolean = true,
    modifier: Modifier = Modifier
) {
    var remainingFraction by remember(questionId) { mutableFloatStateOf(1f) }

    LaunchedEffect(questionId, revealed) {
        if (revealed) return@LaunchedEffect
        onStart()
        val start = TimeSource.Monotonic.markNow()
        while (isActive) {
            val elapsedMs = start.elapsedNow().inWholeMilliseconds
            remainingFraction = (1f - elapsedMs.toFloat() / windowMs).coerceIn(0f, 1f)
            if (remainingFraction <= 0f) break
            withFrameMillis {}
        }
    }

    // Po odpowiedzi petla wyzej sie zatrzymuje, wiec pasek zamraza sie dokladnie na tej wartosci,
    // ktora policzyla mnoznik salwy - pokazuje to, co faktycznie trafilo.
    val multiplier = AnswerTimer.multiplierFor(remainingFraction)
    val inGrace = AnswerTimer.isInGrace(remainingFraction)
    val urgent = !revealed && remainingFraction < URGENT_FRACTION
    val barColor = timerColor(remainingFraction)
    val remainingSeconds = remainingFraction * windowMs / 1000f

    val pulse = rememberInfiniteTransition(label = "timerPulse")
    val pulseAlpha by pulse.animateFloat(
        initialValue = 0.35f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(380, easing = EaseInOutSine), RepeatMode.Reverse),
        label = "timerPulseAlpha"
    )
    val shimmer by pulse.animateFloat(
        initialValue = -0.3f,
        targetValue = 1.3f,
        animationSpec = infiniteRepeatable(tween(1400, easing = LinearEasing)),
        label = "timerShimmer"
    )

    // Tykanie w ostatnich sekundach - jak zegar nad bomba.
    val secondsLeft = kotlin.math.ceil(remainingSeconds).toInt()
    LaunchedEffect(secondsLeft, revealed) {
        if (!revealed && remainingFraction > 0f && secondsLeft in 1..3) {
            com.lernhero.shared.audio.Sounds.play(com.lernhero.shared.audio.Sfx.Tap)
        }
    }
    val labelColor = if (dark) PanelGold else InkMuted
    val secondsColor = if (dark) PanelCream else InkMuted

    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(5.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "⚡ $label",
                    color = labelColor,
                    fontFamily = FontFirst(),
                    fontWeight = FontWeight.Black,
                    fontSize = 10.sp,
                    letterSpacing = 0.8.sp
                )
                Text(
                    text = "${(remainingSeconds * 10).toInt() / 10f}s",
                    color = if (urgent) TimerRed.copy(alpha = pulseAlpha) else secondsColor,
                    fontFamily = FontFirst(),
                    fontWeight = FontWeight.Black,
                    fontSize = 12.sp
                )
            }

            BurningFuse(
                remaining = remainingFraction,
                graceFraction = if (showMultiplier) AnswerTimer.GRACE_FRACTION else 0f,
                burning = !revealed,
                urgent = urgent,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(TIMER_BAR_HEIGHT)
                    .graphicsLayer {
                        // W ostatnich sekundach caly lont lekko drzy.
                        translationX = if (urgent) kotlin.math.sin(shimmer * 60f) * 1.2f * density else 0f
                    }
            )
        }

        if (showMultiplier) {
            MultiplierBadge(multiplier = multiplier, color = barColor, max = inGrace)
        }
    }
}

/** Mnoznik salwy "na zywo" - duza liczba w kapsule, pompuje przy zmianie setnych. */
@Composable
private fun MultiplierBadge(multiplier: Float, color: Color, max: Boolean) {
    val hundredths = (multiplier * 100).roundToInt()
    // Przy kazdym spadku o pelna dziesiata mnoznik "podskakuje" - widac, ze wlasnie cos stracil.
    val tenths = hundredths / 10
    val bump = remember { Animatable(1f) }
    LaunchedEffect(tenths) {
        bump.snapTo(1.18f)
        bump.animateTo(1f, spring(dampingRatio = 0.4f, stiffness = 520f))
    }
    Column(
        modifier = Modifier
            .width(66.dp)
            .graphicsLayer {
                scaleX = bump.value
                scaleY = bump.value
            }
            .clip(RoundedCornerShape(12.dp))
            .background(Brush.verticalGradient(listOf(Color(0xFF140D08), Color(0xFF2A1D12))))
            .border(1.5.dp, Brush.verticalGradient(listOf(Color(0xFFFFE9A8), PanelBorder, Color(0xFF8A6B34))), RoundedCornerShape(12.dp))
            .padding(vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        com.lernhero.shared.theme.StrokedText(
            text = "×${hundredths / 100}.${(hundredths % 100).toString().padStart(2, '0')}",
            size = 17.sp,
            color = color,
            outline = Color(0xFF0A0604),
            strokeWidth = 6f,
            maxLines = 1
        )
        Text(
            text = if (max) "MAX" else "DMG",
            color = if (max) PanelGold else Color.White.copy(alpha = 0.6f),
            fontFamily = FontFirst(),
            fontWeight = FontWeight.Black,
            fontSize = 8.sp,
            letterSpacing = 1.sp
        )
    }
}

/** Zielony w strefie MAX, potem plynnie przez zloto do bursztynu; czerwien tylko na samym koncu. */
private fun timerColor(remainingFraction: Float): Color {
    val decayStart = 1f - AnswerTimer.GRACE_FRACTION
    return when {
        remainingFraction >= decayStart -> TimerGreen
        remainingFraction >= URGENT_FRACTION -> {
            val t = (remainingFraction - URGENT_FRACTION) / (decayStart - URGENT_FRACTION)
            lerp(TimerAmber, TimerGold, t)
        }
        else -> lerp(TimerRed, TimerAmber, remainingFraction / URGENT_FRACTION)
    }
}

private val TIMER_BAR_HEIGHT = 18.dp
private const val URGENT_FRACTION = 0.2f
private val TimerTrack = Color(0xFF3A2A1C)
private val TimerGreen = Color(0xFF2FCB6E)
private val TimerGold = Color(0xFFF2C230)
private val TimerAmber = Color(0xFFF08A2C)
private val TimerRed = Color(0xFFE0453A)

/**
 * Skrocony, zwiniety pasek pokazywany zamiast [LiveQuizBar] w trakcie [com.lernhero.game.battle.TurnPhase.Resolving] -
 * druzyny wymieniaja ciosy, quiz jest na chwile schowany, wiec zostaje tylko krotki status i seria.
 */
@Composable
fun ResolvingStrip(state: BattleUiState, modifier: Modifier = Modifier) {
    // Ten sam uklad "streak w zarezerwowanym miejscu nad zlotym panelem" co w LiveQuizBar -
    // inaczej pasek streaka skakalby w inne miejsce przy kazdym przejsciu quiz <-> rozliczanie tury.
    Box(modifier = modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = STREAK_ROW_GAP)
                .clip(PanelShape)
                .background(DarkPanelGradient)
                .border(3.dp, FrameGradient, PanelShape)
        ) {
            PanelInnerLine()
            val pulse by rememberInfiniteTransition(label = "resolving").animateFloat(
                initialValue = 0.85f,
                targetValue = 1.15f,
                animationSpec = infiniteRepeatable(tween(520, easing = EaseInOutSine), RepeatMode.Reverse),
                label = "swords"
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "⚔",
                    fontSize = 18.sp,
                    color = PanelGold,
                    modifier = Modifier.graphicsLayer {
                        scaleX = pulse
                        scaleY = pulse
                    }
                )
                Text(
                    // W takcie [TurnPhase.Impact] nikt jeszcze nie bije - cala uwaga ma byc na
                    // rozliczeniu odpowiedzi na srodku ekranu, wiec pasek nie udaje trwajacej walki.
                    text = state.message
                        ?: if (state.turnPhase == TurnPhase.Impact) "Volley charging..." else "Exchanging blows...",
                    color = PanelCream,
                    fontFamily = FontFirst(),
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        StreakPipRow(
            streak = state.streak,
            modifier = Modifier.align(Alignment.TopCenter)
        )
    }
}

/**
 * Dziesiec gniazd ostatnich odpowiedzi - zielony kamien za dobra, czerwony za zla, puste gniazdo
 * jeszcze czeka. Przy serii od 3 w gore dobre kamienie zaczynaja swiecic: piec z rzedu odpala ulti.
 */
@Composable
private fun StreakPipRow(streak: StreakTracker, modifier: Modifier = Modifier) {
    val hot = streak.consecutiveCorrect >= 3
    val glow by rememberInfiniteTransition(label = "streakGlow").animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(600, easing = EaseInOutSine), RepeatMode.Reverse),
        label = "streakGlowAlpha"
    )
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(50))
            .background(Color(0xE0140D08))
            .border(1.dp, PanelBorder.copy(alpha = 0.7f), RoundedCornerShape(50))
            .padding(horizontal = 6.dp, vertical = 3.dp),
        horizontalArrangement = Arrangement.spacedBy(3.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        streak.pips.forEach { pip ->
            val gem = when (pip) {
                true -> Color(0xFF2FD06E)
                false -> Color(0xFFE0453A)
                null -> null
            }
            Box(modifier = Modifier.size(12.dp), contentAlignment = Alignment.Center) {
                if (gem != null && pip == true && hot) {
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .graphicsLayer {
                                alpha = glow
                                scaleX = 1.35f
                                scaleY = 1.35f
                            }
                            .clip(CircleShape)
                            .background(Brush.radialGradient(listOf(gem.copy(alpha = 0.8f), Color.Transparent)))
                    )
                }
                Box(
                    modifier = Modifier
                        .size(11.dp)
                        .clip(CircleShape)
                        .background(
                            if (gem != null) Brush.radialGradient(listOf(Color.White, gem, gem.copy(red = gem.red * 0.55f, green = gem.green * 0.55f, blue = gem.blue * 0.55f)))
                            else Brush.verticalGradient(listOf(Color(0xFF0A0604), Color(0xFF2A1D12)))
                        )
                        .border(1.dp, PanelBorder, CircleShape)
                )
            }
        }
    }
}

/** Cienka, zlota linia wewnatrz ramki panelu - to ona robi z niego "oprawiona tablice". */
@Composable
internal fun PanelInnerLine() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(4.dp)
            .border(1.dp, PanelGold.copy(alpha = 0.22f), PanelShape)
    )
}

/**
 * Ile czasu quiz stoi po odpowiedzi, zebys zdazyl zobaczyc, ktora byla dobra.
 *
 * 550ms (tyle bylo wczesniej) wystarczalo tylko na zauwazenie, ZE cos mignelo - przy pytaniach z
 * czterema-pieciu opcjami nie dalo sie w tym czasie znalezc wzrokiem zielonego kafla, a przy blednej
 * odpowiedzi tym bardziej przeczytac, ktora byla poprawna. Skalowane przez [BattleSpeed], wiec
 * przewijanie walki na 4x/8x dalej dziala.
 */
private const val QUIZ_REVEAL_MILLIS = 1700L

/** Odstep miedzy kolejnymi odpowiedziami "wjezdzajacymi" na ekran - szybko, ale po kolei, 1 -> 4. */
private const val TILE_STAGGER_MILLIS = 95L

/** Ile pytanie ma "dla siebie", zanim zaczna wjezdzac odpowiedzi - zeby czytalo sie je pierwsze. */
private const val QUESTION_LEAD_MILLIS = 150L

/** Czas wjazdu pojedynczej odpowiedzi i samego dymka pytania. */
private const val TILE_ENTER_MILLIS = 260
private const val QUESTION_ENTER_MILLIS = 300

private val AnswerGreen = Color(0xFF22A55E)
private val AnswerRed = Color(0xFFD1453D)
private val AnswerFadedText = Color(0xFFB1A283)

/** Stan wizualny jednej odpowiedzi - wspolny dla wyboru pojedynczego, ukladanki i wielokrotnego wyboru. */
private enum class OptionVisual { IDLE, SELECTED, CORRECT, WRONG, FADED }

/**
 * Barwa kamieni w ramce dymka i krazka z numerem. Dopoki odpowiedz nie padla, kazda pozycja swieci
 * wlasnym kolorem z [OptionColors] - po odsloneciu wszystkie przechodza na werdykt (zielen/czerwien),
 * zeby jednym rzutem oka bylo widac, co bylo dobre.
 */
private fun gemTintFor(visual: OptionVisual, optionColor: Color): Color = when (visual) {
    OptionVisual.IDLE, OptionVisual.SELECTED -> optionColor
    OptionVisual.CORRECT -> AnswerGreen
    OptionVisual.WRONG -> AnswerRed
    OptionVisual.FADED -> AnswerFadedText
}

/**
 * Tint pola pod tekstem odpowiedzi. Warstwa `fill` jest znormalizowana do bieli tak, ze zostaly w
 * niej tylko UBYTKI - miekkie cienie przy ramce i ciemny kontur wnetrza. Mnozenie (Modulate) daje
 * wiec dokladnie te barwe na plaskiej czesci pola, a cienie z oryginalnej grafiki przyciemniaja ja
 * po swojemu. Nie jest to plaska plama koloru: kazdy stan zachowuje to samo cieniowanie co krem.
 *
 * [OptionVisual.IDLE] to bazowy kolor kremu z `answer.png` (jego 97. percentyl), wiec nietkniety
 * dymek wyglada 1:1 jak oryginalna grafika.
 */
private fun fillTintFor(visual: OptionVisual): Color = when (visual) {
    OptionVisual.IDLE -> Color(0xFFF9F1E0)
    OptionVisual.SELECTED -> Color(0xFFFFE7A6) // klikniete, wynik jeszcze nieznany - zloty poblask
    OptionVisual.CORRECT -> Color(0xFFA6E8BC)
    OptionVisual.WRONG -> Color(0xFFF4B3AD)
    OptionVisual.FADED -> Color(0xFFCEC2A6)
}

/** Kolor napisu - tlo dymka jest zawsze jasne, wiec werdykt niesie ciemna, nasycona wersja barwy. */
private fun inkFor(visual: OptionVisual): Color = when (visual) {
    OptionVisual.IDLE, OptionVisual.SELECTED -> InkStrong
    OptionVisual.CORRECT -> Color(0xFF12532F)
    OptionVisual.WRONG -> Color(0xFF7A1C17)
    // Wyrazniejszy niz [AnswerFadedText]: wygaszona odpowiedz ma schodzic na drugi plan, ale dalej
    // musi dac sie ja przeczytac - na przyciemnionym, bezowym tle jasny bez znikal.
    OptionVisual.FADED -> Color(0xFF80714F)
}

/**
 * Pelnej szerokosci, ozdobny dymek odpowiedzi: zlota ramka z kamieniami, biale pole na tekst i
 * krazek z etykieta (numer albo litera) wpuszczony w lewa krawedz.
 *
 * Grafika jest ROZBITA NA WARSTWY (patrz [Resources.QuizArt]) wlasnie po to, zeby kolor kamieni i
 * tla dalo sie wyliczyc w kodzie zamiast trzymac osobny PNG na kazdy stan: warstwy `fill` i `gems`
 * sa szare i przechodza przez `ColorFilter.tint(..., BlendMode.Modulate)`, czyli mnozenie - szarosc
 * razy barwa zachowuje cieniowanie i fasetki z oryginalnego rysunku. Zloto idzie bez filtra.
 *
 * [OptionVisual.SELECTED] to stan posredni (ukladanka, wielokrotny wybor) - element juz klikniety,
 * czeka na reszte, ale wynik jeszcze nie jest znany.
 */
/**
 * Jedna odpowiedz gotowa do narysowania - wspolny ksztalt dla wszystkich trzech typow pytania.
 * Dzieki niemu siatka jest napisana RAZ; wczesniej kazdy typ mial wlasna, niemal identyczna kopie
 * petli rysujacej kafle i kazda poprawka ukladu trzeba bylo robic trzy razy.
 */
private data class AnswerOption(
    val label: String,
    val badgeColor: Color,
    val text: String,
    val visual: OptionVisual,
    val enabled: Boolean,
    val onClick: () -> Unit
)

/** Sprowadza dowolny typ pytania do listy [AnswerOption] - cala roznica miedzy typami konczy sie tutaj. */
@Composable
private fun QuizQuestion.toAnswerOptions(
    quiz: LiveQuizState,
    onAnswer: (String) -> Unit,
    onOrderPick: (String) -> Unit,
    onMultiToggle: (String) -> Unit
): List<AnswerOption> = when (this) {
    is MultipleChoiceQuestion -> answers.mapIndexed { index, answer ->
        AnswerOption(
            label = "${index + 1}",
            badgeColor = OptionColors[index % OptionColors.size],
            text = answer.text,
            visual = when {
                !quiz.revealed -> OptionVisual.IDLE
                answer.isCorrect -> OptionVisual.CORRECT
                quiz.selectedAnswerId == answer.id -> OptionVisual.WRONG
                else -> OptionVisual.FADED
            },
            enabled = !quiz.revealed,
            onClick = { onAnswer(answer.id) }
        )
    }

    is OrderingQuestion -> items.mapIndexed { index, item ->
        val position = quiz.orderedSelection.indexOf(item.id)
        AnswerOption(
            label = if (position != -1) "${position + 1}" else itemLabel(index),
            badgeColor = OptionColors[index % OptionColors.size],
            text = item.text,
            visual = when {
                quiz.revealed -> if (position == item.correctPosition) OptionVisual.CORRECT else OptionVisual.WRONG
                position != -1 -> OptionVisual.SELECTED
                else -> OptionVisual.IDLE
            },
            enabled = !quiz.revealed && position == -1,
            onClick = { onOrderPick(item.id) }
        )
    }

    // "Zaznacz wszystkie prawdziwe" - w odroznieniu od pozostalych typow nic nie rozstrzyga sie
    // samo: gracz przelacza dowolna liczbe opcji i dopiero ZATWIERDZ konczy ture.
    is MultiSelectQuestion -> options.mapIndexed { index, option ->
        val picked = option.id in quiz.multiSelection
        AnswerOption(
            label = if (picked) "✔" else itemLabel(index),
            badgeColor = OptionColors[index % OptionColors.size],
            text = option.text,
            visual = when {
                !quiz.revealed -> if (picked) OptionVisual.SELECTED else OptionVisual.IDLE
                option.isCorrect -> OptionVisual.CORRECT
                picked -> OptionVisual.WRONG
                else -> OptionVisual.FADED
            },
            enabled = !quiz.revealed,
            onClick = { onMultiToggle(option.id) }
        )
    }
}

/**
 * Ile kolumn ma miec siatka odpowiedzi przy takiej liczbie opcji i takiej wysokosci.
 *
 * To jest cala regula "dwa rzedy, gdy sie nie mieszcza": liczymy, ile wysokosci przypadloby na
 * kafel w JEDNEJ kolumnie, i jesli wychodzi mniej niz [ANSWER_TILE_READABLE_HEIGHT], przechodzimy
 * na dwie. Przy siedmiu opcjach wielokrotnego wyboru jedna kolumna daje 41 dp na kafel - napis
 * przestaje sie miescic; dwie kolumny robia z tego cztery rzedy po 78 dp, czyli tyle, ile ma
 * zwykle pytanie z czterema odpowiedziami.
 *
 * Powyzej [ANSWER_MAX_COLUMNS] nie idziemy: przy trzech kolumnach na dymek zostaje mniej niz
 * sto dp szerokosci i tekst dluzszy niz jedno slowo i tak zostalby przyciety.
 */
private fun answerColumns(count: Int, available: Dp): Int {
    if (count <= 2) return 1
    val perTile = (available - ANSWER_TILE_GAP * (count - 1)) / count
    return if (perTile < ANSWER_TILE_READABLE_HEIGHT) ANSWER_MAX_COLUMNS else 1
}

/**
 * Siatka odpowiedzi wypelniajaca DOKLADNIE przydzielona wysokosc - bez przewijania i bez przyciecia.
 *
 * Kazdy rzad bierze [weight], wiec kafle zawsze dziela to, co jest; [QuizOptionTile] sam dopasowuje
 * krazek z numerem i wielkosc pisma do wysokosci, ktora dostal. Miejsce kafla istnieje od poczatku,
 * a animacja wejscia ([visibleCount]) dotyczy tylko jego ZAWARTOSCI - inaczej uklad skakalby przy
 * kazdej kolejnej pojawiajacej sie odpowiedzi.
 */
@Composable
private fun AnswerGrid(
    questionId: String,
    revealed: Boolean,
    options: List<AnswerOption>,
    visibleCount: Int,
    modifier: Modifier = Modifier
) {
    BoxWithConstraints(modifier = modifier) {
        // Ile kolumn ma siatka liczy sie TYLKO dopoki odpowiedz nie padla - RevealBanner, ktory
        // wskakuje po odpowiedzi, zabiera kawalek tej samej wysokosci i bez tej blokady siatka
        // przeliczylaby sie od nowa w polowie animacji werdyktu, skakac z jednej kolumny na dwie.
        // Gracz ma wtedy widziec dokladnie ten sam uklad co przed klikniciem - tylko podswietlony.
        var lockedHeight by remember(questionId) { mutableStateOf(maxHeight) }
        if (!revealed) {
            lockedHeight = maxHeight
        }
        val columns = answerColumns(options.size, lockedHeight)
        val rows = options.chunked(columns)

        // Rzedy dziela wysokosc po rowno, ale nie rosna ponad [ANSWER_TILE_MAX_HEIGHT] - przy dwoch
        // odpowiedziach (prawda/falsz) kazdy dymek dostalby pol panelu i grafika by sie rozjechala.
        // Nadmiar miejsca idzie wtedy rowno nad i pod siatke.
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(ANSWER_TILE_GAP, Alignment.CenterVertically)
        ) {
            rows.forEachIndexed { rowIndex, row ->
                Row(
                    modifier = Modifier.fillMaxWidth().weight(1f, fill = false).heightIn(max = ANSWER_TILE_MAX_HEIGHT),
                    horizontalArrangement = Arrangement.spacedBy(ANSWER_TILE_GAP)
                ) {
                    row.forEachIndexed { columnIndex, option ->
                        val index = rowIndex * columns + columnIndex
                        AnswerCell(
                            option = option,
                            visible = index < visibleCount,
                            modifier = Modifier.weight(1f).fillMaxHeight()
                        )
                    }
                    // Niepelny rzad dopychamy pustka, zeby kafle zostaly tej samej szerokosci
                    // co w rzedzie pelnym, a nie rozlazly sie na cala szerokosc panelu.
                    repeat(columns - row.size) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

/**
 * Jedno MIEJSCE w siatce odpowiedzi. Miejsce istnieje od poczatku i ma stala wielkosc - animacja
 * wejscia dotyczy tylko zawartosci, wiec uklad nie skacze, gdy kolejne odpowiedzi sie pojawiaja.
 *
 * Osobna funkcja, a nie Box w srodku rzedu, z konkretnego powodu: w zasiegu Row/Column stoja takze
 * `RowScope.AnimatedVisibility` i `ColumnScope.AnimatedVisibility`, przez co wywolanie bez zasiegu
 * nie daje sie rozwiazac. Tutaj zadnego zasiegu ukladu nie ma i jest jednoznacznie.
 */
@Composable
private fun AnswerCell(
    option: AnswerOption,
    visible: Boolean,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier) {
        AnimatedVisibility(
            visible = visible,
            enter = fadeIn(tween(TILE_ENTER_MILLIS, easing = EaseOutCubic)) +
                slideInHorizontally(
                    animationSpec = tween(TILE_ENTER_MILLIS, easing = EaseOutCubic)
                ) { fullWidth -> fullWidth / 2 }
        ) {
            QuizOptionTile(
                label = option.label,
                badgeColor = option.badgeColor,
                text = option.text,
                visual = option.visual,
                enabled = option.enabled,
                onClick = option.onClick,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

@Composable
private fun QuizOptionTile(
    label: String,
    badgeColor: Color,
    text: String,
    visual: OptionVisual,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val fill by animateColorAsState(fillTintFor(visual))
    val gem by animateColorAsState(gemTintFor(visual, badgeColor))
    val ink = inkFor(visual)

    // Reakcja kafla na werdykt: dobra odpowiedz "podskakuje", zla sie trzesie. Werdykt czyta sie
    // z ruchu, zanim oko zdazy porownac kolory.
    val pop = remember { Animatable(1f) }
    val jolt = remember { Animatable(0f) }
    LaunchedEffect(visual) {
        when (visual) {
            OptionVisual.CORRECT -> {
                pop.animateTo(1.07f, tween(120, easing = EaseOutCubic))
                pop.animateTo(1f, spring(dampingRatio = 0.35f, stiffness = 500f))
            }
            OptionVisual.WRONG -> jolt.animateTo(
                0f,
                keyframes {
                    durationMillis = 360
                    -10f at 45
                    10f at 90
                    -7f at 150
                    7f at 210
                    -3f at 280
                }
            )
            else -> Unit
        }
    }

    BoxWithConstraints(
        modifier = modifier
            .graphicsLayer {
                scaleX = pop.value
                scaleY = pop.value
                translationX = jolt.value * density
            }
            .heightIn(min = ANSWER_TILE_FLOOR_HEIGHT)
            .then(if (enabled) Modifier.clickable(onClick = onClick) else Modifier),
        contentAlignment = Alignment.CenterStart
    ) {
        // Kafel dostaje wysokosc OD UKLADU (siatka dzieli miejsce miedzy rzedy), wiec krazek z
        // numerem i wielkosc pisma musza sie do niej dopasowac - inaczej przy siedmiu opcjach
        // krazek o stalej srednicy 58 dp bylby wyzszy od calego dymka, ktory ma zdobic.
        // Wczesniej wysokosc brala sie z tekstu, bo panel siedzial w verticalScroll i miejsca
        // "na dole" bylo nieskonczenie wiele.
        val badgeSize = (maxHeight * ANSWER_BADGE_HEIGHT_RATIO)
            .coerceIn(ANSWER_BADGE_MIN_SIZE, ANSWER_BADGE_SIZE)
        val textSize = when {
            maxHeight < 52.dp -> 11.sp
            maxHeight < 66.dp -> 12.sp
            else -> 14.sp
        }
        // Dwie kolumny zwezaja dymek na tyle, ze dluzsza odpowiedz musi dostac wiecej wierszy,
        // a nie mniej - inaczej urywalaby sie w polowie slowa.
        val textLines = if (maxWidth < ANSWER_NARROW_TILE_WIDTH) 4 else 3
        val textPadding = (maxHeight * 0.14f).coerceIn(6.dp, 20.dp)

        // Wszystkie trzy warstwy leza na tym samym canvasie, wiec wystarczy ten sam modifier i
        // FillBounds; krazek z numerem wystaje w lewo poza dymek i czesciowo go przykrywa.
        //
        // Obie liczby licza sie Z ROZMIARU KRAZKA, a nie ze stalych - krazek zmienia sie razem z
        // wysokoscia kafla, wiec stale 26/42 dp (dobrane pod krazek 58 dp) rozjechalyby sie przy
        // kazdym innym rozmiarze: dymek zaczynalby sie w zlym miejscu, a napis wchodzilby pod
        // numer. Przy pelnym krazku wychodzi dokladnie tyle, co dawne stale.
        val bubbleStart = badgeSize * ANSWER_BUBBLE_START_RATIO
        // Krazek zachodzi na dymek na glebokosc `nudge + srednica - poczatek dymka`; napis musi
        // zaczac sie za nim, a zeby zostal na SRODKU dymka, tyle samo miejsca schodzi z prawej.
        val textInset = ANSWER_BADGE_NUDGE + badgeSize - bubbleStart + ANSWER_TEXT_CLEARANCE
        val layer = Modifier.matchParentSize().padding(start = bubbleStart)
        Image(
            painter = painterResource(Resources.QuizArt.answerBubbleFill),
            contentDescription = null,
            modifier = layer,
            contentScale = ContentScale.FillBounds,
            colorFilter = ColorFilter.tint(fill, BlendMode.Modulate)
        )
        Image(
            painter = painterResource(Resources.QuizArt.answerBubbleFrame),
            contentDescription = null,
            modifier = layer,
            contentScale = ContentScale.FillBounds
        )
        Image(
            painter = painterResource(Resources.QuizArt.answerBubbleGems),
            contentDescription = null,
            modifier = layer,
            contentScale = ContentScale.FillBounds,
            colorFilter = ColorFilter.tint(gem, BlendMode.Modulate)
        )

        // Napis nalezy do DYMKA, nie do kafla - dlatego siedzi we wlasnym Boxie przystajacym do
        // dymka i jest w nim wysrodkowany. Wysrodkowanie w calym kaflu (czyli w przestrzeni "po
        // krazku") przesuwaloby go w prawo wzgledem kamieni, ktore w grafice leza dokladnie na
        // srodku dymka - i wlasnie z nimi napis ma sie pokrywac.
        Box(
            modifier = Modifier.matchParentSize().padding(start = bubbleStart),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                color = ink,
                fontFamily = FontFirst(),
                fontWeight = if (visual == OptionVisual.CORRECT) FontWeight.Bold else FontWeight.Normal,
                fontSize = textSize,
                maxLines = textLines,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
                // Takie samo wciecie z obu stron - inaczej napis przestalby byc na srodku dymka.
                // Musi wystarczyc, by ominac krazek nachodzacy na lewa krawedz.
                modifier = Modifier.padding(horizontal = textInset, vertical = textPadding)
            )
        }

        AnswerBadge(
            label = label,
            gemTint = gem,
            size = badgeSize,
            modifier = Modifier.align(Alignment.CenterStart).offset(x = ANSWER_BADGE_NUDGE)
        )
    }
}

/**
 * Krazek z numerem odpowiedzi - zlota oprawa bez filtra, kamien w srodku pomnozony przez [gemTint],
 * czyli ten sam kolor co kamienie na dymku obok.
 */
@Composable
private fun AnswerBadge(
    label: String,
    gemTint: Color,
    modifier: Modifier = Modifier,
    size: Dp = ANSWER_BADGE_SIZE
) {
    Box(modifier = modifier.size(size), contentAlignment = Alignment.Center) {
        Image(
            painter = painterResource(Resources.QuizArt.answerBadgeGem),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            colorFilter = ColorFilter.tint(gemTint, BlendMode.Modulate)
        )
        Image(
            painter = painterResource(Resources.QuizArt.answerBadgeFrame),
            contentDescription = null,
            modifier = Modifier.fillMaxSize()
        )
        Text(
            text = label,
            color = Color.White,
            fontFamily = FontFirst(),
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp,
            // Kamien nie jest wysrodkowany w swoim canvasie - jego srodek lezy na 41% wysokosci
            // pliku, nie na 50%. Litera rysowana w srodku Boxa wypadalaby wiec ponizej srodka
            // kamienia; ten offset sadza ja dokladnie na nim. Ulamek, a nie stala w dp, zeby
            // trzymal sie przy kazdym rozmiarze krazka.
            modifier = Modifier.offset(y = -size * BADGE_GEM_CENTER_OFFSET)
        )
    }
}

/** O ile srodek kamienia w [Resources.QuizArt.answerBadgeGem] lezy nad srodkiem jego canvasu. */
private const val BADGE_GEM_CENTER_OFFSET = 0.088f

/**
 * Wysokosc, ponizej ktorej dymek przestaje byc czytelny - i jedyny prog decydujacy o tym, czy
 * siatka odpowiedzi idzie w jedna czy w dwie kolumny (patrz [answerColumns]).
 *
 * Grafika dymka ma proporcje ~3.8:1, a kafel pelnej szerokosci na telefonie w pionie to ok. 300 dp:
 * przy 78 dp rozciagniecie w poziomie schodzi ponizej progu, na ktorym widac deformacje kamieni, a
 * w bialym polu mieszcza sie dwie linijki tekstu. Ponizej ok. 62 dp napis zaczyna sie urywac, wiec
 * lepiej zlozyc opcje na dwie kolumny niz sciskac je w jedna.
 */
private val ANSWER_TILE_READABLE_HEIGHT = 62.dp

/**
 * Absolutna podloga wysokosci kafla. Siatka dzieli miejsce rowno, wiec normalnie nikt tego nie
 * dotyka - to zabezpieczenie na wypadek panelu nizszego niz cokolwiek, co przewidzielismy
 * (np. telefon w poziomie albo przyszly tryb z wieksza arena).
 */
private val ANSWER_TILE_FLOOR_HEIGHT = 40.dp

/** Najwyzszy dymek odpowiedzi - mniej wiecej naturalna proporcja grafiki przy pelnej szerokosci panelu. */
private val ANSWER_TILE_MAX_HEIGHT = 86.dp

/** Odstep miedzy kaflami - ten sam w pionie i w poziomie, zeby siatka byla rowna. */
private val ANSWER_TILE_GAP = 10.dp

/**
 * Wiecej niz dwie kolumny nie ma sensu: przy trzech na dymek zostaje mniej niz sto dp szerokosci
 * i kazda odpowiedz dluzsza od jednego slowa i tak zostalaby przycieta.
 */
private const val ANSWER_MAX_COLUMNS = 2

/** Srednica krazka z numerem przy PELNEJ wysokosci kafla. */
private val ANSWER_BADGE_SIZE = 58.dp

/** Krazek nie moze zejsc ponizej tego - przestalby byc czytelny jako numer odpowiedzi. */
private val ANSWER_BADGE_MIN_SIZE = 30.dp

/**
 * Od ktorego miejsca zaczyna sie sam dymek, liczone w srednicach krazka. Krazek wystaje w lewo
 * poza jego krawedz i czesciowo ja przykrywa, jak w oryginalnej grafice. Przy pelnym krazku
 * (58 dp) daje to 26 dp, czyli dokladnie tyle, ile wynosila dawna stala.
 */
private const val ANSWER_BUBBLE_START_RATIO = 0.45f

/** Ile napis ma zostawic luzu za krazkiem, zeby nie stykal sie z jego krawedzia. */
private val ANSWER_TEXT_CLEARANCE = 3.dp

/**
 * Jaka czesc wysokosci kafla zajmuje krazek. Przy pelnowymiarowym kaflu (78 dp) daje to dokladnie
 * dawne 58 dp; przy niskim kaflu krazek kurczy sie razem z nim, zamiast z niego wystawac.
 */
private const val ANSWER_BADGE_HEIGHT_RATIO = 0.74f

/**
 * Ponizej tej szerokosci kafel uznajemy za WASKI (czyli: jestesmy w dwoch kolumnach) i pozwalamy
 * napisowi na jeden wiersz wiecej - w waskim dymku dluzsza odpowiedz potrzebuje wysokosci, nie
 * przyciecia.
 */
private val ANSWER_NARROW_TILE_WIDTH = 200.dp

/**
 * Przesuniecie krazka w prawo wzgledem lewej krawedzi kafla. Jego srodek ma leciec nieco ZA
 * krawedzia dymka, a nie dokladnie na niej - inaczej krazek zwiesza sie na zewnatrz i odkleja
 * od ramki. Poczatek dymka liczy sie teraz z polowy krazka, bo ten zmienia rozmiar.
 */
private val ANSWER_BADGE_NUDGE = 7.dp
