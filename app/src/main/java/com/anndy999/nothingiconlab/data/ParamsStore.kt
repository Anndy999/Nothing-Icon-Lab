package com.anndy999.nothingiconlab.data

import android.content.Context
import com.anndy999.nothingiconlab.render.NothingRenderParams
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class ParamsStore(context: Context) {
    private val prefs = context.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
        prettyPrint = true
    }

    fun loadParams(): NothingRenderParams {
        val raw = prefs.getString(KEY_PARAMS, null) ?: return NothingRenderParams.nothingDefault()
        return try {
            json.decodeFromString(NothingRenderParams.serializer(), raw)
        } catch (_: Throwable) {
            NothingRenderParams.nothingDefault()
        }
    }

    fun saveParams(params: NothingRenderParams) {
        prefs.edit().putString(KEY_PARAMS, json.encodeToString(params)).apply()
    }

    fun encodeParams(params: NothingRenderParams): String =
        json.encodeToString(NothingRenderParams.serializer(), params)

    fun loadBad(): Set<String> = prefs.getStringSet(KEY_BAD, emptySet())?.toSet() ?: emptySet()

    fun saveBad(bad: Set<String>) {
        prefs.edit().putStringSet(KEY_BAD, bad).apply()
    }

    companion object {
        private const val PREFS = "nothing_icon_lab"
        private const val KEY_PARAMS = "params_json_v3"
        private const val KEY_BAD = "bad_components"
    }
}
