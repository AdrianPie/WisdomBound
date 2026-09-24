package org.apie.lernhero

import android.app.Activity
import android.graphics.Color
import android.os.Bundle
import android.util.TypedValue
import android.widget.ScrollView
import android.widget.TextView

/**
 * Ekran "dlaczego apka chce dostepu do Health Connect". Android go wymaga: bez tej aktywnosci
 * (i aliasu w manifescie) system na Androidzie 14+ nie pokaze prosby o dostep do krokow,
 * a Google Play odrzuca aplikacje z uprawnieniami zdrowotnymi bez takiego wyjasnienia.
 */
class HealthPermissionsRationaleActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val padding = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 24f, resources.displayMetrics).toInt()
        val text = TextView(this).apply {
            setTextColor(Color.WHITE)
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f)
            setPadding(padding, padding, padding, padding)
            text = RATIONALE
        }
        setContentView(ScrollView(this).apply {
            setBackgroundColor(Color.parseColor("#19120C"))
            addView(text)
        })
    }

    private companion object {
        const val RATIONALE = """How WisdomBound uses Health Connect

WisdomBound reads your step count and exercise sessions to reward real-world activity with in-game bonuses on the Training tab.

- Data is read only. WisdomBound never writes to Health Connect.
- Steps and workouts are used on your device to calculate rewards. Raw health data is not sold, shared with advertisers or used for ads.
- You can revoke access at any time in Settings > Health Connect > App permissions.

Jak WisdomBound korzysta z Health Connect

Aplikacja odczytuje liczbe krokow i sesje treningowe, zeby nagradzac aktywnosc bonusami w grze (zakladka Trening). Dane sa tylko odczytywane, nie sa sprzedawane ani uzywane do reklam. Dostep mozesz cofnac w ustawieniach Health Connect."""
    }
}
