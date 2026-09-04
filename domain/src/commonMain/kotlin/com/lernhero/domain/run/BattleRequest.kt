package com.lernhero.domain.run

/** Rodzaj walki, w ktora wchodzi gracz z mapy lochu. */
enum class BattleKind {
    Normal,
    Elite,
    Boss
}

/**
 * Zamowienie na jedna walke, skladane przez mape lochu tuz przed nawigacja do bitwy.
 * Kazde pole mapy to **jedna** walka o znanej z gory liczbie fal - nie nieskonczony ciag.
 *
 * Wedruje przez [RunSession] (a nie przez argumenty nawigacji), bo tamtedy ida juz perki
 * z ognisk - walka i tak czyta ten obiekt.
 */
data class BattleRequest(
    val kind: BattleKind,
    val waves: Int,
    /** Seed pola mapy - ta sama walka po obrocie ekranu sklada sie tak samo. */
    val seed: Long,
    /** Endless mode: fale nadciagaja bez konca (patrz BattleViewModel) - [waves] jest wtedy ignorowane. */
    val isEndless: Boolean = false,
    /** Debug: dodatkowy mnoznik HP wrogow (test walki z bossem - zeby nie padal na jednym ciosie). */
    val enemyHpMultiplier: Float = 1f
)
