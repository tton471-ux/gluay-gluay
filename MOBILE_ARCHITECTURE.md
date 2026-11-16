# GLUAY GLUAY — Mobile-first Architecture

## Product direction
Android-first native editor. Windows remains a development/test environment; Android is the first shipping target. Do not treat the current static frontend or FastAPI scaffold as the finished mobile app.

## Architecture decisions
- **Android app:** Kotlin + Jetpack Compose for the native interface.
- **Media import:** Android Storage Access Framework (system file picker); use content URIs and persist URI permission when available. Never require broad storage permission for user-selected files.
- **Preview:** AndroidX Media3 ExoPlayer for playback and accurate seeking.
- **Editing model:** non-destructive project timeline. Keep original source media unchanged; store clip in/out points, track, order, transform, audio gain, effects and transitions as project metadata.
- **Audio:** separate music, original-video audio, extracted audio and voice-over tracks. Import external audio with the system picker, extract encoded audio with MediaExtractor/MediaMuxer where compatible, and record microphone narration with MediaRecorder after runtime permission. Store durable assets in app-private project storage and metadata in Room.
- **Rendering:** AndroidX Media3 Transformer / Android MediaCodec for on-device export. Use FFmpeg only for features that Media3 cannot support, after validating Android licensing, ABI packaging, device compatibility, performance and thermal behavior.
- **AI subtitles:** on-device Whisper is a later optional module; model download is explicit and cached. Cloud transcription must be opt-in, never silently upload private videos.
- **Persistence:** Room database for project metadata; app-private storage for generated proxy/cache/export files; original user files stay at their chosen locations.
- **Shared contracts:** JSON project schema and effect IDs are platform-neutral. Native Android and any future desktop UI implement the same project semantics.
- **Backend:** FastAPI is optional for development/admin workflows and must not be required for basic offline editing.

## Milestones
1. Android shell: open app, choose a video using system picker, preview/play/pause/seek.
2. Timeline: display duration and waveform/thumbnail strip; split at playhead; delete selected segment; undo/redo.
3. Audio studio: import music, extract source-video audio, record voice-over; then add multitrack placement, trim, volume, mute and fades.
4. Canvas: fit/fill preview and 9:16, 16:9, 1:1 project aspect ratios.
5. Export: render MP4 locally with synchronized video and mixed audio, progress, cancel, output/share.
6. Effects/transitions: safe preset catalogue, intensity, clip-level ranges.
7. Subtitles: edit captions, timing, style, burn-in/export; on-device ASR after device benchmarks.
8. Performance: proxy workflow, background rendering, memory/thermal handling, device compatibility.

## Data safety and engineering rules
- Editing is non-destructive; never overwrite source files.
- Save project state after each edit and recover after app restart.
- Validate actual output on physical Android devices before marking features complete.
- Do not claim HDR/10-bit preservation until verified end-to-end on supported hardware.
- Do not make the desktop Python backend a runtime dependency for Android.

## Current limitations
This repository previously contained a minimal API connectivity page and Python API scaffold. The Android project added in this milestone is an initial app shell, not a complete editor. It still needs a physical-device build/test cycle.
