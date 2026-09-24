package com.lernhero.home.story

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.lernhero.domain.story.StoryWorld
import com.lernhero.shared.resources.Resources
import org.jetbrains.compose.resources.DrawableResource

/**
 * Grafika krainy. [background] = null oznacza "PNG jeszcze nie ma" - wtedy karta krainy rysuje
 * sie jako gradient w [tint] plus nazwa oczekiwanego pliku, dokladnie tak jak przy zdarzeniach
 * lochu (patrz dawne CorridorEventArt - ten sam pomysl).
 *
 * Dorzucenie mapy: wrzuc PNG do shared/composeResources/drawable, dopisz w Resources.Image i
 * podmien [background] na wlasciwy zasob. Nic innego sie nie zmienia.
 */
data class StoryWorldArt(
    val background: DrawableResource?,
    val emoji: String,
    val tint: Color,
    val expectedFile: String
)

fun StoryWorld.art(): StoryWorldArt {
    // Podglad pokazuje ARENE tej krainy, a dopiero gdy jej nie ma - stara ilustracje krainy.
    //
    // Od kiedy walka ma wlasny zestaw teł ([Resources.Image.battleBackground]), te dwa zrodla
    // zaczely mowic co innego: panel kampanii rysowal stara grafike, a bitwa nowa arene. Gracz
    // widzial wiec "stara mape w niektorych miejscach", mimo ze nic nie bylo zepsute - po prostu
    // dwa ekrany czytaly dwa rozne pliki o tej samej krainie.
    val bg = Resources.Image.battleBackground(id) ?: Resources.Image.worldBackground(id)
    val (emoji, tint) = when (id) {
        "forest" -> "🌲" to Color(0xFF3FA96A)
        "mine" -> "⛏" to Color(0xFF9B2C3D)
        "icefield" -> "❄" to Color(0xFF4E8AB8)
        "heat" -> "🌋" to Color(0xFFC7532F)
        "heaven" -> "☁" to Color(0xFF8A5FD0)
        "abyss" -> "🕳" to Color(0xFF6B5A38)
        else -> "🗺" to Color(0xFF6B5A38)
    }
    // Nazwa pliku, ktorego brakuje - te trzy krainy nie maja jeszcze wlasnej areny i dlatego
    // wchodzi pod nie generyczne pole bitwy.
    return StoryWorldArt(bg, emoji, tint, "${id}_new.png")
}

fun StoryWorldArt.gradient(): Brush = Brush.verticalGradient(
    listOf(tint.copy(alpha = 0.55f), Color(0xFF0B0805))
)
