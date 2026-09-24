package com.lernhero.shared.monetization

import com.lernhero.shared.notification.CurrentActivity
import com.revenuecat.purchases.ProductType
import com.revenuecat.purchases.PurchaseParams
import com.revenuecat.purchases.Purchases
import com.revenuecat.purchases.PurchasesException
import com.revenuecat.purchases.PurchasesTransactionException
import com.revenuecat.purchases.awaitCustomerInfo
import com.revenuecat.purchases.awaitGetProducts
import com.revenuecat.purchases.awaitPurchase
import com.revenuecat.purchases.models.StoreProduct

internal class RevenueCatOneTimePurchases : OneTimePurchases {

    private val purchases get() = Purchases.sharedInstance

    override val isAvailable: Boolean = true

    private suspend fun product(productId: String): StoreProduct? = try {
        purchases.awaitGetProducts(listOf(productId), ProductType.INAPP).firstOrNull()
    } catch (_: PurchasesException) {
        null
    }

    override suspend fun price(productId: String): String? = product(productId)?.price?.formatted

    override suspend fun buy(productId: String): PurchaseOutcome {
        val activity = CurrentActivity.value ?: return PurchaseOutcome.Failed("App is not in the foreground.")
        val product = product(productId) ?: return PurchaseOutcome.Failed("This offer is not available yet.")
        return try {
            purchases.awaitPurchase(PurchaseParams.Builder(activity, product).build())
            PurchaseOutcome.Success
        } catch (e: PurchasesTransactionException) {
            if (e.userCancelled) PurchaseOutcome.Cancelled else PurchaseOutcome.Failed(e.error.message)
        } catch (e: PurchasesException) {
            PurchaseOutcome.Failed(e.error.message)
        }
    }

    override suspend fun owns(productId: String): Boolean = try {
        purchases.awaitCustomerInfo().nonSubscriptionTransactions.any { it.productIdentifier == productId }
    } catch (_: PurchasesException) {
        false
    }
}
