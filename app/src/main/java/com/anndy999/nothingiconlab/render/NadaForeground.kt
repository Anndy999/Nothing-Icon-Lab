package com.anndy999.nothingiconlab.render

import android.graphics.Bitmap
import java.nio.ByteBuffer

/**
 * Nada-style foreground extractor for the FORCED_MONO path.
 *
 * The AOSP grayscale path promotes luminance to alpha and stretches
 * contrast to find the glyph. For colored icons (red background, white
 * logo) the foreground and background collapse to similar luminance values
 * and the result is gray-on-gray mush. The Android 13+ native monochrome
 * layer and Nada's renderer both want a clean binary mask over the
 * brightest, lowest-saturation pixels. That is what this extractor
 * produces, exactly as the Nada post-processor does in the binary pipeline.
 *
 * Only used when [ForcedMonoStyle.NOTHING_BINARY] is selected. Default
 * [ForcedMonoStyle.AOSP] keeps the o3/a.m call chain.
 *
 * The inner loop is a pure function over an [IntArray] so it can be
 * unit-tested under JVM JUnit without Robolectric.
 */
object NadaForeground {
    /** Maximum (max-min channel) allowed before a pixel is too colored to read as white-ish. */
    const val DEFAULT_SATURATION_CUT: Float = 70f

    /** Minimum luma (R+G+B)/3 before an opaque pixel is bright enough to read as foreground. */
    const val DEFAULT_LUMA_CUT: Float = 130f

    /** Minimum foreground alpha for a pixel to count as opaque foreground. */
    const val DEFAULT_MIN_ALPHA: Int = 16

    /**
     * Run the extractor on a working bitmap that already contains the icon
     * drawn over BLACK (the same buffer used by the AOSP path). The BLACK
     * padding fails both the luma and saturation tests, so it is naturally
     * ignored.
     */
    fun extract(
        src: Bitmap,
        saturationCut: Float = DEFAULT_SATURATION_CUT,
        lumaCut: Float = DEFAULT_LUMA_CUT,
        minimumAlpha: Int = DEFAULT_MIN_ALPHA,
    ): Result {
        val w = src.width
        val h = src.height
        val pixels = IntArray(w * h)
        src.getPixels(pixels, 0, w, 0, 0, w, h)
        val fromPixels = extractFromPixels(pixels, w, h, saturationCut, lumaCut, minimumAlpha)
        val bmp = Bitmap.createBitmap(w, h, Bitmap.Config.ALPHA_8)
        bmp.copyPixelsFromBuffer(ByteBuffer.wrap(fromPixels.mask))
        return Result(bmp, fromPixels.foregroundRatio)
    }

    /**
     * Pure pixel-array form. Returns the per-pixel alpha mask and the
     * fraction of opaque pixels that became foreground.
     */
    fun extractFromPixels(
        pixels: IntArray,
        width: Int,
        height: Int,
        saturationCut: Float = DEFAULT_SATURATION_CUT,
        lumaCut: Float = DEFAULT_LUMA_CUT,
        minimumAlpha: Int = DEFAULT_MIN_ALPHA,
    ): FromPixels {
        require(pixels.size >= width * height) {
            "pixel buffer too small: ${pixels.size} for ${width}x$height"
        }
        var opaque = 0
        var fore = 0
        val mask = ByteArray(width * height)
        for (i in 0 until width * height) {
            val c = pixels[i]
            val a = (c ushr 24) and 0xFF
            if (a <= minimumAlpha) continue
            opaque++
            val r = (c ushr 16) and 0xFF
            val g = (c ushr 8) and 0xFF
            val b = c and 0xFF
            val mx = maxOf(r, g, b)
            val mn = minOf(r, g, b)
            val isFore = (mx - mn) < saturationCut && ((r + g + b) / 3) >= lumaCut
            if (isFore) {
                fore++
                mask[i] = a.toByte()
            }
        }
        val ratio = if (opaque > 0) fore.toFloat() / opaque else 0f
        return FromPixels(mask, ratio)
    }

    data class Result(val mask: Bitmap, val foregroundRatio: Float)
    data class FromPixels(val mask: ByteArray, val foregroundRatio: Float)
}
