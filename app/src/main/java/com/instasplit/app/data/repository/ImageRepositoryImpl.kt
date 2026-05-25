package com.instasplit.app.data.repository

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import android.media.ExifInterface
import com.instasplit.app.domain.repository.ImageRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ImageRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : ImageRepository {

    override suspend fun loadBitmap(uri: Uri, maxEdgePx: Int): Result<Bitmap> =
        withContext(Dispatchers.IO) {
            runCatching {
                // Step 1: Read natural dimensions without decoding pixels
                val (w, h) = readDimensions(uri).getOrThrow()

                // Step 2: Compute inSampleSize so decoded bitmap fits within maxEdgePx
                val sampleSize = computeSampleSize(w, h, maxEdgePx)

                // Step 3: Decode with subsampling
                val opts = BitmapFactory.Options().apply {
                    inSampleSize = sampleSize
                    inPreferredConfig = Bitmap.Config.ARGB_8888
                }
                val raw = context.contentResolver.openInputStream(uri)!!.use { stream ->
                    BitmapFactory.decodeStream(stream, null, opts)
                        ?: error("Failed to decode bitmap from $uri")
                }

                // Step 4: Apply EXIF rotation so the image is always upright
                applyExifRotation(uri, raw)
            }
        }

    override suspend fun readDimensions(uri: Uri): Result<Pair<Int, Int>> =
        withContext(Dispatchers.IO) {
            runCatching {
                val opts = BitmapFactory.Options().apply { inJustDecodeBounds = true }
                context.contentResolver.openInputStream(uri)!!.use { stream ->
                    BitmapFactory.decodeStream(stream, null, opts)
                }
                Pair(opts.outWidth, opts.outHeight)
            }
        }

    // --- Helpers ---

    private fun computeSampleSize(w: Int, h: Int, maxEdge: Int): Int {
        var sample = 1
        while (maxOf(w / sample, h / sample) > maxEdge) sample *= 2
        return sample
    }

    private fun applyExifRotation(uri: Uri, bitmap: Bitmap): Bitmap {
        val degrees = runCatching {
            context.contentResolver.openInputStream(uri)!!.use { stream ->
                val exif = ExifInterface(stream)
                when (exif.getAttributeInt(
                    ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_NORMAL
                )) {
                    ExifInterface.ORIENTATION_ROTATE_90  -> 90f
                    ExifInterface.ORIENTATION_ROTATE_180 -> 180f
                    ExifInterface.ORIENTATION_ROTATE_270 -> 270f
                    else                                 -> 0f
                }
            }
        }.getOrDefault(0f)

        return if (degrees == 0f) bitmap else {
            val matrix = Matrix().apply { postRotate(degrees) }
            Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
                .also { if (it !== bitmap) bitmap.recycle() }
        }
    }
}

