package com.anndy999.nothingiconlab.render

import android.content.Context

/**
 * Default look is a white circular plate and a black glyph.
 * Optional path still resolves Nothing's system_neutral1_50 / 900 when
 * [NothingRenderParams.useSystemNeutralColors] is on.
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
