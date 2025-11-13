# Development Plan

This document outlines the intended commit-by-commit breakdown for the MVP edge detection project.

## Commit Roadmap

1. **init:** establish repository skeleton, Gradle settings, `.gitignore`.
2. **android/init:** generate Android app module with manifest, resources, and empty `MainActivity`.
3. **android/camera:** implement Camera2 controller capturing frames via `ImageReader`.
4. **jni/stub:** add JNI scaffolding, native library loading, placeholder CMake configuration.
5. **jni/opencv:** integrate OpenCV processing pipeline (grayscale + Canny) and JNI bridge.
6. **gl/renderer:** add OpenGL ES renderer with shader program and texture upload support.
7. **android/ui:** wire UI controls (start/stop, raw/processed toggle) and FPS overlay + binding.
8. **perf/pipeline:** optimise frame flow using direct `ByteBuffer`, synchronisation & timestamps.
9. **web/viewer:** scaffold TypeScript viewer displaying base64 frame + stats overlay.
10. **docs/readme:** fill README with setup instructions, architecture diagrams, screenshots.
11. **cleanup/polish:** fix lint issues, add comments, adjust build scripts, final QA tweaks.

## Architecture Highlights

- **Camera layer:** Kotlin `CameraController` using Camera2 with `ImageReader` for YUV frames.
- **Processing layer:** JNI bridge to C++ `OpenCvProcessor` performing Canny/grayscale.
- **Rendering layer:** OpenGL ES 2.0 renderer drawing processed frames as fullscreen texture.
- **UI layer:** `MainActivity` with controls and FPS overlay using view binding.
- **Web layer:** TypeScript/HTML viewer showing sample frame, resolution, and FPS stats.

## Next Steps

Follow the commit roadmap sequentially, verifying build & runtime behaviour on device after each milestone. Document FPS figures and capture screenshots for inclusion in the README before the final polish commit.

