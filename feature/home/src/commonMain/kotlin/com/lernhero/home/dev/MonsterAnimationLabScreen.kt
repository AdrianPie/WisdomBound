package com.lernhero.home.dev

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lernhero.game.battle.effects.EffectCatalog
import com.lernhero.game.battle.effects.LayeredEffectCatalog
import com.lernhero.game.character.AnimSlot
import com.lernhero.game.character.ClipEasing
import com.lernhero.game.character.ClipEffect
import com.lernhero.game.character.Keyframe
import com.lernhero.game.character.MonsterAnimationClip
import com.lernhero.game.character.MonsterRig
import com.lernhero.game.character.MonsterRigs
import com.lernhero.game.character.MonsterVisualPresets
import com.lernhero.game.character.PartPose
import com.lernhero.game.character.PartTrack
import com.lernhero.game.character.RiggedMonster
import com.lernhero.game.character.poseAt
import com.lernhero.shared.theme.FontFirst
import kotlin.math.abs
import kotlin.math.roundToInt

/**
 * "Dev Animation Center" - lab do animowania potworow, zaprojektowany pod jeden ekran i dwa kciuki.
 *
 * Trzy zasady, na ktorych stoi caly uklad:
 *
 *  1. **Jedna lista rzeczy do animowania.** Czesci rigu i efekty siedza w TEJ SAMEJ osi czasu,
 *     kazde jako wlasny wiersz. Zaznaczasz wiersz - panel na dole pokazuje sterowanie akurat tego
 *     obiektu. Nie ma osobnego "trybu czesci" i "trybu efektow", miedzy ktorymi trzeba skakac.
 *  2. **Auto-keyframe.** Kazdy suwak dziala od razu i sam zaklada albo nadpisuje klatke pod glowica
 *     czasu. Nigdy nie trzeba "najpierw dodac klatki".
 *  3. **Animacje maja nazwy.** [AnimSlot] (SPOCZ/ATAK/CIOS/RZUT/SMIERC) - bo walka bedzie prosic
 *     o "atak tego potwora", a nie o anonimowy zestaw klatek. Kazde gniazdo to osobna animacja.
 *
 * Jak w [MonsterAnchorLabScreen]: nic nie trafia na dysk samo z siebie - `onBack` niszczy stan,
 * wiec jedynym trwalym wynikiem jest kod skopiowany przyciskiem ZAPISZ.
 */
@Composable
fun MonsterAnimationLabScreen(onBack: () -> Unit) {
    val clipboard = LocalClipboardManager.current
    val speciesList = remember { animatableSpecies() }

    var species by remember { mutableStateOf(speciesList.first()) }
    var slot by remember { mutableStateOf(AnimSlot.Attack) }
    val baseRig = remember(species) { MonsterRigs.forSpecies(species) }
    val preset = remember(species) { MonsterVisualPresets.forSpecies(species) }

    // Pivoty i kolejnosc warstw naleza do RIGU - sa wspolne dla wszystkich gniazd animacji.
    val pivots = remember { mutableStateMapOf<String, TransformOrigin>() }
    val layerOrder = remember { mutableStateMapOf<String, MutableList<String>>() }
    // Klatki i efekty naleza do KONKRETNEGO gniazda - "atak" ma inne niz "spocz".
    val tracks = remember { mutableStateMapOf<String, MutableList<Keyframe>>() }
    val effectsBySlot = remember { mutableStateMapOf<String, MutableList<ClipEffect>>() }
    val durations = remember { mutableStateMapOf<String, Int>() }

    var selected by remember(species, slot) { mutableStateOf<Sel>(Sel.Part(baseRig.parts.first().id)) }
    var playhead by remember { mutableStateOf(0f) }
    var selectedLayer by remember { mutableStateOf("") }
    var pivotMode by remember { mutableStateOf(false) }
    var zoom3x by remember { mutableStateOf(false) }
    var playing by remember { mutableStateOf(false) }
    var loop by remember { mutableStateOf(true) }
    var note by remember { mutableStateOf<String?>(null) }

    val slotKey = "$species/${slot.id}"
    fun pivotKey(partId: String) = "$species/$partId"
    fun frameKey(partId: String) = "$slotKey/$partId"
    fun framesOf(partId: String) = tracks.getOrPut(frameKey(partId)) { mutableStateListOf() }
    val effects = effectsBySlot.getOrPut(slotKey) { mutableStateListOf() }
    val durationMs = durations[slotKey] ?: 1000
    val order = layerOrder.getOrPut(species) { baseRig.parts.map { it.id }.toMutableStateList() }

    val rig = MonsterRig(
        order.mapNotNull { id -> baseRig.part(id)?.let { p -> p.copy(pivot = pivots[pivotKey(id)] ?: p.pivot) } }
    )

    // Bez remember - listy sa mutowane w miejscu, wiec kazdy klucz oparty na nich bylby "rowny"
    // poprzedniemu i clip nigdy by sie nie odswiezyl.
    val clip = MonsterAnimationClip(
        id = "${species}_${slot.id}",
        speciesKey = species,
        durationMs = durationMs,
        tracks = baseRig.parts.mapNotNull { p ->
            val f = tracks[frameKey(p.id)].orEmpty().toList()
            if (f.isEmpty()) null else PartTrack(p.id, f.sortedBy { it.at })
        },
        effects = effects.toList()
    )

    fun poseNow(partId: String): PartPose {
        val f = framesOf(partId)
        return if (f.isEmpty()) PartPose() else PartTrack(partId, f.sortedBy { it.at }).poseAt(playhead)
    }

    /** Auto-keyframe czesci: nadpisz klatke pod glowica albo zaloz nowa od biezacej pozy. */
    fun writeKey(partId: String, change: (Keyframe) -> Keyframe) {
        playing = false
        val f = framesOf(partId)
        val i = f.indexOfFirst { abs(it.at - playhead) < 0.004f }
        if (i >= 0) {
            f[i] = change(f[i])
        } else {
            val p = poseNow(partId)
            f.add(change(Keyframe(playhead, p.rotationDeg, p.offsetX, p.offsetY, p.scale, p.alpha)))
            f.sortBy { it.at }
        }
    }

    val clock = remember { Animatable(0f) }
    LaunchedEffect(playing, durationMs, loop, slotKey) {
        if (!playing) return@LaunchedEffect
        do {
            clock.snapTo(0f)
            clock.animateTo(1f, tween(durationMs, easing = LinearEasing)) { playhead = value }
        } while (loop && playing)
        playing = false
    }

    Column(
        modifier = Modifier.fillMaxSize().background(Ink).statusBarsPadding().padding(horizontal = 8.dp, vertical = 4.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        // ============================================================ 1. CO edytujemy
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(
                modifier = Modifier.weight(1f).horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                speciesList.forEach { k -> Chip(shortName(k), k == species) { species = k } }
            }
            Chip("X", false, tone = Red, onClick = onBack)
        }
        Row(
            modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            AnimSlot.entries.forEach { sl ->
                val n = baseRig.parts.sumOf { tracks["$species/${sl.id}/${it.id}"].orEmpty().size }
                Chip(if (n > 0) "${sl.label}·$n" else sl.label, sl == slot, tone = Green) { slot = sl }
            }
        }

        // ============================================================ 2. PODGLAD
        var previewPx by remember { mutableStateOf(0f) }
        val pivotPart = (selected as? Sel.Part)?.partId?.takeIf { pivotMode }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .clip(RoundedCornerShape(8.dp))
                .background(Panel)
                .border(1.dp, if (pivotMode) Green else Frame, RoundedCornerShape(8.dp)),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .onSizeChanged { previewPx = minOf(it.width, it.height).toFloat() }
                    .pointerInput(pivotMode, selected, zoom3x, previewPx, species) {
                        if (zoom3x || previewPx <= 0f) return@pointerInput
                        detectTapGestures { off ->
                            val left = (size.width - previewPx) / 2f
                            val top = (size.height - previewPx) / 2f
                            val fx = ((off.x - left) / previewPx).coerceIn(0f, 1f)
                            val fy = ((off.y - top) / previewPx).coerceIn(0f, 1f)
                            when (val sel = selected) {
                                is Sel.Part -> if (pivotMode) {
                                    pivots[pivotKey(sel.partId)] = TransformOrigin(fx, fy)
                                }
                                is Sel.Effect -> if (sel.index in effects.indices) {
                                    effects[sel.index] = effects[sel.index].copy(anchorX = fx, anchorY = fy)
                                }
                            }
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                val sizeDp = with(LocalDensity.current) { previewPx.toDp() }
                Box(
                    modifier = Modifier.graphicsLayer {
                        scaleX = if (zoom3x) 3f else 1f
                        scaleY = if (zoom3x) 3f else 1f
                    },
                    contentAlignment = Alignment.Center
                ) {
                    if (sizeDp > 0.dp) {
                        RiggedMonster(
                            rig = rig,
                            preset = preset,
                            clip = clip,
                            progress = playhead,
                            sizeDp = sizeDp / preset.visualScale,
                            debugPivotPartId = pivotPart
                        )
                    }
                }
            }
        }

        // ============================================================ 3. TRANSPORT
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(3.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Chip(if (playing) "❚❚" else "▶", playing, tone = Green) { playing = !playing }
            Chip("⏮", false) { playing = false; playhead = 0f }
            Chip("↻", loop) { loop = !loop }
            Chip("3x", zoom3x) { zoom3x = !zoom3x }
            Text(
                "${(playhead * durationMs).roundToInt()} / $durationMs ms",
                color = Gold, fontFamily = FontFirst(), fontSize = 9.sp,
                modifier = Modifier.padding(start = 4.dp)
            )
        }
        LabSlider("Dlugosc", durationMs.toFloat(), 200f, 8000f, "ms") { v ->
            durations[slotKey] = (v / 100f).roundToInt() * 100
        }

        // ============================================================ 4. OS CZASU (wszystkie sciezki)
        MultiTrackTimeline(
            rows = rig.parts.map { p ->
                TrackRow(
                    label = p.label,
                    marks = framesOf(p.id).map { it.at },
                    span = null,
                    selected = selected == Sel.Part(p.id),
                    onSelect = { selected = Sel.Part(p.id) }
                )
            } + effects.mapIndexed { i, e ->
                TrackRow(
                    label = "✦ " + shortName(e.effectKey),
                    marks = e.keyframes.map { k -> e.from + k.at * (e.to - e.from) },
                    span = e.from to e.to,
                    selected = selected == Sel.Effect(i),
                    onSelect = { selected = Sel.Effect(i); selectedLayer = "" }
                )
            },
            playhead = playhead,
            onScrub = { playing = false; playhead = it }
        )

        // ============================================================ 5. STEROWANIE ZAZNACZONEGO
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(6.dp))
                .background(Panel)
                .padding(horizontal = 6.dp, vertical = 4.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            when (val sel = selected) {
                is Sel.Part -> {
                    val pose = poseNow(sel.partId)
                    val frames = framesOf(sel.partId)
                    val here = frames.firstOrNull { abs(it.at - playhead) < 0.004f }

                    Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                        Chip("PIVOT", pivotMode, tone = Green) { pivotMode = !pivotMode }
                        Chip("nizej", false) {
                            val i = order.indexOf(sel.partId)
                            if (i > 0) { val t = order[i - 1]; order[i - 1] = order[i]; order[i] = t }
                        }
                        Chip("wyzej", false) {
                            val i = order.indexOf(sel.partId)
                            if (i in 0 until order.size - 1) { val t = order[i + 1]; order[i + 1] = order[i]; order[i] = t }
                        }
                        if (here != null) {
                            Chip("usun kl.", false, tone = Red) { frames.remove(here) }
                        }
                        Chip("wyczysc", false, tone = Red) { frames.clear() }
                    }

                    if (pivotMode) {
                        Hint("Kliknij w podglad = zawias tej czesci.")
                        rig.part(sel.partId)?.let { p ->
                            LabSlider("Pivot X", p.pivot.pivotFractionX, 0f, 1f) { v ->
                                pivots[pivotKey(p.id)] = TransformOrigin(v, p.pivot.pivotFractionY)
                            }
                            LabSlider("Pivot Y", p.pivot.pivotFractionY, 0f, 1f) { v ->
                                pivots[pivotKey(p.id)] = TransformOrigin(p.pivot.pivotFractionX, v)
                            }
                        }
                    } else {
                        LabSlider("Obrot", pose.rotationDeg, -180f, 180f, "°") { v ->
                            writeKey(sel.partId) { it.copy(rotationDeg = v) }
                        }
                        LabSlider("Poz X", pose.offsetX, -0.5f, 0.5f) { v ->
                            writeKey(sel.partId) { it.copy(offsetX = v) }
                        }
                        LabSlider("Poz Y", pose.offsetY, -0.5f, 0.5f) { v ->
                            writeKey(sel.partId) { it.copy(offsetY = v) }
                        }
                        LabSlider("Skala", pose.scale, 0.2f, 2.5f) { v ->
                            writeKey(sel.partId) { it.copy(scale = v) }
                        }
                        LabSlider("Krycie", pose.alpha, 0f, 1f) { v ->
                            writeKey(sel.partId) { it.copy(alpha = v) }
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                            ClipEasing.entries.forEach { e ->
                                Chip(e.label.take(4), here?.easing == e) {
                                    writeKey(sel.partId) { it.copy(easing = e) }
                                }
                            }
                        }
                    }
                }

                is Sel.Effect -> {
                    if (sel.index !in effects.indices) {
                        Hint("Efekt usuniety.")
                    } else {
                        val e = effects[sel.index]
                        fun upd(c: (ClipEffect) -> ClipEffect) { effects[sel.index] = c(effects[sel.index]) }
                        val layered = LayeredEffectCatalog.byId(e.effectKey)
                        val local = if (e.to > e.from) ((playhead - e.from) / (e.to - e.from)).coerceIn(0f, 1f) else 0f
                        val inWindow = playhead in e.from..e.to
                        val ePose = e.poseAt(local)

                        Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                            Chip("do okna", false) { playing = false; playhead = e.from }
                            Chip(e.anchorPartId ?: "canvas", e.anchorPartId != null) {
                                val fallback = (rig.parts.firstOrNull()?.id ?: "body")
                                upd { it.copy(anchorPartId = if (it.anchorPartId == null) fallback else null) }
                            }
                            Chip("usun", false, tone = Red) {
                                effects.removeAt(sel.index)
                                selected = Sel.Part(rig.parts.first().id)
                            }
                        }
                        LabSlider("Start", e.from, 0f, 1f, "") { v -> upd { it.copy(from = v.coerceAtMost(it.to)) } }
                        LabSlider("Koniec", e.to, 0f, 1f, "") { v -> upd { it.copy(to = v.coerceAtLeast(it.from)) } }
                        LabSlider("Baza", e.sizeFraction, 0.05f, 1.5f) { v -> upd { it.copy(sizeFraction = v) } }

                        // Przemalowanie efektu. "oryg" = kolory z PNG; reszta to jeden odcien.
                        // W walce zdarzenia bossa i tak nadpisuja to swoim kolorem (leczenie=zielony itd.).
                        Row(
                            modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(3.dp)
                        ) {
                            Chip("oryg", e.tintArgb == null) { upd { it.copy(tintArgb = null) } }
                            TINTS.forEach { (name, argb) ->
                                Chip(name, e.tintArgb == argb, tone = Color(argb)) {
                                    upd { it.copy(tintArgb = argb) }
                                }
                            }
                        }

                        if (inWindow) {
                            LabSlider("Skala@", ePose.scale, 0.02f, 3f) { v ->
                                playing = false
                                upd { it.withKey(local) { k -> k.copy(scale = v) } }
                            }
                            LabSlider("Krycie@", ePose.alpha, 0f, 1f) { v ->
                                playing = false
                                upd { it.withKey(local) { k -> k.copy(alpha = v) } }
                            }
                        } else {
                            Hint("Glowica poza oknem efektu - przesun ja, zeby edytowac skale w czasie.")
                        }

                        // Warstwy: chipy i suwak generowane z samego efektu, dla DOWOLNEJ ich liczby.
                        if (layered != null) {
                            val activeId = if (layered.layer(selectedLayer) != null) {
                                selectedLayer
                            } else {
                                layered.layers.first().id
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(3.dp)
                            ) {
                                layered.layers.forEach { l ->
                                    Chip(l.label, l.id == activeId) { selectedLayer = l.id }
                                }
                            }
                            val layer = layered.layer(activeId)!!
                            val spin = e.layerSpins[activeId] ?: layer.spinPerSecond
                            LabSlider("Obr/s", spin, -3f, 3f) { v ->
                                upd { it.copy(layerSpins = it.layerSpins + (activeId to v)) }
                            }
                        }
                    }
                }
            }
        }

        // ============================================================ 6. DODAJ / ZAPISZ
        Row(
            modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(3.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            LayeredEffectCatalog.all.forEach { spec ->
                Chip("+${spec.label}", false, tone = Green) {
                    effects.add(newEffect(spec.id, playhead, (selected as? Sel.Part)?.partId ?: rig.parts.first().id))
                    selected = Sel.Effect(effects.lastIndex)
                    selectedLayer = spec.layers.first().id
                }
            }
            EffectCatalog.previewCatalog.forEach { spec ->
                Chip("+${shortName(spec.id)}", false) {
                    effects.add(newEffect(spec.id, playhead, (selected as? Sel.Part)?.partId ?: rig.parts.first().id))
                    selected = Sel.Effect(effects.lastIndex)
                }
            }
            Chip("ZAPISZ", false, tone = Gold) {
                clipboard.setText(AnnotatedString(buildCode(species, slot, rig, clip)))
                note = "Skopiowano ${slot.label}: ${clip.tracks.sumOf { it.keyframes.size }} kl., ${clip.effects.size} ef."
            }
        }
        note?.let { Hint(it) }
    }
}

/** Co jest teraz zaznaczone na osi czasu - czesc rigu albo efekt. Jedna lista, jeden panel. */
private sealed interface Sel {
    data class Part(val partId: String) : Sel
    data class Effect(val index: Int) : Sel
}

private data class TrackRow(
    val label: String,
    /** Pozycje klatek kluczowych na GLOWNEJ osi 0..1. */
    val marks: List<Float>,
    /** Zakres istnienia (tylko efekty) - rysowany jako belka. */
    val span: Pair<Float, Float>?,
    val selected: Boolean,
    val onSelect: () -> Unit
)

/**
 * Os czasu ze WSZYSTKIMI sciezkami naraz - po jednym wierszu na czesc rigu i na efekt. To jest
 * glowna zmiana wzgledem poprzedniej wersji: widac cala animacje, a nie tylko jedna wybrana czesc,
 * i zaznacza sie obiekt klikajac wprost w jego wiersz.
 *
 * Dotkniecie/przeciagniecie w obszarze sciezki przewija glowice; dotkniecie etykiety zaznacza wiersz.
 */
@Composable
private fun MultiTrackTimeline(
    rows: List<TrackRow>,
    playhead: Float,
    onScrub: (Float) -> Unit
) {
    val scrub by rememberUpdatedState(onScrub)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(6.dp))
            .background(Panel)
            .border(1.dp, Frame, RoundedCornerShape(6.dp))
            .padding(vertical = 2.dp),
        verticalArrangement = Arrangement.spacedBy(1.dp)
    ) {
        rows.forEach { row ->
            var w by remember { mutableStateOf(0f) }
            Row(
                modifier = Modifier.fillMaxWidth().height(20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = row.label,
                    color = if (row.selected) Gold else Cream,
                    fontFamily = FontFirst(),
                    fontWeight = if (row.selected) FontWeight.Bold else FontWeight.Normal,
                    fontSize = 8.sp,
                    maxLines = 1,
                    modifier = Modifier
                        .width(74.dp)
                        .clickable(onClick = row.onSelect)
                        .padding(start = 4.dp)
                )
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxSize()
                        .onSizeChanged { w = it.width.toFloat() }
                        .pointerInput(Unit) {
                            detectTapGestures { o ->
                                row.onSelect()
                                if (w > 0f) scrub((o.x / w).coerceIn(0f, 1f))
                            }
                        }
                        .pointerInput(Unit) {
                            detectDragGestures { ch, _ -> if (w > 0f) scrub((ch.position.x / w).coerceIn(0f, 1f)) }
                        }
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val y = size.height / 2f
                        drawLine(
                            if (row.selected) Color(0xFF6B5A38) else Color(0xFF3A3226),
                            Offset(0f, y), Offset(size.width, y), strokeWidth = 2f
                        )
                        // Belka istnienia efektu - od razu widac, kiedy w ogole jest na ekranie.
                        row.span?.let { (from, to) ->
                            drawLine(
                                Color(0xFF2E7D5B),
                                Offset(from * size.width, y), Offset(to * size.width, y),
                                strokeWidth = 8f
                            )
                        }
                        row.marks.forEach { at ->
                            drawCircle(if (row.selected) Gold else Color(0xFF8A7448), 5f, Offset(at * size.width, y))
                        }
                        val hx = playhead * size.width
                        drawLine(Red, Offset(hx, 0f), Offset(hx, size.height), strokeWidth = 2f)
                    }
                }
            }
        }
    }
}

/**
 * Suwak w jednym waskim wierszu. [onChange] przechodzi przez [rememberUpdatedState] - BEZ tego
 * `pointerInput` zapamietuje lambde z pierwszej kompozycji i suwak do konca zycia pisze do obiektu,
 * ktory byl zaznaczony, gdy powstal (stad "nie da sie zmienic obrotu pierscienia 2 i 3").
 */
@Composable
private fun LabSlider(
    label: String,
    value: Float,
    min: Float,
    max: Float,
    unit: String = "",
    onChange: (Float) -> Unit
) {
    val change by rememberUpdatedState(onChange)
    var w by remember { mutableStateOf(0f) }

    Row(
        modifier = Modifier.fillMaxWidth().height(23.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(label, color = Cream, fontFamily = FontFirst(), fontSize = 8.sp, modifier = Modifier.width(46.dp))
        Box(
            modifier = Modifier
                .weight(1f)
                .height(20.dp)
                .onSizeChanged { w = it.width.toFloat() }
                .pointerInput(Unit) {
                    detectTapGestures { o -> if (w > 0f) change(min + (o.x / w).coerceIn(0f, 1f) * (max - min)) }
                }
                .pointerInput(Unit) {
                    detectDragGestures { ch, _ ->
                        if (w > 0f) change(min + (ch.position.x / w).coerceIn(0f, 1f) * (max - min))
                    }
                }
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val y = size.height / 2f
                val f = ((value - min) / (max - min)).coerceIn(0f, 1f)
                drawLine(Color(0xFF3A3226), Offset(0f, y), Offset(size.width, y), strokeWidth = 6f)
                drawLine(Gold, Offset(0f, y), Offset(f * size.width, y), strokeWidth = 6f)
                // Znacznik zera na suwakach dwustronnych - inaczej trudno trafic w "brak obrotu".
                if (min < 0f && max > 0f) {
                    val zx = ((0f - min) / (max - min)) * size.width
                    drawLine(Color(0xFF7A6A4A), Offset(zx, y - 6f), Offset(zx, y + 6f), strokeWidth = 2f)
                }
                drawCircle(Gold, 7f, Offset(f * size.width, y))
            }
        }
        Text(
            "${fmt(value)}$unit",
            color = Gold, fontFamily = FontFirst(), fontSize = 8.sp, maxLines = 1,
            modifier = Modifier.width(46.dp)
        )
    }
}

/** Nowy efekt w miejscu glowicy - domyslnie rosnacy od zera, bo tak uzywa sie go najczesciej. */
private fun newEffect(effectKey: String, playhead: Float, partId: String) = ClipEffect(
    effectKey = effectKey,
    from = playhead,
    to = (playhead + 0.4f).coerceAtMost(1f),
    anchorPartId = partId,
    sizeFraction = 0.35f,
    keyframes = listOf(
        Keyframe(at = 0f, scale = 0.1f, easing = ClipEasing.Linear),
        Keyframe(at = 1f, scale = 1f, easing = ClipEasing.Smooth)
    )
)

/** Auto-keyframe na wlasnej osi efektu - to samo zachowanie co writeKey dla czesci rigu. */
private fun ClipEffect.withKey(at: Float, change: (Keyframe) -> Keyframe): ClipEffect {
    val i = keyframes.indexOfFirst { abs(it.at - at) < 0.02f }
    val updated = keyframes.toMutableList()
    if (i >= 0) {
        updated[i] = change(updated[i])
    } else {
        val p = poseAt(at)
        updated.add(change(Keyframe(at = at, scale = p.scale, alpha = p.alpha, rotationDeg = p.rotationDeg)))
        updated.sortBy { it.at }
    }
    return copy(keyframes = updated)
}

private fun buildCode(species: String, slot: AnimSlot, rig: MonsterRig, clip: MonsterAnimationClip): String =
    buildString {
        appendLine("// ---- Dev Animation Center: $species / ${slot.label} ----")
        appendLine("// RIG - kolejnosc od spodu, pivoty:")
        rig.parts.forEach { p ->
            appendLine("//   ${p.id}  TransformOrigin(${fmt(p.pivot.pivotFractionX)}f, ${fmt(p.pivot.pivotFractionY)}f)")
        }
        appendLine()
        appendLine("val ${species.camel()}${slot.id.replaceFirstChar { it.uppercase() }} = MonsterAnimationClip(")
        appendLine("    id = \"${clip.id}\", speciesKey = \"${clip.speciesKey}\", durationMs = ${clip.durationMs},")
        appendLine("    tracks = listOf(")
        clip.tracks.forEach { t ->
            appendLine("        PartTrack(\"${t.partId}\", listOf(")
            t.keyframes.forEach { k ->
                appendLine(
                    "            Keyframe(at = ${fmt(k.at)}f, rotationDeg = ${fmt(k.rotationDeg)}f, " +
                        "offsetX = ${fmt(k.offsetX)}f, offsetY = ${fmt(k.offsetY)}f, scale = ${fmt(k.scale)}f, " +
                        "alpha = ${fmt(k.alpha)}f, easing = ClipEasing.${k.easing.name}),"
                )
            }
            appendLine("        )),")
        }
        appendLine("    ),")
        appendLine("    effects = listOf(")
        clip.effects.forEach { e ->
            appendLine("        ClipEffect(")
            appendLine("            effectKey = \"${e.effectKey}\", from = ${fmt(e.from)}f, to = ${fmt(e.to)}f,")
            appendLine(
                "            anchorPartId = ${e.anchorPartId?.let { "\"$it\"" } ?: "null"}, " +
                    "anchorX = ${fmt(e.anchorX)}f, anchorY = ${fmt(e.anchorY)}f, sizeFraction = ${fmt(e.sizeFraction)}f,"
            )
            appendLine("            keyframes = listOf(")
            e.keyframes.forEach { k ->
                appendLine(
                    "                Keyframe(at = ${fmt(k.at)}f, scale = ${fmt(k.scale)}f, alpha = ${fmt(k.alpha)}f, " +
                        "rotationDeg = ${fmt(k.rotationDeg)}f, easing = ClipEasing.${k.easing.name}),"
                )
            }
            appendLine("            ),")
            appendLine(
                "            layerSpins = mapOf(" +
                    e.layerSpins.entries.joinToString { "\"${it.key}\" to ${fmt(it.value)}f" } + "),"
            )
            appendLine(
                "            tintArgb = " + (e.tintArgb?.let { "0x" + it.toString(16).uppercase() } ?: "null")
            )
            appendLine("        ),")
        }
        appendLine("    )")
        appendLine(")")
    }

private fun String.camel(): String =
    split("_").mapIndexed { i, s -> if (i == 0) s else s.replaceFirstChar { c -> c.uppercase() } }.joinToString("")

private fun shortName(key: String): String =
    key.removePrefix("boss_").removePrefix("magic_").replace("_rings", "").replace('_', ' ')

/** Szybkie przemalowania efektu w labie - te same odcienie, ktorych walka uzywa dla zdarzen bossa. */
private val TINTS: List<Pair<String, Long>> = listOf(
    "ziel" to 0xFF4ADE80,
    "fiol" to 0xFF9B6BD6,
    "zolt" to 0xFFFFD34D,
    "czer" to 0xFFE0654F,
    "nieb" to 0xFF3B9DFF,
    "bial" to 0xFFF4E9D7
)

private fun animatableSpecies(): List<String> = listOf(
    "boss_skeleton_dragon", "skeleton_warrior", "skeleton_archer",
    "skeleton_mage", "orc_warrior", "orc_archer", "human_archer"
)

private fun fmt(v: Float): String = ((v * 100f).roundToInt() / 100f).toString()

private val Ink = Color(0xFF14100C)
private val Panel = Color(0xFF1E1811)
private val Frame = Color(0xFF6B5A38)
private val Gold = Color(0xFFFFD34D)
private val Red = Color(0xFFD1453D)
private val Green = Color(0xFF22A55E)
private val Muted = Color(0xFF9D8B6A)
private val Cream = Color(0xFFF4E9D7)

@Composable
private fun Hint(text: String) {
    Text(text, color = Muted, fontFamily = FontFirst(), fontSize = 8.sp)
}

@Composable
private fun Chip(label: String, selected: Boolean, tone: Color = Gold, onClick: () -> Unit) {
    Text(
        text = label,
        color = if (selected) Color(0xFF120E0A) else Cream,
        fontFamily = FontFirst(),
        fontWeight = FontWeight.Bold,
        fontSize = 9.sp,
        maxLines = 1,
        modifier = Modifier
            .clip(RoundedCornerShape(5.dp))
            .background(if (selected) tone else Color(0xFF2A2218))
            .border(1.dp, if (selected) tone else Frame, RoundedCornerShape(5.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 7.dp, vertical = 5.dp)
    )
}
