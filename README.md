# 拾光簿 · Shiguang Contacts

一款只属于你的本地联系人簿。把姓名、关系、电话、备注和照片放在一起，按自己的方式记住重要的人。

Android 原生应用，使用 **Kotlin + Jetpack Compose + Material 3 Expressive**。目标竖屏手机 `412 × 892dp`，只提供浅色 Purple 主题。

[**下载已签名 APK**](https://github.com/zmoee/shiguang-contacts/releases/latest) · [查看源码](https://github.com/zmoee/shiguang-contacts)

## 功能

- **联系人**：新建、查看、编辑、删除、收藏，输入校验与删除确认。
- **搜索**：按姓名、关系、电话、备注或照片标签即时筛选，支持系统语音搜索。
- **收藏**：集中查看标记为收藏的人；新建联系人时可自动收藏。
- **详情**：220dp 头图区、200dp 信息卡、补充资料和照片操作。
- **照片**：CameraX 实时预览、相机权限处理、拍照后写入应用私有目录，并可用标签检索。
- **设置**：排序方式、自动收藏、清空全部联系人、隐私说明。
- **真实持久化**：联系人与设置通过 DataStore 保存；照片保存在应用私有文件目录。没有账号、没有网络权限，也不会写入示例数据。
- **界面与动效**：Material 3 Expressive `WideNavigationRail`（96dp / 220dp）、`MotionScheme.standard()`、Material Symbols Rounded、Roboto、可点击组件的涟漪与轻缩放反馈。返回时反向播放进入动画。

## 构建

项目包含 Gradle Wrapper 9.3.1、AGP 9.1.1，要求 JDK 17+、Android SDK 37。

```bash
./gradlew :app:assembleRelease
```

Release 签名由环境变量提供：

```bash
export SHIGUANG_KEYSTORE_PATH=/absolute/path/to/keystore.p12
export SHIGUANG_KEYSTORE_PASSWORD=...
export SHIGUANG_KEY_ALIAS=shiguang
export SHIGUANG_KEY_PASSWORD=...
./gradlew :app:assembleRelease
```

推送到 `main` 后，GitHub Actions 会自动构建、校验签名并上传 `shiguang-contacts-apk` artifact。所需 Secrets：

- `SHIGUANG_KEYSTORE_BASE64`
- `SHIGUANG_KEYSTORE_PASSWORD`
- `SHIGUANG_KEY_ALIAS`
- `SHIGUANG_KEY_PASSWORD`

## 技术栈

| 用途 | 依赖 |
| --- | --- |
| UI | Jetpack Compose, Material 3 `1.5.0-beta02`（含 Expressive API） |
| 导航 | Navigation Compose |
| 数据 | Preferences DataStore + `org.json` |
| 相机 | CameraX 1.5.3 |
| 图片 | Coil Compose |
| 字体 | Roboto + Material Symbols Rounded（内置） |

## 隐私

应用不声明网络权限。联系人 JSON 和相机照片都存放在本机应用沙盒内。备份规则只声明了照片目录，系统备份是否发生由设备设置决定。

## License

源码采用 MIT License。Roboto 与 Material Symbols 字体遵循 Apache License 2.0；详见 [THIRD_PARTY_NOTICES.md](THIRD_PARTY_NOTICES.md)。
