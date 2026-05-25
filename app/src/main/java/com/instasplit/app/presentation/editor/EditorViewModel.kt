package com.instasplit.app.presentation.editor

import android.graphics.Bitmap
import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.instasplit.app.domain.model.AspectRatio
import com.instasplit.app.domain.model.CropState
import com.instasplit.app.domain.usecase.LoadImageUseCase
import com.instasplit.app.presentation.home.BitmapCache
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EditorViewModel @Inject constructor(
    private val loadImage: LoadImageUseCase,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    // Retrieve type-safe argument from navigation/saved-state
    val uriString: String? = savedStateHandle.get<String>("uriString")

    private val _isBitmapLoading = MutableStateFlow(false)
    val isBitmapLoading = _isBitmapLoading.asStateFlow()

    private val _cropState = MutableStateFlow(restoreCropState())
    val cropState = _cropState.asStateFlow()

    val bitmap get() = BitmapCache.get()

    init {
        // Handle process death: reload cached bitmap if cleared
        if (BitmapCache.get() == null && uriString != null) {
            viewModelScope.launch {
                _isBitmapLoading.value = true
                loadImage(Uri.parse(uriString))
                    .onSuccess {
                        BitmapCache.set(it)
                        // Re-clamp state once bitmap is successfully loaded
                        _cropState.value = clampCropState(_cropState.value)
                    }
                    .onFailure {
                        // Suppress or handle error
                    }
                _isBitmapLoading.value = false
            }
        }
    }

    private fun clampCropState(state: CropState): CropState {
        val bmp = bitmap ?: return state

        val ar = state.aspectRatio
        val canvasAspect = (ar.widthRatio * state.slideCount).toFloat() / ar.heightRatio
        val imageAspect = bmp.width.toFloat() / bmp.height.toFloat()
        val viewportAspect = canvasAspect

        val cropWBase: Float
        val cropHBase: Float
        if (imageAspect > viewportAspect) {
            cropHBase = bmp.height.toFloat()
            cropWBase = cropHBase * viewportAspect
        } else {
            cropWBase = bmp.width.toFloat()
            cropHBase = cropWBase / viewportAspect
        }

        val cropW = cropWBase / state.scale
        val cropH = cropHBase / state.scale

        val maxOffsetX = bmp.width.toFloat() - cropW
        val maxOffsetY = bmp.height.toFloat() - cropH

        val clampedX = state.offsetX.coerceIn(0f, maxOf(0f, maxOffsetX))
        val clampedY = state.offsetY.coerceIn(0f, maxOf(0f, maxOffsetY))

        return state.copy(offsetX = clampedX, offsetY = clampedY)
    }

    private fun restoreCropState(): CropState {
        val scale = savedStateHandle.get<Float>("scale") ?: CropStateCache.cropState.scale
        val offsetX = savedStateHandle.get<Float>("offsetX") ?: CropStateCache.cropState.offsetX
        val offsetY = savedStateHandle.get<Float>("offsetY") ?: CropStateCache.cropState.offsetY
        val slideCount = savedStateHandle.get<Int>("slideCount") ?: CropStateCache.cropState.slideCount
        val ratioStr = savedStateHandle.get<String>("aspectRatio")
        val ratio = ratioStr?.let { AspectRatio.valueOf(it) } ?: CropStateCache.cropState.aspectRatio
        val jpegQuality = savedStateHandle.get<Int>("jpegQuality") ?: CropStateCache.cropState.jpegQuality

        return clampCropState(CropState(scale, offsetX, offsetY, slideCount, ratio, jpegQuality))
    }

    private fun updateState(newState: CropState) {
        val clampedState = clampCropState(newState)
        _cropState.value = clampedState
        CropStateCache.cropState = clampedState

        // Persist to SavedStateHandle for process death stability
        savedStateHandle["scale"] = clampedState.scale
        savedStateHandle["offsetX"] = clampedState.offsetX
        savedStateHandle["offsetY"] = clampedState.offsetY
        savedStateHandle["slideCount"] = clampedState.slideCount
        savedStateHandle["aspectRatio"] = clampedState.aspectRatio.name
        savedStateHandle["jpegQuality"] = clampedState.jpegQuality
    }

    fun onScaleChange(scale: Float) {
        val current = _cropState.value
        updateState(current.copy(scale = scale).withClampedScale())
    }

    fun onOffsetChange(dx: Float, dy: Float) {
        val current = _cropState.value
        updateState(current.copy(
            offsetX = current.offsetX + dx,
            offsetY = current.offsetY + dy
        ))
    }

    fun onSlideCountChange(count: Int) {
        val current = _cropState.value
        updateState(current.copy(slideCount = count.coerceIn(2, 10)))
    }

    fun onAspectRatioChange(ratio: AspectRatio) {
        val current = _cropState.value
        updateState(current.copy(aspectRatio = ratio))
    }

    fun onQualityChange(quality: Int) {
        val current = _cropState.value
        updateState(current.copy(jpegQuality = quality.coerceIn(50, 100)))
    }

    fun onReset() {
        updateState(_cropState.value.reset())
    }

    fun onAutoFit() {
        val bmp = bitmap ?: return
        updateState(_cropState.value.autoFit(bmp.width, bmp.height))
    }
}

