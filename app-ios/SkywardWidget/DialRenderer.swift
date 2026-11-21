/*
 * Based on kastro-demo by James Yox (GPL-3.0)
 * https://github.com/jamesyox/kastro-demo
 */

import SkywardWidgetKit
import SwiftUI

/// The dial's 200x200 box, in dial units.
private let dialSize: CGFloat = 200
private let handWidth: CGFloat = 0.8
private let centreDotRadius: CGFloat = 3
private let handHaloWidth: CGFloat = 3

/// The sun and moon dial for one day, drawn square and scaled to fit. With `monochrome` set, every
/// element is white at its palette luminance, for the widget's vibrant and accented modes. Hour
/// labels are left out: at widget size they would be under three points tall.
struct DialRenderer: View {
    let model: DialModel
    let monochrome: Bool

    var body: some View {
        Canvas { context, size in
            let scale = min(size.width, size.height) / dialSize
            var dial = context
            dial.translateBy(x: size.width / 2, y: size.height / 2)
            dial.scaleBy(x: scale, y: scale)
            DialPainter(context: dial, ink: DialInk(monochrome: monochrome)).draw(model)
        }
        .aspectRatio(1, contentMode: .fit)
    }
}

/// Picks the color an element is drawn in: its palette color, or white at its luminance.
struct DialInk {
    let monochrome: Bool

    func shading(_ color: DialColor, alpha: Float = 1) -> GraphicsContext.Shading {
        if monochrome {
            return .color(.white.opacity(Double(color.luminance * alpha)))
        }
        return .color(Color(argb: color.argb).opacity(Double(alpha)))
    }
}

/// Draws in dial units with the origin at the centre, y down.
struct DialPainter {
    let context: GraphicsContext
    let ink: DialInk

    func draw(_ model: DialModel) {
        model.hourTicks.forEach(drawTick)
        let halo = ink.monochrome ? model.hand.map(handHalo) : nil
        model.arcs.forEach { drawRing($0, cutting: halo) }
        if let hand = model.hand { drawHand(hand) }
        if let moon = model.moonIcon {
            drawMoon(
                phaseDegrees: moon.phaseDegrees,
                center: polar(moon.radius, moon.angleDegrees),
                diameter: moon.size
            )
        }
        for marker in model.markers {
            drawTick(marker.tick)
            drawMarkerGlyph(
                marker.glyph,
                color: marker.glyphColor,
                center: polar(DialGeometry.shared.EVENT_GLYPH_RADIUS, marker.tick.angleDegrees)
            )
        }
        if let tick = model.selectedEventTick { drawTick(tick) }
    }

    private func drawRing(_ arc: DialArc, cutting halo: Path?) {
        guard arc.sweepDegrees > 0 else { return }
        var path = Path()
        path.addArc(
            center: .zero,
            radius: arc.radius,
            startAngle: .degrees(arc.startDegrees),
            endAngle: .degrees(arc.startDegrees + arc.sweepDegrees),
            clockwise: false
        )
        var ring = path.strokedPath(StrokeStyle(lineWidth: arc.width, lineCap: .butt))
        if let halo { ring = ring.subtracting(halo) }
        context.fill(ring, with: ink.shading(arc.color, alpha: arc.alpha))
    }

    /// A dark band around the hand. Monochrome draws the hand and the day ring both at full
    /// luminance, so without it the hand disappears where it crosses the ring.
    private func handHalo(_ hand: DialHand) -> Path {
        handPath(hand).strokedPath(StrokeStyle(lineWidth: handHaloWidth))
    }

    private func handPath(_ hand: DialHand) -> Path {
        var path = Path()
        path.move(to: .zero)
        path.addLine(to: polar(hand.length, hand.angleDegrees))
        return path
    }

    private func drawTick(_ tick: DialTick) {
        var path = Path()
        path.move(to: polar(tick.innerRadius, tick.angleDegrees))
        path.addLine(to: polar(tick.outerRadius, tick.angleDegrees))
        context.stroke(path, with: ink.shading(tick.color), lineWidth: tick.width)
    }

    private func drawHand(_ hand: DialHand) {
        context.stroke(handPath(hand), with: ink.shading(hand.color), lineWidth: handWidth)
        let dot = CGRect(
            x: -centreDotRadius,
            y: -centreDotRadius,
            width: 2 * centreDotRadius,
            height: 2 * centreDotRadius
        )
        context.fill(Path(ellipseIn: dot), with: ink.shading(hand.color))
    }
}

/// The point `radius` dial units from the centre at `angleDegrees`, clockwise from +x.
func polar(_ radius: Double, _ angleDegrees: Double) -> CGPoint {
    let radians = angleDegrees * .pi / 180
    return CGPoint(x: radius * cos(radians), y: radius * sin(radians))
}

extension Color {
    init(argb: Int64) {
        self.init(
            .sRGB,
            red: Double((argb >> 16) & 0xFF) / 255,
            green: Double((argb >> 8) & 0xFF) / 255,
            blue: Double(argb & 0xFF) / 255,
            opacity: Double((argb >> 24) & 0xFF) / 255
        )
    }
}
