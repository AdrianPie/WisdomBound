package com.lernhero.home.city

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lernhero.domain.town.TownResources
import com.lernhero.shared.theme.AppPalette
import com.lernhero.shared.theme.FontFirst
import com.lernhero.shared.theme.FontPixel
import com.lernhero.home.ui.OutlinedText
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.CircleShape

internal val Gold = AppPalette.Gold
internal val GoldDim = AppPalette.GoldDim
internal val Ink = AppPalette.Ink
internal val TextBody = AppPalette.TextBody
internal val Muted = AppPalette.TextMuted

/** Szkielet kazdego ekranu budynku: pasek surowcow u gory, powrot, tresc i warstwa komunikatow. */
@Composable
internal fun TownScreenScaffold(
    title: String,
    resources: TownResources,
    money: Int,
    toasts: List<TownToast>,
    onBack: () -> Unit,
    /**
     * Ekrany, ktore musza zmiescic cala tresc bez scrolla (karta kompana), dostaja naglowek
     * zbity do JEDNEGO wiersza. Trzy wiersze chromu zabieraly tam okolo jednej trzeciej
     * wysokosci - tyle, ile potrzebuje caly panel statystyk.
     */
    compact: Boolean = false,
    /**
     * Pelny pasek surowcow pod tytulem takze w trybie [compact] - dla ekranow, ktore placa wiecej
     * niz jednym zasobem (kuznia). Domyslnie false, zeby ekrany dobrane pod brak scrolla nie
     * traciły wysokosci na cos, czego nie wydaja.
     */
    showAllResources: Boolean = false,
    content: @Composable () -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        // Oba marginesy systemowe siedza TUTAJ, a nie w ekranach.
        //
        // Wczesniej scaffold dawal tylko gorny (statusBarsPadding), przez co tresc kazdego budynku
        // Miasta wchodzila pod dolny pasek gestow - i kazdy ekran laral to u siebie po swojemu
        // (albo wcale). Skoro szkielet rezerwuje miejsce na naglowek, ma tez rezerwowac miejsce na
        // dolna krawedz.
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
        ) {
            TownHeader(
                title = title,
                resources = resources,
                money = money,
                compact = compact,
                showAllResources = showAllResources,
                onBack = onBack
            )
            Box(modifier = Modifier.fillMaxSize()) { content() }
        }

        // Komunikaty ("+50 drewna", "+25% PRODUCTION") - zeby zawsze bylo widac, co wlasnie dostales.
        Column(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .statusBarsPadding()
                .padding(top = 86.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            toasts.forEach { toast ->
                key(toast.id) { ToastPill(toast) }
            }
        }
    }
}

@Composable
private fun ToastPill(toast: TownToast) {
    AnimatedVisibility(
        visible = true,
        enter = fadeIn(tween(180)) + slideInVertically(tween(220)) { -it },
        exit = fadeOut(tween(180))
    ) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(20.dp))
                .background(if (toast.emphasis) AppPalette.Success.copy(alpha = 0.92f) else Ink.copy(alpha = 0.92f))
                .border(
                    width = 1.5.dp,
                    color = if (toast.emphasis) AppPalette.Success else GoldDim,
                    shape = RoundedCornerShape(20.dp)
                )
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Text(
                text = toast.text,
                color = if (toast.emphasis) Color(0xFF08130C) else Gold,
                fontFamily = FontPixel(),
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp
            )
        }
    }
}

@Composable
private fun TownHeader(
    title: String,
    resources: TownResources,
    money: Int,
    compact: Boolean,
    showAllResources: Boolean,
    onBack: () -> Unit
) {
    if (compact) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 10.dp, vertical = 5.dp),
            verticalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(7.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                BackButton(onBack)
                OutlinedText(
                    text = title,
                    size = 19.sp,
                    color = Gold,
                    maxLines = 1,
                    modifier = Modifier.weight(1f).wrapContentWidth(Alignment.Start)
                )
                // Przy pelnym pasku ponizej te dwie pigulki bylyby tym samym dwa razy.
                if (!showAllResources) {
                    ResourcePill("💰", money)
                    ResourcePill("✨", resources.essence)
                }
            }

            // Kuznia placi drewnem, kamieniem, ruda, esencja I zlotem naraz - pokazywanie tylko
            // dwoch z nich zmuszalo do zgadywania, czego brakuje, albo do wychodzenia z ekranu.
            if (showAllResources) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    ResourcePill("🪨", resources.stone, Modifier.weight(1f), stretch = true)
                    ResourcePill("🔩", resources.iron, Modifier.weight(1f), stretch = true)
                    ResourcePill("✨", resources.essence, Modifier.weight(1f), stretch = true)
                    ResourcePill("💰", money, Modifier.weight(1f), stretch = true)
                }
            }
        }
        return
    }

    Column(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            BackButton(onBack)
            OutlinedText(
                text = title,
                size = 24.sp,
                color = Gold,
                modifier = Modifier.weight(1f)
            )
            // Wyrownanie do srodka - ta sama szerokosc co przycisk powrotu.
            Box(modifier = Modifier.size(BACK_SIZE))
        }

        // Ruda i esencja stoja obok drewna i kamienia, bo od tej chwili to one sa glownym
        // materialem miasta - drewno/kamien produkuja budynki, tamte dwa przychodza z lochow.
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            ResourcePill("🪨", resources.stone, Modifier.weight(1f), stretch = true)
            ResourcePill("🔩", resources.iron, Modifier.weight(1f), stretch = true)
            ResourcePill("✨", resources.essence, Modifier.weight(1f), stretch = true)
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            ResourcePill("💰", money, Modifier.weight(1f), stretch = true)
            ResourcePill("🎟️", resources.summonTickets, Modifier.weight(1f), stretch = true)
        }
    }
}

private val BACK_SIZE = 42.dp
private val Outline = Color(0xFF120B05)

/** Okragly przycisk powrotu z grubym obrysem - jak w naglowkach gier mobilnych. */
@Composable
private fun BackButton(onBack: () -> Unit) {
    Box(
        modifier = Modifier
            .size(BACK_SIZE)
            .clip(CircleShape)
            .background(Brush.verticalGradient(listOf(Color(0xFF5A3F24), Color(0xFF2C1D10))))
            .border(2.5.dp, Outline, CircleShape)
            .clickable(onClick = onBack),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .matchParentSize()
                .padding(2.5.dp)
                .border(1.5.dp, Gold.copy(alpha = 0.55f), CircleShape)
        )
        OutlinedText(text = "‹", size = 26.sp, color = Gold, modifier = Modifier.offset(y = (-2).dp))
    }
}

/** Licznik surowca: ikona nachodzi na ciemna pastylke z liczba - ten sam jezyk co gorny pasek gry. */
@Composable
internal fun ResourcePill(icon: String, value: Int, modifier: Modifier = Modifier, stretch: Boolean = false) {
    Box(modifier = modifier, contentAlignment = Alignment.CenterStart) {
        Box(
            modifier = Modifier
                .then(if (stretch) Modifier.fillMaxWidth() else Modifier)
                .padding(start = 10.dp)
                .clip(RoundedCornerShape(50))
                .background(Color(0xE60C0806))
                .border(1.5.dp, Outline, RoundedCornerShape(50))
                .padding(start = 18.dp, end = 10.dp, top = 4.dp, bottom = 4.dp)
        ) {
            Text(
                text = value.compact(),
                color = Color.White,
                fontFamily = FontFirst(),
                fontWeight = FontWeight.Black,
                fontSize = 13.sp,
                maxLines = 1
            )
        }
        Text(text = icon, fontSize = 18.sp)
    }
}

/**
 * Panel w stylu reszty gry: cieply ciemny gradient, gruby ciemny obrys i cienka zlota linia
 * wewnatrz - daje "wypuklosc" drewnianej plyty zamiast plaskiej karty.
 */
@Composable
internal fun TownPanel(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(Brush.verticalGradient(listOf(Color(0xF0352519), Color(0xF01C130C))))
            .border(2.5.dp, Outline, RoundedCornerShape(16.dp))
    ) {
        Box(
            modifier = Modifier
                .matchParentSize()
                .padding(2.5.dp)
                .border(1.dp, Gold.copy(alpha = 0.3f), RoundedCornerShape(14.dp))
        )
        content()
    }
}

/** Duzy "tlusty" przycisk akcji - zielony (glowny), niebieski (poboczny) albo szary (nieaktywny). */
@Composable
internal fun TownActionButton(
    text: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    primary: Boolean = false,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    val colors = when {
        !enabled -> listOf(Color(0xFF7D7468), Color(0xFF4F483F))
        primary -> listOf(Color(0xFF8BE05A), Color(0xFF3F9E2B))
        else -> listOf(Color(0xFF5BB8FF), Color(0xFF2C6FC4))
    }
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(Brush.verticalGradient(colors))
            .border(2.5.dp, Outline, RoundedCornerShape(14.dp))
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        // Jasny polysk na gornej polowie.
        Box(
            modifier = Modifier
                .matchParentSize()
                .padding(start = 5.dp, end = 5.dp, top = 4.dp)
                .wrapContentHeight(Alignment.Top)
                .height(10.dp)
                .clip(RoundedCornerShape(50))
                .background(Color.White.copy(alpha = if (enabled) 0.28f else 0.1f))
        )
        Column(
            modifier = Modifier.padding(vertical = 10.dp, horizontal = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(1.dp)
        ) {
            OutlinedText(text = text, size = 17.sp, color = if (enabled) Color.White else Color(0xFFD8D0C4))
            subtitle?.let {
                Text(
                    text = it,
                    color = if (enabled) Color.White else Color(0xFFD8D0C4),
                    fontFamily = FontFirst(),
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

/** Pasek postepu: gleboka rynna, wypelnienie z polyskiem, plynna animacja. */
@Composable
internal fun TownProgressBar(
    progress: Float,
    modifier: Modifier = Modifier,
    fill: Color = AppPalette.Success,
    height: androidx.compose.ui.unit.Dp = 18.dp,
    label: String? = null
) {
    val animated by animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        animationSpec = tween(220),
        label = "townProgress"
    )
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .clip(RoundedCornerShape(50))
            .background(Color(0xFF0B0704))
            .border(2.dp, Outline, RoundedCornerShape(50)),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .fillMaxWidth(animated)
                .fillMaxSize()
                .padding(2.dp)
                .clip(RoundedCornerShape(50))
                .background(Brush.verticalGradient(listOf(fill.lighten(), fill, fill.copy(alpha = 0.85f))))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.4f)
                    .padding(horizontal = 4.dp, vertical = 1.dp)
                    .clip(RoundedCornerShape(50))
                    .background(Color.White.copy(alpha = 0.3f))
            )
        }
        label?.let { OutlinedText(text = it, size = 12.sp, color = Color.White, strokeWidth = 5f) }
    }
}

private fun Color.lighten(): Color = Color(
    red = red + (1f - red) * 0.35f,
    green = green + (1f - green) * 0.35f,
    blue = blue + (1f - blue) * 0.35f,
    alpha = alpha
)

/** Etykieta sekcji w panelu - zloty napis z obrysem. */
@Composable
internal fun TownSectionLabel(text: String, modifier: Modifier = Modifier) {
    OutlinedText(text = text, size = 16.sp, color = Gold, modifier = modifier, strokeWidth = 5f)
}

/** 1234 -> "1.2K", zeby duze liczby idle nie rozpychaly UI. */
internal fun Int.compact(): String = when {
    this >= 1_000_000 -> "${(this / 100_000) / 10f}M"
    this >= 1_000 -> "${(this / 100) / 10f}K"
    else -> toString()
}

/** mm:ss / hh:mm - do odliczania czasu bonusu. */
internal fun Long.asDuration(): String {
    val totalSeconds = this / 1000
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60
    return if (hours > 0) {
        "${hours}h ${minutes.toString().padStart(2, '0')}m"
    } else {
        "${minutes.toString().padStart(2, '0')}:${seconds.toString().padStart(2, '0')}"
    }
}
