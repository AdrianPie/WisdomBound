package com.lernhero.shared

/**
 * Czy to jest build deweloperski.
 *
 * Istnieje po to, zeby narzedzia dev (rzad linkow na ekranie Dom, przelacznik walki z bossem w
 * zakladce Trening) nie zalezaly od RECZNIE przestawianej stalej. Wczesniej kazdy taki rzad mial
 * wlasne `private const val SHOW_DEV_TOOLS = false`, ktore trzeba bylo pamietac zeby wlaczyc na
 * czas strojenia i - co wazniejsze - WYLACZYC przed wydaniem. Jedno przeoczenie i w sklepie ladowal
 * przycisk "dev: +zasoby", ktory jednym tapnieciem daje pelen worek surowcow i uniewaznia cala
 * ekonomie gry.
 *
 * Teraz odpowiada za to typ buildu, wiec nie da sie tego zapomniec.
 */
expect val isDebugBuild: Boolean
