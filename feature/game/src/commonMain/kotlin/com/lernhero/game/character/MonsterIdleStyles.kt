package com.lernhero.game.character

import androidx.compose.ui.graphics.Color

/**
 * Animacje spoczynku drugiej fali kompanow - jedna linijka stylu na gatunek.
 *
 * Wspolrzedne [Emit] to ulamki grafiki 512x512 TAK JAK NARYSOWANA (art patrzy w prawo), odczytane
 * z siatki 10% nalozonej na kazdy PNG. Gdy grafika sie zmieni, zmieniaja sie tylko te liczby.
 */
internal object MonsterIdleStyles {

    private val RedEye = Color(0xFFFF3B30)
    private val PurpleEye = Color(0xFFC77DFF)
    private val GreenEye = Color(0xFF7CFF6B)
    private val IceEye = Color(0xFF8FE3FF)
    private val FireEye = Color(0xFFFF9A2E)
    private val Gem = Color(0xFF5CE1FF)

    val byKey: Map<String, IdleStyle> = mapOf(
        // ---- Ogien ----
        "ember_imp" to IdleStyle(
            breathe = 0.022f, swayDeg = 1.4f, periodMs = 1900,
            emitters = listOf(
                Emit(0.83f, 0.37f, Fx.Flame, scale = 0.9f),
                Emit(0.17f, 0.57f, Fx.Flame, scale = 0.75f),
                Emit(0.83f, 0.37f, Fx.Ember, density = 0.6f)
            )
        ),
        // Bez trzepotu: sciskanie calego PNG w pionie wygladalo jak blad grafiki, nie jak skrzydla.
        // Zostaje plynne unoszenie z przechylem i iskry sypiace sie ze skrzydel.
        "fire_bat" to IdleStyle(
            breathe = 0.01f, swayDeg = 4f, periodMs = 2200, hover = 0.06f,
            emitters = listOf(
                Emit(0.28f, 0.52f, Fx.Ember, spread = 0.1f),
                Emit(0.75f, 0.6f, Fx.Ember, spread = 0.1f)
            )
        ),
        "lava_slime" to IdleStyle(
            breathe = 0.02f, swayDeg = 0.4f, periodMs = 2000, wobble = 0.035f,
            emitters = listOf(
                Emit(0.5f, 0.45f, Fx.Ember, spread = 0.2f),
                Emit(0.62f, 0.3f, Fx.Flame, scale = 0.6f, density = 0.6f),
                Emit(0.3f, 0.62f, Fx.Drip, spread = 0.12f)
            )
        ),
        "hellhound" to IdleStyle(
            breathe = 0.02f, swayDeg = 0.6f, periodMs = 1700,
            emitters = listOf(
                Emit(0.4f, 0.24f, Fx.Flame, spread = 0.12f),
                Emit(0.68f, 0.2f, Fx.Flame, spread = 0.06f, scale = 0.8f),
                Emit(0.14f, 0.42f, Fx.Flame, scale = 1.1f),
                Emit(0.83f, 0.38f, Fx.Glow, scale = 0.45f, tint = FireEye),
                Emit(0.45f, 0.5f, Fx.Ember, spread = 0.2f)
            )
        ),
        "flame_cultist" to IdleStyle(
            breathe = 0.016f, swayDeg = 0.6f, periodMs = 2600,
            waveFrom = 0.55f, waveAmp = 0.012f,
            emitters = listOf(
                Emit(0.22f, 0.12f, Fx.Flame, scale = 1.1f),
                Emit(0.76f, 0.33f, Fx.Flame, scale = 1.3f),
                Emit(0.76f, 0.33f, Fx.Ember, density = 0.7f),
                Emit(0.56f, 0.2f, Fx.Glow, scale = 0.35f, tint = FireEye)
            )
        ),
        "salamander" to IdleStyle(
            breathe = 0.018f, swayDeg = 1f, periodMs = 2100,
            emitters = listOf(
                Emit(0.52f, 0.18f, Fx.Flame, spread = 0.08f),
                Emit(0.17f, 0.64f, Fx.Flame, scale = 1.2f),
                Emit(0.78f, 0.28f, Fx.Ember, spread = 0.05f),
                Emit(0.63f, 0.23f, Fx.Glow, scale = 0.35f, tint = FireEye)
            )
        ),
        "efreet" to IdleStyle(
            breathe = 0.02f, swayDeg = 0.8f, periodMs = 2300, hover = 0.015f,
            waveFrom = 0.55f, waveAmp = 0.022f, waveCycles = 2f, wavePeriodMs = 1300,
            emitters = listOf(
                Emit(0.5f, 0.08f, Fx.Flame, spread = 0.08f, scale = 1.2f),
                Emit(0.22f, 0.36f, Fx.Flame, scale = 1.1f),
                Emit(0.78f, 0.43f, Fx.Flame, scale = 1.1f),
                Emit(0.5f, 0.75f, Fx.Ember, spread = 0.15f, density = 1.3f)
            )
        ),
        "magma_golem" to IdleStyle(
            breathe = 0.012f, swayDeg = 0.4f, periodMs = 3200,
            emitters = listOf(
                Emit(0.5f, 0.4f, Fx.Ember, spread = 0.32f, density = 1.6f),
                Emit(0.4f, 0.12f, Fx.Flame, spread = 0.12f, scale = 0.8f),
                Emit(0.5f, 0.7f, Fx.Drip, spread = 0.3f),
                Emit(0.62f, 0.26f, Fx.Glow, scale = 0.45f, tint = FireEye)
            )
        ),
        // Minotaur: plomyki w oczach i nozdrzach, jak w opisie grafiki.
        "minotaur" to IdleStyle(
            breathe = 0.016f, swayDeg = 0.5f, periodMs = 2800,
            emitters = listOf(
                Emit(0.61f, 0.22f, Fx.Flame, spread = 0.015f, scale = 0.45f, density = 0.7f),
                Emit(0.61f, 0.22f, Fx.Glow, scale = 0.4f, tint = FireEye),
                Emit(0.66f, 0.3f, Fx.Frost, spread = 0.01f, scale = 0.5f, density = 0.5f),
                Emit(0.86f, 0.5f, Fx.Ember, spread = 0.08f)
            )
        ),
        "phoenix" to IdleStyle(
            breathe = 0.01f, swayDeg = 1.2f, periodMs = 2200, hover = 0.04f,
            flap = 0.07f, flapPivotY = 0.45f, flapMs = 900,
            waveFrom = 0.62f, waveAmp = 0.02f, waveCycles = 1.6f, wavePeriodMs = 1200,
            emitters = listOf(
                Emit(0.18f, 0.2f, Fx.Flame, spread = 0.1f, scale = 1.1f),
                Emit(0.82f, 0.2f, Fx.Flame, spread = 0.1f, scale = 1.1f),
                Emit(0.45f, 0.82f, Fx.Flame, spread = 0.1f, scale = 1.2f),
                Emit(0.5f, 0.5f, Fx.Ember, spread = 0.35f, density = 2f),
                Emit(0.58f, 0.22f, Fx.Glow, scale = 0.4f, tint = Color(0xFFFFF2A8))
            )
        ),

        // ---- Natura ----
        "goblin_spearman" to IdleStyle(breathe = 0.02f, swayDeg = 1.2f, periodMs = 2000),
        "mushroom_sprite" to IdleStyle(
            breathe = 0.02f, swayDeg = 1.2f, periodMs = 2200, wobble = 0.012f,
            emitters = listOf(Emit(0.45f, 0.14f, Fx.Spore, spread = 0.22f, density = 1.3f))
        ),
        "forest_fox" to IdleStyle(
            breathe = 0.02f, swayDeg = 0.8f, periodMs = 1800,
            emitters = listOf(Emit(0.2f, 0.35f, Fx.Leaf, spread = 0.1f))
        ),
        "treant_sapling" to IdleStyle(
            breathe = 0.016f, swayDeg = 1.6f, periodMs = 3000,
            emitters = listOf(
                Emit(0.55f, 0.15f, Fx.Leaf, spread = 0.18f),
                Emit(0.6f, 0.44f, Fx.Glow, scale = 0.45f, tint = GreenEye)
            )
        ),
        "elf_ranger" to IdleStyle(
            breathe = 0.016f, swayDeg = 0.4f, periodMs = 2400,
            emitters = listOf(Emit(0.82f, 0.3f, Fx.Leaf, spread = 0.1f))
        ),
        "boar_rider" to IdleStyle(breathe = 0.02f, swayDeg = 0.6f, periodMs = 1700),
        "centaur_huntress" to IdleStyle(
            breathe = 0.014f, swayDeg = 0.4f, periodMs = 2600,
            emitters = listOf(Emit(0.8f, 0.35f, Fx.Leaf, spread = 0.12f))
        ),
        "thorn_beast" to IdleStyle(
            breathe = 0.018f, swayDeg = 0.5f, periodMs = 2800,
            emitters = listOf(
                Emit(0.45f, 0.25f, Fx.Leaf, spread = 0.3f, density = 1.3f),
                Emit(0.78f, 0.45f, Fx.Glow, scale = 0.35f, tint = FireEye)
            )
        ),
        "centaur" to IdleStyle(
            breathe = 0.014f, swayDeg = 0.4f, periodMs = 2600,
            emitters = listOf(
                Emit(0.8f, 0.3f, Fx.Leaf, spread = 0.12f),
                Emit(0.5f, 0.5f, Fx.Spore, spread = 0.3f, density = 0.7f)
            )
        ),
        "ancient_treant" to IdleStyle(
            breathe = 0.012f, swayDeg = 1.2f, periodMs = 3600,
            emitters = listOf(
                Emit(0.5f, 0.12f, Fx.Leaf, spread = 0.35f, density = 1.6f),
                Emit(0.35f, 0.35f, Fx.Spore, spread = 0.25f),
                Emit(0.67f, 0.22f, Fx.Glow, scale = 0.45f, tint = GreenEye)
            )
        ),

        // ---- Woda ----
        "tidecrawler" to IdleStyle(
            breathe = 0.02f, swayDeg = 0.6f, periodMs = 1900,
            emitters = listOf(
                Emit(0.3f, 0.3f, Fx.Bubble, spread = 0.12f),
                Emit(0.75f, 0.2f, Fx.Bubble, spread = 0.06f, density = 0.7f)
            )
        ),
        "bubble_slime" to IdleStyle(
            breathe = 0.02f, swayDeg = 0.4f, periodMs = 1800, wobble = 0.045f,
            emitters = listOf(Emit(0.45f, 0.35f, Fx.Bubble, spread = 0.28f, density = 1.4f))
        ),
        "murloc" to IdleStyle(
            breathe = 0.02f, swayDeg = 1.2f, periodMs = 1700,
            emitters = listOf(Emit(0.55f, 0.25f, Fx.Bubble, spread = 0.12f, density = 0.8f))
        ),
        "frost_wolf" to IdleStyle(
            breathe = 0.022f, swayDeg = 0.5f, periodMs = 1700,
            emitters = listOf(
                Emit(0.88f, 0.56f, Fx.Frost, spread = 0.02f, density = 1.2f),
                Emit(0.5f, 0.3f, Fx.Snow, spread = 0.3f, density = 0.6f)
            )
        ),
        "ice_archer" to IdleStyle(
            breathe = 0.016f, swayDeg = 0.4f, periodMs = 2400,
            emitters = listOf(
                Emit(0.82f, 0.3f, Fx.Frost, spread = 0.1f, scale = 0.7f),
                Emit(0.5f, 0.2f, Fx.Snow, spread = 0.3f, density = 0.6f)
            )
        ),
        "naga_siren" to IdleStyle(
            breathe = 0.018f, swayDeg = 1f, periodMs = 2600,
            waveFrom = 0.5f, waveAmp = 0.02f, waveCycles = 1.2f, wavePeriodMs = 2000,
            emitters = listOf(
                Emit(0.79f, 0.25f, Fx.Glow, scale = 0.45f, tint = Gem),
                Emit(0.5f, 0.6f, Fx.Bubble, spread = 0.25f)
            )
        ),
        "yeti" to IdleStyle(
            breathe = 0.022f, swayDeg = 0.6f, periodMs = 2200,
            emitters = listOf(
                Emit(0.66f, 0.34f, Fx.Frost, spread = 0.02f),
                Emit(0.5f, 0.1f, Fx.Snow, spread = 0.35f)
            )
        ),
        "frost_giant" to IdleStyle(
            breathe = 0.014f, swayDeg = 0.4f, periodMs = 3400,
            emitters = listOf(
                Emit(0.38f, 0.7f, Fx.Frost, spread = 0.06f, scale = 1.2f),
                Emit(0.5f, 0.05f, Fx.Snow, spread = 0.4f, density = 1.5f),
                Emit(0.58f, 0.2f, Fx.Glow, scale = 0.35f, tint = IceEye)
            )
        ),

        // ---- Smierc ----
        "zombie" to IdleStyle(
            breathe = 0.02f, swayDeg = 2.2f, periodMs = 2800,
            emitters = listOf(Emit(0.5f, 0.35f, Fx.Stink, spread = 0.15f))
        ),
        "ghost" to IdleStyle(
            breathe = 0.012f, swayDeg = 1.4f, periodMs = 2400, hover = 0.045f,
            waveFrom = 0.3f, waveAmp = 0.024f, waveCycles = 1.8f, wavePeriodMs = 1500,
            emitters = listOf(
                Emit(0.52f, 0.33f, Fx.Glow, scale = 0.4f, tint = IceEye),
                Emit(0.5f, 0.8f, Fx.Soul, spread = 0.2f, scale = 0.7f, density = 0.6f)
            )
        ),
        "ghoul" to IdleStyle(
            breathe = 0.024f, swayDeg = 1f, periodMs = 1800,
            emitters = listOf(
                Emit(0.71f, 0.33f, Fx.Glow, scale = 0.4f, tint = PurpleEye),
                Emit(0.5f, 0.5f, Fx.Stink, spread = 0.2f, density = 0.7f)
            )
        ),
        "banshee" to IdleStyle(
            breathe = 0.012f, swayDeg = 1.2f, periodMs = 2600, hover = 0.03f,
            waveFrom = 0.4f, waveAmp = 0.024f, waveCycles = 1.6f, wavePeriodMs = 1600,
            emitters = listOf(
                Emit(0.55f, 0.15f, Fx.Soul, spread = 0.15f),
                Emit(0.35f, 0.72f, Fx.Soul, spread = 0.15f, scale = 0.8f)
            )
        ),
        "vampire" to IdleStyle(
            breathe = 0.016f, swayDeg = 0.6f, periodMs = 2600,
            waveFrom = 0.38f, waveAmp = 0.014f, waveCycles = 1.3f, wavePeriodMs = 2100,
            emitters = listOf(Emit(0.66f, 0.26f, Fx.Glow, scale = 0.35f, tint = RedEye))
        ),
        "death_knight" to IdleStyle(
            breathe = 0.014f, swayDeg = 0.4f, periodMs = 3000,
            waveFrom = 0.45f, waveAmp = 0.01f,
            emitters = listOf(
                Emit(0.63f, 0.13f, Fx.Glow, scale = 0.4f, tint = PurpleEye),
                Emit(0.8f, 0.6f, Fx.Soul, spread = 0.06f)
            )
        ),
        "lich" to IdleStyle(
            breathe = 0.014f, swayDeg = 0.6f, periodMs = 3000, hover = 0.012f,
            waveFrom = 0.5f, waveAmp = 0.018f, waveCycles = 1.5f, wavePeriodMs = 1800,
            emitters = listOf(
                Emit(0.26f, 0.22f, Fx.GreenSoul, spread = 0.03f, scale = 1.1f),
                Emit(0.78f, 0.22f, Fx.Soul, spread = 0.03f, scale = 1.1f),
                Emit(0.24f, 0.5f, Fx.GreenSoul, spread = 0.02f, scale = 0.7f, density = 0.6f),
                Emit(0.82f, 0.55f, Fx.GreenSoul, spread = 0.02f, scale = 0.7f, density = 0.6f),
                Emit(0.52f, 0.2f, Fx.Glow, scale = 0.35f, tint = GreenEye)
            )
        )
    )
}
