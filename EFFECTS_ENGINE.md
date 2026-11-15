# GLUAY GLUAY — Effects Engine

This is the first implementation layer for the Effects workspace.

## Included built-in presets (18)
- Basic: Original, Soft, Sharpen
- Color: Cinematic, Vintage Film, Black & White, Warm, Cool, Vivid, Night Mood, Sunset
- Creative: Dream Glow, Blur, Sketch Edge, Pixelate, Retro TV
- Transform: Mirror, Vertical Flip

## Architecture
- `frontend/src/effects/catalog.js` is the UI-facing preset catalogue.
- `backend/app/services/effects.py` is the trusted render allowlist.
- The backend never accepts arbitrary FFmpeg filter strings from a client.
- Rendering is local through FFmpeg and retains source audio (AAC output).
- Output is MP4/H.264 for broad compatibility.

## API
`POST /api/media/{media_id}/effects/{effect_id}`

The media must first be uploaded through `POST /api/media/upload`.
Response contains the effect ID and a download endpoint:
`GET /api/media/{media_id}/effects/{effect_id}/download`

## Important limitations in this milestone
- This is a render-on-export effect pass, not yet a real-time GPU preview.
- The first pass exports SDR H.264 (yuv420p); HDR metadata/10-bit preservation is not implemented here.
- Effects are applied to the whole uploaded clip. Timeline ranges, keyframes, transition handles, and per-clip effect stacks are later milestones.
- The browser preview/editor UI is not yet wired to these endpoints.
- Local render must be tested on the Windows development machine with FFmpeg installed.
