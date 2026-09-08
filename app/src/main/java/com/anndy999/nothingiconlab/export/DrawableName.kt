package com.anndy999.nothingiconlab.export

object DrawableName {
    private val PACKAGE_ALIASES = mapOf(
        "com.android.chrome" to "chrome",
        "com.google.android.googlequicksearchbox" to "google",
        "com.google.android.gm" to "gmail",
        "com.google.android.youtube" to "youtube",
        "com.tencent.mm" to "wechat",
        "com.openai.chatgpt" to "chatgpt",
        "com.twitter.android" to "x",
        "org.telegram.messenger" to "telegram",
        "org.telegram.messenger.web" to "telegram",
        "app.nixgramx.android" to "nixgramx",
        "ai.x.grok" to "grok",
        "com.ss.android.ugc.aweme" to "douyin",
        "com.appshub.bettbox" to "bettbox",
        "com.anndy999.nothingiconlab" to "nothing_icon_lab",
    )

    fun fromComponent(packageName: String, activityName: String): String {
        PACKAGE_ALIASES[packageName]?.let { return it }
        val shortActivity = activityName.substringAfterLast('.')
        val raw = "${packageName}_$shortActivity"
            .lowercase()
            .replace(Regex("[^a-z0-9]+"), "_")
            .trim('_')
        val named = if (raw.isEmpty()) {
            "unknown"
        } else if (raw.first().isDigit()) {
            "i_$raw"
        } else {
            raw
        }
        return named.take(90)
    }

    fun isLegal(name: String): Boolean = name.matches(Regex("^[a-z][a-z0-9_]*$"))
}
