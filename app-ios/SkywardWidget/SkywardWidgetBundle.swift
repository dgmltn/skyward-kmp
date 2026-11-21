import SwiftUI
import WidgetKit

@main
struct SkywardWidgetBundle: WidgetBundle {
    var body: some Widget {
        SkywardDialWidget()
    }
}

struct SkywardDialWidget: Widget {
    var body: some WidgetConfiguration {
        StaticConfiguration(kind: "SkywardDial", provider: DialProvider()) { entry in
            DialWidgetView(entry: entry)
        }
        .configurationDisplayName("Skyward")
        .description("Today's sun and moon dial.")
        .supportedFamilies([.systemSmall])
        .contentMarginsDisabled()
    }
}

#Preview("London", as: .systemSmall) {
    SkywardDialWidget()
} timeline: {
    for entry in DialPreviewData.london.prefix(12) { entry }
}

#Preview("No location", as: .systemSmall) {
    SkywardDialWidget()
} timeline: {
    DialEntry(date: .now, face: .noLocation)
}

#Preview("Unavailable", as: .systemSmall) {
    SkywardDialWidget()
} timeline: {
    DialEntry(date: .now, face: .unavailable)
}
