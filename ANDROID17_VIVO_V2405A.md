# GLUAY GLUAY — Android 17 / Vivo V2405A profile

Target device supplied by the project owner:
- Device: Vivo V2405A
- OS: Android 17
- UI layer: OriginOS 7

## Build profile
- compileSdk: 37
- targetSdk: 37
- minSdk: 26 (Android 8.0 and later)
- Java bytecode target: 17
- Android Gradle Plugin: 8.10.1
- Kotlin: 2.2.0
- AndroidX Media3: 1.11.1

Install Android SDK Platform 37 and a compatible Android SDK Build-Tools package in Android Studio before syncing/building.

## Device-focused engineering requirements
1. Keep editing, preview, waveform work, and rendering local to the phone by default.
2. Use the Android system document picker for media import so the app does not request broad storage access.
3. Pause preview playback when the editor activity is no longer visible. Do not attempt background audio playback unless a user-facing Media3 MediaSessionService is implemented.
4. Start microphone recording only from an explicit user action while the editor is visible. Add a properly declared microphone foreground service before supporting recording while backgrounded.
5. Treat OriginOS battery/background restrictions as a device test item. Ask the user to allow background activity only if a future feature genuinely needs it; normal editing should work with the app open.
6. Test import, playback, audio extraction, microphone permission, recording, app pause/resume, long video preview, and export on the physical Vivo V2405A.
7. Do not claim device certification until the APK has been built and these tests have passed on the actual phone.

## Current implementation status
This is a compatibility build-profile update, not a completed video editor. The current app remains an early native shell with video preview and basic audio import/extraction/voice-over capture. Timeline editing, multitrack mixing, waveforms, effects UI, and final export are still future milestones.
