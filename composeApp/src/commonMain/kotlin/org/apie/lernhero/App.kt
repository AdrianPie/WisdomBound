package org.apie.lernhero

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import org.jetbrains.compose.ui.tooling.preview.Preview
import com.lernhero.domain.data.PlayerRepository

import com.lernhero.shared.Screen
import com.lernhero.navigation.SetupNavGraph
import com.lernhero.shared.Constans
import com.mmk.kmpauth.google.GoogleAuthCredentials
import com.mmk.kmpauth.google.GoogleAuthProvider
import org.jetbrains.compose.resources.painterResource

import lernhero.composeapp.generated.resources.Res
import lernhero.composeapp.generated.resources.compose_multiplatform
import org.koin.compose.koinInject

@Composable
@Preview
fun App() {
    MaterialTheme {
        val playerRepository = koinInject<PlayerRepository>()
        var appReady by remember { mutableStateOf(false) }
        val userAuthenticated = remember { playerRepository.getUserId() != null }
        var startDestination by remember {
            mutableStateOf(
                if (userAuthenticated) {
                    Screen.HomeGraph
                } else {
                    Screen.Auth
                }
            )
        }
        LaunchedEffect(Unit){
            GoogleAuthProvider.create(
                credentials = GoogleAuthCredentials(serverId = Constans.WEB_CLIENT_ID)
            )
            appReady = true
        }
        AnimatedVisibility(
            modifier = Modifier.fillMaxSize(),
            visible = appReady
        ) {

            SetupNavGraph(
                startDestination = startDestination
            )
        }


    }
}
