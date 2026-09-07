package com.anndy999.nothingiconlab.data

/**
 * First-pass packages to inspect in the lab. Not an icon source.
 */
object VerifyApps {
    val PACKAGES: Set<String> = setOf(
        "com.android.chrome",
        "com.chrome.browser",
        "com.tencent.mm",
        "com.openai.chatgpt",
        "com.twitter.android",
        "com.ss.android.ugc.aweme",
        "org.telegram.messenger",
        "org.telegram.messenger.web",
        "com.google.android.googlequicksearchbox",
        "com.google.android.gm",
        "com.google.android.youtube",
    )

    fun matches(app: LaunchedApp): Boolean = app.packageName in PACKAGES
}
