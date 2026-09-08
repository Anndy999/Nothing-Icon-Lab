#!/usr/bin/env python3
"""Import a NothingIconLab-export.zip into iconpack resources."""
from __future__ import annotations

import re
import sys
import zipfile
from pathlib import Path

PACKAGE_DRAWABLE = {
    "com.android.chrome": "chrome",
    "com.google.android.googlequicksearchbox": "google",
    "com.google.android.gm": "gmail",
    "com.google.android.youtube": "youtube",
    "com.tencent.mm": "wechat",
    "com.openai.chatgpt": "chatgpt",
    "com.twitter.android": "x",
    "org.telegram.messenger": "telegram",
    "org.telegram.messenger.web": "telegram",
    "app.nixgramx.android": "nixgramx",
    "ai.x.grok": "grok",
    "com.ss.android.ugc.aweme": "douyin",
    "com.appshub.bettbox": "bettbox",
    "com.anndy999.nothingiconlab": "nothing_icon_lab",
}


def sanitize(raw: str) -> str:
    s = re.sub(r"[^a-z0-9]+", "_", raw.lower()).strip("_")
    if not s:
        s = "unknown"
    if s[0].isdigit():
        s = "i_" + s
    return s[:90]


def parse_apps(text: str) -> list[dict[str, str]]:
    rows: list[dict[str, str]] = []
    for line in text.splitlines()[1:]:
        if not line.strip():
            continue
        parts = line.split("\t")
        if len(parts) < 4:
            continue
        rows.append(
            {
                "label": parts[0],
                "package": parts[1],
                "activity": parts[2],
                "component": parts[3],
            }
        )
    return rows


def write_xml(assets: Path, xml_dir: Path, mappings: list[tuple[str, str, str]]) -> None:
    assets.mkdir(parents=True, exist_ok=True)
    xml_dir.mkdir(parents=True, exist_ok=True)
    filt = ['<?xml version="1.0" encoding="utf-8"?>', "<resources>"]
    for draw, pkg, act in mappings:
        filt.append(
            f'    <item component="ComponentInfo{{{pkg}/{act}}}" drawable="{draw}" />'
        )
    filt.append("</resources>")
    body = "\n".join(filt) + "\n"
    (assets / "appfilter.xml").write_text(body)
    (xml_dir / "appfilter.xml").write_text(body)
    draws = sorted({m[0] for m in mappings})
    dxml = ['<?xml version="1.0" encoding="utf-8"?>', "<resources>"]
    dxml += [f'    <item drawable="{d}" />' for d in draws]
    dxml.append("</resources>")
    dbody = "\n".join(dxml) + "\n"
    (assets / "drawable.xml").write_text(dbody)
    (xml_dir / "drawable.xml").write_text(dbody)


def main() -> int:
    if len(sys.argv) < 2:
        print("usage: python3 tools/import_iconlab_export.py NothingIconLab-export.zip")
        return 2
    zip_path = Path(sys.argv[1]).expanduser().resolve()
    if not zip_path.is_file():
        print(f"zip not found: {zip_path}")
        return 1
    root = Path(__file__).resolve().parents[1]
    dest = root / "iconpack" / "src" / "main" / "res" / "drawable-nodpi"
    assets = root / "iconpack" / "src" / "main" / "assets"
    xml_dir = root / "iconpack" / "src" / "main" / "res" / "xml"
    dest.mkdir(parents=True, exist_ok=True)

    zf = zipfile.ZipFile(zip_path)
    names = set(zf.namelist())
    rows = parse_apps(zf.read("apps.txt").decode("utf-8"))
    used: dict[str, str] = {}
    mappings: list[tuple[str, str, str]] = []
    missing = 0
    for row in rows:
        pkg, act = row["package"], row["activity"]
        draw = PACKAGE_DRAWABLE.get(pkg)
        if not draw:
            draw = sanitize(f"{pkg}_{act.rsplit('.', 1)[-1]}")
            n = 2
            base = draw
            while draw in used and used[draw] != f"{pkg}/{act}":
                draw = f"{base}_{n}"
                n += 1
        used[draw] = f"{pkg}/{act}"
        old = sanitize(f"{pkg}_{act.rsplit('.', 1)[-1]}")
        candidates = [f"icons/final/{draw}.png", f"icons/final/{old}.png"]
        candidates += [
            n
            for n in names
            if n.startswith("icons/final/") and pkg.replace(".", "_") in n
        ]
        png = next((c for c in candidates if c in names), None)
        if png is None:
            print(f"MISSING {pkg}/{act}")
            missing += 1
            continue
        (dest / f"{draw}.png").write_bytes(zf.read(png))
        mappings.append((draw, pkg, act))
    write_xml(assets, xml_dir, mappings)
    print(f"imported {len(mappings)} icons, missing={missing}, dest={dest}")
    return 0 if mappings and missing == 0 else 1


if __name__ == "__main__":
    raise SystemExit(main())
