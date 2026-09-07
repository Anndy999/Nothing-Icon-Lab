# Nothing 图标实验室

研究、预览、调节并导出 **Nothing 风格** 单色图标 — 不替换三星 One UI 桌面。

这是一个侧载的 Android 13+ 工具。它 **不是** 启动器，也 **不会** 修改 Theme Park、One UI Home 或任何系统应用。

**Nothing Icon Lab 与 Nothing Technology Limited 没有任何关联。**
**Nothing 与 Nothing OS 是其权利人的商标。**

## 为什么做这个

Theme Park 的循环太慢：

> 改几个图标 → 编译 APK → 安装 → Theme Park → 应用 → 截图 → 再改

这个应用把循环放到 **设备内部**：

1. 扫描所有可启动应用
2. 读取 **真实** APK 图标（`packageName` + 启动 Activity + Adaptive Icon + `monochrome`）
3. 优先官方 Android 13+ 单色；否则走 AOSP `MonochromeIconFactory`
4. 套用还原的 Nothing 底板（圆形背景 + 居中字形）
5. 实时调参，网格立刻更新
6. 导出面向 Theme Park 的测试包（`appfilter.xml` + PNG）

默认视觉是 **白底黑标**（Nada 无题已适配图标的字形占比，颜色取反）。参数页可切回黑底白标，也可还原 Nothing Launcher 2.5.9 常量。Lawnicons 只当基础设施（扫描 / Adaptive Icon / appfilter 思路）。

## 当前状态

**v0.1.2** — 默认白底黑标（圆形 `#F1F1F1` 底板 + `#1B1B1B` 字形，字形约占 0.37）；中文界面。

| 功能 | 状态 |
| --- | --- |
| 扫描可启动应用 | 有 |
| 官方单色 | 有 |
| 强制单色（AOSP） | 有 |
| Nothing 风格渲染 + 实时参数 | 有 |
| 网格 / 列表 / 详情 | 有 |
| 导出 ZIP | 有 |
| 中文界面 | 有（英语走 `values-en`） |
| 按应用覆盖 | v0.2 |
| Theme Park 工程 | v0.3 |
| 自动 Theme Park APK | v0.4 |

## 安装

1. 打开 [GitHub Releases](https://github.com/Anndy999/Nothing-Icon-Lab/releases)。
2. 下载 `Nothing-Icon-Lab-vX.Y.Z.apk`。
3. 侧载。允许 `QUERY_ALL_PACKAGES`，扫描器才能看到全部应用。

在配置发布签名 secrets 之前，APK 可能是 **debug 签名**。发布说明里会写明。不要把它当成稳定升级签名。

## 支持的 Android 版本

- 最低：**Android 13 (API 33)**
- 目标：**Android 15 (API 35)**
- 原因：`AdaptiveIconDrawable.monochrome` 与主题图标研究

## 导出格式

应用内点 **导出**，分享 `NothingIconLab-export.zip`。

```
NothingIconLab-export.zip
├── appfilter.xml
├── apps.txt
├── config.json
└── icons/
    ├── final/
    ├── native_monochrome/
    └── forced_monochrome/
```

`appfilter.xml` 使用：

```xml
<item component="ComponentInfo{package/activity}" drawable="name" />
```

这是 Theme Park / 第三方图标包需要的映射。v0.1 还 **不会** 生成完整 Theme Park APK。

## 如何构建

```bash
export ANDROID_HOME=/path/to/Android/Sdk
./gradlew testDebugUnitTest
./gradlew assembleRelease
```

APK 在 `app/build/outputs/apk/`。

### GitHub Actions

推送到 `main` 或推送 `v*` 标签：

- Checkout、Java 17、Gradle cache
- 单元测试 + lint
- Release APK
- 上传 artifact
- `v*` 标签：GitHub Release + 上传 APK

### 发布签名 secrets（不进仓库）

| Secret | 用途 |
| --- | --- |
| `ANDROID_KEYSTORE_BASE64` | Base64 编码的 `.jks` / `.keystore` |
| `ANDROID_KEYSTORE_PASSWORD` | Keystore 密码 |
| `ANDROID_KEY_ALIAS` | 密钥别名 |
| `ANDROID_KEY_PASSWORD` | 密钥密码 |

缺失时 CI 仍会构建并在打标签时发布 Release，但 APK 是 **debug 签名**。配置一次这四个 secrets 后，后续版本才能在手机上覆盖安装。

## 路线图

- **v0.1** 扫描、预览、官方/强制单色、Nothing 渲染、实时参数、ZIP 导出
- **v0.2** 较差结果工作流、按应用覆盖（例如 Chrome scale = 0.39）
- **v0.3** 三星 Theme Park 图标包工程输出
- **v0.4** 自动生成 APK，并监视新安装的应用

## 开发原则

- 从 Chrome 的 APK 读 Chrome 图标。不要用网上下载的 Logo 替换。
- 优先 AOSP / Apache 2.0 / Lawnicons 基础设施。不要复制 Nothing 闭源。
- 架构保持小、可调试。记录 `IconSource` 和渲染参数。
- 不 root、不改 One UI Home、不改 Theme Park。
- 不把 Nothing / Nada 专有 APK 提交进仓库。

## License

Apache License 2.0。见 [LICENSE](LICENSE) 和 [NOTICE](NOTICE)。
研究笔记：[docs/RESEARCH.md](docs/RESEARCH.md)。
