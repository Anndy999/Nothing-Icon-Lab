package com.anndy999.nothingiconlab.render

/**
 * How the FORCED_MONO path turns a colored icon into a single-channel glyph.
 *
 * - [AOSP] (default): the AOSP Launcher3 MonochromeIconFactory flow that
 *   Nothing's [o3/a.m] reproduces — grayscale, alpha push, contrast stretch,
 *   optional threshold, AOSP edge-average auto-invert. Faithful to the call
 *   chain in docs/RESEARCH.md.
 *
 * - [NOTHING_BINARY]: same drawing pipeline, but the alpha mask is built by
 *   [NadaForeground]. Bright + low-saturation opaque pixels become the
 *   foreground; the rest becomes the background. Detects "white logo on
 *   colored background" the AOSP grayscale path turns gray-on-gray, and
 *   produces a crisp binary glyph. Falls back to [AOSP] when the extraction
 *   finds no foreground — useful for monochrome vector icons.
 *
 * Nada is never allowed to set geometry ([logoScale], [monochromeInset],
 * [cropToContent]). [NOTHING_BINARY] changes only how the FORCED_MONO mask
 * is extracted; geometry, rendering, and the display direction (white
 * plate + black glyph) are unchanged.
 */
enum class ForcedMonoStyle {
    AOSP,
    NOTHING_BINARY,
}
