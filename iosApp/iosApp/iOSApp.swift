import SwiftUI
import GoogleSignIn
import ComposeApp

@main
struct iOSApp: App {
    @UIApplicationDelegateAdaptor(AppDelegate.self) var delegate
    
    var body: some Scene {
        WindowGroup {
//            ContentView().onOpenURL { url in
//                print("url 1: \(url)")
//                GIDSignIn.sharedInstance.handle(url)
//            }
            ContentView().onOpenURL { url in
                print("Received URL: \(url)")
                
                if url.scheme?.hasPrefix("io.supabase") ?? false {
                    print("Handling Supabase URL")
                    let client = DependencyProvider.shared.provideSupabaseClient()
                    let urlReturn = GIDSignIn.sharedInstance.handle(url)
                    print(urlReturn)
//                    client.onDeepLink(url: url.absoluteString)
                } else {
                    print("Handling Google URL")
                    GIDSignIn.sharedInstance.handle(url)
                }
            }
        }
    }
}

class AppDelegate: NSObject, UIApplicationDelegate {
    func application(
        _ application: UIApplication,
        didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey: Any]? = nil
    ) -> Bool {
        // Configure Google Sign-In
        GIDSignIn.sharedInstance.configuration = GIDConfiguration(
            clientID: "299296791038-hi3i5rcti7g827hv1on3ckl5pjov4lrq.apps.googleusercontent.com"
        )
        return true
    }

    func application(
        _ app: UIApplication,
        open url: URL,
        options: [UIApplication.OpenURLOptionsKey: Any] = [:]
    ) -> Bool {
        print("AppDelegate received URL: \(url)")
        // Handle other custom URL types.
//        var client = DependencyProvider.shared.provideSupabaseClient()
//        client.handleDeeplinks(url)
        return true
    }
}

//class AppDelegate: NSObject, UIApplicationDelegate {
//
//    func application(
//      _ app: UIApplication,
//      open url: URL,
//      options: [UIApplication.OpenURLOptionsKey : Any] = [:]
//    ) -> Bool {
//        print("url 2: \(url)")
//        
//        var handled: Bool
//
//        // Let Google Sign-In handle the URL if it's related to Google Sign-In
//        handled = GIDSignIn.sharedInstance.handle(url)
//        if handled {
//            return true
//        }
//        
//        // Handle other custom URL types.
//        var client = DependencyProvider.shared.provideSupabaseClient()
//        client.handleDeeplinks(url)
//        
//        // If not handled by this app, return false.
//        return false
//    }
//}
