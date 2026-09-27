package com.lernhero.home.navigation

import com.lernhero.shared.audio.Sounds
import com.lernhero.shared.audio.Sfx
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
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
private val Muted = Color(0xFF8B7B5E)

/* ---- wymiary --------------------------------------------------------------------------------- */
/** Kafel niewybranej zakladki - sama ikona, bez pudelka. */
private val TILE = 50.dp
private val ICON = 38.dp

/** Kafel WYBRANEJ zakladki - pomaranczowy i wyraznie wiekszy. */
private val TILE_ON = 58.dp
private val ICON_ON = 42.dp

/**
 * O ile wybrany kafel jest PODNIESIONY ponad rzad.
 *
 * Uwaga przy strojeniu: ponad pasek kafel wystaje o TO PLUS roznice rozmiarow (rzad jest
 * wyrownany do dolu, wiec wiekszy kafel rosnie w gore). Przy [TILE_ON] 64 i [TILE] 50 samo
 * uniesienie odpowiada wiec tylko za jakies dwie piate tego, co widac - zmiana samej tej liczby
 * prawie nic nie daje i trzeba ruszac obie naraz.
 */
private val LIFT_ON = 6.dp

/**
 * Wysokosc samej plyty paska (bez wciecia systemowego).
 *
 * Plyta jest WYZSZA niz rzad ikon - i o to chodzi. Gdy konczyla sie dokladnie na ich gornej
 * krawedzi, wybrany kafel wychodzil poza nia caly i wygladal, jakby lewitowal nad paskiem, zamiast
 * z niego wystawac. Teraz plyta siega mu mniej wiecej do polowy: kafel wyraznie nad nia wystaje,
 * ale caly czas z niej wyrasta.
 */
private val BAR_HEIGHT = 86.dp

private const val SWITCH_MS = 220

/**
 * Dolny pasek nawigacji w stylu mobilnego RPG: niski, ciemny pasek, a na nim JEDEN duzy,
 * pomaranczowy kafel - ten, na ktorym gracz wlasnie jest.
 *
 * Wyroznienie WEDRUJE za wyborem. Wczesniej wielki zloty guzik byl przyspawany do "Walcz"
 * (BottomBarDestination.isPrimary), a reszta zakladek - lacznie z ta otwarta - siedziala w
 * ciemnych, wglebionych kwadratach. Znaczylo to, ze pasek pokazywal, co jest WAZNE, ale nie
 * pokazywal, GDZIE JESTEM: otwarta zakladka wygladala tak samo jak zamknieta, a zawsze
 * podswietlony srodek sugerowal, ze to on jest aktywny.
 *
 * Niewybrane zakladki nie maja juz zadnego pudelka - sama ikona i podpis. Ciemne ramki wokol nich
 * czytaly sie jak przyciski WCISNIETE, czyli dokladnie odwrotnie, niz mialy znaczyc.
 *
 * Kazda roznica miedzy stanami (rozmiar, uniesienie, kolor, podpis) jest ANIMOWANA tym samym
 * czasem [SWITCH_MS] - przelaczenie ma wygladac jak przesuniecie jednego wyroznienia, a nie jak
 * zgasniecie jednej rzeczy i zapalenie drugiej.
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
    // Box BEZ przyciecia - wybrany kafel musi moc wyjsc ponad plyte paska.
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
            Box(modifier = Modifier.fillMaxWidth().height(BAR_HEIGHT).navigationBarsPadding())
        }

        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 10.dp),
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            BottomBarDestination.entries.forEach { destination ->
                NavItem(
                    destination = destination,
                    selected = selected == destination,
                    onClick = { Sounds.play(Sfx.Tap); onSelect(destination) },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

/**
 * Jedna zakladka. Ten SAM sklad dla obu stanow - zmieniaja sie tylko liczby i kolory, wiec
 * przejscie da sie zanimowac. Dwa osobne komponenty (wybrany/niewybrany) dawalyby skok.
 */
@Composable
private fun NavItem(
    destination: BottomBarDestination,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val spec = tween<Float>(SWITCH_MS, easing = FastOutSlowInEasing)

    val tile by animateDpAsState(
        if (selected) TILE_ON else TILE,
        tween(SWITCH_MS, easing = FastOutSlowInEasing),
        label = "navTile"
    )
    val icon by animateDpAsState(
        if (selected) ICON_ON else ICON,
        tween(SWITCH_MS, easing = FastOutSlowInEasing),
        label = "navIcon"
    )
    val lift by animateDpAsState(
        if (selected) -LIFT_ON else 0.dp,
        tween(SWITCH_MS, easing = FastOutSlowInEasing),
        label = "navLift"
    )
    // Tlo i ramka gasna do PRZEZROCZYSTOSCI, a nie do ciemnego kwadratu - niewybrana zakladka
    // ma byc sama ikona.
    val plate by animateFloatAsState(if (selected) 1f else 0f, spec, label = "navPlate")
    val label by animateColorAsState(
        if (selected) Color.White else Muted,
        tween(SWITCH_MS),
        label = "navLabel"
    )
    val press by animateFloatAsState(if (pressed) 0.9f else 1f, tween(90), label = "navPress")

    Column(
        modifier = modifier
            .clickable(interactionSource = interaction, indication = null, onClick = onClick)
            .padding(bottom = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Box(
            modifier = Modifier
                .offset(y = lift)
                .scale(press)
                .size(tile)
                .clip(RoundedCornerShape(18.dp))
                .background(
                    Brush.verticalGradient(
                        listOf(
                            OrangeTop.copy(alpha = plate),
                            OrangeBottom.copy(alpha = plate)
                        )
                    )
                )
                .border(
                    width = 2.5.dp,
                    color = OrangeEdge.copy(alpha = plate),
                    shape = RoundedCornerShape(18.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(destination.icon),
                contentDescription = destination.title(strings),
                modifier = Modifier.size(icon)
            )
        }
        Text(
            text = destination.title(strings),
            fontFamily = FontFirst(),
            fontSize = 9.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
            // Podpis wybranej zakladki wjezdza pod uniesiony kafel, a nie za nim - stad ten sam
            // offset co kafel, tylko mniejszy.
            color = label,
            style = TextStyle(shadow = Shadow(color = Color(0xFF120D09), blurRadius = 4f)),
            maxLines = 1,
            modifier = Modifier.offset(y = lift / 2)
        )
    }
}

private val OrangeTop = Color(0xFFFFC46A)
private val OrangeBottom = Color(0xFFEF8B23)
private val OrangeEdge = Color(0xFFFFE6B4)

