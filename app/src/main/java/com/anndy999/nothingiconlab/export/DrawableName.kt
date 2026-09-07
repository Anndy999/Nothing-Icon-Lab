package com.anndy999.nothingiconlab.export

object DrawableName {
    fun fromComponent(packageName: String, activityName: String): String {
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
}
