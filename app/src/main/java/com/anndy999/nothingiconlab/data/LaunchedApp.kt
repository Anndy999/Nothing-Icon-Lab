package com.anndy999.nothingiconlab.data

import android.content.ComponentName

data class LaunchedApp(
    val label: String,
    val packageName: String,
    val activityName: String,
    val component: ComponentName,
) {
    val componentFlattened: String
        get() = component.flattenToString()

    val componentInfoString: String
        get() = "ComponentInfo{${component.packageName}/${component.className}}"
}
