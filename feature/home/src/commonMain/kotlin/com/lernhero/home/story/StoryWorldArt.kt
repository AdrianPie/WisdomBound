package com.lernhero.home.story

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.lernhero.domain.story.StoryWorld
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

fun StoryWorld.art(): StoryWorldArt = when (id) {
    "forest" -> StoryWorldArt(null, "🌲", Color(0xFF3FA96A), "world_forest.png")
    "mine" -> StoryWorldArt(null, "⛏", Color(0xFF9B2C3D), "world_mine.png")
    "icefield" -> StoryWorldArt(null, "❄", Color(0xFF4E8AB8), "world_icefield.png")
    "heat" -> StoryWorldArt(null, "🌋", Color(0xFFC7532F), "world_heat.png")
    "heaven" -> StoryWorldArt(null, "☁", Color(0xFF8A5FD0), "world_heaven.png")
    "abyss" -> StoryWorldArt(null, "🕳", Color(0xFF6B5A38), "world_abyss.png")
    else -> StoryWorldArt(null, "🗺", Color(0xFF6B5A38), "world_$id.png")
}

fun StoryWorldArt.gradient(): Brush = Brush.verticalGradient(
    listOf(tint.copy(alpha = 0.55f), Color(0xFF0B0805))
)
