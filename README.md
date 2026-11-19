# mediaplay

Android 原生 Java 示例：**主屏控制 + 副屏同步显示**的音乐播放演示，面向车机 / 多屏场景。

用 `DisplayManager` 检测第二块显示设备，用 `android.app.Presentation` 在副屏渲染独立界面，主屏与副屏通过本地广播（`LocalBroadcastManager`）与后台服务交换控制指令和播放状态。

## 功能

- **后台播放服务**：`MusicService` 维护 3 首曲目列表，支持播放 / 暂停、上一首、下一首、静音、音量调节；切歌循环取模
- **主屏界面**：显示歌名、音量条、4 个控制按钮
- **副屏同步**：检测到显示数量 ≥ 2 时，在第二块显示上创建 `MediaPresentation`，副屏拥有独立布局与按钮，状态与主屏实时同步
- **指令与状态广播**：控制指令 `PLAY_PAUSE / PREV / NEXT / MUTE / VOLUME_CHANGE` 由 `MusicService` 接收；状态回传 `MEDIA_STATUS_UPDATE`（歌名、是否播放、音量、静音）
- **音量与静音**：`AudioManager.setStreamMute(AudioManager.STREAM_MUSIC, ...)`，音量映射为 `vol / 100`

## 页面与组件

| 类型 | 名称 | 说明 |
|---|---|---|
| Activity | `MainActivity` | 主屏页面（LAUNCHER 入口），启动服务、创建副屏、发送控制指令 |
| Service | `MusicService` | 后台播放服务（`MediaPlayer` + `AudioManager`） |
| Presentation | `MediaPresentation` | 副屏界面，布局 `presentation_media.xml` |

- `activity_main.xml`：`tv_song_name`、`seek_progress`、`tv_lyrics`、`seek_volume`、`btn_prev`、`btn_play`、`btn_next`、`btn_mute`
- `presentation_media.xml`：`tv_song_name`、`tv_volume`、`seek_volume`、`btn_prev`、`btn_play`、`btn_next`、`btn_mute`

## 核心类

| 类 | 职责 |
|---|---|
| `MainActivity` | 启动 `MusicService`；`postDelayed` 100ms 后按显示数量决定是否创建副屏；按钮与 `SeekBar` 发控制指令；注册广播接收器刷新歌名、播放状态、音量 |
| `MusicService` | `MediaPlayer` 播放控制、曲目切换、静音、音量设置、`postUpdate()` 广播状态、`onDestroy` 释放资源 |
| `MediaPresentation` | 副屏界面与独立按钮，指令走本地广播，接收状态广播刷新 UI |

## 权限

`AndroidManifest.xml` 声明：`INTERNET`、`WAKE_LOCK`、`SYSTEM_ALERT_WINDOW`、`ACCESS_DISPLAY_INFO`、`READ_MEDIA_AUDIO`。

> 说明：`INTERNET`、`SYSTEM_ALERT_WINDOW`、`ACCESS_DISPLAY_INFO`、`READ_MEDIA_AUDIO` 均已声明，但源码中未发现对应的网络调用、悬浮窗使用或媒体文件读取，也未见运行时权限申请；原设置副屏窗口类型为 `TYPE_SYSTEM_ALERT` 的代码行已被注释。

## 技术栈

| 项 | 值 |
|---|---|
| 语言 | Java 8 |
| compileSdk / targetSdk / minSdk | 34 / 34 / 24 |
| applicationId | `com.example.mediaplay` |
| Android Gradle Plugin | 8.5.2 |
| Gradle | 8.7（含 Wrapper） |
| 依赖 | appcompat 1.2.0、material 1.2.1、constraintlayout 2.0.1、localbroadcastmanager 1.1.0 |

## 运行

1. Android Studio 打开工程并同步（需本机 SDK 34）
2. **副屏功能需要两块显示设备**：车机中控 + 仪表，或在开发者选项中开启「模拟辅助显示设备」
3. 单屏设备上只能看到主界面，不会创建副屏

## 已知限制

- 3 首「歌曲」实际共用同一个音频文件 `res/raw/test1.mp3`，切歌只改变显示的曲名
- `seek_progress` 仅在 `onCreate` 中绑定，未注册监听、未调用 `setProgress`，也未读取 `getCurrentPosition` / `getDuration`，**进度条未接通**
- 歌词区 `tv_lyrics` 为布局内静态文本，未做歌词解析
- 副屏窗口水平偏移固定 `x = -200`，需在双屏设备上按实际屏参调整
- 无播放列表持久化、无网络流媒体、无封面加载

## License

[MIT](LICENSE)
