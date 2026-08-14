package com.lernhero.home.map

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lernhero.domain.run.BattleKind
import com.lernhero.domain.run.RunSession
import com.lernhero.shared.resources.Resources
import com.lernhero.shared.theme.FontFirst
import com.lernhero.shared.theme.FontPixel
import kotlin.math.roundToInt
import kotlin.random.Random
import org.jetbrains.compose.resources.painterResource
import org.koin.compose.koinInject

private val PanelDark = Color(0xFF1A1410)
private val Gold = Color(0xFFC8A45A)
private val GoldDim = Color(0xFF6B5A38)
private val TextGold = Color(0xFFFFD100)
private val TextMuted = Color(0xFFC9BCA0)

/** Rzeczywisty rozmiar game_map.png w pikselach zrodlowych. */
private const val MAP_IMAGE_WIDTH = 1536f
private const val MAP_IMAGE_HEIGHT = 1024f

/**
 * Mapa jest renderowana zawsze w TYM SAMYM, stalym rozmiarze w pikselach - niezaleznie od
 * wielkosci ramki (pelnoekranowej czy nie). Dluzszy bok (szerokosc, bo obrazek jest szeroki i
 * poziomy) ma zawsze tyle pikseli.
 *
 * Dwa powody, dla ktorych NIE liczymy tego rozmiaru dynamicznie z rozmiaru ramki (jak w
 * poprzednich podejsciach):
 * 1) Przy pelnoekranowym, wysokim kontenerze wymagane "pokrycie" tak szerokiego obrazka
 *    eksplodowalo do kilku-kilkunastu tysiecy pikseli, co na czesci urzadzen/emulatorow konczylo
 *    sie calkowicie czarnym, nieinteraktywnym ekranem (prawdopodobnie limit tekstury GPU).
 * 2) [ContentScale.Crop] + [androidx.compose.ui.graphics.graphicsLayer] (kolejne podejscie) NIE
 *    dziala do przesuwania: Crop raz na zawsze "zamraza" wyciety fragment obrazka w fazie
 *    rysowania, wiec zadne pozniejsze skalowanie/przesuwanie warstwy juz nie odslania wiecej
 *    tresci zrodlowej - stad byla "czarna reszta" po zwiekszeniu zakresu przesuwania.
 *
 * Ten staly rozmiar naprawia oba: obrazek renderuje sie jawnie (`Modifier.size`) w bezpiecznych,
 * skonczonych px, a przesuwanie to zwykle przesuniecie pozycji (`Modifier.offset`) tego JUZ
 * calego, w pelni narysowanego obrazka - kazdy piksel zrodlowy jest realnie osiagalny.
 */
private const val RENDERED_LONG_EDGE = 6000f
private val RENDER_SCALE = RENDERED_LONG_EDGE / MAP_IMAGE_WIDTH
private val RENDERED_WIDTH = MAP_IMAGE_WIDTH * RENDER_SCALE
private val RENDERED_HEIGHT = MAP_IMAGE_HEIGHT * RENDER_SCALE

private val MARKER_SIZE = 34.dp

/**
 * Mapa krainy jako pelnoekranowe tlo zakladki "Walcz" (pod paskiem nawigacji na dole) - odpowiednik
 * "Estate Map" z Darkest Dungeon. Sklad druzyny plywa nad nia jako karta ([com.lernhero.home.team.TeamScreen]
 * uklada ja jako overlay). Wybor lokacji na mapie ustawia jednoczesnie trudnosc i kategorie pytan
 * biezacego runu (patrz [DungeonMapLocation]) i skladuje seed lochu w [RunSession.pendingDungeonSeed] -
 * to loch (nie ta mapa) decyduje, w ktory konkretny wezel/walke gracz ostatecznie trafi.
 *
 * Obrazek renderuje sie zawsze w [RENDERED_WIDTH]x[RENDERED_HEIGHT] px (patrz ich dokumentacja),
 * a przesuwanie przycina offset do granic tego renderu, wiec nigdy nie da sie "wyjechac" poza obrazek.
 */
@Composable
fun DungeonMapPanel(
    onStartBattle: () -> Unit,
    modifier: Modifier = Modifier
) {
    val runSession = koinInject<RunSession>()
    var selectedId by remember { mutableStateOf<String?>(null) }
    val selected = DungeonMapLocations.all.firstOrNull { it.id == selectedId }

    Box(modifier = modifier) {
        PannableMap(
            selectedId = selectedId,
            onSelect = { selectedId = it }
        )

        AnimatedVisibility(
            visible = selected != null,
            enter = fadeIn() + slideInVertically(initialOffsetY = { it / 2 }),
            exit = fadeOut() + slideOutVertically(targetOffsetY = { it / 2 }),
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            selected?.let { location ->
                LocationInfoPanel(
                    location = location,
                    onStartBattle = {
                        runSession.startNewRun(category = location.category, difficulty = location.difficulty)
                        runSession.pendingDungeonSeed = Random.nextLong()
                        onStartBattle()
                    }
                )
            }
        }
    }
}

@Composable
private fun PannableMap(
    selectedId: String?,
    onSelect: (String) -> Unit
) {
    val density = LocalDensity.current
    var containerSize by remember { mutableStateOf(IntSize.Zero) }
    // Null dopoki nie poznamy prawdziwego rozmiaru ramki - inaczej pierwsza klatka
    // wystartowalaby z (0,0) zamiast wysrodkowana, dajac widoczne "skoczenie" obrazka.
    var offset by remember { mutableStateOf<Offset?>(null) }

    // coerceAtMost(0f): gdyby [RENDERED_WIDTH]/[RENDERED_HEIGHT] okazaly sie mniejsze niz ramka
    // (bardzo duzy ekran), min nie moze wyjsc dodatni - inaczej coerceIn(min, 0f) nizej by
    // wywalil IllegalArgumentException. W tym skrajnym przypadku po prostu nie ma gdzie przesuwac.
    val minOffsetX = (containerSize.width - RENDERED_WIDTH).coerceAtMost(0f)
    val minOffsetY = (containerSize.height - RENDERED_HEIGHT).coerceAtMost(0f)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .clipToBounds()
            .background(Color(0xFF0B0805))
            .onSizeChanged { size ->
                containerSize = size
                if (offset == null) {
                    offset = Offset((size.width - RENDERED_WIDTH) / 2f, (size.height - RENDERED_HEIGHT) / 2f)
                }
            }
            .pointerInput(minOffsetX, minOffsetY) {
                detectDragGestures { change, dragAmount ->
                    change.consume()
                    val current = offset ?: Offset.Zero
                    offset = Offset(
                        x = (current.x + dragAmount.x).coerceIn(minOffsetX, 0f),
                        y = (current.y + dragAmount.y).coerceIn(minOffsetY, 0f)
                    )
                }
            }
    ) {
        val currentOffset = offset
        if (currentOffset != null) {
            Image(
                painter = painterResource(Resources.Image.dungeonMap),
                contentDescription = null,
                contentScale = ContentScale.FillBounds,
                modifier = Modifier
                    .offset { IntOffset(currentOffset.x.roundToInt(), currentOffset.y.roundToInt()) }
                    // KLUCZOWE: bez `unbounded = true` Modifier.size jest po cichu przycinany do
                    // rozmiaru rodzica (dziecko nie moze byc wieksze niz ramka) - mapa renderowala
                    // sie wtedy zgnieciona do szerokosci ekranu, a pinezki (liczone od pelnych
                    // RENDERED_* px) ladowaly poza nia, w czarnej pustce. wrapContentSize z
                    // unbounded pozwala obrazkowi przyjac pelny rozmiar i wystawac poza ramke,
                    // zakotwiczony lewym-gornym rogiem (TopStart) w punkcie po offsecie.
                    .wrapContentSize(align = Alignment.TopStart, unbounded = true)
                    .size(
                        width = with(density) { RENDERED_WIDTH.toDp() },
                        height = with(density) { RENDERED_HEIGHT.toDp() }
                    )
            )

            val markerHalfWidthPx = with(density) { (MARKER_SIZE / 2).toPx() }
            val markerHeightPx = with(density) { MARKER_SIZE.toPx() }

            DungeonMapLocations.all.forEach { location ->
                val markerX = currentOffset.x + location.x * RENDERED_WIDTH
                val markerY = currentOffset.y + location.y * RENDERED_HEIGHT

                MapMarker(
                    location = location,
                    selected = location.id == selectedId,
                    modifier = Modifier
                        .offset {
                            IntOffset(
                                x = (markerX - markerHalfWidthPx).roundToInt(),
                                y = (markerY - markerHeightPx).roundToInt()
                            )
                        }
                        .clickable { onSelect(location.id) }
                )
            }
        }
    }
}

@Composable
private fun MapMarker(
    location: DungeonMapLocation,
    selected: Boolean,
    modifier: Modifier = Modifier
) {
    val badgeColor = when (location.kind) {
        BattleKind.Normal -> Color(0xFFC8A45A)
        BattleKind.Elite -> Color(0xFF8B5CF6)
        BattleKind.Boss -> Color(0xFFD1453D)
    }

    Box(
        modifier = modifier
            .size(if (selected) MARKER_SIZE else MARKER_SIZE * 0.82f)
            .clip(CircleShape)
            .background(Color(0xFF120E0A).copy(alpha = 0.85f))
            .border(if (selected) 2.5.dp else 1.5.dp, if (selected) TextGold else badgeColor, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(Resources.Icon.MapPin),
            contentDescription = location.name,
            modifier = Modifier.size(MARKER_SIZE * 0.55f),
            colorFilter = androidx.compose.ui.graphics.ColorFilter.tint(if (selected) TextGold else badgeColor)
        )
    }
}

@Composable
private fun LocationInfoPanel(
    location: DungeonMapLocation,
    onStartBattle: () -> Unit
) {
    val kindColor = when (location.kind) {
        BattleKind.Normal -> Color(0xFFC8A45A)
        BattleKind.Elite -> Color(0xFF8B5CF6)
        BattleKind.Boss -> Color(0xFFD1453D)
    }
    val kindLabel = when (location.kind) {
        BattleKind.Normal -> "Zwykla walka"
        BattleKind.Elite -> "Elitarna walka"
        BattleKind.Boss -> "Bos"
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(PanelDark)
            .border(2.dp, GoldDim, RoundedCornerShape(topStart = 14.dp, topEnd = 14.dp))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(
            text = location.name,
            color = TextGold,
            fontFamily = FontPixel(),
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.titleMedium
        )

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            InfoBadge(text = kindLabel, color = kindColor)
            InfoBadge(text = location.difficulty.label, color = Gold)
            InfoBadge(text = location.category?.label ?: "Losowe pytania", color = Color(0xFF2F8FE0))
        }

        Text(
            text = "${location.waves} " + if (location.waves == 1) "fala wrogow" else "fale wrogow",
            color = TextMuted,
            fontFamily = FontFirst(),
            fontSize = 12.sp
        )

        Button(
            onClick = onStartBattle,
            modifier = Modifier.fillMaxWidth().height(52.dp).padding(top = 6.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFB4451F), contentColor = Color.White)
        ) {
            Text(
                text = "ROZPOCZNIJ WALKE",
                fontFamily = FontPixel(),
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleMedium
            )
        }
    }
}

@Composable
private fun InfoBadge(text: String, color: Color) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(color.copy(alpha = 0.16f))
            .border(1.dp, color, RoundedCornerShape(50))
            .padding(horizontal = 10.dp, vertical = 5.dp)
    ) {
        Text(text = text, color = color, fontFamily = FontFirst(), fontWeight = FontWeight.Bold, fontSize = 11.sp)
    }
}
