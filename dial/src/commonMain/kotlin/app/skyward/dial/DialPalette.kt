/*
 * Based on kastro-demo by James Yox (GPL-3.0)
 * https://github.com/jamesyox/kastro-demo
 */

package app.skyward.dial

import dev.jamesyox.kastro.common.HorizonState
import dev.jamesyox.kastro.sol.LightState
import dev.jamesyox.kastro.sol.SolarPhase
import dev.jamesyox.kastro.sol.Twilight

@Suppress("MagicNumber")
object DialPalette {
    val Day = DialColor(0xFFFFFF00, 1.0f)
    val CivilTwilight = DialColor(0xFF00ABFF, 0.65f)
    val NauticalTwilight = DialColor(0xFF003FC5, 0.45f)
    val AstronomicalTwilight = DialColor(0xFF030C63, 0.3f)
    val Night = DialColor(0xFF350038, 0.15f)
    val GoldenHour = DialColor(0xFFDAA520, 0.85f)
    val BlueHour = DialColor(0xFF4242F5, 0.55f)
    val MoonUp = DialColor(0xFFFFFFFF, 1.0f)
    val MoonDown = DialColor(0xFF2F4F4F, 0.25f)
    val Hour = DialColor(0xFF808080, 0.5f)
    val EventTick = DialColor(0xFFFFFFFF, 0.8f)
    val SunGlyph = DialColor(0xFFFFFF00, 1.0f)
    val MoonGlyph = DialColor(0xFFFFFFFF, 1.0f)
    val Hand = DialColor(0xFFFF0000, 1.0f)
    val Background = DialColor(0xFF000000, 0.0f)

    fun of(phase: SolarPhase): DialColor =
        when (phase) {
            is Twilight.AstronomicalTwilight -> AstronomicalTwilight
            is Twilight.CivilTwilight -> CivilTwilight
            is Twilight.NauticalTwilight -> NauticalTwilight
            is SolarPhase.Day -> Day
            is SolarPhase.Night -> Night
        }

    fun of(state: LightState): DialColor =
        when (state) {
            LightState.BlueHourDawn,
            LightState.BlueHourDusk -> BlueHour
            LightState.GoldenHourDawn,
            LightState.GoldenHourDusk -> GoldenHour
        }

    fun of(state: HorizonState): DialColor =
        when (state) {
            HorizonState.Up -> MoonUp
            HorizonState.Down -> MoonDown
        }
}
