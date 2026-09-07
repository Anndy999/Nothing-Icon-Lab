# 研究笔记 — Nothing 渲染器还原

Nothing Icon Lab **不**附带 Nothing 专有代码。渲染器是根据以下材料重建的：

1. AOSP Launcher3 `MonochromeIconFactory`（Apache 2.0）
2. AOSP `AdaptiveIconDrawable` extra-inset 计算
3. AOSP `ThemedIconDrawable` 的 themed-icon 内缩
4. Lawnicons 可启动应用扫描（`ACTION_MAIN` + `CATEGORY_LAUNCHER`）
5. Nothing OS 主题图标的公开外观（圆形底板 + 黑白字形）
6. **Nothing Launcher 2.5.9 / Nothing Icon 1.0.1 字节码核对**（v0.1.1，APK 仅用于本地研究，不入库）

## 已核实（AOSP）

| 常量 | 值 | 来源 |
| --- | --- | --- |
| Adaptive extra inset | `0.25` | `AdaptiveIconDrawable.getExtraInsetFraction()` |
| Viewport scale | `1 / (1 + 2 * 0.25) = 2/3` | AOSP |
| Themed mono inset | `0.25 / 1.5 = 1/6 ≈ 0.1667` | AOSP ThemedIconDrawable |
| Forced mono | 灰度 → RGB 均值作 alpha → 对比拉伸 → 可选边缘翻转 | AOSP MonochromeIconFactory |

## 已核实（Nothing Launcher 2.5.9）

本地对 `classes2.dex` 的扫描结果（IEEE-754 小端 `72 1c c7 3e` = `0.3888889f`）出现 **3 次**，随后都是 `invoke-virtual`，与 `createIconBitmap` 一类缩放一致。

| 参数 | 默认 | 状态 |
| --- | --- | --- |
| `logoScale` | `0.3888889`（7/18） | **已核实**：`classes2.dex` 偏移 2641702 / 2641862 / 2686772 |
| 圆形底板 | `backgroundSize = 1.0` | 与公开截图一致 |
| 浅色底板 / 字形 | `system_neutral1_50` / `system_neutral1_900` | 资源 `mono_nothing_background_color` / `foreground_color` 指向 `android.R.color`（默认约 `#F1F0F7` / `#1A1B20`，可随壁纸变） |
| 深色（night） | 两者对调 | 同资源的 night 配置 |
| `cropToContent` | **关** | `0.3888889` 缩放的是整张 drawable，而不是裁切后的内容框 |

资源中还能看到：

- 字符串：`THEMED_ICONS_NOTHING`、`themed_icons_nothing`、`nothing_icon_pack_force_render_enable`、`ThemedIconDrawable`、`forced_mono`、`isNTMono` / `setNTMono`、`createGeneralMono`
- 颜色：`mono_nothing_background_color`、`mono_nothing_foreground_color`、`icon_pack_nothing_icon_bg_color`
- XML：`grayscale_icon_map` / `nt_grayscale_icon_map` 存在，但解码后是空的 `<icons/>` 占位
- **没有**名为 `MonochromeIconFactory` 的 Nothing 类（强制单色走 AOSP 路径）

`com.nothing.icon` 1.0.1 是 OS feature 主题图标提供者（要求 `com.nothing.feature.OS.V2_0`），不是经典 appfilter 图标包。

## v0.1 管线

```
真实 APK 图标
  ├─ AdaptiveIconDrawable.monochrome  → NATIVE_MONO
  ├─ 否则 AOSP MonochromeIconFactory  → FORCED_MONO
  └─ 否则启动图标灰度                 → FALLBACK

字形
  → 可选 alpha 阈值
  → 可选内容裁切（默认关）或 adaptive + mono 内缩
  → 缩放到 logoScale * foregroundScale
  → 居中放到圆形底板
```

不要默认使用 Lawnicons SVG 或网上下载的品牌 Logo。
仓库中也不保留 Nothing 专有 APK。
