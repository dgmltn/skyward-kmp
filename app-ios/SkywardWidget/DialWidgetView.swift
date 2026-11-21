import SkywardWidgetKit
import SwiftUI
import WidgetKit

struct DialWidgetView: View {
    let entry: DialEntry
    @Environment(\.widgetRenderingMode) private var renderingMode

    var body: some View {
        DialWidgetContent(entry: entry, monochrome: renderingMode != .fullColor)
            .containerBackground(Color.black, for: .widget)
    }
}

/// The widget's face, with the rendering mode passed in so it can be drawn outside WidgetKit.
struct DialWidgetContent: View {
    let entry: DialEntry
    let monochrome: Bool

    var body: some View {
        switch entry.face {
        case .dial(let dial):
            DialRenderer(model: dial, monochrome: monochrome)
                .padding(4)
        case .noLocation:
            WidgetMessage(text: "Open Skyward to set location")
        case .unavailable:
            WidgetMessage(text: "Sky times unavailable")
        }
    }
}

private struct WidgetMessage: View {
    let text: String

    var body: some View {
        Text(text)
            .font(.footnote)
            .multilineTextAlignment(.center)
            .foregroundStyle(.white)
            .padding()
    }
}
