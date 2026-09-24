package com.lernhero.home.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lernhero.shared.theme.FontFirst

/**
 * Wspolne czesci OKIEN gry - karty postaci, talentow, przedmiotu.
 *
 * Okna zastapily tu osobne ekrany, bo kazde wejscie w podekran cofalo gracza gdzie indziej niz
 * tam, skad wszedl (patrz [com.lernhero.home.equipment.EquipmentDialog]). Zeby jednak czytaly sie
 * jak jeden system, a nie jak trzy rozne wynalazki, przyciemnienie i krzyzyk sa JEDNE dla wszystkich.
 */

/** Przyciemnione tlo okna. Tapniecie obok zamyka - tak dziala kazde okno w tej grze. */
@Composable
fun DialogScrim(onDismiss: () -> Unit, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xCC07050B))
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() },
                onClick = onDismiss
            )
    )
}

/**
 * Krzyzyk pod oknem - na dole, na srodku, w zasiegu kciuka. Gora okna nalezy do tresci, nie do
 * przycisku zamykania.
 */
@Composable
fun DialogCloseButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(52.dp)
            .clip(CircleShape)
            .background(Color(0xFF1B2745))
            .border(3.dp, Color(0xFF6E86B8), CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "✕",
            color = Color.White,
            fontFamily = FontFirst(),
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp
        )
    }
}

/** Ile miejsca u dolu okna nalezy do krzyzyka - o tyle tresc okna jest krotsza. */
val DIALOG_CLOSE_LANE = 74.dp
