package com.anndy999.nothingiconlab.data

import android.graphics.Bitmap
import android.graphics.drawable.Drawable

/**
 * Layers extracted from the real installed APK. No third-party logos.
 */
data class AppIconLayers(
    val app: LaunchedApp,
    val original: Drawable?,
    val originalBitmap: Bitmap?,
    val isAdaptive: Boolean,
    val foreground: Drawable?,
    val foregroundBitmap: Bitmap?,
    val background: Drawable?,
    val backgroundBitmap: Bitmap?,
    val nativeMonochrome: Drawable?,
    val nativeMonochromeBitmap: Bitmap?,
    val hasNativeMonochrome: Boolean,
)
