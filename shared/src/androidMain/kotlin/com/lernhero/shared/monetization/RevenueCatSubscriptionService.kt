package com.lernhero.shared.monetization

import com.lernhero.shared.notification.CurrentActivity
import com.revenuecat.purchases.CustomerInfo
import com.revenuecat.purchases.Package
import com.revenuecat.purchases.PurchaseParams
import com.revenuecat.purchases.Purchases
import com.revenuecat.purchases.PurchasesException
import com.revenuecat.purchases.PurchasesTransactionException
import com.revenuecat.purchases.awaitCustomerInfo
import com.revenuecat.purchases.awaitLogIn
import com.revenuecat.purchases.awaitLogOut
import com.revenuecat.purchases.awaitOfferings
import com.revenuecat.purchases.awaitPurchase
import com.revenuecat.purchases.awaitRestore
import com.revenuecat.purchases.interfaces.UpdatedCustomerInfoListener
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

internal class RevenueCatSubscriptionService : SubscriptionService {

    private val purchases get() = Purchases.sharedInstance
    private val premium = MutableStateFlow(false)

    /** Pakiety z ostatniego [offers] - zakup potrzebuje obiektu SDK, a UI zna tylko jego id. */
    private var packages: Map<String, Package> = emptyMap()

    override val isAvailable: Boolean = true
    override val isPremium: StateFlow<Boolean> = premium.asStateFlow()

    init {
        purchases.updatedCustomerInfoListener = UpdatedCustomerInfoListener(::publish)
    }

    override suspend fun identify(userId: String?) {
        try {
            when {
                userId == null -> if (!purchases.isAnonymous) purchases.awaitLogOut()
                userId != purchases.appUserID -> publish(purchases.awaitLogIn(userId).customerInfo)
                else -> publish(purchases.awaitCustomerInfo())
            }
            if (userId == null) premium.value = false
        } catch (_: PurchasesException) {
        }
    }

    override suspend fun offers(): List<SubscriptionOffer> = try {
        val available = purchases.awaitOfferings().current?.availablePackages.orEmpty()
        packages = available.associateBy { it.identifier }
        available.map { pkg ->
            SubscriptionOffer(
                id = pkg.identifier,
                title = pkg.product.title,
                price = pkg.product.price.formatted,
                period = pkg.product.period?.unit?.name?.lowercase().orEmpty()
            )
        }
    } catch (_: PurchasesException) {
        emptyList()
    }

    override suspend fun purchase(offerId: String): PurchaseOutcome {
        val activity = CurrentActivity.value ?: return PurchaseOutcome.Failed("App is not in the foreground.")
        val pkg = packages[offerId] ?: return PurchaseOutcome.Failed("This offer is no longer available.")
        return try {
            publish(purchases.awaitPurchase(PurchaseParams.Builder(activity, pkg).build()).customerInfo)
            PurchaseOutcome.Success
        } catch (e: PurchasesTransactionException) {
            if (e.userCancelled) PurchaseOutcome.Cancelled else PurchaseOutcome.Failed(e.error.message)
        } catch (e: PurchasesException) {
            PurchaseOutcome.Failed(e.error.message)
        }
    }

    override suspend fun restore(): Boolean = try {
        publish(purchases.awaitRestore())
        premium.value
    } catch (_: PurchasesException) {
        false
    }

    private fun publish(info: CustomerInfo) {
        premium.value = info.entitlements.active.containsKey(MonetizationConfig.PREMIUM_ENTITLEMENT)
    }
}
