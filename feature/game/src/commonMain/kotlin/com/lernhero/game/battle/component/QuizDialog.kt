package com.lernhero.game.battle.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.CubicBezierEasing
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
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
internal val BubbleBg = Color(0xFF241A2E)
internal val BubbleText = Color(0xFFF6EEDD)

/** Ksztalt panelu i "kutej", zlotej ramki wokol niego - dwie warstwy zlota daja wrazenie ozdobnej oprawy. */
internal val PanelShape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
internal val PanelGradient = Brush.verticalGradient(listOf(Color(0xFFFAF1DE), PanelBg))
internal val FrameGradient = Brush.verticalGradient(listOf(Color(0xFFFFE9A8), PanelBorder, Color(0xFF8A6B34)))

/** Kolory ponumerowanych krazkow przy odpowiedziach - stale przypisanie do pozycji 1-4, jak w grach z quizami. */
private val OptionColors = listOf(
    Color(0xFF8B5CF6), // 1/A - fiolet
    Color(0xFF22A55E), // 2/B - zielen
    Color(0xFF2F8FE0), // 3/C - blekit
    Color(0xFFE08A2F) // 4/D - pomarancz
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
                .background(PanelGradient)
                .border(3.dp, FrameGradient, PanelShape)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (question is OrderingQuestion) {
                        SequenceProgressRow(question = question, orderedSelection = quiz.orderedSelection)
                    }

                    AnswerCountdownBar(questionId = question.id, revealed = quiz.revealed)

                    QuestionEntrance(questionId = question.id) {
                        QuestionBubble(prompt = question.prompt)
                    }

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

                    if (quiz.revealed) {
                        RevealBanner(
                            correct = quiz.answeredCorrectly,
                            correctText = revealCorrectText(question, quiz),
                            reward = question.reward
                        )
                    }
                }

                // Odpowiedzi jedna pod druga, pelnej szerokosci. Najpierw na miejsce trafia pytanie
                // ([QUESTION_LEAD_MILLIS]), dopiero potem wjezdzaja z boku kolejne odpowiedzi -
                // 1, 2, 3, 4 - kazda o [TILE_STAGGER_MILLIS] pozniej od poprzedniej.
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    var visibleCount by remember(question.id) { mutableStateOf(0) }
                    LaunchedEffect(question.id) {
                        visibleCount = 0
                        val optionCount = when (question) {
                            is MultipleChoiceQuestion -> question.answers.size
                            is OrderingQuestion -> question.items.size
                            is MultiSelectQuestion -> question.options.size
                        }
                        delay(QUESTION_LEAD_MILLIS)
                        repeat(optionCount) {
                            visibleCount += 1
                            delay(TILE_STAGGER_MILLIS)
                        }
                    }

                    when (question) {
                        is MultipleChoiceQuestion -> question.answers.forEachIndexed { index, answer ->
                            AnimatedVisibility(
                                visible = index < visibleCount,
                                enter = fadeIn(tween(TILE_ENTER_MILLIS, easing = EaseOutCubic)) +
                                    slideInHorizontally(
                                        animationSpec = tween(TILE_ENTER_MILLIS, easing = EaseOutCubic)
                                    ) { fullWidth -> fullWidth / 2 }
                            ) {
                                val visual = when {
                                    !quiz.revealed -> OptionVisual.IDLE
                                    answer.isCorrect -> OptionVisual.CORRECT
                                    quiz.selectedAnswerId == answer.id -> OptionVisual.WRONG
                                    else -> OptionVisual.FADED
                                }
                                QuizOptionTile(
                                    label = "${index + 1}",
                                    badgeColor = OptionColors[index % OptionColors.size],
                                    text = answer.text,
                                    visual = visual,
                                    enabled = !quiz.revealed,
                                    onClick = { onAnswer(answer.id) },
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }

                        is OrderingQuestion -> question.items.forEachIndexed { index, item ->
                            AnimatedVisibility(
                                visible = index < visibleCount,
                                enter = fadeIn(tween(TILE_ENTER_MILLIS, easing = EaseOutCubic)) +
                                    slideInHorizontally(
                                        animationSpec = tween(TILE_ENTER_MILLIS, easing = EaseOutCubic)
                                    ) { fullWidth -> fullWidth / 2 }
                            ) {
                                val position = quiz.orderedSelection.indexOf(item.id)
                                val visual = when {
                                    quiz.revealed -> if (position == item.correctPosition) OptionVisual.CORRECT else OptionVisual.WRONG
                                    position != -1 -> OptionVisual.SELECTED
                                    else -> OptionVisual.IDLE
                                }
                                QuizOptionTile(
                                    label = if (position != -1) "${position + 1}" else itemLabel(index),
                                    badgeColor = OptionColors[index % OptionColors.size],
                                    text = item.text,
                                    visual = visual,
                                    enabled = !quiz.revealed && position == -1,
                                    onClick = { onOrderPick(item.id) },
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }

                        // "Zaznacz wszystkie prawdziwe" - w odroznieniu od pozostalych typow nic
                        // nie rozstrzyga sie samo: gracz przelacza dowolna liczbe opcji i dopiero
                        // ZATWIERDZ konczy ture. Po odsłonieciu widac komplet - co bylo poprawne,
                        // co gracz przeoczyl i co zaznaczyl niepotrzebnie.
                        is MultiSelectQuestion -> question.options.forEachIndexed { index, option ->
                            AnimatedVisibility(
                                visible = index < visibleCount,
                                enter = fadeIn(tween(TILE_ENTER_MILLIS, easing = EaseOutCubic)) +
                                    slideInHorizontally(
                                        animationSpec = tween(TILE_ENTER_MILLIS, easing = EaseOutCubic)
                                    ) { fullWidth -> fullWidth / 2 }
                            ) {
                                val picked = option.id in quiz.multiSelection
                                val visual = when {
                                    !quiz.revealed -> if (picked) OptionVisual.SELECTED else OptionVisual.IDLE
                                    option.isCorrect -> OptionVisual.CORRECT
                                    picked -> OptionVisual.WRONG
                                    else -> OptionVisual.FADED
                                }
                                QuizOptionTile(
                                    label = if (picked) "✔" else itemLabel(index),
                                    badgeColor = OptionColors[index % OptionColors.size],
                                    text = option.text,
                                    visual = visual,
                                    enabled = !quiz.revealed,
                                    onClick = { onMultiToggle(option.id) },
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }

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

/** Litera przypisana do elementu ukladanki wedlug jego miejsca w wyswietlonej (przetasowanej) liscie - A, B, C, D... */
private fun itemLabel(displayIndex: Int): String = ('A' + displayIndex).toString()

/** Poprawna odpowiedz/kolejnosc pokazywana w [RevealBanner] po nietrafionej probie. */
private fun revealCorrectText(question: QuizQuestion, quiz: LiveQuizState): String = when (question) {
    is MultipleChoiceQuestion -> quiz.correctAnswer?.text.orEmpty()
    is OrderingQuestion -> question.items.sortedBy { it.correctPosition }.joinToString(" → ") { it.text }
    is MultiSelectQuestion -> question.options.filter { it.isCorrect }.joinToString(", ") { it.text }
}

/**
 * Przycisk konczacy pytanie wielokrotnego wyboru. Licznik zaznaczen jest tu celowo: przy szescioma
 * i wiecej opcjami latwo zgubic rachube, ile sie juz kliknelo.
 */
@Composable
private fun MultiSelectConfirm(pickedCount: Int, enabled: Boolean, onConfirm: () -> Unit) {
    val accent = if (enabled) Color(0xFF22A55E) else Color(0xFF7A6A4A)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 4.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(accent)
            .then(if (enabled) Modifier.clickable(onClick = onConfirm) else Modifier)
            .padding(vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = if (pickedCount > 0) "ZATWIERDZ ($pickedCount)" else "ZAZNACZ ODPOWIEDZI",
            color = Color.White,
            fontFamily = FontFirst(),
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp
        )
    }
}

/**
 * Wejscie samego dymka pytania - pierwszy element, ktory laduje na miejscu w panelu (odpowiedzi ida
 * po nim, patrz [QUESTION_LEAD_MILLIS]). Odtwarzane raz na kazde nowe [questionId].
 */
@Composable
private fun QuestionEntrance(questionId: String, content: @Composable () -> Unit) {
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

/** Dymek pytania - ciemna, kontrastowa "karteczka" nad odpowiedziami, jak w grze z reference. */
@Composable
internal fun QuestionBubble(prompt: String, modifier: Modifier = Modifier) {
    Column(modifier = modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(18.dp))
                .background(BubbleBg)
        ) {
            Text(
                text = prompt,
                color = BubbleText,
                fontFamily = FontFirst(),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp)
            )
        }
        // Ogonek dymka - maly trojkat pod lewa krawedzia, wskazujacy w strone bohatera nad panelem.
        Box(
            modifier = Modifier
                .padding(start = 22.dp)
                .size(14.dp)
                .clip(BubbleTailShape)
                .background(BubbleBg)
        )
    }
}

private val BubbleTailShape = androidx.compose.foundation.shape.GenericShape { size, _ ->
    moveTo(0f, 0f)
    lineTo(size.width, 0f)
    lineTo(size.width / 2f, size.height)
    close()
}

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
 * Pasek czasu na odpowiedz tuz nad pytaniem - startuje pelny ([AnswerTimer.SEGMENTS] segmentow) w
 * chwili pojawienia sie pytania i opada segment po segmencie przez [AnswerTimer.WINDOW_MS] (kilka
 * sekund, nie blyskawicznie). Im wiecej segmentow zostalo w chwili odpowiedzi, tym mocniej druzyna
 * uderzy w tej turze - stad kolor jedzie od czerwonego (ostatni, najslabszy segment) do zielonego
 * (pelna moc). Liczba sekund obok paska pokazuje dokladnie, ile jeszcze zostalo.
 *
 * Ten sam komponent (i to samo miejsce w panelu) uzywa [com.lernhero.game.battle.component.BossEventQuizBar]
 * dla zaklecia bossa - std parametr [label], zeby podpis dalo sie dopasowac do kontekstu.
 */
@Composable
internal fun AnswerCountdownBar(
    questionId: String,
    revealed: Boolean,
    label: String = "SILA CIOSU",
    modifier: Modifier = Modifier
) {
    var remainingFraction by remember(questionId) { mutableFloatStateOf(1f) }

    LaunchedEffect(questionId, revealed) {
        if (revealed) return@LaunchedEffect
        val start = TimeSource.Monotonic.markNow()
        while (isActive) {
            val elapsedMs = start.elapsedNow().inWholeMilliseconds
            remainingFraction = (1f - elapsedMs.toFloat() / AnswerTimer.WINDOW_MS).coerceIn(0f, 1f)
            if (remainingFraction <= 0f) break
            withFrameMillis {}
        }
    }

    // Po odpowiedzi petla wyzej sie zatrzymuje, wiec remainingFraction zamraza sie dokladnie na
    // tej wartosci, ktora policzyla mnoznik obrazen tej tury - pasek pokazuje to, co faktycznie trafilo.
    val litSegments = AnswerTimer.segmentIndexFor(remainingFraction) + 1
    val leadColor by animateColorAsState(segmentColor(litSegments - 1))
    val remainingSeconds = remainingFraction * AnswerTimer.WINDOW_MS / 1000f

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                color = InkMuted,
                fontFamily = FontFirst(),
                fontWeight = FontWeight.Bold,
                fontSize = 10.sp
            )
            Text(
                text = "${(remainingSeconds * 10).toInt() / 10f}s",
                color = leadColor,
                fontFamily = FontFirst(),
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp
            )
        }

        Row(
            modifier = Modifier
                .padding(top = 4.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            repeat(AnswerTimer.SEGMENTS) { index ->
                val lit = index < litSegments
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(12.dp)
                        .clip(RoundedCornerShape(50))
                        .background(if (lit) segmentColor(index) else Color(0xFFE0D2B0))
                        .border(1.dp, if (lit) leadColor else PanelBorder, RoundedCornerShape(50))
                )
            }
        }
    }
}

/**
 * Kolor kazdego segmentu paska - od czerwonego (indeks 0, ostatni zostaly) po zielony (ostatni
 * indeks, pelna moc), plynnie interpolowany, wiec dziala dla dowolnej liczby [AnswerTimer.SEGMENTS]
 * zamiast sztywnej tabeli na 5 wpisow.
 */
private fun segmentColor(index: Int): Color {
    val t = (index.toFloat() / (AnswerTimer.SEGMENTS - 1).coerceAtLeast(1)).coerceIn(0f, 1f)
    return lerp(Color(0xFFD1453D), Color(0xFF22A55E), t)
}

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
                .background(PanelGradient)
                .border(3.dp, FrameGradient, PanelShape)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    // W takcie [TurnPhase.Impact] nikt jeszcze nie bije - cala uwaga ma byc na
                    // rozliczeniu odpowiedzi na srodku ekranu, wiec pasek nie udaje trwajacej walki.
                    text = state.message
                        ?: if (state.turnPhase == TurnPhase.Impact) "Salwa sie laduje..." else "Exchanging blows...",
                    color = InkStrong,
                    fontFamily = FontFirst(),
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
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
 * Dziesiec kratek ostatnich odpowiedzi - zielona za dobra, czerwona za zla, szara jeszcze pusta.
 * Piec zielonych z rzedu (nawet miedzy cyklami) odpala ulti gdzie indziej w viewmodelu.
 */
@Composable
private fun StreakPipRow(streak: StreakTracker, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        streak.pips.forEach { pip ->
            Box(
                modifier = Modifier
                    .size(13.dp)
                    .clip(CircleShape)
                    .background(
                        when (pip) {
                            true -> Color(0xFF22A55E)
                            false -> Color(0xFFD1453D)
                            null -> Color(0xFFDDCCA8)
                        }
                    )
            )
        }
    }
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
private val AnswerFadedBg = Color(0xFFEADFC6)
private val AnswerFadedText = Color(0xFFB1A283)

/** Stan wizualny jednej "pigulki" odpowiedzi - wspolny dla wyboru pojedynczego i ukladanki. */
private enum class OptionVisual { IDLE, SELECTED, CORRECT, WRONG, FADED }

/**
 * Pelnej szerokosci "pigulka" odpowiedzi: kolorowy krazek z etykieta (numer albo litera) i tekst.
 * [OptionVisual.SELECTED] to stan posredni ukladanki - element juz klikniety, czeka na reszte,
 * ale wynik jeszcze nie jest znany, wiec jest tylko wygaszony, bez koloru zielonego/czerwonego.
 */
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
    val container by animateColorAsState(
        when (visual) {
            OptionVisual.IDLE -> Color.White
            OptionVisual.SELECTED -> AnswerFadedBg
            OptionVisual.CORRECT -> AnswerGreen
            OptionVisual.WRONG -> AnswerRed
            OptionVisual.FADED -> AnswerFadedBg
        }
    )
    val content = when (visual) {
        OptionVisual.CORRECT, OptionVisual.WRONG -> Color.White
        OptionVisual.FADED, OptionVisual.SELECTED -> AnswerFadedText
        OptionVisual.IDLE -> InkStrong
    }
    val borderColor = when (visual) {
        OptionVisual.IDLE -> PanelBorder
        OptionVisual.SELECTED -> AnswerFadedText
        OptionVisual.CORRECT -> AnswerGreen
        OptionVisual.WRONG -> AnswerRed
        OptionVisual.FADED -> AnswerFadedBg
    }

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(26.dp))
            .background(container)
            .border(2.dp, borderColor, RoundedCornerShape(26.dp))
            .then(if (enabled) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(28.dp)
                .clip(CircleShape)
                .background(badgeColor),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = label,
                color = Color.White,
                fontFamily = FontFirst(),
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp
            )
        }
        Text(
            text = text,
            color = content,
            fontFamily = FontFirst(),
            fontWeight = if (visual == OptionVisual.CORRECT) FontWeight.Bold else FontWeight.Normal,
            fontSize = 14.sp,
            maxLines = 3,
            modifier = Modifier.weight(1f)
        )
    }
}

/** Jednozdaniowy werdykt pod odpowiedziami. */
@Composable
private fun RevealBanner(
    correct: Boolean,
    correctText: String,
    reward: Int
) {
    val color = if (correct) AnswerGreen else AnswerRed
    val text = if (correct) "Correct! +$reward pts" else "Wrong. Correct answer: $correctText"

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(color.copy(alpha = 0.14f))
            .border(1.dp, color, RoundedCornerShape(12.dp))
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Text(
            text = text,
            color = color,
            fontFamily = FontFirst(),
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp
        )
    }
}
