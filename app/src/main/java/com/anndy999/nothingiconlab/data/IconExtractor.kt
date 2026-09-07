package com.anndy999.nothingiconlab.data

import android.content.Context
import android.content.pm.PackageManager
import android.graphics.drawable.AdaptiveIconDrawable
import android.graphics.drawable.Drawable
import android.util.Log
import com.anndy999.nothingiconlab.LabLog
import com.anndy999.nothingiconlab.render.BitmapUtils
import com.anndy999.nothingiconlab.render.NothingRenderParams

/**
 * Reads the real installed APK icon. Never substitutes a downloaded brand logo.
 * Drawables stay Drawables; bitmaps are rasterized at the caller's size.
 */
object IconExtractor {

    fun extract(
        context: Context,
        app: LaunchedApp,
        layerSize: Int,
        extraInset: Float = NothingRenderParams.DEFAULT_ADAPTIVE_INSET,
    ): AppIconLayers {
        val pm = context.packageManager
        val original = loadHighResIcon(pm, app)
        val adaptive = unwrapAdaptive(original)
        val foreground = adaptive?.foreground
        val background = adaptive?.background
        val nativeMono = adaptive?.monochrome
        val layers = AppIconLayers(
            app = app,
            original = original,
            originalBitmap = original?.let { BitmapUtils.drawableToBitmap(it, layerSize) },
            isAdaptive = adaptive != null,
            foreground = foreground,
            foregroundBitmap = foreground?.let {
                BitmapUtils.rasterizeAdaptiveLayer(it, layerSize, extraInset)
            },
            background = background,
            backgroundBitmap = background?.let {
                BitmapUtils.rasterizeAdaptiveLayer(it, layerSize, extraInset)
            },
            nativeMonochrome = nativeMono,
            nativeMonochromeBitmap = nativeMono?.let {
                BitmapUtils.rasterizeClippedMono(it, layerSize, extraInset)
            },
            hasNativeMonochrome = nativeMono != null,
        )
        Log.i(
            LabLog.TAG,
            "IconExtractor package=${app.packageName} component=${app.componentFlattened} " +
                "adaptive=${layers.isAdaptive} nativeMono=${layers.hasNativeMonochrome} layerSize=$layerSize",
        )
        return layers
    }

    private fun loadHighResIcon(pm: PackageManager, app: LaunchedApp): Drawable? {
        val component = app.component
        try {
            val ri = pm.getActivityInfo(component, PackageManager.GET_META_DATA)
            val iconRes = ri.icon.takeIf { it != 0 } ?: ri.applicationInfo?.icon ?: 0
            if (iconRes != 0) {
                val res = pm.getResourcesForActivity(component)
                val density = android.util.DisplayMetrics.DENSITY_XXXHIGH
                res.getDrawableForDensity(iconRes, density, null)?.let { return it }
                @Suppress("DEPRECATION")
                res.getDrawable(iconRes, null)?.let { return it }
            }
        } catch (t: Throwable) {
            Log.d(LabLog.TAG, "high-res icon failed for ${app.componentFlattened}: $t")
        }
        return try {
            pm.getActivityIcon(component)
        } catch (t: Throwable) {
            Log.w(LabLog.TAG, "activity icon failed for ${app.componentFlattened}", t)
            try {
                pm.getApplicationIcon(app.packageName)
            } catch (t2: Throwable) {
                Log.w(LabLog.TAG, "application icon failed for ${app.packageName}", t2)
                null
            }
        }
    }

    fun unwrapAdaptive(drawable: Drawable?): AdaptiveIconDrawable? {
        var current = drawable
        var hops = 0
        while (current != null && hops < 6) {
            if (current is AdaptiveIconDrawable) return current
            current = try {
                val method = current.javaClass.methods.firstOrNull {
                    it.name == "getDrawable" && it.parameterCount == 0
                }
                method?.invoke(current) as? Drawable
            } catch (_: Throwable) {
                null
            }
            hops++
        }
        return drawable as? AdaptiveIconDrawable
    }
}
