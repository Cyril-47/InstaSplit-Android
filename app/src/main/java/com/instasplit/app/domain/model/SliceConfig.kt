package com.instasplit.app.domain.model

import android.graphics.RectF

/**
 * Pre-computed source and destination rectangles for one carousel slice.
 * Passed from ComputeSlicesUseCase to the canvas renderer and exporter.
 */
data class SliceConfig(
    val index: Int,
    val srcRect: RectF,   // Region of the source bitmap to read
    val dstRect: RectF    // Destination region in the output bitmap (always full-frame)
)

