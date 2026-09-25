package com.lernhero.home.quiz

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.size
import com.lernhero.home.ui.OutlinedText
import androidx.compose.ui.graphics.Brush
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lernhero.domain.monetization.PremiumLimits
import com.lernhero.domain.quiz.QuizDifficulty
import com.lernhero.domain.quiz.ai.QuizGenerationRequest
import com.lernhero.domain.quiz.custom.CustomDeck
import com.lernhero.domain.quiz.custom.CustomQuestion
import com.lernhero.domain.quiz.custom.CustomQuestionType
import com.lernhero.shared.monetization.PremiumPaywall
import com.lernhero.shared.theme.FontFirst
import org.koin.compose.koinInject
import com.lernhero.shared.theme.FontPixel

/**
 * Edytor jednej talii: nazwa, generowanie przez model i lista pytan.
 *
 * Kolejnosc na ekranie jest celowa - generowanie stoi NAD lista pytan, bo najczestszy przebieg to
 * "popros model o zestaw, przejrzyj, popraw dwa pytania, dopisz jedno swoje". Reczne pisanie od
 * zera jest w pelni mozliwe, ale to nie jest droga, ktora ktokolwiek wybiera przy 30 pytaniach.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DeckEditorScreen(
    viewModel: CustomQuizViewModel,
    deckId: String,
    onBack: () -> Unit,
    startWithAi: Boolean = false,
    insets: PaddingValues = PaddingValues(0.dp)
) {
    val state by viewModel.uiState.collectAsState()
    val isPremium by viewModel.isPremium.collectAsState()
    var paywallOpen by remember { mutableStateOf(false) }

    LaunchedEffect(deckId) { viewModel.open(deckId) }

    // Zapis takze przy wyjsciu SYSTEMOWYM gestem wstecz - tam nasz przycisk "‹ WSTECZ" nigdy nie
    // zostanie klikniety, a napisana talia nie moze przepadac przez to, ktorej drogi wyjscia
    // gracz uzyl. Powtorny zapis jest nieszkodliwy, bo nadpisuje ten sam dokument.
    DisposableEffect(deckId) {
        onDispose { viewModel.save() }
    }

    val deck = state.draft
    var editing by remember { mutableStateOf<CustomQuestion?>(null) }
    var addingType by remember { mutableStateOf(false) }
    var aiOpen by remember { mutableStateOf(startWithAi) }
    var confirmDelete by remember { mutableStateOf(false) }

    Surface(modifier = Modifier.fillMaxSize(), color = Color(0xFF140E0A)) {
        // Naglowek z POWROTEM i ZAPISEM zostaje przyklejony - przewijaja sie same pytania.
        // Przy talii na kilkadziesiat pozycji przycisk zapisu uciekal poza ekran dokladnie
        // wtedy, gdy byl potrzebny.
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(insets)
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            EditorHeader(
                deck = deck,
                dirty = state.dirty,
                onBack = {
                    // Wyjscie zapisuje - talia napisana i porzucona przez cofniecie systemowym
                    // gestem byla by najgorszym mozliwym zachowaniem tego ekranu.
                    viewModel.save()
                    viewModel.closeEditor()
                    onBack()
                },
                onSave = { viewModel.save() }
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
            if (deck == null) {
                Text(
                    text = "Couldn't find this deck.",
                    color = DeckMuted,
                    fontFamily = FontFirst(),
                    fontSize = 12.sp
                )
                return@Column
            }

            DeckField(
                value = deck.name,
                onValueChange = viewModel::rename,
                label = "Nazwa talii (np. Prawo jazdy kat. B)",
                modifier = Modifier.fillMaxWidth()
            )

            AiSection(
                open = aiOpen,
                available = viewModel.aiAvailable,
                isPremium = isPremium,
                remainingThisMonth = state.remainingAiGenerationsThisMonth,
                onUnlock = { paywallOpen = true },
                generating = state.generating,
                topic = deck.topic,
                onToggle = { aiOpen = !aiOpen },
                onTopicChange = viewModel::setTopic,
                onGenerate = { count, difficulty, types, notes ->
                    viewModel.generate(deck.topic, count, difficulty, types, notes)
                }
            )

            if (state.warnings.isNotEmpty()) {
                WarningsPanel(warnings = state.warnings, onDismiss = viewModel::clearWarnings)
            }

            state.message?.let { message ->
                Text(
                    text = message,
                    color = DeckGold,
                    fontFamily = FontFirst(),
                    fontSize = 11.sp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF241E18))
                        .clickable { viewModel.clearMessage() }
                        .padding(10.dp)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                DeckSectionLabel("Questions (${deck.turnReady.size} ready of ${deck.questions.size})")
                if (deck.brokenCount > 0) {
                    Text(
                        text = "${deck.brokenCount} do dokonczenia",
                        color = DeckRed,
                        fontFamily = FontFirst(),
                        fontSize = 10.sp
                    )
                }
            }

            if (deck.questions.isEmpty()) {
                Text(
                    text = "Empty. Add a question by hand or ask the model above for a ready set.",
                    color = DeckMuted,
                    fontFamily = FontFirst(),
                    fontSize = 11.sp
                )
            }

            deck.questions.forEach { question ->
                QuestionRow(
                    question = question,
                    onClick = { editing = question },
                    onDelete = { viewModel.removeQuestion(question.id) }
                )
            }

            DeckButton(
                label = "+ ADD QUESTION",
                onClick = { addingType = true },
                color = Color(0xFF3F9E2B),
                modifier = Modifier.fillMaxWidth()
            )

            if (!deck.isPlayable) {
                Text(
                    text = "Battle needs ${CustomDeck.MIN_QUESTIONS} finished questions " +
                        "(\"estimate\" questions count separately - they only appear against a boss).",
                    color = DeckMuted,
                    fontFamily = FontFirst(),
                    fontSize = 10.sp
                )
            }

            DeckButton(
                label = "DELETE DECK",
                onClick = { confirmDelete = true },
                color = Color(0xFF8C2A22),
                fontSize = 12,
                modifier = Modifier.fillMaxWidth()
            )
            }
        }
    }

    if (addingType) {
        TypePickerDialog(
            onPick = { type ->
                addingType = false
                editing = viewModel.blankQuestion(type, QuizDifficulty.Medium)
            },
            onDismiss = { addingType = false }
        )
    }

    editing?.let { question ->
        QuestionEditorDialog(
            question = question,
            onSave = {
                viewModel.upsertQuestion(it)
                editing = null
            },
            onDismiss = { editing = null },
            onDelete = if (deck?.questions?.any { it.id == question.id } == true) {
                {
                    viewModel.removeQuestion(question.id)
                    editing = null
                }
            } else {
                null
            }
        )
    }

    if (paywallOpen || state.subscriptionRequired) {
        PremiumPaywall(
            subscription = koinInject(),
            reason = "AI generation is a Premium feature.",
            onDismiss = {
                paywallOpen = false
                viewModel.dismissSubscriptionPrompt()
            }
        )
    }

    if (confirmDelete && deck != null) {
        ConfirmDialog(
            title = "DELETE DECK?",
            body = "\"${deck.displayName}\" and all ${deck.questions.size} questions are gone for good.",
            onConfirm = {
                confirmDelete = false
                viewModel.delete(deck.id)
                viewModel.closeEditor()
                onBack()
            },
            onDismiss = { confirmDelete = false }
        )
    }
}

@Composable
private fun EditorHeader(
    deck: CustomDeck?,
    dirty: Boolean,
    onBack: () -> Unit,
    onSave: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(42.dp)
                .clip(CircleShape)
                .background(Brush.verticalGradient(listOf(Color(0xFF5A3F24), Color(0xFF2C1D10))))
                .border(2.5.dp, Color(0xFF120B05), CircleShape)
                .clickable(onClick = onBack),
            contentAlignment = Alignment.Center
        ) {
            OutlinedText(text = "‹", size = 26.sp, color = DeckGold)
        }
        OutlinedText(
            text = deck?.displayName ?: "Tome",
            size = 20.sp,
            color = DeckGold,
            maxLines = 1,
            modifier = Modifier.weight(1f)
        )
        DeckButton(
            label = if (dirty) "SAVE •" else "SAVED",
            onClick = onSave,
            color = if (dirty) Color(0xFF3F9E2B) else Color(0xFF3A2E22),
            textColor = if (dirty) Color.White else DeckMuted,
            fontSize = 13
        )
    }
}

/**
 * Zamowienie na pytania od modelu. Panel jest domyslnie zwiniety - gracz, ktory chce pisac sam,
 * nie powinien musiec go omijac wzrokiem przy kazdym pytaniu.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun AiSection(
    open: Boolean,
    available: Boolean,
    isPremium: Boolean,
    remainingThisMonth: Int?,
    onUnlock: () -> Unit,
    generating: Boolean,
    topic: String,
    onToggle: () -> Unit,
    onTopicChange: (String) -> Unit,
    onGenerate: (count: Int, difficulty: QuizDifficulty?, types: Set<CustomQuestionType>, notes: String) -> Unit
) {
    var count by remember { mutableStateOf(QuizGenerationRequest.DEFAULT_COUNT) }
    var difficulty by remember { mutableStateOf<QuizDifficulty?>(null) }
    var types by remember { mutableStateOf(setOf(CustomQuestionType.Choice)) }
    var notes by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Brush.verticalGradient(listOf(Color(0xFF3A1E6E), Color(0xFF1E1038))))
            .border(3.dp, Color(0xFF120B05), RoundedCornerShape(16.dp))
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().clickable(onClick = onToggle),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "📜", fontSize = 28.sp)
            Column(modifier = Modifier.weight(1f)) {
                OutlinedText(text = "AI Scroll", size = 18.sp, color = Color.White, strokeWidth = 5f)
                Text(
                    text = if (isPremium) "Give a topic - get a ready deck" else "Premium - the Sage writes decks for you",
                    color = Color(0xFFCBB6F0),
                    fontFamily = FontFirst(),
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp
                )
            }
            if (!isPremium) {
                Text(
                    text = "👑",
                    fontSize = 16.sp,
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(Brush.verticalGradient(listOf(Color(0xFFFFE27A), Color(0xFFE0A21E))))
                        .border(2.dp, Color(0xFF120B05), CircleShape)
                        .padding(6.dp)
                )
            }
            Text(text = if (open) "▲" else "▼", color = Color(0xFFCBB6F0), fontSize = 14.sp)
        }

        if (!open) return@Column

        // Bez subskrypcji generowanie jest zamkniete w calosci - formularz nie ma sensu, skoro
        // serwer i tak odmowi. Zamiast niego jedna rzecz do zrobienia: odblokuj.
        if (!isPremium) {
            listOf(
                "✨" to "Up to ${PremiumLimits.PREMIUM_AI_QUIZZES_PER_MONTH} decks a month on any topic",
                "🎯" to "Pick the level, question count and types",
                "⚡" to "A full deck in seconds - ready for battle"
            ).forEach { (icon, line) ->
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(icon, fontSize = 15.sp)
                    Text(line, color = Color.White, fontFamily = FontFirst(), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }
            DeckButton(label = "👑 UNLOCK PREMIUM", onClick = onUnlock, color = Color(0xFFE0A21E), modifier = Modifier.fillMaxWidth())
            Text(
                text = "You can still write questions by hand below.",
                color = Color(0xFFCBB6F0),
                fontFamily = FontFirst(),
                fontSize = 11.sp
            )
            return@Column
        }

        remainingThisMonth?.let { left ->
            Text(
                text = "$left of ${PremiumLimits.PREMIUM_AI_QUIZZES_PER_MONTH} generations left this month",
                color = DeckGold,
                fontFamily = FontFirst(),
                fontSize = 10.sp
            )
        }

        if (!available) {
            Text(
                text = "The model isn't connected yet - the screen works, but generating returns nothing. " +
                    "Until then, write questions by hand below.",
                color = DeckMuted,
                fontFamily = FontFirst(),
                fontSize = 10.sp
            )
        }

        DeckField(
            value = topic,
            onValueChange = onTopicChange,
            label = "Temat (np. znaki drogowe, prawo jazdy kat. B)",
            modifier = Modifier.fillMaxWidth()
        )

        DeckSectionLabel("How many questions")
        FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            listOf(5, 10, 20, 30).forEach { option ->
                DeckChip(
                    label = option.toString(),
                    selected = count == option,
                    onClick = { count = option }
                )
            }
        }

        DeckSectionLabel("Level")
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            DeckChip(label = "Mixed", selected = difficulty == null, onClick = { difficulty = null })
            QuizDifficulty.entries.forEach { level ->
                DeckChip(
                    label = level.label,
                    selected = difficulty == level,
                    onClick = { difficulty = level }
                )
            }
        }

        DeckSectionLabel("Question types")
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            CustomQuestionType.entries.forEach { type ->
                DeckChip(
                    label = type.label,
                    selected = type in types,
                    // Ostatni zaznaczony typ zostaje - zamowienie bez zadnego typu nie ma sensu.
                    onClick = {
                        types = if (type in types) {
                            (types - type).ifEmpty { types }
                        } else {
                            types + type
                        }
                    }
                )
            }
        }

        DeckField(
            value = notes,
            onValueChange = { notes = it },
            label = "Dodatkowe wytyczne (opcjonalnie)",
            singleLine = false,
            minLines = 2,
            modifier = Modifier.fillMaxWidth()
        )

        DeckButton(
            label = if (generating) "✨ THE SAGE IS WRITING..." else "✨ GENERATE AND ADD",
            onClick = { if (!generating) onGenerate(count, difficulty, types, notes) },
            enabled = !generating,
            color = Color(0xFF8A3FE0),
            modifier = Modifier.fillMaxWidth()
        )
        Text(
            text = "Questions are added to the deck - they don't erase what's already there.",
            color = DeckMuted,
            fontFamily = FontFirst(),
            fontSize = 10.sp
        )
    }
}

@Composable
private fun WarningsPanel(warnings: List<String>, onDismiss: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(Color(0xFF2A2114))
            .border(1.dp, DeckGold.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
            .clickable(onClick = onDismiss)
            .padding(10.dp),
        verticalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        Text(
            text = "THE MODEL ALSO SENT SOMETHING UNUSABLE:",
            color = DeckGold,
            fontFamily = FontPixel(),
            fontSize = 10.sp
        )
        warnings.take(6).forEach { warning ->
            Text(text = "• $warning", color = DeckMuted, fontFamily = FontFirst(), fontSize = 10.sp)
        }
        if (warnings.size > 6) {
            Text(
                text = "...and ${warnings.size - 6} more.",
                color = DeckMuted,
                fontFamily = FontFirst(),
                fontSize = 10.sp
            )
        }
    }
}

@Composable
private fun QuestionRow(
    question: CustomQuestion,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    val issues = question.issues()
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(DeckPanel)
            .border(
                width = 1.dp,
                color = if (issues.isEmpty()) DeckFrameDim.copy(alpha = 0.6f) else DeckRed.copy(alpha = 0.6f),
                shape = RoundedCornerShape(10.dp)
            )
            .clickable(onClick = onClick)
            .padding(10.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            Text(
                text = question.prompt.ifBlank { "(bez tresci)" },
                color = DeckCream,
                fontFamily = FontFirst(),
                fontSize = 12.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = "${question.type.label} • ${question.difficulty.label}" +
                    issues.firstOrNull()?.let { " • $it" }.orEmpty(),
                color = if (issues.isEmpty()) DeckMuted else DeckRed,
                fontFamily = FontFirst(),
                fontSize = 10.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xFF241E18))
                .clickable(onClick = onDelete)
                .padding(horizontal = 10.dp, vertical = 8.dp)
        ) {
            Text(text = "🗑", fontSize = 12.sp)
        }
    }
}

@Composable
private fun TypePickerDialog(onPick: (CustomQuestionType) -> Unit, onDismiss: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xE607050B))
            .clickable(onClick = onDismiss),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .padding(24.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(DeckPanel)
                .border(2.dp, DeckFrame, RoundedCornerShape(14.dp))
                .clickable(enabled = false) {}
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            DeckSectionLabel("Question type")
            CustomQuestionType.entries.forEach { type ->
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(DeckInk)
                        .border(1.dp, DeckFrameDim, RoundedCornerShape(10.dp))
                        .clickable { onPick(type) }
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(
                        text = type.label,
                        color = DeckGold,
                        fontFamily = FontPixel(),
                        fontSize = 12.sp
                    )
                    Text(text = type.hint, color = DeckMuted, fontFamily = FontFirst(), fontSize = 10.sp)
                }
            }
        }
    }
}

@Composable
private fun ConfirmDialog(
    title: String,
    body: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xE607050B))
            .clickable(onClick = onDismiss),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .padding(28.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(DeckPanel)
                .border(2.dp, DeckRed.copy(alpha = 0.7f), RoundedCornerShape(14.dp))
                .clickable(enabled = false) {}
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                color = DeckRed,
                fontFamily = FontPixel(),
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp
            )
            Text(text = body, color = DeckCream, fontFamily = FontFirst(), fontSize = 11.sp)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                DeckButton(
                    label = "CANCEL",
                    onClick = onDismiss,
                    color = Color(0xFF2A2018),
                    textColor = DeckCream,
                    fontSize = 11,
                    modifier = Modifier.weight(1f)
                )
                DeckButton(
                    label = "DELETE",
                    onClick = onConfirm,
                    color = Color(0xFF6B241C),
                    fontSize = 11,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}
