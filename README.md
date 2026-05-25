# InstaSplit Android 📱✨

[![Android CI/CD](https://github.com/com.instasplit.app/actions/workflows/android.yml/badge.svg)](#)
![Size](https://img.shields.io/badge/APK%20Size-1.8%20MB-success)
![Kotlin](https://img.shields.io/badge/Kotlin-100%25-blue?logo=kotlin)
![Compose](https://img.shields.io/badge/Jetpack%20Compose-Modern-orange?logo=jetpackcompose)

A premium, privacy-first, fully native Android application designed to slice panoramic images into seamless carousels for Instagram. Rebuilt from the ground up matching features of the web PWA version, leveraging native performance, scoped storage, and modern Android design system guidelines.

---

## 🌟 Key Features

*   **Privacy-First Architecture**: Declares absolutely **zero internet permissions**. All image processing, rendering, and encoding happens 100% on-device.
*   **Intuitive Gesture Controls**: Supports fluid zoom (`0.5x` to `10.0x`) and multi-touch panning gestures. Horizontal bounds clamp automatically to keep image boundaries aligned.
*   **Symmetric Auto-Fit Centering**: Automatically centers the image Crop Box horizontally (for wide panoramas) or vertically (for tall photos) upon load, giving a balanced starting position.
*   **Dynamic Grid Preview**: An interactive phone-frame layout showing exactly how slides will look when uploaded as a swipeable Instagram carousel.
*   **Stateless Zip Exporter**: Compresses and packs slices into a single zip file stream-by-stream directly to disk, conserving RAM and preventing Out-Of-Memory (OOM) crashes on large files.
*   **Haptic Completion Feedback**: Short tactile vibrations notify you when slice exports complete.
*   **Lightweight Build footprint**: Optimized using R8 shrinking rules down to a minimal **1.8 MB** release APK.

---

## 🛠 Tech Stack & Architecture

The project follows **MVVM (Model-View-ViewModel)** with **Clean Architecture** patterns separating domain logic, data models, and native Android implementations:

*   **UI Framework**: Jetpack Compose (Material 3).
*   **Dependency Injection**: Hilt.
*   **Image Processing**: Native `Canvas`, `BitmapRegionDecoder`, and `BitmapFactory`.
*   **Storage Access**: MediaStore API (Android 10+), Scoped Storage, and legacy SAF fallback.
*   **Asynchronous Flows**: Kotlin Coroutines and StateFlow.
*   **Serialization**: Kotlinx Serialization.
*   **Unit Testing**: JUnit 4 & 5, Robolectric (for JVM-level Android Graphics APIs mocking), and MockK.

---

## 📂 Project Structure

```
app/src/main/java/com/instasplit/app/
│
├── domain/                  # Pure Kotlin Domain Layer
│   ├── model/               # Data structures (CropState, AspectRatio, SliceConfig)
│   ├── repository/          # Repository interfaces (ImageRepository, ExportRepository)
│   └── usecase/             # Core business actions (LoadImageUseCase, ComputeSlicesUseCase)
│
├── data/                    # Data/Platform Implementations
│   ├── repository/          # Concrete Image & Export implementations
│   └── storage/             # File compression & MediaStore providers (ZipExporter, MediaStoreHelper)
│
├── di/                      # Dependency Injection modules (Hilt)
│
└── presentation/            # Compose UI Screens & ViewModels
    ├── home/                # Welcome screen, Drag-zone & Scoped URI loader
    ├── editor/              # Zoomable canvas viewport & crop customization settings
    ├── preview/             # Interactive Instagram swipeable phone-frame mockup
    ├── export/              # Compiling progress screen & zip sharing trigger
    ├── navigation/          # Compose type-safe Navigation Graph
    └── theme/               # Dark mode color schemes and premium typography
```

---

## 🚀 Building & Running

Ensure you have Android SDK installed (matching target SDK 35) and JDK 17 configured.

### Run Unit Tests
To execute all local unit tests (including Robolectric crop calculations and auto-fit tests):
```powershell
./gradlew :app:testDebugUnitTest
```

### Compile Debug Build
To build a developer debug APK:
```powershell
./gradlew assembleDebug
```

### Compile Production Release
To compile the minified, signed release APK:
```powershell
./gradlew assembleRelease
```
The optimized release binary will be packaged at:
`app/build/outputs/apk/release/app-release.apk`

---

## 🔒 Security & Privacy

Since this app is designed for processing personal photos, we are committed to complete data safety:
1.  **No Internet Permission**: The app cannot make API calls, fetch resources, or upload telemetry.
2.  **No Background Syncing**: All tasks terminate as soon as you exit the export interface.
3.  **Local Storage**: Exported files are written directly into standard shared system directories (`Pictures/InstaSplit`) or shared explicitly through the app's internal FileProvider.
