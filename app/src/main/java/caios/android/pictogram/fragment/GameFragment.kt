package caios.android.pictogram.fragment

import android.os.Bundle
import android.util.Size
import android.view.OrientationEventListener
import android.view.Surface
import android.view.View
import androidx.camera.core.AspectRatio
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import caios.android.pictogram.R
import caios.android.pictogram.analyze.PostureEstimator
import caios.android.pictogram.analyze.Device
import caios.android.pictogram.analyze.PostureSurfaceView
import caios.android.pictogram.databinding.FragmentGameBinding
import caios.android.pictogram.utils.PermissionUtils
import caios.android.pictogram.utils.SystemUtils
import caios.android.pictogram.utils.ToastUtils
import caios.android.pictogram.utils.autoCleared
import com.google.android.material.transition.MaterialSharedAxis
import com.google.common.util.concurrent.ListenableFuture
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class GameFragment: Fragment(R.layout.fragment_game) {

    private var binding by autoCleared<FragmentGameBinding>()

    private lateinit var postureSurfaceView: PostureSurfaceView
    private lateinit var cameraProviderFuture: ListenableFuture<ProcessCameraProvider>
    private lateinit var cameraExecutor: ExecutorService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val animDuration = resources.getInteger(R.integer.anim_duration_fragment_transition).toLong()
        val enterSharedAxisTransition = MaterialSharedAxis(MaterialSharedAxis.X, true).apply { duration = animDuration }
        val returnSharedAxisTransition = MaterialSharedAxis(MaterialSharedAxis.X, false).apply { duration = animDuration }

        enterTransition = enterSharedAxisTransition
        returnTransition = returnSharedAxisTransition

        exitTransition = enterSharedAxisTransition
        reenterTransition = returnSharedAxisTransition
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        binding = FragmentGameBinding.bind(view)

        postureSurfaceView = PostureSurfaceView(binding.surfaceView)

        if(PermissionUtils.isAllowed(requireContext(), PermissionUtils.requestPermissions)) {
            setupCamera()
        } else {
            findNavController().navigateUp()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }

    private fun setupCamera() {
        cameraExecutor = Executors.newSingleThreadExecutor()
        cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())

        cameraProviderFuture.addListener({
             bindPreview(cameraProviderFuture.get())
        }, ContextCompat.getMainExecutor(requireContext()))
    }

    private fun bindPreview(cameraProvider: ProcessCameraProvider) {

        binding.previewView.implementationMode = PreviewView.ImplementationMode.COMPATIBLE

        val previewSize = Size(binding.surfaceView.width, binding.surfaceView.height)

        val preview = Preview.Builder().build().also {
            it.setSurfaceProvider(binding.previewView.surfaceProvider)
        }

        val selector = CameraSelector.Builder().apply {
            requireLensFacing(CameraSelector.LENS_FACING_FRONT)
        }.build()

        val imageAnalyzer = ImageAnalysis.Builder().apply {
            setTargetRotation(binding.previewView.display.rotation)
            setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
        }.build().also {
            it.setAnalyzer(cameraExecutor, PostureEstimator(requireContext(), Device.CPU) { posture, bitmap ->
                postureSurfaceView.drawPosture(posture, bitmap, previewSize)
            })
        }

        val orientationEventListener = object : OrientationEventListener(requireContext()) {
            override fun onOrientationChanged(orientation: Int) {
                val rotation = when(orientation) {
                    ORIENTATION_UNKNOWN -> return
                    in 45 until 135     -> Surface.ROTATION_270
                    in 135 until 225    -> Surface.ROTATION_270
                    in 225 until 315    -> Surface.ROTATION_90
                    else                -> Surface.ROTATION_90
                }

                preview.targetRotation = rotation
                imageAnalyzer.targetRotation = rotation
            }
        }

        orientationEventListener.enable()

        try {
            cameraProvider.unbindAll()
            cameraProvider.bindToLifecycle(this, selector, preview, imageAnalyzer)
        } catch (e: Throwable) {
            ToastUtils.show(requireContext(), R.string.unknownError)
            findNavController().navigateUp()
        }
    }

    private fun getPreviewSize(): Size {
        val displaySize = SystemUtils.getDisplaySize(requireActivity().windowManager)
        return Size(displaySize.width / 2, displaySize.height)
    }
}