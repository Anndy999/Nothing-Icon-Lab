# Icon pack import pipeline

## Priority

1. `iconpack/curated/png/<name>.png` — already-adapted icons (`source=CURATED`)
2. `NothingIconLab-export.zip` `icons/final/` — Lab generated (`source=GENERATED`)
3. ZIP original/fallback only if 1 and 2 are missing (`source=FALLBACK`)

Re-importing a ZIP **never overwrites** curated PNGs.

## Nada APK

`Nada_adapted-aligned-debugSigned.apk` is a one-time icon source only.
It is **not** the Theme Park APK.

Extract once:

```bash
python3 tools/extract_curated_from_apk.py Nada_adapted-aligned-debugSigned.apk
```

Then forget the Nada APK.
