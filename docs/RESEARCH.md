# Research notes — Nothing renderer reconstruction

Nothing Icon Lab does **not** ship Nothing proprietary code. The renderer is a
reconstruction built from:

1. AOSP Launcher3 `MonochromeIconFactory` (Apache 2.0)
2. AOSP `AdaptiveIconDrawable` extra-inset math
3. AOSP themed-icon inset used by `ThemedIconDrawable`
4. Lawnicons launchable-app scan (`ACTION_MAIN` + `CATEGORY_LAUNCHER`)
5. Public observation of Nothing OS themed icons (circular plate, B/W glyph)
6. A previously reported constant `0.3888889` (7/18)

## What is verified (AOSP)

| Constant | Value | Source |
| --- | --- | --- |
| Adaptive extra inset | `0.25` | `AdaptiveIconDrawable.getExtraInsetFraction()` |
| Viewport scale | `1 / (1 + 2 * 0.25) = 2/3` | AOSP |
| Themed mono inset | `0.25 / 1.5 = 1/6 ≈ 0.1667` | AOSP ThemedIconDrawable |
| Forced mono | grayscale → RGB average as alpha → contrast stretch → optional edge flip | AOSP MonochromeIconFactory |

## What is hypothesized (Nothing Launcher 2.5.9)

The workspace did **not** contain `com.nothing.launcher` 2.5.9 or
`com.nothing.icon` 1.0.1 at v0.1.0 build time. The following remain tunable
until those APKs are provided for re-verification:

| Parameter | Default | Status |
| --- | --- | --- |
| `logoScale` | `0.3888889` | Hypothesized glyph visual size vs full canvas. **Not treated as proven.** |
| Circular plate | `backgroundSize = 1.0` | Matches public Nothing screenshots; exact px not verified |
| Light colors | white plate / black glyph | Public observation |
| Dark colors | black plate / white glyph | Public observation |
| Crop-to-content | on | Makes `logoScale` apply to the visible glyph, not padded adaptive canvas |

Strings previously flagged for bytecode search (when APKs arrive):

- `THEMED_ICONS_NOTHING`
- `com.nothing.launcher.themed_icons_nothing`
- `com.nothing.icon`
- `nothing_icon_pack_force_render_enable`
- `MonochromeIconFactory`
- `ThemedIconDrawable`
- `grayscale_icon_map` / `nt_grayscale_icon_map`
- `0.3888889`

## Pipeline used in v0.1

```
real APK icon
  ├─ AdaptiveIconDrawable.monochrome  → NATIVE_MONO
  ├─ else AOSP MonochromeIconFactory  → FORCED_MONO
  └─ else grayscale of launcher icon  → FALLBACK

glyph
  → optional alpha threshold
  → optional content crop (or adaptive+mono insets)
  → scale to logoScale * foregroundScale
  → center on circular plate
```

Do not default to Lawnicons SVGs or downloaded brand logos.
