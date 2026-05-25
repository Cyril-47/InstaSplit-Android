package com.instasplit.app.presentation.home

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.instasplit.app.domain.usecase.LoadImageUseCase
import com.instasplit.app.presentation.editor.CropStateCache
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeUiState(
    val isLoading: Boolean = false,
    val error: String? = null
)

sealed class HomeEvent {
    data class NavigateToEditor(val uri: Uri) : HomeEvent()
}

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val loadImage: LoadImageUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<HomeEvent>()
    val events = _events.asSharedFlow()

    fun onUriReceived(uri: Uri) {
        viewModelScope.launch {
            _uiState.value = HomeUiState(isLoading = true)
            loadImage(uri)
                .onSuccess {
                    BitmapCache.set(it)      // Store in the in-memory singleton cache
                    CropStateCache.reset()   // Reset editor state
                    CropStateCache.cropState = CropStateCache.cropState.autoFit(it.width, it.height)
                    _uiState.value = HomeUiState()
                    _events.emit(HomeEvent.NavigateToEditor(uri))
                }
                .onFailure { e ->
                    _uiState.value = HomeUiState(error = e.message ?: "Failed to load image")
                }
        }
    }

    fun clearError() { _uiState.value = HomeUiState() }
}

