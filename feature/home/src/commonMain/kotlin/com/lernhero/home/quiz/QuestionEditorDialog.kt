package com.lernhero.home.quiz

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lernhero.domain.quiz.QuizDifficulty
import com.lernhero.domain.quiz.custom.CustomQuestion
import com.lernhero.domain.quiz.custom.CustomQuestionType
import com.lernhero.shared.theme.FontFirst
import com.lernhero.shared.theme.FontPixel

/**
 * Formularz jednego pytania - ten sam dla pytania pisanego od zera i dla pytania, ktore przyszlo
 * z modelu. To jest cala odpowiedz na "moze poprosic AI i potem nawet poprawic po AI": pytanie
 * z generowania jest zwyklym wpisem talii, wiec ladunek z modelu otwiera sie w tym samym oknie.
 *
 * Bledy nie blokuja zapisu - pytanie nie do konca skonczone zostaje w talii i jest widoczne na
 * liscie jako "do dokonczenia". Do walki nie wejdzie (patrz [CustomQuestion.isValid]), ale nie
 * przepadnie przez to, ze gracz musial na chwile wyjsc.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun QuestionEditorDialog(
    question: CustomQuestion,
    onSave: (CustomQuestion) -> Unit,
    onDismiss: () -> Unit,
    onDelete: (() -> Unit)? = null
) {
    var draft by remember(question.id) { mutableStateOf(question) }
    val issues = draft.issues()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xE607050B))
            .clickable(onClick = onDismiss),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 420.dp)
                .padding(16.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(DeckPanel)
                .border(2.dp, DeckFrame, RoundedCornerShape(16.dp))
                .clickable(enabled = false) {}
                .padding(14.dp)
                .heightIn(max = 620.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = "QUESTION",
                color = DeckGold,
                fontFamily = FontPixel(),
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )

            DeckSectionLabel("Type")
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                CustomQuestionType.entries.forEach { type ->
                    DeckChip(
                        label = type.label,
                        selected = draft.type == type,
                        // Zmiana rodzaju zachowuje tresc i poziom, a reszte pol buduje od nowa -
                        // odpowiedzi z "wyboru jednej" nie znacza nic w "ulóz w kolejnosci".
                        onClick = {
                            if (draft.type != type) {
                                draft = CustomQuestion.blank(draft.id, type, draft.difficulty)
                                    .copy(prompt = draft.prompt)
                            }
                        }
                    )
                }
            }
            Text(
                text = draft.type.hint,
                color = DeckMuted,
                fontFamily = FontFirst(),
                fontSize = 10.sp
            )

            DeckField(
                value = draft.prompt,
                onValueChange = { draft = draft.copy(prompt = it) },
                label = "Question text",
                singleLine = false,
                minLines = 2,
                modifier = Modifier.fillMaxWidth()
            )

            DeckSectionLabel("Level")
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                QuizDifficulty.entries.forEach { level ->
                    DeckChip(
                        label = level.label,
                        selected = draft.difficulty == level,
                        onClick = { draft = draft.copy(difficulty = level) }
                    )
                }
            }

            when (draft.type) {
                CustomQuestionType.Choice -> {
                    DeckSectionLabel("Correct answer")
                    DeckField(
                        value = draft.correct.firstOrNull().orEmpty(),
                        onValueChange = { draft = draft.copy(correct = listOf(it)) },
                        label = "Correct",
                        modifier = Modifier.fillMaxWidth()
                    )
                    DeckSectionLabel("Wrong answers")
                    AnswerRows(
                        values = draft.wrong,
                        label = "Wrong",
                        minRows = CustomQuestion.MIN_WRONG_ANSWERS,
                        maxRows = CustomQuestion.MAX_WRONG_ANSWERS,
                        onChange = { draft = draft.copy(wrong = it) }
                    )
                }

                CustomQuestionType.MultiSelect -> {
                    DeckSectionLabel("Correct (mark all that apply)")
                    AnswerRows(
                        values = draft.correct,
                        label = "Correct",
                        minRows = 1,
                        maxRows = CustomQuestion.MAX_OPTIONS - 1,
                        onChange = { draft = draft.copy(correct = it) }
                    )
                    DeckSectionLabel("Wrong")
                    AnswerRows(
                        values = draft.wrong,
                        label = "Wrong",
                        minRows = 1,
                        maxRows = CustomQuestion.MAX_OPTIONS - 1,
                        onChange = { draft = draft.copy(wrong = it) }
                    )
                }

                CustomQuestionType.Ordering -> {
                    DeckSectionLabel("Items in CORRECT order")
                    Text(
                        text = "The player will see them shuffled - here you write the pattern.",
                        color = DeckMuted,
                        fontFamily = FontFirst(),
                        fontSize = 10.sp
                    )
                    OrderingRows(
                        values = draft.items,
                        onChange = { draft = draft.copy(items = it) }
                    )
                }

                CustomQuestionType.Estimation -> {
                    DeckSectionLabel("Target value")
                    NumberField(
                        value = draft.correctValue,
                        label = "Correct value",
                        onChange = { draft = draft.copy(correctValue = it) }
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        NumberField(
                            value = draft.minValue,
                            label = "Range from",
                            onChange = { draft = draft.copy(minValue = it) },
                            modifier = Modifier.weight(1f)
                        )
                        NumberField(
                            value = draft.maxValue,
                            label = "Range to",
                            onChange = { draft = draft.copy(maxValue = it) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        DeckField(
                            value = draft.unit,
                            onValueChange = { draft = draft.copy(unit = it) },
                            label = "Unit",
                            modifier = Modifier.weight(1f)
                        )
                        NumberField(
                            value = draft.tolerance,
                            label = "Tolerance",
                            onChange = { draft = draft.copy(tolerance = it) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Text(
                        text = "Tolerance is how many units off still counts as a hit. " +
                            "Leave blank and it will be calculated automatically.",
                        color = DeckMuted,
                        fontFamily = FontFirst(),
                        fontSize = 10.sp
                    )
                }
            }

            if (issues.isNotEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF2A1512))
                        .border(1.dp, DeckRed.copy(alpha = 0.6f), RoundedCornerShape(8.dp))
                        .padding(10.dp),
                    verticalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    issues.forEach { issue ->
                        Text(text = "• $issue", color = DeckRed, fontFamily = FontFirst(), fontSize = 10.sp)
                    }
                    Text(
                        text = "You can still save - the question will wait in the deck, but won't enter battle.",
                        color = DeckMuted,
                        fontFamily = FontFirst(),
                        fontSize = 10.sp
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                DeckButton(
                    label = "CANCEL",
                    onClick = onDismiss,
                    color = Color(0xFF2A2018),
                    textColor = DeckCream,
                    fontSize = 11,
                    modifier = Modifier.weight(1f)
                )
                onDelete?.let { delete ->
                    DeckButton(
                        label = "DELETE",
                        onClick = delete,
                        color = Color(0xFF3A1A16),
                        textColor = DeckRed,
                        fontSize = 11,
                        modifier = Modifier.weight(1f)
                    )
                }
                DeckButton(
                    label = "SAVE",
                    onClick = { onSave(draft) },
                    color = if (issues.isEmpty()) Color(0xFF2E6B3F) else DeckAccent,
                    fontSize = 11,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

/** Lista pol odpowiedzi z dokladaniem i usuwaniem - wspolna dla wyboru jednej i wielu. */
@Composable
private fun AnswerRows(
    values: List<String>,
    label: String,
    minRows: Int,
    maxRows: Int,
    onChange: (List<String>) -> Unit
) {
    val rows = if (values.size < minRows) values + List(minRows - values.size) { "" } else values

    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        rows.forEachIndexed { index, value ->
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                DeckField(
                    value = value,
                    onValueChange = { text ->
                        onChange(rows.toMutableList().also { it[index] = text })
                    },
                    label = "$label ${index + 1}",
                    modifier = Modifier.weight(1f)
                )
                if (rows.size > minRows) {
                    SmallSquare(symbol = "✕", color = DeckRed) {
                        onChange(rows.filterIndexed { i, _ -> i != index })
                    }
                }
            }
        }
        if (rows.size < maxRows) {
            DeckButton(
                label = "+ ADD",
                onClick = { onChange(rows + "") },
                color = Color(0xFF2A2018),
                textColor = DeckGold,
                fontSize = 10
            )
        }
    }
}

/** Elementy ukladanki - tu kolejnosc JEST trescia, wiec doszly strzalki przesuwania. */
@Composable
private fun OrderingRows(values: List<String>, onChange: (List<String>) -> Unit) {
    val rows = if (values.size < CustomQuestion.MIN_ORDERING_ITEMS) {
        values + List(CustomQuestion.MIN_ORDERING_ITEMS - values.size) { "" }
    } else {
        values
    }

    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        rows.forEachIndexed { index, value ->
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${index + 1}.",
                    color = DeckGold,
                    fontFamily = FontPixel(),
                    fontSize = 12.sp
                )
                DeckField(
                    value = value,
                    onValueChange = { text ->
                        onChange(rows.toMutableList().also { it[index] = text })
                    },
                    label = "Element ${index + 1}",
                    modifier = Modifier.weight(1f)
                )
                SmallSquare(symbol = "▲", color = DeckCream, enabled = index > 0) {
                    onChange(rows.toMutableList().also {
                        val moved = it.removeAt(index)
                        it.add(index - 1, moved)
                    })
                }
                SmallSquare(symbol = "▼", color = DeckCream, enabled = index < rows.lastIndex) {
                    onChange(rows.toMutableList().also {
                        val moved = it.removeAt(index)
                        it.add(index + 1, moved)
                    })
                }
                if (rows.size > CustomQuestion.MIN_ORDERING_ITEMS) {
                    SmallSquare(symbol = "✕", color = DeckRed) {
                        onChange(rows.filterIndexed { i, _ -> i != index })
                    }
                }
            }
        }
        if (rows.size < CustomQuestion.MAX_ORDERING_ITEMS) {
            DeckButton(
                label = "+ ADD",
                onClick = { onChange(rows + "") },
                color = Color(0xFF2A2018),
                textColor = DeckGold,
                fontSize = 10
            )
        }
    }
}

/** Pole liczbowe - puste znaczy "nie podano", nie zero. */
@Composable
private fun NumberField(
    value: Int?,
    label: String,
    onChange: (Int?) -> Unit,
    modifier: Modifier = Modifier
) {
    DeckField(
        value = value?.toString().orEmpty(),
        onValueChange = { text ->
            val cleaned = text.filter { it.isDigit() || it == '-' }
            onChange(if (cleaned.isBlank() || cleaned == "-") null else cleaned.toIntOrNull())
        },
        label = label,
        modifier = modifier
    )
}

@Composable
private fun SmallSquare(
    symbol: String,
    color: Color,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFF241E18))
            .border(1.dp, DeckFrameDim, RoundedCornerShape(8.dp))
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = 9.dp, vertical = 9.dp)
    ) {
        Text(
            text = symbol,
            color = if (enabled) color else DeckFrameDim,
            fontSize = 11.sp
        )
    }
}
