package com.lernhero.home.navigation

import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Ile miejsca u dolu ekranu zaslania dolny pasek nawigacji (plyta + wciecie systemowe).
 *
 * Potrzebne ekranom, ktore przewijaja sie POD paskiem - a nie kończą się nad nim. Tresc ma
 * wjezdzac za brazowa plyte, bo inaczej lista urywa sie w powietrzu i miedzy nia a paskiem zieje
 * dziura; ostatnia pozycja musi jednak dac sie doscrollowac ponad pasek, i do tego sluzy ta
 * liczba - jako pusty ogon NA KONCU przewijanej tresci, a nie jako skrocenie calego ekranu.
 *
 * Podaje ja [HomeNavGraph] przy wejsciu w zakladke; poza nim jest zerem, wiec ekran uzyty gdzie
 * indziej (podglad, test) nie rezerwuje miejsca na pasek, ktorego tam nie ma.
 */
val LocalBottomBarSpace = compositionLocalOf { 0.dp }

/** Skrot do czytelnego uzycia w ekranach: `Spacer(Modifier.height(bottomBarSpace()))`. */
@androidx.compose.runtime.Composable
fun bottomBarSpace(): Dp = LocalBottomBarSpace.current
