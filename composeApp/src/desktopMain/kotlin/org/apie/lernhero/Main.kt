package org.apie.lernhero

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowState
import androidx.compose.ui.window.application
import com.lernhero.di.initializeKoin
import com.lernhero.navigation.SetupNavGraph
import com.lernhero.shared.Screen

fun main() = application {
    initializeKoin()

    Window(
        onCloseRequest = ::exitApplication,
        title = "LernHero",
        state = WindowState(width = 1280.dp, height = 800.dp)
    ) {
        MaterialTheme {
            SetupNavGraph(startDestination = Screen.HomeGraph)
        }
    }
}
