from __future__ import annotations

import os
from pathlib import Path
from threading import Lock

_model = None
_model_lock = Lock()


def _get_model():
    global _model
    with _model_lock:
        if _model is None:
            try:
                from faster_whisper import WhisperModel
            except ImportError as exc:
                raise RuntimeError(
                    "Faster-Whisper is not installed in the Python environment running the API. "
                    "Run: python -m pip install -r requirements.txt"
                ) from exc

            model_name = os.getenv("WHISPER_MODEL", "small")
            device = os.getenv("WHISPER_DEVICE", "cpu")
            compute_type = os.getenv("WHISPER_COMPUTE_TYPE", "int8")
            try:
                _model = WhisperModel(model_name, device=device, compute_type=compute_type)
            except Exception as exc:
                raise RuntimeError(
                    f"Could not load Faster-Whisper model '{model_name}': {exc}"
                ) from exc
    return _model


def _stamp(seconds: float) -> str:
    milliseconds = max(0, round(seconds * 1000))
    hours, remainder = divmod(milliseconds, 3_600_000)
    minutes, remainder = divmod(remainder, 60_000)
    secs, millis = divmod(remainder, 1000)
    return f"{hours:02}:{minutes:02}:{secs:02},{millis:03}"


def transcribe_to_srt(media_path: Path, language: str = "auto") -> str:
    if not Path(media_path).is_file():
        raise RuntimeError("The uploaded media file no longer exists.")

    model = _get_model()
    options = {"vad_filter": True, "beam_size": 5}
    if language and language.lower() != "auto":
        options["language"] = language

    segments, info = model.transcribe(str(media_path), **options)
    entries = []
    for index, segment in enumerate(segments, start=1):
        text = (segment.text or "").strip()
        if text:
            entries.append(
                f"{len(entries) + 1}\n{_stamp(segment.start)} --> {_stamp(segment.end)}\n{text}\n"
            )

    if not entries:
        raise RuntimeError(
            f"No speech was detected (detected language: {getattr(info, 'language', 'unknown')})."
        )
    return "\n".join(entries)
