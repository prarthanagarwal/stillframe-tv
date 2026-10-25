// Regenerate the checked-in vector banner on macOS using the repository's OFL-licensed Outfit font.
// swift generate-banner.swift ../app/src/main/res/font/outfit.ttf src/main/res/drawable/banner_cable_tv.xml
import Foundation
import CoreText
import CoreGraphics

let fontURL = URL(fileURLWithPath: CommandLine.arguments[1])
let provider = CGDataProvider(url: fontURL as CFURL)!
let graphicsFont = CGFont(provider)!
func outline(_ text: String, size: CGFloat, baseline: CGFloat, weight: CGFloat) -> String {
    let base = CTFontCreateWithGraphicsFont(graphicsFont, size, nil, nil)
    let attributes = [kCTFontVariationAttribute: [NSNumber(value: 0x77676874): NSNumber(value: Double(weight))]]
    let descriptor = CTFontDescriptorCreateCopyWithAttributes(CTFontCopyFontDescriptor(base), attributes as CFDictionary)
    let font = CTFontCreateWithFontDescriptor(descriptor, size, nil)
    let chars = Array(text.utf16)
    var glyphs = [CGGlyph](repeating: 0, count: chars.count)
    CTFontGetGlyphsForCharacters(font, chars, &glyphs, chars.count)
    var advances = [CGSize](repeating: .zero, count: glyphs.count)
    let width = CTFontGetAdvancesForGlyphs(font, .horizontal, glyphs, &advances, glyphs.count)
    var x = (320 - width) / 2
    var commands = [String]()
    func point(_ p: CGPoint) -> String { String(format: "%.2f,%.2f", p.x, p.y) }
    for (i, glyph) in glyphs.enumerated() {
        var transform = CGAffineTransform(a: 1, b: 0, c: 0, d: -1, tx: x, ty: baseline)
        if let path = CTFontCreatePathForGlyph(font, glyph, &transform) {
            path.applyWithBlock { element in
                let e = element.pointee
                switch e.type {
                case .moveToPoint: commands.append("M" + point(e.points[0]))
                case .addLineToPoint: commands.append("L" + point(e.points[0]))
                case .addQuadCurveToPoint: commands.append("Q" + point(e.points[0]) + " " + point(e.points[1]))
                case .addCurveToPoint: commands.append("C" + point(e.points[0]) + " " + point(e.points[1]) + " " + point(e.points[2]))
                case .closeSubpath: commands.append("Z")
                @unknown default: break
                }
            }
        }
        x += advances[i].width
    }
    return commands.joined()
}
let xml = """
<?xml version="1.0" encoding="utf-8"?>
<!-- Text outlines from Outfit, SIL Open Font License; see ../../../../../FONT-LICENSE-OFL.txt. -->
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="320dp" android:height="180dp" android:viewportWidth="320" android:viewportHeight="180">
    <path android:fillColor="#173D50" android:pathData="M0,0H320V180H0Z" />
    <path android:fillColor="#00000000" android:strokeColor="#D7EDF4" android:strokeWidth="3"
        android:strokeLineJoin="round" android:pathData="M140,25H180V54H140ZM160,54V62M150,62H170" />
    <path android:fillColor="#FFFFFF" android:pathData="\(outline("Cable TV", size: 46, baseline: 117, weight: 650))" />
    <path android:fillColor="#BAD5DF" android:pathData="\(outline("HDMI 2", size: 22, baseline: 150, weight: 500))" />
</vector>
"""
try xml.write(toFile: CommandLine.arguments[2], atomically: true, encoding: .utf8)
