package com.example.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.domain.SwfEngine
import com.example.swf.model.SwfFile
import com.example.swf.tags.DefineEditText
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File

sealed class AppState {
    object Idle : AppState()
    object Loading : AppState()
    data class Loaded(val file: File, val swf: SwfFile) : AppState()
    data class Error(val message: String) : AppState()
}

class MainViewModel : ViewModel() {
    private val engine = SwfEngine()

    private val _appState = MutableStateFlow<AppState>(AppState.Idle)
    val appState: StateFlow<AppState> = _appState.asStateFlow()

    private val _editTexts = MutableStateFlow<List<Pair<Int, DefineEditText>>>(emptyList())
    val editTexts: StateFlow<List<Pair<Int, DefineEditText>>> = _editTexts.asStateFlow()

    private val _unsupportedTextTags = MutableStateFlow<List<Int>>(emptyList())
    val unsupportedTextTags: StateFlow<List<Int>> = _unsupportedTextTags.asStateFlow()

    fun loadSwf(file: File) {
        viewModelScope.launch {
            _appState.value = AppState.Loading
            try {
                val swf = engine.parseSwf(file)
                val texts = engine.extractEditTexts(swf)
                _editTexts.value = texts
                _unsupportedTextTags.value = engine.getUnsupportedTextTags(swf)
                _appState.value = AppState.Loaded(file, swf)
            } catch (e: Exception) {
                _appState.value = AppState.Error(e.message ?: "Unknown Error")
            }
        }
    }

    fun modifyText(tagIndex: Int, newText: String) {
        val state = _appState.value
        if (state is AppState.Loaded) {
            engine.applyTextModification(state.swf, tagIndex, newText)
            // Refresh list
            _editTexts.value = engine.extractEditTexts(state.swf)
        }
    }

    fun saveSwf(uri: Uri, context: android.content.Context) {
        val state = _appState.value
        if (state is AppState.Loaded) {
            viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
                _appState.value = AppState.Loading
                val tempOut = File(context.cacheDir, "temp_build.swf")
                val success = engine.buildSwf(state.swf, tempOut)
                if (success) {
                    try {
                        context.contentResolver.openOutputStream(uri)?.use { outStream ->
                            tempOut.inputStream().use { inStream ->
                                inStream.copyTo(outStream)
                            }
                        }
                        _appState.value = AppState.Loaded(state.file, state.swf)
                    } catch (e: Exception) {
                        _appState.value = AppState.Error("Failed to write to selected file: ${e.message}")
                    }
                } else {
                    _appState.value = AppState.Error("Build failed during validation.")
                }
            }
        }
    }
    
    fun closeFile() {
        _appState.value = AppState.Idle
        _editTexts.value = emptyList()
        _unsupportedTextTags.value = emptyList()
    }
}
