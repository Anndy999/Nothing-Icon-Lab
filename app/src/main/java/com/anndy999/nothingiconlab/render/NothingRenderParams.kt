package com.anndy999.nothingiconlab.render

import kotlinx.serialization.Serializable

/**
 * Tunable Nothing-style render parameters.
 *
 * Defaults verified against Nothing Launcher 2.5.9 (see docs/RESEARCH.md):
 * - logoScale 0.3888889f appears 3 times as a little-endian float in classes2.dex
 * - plate/glyph colors reference android.R.color.system_neutral1_50 / 900
 * - cropToContent is off so 0.3888889 scales the full drawable like createIconBitmap
 */
@Serializable
data class NothingRenderParams(
    val logoScale: Float = DEFAULT_LOGO_SCALE,
    val foregroundScale: Float = 1.0f,
    val adaptiveIconInset: Float = DEFAULT_ADAPTIVE_INSET,
    val monochromeInset: Float = DEFAULT_MONOCHROME_INSET,
    val backgroundSize: Float = 1.0f,
    val threshold: Float = 0.0f,
    val contrast: Float = 1.0f,
    val alphaThreshold: Float = 0.04f,
    val invert: Boolean = false,
    val autoInvert: Boolean = true,
    val forceMonochrome: Boolean = false,
    val preferNativeMonochrome: Boolean = true,
    val cropToContent: Boolean = false,
    val followSystemDark: Boolean = true,
    val previewDark: Boolean = true,
    val useSystemNeutralColors: Boolean = true,
    val lightBackground: Int = NEUTRAL_50,
    val lightForeground: Int = NEUTRAL_900,
    val darkBackground: Int = NEUTRAL_900,
    val darkForeground: Int = NEUTRAL_50,
    val outputSize: Int = 192,
) {
    fun effectiveLogoScale(): Float = (logoScale * foregroundScale).coerceIn(0.05f, 1.5f)

    companion object {
        /** 7/18. Verified IEEE-754 constant in Nothing Launcher 2.5.9 classes2.dex. */
        const val DEFAULT_LOGO_SCALE: Float = 0.3888889f

        /** Framework defaults for system_neutral1_50 / 900 (wallpaper can shift these). */
        const val NEUTRAL_50: Int = 0xFFF1F0F7.toInt()
        const val NEUTRAL_900: Int = 0xFF1A1B20.toInt()

        /** AdaptiveIconDrawable extra inset (AOSP). */
        const val DEFAULT_ADAPTIVE_INSET: Float = 0.25f

        /**
         * AOSP ThemedIconDrawable inset:
         * extraInset / (1 + 2 * extraInset) = 0.25 / 1.5 = 1/6.
         */
        const val DEFAULT_MONOCHROME_INSET: Float = 1f / 6f

        fun nothingDefault(): NothingRenderParams = NothingRenderParams()
    }
}
