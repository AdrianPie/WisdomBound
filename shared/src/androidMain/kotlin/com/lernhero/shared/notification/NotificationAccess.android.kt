package com.lernhero.shared.notification

import android.Manifest
import android.app.Activity
import android.app.Application
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.core.app.ActivityCompat

/**
 * Biezaca widoczna Activity. Potrzebna, bo o uprawnienie POST_NOTIFICATIONS moze poprosic TYLKO
 * Activity - z samego Contextu aplikacji system nie pokaze okienka.
 *
 * Rejestruje sie samo w [trackActivities], wolanym raz z MyApp; nie trzyma referencji po
 * zatrzymaniu ekranu, wiec nie przecieka.
 */
internal object CurrentActivity {
    @Volatile
    var value: Activity? = null
}

/** Wolane raz z MyApp.onCreate - patrz [CurrentActivity]. */
fun Application.trackActivities() {
    registerActivityLifecycleCallbacks(object : Application.ActivityLifecycleCallbacks {
        override fun onActivityResumed(activity: Activity) {
            CurrentActivity.value = activity
        }

        override fun onActivityPaused(activity: Activity) {
            if (CurrentActivity.value === activity) CurrentActivity.value = null
        }

        override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) = Unit
        override fun onActivityStarted(activity: Activity) = Unit
        override fun onActivityStopped(activity: Activity) = Unit
        override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) = Unit
        override fun onActivityDestroyed(activity: Activity) = Unit
    })
}

actual fun requestNotificationAccess() {
    val context = ReminderContext.application ?: return
    val activity = CurrentActivity.value

    // Ponizej Androida 13 nie ma o co prosic - powiadomienia sa domyslnie wlaczone. Gracz mogl je
    // jednak wylaczyc recznie, wiec jedyne, co ma sens, to otworzyc ustawienia aplikacji.
    val needsRuntimePermission = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU

    if (needsRuntimePermission && activity != null &&
        ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.POST_NOTIFICATIONS)
    ) {
        // Gracz odmowil raz - system nie pokaze juz okienka, zostaja ustawienia.
        openAppNotificationSettings(context)
        return
    }

    if (needsRuntimePermission && activity != null) {
        ActivityCompat.requestPermissions(
            activity,
            arrayOf(Manifest.permission.POST_NOTIFICATIONS),
            REQUEST_CODE
        )
        return
    }

    openAppNotificationSettings(context)
}

private fun openAppNotificationSettings(context: android.content.Context) {
    val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
        .putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

    val fallback = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        .setData(Uri.fromParts("package", context.packageName, null))
        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

    runCatching { context.startActivity(intent) }
        .recoverCatching { context.startActivity(fallback) }
}

private const val REQUEST_CODE = 4711
