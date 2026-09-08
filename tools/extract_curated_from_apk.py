#!/usr/bin/env python3
"""One-time extract: adapted icons from a source APK into iconpack/curated/."""
from __future__ import annotations

import json
import re
import sys
import zipfile
from pathlib import Path

from iconpack_names import PACKAGE_DRAWABLE, sanitize


def stem_to_package_guess(name: str) -> str | None:
    stem = Path(name).stem.lower()
    for pkg, draw in PACKAGE_DRAWABLE.items():
        if stem == draw or stem.endswith("_" + draw):
            return pkg
    return None


def main() -> int:
    if len(sys.argv) < 2:
        print("usage: python3 tools/extract_curated_from_apk.py Nada_adapted-aligned-debugSigned.apk")
        return 2
    apk = Path(sys.argv[1]).expanduser().resolve()
    if not apk.is_file():
        print(f"apk not found: {apk}")
        return 1
    root = Path(__file__).resolve().parents[1]
    curated_png = root / "iconpack" / "curated" / "png"
    index_path = root / "iconpack" / "curated" / "index.json"
    curated_png.mkdir(parents=True, exist_ok=True)
    index = {"version": 1, "icons": []}
    if index_path.exists():
        index = json.loads(index_path.read_text())
    existing = {item.get("drawable") for item in index.get("icons", [])}

    zf = zipfile.ZipFile(apk)
    pngs = [
        n
        for n in zf.namelist()
        if n.lower().endswith(".png") and ("drawable" in n.lower() or n.startswith("res/"))
    ]
    added = 0
    skipped = 0
    for name in pngs:
        stem = sanitize(Path(name).stem)
        dest = curated_png / f"{stem}.png"
        if dest.exists() or stem in existing:
            skipped += 1
            continue
        dest.write_bytes(zf.read(name))
        index.setdefault("icons", []).append(
            {
                "drawable": stem,
                "apk_entry": name,
                "package": stem_to_package_guess(stem),
                "source": "CURATED",
                "origin": apk.name,
            }
        )
        existing.add(stem)
        added += 1
    index_path.write_text(json.dumps(index, indent=2, ensure_ascii=False) + "\n")
    print(f"curated added={added} skipped_existing={skipped} png_dir={curated_png}")
    print("Nada APK is now only a historical source. Do not ship it as Theme Park APK.")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
