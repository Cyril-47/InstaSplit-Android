package com.instasplit.app.domain.usecase

import android.graphics.RectF
import com.instasplit.app.domain.model.CropState
import com.instasplit.app.domain.model.SliceConfig
import javax.inject.Inject

/**
 * Pure math use-case â€” no Android context needed, fully unit-testable.
 *
 * Computes the source RectF for each slide such that adjacent slices share
 * exactly the same column of pixels (seamless boundary).
 *
 * Coordinate space: all values are in *source bitmap pixels*.
 */
class ComputeSlicesUseCase @Inject constructor() {

    operator fun invoke(
        bitmapWidth: Int,
        bitmapHeight: Int,
        cropState: CropState
    ): List<SliceConfig> {
        val ar = cropState.aspectRatio
        val canvasAspect = (ar.widthRatio * cropState.slideCount).toFloat() / ar.heightRatio
        val imageAspect = bitmapWidth.toFloat() / bitmapHeight.toFloat()
        val viewportAspect = canvasAspect

        val cropWBase: Float
        val cropHBase: Float
        if (imageAspect > viewportAspect) {
            cropHBase = bitmapHeight.toFloat()
            cropWBase = cropHBase * viewportAspect
        } else {
            cropWBase = bitmapWidth.toFloat()
            cropHBase = cropWBase / viewportAspect
        }

        val cropW = maxOf(1f, cropWBase / cropState.scale)
        val cropH = maxOf(1f, cropHBase / cropState.scale)

        val sliceWidth = cropW / cropState.slideCount

        val maxOffsetX = bitmapWidth.toFloat() - cropW
        val maxOffsetY = bitmapHeight.toFloat() - cropH
        val clampedOffsetX = cropState.offsetX.coerceIn(0f, maxOf(0f, maxOffsetX))
        val clampedOffsetY = cropState.offsetY.coerceIn(0f, maxOf(0f, maxOffsetY))

        return (0 until cropState.slideCount).map { index ->
            val srcLeft = clampedOffsetX + index * sliceWidth
            val srcRight = srcLeft + sliceWidth

            SliceConfig(
                index = index,
                srcRect = RectF(
                    srcLeft,
                    clampedOffsetY,
                    srcRight,
                    clampedOffsetY + cropH
                ),
                dstRect = RectF(
                    0f, 0f,
                    ar.outputW.toFloat(),
                    ar.outputH.toFloat()
                )
            )
        }
    }
}

