package com.anndy999.nothingiconlab.render

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.abs

class NothingRenderParamsTest {
    @Test
    fun defaultIsWhitePlateBlackGlyph() {
        val params = NothingRenderParams.nothingDefault()
        assertTrue(abs(params.logoScale - (106f / 288f)) < 1e-6)
        assertEquals(0f, params.adaptiveIconInset)
        assertEquals(0f, params.monochromeInset)
        assertTrue(params.preferNativeMonochrome)
        assertTrue(params.cropToContent)
        assertFalse(params.useSystemNeutralColors)
        assertFalse(params.previewDark)
        assertEquals(NothingRenderParams.NADA_GLYPH, params.lightBackground)
        assertEquals(NothingRenderParams.NADA_PLATE, params.lightForeground)
        assertEquals(NothingRenderParams.NADA_PLATE, params.darkBackground)
        assertEquals(NothingRenderParams.NADA_GLYPH, params.darkForeground)
    }

    @Test
    fun effectiveLogoScaleMultipliesForeground() {
        val params = NothingRenderParams(logoScale = 0.4f, foregroundScale = 1.25f)
        assertEquals(0.5f, params.effectiveLogoScale(), 1e-5f)
    }
}
