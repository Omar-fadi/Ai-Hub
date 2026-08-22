package com.example

import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.theme.AIHubTheme
import com.example.viewmodel.MainViewModel
import com.example.ui.swfexplorer.AppNavigation
import java.io.File
import java.io.FileOutputStream

class MainActivity : ComponentActivity() {

    private lateinit var viewModel: MainViewModel
    
    private val openDocumentLauncher = registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
        uri?.let {
            val file = copyUriToFile(it)
            if (file != null) {
                viewModel.loadSwf(file)
            }
        }
    }
    
    private val createDocumentLauncher = registerForActivityResult(ActivityResultContracts.CreateDocument("application/x-shockwave-flash")) { uri: Uri? ->
        uri?.let {
            viewModel.saveSwf(it, applicationContext)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AIHubTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    viewModel = viewModel()
                    AppNavigation(
                        viewModel = viewModel,
                        onOpenSwf = { openDocumentLauncher.launch(arrayOf("*/*")) },
                        onSaveSwf = { createDocumentLauncher.launch("edited.swf") }
                    )
                }
            }
        }
    }
    
    private fun copyUriToFile(uri: Uri): File? {
        return try {
            val inputStream = contentResolver.openInputStream(uri)
            val file = File(cacheDir, "temp.swf")
            val outputStream = FileOutputStream(file)
            inputStream?.copyTo(outputStream)
            inputStream?.close()
            outputStream.close()
            file
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
