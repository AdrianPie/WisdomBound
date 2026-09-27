package org.apie.lernhero

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import org.jetbrains.compose.ui.tooling.preview.Preview
import com.lernhero.domain.repository.PlayerRepository

import com.lernhero.shared.navigation.Screen
import com.lernhero.navigation.SetupNavGraph
import com.lernhero.shared.Constants
import com.mmk.kmpauth.google.GoogleAuthCredentials
import com.mmk.kmpauth.google.GoogleAuthProvider
import org.jetbrains.compose.resources.painterResource

import lernhero.composeapp.generated.resources.Res
import lernhero.composeapp.generated.resources.compose_multiplatform
import org.koin.compose.koinInject
import com.lernhero.shared.audio.Sounds
import com.lernhero.shared.prefs.AppPreferences
import com.lernhero.shared.i18n.AppLanguageStore
import com.lernhero.shared.i18n.ProvideAppStrings

@Composable
@Preview
fun App() {
    val languageStore = koinInject<AppLanguageStore>()
    val preferences = koinInject<AppPreferences>()
    LaunchedEffect(Unit) {
        // Dzwieki czytaja przelacznik z Ustawien na biezaco - wylaczenie dziala od razu.
        Sounds.isEnabled = { preferences.soundEnabled }
        Sounds.preload()
    }
    ProvideAppStrings(languageStore) {
    MaterialTheme {
        val playerRepository = koinInject<PlayerRepository>()
        var appReady by remember { mutableStateOf(false) }
        var startDestination by remember { mutableStateOf<Screen>(Screen.Auth) }
        LaunchedEffect(Unit){
            GoogleAuthProvider.create(
                credentials = GoogleAuthCredentials(serverId = Constants.WEB_CLIENT_ID)
            )
            // Zalogowany z postacia -> Home; zalogowany bez postaci -> tworzenie
            // postaci; niezalogowany -> ekran logowania.
            startDestination = if (playerRepository.getUserId() != null) {
                if (playerRepository.getPlayer()?.character != null) {
                    Screen.HomeGraph
                } else {
                    Screen.CharacterCreation()
                }
            } else {
                Screen.Auth
            }
            appReady = true
        }
        AnimatedVisibility(
            // Gra rysuje sie pod paskami systemowymi, ale NIE pod dolna nawigacja telefonu: na
            // telefonach z trzema przyciskami zakrywala dolne menu i przyciski typu CONFIRM.
            // Odstep tutaj, w korzeniu, zalatwia to dla kazdego ekranu naraz; pasek pod spodem
            // dostaje ciemne tlo gry zamiast przypadkowego fragmentu ekranu.
            modifier = Modifier
                .fillMaxSize()
                .background(AppBackground)
                .windowInsetsPadding(WindowInsets.navigationBars),
            visible = appReady
        ) {

            SetupNavGraph(
                startDestination = startDestination
            )
        }


    }
    }
}

/** To samo co tlo okna w motywie Androida (splash.xml) - bez przeskoku koloru przy starcie. */
private val AppBackground = Color(0xFF0E1626)
