package com.anndy999.nothingiconlab.render

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.drawable.Drawable
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

object BitmapUtils {
    fun drawableToBitmap(drawable: Drawable, size: Int): Bitmap {
        val bmp = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bmp)
        drawable.setBounds(0, 0, size, size)
        drawable.draw(canvas)
        return bmp
    }

    fun copy(src: Bitmap): Bitmap = src.copy(src.config ?: Bitmap.Config.ARGB_8888, true)

    fun alphaRange(bitmap: Bitmap): Pair<Int, Int> {
        val w = bitmap.width
        val h = bitmap.height
        val pixels = IntArray(w * h)
        bitmap.getPixels(pixels, 0, w, 0, 0, w, h)
        var minA = 255
        var maxA = 0
        for (p in pixels) {
            val a = Color.alpha(p)
            if (a < minA) minA = a
            if (a > maxA) maxA = a
        }
        return minA to maxA
    }

    fun contentBounds(bitmap: Bitmap, alphaCut: Int): Rect {
        val w = bitmap.width
        val h = bitmap.height
        val pixels = IntArray(w * h)
        bitmap.getPixels(pixels, 0, w, 0, 0, w, h)
        var minX = w
        var minY = h
        var maxX = -1
        var maxY = -1
        for (y in 0 until h) {
            val row = y * w
            for (x in 0 until w) {
                if (Color.alpha(pixels[row + x]) > alphaCut) {
                    if (x < minX) minX = x
                    if (y < minY) minY = y
                    if (x > maxX) maxX = x
                    if (y > maxY) maxY = y
                }
            }
        }
        if (maxX < minX || maxY < minY) {
            return Rect(0, 0, w, h)
        }
        return Rect(minX, minY, maxX + 1, maxY + 1)
    }

    fun crop(bitmap: Bitmap, bounds: Rect): Bitmap {
        val w = max(1, bounds.width())
        val h = max(1, bounds.height())
        val left = bounds.left.coerceIn(0, bitmap.width - 1)
        val top = bounds.top.coerceIn(0, bitmap.height - 1)
        val cw = min(w, bitmap.width - left)
        val ch = min(h, bitmap.height - top)
        return Bitmap.createBitmap(bitmap, left, top, cw, ch)
    }

    fun squarePad(bitmap: Bitmap): Bitmap {
        val side = max(bitmap.width, bitmap.height)
        if (bitmap.width == side && bitmap.height == side) return bitmap
        val out = Bitmap.createBitmap(side, side, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(out)
        val left = (side - bitmap.width) / 2f
        val top = (side - bitmap.height) / 2f
        canvas.drawBitmap(bitmap, left, top, Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG))
        return out
    }

    fun scaleTo(bitmap: Bitmap, size: Int): Bitmap {
        if (bitmap.width == size && bitmap.height == size) return bitmap
        return Bitmap.createScaledBitmap(bitmap, size, size, true)
    }

    fun applyAlphaThreshold(src: Bitmap, threshold: Float): Bitmap {
        if (threshold <= 0f) return src
        val cut = (threshold.coerceIn(0f, 1f) * 255f).roundToInt()
        val w = src.width
        val h = src.height
        val out = src.copy(Bitmap.Config.ARGB_8888, true)
        val pixels = IntArray(w * h)
        out.getPixels(pixels, 0, w, 0, 0, w, h)
        for (i in pixels.indices) {
            val a = Color.alpha(pixels[i])
            pixels[i] = if (a < cut) Color.TRANSPARENT else (pixels[i] or 0xFF000000.toInt())
        }
        out.setPixels(pixels, 0, w, 0, 0, w, h)
        return out
    }

    fun invertAlpha(src: Bitmap): Bitmap {
        val w = src.width
        val h = src.height
        val out = src.copy(Bitmap.Config.ARGB_8888, true)
        val pixels = IntArray(w * h)
        out.getPixels(pixels, 0, w, 0, 0, w, h)
        for (i in pixels.indices) {
            val p = pixels[i]
            val a = Color.alpha(p)
            pixels[i] = (p and 0x00FFFFFF) or ((255 - a) shl 24)
        }
        out.setPixels(pixels, 0, w, 0, 0, w, h)
        return out
    }

    fun toWhiteGlyph(src: Bitmap): Bitmap {
        val w = src.width
        val h = src.height
        val out = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        val pixels = IntArray(w * h)
        src.getPixels(pixels, 0, w, 0, 0, w, h)
        for (i in pixels.indices) {
            val a = Color.alpha(pixels[i])
            pixels[i] = (a shl 24) or 0x00FFFFFF
        }
        out.setPixels(pixels, 0, w, 0, 0, w, h)
        return out
    }
}
