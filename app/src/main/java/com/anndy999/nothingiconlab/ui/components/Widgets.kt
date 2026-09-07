package com.anndy999.nothingiconlab.ui.components

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.anndy999.nothingiconlab.R
import com.anndy999.nothingiconlab.data.IconSource
import com.anndy999.nothingiconlab.ui.SourceFilter

@Composable
fun BitmapIcon(bitmap: Bitmap?, size: Dp, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surfaceVariant),
        contentAlignment = Alignment.Center,
    ) {
        if (bitmap != null) {
            Image(
                bitmap = bitmap.asImageBitmap(),
                contentDescription = null,
                modifier = Modifier.size(size),
                contentScale = ContentScale.Crop,
            )
        }
    }
}

@Composable
fun SourceChip(source: IconSource?) {
    val label = source?.let { stringResource(it.shortLabelRes) } ?: "…"
    FilterChip(
        selected = false,
        onClick = {},
        enabled = false,
        label = { Text(label) },
        colors = FilterChipDefaults.filterChipColors(
            disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
            disabledLabelColor = MaterialTheme.colorScheme.onSurface,
        ),
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FilterRow(
    selected: SourceFilter,
    onSelect: (SourceFilter) -> Unit,
) {
    val items = listOf(
        SourceFilter.ALL to stringResource(R.string.filter_all),
        SourceFilter.NATIVE to stringResource(R.string.filter_native),
        SourceFilter.FORCED to stringResource(R.string.filter_forced),
        SourceFilter.FALLBACK to stringResource(R.string.filter_fallback),
        SourceFilter.BAD to stringResource(R.string.filter_bad),
        SourceFilter.VERIFY to stringResource(R.string.filter_verify),
    )
    FlowRow {
        items.forEach { (value, label) ->
            FilterChip(
                selected = selected == value,
                onClick = { onSelect(value) },
                label = { Text(label) },
            )
        }
    }
}

@Composable
fun ParamSlider(
    label: String,
    value: Float,
    range: ClosedFloatingPointRange<Float>,
    format: (Float) -> String = { "%.3f".format(it) },
    onChange: (Float) -> Unit,
) {
    androidx.compose.foundation.layout.Column {
        androidx.compose.foundation.layout.Row {
            Text(label, modifier = Modifier.weight(1f), maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(format(value), color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Slider(value = value, onValueChange = onChange, valueRange = range)
    }
}
