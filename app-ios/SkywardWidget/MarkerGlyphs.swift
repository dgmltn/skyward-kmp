/*
 * Based on kastro-demo by James Yox (GPL-3.0)
 * https://github.com/jamesyox/kastro-demo
 */

import SkywardWidgetKit
import SwiftUI

/// Width and height of a marker glyph, in dial units.
private let glyphSize: CGFloat = 4

/// The glyph paths below are drawn in a 6x6 box centred on the origin.
private let designSize: CGFloat = 6
private let designHalf = designSize / 2

private let arrowWidth: CGFloat = 0.8
private let nadirOutlineWidth: CGFloat = 0.2

private let arrowColor = Color.red
private let nadirOutline = DialColor(argb: 0xFFFF_FFFF, luminance: 1)

private let riseArrow = Path { path in
    path.move(to: CGPoint(x: 0, y: 3))
    path.addLine(to: CGPoint(x: 0, y: 1))
    path.move(to: CGPoint(x: -1, y: 2))
    path.addLine(to: CGPoint(x: 0, y: 1))
    path.addLine(to: CGPoint(x: 1, y: 2))
}

private let setArrow = Path { path in
    path.move(to: CGPoint(x: 0, y: 0.5))
    path.addLine(to: CGPoint(x: 0, y: 2.5))
    path.move(to: CGPoint(x: -1, y: 1.5))
    path.addLine(to: CGPoint(x: 0, y: 2.5))
    path.addLine(to: CGPoint(x: 1, y: 1.5))
}

private let upTriangle = Path { path in
    path.move(to: CGPoint(x: -designHalf, y: designHalf))
    path.addLine(to: CGPoint(x: 0, y: -designHalf))
    path.addLine(to: CGPoint(x: designHalf, y: designHalf))
    path.closeSubpath()
}

private let downTriangle = Path { path in
    path.move(to: CGPoint(x: -designHalf, y: -designHalf))
    path.addLine(to: CGPoint(x: 0, y: designHalf))
    path.addLine(to: CGPoint(x: designHalf, y: -designHalf))
    path.closeSubpath()
}

/// The sun or moon half above the horizon.
private let upperHalfDisc = Path { path in
    path.move(to: .zero)
    path.addArc(.degrees(180), sweep: 180, radius: designHalf)
    path.closeSubpath()
}

extension DialPainter {
    /// Draws `glyph` upright, centred on `center`, in `color` where the glyph takes one.
    func drawMarkerGlyph(_ glyph: MarkerGlyph, color: DialColor, center: CGPoint) {
        switch glyph {
        case .theNewMoon: drawMoon(phaseDegrees: 0, center: center, diameter: glyphSize)
        case .firstQuarter: drawMoon(phaseDegrees: 90, center: center, diameter: glyphSize)
        case .fullMoon: drawMoon(phaseDegrees: 180, center: center, diameter: glyphSize)
        case .lastQuarter: drawMoon(phaseDegrees: 270, center: center, diameter: glyphSize)
        case .sunrise, .moonrise: drawHorizonGlyph(color: color, arrow: riseArrow, center: center)
        case .sunset, .moonset: drawHorizonGlyph(color: color, arrow: setArrow, center: center)
        case .noon:
            designBox(center).fill(upTriangle, with: ink.shading(DialPalette.shared.Day))
        case .nadir:
            let box = designBox(center)
            box.fill(downTriangle, with: ink.shading(DialPalette.shared.Night))
            box.stroke(downTriangle, with: ink.shading(nadirOutline), lineWidth: nadirOutlineWidth)
        default: break
        }
    }

    private func designBox(_ center: CGPoint) -> GraphicsContext {
        var box = context
        box.translateBy(x: center.x, y: center.y)
        box.scaleBy(x: glyphSize / designSize, y: glyphSize / designSize)
        return box
    }

    /// The half disc with an arrow below it showing which way the body is going. Monochrome has no
    /// red, so the arrow takes the glyph's luminance instead.
    private func drawHorizonGlyph(color: DialColor, arrow: Path, center: CGPoint) {
        let box = designBox(center)
        box.fill(upperHalfDisc, with: ink.shading(color))
        let arrowShading = ink.monochrome ? ink.shading(color) : .color(arrowColor)
        box.stroke(arrow, with: arrowShading, style: StrokeStyle(lineWidth: arrowWidth))
    }
}
