# Real-Time Edge Detection Viewer (Android + OpenCV + OpenGL + Web)

## Features implemented (Android + Web)
- Android Camera (Camera2) capture using TextureView.
- JNI bridge to native C++ (OpenCV) applying Canny Edge Detection.
- Render processed frames using OpenGL ES 2.0 texture.
- UI: Start/Stop, Toggle Raw/Edge, FPS overlay.
- Web viewer (TypeScript) that displays a sample processed frame and shows FPS/resolution.

## Screenshots / GIF
(TODO: Drop in screenshots or a GIF showing raw vs processed feed before final submission.)

## Repo structure
/app
/jni
/gl
/web
README.md
CMakeLists.txt

## Setup instructions (NDK, OpenCV dependencies)
1. **Android Studio** (Arctic Fox or later recommended).
2. **NDK**: Install via SDK Manager (e.g., r25 or compatible).
3. **CMake**: Install via SDK Manager.
4. **OpenCV**:
   - Option A: Use OpenCV Android SDK: download OpenCV Android (e.g., 4.x) and include `sdk/native/jni/include` in `CMakeLists.txt`. Add prebuilt libs or build OpenCV for Android. Update `OpenCV_DIR` in `local.properties` or export it before syncing (e.g. `OpenCV_DIR=C:/opencv-android/sdk/native/jni`).
   - Option B: Build OpenCV from source into `jniLibs`.
5. **Build**:
   - Open project in Android Studio.
   - Gradle will call CMake and build native library.
   - Run on a real device for camera support.

## Quick architecture explanation
- **Camera (Java/Kotlin)**: captures frames with Camera2 → provides YUV buffers.
- **JNI**: `nativeProcessFrame` receives buffer pointer via `GetDirectBufferAddress`, converts to `cv::Mat`.
- **C++ (OpenCV)**: performs `cvtColor`, `GaussianBlur`, `Canny`. Outputs RGBA bytes.
- **OpenGL ES (Java/Kotlin)**: texture updated each frame (`glTexSubImage2D`) and drawn on a fullscreen quad.
- **Web (TypeScript)**: displays an exported processed frame as base64; shows simple FPS/resolution overlay.

## How to run (short)
1. Clone repo: `git clone https://github.com/<you>/edge-viewer.git`
2. Open in Android Studio and build.
3. Deploy to device (camera permission required).
4. For web, `cd web && npm install && npm run build` then open `web/index.html`.

## Commit history notes
Check commit history for step-by-step development. Commits are small and descriptive (see commit messages).  
For a suggested roadmap, see `docs/development_plan.md`.

## Final checklist before submission
- Public GitHub repo (or shareable private) with correct permissions.
- Commits show incremental development — no single giant final commit.
- README filled with screenshots/GIF and instructions.
- NDK/OpenCV build instructions and CMake paths documented.
- App runs on real device and shows processed frames ≥ 10–15 FPS.
- Web viewer displays sample base64 image and shows FPS/resolution.
- Zip a release or tag final commit: v1.0.

## Commit history notes
Check commit history for step-by-step development. Commits are small and descriptive (see commit messages).

## Final checklist before submission
- Public GitHub repo (or shareable private) with correct permissions.
- Commits show incremental development — no single giant final commit.
- README filled with screenshots/GIF and instructions.
- NDK/OpenCV build instructions and CMake paths documented.
- App runs on real device and shows processed frames ≥ 10–15 FPS.
- Web viewer displays sample base64 image and shows FPS/resolution.
- Zip a release or tag final commit: v1.0.

