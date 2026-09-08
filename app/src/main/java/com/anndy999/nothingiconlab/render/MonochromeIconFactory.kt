package com.anndy999.nothingiconlab.render

import android.graphics.Bitmap
import android.graphics.BlendMode
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Paint
import android.graphics.Path
import android.graphics.drawable.AdaptiveIconDrawable
import android.graphics.drawable.Drawable
import android.util.Log
import com.anndy999.nothingiconlab.LabLog
import java.nio.ByteBuffer
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

/**
 * Kotlin port of AOSP Launcher3 MonochromeIconFactory (Apache 2.0).
 * Original: packages/apps/Launcher3/.../MonochromeIconFactory.java
 *
 * Operates at two working sizes (576 export / 256 preview) so the contrast
 * stretch has enough detail to land between foreground and background.
 *
 * Two extraction styles are supported and selected by
 * [NothingRenderParams.forcedMonoStyle]:
 * - [ForcedMonoStyle.AOSP]: grayscale, contrast stretch, threshold,
 *   AOSP edge-average auto-invert. The o3/a.m path documented in
 *   docs/RESEARCH.md. Default.
 * - [ForcedMonoStyle.NOTHING_BINARY]: foreground detection via
 *   bright + low-saturation pixels, falling back to AOSP when no
 *   foreground is found (monochrome vectors, full-frame photography).
 */
class MonochromeIconFactory(iconBitmapSize: Int) {
    private val extraFactor = AdaptiveIconDrawable.getExtraInsetFraction()
    private val viewPortScale = 1f / (1f + 2f * extraFactor)
    private val bitmapSize = (iconBitmapSize * 2f * viewPortScale).roundToInt().coerceAtLeast(1)
    private val pixels = ByteArray(bitmapSize * bitmapSize)
    private val edgePixelLength = bitmapSize * (bitmapSize - iconBitmapSize).coerceAtLeast(0) / 2

    private val flatBitmap = Bitmap.createBitmap(bitmapSize, bitmapSize, Bitmap.Config.ARGB_8888)
    private val flatCanvas = Canvas(flatBitmap)
    private val alphaBitmap = Bitmap.createBitmap(bitmapSize, bitmapSize, Bitmap.Config.ALPHA_8)
    private val alphaCanvas = Canvas(alphaBitmap)

    private val copyPaint = Paint(Paint.FILTER_BITMAP_FLAG).apply {
        blendMode = BlendMode.SRC
        val satMatrix = ColorMatrix()
        satMatrix.setSaturation(0f)
        val vals = satMatrix.array
        vals[15] = 0.3333f
        vals[16] = 0.3333f
        vals[17] = 0.3333f
        vals[18] = 0f
        vals[19] = 0f
        colorFilter = ColorMatrixColorFilter(vals)
    }

    fun wrap(
        icon: AdaptiveIconDrawable,
        params: NothingRenderParams,
        style: ForcedMonoStyle = ForcedMonoStyle.AOSP,
    ): ForcedMonoResult {
        flatCanvas.drawColor(Color.BLACK)
        drawDrawable(icon.background)
        drawDrawable(icon.foreground)
        val generated = generateMono(params, style)
        val clipped = clipToCircle(generated.bitmap)
        return generated.copy(bitmap = clipped)
    }

    fun wrapNonAdaptive(
        drawable: Drawable,
        params: NothingRenderParams,
        style: ForcedMonoStyle = ForcedMonoStyle.AOSP,
    ): ForcedMonoResult {
        flatCanvas.drawColor(Color.BLACK)
        drawDrawable(drawable)
        return generateMono(params, style)
    }

    private fun drawDrawable(drawable: Drawable?) {
        if (drawable == null) return
        drawable.setBounds(0, 0, bitmapSize, bitmapSize)
        drawable.draw(flatCanvas)
    }

    private fun generateMono(params: NothingRenderParams, style: ForcedMonoStyle): ForcedMonoResult {
        if (style == ForcedMonoStyle.NOTHING_BINARY) {
            val binary = extractBinary()
            if (binary != null) return binary
            Log.d(LabLog.TAG, "binary path fell back to AOSP (no foreground)")
        }
        return extractAosp(params)
    }

    /**
     * Bright + low-saturation foreground detection on the same drawn buffer
     * the AOSP path uses. Returns null when the extraction finds no
     * foreground; the caller should fall back to [extractAosp].
     */
    private fun extractBinary(): ForcedMonoResult? {
        val extraction = NadaForeground.extract(flatBitmap)
        if (extraction.foregroundRatio < BINARY_FOREGROUND_FLOOR) return null
        val argb = Bitmap.createBitmap(bitmapSize, bitmapSize, Bitmap.Config.ARGB_8888)
        val src = IntArray(bitmapSize * bitmapSize)
        val maskBytes = ByteArray(bitmapSize * bitmapSize)
        val buf = ByteBuffer.wrap(maskBytes)
        buf.rewind()
        extraction.mask.copyPixelsToBuffer(buf)
        for (i in src.indices) {
            val a = maskBytes[i].toInt() and 0xFF
            src[i] = (a shl 24) or 0x00FFFFFF
        }
        argb.setPixels(src, 0, bitmapSize, 0, 0, bitmapSize, bitmapSize)
        Log.d(
            LabLog.TAG,
            "MonochromeIconFactory binary size=$bitmapSize ratio=${extraction.foregroundRatio}",
        )
        return ForcedMonoResult(
            bitmap = argb,
            minAlpha = 0,
            maxAlpha = 255,
            inverted = false,
        )
    }

    private fun extractAosp(params: NothingRenderParams): ForcedMonoResult {
        alphaCanvas.drawBitmap(flatBitmap, 0f, 0f, copyPaint)
        val buffer = ByteBuffer.wrap(pixels)
        buffer.rewind()
        alphaBitmap.copyPixelsToBuffer(buffer)

        var minV = 0xFF
        var maxV = 0
        for (b in pixels) {
            val v = b.toInt() and 0xFF
            minV = min(minV, v)
            maxV = max(maxV, v)
        }

        var flipped = false
        if (minV < maxV) {
            val range = (maxV - minV).toFloat()
            val edgeCount = max(1, edgePixelLength)
            var sum = 0
            for (i in 0 until edgeCount) {
                sum += pixels[i].toInt() and 0xFF
                sum += pixels[pixels.size - 1 - i].toInt() and 0xFF
            }
            val edgeAverage = sum / (edgeCount * 2f)
            val edgeMapped = (edgeAverage - minV) / range
            val autoFlip = params.autoInvert && edgeMapped > 0.5f
            flipped = if (params.invert) !autoFlip else autoFlip

            val contrast = params.contrast.coerceIn(0.25f, 4f)
            val threshold = params.threshold.coerceIn(0f, 1f)
            for (i in pixels.indices) {
                val p = pixels[i].toInt() and 0xFF
                var stretched = ((p - minV) * 255f / range)
                stretched = ((stretched - 127.5f) * contrast + 127.5f).coerceIn(0f, 255f)
                if (threshold > 0f) {
                    stretched = if (stretched / 255f >= threshold) 255f else 0f
                }
                var out = stretched.roundToInt().coerceIn(0, 255)
                if (flipped) out = 255 - out
                pixels[i] = out.toByte()
            }
            buffer.rewind()
            alphaBitmap.copyPixelsFromBuffer(buffer)
        }

        val argb = Bitmap.createBitmap(bitmapSize, bitmapSize, Bitmap.Config.ARGB_8888)
        val src = IntArray(bitmapSize * bitmapSize)
        val alphaPixels = ByteArray(bitmapSize * bitmapSize)
        val alphaBuf = ByteBuffer.wrap(alphaPixels)
        alphaBitmap.copyPixelsToBuffer(alphaBuf)
        for (i in src.indices) {
            val a = alphaPixels[i].toInt() and 0xFF
            src[i] = (a shl 24) or 0x00FFFFFF
        }
        argb.setPixels(src, 0, bitmapSize, 0, 0, bitmapSize, bitmapSize)
        Log.d(
            LabLog.TAG,
            "MonochromeIconFactory aosp size=$bitmapSize min=$minV max=$maxV flip=$flipped contrast=${params.contrast}",
        )
        return ForcedMonoResult(
            bitmap = argb,
            minAlpha = minV,
            maxAlpha = maxV,
            inverted = flipped,
        )
    }

    private fun clipToCircle(src: Bitmap): Bitmap {
        val out = Bitmap.createBitmap(src.width, src.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(out)
        val path = Path().apply {
            addCircle(src.width / 2f, src.height / 2f, src.width / 2f, Path.Direction.CW)
        }
        canvas.save()
        canvas.clipPath(path)
        canvas.drawBitmap(src, 0f, 0f, Paint(Paint.FILTER_BITMAP_FLAG))
        canvas.restore()
        return out
    }

    data class ForcedMonoResult(
        val bitmap: Bitmap,
        val minAlpha: Int,
        val maxAlpha: Int,
        val inverted: Boolean,
    )

    companion object {
        /**
         * Minimum fraction of opaque pixels that must be foreground before
         * the binary path keeps its result. Below this, the path falls
         * back to AOSP — useful for monochrome vector icons where there
         * is no separate "white-on-color" foreground.
         */
        const val BINARY_FOREGROUND_FLOOR: Float = 0.01f
    }
}
