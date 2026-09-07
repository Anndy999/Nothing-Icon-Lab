# Changelog

## v0.1.0

### Added

- Android 13+ lab app that scans every launchable app (`ACTION_MAIN` + `CATEGORY_LAUNCHER`).
- Reads the real APK icon: original, Adaptive Icon foreground/background, and Android 13+ `monochrome`.
- Icon source priority: Native Monochrome → AOSP Forced Monochrome → grayscale fallback.
- Nothing-style renderer with a circular plate, live-tunable scale/inset/threshold/contrast/invert.
- Grid preview, list preview, and per-app detail (Original / FG / BG / Mono / Forced / Result).
- Filters: All / Native / Forced / Fallback / Bad.
- Mark-as-bad (local).
- Export Test Pack ZIP: `appfilter.xml`, `apps.txt`, `config.json`, `icons/final|native_monochrome|forced_monochrome`.
- GitHub Actions: test, lint, assemble, artifact upload, tag → GitHub Release.

### Changed

- N/A (first release).

### Fixed

- N/A (first release).

### Known Issues

- Nothing Launcher 2.5.9 / Nothing Icon 1.0.1 APKs were not in the workspace, so `logoScale = 0.3888889` is a hypothesized default, not a bytecode-verified constant.
- GitHub Release signing secrets are not configured; APKs are debug-signed until secrets exist.
- No Theme Park APK generator yet (planned for v0.3 / v0.4).
- Forced mono quality is poor on photographic / multi-color icons (expected; fallback/overrides come in v0.2).
- Grid re-renders all icons on every parameter change; large device app lists can hitch.
- `QUERY_ALL_PACKAGES` is required; Play Store distribution is not a goal.
