package com.lernhero.home.navigation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

import com.lernhero.shared.theme.FontFirst
import com.lernhero.shared.theme.FontSize
import com.lernhero.shared.theme.IconPrimary
import com.lernhero.shared.theme.IconSecondary
import com.lernhero.shared.theme.Surface
import com.lernhero.shared.theme.SurfaceDarker
import org.jetbrains.compose.resources.painterResource

@Composable
fun NavBottomBar(
    modifier: Modifier = Modifier,
    selected : BottomBarDestination,
    onSelect: (BottomBarDestination) -> Unit,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(Surface)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            BottomBarDestination.entries.forEach { destination ->
                val isSelected = selected == destination

                val iconColor by animateColorAsState(
                    targetValue = if (isSelected) IconSecondary else IconPrimary
                )

                val backgroundColor by animateColorAsState(
                    targetValue = if (isSelected) Color(0xFFFFF1E8) else Surface
                )

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(backgroundColor)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = { onSelect(destination) }
                        )
                        .padding(vertical = 8.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        painter = painterResource(destination.icon),
                        tint = iconColor,
                        contentDescription = destination.title,
                        modifier = Modifier.size(22.dp)
                    )

                    AnimatedVisibility(visible = isSelected) {
                        Text(
                            text = destination.title,
                            fontFamily = FontFirst(),
                            fontSize = FontSize.SMALL,
                            color = iconColor
                        )
                    }
                    Box(
                        modifier = Modifier
                            .padding(top = 4.dp)
                            .height(3.dp)
                            .fillMaxWidth(0.24f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSelected) IconSecondary else SurfaceDarker)
                    )
                }
            }
        }
    }
}
