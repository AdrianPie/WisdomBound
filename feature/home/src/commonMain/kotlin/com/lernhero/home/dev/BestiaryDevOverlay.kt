package com.lernhero.home.dev

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lernhero.domain.monster.MonsterCatalog
import com.lernhero.game.character.CharacterState
import com.lernhero.game.character.MonsterCharacter
import com.lernhero.game.character.MonsterVisualPresets

/**
 * Dev: wszyscy kompani naraz, w animacji spoczynku - do strojenia [com.lernhero.game.character.IdleStyle]
 * bez szukania kazdego gatunku w walce. Tylko w buildzie debug (rzad linkow dev).
 */
@Composable
fun BestiaryDevOverlay(onClose: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF221A2A))
            .systemBarsPadding()
    ) {
        Text(
            text = "✕  Bestiary (${MonsterCatalog.all.size})",
            color = Color.White,
            fontSize = 16.sp,
            modifier = Modifier.fillMaxWidth().clickable(onClick = onClose).padding(12.dp)
        )
        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            items(MonsterCatalog.all) { card ->
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(modifier = Modifier.height(130.dp), contentAlignment = Alignment.BottomCenter) {
                        MonsterCharacter(
                            preset = MonsterVisualPresets.forSpecies(card.spriteKey),
                            state = CharacterState.IDLE,
                            attackTrigger = 0,
                            isEnemySide = true,
                            sizeDp = 90.dp
                        )
                    }
                    Text(text = card.name, color = Color.White, fontSize = 10.sp)
                }
            }
        }
    }
}
