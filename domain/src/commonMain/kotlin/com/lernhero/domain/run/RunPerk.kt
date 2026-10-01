package com.lernhero.domain.run

// Zbudowane, jeszcze nie podpiete: nic obecnie nie wola RunPerk.offer() z poziomu RunSession czy
// UI obozowiska. Zostawione swiadomie jako gotowy element pod przyszly ekran wyboru perka -
// nie kasowac jako "martwy kod".

import kotlin.random.Random

/**
 * Perk z obozowiska - wzmocnienie na czas jednego zejscia do lochu. Kazdy to nazwa, opis i
 * przeksztalcenie [RunModifiers]. Nowy perk to jeden wpis; nic wiecej.
 */
enum class RunPerk(
    val title: String,
    val description: String,
    val apply: (RunModifiers) -> RunModifiers
) {
    Focus(
        title = "Focus",
        description = "+15% spell power for the rest of the run.",
        apply = { it.copy(bonusSpellPower = it.bonusSpellPower + 0.15f) }
    ),
    Edge(
        title = "Edge",
        description = "+6 attack for the rest of the run.",
        apply = { it.copy(bonusAttack = it.bonusAttack + 6) }
    ),
    Vitality(
        title = "Vigor",
        description = "+25 max health for the rest of the run.",
        apply = { it.copy(bonusMaxHp = it.bonusMaxHp + 25) }
    ),
    Arcana(
        title = "Arcana",
        description = "+30% spell power for the rest of the run.",
        apply = { it.copy(bonusSpellPower = it.bonusSpellPower + 0.3f) }
    );

    companion object {
        /**
         * Trzy losowe perki na dane ognisko. Losowanie idzie z seeda pola, wiec te samo ognisko
         * proponuje zawsze ten sam zestaw - gracz nie "przewija" go wchodzeniem tam i z powrotem.
         */
        fun offer(seed: Long): List<RunPerk> = entries.shuffled(Random(seed)).take(3)
    }
}
