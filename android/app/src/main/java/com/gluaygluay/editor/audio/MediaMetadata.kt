package com.gluaygluay.editor.audio

import android.content.Context
import android.media.MediaMetadataRetriever
import android.net.Uri
import java.io.File

/** Reads duration metadata without decoding or modifying the source media. */
fun readMediaDurationMs(context: Context, uriOrPath: String): Long {
    val retriever = MediaMetadataRetriever()
    return try {
        val parsed = Uri.parse(uriOrPath)
        if (parsed.scheme == "content" || parsed.scheme == "android.resource") {
            retriever.setDataSource(context, parsed)
        } else if (parsed.scheme == "file") {
            retriever.setDataSource(parsed.path)
        } else {
            retriever.setDataSource(File(uriOrPath).absolutePath)
        }
        retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
            ?.toLongOrNull()?.coerceAtLeast(0L) ?: 0L
    } catch (_: Exception) {
        0L
    } finally {
        try { retriever.release() } catch (_: Exception) {}
    }
}
