/*
 * Based on kastro-demo by James Yox (GPL-3.0)
 * https://github.com/jamesyox/kastro-demo
 */

import SkywardWidgetKit
import SwiftUI

private let halfTurn = 180.0
private let top = Angle.degrees(-90)

extension DialPainter {
    /// Draws the moon as seen at `phaseDegrees` (0 new, 90 first quarter, 180 full, 270 last
    /// quarter): a lit disc of `diameter` centred on `center` with the dark part painted over it, or
    /// in monochrome the lit and dark parts as separate shapes.
    func drawMoon(phaseDegrees: Double, center: CGPoint, diameter: Double) {
        let radius = diameter / 2
        let disc = Path(
            ellipseIn: CGRect(x: -radius, y: -radius, width: diameter, height: diameter)
        )
        let shadow = moonShadow(phaseDegrees: phaseDegrees, radius: radius)
        let palette = DialPalette.shared
        var moon = context
        moon.translateBy(x: center.x, y: center.y)
        // Monochrome inks are translucent white, so a shadow painted over the lit disc would not
        // darken it; the lit part is cut to shape instead.
        let lit = ink.monochrome ? disc.subtracting(shadow) : disc
        moon.fill(lit, with: ink.shading(palette.MoonUp))
        moon.fill(shadow, with: ink.shading(palette.MoonDown))
    }
}

/// The dark part of the moon, centred on the origin: the semicircle on the unlit side, closed by a
/// half-ellipse whose x-radius is `radius·cos(phase)`, bulging into the lit side for a crescent and
/// back into the dark side for a gibbous moon.
func moonShadow(phaseDegrees: Double, radius: Double) -> Path {
    let isWaning = phaseDegrees > halfTurn
    let terminatorX = radius * cos(phaseDegrees * .pi / halfTurn)
    let isGibbous = terminatorX < 0
    let terminatorRadius = abs(terminatorX)
    var path = Path()
    path.move(to: CGPoint(x: 0, y: -radius))
    path.addArc(top, sweep: isWaning ? halfTurn : -halfTurn, radius: radius)
    if terminatorRadius > 0 {
        let sweep = isGibbous != isWaning ? halfTurn : -halfTurn
        path.addArc(
            .degrees(90),
            sweep: sweep,
            radius: radius,
            transform: CGAffineTransform(scaleX: terminatorRadius / radius, y: 1)
        )
    }
    path.closeSubpath()
    return path
}

extension Path {
    /// Adds an arc around the origin the way Compose's `arcTo` does: a positive `sweep` turns
    /// clockwise on screen.
    mutating func addArc(
        _ start: Angle,
        sweep: Double,
        radius: Double,
        transform: CGAffineTransform = .identity
    ) {
        addArc(
            center: .zero,
            radius: radius,
            startAngle: start,
            endAngle: start + .degrees(sweep),
            clockwise: sweep < 0,
            transform: transform
        )
    }
}
