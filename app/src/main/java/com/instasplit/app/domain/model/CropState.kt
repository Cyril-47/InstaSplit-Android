package com.instasplit.app.domain.model

/**
 * Immutable snapshot of the editor state.
 *
 * @param scale        Current zoom level (1.0 = fit, range 0.5â€“10.0).
 * @param offsetX      Horizontal pan in source-bitmap pixels.
 * @param offsetY      Vertical pan in source-bitmap pixels.
 * @param slideCount   Number of output carousel slides (2â€“10).
 * @param aspectRatio  Output aspect ratio preset.
 * @param jpegQuality  JPEG compression quality (0â€“100).
 */
data class CropState(
    val scale: Float = 1f,
    val offsetX: Float = 0f,
    val offsetY: Float = 0f,
    val slideCount: Int = 3,
    val aspectRatio: AspectRatio = AspectRatio.PORTRAIT_4_5,
    val jpegQuality: Int = 90
) {
    /** Clamp helpers called after every gesture update. */
    fun withClampedScale(): CropState =
        copy(scale = scale.coerceIn(0.5f, 10f))

    /** Reset pan and zoom to defaults while keeping all other settings. */
    fun reset(): CropState = copy(scale = 1f, offsetX = 0f, offsetY = 0f)

    /**
     * Scale so the image fills the full canvas width for [slideCount] slides,
     * then reset pan to origin.
     */
    fun autoFit(imageWidth: Int, imageHeight: Int): CropState {
        val ar = aspectRatio
        val viewportAspect = (ar.widthRatio * slideCount).toFloat() / ar.heightRatio
        val imageAspect = imageWidth.toFloat() / imageHeight.toFloat()

        val cropWBase: Float
        val cropHBase: Float
        if (imageAspect > viewportAspect) {
            cropHBase = imageHeight.toFloat()
            cropWBase = cropHBase * viewportAspect
        } else {
            cropWBase = imageWidth.toFloat()
            cropHBase = cropWBase / viewportAspect
        }

        val initialOffsetX = (imageWidth - cropWBase) / 2f
        val initialOffsetY = (imageHeight - cropHBase) / 2f

        return copy(
            scale = 1f,
            offsetX = maxOf(0f, initialOffsetX),
            offsetY = maxOf(0f, initialOffsetY)
        )
    }
}

