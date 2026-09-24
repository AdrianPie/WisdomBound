package com.lernhero.shared.monetization

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Subskrypcja premium przez RevenueCat. Apka pyta o [isPremium], zeby pokazac/ukryc limity, ale
 * NIE jest ostateczna instancja tam, gdzie limit cos kosztuje - generowanie AI sprawdza serwer
 * wprost w RevenueCat (patrz :server Entitlements). Tlumaczenie dziala na urzadzeniu i nic nie
 * kosztuje, wiec tam wystarczy to, co mowi apka.
 */
interface SubscriptionService {
    /** False, gdy sklep nie jest skonfigurowany na tej platformie (brak klucza, desktop). */
    val isAvailable: Boolean

    val isPremium: StateFlow<Boolean>

    /** Wiaze zakupy z kontem gracza (uid Firebase), zeby subskrypcja szla za nim na inne urzadzenie. */
    suspend fun identify(userId: String?)

    suspend fun offers(): List<SubscriptionOffer>

    suspend fun purchase(offerId: String): PurchaseOutcome

    /** Przywraca zakupy z konta sklepu. True = po przywroceniu gracz ma premium. */
    suspend fun restore(): Boolean

    /** Brak sklepu: nigdy premium, nic do kupienia. */
    object None : SubscriptionService {
        override val isAvailable: Boolean = false
        override val isPremium: StateFlow<Boolean> = MutableStateFlow(false).asStateFlow()
        override suspend fun identify(userId: String?) = Unit
        override suspend fun offers(): List<SubscriptionOffer> = emptyList()
        override suspend fun purchase(offerId: String): PurchaseOutcome = PurchaseOutcome.Failed("Store unavailable")
        override suspend fun restore(): Boolean = false
    }
}

/** Jeden pakiet z oferty RevenueCat (miesieczny, roczny...). */
data class SubscriptionOffer(
    val id: String,
    val title: String,
    /** Cena sformatowana przez sklep, w walucie gracza ("19,99 zl"). */
    val price: String,
    /** "month", "year", "week" - albo pusty, gdy sklep nie podal okresu. */
    val period: String
)

sealed interface PurchaseOutcome {
    data object Success : PurchaseOutcome
    data object Cancelled : PurchaseOutcome
    data class Failed(val reason: String) : PurchaseOutcome
}

expect fun platformSubscriptionService(): SubscriptionService
