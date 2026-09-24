package com.lernhero.game.character

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import com.lernhero.domain.monster.Element
import com.lernhero.shared.resources.Resources
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource

/** Grafika emblematu zywiolu - jedno miejsce mapowania [Element] na PNG. */
fun Element.emblem(): DrawableResource = when (this) {
    Element.Fire -> Resources.ElementArt.fire
    Element.Nature -> Resources.ElementArt.nature
    Element.Water -> Resources.ElementArt.water
    Element.Death -> Resources.ElementArt.death
}

/** Emblemat zywiolu w zadanym rozmiarze - zastepuje dawne emoji w kolorowym kolku. */
@Composable
fun ElementEmblem(element: Element, size: Dp, modifier: Modifier = Modifier) {
    Image(
        painter = painterResource(element.emblem()),
        contentDescription = element.label,
        modifier = modifier.size(size)
    )
}
