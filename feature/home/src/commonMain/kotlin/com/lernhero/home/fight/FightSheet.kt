package com.lernhero.home.fight

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lernhero.home.ui.DIALOG_CLOSE_LANE
import com.lernhero.home.ui.DialogCloseButton
import com.lernhero.home.ui.DialogScrim
import com.lernhero.shared.theme.FontFirst
import com.lernhero.shared.theme.FontPixel

/**
 * Wspolna oprawa okien wyprawy - druzyny ([PartySheet]) i materialu ([MaterialSheet]).
 *
 * Oba otwieraja sie NA scenie, a nie zamiast niej: wybor skladu i wybor tematu to przygotowanie
 * do tego samego wejscia, wiec gracz ma caly czas widziec, dokad idzie. Stad przyciemnienie
 * zamiast pelnego ekranu i krzyzyk u dolu, w zasiegu kciuka - tak samo jak w karcie postaci
 * (patrz [com.lernhero.home.ui.DialogChrome]).
 *
 * Ciemna plyta ze zlota ramka, a nie pergamin karty postaci: pergamin nalezy do rzeczy, ktore
 * gracz OGLADA (statystyki, przedmioty), a te dwa okna sa ustawieniem robionym w biegu, tuz przed
 * naciśnieciem START - maja czytac sie jako czesc sceny, nie jako osobny ekran.
 */
@Composable
fun FightSheet(
    title: String,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
    /** Jedna linijka pod tytulem - po co tu wlasciwie jestesmy. */
    subtitle: String? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    Box(modifier = modifier.fillMaxSize()) {
        DialogScrim(onDismiss = onClose)

        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .systemBarsPadding()
                .padding(horizontal = 14.dp)
                // Dolne miejsce nalezy do krzyzyka - okno konczy sie nad nim, a nie pod nim.
                .padding(top = 8.dp, bottom = DIALOG_CLOSE_LANE)
                .fillMaxWidth()
                .widthIn(max = SHEET_MAX_WIDTH)
                .clip(RoundedCornerShape(18.dp))
                .background(Brush.verticalGradient(listOf(SheetTop, SheetBottom)))
                .border(2.5.dp, SheetFrame, RoundedCornerShape(18.dp))
                .padding(3.dp)
        ) {
            SheetHeader(title = title, subtitle = subtitle)
            Column(
                // Okno ma wysokosc swojej tresci, dopoki ta miesci sie na ekranie - dluga lista
                // (kolekcja kompanow, tematy) zaczyna przewijac sie w srodku, zamiast rozpychac
                // okno poza krawedz.
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 11.dp, vertical = 11.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                content = content
            )
        }

        DialogCloseButton(
            onClick = onClose,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(bottom = 14.dp)
        )
    }
}

/** Belka tytulu - ciemniejsza od plyty, zeby okno mialo gore, a nie samo zaczynalo sie trescia. */
@Composable
private fun SheetHeader(title: String, subtitle: String?) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(topStart = 15.dp, topEnd = 15.dp, bottomStart = 4.dp, bottomEnd = 4.dp))
            .background(SheetHeaderBg)
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Text(
            text = title,
            color = SheetGold,
            fontFamily = FontPixel(),
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp
        )
        subtitle?.let {
            Text(
                text = it,
                color = SheetMuted,
                fontFamily = FontFirst(),
                fontSize = 11.sp
            )
        }
    }
}

/** Naglowek sekcji w srodku okna - male, zlote wersaliki. */
@Composable
fun SheetSectionLabel(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text.uppercase(),
        color = SheetGold,
        fontFamily = FontPixel(),
        fontWeight = FontWeight.Bold,
        fontSize = 11.sp,
        modifier = modifier
    )
}

/** Zdanie wyjasniajace pod sekcja - szare, male, nigdy nie krzyczy. */
@Composable
fun SheetHint(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        color = SheetMuted,
        fontFamily = FontFirst(),
        fontSize = 10.sp,
        modifier = modifier
    )
}

/** Pasek pod naglowkiem z dwoma liczbami po bokach - np. sila druzyny. */
@Composable
fun SheetStatRow(content: @Composable () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(SheetInk)
            .border(1.dp, SheetFrameDim, RoundedCornerShape(10.dp))
            .padding(horizontal = 10.dp, vertical = 7.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        content()
    }
}

/** Najszersze, na ile okno moze urosnac - na tablecie ma zostac oknem, a nie rozlac sie na ekran. */
private val SHEET_MAX_WIDTH = 420.dp

internal val SheetTop = Color(0xFF241A11)
internal val SheetBottom = Color(0xFF17110B)
internal val SheetHeaderBg = Color(0xFF3A2A18)
internal val SheetFrame = Color(0xFFC8A45A)
internal val SheetFrameDim = Color(0xFF6B5A38)
internal val SheetInk = Color(0xFF120E0A)
internal val SheetCream = Color(0xFFF4E9D7)
internal val SheetMuted = Color(0xFF9D8B6A)
internal val SheetGold = Color(0xFFFFD34D)
internal val SheetGreen = Color(0xFF4ADE80)
internal val SheetRed = Color(0xFFE56A5D)
