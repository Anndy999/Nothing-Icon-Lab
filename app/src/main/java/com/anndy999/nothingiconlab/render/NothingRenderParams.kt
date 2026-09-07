package com.anndy999.nothingiconlab.render

import kotlinx.serialization.Serializable

/**
 * Tunable Nothing-style render parameters.
 *
 * Defaults are filled from AOSP AdaptiveIconDrawable / MonochromeIconFactory
 * plus the 0.3888889 value previously observed in Nothing Launcher analysis.
 *
 * That 0.3888889 figure is treated as a **hypothesis**, not a proven constant:
 * Nothing Launcher 2.5.9 APK was not present in this workspace, so the value
 * is exposed as [logoScale] and can be reset / live-tuned in the app.
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
    val cropToContent: Boolean = true,
    val followSystemDark: Boolean = true,
    val previewDark: Boolean = true,
    val lightBackground: Int = 0xFFFFFFFF.toInt(),
    val lightForeground: Int = 0xFF000000.toInt(),
    val darkBackground: Int = 0xFF000000.toInt(),
    val darkForeground: Int = 0xFFFFFFFF.toInt(),
    val outputSize: Int = 192,
) {
    fun effectiveLogoScale(): Float = (logoScale * foregroundScale).coerceIn(0.05f, 1.5f)

    companion object {
        /** 7/18. Hypothesized Nothing glyph visual size vs full canvas. */
        const val DEFAULT_LOGO_SCALE: Float = 0.3888889f

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
