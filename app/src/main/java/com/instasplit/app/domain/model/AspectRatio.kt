package com.instasplit.app.domain.model

/**
 * Output format presets. outputW and outputH are the Instagram-spec pixel dimensions
 * for the exported JPEG slices.
 */
enum class AspectRatio(
    val widthRatio: Int,
    val heightRatio: Int,
    val outputW: Int,
    val outputH: Int,
    val displayName: String
) {
    PORTRAIT_4_5(4, 5, 1080, 1350, "4:5 Portrait"),
    SQUARE_1_1(1, 1, 1080, 1080, "1:1 Square")
}

