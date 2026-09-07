package com.anndy999.nothingiconlab

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.anndy999.nothingiconlab.ui.LabViewModel
import com.anndy999.nothingiconlab.ui.screens.LabApp
import com.anndy999.nothingiconlab.ui.theme.NothingIconLabTheme

class MainActivity : ComponentActivity() {
    private val viewModel: LabViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val state by viewModel.state.collectAsState()
            val dark = if (state.params.followSystemDark) isSystemInDarkTheme() else state.darkPreview
            NothingIconLabTheme(darkTheme = dark) {
                LabApp(viewModel = viewModel)
            }
        }
    }
}
