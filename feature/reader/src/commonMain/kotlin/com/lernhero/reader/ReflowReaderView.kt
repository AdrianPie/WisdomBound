package com.lernhero.reader

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lernhero.domain.reading.Book
import com.lernhero.domain.reading.BookContent
import com.lernhero.domain.reading.ReaderSettings
import com.lernhero.domain.reading.ReaderTheme
import com.lernhero.domain.reading.ReadingAnchor
import com.lernhero.domain.reading.SavedWord
import com.lernhero.domain.repository.PlayerRepository
import com.lernhero.reader.reflow.ReaderPalette
import com.lernhero.reader.reflow.ReflowReader
import com.lernhero.reader.reflow.SelectionScope
import com.lernhero.reader.reflow.WordSelection
import com.lernhero.reader.translate.platformTranslationSource
import com.lernhero.shared.theme.FontFirst
import com.lernhero.shared.theme.FontPixel
import kotlin.time.Clock
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

/**
 * Czytnik w trybie reflow razem z calym chromem: gorny pasek, dolny licznik i panel ustawien.
 *
 * Chrome jest domyslnie ukryty i lezy NAD strona, a nie obok niej - tekst ma caly ekran dla
 * siebie, dokladnie jak w czytnikach, na ktorych sie wzorujemy.
 */
@Composable
fun ReflowReaderView(
    book: Book,
    content: BookContent,
    startAnchor: ReadingAnchor,
    /** Kazda zmiana strony - zapis miejsca per ksiazka, patrz LibraryViewModel.savePosition. */
    onPositionChanged: (ReadingAnchor) -> Unit,
    initialSettings: ReaderSettings,
    /** Wolane raz, przy zamknieciu czytnika - patrz PlayerRepository.updateReaderSettings. */
    onSettingsChanged: (ReaderSettings) -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
    /** Dzisiejszy czas czytania z konta w chwili otwarcia - baza pierscienia celu. */
    todayBaseSeconds: Int = 0
) {
    val scope = rememberCoroutineScope()
    val playerRepository = koinInject<PlayerRepository>()
    // ML Kit na Androidzie, Apple Translation Framework na iOS (26+) - oba offline, bez klucza
    // API. Ponizej minimalnej wersji systemowej (albo na desktopie) dostajemy TranslationSource.Stub.
    val translationSource = remember { platformTranslationSource() }

    val clock = remember(book.id) { ReadingClock(playerRepository, scope, todayBaseSeconds) }
    val clockState = clock.state

    var settings by remember { mutableStateOf(initialSettings) }
    var chromeVisible by remember { mutableStateOf(false) }
    var settingsVisible by remember { mutableStateOf(false) }
    var anchor by remember(book.id) { mutableStateOf(startAnchor) }
    // Zaznaczenie zyje tutaj, nie w ReflowReader - popup tlumaczenia jest nad calym ekranem
    // czytnika, wiec musi widziec ten sam stan, ktory ReflowReader tylko zglasza wyzej.
    var selection by remember { mutableStateOf<WordSelection?>(null) }
    var savedFeedback by remember { mutableStateOf(false) }

    val palette = ReaderPalette.of(settings.theme)

    // Chowa dolna nawigacje aplikacji i systemowy pasek statusu - strona zajmuje caly ekran.
    DisposableEffect(Unit) {
        ReaderChrome.immersive = true
        setStatusBarHidden(true)
        onDispose {
            ReaderChrome.immersive = false
            setStatusBarHidden(false)
            clock.close()
            onSettingsChanged(settings)
        }
    }

    fun toggleChrome() {
        chromeVisible = !chromeVisible
        selection = null
        if (!chromeVisible) settingsVisible = false
    }

    Box(modifier = modifier.fillMaxSize().background(palette.background)) {
        ReflowReader(
            content = content,
            settings = settings,
            startAnchor = anchor,
            palette = palette,
            selection = selection,
            onAnchorChanged = { newAnchor ->
                // Kotwica to dokladny poczatek strony, wiec po powrocie pageIndexOf trafia na te
                // sama strone. Zapisywany dawniej sam indeks akapitu cofal na strone, na ktorej
                // akapit sie ZACZYNAL.
                if (newAnchor != anchor) {
                    anchor = newAnchor
                    // Kotwica znakowa, nie numer strony: strona w reflow zmienia znaczenie przy
                    // kazdej zmianie czcionki, a akapit + znak zostaje tym samym miejscem.
                    onPositionChanged(newAnchor)
                }
            },
            onPageAdvanced = { words -> clock.onPageAdvanced(words) },
            onWordTapped = {
                clock.onInteraction()
                selection = it
            },
            onToggleChrome = ::toggleChrome
        )

        // Licznik celu - zawsze widoczny, maly, w kolorach motywu strony.
        if (!chromeVisible) {
            ReadingGoalPill(
                todaySeconds = clockState.todaySeconds,
                paused = clockState.paused,
                ink = palette.text,
                modifier = Modifier.align(Alignment.BottomEnd).navigationBarsPadding().padding(bottom = 6.dp, end = 12.dp)
            )
        }
        GoalReachedBanner(
            todaySeconds = clockState.todaySeconds,
            modifier = Modifier.align(Alignment.TopCenter).padding(top = 40.dp)
        )

        AnimatedVisibility(
            visible = chromeVisible,
            enter = fadeIn() + slideInVertically { -it },
            exit = fadeOut() + slideOutVertically { -it },
            modifier = Modifier.align(Alignment.TopCenter)
        ) {
            ReaderTopBar(
                title = book.title,
                clock = clockState,
                onClose = onClose,
                onSettings = { settingsVisible = !settingsVisible }
            )
        }

        AnimatedVisibility(
            visible = chromeVisible && settingsVisible,
            enter = fadeIn() + slideInVertically { it },
            exit = fadeOut() + slideOutVertically { it },
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            ReaderSettingsPanel(
                settings = settings,
                onSettingsChanged = { settings = it }
            )
        }

        selection?.let { current ->
            TranslationPopup(
                selection = current,
                translationSource = translationSource,
                onDismiss = { selection = null },
                onExpandToSentence = {
                    selection = current.copy(scope = SelectionScope.Sentence)
                },
                onSave = { word ->
                    selection = null
                    savedFeedback = true
                    scope.launch {
                        playerRepository.saveWord(
                            word.copy(
                                bookId = book.id,
                                bookTitle = book.title,
                                // Znacznik czasu nadaje dopiero to miejsce - popup tlumaczenia
                                // nie zna zegara i zostawia go na zerze.
                                savedAt = Clock.System.now().toEpochMilliseconds()
                            )
                        )
                    }
                }
            )
        }

        AnimatedVisibility(
            visible = savedFeedback,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            SavedToast(onDismissed = { savedFeedback = false })
        }
    }
}

@Composable
private fun SavedToast(onDismissed: () -> Unit) {
    androidx.compose.runtime.LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(1400)
        onDismissed()
    }
    Text(
        text = "Saved to flashcards",
        color = Color(0xFF120E0A),
        fontFamily = FontFirst(),
        fontWeight = FontWeight.Bold,
        fontSize = 12.sp,
        modifier = Modifier
            .padding(bottom = 40.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(Color(0xFFFFD100))
            .padding(horizontal = 16.dp, vertical = 10.dp)
    )
}

@Composable
private fun ReaderTopBar(
    title: String,
    clock: ReadingClockState,
    onClose: () -> Unit,
    onSettings: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF120E0A))
            .padding(top = 44.dp, bottom = 10.dp)
            .padding(horizontal = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "‹",
            color = Color(0xFFFFD100),
            fontFamily = FontPixel(),
            fontWeight = FontWeight.Bold,
            fontSize = 22.sp,
            modifier = Modifier.clickable(onClick = onClose)
        )

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                color = Color(0xFFEADFC8),
                fontFamily = FontFirst(),
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                maxLines = 1
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(if (clock.paused) Color(0xFFE0913A) else Color(0xFF4ADE80))
                )
                Text(
                    text = if (clock.paused) "paused" else "session ${clock.sessionSeconds.asClock()} · today ${clock.todaySeconds.asTimer()}",
                    color = if (clock.paused) Color(0xFFE0913A) else Color(0xFF9D8B6A),
                    fontFamily = FontFirst(),
                    fontSize = 10.sp
                )
                if (clock.wpm > 0) {
                    Text(
                        text = "· ${clock.wpm} wpm",
                        color = Color(0xFF9D8B6A),
                        fontFamily = FontFirst(),
                        fontSize = 10.sp
                    )
                }
            }
        }

        Text(
            text = "Aa",
            color = Color(0xFFC8A45A),
            fontFamily = FontFirst(),
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp,
            modifier = Modifier.clickable(onClick = onSettings)
        )
    }
}

@Composable
private fun ReaderSettingsPanel(
    settings: ReaderSettings,
    onSettingsChanged: (ReaderSettings) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF120E0A))
            .padding(horizontal = 18.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "Text Size",
                color = Color(0xFF9D8B6A),
                fontFamily = FontFirst(),
                fontSize = 11.sp,
                modifier = Modifier.weight(1f)
            )
            StepButton("A-") { onSettingsChanged(settings.withFontSize(settings.fontSizeSp - 1)) }
            Text(
                text = "${settings.fontSizeSp}",
                color = Color(0xFFFFD100),
                fontFamily = FontFirst(),
                fontSize = 12.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.width(34.dp)
            )
            StepButton("A+") { onSettingsChanged(settings.withFontSize(settings.fontSizeSp + 1)) }
        }

        // Interlinia. Wczesniej byla zaszyta na sztywno i za ciasna - ogonki liter z jednej linii
        // niemal dotykaly liter z nastepnej, a gracz nie mial jak tego poprawic.
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "Line Spacing",
                color = Color(0xFF9D8B6A),
                fontFamily = FontFirst(),
                fontSize = 11.sp,
                modifier = Modifier.weight(1f)
            )
            StepButton("−") {
                onSettingsChanged(
                    settings.withLineHeight(settings.effectiveLineHeight - ReaderSettings.LINE_HEIGHT_STEP)
                )
            }
            Text(
                text = settings.effectiveLineHeight.oneDecimal(),
                color = Color(0xFFFFD100),
                fontFamily = FontFirst(),
                fontSize = 12.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.width(34.dp)
            )
            StepButton("+") {
                onSettingsChanged(
                    settings.withLineHeight(settings.effectiveLineHeight + ReaderSettings.LINE_HEIGHT_STEP)
                )
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            ReaderTheme.entries.forEach { theme ->
                ChoiceChip(
                    label = when (theme) {
                        ReaderTheme.Light -> "Light"
                        ReaderTheme.Sepia -> "Sepia"
                        ReaderTheme.Dark -> "Dark"
                    },
                    selected = settings.theme == theme,
                    onClick = { onSettingsChanged(settings.copy(theme = theme)) }
                )
            }
            Spacer(Modifier.weight(1f))
            ChoiceChip(
                label = "Justify",
                selected = settings.justify,
                onClick = { onSettingsChanged(settings.copy(justify = !settings.justify)) }
            )
        }
    }
}

@Composable
private fun StepButton(label: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(30.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFF0F0B08))
            .border(1.dp, Color(0xFFC8A45A), RoundedCornerShape(8.dp))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(text = label, color = Color(0xFFFFD100), fontFamily = FontFirst(), fontSize = 11.sp)
    }
}

@Composable
private fun ChoiceChip(label: String, selected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(if (selected) Color(0xFF3A2C14) else Color(0xFF0F0B08))
            .border(1.dp, if (selected) Color(0xFFFFD100) else Color(0xFF6B5A38), RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        Text(
            text = label,
            color = if (selected) Color(0xFFFFD100) else Color(0xFF9D8B6A),
            fontFamily = FontFirst(),
            fontSize = 10.sp
        )
    }
}

private fun Int.asClock(): String = "${this / 60}:${(this % 60).toString().padStart(2, '0')}"

/** "1.8" - bez toString(Locale) i bez String.format, ktorych nie ma w common. */
private fun Float.oneDecimal(): String {
    val tenths = kotlin.math.round(this * 10).toInt()
    return "${tenths / 10}.${tenths % 10}"
}
