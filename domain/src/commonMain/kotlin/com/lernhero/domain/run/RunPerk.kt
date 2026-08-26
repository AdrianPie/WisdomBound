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
        title = "Skupienie",
        description = "+10% pojemnosci paska zdolnosci do konca runu.",
        apply = { it.copy(bonusSpellCapacity = it.bonusSpellCapacity + 10) }
    ),
    Edge(
        title = "Ostrze",
        description = "+6 do ataku do konca runu.",
        apply = { it.copy(bonusAttack = it.bonusAttack + 6) }
    ),
    Vitality(
        title = "Wigor",
        description = "+25 maksymalnego zdrowia do konca runu.",
        apply = { it.copy(bonusMaxHp = it.bonusMaxHp + 25) }
    ),
    Arcana(
        title = "Arkana",
        description = "+30% sily zaklec do konca runu.",
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
