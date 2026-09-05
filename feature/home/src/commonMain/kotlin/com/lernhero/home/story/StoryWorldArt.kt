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
    // Tlo bierze sie z jednego wspolnego miejsca ([Resources.Image.worldBackground]) - tego samego,
    // ktorego uzywa tlo walki - zeby podglad krainy i sama bitwa nigdy sie nie rozjechaly.
    val bg = Resources.Image.worldBackground(id)
    val (emoji, tint) = when (id) {
        "forest" -> "🌲" to Color(0xFF3FA96A)
        "mine" -> "⛏" to Color(0xFF9B2C3D)
        "icefield" -> "❄" to Color(0xFF4E8AB8)
        "heat" -> "🌋" to Color(0xFFC7532F)
        "heaven" -> "☁" to Color(0xFF8A5FD0)
        "abyss" -> "🕳" to Color(0xFF6B5A38)
        else -> "🗺" to Color(0xFF6B5A38)
    }
    return StoryWorldArt(bg, emoji, tint, "world_$id.png")
}

fun StoryWorldArt.gradient(): Brush = Brush.verticalGradient(
    listOf(tint.copy(alpha = 0.55f), Color(0xFF0B0805))
)
