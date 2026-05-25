package com.instasplit.app.presentation.editor

import android.graphics.Bitmap
import android.graphics.RectF
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import com.instasplit.app.domain.model.CropState

/**
 * Full-width interactive canvas that renders the source bitmap transformed by [cropState].
 * Vertical lines show slice boundaries. Supports pinch-to-zoom and two-finger pan.
 */
@Composable
fun CropCanvas(
    bitmap: Bitmap,
    cropState: CropState,
    onScaleChange: (Float) -> Unit,
    onOffsetChange: (Float, Float) -> Unit,
    onScrollLockChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val ar = cropState.aspectRatio
    val canvasAspect = (ar.widthRatio * cropState.slideCount).toFloat() / ar.heightRatio

    // Calculate crop window dimensions in source pixels
    val imageAspect = bitmap.width.toFloat() / bitmap.height.toFloat()
    val viewportAspect = canvasAspect

    val cropWBase: Float
    val cropHBase: Float
    if (imageAspect > viewportAspect) {
        cropHBase = bitmap.height.toFloat()
        cropWBase = cropHBase * viewportAspect
    } else {
        cropWBase = bitmap.width.toFloat()
        cropHBase = cropWBase / viewportAspect
    }

    val cropW = cropWBase / cropState.scale
    val cropH = cropHBase / cropState.scale

    // Keep track of canvas screen width and height for precise dragging calibration
    var canvasW by remember { mutableStateOf(1f) }
    var canvasH by remember { mutableStateOf(1f) }

    val transformableState = rememberTransformableState { zoomChange, panChange, _ ->
        onScaleChange(cropState.scale * zoomChange)

        // Convert screen pan change delta into source bitmap coordinate space delta
        // Dragging finger right (panChange.x > 0) moves crop window left (srcDx < 0)
        val sensitivity = 1.2f
        val srcDx = -panChange.x * (cropW / canvasW) * sensitivity
        val srcDy = -panChange.y * (cropH / canvasH) * sensitivity
        onOffsetChange(srcDx, srcDy)
    }

    val dividerPaint = android.graphics.Paint().apply {
        color = android.graphics.Color.argb(180, 255, 255, 255)
        strokeWidth = 3f
        style = android.graphics.Paint.Style.STROKE
    }

    val shadowPaint = android.graphics.Paint().apply {
        color = android.graphics.Color.argb(100, 0, 0, 0)
        strokeWidth = 6f
        style = android.graphics.Paint.Style.STROKE
    }

    val textPaint = android.graphics.Paint().apply {
        color = android.graphics.Color.WHITE
        textSize = 38f
        isFakeBoldText = true
        setShadowLayer(4f, 2f, 2f, android.graphics.Color.BLACK)
    }

    Canvas(
        modifier = modifier
            .clipToBounds()
            .onSizeChanged { size ->
                canvasW = maxOf(1f, size.width.toFloat())
                canvasH = maxOf(1f, size.height.toFloat())
            }
            .pointerInput(Unit) {
                awaitPointerEventScope {
                    while (true) {
                        val event = awaitPointerEvent()
                        // Lock container scroll if there are active touches inside canvas bounds
                        val isAnyPressed = event.changes.any { it.pressed }
                        onScrollLockChange(!isAnyPressed)
                    }
                }
            }
            .transformable(state = transformableState)
    ) {
        drawIntoCanvas { canvas ->
            val nativeCanvas = canvas.nativeCanvas
            val currentCanvasW = size.width
            val currentCanvasH = size.height

            val finalSrcRect = RectF(
                cropState.offsetX,
                cropState.offsetY,
                cropState.offsetX + cropW,
                cropState.offsetY + cropH
            )

            val dstRect = RectF(0f, 0f, currentCanvasW, currentCanvasH)

            // Convert to integer Rect to avoid floating point compilation issues
            val srcRect = android.graphics.Rect(
                finalSrcRect.left.toInt(),
                finalSrcRect.top.toInt(),
                finalSrcRect.right.toInt(),
                finalSrcRect.bottom.toInt()
            )

            // Draw cropped source bitmap portion scaled onto the full canvas surface
            nativeCanvas.drawBitmap(
                bitmap,
                srcRect,
                dstRect,
                android.graphics.Paint(android.graphics.Paint.FILTER_BITMAP_FLAG)
            )

            // Draw vertical split guidelines and numbering
            val sliceWidth = currentCanvasW / cropState.slideCount
            for (i in 1 until cropState.slideCount) {
                val x = sliceWidth * i
                // Draw shadow for divider first
                nativeCanvas.drawLine(x, 0f, x, currentCanvasH, shadowPaint)
                // Draw white divider line
                nativeCanvas.drawLine(x, 0f, x, currentCanvasH, dividerPaint)
            }

            // Draw slide indices
            for (i in 0 until cropState.slideCount) {
                val x = sliceWidth * i + 24f
                val y = 50f
                nativeCanvas.drawText((i + 1).toString(), x, y, textPaint)
            }
        }
    }
}

