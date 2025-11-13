# OpenGL ES Module Notes

The OpenGL ES 2.0 renderer is implemented in `app/src/main/java/com/example/edgeviewer/GlRenderer.kt` and `TextureViewRenderer.kt`.  

These classes live inside the Android application module so that they can access Android context APIs and receive lifecycle callbacks directly from `MainActivity`.  

If you prefer to keep rendering utilities in a standalone library, move the two Kotlin files above into this `gl` directory and expose them via an Android library module, then update imports accordingly.

