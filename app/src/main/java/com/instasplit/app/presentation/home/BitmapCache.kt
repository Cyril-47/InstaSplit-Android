package com.instasplit.app.presentation.home

import android.graphics.Bitmap

/**
 * In-memory singleton cache for the loaded source bitmap.
 * Avoids passing a large Bitmap through the navigation back-stack.
 * The bitmap is replaced each time the user loads a new image.
 */
object BitmapCache {
    @Volatile private var bitmap: Bitmap? = null

    fun set(bmp: Bitmap) {
        val old = bitmap
        if (old !== bmp) {
            old?.recycle()
        }
        bitmap = bmp
    }
    fun get(): Bitmap? = bitmap
    fun clear() { bitmap?.recycle(); bitmap = null }
}

