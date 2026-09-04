package com.lernhero.game

import androidx.compose.runtime.Composable

/**
 * Ekran walki jest teraz pionowy (formacja u gory, quiz na dole) - tak samo jak reszta aplikacji,
 * ktora i tak jest zablokowana w manifescie na "portrait". Nie ma juz czego wymuszac recznie.
 */
@Composable
actual fun FightScreenOrientation() = Unit
