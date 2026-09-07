package com.anndy999.nothingiconlab.render

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.abs

class NothingRenderParamsTest {
    @Test
    fun defaultGeometryIsNothingNotNada() {
        val params = NothingRenderParams.nothingDefault()
        assertTrue(abs(params.logoScale - 0.3888889f) < 1e-6)
        assertEquals(0.25f, params.adaptiveIconInset)
        assertEquals(1f / 6f, params.monochromeInset)
        assertFalse(params.cropToContent)
        assertEquals(0f, params.alphaThreshold)
        assertTrue(params.preferNativeMonochrome)
        assertFalse(params.useSystemNeutralColors)
        assertFalse(params.previewDark)
        assertEquals(NothingRenderParams.WHITE_PLATE, params.lightBackground)
        assertEquals(NothingRenderParams.BLACK_GLYPH, params.lightForeground)
        assertEquals(NothingRenderParams.BLACK_GLYPH, params.darkBackground)
        assertEquals(NothingRenderParams.WHITE_PLATE, params.darkForeground)
        assertEquals(512, params.outputSize)
        assertEquals(576, NothingRenderParams.FORCED_WORK_SIZE)
        assertEquals(128, NothingRenderParams.PREVIEW_SIZE)
    }

    @Test
    fun effectiveLogoScaleMultipliesForeground() {
        val params = NothingRenderParams(logoScale = 0.4f, foregroundScale = 1.25f)
        assertEquals(0.5f, params.effectiveLogoScale(), 1e-5f)
    }
}
