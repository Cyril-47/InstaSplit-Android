package com.instasplit.app.domain.usecase

import android.graphics.Bitmap
import android.net.Uri
import com.instasplit.app.domain.repository.ImageRepository
import javax.inject.Inject

class LoadImageUseCase @Inject constructor(
    private val imageRepository: ImageRepository
) {
    suspend operator fun invoke(uri: Uri): Result<Bitmap> =
        imageRepository.loadBitmap(uri)
}

