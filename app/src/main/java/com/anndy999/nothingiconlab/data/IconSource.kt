package com.anndy999.nothingiconlab.data

import androidx.annotation.StringRes
import com.anndy999.nothingiconlab.R

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
enum class IconSource(
    @StringRes val shortLabelRes: Int,
    @StringRes val longLabelRes: Int,
) {
    NATIVE_MONO(R.string.source_native, R.string.source_native_long),
    FORCED_MONO(R.string.source_forced, R.string.source_forced_long),
    FALLBACK(R.string.source_fallback, R.string.source_fallback_long),
}
