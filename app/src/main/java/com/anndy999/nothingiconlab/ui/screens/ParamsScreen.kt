package com.anndy999.nothingiconlab.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.anndy999.nothingiconlab.ui.LabViewModel
import com.anndy999.nothingiconlab.ui.components.ParamSlider

@Composable
fun ParamsScreen(
    viewModel: LabViewModel,
    modifier: Modifier = Modifier,
    scrollable: Boolean = true,
) {
    val state by viewModel.state.collectAsState()
    val p = state.params
    val scrollMod = if (scrollable) modifier.verticalScroll(rememberScrollState()) else modifier
    Column(
        modifier = scrollMod
            .fillMaxWidth()
            .padding(16.dp),
    ) {
        Text("Nothing renderer", style = MaterialTheme.typography.titleLarge)
        Text(
            "Defaults come from AOSP AdaptiveIconDrawable / MonochromeIconFactory and the hypothesized 0.3888889 logo scale. Every value is live-tunable.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 12.dp),
        )
        ParamSlider("Logo Scale", p.logoScale, 0.10f..1.00f) { v ->
            viewModel.updateParams { it.copy(logoScale = v) }
        }
        ParamSlider("Foreground Scale", p.foregroundScale, 0.50f..1.50f) { v ->
            viewModel.updateParams { it.copy(foregroundScale = v) }
        }
        ParamSlider("Adaptive Icon Inset", p.adaptiveIconInset, 0f..0.40f) { v ->
            viewModel.updateParams { it.copy(adaptiveIconInset = v) }
        }
        ParamSlider("Monochrome Inset", p.monochromeInset, 0f..0.40f) { v ->
            viewModel.updateParams { it.copy(monochromeInset = v) }
        }
        ParamSlider("Background Size", p.backgroundSize, 0.50f..1.20f) { v ->
            viewModel.updateParams { it.copy(backgroundSize = v) }
        }
        ParamSlider("Threshold", p.threshold, 0f..1f) { v ->
            viewModel.updateParams { it.copy(threshold = v) }
        }
        ParamSlider("Contrast", p.contrast, 0.25f..3f) { v ->
            viewModel.updateParams { it.copy(contrast = v) }
        }
        ParamSlider("Alpha Threshold", p.alphaThreshold, 0f..0.40f) { v ->
            viewModel.updateParams { it.copy(alphaThreshold = v) }
        }

        Toggle("Invert", p.invert) { v -> viewModel.updateParams { it.copy(invert = v) } }
        Toggle("Auto invert (AOSP edge average)", p.autoInvert) { v ->
            viewModel.updateParams { it.copy(autoInvert = v) }
        }
        Toggle("Force Monochrome", p.forceMonochrome) { v ->
            viewModel.updateParams { it.copy(forceMonochrome = v) }
        }
        Toggle("Prefer Native Monochrome", p.preferNativeMonochrome) { v ->
            viewModel.updateParams { it.copy(preferNativeMonochrome = v) }
        }
        Toggle("Crop to content", p.cropToContent) { v ->
            viewModel.updateParams { it.copy(cropToContent = v) }
        }
        Toggle("Dark preview plate", state.darkPreview) { viewModel.setDarkPreview(it) }

        Button(
            onClick = { viewModel.resetParams() },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp),
        ) {
            Text("Reset to Nothing Default")
        }
    }
}

@Composable
private fun Toggle(label: String, checked: Boolean, onChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label, modifier = Modifier.weight(1f))
        Switch(checked = checked, onCheckedChange = onChange)
    }
}
