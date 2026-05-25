package com.instasplit.app.domain.usecase

import android.graphics.Bitmap
import com.instasplit.app.domain.model.CropState
import com.instasplit.app.domain.model.ExportResult
import com.instasplit.app.domain.repository.ExportRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ExportZipUseCase @Inject constructor(
    private val exportRepository: ExportRepository,
    private val computeSlices: ComputeSlicesUseCase
) {
    fun invoke(
        sourceBitmap: Bitmap,
        cropState: CropState
    ): Flow<ExportRepository.ExportEvent> {
        val configs = computeSlices(
            bitmapWidth = sourceBitmap.width,
            bitmapHeight = sourceBitmap.height,
            cropState = cropState
        )

        return exportRepository.export(sourceBitmap, configs, cropState.jpegQuality)
    }
}

