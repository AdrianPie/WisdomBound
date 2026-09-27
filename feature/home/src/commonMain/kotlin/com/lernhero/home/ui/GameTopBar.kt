package com.lernhero.home.ui

import com.lernhero.shared.theme.GlyphText
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.GenericShape
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lernhero.domain.energy.EnergyRules
import com.lernhero.domain.energy.EnergyState
import com.lernhero.domain.player.HeroRank
import com.lernhero.game.character.HeroLook
import com.lernhero.game.character.KnightEquipment
import com.lernhero.game.character.ModularKnight
import com.lernhero.shared.theme.FontFirst
import kotlinx.coroutines.delay
import kotlin.time.Clock

/**
 * Gorny pasek gry - widoczny na KAZDYM ekranie poza walka.
 *
 * Do tej pory skrot postaci i zasoby siedzialy wylacznie na ekranie Dom: wchodzac do Kuzni, Sklepu
 * czy Postepu gracz tracil z oczu zloto i energie, wiec zeby sprawdzic, czy stac go na cokolwiek,
 * musial wyjsc z ekranu, na ktorym wlasnie podejmowal decyzje.
 *
 * Uklad ma trzy warstwy, dokladnie jak paski w grach, z ktorych pochodzi:
 *
 * 1. **Plyta paska** - ciemna, przez cala szerokosc, z imieniem, ranga i zasobami.
 * 2. **Portret WYSTAJACY z plyty** w dol i w lewo. To nie jest ozdoba: portret jest najwiekszym
 *    i najlatwiejszym do trafienia kciukiem celem na ekranie, a wystajac, nie zabiera paskowi
 *    wysokosci. Otwiera karte postaci.
 * 3. **Plakietka mocy** pod portretem - jedna liczba mowiaca "ile jestem wart", w tej samej skali
 *    co moc przedmiotow ([com.lernhero.domain.item.GearPower]).
 *
 * Ikony zasobow sa PLACEHOLDERAMI (emoji) - podmiana na grafike to jedna linia w [ResourcePill].
 */
@Composable
fun GameTopBar(
    name: String,
    level: Int,
    power: Int,
    money: Int,
    gems: Int,
    energy: EnergyState,
    knight: KnightEquipment,
    look: HeroLook,
    onOpenCharacter: () -> Unit,
    onOpenSettings: () -> Unit,
    /** "+" przy diamentach - sklep na zakladce z pakietami. */
    onAddGems: () -> Unit,
    /** "+" przy zlocie - sklep na zakladce z zapasami (zloto za diamenty). */
    onAddGold: () -> Unit,
    /** "+" przy energii - okno doladowania. */
    onAddEnergy: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Energia rosnie SAMA z uplywem czasu, wiec pasek musi tykac wlasnym zegarem - stan w
    // PlayerViewModel zmienia sie dopiero przy wydaniu albo ponownym wczytaniu gracza.
    var now by remember { mutableStateOf(Clock.System.now().toEpochMilliseconds()) }
    LaunchedEffect(Unit) {
        while (true) {
            now = Clock.System.now().toEpochMilliseconds()
            delay(1000)
        }
    }
    val current = EnergyRules.regenerated(energy, now)

    Box(modifier = modifier.fillMaxWidth()) {
        // Warstwa 1: plyta - farba siega od gory ekranu (pod paskiem systemowym) do dolu plyty.
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Brush.verticalGradient(listOf(PanelTop, PanelBottom)))
                .statusBarsPadding()
                .height(BAR_HEIGHT)
        ) {
            Box(modifier = Modifier.align(Alignment.BottomStart).fillMaxWidth().height(2.dp).background(PanelEdge))
        }

        Box(modifier = Modifier.statusBarsPadding().fillMaxWidth().height(TOP_BAR_HEIGHT)) {
            // Warstwa 2: sciety "jezyk" zwisajacy pod plyta - niesie moc bohatera.
            Row(
                modifier = Modifier
                    .offset(x = 30.dp, y = BAR_HEIGHT - 2.dp)
                    .width(TAB_WIDTH)
                    .height(TAB_HEIGHT)
                    .clip(SlantTabShape)
                    .background(Brush.verticalGradient(listOf(PanelBottom, TabFill)))
                    .border(2.dp, PanelEdge, SlantTabShape)
                    .padding(start = AVATAR_LANE - 26.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                GlyphText(text = "🛡", fontSize = 12.sp)
                GlyphText(text = "$power", color = Color.White, fontFamily = FontFirst(), fontWeight = FontWeight.Black, fontSize = 13.sp)
            }

            // Warstwa 3: zawartosc plyty.
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(BAR_HEIGHT)
                    .padding(start = AVATAR_LANE + 4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f).clickable(onClick = onOpenCharacter)) {
                    GlyphText(
                        text = name.ifBlank { "Sir Lernhero" },
                        color = Color.White,
                        fontFamily = FontFirst(),
                        fontWeight = FontWeight.Black,
                        fontSize = 15.sp,
                        lineHeight = 17.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    GlyphText(
                        text = HeroRank.titleFor(level),
                        color = RankViolet,
                        fontFamily = FontFirst(),
                        fontWeight = FontWeight.Black,
                        fontSize = 13.sp,
                        lineHeight = 15.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                ResourcePill(icon = "💎", value = shortCount(gems), onPlus = onAddGems)
                ResourcePill(icon = "🪙", value = shortCount(money), onPlus = onAddGold)
                // Energia niesie DWIE liczby: ile jest i kiedy przybedzie nastepna.
                ResourcePill(
                    icon = "⚡",
                    value = "${current.amount}",
                    caption = if (current.amount >= EnergyRules.MAX) null else shortTime(EnergyRules.millisToNext(current, now)),
                    onPlus = onAddEnergy
                )

                // Osobna "wneka" na ustawienia - pionowa kreska i jasniejsze tlo, jak w referencji.
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .width(44.dp)
                        .background(Brush.horizontalGradient(listOf(PanelEdge.copy(alpha = 0.35f), Color.Transparent)))
                        .clickable(onClick = onOpenSettings),
                    contentAlignment = Alignment.Center
                ) {
                    Box(modifier = Modifier.align(Alignment.CenterStart).width(2.dp).fillMaxHeight().background(PanelEdge))
                    GlyphText(text = "⚙", fontSize = 20.sp, color = Color(0xFFBFD3F0))
                }
            }

            // Warstwa 4: portret - WYSTAJE pod plyte i nachodzi na jezyk z moca.
            Box(
                modifier = Modifier
                    .offset(x = 4.dp, y = 6.dp)
                    .clickable(onClick = onOpenCharacter)
            ) {
                HeroPortrait(knight = knight, look = look, level = level)
            }
        }
    }
}

/** Jezyk pod plyta: prostokat ze scietym prawym brzegiem. */
private val SlantTabShape = GenericShape { size, _ ->
    moveTo(0f, 0f)
    lineTo(size.width, 0f)
    lineTo(size.width - size.height * 1.3f, size.height)
    lineTo(0f, size.height)
    close()
}

/** Kwadratowy portret w jasnej ramce, z poziomem na rogu. */
@Composable
private fun HeroPortrait(knight: KnightEquipment, look: HeroLook, level: Int) {
    Box(contentAlignment = Alignment.BottomEnd) {
        Box(
            modifier = Modifier
                .size(AVATAR_SIZE)
                .clip(RoundedCornerShape(10.dp))
                .background(Brush.verticalGradient(listOf(Color(0xFF4A8BD6), Color(0xFF2A5C99))))
                .border(3.dp, PortraitEdge, RoundedCornerShape(10.dp)),
            contentAlignment = Alignment.Center
        ) {
            ModularKnight(
                equipment = knight,
                look = look,
                sizeDp = AVATAR_SIZE * 1.45f,
                interactive = false,
                modifier = Modifier
                    .requiredSize(AVATAR_SIZE * 1.45f)
                    .offset(y = AVATAR_SIZE * 0.26f)
            )
        }
        Box(
            modifier = Modifier
                .offset(x = 5.dp, y = 5.dp)
                .size(20.dp)
                .clip(CircleShape)
                .background(LevelBlue)
                .border(1.5.dp, PortraitEdge, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            GlyphText(text = "$level", color = Color.White, fontFamily = FontFirst(), fontWeight = FontWeight.Bold, fontSize = 9.sp)
        }
    }
}

/**
 * Licznik zasobu: duza ikona NACHODZACA na lewa krawedz ciemnej pastylki z liczba, a na ikonie
 * zielony plusik (gdy jest [onPlus]) - skrot do sklepu. Ikony to wciaz emoji-placeholdery.
 */
@Composable
private fun ResourcePill(icon: String, value: String, caption: String? = null, onPlus: (() -> Unit)? = null) {
    Box(
        modifier = if (onPlus != null) Modifier.clickable(onClick = onPlus) else Modifier,
        contentAlignment = Alignment.CenterStart
    ) {
        // Pigulka ma ZAWSZE jedna linijke - wszystkie trzy stoja rowno, niezaleznie od timera.
        Box(
            modifier = Modifier
                .padding(start = 12.dp)
                .clip(RoundedCornerShape(50))
                .background(SlotFill)
                .padding(start = 18.dp, end = 7.dp, top = 2.dp, bottom = 2.dp),
            contentAlignment = Alignment.Center
        ) {
            GlyphText(text = value, color = Color.White, fontFamily = FontFirst(), fontWeight = FontWeight.Black, fontSize = 12.sp, maxLines = 1)
        }
        // Timer wisi POD pigulka jak znaczek na dolnej krawedzi plyty - nie sciska liczby
        // i nie rozpycha paska (offset nie zajmuje miejsca w ukladzie).
        if (caption != null) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .offset(x = 6.dp, y = 15.dp)
                    .clip(RoundedCornerShape(50))
                    .background(TimerFill)
                    .border(1.dp, PanelEdge, RoundedCornerShape(50))
                    .padding(horizontal = 6.dp, vertical = 1.dp)
            ) {
                GlyphText(
                    text = caption,
                    color = Color(0xFFFFE08A),
                    fontFamily = FontFirst(),
                    fontWeight = FontWeight.Bold,
                    fontSize = 9.sp,
                    lineHeight = 10.sp,
                    maxLines = 1
                )
            }
        }
        Box {
            GlyphText(text = icon, fontSize = 20.sp)
            if (onPlus != null) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .offset(x = 3.dp, y = 2.dp)
                        .size(12.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(PlusGreen),
                    contentAlignment = Alignment.Center
                ) {
                    GlyphText(text = "+", color = Color.White, fontWeight = FontWeight.Black, fontSize = 10.sp, lineHeight = 10.sp)
                }
            }
        }
    }
}

/** "7:01" - zegarowo, krotko, zeby zmiescilo sie w znaczku pod pigulka. */
private fun shortTime(millis: Long): String {
    val total = (millis / 1000).coerceAtLeast(0)
    val minutes = total / 60
    val seconds = total % 60
    return "$minutes:${seconds.toString().padStart(2, '0')}"
}

/**
 * Skroty przy duzych liczbach ("1.2M"). Pelne "10000167" rozpychalo pasek tak, ze imie gracza
 * scinalo sie do trzech liter - a nikt i tak nie czyta zlota co do sztuki.
 */
private fun shortCount(value: Int): String = when {
    value >= 1_000_000 -> "${value / 100_000 / 10.0}M"
    value >= 10_000 -> "${value / 1_000}k"
    else -> "$value"
}

/** Ile miejsca z lewej nalezy do portretu - o tyle plyta paska jest odsunieta. */
private val AVATAR_SIZE = 68.dp
private val AVATAR_LANE = AVATAR_SIZE + 8.dp
private val TAB_WIDTH = 170.dp
private val TAB_HEIGHT = 24.dp

/** Wysokosc samej plyty paska. */
private val BAR_HEIGHT = 50.dp

/**
 * Ile pasek REALNIE zakrywa, czyli wysokosc jego nieprzezroczystej plyty.
 *
 * To NIE to samo co [TOP_BAR_HEIGHT]: tamta liczba mowi, ile miejsca zarezerwowac na tresc (razem
 * z portretem zwieszonym pod plyta), a ta - dokad siega farba. Roznica to pas, w ktorym wisi sam
 * portret, a obok niego jest przezroczysto. Pelnoekranowe sceny (mapa Domu) musza znac te druga
 * liczbe: gdyby konczyly sie na [TOP_BAR_HEIGHT], w tym pasie nie byloby juz ich obrazu, tylko to,
 * co akurat lezy pod spodem.
 */
val TOP_BAR_PLATE_HEIGHT = BAR_HEIGHT

/**
 * Ile miejsca u gory ekranu nalezy do paska - razem z tym, co portret i plakietka mocy zwieszaja
 * pod plyta. Gdyby liczyla sie sama [BAR_HEIGHT], tresc ekranu wchodzilaby pod portret.
 */
val TOP_BAR_HEIGHT = 74.dp

private val PanelTop = Color(0xFF1E2B4C)
private val PanelBottom = Color(0xFF141C33)
private val PanelEdge = Color(0xFF3A4D78)
private val PortraitEdge = Color(0xFFBFD3F0)
private val SlotFill = Color(0xFF0C1322)
private val TimerFill = Color(0xFF1A2440)
private val RankViolet = Color(0xFFB48BFF)
private val LevelBlue = Color(0xFF2F6FB5)
private val GoldTint = Color(0xFFF2C14B)
private val EnergyTint = Color(0xFF4FA3F0)
private val TabFill = Color(0xFF0B1020)
private val PlusGreen = Color(0xFF43C743)
