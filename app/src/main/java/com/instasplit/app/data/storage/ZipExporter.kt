package com.instasplit.app.data.storage

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Provides file utilities for packaging slice JPEG files into a ZIP archive,
 * including obtaining secure FileProvider Uris for file sharing.
 */
@Singleton
class ZipExporter @Inject constructor(
    @ApplicationContext private val context: Context
) {
    fun getExportFile(): File {
        val exportDir = File(context.cacheDir, "exports").apply { mkdirs() }
        return File(exportDir, "instasplit_carousel.zip")
    }

    fun getUriForFile(file: File): Uri {
        return FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
    }
}

