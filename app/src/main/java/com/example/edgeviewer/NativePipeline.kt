package com.example.edgeviewer

import java.nio.ByteBuffer

/**
 * Kotlin facade for the JNI bridge into the native OpenCV processing pipeline.
 * All heavy image processing takes place on the C++ side to keep GC pressure low.
 */
object NativePipeline {

    init {
        System.loadLibrary("edge_pipeline")
    }

    private external fun nativeInit(width: Int, height: Int)
    private external fun nativeProcessFrame(
        yBuffer: ByteBuffer,
        uBuffer: ByteBuffer,
        vBuffer: ByteBuffer,
        pixelStride: Int,
        rowStride: Int,
        width: Int,
        height: Int,
        edgeMode: Boolean
    ): Long

    private external fun nativeUploadToTexture(textureId: Int)
    private external fun nativeRelease()

    fun initialise(width: Int, height: Int) {
        nativeInit(width, height)
    }

    /**
     * Sends the camera YUV planes to native and returns a timestamp in nanoseconds corresponding to
     * the frame processing completion. The native side also keeps the most recent RGBA texture ready.
     */
    fun processFrame(
        yBuffer: ByteBuffer,
        uBuffer: ByteBuffer,
        vBuffer: ByteBuffer,
        pixelStride: Int,
        rowStride: Int,
        width: Int,
        height: Int,
        edgeMode: Boolean
    ): Long {
        return nativeProcessFrame(
            yBuffer,
            uBuffer,
            vBuffer,
            pixelStride,
            rowStride,
            width,
            height,
            edgeMode
        )
    }

    fun uploadToTexture(textureId: Int) {
        nativeUploadToTexture(textureId)
    }

    fun release() {
        nativeRelease()
    }
}

