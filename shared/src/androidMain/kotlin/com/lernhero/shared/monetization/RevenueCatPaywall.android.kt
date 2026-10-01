package com.lernhero.shared.monetization

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.revenuecat.purchases.Offering
import com.revenuecat.purchases.Purchases
import com.revenuecat.purchases.PurchasesException
import com.revenuecat.purchases.awaitOfferings
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.revenuecat.purchases.CustomerInfo
import com.revenuecat.purchases.models.StoreTransaction
import com.revenuecat.purchases.ui.revenuecatui.Paywall
import com.revenuecat.purchases.ui.revenuecatui.PaywallListener
import com.revenuecat.purchases.ui.revenuecatui.PaywallOptions

actual val revenueCatPaywallAvailable: Boolean get() = Purchases.isConfigured

/** Stan ladowania oferty dla placementu - paywall RevenueCat potrzebuje gotowego [Offering]. */
private sealed interface OfferingLoad {
    data object Loading : OfferingLoad
    data class Ready(val offering: Offering?) : OfferingLoad
    data object Failed : OfferingLoad
}

@Composable
actual fun RevenueCatPaywall(placementId: String, onDismiss: () -> Unit, fallback: @Composable () -> Unit) {
    if (!Purchases.isConfigured) {
        fallback()
        return
    }
    var load by remember(placementId) { mutableStateOf<OfferingLoad>(OfferingLoad.Loading) }
    LaunchedEffect(placementId) {
        load = try {
            val offerings = Purchases.sharedInstance.awaitOfferings()
            OfferingLoad.Ready(offerings.getCurrentOfferingForPlacement(placementId) ?: offerings.current)
        } catch (_: PurchasesException) {
            OfferingLoad.Failed
        }
    }
    when (val state = load) {
        // Oferty zwykle sa juz w pamieci SDK - to trwa klatke-dwie, nie ma czego pokazywac.
        OfferingLoad.Loading -> Unit
        OfferingLoad.Failed -> fallback()
        // Wlasne okno na CALY ekran zamiast PaywallDialog: tamten rysowal sie jak zwykly dialog -
        // spod spodu przebijal pasek gry, a krzyzyk u gory wyjezdzal poza ekran i trzeba bylo
        // przewijac. Tlo pod paskami systemu ma kolory gornej i dolnej krawedzi paywalla.
        is OfferingLoad.Ready -> Dialog(
            onDismissRequest = onDismiss,
            properties = DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Brush.verticalGradient(listOf(Color(0xFF2B1150), Color(0xFF0B0714))))
                    .systemBarsPadding()
            ) {
                Paywall(
                    PaywallOptions.Builder(dismissRequest = onDismiss)
                        .setShouldDisplayDismissButton(true)
                        .setListener(object : PaywallListener {
                            // Po zakupie okno ma zniknac - Premium wlacza sie samo (UpdatedCustomerInfoListener).
                            override fun onPurchaseCompleted(customerInfo: CustomerInfo, storeTransaction: StoreTransaction) {
                                onDismiss()
                            }
                        })
                        .apply { state.offering?.let(::setOffering) }
                        .build()
                )
            }
        }
    }
}
