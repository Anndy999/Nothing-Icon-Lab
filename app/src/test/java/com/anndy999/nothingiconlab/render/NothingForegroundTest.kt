package com.anndy999.nothingiconlab.render

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Pure-JVM tests for [NadaForeground.extractFromPixels]. No Android runtime,
 * no Robolectric — only the integer math in the inner loop is exercised.
 */
class NothingForegroundTest {
    private fun argb(a: Int, r: Int, g: Int, b: Int): Int =
        (a shl 24) or (r shl 16) or (g shl 8) or b

    @Test
    fun extractFindsWhiteCircleOnRedSquare() {
        val size = 100
        val pixels = IntArray(size * size) { idx ->
            val x = idx % size
            val y = idx / size
            val dx = x - size / 2
            val dy = y - size / 2
            val r2 = dx * dx + dy * dy
            if (r2 <= 10 * 10) argb(255, 250, 250, 250) else argb(255, 230, 30, 30)
        }
        val result = NadaForeground.extractFromPixels(pixels, size, size)
        assertTrue(
            "foregroundRatio too small: ${result.foregroundRatio}",
            result.foregroundRatio > 0.01f,
        )
        assertTrue(
            "foregroundRatio too large: ${result.foregroundRatio}",
            result.foregroundRatio < 0.5f,
        )
        var circleHits = 0
        for (y in 30 until 70) {
            for (x in 30 until 70) {
                val dx = x - 50
                val dy = y - 50
                if (dx * dx + dy * dy <= 100 &&
                    (result.mask[y * size + x].toInt() and 0xFF) > 0
                ) {
                    circleHits++
                }
            }
        }
        assertTrue("no foreground pixels inside the white circle", circleHits > 0)
    }

    @Test
    fun extractIgnoresColoredForeground() {
        // red circle on green background: nothing is white-ish, ratio must collapse.
        val size = 100
        val pixels = IntArray(size * size) { idx ->
            val x = idx % size
            val y = idx / size
            val dx = x - size / 2
            val dy = y - size / 2
            val r2 = dx * dx + dy * dy
            if (r2 <= 10 * 10) argb(255, 230, 30, 30) else argb(255, 30, 230, 30)
        }
        val result = NadaForeground.extractFromPixels(pixels, size, size)
        assertEquals(0f, result.foregroundRatio, 0.001f)
    }

    @Test
    fun extractSkipsTransparentAreas() {
        val size = 50
        val pixels = IntArray(size * size) { argb(0, 0, 0, 0) }
        val result = NadaForeground.extractFromPixels(pixels, size, size)
        assertEquals(0f, result.foregroundRatio, 0.001f)
    }

    @Test
    fun extractPreservesEdgeAlpha() {
        val size = 30
        val pixels = IntArray(size * size) { idx ->
            val x = idx % size
            val y = idx / size
            if (x in 10..20 && y in 10..20) argb(180, 250, 250, 250) else argb(0, 0, 0, 0)
        }
        val result = NadaForeground.extractFromPixels(pixels, size, size)
        val centerAlpha = result.mask[15 * size + 15].toInt() and 0xFF
        assertEquals(180, centerAlpha)
    }

    @Test
    fun extractRejectsNearWhiteWithHighSaturation() {
        // pinky-yellow: luma high, saturation just over the cut — should not be foreground.
        val pixels = IntArray(64 * 64) { argb(255, 250, 230, 200) }
        val result = NadaForeground.extractFromPixels(pixels, 64, 64)
        // max-min = 50, > satCut 70? no, 50 < 70 so it WOULD be foreground here.
        // make a stricter case: yellow max-min = 50, sat cut = 30, this should reject.
        val strict = NadaForeground.extractFromPixels(pixels, 64, 64, saturationCut = 30f)
        assertEquals(0f, strict.foregroundRatio, 0.001f)
    }
}
