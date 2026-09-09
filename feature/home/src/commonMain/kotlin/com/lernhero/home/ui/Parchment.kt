package com.lernhero.home.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lernhero.shared.resources.Resources
import com.lernhero.shared.theme.FontFirst
import com.lernhero.shared.theme.FontPixel
import org.jetbrains.compose.resources.imageResource
import kotlin.math.roundToInt

/**
 * Zestaw "pergaminowy" - jasne, kremowe panele w grubej brazowej ramce, zolte przyciski i
 * segmentowane zakladki u gory. Odwzorowuje styl ekranow kuzni/bohatera z mobilnych RPG.
 *
 * Kolory sa PLACEHOLDERAMI pod przyszla grafike: caly wyglad da sie przestroic zmieniajac
 * wylacznie stale w [ParchmentPalette], bez dotykania ani jednego ekranu. Gdy dojda prawdziwe
 * ramki 9-patch, podmienia sie tlo w [ParchmentPanel] i reszta pojdzie za nim.
 */
object ParchmentPalette {
    /** Wnetrze panelu - kremowy papier. */
    val Paper = Color(0xFFE8D5B0)
    val PaperDark = Color(0xFFD8BF93)

    /** Wglebienie w panelu (siatki, listy) - ciemniejszy odcien tego samego papieru. */
    val Inset = Color(0xFFC9AC7E)

    /** Ramka - ciemny braz, gruba jak w drewnianej oprawie. */
    val Frame = Color(0xFF6B4A2B)
    val FrameLight = Color(0xFF9A7043)

    /** Tekst na papierze. */
    val Ink = Color(0xFF4A3620)
    val InkMuted = Color(0xFF7A6244)

    /** Zolty przycisk akcji. */
    val Button = Color(0xFFF5CE5A)
    val ButtonEdge = Color(0xFFC79B22)
    val ButtonDisabled = Color(0xFFB5A88A)

    /** Pasek zakladek nad panelem - ciemniejsze drewno. */
    val TabBar = Color(0xFF7A3F5C)
    val TabActive = Color(0xFFE8A63C)

    /** Naglowek panelu - ciemny braz pod tytul/zakladki wewnatrz pergaminu. */
    val TitleBar = Color(0xFF8A5A30)

    /**
     * Karta wewnatrz panelu (tabelka statystyk, pole kosztu) - JASNIEJSZA od papieru, nie
     * ciemniejsza. To ona daje hierarchie: panel jest tlem, karta trescia.
     */
    val Card = Color(0xFFF3E6C8)
    val CardEdge = Color(0xFFB9945E)
    val CardStripe = Color(0x33B08A57)

    /** Wnetrze slotu ekwipunku - ciemne, zeby ikona przedmiotu odcinala sie od papieru. */
    val SlotFill = Color(0xFF5C3E22)
}

/**
 * Panel-pergamin. [title] rysuje sie jako naglowek wewnatrz ramki, tak jak "Forge Glittery
 * Mysterious Artifact" na ekranie kuzni.
 */
@Composable
fun ParchmentPanel(
    modifier: Modifier = Modifier,
    title: String? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(
                Brush.verticalGradient(
                    listOf(ParchmentPalette.Paper, ParchmentPalette.PaperDark)
                )
            )
            .border(4.dp, ParchmentPalette.Frame, RoundedCornerShape(16.dp))
            .padding(4.dp)
    ) {
        // Druga, cienka ramka w srodku - to ona daje wrazenie "oprawionej tablicy" zamiast
        // zwyklego prostokata.
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .border(1.dp, ParchmentPalette.FrameLight.copy(alpha = 0.7f), RoundedCornerShape(12.dp))
                .padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            title?.let {
                Text(
                    text = it,
                    color = ParchmentPalette.Ink,
                    fontFamily = FontPixel(),
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            content()
        }
    }
}

/**
 * Segmentowany pasek zakladek z gory ekranu ("Artifact Forge | Disassemble | Regress").
 * Zakladki sa rownej szerokosci i oddzielone pionowa kreska, aktywna jest podswietlona.
 */
@Composable
fun ParchmentTabs(
    tabs: List<String>,
    selectedIndex: Int,
    modifier: Modifier = Modifier,
    onSelect: (Int) -> Unit
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(ParchmentPalette.TabBar)
            .border(3.dp, ParchmentPalette.Frame, RoundedCornerShape(12.dp))
            .padding(3.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        tabs.forEachIndexed { index, label ->
            val active = index == selectedIndex
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(9.dp))
                    .background(if (active) ParchmentPalette.TabActive else Color.Transparent)
                    .clickable { onSelect(index) }
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = label,
                    color = if (active) ParchmentPalette.Ink else ParchmentPalette.Paper.copy(alpha = 0.75f),
                    fontFamily = FontPixel(),
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    maxLines = 1
                )
            }
            if (index != tabs.lastIndex) {
                // Pionowa kreska miedzy zakladkami - to ona robi z paska "segmenty",
                // a nie trzy osobne przyciski.
                Box(
                    modifier = Modifier
                        .size(width = 2.dp, height = 22.dp)
                        .background(ParchmentPalette.Frame.copy(alpha = 0.6f))
                )
            }
        }
    }
}

/** Duzy zolty przycisk akcji - odpowiednik "Forge"/"Auto-equip" ze screenow. */
@Composable
fun ParchmentButton(
    text: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    color: Color = ParchmentPalette.Button,
    onClick: () -> Unit
) {
    val face = if (enabled) color else ParchmentPalette.ButtonDisabled
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(face)
            .border(
                width = 3.dp,
                color = if (enabled) ParchmentPalette.ButtonEdge else ParchmentPalette.Frame.copy(alpha = 0.4f),
                shape = RoundedCornerShape(12.dp)
            )
            .clickable(enabled = enabled, onClick = onClick)
            .padding(vertical = 13.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = if (enabled) ParchmentPalette.Ink else ParchmentPalette.InkMuted,
            // FontFirst, nie FontPixel: font pikselowy rysuje 'Z' i 'J' odbite lustrzanie,
            // wiec "WYKUJ" i "JUZ MASZ" wychodzily z niego nieczytelne.
            fontFamily = FontFirst(),
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp
        )
    }
}

/** Pigulka z zasobem - ciemne wglebienie z ikona i liczba, jak licznik krysztalow nad kuznia. */
@Composable
fun ResourcePill(
    icon: String,
    amount: String,
    modifier: Modifier = Modifier,
    tint: Color = ParchmentPalette.Paper
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(Color(0xFF3D2A18))
            .border(2.dp, ParchmentPalette.Frame, RoundedCornerShape(20.dp))
            .padding(horizontal = 12.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = icon, fontSize = 14.sp)
        Text(
            text = amount,
            color = tint,
            fontFamily = FontPixel(),
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp
        )
    }
}

/** Wglebiona siatka na papierze - tlo pod kafelki przedmiotow. */
@Composable
fun ParchmentInset(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(ParchmentPalette.Inset)
            .border(2.dp, ParchmentPalette.FrameLight, RoundedCornerShape(12.dp))
            .padding(8.dp)
    ) {
        content()
    }
}

/** Kropki stronicowania pod siatka - (1)(2)(3) ze screena kuzni. */
@Composable
fun PageDots(
    count: Int,
    selected: Int,
    modifier: Modifier = Modifier,
    /**
     * Czy cala strona jest jeszcze zamknieta. Kropka dostaje wtedy klodke i gasnie - ale ZOSTAJE
     * klikalna, bo pokazanie graczowi, co czeka wyzej, jest calym sensem tego znacznika.
     * Domyslnie nic nie jest zamkniete, wiec pozostali uzytkownicy [PageDots] nie zauwaza roznicy.
     */
    locked: (Int) -> Boolean = { false },
    onSelect: (Int) -> Unit
) {
    if (count <= 1) return
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(count) { index ->
            val active = index == selected
            val shut = locked(index)
            Box(contentAlignment = Alignment.TopEnd) {
                Box(
                    modifier = Modifier
                        .size(if (active) 30.dp else 26.dp)
                        .clip(CircleShape)
                        .background(if (active) ParchmentPalette.TabActive else Color(0xFF8E3A5B))
                        .border(
                            width = 2.dp,
                            color = if (active) ParchmentPalette.ButtonEdge else ParchmentPalette.Frame,
                            shape = CircleShape
                        )
                        .clickable { onSelect(index) }
                        .alpha(if (shut) 0.5f else 1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "${index + 1}",
                        color = if (active) ParchmentPalette.Ink else ParchmentPalette.Paper,
                        fontFamily = FontPixel(),
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }
                // Klodka POZA przygaszonym kolem, nie w nim: gdyby sama tez byla wyblakla,
                // znikalaby dokladnie tam, gdzie ma cos komunikowac.
                if (shut) {
                    Text(text = "🔒", fontSize = 10.sp)
                }
            }
        }
    }
}

/**
 * Panel z ozdobna ramka 9-slice ([Resources.Image.forgeFrame]) zamiast rysowanej ramki
 * [ParchmentPanel]. Rogi grafiki ida w stalym rozmiarze, boki sa rozciagane - dzieki temu ta
 * sama grafika pasuje do panelu kazdej szerokosci i wysokosci bez deformacji ornamentow.
 *
 * API zgodne z [ParchmentPanel] ([title] + tresc w [ColumnScope]) - podmiana to jedna linia.
 */
@Composable
fun FramedPanel(
    modifier: Modifier = Modifier,
    title: String? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    FantasyFrame(modifier = modifier) {
        // Papier wchodzi POD belki ramki (wciecie [FRAME_INLAY] jest mniejsze niz grubosc belki),
        // zeby ramka lezala na krawedzi papieru, a nie lewitowala obok niego.
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(
                    Brush.verticalGradient(
                        listOf(ParchmentPalette.Paper, ParchmentPalette.PaperDark)
                    )
                )
                .padding(horizontal = 10.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            title?.let {
                Text(
                    text = it,
                    color = ParchmentPalette.Ink,
                    fontFamily = FontPixel(),
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            content()
        }
    }
}

/**
 * Sama ozdobna ramka 9-slice ([Resources.Image.forgeFrame]) narysowana NA obrysie dowolnej
 * tresci - bez papieru w srodku. Do paneli, ktore maja wlasne tlo/uklad (mapa krainy, endless).
 *
 * [inset] to wciecie tresci. UWAGA: ramka rysuje sie NA WIERZCHU, wiec inset mniejszy niz grubosc
 * belki (~[FRAME_CORNER] * 33/96, czyli ok. 15 dp) chowa tresc pod belka - tak wlasnie ucinalo
 * pierwsza litere tytulu krainy przy inset = 5 dp. Zmniejszaj go tylko tam, gdzie pod belka ma
 * wejsc TLO (papier), a nie tekst czy przycisk.
 */
@Composable
fun FantasyFrame(
    modifier: Modifier = Modifier,
    inset: Dp = FRAME_INLAY,
    content: @Composable BoxScope.() -> Unit
) {
    val frame = imageResource(Resources.Image.forgeFrame)
    Box(modifier = modifier) {
        Box(modifier = Modifier.padding(inset), content = content)
        Canvas(modifier = Modifier.matchParentSize()) {
            drawNineSlice(frame, FRAME_SRC_INSET, FRAME_CORNER.toPx())
        }
    }
}

/**
 * Ile pikseli zrodlowej grafiki (512x512) to "rog" - nierozciagana czesc z ornamentem.
 * Zmierzone: ozdobnik rogu konczy sie ~94 px, wiec 96 lapie go w calosci bez zbednej pustej ramki.
 */
private const val FRAME_SRC_INSET = 96

/** Rysowany rozmiar rogu na ekranie - wieksza wartosc = grubsze belki i wieksze ornamenty. */
private val FRAME_CORNER = 43.dp

/**
 * O ile papier jest wciety wzgledem krawedzi ramki. Mniejsze niz rysowana grubosc belki
 * (~[FRAME_CORNER] * 33/96 ≈ 16 dp), wiec papier wchodzi pod belke i nie ma szczeliny.
 */
private val FRAME_INLAY = 13.dp

/**
 * Rysuje [image] jako ramke 9-slice na calej powierzchni rysowania. Srodkowy fragment
 * (przezroczysty na grafice ramki) jest pomijany - pod nim widac tlo panelu.
 */
private fun DrawScope.drawNineSlice(image: ImageBitmap, srcInset: Int, corner: Float) {
    val iw = image.width
    val ih = image.height
    val w = size.width
    val h = size.height
    val c = corner.coerceAtMost(minOf(w, h) / 2f)

    fun piece(sx: Int, sy: Int, sw: Int, sh: Int, dx: Float, dy: Float, dw: Float, dh: Float) {
        if (dw <= 0f || dh <= 0f || sw <= 0 || sh <= 0) return
        drawImage(
            image = image,
            srcOffset = IntOffset(sx, sy),
            srcSize = IntSize(sw, sh),
            dstOffset = IntOffset(dx.roundToInt(), dy.roundToInt()),
            dstSize = IntSize(dw.roundToInt().coerceAtLeast(1), dh.roundToInt().coerceAtLeast(1)),
            filterQuality = FilterQuality.High
        )
    }

    val midSW = iw - 2 * srcInset
    val midSH = ih - 2 * srcInset
    val midDW = w - 2 * c
    val midDH = h - 2 * c

    // Rogi - staly rozmiar.
    piece(0, 0, srcInset, srcInset, 0f, 0f, c, c)
    piece(iw - srcInset, 0, srcInset, srcInset, w - c, 0f, c, c)
    piece(0, ih - srcInset, srcInset, srcInset, 0f, h - c, c, c)
    piece(iw - srcInset, ih - srcInset, srcInset, srcInset, w - c, h - c, c, c)

    // Boki - rozciagane wzdluz jednej osi.
    piece(srcInset, 0, midSW, srcInset, c, 0f, midDW, c)
    piece(srcInset, ih - srcInset, midSW, srcInset, c, h - c, midDW, c)
    piece(0, srcInset, srcInset, midSH, 0f, c, c, midDH)
    piece(iw - srcInset, srcInset, srcInset, midSH, w - c, c, c, midDH)
}
