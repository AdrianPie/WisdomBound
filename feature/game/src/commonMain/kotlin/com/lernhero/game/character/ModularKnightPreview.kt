package com.lernhero.game.character

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.lernhero.shared.resources.Resources.Player
import org.jetbrains.compose.resources.painterResource


// Pamiętaj o imporcie swojego wygenerowanego obiektu Res! np:
// import twojprojekt.generated.resources.*

@Composable
fun ModularKnightPreview() {
    Box(modifier = Modifier.size(300.dp)) {
        Image(
            painter = painterResource(Player.leftLeg),
            contentDescription = null,
            modifier = Modifier.fillMaxSize()
        )
        Image(
            painter = painterResource(Player.rightLeg),
            contentDescription = null,
            modifier = Modifier.fillMaxSize()
        )
        Image(
            painter = painterResource(Player.rightHand),
            contentDescription = null,
            modifier = Modifier.fillMaxSize()
        )
        Image(
            painter = painterResource(Player.body),
            contentDescription = null,
            modifier = Modifier.fillMaxSize()
        )
        Image(
            painter = painterResource(Player.head),
            contentDescription = null,
            modifier = Modifier.fillMaxSize()
        )
        Image(
            painter = painterResource(Player.leftHand),
            contentDescription = null,
            modifier = Modifier.fillMaxSize()
        )
    }
}
