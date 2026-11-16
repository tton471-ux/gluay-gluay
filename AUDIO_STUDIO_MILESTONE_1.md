# GLUAY GLUAY Audio Studio — Milestone 1

## Audio workflows planned
1. **Import external music/audio:** choose a local audio file through Android's system document picker. The app retains access to the selected content URI when the provider permits persistent grants.
2. **Extract audio from a video:** inspect the selected video's tracks with Android MediaExtractor, copy its encoded audio samples into an M4A/MP4 container with MediaMuxer, and keep the original video unchanged. This first pass supports compatible audio codecs; unsupported codecs/containers will show an error and need a later transcode fallback.
3. **Record voice-over:** request RECORD_AUDIO at runtime, record microphone input to AAC/M4A with MediaRecorder, and stop/save the recording as a local audio asset.

## Current Android shell status
- The Android starter screen now contains buttons for importing audio, extracting audio from the selected video, and recording a voice-over.
- Extracted audio and voice recordings are written under app cache for this prototype. Imported music remains referenced by its Android content URI.
- The current screen lists the audio assets created/selected during the running session.
- The emulator cannot be relied on for microphone testing; use a physical Android phone and grant microphone permission.
- This milestone does **not yet** provide multitrack playback/mixing, waveform, clip placement, trimming, volume automation, fade controls, ducking, or audio-included final video export. The assets are not yet persisted as a reopenable project. These are the next audio-timeline milestones.

## Next implementation
- Persist assets and clip metadata in Room; copy extracted/recorded assets from cache into durable app-private project storage.
- Build separate music, original-video-audio, extracted-audio and voice-over timeline tracks.
- Add clip placement, split/trim, mute, volume, fades, waveform and preview mix.
- Render the final mix with video using Media3 Transformer/MediaCodec, then verify synchronization and supported formats on physical devices.

## Privacy
Microphone recording is only started after the user taps the record control and grants Android's RECORD_AUDIO permission. No audio is uploaded to a server by this local feature.
