package com.anndy999.nothingiconlab.data

import org.junit.Assert.assertEquals
import org.junit.Test

class SourceResolverTest {
    @Test
    fun nativeWinsWhenPreferred() {
        assertEquals(
            IconSource.NATIVE_MONO,
            SourceResolver.resolve(
                hasNative = true,
                preferNative = true,
                forceMono = false,
                canForce = true,
            ),
        )
    }

    @Test
    fun forceMonoOverridesNative() {
        assertEquals(
            IconSource.FORCED_MONO,
            SourceResolver.resolve(
                hasNative = true,
                preferNative = true,
                forceMono = true,
                canForce = true,
            ),
        )
    }

    @Test
    fun forcedWhenNoNative() {
        assertEquals(
            IconSource.FORCED_MONO,
            SourceResolver.resolve(
                hasNative = false,
                preferNative = true,
                forceMono = false,
                canForce = true,
            ),
        )
    }

    @Test
    fun fallbackWhenNothingWorks() {
        assertEquals(
            IconSource.FALLBACK,
            SourceResolver.resolve(
                hasNative = false,
                preferNative = true,
                forceMono = false,
                canForce = false,
            ),
        )
    }
}
