package app.skyward.domain

import dev.jamesyox.kastro.luna.LunarEvent
import dev.jamesyox.kastro.sol.SolarEvent

sealed interface SkyItem {
    data class Solar(val span: SolarPhaseSpan) : SkyItem

    data class Light(val span: LightSpan) : SkyItem

    data class Moon(val span: MoonSpan) : SkyItem

    data class SolarEventItem(val event: SolarEvent) : SkyItem

    data class LunarEventItem(val event: LunarEvent) : SkyItem
}
