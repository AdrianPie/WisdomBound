package org.apie.lernhero

import android.app.Application
import com.google.firebase.FirebaseApp
import com.lernhero.di.initializeKoin
import com.lernhero.shared.notification.ReminderContext
import com.lernhero.shared.notification.trackActivities
import org.koin.android.ext.koin.androidContext

class MyApp: Application() {
    override fun onCreate() {
        super.onCreate()
        // Kontekst dla warstwy powiadomien MUSI stanac przed Koinem: to z niego
        // platformReminderScheduler() buduje harmonogram wstrzykiwany do grafu.
        ReminderContext.application = this
        // Sledzenie widocznej Activity - o zgode na powiadomienia moze poprosic
        // tylko Activity, nie sam Context aplikacji.
        trackActivities()
        initializeKoin(
            config = {
                androidContext(this@MyApp)
            }
        )
        FirebaseApp.initializeApp(this)

    }
}