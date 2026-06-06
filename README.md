# m4 task

A minimal and sophisticated task management app for Android, built with Kotlin and Jetpack Compose. Designed with a focus on simplicity, fluid animations, and high customizability.

## Features

- **Clean & Elegant UI**: iOS-inspired minimal design with focus on readability and smooth 144Hz animations.
- **Advanced Customization**:
  - Move the Floating Action Button (FAB) to any position on the screen.
  - Custom Accent Colors: Choose from presets or enter any Hex color code.
  - Personal Backgrounds: Set your favorite image as background with adjustable blur.
- **Statistics & Insights**:
  - Track total completed tasks.
  - Monitor your 24-hour completion rate to stay motivated.
- **Backup & Restore**: Safeguard your entire experience, including tasks, settings, and custom backgrounds, using a single ZIP file.
- **Multi-language Support**: Native localization for English, Japanese (日本語), Simplified Chinese (简体中文), and Traditional Chinese (繁體中文).
- **Dark Mode**: High-contrast dark theme with a deep gray base for comfort.

## Installation

You can download the latest APK from the [Releases](https://github.com/masaki-09/m4sak1-task-app/releases) section of this repository.

## Technical Stack

- **Language**: Kotlin
- **UI Framework**: Jetpack Compose (Material 3)
- **Database**: Room Persistence Library
- **Concurrency**: Kotlin Coroutines & Flow
- **Serialization**: Kotlinx Serialization (JSON/ZIP)
- **Architecture**: MVVM (Model-View-ViewModel)

## Development

To build the project yourself:
1. Clone the repository: `git clone https://github.com/masaki-09/m4sak1-task-app.git`
2. Open the project in **Android Studio (Hedgehog or newer)**.
3. Sync Gradle and run the `:app` module.

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

---
*Maintained by m4sak1*
