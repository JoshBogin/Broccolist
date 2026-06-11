import UIKit
import SwiftUI
import GoogleSignIn
import ComposeApp

struct ComposeView: UIViewControllerRepresentable {
    func makeUIViewController(context: Context) -> UIViewController {
        return MainViewControllerKt.MainViewController(url: nil)
    }

    func updateUIViewController(_ uiViewController: UIViewController, context: Context) {}
}

struct ContentView: View {
    var body: some View {
        ComposeView()
            .ignoresSafeArea(.keyboard) // Compose has own keyboard handler
            .onOpenURL { url in
                // SwiftUI App lifecycle uses scene-based URL handling. The
                // AppDelegate's `application(_:open:)` is NOT invoked here —
                // we have to wire deep links through `.onOpenURL`.
                print("ContentView.onOpenURL: \(url)")
                if GIDSignIn.sharedInstance.handle(url) {
                    return
                }
                // Forward Supabase OAuth / magic-link / verification URLs.
                MainViewControllerKt.handleSupabaseDeeplink(url: url)
            }
    }
}
