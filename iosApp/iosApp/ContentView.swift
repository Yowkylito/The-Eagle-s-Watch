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
            .background(Color(red: 20 / 255, green: 64 / 255, blue: 194 / 255))
            // Light status-bar text over the flag-blue top bar.
            .preferredColorScheme(.dark)
    }
}
