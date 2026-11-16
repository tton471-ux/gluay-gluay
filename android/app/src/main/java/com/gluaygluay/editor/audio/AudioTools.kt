package com.gluaygluay.editor.audio

import android.content.Context
import android.media.MediaExtractor
import android.media.MediaFormat
import android.media.MediaMuxer
import android.media.MediaRecorder
import android.net.Uri
import java.io.File
import java.nio.ByteBuffer

data class AudioAsset(
    val id: String,
    val uriOrPath: String,
    val displayName: String,
    val sourceType: String,
    val durationMs: Long = 0L
)

/**
 * Imports the encoded audio stream from a selected video into an M4A/MP4 container.
 * This is a stream copy (no re-encoding) and therefore depends on the source audio
 * codec being supported by the MP4 container. The source video is never modified.
 */
fun extractAudioFromVideo(context: Context, videoUri: Uri, outputFile: File): File {
    val extractor = MediaExtractor()
    var muxer: MediaMuxer? = null
    try {
        extractor.setDataSource(context, videoUri, null)
        var audioTrack = -1
        var audioFormat: MediaFormat? = null
        for (index in 0 until extractor.trackCount) {
            val format = extractor.getTrackFormat(index)
            val mime = format.getString(MediaFormat.KEY_MIME) ?: continue
            if (mime.startsWith("audio/")) {
                audioTrack = index
                audioFormat = format
                break
            }
        }
        require(audioTrack >= 0 && audioFormat != null) {
            "วิดีโอนี้ไม่มีแทร็กเสียงให้แยก"
        }

        outputFile.parentFile?.mkdirs()
        muxer = MediaMuxer(outputFile.absolutePath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4)
        val outputTrack = muxer.addTrack(audioFormat)
        extractor.selectTrack(audioTrack)
        muxer.start()

        val maxInputSize = if (audioFormat.containsKey(MediaFormat.KEY_MAX_INPUT_SIZE)) {
            audioFormat.getInteger(MediaFormat.KEY_MAX_INPUT_SIZE).coerceAtLeast(256 * 1024)
        } else {
            1 * 1024 * 1024
        }
        val buffer = ByteBuffer.allocateDirect(maxInputSize)
        val info = MediaMuxer.BufferInfo()

        while (true) {
            buffer.clear()
            val size = extractor.readSampleData(buffer, 0)
            if (size < 0) break
            info.offset = 0
            info.size = size
            info.presentationTimeUs = extractor.sampleTime
            info.flags = extractor.sampleFlags
            muxer.writeSampleData(outputTrack, buffer, info)
            if (!extractor.advance()) break
        }
        muxer.stop()
        return outputFile
    } catch (error: Exception) {
        outputFile.delete()
        throw error
    } finally {
        try { muxer?.release() } catch (_: Exception) {}
        extractor.release()
    }
}

/** Records a microphone voice-over to a local AAC/M4A file. Call only after permission is granted. */
class VoiceOverRecorder(private val outputFile: File) {
    private var recorder: MediaRecorder? = null

    fun start() {
        outputFile.parentFile?.mkdirs()
        val instance = MediaRecorder()
        instance.setAudioSource(MediaRecorder.AudioSource.MIC)
        instance.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
        instance.setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
        instance.setAudioEncodingBitRate(128_000)
        instance.setAudioSamplingRate(44_100)
        instance.setOutputFile(outputFile.absolutePath)
        instance.prepare()
        instance.start()
        recorder = instance
    }

    fun stop(): File {
        val active = recorder ?: error("ยังไม่ได้เริ่มอัดเสียง")
        try {
            active.stop()
        } finally {
            active.reset()
            active.release()
            recorder = null
        }
        return outputFile
    }

    fun cancel() {
        recorder?.let {
            try { it.stop() } catch (_: Exception) {}
            it.release()
        }
        recorder = null
        outputFile.delete()
    }
}
