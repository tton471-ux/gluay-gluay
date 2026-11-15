from __future__ import annotations

import shutil
import subprocess
from pathlib import Path

# Presets are deliberately fixed: never accept raw FFmpeg filter text from clients.
EFFECT_FILTERS: dict[str, str | None] = {
    "none": None,
    "cinematic": "eq=contrast=1.12:saturation=1.16:gamma=0.98,vignette=PI/5",
    "vintage": "colorbalance=rs=0.04:gs=0.01:bs=-0.04,eq=saturation=0.78:contrast=0.94,vignette=PI/4",
    "bw": "hue=s=0,eq=contrast=1.12",
    "warm": "colorbalance=rs=0.08:gs=0.02:bs=-0.08",
    "cool": "colorbalance=rs=-0.05:gs=0:bs=0.08",
    "vivid": "eq=saturation=1.42:contrast=1.08",
    "soft": "gblur=sigma=0.65",
    "sharpen": "unsharp=5:5:0.8:3:3:0.2",
    "dream": "split[a][b];[a]gblur=sigma=12[glow];[b][glow]blend=all_mode=screen:all_opacity=0.18",
    "mirror": "hflip",
    "flip": "vflip",
    "blur": "gblur=sigma=8",
    "edge": "edgedetect=low=0.08:high=0.22",
    "pixel": "scale=iw/18:ih/18:flags=neighbor,scale=iw*18:ih*18:flags=neighbor",
    "night": "eq=brightness=-0.08:saturation=0.72:contrast=1.2,colorbalance=bs=0.12",
    "sunset": "colorbalance=rs=0.12:gs=0.03:bs=-0.12,eq=saturation=1.18",
    "oldtv": "eq=saturation=0.72:contrast=1.18,noise=alls=10:allf=t",
}


def render_effect(input_path: Path, output_path: Path, effect_id: str) -> Path:
    """Render a selected built-in effect while preserving the source audio."""
    if effect_id not in EFFECT_FILTERS:
        raise ValueError(f"Unknown effect: {effect_id}")
    ffmpeg = shutil.which("ffmpeg")
    if not ffmpeg:
        raise RuntimeError("FFmpeg is not installed or is not available in PATH")

    command = [ffmpeg, "-y", "-i", str(input_path)]
    filter_value = EFFECT_FILTERS[effect_id]
    if filter_value:
        command += ["-vf", filter_value]
    command += [
        "-map", "0:v:0", "-map", "0:a?",
        "-c:v", "libx264", "-preset", "medium", "-crf", "18",
        "-pix_fmt", "yuv420p", "-c:a", "aac", "-b:a", "192k",
        "-movflags", "+faststart", str(output_path),
    ]
    try:
        subprocess.run(command, check=True, capture_output=True, text=True, timeout=3600)
    except subprocess.CalledProcessError as exc:
        detail = (exc.stderr or exc.stdout or "FFmpeg failed")[-3000:]
        raise RuntimeError(detail) from exc
    except subprocess.TimeoutExpired as exc:
        raise RuntimeError("Effect render exceeded the 60-minute safety limit") from exc
    return output_path
