package org.apie.lernhero

import com.lernhero.shared.monetization.gatherAdConsent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.view.WindowCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        setUpEdgeToEdge()

        super.onCreate(savedInstanceState)

        setContent {
            App()
        }
        // Na wszelki wypadek rowniez w kodzie, nie tylko w motywie: MIUI potrafi wymuszac tryb
        // ciemny mimo ustawien motywu, a to odwraca kolory gry (bialy tekst na bialym tle).
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            window.decorView.isForceDarkAllowed = false
        }
        gatherAdConsent()
    }

    /**
     * Gra rysuje sie pod paskami systemowymi (bez ramki), paski sa przezroczyste z jasnymi ikonami.
     *
     * Od Androida 15 system robi to sam dla targetSdk 35+, a biblioteczne enableEdgeToEdge() i tak
     * wola tam wycofane Window.setStatusBarColor/setNavigationBarColor - Play Console zglasza to
     * jako "wycofane API wyswietlania bez ramki". Na 15+ ustawiamy wiec tylko to, czego system
     * nie zrobi za nas (jasne ikony na ciemnej grze, brak szarej nakladki pod 3 przyciskami);
     * enableEdgeToEdge() zostaje dla starszych wersji, na ktorych innej drogi nie ma.
     */
    private fun setUpEdgeToEdge() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.VANILLA_ICE_CREAM) {
            WindowCompat.getInsetsController(window, window.decorView).apply {
                isAppearanceLightStatusBars = false
                isAppearanceLightNavigationBars = false
            }
            window.isNavigationBarContrastEnforced = false
            window.isStatusBarContrastEnforced = false
        } else {
            enableEdgeToEdge(
                statusBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
                navigationBarStyle = SystemBarStyle.dark(Color.TRANSPARENT)
            )
        }
    }
}

@Preview
@Composable
fun AppAndroidPreview() {
    App()
}
