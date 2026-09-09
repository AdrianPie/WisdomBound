package com.lernhero.shared.notification

import platform.UserNotifications.UNAuthorizationOptionAlert
import platform.UserNotifications.UNAuthorizationOptionBadge
import platform.UserNotifications.UNAuthorizationOptionSound
import platform.UserNotifications.UNUserNotificationCenter

/**
 * iOS pokazuje swoje okienko sam, przy pierwszym wywolaniu. Kolejne wywolania nic nie robia -
 * oddaja tylko zapisana wczesniej odpowiedz, wiec ponowne tapniecie w przelacznik nie meczy gracza.
 */
actual fun requestNotificationAccess() {
    UNUserNotificationCenter.currentNotificationCenter().requestAuthorizationWithOptions(
        options = UNAuthorizationOptionAlert or UNAuthorizationOptionBadge or UNAuthorizationOptionSound,
        completionHandler = { _, _ -> }
    )
}
