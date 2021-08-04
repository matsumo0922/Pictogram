package caios.android.pictogram.fragment

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.util.Size
import android.view.OrientationEventListener
import android.view.Surface
import android.view.View
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
import caios.android.pictogram.analyze.*
import caios.android.pictogram.analyze.PictogramEvent.*
import caios.android.pictogram.databinding.FragmentGameBinding
import caios.android.pictogram.dialog.CountdownDialog
import caios.android.pictogram.utils.LogUtils.TAG
import caios.android.pictogram.utils.PermissionUtils
import caios.android.pictogram.utils.ToastUtils
import caios.android.pictogram.utils.autoCleared
import com.google.android.material.transition.MaterialSharedAxis
import com.google.common.util.concurrent.ListenableFuture
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class GameFragment: Fragment(R.layout.fragment_game) {

    private var binding by autoCleared<FragmentGameBinding>()
    private val handler = Handler(Looper.myLooper()!!)

    private lateinit var postureSurfaceView: PostureSurfaceView
    private lateinit var pictogramComparator: PictogramComparator

    private lateinit var cameraProviderFuture: ListenableFuture<ProcessCameraProvider>
    private lateinit var cameraExecutor: ExecutorService

    private var gameTurn = 1
    private val gameMaxTurn = 3
    private var gameEventList = mutableListOf<PictogramEvent>()
    private var gameTimer = 0L
    private var gameLapTime = mutableListOf<Long>()

    private var isShouldTest = true
    private var stopAnalyzeFlag = false

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

        initGame()
        setTest()

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

    private fun initGame() {
        val randomEventList = listOf(WEIGHTLIFTING, ARCHERY, VOLLEYBALL).shuffled().take(gameMaxTurn)
        //val randomEventList = enumValues<PictogramEvent>().toMutableList().shuffled().take(gameTurnCount)

        gameEventList.clear()
        gameEventList.addAll(randomEventList)
    }

    private fun setTurn(turn: Int) {
        if (gameMaxTurn >= turn) {
            gameTurn = turn

            val turnEvent = gameEventList[turn - 1]

            pictogramComparator = PictogramComparator(requireContext(), turnEvent)

            binding.themeSportsText.text = getEventName(turnEvent)
            binding.pictogramImage.setImageResource(getEventPictogram(turnEvent))
        } else {
            ToastUtils.show(requireContext(), "GAME CLEAR!!")
            findNavController().popBackStack()
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
        if (keyPoints.size == enumValues<BodyPart>().size && childFragmentManager.findFragmentByTag("CountdownDialog") == null) {
            handler.post {
                CountdownDialog.build(3) {
                    isShouldTest = false

                    setTurn(gameTurn)

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
            it.setAnalyzer(cameraExecutor, PostureEstimator(requireContext(), Device.CPU) { posture, bitmap, time ->
                val drawKeyPoint = postureSurfaceView.drawPosture(posture, bitmap, previewSize, time)

                when {
                    stopAnalyzeFlag -> Unit
                    isShouldTest    -> setTestResult(drawKeyPoint)
                    else            -> frameUpdate(pictogramComparator.comparate(drawKeyPoint, previewSize))
                }
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

    @SuppressLint("SetTextI18n")
    private fun frameUpdate(score: Float) {
        if (score < THRESHOLD_DEGREE_MATCHES) {
            handler.post {
                stopAnalyzeFlag = true
                binding.clearImage.visibility = View.VISIBLE

                handler.postDelayed({
                    binding.clearImage.visibility = View.GONE
                    setTurn(gameTurn + 1)

                    stopAnalyzeFlag = false
                }, 1000)
            }
        }
    }

    private fun getEventName(event: PictogramEvent): String {
        return when(event) {
            ARCHERY             -> getString(R.string.archery)
            WEIGHTLIFTING       -> getString(R.string.weightlifting)
            VOLLEYBALL          -> getString(R.string.volleyball)
            TENNIS              -> getString(R.string.tennis)
            ATHLETICS           -> getString(R.string.athletics)
            BADMINTON           -> getString(R.string.badminton)
            BASKETBALL          -> getString(R.string.basketball)
            BEACH_VOLLEYBALL    -> getString(R.string.beachVolleyball)
            BOXING              -> getString(R.string.boxing)
            CYCLING             -> getString(R.string.cycring)
            DIVING              -> getString(R.string.diving)
            FENCING             -> getString(R.string.fencing)
            FOOTBALL            -> getString(R.string.football)
            GOLF                -> getString(R.string.golf)
            HANDBALL            -> getString(R.string.handball)
            HOCKEY              -> getString(R.string.hockey)
            RHYTHMIC_GYMNASTICS -> getString(R.string.rhythmicGymnastics)
            RUGBY               -> getString(R.string.rugby)
            SHOOTING            -> getString(R.string.shooting)
            TABLE_TENNIS        -> getString(R.string.tableTennis)
            TAEKWONDO           -> getString(R.string.taekwondo)
            WRESTLING           -> getString(R.string.wrestling)
        }
    }

    @DrawableRes
    private fun getEventPictogram(event: PictogramEvent): Int {
        return when(event) {
            ARCHERY             -> R.drawable.vec_archery
            WEIGHTLIFTING       -> R.drawable.vec_weightlifting
            VOLLEYBALL          -> R.drawable.vec_volleyball
            TENNIS              -> R.drawable.vec_tennis
            ATHLETICS           -> R.drawable.vec_athletics
            BADMINTON           -> R.drawable.vec_badminton
            BASKETBALL          -> R.drawable.vec_basketball
            BEACH_VOLLEYBALL    -> R.drawable.vec_beach_volleyball
            BOXING              -> R.drawable.vec_boxing
            CYCLING             -> R.drawable.vec_cycling_road
            DIVING              -> R.drawable.vec_diving
            FENCING             -> R.drawable.vec_fencing
            FOOTBALL            -> R.drawable.vec_football
            GOLF                -> R.drawable.vec_golf
            HANDBALL            -> R.drawable.vec_handball
            HOCKEY              -> R.drawable.vec_hockey
            RHYTHMIC_GYMNASTICS -> R.drawable.vec_rhythmic_gymnastics
            RUGBY               -> R.drawable.vec_rugby_sevens
            SHOOTING            -> R.drawable.vec_shooting
            TABLE_TENNIS        -> R.drawable.vec_table_tennis
            TAEKWONDO           -> R.drawable.vec_taekwondo
            WRESTLING           -> R.drawable.vec_weightlifting
        }
    }
}