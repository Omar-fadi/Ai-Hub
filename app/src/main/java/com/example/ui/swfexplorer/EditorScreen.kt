package com.example.ui.swfexplorer

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditorScreen(
    viewModel: MainViewModel,
    tagIndex: Int,
    onBack: () -> Unit
) {
    val editTexts by viewModel.editTexts.collectAsState()
    val editTextPair = editTexts.find { it.first == tagIndex }
    
    if (editTextPair == null) {
        onBack()
        return
    }
    
    val editText = editTextPair.second
    var textValue by remember { mutableStateOf(editText.initialText ?: "") }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Edit Text") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        viewModel.modifyText(tagIndex, textValue)
                        onBack()
                    }) {
                        Icon(Icons.Default.Check, contentDescription = "Apply")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize().padding(16.dp)) {
            Text("Variable Name: ${editText.variableName}", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = textValue,
                onValueChange = { textValue = it },
                label = { Text("Initial Text") },
                modifier = Modifier.fillMaxWidth().weight(1f),
                textStyle = MaterialTheme.typography.bodyLarge
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = {
                    viewModel.modifyText(tagIndex, textValue)
                    onBack()
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Apply Changes")
            }
        }
    }
}
