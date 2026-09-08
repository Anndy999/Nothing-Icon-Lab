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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.anndy999.nothingiconlab.R
import com.anndy999.nothingiconlab.render.ForcedMonoStyle
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
        Text(stringResource(R.string.params_title), style = MaterialTheme.typography.titleLarge)
        Text(
            stringResource(R.string.params_blurb),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 12.dp),
        )
        ParamSlider(stringResource(R.string.param_logo_scale), p.logoScale, 0.10f..1.00f) { v ->
            viewModel.updateParams { it.copy(logoScale = v) }
        }
        ParamSlider(stringResource(R.string.param_foreground_scale), p.foregroundScale, 0.50f..1.50f) { v ->
            viewModel.updateParams { it.copy(foregroundScale = v) }
        }
        ParamSlider(stringResource(R.string.param_adaptive_inset), p.adaptiveIconInset, 0f..0.40f) { v ->
            viewModel.updateParams { it.copy(adaptiveIconInset = v) }
        }
        ParamSlider(stringResource(R.string.param_mono_inset), p.monochromeInset, 0f..0.40f) { v ->
            viewModel.updateParams { it.copy(monochromeInset = v) }
        }
        ParamSlider(stringResource(R.string.param_background_size), p.backgroundSize, 0.50f..1.20f) { v ->
            viewModel.updateParams { it.copy(backgroundSize = v) }
        }
        ParamSlider(stringResource(R.string.param_threshold), p.threshold, 0f..1f) { v ->
            viewModel.updateParams { it.copy(threshold = v) }
        }
        ParamSlider(stringResource(R.string.param_contrast), p.contrast, 0.25f..3f) { v ->
            viewModel.updateParams { it.copy(contrast = v) }
        }
        ParamSlider(stringResource(R.string.param_alpha_threshold), p.alphaThreshold, 0f..0.40f) { v ->
            viewModel.updateParams { it.copy(alphaThreshold = v) }
        }

        Toggle(stringResource(R.string.param_invert), p.invert) { v -> viewModel.updateParams { it.copy(invert = v) } }
        Toggle(stringResource(R.string.param_auto_invert), p.autoInvert) { v ->
            viewModel.updateParams { it.copy(autoInvert = v) }
        }
        Toggle(stringResource(R.string.param_force_mono), p.forceMonochrome) { v ->
            viewModel.updateParams { it.copy(forceMonochrome = v) }
        }
        Toggle(stringResource(R.string.param_prefer_native), p.preferNativeMonochrome) { v ->
            viewModel.updateParams { it.copy(preferNativeMonochrome = v) }
        }
        Toggle(stringResource(R.string.param_crop), p.cropToContent) { v ->
            viewModel.updateParams { it.copy(cropToContent = v) }
        }
        Toggle(stringResource(R.string.param_nada_binary), p.forcedMonoStyle == ForcedMonoStyle.NOTHING_BINARY) { v ->
            viewModel.updateParams {
                it.copy(forcedMonoStyle = if (v) ForcedMonoStyle.NOTHING_BINARY else ForcedMonoStyle.AOSP)
            }
        }
        Toggle(stringResource(R.string.param_system_neutral), p.useSystemNeutralColors) { v ->
            viewModel.updateParams { it.copy(useSystemNeutralColors = v) }
        }
        Toggle(stringResource(R.string.param_dark_preview), state.darkPreview) { viewModel.setDarkPreview(it) }

        Button(
            onClick = { viewModel.resetParams() },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp),
        ) {
            Text(stringResource(R.string.reset_defaults))
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
