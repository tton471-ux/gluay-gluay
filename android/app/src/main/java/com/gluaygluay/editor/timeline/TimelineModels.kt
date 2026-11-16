package com.gluaygluay.editor.timeline

import java.util.UUID

enum class TrackType { VIDEO, AUDIO, TEXT, CAPTION, OVERLAY }
enum class AudioRole { ORIGINAL, MUSIC, SOUND_EFFECTS, VOICEOVER, EXTRACTED }

data class TimelineProject(
    val id: String = UUID.randomUUID().toString(),
    val name: String = "Untitled Project",
    val width: Int = 1080,
    val height: Int = 1920,
    val frameRate: Int = 30,
    val tracks: List<TimelineTrack> = emptyList()
)

data class TimelineTrack(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val type: TrackType,
    val order: Int,
    val visible: Boolean = true,
    val locked: Boolean = false,
    val muted: Boolean = false,
    val clips: List<TimelineClip> = emptyList()
)

data class TimelineClip(
    val id: String = UUID.randomUUID().toString(),
    val assetUri: String,
    val timelineStartMs: Long,
    val sourceInMs: Long,
    val sourceOutMs: Long,
    val speed: Float = 1f,
    val volume: Float = 1f,
    val muted: Boolean = false
) {
    init {
        require(timelineStartMs >= 0) { "timelineStartMs must be >= 0" }
        require(sourceInMs >= 0) { "sourceInMs must be >= 0" }
        require(sourceOutMs > sourceInMs) { "sourceOutMs must be greater than sourceInMs" }
        require(speed > 0f) { "speed must be > 0" }
        require(volume >= 0f) { "volume must be >= 0" }
    }

    val sourceDurationMs: Long get() = sourceOutMs - sourceInMs
    val durationMs: Long get() = (sourceDurationMs / speed).toLong().coerceAtLeast(1L)
    val timelineEndMs: Long get() = timelineStartMs + durationMs
}

/** Pure, non-destructive timeline operations. They update metadata only; source files are never modified. */
object TimelineOperations {
    fun addClip(track: TimelineTrack, clip: TimelineClip): TimelineTrack {
        require(!track.locked) { "Track is locked" }
        return track.copy(clips = (track.clips + clip).sortedBy { it.timelineStartMs })
    }

    fun moveClip(track: TimelineTrack, clipId: String, newStartMs: Long): TimelineTrack {
        require(!track.locked) { "Track is locked" }
        require(newStartMs >= 0) { "newStartMs must be >= 0" }
        val found = track.clips.any { it.id == clipId }
        require(found) { "Clip not found" }
        return track.copy(clips = track.clips.map {
            if (it.id == clipId) it.copy(timelineStartMs = newStartMs) else it
        }.sortedBy { it.timelineStartMs })
    }

    fun trimClip(track: TimelineTrack, clipId: String, newSourceInMs: Long, newSourceOutMs: Long): TimelineTrack {
        require(!track.locked) { "Track is locked" }
        require(newSourceInMs >= 0 && newSourceOutMs > newSourceInMs) { "Invalid trim range" }
        require(track.clips.any { it.id == clipId }) { "Clip not found" }
        return track.copy(clips = track.clips.map {
            if (it.id == clipId) it.copy(sourceInMs = newSourceInMs, sourceOutMs = newSourceOutMs) else it
        })
    }

    fun deleteClip(track: TimelineTrack, clipId: String): TimelineTrack {
        require(!track.locked) { "Track is locked" }
        require(track.clips.any { it.id == clipId }) { "Clip not found" }
        return track.copy(clips = track.clips.filterNot { it.id == clipId })
    }

    /** Split at timeline time. Both resulting clips refer to the same untouched source asset. */
    fun splitClip(track: TimelineTrack, clipId: String, atTimelineMs: Long): TimelineTrack {
        require(!track.locked) { "Track is locked" }
        val clip = track.clips.firstOrNull { it.id == clipId } ?: error("Clip not found")
        require(atTimelineMs > clip.timelineStartMs && atTimelineMs < clip.timelineEndMs) {
            "Split point must be inside the clip"
        }
        val leftTimelineDuration = atTimelineMs - clip.timelineStartMs
        val leftSourceDuration = (leftTimelineDuration * clip.speed).toLong()
        val splitSourceMs = (clip.sourceInMs + leftSourceDuration).coerceIn(clip.sourceInMs + 1, clip.sourceOutMs - 1)
        val left = clip.copy(sourceOutMs = splitSourceMs)
        val right = clip.copy(
            id = UUID.randomUUID().toString(),
            sourceInMs = splitSourceMs,
            timelineStartMs = atTimelineMs
        )
        return track.copy(clips = (track.clips.filterNot { it.id == clipId } + left + right).sortedBy { it.timelineStartMs })
    }
}
