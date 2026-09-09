package com.lernhero.home.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lernhero.domain.combat.StatType
import com.lernhero.domain.item.Item
import com.lernhero.shared.theme.FontFirst
import com.lernhero.shared.theme.FontPixel

/**
 * "Co ten przedmiot robi" - opis, statystyki i lista zdolnosci, w JEDNYM bloku o STALEJ wysokosci.
 *
 * Dwa problemy naraz, oba widoczne w kuzni:
 *
 * 1. **Nie bylo widac zdolnosci.** Warsztat pokazywal sam opis fabularny ("The bigger the horn,
 *    the fewer the questions"), a to, ze bron truje albo bije dwoch naraz, gracz mogl odkryc
 *    dopiero w walce. Zdolnosci sa TU, nad kosztem, bo to one decyduja o wyborze - nie liczba ataku.
 * 2. **Menu skakalo.** Wysokosc panelu szla za dlugoscia opisu: krotszy przedmiot skracal warsztat,
 *    a wtedy siatka pod spodem rosla i kafelki zmienialy rozmiar w trakcie przegladania. Blok ma
 *    wiec SZTYWNA wysokosc [height] - dluzsza tresc przewija sie w srodku, zamiast rozpychac ekran.
 *
 * Wysokosc jest parametrem, bo warsztat kucia i warsztat ulepszania maja rozne budzety miejsca -
 * ale w obrebie jednego ekranu jest stala, i tylko to sie liczy.
 */
@Composable
fun ItemInfoBlock(
    item: Item,
    modifier: Modifier = Modifier,
    height: Dp = ITEM_INFO_HEIGHT,
    showDescription: Boolean = true,
    /** Wylaczane tam, gdzie ekran pokazuje juz wlasna tabelke statystyk (warsztat ulepszania). */
    showStats: Boolean = true
) {
    Box(modifier = modifier.fillMaxWidth().height(height)) {
        Column(
            modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(3.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (showDescription && item.description.isNotBlank()) {
                Text(
                    text = item.description,
                    color = ParchmentPalette.InkMuted,
                    fontFamily = FontFirst(),
                    fontSize = 10.sp,
                    textAlign = TextAlign.Center
                )
            }

            val stats = item.stats.asMap().filterValues { it != 0f }
            if (showStats && stats.isNotEmpty()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    stats.entries.forEach { (stat, value) ->
                        Text(
                            text = "${stat.shortLabel()} ${stat.format(value)}",
                            color = if (value > 0f) ParchmentPalette.Ink else Color_Negative,
                            fontFamily = FontPixel(),
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            modifier = Modifier.padding(horizontal = 5.dp)
                        )
                    }
                }
            }

            // Zdolnosci na koncu, ale wizualnie najmocniej - to one sa powodem, dla ktorego gracz
            // wybiera slabszy statystycznie przedmiot.
            item.effects.forEach { effect ->
                Text(
                    text = "✦ ${effect.description}",
                    color = ParchmentPalette.Ink,
                    fontFamily = FontFirst(),
                    fontWeight = FontWeight.Bold,
                    fontSize = 10.sp,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

/** Domyslny budzet miejsca na opis + staty + zdolnosci. Dobrany pod najbogatszy przedmiot w grze. */
val ITEM_INFO_HEIGHT = 74.dp

private val Color_Negative = androidx.compose.ui.graphics.Color(0xFF9E2B20)

/** Krotka etykieta statystyki - w warsztacie nie ma miejsca na "Critical damage". */
internal fun StatType.shortLabel(): String = when (this) {
    StatType.MaxHp -> "HP"
    StatType.Attack -> "ATK"
    StatType.Defense -> "DEF"
    StatType.Speed -> "SPD"
    StatType.CritChance -> "CRIT"
    StatType.CritDamage -> "CDMG"
    StatType.Dodge -> "DODGE"
    StatType.Lifesteal -> "LEECH"
    StatType.DamageReduction -> "RED"
    StatType.HealPower -> "HEAL"
    StatType.SpellPower -> "SPELL"
    StatType.AttackTargets -> "TGT"
    StatType.CooldownReduction -> "CD"
}
