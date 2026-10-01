import Foundation
import UIKit
import ComposeApp
#if canImport(RevenueCat)
import RevenueCat
#endif
#if canImport(RevenueCatUI)
import RevenueCatUI
#endif
#if canImport(GoogleMobileAds)
import GoogleMobileAds
#endif

/// Podpina RevenueCat (subskrypcja premium) i Google Mobile Ads (reklamy z nagroda) pod Kotlina -
/// patrz `shared/monetization/Monetization.ios.kt`. Wolane z AppDelegate PRZED startem Compose.
///
/// Oba SDK dochodza przez Swift Package Manager (File > Add Package Dependencies):
///  - https://github.com/RevenueCat/purchases-ios-spm  (produkt: RevenueCat)
///  - https://github.com/googleads/swift-package-manager-google-mobile-ads  (produkt: GoogleMobileAds)
/// Dopoki pakietow nie ma, `canImport` wycina ten kod i gra dziala bez sklepu i reklam.
enum MonetizationBridge {
    /// Publiczny klucz SDK RevenueCat dla iOS (appl_...). Ten sam co MonetizationConfig.REVENUECAT_IOS_KEY.
    static let revenueCatKey = "appl_igrzWtgdTKUxgJElwXFLyNrZrii"

    static func install() {
        #if canImport(RevenueCat)
        if !revenueCatKey.isEmpty {
            Purchases.configure(withAPIKey: revenueCatKey)
            Purchases.shared.delegate = PurchasesDelegateHolder.shared
            IosMonetizationBridge.shared.purchases = RevenueCatProvider()
            IosMonetizationBridge.shared.oneTime = RevenueCatOneTimeProvider()
            #if canImport(RevenueCatUI)
            // Paywalle projektowane w panelu RevenueCat - patrz RevenueCatPaywall.ios.kt.
            IosMonetizationBridge.shared.paywall = RevenueCatPaywallPresenter()
            #endif
        }
        #endif
        #if canImport(GoogleMobileAds)
        MobileAds.shared.start(completionHandler: nil)
        IosMonetizationBridge.shared.ads = AdMobProvider()
        #endif
    }
}

#if canImport(RevenueCat)
private func publish(_ info: CustomerInfo?) {
    let active = info?.entitlements.active[MonetizationConfig.shared.PREMIUM_ENTITLEMENT] != nil
    IosMonetizationBridge.shared.setPremium(active: active)
}

private final class PurchasesDelegateHolder: NSObject, PurchasesDelegate {
    static let shared = PurchasesDelegateHolder()
    func purchases(_ purchases: Purchases, receivedUpdated customerInfo: CustomerInfo) {
        publish(customerInfo)
    }
}

private final class RevenueCatProvider: NSObject, IosPurchasesProvider {
    private var packages: [String: Package] = [:]

    func logIn(userId: String, completion: @escaping () -> Void) {
        Purchases.shared.logIn(userId) { info, _, _ in
            publish(info)
            completion()
        }
    }

    func logOut(completion: @escaping () -> Void) {
        guard !Purchases.shared.isAnonymous else { completion(); return }
        Purchases.shared.logOut { _, _ in
            IosMonetizationBridge.shared.setPremium(active: false)
            completion()
        }
    }

    func loadOffers(completion: @escaping ([SubscriptionOffer]) -> Void) {
        Purchases.shared.getOfferings { offerings, _ in
            let available = offerings?.current?.availablePackages ?? []
            self.packages = Dictionary(uniqueKeysWithValues: available.map { ($0.identifier, $0) })
            completion(available.map { pkg in
                SubscriptionOffer(
                    id: pkg.identifier,
                    title: pkg.storeProduct.localizedTitle,
                    price: pkg.storeProduct.localizedPriceString,
                    period: Self.period(pkg.storeProduct.subscriptionPeriod),
                    priceMicros: Int64(truncating: (pkg.storeProduct.priceDecimalNumber.multiplying(byPowerOf10: 6))),
                    pricePerMonth: pkg.storeProduct.localizedPricePerMonth
                )
            })
        }
    }

    func purchase(offerId: String, completion: @escaping (String) -> Void) {
        guard let pkg = packages[offerId] else { completion("This offer is no longer available."); return }
        Purchases.shared.purchase(package: pkg) { _, info, error, cancelled in
            publish(info)
            if cancelled { completion("cancelled") }
            else if let error = error { completion(error.localizedDescription) }
            else { completion("success") }
        }
    }

    func restore(completion: @escaping () -> Void) {
        Purchases.shared.restorePurchases { info, _ in
            publish(info)
            completion()
        }
    }

    private static func period(_ period: SubscriptionPeriod?) -> String {
        switch period?.unit {
        case .day: return "day"
        case .week: return "week"
        case .month: return "month"
        case .year: return "year"
        default: return ""
        }
    }
}
#endif

#if canImport(RevenueCat)
/// Paczki diamentow i Legend Pack - te same identyfikatory produktow co w Google Play.
private final class RevenueCatOneTimeProvider: NSObject, IosOneTimeProvider {
    private var cache: [String: StoreProduct] = [:]

    private func product(_ id: String, _ completion: @escaping (StoreProduct?) -> Void) {
        if let cached = cache[id] { completion(cached); return }
        Purchases.shared.getProducts([id]) { products in
            let found = products.first { $0.productIdentifier == id }
            if let found = found { self.cache[id] = found }
            completion(found)
        }
    }

    func price(productId: String, completion: @escaping (String?) -> Void) {
        product(productId) { completion($0?.localizedPriceString) }
    }

    func buy(productId: String, completion: @escaping (String) -> Void) {
        product(productId) { product in
            guard let product = product else { completion("This offer is not available yet."); return }
            Purchases.shared.purchase(product: product) { _, info, error, cancelled in
                publish(info)
                if cancelled { completion("cancelled") }
                else if let error = error { completion(error.localizedDescription) }
                else { completion("success") }
            }
        }
    }

    func owns(productId: String, completion: @escaping (KotlinBoolean) -> Void) {
        Purchases.shared.getCustomerInfo { info, _ in
            let owned = info?.nonSubscriptions.contains { $0.productIdentifier == productId } ?? false
            completion(KotlinBoolean(value: owned))
        }
    }
}
#endif

#if canImport(GoogleMobileAds)
private final class AdMobProvider: NSObject, IosAdsProvider, FullScreenContentDelegate {
    private var completion: ((String) -> Void)?
    private var earned = false

    func showRewarded(adUnitId: String, completion: @escaping (String) -> Void) {
        RewardedAd.load(with: adUnitId, request: Request()) { ad, _ in
            DispatchQueue.main.async {
                guard let ad = ad, let root = Self.topController() else { completion("unavailable"); return }
                self.completion = completion
                self.earned = false
                ad.fullScreenContentDelegate = self
                ad.present(from: root) { self.earned = true }
            }
        }
    }

    func adDidDismissFullScreenContent(_ ad: FullScreenPresentingAd) {
        finish(earned ? "rewarded" : "skipped")
    }

    func ad(_ ad: FullScreenPresentingAd, didFailToPresentFullScreenContentWithError error: Error) {
        finish("unavailable")
    }

    private func finish(_ result: String) {
        completion?(result)
        completion = nil
    }

    private static func topController() -> UIViewController? {
        let scene = UIApplication.shared.connectedScenes.first { $0.activationState == .foregroundActive } as? UIWindowScene
        var top = scene?.windows.first { $0.isKeyWindow }?.rootViewController
        while let presented = top?.presentedViewController { top = presented }
        return top
    }
}
#endif

#if canImport(RevenueCat) && canImport(RevenueCatUI)
/// Paywall zaprojektowany w panelu RevenueCat (RevenueCatUI) - modal `PaywallViewController` nad
/// cala aplikacja. Wyglad, ceny i testy A/B zmienia sie w panelu, bez nowego builda.
///
/// Oferta idzie z placementu (Targeting -> Placements w panelu); bez placementu - biezaca oferta.
/// Po zakupie/przywroceniu Premium wlacza sie przez `publish` (i tak samo przez `PurchasesDelegate`),
/// a `onDismiss` leci do Kotlina dokladnie raz - niezaleznie od tego, jak okno zniknelo.
private final class RevenueCatPaywallPresenter: NSObject, IosPaywallPresenter, PaywallViewControllerDelegate {
    private weak var shown: PaywallViewController?
    private var onDismiss: (() -> Void)?

    func present(placementId: String, onDismiss: @escaping () -> Void) {
        finish() // poprzedni paywall, jesli jakis jeszcze wisial
        self.onDismiss = onDismiss
        Purchases.shared.getOfferings { [weak self] offerings, _ in
            DispatchQueue.main.async {
                // Kotlin zdazyl zamknac paywall, zanim oferty przyszly - nic nie pokazujemy.
                guard let self = self, self.onDismiss != nil else { return }
                guard let top = Self.topController() else { self.finish(); return }
                let offering = offerings?.currentOffering(forPlacement: placementId) ?? offerings?.current
                let controller = PaywallViewController(offering: offering, displayCloseButton: true)
                controller.delegate = self
                self.shown = controller
                top.present(controller, animated: true)
            }
        }
    }

    func dismiss() {
        // Zamyka Kotlin (ekran zniknal) - nie odsylamy mu jego wlasnego zamkniecia.
        onDismiss = nil
        shown?.dismiss(animated: true)
        shown = nil
    }

    func paywallViewController(_ controller: PaywallViewController, didFinishPurchasingWith customerInfo: CustomerInfo) {
        publish(customerInfo)
    }

    func paywallViewController(_ controller: PaywallViewController, didFinishRestoringWith customerInfo: CustomerInfo) {
        publish(customerInfo)
    }

    func paywallViewControllerWasDismissed(_ controller: PaywallViewController) {
        finish()
    }

    private func finish() {
        let callback = onDismiss
        onDismiss = nil
        shown = nil
        callback?()
    }

    private static func topController() -> UIViewController? {
        let scene = UIApplication.shared.connectedScenes.first { $0.activationState == .foregroundActive } as? UIWindowScene
        var top = scene?.windows.first { $0.isKeyWindow }?.rootViewController
        while let presented = top?.presentedViewController { top = presented }
        return top
    }
}
#endif
