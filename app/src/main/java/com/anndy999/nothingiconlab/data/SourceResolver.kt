package com.anndy999.nothingiconlab.data

object SourceResolver {
    fun resolve(
        hasNative: Boolean,
        preferNative: Boolean,
        forceMono: Boolean,
        canForce: Boolean,
    ): IconSource {
        if (preferNative && hasNative && !forceMono) return IconSource.NATIVE_MONO
        if (canForce) return IconSource.FORCED_MONO
        if (hasNative) return IconSource.NATIVE_MONO
        return IconSource.FALLBACK
    }
}
