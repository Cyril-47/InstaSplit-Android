package com.instasplit.app

import com.instasplit.app.domain.model.AspectRatio
import com.instasplit.app.domain.model.CropState
import org.junit.Assert.assertEquals
import org.junit.Test

class CropStateTest {

    @Test
    fun testClampedScale() {
        val stateUnder = CropState(scale = 0.2f)
        val stateOver = CropState(scale = 12f)

        assertEquals(0.5f, stateUnder.withClampedScale().scale, 0.001f)
        assertEquals(10f, stateOver.withClampedScale().scale, 0.001f)
    }

    @Test
    fun testReset() {
        val state = CropState(scale = 2.5f, offsetX = 100f, offsetY = -50f)
        val resetState = state.reset()

        assertEquals(1f, resetState.scale, 0.001f)
        assertEquals(0f, resetState.offsetX, 0.001f)
        assertEquals(0f, resetState.offsetY, 0.001f)
    }

    @Test
    fun testAutoFitWideImage() {
        // slideCount = 3, AspectRatio.PORTRAIT_4_5 (4:5)
        // viewportAspect = (4 * 3) / 5 = 12 / 5 = 2.4
        // imageWidth = 2400, imageHeight = 800 -> imageAspect = 3.0 (wide image)
        val state = CropState(slideCount = 3, aspectRatio = AspectRatio.PORTRAIT_4_5)
        val fitted = state.autoFit(2400, 800)

        // cropHBase = 800, cropWBase = 800 * 2.4 = 1920
        // initialOffsetX = (2400 - 1920) / 2 = 240
        // initialOffsetY = 0
        assertEquals(1f, fitted.scale, 0.001f)
        assertEquals(240f, fitted.offsetX, 0.001f)
        assertEquals(0f, fitted.offsetY, 0.001f)
    }

    @Test
    fun testAutoFitTallImage() {
        // slideCount = 3, AspectRatio.PORTRAIT_4_5 (4:5)
        // viewportAspect = 2.4
        // imageWidth = 1200, imageHeight = 1000 -> imageAspect = 1.2 (tall image)
        val state = CropState(slideCount = 3, aspectRatio = AspectRatio.PORTRAIT_4_5)
        val fitted = state.autoFit(1200, 1000)

        // cropWBase = 1200, cropHBase = 1200 / 2.4 = 500
        // initialOffsetX = 0
        // initialOffsetY = (1000 - 500) / 2 = 250
        assertEquals(1f, fitted.scale, 0.001f)
        assertEquals(0f, fitted.offsetX, 0.001f)
        assertEquals(250f, fitted.offsetY, 0.001f)
    }
}

