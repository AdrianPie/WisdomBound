package com.lernhero.home.reading

import java.time.LocalDate
import java.time.format.DateTimeFormatter

actual fun todayDateKey(): String = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)
