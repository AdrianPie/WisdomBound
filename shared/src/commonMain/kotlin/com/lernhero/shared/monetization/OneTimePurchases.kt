package com.lernhero.shared.monetization

/**
 * Zakupy jednorazowe (np. Legend Pack) - osobno od subskrypcji, bo iOS-owy most Swift implementuje
 * [SubscriptionService] jako protokol i dopisanie tam metod zepsuloby jego kompilacje.
 */
interface OneTimePurchases {
    val isAvailable: Boolean

    /** Cena ze sklepu w walucie gracza albo null, gdy sklep jej nie zna. */
    suspend fun price(productId: String): String?

    suspend fun buy(productId: String): PurchaseOutcome

    /** Czy konto sklepu ma juz ten produkt (do przywrocenia po reinstalacji). */
    suspend fun owns(productId: String): Boolean

    object None : OneTimePurchases {
        override val isAvailable: Boolean = false
        override suspend fun price(productId: String): String? = null
        override suspend fun buy(productId: String): PurchaseOutcome = PurchaseOutcome.Failed("Store unavailable")
        override suspend fun owns(productId: String): Boolean = false
    }
}

expect fun platformOneTimePurchases(): OneTimePurchases
