package com.anndy999.nothingiconlab.render

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.graphics.RectF
import android.util.Log
import com.anndy999.nothingiconlab.LabLog
import com.anndy999.nothingiconlab.data.IconSource

/**
 * Reconstructs Nothing ThemedIconDrawable composition.
 *
 * On device, ThemedIconDrawable.drawInternal draws mBgBitmap then mMonoIcon
 * both full-bleed into the icon bounds, with SRC_IN colorBg / colorFg.
 * 0.3888889 is already inside mMonoIcon (n3/a.g / n3/a.h / o3/a.m).
 * This lab keeps the glyph unscaled and applies logoScale at compose time
 * so the Params page can still tune it. Same visual, not a second crop.
 *
 * Nada is not used for geometry.
 */
object NothingRenderer {

    fun render(
        glyph: Bitmap,
        source: IconSource,
        params: NothingRenderParams,
        packageName: String,
        component: String,
        dark: Boolean,
        size: Int = params.outputSize,
    ): Bitmap {
        var working = BitmapUtils.toWhiteGlyph(glyph)
        working = BitmapUtils.applyAlphaThreshold(working, params.alphaThreshold)

        if (params.cropToContent) {
            val alphaCut = (params.alphaThreshold.coerceIn(0f, 1f) * 255f).toInt()
            val bounds = BitmapUtils.contentBounds(working, alphaCut)
            working = BitmapUtils.squarePad(BitmapUtils.crop(working, bounds))
        }

        val (minA, maxA) = BitmapUtils.alphaRange(working)
        val bg = if (dark) params.darkBackground else params.lightBackground
        val fg = if (dark) params.darkForeground else params.lightForeground

        val out = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(out)
        val cx = size / 2f
        val cy = size / 2f
        val bgRadius = (size / 2f) * params.backgroundSize.coerceIn(0.2f, 1.2f)
        val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = bg }
        canvas.drawCircle(cx, cy, bgRadius, bgPaint)

        val logo = params.effectiveLogoScale().coerceIn(0.05f, 1.4f)
        val logoPx = size * logo
        val dst = RectF(
            cx - logoPx / 2f,
            cy - logoPx / 2f,
            cx + logoPx / 2f,
            cy + logoPx / 2f,
        )
        val glyphPaint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG).apply {
            colorFilter = PorterDuffColorFilter(fg, PorterDuff.Mode.SRC_IN)
        }
        canvas.drawBitmap(working, null, dst, glyphPaint)

        Log.i(
            LabLog.TAG,
            "NothingRenderer package=$packageName component=$component source=$source " +
                "scale=${params.logoScale} fgScale=${params.foregroundScale} " +
                "adaptiveInset=${params.adaptiveIconInset} monoInset=${params.monochromeInset} " +
                "invert=${params.invert} autoInvert=${params.autoInvert} " +
                "alphaRange=$minA..$maxA dark=$dark size=$size",
        )
        return out
    }
}
