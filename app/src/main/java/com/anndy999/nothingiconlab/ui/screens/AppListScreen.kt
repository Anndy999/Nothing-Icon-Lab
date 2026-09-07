package com.anndy999.nothingiconlab.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.anndy999.nothingiconlab.R
import com.anndy999.nothingiconlab.ui.LabViewModel
import com.anndy999.nothingiconlab.ui.components.BitmapIcon
import com.anndy999.nothingiconlab.ui.components.FilterRow

@Composable
fun AppListScreen(viewModel: LabViewModel, modifier: Modifier = Modifier) {
    val state by viewModel.state.collectAsState()
    val apps = viewModel.visibleApps()

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(12.dp),
    ) {
        item {
            OutlinedTextField(
                value = state.query,
                onValueChange = viewModel::setQuery,
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                label = { Text(stringResource(R.string.search_short)) },
            )
            FilterRow(selected = state.filter, onSelect = viewModel::setFilter)
        }
        items(apps, key = { it.componentFlattened }) { app ->
            LaunchedEffect(app.componentFlattened, state.params, state.darkPreview) {
                viewModel.ensurePreview(app)
            }
            val preview = state.previewCache[app.componentFlattened]
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { viewModel.select(app) }
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                BitmapIcon(bitmap = preview?.original, size = 40.dp)
                BitmapIcon(bitmap = preview?.glyph, size = 40.dp, modifier = Modifier.padding(start = 8.dp))
                BitmapIcon(bitmap = preview?.result, size = 48.dp, modifier = Modifier.padding(start = 8.dp))
                Column(modifier = Modifier.padding(start = 12.dp)) {
                    Text(app.label, style = MaterialTheme.typography.titleSmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text(app.packageName, style = MaterialTheme.typography.bodySmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text(app.activityName.substringAfterLast('.'), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
                    Text(
                        preview?.source?.let { stringResource(it.longLabelRes) } ?: stringResource(R.string.rendering),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.secondary,
                    )
                }
            }
        }
    }
}
