package com.anndy999.nothingiconlab.export

import org.junit.Assert.assertTrue
import org.junit.Test

class AppFilterXmlTest {
    @Test
    fun usesComponentInfoFormat() {
        val xml = AppFilterXml.document(
            listOf(
                AppFilterItem(
                    packageName = "com.android.chrome",
                    activityName = "com.google.android.apps.chrome.Main",
                    drawable = "com_android_chrome_main",
                ),
            ),
        )
        assertTrue(xml.contains("ComponentInfo{com.android.chrome/com.google.android.apps.chrome.Main}"))
        assertTrue(xml.contains("drawable=\"com_android_chrome_main\""))
        assertTrue(xml.trim().startsWith("<?xml"))
        assertTrue(xml.contains("<resources>"))
    }
}
