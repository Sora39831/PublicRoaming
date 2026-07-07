# BiliRoaming 适配 B站 9.1.0 迁移说明

## 概述

本文档记录了将 BiliRoaming 从兼容 B站 8.x 版本升级到兼容 9.1.0 版本所做的修改。

**目标 B站版本**: 9.1.0 (版本号 9010300)
**分析日期**: 2026-07-07
**APK 来源**: `iBiliPlayer-bili.apk`

---

## 核心发现

### 1. DEX 扫描引擎仍然有效 ✅
- 59/64 锚点字符串在 9.1.0 中仍然有效
- 自动符号发现机制不需要修改
- C++ Native 层 (`biliroaming.cc`) 无需修改

### 2. JSON 框架变化
- **kotlinx.serialization** 已成为主力框架 (1723 次引用)
- Gson 仍在使用 (467 次引用，次要)
- Fastjson 仍在使用 (294 次引用，次要)
- KotlinxJsonHook 已能处理，无需大改

### 3. 主要类名变化

| 旧类名 | 新类名 (9.1.0) | 状态 |
|--------|---------------|------|
| `okretro/Call` | `okretro/call/BiliCall` | ✅ 源码已适配 |
| `okretro/HttpResponse` | `lib/ighttp/IgHttpResponse` | ⚠️ HTTP 层重构 |
| `BangumiEpisode` | `BangumiUniformEpisode` | ✅ 源码已适配 |
| `BangumiSeason` | `BangumiUniformSeason` | ✅ 源码已适配 |
| `AccountMine` | `tv/danmaku/bili/ui/main2/api/AccountMine` | ✅ 源码已适配 |
| `SplashActivity` | 已不存在 | ⚠️ 回退到 MainActivityV2 |
| `Drawer` 相关 | 已不存在 | ⚠️ 功能可能受影响 |
| `PlayerQualityService` | `TheseusPlayerQualityService` | ✅ 已适配 |
| `GripperExecute` (gripper) | 改名 | ✅ 已适配 |

---

## 修改文件清单

### 1. BiliBiliPackage.kt (核心修改)

**修改内容**: 为 9 个硬编码类路径增加回退选项

- `rxGeneralResponseClass` - 增加 rxjava3 回退
- `seasonParamsMapClass` - 增加 SeasonRepository 回退
- `splashActivityClass` - 增加 MainActivity 回退
- `gripperBootExpClass` - 增加 GripperExecute 回退
- `splashInfoClass` - 增加内部类回退
- `biliVideoDetailClass` - 增加 ViewUnite 回退
- `commentInvalidFragmentClass` - 增加新 fragment 回退
- `playerQualityServiceClass` - 增加 TheseusPlayerQualityService 回退
- 新增 `isV9OrAbove` 版本检测属性

### 2. KillDelayBootHook.kt
- 增加 gripper 类方法名变化的回退处理
- 当原方法名 `getDelayMillis` 失效时，自动查找返回 long 的无参方法

### 3. SplashHook.kt
- 增加 BrandShowInfo 类变化的回退处理
- 当 `getMode` 方法 hook 失败时，手动查找并 hook

### 4. SettingHook.kt
- splashActivityClass 为空时回退到 mainActivityClass

### 5. BangumiSeasonHook.kt
- isSerializable 检测增加 kotlinx Json 类检测

### 6. JsonHook.kt
- AccountMine 类路径增加 AccountInfo 回退

### 7. DrawerHook.kt
- BaseMainFrameFragment 增加包名变化的回退

### 8. KotlinxJsonHook.kt
- 增加注释说明 kotlinx 在 9.1.0 中的主力地位

### 9. BangumiPlayUrlHook.kt
- 增加注释说明 v1 playurl 可能已被移除

---

## 未修改但需注意的部分

### 不需要修改的部分
- **C++ Native 层** (biliroaming.cc) - DEX 扫描引擎仍有效
- **Protobuf 定义** (api.proto) - 结构未变
- **大多数 Hook 类** - 通过 DEX 扫描自动适配
- **OkHttp 处理** - 所有锚点仍有效
- **Moss 框架** - 核心类仍存在

### 可能仍有问题的部分
1. **DrawerHook** - drawer 类可能已完全重写，功能可能不完整
2. **SplashHook** - 开屏广告逻辑可能因 SplashActivity 移除而部分失效
3. **CustomThemeHook** - 主题相关类结构可能有微调

---

## 编译说明

由于没有 Java 环境，本次修改未进行编译验证。建议在以下环境编译：
- JDK 17+
- Android SDK 35
- NDK 29.0.14206865
- Gradle 8.9.1+

编译命令:
```bash
./gradlew assembleDebug
```

---

## 测试建议

按优先级测试以下功能：

### P0 (核心功能)
- [ ] 模块加载/激活
- [ ] 番剧区域限制解除
- [ ] 番剧播放地址替换

### P1 (重要功能)
- [ ] 主题色定制
- [ ] 开屏广告
- [ ] 自动点赞
- [ ] 画质解锁

### P2 (辅助功能)
- [ ] 侧边栏 (Drawer)
- [ ] 青少年模式弹窗
- [ ] 分享链接
- [ ] 下载线程数

---

## 后续工作

1. 在真机/模拟器上安装 B站 9.1.0 测试
2. 根据日志调整失效的 Hook
3. 更新 `api.proto` 如果有 protobuf 结构变化
4. 持续跟踪 B站 版本更新

