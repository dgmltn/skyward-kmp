import SkywardKit
import SwiftUI
import UIKit
import WidgetKit

private let appGroup = "group.app.skyward"

struct ContentView: View {
    var body: some View {
        ComposeView().ignoresSafeArea(.all)
    }
}

private struct ComposeView: UIViewControllerRepresentable {
    func makeUIViewController(context: Context) -> UIViewController {
        MainViewControllerKt.MainViewController(
            appGroup: appGroup,
            reloadWidgets: { WidgetCenter.shared.reloadAllTimelines() }
        )
    }

    func updateUIViewController(_ uiViewController: UIViewController, context: Context) {}
}
