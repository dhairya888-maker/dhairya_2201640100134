package com.example.edgeviewer

import android.os.SystemClock
import java.nio.ByteBuffer
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicReference

/**
 * Handles threading between camera callbacks and GL rendering. The camera thread enqueues
 * frames, while the GL thread drains and uploads them using the native pipeline.
 */
class FrameProcessor {

    private val running = AtomicBoolean(false)
    private val lastFrameTimestampNs = AtomicReference(0L)
    private val lastFps = AtomicReference(0.0)
    private var frameWidth = 0
    private var frameHeight = 0

    fun start(width: Int, height: Int) {
        if (running.getAndSet(true)) return
        NativePipeline.initialise(width, height)
        frameWidth = width
        frameHeight = height
    }

    fun stop() {
        running.set(false)
        NativePipeline.release()
    }

    fun processDirect(
        y: ByteBuffer,
        u: ByteBuffer,
        v: ByteBuffer,
        pixelStride: Int,
        rowStride: Int,
        edgeMode: Boolean
    ) {
        if (!running.get()) return
        y.rewind()
        u.rewind()
        v.rewind()
        val timestamp = NativePipeline.processFrame(
            y,
            u,
            v,
            pixelStride,
            rowStride,
            frameWidth,
            frameHeight,
            edgeMode
        )
        lastFrameTimestampNs.set(timestamp)
    }

    fun drainToTexture(textureId: Int): Double {
        if (!running.get()) return 0.0
        NativePipeline.uploadToTexture(textureId)
        val nowNs = SystemClock.elapsedRealtimeNanos()
        val frameNs = lastFrameTimestampNs.get()
        val deltaNs = nowNs - frameNs
        val fps = if (deltaNs > 0) 1e9 / deltaNs else 0.0
        lastFps.set(fps)
        return fps
    }

    fun lastFps(): Double = lastFps.get()

    fun frameSize(): Pair<Int, Int> = frameWidth to frameHeight

    fun hasFrame(): Boolean = lastFrameTimestampNs.get() != 0L

    fun isRunning(): Boolean = running.get()

    fun updateFrameSize(width: Int, height: Int) {
        frameWidth = width
        frameHeight = height
    }
}

