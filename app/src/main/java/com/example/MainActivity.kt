package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.VLearnAppContent
import com.example.ui.VLearnViewModel
import com.example.ui.VLearnViewModelFactory

class MainActivity : ComponentActivity() {
  private val viewModel: VLearnViewModel by viewModels {
    VLearnViewModelFactory(application)
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      MyApplicationTheme {
        VLearnAppContent(viewModel)
      }
    }
  }
}
