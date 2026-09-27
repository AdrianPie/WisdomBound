import Foundation
import UIKit
import ComposeApp
#if canImport(RevenueCat)
import RevenueCat
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
    static let revenueCatKey = ""

    static func install() {
        #if canImport(RevenueCat)
        if !revenueCatKey.isEmpty {
            Purchases.configure(withAPIKey: revenueCatKey)
            Purchases.shared.delegate = PurchasesDelegateHolder.shared
            IosMonetizationBridge.shared.purchases = RevenueCatProvider()
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
