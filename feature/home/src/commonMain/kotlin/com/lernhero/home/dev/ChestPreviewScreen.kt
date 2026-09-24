package com.lernhero.home.dev

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt
import com.lernhero.domain.run.ChestSlot
import com.lernhero.domain.run.ChestWheel
import com.lernhero.game.spritesheet.SpriteSheetFrame
import com.lernhero.home.story.BossChestOverlay
import com.lernhero.home.story.resolveChestPrize
import kotlin.random.Random
import com.lernhero.game.spritesheet.SpriteSheetSpec
import com.lernhero.game.spritesheet.frameForProgress
import com.lernhero.shared.resources.Resources
import com.lernhero.shared.theme.ButtonPrimary
import com.lernhero.shared.theme.FontFirst
import com.lernhero.shared.theme.Surface
import com.lernhero.shared.theme.TextPrimary

/**
 * Podglad spritesheeta skrzyni ([Resources.Image.treasureChestSheet], 40 klatek, siatka 4x10) -
 * jedyny cel tego ekranu to zobaczyc animacje w ruchu, bez grzebania w kodzie walki/lupu, gdzie
 * skrzynia finalnie ladowala.
 *
 * Klatka jest PROSTOKATNA, nie kwadratowa (768x432 - 16:9), stad [CHEST_FRAME_RATIO]: podglad
 * musi trzymac te sama proporcje, inaczej obraz sie splaszcza/rozciaga do kwadratu, ktorym nigdy
 * nie byl.
 *
 * Tak jak [KnightPreviewScreen]/[MonsterAnimationLabScreen]: nic tu nie zapisuje sie na dysku,
 * to tylko podglad. Suwak przewija klatki recznie, ODTWORZ puszcza cala animacje od poczatku.
 */
@Composable
fun ChestPreviewScreen(onBack: () -> Unit) {
    val spec = remember { SpriteSheetSpec(drawable = Resources.Image.treasureChestSheet, frameCount = 40, columns = 4) }

    var progress by remember { mutableStateOf(0f) }
    var playing by remember { mutableStateOf(false) }
    var playMillis by remember { mutableStateOf(PLAY_MILLIS_DEFAULT) }
    val clock = remember { Animatable(0f) }

    LaunchedEffect(playing) {
        if (!playing) return@LaunchedEffect
        clock.snapTo(0f)
        clock.animateTo(1f, tween(playMillis, easing = LinearEasing)) { progress = value }
        playing = false
    }

    val frame = spec.frameForProgress(progress)

    // Pelna scena skrzyni po bossie z losowa nagroda - nic nie przyznaje, CLAIM tylko zamyka.
    var dropSlot by remember { mutableStateOf<ChestSlot?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Surface)
            .statusBarsPadding()
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedButton(
                onClick = onBack,
                colors = ButtonDefaults.outlinedButtonColors(contentColor = TextPrimary)
            ) {
                Text(text = "Wroc", fontFamily = FontFirst())
            }
            Text(
                text = "Chest Sprite Preview",
                color = TextPrimary,
                fontFamily = FontFirst(),
                style = MaterialTheme.typography.titleMedium
            )
        }

        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 420.dp)
                    .aspectRatio(CHEST_FRAME_RATIO)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF1E1E1E))
                    .clickable {
                        progress = 0f
                        playing = true
                    },
                contentAlignment = Alignment.Center
            ) {
                SpriteSheetFrame(
                    spec = spec,
                    frame = frame,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }

        Text(
            text = "Klatka ${frame + 1} / ${spec.frameCount}",
            color = Color(0xFF6D6D6D),
            fontFamily = FontFirst(),
            style = MaterialTheme.typography.bodySmall
        )
        Slider(
            value = progress,
            onValueChange = {
                playing = false
                progress = it
            },
            valueRange = 0f..1f
        )

        Text(
            text = "Czas odtwarzania: ${playMillis} ms",
            color = Color(0xFF6D6D6D),
            fontFamily = FontFirst(),
            style = MaterialTheme.typography.bodySmall
        )
        Slider(
            value = playMillis.toFloat(),
            onValueChange = { playMillis = it.roundToInt() },
            valueRange = PLAY_MILLIS_MIN.toFloat()..PLAY_MILLIS_MAX.toFloat()
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            OutlinedButton(
                onClick = { playing = false; progress = 0f },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = TextPrimary)
            ) {
                Text(text = "Reset", fontFamily = FontFirst())
            }
            Button(
                onClick = { progress = 0f; playing = true },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(containerColor = ButtonPrimary, contentColor = TextPrimary)
            ) {
                Text(text = if (playing) "Gra sie..." else "Odtworz", fontFamily = FontFirst())
            }
        }

        Button(
            onClick = { dropSlot = ChestWheel.roll(Random.Default, dungeonLevel = 10) },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = ButtonPrimary, contentColor = TextPrimary)
        ) {
            Text(text = "Test boss drop", fontFamily = FontFirst())
        }

        Text(
            text = "Zrodlo: ${spec.frameCount} klatek, siatka ${spec.columns}x${spec.rows}. " +
                "Recznie przesuwaj suwak albo tapnij w podglad, zeby odtworzyc otwarcie od poczatku.",
            color = Color(0xFF6D6D6D),
            fontFamily = FontFirst(),
            style = MaterialTheme.typography.bodySmall
        )
    }

    dropSlot?.let { slot ->
        val prize = remember(slot) { resolveChestPrize(slot, Random.Default) }
        BossChestOverlay(dungeonLevel = 10, slot = slot, prize = prize, onClaim = { dropSlot = null })
    }
}

private const val PLAY_MILLIS_DEFAULT = 2000
private const val PLAY_MILLIS_MIN = 200
private const val PLAY_MILLIS_MAX = 6000

/** Proporcja jednej klatki skrzyni (768x432 px w zrodlowym pliku) - szerokosc/wysokosc. */
private const val CHEST_FRAME_RATIO = 768f / 432f