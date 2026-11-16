package com.gluaygluay.editor.timeline

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TimelineOperationsTest {
    private fun sampleTrack(locked: Boolean = false) = TimelineTrack(
        id = "video-1",
        name = "Video 1",
        type = TrackType.VIDEO,
        order = 0,
        locked = locked,
        clips = listOf(
            TimelineClip(
                id = "clip-1",
                assetUri = "content://video",
                timelineStartMs = 0,
                sourceInMs = 0,
                sourceOutMs = 10_000
            )
        )
    )

    @Test
    fun splitCreatesTwoAdjacentNonDestructiveClips() {
        val result = TimelineOperations.splitClip(sampleTrack(), "clip-1", 4_000)
        assertEquals(2, result.clips.size)
        val left = result.clips.minBy { it.timelineStartMs }
        val right = result.clips.maxBy { it.timelineStartMs }
        assertEquals(4_000L, left.sourceOutMs)
        assertEquals(4_000L, right.sourceInMs)
        assertEquals(4_000L, right.timelineStartMs)
        assertEquals("content://video", left.assetUri)
        assertEquals(left.assetUri, right.assetUri)
    }

    @Test
    fun movingClipChangesOnlyTimelineStart() {
        val result = TimelineOperations.moveClip(sampleTrack(), "clip-1", 2_500)
        assertEquals(2_500L, result.clips.single().timelineStartMs)
        assertEquals(0L, result.clips.single().sourceInMs)
        assertEquals(10_000L, result.clips.single().sourceOutMs)
    }

    @Test
    fun deletingClipRemovesIt() {
        val result = TimelineOperations.deleteClip(sampleTrack(), "clip-1")
        assertTrue(result.clips.isEmpty())
    }

    @Test
    fun lockedTrackRejectsEdits() {
        val result = runCatching {
            TimelineOperations.deleteClip(sampleTrack(locked = true), "clip-1")
        }
        assertTrue(result.isFailure)
    }

    @Test
    fun splitRejectsVeryShortClip() {
        val track = sampleTrack().copy(
            clips = listOf(
                TimelineClip(
                    id = "tiny",
                    assetUri = "content://tiny",
                    timelineStartMs = 0,
                    sourceInMs = 0,
                    sourceOutMs = 1
                )
            )
        )
        val result = runCatching { TimelineOperations.splitClip(track, "tiny", 0) }
        assertTrue(result.isFailure)
    }
}
