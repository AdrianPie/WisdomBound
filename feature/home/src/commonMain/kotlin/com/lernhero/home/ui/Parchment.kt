package com.lernhero.home.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lernhero.shared.theme.FontFirst
import com.lernhero.shared.theme.FontPixel

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
                    .clickable { onSelect(index) },
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
        }
    }
}
