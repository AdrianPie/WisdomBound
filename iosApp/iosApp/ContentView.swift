import UIKit
import SwiftUI
import ComposeApp

struct ComposeView: UIViewControllerRepresentable {
    func makeUIViewController(context: Context) -> UIViewController {
        MainViewControllerKt.MainViewController()
    }

    func updateUIViewController(_ uiViewController: UIViewController, context: Context) {}
}

private let statusBarNotificationName = Notification.Name("LernHeroStatusBarHidden")

struct ContentView: View {
    @State private var statusBarHidden = false

    var body: some View {
        ComposeView()
            .overlay(TranslationHostView(host: TranslationBridge.host))
            .ignoresSafeArea()
            .statusBarHidden(statusBarHidden)
            .onReceive(NotificationCenter.default.publisher(for: statusBarNotificationName)) { notification in
                if let hidden = notification.userInfo?["hidden"] as? Bool {
                    statusBarHidden = hidden
                }
            }
    }
}
