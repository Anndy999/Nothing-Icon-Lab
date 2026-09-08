#!/usr/bin/env python3
from __future__ import annotations

import re

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


def drawable_for(package: str, activity: str, used: dict[str, str] | None = None) -> str:
    draw = PACKAGE_DRAWABLE.get(package)
    if not draw:
        draw = sanitize(f"{package}_{activity.rsplit('.', 1)[-1]}")
    used = used if used is not None else {}
    key = f"{package}/{activity}"
    if draw in used and used[draw] != key:
        base = draw
        n = 2
        while draw in used and used[draw] != key:
            draw = f"{base}_{n}"
            n += 1
    used[draw] = key
    return draw
