package com.lernhero.game.battle.effects

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.lernhero.domain.combat.playback.HitKind
import com.lernhero.game.spritesheet.SpriteSheetSpec
import com.lernhero.shared.resources.Resources
import org.jetbrains.compose.resources.DrawableResource

/** Jak sie porusza jeden efekt wizualny - trzy ksztalty ruchu wystarczaja na kazdy przyszly PNG. */
enum class EffectMotion {
    /** Pojawia sie w miejscu, robi krotki "pop" i gasnie (hit effect, spell na targecie). */
    STATIC_FADE,
    /** Leci po prostej od jednego anchoru do drugiego (pocisk atakujacy -> cel). */
    TRAVEL,
    /** Zapetlona ikonka nad postacia, dopoki status jest aktywny (poison/buff/debuff). */
    LOOP_STATUS
}

/**
 * Definicja jednego efektu: PNG (albo spritesheet - patrz [spriteSheet]) + jak sie rusza + jak
 * dlugo. Nowy efekt w grze = nowy wpis w [EffectCatalog] (+ nowy PNG w drawable) - zero nowego
 * kodu Compose potrzebnego per efekt.
 *
 * [spriteSheet] != null zamienia [drawable] w siatke klatek zamiast pojedynczego PNG - klatka
 * jest liczona wprost z postepu animacji (patrz [com.lernhero.game.spritesheet.frameForProgress]),
 * wiec dluzszy [durationMs] po prostu odtwarza te same klatki wolniej.
 *
 * [followUpAtTarget] pozwala spiac dwa efekty w jeden lancuch - np. pocisk (TRAVEL) po dolocie do
 * celu odpala wybuch (STATIC_FADE) dokladnie w miejscu, w ktore trafil - bez pisania osobnej
 * logiki sekwencji, [EffectsController] robi to samo automatycznie za kazdym razem.
 */
data class EffectSpec(
    val id: String,
    val drawable: DrawableResource,
    val motion: EffectMotion,
    val durationMs: Int,
    val sizeDp: Dp = 64.dp,
    /** Wysokosc, jesli inna niz [sizeDp] (klatki nie musza byc kwadratowe, np. plaski wybuch). */
    val heightDp: Dp = sizeDp,
    val spriteSheet: SpriteSheetSpec? = null,
    val followUpAtTarget: EffectSpec? = null,
    /**
     * Poprawka na asymetryczne dopelnienie w samym PNG - ulamek (x,y) [sizeDp]/[heightDp], o jaki
     * WIDOCZNA tresc klatki jest przesunieta od geometrycznego srodka ramki. Dodatnie = tresc
     * siedzi nizej/bardziej w prawo niz srodek. Bez tego pozycjonowanie centruje NIEWIDOCZNY,
     * wyposrodkowany box klatki na anchorze - jesli sam rysunek w klatce nie jest wysrodkowany
     * (typowe przy eksporcie ze spritesheet-generatora), efekt "ucieka" od anchoru, a przy
     * skalowaniu ten blad rosnie proporcjonalnie (patrz magic_bomb - zmierzone ~19% wysokosci w dol).
     */
    val contentOffset: Offset = Offset.Zero
)

/**
 * Katalog efektow dostepnych w grze, kluczowany po [HitKind] na razie (rozszerzalny pozniej o
 * osobne klucze dla spelli/statusow, gdy dojda).
 */
object EffectCatalog {
    private val impactBomb = EffectSpec(
        id = "magic_bomb",
        drawable = Resources.EffectArt.magicBomb,
        motion = EffectMotion.STATIC_FADE,
        durationMs = 480,
        // Wytunowane w Monster Anchor Lab po naprawie mirrorowania anchorow (patrz BattleField.kt).
        sizeDp = 303.dp,
        heightDp = 170.dp,
        spriteSheet = SpriteSheetSpec(
            drawable = Resources.EffectArt.magicBomb,
            frameCount = 8,
            columns = 4
        ),
        // Zmierzone z bounding boxa alpha kanalu kazdej z 8 klatek: tresc siedzi srednio ~19%
        // wysokosci klatki PONIZEJ geometrycznego srodka (39px w pierwszej klatce do 19px w
        // ostatniej, na 144px wysokosci klatki) - stad ta stala korekta w dol.
        contentOffset = Offset(0f, 0.19f)
    )

    private val mageMissile = EffectSpec(
        id = "mage_missile",
        drawable = Resources.EffectArt.mageMissile,
        motion = EffectMotion.TRAVEL,
        durationMs = 260,
        sizeDp = 48.dp,
        followUpAtTarget = impactBomb
    )

    val byHitKind: Map<HitKind, EffectSpec> = HitKind.entries.associateWith { mageMissile }

    /**
     * Lista do podgladu w "Monster Anchor Lab" (feature/home/.../dev/MonsterAnchorLabScreen.kt) -
     * pozwala odpalic dowolny efekt w miejscu wybranego anchoru, zeby zobaczyc na oko, gdzie
     * faktycznie ladowalby wybuch/pocisk/etc., zamiast zgadywac z samej kropki na podgladzie.
     */
    val previewCatalog: List<EffectSpec> = listOf(impactBomb, mageMissile)
}
