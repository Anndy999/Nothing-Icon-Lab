package com.anndy999.nothingiconlab.export

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import com.anndy999.nothingiconlab.LabLog
import com.anndy999.nothingiconlab.data.IconPipeline
import com.anndy999.nothingiconlab.data.IconSource
import com.anndy999.nothingiconlab.data.LaunchedApp
import com.anndy999.nothingiconlab.data.ParamsStore
import com.anndy999.nothingiconlab.render.NothingRenderParams
import java.io.BufferedOutputStream
import java.io.File
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

object TestPackExporter {
    const val ZIP_NAME = "NothingIconLab-export.zip"

    fun export(
        context: Context,
        apps: List<LaunchedApp>,
        params: NothingRenderParams,
        dark: Boolean,
        onProgress: (Int, Int) -> Unit = { _, _ -> },
    ): File {
        val outFile = File(context.cacheDir, ZIP_NAME)
        if (outFile.exists()) outFile.delete()
        val store = ParamsStore(context)
        val filterItems = mutableListOf<AppFilterItem>()
        val appsTxt = StringBuilder()
        appsTxt.appendLine("App Name\tpackage\tActivity\tComponentName\tsource")

        ZipOutputStream(BufferedOutputStream(outFile.outputStream())).use { zip ->
            apps.forEachIndexed { index, app ->
                onProgress(index + 1, apps.size)
                val processed = IconPipeline.process(context, app, params, dark, outputSize = params.outputSize)
                val drawable = DrawableName.fromComponent(app.packageName, app.activityName)
                filterItems += AppFilterItem(app.packageName, app.activityName, drawable)
                appsTxt.appendLine(
                    "${app.label}\t${app.packageName}\t${app.activityName}\t" +
                        "${app.componentFlattened}\t${processed.source.name}",
                )
                processed.nothingResult?.let { putPng(zip, "icons/final/$drawable.png", it) }
                processed.layers.nativeMonochromeBitmap?.let {
                    putPng(zip, "icons/native_monochrome/$drawable.png", it)
                }
                processed.forcedMono?.let { putPng(zip, "icons/forced_monochrome/$drawable.png", it) }
                if (processed.source == IconSource.FALLBACK) {
                    processed.glyph?.let { putPng(zip, "icons/fallback/$drawable.png", it) }
                }
            }
            putText(zip, "appfilter.xml", AppFilterXml.document(filterItems))
            putText(zip, "apps.txt", appsTxt.toString())
            putText(zip, "config.json", store.encodeParams(params))
        }
        Log.i(LabLog.TAG, "Export wrote ${apps.size} apps to ${outFile.absolutePath}")
        return outFile
    }

    private fun putPng(zip: ZipOutputStream, path: String, bitmap: Bitmap) {
        zip.putNextEntry(ZipEntry(path))
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, zip)
        zip.closeEntry()
    }

    private fun putText(zip: ZipOutputStream, path: String, text: String) {
        zip.putNextEntry(ZipEntry(path))
        zip.write(text.toByteArray(Charsets.UTF_8))
        zip.closeEntry()
    }
}
