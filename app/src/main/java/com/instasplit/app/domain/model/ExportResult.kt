package com.instasplit.app.domain.model

import android.net.Uri

sealed class ExportResult {
    /** Export succeeded. [zipUri] is a FileProvider URI safe for sharing. */
    data class Success(
        val zipUri: Uri,
        val slideUris: List<Uri>
    ) : ExportResult()

    /** Export failed with a human-readable [message]. */
    data class Error(val message: String) : ExportResult()
}

