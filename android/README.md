# Android app — GLUAY GLUAY

## First milestone
A native Android shell built with Kotlin, Jetpack Compose and AndroidX Media3. It lets the user select a video using Android's system file picker and preview/play it locally.

## Open in Android Studio
1. Open the repository root (the folder containing `settings.gradle.kts`).
2. Use JDK 17.
3. Allow Android Studio to sync Gradle and install Android SDK Platform 35.
4. Run the `app` configuration on an Android device or emulator (Android 8.0 / API 26 or newer).

## Current scope
- Native Android entry screen.
- System video picker (no broad storage permission).
- Media3 playback preview with fit-to-frame behavior.
- No export, editing timeline, subtitles or effects UI yet.

## Next
Implement a non-destructive project model, Room persistence, timeline split/delete/undo, aspect-ratio canvas and Media3 Transformer export. Test on real devices before claiming support.
