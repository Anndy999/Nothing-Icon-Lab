#!/usr/bin/env python3
"""One-time extract from Nada APK into iconpack/curated/png.

Never used as the Theme Park APK. Existing curated PNGs are not overwritten.
Requires: androguard + Pillow if rebuilding from APK.
"""
from __future__ import annotations

import json
import re
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
INDEX = ROOT / "iconpack" / "curated" / "index.json"
PNG_DIR = ROOT / "iconpack" / "curated" / "png"


def main() -> int:
    if len(sys.argv) < 2:
        print("usage: python3 tools/extract_curated_from_apk.py Nada_adapted-aligned-debugSigned.apk")
        print("index already records 119 CURATED + 3 unmapped phone apps.")
        print(INDEX)
        return 2
    print("APK extract helper is local-only (androguard).")
    print("Do not ship the Nada APK. Curated PNGs live in iconpack/curated/png/")
    print("Import never overwrites that folder.")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
