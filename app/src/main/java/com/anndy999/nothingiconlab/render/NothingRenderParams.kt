package com.anndy999.nothingiconlab.render

import kotlinx.serialization.Serializable

/**
 * Tunable plate + glyph parameters.
 *
 * Visual defaults match the finished Nada 无题 (com.panpandada.nada.pay 16.0)
 * themed icons measured from 288px assets — not a copy of that pack:
 * - circular charcoal plate #1B1B1B, light glyph #F1F1F1
 * - cropped glyph bbox ≈ 106/288 ≈ 0.368 of the canvas
 * - appfilter scale 0.44 is only for unthemed (uncropped) fallbacks
 *
 * Nothing Launcher 2.5.9 bytecode still documents 0.3888889; it is no longer
 * the app default because stacking it with adaptive+mono insets made glyphs tiny.
 */
@Serializable
data class NothingRenderParams(
    val logoScale: Float = DEFAULT_LOGO_SCALE,
    val foregroundScale: Float = 1.0f,
    val adaptiveIconInset: Float = 0f,
    val monochromeInset: Float = 0f,
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
    val useSystemNeutralColors: Boolean = false,
    val lightBackground: Int = NADA_GLYPH,
    val lightForeground: Int = NADA_PLATE,
    val darkBackground: Int = NADA_PLATE,
    val darkForeground: Int = NADA_GLYPH,
    val outputSize: Int = 192,
) {
    fun effectiveLogoScale(): Float = (logoScale * foregroundScale).coerceIn(0.05f, 1.5f)

    companion object {
        /** Cropped glyph bbox / 288px canvas, median of Nada 无题 adapted icons. */
        const val DEFAULT_LOGO_SCALE: Float = 106f / 288f

        /** Nada appfilter scale for unthemed full icons (not used once cropToContent is on). */
        const val NADA_UNTHEMED_SCALE: Float = 0.44f

        /** 7/18. Verified in Nothing Launcher 2.5.9 classes2.dex. */
        const val NOTHING_LOGO_SCALE: Float = 0.3888889f

        /** Plate / glyph measured from Nada 288px assets (rgb 27 / 241). */
        const val NADA_PLATE: Int = 0xFF1B1B1B.toInt()
        const val NADA_GLYPH: Int = 0xFFF1F1F1.toInt()

        /** Framework defaults for system_neutral1_50 / 900. */
        const val NEUTRAL_50: Int = 0xFFF1F0F7.toInt()
        const val NEUTRAL_900: Int = 0xFF1A1B20.toInt()

        const val DEFAULT_ADAPTIVE_INSET: Float = 0.25f
        const val DEFAULT_MONOCHROME_INSET: Float = 1f / 6f

        fun nothingDefault(): NothingRenderParams = NothingRenderParams()
    }
}
