package com.anndy999.nothingiconlab.export

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DrawableNameTest {
    @Test
    fun chromeMain() {
        val name = DrawableName.fromComponent(
            "com.android.chrome",
            "com.google.android.apps.chrome.Main",
        )
        assertEquals("chrome", name)
    }

    @Test
    fun wechatUsesShortName() {
        val name = DrawableName.fromComponent(
            "com.tencent.mm",
            "com.tencent.mm.ui.LauncherUI",
        )
        assertEquals("wechat", name)
    }

    @Test
    fun startsWithDigitIsPrefixed() {
        val name = DrawableName.fromComponent("123.app", "MainActivity")
        assertTrue(name.startsWith("i_"))
    }

    @Test
    fun onlySafeChars() {
        val name = DrawableName.fromComponent("com.foo.Bar-1", "ui.Home.Activity")
        assertFalse(name.contains("."))
        assertFalse(name.contains("-"))
        assertTrue(name.matches(Regex("[a-z0-9_]+")))
    }
}
