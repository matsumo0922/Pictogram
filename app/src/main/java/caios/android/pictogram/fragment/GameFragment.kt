package caios.android.pictogram.fragment

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.util.Size
import android.view.OrientationEventListener
import android.view.Surface
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.annotation.DrawableRes
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import caios.android.pictogram.R
import caios.android.pictogram.activity.MainActivity
import caios.android.pictogram.analyze.PictogramComparator
import caios.android.pictogram.analyze.PostureEstimator
import caios.android.pictogram.data.*
import caios.android.pictogram.databinding.FragmentGameBinding
import caios.android.pictogram.dialog.CountdownDialog
import caios.android.pictogram.dialog.ErrorDialog
import caios.android.pictogram.global.SettingClass
import caios.android.pictogram.global.ranking
import caios.android.pictogram.global.setting
import caios.android.pictogram.utils.LogUtils
import caios.android.pictogram.utils.PermissionUtils
import caios.android.pictogram.utils.autoCleared
import caios.android.pictogram.view.DebugSurfaceView
import caios.android.pictogram.view.PostureSurfaceView
import caios.android.pictogram.view.ResultSurfaceView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.transition.MaterialSharedAxis
import com.google.common.util.concurrent.ListenableFuture
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class GameFragment: Fragment(R.layout.fragment_game) {

    private var binding by autoCleared<FragmentGameBinding>()
    private val handler = Handler(Looper.myLooper()!!)

    private lateinit var postureSurfaceView: ResultSurfaceView
    private lateinit var pictogramComparator: PictogramComparator

    private lateinit var cameraProviderFuture: ListenableFuture<ProcessCameraProvider>
    private lateinit var cameraExecutor: ExecutorService

    private var gameTurn = 1
    private val gameMaxTurn = 5
    private var gameEventList = mutableListOf<PictogramEvent>()
    private var gameTime = 0.0f

    private var isShouldTest = true
    private var stopAnalyzeFlag = false

    private val timeProcess = object : Runnable {

        @SuppressLint("SetTextI18n")
        override fun run() {
            gameTime += 0.01f
            binding.themeSubText.text = "${getString(R.string.elapsedTime)}: %.2f".format(gameTime)

            handler.postDelayed(this, 10)
        }
    }

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
        postureSurfaceView = if(setting.getBoolean("DebugMode", false)) DebugSurfaceView(binding.surfaceView) else PostureSurfaceView(binding.surfaceView)

        initGame()
        setTest()

        if(PermissionUtils.isAllowed(requireContext(), PermissionUtils.requestPermissions)) setupCamera()
        else findNavController().navigateUp()

        (activity as MainActivity).onBackPressedDispatcher.addCallback(object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                MaterialAlertDialogBuilder(requireContext()).apply {
                    setTitle(R.string.caution)
                    setMessage(R.string.cancelGameMessage)
                    setPositiveButton(R.string.stopGame) { _, _ ->
                        stopAnalyzeFlag = true
                        handler.removeCallbacks(timeProcess)

                        findNavController().popBackStack()
                    }
                    setNegativeButton(R.string.continueGame, null)
                }.show()
            }
        })
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }

    private fun initGame() {
        val eventList = enumValues<PictogramEvent>().toMutableList().filter { it !in pictogramEventDisables }
        val randomEventList = eventList.shuffled().take(gameMaxTurn)

        gameEventList.clear()
        gameEventList.addAll(randomEventList)
    }

    private fun setTurn(turn: Int) {
        try {
            if (gameMaxTurn >= turn) {
                gameTurn = turn

                val turnEvent = gameEventList[turn - 1]

                pictogramComparator = PictogramComparator(requireContext(), turnEvent)

                binding.themeSportsText.text = getEventName(turnEvent)
                binding.pictogramImage.setImageResource(getEventPictogram(turnEvent))
            } else {
                val ranking = ranking.setRanking(gameTime, Calendar.getInstance().time.time) ?: -1

                findNavController().navigate(GameFragmentDirections.actionGameFragmentToResultFragment(gameTime, ranking))
                handler.removeCallbacks(timeProcess)
            }
        } catch (e: Throwable) {
            handler.post { onError(e) }
        }
    }

    private fun setTest() {
        isShouldTest = true

        binding.themeSportsText.text = getString(R.string.adjustment)

        binding.themeSubText.visibility = View.GONE
        binding.pictogramImage.visibility = View.GONE
        binding.testText.visibility = View.VISIBLE
    }

    private fun setTestResult(keyPoints: List<KeyPoint>) {
        if ((setting.getBoolean("DebugMode", false) || keyPoints.size == enumValues<BodyPart>().size) && childFragmentManager.findFragmentByTag("CountdownDialog") == null) {
            handler.post {
                CountdownDialog.build(3) {
                    setTurn(gameTurn)

                    isShouldTest = false

                    handler.post(timeProcess)

                    binding.themeSubText.visibility = View.VISIBLE
                    binding.pictogramImage.visibility = View.VISIBLE
                    binding.testText.visibility = View.GONE
                }.show(childFragmentManager, "CountdownDialog")
            }
        }
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

        val mlModel = Model.valueOf(setting.getString(SettingClass.ML_MODEL, Model.MOVENET_LIGHTNING.name))
        val processingMethod = Device.valueOf(setting.getString(SettingClass.PROCESSING_METHOD, Device.CPU.name))
        val previewSize = Size(binding.surfaceView.width, binding.surfaceView.height)

        val estimationListener = object : PostureEstimator.EstimationListener {
            override fun onSuccess(posture: PostureData, bitmap: Bitmap, time: Long) {
                val drawKeyPoint = postureSurfaceView.drawPosture(posture, bitmap, previewSize, time)

                when {
                    stopAnalyzeFlag -> Unit
                    isShouldTest    -> setTestResult(drawKeyPoint)
                    else            -> frameUpdate(pictogramComparator.comparate(drawKeyPoint, previewSize))
                }
            }

            override fun onError(e: Throwable) {
                handler.post { onError(e) }
            }
        }

        val preview = if(setting.getBoolean("PreviewCamera", true)) {
            Preview.Builder().build().also {
                it.setSurfaceProvider(binding.previewView.surfaceProvider)
            }
        } else {
            binding.previewView.visibility = View.GONE
            null
        }

        val selector = CameraSelector.Builder().apply {
            requireLensFacing(CameraSelector.LENS_FACING_FRONT)
        }.build()

        val imageAnalyzer = ImageAnalysis.Builder().apply {
            setTargetRotation(binding.previewView.display.rotation)
            setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
        }.build().also {
            it.setAnalyzer(cameraExecutor, PostureEstimator(requireContext(), mlModel, processingMethod, estimationListener))
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

                preview?.targetRotation = rotation
                imageAnalyzer.targetRotation = rotation
            }
        }

        orientationEventListener.enable()

        try {
            cameraProvider.unbindAll()

            if(preview != null) cameraProvider.bindToLifecycle(this, selector, preview, imageAnalyzer)
            else cameraProvider.bindToLifecycle(this, selector, imageAnalyzer)
        } catch (e: Throwable) {
            onError(e)
        }
    }

    @SuppressLint("SetTextI18n")
    private fun frameUpdate(score: Float) {
        if (score < THRESHOLD_DEGREE_MATCHES) {
            handler.post {
                stopAnalyzeFlag = true
                binding.clearImage.visibility = View.VISIBLE

                handler.postDelayed({
                    binding.clearImage.visibility = View.GONE
                    setTurn(gameTurn + 1)
                }, 1000)

                handler.postDelayed({
                    stopAnalyzeFlag = false
                }, 3000)
            }
        }
    }

    private fun onError(e: Throwable) {
        e.printStackTrace()

        stopAnalyzeFlag = true
        handler.removeCallbacks(timeProcess)

        ErrorDialog.build(e).show(parentFragmentManager, null)
        findNavController().popBackStack()
    }

    private fun getEventName(event: PictogramEvent): String {
        return when(event) {
            PictogramEvent.ARCHERY             -> getString(R.string.archery)
            PictogramEvent.WEIGHTLIFTING       -> getString(R.string.weightlifting)
            PictogramEvent.VOLLEYBALL          -> getString(R.string.volleyball)
            PictogramEvent.TENNIS              -> getString(R.string.tennis)
            PictogramEvent.ATHLETICS           -> getString(R.string.athletics)
            PictogramEvent.BADMINTON           -> getString(R.string.badminton)
            PictogramEvent.BASKETBALL          -> getString(R.string.basketball)
            PictogramEvent.BEACH_VOLLEYBALL    -> getString(R.string.beachVolleyball)
            PictogramEvent.BOXING              -> getString(R.string.boxing)
            PictogramEvent.CYCLING             -> getString(R.string.cycling)
            PictogramEvent.DIVING              -> getString(R.string.diving)
            PictogramEvent.FENCING             -> getString(R.string.fencing)
            PictogramEvent.FOOTBALL            -> getString(R.string.football)
            PictogramEvent.GOLF                -> getString(R.string.golf)
            PictogramEvent.HANDBALL            -> getString(R.string.handball)
            PictogramEvent.HOCKEY              -> getString(R.string.hockey)
            PictogramEvent.RHYTHMIC_GYMNASTICS -> getString(R.string.rhythmicGymnastics)
            PictogramEvent.RUGBY               -> getString(R.string.rugby)
            PictogramEvent.SHOOTING            -> getString(R.string.shooting)
            PictogramEvent.TABLE_TENNIS        -> getString(R.string.tableTennis)
            PictogramEvent.TAEKWONDO           -> getString(R.string.taekwondo)
            PictogramEvent.WRESTLING           -> getString(R.string.wrestling)
        }
    }

    @DrawableRes
    private fun getEventPictogram(event: PictogramEvent): Int {
        return when(event) {
            PictogramEvent.ARCHERY             -> R.drawable.vec_archery
            PictogramEvent.WEIGHTLIFTING       -> R.drawable.vec_weightlifting
            PictogramEvent.VOLLEYBALL          -> R.drawable.vec_volleyball
            PictogramEvent.TENNIS              -> R.drawable.vec_tennis
            PictogramEvent.ATHLETICS           -> R.drawable.vec_athletics
            PictogramEvent.BADMINTON           -> R.drawable.vec_badminton
            PictogramEvent.BASKETBALL          -> R.drawable.vec_basketball
            PictogramEvent.BEACH_VOLLEYBALL    -> R.drawable.vec_beach_volleyball
            PictogramEvent.BOXING              -> R.drawable.vec_boxing
            PictogramEvent.CYCLING             -> R.drawable.vec_cycling_road
            PictogramEvent.DIVING              -> R.drawable.vec_diving
            PictogramEvent.FENCING             -> R.drawable.vec_fencing
            PictogramEvent.FOOTBALL            -> R.drawable.vec_football
            PictogramEvent.GOLF                -> R.drawable.vec_golf
            PictogramEvent.HANDBALL            -> R.drawable.vec_handball
            PictogramEvent.HOCKEY              -> R.drawable.vec_hockey
            PictogramEvent.RHYTHMIC_GYMNASTICS -> R.drawable.vec_rhythmic_gymnastics
            PictogramEvent.RUGBY               -> R.drawable.vec_rugby_sevens
            PictogramEvent.SHOOTING            -> R.drawable.vec_shooting
            PictogramEvent.TABLE_TENNIS        -> R.drawable.vec_table_tennis
            PictogramEvent.TAEKWONDO           -> R.drawable.vec_taekwondo
            PictogramEvent.WRESTLING           -> R.drawable.vec_weightlifting
        }
    }
}