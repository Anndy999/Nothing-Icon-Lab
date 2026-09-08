#!/usr/bin/env python
# -*- coding: utf-8 -*-
"""
adapt_new_app.py — 一键把刚下载的 app 适配成 Nothing 风格 (白底黑标)

用法:
  # 方式1: 给包名 (自动从手机拉 APK 并提取图标)
  python adapt_new_app.py com.example.app

  # 方式2: 给本地 APK 路径
  python adapt_new_app.py --apk C:/path/to/app.apk

流程: 拉 APK -> apktool 提取真实图标 -> 生成 Nothing 图标 -> 字节级替换进主题包
      -> 加 appfilter 映射 -> 签名 -> 安装到手机
完成后去三星主题公园重新导入即可。

依赖: Python (Pillow/numpy/svglib/reportlab/pycairo/rlPyCairo/scipy)、
      apktool.jar、uber-apk-signer.jar、adb
"""
import os
import re
import subprocess
import sys
import zipfile
import hashlib

import numpy as np

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
import nada_adapter as NA
from extract_real_icons import compose_icon  # 复用 apktool 图标提取

ADB = r"C:/Users/77297/AppData/Local/Microsoft/WinGet/Packages/Google.PlatformTools_Microsoft.Winget.Source_8wekyb3d8bbwe/platform-tools/adb"
APKTOOL = "apktool.jar"
SIGNER = "uber-apk-signer.jar"
SKELETON = "Nada_adapted/Nada_adapted-aligned-debugSigned.apk"  # 当前已签名的适配包作骨架
OUT_APK = "Nada_adapted/Nada_adapted_new.apk"


def get_apk_path(pkg):
    out = subprocess.run([ADB, "shell", "pm", "path", pkg], capture_output=True, text=True).stdout
    m = re.search(r"package:(/data/.*?base\.apk)", out)
    if not m:
        sys.exit(f"未找到包 {pkg}")
    return m.group(1)


def get_component(pkg):
    """从手机查 launcher 组件, 转 ComponentInfo{...}"""
    out = subprocess.run(
        [ADB, "shell", "cmd", "package", "query-activities", "--brief",
         "-a", "android.intent.action.MAIN", "-c", "android.intent.category.LAUNCHER"],
        capture_output=True, text=True).stdout
    for line in out.splitlines():
        line = line.strip()
        if line.startswith(pkg + "/"):
            p, act = line.split("/", 1)
            full = p + act if act.startswith(".") else act
            return f"ComponentInfo{{{p}/{full}}}"
    return None


def extract_icon(apk_path, pkg):
    """apktool 解码 + compose_icon 提取真实图标"""
    dec = f"dec_cache/_new_{pkg}"
    subprocess.run(["java", "-jar", APKTOOL, "d", "-s", "-f", "-o", dec, apk_path],
                   check=True, capture_output=True)
    im, how = compose_icon(dec)
    if im is None:
        sys.exit(f"提取图标失败: {how}")
    out_png = f"extracted_icons/_new_{pkg}.png"
    im.save(out_png)
    print(f"  [{how}] 图标 -> {out_png}")
    return out_png


def main():
    args = [a for a in sys.argv[1:] if not a.startswith("--")]
    apk_arg = None
    if "--apk" in sys.argv:
        apk_arg = sys.argv[sys.argv.index("--apk") + 1]

    if apk_arg:
        apk_path = apk_arg
        pkg = None
        # 从 manifest 猜包名太麻烦, 直接让用户给组件
        comp = input("请输入 appfilter 组件 ComponentInfo{pkg/activity}: ").strip()
        if not comp:
            comp = None
    else:
        pkg = args[0] if args else sys.exit("请提供包名: python adapt_new_app.py com.example.app")
        apk_path = get_apk_path(pkg)
        comp = get_component(pkg)
        print(f"包 {pkg} -> APK {apk_path}")

    print(f"组件: {comp or '(未指定)'}")
    print("1. 提取图标...")
    icon = extract_icon(apk_path, pkg or "app")

    print("2. 生成 Nothing 图标...")
    new_png_bytes = NA.make_nothing_png(icon)
    prev = f"preview_new_{pkg or 'app'}.png"
    with open(prev, "wb") as f:
        f.write(new_png_bytes)
    print(f"   预览: {prev}")

    print("3. 定位空闲槽位...")
    free = NA.list_free_slots()
    # 优先用还没被占用的槽位 (读 slot_map 记录)
    used = set()
    sm = "Nada_adapted/slot_map.txt"
    if os.path.exists(sm):
        for line in open(sm, encoding="utf-8"):
            parts = line.split("\t")
            if len(parts) >= 2:
                used.add(parts[1])
    free = [s for s in free if s not in used]
    if not free:
        sys.exit("没有空闲槽位了")
    slot = free[0]
    print(f"   使用槽位: {slot}")

    # 4. 字节级替换 + 加映射
    print("4. 构建新 APK...")
    z, hash_to_path = NA.index_apk(NA.ORIG)
    apk_path_in = NA.drawable_to_apk_path(slot, hash_to_path)
    if apk_path_in is None:
        sys.exit(f"槽位 {slot} 定位失败")

    with open(NA.DECODED_APPFILTER, encoding="utf-8") as f:
        af = f.read()
    if comp and comp not in af:
        af = af.replace("</resources>",
                        f'    <item component="{comp}" drawable="{slot}" />\n</resources>')

    with zipfile.ZipFile(SKELETON, "r") as zin, zipfile.ZipFile(OUT_APK, "w") as zout:
        for item in zin.infolist():
            if item.filename == apk_path_in:
                zi = zipfile.ZipInfo(item.filename, item.date_time)
                zi.compress_type, zi.external_attr, zi.create_system = (
                    item.compress_type, item.external_attr, item.create_system)
                zout.writestr(zi, new_png_bytes)
            elif item.filename == NA.APKFILTER_PATH_IN_APK:
                zi = zipfile.ZipInfo(item.filename, item.date_time)
                zi.compress_type, zi.external_attr, zi.create_system = (
                    item.compress_type, item.external_attr, item.create_system)
                zout.writestr(zi, af.encode("utf-8"))
            else:
                zout.writestr(item, zin.read(item.filename))
    print(f"   输出: {OUT_APK}")

    # 5. 签名 + 安装
    print("5. 签名...")
    subprocess.run(["java", "-jar", SIGNER, "-a", OUT_APK, "--allowResign"],
                   check=True, capture_output=True)
    signed = OUT_APK.replace(".apk", "-aligned-debugSigned.apk")
    print(f"6. 安装 {signed} ...")
    r = subprocess.run([ADB, "install", "-r", signed], capture_output=True, text=True)
    print(r.stdout.strip() or r.stderr.strip())
    print("\n完成! 去三星主题公园重新导入一次即可。")


if __name__ == "__main__":
    main()
