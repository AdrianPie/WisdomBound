package com.lernhero.shared.monetization

import androidx.compose.runtime.Composable

/**
 * Paywall zaprojektowany w panelu RevenueCat (RevenueCatUI - "Paywalls").
 *
 * Wyglad, teksty, ceny, kolejnosc planow i testy A/B ustawia sie w panelu, bez nowego builda;
 * zakup i przywracanie obsluguje sam RevenueCat, a Premium w grze wlacza sie przez zwykly nasluch
 * zmian klienta (patrz RevenueCatSubscriptionService / MonetizationBridge.swift).
 *
 * [placementId] wybiera oferte przez placement z panelu; gdy placementu nie ma - biezaca oferta.
 * [fallback] rysuje sie zamiast, gdy paywalla RevenueCat nie da sie pokazac (brak klucza/SDK na tej
 * platformie, blad ladowania ofert) - wtedy gracz dostaje nasz wlasny paywall, a nie pusty ekran.
 */
@Composable
expect fun RevenueCatPaywall(placementId: String, onDismiss: () -> Unit, fallback: @Composable () -> Unit)

/** Czy na tej platformie w ogole jest paywall RevenueCat (skonfigurowany SDK + RevenueCatUI). */
expect val revenueCatPaywallAvailable: Boolean
