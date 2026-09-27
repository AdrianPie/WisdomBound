package com.lernhero.game.battle.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import com.lernhero.domain.run.EndlessBuff
import com.lernhero.domain.run.UpgradeRarity
import com.lernhero.game.battle.BattleSpeed
import com.lernhero.game.battle.GamePhase
import com.lernhero.shared.theme.FontFirst

private val HudInk = Color(0xFF241A12)
private val HudGold = Color(0xFFFFD34D)
private val HudGoldFrame = Color(0xFFC8A45A)
private val HudCream = Color(0xFFF4E9D7)

/**
 * Odznaka fali w lewym gornym rogu, jak poziom postaci w RPG - numer fali plus cienki pasek HP
 * bohatera pod spodem, zeby stan druzyny byl widoczny bez patrzenia w glab pola bitwy.
 */
@Composable
internal fun WaveBadge(wave: Int, heroHpFraction: Float, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(HudInk)
            .border(2.dp, HudGoldFrame, RoundedCornerShape(12.dp))
            .padding(horizontal = 14.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "WAVE $wave",
            color = HudGold,
            fontFamily = FontFirst(),
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp
        )
        Box(
            modifier = Modifier
                .padding(top = 4.dp)
                .width(64.dp)
                .height(6.dp)
                .clip(RoundedCornerShape(50))
                .background(Color(0xFF120E0A))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(heroHpFraction.coerceIn(0f, 1f))
                    .height(6.dp)
                    .clip(RoundedCornerShape(50))
                    .background(Color(0xFF4ADE80))
            )
        }
    }
}

/**
 * Rzad malych znaczkow aktywnych endless bonusow ([EndlessBuff], patrz BattleViewModel.spawnNextWave) -
 * pusty poza endless mode, wiec nie zajmuje miejsca w story mode. Przewijalny w bok, bo przy dlugim
 * biegu i sporym szczesciu moze sie ich uzbierac wiecej, niz zmiesci sie na ekranie na raz.
 */
@Composable
internal fun EndlessBuffRow(buffs: Map<EndlessBuff, Int>, modifier: Modifier = Modifier) {
    if (buffs.isEmpty()) return
    Row(
        modifier = modifier.horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        buffs.entries.sortedBy { it.key.ordinal }.forEach { (buff, stacks) ->
            // Ta sama ikona i kolor klasy co na karcie wyboru (UpgradeChoiceDialog).
            val accent = buff.rarity.color()
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    // Maly i polprzezroczysty - lezy nad polem bitwy i nie moze zaslaniac postaci.
                    .background(HudInk.copy(alpha = 0.72f))
                    .border(1.dp, accent.copy(alpha = 0.85f), RoundedCornerShape(50))
                    .padding(horizontal = 4.dp, vertical = 1.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(1.dp)
            ) {
                Text(text = buff.icon(), fontSize = 9.sp, lineHeight = 11.sp)
                if (stacks > 1) {
                    Text(
                        text = "x$stacks",
                        color = accent,
                        fontFamily = FontFirst(),
                        fontWeight = FontWeight.Black,
                        fontSize = 8.sp,
                        lineHeight = 10.sp
                    )
                }
            }
        }
    }
}

/** Odznaka licznika pytan w prawym gornym rogu - lustro [WaveBadge] po drugiej stronie ekranu. */
@Composable
internal fun AnswerCountBadge(count: Int, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(HudInk)
            .border(2.dp, HudGoldFrame, RoundedCornerShape(12.dp))
            .padding(horizontal = 14.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "QUESTIONS",
            color = Color(0xFF9D8B6A),
            fontFamily = FontFirst(),
            fontWeight = FontWeight.Bold,
            fontSize = 10.sp
        )
        Text(
            text = "$count",
            color = HudGold,
            fontFamily = FontFirst(),
            fontWeight = FontWeight.Bold,
            fontSize = 17.sp
        )
    }
}

/** Krotki komunikat z silnika walki (fala nadciaga, passa, ulti...), wyswietlany na srodku gory. */
@Composable
internal fun BattleMessageBanner(
    message: String?,
    buffLabel: String?,
    buffPositive: Boolean,
    modifier: Modifier = Modifier
) {
    if (message == null && buffLabel == null) return
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(Color(0xCC241A12))
            .padding(horizontal = 12.dp, vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        buffLabel?.let {
            Text(
                text = it,
                color = if (buffPositive) Color(0xFF7FD1A0) else Color(0xFFE7A08A),
                fontFamily = FontFirst(),
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp
            )
        }
        message?.let {
            Text(
                text = it,
                color = HudCream,
                fontFamily = FontFirst(),
                fontSize = 12.sp,
                modifier = Modifier.padding(top = if (buffLabel != null) 2.dp else 0.dp)
            )
        }
    }
}

/** Sterowanie tempem walki + kapitulacja - male, schowane w rogu, zeby nie zaslaniac pola bitwy. */
@Composable
internal fun BattleControls(
    phase: GamePhase,
    onCapitulate: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var confirmCapitulate by remember { mutableStateOf(false) }

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        SpeedSwitch()

        if (phase == GamePhase.Battle) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xCC241A12))
                    .border(1.dp, Color(0xFFE7A08A), RoundedCornerShape(8.dp))
                    .clickable { confirmCapitulate = true }
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Text(
                    text = "Surrender",
                    color = Color(0xFFE7A08A),
                    fontFamily = FontFirst(),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }

    if (confirmCapitulate) {
        AlertDialog(
            onDismissRequest = { confirmCapitulate = false },
            containerColor = Color(0xFF1A1410),
            title = { Text(text = "Surrender?", color = Color(0xFFFFD100), fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    text = "You are pulling your party out of the run. Only 25% of collected loot is saved - the rest is lost.",
                    color = Color(0xFFCDBB99)
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    confirmCapitulate = false
                    onCapitulate()
                }) {
                    Text(text = "Surrender", color = Color(0xFFE7A08A))
                }
            },
            dismissButton = {
                TextButton(onClick = { confirmCapitulate = false }) {
                    Text(text = "Keep Fighting", color = Color(0xFFFFD100))
                }
            }
        )
    }
}

/**
 * Sterowanie tempem walki: [-] wolniej, [+] szybciej.
 */
@Composable
private fun SpeedSwitch() {
    Row(
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        SpeedButton(symbol = "−", enabled = BattleSpeed.canSlowDown, onClick = { BattleSpeed.slower() })
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(6.dp))
                .background(Color(0xCC241A12))
                .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Text(
                text = BattleSpeed.label,
                color = Color.White,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
        }
        SpeedButton(symbol = "+", enabled = BattleSpeed.canSpeedUp, onClick = { BattleSpeed.faster() })
    }
}

@Composable
private fun SpeedButton(symbol: String, enabled: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(24.dp)
            .clip(CircleShape)
            .background(if (enabled) Color(0xFFB4451F) else Color(0x55B4451F))
            .then(if (enabled) Modifier.clickable(onClick = onClick) else Modifier),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = symbol,
            color = Color.White.copy(alpha = if (enabled) 1f else 0.5f),
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold
        )
    }
}
