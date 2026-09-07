package com.anndy999.nothingiconlab.render

import kotlinx.serialization.Serializable

/**
 * Tunable plate + glyph parameters.
 *
 * Geometry follows Nothing Launcher 2.5.9 / AOSP Launcher3, not Nada:
 * - logoScale 0.3888889f is a const in n3/a.g, n3/a.h, o3/a.m (classes2.dex)
 * - cropToContent is off so that scale applies to the full drawable
 * - adaptive extra inset 0.25 from AdaptiveIconDrawable.getExtraInsetFraction()
 * - themed inset 1/6 from n3/a.<clinit> extra/(1+2*extra)
 *
 * Display colors are user-requested: white circular plate, black glyph.
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
    val alphaThreshold: Float = 0.0f,
    val invert: Boolean = false,
    val autoInvert: Boolean = true,
    val forceMonochrome: Boolean = false,
    val preferNativeMonochrome: Boolean = true,
    val cropToContent: Boolean = false,
    val followSystemDark: Boolean = true,
    val previewDark: Boolean = false,
    val useSystemNeutralColors: Boolean = false,
    val lightBackground: Int = WHITE_PLATE,
    val lightForeground: Int = BLACK_GLYPH,
    val darkBackground: Int = BLACK_GLYPH,
    val darkForeground: Int = WHITE_PLATE,
    val outputSize: Int = EXPORT_SIZE,
) {
    fun effectiveLogoScale(): Float = (logoScale * foregroundScale).coerceIn(0.05f, 1.5f)

    companion object {
        /** IEEE-754 0.3888889f in Nothing Launcher 2.5.9 n3/a and o3/a. */
        const val DEFAULT_LOGO_SCALE: Float = 0.3888889f

        /** AdaptiveIconDrawable.getExtraInsetFraction(). */
        const val DEFAULT_ADAPTIVE_INSET: Float = 0.25f

        /**
         * n3/a static inset: extra / (1 + 2 * extra) = 0.25 / 1.5 = 1/6.
         * Used by ClippedMonoDrawable / InsetDrawable, not as a second crop
         * stacked on top of [DEFAULT_LOGO_SCALE].
         */
        const val DEFAULT_MONOCHROME_INSET: Float = 1f / 6f

        const val WHITE_PLATE: Int = 0xFFFFFFFF.toInt()
        const val BLACK_GLYPH: Int = 0xFF000000.toInt()

        const val NEUTRAL_50: Int = 0xFFF1F0F7.toInt()
        const val NEUTRAL_900: Int = 0xFF1A1B20.toInt()

        const val PREVIEW_SIZE: Int = 128
        const val DETAIL_SIZE: Int = 512
        const val EXPORT_SIZE: Int = 512
        const val FORCED_WORK_SIZE: Int = 576
        const val PREVIEW_FORCED_WORK_SIZE: Int = 256

        fun nothingDefault(): NothingRenderParams = NothingRenderParams()
    }
}
