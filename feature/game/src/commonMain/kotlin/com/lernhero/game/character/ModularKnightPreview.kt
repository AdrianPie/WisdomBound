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
            painter = painterResource(Player.leftUpLeg),
            contentDescription = null,
            modifier = Modifier.fillMaxSize()
        )
        Image(
            painter = painterResource(Player.leftDownLeg),
            contentDescription = null,
            modifier = Modifier.fillMaxSize()
        )
        Image(
            painter = painterResource(Player.rightUpLeg),
            contentDescription = null,
            modifier = Modifier.fillMaxSize()
        )
        Image(
            painter = painterResource(Player.rightDownLeg),
            contentDescription = null,
            modifier = Modifier.fillMaxSize()
        )
        Image(
            painter = painterResource(Player.rightUpHand),
            contentDescription = null,
            modifier = Modifier.fillMaxSize()
        )
        Image(
            painter = painterResource(Player.rightDownHand),
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
            painter = painterResource(Player.leftUpHand),
            contentDescription = null,
            modifier = Modifier.fillMaxSize()
        )
        Image(
            painter = painterResource(Player.leftDownHand),
            contentDescription = null,
            modifier = Modifier.fillMaxSize()
        )
    }
}