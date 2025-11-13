package com.example.edgeviewer

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.TextureView
import android.widget.Button
import android.widget.TextView
import android.widget.ToggleButton
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.edgeviewer.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity(), CameraController.FrameListener {

    private lateinit var binding: ActivityMainBinding
    private lateinit var cameraController: CameraController
    private lateinit var frameProcessor: FrameProcessor
    private lateinit var renderer: GlRenderer
    private lateinit var textureRenderer: TextureViewRenderer

    @Volatile
    private var renderProcessed = true

    private val permissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) {
                setupCameraPipeline()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        frameProcessor = FrameProcessor()
        renderer = GlRenderer(frameProcessor) { fps ->
            runOnUiThread {
                binding.fpsView.text = getString(R.string.fps_label, fps)
            }
        }
        textureRenderer = TextureViewRenderer(binding.textureView, renderer)

        cameraController = CameraController(this, this)

        bindUi()
        ensurePermission()
    }

    override fun onResume() {
        super.onResume()
        textureRenderer.start()
    }

    override fun onPause() {
        super.onPause()
        cameraController.stop()
        textureRenderer.stop()
        frameProcessor.stop()
    }

    private fun bindUi() {
        binding.startButton.setOnClickListener {
            setupCameraPipeline()
        }
        binding.stopButton.setOnClickListener {
            cameraController.stop()
            frameProcessor.stop()
        }
        binding.modeToggle.isChecked = true
        binding.modeToggle.setOnCheckedChangeListener { _, isChecked ->
            renderProcessed = isChecked
        }
    }

    private fun ensurePermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) ==
            PackageManager.PERMISSION_GRANTED
        ) {
            setupCameraPipeline()
        } else {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    private fun setupCameraPipeline() {
        val width = 1280
        val height = 720
        frameProcessor.start(width, height)
        cameraController.start(null, width, height)
        binding.fpsView.text = getString(R.string.fps_label, 0.0)
    }

    override fun onFrameAvailable(
        y: java.nio.ByteBuffer,
        u: java.nio.ByteBuffer,
        v: java.nio.ByteBuffer,
        pixelStride: Int,
        rowStride: Int,
        width: Int,
        height: Int
    ) {
        frameProcessor.updateFrameSize(width, height)
        frameProcessor.processDirect(
            y,
            u,
            v,
            pixelStride,
            rowStride,
            renderProcessed
        )
    }
}

