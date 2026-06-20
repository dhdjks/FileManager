# FileManager

一款现代化的 Android 文件管理器应用

## 功能特性

- 文件浏览 - 管理内部存储中的文件和文件夹
- 文件操作 - 复制、粘贴、剪切、删除、重命名
- 新建文件夹 - 快速创建新文件夹
- 排序 - 按名称/大小/时间排序
- 视图切换 - 列表视图/网格视图

## 技术栈

- 语言: Kotlin
- 最低 SDK: 26 (Android 8.0)
- 目标 SDK: 34 (Android 14)
- UI 框架: Jetpack Compose + Material Design 3
- 架构: MVVM + Clean Architecture
- 异步: Kotlin Coroutines + Flow
- 依赖注入: Hilt

## 项目结构

```
app/src/main/java/com/filemanager/app/
├── FileManagerApp.kt        # Hilt Application
├── data/
│   └── repository/
│       └── FileRepository.kt # 文件操作仓库
├── domain/
│   └── model/
│       ├── FileItem.kt    # 文件模型
│       └── ClipboardItem.kt  # 剪贴板模型
├── presentation/
│   └── viewmodel/
│       └── FileManagerViewModel.kt  # MVVM ViewModel
└── ui/
    ├── MainActivity.kt      # 主界面 (Compose UI)
    └── theme/              # Material 3 主题
```

## 构建

```bash
# 构建 Debug APK
./gradlew assembleDebug

# 构建 Release APK
./gradlew assembleRelease
```

## GitHub Actions

项目包含 CI/CD 工作流：

- **build.yml**: 每次推送到 main/master 或 Pull Request 时自动构建并上传 APK
- **release.yml**: 创建 tag (如 v1.0.0) 时自动发布 Release APK

构建完成的 APK 可以在 GitHub Actions 的 Artifacts 中下载。

## 贡献

欢迎提交 Issue 和 Pull Request！
