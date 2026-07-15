package com.lernhero.home.dungeon

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lernhero.domain.run.RunPerk
import com.lernhero.domain.run.RunSession
import com.lernhero.domain.spell.AbilityLoadout
import com.lernhero.home.player.PlayerViewModel
import com.lernhero.home.ui.AbilityBar
import com.lernhero.shared.theme.FontFirst
import com.lernhero.shared.theme.FontPixel
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

private val Panel = Color(0xFF1A1410)
private val Ink = Color(0xFF120E0A)
private val Gold = Color(0xFFFFD100)
private val GoldFrame = Color(0xFFC8A45A)
private val GoldDim = Color(0xFF6B5A38)
private val Muted = Color(0xFF9D8B6A)
private val Ember = Color(0xFFE0913A)

/**
 * Obozowisko: gracz wybiera jeden perk na czas runu, a potem moze przemeblowac pasek zdolnosci
 * (z uwzglednieniem dodatkowej pojemnosci z perka). Wszystko ląduje w [RunSession] i znika po runie.
 *
 * [nodeSeed] okresla zestaw oferowanych perkow - to samo ognisko zawsze proponuje to samo.
 */
@Composable
internal fun CampfireDialog(
    nodeSeed: Long,
    onLeave: () -> Unit
) {
    val runSession = koinInject<RunSession>()
    val playerViewModel = koinViewModel<PlayerViewModel>()
    val playerState by playerViewModel.uiState.collectAsState()

    val offered = remember(nodeSeed) { RunPerk.offer(nodeSeed) }
    var pickedPerk by remember { mutableStateOf<RunPerk?>(null) }
    // Wagi na czas runu: startuja z tego, co jest, i lezą w RunSession po kazdej zmianie.
    var runWeights by remember {
        mutableStateOf(runSession.modifiers.spellWeightOverride ?: playerState.spellWeights)
    }

    val capacity = AbilityLoadout.BASE_CAPACITY + runSession.modifiers.bonusSpellCapacity
    val runAbility = remember(runWeights, capacity) {
        runWeights.entries.fold(AbilityLoadout(capacity = capacity)) { bar, (id, pct) ->
            bar.withPercent(id, pct)
        }
    }

    AlertDialog(
        onDismissRequest = onLeave,
        containerColor = Panel,
        title = {
            Text(
                text = "Obozowisko",
                color = Gold,
                fontFamily = FontPixel(),
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = if (pickedPerk == null) {
                        "Odpocznij i wybierz jedno wzmocnienie na ten run."
                    } else {
                        "Wzmocnienie przyjete. Mozesz teraz poukladac pasek zdolnosci na dalsza droge."
                    },
                    color = Muted,
                    fontFamily = FontFirst(),
                    fontSize = 12.sp
                )

                offered.forEach { perk ->
                    PerkCard(
                        perk = perk,
                        picked = pickedPerk == perk,
                        locked = pickedPerk != null && pickedPerk != perk,
                        onPick = {
                            if (pickedPerk == null) {
                                runSession.apply(perk.apply)
                                pickedPerk = perk
                            }
                        }
                    )
                }

                if (playerState.spells.isNotEmpty()) {
                    AbilityBar(
                        ability = runAbility,
                        ownedSpells = playerState.spells,
                        onSet = { id, percent ->
                            val next = runAbility.withPercent(id, percent).weights
                            runWeights = next
                            runSession.overrideSpellWeights(next)
                        }
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onLeave) {
                Text(text = "Rusz dalej", color = Gold, fontFamily = FontFirst())
            }
        }
    )
}

@Composable
private fun PerkCard(
    perk: RunPerk,
    picked: Boolean,
    locked: Boolean,
    onPick: () -> Unit
) {
    val border = when {
        picked -> Ember
        locked -> GoldDim.copy(alpha = 0.4f)
        else -> GoldFrame
    }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(Ink)
            .border(if (picked) 2.dp else 1.dp, border, RoundedCornerShape(10.dp))
            .then(if (!locked && !picked) Modifier.clickable(onClick = onPick) else Modifier)
            .padding(10.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Text(
            text = perk.title + if (picked) "  ✓" else "",
            color = if (locked) Muted else Ember,
            fontFamily = FontPixel(),
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp
        )
        Text(
            text = perk.description,
            color = if (locked) Muted.copy(alpha = 0.6f) else Color(0xFFCDBB99),
            fontFamily = FontFirst(),
            fontSize = 11.sp
        )
    }
}
