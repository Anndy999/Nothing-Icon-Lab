package com.anndy999.nothingiconlab.data

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.util.Log
import com.anndy999.nothingiconlab.LabLog

/**
 * Launchable-app scanner modeled after Lawnicons GetSystemPackageList:
 * ACTION_MAIN + CATEGORY_LAUNCHER.
 */
object LauncherAppScanner {
    fun scan(context: Context): List<LaunchedApp> {
        val pm = context.packageManager
        val intent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
        val resolved = try {
            pm.queryIntentActivities(
                intent,
                PackageManager.ResolveInfoFlags.of(PackageManager.MATCH_ALL.toLong()),
            )
        } catch (t: Throwable) {
            Log.e(LabLog.TAG, "scan failed", t)
            emptyList()
        }
        val apps = resolved.mapNotNull { ri ->
            val activity = ri.activityInfo ?: return@mapNotNull null
            val pkg = activity.packageName ?: return@mapNotNull null
            val name = activity.name ?: return@mapNotNull null
            val label = ri.loadLabel(pm)?.toString()?.ifBlank { pkg } ?: pkg
            LaunchedApp(
                label = label,
                packageName = pkg,
                activityName = name,
                component = ComponentName(pkg, name),
            )
        }.distinctBy { it.componentFlattened }
            .sortedBy { it.label.lowercase() }
        Log.i(LabLog.TAG, "LauncherAppScanner found ${apps.size} launchable apps")
        return apps
    }
}
