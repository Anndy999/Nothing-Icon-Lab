package com.anndy999.nothingiconlab.data

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.AdaptiveIconDrawable
import android.util.Log
import com.anndy999.nothingiconlab.LabLog
import com.anndy999.nothingiconlab.render.BitmapUtils
import com.anndy999.nothingiconlab.render.MonochromeIconFactory
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
    private const val WORK_SIZE = 192

    fun process(
        context: Context,
        app: LaunchedApp,
        params: NothingRenderParams,
        dark: Boolean,
        outputSize: Int = params.outputSize,
    ): PipelineResult {
        val layers = IconExtractor.extract(context, app)
        val adaptive = IconExtractor.unwrapAdaptive(layers.original)
        val forced = buildForced(adaptive, layers, params)
        val source = SourceResolver.resolve(
            hasNative = layers.hasNativeMonochrome,
            preferNative = params.preferNativeMonochrome,
            forceMono = params.forceMonochrome,
            canForce = forced != null || layers.original != null,
        )
        val glyph = when (source) {
            IconSource.NATIVE_MONO -> layers.nativeMonochromeBitmap
            IconSource.FORCED_MONO -> forced?.bitmap
            IconSource.FALLBACK -> layers.originalBitmap?.let { grayscaleFallback(it) }
        }
        val result = glyph?.let {
            NothingRenderer.render(
                glyph = it,
                source = source,
                params = params,
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
                "native=${layers.hasNativeMonochrome} adaptive=${layers.isAdaptive}",
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

    private fun buildForced(
        adaptive: AdaptiveIconDrawable?,
        layers: AppIconLayers,
        params: NothingRenderParams,
    ): MonochromeIconFactory.ForcedMonoResult? {
        val factory = MonochromeIconFactory(WORK_SIZE)
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
