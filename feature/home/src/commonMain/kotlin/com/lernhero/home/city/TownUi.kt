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
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(9.dp))
                        .background(Ink.copy(alpha = 0.8f))
                        .border(1.5.dp, GoldDim, RoundedCornerShape(9.dp))
                        .clickable(onClick = onBack)
                        .padding(horizontal = 9.dp, vertical = 5.dp)
                ) {
                    Text(text = "←", color = Gold, fontFamily = FontPixel(), fontSize = 13.sp)
                }
                Text(
                    text = title,
                    color = Gold,
                    fontFamily = FontPixel(),
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    maxLines = 1,
                    modifier = Modifier.weight(1f)
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
                    ResourcePill("🪵", resources.wood, Modifier.weight(1f))
                    ResourcePill("🪨", resources.stone, Modifier.weight(1f))
                    ResourcePill("⚙", resources.iron, Modifier.weight(1f))
                    ResourcePill("✨", resources.essence, Modifier.weight(1f))
                    ResourcePill("💰", money, Modifier.weight(1f))
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
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(Ink.copy(alpha = 0.8f))
                    .border(1.5.dp, GoldDim, RoundedCornerShape(10.dp))
                    .clickable(onClick = onBack)
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Text(text = "←", color = Gold, fontFamily = FontPixel(), fontSize = 15.sp)
            }
            Text(
                text = title,
                color = Gold,
                fontFamily = FontPixel(),
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.weight(1f)
            )
            // Wyrownanie do srodka - ta sama szerokosc co przycisk powrotu.
            Box(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                Text(text = "←", color = Color.Transparent, fontFamily = FontPixel(), fontSize = 15.sp)
            }
        }

        // Ruda i esencja stoja obok drewna i kamienia, bo od tej chwili to one sa glownym
        // materialem miasta - drewno/kamien produkuja budynki, tamte dwa przychodza z lochow.
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            ResourcePill("🪵", resources.wood, Modifier.weight(1f))
            ResourcePill("🪨", resources.stone, Modifier.weight(1f))
            ResourcePill("⚙", resources.iron, Modifier.weight(1f))
            ResourcePill("✨", resources.essence, Modifier.weight(1f))
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            ResourcePill("💰", money, Modifier.weight(1f))
            ResourcePill("🎟️", resources.summonTickets, Modifier.weight(1f))
        }
    }
}

@Composable
internal fun ResourcePill(icon: String, value: Int, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(18.dp))
            .background(Ink.copy(alpha = 0.85f))
            .border(1.5.dp, GoldDim, RoundedCornerShape(18.dp))
            .padding(horizontal = 8.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = icon, fontSize = 13.sp)
        Text(
            text = value.compact(),
            color = Gold,
            fontFamily = FontPixel(),
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp,
            maxLines = 1
        )
    }
}

/** Panel w stylu reszty gry - ciemna karta w zlotej ramce. */
@Composable
internal fun TownPanel(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(AppPalette.Panel.copy(alpha = 0.88f))
            .border(2.dp, GoldDim, RoundedCornerShape(16.dp))
    ) {
        content()
    }
}

/** Duzy przycisk akcji - podstawowy (zloty) albo poboczny (ciemny). */
@Composable
internal fun TownActionButton(
    text: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    primary: Boolean = false,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    val background = when {
        !enabled -> Brush.verticalGradient(listOf(AppPalette.Panel, AppPalette.Panel))
        primary -> Brush.verticalGradient(listOf(AppPalette.ActionBright, AppPalette.Action))
        else -> Brush.verticalGradient(listOf(AppPalette.PanelRaised, AppPalette.Panel))
    }
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(background)
            .border(2.dp, if (enabled) Gold else GoldDim.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
            .clickable(enabled = enabled, onClick = onClick)
            .padding(vertical = 12.dp, horizontal = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Text(
            text = text,
            color = if (enabled) Color.White else Muted,
            fontFamily = FontPixel(),
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            textAlign = TextAlign.Center
        )
        subtitle?.let {
            Text(
                text = it,
                color = if (enabled) Color.White.copy(alpha = 0.85f) else Muted,
                fontFamily = FontFirst(),
                fontSize = 10.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}

/** Pasek postepu z wypelnieniem animowanym plynnie - serce "idle feelingu". */
@Composable
internal fun TownProgressBar(
    progress: Float,
    modifier: Modifier = Modifier,
    fill: Color = AppPalette.Success,
    height: androidx.compose.ui.unit.Dp = 18.dp
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
            .background(Ink)
            .border(1.5.dp, GoldDim, RoundedCornerShape(50))
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(animated)
                .fillMaxSize()
                .clip(RoundedCornerShape(50))
                .background(Brush.horizontalGradient(listOf(fill.copy(alpha = 0.75f), fill)))
        )
    }
}

/** Etykieta sekcji w panelu. */
@Composable
internal fun TownSectionLabel(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        color = Gold,
        fontFamily = FontPixel(),
        fontWeight = FontWeight.Bold,
        fontSize = 13.sp,
        modifier = modifier
    )
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
