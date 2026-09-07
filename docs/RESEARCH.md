# 研究笔记 — Nothing 渲染器还原

Nothing Icon Lab **不**附带 Nothing 或 Nada 专有代码 / APK。渲染器根据以下材料重建：

1. AOSP Launcher3 `BaseIconFactory` / `ThemedIconDrawable` / `ClippedMonoDrawable`（Apache 2.0）
2. AOSP `AdaptiveIconDrawable.getExtraInsetFraction()` = 0.25
3. **Nothing Launcher 2.5.9** `classes.dex` + `classes2.dex` 实际方法（本地分析，不入库）
4. **Nothing Icon 1.0.1** 作为 OS themed-icon provider（不是 appfilter 包）

Nada 无题 **只**用来确认用户要的显示方向：白色圆形底板 + 黑色字形。它不参与 scale / inset / crop / forced mono / normalization / shape / pipeline。

## 调用链（Nothing Launcher 2.5.9）

```
Themes.isThemedIconEnabled()
  ← THEMED_ICONS_NOTHING / themed_icons_nothing

n3/a.a(Context, size…, Drawable icon, …, BaseIconFactory)
  ├─ icon is ThemedIconDrawable → getMonoIcon()
  ├─ icon is AdaptiveIconDrawable → getMonochrome()     // native
  ├─ n3/a.f(package) true → n3/a.h(mono, IconGrayConverter)  // package override
  └─ Lo3/b.s() force-render
        ├─ n3/a.j → n3/a.g(native drawable, converter)
        └─ n3/a.d → n3/a.c  // log: "createGeneralMono failed, exception is"

n3/a.g / n3/a.h
  ├─ ClippedMonoDrawable(mono, AdaptiveIconDrawable.getExtraInsetFraction())
  ├─ rasterize ALPHA_8 at converter size, scale 1.0
  └─ rasterize ALPHA_8 at converter size, scale 0.3888889 * converter.h()

n3/a.c  createGeneralMono
  ├─ n3/a.i → may strip ColorDrawable background, then
  │     BaseIconFactory.createNormalizedBitmap(drawable, bitmap, flags)
  └─ o3/a.m(Bitmap)  // IconGrayConverter, class log tag IconGrayConverter
        ├─ requires square bitmap of converter size
        ├─ o3/a.l → content Rect (forced path only)
        └─ scale const 0.3888889f onto ALPHA_8

n3/a.b(mono, scale, Context) → AdaptiveIconDrawable
  ├─ ClippedMonoDrawable(mono, extraInset)
  ├─ InsetDrawable(clipped, n3/a.a)   // n3/a.a = extra/(1+2*extra) = 1/6
  ├─ ThemedIconDrawable.getColors(context) → tint glyph
  └─ AdaptiveIconDrawable(ColorDrawable(bg), tintedMono)

ThemedIconDrawable.drawInternal
  ├─ fill circular/path plate with bg color
  └─ draw mono with fg color filter

BitmapInfo.isNTMono / BaseIconFactory.setNTMono
  marks NT-generated mono vs generic themed icons

nothing_icon_pack_force_render_enable
  Lo3/b.s() / NothingIconForceRenderUpdateTask
```

`0.3888889f`（IEEE-754 `72 1c c7 3e`）出现在 `classes2.dex`：

| 方法 | 作用 |
| --- | --- |
| `n3/a.g` | native mono 栅格化 scale |
| `n3/a.h` | 另一条 mono 栅格化 scale |
| `o3/a.m` | forced `IconGrayConverter` scale |

这是 **createIconBitmap 风格的整图缩放**，不是把字形 bbox 裁成 106/288。

## 已核实常量

| 常量 | 值 | 来源 |
| --- | --- | --- |
| Adaptive extra inset | `0.25` | `AdaptiveIconDrawable.getExtraInsetFraction()`；`n3/a.<clinit>` 读取 |
| Themed / ClippedMono inset | `0.25/1.5 = 1/6` | `n3/a.<clinit>` 写入静态字段 `n3/a.a`；`InsetDrawable` |
| logoScale | `0.3888889` | 上表三个方法 |
| cropToContent | **关**（默认） | native 路径按整张 drawable 缩放；forced 的 `o3/a.l` Rect 只在 gray converter 内部 |
| Forced work size (lab) | **576** | 本仓库；Nothing 用 `BaseIconFactory` 的 icon bitmap size |
| Export size | **≥512** | 本仓库；preview 缓存不得用于 export |
| 底板 / 字形（显示） | `#FFFFFF` / `#000000` | 用户指定白底黑标；不是 Nada 几何 |

## 本仓库如何对应

```
真实 APK AdaptiveIconDrawable
  ├─ getMonochrome()     → NATIVE_MONO   rasterizeClippedMono at output size
  ├─ else createNormalizedBitmap + AOSP MonochromeIconFactory → FORCED_MONO at 576
  └─ else grayscale of launcher icon → FALLBACK

字形（保留 smooth alpha）
  → 不默认 alpha 二值化
  → 不默认 cropToContent
  → 缩放到 logoScale 0.3888889
  → 居中放到白色圆形底板，黑色 SRC_IN
```

Preview 用 128px 缓存。详情 / 导出从 Drawable 或 576 forced 源 **重新渲染**，不用 preview bitmap。
