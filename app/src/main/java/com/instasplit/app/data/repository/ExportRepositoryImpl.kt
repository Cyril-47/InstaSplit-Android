package com.instasplit.app.data.repository

import android.graphics.Bitmap
import android.net.Uri
import com.instasplit.app.data.storage.MediaStoreHelper
import com.instasplit.app.data.storage.ZipExporter
import com.instasplit.app.domain.model.ExportResult
import com.instasplit.app.domain.model.SliceConfig
import com.instasplit.app.domain.repository.ExportRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.io.File
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ExportRepositoryImpl @Inject constructor(
    private val mediaStoreHelper: MediaStoreHelper,
    private val zipExporter: ZipExporter
) : ExportRepository {

    override fun export(
        sourceBitmap: Bitmap,
        configs: List<SliceConfig>,
        quality: Int
    ): Flow<ExportRepository.ExportEvent> =
        flow {
            val savedUris = mutableListOf<Uri>()
            val zipFile = zipExporter.getExportFile()

            // Phase 4: Disk Space Check (Minimum 50MB free)
            val requiredSpaceBytes = 50 * 1024 * 1024L
            val usableSpace = zipFile.parentFile?.usableSpace ?: 0L
            if (usableSpace in 1 until requiredSpaceBytes) {
                emit(
                    ExportRepository.ExportEvent.Done(
                        ExportResult.Error("Insufficient disk space. Please free up at least 50MB.")
                    )
                )
                return@flow
            }

            try {
                ZipOutputStream(zipFile.outputStream().buffered()).use { zipStream ->
                    configs.forEachIndexed { index, config ->
                        val srcRect = android.graphics.Rect(
                            config.srcRect.left.toInt(),
                            config.srcRect.top.toInt(),
                            config.srcRect.right.toInt(),
                            config.srcRect.bottom.toInt()
                        )
                        val dstWidth = config.dstRect.width().toInt()
                        val dstHeight = config.dstRect.height().toInt()

                        // Create slice bitmap inside a try-catch to prevent OOM crash on low-end hardware
                        var sliceBitmap: Bitmap? = null
                        try {
                            sliceBitmap = Bitmap.createBitmap(dstWidth, dstHeight, Bitmap.Config.ARGB_8888)
                            val canvas = android.graphics.Canvas(sliceBitmap)
                            val paint = android.graphics.Paint(android.graphics.Paint.FILTER_BITMAP_FLAG)
                            canvas.drawBitmap(sourceBitmap, srcRect, config.dstRect, paint)
                        } catch (oom: OutOfMemoryError) {
                            sliceBitmap?.recycle()
                            emit(
                                ExportRepository.ExportEvent.Done(
                                    ExportResult.Error("Device ran out of memory while slicing. Try reducing the slide count.")
                                )
                            )
                            return@flow
                        }

                        val bitmap = sliceBitmap ?: throw IllegalStateException("Failed to generate slice bitmap")

                        // 1. Save individual JPEG slice to MediaStore Gallery (Pictures/InstaSplit/)
                        val uri = mediaStoreHelper.saveJpeg(
                            bitmap,
                            "slide_${String.format("%02d", index + 1)}.jpg",
                            quality
                        )
                        savedUris.add(uri)

                        // 2. Package JPEG into ZIP stream
                        zipStream.putNextEntry(ZipEntry("slide_${String.format("%02d", index + 1)}.jpg"))
                        bitmap.compress(Bitmap.CompressFormat.JPEG, quality, zipStream)
                        zipStream.closeEntry()

                        // 3. Create a tiny thumbnail (e.g. 200px wide) for UI feedback to save RAM
                        val thumbW = 200
                        val thumbH = (200 * (dstHeight.toFloat() / dstWidth)).toInt()
                        val thumbnail = Bitmap.createScaledBitmap(bitmap, thumbW, thumbH, true)

                        // 4. Recycle slice memory immediately! Only keep the tiny thumbnail.
                        bitmap.recycle()

                        val fraction = (index + 1).toFloat() / configs.size
                        emit(ExportRepository.ExportEvent.Progress(fraction, thumbnail))
                    }
                }

                // Retrieve safe FileProvider URI for the completed ZIP archive
                val zipUri = zipExporter.getUriForFile(zipFile)
                emit(
                    ExportRepository.ExportEvent.Done(
                        ExportResult.Success(zipUri = zipUri, slideUris = savedUris)
                    )
                )
            } catch (e: Exception) {
                emit(
                    ExportRepository.ExportEvent.Done(
                        ExportResult.Error(e.message ?: "Failed during carousel export process.")
                    )
                )
            }
        }
}

