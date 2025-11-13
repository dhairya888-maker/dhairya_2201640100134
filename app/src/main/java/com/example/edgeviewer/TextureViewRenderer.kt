package com.example.edgeviewer

import android.graphics.SurfaceTexture
import android.opengl.EGL14
import android.opengl.EGLConfig
import android.opengl.EGLContext
import android.opengl.EGLDisplay
import android.opengl.EGLSurface
import android.opengl.GLES20
import android.view.TextureView
import android.view.TextureView.SurfaceTextureListener
import javax.microedition.khronos.opengles.GL10
import kotlin.concurrent.thread

/**
 * Lightweight EGL wrapper that allows rendering OpenGL content directly into a TextureView.
 * The renderer runs on a dedicated thread and continuously draws frames as long as the engine
 * is marked as running.
 */
class TextureViewRenderer(
    private val textureView: TextureView,
    private val renderer: Renderer
) : SurfaceTextureListener {

    interface Renderer {
        fun onSurfaceCreated(
            gl: GL10?,
            config: EGLConfig?,
            surfaceTexture: SurfaceTexture,
            width: Int,
            height: Int
        )

        fun onSurfaceChanged(gl: GL10?, width: Int, height: Int)
        fun onDrawFrame(gl: GL10?)
        fun onSurfaceDestroyed()
    }

    private var renderThread: Thread? = null
    @Volatile
    private var eglDisplay: EGLDisplay? = null
    @Volatile
    private var eglContext: EGLContext? = null
    @Volatile
    private var eglSurface: EGLSurface? = null
    @Volatile
    private var running = false
    private var width = 0
    private var height = 0

    fun start() {
        textureView.surfaceTextureListener = this
        if (textureView.isAvailable) {
            textureView.surfaceTexture?.let {
                onSurfaceTextureAvailable(it, textureView.width, textureView.height)
            }
        }
    }

    fun stop() {
        running = false
        renderThread?.join()
        renderThread = null
        releaseEgl()
        textureView.surfaceTextureListener = null
    }

    override fun onSurfaceTextureAvailable(surfaceTexture: SurfaceTexture, width: Int, height: Int) {
        this.width = width
        this.height = height
        startRendererThread(surfaceTexture)
    }

    override fun onSurfaceTextureSizeChanged(surface: SurfaceTexture, width: Int, height: Int) {
        this.width = width
        this.height = height
    }

    override fun onSurfaceTextureDestroyed(surface: SurfaceTexture): Boolean {
        running = false
        renderer.onSurfaceDestroyed()
        return true
    }

    override fun onSurfaceTextureUpdated(surface: SurfaceTexture) = Unit

    private fun startRendererThread(surfaceTexture: SurfaceTexture) {
        if (renderThread != null) {
            running = false
            renderThread?.join()
        }
        running = true
        renderThread = thread(start = true, name = "TextureRenderer") {
            setupEgl(surfaceTexture)
            renderer.onSurfaceCreated(null, null, surfaceTexture, width, height)
            renderer.onSurfaceChanged(null, width, height)
            while (running) {
                renderer.onDrawFrame(null)
                EGL14.eglSwapBuffers(eglDisplay, eglSurface)
                Thread.sleep(1)
            }
            renderer.onSurfaceDestroyed()
            releaseEgl()
        }
    }

    private fun setupEgl(surfaceTexture: SurfaceTexture) {
        eglDisplay = EGL14.eglGetDisplay(EGL14.EGL_DEFAULT_DISPLAY)
        if (eglDisplay == EGL14.EGL_NO_DISPLAY) {
            throw RuntimeException("Unable to get EGL14 display")
        }
        val version = IntArray(2)
        if (!EGL14.eglInitialize(eglDisplay, version, 0, version, 1)) {
            throw RuntimeException("Unable to initialize EGL14")
        }
        val attribList = intArrayOf(
            EGL14.EGL_RED_SIZE, 8,
            EGL14.EGL_GREEN_SIZE, 8,
            EGL14.EGL_BLUE_SIZE, 8,
            EGL14.EGL_RENDERABLE_TYPE, EGL14.EGL_OPENGL_ES2_BIT,
            EGL14.EGL_NONE
        )
        val configs = arrayOfNulls<EGLConfig>(1)
        val numConfigs = IntArray(1)
        EGL14.eglChooseConfig(
            eglDisplay,
            attribList,
            0,
            configs,
            0,
            configs.size,
            numConfigs,
            0
        )
        val attribListContext = intArrayOf(
            EGL14.EGL_CONTEXT_CLIENT_VERSION, 2,
            EGL14.EGL_NONE
        )
        eglContext = EGL14.eglCreateContext(
            eglDisplay,
            configs[0],
            EGL14.EGL_NO_CONTEXT,
            attribListContext,
            0
        )
        val attribListSurface = intArrayOf(EGL14.EGL_NONE)
        eglSurface = EGL14.eglCreateWindowSurface(
            eglDisplay,
            configs[0],
            surfaceTexture,
            attribListSurface,
            0
        )
        EGL14.eglMakeCurrent(eglDisplay, eglSurface, eglSurface, eglContext)
        GLES20.glDisable(GLES20.GL_DEPTH_TEST)
    }

    private fun releaseEgl() {
        val display = eglDisplay
        val surface = eglSurface
        val context = eglContext
        if (display != null && display !== EGL14.EGL_NO_DISPLAY) {
            EGL14.eglMakeCurrent(display, EGL14.EGL_NO_SURFACE, EGL14.EGL_NO_SURFACE, EGL14.EGL_NO_CONTEXT)
            if (surface != null && surface !== EGL14.EGL_NO_SURFACE) {
                EGL14.eglDestroySurface(display, surface)
            }
            if (context != null) {
                EGL14.eglDestroyContext(display, context)
            }
            EGL14.eglTerminate(display)
        }
        eglDisplay = null
        eglSurface = null
        eglContext = null
    }
}

