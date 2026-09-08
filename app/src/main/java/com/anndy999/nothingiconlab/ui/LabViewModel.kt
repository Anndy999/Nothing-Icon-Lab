package com.anndy999.nothingiconlab.ui

import android.app.Application
import android.content.Intent
import android.graphics.Bitmap
import android.util.Log
import androidx.core.content.FileProvider
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.anndy999.nothingiconlab.LabLog
import com.anndy999.nothingiconlab.R
import com.anndy999.nothingiconlab.data.IconPipeline
import com.anndy999.nothingiconlab.data.IconSource
import com.anndy999.nothingiconlab.data.LaunchedApp
import com.anndy999.nothingiconlab.data.LauncherAppScanner
import com.anndy999.nothingiconlab.data.ParamsStore
import com.anndy999.nothingiconlab.data.PipelineResult
import com.anndy999.nothingiconlab.data.VerifyApps
import com.anndy999.nothingiconlab.export.TestPackExporter
import com.anndy999.nothingiconlab.render.NothingRenderParams
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

enum class LabTab { GRID, LIST, PARAMS }
enum class SourceFilter { ALL, NATIVE, FORCED, FALLBACK, BAD, VERIFY }

data class LabUiState(
    val loading: Boolean = true,
    val exporting: Boolean = false,
    val exportProgress: Int = 0,
    val exportTotal: Int = 0,
    val apps: List<LaunchedApp> = emptyList(),
    val query: String = "",
    val tab: LabTab = LabTab.GRID,
    val filter: SourceFilter = SourceFilter.ALL,
    val params: NothingRenderParams = NothingRenderParams.nothingDefault(),
    val selected: LaunchedApp? = null,
    val selectedResult: PipelineResult? = null,
    val previewCache: Map<String, CachedPreview> = emptyMap(),
    val bad: Set<String> = emptySet(),
    val message: String? = null,
    val darkPreview: Boolean = false,
)

data class CachedPreview(
    val result: Bitmap,
    val source: IconSource,
    val original: Bitmap?,
    val glyph: Bitmap?,
)

class LabViewModel(application: Application) : AndroidViewModel(application) {
    private val store = ParamsStore(application)
    private val cacheMutex = Mutex()
    private var debounceJob: Job? = null

    private val _state = MutableStateFlow(
        LabUiState(
            params = store.loadParams(),
            bad = store.loadBad(),
            darkPreview = store.loadParams().previewDark,
        ),
    )
    val state: StateFlow<LabUiState> = _state

    init { refreshApps() }

    fun refreshApps() {
        viewModelScope.launch {
            _state.update { it.copy(loading = true) }
            val apps = withContext(Dispatchers.IO) { LauncherAppScanner.scan(getApplication()) }
            _state.update { it.copy(loading = false, apps = apps, previewCache = emptyMap()) }
            Log.i(LabLog.TAG, "ViewModel loaded ${apps.size} apps")
        }
    }

    fun setQuery(query: String) = _state.update { it.copy(query = query) }
    fun setTab(tab: LabTab) = _state.update { it.copy(tab = tab) }
    fun setFilter(filter: SourceFilter) = _state.update { it.copy(filter = filter) }
    fun consumeMessage() = _state.update { it.copy(message = null) }

    fun updateParams(transform: (NothingRenderParams) -> NothingRenderParams) {
        val next = transform(_state.value.params)
        _state.update { it.copy(params = next, previewCache = emptyMap(), selectedResult = null) }
        debounceJob?.cancel()
        debounceJob = viewModelScope.launch {
            delay(80)
            store.saveParams(next)
            _state.value.selected?.let { loadDetail(it) }
        }
    }

    fun resetParams() {
        updateParams { NothingRenderParams.nothingDefault() }
        _state.update { it.copy(message = getApplication<Application>().getString(R.string.reset_done)) }
    }

    fun setDarkPreview(dark: Boolean) {
        _state.update { it.copy(darkPreview = dark, previewCache = emptyMap(), selectedResult = null) }
        updateParams { it.copy(previewDark = dark) }
    }

    fun select(app: LaunchedApp?) {
        _state.update { it.copy(selected = app, selectedResult = null) }
        if (app != null) loadDetail(app)
    }

    fun toggleBad(app: LaunchedApp) {
        val next = _state.value.bad.toMutableSet()
        if (!next.add(app.componentFlattened)) next.remove(app.componentFlattened)
        store.saveBad(next)
        _state.update { it.copy(bad = next) }
    }

    fun visibleApps(): List<LaunchedApp> {
        val s = _state.value
        val q = s.query.trim().lowercase()
        return s.apps.filter { app ->
            val matchesQuery = q.isEmpty() || app.label.lowercase().contains(q) || app.packageName.lowercase().contains(q) || app.activityName.lowercase().contains(q)
            val source = s.previewCache[app.componentFlattened]?.source
            val matchesFilter = when (s.filter) {
                SourceFilter.ALL -> true
                SourceFilter.NATIVE -> source == IconSource.NATIVE_MONO
                SourceFilter.FORCED -> source == IconSource.FORCED_MONO
                SourceFilter.FALLBACK -> source == IconSource.FALLBACK
                SourceFilter.BAD -> app.componentFlattened in s.bad
                SourceFilter.VERIFY -> VerifyApps.matches(app)
            }
            matchesQuery && matchesFilter
        }
    }

    fun ensurePreview(app: LaunchedApp) {
        val key = app.componentFlattened
        if (_state.value.previewCache.containsKey(key)) return
        viewModelScope.launch {
            val preview = withContext(Dispatchers.Default) { renderPreview(app) } ?: return@launch
            cacheMutex.withLock { _state.update { it.copy(previewCache = it.previewCache + (key to preview)) } }
        }
    }

    private fun loadDetail(app: LaunchedApp) {
        viewModelScope.launch {
            val result = withContext(Dispatchers.Default) {
                val s = _state.value
                IconPipeline.process(getApplication(), app, s.params, s.darkPreview, NothingRenderParams.DETAIL_SIZE, NothingRenderParams.FORCED_WORK_SIZE, NothingRenderParams.DETAIL_SIZE)
            }
            _state.update { it.copy(selectedResult = result) }
        }
    }

    private fun renderPreview(app: LaunchedApp): CachedPreview? {
        val s = _state.value
        val processed = IconPipeline.process(getApplication(), app, s.params, s.darkPreview, NothingRenderParams.PREVIEW_SIZE, NothingRenderParams.PREVIEW_FORCED_WORK_SIZE, NothingRenderParams.PREVIEW_SIZE)
        val result = processed.nothingResult ?: return null
        return CachedPreview(result, processed.source, processed.layers.originalBitmap, processed.glyph)
    }

    fun export() {
        viewModelScope.launch {
            val apps = _state.value.apps
            _state.update { it.copy(exporting = true, exportProgress = 0, exportTotal = apps.size) }
            try {
                val file = withContext(Dispatchers.IO) {
                    TestPackExporter.export(getApplication(), apps, _state.value.params, _state.value.darkPreview) { done, total ->
                        _state.update { it.copy(exportProgress = done, exportTotal = total) }
                    }
                }
                share(file)
                _state.update { it.copy(exporting = false, message = getApplication<Application>().getString(R.string.export_done, apps.size)) }
            } catch (t: Throwable) {
                Log.e(LabLog.TAG, "export failed", t)
                _state.update { it.copy(exporting = false, message = getApplication<Application>().getString(R.string.export_failed, t.message ?: "")) }
            }
        }
    }

    private fun share(file: java.io.File) {
        val context = getApplication<Application>()
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.files", file)
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "application/zip"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(Intent.createChooser(intent, context.getString(R.string.export_chooser)).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
    }
}
