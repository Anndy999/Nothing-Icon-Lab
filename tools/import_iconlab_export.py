#!/usr/bin/env python3
"""Build iconpack resources from phone scan + curated-first icons.

Priority:
  1. iconpack/curated/png/<drawable>.png     CURATED (never overwritten)
  2. ZIP icons/final/<drawable>.png          GENERATED
  3. ZIP icons/original or similar           FALLBACK (only if needed)
"""
from __future__ import annotations

import json
import sys
import zipfile
from pathlib import Path

from iconpack_names import drawable_for, sanitize


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


def write_xml(assets: Path, xml_dir: Path, mappings: list[dict]) -> None:
    assets.mkdir(parents=True, exist_ok=True)
    xml_dir.mkdir(parents=True, exist_ok=True)
    filt = ['<?xml version="1.0" encoding="utf-8"?>', "<resources>"]
    for row in mappings:
        filt.append(
            f'    <item component="ComponentInfo{{{row["package"]}/{row["activity"]}}}" '
            f'drawable="{row["drawable"]}" />'
        )
    filt.append("</resources>")
    body = "\n".join(filt) + "\n"
    (assets / "appfilter.xml").write_text(body)
    (xml_dir / "appfilter.xml").write_text(body)
    draws = sorted({row["drawable"] for row in mappings})
    dxml = ['<?xml version="1.0" encoding="utf-8"?>', "<resources>"]
    dxml += [f'    <item drawable="{d}" />' for d in draws]
    dxml.append("</resources>")
    dbody = "\n".join(dxml) + "\n"
    (assets / "drawable.xml").write_text(dbody)
    (xml_dir / "drawable.xml").write_text(dbody)


def pick_zip_png(zf: zipfile.ZipFile, names: set[str], draw: str, pkg: str, act: str) -> tuple[bytes | None, str]:
    old = sanitize(f"{pkg}_{act.rsplit('.', 1)[-1]}")
    generated = [f"icons/final/{draw}.png", f"icons/final/{old}.png"]
    generated += [
        n for n in names if n.startswith("icons/final/") and pkg.replace(".", "_") in n
    ]
    for cand in generated:
        if cand in names:
            return zf.read(cand), "GENERATED"
    fallback_prefixes = ("icons/original/", "icons/fallback/", "icons/native_monochrome/")
    for prefix in fallback_prefixes:
        for cand in (f"{prefix}{draw}.png", f"{prefix}{old}.png"):
            if cand in names:
                return zf.read(cand), "FALLBACK"
    return None, "MISSING"


def main() -> int:
    if len(sys.argv) < 2:
        print("usage: python3 tools/import_iconlab_export.py NothingIconLab-export.zip")
        return 2
    zip_path = Path(sys.argv[1]).expanduser().resolve()
    if not zip_path.is_file():
        print(f"zip not found: {zip_path}")
        return 1

    root = Path(__file__).resolve().parents[1]
    curated_png = root / "iconpack" / "curated" / "png"
    dest = root / "iconpack" / "src" / "main" / "res" / "drawable-nodpi"
    assets = root / "iconpack" / "src" / "main" / "assets"
    xml_dir = root / "iconpack" / "src" / "main" / "res" / "xml"
    dest.mkdir(parents=True, exist_ok=True)
    curated_png.mkdir(parents=True, exist_ok=True)

    zf = zipfile.ZipFile(zip_path)
    names = set(zf.namelist())
    rows = parse_apps(zf.read("apps.txt").decode("utf-8"))
    used: dict[str, str] = {}
    mappings: list[dict] = []
    counts = {"CURATED": 0, "GENERATED": 0, "FALLBACK": 0, "MISSING": 0}

    for row in rows:
        pkg, act = row["package"], row["activity"]
        draw = drawable_for(pkg, act, used)
        curated = curated_png / f"{draw}.png"
        if curated.is_file():
            dest.joinpath(f"{draw}.png").write_bytes(curated.read_bytes())
            source = "CURATED"
        else:
            data, source = pick_zip_png(zf, names, draw, pkg, act)
            if data is None:
                counts["MISSING"] += 1
                print(f"MISSING {pkg}/{act} drawable={draw}")
                continue
            dest.joinpath(f"{draw}.png").write_bytes(data)
        counts[source] += 1
        mappings.append(
            {
                "label": row["label"],
                "package": pkg,
                "activity": act,
                "component": f"ComponentInfo{{{pkg}/{act}}}",
                "drawable": draw,
                "source": source,
            }
        )

    write_xml(assets, xml_dir, mappings)
    report = {
        "scanned": len(rows),
        "written": len(mappings),
        "counts": counts,
        "icons": mappings,
    }
    report_path = root / "iconpack" / "curated" / "last-import-provenance.json"
    report_path.write_text(json.dumps(report, indent=2, ensure_ascii=False) + "\n")
    print(
        f"scanned={len(rows)} written={len(mappings)} "
        f"CURATED={counts['CURATED']} GENERATED={counts['GENERATED']} "
        f"FALLBACK={counts['FALLBACK']} MISSING={counts['MISSING']}"
    )
    print(f"provenance={report_path}")
    print("curated pngs were not modified")
    return 0 if mappings and counts["MISSING"] == 0 else 1


if __name__ == "__main__":
    raise SystemExit(main())
