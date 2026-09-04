package com.lernhero.home.dev

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lernhero.game.battle.effects.EffectCatalog
import com.lernhero.game.battle.effects.EffectSpec
import com.lernhero.game.spritesheet.SpriteSheetFrame
import com.lernhero.game.spritesheet.frameForProgress
import com.lernhero.shared.resources.Resources
import com.lernhero.shared.theme.ButtonPrimary
import com.lernhero.shared.theme.FontFirst
import com.lernhero.shared.theme.Surface
import com.lernhero.shared.theme.TextPrimary
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import kotlin.math.roundToInt

/**
 * "Monster Anchor Lab" - ten sam workflow co Knight Pivot Lab (KnightPreviewScreen.kt), tylko dla
 * [com.lernhero.shared.resources.CombatAnchorSet] potworow: wybierasz gatunek, wybierasz ktory
 * anchor ustawiasz (root/effectAnchor/hitPoint/projectileOrigin/statusEffectAnchor/hpBarAnchor),
 * suwakami X/Y (albo klikniciem wprost na podglad) dopasowujesz punkt na obrazku, "Zapisz"
 * zatwierdza W PAMIECI TEGO EKRANU.
 *
 * WAZNE: nic tu nie trafia do dysku samo z siebie. Ten ekran zyje na stosie nawigacji (composable<Screen.MonsterAnchorLab>
 * w HomeNavGraph.kt) - `onBack` robi `popBackStack()`, co CALKOWICIE niszczy ta kompozycje razem
 * z jej `remember`-owanym stanem. Jedyny trwaly efekt to recznie skopiowany kod wklejony do
 * shared/.../resources/CombatAnchors.kt. Dlatego "Zapisz anchor" od razu kopiuje aktualny kod do
 * schowka (patrz przycisk nizej) - to jedyna siatka bezpieczenstwa przed wyjsciem bez wklejenia.
 *
 * Dwie dodatkowe mozliwosci ponad samo ustawianie kropki:
 * - "Globalny" przelacznik przy polu: niektore anchory (np. miejsce wybuchu) maja sens jako JEDNA
 *   wartosc dla wszystkich gatunkow, nie osobna per gatunek - suwaki wtedy edytuja wspolna wartosc
 *   niezaleznie od wybranego gatunku, a kod generuje sie jako jedna stala + referencja w kazdym
 *   CombatAnchorSet (patrz [buildMonsterAnchorsCode]).
 * - Podglad efektu: wybierz PNG z [EffectCatalog.previewCatalog], suwakiem "Wielkosc" dopasuj
 *   rozmiar (skalowany tak, zeby odpowiadal PRAWDZIWEMU rozmiarowi w walce - patrz
 *   [BATTLE_ACCURATE_SCALE]) i kliknij "Odtworz" - gra dokladnie w miejscu wybranego anchoru.
 *
 * Caly ekran jest scisniety pod katem miejsca: male czipy w FlowRow (bez przewijania w bok,
 * zawijaja sie same), cienkie suwaki, a wygenerowany kod ma wlasny, ograniczony pasek przewijania
 * zamiast rozciagac cala strone w nieskonczonosc.
 */
@Composable
fun MonsterAnchorLabScreen(
    onBack: () -> Unit
) {
    val species = remember { monsterSpeciesList() }
    val fields = remember { anchorFields() }
    var selectedSpecies by remember { mutableStateOf(species.first()) }
    var selectedField by remember { mutableStateOf(fields.first()) }
    val clipboard = LocalClipboardManager.current

    val originXs = remember { mutableStateMapOf<String, Float>() }
    val originYs = remember { mutableStateMapOf<String, Float>() }
    // Anchory zatwierdzone przyciskiem "Zapisz anchor" - tylko one traja do wygenerowanego kodu.
    val savedOrigins = remember { mutableStateMapOf<String, Offset>() }
    // Ktore pola (po field.id) sa oznaczone jako wspolne dla wszystkich gatunkow - patrz anchorKey().
    val globalFields = remember { mutableStateMapOf<String, Boolean>() }
    val isSelectedFieldGlobal = globalFields[selectedField.id] == true

    var selectedEffect by remember { mutableStateOf<EffectSpec?>(EffectCatalog.previewCatalog.firstOrNull()) }
    var effectPlaybackId by remember { mutableStateOf(0L) }
    // 1f = "tak duzy, jak w EffectSpec dzisiaj". Startuje NIE od 1f, tylko od BATTLE_ACCURATE_SCALE,
    // bo scena podgladu jest wieksza niz postac naprawde renderuje sie w walce (patrz stala nizej) -
    // inaczej ten sam sizeDp wygladalby tu karlowato w porownaniu do tego, co widac w realnej bitwie.
    var effectScale by remember { mutableStateOf(BATTLE_ACCURATE_SCALE) }
    // Rozmiar suwaka "Wielkosc" TEZ musi trafic do zapisu - inaczej to tylko podglad, ktory znika
    // bez sladu przy wyjsciu (dokladnie zglos zone "myslalem, ze jak zmieniam to tez zapisuje").
    // Klucz = effect.id, bo sizeDp/heightDp w EffectSpec jest jeden na caly katalog, nie per gatunek.
    val savedEffectSizes = remember { mutableStateMapOf<String, Float>() }

    val key = anchorKey(selectedSpecies, selectedField, isSelectedFieldGlobal)
    val originX = originXs[key] ?: 0.5f
    val originY = originYs[key] ?: 0.5f

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Surface)
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(10.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            CompactChip(label = "Wroc", selected = false, onClick = onBack)
            Text(
                text = "Monster Anchor Lab",
                color = TextPrimary,
                fontFamily = FontFirst(),
                fontSize = 11.sp
            )
        }

        CompactChipRow(
            items = species,
            keyOf = { it.key },
            labelOf = { it.label },
            selectedKey = selectedSpecies.key,
            onSelect = { selectedSpecies = it }
        )
        CompactChipRow(
            items = fields,
            keyOf = { it.id },
            labelOf = { it.label },
            selectedKey = selectedField.id,
            onSelect = { selectedField = it }
        )

        // GLOWNY WIDOK: obrazek po lewej, WSZYSTKIE najwazniejsze akcje (efekt/Zapisz/Odtworz) po
        // prawej, w miejscu ktore wczesniej stalo puste - to jest sedno "bez scrollowania": gatunek,
        // pole, pozycja (klikniecie na obrazku), zapis i odtworzenie miesza sie w jednym ekranie.
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            MonsterAnchorStage(
                species = selectedSpecies,
                fields = fields,
                selectedField = selectedField,
                resolveOrigin = { field ->
                    val isGlobal = globalFields[field.id] == true
                    val k = anchorKey(selectedSpecies, field, isGlobal)
                    Offset(originXs[k] ?: 0.5f, originYs[k] ?: 0.5f)
                },
                onTapOrigin = { tapped ->
                    // Kliknieta pozycja dziala DOKLADNIE tak samo jak przesuniecie obu suwakow naraz -
                    // sam klucz (per gatunek albo globalny) juz jest policzony wyzej.
                    originXs[key] = tapped.x
                    originYs[key] = tapped.y
                },
                previewEffect = selectedEffect,
                previewTrigger = effectPlaybackId,
                previewScale = effectScale
            )

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                CompactChipRow(
                    items = EffectCatalog.previewCatalog,
                    keyOf = { it.id },
                    labelOf = { it.id },
                    selectedKey = selectedEffect?.id,
                    onSelect = { selectedEffect = it }
                )
                Button(
                    onClick = {
                        val originValue = Offset(originX, originY)
                        savedOrigins[key] = originValue
                        // Rozmiar aktualnie wybranego efektu zapisuje sie RAZEM z anchorem - jeden
                        // klik, jeden schowek, zero "a co z wielkoscia?" pozniej.
                        selectedEffect?.let { savedEffectSizes[it.id] = effectScale }
                        // "Zapisz" NIE trafia do zadnego pliku - to tylko stan tego ekranu, ginie
                        // przy wyjsciu (popBackStack niszczy cala kompozycje). Kopiujemy wiec kod
                        // od razu do schowka, zeby ostatni stan zawsze byl odzyskiwalny nawet gdy
                        // ktos zapomni recznie kliknac "Kopiuj kod" przed wyjsciem.
                        clipboard.setText(
                            AnnotatedString(
                                buildEffectSizesComment(savedEffectSizes) +
                                    buildMonsterAnchorsCode(species, fields, savedOrigins, globalFields)
                            )
                        )
                    },
                    modifier = Modifier.fillMaxWidth().heightIn(min = 34.dp),
                    contentPadding = PaddingValues(vertical = 4.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (savedOrigins[key]?.let { it.x == originX && it.y == originY } == true) {
                            Color(0xFF3C8A3C)
                        } else {
                            ButtonPrimary
                        },
                        contentColor = TextPrimary
                    )
                ) {
                    Text(text = "Zapisz", fontFamily = FontFirst(), fontSize = 12.sp)
                }
                Button(
                    onClick = { effectPlaybackId += 1 },
                    modifier = Modifier.fillMaxWidth().heightIn(min = 34.dp),
                    enabled = selectedEffect != null,
                    contentPadding = PaddingValues(vertical = 4.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFB4451F), contentColor = Color.White)
                ) {
                    Text(text = "▶ Odtworz", fontFamily = FontFirst(), fontSize = 12.sp)
                }
                Text(
                    text = "TransformOrigin(${originX.toCodeValue()}, ${originY.toCodeValue()})",
                    color = Color(0xFF6D6D6D),
                    fontFamily = FontFirst(),
                    fontSize = 9.sp
                )
            }
        }

        AnchorSlider(
            title = "X",
            valueText = "${originX.toDisplayValue()} / ${(originX * selectedSpecies.canvasPx).roundToInt()}px",
            value = originX,
            onValueChange = { originXs[key] = it }
        )
        AnchorSlider(
            title = "Y",
            valueText = "${originY.toDisplayValue()} / ${(originY * selectedSpecies.canvasPx).roundToInt()}px",
            value = originY,
            onValueChange = { originYs[key] = it }
        )

        // Reszta (globalny przelacznik, dostrajanie wielkosci efektu, gotowy kod) jest ponizej,
        // dostepna scrollem - ale NIE jest potrzebna do samego "ustaw i odtworz", ktore miesci sie
        // teraz calkowicie na starcie ekranu.
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(6.dp))
                .background(Color(0xFFF5F5F5))
                .padding(horizontal = 8.dp, vertical = 2.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Globalny (wspolny dla wszystkich gatunkow)",
                color = TextPrimary,
                fontFamily = FontFirst(),
                fontSize = 10.sp,
                modifier = Modifier.weight(1f)
            )
            Switch(
                checked = isSelectedFieldGlobal,
                onCheckedChange = { checked ->
                    if (checked) {
                        // Przenosimy aktualna wartosc TEGO gatunku na wspolny klucz, zeby przelaczenie
                        // nie "zerowalo" wizualnie tego, na co ktos wlasnie patrzyl.
                        val perSpeciesKey = anchorKey(selectedSpecies, selectedField, isGlobal = false)
                        originXs[anchorKey(selectedSpecies, selectedField, isGlobal = true)] = originXs[perSpeciesKey] ?: 0.5f
                        originYs[anchorKey(selectedSpecies, selectedField, isGlobal = true)] = originYs[perSpeciesKey] ?: 0.5f
                    }
                    globalFields[selectedField.id] = checked
                },
                modifier = Modifier.scale(0.8f),
                colors = SwitchDefaults.colors(checkedTrackColor = ButtonPrimary)
            )
        }

        selectedEffect?.let { effect ->
            CompactPanel(title = "Wielkosc efektu") {
                AnchorSlider(
                    title = "Wielkosc",
                    valueText = "${effectScale.toDisplayValue()}x",
                    value = effectScale,
                    onValueChange = { effectScale = it },
                    valueRange = 0.3f..8f
                )
                val sizeSaved = savedEffectSizes[effect.id] == effectScale
                Text(
                    text = "W kodzie: sizeDp=${(effect.sizeDp.value * effectScale).roundToInt()}.dp, " +
                        "heightDp=${(effect.heightDp.value * effectScale).roundToInt()}.dp" +
                        if (sizeSaved) " - zapisane ✓" else " - NIEZAPISANE, kliknij Zapisz",
                    color = if (sizeSaved) Color(0xFF3C8A3C) else Color(0xFFB4451F),
                    fontFamily = FontFirst(),
                    fontSize = 10.sp
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(6.dp))
                .background(Color(0xFF1E1E1E))
                .padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "MonsterAnchors (${savedOrigins.size}/${species.size * fields.size})",
                    color = Color.White,
                    fontFamily = FontFirst(),
                    fontSize = 10.sp
                )
                CompactChip(
                    label = "Kopiuj kod",
                    selected = false,
                    dark = true,
                    onClick = {
                        clipboard.setText(
                            AnnotatedString(
                                buildEffectSizesComment(savedEffectSizes) +
                                    buildMonsterAnchorsCode(species, fields, savedOrigins, globalFields)
                            )
                        )
                    }
                )
            }
            // Wlasny, ograniczony scroll - wygenerowany kod dla 7 gatunkow x 6 pol potrafi byc
            // dlugi, i bez tego rozciagalby cala reszte ekranu w dol zamiast zostac w miejscu.
            Text(
                text = buildEffectSizesComment(savedEffectSizes) +
                    buildMonsterAnchorsCode(species, fields, savedOrigins, globalFields),
                color = Color(0xFF9CDB9C),
                fontFamily = FontFirst(),
                fontSize = 10.sp,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 140.dp)
                    .verticalScroll(rememberScrollState())
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedButton(
                onClick = {
                    originXs[key] = 0.5f
                    originYs[key] = 0.5f
                },
                modifier = Modifier.weight(1f).heightIn(min = 34.dp),
                contentPadding = PaddingValues(vertical = 4.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = TextPrimary)
            ) {
                Text(text = "Reset anchoru", fontFamily = FontFirst(), fontSize = 12.sp)
            }
            Button(
                onClick = onBack,
                modifier = Modifier.weight(1f).heightIn(min = 34.dp),
                contentPadding = PaddingValues(vertical = 4.dp),
                colors = ButtonDefaults.buttonColors(containerColor = ButtonPrimary, contentColor = TextPrimary)
            ) {
                Text(text = "Wyjdz", fontFamily = FontFirst(), fontSize = 12.sp)
            }
        }
    }
}

/** Male, gesto pakowane "opakowanie" (tlo + naglowek) na sekcje ponizej sceny podgladu. */
@Composable
private fun CompactPanel(title: String?, content: @Composable () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(6.dp))
            .background(Color(0xFFF5F5F5))
            .padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        title?.let {
            Text(text = it, color = TextPrimary, fontFamily = FontFirst(), fontSize = 11.sp, fontWeight = FontWeight.Bold)
        }
        content()
    }
}

/** [FlowRow] zamiast [androidx.compose.foundation.lazy.LazyRow] - male czipy zawijaja sie same, bez przewijania w bok. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun <T> CompactChipRow(
    items: List<T>,
    keyOf: (T) -> String,
    labelOf: (T) -> String,
    selectedKey: String?,
    onSelect: (T) -> Unit
) {
    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        items.forEach { item ->
            key(keyOf(item)) {
                CompactChip(
                    label = labelOf(item),
                    selected = keyOf(item) == selectedKey,
                    onClick = { onSelect(item) }
                )
            }
        }
    }
}

/** Wlasny, malutki czip zamiast pelnowymiarowego Button/OutlinedButton - te maja spory wbudowany min-rozmiar. */
@Composable
private fun CompactChip(label: String, selected: Boolean, dark: Boolean = false, onClick: () -> Unit) {
    val (bg, border, text) = when {
        selected -> Triple(ButtonPrimary, ButtonPrimary, TextPrimary)
        dark -> Triple(Color(0xFF2A2A2A), Color(0xFF4A4A4A), Color.White)
        else -> Triple(Color.Transparent, Color(0xFFBBBBBB), TextPrimary)
    }
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(bg)
            .border(1.dp, border, RoundedCornerShape(50))
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 5.dp)
    ) {
        Text(text = label, color = text, fontFamily = FontFirst(), fontSize = 10.sp)
    }
}

@Composable
private fun MonsterAnchorStage(
    species: MonsterSpeciesEntry,
    fields: List<MonsterAnchorField>,
    selectedField: MonsterAnchorField,
    resolveOrigin: (MonsterAnchorField) -> Offset,
    onTapOrigin: (Offset) -> Unit,
    previewEffect: EffectSpec?,
    previewTrigger: Long,
    previewScale: Float
) {
    // pointerInput(Unit) instaluje gest raz i NIGDY nie restartuje sie sam - jesli zamkniemy go
    // bezposrednio nad `onTapOrigin`, zamknie sie nad TA PIERWSZA instancja lambdy (z pierwszej
    // kompozycji, gdy selectedField = "root") i bedzie jej uzywac na zawsze, ignorujac kolejne
    // zaznaczenia pola - dokladnie ten bug ("zawsze rusza sie root"). rememberUpdatedState daje
    // zawsze-aktualna referencje, ktora mozna bezpiecznie czytac z wnetrza dlugozyjacego gestu.
    val currentOnTapOrigin = rememberUpdatedState(onTapOrigin)

    // BEZ fillMaxWidth(): ten Box siedzi teraz obok kolumny przyciskow w Row (patrz wywolanie w
    // MonsterAnchorLabScreen), wiec musi zostac przy swoim naturalnym, stalym rozmiarze STAGE_DP -
    // fillMaxWidth zabraloby cala szerokosc wiersza i zepchneloby przyciski poza ekran.
    Box(contentAlignment = Alignment.Center) {
        Box(
            modifier = Modifier
                .size(STAGE_DP.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xFF1E1E1E))
                // Klikniecie ustawia anchor od razu na ta pozycje (jako ulamek 0..1 tej sceny) -
                // suwaki X/Y ponizej zostaja dostepne obok, do dopieszczenia "na piksel".
                .pointerInput(Unit) {
                    detectTapGestures { offset ->
                        currentOnTapOrigin.value(Offset(offset.x / size.width, offset.y / size.height))
                    }
                }
        ) {
            Image(
                painter = painterResource(species.drawable),
                contentDescription = species.label,
                modifier = Modifier.fillMaxSize()
            )
            AllAnchorMarkers(
                fields = fields,
                selectedFieldId = selectedField.id,
                resolveOrigin = resolveOrigin
            )
        }
        // WAZNE: ten Box ma STALY rozmiar STAGE_DP (nie wrap-content) i BRAK .clip() - dwie rzeczy
        // naraz. Staly rozmiar = wielokrotnie wiekszy podglad efektu (patrz EffectPreviewOverlay,
        // do EFFECT_CANVAS_DP) NIE nadmuchuje wysokosci calego ekranu, bo Box i tak zawsze raportuje
        // rodzicowi dokladnie STAGE_DP niezaleznie od tego, co jest w srodku - to byl PRAWDZIWY
        // powod ogromnej pustej przestrzeni po pierwszym "Odtworz" (previewTrigger juz nigdy nie
        // wraca do 0, wiec ten podglad zostawal doklejony na stale, a bez tego stalego rozmiaru
        // rozpychal caly ekran w dol o EFFECT_CANVAS_DP). Brak .clip() pozwala mu jednoczesnie
        // wizualnie wystawac poza wlasne granice bez obcinania (drugi, wczesniej naprawiony bug).
        Box(modifier = Modifier.size(STAGE_DP.dp), contentAlignment = Alignment.Center) {
            if (previewEffect != null && previewTrigger > 0) {
                key(previewTrigger) {
                    EffectPreviewOverlay(
                        effect = previewEffect,
                        origin = resolveOrigin(selectedField),
                        scale = previewScale,
                        modifier = Modifier.wrapContentSize(unbounded = true).size(EFFECT_CANVAS_DP.dp)
                    )
                }
            }
        }
    }
}

/**
 * Odtwarza [effect] JEDEN RAZ w miejscu [origin] (ulamek 0..1 wzgledem sceny [STAGE_DP]) - dokladnie
 * ta sama logika ruchu/klatek co prawdziwa gra ([com.lernhero.game.battle.effects.EffectsOverlay]),
 * tylko bez calego systemu root-koordynatow (scena jest juz lokalnym, samodzielnym Boxem). Zawsze
 * gra jako "stempel" w jednym miejscu, nawet jesli [EffectSpec.motion] to TRAVEL w prawdziwej grze -
 * tu chodzi o to, JAK i GDZIE wyglada, nie o cala sekwencje ataku.
 *
 * [modifier] daje temu Boxowi wlasny, NIEPRZYCIETY [EFFECT_CANVAS_DP] rozmiar (patrz wywolanie w
 * [MonsterAnchorStage]) - wysrodkowany na tym samym punkcie co postac, wiec [origin] trzeba
 * przeliczyc z ulamka wzgledem [STAGE_DP] na piksele w TEJ wiekszej, wspolsrodkowej ramce.
 */
@Composable
private fun EffectPreviewOverlay(effect: EffectSpec, origin: Offset, scale: Float, modifier: Modifier = Modifier) {
    val progress = remember(effect.id, origin) { Animatable(0f) }
    LaunchedEffect(effect.id, origin) {
        progress.snapTo(0f)
        progress.animateTo(1f, tween(effect.durationMs, easing = LinearEasing))
    }

    val density = LocalDensity.current
    val alpha = when {
        progress.value < 0.15f -> progress.value / 0.15f
        progress.value > 0.8f -> (1f - progress.value) / 0.2f
        else -> 1f
    }

    Box(modifier = modifier) {
        val scaledWidth = effect.sizeDp * scale
        val scaledHeight = effect.heightDp * scale
        val halfWidthPx = with(density) { (scaledWidth / 2).toPx() }
        val halfHeightPx = with(density) { (scaledHeight / 2).toPx() }
        val stagePx = with(density) { STAGE_DP.dp.toPx() }
        // Margines miedzy krawedzia tej wiekszej ramki a krawedzia 210dp postaci, ktora siedzi
        // dokladnie na jej srodku (obie maja Alignment.Center w tym samym rodzicu).
        val marginPx = with(density) { ((EFFECT_CANVAS_DP - STAGE_DP) / 2).dp.toPx() }
        // Ta sama korekta co w prawdziwej grze (patrz EffectsOverlay.kt) - inaczej Lab i walka
        // wygladalyby inaczej dla efektow z niewysrodkowana trescia (np. magic_bomb).
        val contentOffsetXPx = effect.contentOffset.x * with(density) { scaledWidth.toPx() }
        val contentOffsetYPx = effect.contentOffset.y * with(density) { scaledHeight.toPx() }
        val xPx = marginPx + stagePx * origin.x - halfWidthPx - contentOffsetXPx
        val yPx = marginPx + stagePx * origin.y - halfHeightPx - contentOffsetYPx
        val positionModifier = Modifier
            .size(width = scaledWidth, height = scaledHeight)
            .offset { IntOffset(xPx.toInt(), yPx.toInt()) }
            .graphicsLayer { this.alpha = alpha }

        val spriteSheet = effect.spriteSheet
        if (spriteSheet != null) {
            SpriteSheetFrame(
                spec = spriteSheet,
                frame = spriteSheet.frameForProgress(progress.value),
                modifier = positionModifier
            )
        } else {
            Image(
                painter = painterResource(effect.drawable),
                contentDescription = null,
                modifier = positionModifier
            )
        }
    }
}

@Composable
private fun AllAnchorMarkers(
    fields: List<MonsterAnchorField>,
    selectedFieldId: String,
    resolveOrigin: (MonsterAnchorField) -> Offset
) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        fields.forEach { field ->
            val origin = resolveOrigin(field)
            val x = size.width * origin.x
            val y = size.height * origin.y
            val selected = field.id == selectedFieldId
            val color = if (selected) Color(0xFFFF4D00) else Color(0xFF6D9CFF)

            if (selected) {
                drawLine(color = color.copy(alpha = 0.5f), start = Offset(x, 0f), end = Offset(x, size.height), strokeWidth = 1.dp.toPx())
                drawLine(color = color.copy(alpha = 0.5f), start = Offset(0f, y), end = Offset(size.width, y), strokeWidth = 1.dp.toPx())
            }
            val outerRadius = if (selected) 7.dp.toPx() else 5.dp.toPx()
            val innerRadius = if (selected) 2.5.dp.toPx() else 1.8.dp.toPx()
            drawCircle(color = color, radius = outerRadius, center = Offset(x, y))
            drawCircle(color = Color.White, radius = innerRadius, center = Offset(x, y))
        }
    }
}

@Composable
private fun AnchorSlider(
    title: String,
    valueText: String,
    value: Float,
    onValueChange: (Float) -> Unit,
    // Nieco szerzej niz 0..1 - przydatne do anchorow celowo wystajacych poza canvas assetu
    // (np. pasek HP kawalek nad glowa, poza gornym brzegiem obrazka).
    valueRange: ClosedFloatingPointRange<Float> = -0.3f..1.3f
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(6.dp))
            .background(Color(0xFFF5F5F5))
            .padding(horizontal = 8.dp),
        verticalArrangement = Arrangement.spacedBy(0.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = title, color = TextPrimary, fontFamily = FontFirst(), fontSize = 10.sp)
            Text(text = valueText, color = Color(0xFF6D6D6D), fontFamily = FontFirst(), fontSize = 10.sp)
        }
        CompactSlider(value = value, onValueChange = onValueChange, valueRange = valueRange)
    }
}

/**
 * Wlasny, cienki suwak zamiast Material3 [Slider] - ten ma sztywno wbudowany min. rozmiar
 * touch-targetu (~48dp), ktorego NIE da sie zmniejszyc zadnym modifierem (Modifier.height/heightIn
 * na Sliderze nie robi nic - to bylo dokladnie zglos zone "ogromne suwaki"). Ten ma tylko tyle
 * wysokosci, ile faktycznie widac: cienki pasek + maly kolek, przeciagany albo klikany wprost.
 */
@Composable
private fun CompactSlider(
    value: Float,
    onValueChange: (Float) -> Unit,
    valueRange: ClosedFloatingPointRange<Float>
) {
    val density = LocalDensity.current
    var widthPx by remember { mutableStateOf(0f) }
    // rememberUpdatedState: pointerInput(Unit) instaluje gest raz i nie restartuje sie sam (patrz
    // ten sam bug naprawiony wczesniej przy klikaniu anchoru) - bez tego przeciaganie uzywaloby
    // NAJPIERWSZEGO onValueChange/valueRange na zawsze, ignorujac kolejne rekompozycje.
    val currentOnValueChange = rememberUpdatedState(onValueChange)
    val currentRange = rememberUpdatedState(valueRange)

    fun fractionToValue(fraction: Float): Float {
        val range = currentRange.value
        return (range.start + fraction.coerceIn(0f, 1f) * (range.endInclusive - range.start))
    }

    val fraction = ((value - valueRange.start) / (valueRange.endInclusive - valueRange.start)).coerceIn(0f, 1f)
    val thumbDiameter = 14.dp
    val trackHeight = 3.dp

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(20.dp)
            .onSizeChanged { widthPx = it.width.toFloat() }
            .pointerInput(Unit) {
                detectTapGestures { offset ->
                    if (widthPx > 0f) currentOnValueChange.value(fractionToValue(offset.x / widthPx))
                }
            }
            .pointerInput(Unit) {
                detectDragGestures { change, _ ->
                    change.consume()
                    if (widthPx > 0f) currentOnValueChange.value(fractionToValue(change.position.x / widthPx))
                }
            },
        contentAlignment = Alignment.CenterStart
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(trackHeight)
                .clip(RoundedCornerShape(50))
                .background(Color(0xFFDDDDDD))
        )
        Box(
            modifier = Modifier
                .fillMaxWidth(fraction)
                .height(trackHeight)
                .clip(RoundedCornerShape(50))
                .background(ButtonPrimary)
        )
        val thumbOffsetPx = with(density) { (widthPx * fraction - thumbDiameter.toPx() / 2).toInt() }
        Box(
            modifier = Modifier
                .offset { IntOffset(thumbOffsetPx, 0) }
                .size(thumbDiameter)
                .clip(CircleShape)
                .background(ButtonPrimary)
        )
    }
}

/**
 * Scena podgladu ([MonsterAnchorStage]) rysuje postac na stalym kwadracie [STAGE_DP] (dobrym do
 * precyzyjnego ustawiania anchorow, w tym klikniecia - patrz onTapOrigin), ale w PRAWDZIWEJ walce
 * postac renderuje sie duzo mniejsza - [com.lernhero.game.battle.component.calculateCardSize]
 * przycina do 55-105dp, a wiekszosc zwyklych potworow (battleScale=1) na typowym telefonie i tak
 * laduje blisko gornego widelka, wiec przyjete tu ~100dp jako "typowa" wielkosc bojowa. Bez tej
 * poprawki ten sam EffectSpec.sizeDp wygladal w labie kilka razy mniejszy wzgledem postaci, niz
 * naprawde wyglada w walce - stad [BATTLE_ACCURATE_SCALE].
 */
private const val STAGE_DP = 148f
private const val TYPICAL_BATTLE_CARD_DP = 100f
private const val BATTLE_ACCURATE_SCALE = STAGE_DP / TYPICAL_BATTLE_CARD_DP

/**
 * Rozmiar NIEPRZYCIETEGO Boxa, w ktorym renderuje sie podglad efektu (patrz [EffectPreviewOverlay]) -
 * spory margines ponad [STAGE_DP], zeby nawet mocno powiekszony (do 8x) wybuch mial gdzie
 * "wystawac" bez obcinania. Compose i tak nie przycina dzieci Boxa bez jawnego .clip(), wiec ta
 * wartosc nie musi byc idealnie dopasowana do najwiekszego mozliwego efektu - liczy sie tylko jako
 * punkt odniesienia dla przeliczenia [STAGE_DP]-owego anchoru na wspolrzedne w tej wiekszej ramce.
 */
private const val EFFECT_CANVAS_DP = 500f

private data class MonsterAnchorField(val id: String, val label: String)

/**
 * WAZNE: nie kazde pole jest juz podpiete do walki (patrz BattleField.kt/CombatEffectsCoordinator).
 * Realnie uzywane dzis: hitPoint (koniec lotu pocisku - TU odpala sie wybuch/impact) i
 * projectileOrigin (start lotu pocisku). effectAnchor i statusEffectAnchor sa zarezerwowane pod
 * przyszle spelle/statusy - ustawienie ich NIE zmieni niczego w biezacej walce, stad etykiety
 * nizej wprost to mowia (zeby nie powtorzyc pomylki "ustawilem effectAnchor, a nic sie nie zmienilo").
 */
private fun anchorFields(): List<MonsterAnchorField> = listOf(
    MonsterAnchorField("root", "Root (grunt)"),
    MonsterAnchorField("effectAnchor", "Effect Anchor (jeszcze nieuzywane)"),
    MonsterAnchorField("hitPoint", "Hit Point (trafienie/wybuch)"),
    MonsterAnchorField("projectileOrigin", "Projectile Origin (start pocisku)"),
    MonsterAnchorField("statusEffectAnchor", "Status Anchor (jeszcze nieuzywane)"),
    MonsterAnchorField("hpBarAnchor", "HP Bar")
)

private data class MonsterSpeciesEntry(
    val key: String,
    val label: String,
    val fieldName: String,
    val drawable: DrawableResource,
    val canvasPx: Float
)

private fun monsterSpeciesList(): List<MonsterSpeciesEntry> = listOf(
    MonsterSpeciesEntry("skeleton_warrior", "Szkielet wojownik", "skeletonWarrior", Resources.MonsterArt.skeletonWarrior, 256f),
    MonsterSpeciesEntry("skeleton_archer", "Szkielet lucznik", "skeletonArcher", Resources.MonsterArt.skeletonArcher, 256f),
    MonsterSpeciesEntry("skeleton_mage", "Szkielet mag", "skeletonMage", Resources.MonsterArt.skeletonMage, 256f),
    MonsterSpeciesEntry("orc_warrior", "Ork wojownik", "orcWarrior", Resources.MonsterArt.orcWarrior, 256f),
    MonsterSpeciesEntry("orc_archer", "Ork lucznik", "orcArcher", Resources.MonsterArt.orcArcher, 256f),
    MonsterSpeciesEntry("human_archer", "Ludzki lucznik", "humanArcher", Resources.MonsterArt.humanArcher, 256f),
    MonsterSpeciesEntry("boss_skeleton_dragon", "Boss: smoczy szkielet", "bossSkeletonDragon", Resources.MonsterArt.bossSkeletonDragon, 768f)
)

/**
 * [isGlobal] = true zwraca klucz WSPOLNY dla wszystkich gatunkow (np. "GLOBAL:effectAnchor") -
 * uzywany, gdy dane pole jest oznaczone przelacznikiem "Globalny" w UI (patrz [MonsterAnchorLabScreen]).
 */
private fun anchorKey(species: MonsterSpeciesEntry, field: MonsterAnchorField, isGlobal: Boolean): String =
    if (isGlobal) "GLOBAL:${field.id}" else "${species.key}:${field.id}"

/**
 * Blok komentarzy nad wygenerowanym kodem anchorow - jeden wiersz na kazdy efekt, ktorego rozmiar
 * zostal zapisany (patrz [savedEffectSizes], wypelniane razem z anchorem przy "Zapisz anchor").
 * Bez tego suwak "Wielkosc" byl czystym podgladem, ktory znikal bez sladu przy wyjsciu z ekranu -
 * to jedyny sposob, w jaki wynik dostrajania rozmiaru w ogole opuszcza ten ekran.
 */
private fun buildEffectSizesComment(savedEffectSizes: Map<String, Float>): String {
    if (savedEffectSizes.isEmpty()) return ""
    val lines = EffectCatalog.previewCatalog.mapNotNull { effect ->
        val scale = savedEffectSizes[effect.id] ?: return@mapNotNull null
        val width = (effect.sizeDp.value * scale).roundToInt()
        val height = (effect.heightDp.value * scale).roundToInt()
        "// ${effect.id}: sizeDp = $width.dp, heightDp = $height.dp (EffectCatalog.kt)"
    }
    return if (lines.isEmpty()) "" else lines.joinToString("\n") + "\n\n"
}

/**
 * Generuje gotowy do wklejenia blok obiektu MonsterAnchors, na podstawie zapisanych anchorow.
 * Pola oznaczone jako globalne (patrz [globalFields]) dostaja JEDNA wspolna stala nad tabelka
 * gatunkow (np. `explosionAnchorGlobal`), a kazdy CombatAnchorSet odwoluje sie do niej po nazwie
 * zamiast powtarzac ten sam TransformOrigin szesc razy - jasno widac w kodzie, ze to celowo jedna
 * wartosc, a nie szesc przypadkiem identycznych.
 */
private fun buildMonsterAnchorsCode(
    species: List<MonsterSpeciesEntry>,
    fields: List<MonsterAnchorField>,
    savedOrigins: Map<String, Offset>,
    globalFields: Map<String, Boolean>
): String {
    val globalConstants = fields
        .filter { globalFields[it.id] == true }
        .mapNotNull { field ->
            val origin = savedOrigins["GLOBAL:${field.id}"] ?: return@mapNotNull null
            "val ${field.id}Global = TransformOrigin(${origin.x.toCodeValue()}, ${origin.y.toCodeValue()})"
        }

    val table = species.joinToString(separator = "\n\n") { s ->
        val body = fields.joinToString(separator = "\n") { field ->
            val isGlobal = globalFields[field.id] == true
            val value = if (isGlobal) {
                if (savedOrigins.containsKey("GLOBAL:${field.id}")) {
                    "${field.id}Global"
                } else {
                    "${field.id}Global // TODO: nie zapisano jeszcze"
                }
            } else {
                val origin = savedOrigins[anchorKey(s, field, isGlobal = false)]
                if (origin != null) {
                    "TransformOrigin(${origin.x.toCodeValue()}, ${origin.y.toCodeValue()})"
                } else {
                    "TransformOrigin(0.5f, 0.5f) // TODO: nie zapisano jeszcze"
                }
            }
            "        ${field.id} = $value,"
        }
        "    val ${s.fieldName} = CombatAnchorSet(\n$body\n    )"
    }

    return if (globalConstants.isEmpty()) table else globalConstants.joinToString("\n") + "\n\n" + table
}

private fun Float.toDisplayValue(): String {
    return ((this * 100f).roundToInt() / 100f).toString()
}

private fun Float.toCodeValue(): String {
    return "${((this * 1000f).roundToInt() / 1000f)}f"
}
