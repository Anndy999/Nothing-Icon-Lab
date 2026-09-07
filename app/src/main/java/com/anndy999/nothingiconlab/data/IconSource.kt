package com.anndy999.nothingiconlab.data

/**
 * How the monochrome glyph for an app was obtained.
 *
 * Priority (v0.1):
 * 1. [NATIVE_MONO] — AdaptiveIconDrawable.monochrome from the real APK
 * 2. [FORCED_MONO] — AOSP Launcher3 MonochromeIconFactory on the real icon
 * 3. [FALLBACK]    — last-resort grayscale of the launcher icon
 *
 * Lawnicons / hand-drawn SVG is not used as a default source.
 */
enum class IconSource {
    NATIVE_MONO,
    FORCED_MONO,
    FALLBACK,
    ;

    val shortLabel: String
        get() = when (this) {
            NATIVE_MONO -> "Native"
            FORCED_MONO -> "Forced"
            FALLBACK -> "Fallback"
        }

    val longLabel: String
        get() = when (this) {
            NATIVE_MONO -> "Native Monochrome"
            FORCED_MONO -> "Forced Monochrome"
            FALLBACK -> "Fallback"
        }
}
