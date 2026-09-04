package com.lernhero.home.navigation

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lernhero.shared.i18n.strings
import com.lernhero.shared.theme.FontFirst
import org.jetbrains.compose.resources.painterResource

/* ---- paleta ----------------------------------------------------------------------------------- */
private val BarBg = Color(0xFF1E1811)
private val BarEdge = Color(0xFF6B5A38)
private val TileBg = Color(0xFF120D09)
private val TileBgOn = Color(0xFF35281A)
private val TileEdge = Color(0xFF3D3122)
private val TileEdgeOn = Color(0xFFE0BE63)
private val Gold = Color(0xFFFFD34D)
private val Muted = Color(0xFF8B7B5E)

/* ---- wymiary --------------------------------------------------------------------------------- */
/** Boczny kafel. Duzy i czytelny - ikona ma niesc informacje sama z siebie. */
private val SIDE_TILE = 52.dp
private val SIDE_ICON = 38.dp

/** Centralny kafel "Walcz". Wyraznie wiekszy od bocznych - roznica ma byc widoczna od razu. */
private val CENTER_TILE = 72.dp
private val CENTER_ICON = 46.dp

/** O ile centralny kafel wystaje ponad gorna krawedz paska. */
private val CENTER_LIFT = 24.dp

/**
 * Dolny pasek nawigacji w stylu mobilnego RPG: niski, ciemny pasek z malymi kaflami po bokach
 * i JEDNYM duzym, zlotym kaflem "Walcz" w srodku, ktory wystaje ponad pasek.
 *
 * Cala hierarchia jest w rozmiarze: boczne 40dp, srodek 62dp i uniesiony - gracz ma widziec, gdzie
 * sie klika, bez czytania podpisow.
 *
 * Ikony to na razie placeholdery ([com.lernhero.shared.resources.Resources.NavIcon]) - podmiana
 * plikow `nav_*.png` wystarczy, kod zostaje bez zmian.
 */
@Composable
fun NavBottomBar(
    modifier: Modifier = Modifier,
    selected: BottomBarDestination,
    onSelect: (BottomBarDestination) -> Unit,
) {
    // Box bez przyciecia - zloty kafel "Walcz" musi moc wyjsc ponad tlo paska.
    Box(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
                .background(BarBg)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(BarEdge.copy(alpha = 0.5f))
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(start = 10.dp, end = 10.dp, top = 6.dp, bottom = 3.dp),
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                BottomBarDestination.entries.forEach { destination ->
                    if (destination.isPrimary) {
                        // Miejsce pod uniesiony kafel - sam kafel lezy w nakladce nizej, zeby
                        // nie byl przycinany gorna krawedzia paska.
                        Spacer(modifier = Modifier.weight(1.25f))
                    } else {
                        SideItem(
                            destination = destination,
                            selected = selected == destination,
                            onClick = { onSelect(destination) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        BottomBarDestination.entries.firstOrNull { it.isPrimary }?.let { fight ->
            CenterItem(
                destination = fight,
                selected = selected == fight,
                onClick = { onSelect(fight) },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .navigationBarsPadding()
                    .padding(bottom = 2.dp)
            )
        }
    }
}

@Composable
private fun SideItem(
    destination: BottomBarDestination,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()

    // Wybrany kafel unosi sie o wlos - subtelny sygnal, ktory nie rozpycha paska.
    val lift by animateDpAsState(if (selected) (-3).dp else 0.dp, label = "sideLift")
    // Dotkniecie = ikona lekko "odbija", wybor trzyma ja odrobine powiekszona.
    val scale by animateFloatAsState(
        when {
            pressed -> 0.86f
            selected -> 1.12f
            else -> 1f
        },
        label = "sideScale"
    )

    Column(
        modifier = modifier
            .clickable(
                interactionSource = interaction,
                indication = null,
                onClick = onClick
            )
            .padding(vertical = 2.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        Box(
            modifier = Modifier
                .offset(y = lift)
                .size(SIDE_TILE)
                .clip(RoundedCornerShape(14.dp))
                .background(if (selected) TileBgOn else TileBg)
                .border(
                    width = if (selected) 1.5.dp else 1.dp,
                    color = if (selected) TileEdgeOn else TileEdge,
                    shape = RoundedCornerShape(13.dp)
                )
                .padding(5.dp),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(destination.icon),
                contentDescription = destination.title(strings),
                modifier = Modifier.size(SIDE_ICON).scale(scale)
            )
        }
        Text(
            text = destination.title(strings),
            fontFamily = FontFirst(),
            fontSize = 9.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
            color = if (selected) Gold else Muted,
            maxLines = 1
        )
    }
}

/**
 * Centralny "Walcz": duzy zloty kafel uniesiony ponad pasek, z podpisem WEWNATRZ, u dolu kafla -
 * dokladnie jak w mobilnych RPG, gdzie glowna akcja jest osobnym, wystajacym guzikiem, a nie
 * kolejna pozycja w rzedzie.
 */
@Composable
private fun CenterItem(
    destination: BottomBarDestination,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()

    val glow by animateFloatAsState(if (selected) 1f else 0.75f, label = "centerGlow")
    val scale by animateFloatAsState(if (pressed) 0.9f else 1f, label = "centerScale")

    Box(
        modifier = modifier
            .offset(y = -CENTER_LIFT)
            .scale(scale)
            .size(CENTER_TILE)
            .clip(RoundedCornerShape(18.dp))
            .background(
                Brush.verticalGradient(
                    if (selected) listOf(Color(0xFFFFE9A3), Color(0xFFF0A92B))
                    else listOf(Color(0xFFE8C765), Color(0xFFBE8A22))
                )
            )
            .border(
                width = 2.5.dp,
                color = Color(0xFFFFF3CE).copy(alpha = glow),
                shape = RoundedCornerShape(18.dp)
            )
            .clickable(
                interactionSource = interaction,
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(destination.icon),
            contentDescription = destination.title(strings),
            modifier = Modifier
                .size(CENTER_ICON)
                // Lekko w gore, zeby podpis u dolu kafla mial swoje miejsce.
                .offset(y = (-5).dp)
        )
        Text(
            text = destination.title(strings),
            fontFamily = FontFirst(),
            fontWeight = FontWeight.Bold,
            fontSize = 9.sp,
            color = Color.White,
            style = TextStyle(shadow = Shadow(color = Color(0xFF4A3000), blurRadius = 5f)),
            maxLines = 1,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 5.dp)
        )
    }
}
