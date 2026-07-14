package com.lernhero.home.ui

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.lernhero.domain.common.Rarity

/**
 * Maps a domain [Rarity] to its UI colors (frame, glow, label). Rarity is kept domain-pure,
 * so all color decisions live here in the feature layer.
 */
object RarityPalette {
    fun frame(rarity: Rarity): Color = when (rarity) {
        Rarity.Common -> Color(0xFF9DA3AE)
        Rarity.Uncommon -> Color(0xFF4ADE80)
        Rarity.Rare -> Color(0xFF3B9DFF)
        Rarity.Epic -> Color(0xFFB061FF)
        Rarity.Legendary -> Color(0xFFFFB020)
    }

    fun glow(rarity: Rarity): Color = frame(rarity).copy(alpha = 0.35f)

    fun label(rarity: Rarity): String = when (rarity) {
        Rarity.Common -> "Zwykly"
        Rarity.Uncommon -> "Niezwykly"
        Rarity.Rare -> "Rzadki"
        Rarity.Epic -> "Epicki"
        Rarity.Legendary -> "Legendarny"
    }

    /** Soft vertical gradient used behind rarity cards. */
    fun cardBackground(rarity: Rarity): Brush = Brush.verticalGradient(
        colors = listOf(
            frame(rarity).copy(alpha = 0.18f),
            Color(0xFF17120D)
        )
    )
}
