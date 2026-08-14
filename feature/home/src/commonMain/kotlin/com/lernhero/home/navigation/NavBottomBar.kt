package com.lernhero.home.navigation

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lernhero.shared.theme.AppPalette
import com.lernhero.shared.theme.FontFirst

@Composable
fun NavBottomBar(
    modifier: Modifier = Modifier,
    selected: BottomBarDestination,
    onSelect: (BottomBarDestination) -> Unit,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(AppPalette.Background)
            .border(width = 1.dp, color = AppPalette.GoldDim.copy(alpha = 0.4f))
            .padding(horizontal = 8.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().height(60.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            BottomBarDestination.entries.forEach { destination ->
                val isSelected = selected == destination

                val tint by animateColorAsState(
                    targetValue = if (isSelected) AppPalette.Gold else AppPalette.TextMuted
                )
                val backgroundColor by animateColorAsState(
                    targetValue = if (isSelected) AppPalette.Panel else AppPalette.Background
                )

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(backgroundColor)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = { onSelect(destination) }
                        )
                        .padding(vertical = 6.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(text = destination.icon, fontSize = 19.sp)
                    Text(
                        text = destination.title,
                        fontFamily = FontFirst(),
                        fontSize = 9.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color = tint,
                        maxLines = 1
                    )
                }
            }
        }
    }
}
