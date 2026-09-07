# 研究笔记 — Nothing 渲染器还原

Nothing Icon Lab **不**附带 Nothing 专有代码。渲染器是根据以下材料重建的：

1. AOSP Launcher3 `MonochromeIconFactory`（Apache 2.0）
2. AOSP `AdaptiveIconDrawable` extra-inset 计算
3. AOSP `ThemedIconDrawable` 的 themed-icon 内缩
4. Lawnicons 可启动应用扫描（`ACTION_MAIN` + `CATEGORY_LAUNCHER`）
5. Nothing OS 主题图标的公开外观（圆形底板 + 黑白字形）
6. **Nothing Launcher 2.5.9 / Nothing Icon 1.0.1 字节码核对**（v0.1.1，APK 仅用于本地研究，不入库）
7. **Nada 无题**（`com.panpandada.nada.pay` 16.0 / 260719）已适配图标的几何与颜色实测（v0.1.2，APK 仅用于本地研究，**不复制其画稿，不入库**）

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

## 已核实（Nada 无题 16.0，v0.1.2 视觉默认）

v0.1.1 把 Nothing 的 `0.3888889` 叠在 adaptive 0.25 + mono 1/6 内缩上，字形被裁得很小，看起来不像成品图标包。Nada 无题里已经画好的图标（Chrome / 微信 / 支付宝 / QQ 等，288px）实测：

| 参数 | 值 | 说明 |
| --- | --- | --- |
| 画布 | 288×288，四角全透明 | 圆形底板 |
| 底板 | `#1B1B1B`（rgb 27,27,27） | 40 张抽样全部相同 |
| 字形 | `#F1F1F1`（rgb 241,241,241） | Nada 本体是黑底白标 |
| 裁切后字形 bbox | 中位 **106/288 ≈ 0.368**（范围约 0.33–0.39） | 用作 `logoScale` + `cropToContent` |
| appfilter `<scale>` | **0.44** | 只用于未适配的彩色原图标，不是已画好图标的字形占比 |
| iconback 中心白孔 | 106×106（0.368） | 与已适配字形同尺寸 |
| iconupon | 全透明 | 无叠加层 |

**不把 Nada 的 PNG / appfilter 画稿打进本仓库。** 只吸收圆形底板和字形占比。v0.1.2 默认把颜色取反成 **白底黑标**（`#F1F1F1` 底板 / `#1B1B1B` 字形）；参数页的「深色预览」可切回 Nada 同款黑底白标。Nothing 的 `0.3888889` 仍可手动拨回去。

## v0.1 管线

```
真实 APK 图标
  ├─ AdaptiveIconDrawable.monochrome  → NATIVE_MONO
  ├─ 否则 AOSP MonochromeIconFactory  → FORCED_MONO
  └─ 否则启动图标灰度                 → FALLBACK

字形
  → 可选 alpha 阈值
  → 默认裁切到内容（v0.1.2），不再叠 adaptive+mono 内缩
  → 缩放到 logoScale * foregroundScale（默认 106/288）
  → 居中放到圆形白底板（默认） / 炭黑板（深色预览）
```

不要默认使用 Lawnicons SVG 或网上下载的品牌 Logo。
仓库中也不保留 Nothing / Nada 专有 APK。
