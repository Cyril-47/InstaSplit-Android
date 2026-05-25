package com.instasplit.app.presentation.export

import android.graphics.Bitmap
import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.instasplit.app.domain.model.ExportResult
import com.instasplit.app.domain.repository.ExportRepository
import com.instasplit.app.domain.usecase.ExportZipUseCase
import com.instasplit.app.domain.usecase.LoadImageUseCase
import com.instasplit.app.presentation.editor.CropStateCache
import com.instasplit.app.presentation.home.BitmapCache
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class ExportUiState {
    object Idle : ExportUiState()
    data class Exporting(val progress: Float, val completedSlice: Bitmap?) : ExportUiState()
    data class Success(val zipUri: Uri, val slideUris: List<Uri>) : ExportUiState()
    data class Error(val message: String) : ExportUiState()
}

@HiltViewModel
class ExportViewModel @Inject constructor(
    private val exportZip: ExportZipUseCase,
    private val loadImage: LoadImageUseCase,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    // Retrieve type-safe URI from navigation/saved-state
    val uriString: String? = savedStateHandle.get<String>("uriString")

    private val _uiState = MutableStateFlow<ExportUiState>(ExportUiState.Idle)
    val uiState = _uiState.asStateFlow()

    fun triggerExport() {
        val bitmap = BitmapCache.get()
        if (bitmap == null) {
            if (uriString != null) {
                // Handle process death: reload image from SavedStateHandle before exporting
                viewModelScope.launch {
                    _uiState.value = ExportUiState.Idle
                    loadImage(Uri.parse(uriString))
                        .onSuccess { reloadedBitmap ->
                            BitmapCache.set(reloadedBitmap)
                            runExport(reloadedBitmap)
                        }
                        .onFailure { e ->
                            _uiState.value = ExportUiState.Error(e.message ?: "Failed to reload image for export")
                        }
                }
            } else {
                _uiState.value = ExportUiState.Error("No image found in cache")
            }
            return
        }

        runExport(bitmap)
    }

    private fun runExport(bitmap: Bitmap) {
        val cropState = CropStateCache.cropState
        viewModelScope.launch {
            exportZip.invoke(bitmap, cropState).collect { event ->
                when (event) {
                    is ExportRepository.ExportEvent.Progress -> {
                        _uiState.value = ExportUiState.Exporting(event.fraction, event.completedSlice)
                    }
                    is ExportRepository.ExportEvent.Done -> {
                        when (val result = event.result) {
                            is ExportResult.Success -> {
                                _uiState.value = ExportUiState.Success(result.zipUri, result.slideUris)
                            }
                            is ExportResult.Error -> {
                                _uiState.value = ExportUiState.Error(result.message)
                            }
                        }
                    }
                }
            }
        }
    }
}

