package com.lernhero.home.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lernhero.domain.craft.CraftCost
import com.lernhero.domain.craft.SummonCosts
import com.lernhero.domain.spell.Spell
import com.lernhero.shared.theme.FontFirst
import com.lernhero.shared.theme.FontPixel

private val Panel = Color(0xFF19120C)
private val Frame = Color(0xFFC8A45A)
private val Cream = Color(0xFFF4E9D7)
private val Muted = Color(0xFF9D8B6A)
private val GoldText = Color(0xFFFFD34D)
private val Good = Color(0xFF5FD08A)
private val Bad = Color(0xFFD1453D)

/**
 * Odblokowywanie zaklec za esencje z lochow - stoi TUZ POD paskiem zdolnosci, czyli tam, gdzie
 * zaklecia sie przypisuje.
 *
 * Byly przez chwile w Summon Center razem z kompanami i to byl blad: Summon Center przyzywa istoty,
 * a nie uczy zaklec, wiec gracz szukal ich tam, gdzie ich uzywa - i nie znajdowal. Zdobywanie
 * czegokolwiek powinno siedziec obok miejsca, w ktorym sie z tego korzysta.
 */
@Composable
fun SpellUnlockPanel(
    spells: List<Spell>,
    ownedSpellIds: List<String>,
    essence: Int,
    onUnlock: (Spell, CraftCost) -> Unit,
    modifier: Modifier = Modifier
) {
    val locked = spells.filter { it.id !in ownedSpellIds }
    if (locked.isEmpty()) return

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Panel)
            .border(2.dp, Frame.copy(alpha = 0.6f), RoundedCornerShape(12.dp))
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(9.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Nowe zaklecia",
                color = GoldText,
                fontFamily = FontPixel(),
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
            Text(
                text = "✨ $essence",
                color = if (essence > 0) Cream else Muted,
                fontFamily = FontFirst(),
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp
            )
        }

        Text(
            text = "Esencja wypada w lochach - za kazda oczyszczona fale i za bossow.",
            color = Muted,
            fontFamily = FontFirst(),
            fontSize = 11.sp
        )

        locked.forEach { spell ->
            val cost = SummonCosts.forSpell(spell)
            val affordable = essence >= cost.essence

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFF241A12))
                    .border(
                        width = 1.dp,
                        color = if (affordable) RarityPalette.frame(spell.rarity) else Color(0xFF3A2E22),
                        shape = RoundedCornerShape(10.dp)
                    )
                    .clickable(enabled = affordable) { onUnlock(spell, cost) }
                    .padding(horizontal = 10.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = spell.icon(), fontSize = 18.sp)
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = spell.name,
                        color = Cream,
                        fontFamily = FontFirst(),
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                    Text(
                        text = spell.description,
                        color = Muted,
                        fontFamily = FontFirst(),
                        fontSize = 10.sp
                    )
                }
                Text(
                    text = "✨${cost.essence}",
                    color = if (affordable) Good else Bad,
                    fontFamily = FontPixel(),
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
            }
        }
    }
}
