# Changelog

## v0.1.2

### Changed

- 默认改成 **白底黑标**：圆形底板 `#F1F1F1`、字形 `#1B1B1B`（Nada 无题的几何实测，颜色取反；不复制其画稿）。
- `logoScale` 默认 `106/288 ≈ 0.368`，并打开 `cropToContent`，让字形占比接近 Nada 成品，而不是把 0.3888889 再叠一层内缩。
- 默认关掉系统中性色，adaptive / mono 内缩归零（仍可在参数页打开）。「深色预览」可切回黑底白标。
- 升级后忽略 v0.1.1 存下来的旧参数，避免继续用那套过小的默认值。

### Fixed

- v0.1.1 网格里图标又灰又小，和参考图标包差得很远。

### Known Issues

- GitHub Release 签名 secrets 仍未配置；APK 在配置前仍是 debug 签名。
- 尚无 Theme Park APK 生成器（计划 v0.3 / v0.4）。
- 照片/多色图标的强制单色质量较差（预期行为；覆盖规则在 v0.2）。
- Nada 等第三方图标包的画稿不会打进本应用。

## v0.1.1

### Added

- 界面默认中文（系统语言为英语时使用 `values-en`）。
- 参数页增加「使用系统中性色」开关，对应 Nothing 的 `system_neutral1_50` / `900`。

### Changed

- `logoScale = 0.3888889` 从假设改为 **Nothing Launcher 2.5.9 字节码核实**。
- 默认 `cropToContent = false`，使 0.3888889 缩放整张 drawable（与 `createIconBitmap` 一致）。
- 默认底板/字形颜色改为系统中性色（#F1F0F7 / #1A1B20 框架默认值，可随壁纸变化）。
- 应用图标改为整数对齐的矢量块状 N + 红色方块，并补齐 mdpi–xxxhdpi mipmap。
- 研究笔记改为已核实结论；仓库不再保留 Nothing 专有 APK。

### Fixed

- 启动器图标发糊：原先前景矢量过细、红色标记只有约 4vp，在桌面密度下几乎糊成一点。
- 界面文案不再硬编码英文。

### Known Issues

- GitHub Release 签名 secrets 仍未配置；APK 在配置前仍是 debug 签名。
- 尚无 Theme Park APK 生成器（计划 v0.3 / v0.4）。
- 照片/多色图标的强制单色质量较差（预期行为；覆盖规则在 v0.2）。
- 改参数会重绘整个网格，应用很多时可能卡顿。
- 需要 `QUERY_ALL_PACKAGES`；不以 Play 上架为目标。

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
