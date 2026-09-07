# Nothing Icon Lab

Research, preview, tune, and export **Nothing-style** monochrome icons — without replacing Samsung One UI Home.

Nothing Icon Lab is a sideload Android 13+ tool. It is **not** a launcher and it does **not** modify Theme Park, One UI Home, or any system app.

**Nothing Icon Lab is not affiliated with Nothing Technology Limited.**
**Nothing and Nothing OS are trademarks of their respective owner.**

## Why this exists

The usual Theme Park loop is too slow:

> tweak a few icons → compile APK → install → Theme Park → apply → screenshot → tweak again

This app moves that loop **inside the device**:

1. Scan every launchable app
2. Read the **real** APK icon (`packageName` + launcher Activity + Adaptive Icon + `monochrome`)
3. Prefer official Android 13+ monochrome; otherwise run AOSP `MonochromeIconFactory`
4. Apply a reconstructed Nothing plate (circular background + centered glyph)
5. Live-tune parameters and see the whole grid update
6. Export a Theme Park–oriented test pack (`appfilter.xml` + PNGs)

The visual reference is **Nothing Launcher 2.5.9**, not Lawnicons. Lawnicons is used only as infrastructure (scan / Adaptive Icon / appfilter ideas).

## Current status

**v0.1.0** — first lab build.

| Feature | v0.1 |
| --- | --- |
| Scan launchable apps | yes |
| Native monochrome | yes |
| Forced monochrome (AOSP) | yes |
| Nothing-style render + live params | yes |
| Grid / list / detail | yes |
| Export ZIP | yes |
| Per-app override | v0.2 |
| Theme Park project | v0.3 |
| Auto Theme Park APK | v0.4 |

## Install

1. Open the [GitHub Releases](https://github.com/Anndy999/Nothing-Icon-Lab/releases) page.
2. Download `Nothing-Icon-Lab-vX.Y.Z.apk`.
3. Sideload. Allow `QUERY_ALL_PACKAGES` so the scanner can see every app.

Until release-signing secrets are configured, APKs may be **debug-signed**. That is called out in the release notes. Do not treat those as a stable upgrade signature.

## Supported Android versions

- Minimum: **Android 13 (API 33)**
- Target: **Android 15 (API 35)**
- Reason: `AdaptiveIconDrawable.monochrome` and themed-icon research

## How export works

In the app: **Export** → share `NothingIconLab-export.zip`.

```
NothingIconLab-export.zip
├── appfilter.xml
├── apps.txt
├── config.json
└── icons/
    ├── final/
    ├── native_monochrome/
    └── forced_monochrome/
```

`appfilter.xml` uses:

```xml
<item component="ComponentInfo{package/activity}" drawable="name" />
```

This is the mapping Theme Park / third-party icon packs expect. v0.1 does **not** yet emit a full Theme Park APK.

## How to build

```bash
export ANDROID_HOME=/path/to/Android/Sdk
./gradlew testDebugUnitTest
./gradlew assembleRelease
```

The APK lands under `app/build/outputs/apk/`.

### GitHub Actions

Push to `main` or push a tag `v*`:

- Checkout, Java 17, Gradle cache
- Unit tests + lint
- Release APK
- Artifact upload
- On `v*` tags: GitHub Release + APK upload

### Release signing secrets (not in the repo)

| Secret | Purpose |
| --- | --- |
| `ANDROID_KEYSTORE_BASE64` | Base64-encoded `.jks` / `.keystore` |
| `ANDROID_KEYSTORE_PASSWORD` | Keystore password |
| `ANDROID_KEY_ALIAS` | Key alias |
| `ANDROID_KEY_PASSWORD` | Key password |

If these are missing, CI still builds and still publishes a tag Release, but the APK is **debug-signed**. Configure the four secrets once and every later version can overwrite-upgrade on the phone.

## Roadmap

- **v0.1** scan, preview, native/forced mono, Nothing render, live params, ZIP export
- **v0.2** bad-result workflow, per-app overrides (e.g. Chrome scale = 0.39)
- **v0.3** Samsung Theme Park Icon Pack project output
- **v0.4** automatic APK generation, then watch for newly installed apps

## Development principles

- Read Chrome from the Chrome APK. Never replace it with a downloaded logo.
- Prefer AOSP / Apache 2.0 / Lawnicons infrastructure. Do not copy Nothing closed source.
- Keep the architecture small and debuggable. Log `IconSource` and renderer parameters.
- Do not root, do not patch One UI Home, do not patch Theme Park.

## License

Apache License 2.0. See [LICENSE](LICENSE) and [NOTICE](NOTICE).
Research notes: [docs/RESEARCH.md](docs/RESEARCH.md).
