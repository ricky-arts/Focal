# Focal (焦点)

> **Slogan:** *Your interest, the world's Focal. 任何你关注的都是焦点。*

专注于全平台资讯与热榜聚合的高性能生产级 Android 原生应用，配套生产级 BFF 数据聚合网关与全文去广告清洗引擎。

---

## 一、 系统架构设计

```
focal-platform/
├── focal-server/                         # [BFF 数据聚合与清洗网关]
│   ├── package.json
│   ├── src/
│   │   ├── index.ts                      # 服务入口与统一 API 暴露
│   │   ├── types/schema.ts               # AST 结构化正文与评论数据契约
│   │   ├── engine/
│   │   │   ├── rule-engine.ts            # Hot-Fix 动态热修规则引擎 (免发版更新选择器)
│   │   │   ├── ast-converter.ts          # Cheerio 广告清洗并转化为纯原生 AST
│   │   │   └── user-agent-pool.ts        # 反爬防封随机 UA 池与重试引擎
│   │   ├── cache/redis.ts                # Redis 多级缓存与分布式互斥锁
│   │   ├── cron/hotlist-poller.ts        # Cron 后台静默轮询 (热榜对原站 0 实时穿透)
│   │   ├── proxy/image-proxy.ts          # 防盗链图片流代理 (防 403 裂图)
│   │   └── adapters/                     # 插件化多源适配器
│   │       ├── zhihu.adapter.ts          # 知乎热榜 + 回答 AST + 热评
│   │       ├── bilibili.adapter.ts       # B站热门 + DASH 音视频轨 + 楼中楼评论
│   │       ├── weibo.adapter.ts          # 微博热搜 + 话题 AST + 互动
│   │       ├── ithome.adapter.ts         # IT之家新闻 + 极客图文
│   │       ├── kr36.adapter.ts           # 36氪创投快讯
│   │       └── sspai.adapter.ts          # 少数派效率专栏
│   └── tests/server.test.ts              # 自动化核心单元测试
│
└── focal-android/                        # [Android 客户端 (纯原生 Jetpack Compose)]
    ├── app/src/main/java/com/focal/app/
    │   ├── MainActivity.kt               # M3 沉浸式单容器
    │   ├── core/
    │   │   ├── model/UnifiedModels.kt    # AST 结构化节点与强类型实体
    │   │   ├── network/                  # FocalApiClient 与 OkHttp + Coil 双重缓存
    │   │   ├── storage/db/               # Room Database 本地离线正文缓存
    │   │   ├── storage/AccountHubStore.kt# 账号绑定中心与硬件加密存储
    │   │   └── ui/UnifiedMediaCard.kt    # 统一左图右文卡片组件
    │   ├── feature_renderer/             # 原生渲染引擎
    │   │   ├── NativeArticleRenderer.kt  # 1.6~1.8x 黄金行距与无级字体缩放
    │   │   ├── NativeDashPlayerView.kt   # B站 DASH 音视频独立双轨并发合成播放器
    │   │   └── PreFetchManager.kt        # 列表滑动后台静默预热 (秒开率 100%)
    │   ├── feature_comment/              # 原生评论交互
    │   │   └── CommentBottomSheet.kt     # 半屏精彩热评抽屉 + 楼中楼回复
    │   └── feature_set/                  # 频道定制与背景画境微调
```

---

## 二、 5 大核心技术亮点

1. **BFF 智能多级缓存与防刷限流 (Rate Limiting)**：
   - 热榜数据由后台 Cron Job 每 3 分钟自动轮询写入 Redis，用户查询直接读缓存，**对源站 0 实时穿透**；
   - 详情接口采用 Lazy-Cache（缓存 2 小时）与分布式互斥锁防止高并发击穿；
   - 提供 IP 滑动窗口限流中间件，拒绝恶意爬取与刷量。
2. **动态规则与热修复引擎 (Hot-Fix Rules)**：
   - 网页 class 改动或广告结构变异时，后台秒级下发新选择器 JSON，零停机更新，彻底告别频繁发版。
3. **客户端离线优先与智能预加载 (Pre-fetching)**：
   - 停留在热榜时，`PreFetchManager` 异步在后台预加载前 8 篇正文入本地 Room DB，并通知 Coil 预下载首图；
   - 用户点击瞬间 **0 毫秒即刻渲染**，且完全支持弱网/地铁断网离线阅读。
4. **B站音视频分离 (DASH) 原生并发合成播放**：
   - 服务端提取 1080P/720P 独立 `video.m4s` 与 `audio.m4s`；
   - 客户端基于 Media3 `MergingMediaSource` 硬件级解耦并发实时合成播放，彻底解决无声痛点与 480P 模糊限制。
5. **多平台登录态账号绑定中心 (Account Hub)**：
   - 抽离出独立的授权沙箱，拦截提取用户 Cookie 并通过 Android Jetpack `EncryptedSharedPreferences` 硬件级加密保存；
   - 日常浏览与正文阅读 100% 纯原生 Compose 渲染，彻底无 WebView；发评点赞时安全带凭据由 BFF 代理交互。

---

## 三、 快速启动与构建说明

### 1. 运行 BFF 网关服务端
```bash
cd focal-platform/focal-server
bun install  # 或 npm install
bun test     # 执行单元测试
bun run dev  # 启动服务在 http://localhost:8080
```

### 2. 编译 Android 客户端
```bash
cd focal-android
./gradlew testDebugUnitTest
./gradlew assembleRelease
# 安装产物位于 app/build/outputs/apk/release/app-release.apk
```
