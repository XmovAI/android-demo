# XingyunAvatarAgent SDK Android Demo

星云数字人 Agent Android SDK（`xingyun_avatar_agent-v0.0.2`）的接入示例工程，演示数字人渲染与端到端语音对话（ASR → LLM → TTS）。

## 功能

- 数字人初始化、销毁、在线/离线切换、静音
- 数字人状态控制：`speak` / `listen` / `think` / `idle` / `interactiveidle`
- 端到端语音 Agent：开始说话、停止说话、打断
- 文本提问 → LLM 流式回复 → TTS 播报
- 数字人预缓存（`preCache` / `preCacheChar`）
- 右上角“参数”面板可实时修改连接参数与 JSON 配置

## 环境要求

| 项 | 版本 |
|---|---|
| JDK | 17+ |
| Android Gradle Plugin | 8.13.2 |
| Gradle | 8.13（使用仓库内 wrapper） |
| compileSdk / targetSdk | 36 |
| minSdk | 24 |

## 快速开始

1. 填写凭据
   编辑 `app/src/main/assets/agent_config.json`，将 `avatarAppId`、`avatarAppSecret` 替换为你在星云开放平台创建的应用凭据：

   ```json
   {
     "avatarAppId": "your-app-id",
     "avatarAppSecret": "your-app-secret"
   }
   ```

   也可以先启动 App，再点右上角“参数”面板临时填写。

   > 仓库中的 `agent_config.json` 使用 `****` 占位符，请勿把真实凭据提交到版本库。

2. 构建 Debug APK

   ```bash
   ./gradlew :app:assembleDebug
   ```

   Windows 使用 `gradlew.bat`。产物位于：

   ```text
   app/build/outputs/apk/debug/XingYunAvatarAgentSDKDemo_<yyyyMMddHHmm>.apk
   ```

3. 安装并运行

   ```bash
   adb install -r app/build/outputs/apk/debug/XingYunAvatarAgentSDKDemo_<yyyyMMddHHmm>.apk
   ```

   打开 App 后点击“初始化”，等待状态变为 `CONNECTED` / `LISTENING`。

## 配置项说明

`app/src/main/assets/agent_config.json` 字段：

| 字段 | 说明 |
|---|---|
| `asrConfig` | ASR 配置（JSON 字符串，按部署环境填写，可为空） |
| `ttsConfig` | TTS 配置（JSON 字符串，可为空） |
| `brainConfig` | LLM 配置（JSON 字符串，按部署环境填写，可为空） |
| `features` | 功能开关（JSON 字符串，如 AEC、降噪、抗干扰等） |
| `sessionSpeakReqId` | 会话播报请求 ID，可选 |
| `avatarAppId` | 星云具身应用 appId，**必填** |
| `avatarAppSecret` | 星云具身应用 appSecret，**必填** |
| `avatarGatewayServer` | 数字人网关地址，**必填** |
| `preCacheUrl` | 预缓存接口地址 |
| `avatarInitConfigJson` | 数字人初始化参数（JSON 字符串） |
| `useExternalPcmAudio` | 是否使用外部 PCM 音频，可选 |
| `extras` | 透传到 `startSession` 的扩展参数（JSON 字符串），可选 |

`asrConfig` / `ttsConfig` / `brainConfig` / `features` / `avatarInitConfigJson` / `extras` 的值均为 JSON 字符串，写入外层 JSON 时需要转义。

## 目录结构

```text
.
├── app/
│   ├── libs/xingyun_avatar_agent-v0.0.2.aar   # 星云数字人 Agent SDK
│   └── src/main/
│       ├── assets/agent_config.json           # 示例配置（凭据占位符）
│       └── java/com/xmov/ai/echomind/demo/    # Demo 代码
├── lib_core/                                  # 基础 UI / 工具库
├── gradle/libs.versions.toml                  # 依赖版本
└── settings.gradle.kts
```

## 签名配置（可选）

Release 构建默认回退到 debug 签名。如需使用自己的签名，在项目根目录创建 `keystore.properties`（已被 `.gitignore` 忽略）：

```properties
storeFile=/absolute/path/to/your-release.jks
storePassword=your-store-password
keyAlias=your-key-alias
keyPassword=your-key-password
```

建议 `storeFile` 使用绝对路径；相对路径会以 `app/` 为基准解析。

## 权限

- `android.permission.INTERNET`
- `android.permission.RECORD_AUDIO`

## 说明

- 本工程为 SDK 接入示例，未包含完整的生产级错误处理、权限引导和 UI 适配。
- 数字人、语音 Agent 的具体接口说明请参考随 SDK 发布的接入文档。
- `app/src/main/assets/agent_config.json` 中的网关地址为 SDK 示例地址，不包含密钥。