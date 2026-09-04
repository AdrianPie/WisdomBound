package com.lernhero.game.character

import androidx.compose.ui.graphics.TransformOrigin
import com.lernhero.game.battle.effects.EffectCatalog
import com.lernhero.game.spritesheet.SpriteSheetSpec
import com.lernhero.shared.resources.Resources
import org.jetbrains.compose.resources.DrawableResource

/**
 * Rig potwora: lista czesci (PNG) rysowanych jedna na drugiej, kazda z wlasnym pivotem obrotu.
 * To odpowiednik rigu rycerza ([ModularKnight] + [Resources.KnightPivots]), tylko opisany DANYMI,
 * a nie zaszyty w composable - dzieki temu ten sam kod obsluguje i plaskiego szkieleta, i bossa
 * z rucham szczeka, i cokolwiek dojdzie pozniej.
 *
 * KLUCZOWE: potwor z JEDNA czescia to dokladnie dzisiejszy plaski sprite - nic sie dla niego nie
 * zmienia. Zeby cokolwiek moglo sie ruszac osobno (szczeka, skrzydlo, ogon), ta czesc musi byc
 * WYCIETA DO OSOBNEGO PNG. Nie da sie obrocic fragmentu plaskiej bitmapy - trzeba miec ja jako
 * osobna warstwe, tak samo jak rycerz ma osobny plik na kazda konczyne.
 *
 * Wszystkie czesci rysowane sa w tym samym pudelku o tym samym rozmiarze, wiec ich PNG powinny
 * miec TEN SAM canvas co czesc bazowa (ta sama szerokosc/wysokosc, przezroczyste tlo) - wtedy
 * kazda siedzi sama z siebie na wlasciwym miejscu i nie trzeba jej recznie przesuwac.
 */
data class MonsterRig(
    val parts: List<MonsterPart>
) {
    /** Plaski sprite = rig z jedna czescia. Wygodne sprawdzenie dla UI i sciezki renderowania. */
    val isFlat: Boolean get() = parts.size == 1

    fun part(id: String): MonsterPart? = parts.firstOrNull { it.id == id }
}

/**
 * Jedna warstwa rigu. [pivot] to punkt, wokol ktorego ta czesc sie obraca, we wspolrzednych 0..1
 * CALEGO canvasu PNG (tak samo jak [Resources.KnightPivots] i [com.lernhero.shared.resources.CombatAnchorSet]) -
 * np. zawias szczeki smoka. Kolejnosc na liscie [MonsterRig.parts] = kolejnosc rysowania: pierwsza
 * na spodzie, ostatnia na wierzchu.
 */
data class MonsterPart(
    val id: String,
    /** Nazwa pokazywana w labie - po polsku, zeby bylo widac co sie edytuje. */
    val label: String,
    val drawable: DrawableResource,
    val pivot: TransformOrigin = TransformOrigin(0.5f, 0.5f)
)

/**
 * Rigi wszystkich gatunkow. Dorzucenie ruchomej czesci to JEDNA linijka tutaj - wystarczy wyciac
 * ja do osobnego PNG (ten sam canvas co bazowy plik, reszta przezroczysta), zarejestrowac
 * w [Resources.MonsterArt] i dopisac kolejny [MonsterPart].
 */
object MonsterRigs {

    /** Plaski rig - jedna czesc na caly PNG. Tak wyglada dzis kazdy potwor. */
    private fun flat(key: String, drawable: DrawableResource): MonsterRig = MonsterRig(
        listOf(
            MonsterPart(
                id = "body",
                label = "Cale cialo",
                drawable = drawable,
                // Plaska sylwetka koleblie sie wokol ziemi pod stopami, jak rycerz (KnightPivots.ground).
                pivot = TransformOrigin(0.5f, 0.92f)
            )
        )
    )

    /**
     * Smok bossa - 3 warstwy (ten sam canvas 768x768). Kolejnosc = kolejnosc rysowania:
     *  - cialo (kregoslup, nogi, ogon, skrzydla) na spodzie,
     *  - dolna szczeka nad nim - to ONA sie rusza (obrot wokol zawiasu przy karku),
     *  - gorna czaszka na wierzchu, zeby jej zeby zaslanialy szczeke przy zamknietym pysku.
     *
     * Kolejnosc da sie przestawic w Dev Animation Center (zakladka PIVOT -> nizej/wyzej).
     *
     * Pivoty to punkty startowe zmierzone z bounding boxow alpha - dostrajasz je klikniciem
     * w Dev Animation Center. Zawias szczeki siedzi z tylu (od strony karku), bo tam sie lamie.
     */
    private val skeletonDragon: MonsterRig = MonsterRig(
        listOf(
            MonsterPart(
                id = "body",
                label = "Cialo",
                drawable = Resources.MonsterArt.bossSkeletonDragonBody,
                pivot = TransformOrigin(0.5f, 0.84f)
            ),
            MonsterPart(
                id = "skull_down",
                label = "Szczeka (dol)",
                drawable = Resources.MonsterArt.bossSkeletonDragonSkullDown,
                // Zawias szczeki - ustawiony w Dev Animation Center na realnym stawie zuchwy.
                pivot = TransformOrigin(0.29f, 0.33f)
            ),
            MonsterPart(
                id = "skull_up",
                label = "Czaszka (gora)",
                drawable = Resources.MonsterArt.bossSkeletonDragonSkullUp,
                // Ten sam staw co szczeka - gora i dol rozwieraja sie wokol wspolnego zawiasu.
                pivot = TransformOrigin(0.3f, 0.32f)
            )
        )
    )

    fun forSpecies(key: String): MonsterRig = when (key) {
        "boss_skeleton_dragon" -> skeletonDragon
        else -> flat(key, MonsterVisualPresets.forSpecies(key).drawable)
    }
}

/**
 * Nazwane gniazdo animacji. Walka nie pyta "zagraj klatki 0..1", tylko "zagraj ATAK tego potwora" -
 * dzieki temu kazdy gatunek moze miec wlasne animacje pod te same zdarzenia, a kod walki nie wie
 * nic o klatkach kluczowych.
 */
enum class AnimSlot(val id: String, val label: String) {
    Idle("idle", "SPOCZ"),
    Attack("attack", "ATAK"),
    Hit("hit", "CIOS"),
    Cast("cast", "RZUT"),
    Death("death", "SMIERC")
}

// ---------------------------------------------------------------------------------------------
// Animacja
// ---------------------------------------------------------------------------------------------

/**
 * Jedna animacja potwora - to, co wychodzi z laba i co pozniej odtwarza walka. Czas jest opisany
 * ULAMKIEM 0..1, a nie milisekundami: dzieki temu ta sama animacja gra wolniej/szybciej razem
 * z [com.lernhero.game.battle.BattleSpeed] i nie trzeba jej przeliczac przy kazdej zmianie tempa.
 * [durationMs] to tylko domyslna dlugosc zaproponowana przez autora animacji.
 */
data class MonsterAnimationClip(
    val id: String,
    val speciesKey: String,
    val durationMs: Int,
    /** Po jednej sciezce na ruchoma czesc rigu. Czesci bez sciezki po prostu stoja. */
    val tracks: List<PartTrack> = emptyList(),
    /** Naklacane efekty (spritesheet/PNG) odpalane w trakcie animacji. */
    val effects: List<ClipEffect> = emptyList()
)

/** Sciezka jednej czesci: klatki kluczowe posortowane po [Keyframe.at]. */
data class PartTrack(
    val partId: String,
    val keyframes: List<Keyframe>
)

/**
 * Jedna klatka kluczowa. Przesuniecia i rozmiary sa UŁAMKAMI rozmiaru sprite'a, nie pikselami -
 * to jest cala sztuczka, dzieki ktorej animacja zrobiona na podgladzie 1x wyglada identycznie na
 * bossie powiekszonym 3x (patrz [com.lernhero.game.character.CharacterUiModel.battleScale]).
 */
data class Keyframe(
    /** Moment na osi czasu, 0f = poczatek animacji, 1f = koniec. */
    val at: Float,
    /** Obrot wokol [MonsterPart.pivot], w stopniach. Dodatni = zgodnie z ruchem wskazowek. */
    val rotationDeg: Float = 0f,
    /** Przesuniecie w ulamku szerokosci/wysokosci sprite'a. */
    val offsetX: Float = 0f,
    val offsetY: Float = 0f,
    val scale: Float = 1f,
    val alpha: Float = 1f,
    /** Jak dojsc do TEJ klatki z poprzedniej. */
    val easing: ClipEasing = ClipEasing.Smooth
)

enum class ClipEasing(val label: String) {
    Linear("Liniowo"),
    Smooth("Gladko"),
    Accelerate("Przyspiesza"),
    Decelerate("Zwalnia"),
    /** Bez przejscia - wartosc przeskakuje dopiero w momencie tej klatki. */
    Step("Skok")
}

/**
 * Efekt naklacany na animacje - np. rosnaca kula ognia w szczece. [sizeFraction] jest ulamkiem
 * rozmiaru sprite'a potwora, wiec efekt skaluje sie RAZEM z nim (boss 3x = kula 3x) i nie trzeba
 * go poprawiac po kazdej zmianie wielkosci potwora.
 *
 * [anchorPartId] przypina efekt do pivota danej czesci - kula ognia przypieta do szczeki jedzie
 * razem z jej obrotem. Null = przyklejona do calego sprite'a, w miejscu [anchorX]/[anchorY].
 */
data class ClipEffect(
    val effectKey: String,
    /** Kiedy efekt startuje i konczy sie, w ulamku dlugosci animacji. */
    val from: Float,
    val to: Float,
    val anchorPartId: String? = null,
    /** Miejsce efektu w ulamku canvasu sprite'a (0..1), gdy nie jest przypiety do czesci. */
    val anchorX: Float = 0.5f,
    val anchorY: Float = 0.5f,
    /** Rozmiar BAZOWY efektu jako ulamek szerokosci sprite'a potwora - mnozony przez skale z [keyframes]. */
    val sizeFraction: Float = 0.5f,
    /**
     * Wlasna os czasu efektu (skala/krycie/obrot), liczona w jego WLASNYM postepie 0..1 miedzy
     * [from] a [to] - stad "prawie niewidoczny na starcie, duzy na koncu" to po prostu dwie klatki:
     * scale 0.1 w 0f i scale 1.0 w 1f. Pusta lista = staly rozmiar [sizeFraction].
     *
     * Swiadomie uzywamy tego samego [Keyframe] co czesci rigu: ta sama interpolacja, te same
     * easingi, jeden zestaw pojec do nauczenia sie.
     */
    val keyframes: List<Keyframe> = emptyList(),
    /**
     * Predkosci obrotu poszczegolnych warstw efektu warstwowego (id warstwy -> obroty na sekunde),
     * nadpisujace te z [com.lernhero.game.battle.effects.LayeredEffectCatalog]. Puste = katalogowe.
     * Dziala dla DOWOLNEJ liczby warstw - klucze biora sie z samego efektu, nie z twardej listy.
     */
    val layerSpins: Map<String, Float> = emptyMap(),
    /**
     * Przemalowanie efektu na jeden odcien, ARGB jako 0xAARRGGBB (np. 0xFF4ADE80 = zielony).
     * null = oryginalne kolory PNG. Walka moze to nadpisac w locie (patrz
     * [com.lernhero.game.character.CharacterUiModel.animEffectTintArgb]) - np. zielony przy leczeniu.
     */
    val tintArgb: Long? = null
)

/** Skala/krycie/obrot efektu w danym momencie jego wlasnej osi czasu. */
fun ClipEffect.poseAt(localProgress: Float): PartPose =
    if (keyframes.isEmpty()) PartPose() else PartTrack(effectKey, keyframes).poseAt(localProgress)

/** Wartosci jednej czesci w danym momencie animacji - to, co [RiggedMonster] wklada w graphicsLayer. */
data class PartPose(
    val rotationDeg: Float = 0f,
    val offsetX: Float = 0f,
    val offsetY: Float = 0f,
    val scale: Float = 1f,
    val alpha: Float = 1f
)

private val NeutralPose = PartPose()

/**
 * Interpoluje sciezke w momencie [progress] (0..1). Przed pierwsza klatka trzyma pierwsza, po
 * ostatniej - ostatnia (bez zapetlania: o tym, czy animacja leci w kolko, decyduje wolajacy).
 */
fun PartTrack.poseAt(progress: Float): PartPose {
    if (keyframes.isEmpty()) return NeutralPose
    val t = progress.coerceIn(0f, 1f)

    val sorted = keyframes.sortedBy { it.at }
    val first = sorted.first()
    if (t <= first.at) return first.toPose()
    val last = sorted.last()
    if (t >= last.at) return last.toPose()

    // Para klatek otaczajaca ten moment - przy kilku klatkach liniowe szukanie jest tansze niz
    // cokolwiek sprytniejszego, a ta funkcja leci raz na klatke renderowania.
    for (index in 0 until sorted.size - 1) {
        val from = sorted[index]
        val to = sorted[index + 1]
        if (t < from.at || t > to.at) continue

        val span = (to.at - from.at)
        if (span <= 0f) return to.toPose()
        val raw = (t - from.at) / span
        val eased = to.easing.apply(raw)

        return PartPose(
            rotationDeg = lerp(from.rotationDeg, to.rotationDeg, eased),
            offsetX = lerp(from.offsetX, to.offsetX, eased),
            offsetY = lerp(from.offsetY, to.offsetY, eased),
            scale = lerp(from.scale, to.scale, eased),
            alpha = lerp(from.alpha, to.alpha, eased)
        )
    }
    return last.toPose()
}

private fun Keyframe.toPose(): PartPose =
    PartPose(rotationDeg = rotationDeg, offsetX = offsetX, offsetY = offsetY, scale = scale, alpha = alpha)

private fun lerp(from: Float, to: Float, fraction: Float): Float = from + (to - from) * fraction

private fun ClipEasing.apply(t: Float): Float = when (this) {
    ClipEasing.Linear -> t
    // Klasyczny smoothstep - startuje i konczy lagodnie, bez biblioteki easingow.
    ClipEasing.Smooth -> t * t * (3f - 2f * t)
    ClipEasing.Accelerate -> t * t
    ClipEasing.Decelerate -> 1f - (1f - t) * (1f - t)
    ClipEasing.Step -> if (t >= 1f) 1f else 0f
}

/** Efekty aktywne w danym momencie animacji, razem z ich wlasnym postepem 0..1. */
fun MonsterAnimationClip.activeEffectsAt(progress: Float): List<Pair<ClipEffect, Float>> =
    effects.mapNotNull { effect ->
        if (progress < effect.from || progress > effect.to) return@mapNotNull null
        val span = effect.to - effect.from
        val local = if (span <= 0f) 1f else ((progress - effect.from) / span).coerceIn(0f, 1f)
        effect to local
    }

/** Spritesheet/PNG efektu po kluczu z [EffectCatalog.previewCatalog] - lab i walka czytaja to samo. */
fun effectSheetFor(effectKey: String): SpriteSheetSpec? =
    EffectCatalog.previewCatalog.firstOrNull { it.id == effectKey }?.spriteSheet

fun effectDrawableFor(effectKey: String): DrawableResource? =
    EffectCatalog.previewCatalog.firstOrNull { it.id == effectKey }?.drawable
