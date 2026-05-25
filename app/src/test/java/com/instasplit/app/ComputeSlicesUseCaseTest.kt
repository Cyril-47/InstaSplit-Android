package com.instasplit.app

import com.instasplit.app.domain.model.AspectRatio
import com.instasplit.app.domain.model.CropState
import com.instasplit.app.domain.usecase.ComputeSlicesUseCase
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE)
class ComputeSlicesUseCaseTest {

    private val computeSlices = ComputeSlicesUseCase()

    @Test
    fun testThreeSlicesHaveEqualWidths() {
        val state = CropState(
            scale = 1f,
            offsetX = 0f,
            offsetY = 0f,
            slideCount = 3,
            aspectRatio = AspectRatio.SQUARE_1_1
        )
        val configs = computeSlices(300, 100, state)
        assertEquals(3, configs.size)

        val w1 = configs[0].srcRect.width()
        val w2 = configs[1].srcRect.width()
        val w3 = configs[2].srcRect.width()

        assertEquals(100f, w1, 0.1f)
        assertEquals(100f, w2, 0.1f)
        assertEquals(100f, w3, 0.1f)
    }

    @Test
    fun testPortraitAspectRatioReducesCropHeight() {
        val state = CropState(
            scale = 1f,
            offsetX = 0f,
            offsetY = 0f,
            slideCount = 1,
            aspectRatio = AspectRatio.PORTRAIT_4_5
        )
        // 1000x1000 image. Portrait aspect ratio (4:5) means crop size should be 800x1000 if width is full,
        // or 1000x1250 if height is full. Since image is 1000x1000, height of crop is restricted to 1000,
        // so width is 800. Let's verify.
        val configs = computeSlices(1000, 1000, state)
        val cropRect = configs[0].srcRect
        assertEquals(800f, cropRect.width(), 0.1f)
        assertEquals(1000f, cropRect.height(), 0.1f)
    }
}

