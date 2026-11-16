# GLUAY GLUAY — Mobile-first Video Editor

GLUAY GLUAY is being developed Android-first as a native, local-first video editor. The goal is to import, preview, cut, arrange, subtitle, add effects and export videos on the phone without requiring a cloud backend for basic editing.

## Repository status
This repository contains:
- An initial native Android app shell under `android/` (Kotlin, Jetpack Compose, AndroidX Media3).
- A system video picker and local playback preview as the first Android milestone.
- A Python FastAPI development scaffold under `backend/` for optional transcription/effect experiments.
- A small web/API check page under `frontend/`; this is not the Android editor UI.
- A platform-neutral project schema under `shared/`.

The original Windows v0.3.0 working folder is separate and is not automatically synchronized with this repository. Keep a backup before merging any files.

## Android first milestone
Open the `android/` folder in Android Studio, use JDK 17, install Android SDK Platform 35, sync Gradle, then run the `app` configuration on an Android 8.0+ device or emulator. See [android/README.md](android/README.md).

Current Android scope: choose a video from the system picker and preview/play it locally. Timeline editing, split/delete, project saving, effects, subtitles and export are not implemented in this shell yet.

## GLUAY GLUAY PRO master plan
See [GLUAY_GLUAY_PRO_MASTER_BLUEPRINT.md](GLUAY_GLUAY_PRO_MASTER_BLUEPRINT.md) for the agreed product scope, feature modules, offline-first architecture, roadmap, acceptance gates and pending defaults. Planned features in that document are specifications, not claims of implementation.

## Architecture
See [MOBILE_ARCHITECTURE.md](MOBILE_ARCHITECTURE.md). Android editing will use a non-destructive timeline model, Media3 playback/Transformer and Android MediaCodec where supported. Basic editing must not depend on the Python backend or internet access.

## Optional Python development API
Requirements: Python 3.10–3.12 and FFmpeg/FFprobe on PATH.

```bat
py -3.12 -m venv .venv
.venv\\Scripts\\activate
python -m pip install --upgrade pip
pip install -r requirements.txt
uvicorn backend.app.main:app --host 127.0.0.1 --port 8000 --reload
```

API health check: http://127.0.0.1:8000/health

## Safety and privacy
- User-selected media remains on-device for the Android editor.
- Do not upload private videos to a cloud service without explicit opt-in.
- Never overwrite original source media during editing.
- Do not claim HDR/10-bit preservation until verified on supported physical devices.


## Implementation progress
- Added `shared/project-schema-v2.json` as an expanded project format covering canvas/export profile, assets, video/audio/text/caption tracks, clip transforms, keyframe/effect/transition placeholders, AI suggestions and proxy/cache references. Schema v1 remains unchanged.
- Added `android/app/src/main/java/com/gluaygluay/editor/timeline/TimelineModels.kt` with a non-destructive in-memory timeline model and pure add/move/trim/delete/split operations. This is an engine foundation, not yet connected to the app UI or persistent project storage.
- These changes have not yet been built or tested on Android. Do not treat them as a finished usable timeline until Room persistence, UI integration, rendering and device tests are complete.
