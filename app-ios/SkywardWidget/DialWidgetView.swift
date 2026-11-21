import SkywardWidgetKit
import SwiftUI
import WidgetKit

struct DialWidgetView: View {
    let entry: DialEntry
    @Environment(\.widgetRenderingMode) private var renderingMode
    /// False in StandBy, which removes the widget's background and shows it larger.
    @Environment(\.showsWidgetContainerBackground) private var showsBackground

    var body: some View {
        DialWidgetContent(
            entry: entry,
            monochrome: renderingMode != .fullColor,
            showsHourLabels: !showsBackground
        )
        .containerBackground(Color.black, for: .widget)
    }
}

/// The widget's face, with the rendering mode passed in so it can be drawn outside WidgetKit.
struct DialWidgetContent: View {
    let entry: DialEntry
    let monochrome: Bool
    var showsHourLabels = false

    var body: some View {
        switch entry.face {
        case .dial(let dial):
            DialRenderer(model: dial, monochrome: monochrome, showsHourLabels: showsHourLabels)
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
