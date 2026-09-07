package com.anndy999.nothingiconlab.data

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class VerifyAppsTest {
    @Test
    fun containsTheNineTargets() {
        val needed = listOf(
            "com.android.chrome",
            "com.tencent.mm",
            "com.openai.chatgpt",
            "com.twitter.android",
            "com.ss.android.ugc.aweme",
            "org.telegram.messenger",
            "com.google.android.googlequicksearchbox",
            "com.google.android.gm",
            "com.google.android.youtube",
        )
        needed.forEach { pkg ->
            assertTrue(pkg, pkg in VerifyApps.PACKAGES)
        }
        assertFalse("com.nothing.launcher" in VerifyApps.PACKAGES)
    }
}
