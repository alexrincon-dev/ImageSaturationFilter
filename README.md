# Image Saturation Filter

Android demo that applies a **saturation** adjustment to a photo you pick from the gallery. The UI is built with **Jetpack Compose** and **Material 3**; image processing runs on the CPU using Android `ColorMatrix`.

## Features

- Pick an image with the system picker (`image/*`).
- Adjust saturation with a slider (range `0f`–`2f`, default `1f`); updates are debounced to avoid redoing work on every tiny drag.
- Remove the current image and return to the empty state (top-right control).
- Errors while loading the image are shown in a **Snackbar**.

## Tech stack

| Area | Choice |
|------|--------|
| Language | Kotlin |
| UI | Jetpack Compose, Material 3 |
| DI | Hilt (KSP) |
| Async | Kotlin Coroutines, `StateFlow` |
| Image processing | `Canvas` + `ColorMatrix` / `ColorMatrixColorFilter` |

**Requirements:** Android **API 28+** (minSdk 28), target/compile SDK **36**. JDK **11** for compilation.

## Architecture

Roughly **MVVM** with a small domain/data split:

- **UI:** `ImageScreen`, composables under `ui/components/` (`EmptyImageSection`, `EditableImageSection`).
- **Presentation:** `ImageViewModel` exposes slider state, optional source `Bitmap`, and filter result as `UiState`.
- **Domain:** `ApplyFilterUseCase` runs the filter and exposes a `Flow<UiState>`.
- **Data:** `ImageRepository` decodes a `Uri` to `Bitmap`; `ImageProcessor` applies saturation and returns a new `Bitmap`.

The filtered preview is shown with Compose `Image` + `ImageBitmap` (in-memory bitmap after processing; no Coil required for this pipeline).

## Project Structure

```
ImageSaturationFilter/
├── app/
│   ├── build.gradle.kts
│   └── src/main/
│       ├── AndroidManifest.xml
│       ├── java/dev/alexrincon/imagesaturationfilter/
│       │   ├── ImageApp.kt                 # Application class (@HiltAndroidApp)
│       │   ├── MainActivity.kt             # Single-activity entry, sets Compose content
│       │   ├── core/ui/theme/              # Material 3 theme (Color, Type, Theme)
│       │   ├── data/
│       │   │   ├── ImageProcessor.kt       # Saturation via ColorMatrix + Canvas
│       │   │   └── ImageRepository.kt      # Decode Bitmap from content Uri
│       │   ├── di/
│       │   │   └── AppModule.kt            # Hilt: ImageProcessor, ApplyFilterUseCase
│       │   ├── domain/
│       │   │   └── ApplyFilterUseCase.kt   # Wraps processor; exposes Flow<UiState>
│       │   └── ui/
│       │       ├── ImageScreen.kt          # Screen, picker launcher, snackbar wiring
│       │       ├── ImageViewModel.kt       # State: slider, bitmap, derived uiState
│       │       ├── UiState.kt              # Idle / Loading / Success / Error
│       │       └── components/
│       │           ├── EmptyImageSection.kt    # Placeholder + “Select Image”
│       │           └── EditableImageSection.kt # Filtered image + remove button
│       └── res/                          # Strings, themes, launcher assets, etc.
├── gradle/
│   └── libs.versions.toml                # Version catalog
├── build.gradle.kts
├── settings.gradle.kts
└── README.md
```

Gradle wrapper files (`gradlew`, `gradle/wrapper/`) live at the repo root for command-line builds.

## How It Works

1. **Cold start** — `MainActivity` applies the app theme and shows `ImageScreen`. With no bitmap loaded, `EmptyImageSection` invites the user to pick a photo.

2. **Pick an image** — `ActivityResultContracts.GetContent()` opens the system picker. On success, `ImageViewModel.onPhotoSelected(uri)` decodes the `Uri` on a background dispatcher via `ImageRepository` and stores an `ARGB_8888` `Bitmap` in a `StateFlow`. If decoding fails, a message is sent through a `SharedFlow` and shown as a **Snackbar**.

3. **Clear selection** — `onPhotoSelected(null)` clears the bitmap so the UI returns to the empty state (used by the remove control in `EditableImageSection`).

4. **Filter pipeline** — The ViewModel **combines** the saturation slider (debounced) with the current bitmap. For a non-null bitmap it **flatMapLatest** into `ApplyFilterUseCase`, which runs `ImageProcessor.applyFilter(bitmap, saturation)` on a worker thread and emits `UiState.Success` with the output `Bitmap`, or `UiState.Error` if something goes wrong.

5. **Display** — While the filter is recomputing, the UI can show loading; on success, `EditableImageSection` draws the result with Compose `Image` and overlays the top-end remove `IconButton`.

6. **Slider** — Range `0f`–`2f` maps to `ColorMatrix.setSaturation` (`1f` is unchanged). Debouncing reduces how often the heavy bitmap work runs while dragging.

## Build

From the project root:

```bash
./gradlew assembleDebug
```

Or compile Kotlin only:

```bash
./gradlew compileDebugKotlin
```

Open the project in **Android Studio** and run the `app` configuration on a device or emulator.

## License

This repository is a personal demo project; add a license file if you intend to distribute or open-source it.
