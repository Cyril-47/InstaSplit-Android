package com.instasplit.app.presentation.preview

import android.graphics.Bitmap
import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.instasplit.app.domain.usecase.ComputeSlicesUseCase
import com.instasplit.app.domain.usecase.LoadImageUseCase
import com.instasplit.app.presentation.editor.CropStateCache
import com.instasplit.app.presentation.home.BitmapCache
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

data class PreviewUiState(
    val isLoading: Boolean = false,
    val slices: List<Bitmap> = emptyList(),
    val error: String? = null
)

@HiltViewModel
class PreviewViewModel @Inject constructor(
    private val computeSlices: ComputeSlicesUseCase,
    private val loadImage: LoadImageUseCase,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    // Retrieve type-safe URI from navigation/saved-state
    val uriString: String? = savedStateHandle.get<String>("uriString")

    private val _uiState = MutableStateFlow(PreviewUiState())
    val uiState = _uiState.asStateFlow()

    init {
        val bitmap = BitmapCache.get()
        if (bitmap == null && uriString != null) {
            viewModelScope.launch {
                _uiState.value = PreviewUiState(isLoading = true)
                loadImage(Uri.parse(uriString))
                    .onSuccess {
                        BitmapCache.set(it)
                        generatePreviewSlices()
                    }
                    .onFailure { e ->
                        _uiState.value = PreviewUiState(error = e.message ?: "Failed to reload image")
                    }
            }
        } else {
            generatePreviewSlices()
        }
    }

    private fun generatePreviewSlices() {
        val bitmap = BitmapCache.get()
        val cropState = CropStateCache.cropState
        if (bitmap == null) {
            _uiState.value = PreviewUiState(error = "No image found in cache")
            return
        }

        viewModelScope.launch {
            _uiState.value = PreviewUiState(isLoading = true)
            try {
                val slices = withContext(Dispatchers.Default) {
                    val configs = computeSlices(bitmap.width, bitmap.height, cropState)
                    val ar = cropState.aspectRatio
                    configs.map { config ->
                        val slice = Bitmap.createBitmap(ar.outputW, ar.outputH, Bitmap.Config.ARGB_8888)
                        val canvas = android.graphics.Canvas(slice)
                        val paint = android.graphics.Paint(android.graphics.Paint.FILTER_BITMAP_FLAG)
                        val srcRect = android.graphics.Rect(
                            config.srcRect.left.toInt(),
                            config.srcRect.top.toInt(),
                            config.srcRect.right.toInt(),
                            config.srcRect.bottom.toInt()
                        )
                        canvas.drawBitmap(bitmap, srcRect, config.dstRect, paint)
                        slice
                    }
                }
                _uiState.value = PreviewUiState(slices = slices)
            } catch (e: Exception) {
                _uiState.value = PreviewUiState(error = e.message ?: "Failed to render preview slices")
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        _uiState.value.slices.forEach {
            if (!it.isRecycled) it.recycle()
        }
    }
}

