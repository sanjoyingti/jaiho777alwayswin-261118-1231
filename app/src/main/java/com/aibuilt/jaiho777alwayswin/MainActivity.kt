package com.aibuilt.jaiho777alwayswin

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.viewmodel.compose.viewModel
import com.aibuilt.jaiho777alwayswin.ui.AlwaysWinApp
import com.aibuilt.jaiho777alwayswin.ui.theme.JaihoTheme
import com.aibuilt.jaiho777alwayswin.viewmodel.MainViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            JaihoTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val vm: MainViewModel = viewModel()
                    AlwaysWinApp(vm)
                }
            }
        }
    }
}
