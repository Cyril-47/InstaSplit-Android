package com.instasplit.app.presentation.editor

import com.instasplit.app.domain.model.CropState

/**
 * In-memory singleton cache for the editor crop state.
 * Shares the configuration between the Editor, Preview, and Export screens.
 */
object CropStateCache {
    @Volatile var cropState: CropState = CropState()

    fun reset() {
        cropState = CropState()
    }
}

