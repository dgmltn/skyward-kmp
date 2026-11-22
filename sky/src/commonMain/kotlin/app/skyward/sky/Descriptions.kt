/*
 * Based on kastro-demo by James Yox (GPL-3.0)
 * https://github.com/yoxjames/kastro-demo
 */

package app.skyward.sky

import app.skyward.domain.SkyItem
import dev.jamesyox.kastro.common.HorizonState
import dev.jamesyox.kastro.luna.LunarEvent
import dev.jamesyox.kastro.luna.LunarPhase
import dev.jamesyox.kastro.sol.LightState
import dev.jamesyox.kastro.sol.SolarEvent
import dev.jamesyox.kastro.sol.SolarPhase

internal val SkyItem.title: String
    get() =
        when (this) {
            is SkyItem.Solar -> span.phase.title
            is SkyItem.Light -> span.lightState.title
            is SkyItem.Moon -> span.horizonState.moonTitle
            is SkyItem.SolarEventItem -> event.title
            is SkyItem.LunarEventItem -> event.title
        }

internal val SkyItem.description: String
    get() =
        when (this) {
            is SkyItem.Solar -> span.phase.description
            is SkyItem.Light -> span.lightState.description
            is SkyItem.Moon -> span.horizonState.moonDescription
            is SkyItem.SolarEventItem -> event.description
            is SkyItem.LunarEventItem -> event.description
        }

internal val SolarPhase.title: String
    get() =
        when (this) {
            SolarPhase.AstronomicalDawn -> "Astronomical Dawn"
            SolarPhase.AstronomicalDusk -> "Astronomical Dusk"
            SolarPhase.CivilDawn -> "Civil Dawn"
            SolarPhase.CivilDusk -> "Civil Dusk"
            SolarPhase.Day -> "Day"
            SolarPhase.NauticalDawn -> "Nautical Dawn"
            SolarPhase.NauticalDusk -> "Nautical Dusk"
            SolarPhase.Night -> "Night"
        }

internal val LightState.title: String
    get() =
        when (this) {
            LightState.BlueHourDawn -> "Blue Hour (Dawn)"
            LightState.BlueHourDusk -> "Blue Hour (Dusk)"
            LightState.GoldenHourDawn -> "Golden Hour (Dawn)"
            LightState.GoldenHourDusk -> "Golden Hour (Dusk)"
        }

internal val HorizonState.moonTitle: String
    get() =
        when (this) {
            HorizonState.Up -> "Moon Up"
            HorizonState.Down -> "Moon Down"
        }

internal val LunarPhase.Intermediate.title: String
    get() =
        when (this) {
            LunarPhase.Intermediate.WaningCrescent -> "Waning Crescent"
            LunarPhase.Intermediate.WaningGibbous -> "Waning Gibbous"
            LunarPhase.Intermediate.WaxingCrescent -> "Waxing Crescent"
            LunarPhase.Intermediate.WaxingGibbous -> "Waxing Gibbous"
        }

internal val SolarEvent.title: String
    get() =
        when (this) {
            is SolarEvent.Sunrise -> "Sunrise"
            is SolarEvent.Sunset -> "Sunset"
            is SolarEvent.SunriseEnd -> "Sunrise End"
            is SolarEvent.SunsetBegin -> "Sunset Begin"
            is SolarEvent.BlueHourDawn -> "Blue Hour Dawn"
            is SolarEvent.BlueHourDawnEnd -> "Blue Hour Dawn End"
            is SolarEvent.BlueHourDusk -> "Blue Hour Dusk"
            is SolarEvent.BlueHourDuskEnd -> "Blue Hour Dusk End"
            is SolarEvent.GoldenHourDawn -> "Golden Hour Dawn"
            is SolarEvent.GoldenHourDawnEnd -> "Golden Hour Dawn End"
            is SolarEvent.GoldenHourDusk -> "Golden Hour Dusk"
            is SolarEvent.GoldenHourDuskEnd -> "Golden Hour Dusk End"
            is SolarEvent.Nadir -> "Nadir"
            is SolarEvent.Noon -> "Noon"
            is SolarEvent.AstronomicalDawn -> "Astronomical Dawn"
            is SolarEvent.AstronomicalDusk -> "Astronomical Dusk"
            is SolarEvent.CivilDawn -> "Civil Dawn"
            is SolarEvent.CivilDusk -> "Civil Dusk"
            is SolarEvent.Day -> "Day"
            is SolarEvent.NauticalDawn -> "Nautical Dawn"
            is SolarEvent.NauticalDusk -> "Nautical Dusk"
            is SolarEvent.Night -> "Night"
        }

internal val LunarEvent.title: String
    get() =
        when (this) {
            is LunarEvent.HorizonEvent.Moonrise -> "Moonrise"
            is LunarEvent.HorizonEvent.Moonset -> "Moonset"
            is LunarEvent.PhaseEvent.FirstQuarter -> "First Quarter"
            is LunarEvent.PhaseEvent.FullMoon -> "Full Moon"
            is LunarEvent.PhaseEvent.LastQuarter -> "Last Quarter"
            is LunarEvent.PhaseEvent.NewMoon -> "New Moon"
        }

private val SolarPhase.description: String
    get() =
        when (this) {
            SolarPhase.Night ->
                "The sun is 18° or more below the horizon. This state represents the absence of " +
                    "any subtype of twilight while the sun is below the horizon."
            SolarPhase.AstronomicalDawn ->
                "Astronomical dawn is a subtype of astronomical twilight. $ASTRONOMICAL_TWILIGHT " +
                    "Astronomical dawn occurs while the sun is rising after nadir but before " +
                    "solar noon."
            SolarPhase.NauticalDawn ->
                "Nautical dawn is a subtype of nautical twilight. $NAUTICAL_TWILIGHT Nautical " +
                    "dawn occurs while the sun is rising after nadir but before solar noon."
            SolarPhase.CivilDawn ->
                "Civil dawn is a subtype of civil twilight. $CIVIL_TWILIGHT Civil dawn occurs " +
                    "while the sun is rising after nadir but before solar noon."
            SolarPhase.Day ->
                "The sun is above 0° relative to the horizon. The sky is fully illuminated."
            SolarPhase.CivilDusk ->
                "Civil dusk is a subtype of civil twilight. $CIVIL_TWILIGHT Civil dusk occurs " +
                    "while the sun is setting after solar noon but before nadir."
            SolarPhase.NauticalDusk ->
                "Nautical dusk is a subtype of nautical twilight. $NAUTICAL_TWILIGHT Nautical " +
                    "dusk occurs while the sun is setting after solar noon but before nadir."
            SolarPhase.AstronomicalDusk ->
                "Astronomical dusk is a subtype of astronomical twilight. $ASTRONOMICAL_TWILIGHT " +
                    "Astronomical dusk occurs while the sun is setting after solar noon but " +
                    "before nadir."
        }

private const val ASTRONOMICAL_TWILIGHT =
    "Astronomical twilight occurs when the sun is between 12° and 18° below the horizon. " +
        "During this state the sun is low enough that its scattered light is less than that of " +
        "starlight or other astronomical sources."

private const val NAUTICAL_TWILIGHT =
    "Nautical twilight occurs when the sun is between 6° and 12° below the horizon. Some " +
        "observable light may be present during this state. The horizon should still be " +
        "visible on a moonless night allowing mariners to navigate which is the origin of the " +
        "name."

private const val CIVIL_TWILIGHT =
    "Civil twilight occurs when the sun is between 0° and 6° below the horizon. During this " +
        "state the sky is still illuminated enough to see under normal conditions. Under ideal " +
        "atmospheric conditions the brightest stars and celestial objects should be observable."

private val LightState.description: String
    get() =
        when (this) {
            LightState.BlueHourDawn ->
                "$BLUE_HOUR Blue hour dawn refers to blue hour taking place before solar noon. " +
                    BLUE_HOUR_OVERLAP
            LightState.BlueHourDusk ->
                "$BLUE_HOUR Blue hour dusk refers to blue hour taking place after solar noon. " +
                    BLUE_HOUR_OVERLAP
            LightState.GoldenHourDawn ->
                "$GOLDEN_HOUR Golden hour dawn refers to golden hour taking place before solar " +
                    "noon. $GOLDEN_HOUR_OVERLAP"
            LightState.GoldenHourDusk ->
                "$GOLDEN_HOUR Golden hour dusk refers to golden hour taking place after solar " +
                    "noon. $GOLDEN_HOUR_OVERLAP"
        }

private const val BLUE_HOUR =
    "Blue hour refers to a time when the sun is below the horizon and the atmosphere causes " +
        "predominately blue light to appear visible. Blue hour generally occurs twice in a day, " +
        "once in the morning and once in the evening."

private const val BLUE_HOUR_OVERLAP =
    "Blue hour overlaps with the civil and nautical twilight solar phases."

private const val GOLDEN_HOUR =
    "Golden hour refers to a period where the sun's light is warm and ideal for taking " +
        "photographs. Golden hour generally occurs twice in a day, once in the morning and once " +
        "in the evening."

private const val GOLDEN_HOUR_OVERLAP =
    "Golden hour overlaps with the civil twilight and day solar phases."

private val HorizonState.moonDescription: String
    get() =
        when (this) {
            HorizonState.Up -> "The moon is above the horizon."
            HorizonState.Down -> "The moon is below the horizon."
        }

private val SolarEvent.description: String
    get() =
        when (this) {
            is SolarEvent.Sunrise ->
                "The top edge of the sun rises above the horizon. This is colloquially known as " +
                    "\"Sunrise\"."
            is SolarEvent.Sunset ->
                "The top edge of the sun sets below the horizon. This is colloquially known as " +
                    "\"Sunset\"."
            is SolarEvent.SunriseEnd -> "The bottom edge of the sun rises above the horizon."
            is SolarEvent.SunsetBegin -> "The bottom edge of the sun sets below the horizon."
            is SolarEvent.BlueHourDawn -> "Blue hour has begun during dawn."
            is SolarEvent.BlueHourDawnEnd -> "Blue hour has ended during dawn."
            is SolarEvent.BlueHourDusk -> "Blue hour has started during dusk."
            is SolarEvent.BlueHourDuskEnd -> "Blue hour has ended during dusk."
            is SolarEvent.GoldenHourDawn -> "Golden hour has started during dawn."
            is SolarEvent.GoldenHourDawnEnd -> "Golden hour has ended during dawn."
            is SolarEvent.GoldenHourDusk -> "Golden hour has started during dusk."
            is SolarEvent.GoldenHourDuskEnd -> "Golden hour has ended during dusk."
            is SolarEvent.Nadir -> "The sun has reached the lowest point in the sky."
            is SolarEvent.Noon -> "The sun has reached the highest point in the sky."
            is SolarEvent.AstronomicalDawn -> "Astronomical dawn has begun."
            is SolarEvent.AstronomicalDusk -> "Astronomical dusk has begun."
            is SolarEvent.CivilDawn -> "Civil dawn has begun."
            is SolarEvent.CivilDusk -> "Civil dusk has begun."
            is SolarEvent.Day ->
                "The middle of the sun is above 0° over the horizon. This does not account for " +
                    "atmospheric refraction."
            is SolarEvent.NauticalDawn -> "Nautical dawn has begun."
            is SolarEvent.NauticalDusk -> "Nautical dusk has begun."
            is SolarEvent.Night -> "Night has begun."
        }

private val LunarEvent.description: String
    get() =
        when (this) {
            is LunarEvent.HorizonEvent.Moonrise -> "The center of the moon rises above the horizon."
            is LunarEvent.HorizonEvent.Moonset -> "The center of the moon sets below the horizon."
            is LunarEvent.PhaseEvent.FirstQuarter ->
                "Half the moon is illuminated during the waxing phase."
            is LunarEvent.PhaseEvent.FullMoon -> "The moon is fully illuminated."
            is LunarEvent.PhaseEvent.LastQuarter ->
                "Half the moon is illuminated during the waning phase."
            is LunarEvent.PhaseEvent.NewMoon ->
                "The sun and moon are aligned and the moon is not illuminated."
        }
