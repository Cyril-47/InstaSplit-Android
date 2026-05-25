package com.instasplit.app.domain.repository

import android.graphics.Bitmap
import android.net.Uri
import com.instasplit.app.domain.model.ExportResult
import kotlinx.coroutines.flow.Flow
interface ExportRepository {
    fun export(
        sourceBitmap: Bitmap,
        configs: List<com.instasplit.app.domain.model.SliceConfig>,
        quality: Int
    ): Flow<ExportEvent>

    sealed class ExportEvent {
        data class Progress(val fraction: Float, val completedSlice: Bitmap?) : ExportEvent()
        data class Done(val result: ExportResult) : ExportEvent()
    }
}

