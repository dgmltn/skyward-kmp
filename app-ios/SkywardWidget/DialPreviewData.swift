import Foundation
import SkywardWidgetKit

/// Dials for a fixed London afternoon, built through the same Kotlin API the widget uses from a
/// location saved in a preview-only defaults suite.
enum DialPreviewData {
    private static let suite = "app.skyward.widget.preview"

    static let start = ISO8601DateFormatter().date(from: "2026-11-21T13:30:00Z")!

    /// Every five minutes for six hours from `start`.
    static let london: [DialEntry] = {
        let defaults = UserDefaults(suiteName: suite)!
        defaults.set(51.5074, forKey: "lat")
        defaults.set(-0.1278, forKey: "lon")
        defaults.set(Int64(start.timeIntervalSince1970 * 1000), forKey: "fixedAtEpochMs")
        let timeline = SkywardWidgetData.shared.timeline(
            appGroup: suite,
            nowEpochMillis: Int64(start.timeIntervalSince1970 * 1000),
            timeZoneId: "Europe/London",
            use24Hour: true
        )
        return dialEntries(timeline, now: start)
    }()
}
