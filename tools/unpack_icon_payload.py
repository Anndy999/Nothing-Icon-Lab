#!/usr/bin/env python3
"""Decode tools/icon-payload and write iconpack resources."""
from __future__ import annotations

import base64
import io
import tarfile
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
PAYLOAD = ROOT / "tools" / "icon-payload"
DEST = ROOT / "iconpack" / "src" / "main" / "res" / "drawable-nodpi"
ASSETS = ROOT / "iconpack" / "src" / "main" / "assets"
XML = ROOT / "iconpack" / "src" / "main" / "res" / "xml"
MAP = ROOT / "tools" / "icon-payload" / "mappings.tsv"


def write_xml(mappings: list[tuple[str, str, str]]) -> None:
    ASSETS.mkdir(parents=True, exist_ok=True)
    XML.mkdir(parents=True, exist_ok=True)
    filt = ['<?xml version="1.0" encoding="utf-8"?>', "<resources>"]
    for draw, pkg, act in mappings:
        filt.append(
            f'    <item component="ComponentInfo{{{pkg}/{act}}}" drawable="{draw}" />'
        )
    filt.append("</resources>")
    body = "\n".join(filt) + "\n"
    (ASSETS / "appfilter.xml").write_text(body)
    (XML / "appfilter.xml").write_text(body)
    draws = sorted({m[0] for m in mappings})
    dxml = ['<?xml version="1.0" encoding="utf-8"?>', "<resources>"]
    dxml += [f'    <item drawable="{d}" />' for d in draws]
    dxml.append("</resources>")
    dbody = "\n".join(dxml) + "\n"
    (ASSETS / "drawable.xml").write_text(dbody)
    (XML / "drawable.xml").write_text(dbody)


def main() -> int:
    mappings: list[tuple[str, str, str]] = []
    if MAP.exists():
        for line in MAP.read_text().splitlines():
            if not line.strip():
                continue
            draw, pkg, act = line.split("\t")
            mappings.append((draw, pkg, act))
        write_xml(mappings)
        print(f"wrote appfilter items={len(mappings)}")

    parts = sorted(PAYLOAD.glob("part*.b64"))
    if not parts:
        print("no png payload parts")
        return 0 if mappings else 1
    blob = "".join(p.read_text().strip() for p in parts)
    raw = base64.b64decode(blob)
    DEST.mkdir(parents=True, exist_ok=True)
    stub = ROOT / "iconpack/src/main/res/drawable"
    if stub.exists():
        for xml in stub.glob("*.xml"):
            if xml.stem != "ic_launcher":
                xml.unlink()
    with tarfile.open(fileobj=io.BytesIO(raw), mode="r:gz") as tf:
        tf.extractall(DEST)
    pngs = list(DEST.glob("*.png"))
    print(f"unpacked {len(pngs)} pngs")
    return 0 if pngs else 1


if __name__ == "__main__":
    raise SystemExit(main())
