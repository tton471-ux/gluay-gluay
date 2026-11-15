from __future__ import annotations

import shutil
import subprocess


def _version(binary: str) -> str | None:
    executable = shutil.which(binary)
    if not executable:
        return None
    try:
        result = subprocess.run(
            [executable, "-version"],
            capture_output=True,
            text=True,
            timeout=5,
            check=False,
        )
        lines = (result.stdout or "").splitlines()
        return lines[0] if lines else "available"
    except (OSError, subprocess.TimeoutExpired):
        return "available"


def ffmpeg_status() -> dict[str, str | bool | None]:
    ffmpeg = _version("ffmpeg")
    ffprobe = _version("ffprobe")
    return {
        "available": bool(ffmpeg and ffprobe),
        "ffmpeg": ffmpeg,
        "ffprobe": ffprobe,
    }
