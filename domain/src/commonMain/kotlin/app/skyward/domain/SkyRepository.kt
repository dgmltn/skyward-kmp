package app.skyward.domain

import kotlin.time.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone

interface SkyRepository {
    fun day(point: GeoPoint, date: LocalDate, timeZone: TimeZone): SkyDay

    fun now(point: GeoPoint, instant: Instant): SkyNow
}
