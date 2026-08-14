package com.lernhero.home.reading

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

/**
 * Czy czytnik jest akurat otwarty na pelnym ekranie. `HomeNavGraph` czyta ta flage, zeby
 * schowac gorny i dolny pasek nawigacji podczas czytania - tekst ma zajmowac ekran, nie
 * dzielic go z chromem reszty aplikacji. `ReadingScreen` ja ustawia, bo to jedyne miejsce,
 * ktore wie, czy biblioteka pokazuje liste, czy otwarta ksiazke.
 */
object ReaderChrome {
    var immersive by mutableStateOf(false)
}
