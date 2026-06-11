import SwiftUI
import GoogleSignIn
import ComposeApp

@main
struct iOSApp: App {
    @UIApplicationDelegateAdaptor(AppDelegate.self) var delegate

    var body: some Scene {
        WindowGroup {
            ContentView()
        }
    }
}

class AppDelegate: NSObject, UIApplicationDelegate {
    // Intentionally empty. SwiftUI's `App` lifecycle uses UIScene-based URL
    // routing, so `application(_:open:)` is never invoked here — inbound
    // URLs are handled in `ContentView.onOpenURL` instead. Keeping the
    // adaptor around in case we add other delegate methods later (push
    // notifications, etc.).
}
