package com.lernhero.reader

import platform.Foundation.NSDate
import platform.Foundation.NSDateFormatter

actual fun todayDateKey(): String {
    val formatter = NSDateFormatter()
    formatter.dateFormat = "yyyy-MM-dd"
    return formatter.stringFromDate(NSDate())
}
