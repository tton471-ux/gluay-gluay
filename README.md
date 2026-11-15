# GLUAY GLUAY — AI Video Workspace

A local-first video utility for importing clips, generating AI subtitles with Faster-Whisper, and rendering MP4 files with FFmpeg.

## Project status
Starter repository scaffold. The existing working copy on the Windows PC is not automatically connected to this repository; review and merge these files into that working copy before replacing or deploying anything.

## Requirements
- Windows 10/11
- Python 3.10–3.12 (Python 3.12 recommended)
- FFmpeg and FFprobe available on PATH

## Quick start (Windows)
1. Install FFmpeg and confirm `ffmpeg -version` and `ffprobe -version` work in Command Prompt.
2. Create and activate a virtual environment:
   ```bat
   py -3.12 -m venv .venv
   .venv\Scripts\activate
   ```
3. Install dependencies:
   ```bat
   python -m pip install --upgrade pip
   pip install -r requirements.txt
   ```
4. Start the API:
   ```bat
   uvicorn backend.app.main:app --host 127.0.0.1 --port 8000 --reload
   ```
5. Check the API at http://127.0.0.1:8000/health.

## API endpoints
- `GET /health` — service status and dependency checks
- `POST /api/media/upload` — upload a video/audio file; returns a media ID
- `POST /api/media/{media_id}/transcribe?language=auto` — transcribe speech and return SRT subtitle text

Uploaded files and generated subtitles are stored under local `data/`. Do not commit personal videos or generated media.

## Notes
- The first Faster-Whisper transcription may download a model and can take time.
- Default model is `small`; set `WHISPER_MODEL` to `base`, `medium`, or another supported model if your PC has sufficient resources.
- This starter does not yet implement the full timeline editor or final MP4 rendering UI. Those will be added in later stages.
