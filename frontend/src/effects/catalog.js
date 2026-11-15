/** GLUAY GLUAY effect presets. Values are FFmpeg video-filter fragments from a fixed allowlist. */
export const EFFECT_PRESETS = [
  { id: "none", name: "Original", category: "Basic", description: "No visual change", filter: null },
  { id: "cinematic", name: "Cinematic", category: "Color", description: "Film contrast and subtle saturation", filter: "eq=contrast=1.12:saturation=1.16:gamma=0.98,vignette=PI/5" },
  { id: "vintage", name: "Vintage Film", category: "Color", description: "Warm faded film look", filter: "colorbalance=rs=0.04:gs=0.01:bs=-0.04,eq=saturation=0.78:contrast=0.94,vignette=PI/4" },
  { id: "bw", name: "Black & White", category: "Color", description: "Monochrome high contrast", filter: "hue=s=0,eq=contrast=1.12" },
  { id: "warm", name: "Warm", category: "Color", description: "Warm sunlight tone", filter: "colorbalance=rs=0.08:gs=0.02:bs=-0.08" },
  { id: "cool", name: "Cool", category: "Color", description: "Cool blue tone", filter: "colorbalance=rs=-0.05:gs=0:bs=0.08" },
  { id: "vivid", name: "Vivid", category: "Color", description: "Punchy color", filter: "eq=saturation=1.42:contrast=1.08" },
  { id: "soft", name: "Soft", category: "Basic", description: "Gentle softened image", filter: "gblur=sigma=0.65" },
  { id: "sharpen", name: "Sharpen", category: "Basic", description: "Crisper details", filter: "unsharp=5:5:0.8:3:3:0.2" },
  { id: "dream", name: "Dream Glow", category: "Creative", description: "Soft dreamy glow", filter: "split[a][b];[a]gblur=sigma=12[glow];[b][glow]blend=all_mode=screen:all_opacity=0.18" },
  { id: "mirror", name: "Mirror", category: "Transform", description: "Horizontal mirror", filter: "hflip" },
  { id: "flip", name: "Vertical Flip", category: "Transform", description: "Flip upside down", filter: "vflip" },
  { id: "blur", name: "Blur", category: "Creative", description: "Strong gaussian blur", filter: "gblur=sigma=8" },
  { id: "edge", name: "Sketch Edge", category: "Creative", description: "Edge-emphasis sketch look", filter: "edgedetect=low=0.08:high=0.22" },
  { id: "pixel", name: "Pixelate", category: "Creative", description: "Pixelated mosaic", filter: "scale=iw/18:ih/18:flags=neighbor,scale=iw*18:ih*18:flags=neighbor" },
  { id: "night", name: "Night Mood", category: "Color", description: "Dark cool night grade", filter: "eq=brightness=-0.08:saturation=0.72:contrast=1.2,colorbalance=bs=0.12" },
  { id: "sunset", name: "Sunset", category: "Color", description: "Golden orange highlights", filter: "colorbalance=rs=0.12:gs=0.03:bs=-0.12,eq=saturation=1.18" },
  { id: "oldtv", name: "Retro TV", category: "Creative", description: "Retro low-resolution scanline feel", filter: "eq=saturation=0.72:contrast=1.18,noise=alls=10:allf=t" }
];

export const EFFECT_CATEGORIES = ["All", "Basic", "Color", "Creative", "Transform"];
