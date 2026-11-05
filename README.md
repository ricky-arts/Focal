# Focal (聚点)

> **Slogan:** *Your interest, the world's Focal.*

零服务端、纯端侧聚合（Client-Side Aggregation）、Google Material 3 现代规范交互、**全原生排版渲染（彻底移除 WebView）**的 Android 应用。

---

## 一、 核心痛点与解法
1. **解决痛点**:
   - 告别在多个独立 App 之间反复横跳切换。
   - 彻底摒弃内置 WebView 浏览器模式，所有内容（图文、视频、图集）走 App 内原生 Compose 渲染。
   - 自动清洗网页中恶心的牛皮癣广告、营销推广、悬浮弹窗与无关推荐，只保留纯净主体内容。
   - 开放自定义上传本地相册图片作为 App 沉浸式背景壁纸，支持透明度与对比度实时微调。

2. **多形态内容全原生渲染**:
   - 📰 **图文资讯 (Native Article)**：行间距舒适、字体大小 A-/A+ 无级可调、正文高清大图点击弹窗预览，跟随全局主题自适应。
   - 🎬 **原生视频 (Native Video)**：内置 AndroidX Media3 ExoPlayer 原生控件，支持播放/暂停、滑动条进度拖动、静音切换、切后台自动暂停。
   - 🖼️ **原生图集 (Native Gallery)**：原生 `HorizontalPager` 左右丝滑翻页预览、右上角页码指示、双指无级缩放手势。
   - ⭐ **统一原生操作**：全屏详情页均提供原生收藏持久化、系统级标准分享 (`Intent.ACTION_SEND`)、一键正文复制。

---

## 二、 UI/UX 架构亮点 (Material 3 体系)

### 1. 干净简约高级的 M3 设计语言
- **动态色彩体系 (ColorScheme)**：完全拒绝硬编码 `#` 开头色值，严格从 `MaterialTheme.colorScheme` 读取，支持应用内主题自适应与切换。
- **官方规范矢量图标**：统一标准尺寸 24dp，线条粗细一致，跟随主题动态自适应变色。
- **全局圆角规范**：卡片统一圆角 12dp，常规按钮 8dp，小标签 4dp，胶囊 20dp，规整一致。
- **柔和轻阴影**：卡片 elevation 控制在 1~2dp，轻盈透光，无生硬发黑阴影。
- **间距与留白**：固定 16dp 安全边距，8dp/16dp 栅格模数对齐，留白充足无错位。
- **文字层级 (Roboto)**：大标题 18sp 加粗，正文 14sp，辅助说明文字 12sp，清晰易读。

### 2. 沉浸式壁纸与滤镜工坊 (自定义相册图片上传支持)
- 在侧边抽屉中内置画境配置：
  * 支持壁纸预设切换（竹影、水乡、落日、纯粹等）；
  * **支持用户选择手机相册图片**作为专属背景壁纸；
  * **透明度滑块 (0.1 ~ 1.0)**：动态控制背景透光度，卡片背后的意境自然漫透；
  * **对比度滑块 (0.5 ~ 1.5)**：通过底层 `ColorMatrix` 进行动态对比度调节；
  * 配置持久化保存，冷启动无缝还原。

---

## 三、 工程结构
```
focal-android/
├── app/
│   ├── src/main/
│   │   ├── AndroidManifest.xml       # 声明前台音频播放服务与网络权限
│   │   └── java/com/focal/app/
│   │       ├── MainActivity.kt        # M3 主容器与动态壁纸/对比度滤镜容器
│   │       ├── core/
│   │       │   ├── content/           # 结构化数据契约与原生内容清洗提取引擎
│   │       │   ├── navigation/        # 阅/视/听/问 纯图标意象枚举
│   │       │   ├── network/           # 统一端侧 OkHttp 网络客户端
│   │       │   ├── storage/           # 偏好配置、自定义壁纸存储与硬件加密身份
│   │       │   └── theme/             # M3 ColorScheme、Dimens度量与Wallpaper容器
│   │       ├── feature_read/          # 「阅」全网热度排行榜与缩略图卡片
│   │       ├── feature_watch/         # 「视」16:9卡片视频流 (抖音 + B站)
│   │       ├── feature_listen/        # 「听」唱片质感后台常驻音频流与胶囊控制
│   │       ├── feature_ask/           # 「问」多AI同问对比引擎
│   │       ├── feature_detail/        # 原生图文阅读器、原生视频播放器、原生图集画廊
│   │       └── feature_set/           # 「设」侧边抽屉设置与壁纸微调面板
│   └── build.gradle.kts
├── settings.gradle.kts
└── local.properties
```
