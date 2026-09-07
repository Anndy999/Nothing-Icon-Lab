package com.anndy999.nothingiconlab.data

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.AdaptiveIconDrawable
import android.graphics.drawable.Drawable
import android.util.Log
import com.anndy999.nothingiconlab.LabLog
import com.anndy999.nothingiconlab.render.BitmapUtils
import com.anndy999.nothingiconlab.render.MonochromeIconFactory
import com.anndy999.nothingiconlab.render.NothingColors
import com.anndy999.nothingiconlab.render.NothingRenderParams
import com.anndy999.nothingiconlab.render.NothingRenderer

data class PipelineResult(
    val app: LaunchedApp,
    val layers: AppIconLayers,
    val source: IconSource,
    val glyph: Bitmap?,
    val forcedMono: Bitmap?,
    val nothingResult: Bitmap?,
    val inverted: Boolean,
    val alphaMin: Int,
    val alphaMax: Int,
)

object IconPipeline {

    fun process(
        context: Context,
        app: LaunchedApp,
        params: NothingRenderParams,
        dark: Boolean,
        outputSize: Int,
        forcedWorkSize: Int = NothingRenderParams.FORCED_WORK_SIZE,
        layerSize: Int = outputSize,
    ): PipelineResult {
        val layers = IconExtractor.extract(context, app, layerSize, params.adaptiveIconInset)
        val adaptive = IconExtractor.unwrapAdaptive(layers.original)
        val forced = buildForced(adaptive, layers, params, forcedWorkSize)
        val source = SourceResolver.resolve(
            hasNative = layers.hasNativeMonochrome,
            preferNative = params.preferNativeMonochrome,
            forceMono = params.forceMonochrome,
            canForce = forced != null || layers.original != null,
        )
        val glyph = when (source) {
            IconSource.NATIVE_MONO -> rasterizeNative(layers.nativeMonochrome, outputSize, params)
            IconSource.FORCED_MONO -> forced?.bitmap?.let { prepareForcedGlyph(it, params) }
            IconSource.FALLBACK -> fallbackGlyph(layers, outputSize)
        }
        val (bg, fg) = NothingColors.resolve(context, params, dark)
        val colored = params.copy(
            lightBackground = if (dark) params.lightBackground else bg,
            lightForeground = if (dark) params.lightForeground else fg,
            darkBackground = if (dark) bg else params.darkBackground,
            darkForeground = if (dark) fg else params.darkForeground,
        )
        val result = glyph?.let {
            NothingRenderer.render(
                glyph = it,
                source = source,
                params = colored,
                packageName = app.packageName,
                component = app.componentFlattened,
                dark = dark,
                size = outputSize,
            )
        }
        val (minA, maxA) = glyph?.let { BitmapUtils.alphaRange(it) } ?: (0 to 0)
        Log.i(
            LabLog.TAG,
            "IconSource: $source package=${app.packageName} component=${app.componentFlattened} " +
                "native=${layers.hasNativeMonochrome} adaptive=${layers.isAdaptive} " +
                "output=$outputSize forcedWork=$forcedWorkSize",
        )
        return PipelineResult(
            app = app,
            layers = layers,
            source = source,
            glyph = glyph,
            forcedMono = forced?.bitmap,
            nothingResult = result,
            inverted = forced?.inverted == true || params.invert,
            alphaMin = minA,
            alphaMax = maxA,
        )
    }

    private fun rasterizeNative(
        drawable: Drawable?,
        size: Int,
        params: NothingRenderParams,
    ): Bitmap? {
        if (drawable == null) return null
        return BitmapUtils.rasterizeClippedMono(drawable, size, params.adaptiveIconInset)
    }

    /**
     * Forced path matches IconGrayConverter o3/a.m: content Rect then scale.
     * Scale itself is applied later by [NothingRenderer] as logoScale 0.3888889
     * (ThemedIconDrawable draws mMonoIcon full-bleed; the 0.3888889 is already
     * in that bitmap on device, which we reconstruct at compose time).
     */
    private fun prepareForcedGlyph(bitmap: Bitmap, params: NothingRenderParams): Bitmap {
        if (params.cropToContent) return bitmap
        val alphaCut = (params.alphaThreshold.coerceIn(0f, 1f) * 255f).toInt()
        return BitmapUtils.cropToContentSquare(bitmap, alphaCut)
    }

    private fun fallbackGlyph(layers: AppIconLayers, size: Int): Bitmap? {
        val src = layers.original?.let { BitmapUtils.drawableToBitmap(it, size) }
            ?: layers.originalBitmap
            ?: return null
        return grayscaleFallback(src)
    }

    private fun buildForced(
        adaptive: AdaptiveIconDrawable?,
        layers: AppIconLayers,
        params: NothingRenderParams,
        workSize: Int,
    ): MonochromeIconFactory.ForcedMonoResult? {
        val factory = MonochromeIconFactory(workSize)
        return try {
            if (adaptive != null) {
                factory.wrap(adaptive, params)
            } else {
                val original = layers.original ?: return null
                factory.wrapNonAdaptive(original, params)
            }
        } catch (t: Throwable) {
            Log.w(LabLog.TAG, "forced mono failed for ${layers.app.packageName}", t)
            null
        }
    }

    private fun grayscaleFallback(src: Bitmap): Bitmap {
        val w = src.width
        val h = src.height
        val out = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        val pixels = IntArray(w * h)
        src.getPixels(pixels, 0, w, 0, 0, w, h)
        for (i in pixels.indices) {
            val p = pixels[i]
            val a = android.graphics.Color.alpha(p)
            val r = android.graphics.Color.red(p)
            val g = android.graphics.Color.green(p)
            val b = android.graphics.Color.blue(p)
            val y = (0.299f * r + 0.587f * g + 0.114f * b).toInt().coerceIn(0, 255)
            pixels[i] = (a shl 24) or (y shl 16) or (y shl 8) or y
        }
        out.setPixels(pixels, 0, w, 0, 0, w, h)
        return BitmapUtils.toWhiteGlyph(out)
    }
}
