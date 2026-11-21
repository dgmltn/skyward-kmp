import Foundation
import SkywardWidgetKit
import WidgetKit

let appGroup = "group.com.dgmltn.skyward"

/// One moment of the widget.
struct DialEntry: TimelineEntry {
    let date: Date
    let face: Face

    enum Face {
        case dial(DialModel)
        case noLocation
        case unavailable
    }
}

struct DialProvider: TimelineProvider {
    func placeholder(in context: Context) -> DialEntry {
        currentEntries().first ?? DialEntry(date: .now, face: .noLocation)
    }

    func getSnapshot(in context: Context, completion: @escaping (DialEntry) -> Void) {
        completion(placeholder(in: context))
    }

    func getTimeline(in context: Context, completion: @escaping (Timeline<DialEntry>) -> Void) {
        completion(Timeline(entries: currentEntries(), policy: .atEnd))
    }

    private func currentEntries() -> [DialEntry] {
        dialEntries(
            SkywardWidgetData.shared.timeline(
                appGroup: appGroup,
                nowEpochMillis: Int64(Date.now.timeIntervalSince1970 * 1000),
                timeZoneId: TimeZone.current.identifier,
                use24Hour: uses24HourClock()
            )
        )
    }
}

func dialEntries(_ timeline: WidgetTimeline, now: Date = .now) -> [DialEntry] {
    switch timeline {
    case let timeline as WidgetTimelineEntries:
        return timeline.entries.map {
            DialEntry(
                date: Date(timeIntervalSince1970: Double($0.epochMillis) / 1000),
                face: .dial($0.dial)
            )
        }
    case is WidgetTimelineUnavailable:
        return [DialEntry(date: now, face: .unavailable)]
    default:
        return [DialEntry(date: now, face: .noLocation)]
    }
}

private func uses24HourClock() -> Bool {
    guard let pattern = DateFormatter.dateFormat(fromTemplate: "j", options: 0, locale: .current) else {
        return false
    }
    return pattern.contains("H") || pattern.contains("k")
}
