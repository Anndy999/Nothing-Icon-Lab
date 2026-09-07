# 研究笔记 — Nothing 渲染器还原

Nothing Icon Lab **不**附带 Nothing 或 Nada 专有代码 / APK。渲染器根据以下材料重建：

1. AOSP Launcher3 `BaseIconFactory` / `ThemedIconDrawable` / `ClippedMonoDrawable`（Apache 2.0）
2. AOSP `AdaptiveIconDrawable.getExtraInsetFraction()` = 0.25
3. **Nothing Launcher 2.5.9** `classes.dex` + `classes2.dex` 实际方法（本地分析，不入库）
4. **Nothing Icon 1.0.1** 作为 OS themed-icon provider（不是 appfilter 包）

Nada 无题 **只**用来确认用户要的显示方向：白色圆形底板 + 黑色字形。它不参与 scale / inset / crop / forced mono / normalization / shape / pipeline。

分析对象（仅本地）：

- `com.nothing.launcher` 2.5.9（`classes.dex` AOSP 分叉 + `classes2.dex` Kotlin `n3/a` `o3/a` `o3/b`）
- `com.nothing.icon` 1.0.1

## 实际调用链（Launcher 2.5.9 字节码）

```
Themes / themed-icon enable
  THEMED_ICONS_NOTHING
  BaseIconFactory.isMonoIconEnabled()
    → Lo3/b.x()     // Nothing themed-icon master switch

BaseIconFactory.createBadgedIconBitmap(drawable, IconOptions)
  → normalizeAndWrapToAdaptiveIcon(drawable, shrinkNonAdaptive, bounds, scale[])
        AdaptiveIconDrawable:
          IconNormalizer.getScale → scale[0]
        非 Adaptive:
          wrap into AdaptiveIconDrawable wrapper + FixedScaleDrawable
  → createIconBitmap(drawable, scale[0], generationMode)
        mode 1/3/4 → ALPHA_8 画布
        否则 ARGB_8888 / hardware bitmap
        drawIconBitmap(canvas, drawable, scale, mode, bitmap)
  → if ATLEAST_T && isMonoIconEnabled():
        setNTMono(drawable, bitmapInfo, scale[], options)   // Nothing 优先
        if false → setMonoIcon(drawable, bitmapInfo, scale[]) // AOSP native fallback

setNTMono
  mConverter = o3/a IconGrayConverter
  converter.r(scale[0])          // 存 normalization scale → converter.h()
  converter.q(isBadForeground)
  n3/a.e(drawable, res, converter, factory, bitmapInfo.icon)
        ├─ AdaptiveIconDrawable.getMonochrome() 是 VectorDrawable 或 InsetDrawable
        │     → n3/a.g          // native vector-ish
        ├─ 否则若 getMonochrome() 非空
        │     → n3/a.h          // native BitmapDrawable-ish
        └─ 否则若 Lo3/b.s() force-render enable
              → n3/a.c          // createGeneralMono / forced
  若结果是 BitmapDrawable → 直接用其 bitmap
  否则 createIconBitmap(monoDrawable, scale, 1)
  BitmapInfo.setMonoIcon(bitmap, factory, typeInt)
        type 进入 mMonoFlag；isNTMono() 看 mMono != null && mMonoFlag

setMonoIcon（AOSP 回退，仅当 setNTMono 失败且 AID 自带 monochrome）
  ClippedMonoDrawable(mono, -getExtraInsetFraction())   // -0.25
  createIconBitmap(clipped, scale[0], 1)
  BitmapInfo.setMonoIcon(bitmap, factory)               // mMonoFlag = 1

BitmapInfo.newIcon(context, flags)
  若 mMono != null → ThemedIconDrawable.newDrawable(info, context)
  否则 FastBitmapDrawable

ThemedIconDrawable
  getColors(context) → [bg, fg]  via o3/a$a.a/b(context)
        （系统/主题色；本实验室显示层改成白底黑标）
  <init>:
        mMonoIcon = bitmapInfo.mMono
        mBgBitmap = bitmapInfo.mWhiteShadowLayer   // getWhiteShadowLayer()
        mMonoPaint SRC_IN colorFg
        mBgPaint   SRC_IN colorBg
  drawInternal(canvas, bounds):
        canvas.drawBitmap(mBgBitmap,  null, bounds, mBgPaint)
        canvas.drawBitmap(mMonoIcon,  null, bounds, mMonoPaint)
        // 两张图都铺满 bounds。0.3888889 已经在 mMonoIcon 像素里。
```

### n3/a 工厂（classes2.dex）

`n3/a.<clinit>`：

```
n3/a.a = extra / (1 + 2 * extra)
       = 0.25 / 1.5 = 1/6
```

`n3/a.a(Context, size…, icon, …, package, BaseIconFactory) → AdaptiveIconDrawable`  
另一条「产出 AdaptiveIconDrawable」路径（图标包 / drawable 包装），不是 `BitmapInfo.mMono` 主路径：

1. 若 `icon is ThemedIconDrawable` 且 `getMonoIcon() != null` → `BitmapDrawable(monoBitmap)`
2. 否则 `icon.mutate()`
3. `n3/a.f(package)`：`Lo3/b.g()` Function 包名覆盖
4. 若 `AdaptiveIconDrawable.getMonochrome() != null`：
   - BitmapDrawable → `n3/a.h`
   - 否则 `n3/a.j` → `n3/a.g`
5. 否则若 `Lo3/b.s()`（`nothing_icon_pack_force_render_enable`）→ `n3/a.d` → `n3/a.c`
6. 最后 `n3/a.b(mono, scale, context)` 包成 AdaptiveIconDrawable

`n3/a.b(mono, scale, Context)`：

```
inset = (1 - scale) / 2
ClippedMonoDrawable(mono, extra)
InsetDrawable(clipped, n3/a.a)          // 1/6
ThemedIconDrawable.getColors(context)
mono.setTint(colors[1])
return AdaptiveIconDrawable(ColorDrawable(colors[0]), tintedMono)
```

### Native monochrome

`n3/a.g(res, monochrome, converter)`（Vector / InsetDrawable）：

```
clipped = ClippedMonoDrawable(mono, -getExtraInsetFraction())   // -0.25
full    = y3/a.j(clipped, converter.e(), scale=1.0, ALPHA_8)
          y3/d.k / y3/d.a   // alpha 阵列处理（非二值化）
out     = y3/a.f(full, converter.e(), 0.3888889, converter.h(), ALPHA_8)
return BitmapDrawable(res, out)
```

`n3/a.h(monochrome, converter)`（BitmapDrawable）：

```
measured = y3/d.f(drawable)
out = y3/a.i(drawable, converter.e(), 0.3888889, converter.h(), ALPHA_8)
return Drawable
```

`n3/a.j` = 建 `o3/a(size)` 后转调 `g`。

**结论：** native 路径按整张 ClippedMono drawable 缩放，**不**做 bbox crop。`0.3888889 * converter.h()` 写进 ALPHA_8。`converter.h()` 是 `setNTMono` 写入的 normalization scale，Adaptive 图标通常 ≈ 1。

### Forced monochrome（createGeneralMono）

`n3/a.c` 日志字符串：`createGeneralMono failed, exception is`

```
pair = n3/a.i(drawable, bitmap, stripBg=false, factory, converter.e())
gray = converter.m(pair.second)          // IconGrayConverter
若 gray 失败或等于输入:
    pair = n3/a.i(..., stripBg=true)     // 再试「完整图标」
    gray = converter.m(fullIcon)
return BitmapDrawable(gray)
```

`n3/a.i`：

- Adaptive 且 background 是 `ColorDrawable` 且 color==0 时，去掉纯色/空背景，用 foreground 重建 AdaptiveIconDrawable
- 然后 `BaseIconFactory.createNormalizedBitmap(drawable, bitmap, flags)`
  - 非 Adaptive → `normalizeNonAdaptiveIcon`
  - Adaptive 且 bg 为透明 ColorDrawable → `ClearBlackPixelsDrawable` + 三参数 AdaptiveIconDrawable
  - `drawIconBitmap` 画到 `mIconBitmapSize` 方图

`o3/a.m(Bitmap)` IconGrayConverter：

```
要求宽高 == converter.a（icon bitmap size）
rect = o3/a.l(bitmap)     // content Rect；空则失败
out  = y3/a.h(bitmap, rect, size, 0.3888889, ALPHA_8)
```

`o3/a.l` → `o3/a.p`：从 alpha 阵列取 `Rect`（forced **内部** crop，不是全局 `cropToContent`）。

`Lo3/b.s()`：`x3/a.d() && u()`，对应 `nothing_icon_pack_force_render_enable`。

### ClippedMonoDrawable（classes.dex）

```
super InsetDrawable(drawable, extra)     // extra 由调用方传入
mCrop = AdaptiveIconDrawable(ColorDrawable(BLACK), null)
draw:
  mCrop.bounds = this.bounds
  clipPath(mCrop.iconMask)
  super.draw()
```

`setMonoIcon` / `n3/a.g` 传入的 extra 是 **`-getExtraInsetFraction()` = -0.25**，不是 1/6。负 inset 把 25% adaptive padding 展开，字形铺满 mask。

### 0.3888889 出现位置

IEEE-754 LE `72 1c c7 3e` 在 `classes2.dex` 三处，随后都是 invoke-virtual：

| 方法 | 作用 |
| --- | --- |
| `n3/a.g` | native vector 栅格化：`y3/a.f(..., 0.3888889, converter.h(), ALPHA_8)` |
| `n3/a.h` | native bitmap 栅格化：`y3/a.i(..., 0.3888889, converter.h(), ALPHA_8)` |
| `o3/a.m` | forced gray：`y3/a.h(bitmap, contentRect, size, 0.3888889, ALPHA_8)` |

这是写进 **mMonoIcon** 的缩放，不是 Nada 的 106/288 bbox，也不是再叠一层 inset。

`ThemedIconDrawable.drawInternal` 把这张已经缩小过的图铺满底板。本仓库把同一缩放留到 compose 时做，方便参数页调节，视觉等价。

## 已核实常量

| 常量 | 值 | 来源 |
| --- | --- | --- |
| Adaptive extra inset | `0.25` | `AdaptiveIconDrawable.getExtraInsetFraction()` |
| ClippedMono InsetDrawable | **`-0.25`** | `setMonoIcon` / `n3/a.g`：`neg-float` extra |
| n3/a.a wrap inset | `0.25/1.5 = 1/6` | `n3/a.<clinit>`，给 `n3/a.b` 用 |
| logoScale | `0.3888889` | `n3/a.g` / `n3/a.h` / `o3/a.m` |
| cropToContent（全局） | **关** | native 不 crop；forced 只在 `o3/a.l` 内部 |
| invert | 无全局二值化 | native 走 drawable alpha；forced 本仓库用 AOSP 边缘平均可选反相 |
| Alpha | 保留 | `y3/d` 处理 ALPHA_8，不是把边缘打成 255 |
| 底板形状 | `mWhiteShadowLayer` + SRC_IN | 本仓库简化为抗锯齿圆 |
| 底板 / 字形（显示） | `#FFFFFF` / `#000000` | 用户指定白底黑标；Nothing 原色来自 `ThemedIconDrawable.getColors` |

## 本仓库如何对应

```
真实 APK AdaptiveIconDrawable（尽量保持 Drawable，按调用方尺寸栅格化）
  ├─ getMonochrome()
  │     rasterizeClippedMono at *output* size, inset = -0.25
  │     → NATIVE_MONO
  ├─ else createNormalizedBitmap 风格 + AOSP MonochromeIconFactory
  │     work size 576（preview 可用 256）
  │     然后 o3/a.l 风格 content crop
  │     → FORCED_MONO
  └─ else grayscale of launcher icon → FALLBACK

字形
  → 默认不 alpha 二值化（threshold=0，低于阈值才变透明，其余保留原 alpha）
  → 全局 cropToContent=false
  → 缩放到 logoScale 0.3888889（等价于写进 mMonoIcon）
  → 居中放到白色圆形底板，黑色 SRC_IN
```

分辨率：

| 用途 | 尺寸 |
| --- | --- |
| Preview 缓存 | 128；forced 工作图 256 |
| 详情 | 512；forced 工作图 576 |
| Export | ≥512；forced 工作图 576；**重新渲染，不用 preview bitmap** |

Native 从 Drawable 在输出尺寸栅格化，不先压成 192。

## Nada 无题明确不参与

Nada 不得决定：

- logoScale（禁止 106/288 ≈ 0.368）
- adaptive inset / monochrome inset
- cropToContent 默认值
- forced monochrome 算法
- normalization / icon shape / rendering pipeline

它只提供显示方向：**白圆形底板 + 黑色字形**。
