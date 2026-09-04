package com.lernhero.auth

/**
 * Czy da sie tu zalogowac przez Apple ID. Przycisk Apple ma sens WYLACZNIE na iOS - na Androidzie
 * i desktopie kmpauth nie ma czego wywolac, a pusty przycisk logowania jest gorszy niz jego brak.
 */
expect fun supportsAppleSignIn(): Boolean

/**
 * Czy to build deweloperski. Na tym wisi skrot z ekranu logowania prosto do tworzenia postaci -
 * z zalozenia OMIJA logowanie, wiec nie moze byc zwyklym `if (true)`, ktory kiedys pojedzie na
 * produkcje. W release'ie funkcja zwraca false i przycisku po prostu nie ma.
 */
expect fun isDebugBuild(): Boolean
