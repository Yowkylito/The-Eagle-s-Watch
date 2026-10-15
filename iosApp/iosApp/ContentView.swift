import UIKit
import SwiftUI
import ComposeApp

struct ComposeView: UIViewControllerRepresentable {
    func makeUIViewController(context: Context) -> UIViewController {
        MainViewControllerKt.MainViewController()
    }

    func updateUIViewController(_ uiViewController: UIViewController, context: Context) {}
}

struct ContentView: View {
    var body: some View {
        ComposeView()
            .ignoresSafeArea()
            .background(Color(red: 12 / 255, green: 43 / 255, blue: 43 / 255))
            // Light status-bar text over the night-jade header bands.
            .preferredColorScheme(.dark)
    }
}
