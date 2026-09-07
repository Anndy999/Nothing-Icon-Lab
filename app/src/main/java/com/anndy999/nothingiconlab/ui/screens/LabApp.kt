package com.anndy999.nothingiconlab.ui.screens

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Apps
import androidx.compose.material.icons.outlined.GridView
import androidx.compose.material.icons.outlined.IosShare
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.anndy999.nothingiconlab.R
import com.anndy999.nothingiconlab.ui.LabTab
import com.anndy999.nothingiconlab.ui.LabViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LabApp(viewModel: LabViewModel) {
    val state by viewModel.state.collectAsState()
    val snackbar = remember { SnackbarHostState() }

    LaunchedEffect(state.message) {
        val msg = state.message ?: return@LaunchedEffect
        snackbar.showSnackbar(msg)
        viewModel.consumeMessage()
    }

    if (state.selected != null) {
        AppDetailScreen(viewModel = viewModel)
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.app_name)) },
            )
        },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = state.tab == LabTab.GRID,
                    onClick = { viewModel.setTab(LabTab.GRID) },
                    icon = { Icon(Icons.Outlined.GridView, contentDescription = stringResource(R.string.tab_grid)) },
                    label = { Text(stringResource(R.string.tab_grid)) },
                )
                NavigationBarItem(
                    selected = state.tab == LabTab.LIST,
                    onClick = { viewModel.setTab(LabTab.LIST) },
                    icon = { Icon(Icons.Outlined.Apps, contentDescription = stringResource(R.string.tab_list)) },
                    label = { Text(stringResource(R.string.tab_list)) },
                )
                NavigationBarItem(
                    selected = state.tab == LabTab.PARAMS,
                    onClick = { viewModel.setTab(LabTab.PARAMS) },
                    icon = { Icon(Icons.Outlined.Tune, contentDescription = stringResource(R.string.tab_params)) },
                    label = { Text(stringResource(R.string.tab_params)) },
                )
                NavigationBarItem(
                    selected = false,
                    onClick = { if (!state.exporting) viewModel.export() },
                    icon = { Icon(Icons.Outlined.IosShare, contentDescription = stringResource(R.string.tab_export)) },
                    label = { Text(stringResource(R.string.tab_export)) },
                )
            }
        },
        snackbarHost = { SnackbarHost(snackbar) },
    ) { padding ->
        if (state.exporting) {
            LinearProgressIndicator(modifier = Modifier.padding(padding))
        }
        when (state.tab) {
            LabTab.GRID -> GridScreen(viewModel, Modifier.padding(padding))
            LabTab.LIST -> AppListScreen(viewModel, Modifier.padding(padding))
            LabTab.PARAMS -> ParamsScreen(viewModel, Modifier.padding(padding))
        }
    }
}
