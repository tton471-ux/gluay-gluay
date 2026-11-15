from __future__ import annotations

import shutil
import uuid
from pathlib import Path
from typing import Optional

from fastapi import FastAPI, File, HTTPException, UploadFile
from fastapi.middleware.cors import CORSMiddleware
from fastapi.responses import FileResponse, JSONResponse

from backend.app.services.effects import EFFECT_FILTERS, render_effect
from backend.app.services.subtitle import transcribe_to_srt
from backend.app.services.video import ffmpeg_status

ROOT_DIR = Path(__file__).resolve().parents[2]
DATA_DIR = ROOT_DIR / "data"
UPLOAD_DIR = DATA_DIR / "uploads"
SUBTITLE_DIR = DATA_DIR / "subtitles"
EFFECT_DIR = DATA_DIR / "effects"
for folder in (UPLOAD_DIR, SUBTITLE_DIR, EFFECT_DIR):
    folder.mkdir(parents=True, exist_ok=True)

app = FastAPI(title="GLUAY GLUAY API", version="0.2.0")
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],  # Restrict to trusted frontend origins before public deployment.
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

MEDIA: dict[str, Path] = {}
ALLOWED_EXTENSIONS = {
    ".mp4", ".mov", ".mkv", ".webm", ".avi",
    ".mp3", ".wav", ".m4a", ".aac", ".flac",
}


@app.get("/")
def root():
    return {"name": "GLUAY GLUAY", "status": "running", "docs": "/docs"}


@app.get("/health")
def health():
    return {
        "status": "ok",
        "service": "gluay-gluay-api",
        "ffmpeg": ffmpeg_status(),
        "subtitle_engine": "faster-whisper",
        "effects": len(EFFECT_FILTERS),
    }


@app.get("/api/effects")
def list_effects():
    return {
        "effects": [
            {"id": effect_id, "available": True}
            for effect_id in EFFECT_FILTERS
        ]
    }


@app.post("/api/media/upload")
async def upload_media(file: UploadFile = File(...)):
    suffix = Path(file.filename or "").suffix.lower()
    if suffix not in ALLOWED_EXTENSIONS:
        raise HTTPException(status_code=415, detail="Unsupported media format")

    media_id = uuid.uuid4().hex
    destination = UPLOAD_DIR / f"{media_id}{suffix}"
    try:
        with destination.open("wb") as output:
            shutil.copyfileobj(file.file, output)
    finally:
        await file.close()

    MEDIA[media_id] = destination
    return {
        "id": media_id,
        "media_id": media_id,
        "filename": file.filename,
        "size": destination.stat().st_size,
        "status": "uploaded",
    }


@app.post("/api/media/{media_id}/transcribe")
async def transcribe_media(media_id: str, language: Optional[str] = "auto"):
    media_path = MEDIA.get(media_id)
    if media_path is None or not media_path.is_file():
        raise HTTPException(status_code=404, detail="Media not found. Upload the file again.")

    try:
        srt_text = transcribe_to_srt(media_path, language=language or "auto")
    except RuntimeError as exc:
        raise HTTPException(status_code=503, detail=str(exc)) from exc
    except Exception as exc:
        raise HTTPException(status_code=500, detail=f"Transcription failed: {exc}") from exc

    subtitle_path = SUBTITLE_DIR / f"{media_id}.srt"
    subtitle_path.write_text(srt_text, encoding="utf-8")
    return {
        "media_id": media_id,
        "language": language,
        "subtitle": srt_text,
        "subtitle_url": f"/api/media/{media_id}/subtitle",
        "status": "completed",
    }


@app.get("/api/media/{media_id}/subtitle")
def get_subtitle(media_id: str):
    subtitle_path = SUBTITLE_DIR / f"{media_id}.srt"
    if not subtitle_path.is_file():
        raise HTTPException(status_code=404, detail="Subtitle not generated yet")
    return JSONResponse({"media_id": media_id, "subtitle": subtitle_path.read_text(encoding="utf-8")})


@app.post("/api/media/{media_id}/effects/{effect_id}")
def apply_effect(media_id: str, effect_id: str):
    media_path = MEDIA.get(media_id)
    if media_path is None or not media_path.is_file():
        raise HTTPException(status_code=404, detail="Media not found. Upload the file again.")
    if effect_id not in EFFECT_FILTERS:
        raise HTTPException(status_code=404, detail="Unknown effect preset")

    output_path = EFFECT_DIR / f"{media_id}_{effect_id}.mp4"
    try:
        render_effect(media_path, output_path, effect_id)
    except RuntimeError as exc:
        raise HTTPException(status_code=503, detail=str(exc)) from exc
    except ValueError as exc:
        raise HTTPException(status_code=400, detail=str(exc)) from exc

    return {
        "media_id": media_id,
        "effect_id": effect_id,
        "status": "completed",
        "download_url": f"/api/media/{media_id}/effects/{effect_id}/download",
    }


@app.get("/api/media/{media_id}/effects/{effect_id}/download")
def download_effect(media_id: str, effect_id: str):
    if effect_id not in EFFECT_FILTERS:
        raise HTTPException(status_code=404, detail="Unknown effect preset")
    output_path = EFFECT_DIR / f"{media_id}_{effect_id}.mp4"
    if not output_path.is_file():
        raise HTTPException(status_code=404, detail="Rendered effect not found")
    return FileResponse(output_path, media_type="video/mp4", filename=f"gluay_{effect_id}.mp4")
