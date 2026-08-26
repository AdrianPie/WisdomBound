package com.lernhero.reader

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lernhero.domain.reading.Book
import com.lernhero.domain.reading.BookContent
import com.lernhero.domain.reading.ReadingGoal
import com.lernhero.domain.reading.ReadingGoalCatalog
import com.lernhero.domain.reading.ReadingGoalKind
import com.lernhero.domain.repository.PlayerRepository
import com.lernhero.shared.theme.FontFirst
import com.lernhero.shared.theme.FontPixel
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

private val Bg = Color(0xFF120E0A)
private val Panel = Color(0xFF1A1410)
private val Ink = Color(0xFF0F0B08)
private val Gold = Color(0xFFFFD100)
private val GoldFrame = Color(0xFFC8A45A)
private val GoldDim = Color(0xFF6B5A38)
private val Muted = Color(0xFF9D8B6A)
private val Green = Color(0xFF4ADE80)
private val Amber = Color(0xFFE0913A)

/**
 * Zakladka "Czytelnia": biblioteka (bundlowane + wgrane ksiazki, statystyki, cele dzienne)
 * albo otwarty czytnik - jedno miejsce, przelaczane lokalnym stanem, bez osobnej trasy nawigacji
 * (bajty ksiazki nie nadaja sie do przekazania przez argumenty nawigacji).
 */
@Composable
fun ReadingScreen(
    /**
     * Marginesy pod paski aplikacji - dotycza WYLACZNIE biblioteki. Otwarta ksiazka chowa paski
     * (ReaderChrome.immersive) i zajmuje caly ekran, wiec ich nie dostaje.
     */
    libraryInsets: PaddingValues = PaddingValues(0.dp)
) {
    val viewModel = koinViewModel<LibraryViewModel>()
    val state by viewModel.uiState.collectAsState()
    val scope = rememberCoroutineScope()

    var openBook by remember { mutableStateOf<Book?>(null) }
    var openBytes by remember { mutableStateOf<ByteArray?>(null) }
    var openContent by remember { mutableStateOf<BookContent?>(null) }
    var opening by remember { mutableStateOf<String?>(null) }

    val requestImport = rememberBookFilePicker(onPicked = { picked ->
        val book = viewModel.onBookImported(picked)
        openBook = book
        openBytes = picked.bytes
        scope.launch { openContent = viewModel.loadContent(book) }
    })

    fun open(book: Book) {
        opening = book.id
        scope.launch {
            // Reflow potrzebuje tekstu, podglad oryginalu - bajtow. Format decyduje, czego szukamy.
            val content = viewModel.loadContent(book)
            val bytes = if (content == null) viewModel.loadBytes(book) else null
            opening = null
            if (content != null || bytes != null) {
                openBook = book
                openContent = content
                openBytes = bytes
            }
        }
    }

    fun closeReader() {
        openBook = null
        openBytes = null
        openContent = null
        viewModel.refresh()
    }

    Surface(modifier = Modifier.fillMaxSize(), color = Bg) {
        val book = openBook
        val bytes = openBytes
        val content = openContent
        if (book != null && content != null) {
            ReflowReaderView(
                book = book,
                content = content,
                startBlockIndex = state.pageFor(book),
                initialSettings = state.readerSettings,
                onSettingsChanged = viewModel::saveReaderSettings,
                onClose = ::closeReader
            )
        } else if (book != null && bytes != null) {
            ReaderView(
                book = book,
                bytes = bytes,
                startPage = state.pageFor(book),
                onClose = ::closeReader
            )
        } else {
            LibraryView(
                state = state,
                openingId = opening,
                onOpen = ::open,
                onImport = requestImport,
                onClaim = viewModel::claim,
                insets = libraryInsets
            )
        }
    }
}

@Composable
private fun LibraryView(
    state: LibraryUiState,
    openingId: String?,
    onOpen: (Book) -> Unit,
    onImport: () -> Unit,
    onClaim: (ReadingGoal) -> Unit,
    insets: PaddingValues
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            // Marginesy pod paski SA CZESCIA obszaru przewijania, nie ramka wokol niego - dzieki
            // temu tresc przewija sie pod paskiem, zamiast urywac sie tuz nad nim.
            .verticalScroll(rememberScrollState())
            .padding(insets)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text(
            text = "Czytelnia",
            color = Gold,
            fontFamily = FontPixel(),
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.headlineSmall
        )

        StatsCard(state)

        Text(
            text = "Cele dnia",
            color = Gold,
            fontFamily = FontPixel(),
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.titleMedium
        )
        ReadingGoalCatalogList(state = state, onClaim = onClaim)

        LibrarySection(
            title = "Z wygasla licencja",
            books = state.bundledBooks,
            state = state,
            openingId = openingId,
            onOpen = onOpen
        )

        LibrarySection(
            title = "Twoja biblioteka",
            books = state.importedBooks,
            state = state,
            openingId = openingId,
            onOpen = onOpen,
            emptyHint = "Nie masz jeszcze wgranych ksiazek."
        )

        ImportButton(onClick = onImport)

        state.message?.let { message ->
            Text(text = message, color = Green, fontFamily = FontFirst(), fontSize = 12.sp)
        }

        Spacer(modifier = Modifier.height(4.dp))
    }
}

@Composable
private fun StatsCard(state: LibraryUiState) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Panel)
            .border(2.dp, GoldFrame, RoundedCornerShape(12.dp))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            StatBlock(label = "Stron", value = "${state.readingStats.totalPages}")
            StatBlock(label = "Minut", value = "${state.readingStats.totalMinutes}")
            StatBlock(label = "Slow", value = "${state.readingStats.totalWords}")
            StatBlock(label = "Tempo", value = "${state.readingStats.averageWpm}/min")
        }
        Text(
            text = "Dzisiaj: ${state.readingToday.minutes} min, ${state.readingToday.pages} stron",
            color = Muted,
            fontFamily = FontFirst(),
            fontSize = 11.sp
        )
    }
}

@Composable
private fun StatBlock(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = value, color = Gold, fontFamily = FontPixel(), fontWeight = FontWeight.Bold, fontSize = 16.sp)
        Text(text = label, color = Muted, fontFamily = FontFirst(), fontSize = 10.sp)
    }
}

@Composable
private fun ReadingGoalCatalogList(state: LibraryUiState, onClaim: (ReadingGoal) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        ReadingGoalCatalog.DAILY.forEach { goal ->
            val progress = goal.progress(state.readingToday).coerceAtMost(goal.threshold)
            val complete = goal.isComplete(state.readingToday)
            val claimed = state.claimedToday(goal)
            val ratio = if (goal.threshold <= 0) 0f else progress.toFloat() / goal.threshold.toFloat()

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(Ink)
                    .border(1.5.dp, if (complete && !claimed) Gold else GoldDim, RoundedCornerShape(10.dp))
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(text = goal.title, color = Gold, fontFamily = FontPixel(), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text(text = goal.description, color = Muted, fontFamily = FontFirst(), fontSize = 11.sp)
                    }
                    Text(text = "+${goal.reward} zl", color = GoldFrame, fontFamily = FontFirst(), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }

                Box(
                    modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)).background(GoldDim.copy(alpha = 0.4f))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(ratio.coerceIn(0f, 1f))
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(if (complete) Green else GoldFrame)
                    )
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    val unit = if (goal.kind == ReadingGoalKind.Minutes) " min" else " str."
                    Text(text = "$progress / ${goal.threshold}$unit", color = Muted, fontFamily = FontFirst(), fontSize = 10.sp)

                    val enabled = complete && !claimed
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (enabled) Color(0xFFB4451F) else Ink)
                            .border(1.dp, if (enabled) Gold else GoldDim, RoundedCornerShape(8.dp))
                            .clickable(enabled = enabled) { onClaim(goal) }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = when { claimed -> "Odebrano"; complete -> "Odbierz"; else -> "Za wczesnie" },
                            color = when { claimed -> Green; enabled -> Color.White; else -> Muted },
                            fontFamily = FontFirst(),
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun LibrarySection(
    title: String,
    books: List<Book>,
    state: LibraryUiState,
    openingId: String?,
    onOpen: (Book) -> Unit,
    emptyHint: String? = null
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(text = title, color = Gold, fontFamily = FontPixel(), fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
        if (books.isEmpty() && emptyHint != null) {
            Text(text = emptyHint, color = Muted, fontFamily = FontFirst(), fontSize = 11.sp)
        } else {
            books.forEach { book ->
                BookRow(
                    book = book,
                    page = state.bookProgress[book.id],
                    loading = openingId == book.id,
                    onClick = { onOpen(book) }
                )
            }
        }
    }
}

@Composable
private fun BookRow(book: Book, page: Int?, loading: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(Panel)
            .border(1.5.dp, GoldDim, RoundedCornerShape(10.dp))
            .clickable(onClick = onClick)
            .padding(12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Ink)
                .border(1.dp, GoldFrame, RoundedCornerShape(8.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(text = book.format.name.uppercase(), color = GoldFrame, fontFamily = FontFirst(), fontWeight = FontWeight.Bold, fontSize = 10.sp)
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(text = book.title, color = Color(0xFFEADFC8), fontFamily = FontFirst(), fontWeight = FontWeight.Bold, fontSize = 13.sp)
            Text(
                text = book.author + if (page != null && page > 0) "  -  strona ${page + 1}" else "",
                color = Muted,
                fontFamily = FontFirst(),
                fontSize = 10.sp
            )
        }
        if (loading) {
            CircularProgressIndicator(color = Gold, modifier = Modifier.size(18.dp))
        } else {
            Text(text = "Czytaj", color = GoldFrame, fontFamily = FontFirst(), fontWeight = FontWeight.Bold, fontSize = 11.sp)
        }
    }
}

@Composable
private fun ImportButton(onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(Color(0xFFB4451F))
            .border(1.5.dp, Gold, RoundedCornerShape(10.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text = "+ Dodaj ksiazke (TXT, PDF)", color = Color.White, fontFamily = FontPixel(), fontWeight = FontWeight.Bold, fontSize = 13.sp)
    }
}

/* --------------------------------------------------------------------------------------------- */

@Composable
private fun ReaderView(
    book: Book,
    bytes: ByteArray,
    startPage: Int,
    onClose: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val playerRepository = koinInject<PlayerRepository>()

    val session = remember(book.id) { ReaderSession(book, bytes, startPage, playerRepository, scope) }
    val state = session.uiState
    var zoom by remember { mutableStateOf(1f) }
    var chromeVisible by remember { mutableStateOf(true) }

    // Chowa gorny/dolny pasek nawigacji i systemowy pasek statusu na czas czytania -
    // strona ma zajac caly ekran, bez zadnego chromu ponad nia.
    androidx.compose.runtime.DisposableEffect(Unit) {
        ReaderChrome.immersive = true
        setStatusBarHidden(true)
        onDispose {
            ReaderChrome.immersive = false
            setStatusBarHidden(false)
        }
    }
    OnLeaveScreen { session.close() }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0A0806))
    ) {
        when {
            state.error != null -> Text(
                text = state.error,
                color = Amber,
                fontFamily = FontFirst(),
                textAlign = TextAlign.Center,
                modifier = Modifier.align(Alignment.Center).padding(24.dp)
            )

            state.loading || state.pageImage == null -> CircularProgressIndicator(color = Gold, modifier = Modifier.align(Alignment.Center))

            else -> {
                val image = state.pageImage
                val vScroll = remember(state.pageIndex) { androidx.compose.foundation.ScrollState(0) }
                Box(
                    modifier = Modifier.fillMaxSize().verticalScroll(vScroll),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        bitmap = image,
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxSize()
                            .graphicsLayer(
                                scaleX = zoom,
                                scaleY = zoom,
                                transformOrigin = TransformOrigin(0.5f, 0.5f)
                            ),
                        contentScale = ContentScale.Fit
                    )
                }

                // Brzegi kartkuja strone, srodek chowa/pokazuje maly pasek na dole.
                Row(modifier = Modifier.fillMaxSize()) {
                    Box(modifier = Modifier.fillMaxHeight().weight(0.2f).clickable { session.previousPage() })
                    Box(modifier = Modifier.fillMaxHeight().weight(0.6f).clickable { chromeVisible = !chromeVisible })
                    Box(modifier = Modifier.fillMaxHeight().weight(0.2f).clickable { session.nextPage() })
                }
            }
        }

        Text(
            text = "‹",
            color = Gold,
            fontFamily = FontPixel(),
            fontWeight = FontWeight.Bold,
            fontSize = 22.sp,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(top = 40.dp, start = 14.dp)
                .clip(CircleShape)
                .background(Color(0x99120E0A))
                .clickable(onClick = onClose)
                .padding(horizontal = 10.dp, vertical = 4.dp)
        )

        androidx.compose.animation.AnimatedVisibility(
            visible = chromeVisible,
            enter = androidx.compose.animation.fadeIn() + androidx.compose.animation.slideInVertically { it },
            exit = androidx.compose.animation.fadeOut() + androidx.compose.animation.slideOutVertically { it },
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            ReaderBottomBar(
                state = state,
                clock = session.clock.state,
                zoom = zoom,
                onZoomOut = { zoom = (zoom - 0.25f).coerceAtLeast(0.75f) },
                onZoomIn = { zoom = (zoom + 0.25f).coerceAtMost(2.5f) }
            )
        }
    }
}

/**
 * Cienki, polprzezroczysty pasek nad tekstem - powrot, licznik stron, czas aktywnego czytania
 * i przyciski powiekszenia. Nie zajmuje wlasnej przestrzeni w Column jak dawniej: lezy NA
 * stronie, wiec sama strona nadal ma cala wysokosc ekranu pod spodem.
 */
@Composable
private fun ReaderBottomBar(
    state: ReaderUiState,
    clock: ReadingClockState,
    zoom: Float,
    onZoomOut: () -> Unit,
    onZoomIn: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .padding(bottom = 22.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(Color(0xCC120E0A))
            .border(1.dp, GoldDim, RoundedCornerShape(20.dp))
            .padding(horizontal = 12.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(if (clock.paused) Amber else Green))
        Text(
            text = if (clock.paused) {
                "wstrzymano"
            } else {
                "${clock.sessionSeconds / 60}:${(clock.sessionSeconds % 60).toString().padStart(2, '0')}"
            },
            color = if (clock.paused) Amber else Muted,
            fontFamily = FontFirst(),
            fontSize = 10.sp
        )

        Text(
            text = "${state.pageIndex + 1}/${state.pageCount}",
            color = Gold,
            fontFamily = FontFirst(),
            fontWeight = FontWeight.Bold,
            fontSize = 11.sp
        )

        ZoomButton(text = "–", enabled = zoom > 0.75f, onClick = onZoomOut)
        Text(text = "${(zoom * 100).toInt()}%", color = Muted, fontFamily = FontFirst(), fontSize = 9.sp, modifier = Modifier.width(30.dp), textAlign = TextAlign.Center)
        ZoomButton(text = "+", enabled = zoom < 2.5f, onClick = onZoomIn)
    }
}

@Composable
private fun ZoomButton(text: String, enabled: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(20.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(if (enabled) Ink else Ink.copy(alpha = 0.4f))
            .border(1.dp, if (enabled) GoldFrame else GoldDim, RoundedCornerShape(6.dp))
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(text = text, color = if (enabled) Gold else Muted, fontFamily = FontPixel(), fontWeight = FontWeight.Bold, fontSize = 12.sp)
    }
}

@Composable
private fun OnLeaveScreen(action: () -> Unit) {
    androidx.compose.runtime.DisposableEffect(Unit) {
        onDispose { action() }
    }
}
