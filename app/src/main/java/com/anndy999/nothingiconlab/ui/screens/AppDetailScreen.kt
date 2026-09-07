package com.anndy999.nothingiconlab.ui.screens

import android.graphics.Bitmap
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Flag
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.anndy999.nothingiconlab.ui.LabViewModel
import com.anndy999.nothingiconlab.ui.components.BitmapIcon

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AppDetailScreen(viewModel: LabViewModel) {
    val state by viewModel.state.collectAsState()
    val app = state.selected ?: return
    val result = state.selectedResult
    val bad = app.componentFlattened in state.bad

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(app.label, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                navigationIcon = {
                    IconButton(onClick = { viewModel.select(null) }) {
                        Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.toggleBad(app) }) {
                        Icon(
                            Icons.Outlined.Flag,
                            contentDescription = "Mark as bad",
                            tint = if (bad) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onSurface,
                        )
                    }
                },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            BitmapIcon(
                bitmap = result?.nothingResult,
                size = 128.dp,
                modifier = Modifier.align(Alignment.CenterHorizontally),
            )
            Text(result?.source?.longLabel ?: "Rendering...", style = MaterialTheme.typography.titleMedium)
            Meta("packageName", app.packageName)
            Meta("launcher Activity", app.activityName)
            Meta("ComponentName", app.componentFlattened)
            if (result != null) {
                Meta("alpha range", "${result.alphaMin}..${result.alphaMax}")
                Meta("invert", result.inverted.toString())
            }

            Text("Layers", style = MaterialTheme.typography.titleMedium)
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Layer("Original", result?.layers?.originalBitmap)
                Layer("Foreground", result?.layers?.foregroundBitmap)
                Layer("Background", result?.layers?.backgroundBitmap)
                Layer("Monochrome", result?.layers?.nativeMonochromeBitmap)
                Layer("Forced Mono", result?.forcedMono)
                Layer("Nothing Result", result?.nothingResult)
            }

            ParamsScreen(viewModel = viewModel, modifier = Modifier.fillMaxWidth(), scrollable = false)
        }
    }
}

@Composable
private fun Meta(label: String, value: String) {
    Column {
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
private fun Layer(label: String, bitmap: Bitmap?) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        BitmapIcon(bitmap = bitmap, size = 72.dp)
        Text(label, style = MaterialTheme.typography.labelSmall, modifier = Modifier.padding(top = 4.dp))
    }
}
