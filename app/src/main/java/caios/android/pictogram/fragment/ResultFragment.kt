package caios.android.pictogram.fragment

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import caios.android.pictogram.R
import caios.android.pictogram.activity.MainActivity
import caios.android.pictogram.databinding.FragmentResultBinding
import caios.android.pictogram.utils.autoCleared
import com.google.android.material.transition.MaterialSharedAxis
import nl.dionsegijn.konfetti.emitters.StreamEmitter
import nl.dionsegijn.konfetti.models.Shape
import nl.dionsegijn.konfetti.models.Size
import kotlin.random.Random

class ResultFragment: Fragment(R.layout.fragment_result) {

    private var binding by autoCleared<FragmentResultBinding>()
    private val args by navArgs<ResultFragmentArgs>()

    private val handler = Handler(Looper.myLooper()!!)
    private val random = Random(System.currentTimeMillis())

    private val confettiProcess = object : Runnable {
        override fun run() {
            val x = binding.confettiView.x.toInt() + (random.nextFloat() * binding.confettiView.width)
            val y = binding.confettiView.y.toInt() + (random.nextFloat() * binding.confettiView.height)
            confettiBurst(x, y)
            handler.postDelayed(this, 750)
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
        reenterTransition = returnSharedAxisTransitions
    }

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding = FragmentResultBinding.bind(view)

        binding.timeText.text = "${getString(R.string.clearTime)}: %.2f ${getString(R.string.second)}".format(args.time)
        binding.rankingText.text = if(args.rank != -1) "${getString(R.string.ranking)}: ${args.rank} ${getString(R.string.rank)}" else getString(R.string.failedToRegisterRanking)

        binding.endButton.setOnClickListener {
            findNavController().navigate(R.id.action_resultFragment_to_startingFragment)
        }

        (activity as MainActivity).onBackPressedDispatcher.addCallback(object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                findNavController().navigate(R.id.action_resultFragment_to_startingFragment)
            }
        })
    }

    override fun onResume() {
        super.onResume()
        handler.post(confettiProcess)
    }

    override fun onPause() {
        super.onPause()
        handler.removeCallbacks(confettiProcess)
    }

    private fun confettiBurst(x: Float, y: Float) {
        binding.confettiView.build().apply {
            addColors(Color.YELLOW, Color.GREEN, Color.MAGENTA, Color.BLUE)
            setDirection(0.0, 359.0)
            setSpeed(0.5f, 4f)
            setFadeOutEnabled(true)
            setTimeToLive(2000L)
            addShapes(Shape.RECT, Shape.CIRCLE)
            addSizes(Size(12))
            setPosition(x, y)
        }.burst(150)
    }
}