# Focal (聚点)

> **Slogan:** *Your interest, the world's Focal.*

零服务端、纯端侧聚合（Client-Side Aggregation）、高审美极简图标化交互的 Android 应用。

---

## 一、 核心痛点与解法
1. **解决痛点**:
   - 告别在多个独立 App 之间反复横跳切换。
   - 告别各平台频繁扫码与繁琐账户登录管理。
   - 打破算法茧房，真正由用户自主掌控信息流与喜好。
2. **核心业务板块 (极简单字意象与纯图标化导航)**:
   - **阅** (News & RSS): 国内外热搜榜单与深度热点聚合。
   - **视** (Watch): 抖音竖屏短视频与 Bilibili 横屏长视频无缝流式浏览。
   - **听** (Listen): 经典有声书与精选播客，支持 Media3 常驻后台与锁屏/通知栏胶囊控制。
   - **问** (Ask): 豆包、Kimi、通义千问、腾讯元宝一键同问，多模态实时分屏/卡片对比。
   - **设** (Settings): 侧边手势/图标呼出的独立抽屉，不挤占主界面空间；支持源开关与身份对接持久化。

---

## 二、 核心架构亮点

### 1. 极致图标化设计语言 (Icon-Driven UI)
- 贯彻**东方美学与新中式深空留白**（浓墨、玄灰、竹青、月白），界面去掉所有臃肿冗余的标题修饰。
- 主导航与操作全面采用精准直观的矢量微标（Vector Icons），能用图标标识的地方绝不出现冗余描述。

### 2. 侧边独立展开的设置抽屉 (Slide-out Drawer)
- 设置模块完全从主屏幕解耦，常态下彻底隐藏，最大化呈现内容本身。
- 用户点击顶栏菜单或左滑即可唤起，即用即走。

### 3. AI 身份对接持久化工具 (Auth Bridge)
- 提问直接可用，彻底告别开发者 API Key 的繁琐申请。
- 提供内置隔离 WebView 身份桥：用户登录后，自动捕获 Session/Cookie，使用 Android Jetpack 硬件级 `EncryptedSharedPreferences` (AES-256-GCM) 持久化于本地。
- 支持并发协程向选中的多个 AI 模型发起流式问答，并排呈现各家回答差异，方便比对决策。

---

## 三、 工程结构
```
focal-android/
├── app/
│   ├── src/main/
│   │   ├── AndroidManifest.xml       # 声明前台音频播放服务与网络权限
│   │   └── java/com/focal/app/
│   │       ├── MainActivity.kt        # 纯图标单 Activity 骨架与侧边抽屉托管
│   │       ├── core/
│   │       │   ├── navigation/        # 阅/视/听/问 纯图标意象枚举
│   │       │   ├── storage/           # 硬件加密身份持久化与源偏好
│   │       │   └── theme/             # 东方美学水墨色系与主题
│   │       ├── feature_read/          # 「阅」资讯聚合流
│   │       ├── feature_watch/         # 「视」长短视频卡片流
│   │       ├── feature_listen/        # 「听」后台常驻音频服务与胶囊播放器
│   │       ├── feature_ask/           # 「问」多AI同问对比引擎
│   │       └── feature_set/           # 「设」侧边抽屉与 AuthBridge 身份对接沙箱
│   └── build.gradle.kts
├── settings.gradle.kts
└── local.properties
```
