package com.lernhero.home.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lernhero.domain.monster.MonsterCard
import com.lernhero.domain.monster.MonsterLevels
import com.lernhero.domain.monster.CompanionPower
import com.lernhero.domain.monster.MonsterStars
import com.lernhero.game.character.MonsterVisualPresets
import org.jetbrains.compose.resources.painterResource

import com.lernhero.shared.theme.FontFirst
import com.lernhero.shared.theme.FontPixel

private val CardInk = Color(0xFF17120D)
private val CardTextGold = Color(0xFFFFD100)
private val CardTextMuted = Color(0xFF9D8B6A)
private val StatHp = Color(0xFFEF4444)
private val StatAtk = Color(0xFFF97316)

/**
 * Rarity-framed monster card with an animated portrait. Reused by the party inventory (as a
 * drag source) and the shop (with a buy [footer]). The caller applies drag/click modifiers.
 */
@Composable
fun MonsterCardView(
    card: MonsterCard,
    modifier: Modifier = Modifier,
    portraitHeight: Int = 92,
    showSkills: Boolean = false,
    /**
     * Poziom kompana (patrz [MonsterLevels]) - domyslnie 1, czyli "bez levelu, jak w katalogu".
     * Karta pokazuje TU obliczone HP/ATK (a nie bazowe z katalogu), zeby ulepszenie kupione w
     * Summon Center bylo widoczne wszedzie, gdzie ta sama karta sie pojawia - w druzynie, w
     * pickerze kompanow, w kolekcji.
     */
    level: Int = MonsterLevels.MIN_LEVEL,
    /** Gwiazdki kompana - podnosza statystyki i sufit poziomu (patrz [MonsterStars]). */
    stars: Int = MonsterStars.MIN_STARS,
    footer: (@Composable ColumnScope.() -> Unit)? = null
) {
    val frame = RarityPalette.frame(card.rarity)
    val leveled = level > MonsterLevels.MIN_LEVEL

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(RarityPalette.cardBackground(card.rarity))
            .border(1.5.dp, frame, RoundedCornerShape(10.dp))
            .padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Portrait
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(portraitHeight.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(CardInk)
                .border(1.dp, frame.copy(alpha = 0.5f), RoundedCornerShape(8.dp)),
            contentAlignment = Alignment.BottomCenter
        ) {
            Box(modifier = Modifier.fillMaxWidth(0.68f).padding(bottom = 4.dp)) {
                Image(
                    painter = painterResource(MonsterVisualPresets.forSpecies(card.spriteKey).drawable),
                    contentDescription = null,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            // Plakietka poziomu - w rogu portretu, zeby bylo widac na pierwszy rzut oka, a nie
            // dopiero po przeczytaniu liczb HP/ATK ponizej.
            if (leveled) {
                Text(
                    text = "LV $level",
                    color = CardTextGold,
                    fontFamily = FontPixel(),
                    fontWeight = FontWeight.Bold,
                    fontSize = 9.sp,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(3.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(CardInk.copy(alpha = 0.85f))
                        .padding(horizontal = 4.dp, vertical = 1.dp)
                )
            }
        }

        Text(
            text = card.name,
            color = CardTextGold,
            fontFamily = FontPixel(),
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            maxLines = 1
        )

        Row(horizontalArrangement = Arrangement.spacedBy(1.dp)) {
            (1..MonsterStars.MAX_STARS).forEach { index ->
                Text(
                    text = "★",
                    color = if (index <= stars) CardTextGold else Color(0xFF3A2E22),
                    fontSize = 9.sp
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            StatPill(label = "HP", value = CompanionPower.hpAt(card, stars, level), color = StatHp)
            StatPill(label = "ATK", value = CompanionPower.attackAt(card, stars, level), color = StatAtk)
        }

        if (showSkills && card.skills.isNotEmpty()) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                card.skills.forEach { skill ->
                    Text(
                        text = "✦ ${skill.name}",
                        color = CardTextMuted,
                        fontFamily = FontFirst(),
                        fontSize = 10.sp,
                        maxLines = 1
                    )
                }
            }
        }

        footer?.invoke(this)
    }
}

@Composable
private fun StatPill(label: String, value: Int, color: Color) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        Text(
            text = label,
            color = CardTextMuted,
            fontFamily = FontFirst(),
            fontSize = 10.sp
        )
        Text(
            text = value.toString(),
            color = color,
            fontFamily = FontFirst(),
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold
        )
    }
}
