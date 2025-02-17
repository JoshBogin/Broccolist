import UIKit
import SwiftUI
import ComposeApp

struct ComposeView: UIViewControllerRepresentable {
    var url: URL?

    func makeUIViewController(context: Context) -> UIViewController {
        print("Deeplink: \(url)")
        return MainViewControllerKt.MainViewController(url: url ?? nil)
    }

    func updateUIViewController(_ uiViewController: UIViewController, context: Context) {
        print("Deeplink update: \(url)")
    }
}

struct ContentView: View {
    @State private var deepLinkURL: URL?
    
    var body: some View {
        ComposeView(url: deepLinkURL)
            .ignoresSafeArea(.keyboard) // Compose has own keyboard handler
            .onOpenURL { url in
                print("Received URL 123: \(url)")
                deepLinkURL = url
            }
    }
}
