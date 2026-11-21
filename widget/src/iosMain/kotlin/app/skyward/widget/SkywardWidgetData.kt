package app.skyward.widget

import app.skyward.data.location.readSavedLocation
import app.skyward.data.sky.KastroSkyRepository

object SkywardWidgetData {
    private val builder by lazy { WidgetTimelineBuilder(KastroSkyRepository()) }

    fun timeline(
        appGroup: String,
        nowEpochMillis: Long,
        timeZoneId: String,
        use24Hour: Boolean,
    ): WidgetTimeline = timelineOrUnavailable {
        builder.build(readSavedLocation(appGroup), nowEpochMillis, timeZoneId, use24Hour)
    }
}
