package com.anndy999.nothingiconlab.data

import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.drawable.AdaptiveIconDrawable
import android.graphics.drawable.Drawable
import android.util.Log
import com.anndy999.nothingiconlab.LabLog
import com.anndy999.nothingiconlab.render.BitmapUtils

/**
 * Reads the real installed APK icon. Never substitutes a downloaded brand logo.
 */
object IconExtractor {
    private const val LAYER_SIZE = 192

    fun extract(context: Context, app: LaunchedApp): AppIconLayers {
        val pm = context.packageManager
        val original = loadHighResIcon(pm, app)
        val adaptive = unwrapAdaptive(original)
        val foreground = adaptive?.foreground
        val background = adaptive?.background
        val nativeMono = adaptive?.monochrome
        val layers = AppIconLayers(
            app = app,
            original = original,
            originalBitmap = original?.let { BitmapUtils.drawableToBitmap(it, LAYER_SIZE) },
            isAdaptive = adaptive != null,
            foreground = foreground,
            foregroundBitmap = foreground?.let { BitmapUtils.drawableToBitmap(it, LAYER_SIZE) },
            background = background,
            backgroundBitmap = background?.let { BitmapUtils.drawableToBitmap(it, LAYER_SIZE) },
            nativeMonochrome = nativeMono,
            nativeMonochromeBitmap = nativeMono?.let { BitmapUtils.drawableToBitmap(it, LAYER_SIZE) },
            hasNativeMonochrome = nativeMono != null,
        )
        Log.i(
            LabLog.TAG,
            "IconExtractor package=${app.packageName} component=${app.componentFlattened} " +
                "adaptive=${layers.isAdaptive} nativeMono=${layers.hasNativeMonochrome}",
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
