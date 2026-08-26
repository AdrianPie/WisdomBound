import ComposeApp
import Foundation
import SwiftUI
import Translation

/// Podpina prawdziwe tlumaczenie w czytniku pod Apple `Translation` framework - w pelni offline,
/// na urzadzeniu, bez klucza API. Kotlin/Native nie moze wywolac tego frameworka bezposrednio
/// (to czysty Swift bez naglowka Objective-C), wiec most idzie w odwrotna strone: Kotlin
/// (`TranslationSource.ios.kt`) odpytuje `IosTranslationBridge.shared.handler`, ktory tutaj
/// jest podpiety pod [TranslationTaskHost].
///
/// `TranslationSession` samodzielnie da sie stworzyc dopiero od iOS 26 (i tylko gdy pakiet
/// jezykowy jest juz zainstalowany - bez mozliwosci poprosic o pobranie). Zamiast tego, jak
/// zaleca Apple, sesje otrzymujemy przez modyfikator SwiftUI `.translationTask` - dziala od
/// iOS 18 (pokrywa caly zakres wspierany przez apke) i system SAM pokazuje uzytkownikowi
/// prompt pobrania pakietu jezykowego, gdy jest potrzebny.
enum TranslationBridge {
    @MainActor static let host = TranslationTaskHost()

    static func install() {
        IosTranslationBridge.shared.handler = { text, target, onResult in
            Task { @MainActor in
                host.requestTranslation(text: text, target: target) { result in
                    _ = onResult(result)
                }
            }
        }
    }
}

/// Widok bez wlasnego wygladu (rozmiar 0x0) doczepiony gdzies w drzewie widokow apki - `.translationTask`
/// dziala tylko jako modyfikator widoku, wiec potrzebuje BYC gdzies w hierarchii, mimo ze
/// tlumaczenie jest wywolywane z zupelnie innego miejsca (Kotlinowego czytnika).
struct TranslationHostView: View {
    @ObservedObject var host: TranslationTaskHost

    var body: some View {
        Color.clear
            .frame(width: 0, height: 0)
            .translationTask(host.configuration) { session in
                await host.onSessionReady(session)
            }
    }
}

/// Kolejkuje prosby o tlumaczenie do czasu, az SwiftUI dostarczy `TranslationSession` dla danej
/// pary jezykow (zmiana [configuration] budzi `.translationTask` na nowo). Sesja dla juz znanej
/// pary jest trzymana i uzywana wielokrotnie - nie tworzymy nowej na kazde dotkniecie slowa.
@MainActor
final class TranslationTaskHost: ObservableObject {
    @Published private(set) var configuration: TranslationSession.Configuration?

    private var readySessions: [String: TranslationSession] = [:]
    private var pendingByTarget: [String: [(String, (String?) -> Void)]] = [:]

    func requestTranslation(text: String, target: String, onResult: @escaping (String?) -> Void) {
        if let session = readySessions[target] {
            translate(session: session, text: text, onResult: onResult)
            return
        }
        pendingByTarget[target, default: []].append((text, onResult))
        configuration = TranslationSession.Configuration(
            source: Locale.Language(identifier: "en"),
            target: Locale.Language(identifier: target)
        )
    }

    func onSessionReady(_ session: TranslationSession) async {
        guard let target = session.targetLanguage?.languageCode?.identifier else { return }
        readySessions[target] = session
        let queued = pendingByTarget.removeValue(forKey: target) ?? []
        for (text, onResult) in queued {
            translate(session: session, text: text, onResult: onResult)
        }
    }

    private func translate(session: TranslationSession, text: String, onResult: @escaping (String?) -> Void) {
        Task {
            do {
                let response = try await session.translate(text)
                onResult(response.targetText)
            } catch {
                NSLog("TranslationBridge error: %@", String(describing: error))
                onResult(nil)
            }
        }
    }
}
