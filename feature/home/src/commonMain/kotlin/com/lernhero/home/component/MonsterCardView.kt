package com.lernhero.home.component

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
import com.lernhero.domain.preset.MonsterCard
import com.lernhero.game.character.MonsterSprites
import com.lernhero.game.character.TestCharacter
import com.lernhero.home.RarityPalette
import com.lernhero.shared.FontFirst
import com.lernhero.shared.FontPixel

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
    footer: (@Composable ColumnScope.() -> Unit)? = null
) {
    val frame = RarityPalette.frame(card.rarity)

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
                TestCharacter(
                    sprite = MonsterSprites.idleFor(card.spriteKey),
                    isLooping = true
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

        Text(
            text = RarityPalette.label(card.rarity),
            color = frame,
            fontFamily = FontFirst(),
            fontSize = 10.sp
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            StatPill(label = "HP", value = card.hp, color = StatHp)
            StatPill(label = "ATK", value = card.attack, color = StatAtk)
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
