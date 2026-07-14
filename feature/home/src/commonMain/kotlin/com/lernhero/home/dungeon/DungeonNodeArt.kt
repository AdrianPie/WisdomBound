package com.lernhero.home.dungeon

import androidx.compose.ui.graphics.Color
import com.lernhero.domain.dungeon.DungeonNodeType
import com.lernhero.shared.resources.Resources
import org.jetbrains.compose.resources.DrawableResource

/**
 * Jedyne miejsce, gdzie typ pola spotyka sie z grafika.
 *
 * Teraz kazdy typ uzywa ikonki-zastepczej z [Resources.Icon]. Kiedy wrzucisz wlasne obrazki
 * (enemy.png, elite.png, shop.png, ...) do shared/composeResources/drawable, podmieniasz tylko
 * pole [icon] ponizej - reszta mapy niczego nie zauwazy.
 */
data class DungeonNodeArt(
    val icon: DrawableResource,
    val tint: Color,
    val label: String
)

/** Kolory ramek pol - czytelne na ciemnym tle mapy. */
private val EnemyRed = Color(0xFFC7532F)
private val EliteCrimson = Color(0xFF9B2C3D)
private val EventPurple = Color(0xFF8A5FD0)
private val ShopGreen = Color(0xFF3FA96A)
private val RestOrange = Color(0xFFE0913A)
private val TreasureGold = Color(0xFFD9B44A)
private val BossPurple = Color(0xFFB03060)

fun DungeonNodeType.art(): DungeonNodeArt = when (this) {
    // TODO: podmien na wlasne grafiki, gdy tylko je narysujesz.
    DungeonNodeType.Enemy -> DungeonNodeArt(Resources.Icon.swords, EnemyRed, "Walka")
    DungeonNodeType.Elite -> DungeonNodeArt(Resources.Icon.swords, EliteCrimson, "Elita")
    DungeonNodeType.Event -> DungeonNodeArt(Resources.Icon.Book, EventPurple, "Zdarzenie")
    DungeonNodeType.Shop -> DungeonNodeArt(Resources.Icon.ShoppingCart, ShopGreen, "Sklep")
    DungeonNodeType.Rest -> DungeonNodeArt(Resources.Icon.Home, RestOrange, "Ognisko")
    DungeonNodeType.Treasure -> DungeonNodeArt(Resources.Icon.Dollar, TreasureGold, "Skarb")
    DungeonNodeType.Boss -> DungeonNodeArt(Resources.Icon.Warning, BossPurple, "Boss")
}

/** Pola, ktore prowadza wprost do walki - reszta na razie tylko odhacza sie na mapie. */
val DungeonNodeType.isCombat: Boolean
    get() = this == DungeonNodeType.Enemy ||
        this == DungeonNodeType.Elite ||
        this == DungeonNodeType.Boss
