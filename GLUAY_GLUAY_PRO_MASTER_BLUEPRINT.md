# GLUAY GLUAY PRO — Master Technical Blueprint

> Status: Planning baseline / feature specification. This document records the agreed product scope; it does not claim that the listed features are already implemented.
> Project: GLUAY GLUAY Android-first native video editor.
> Repository: https://github.com/tton471-ux/gluay-gluay
> Branch: main

## 1. Product vision

Build a professional, native Android video-editing studio with an offline-first, local-processing architecture. Users can create, edit, mix audio, add titles/subtitles/effects, and export videos on-device. Cloud services are optional and must require explicit user consent. Never overwrite original source media.

The Android application is the primary product. The existing Python API and web frontend are development/support tools, not the Android editor UI. The separate Windows v0.3.0 working folder and TON VIDEO PRO repository are out of scope and must not be modified as part of this project unless explicitly requested.

## 2. Current repository baseline (verified before this document)

- Native Android shell exists under `android/` using Kotlin, Jetpack Compose and AndroidX Media3.
- The current starter milestone supports choosing a video and local preview. Audio import, audio extraction and voice-over prototype controls are described in `AUDIO_STUDIO_MILESTONE_1.md`.
- Audio prototype limitations: no complete multitrack playback/mixing, waveform, timeline placement, trimming, volume automation, fades, ducking, or final video export mix yet; temporary assets are not yet a reopenable project.
- Shared project model exists at `shared/project-schema-v1.json`, schema version 1, with initial video, music, voice-over, extracted-audio and caption track concepts.
- Existing visual effect presets and optional FFmpeg API experiments exist under `frontend/` and `backend/`. They are not equivalent to an integrated native Android effects studio. The current FFmpeg render path outputs H.264/yuv420p SDR and must not be represented as HDR-preserving.
- Do not claim a feature is implemented or device-verified until code exists, tests pass, and supported physical-device behavior is confirmed.

## 3. Product areas

### 3.1 Home Studio and Project Manager
- Create, rename, duplicate, open, archive and delete projects (with confirmation).
- Recent projects, thumbnails, project recovery and autosave.
- Templates and user presets.
- Project settings, storage usage and device capability report.
- Non-destructive editing; preserve original source media.

### 3.2 Video Editor and Timeline Engine
- Multi-track video timeline with overlays, images, text, captions and audio tracks.
- Import media through Android system pickers; persist access safely and copy assets into project storage when required.
- Preview with play/pause, seek, frame stepping and fit-to-canvas behavior.
- Split, trim, ripple delete, move, duplicate, reorder, group, multi-select, snap/magnet, track lock/hide/mute and timeline zoom.
- Undo/redo and autosave.
- Clip speed, rotation, crop, position, scale, opacity and volume.
- Non-destructive clip operations and source in/out points.

### 3.3 Canvas, Aspect Ratio and Resolution
Provide presets: 9:16, 16:9, 1:1, 4:5, 3:4, 4:3, 3:2, 2.35:1 and Custom.
- Resolution presets: 360p, 480p, 720p, 1080p, 1440p, 2160p/4K and 4320p/8K as an availability-gated target, plus Custom.
- Frame-rate choices: 23.976, 24, 25, 29.97, 30, 50, 59.94, 60, 90 and 120 fps, gated by source and device encoder support.
- Custom canvas width/height, background, fit/contain, fill/crop and stretch modes.
- Correct dimensions must be calculated for the selected aspect ratio and resolution tier. Do not imply every device can decode or export every combination.

### 3.4 Audio Studio

#### Audio sources and libraries
- Sound Effects Library with three sources: bundled basic effects, optional downloadable sound packs, and user-imported audio.
- Background Music Library with three sources: bundled licensed music, optional downloadable music packs, and user-imported music.
- Pack categories include (not limited to) cinematic/epic, emotional, funny/cartoon, horror/suspense, nature/relax, technology/modern, motivational and lo-fi/chill.
- Sound effect categories include whoosh, impact, cartoon, cinematic, nature, human, technology, transition and horror.
- Search, category filters, preview playback, add-to-timeline, pack size display, download/install status, offline availability and removal of unused packs.
- Only distribute audio assets with documented rights that permit the intended app distribution and use. Do not bundle unlicensed commercial music.

#### Multitrack audio editing
- Separate tracks for original video audio, music, sound effects, voice-over and extracted audio.
- Waveforms, clip placement, split/trim, move, duplicate, mute/solo, volume, fades, loop, speed/pitch controls and audio sync.
- Mixer, audio meters, EQ, compressor, limiter, noise gate/reduction and voice enhancement where supported.
- Auto Ducking to reduce background music under speech.
- Mix preview and synchronized final video/audio render.
- Preserve source audio and never destructively alter original files.

#### AI Audio
- AI Music & Sound Recommendation: analyze scene/context and suggest appropriate licensed/local music and effects; users preview and approve suggestions.
- AI Beat Sync: estimate BPM/beat markers and recommend or apply edit points; always allow manual review and adjustment.
- AI Audio Auto Edit: propose music placement, sound-effect placement, ducking, fades and levels; never silently replace the user's edit.
- Offline-first models where feasible; optional cloud processing only after clear consent. Show model download/storage requirements and capability limits.

### 3.5 Text & Subtitle Studio
- Multi-layer text and title clips on the timeline.
- Text styling: font, size, weight, color, outline, shadow, background, gradient, alignment, line spacing, letter spacing, position, rotation and scale.
- Text presets and reusable styles.
- Text animation: entrance, exit, loop, kinetic word/letter animation and cinematic/social styles.
- Keyframe support for text transforms and opacity.
- AI speech-to-text subtitles with timestamped segments; editable words, punctuation and timing.
- Initial language targets: Thai, Lao and English. Actual offline accuracy depends on installed ASR models; disclose model and language availability.
- Subtitle styles: social caption, cinematic, karaoke/word highlight and cartoon.
- Translation between supported languages with user review.
- SRT/VTT import and export.
- Optional text-to-speech voice-over with voice selection, speed and timeline synchronization, subject to model/language availability.
- Never claim perfect transcription, translation or pronunciation.

### 3.6 Visual Effects and Transitions Studio
- Categorized effect library: basic, color grading, light, blur, distortion, retro, creative, transform, chroma key/green screen and AI-assisted effects.
- Effect controls and adjustable intensity/parameters, with preview and non-destructive application.
- Planned effects include motion blur, glow, glitch, lens flare, blur, sharpen, vignette, pixelate, color adjustments, LUT support, masks and chroma key.
- Transitions: fade/dissolve, zoom, swipe, spin, blur, flash, glitch and mask-based transitions.
- Transition duration and parameters editable on timeline.
- Existing web/API presets are prototypes only; native implementation and render parity are required.
- Effects must be validated for performance, memory use, color correctness and export output.

### 3.7 Keyframe and Motion Tracking
- Keyframe properties: position X/Y, scale X/Y, rotation, opacity, volume, effect intensity and anchor point.
- Easing/interpolation presets.
- Motion tracking targets: people, faces and objects where models/device permit.
- Attach text/stickers to tracked objects and blur tracked subjects.
- Manual correction and tracking confidence feedback.
- Tracking is an advanced milestone and must be tested on real devices.

### 3.8 Green Screen / Chroma Key
- Chroma key color picker, similarity, smoothness, spill suppression and edge feather.
- Background replacement, image/video overlays, masks and invert options.
- Preview and export consistency; performance gating by device.

### 3.9 AI Studio
- Auto Cut, scene detection, highlight detection, auto subtitles, voice cleanup, background removal, smart crop/reframe, speech translation and caption suggestions.
- AI outputs are editable proposals, not irreversible actions.
- Offline-first where practical; optional cloud mode with explicit consent and clear disclosure of data sent.
- Downloadable model manager with model size, language, version, storage and delete controls.

### 3.10 Proxy Editing and Smart Render
- Generate 540p/720p proxies for smoother editing while preserving original media for final export.
- Per-project proxy settings and storage cleanup.
- Smart Render/cache unchanged segments where the rendering pipeline supports it.
- Clearly identify operations/codecs/effects that require full re-render; never promise lossless or instant rendering where unsupported.

### 3.11 Export Studio
- MP4 and MOV targets where supported.
- H.264 and HEVC; AV1 only when supported by device/software.
- Resolution, canvas ratio, frame rate, quality/bitrate and audio settings.
- SDR and HDR choices only when the complete decode/effect/encode path supports the selected format.
- HDR10/HLG preservation requires correct 10-bit pixel format, color primaries, transfer characteristics, metadata and verified device pipeline; a UI label alone is not HDR.
- Export progress, estimated size/time when reliable, cancel and queue; pause/resume only if supported by the render implementation.
- Save to user-selected destination. Never modify source media.

### 3.12 System Center and Device Capability
- Detect runtime RAM/storage, supported codecs, hardware decoders/encoders, 10-bit/HDR capabilities, frame rates, resolution limits and thermal/performance state where Android APIs expose them.
- Modes: Performance, Balanced and Quality.
- Cache/proxy management, project backup/recovery and diagnostics.
- Target test profile includes the user's Vivo V2405A / OriginOS 7 / Android 17, but all capabilities must be detected and verified at runtime. Do not assume 4K/8K, 120fps, HEVC 10-bit or HDR support without tests.
- Account for vendor background-process restrictions and lifecycle behavior.

## 4. Architecture principles

- Native Android: Kotlin, Jetpack Compose, AndroidX Media3 for playback/transform where appropriate, Android MediaCodec for hardware codec paths, OpenGL ES or Vulkan for GPU effects where justified, Room for project metadata, WorkManager for durable background jobs.
- Separate engines/modules: Project Engine, Media Engine, Timeline Engine, Audio Engine, Effects Engine, AI Engine, Render Engine and Android Hardware/Capability Layer.
- FFmpeg may be used for suitable tasks after license/compliance review and must not be the sole assumption for all on-device workflows.
- Offline-first: core editing must not require a server. Network-dependent packs/models are optional downloads.
- Keep project schema platform-neutral and versioned; migrations must preserve existing projects.
- Keep UI responsive; heavy processing must run off the main thread with cancellation/progress/error reporting.
- Use scoped storage and Android document picker APIs; respect user permissions.
- Privacy: no cloud upload without explicit opt-in.

## 5. Proposed project data model expansion

Evolve `shared/project-schema-v1.json` through a versioned migration rather than replacing it incompatibly. Future schema should represent:
- Project canvas, frame rate, color space and export profile.
- Track identity/type/role/order/visibility/lock/mute/volume.
- Video, image, audio, text, subtitle and overlay clips.
- Source asset identity, URI, durable local path where appropriate, source in/out, timeline start/duration, transform, speed, volume and keyframes.
- Effects and transitions with typed parameters and versioned IDs (not arbitrary executable filter strings).
- Subtitle segments, language, style and timing.
- Audio fades, gain automation, ducking and mixer settings.
- Proxy/cache references and AI-generated suggestion provenance.
- Project schema version and migration metadata.

Do not change schema v1 until migration tests and compatibility rules are ready.

## 6. Roadmap and acceptance gates

1. **Core App + Project System:** project create/open/save, durable asset management, schema migration, autosave/recovery.
2. **Timeline Engine:** multitrack model, editing operations, preview synchronization, undo/redo.
3. **Audio Studio:** durable assets, waveform, multitrack mix, SFX/music libraries, voice-over and export mix.
4. **Text & Subtitle Studio:** text layers, subtitle editor, SRT/VTT, ASR integration.
5. **Effects + Transitions:** native preview/render pipeline and effect parity.
6. **Keyframe + Green Screen + Tracking:** editing controls and performance tests.
7. **AI Studio:** Auto Cut, scene/highlight, AI audio suggestions/beat sync, translation and other supported tools.
8. **Proxy + Smart Render:** cache management and partial-render correctness.
9. **Export + HDR:** codec/color pipeline, export queue, device capability gating.
10. **Vivo and broader Android device validation:** performance, thermal, storage, lifecycle, codec and recovery tests.

A milestone is complete only when source code, automated tests where feasible, manual test instructions and known limitations are documented. A planned feature is not an implemented feature.

## 7. Decisions still awaiting explicit user confirmation

The following defaults were proposed but have not yet been confirmed:
- Default canvas ratio: 9:16.
- Default export resolution: 1080p.
- Default FPS: 30.
- Default render mode: Auto.
- AI mode: offline-first with optional cloud.
- HDR: allow user to select HDR or SDR, gated by verified capability.

Keep these as proposed defaults until the user confirms them.

## 8. Immediate implementation preparation

Before opening Android Studio:
1. Review this blueprint against the current repository.
2. Finalize module boundaries and API/data contracts.
3. Define schema v2 and migration tests without breaking schema v1.
4. Produce a device capability test plan for the Vivo target.
5. Then implement in roadmap order, beginning with Project System and Timeline foundations.

## 9. Definition of done

- User can create, save, close and reopen a project without losing edits.
- Source files remain unchanged.
- Import, preview, timeline, audio, text, effects and export behave consistently.
- Preview and exported output match within documented codec/render tolerances.
- Unsupported codecs, AI languages, HDR modes and frame rates are clearly reported rather than falsely offered as working.
- Core editing remains usable offline after required assets/models are installed.
- User explicitly controls cloud processing and downloads.
