package com.anndy999.nothingiconlab.render

import android.content.Context

/**
 * Nothing Launcher 2.5.9 binds plate/glyph to:
 *   android.R.color.system_neutral1_50  (light)
 *   android.R.color.system_neutral1_900 (dark / night)
 * via color/mono_nothing_background_color and color/mono_nothing_foreground_color.
 */
object NothingColors {
    fun resolve(context: Context, params: NothingRenderParams, dark: Boolean): Pair<Int, Int> {
        if (params.useSystemNeutralColors) {
            val light = context.getColor(android.R.color.system_neutral1_50)
            val darkC = context.getColor(android.R.color.system_neutral1_900)
            return if (dark) darkC to light else light to darkC
        }
        return if (dark) {
            params.darkBackground to params.darkForeground
        } else {
            params.lightBackground to params.lightForeground
        }
    }
}
