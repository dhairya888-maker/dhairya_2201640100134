package com.example.edgeviewer

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.ImageFormat
import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraCaptureSession
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraDevice
import android.hardware.camera2.CameraManager
import android.hardware.camera2.CaptureRequest
import android.media.ImageReader
import android.os.Handler
import android.os.HandlerThread
import android.view.Surface
import java.nio.ByteBuffer

class CameraController(
    private val context: Context,
    private val listener: FrameListener
) {

    interface FrameListener {
        fun onFrameAvailable(
            y: ByteBuffer,
            u: ByteBuffer,
            v: ByteBuffer,
            pixelStride: Int,
            rowStride: Int,
            width: Int,
            height: Int
        )
    }

    private val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
    private var cameraDevice: CameraDevice? = null
    private var captureSession: CameraCaptureSession? = null
    private var imageReader: ImageReader? = null
    private var backgroundThread: HandlerThread? = null
    private var backgroundHandler: Handler? = null
    fun start(previewSurface: Surface?, width: Int, height: Int) {
        startBackgroundThread()
        setupImageReader(width, height)
        openCamera(previewSurface, width, height)
    }

    fun stop() {
        captureSession?.close()
        captureSession = null
        cameraDevice?.close()
        cameraDevice = null
        imageReader?.close()
        imageReader = null
        stopBackgroundThread()
    }

    @SuppressLint("MissingPermission")
    private fun openCamera(previewSurface: Surface?, width: Int, height: Int) {
        try {
            val cameraId = cameraManager.cameraIdList.first { id ->
                val characteristics = cameraManager.getCameraCharacteristics(id)
                val facing = characteristics.get(CameraCharacteristics.LENS_FACING)
                facing == CameraCharacteristics.LENS_FACING_BACK
            }
            cameraManager.openCamera(
                cameraId,
                object : CameraDevice.StateCallback() {
                    override fun onOpened(device: CameraDevice) {
                        cameraDevice = device
                        createCaptureSession(device, previewSurface, width, height)
                    }

                    override fun onDisconnected(device: CameraDevice) {
                        device.close()
                    }

                    override fun onError(device: CameraDevice, error: Int) {
                        device.close()
                    }
                },
                backgroundHandler
            )
        } catch (ex: CameraAccessException) {
            ex.printStackTrace()
        }
    }

    private fun createCaptureSession(device: CameraDevice, previewSurface: Surface?, width: Int, height: Int) {
        try {
            val surfaces = mutableListOf<Surface>().apply {
                previewSurface?.let { add(it) }
                imageReader?.surface?.let { add(it) }
            }
            val requestBuilder = device.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW).apply {
                previewSurface?.let { addTarget(it) }
                imageReader?.surface?.let { addTarget(it) }
                set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE)
                set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_ON)
            }
            device.createCaptureSession(
                surfaces,
                object : CameraCaptureSession.StateCallback() {
                    override fun onConfigured(session: CameraCaptureSession) {
                        captureSession = session
                        requestBuilder.build().also { request ->
                            session.setRepeatingRequest(request, null, backgroundHandler)
                        }
                    }

                    override fun onConfigureFailed(session: CameraCaptureSession) {
                        session.close()
                    }
                },
                backgroundHandler
            )
        } catch (ex: CameraAccessException) {
            ex.printStackTrace()
        }
    }

    private fun setupImageReader(width: Int, height: Int) {
        imageReader = ImageReader.newInstance(width, height, ImageFormat.YUV_420_888, 2).apply {
            setOnImageAvailableListener({ reader ->
                val image = reader.acquireLatestImage() ?: return@setOnImageAvailableListener
                val yPlane = image.planes[0]
                val uPlane = image.planes[1]
                val vPlane = image.planes[2]
                listener.onFrameAvailable(
                    yPlane.buffer,
                    uPlane.buffer,
                    vPlane.buffer,
                    uPlane.pixelStride,
                    yPlane.rowStride,
                    image.width,
                    image.height
                )
                image.close()
            }, backgroundHandler)
        }
    }

    private fun startBackgroundThread() {
        backgroundThread = HandlerThread("CameraBackground").also { it.start() }
        backgroundHandler = Handler(backgroundThread!!.looper)
    }

    private fun stopBackgroundThread() {
        backgroundThread?.quitSafely()
        backgroundThread?.join()
        backgroundThread = null
        backgroundHandler = null
    }
}

