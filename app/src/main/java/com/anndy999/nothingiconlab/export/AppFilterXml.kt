package com.anndy999.nothingiconlab.export

data class AppFilterItem(
    val packageName: String,
    val activityName: String,
    val drawable: String,
) {
    val componentInfo: String
        get() = "ComponentInfo{" + packageName + "/" + activityName + "}"
}

object AppFilterXml {
    fun document(items: List<AppFilterItem>): String {
        val body = items.joinToString("\n") { item ->
            "    <item component=\"" + item.componentInfo + "\" drawable=\"" + item.drawable + "\" />"
        }
        return "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n<resources>\n" + body + "\n</resources>\n"
    }
}
