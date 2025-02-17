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
    // Add this method to handle deep links when app is not running
    func application(
        _ application: UIApplication,
        didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey: Any]? = nil
    ) -> Bool {
        // Check if we launched from a deep link
        if let url = launchOptions?[.url] as? URL {
            handleDeepLink(url)
        }
        return true
    }
    
    // Handle deep links when app is in background
    func application(
        _ app: UIApplication,
        open url: URL,
        options: [UIApplication.OpenURLOptionsKey: Any] = [:]
    ) -> Bool {
        print("AppDelegate received URL: \(url)")
        handleDeepLink(url)
        return true
    }
    
    private func handleDeepLink(_ url: URL) {
        print(url)
        let client = DependencyProvider.shared.provideSupabaseClient()
//        AppleKt.handleDeeplinks(client, url: url.absoluteString) { result in
//            print("Deeplink handled with result: \(result)")
//        }
    }
}
