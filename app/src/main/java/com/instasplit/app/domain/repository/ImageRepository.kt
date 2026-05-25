package com.instasplit.app.domain.repository

import android.graphics.Bitmap
import android.net.Uri

interface ImageRepository {
    /**
     * Decode [uri] into a Bitmap. Uses BitmapRegionDecoder and inSampleSize to avoid
     * OOM on large panoramas. The returned bitmap is the full image downsampled to a
     * maximum edge length of [maxEdgePx] for editor preview purposes.
     */
    suspend fun loadBitmap(uri: Uri, maxEdgePx: Int = 4096): Result<Bitmap>

    /**
     * Return the natural pixel dimensions of [uri] without fully decoding it.
     * Used by autoFit() to calculate the correct initial scale.
     */
    suspend fun readDimensions(uri: Uri): Result<Pair<Int, Int>>
}

