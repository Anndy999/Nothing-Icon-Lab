package com.anndy999.nothingiconlab.render

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.abs

class NothingRenderParamsTest {
    @Test
    fun defaultLogoScaleMatchesNothingLauncherBytecode() {
        val params = NothingRenderParams.nothingDefault()
        assertTrue(abs(params.logoScale - 0.3888889f) < 1e-6)
        assertEquals(0.25f, params.adaptiveIconInset)
        assertEquals(1f / 6f, params.monochromeInset)
        assertTrue(params.preferNativeMonochrome)
        assertFalse(params.cropToContent)
        assertTrue(params.useSystemNeutralColors)
        assertEquals(NothingRenderParams.NEUTRAL_50, params.lightBackground)
        assertEquals(NothingRenderParams.NEUTRAL_900, params.lightForeground)
        assertEquals(NothingRenderParams.NEUTRAL_900, params.darkBackground)
        assertEquals(NothingRenderParams.NEUTRAL_50, params.darkForeground)
    }

    @Test
    fun effectiveLogoScaleMultipliesForeground() {
        val params = NothingRenderParams(logoScale = 0.4f, foregroundScale = 1.25f)
        assertEquals(0.5f, params.effectiveLogoScale(), 1e-5f)
    }
}
